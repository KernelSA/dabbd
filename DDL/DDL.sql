CREATE TABLE public.dm_tracker (
	tracker_id varchar(50) NOT NULL,
	fuel int4 NULL,
	power int4 NULL,
	speed int4 NULL,
	latitude float8 NULL,
	longitude float8 NULL,
	gsm_signal int2 NULL,
	gps_satellites int2 NULL,
	online bool NULL,
	last_event_dt timestamp NULL,
	last_event_id int4 NOT NULL,
	ins_dt timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
	upd_dt timestamp NULL,
	CONSTRAINT dm_tracker_pkey PRIMARY KEY (tracker_id)
);


CREATE TABLE events (
	Event_id bigserial NOT NULL,
	Tracker_id varchar(50) NULL,
	Source_type varchar(50)  NULL,
	Event_dt timestamp NULL,
	Fuel int4 NULL,
	Power int4 NULL,
	Speed int4 NULL,
	Latitude double precision null,
	Longitude double precision NULL,
	GSM_Signal int2 NULL,
	GPS_Satellites int2 NULL,
	Profile_Updated boolean null default false,
	Kafka_TimeStamp timestamp null,
	Ins_dt timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP)
	partition by range (Event_dt) ;


CREATE TABLE public.log_triggers (
	trigger_id bigserial NOT NULL,
	tracker_id varchar(50) NULL,
	trigger_type varchar(20) NULL,
	trigger_dt timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
	event_dt timestamp NULL,
	trigger_info varchar(3000) NULL,
	CONSTRAINT log_triggers_pkey PRIMARY KEY (trigger_id)
);


CREATE INDEX log_triggers_trackerid_idx ON public.log_triggers (tracker_id);


alter sequence events_event_id_seq cycle;
alter sequence log_triggers_trigger_id_seq cycle;


CREATE OR REPLACE FUNCTION public.update_dm_tracker()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
declare
  v_lastUpdDt TimeStamp;
BEGIN
 select Last_Event_Dt into v_lastUpdDt
 from DM_TRACKER d
 where d.tracker_id = NEW.Tracker_ID;
 IF NOT FOUND THEN
    INSERT INTO public.dm_tracker
	(tracker_id, fuel, power, speed, longitude, latitude, gsm_signal, gps_satellites, online, last_event_dt, last_event_id, ins_dt)
	VALUES(new.tracker_id, new.fuel, new.power, new.speed, new.longitude, new.latitude, new.gsm_signal, new.gps_satellites, true,
		   new.Event_Dt, new.Event_ID, current_timestamp);
		  new.Profile_Updated = true;

 else
   if v_lastUpdDt < new.Event_Dt then
	UPDATE dm_tracker
	   SET fuel=new.fuel,
	   	   power=new.power,
	   	   speed=new.speed,
	   	   longitude=new.longitude,
	   	   latitude=new.latitude,
	   	   gsm_signal=new.gsm_signal,
	   	   gps_satellites=new.gps_satellites,
	   	   online=true,
	   	   last_event_dt=new.Event_Dt,
	   	   last_event_id=new.Event_ID,
	   	   upd_dt=current_timestamp
	WHERE tracker_id=new.tracker_id;
	new.Profile_Updated = true;

   end if;
 END IF;
 RETURN NEW;
END;
$function$
;


CREATE OR REPLACE FUNCTION public.create_indexes_on_partition(partition_name text)
 RETURNS void
 LANGUAGE plpgsql
AS $function$
begin
     execute 'CREATE INDEX IF NOT EXISTS '||partition_name||'_trackerid_eventdt_idx ON '||partition_name||' USING btree (tracker_id, event_dt)';
     execute 'CREATE INDEX IF NOT EXISTS '||partition_name||'_trackerid_idx ON '||partition_name||' USING btree (tracker_id)';
     execute 'CREATE UNIQUE INDEX '||partition_name||'_pkey ON '||partition_name||' USING btree (event_id)';
END;
$function$
;

CREATE OR REPLACE FUNCTION public.set_offline_tracker()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
BEGIN
  if new.trigger_type = 'SIGNAL_LOST' then
    update DM_TRACKER
      set online = false, upd_dt = current_timestamp
    where tracker_id = new.tracker_id;
  end if;
 return null;
END;
$function$
;

create trigger tr_set_offline_tracker after insert on log_triggers for each row execute procedure set_offline_tracker();



CREATE OR REPLACE FUNCTION public.create_partitions(p_start date, p_end date, p_interval interval)
 RETURNS void
 LANGUAGE plpgsql
AS $function$
declare
   cur cursor for
   select dt::date as start_dt,
   		  (dt+p_interval)::date as end_dt
	 from generate_series(p_start,p_end,p_interval) dt;
 rec record;
 partition_name TEXT;
 v_sql text;

begin
  open cur;
  loop
    fetch cur into rec;
    EXIT WHEN NOT FOUND;
    partition_name = 'events_'||TO_CHAR(rec.start_dt,'YYYYMMDD');
      IF NOT EXISTS(SELECT relname FROM pg_class where relname = partition_name) THEN
        v_sql = 'CREATE TABLE '||partition_name||' partition of events for values from ('''||rec.start_dt||''') to ('''||rec.end_dt||''')';
        execute v_sql;
        PERFORM create_indexes_on_partition(partition_name);
        execute 'create trigger tr_update_dm_tracker_'||partition_name||' before insert on '||partition_name||' for each row execute procedure update_dm_tracker()';
      end if;
  end loop;
end;
$function$
;


 CREATE OR REPLACE FUNCTION public.drop_partitions(table_name text, p_max_dt date)
 RETURNS void
 LANGUAGE plpgsql
AS $function$
declare
   cur cursor for
		select partition_name
		from
		  (
			SELECT
			    child.relname       AS partition_name,
			    to_date(substring(child.relname,position('_' in child.relname)+1,length(child.relname)),'YYYYMMDD') as part_dt
			FROM pg_inherits
			    JOIN pg_class parent ON pg_inherits.inhparent = parent.oid
			    JOIN pg_class child  ON pg_inherits.inhrelid   = child.oid
			WHERE parent.relname=table_name
			  and child.relname !=table_name||'_default'
		  ) as p
		  where part_dt < p_max_dt;
 rec record;
 v_sql text;

begin
  open cur;
  loop
    fetch cur into rec;
    EXIT WHEN NOT FOUND;
        v_sql = 'DROP TABLE '||rec.partition_name;
        execute v_sql;
  end loop;
end;
$function$
;

do
$$
declare
  start_dt date = '1970-01-01';
  end_dt date = current_date-1;
begin
  execute 'CREATE TABLE events_default partition of events for values from ('''||start_dt||''') to ('''||end_dt||''')';
  execute 'CREATE INDEX IF NOT EXISTS events_default_trackerid_eventdt_idx ON events_default USING btree (tracker_id, event_dt)';
  execute 'CREATE INDEX IF NOT EXISTS events_default_trackerid_idx ON events_default USING btree (tracker_id)';
  execute 'CREATE UNIQUE INDEX events_default_pkey ON events_default USING btree (event_id)';
  execute 'create trigger tr_update_dm_tracker_events_default before insert on events_default for each row execute procedure update_dm_tracker()';
  perform create_partitions(current_date-1,current_date+31,'1 day');

end
$$;
