#!/bin/bash
#
DATE=$(/usr/share/cranix/tools/crx_date.sh)
BACKUP="/var/adm/cranix/backup/${DATE}"
mkdir -p ${BACKUP}

/usr/bin/systemctl stop cranix-api cron
mysqldump --databases CRX > ${BACKUP}/CRX.sql
password=$( gawk -F = '/javax.persistence.jdbc.password/ { print $2 }' /opt/cranix-java/conf/cranix-api.properties )
for i in $( echo  'SHOW TABLES' | mysql CRX  );
do
        mysqldump --xml CRX $i  > /tmp/$i.xml
        sed -i s/owner_id/creator_id/g /tmp/$i.xml;
done

echo "DROP DATABASE CRX" | mysql
echo "CREATE DATABASE CRX" | mysql
echo "grant all on CRX.* to 'cranix'@'localhost'  identified by '$password'" | mysql
/usr/bin/systemctl start cranix-api
sleep 5
/usr/sbin/crx_api.sh GET users/all
sleep 5
/usr/bin/systemctl stop cranix-api
for i in $( echo "SHOW TABLES" | mysql CRX);
do
        echo "SET GLOBAL FOREIGN_KEY_CHECKS=0; LOAD XML LOCAL INFILE '/tmp/${i}.xml' REPLACE INTO TABLE ${i}" | mysql CRX --show-warnings > /tmp/${i}.warning
done
echo "SET GLOBAL FOREIGN_KEY_CHECKS=1;" | mysql
/usr/bin/systemctl start cranix-api

