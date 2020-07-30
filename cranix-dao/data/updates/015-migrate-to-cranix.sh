#!/bin/bash
if [ -e /var/adm/cranix/migrated-to-cranix ]; then
	exit 0
fi
export HOME="/root"
CRANIX=$( echo "show tables" | mysql CRX | grep CrxNextID )
if [ "${CRANIX}" ];  then
	exit 0
fi

DATE=$( date +%Y-%m-%d-%H-%M )
echo "INSERT INTO Enumerates VALUES(NULL,'apiAcl','hwconf.modify',6);" | mysql OSS
mysqldump --databases OSS | gzip > OSS-BACKUP-${DATE}.sql.gz

echo "RENAME TABLE OssNextID TO CrxNextID;"     | mysql OSS
echo "RENAME TABLE OSSConfig  TO CrxConfig"     | mysql OSS
echo "RENAME TABLE OSSMConfig TO CrxMConfig"    | mysql OSS
echo "RENAME TABLE OssResponses TO CrxResponse" | mysql OSS
mysqldump --databases OSS > CRX.sql
sed -i '1,26s/OSS/CRX/' CRX.sql
mysql < CRX.sql
password=$( mktemp cranixXXXXXXXXXXXX )
sed -i s/javax.persistence.jdbc.password=.*$/javax.persistence.jdbc.password=${password}/ /opt/cranix-java/conf/cranix-api.properties
sed -i 's/=claxss/=cranix/' /opt/cranix-java/conf/cranix-api.properties
echo "grant all on CRX.* to 'cranix'@'localhost'  identified by '$password'" | mysql
mkdir -p /var/adm/cranix/
touch /var/adm/cranix/migrated-to-cranix
