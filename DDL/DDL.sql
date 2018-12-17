CREATE TABLE dm_tracker (
	Tracker_id varchar(50) NOT NULL,
	Fuel int4 NULL,
	Power int4 NULL,
	Speed int4 NULL,
	Latitude double precision null,
	Longitude double precision NULL,
	GSM_Signal int2 NULL,
	GPS_Satellites int2 NULL,
	Online boolean NULL,
	Last_event_dt timestamp NULL,
	last_event_id int4 NOT NULL,
	Ins_dt timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
	Upd_dt timestamp NULL,
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
	Ins_dt timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT events_pkey PRIMARY KEY (event_id)
);
CREATE INDEX events_trackerid_eventdt_idx ON events USING btree (tracker_id, event_dt);
CREATE INDEX events_trackerid_idx ON events USING btree (tracker_id);

CREATE TABLE log_triggers (
	Trigger_ID bigserial NOT NULL,
	Tracker_ID varchar(50) NULL,
	Trigger_Type varchar(20) NULL,
	Trigger_DT timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
	Event_DT timestamp NULL,
	CONSTRAINT log_triggers_pkey PRIMARY KEY (trigger_id)
);
CREATE INDEX log_triggers_trackerid_idx ON public.log_triggers USING btree (tracker_id);

alter sequence events_event_id_seq cycle;
alter sequence log_triggers_trigger_id_seq cycle;

CREATE OR REPLACE FUNCTION update_dm_tracker()
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
$function$;

create trigger tr_update_dm_tracker before insert
on events for each row execute procedure update_dm_tracker();
