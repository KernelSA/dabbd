#!/bin/bash
scr_home=$1
if [[ -z "$scr_home" ]]; then
   scr_home=/opt/dabbd
fi 
read dbname <<< $(cat $scr_home/environment.cfg |grep db_name| awk '{print $2}')
read dbuser <<< $(cat $scr_home/environment.cfg |grep db_user| awk '{print $2;exit;}')
read dbpass <<< $(cat $scr_home/environment.cfg |grep db_password| awk '{print $2;exit;}')
read dbhost <<< $(cat $scr_home/environment.cfg |grep db_host| awk '{print $2}')
while read line ; do
 is_comment=$(echo $line |cut -c -1)
  if [ $is_comment != "#" ]; then
   IFS=":"
   set -- $line
   table=$1
   drop_partition=$(date --date="$2 days ago" +"%Y"-"%m"-"%d")
   new_partition=$(date -d "+$3 days" +"%Y"-"%m"-"%d")
   current_date=$(date +"%Y"-"%m"-"%d")
   PGPASSWORD=$dbpass psql --host $dbhost -U $dbuser $dbname -c "SELECT public.create_partitions('$current_date','$new_partition','$4');"
   PGPASSWORD=$dbpass psql --host $dbhost -U $dbuser $dbname -c "SELECT public.drop_partitions('$table','$drop_partition');"
  fi
done < $scr_home/table_part.cfg 

