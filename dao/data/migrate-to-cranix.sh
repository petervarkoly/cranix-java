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
mysqldump --databases OSS | gzip > OSS-BACKUP-${DATE}.sql.gz
TYPE=$( echo "describe CephalixInstitutes" | mysql OSS | grep type | gawk '{ print $2 }'  )
if [ "$TYPE" ]
then
	echo "alter table CephalixInstitutes change COLUMN type instituteType VARCHAR(16) default 'school';" | mysql OSS
fi

NETWORK=$( echo "describe CephalixInstitutes" | mysql OSS | grep network | gawk '{ print $2 }'  )
if [ "$TYPE" ]
then
	echo "alter table CephalixInstitutes change COLUMN network internalNetwork VARCHAR(32) default NULL;" | mysql OSS
fi

STATUS=$( echo "describe CephalixInstitutes" | mysql OSS | grep status | gawk '{ print $2 }'  )
if [ "$TYPE" ]
then
	echo "alter table CephalixInstitutes change COLUMN status instituteStatus enum('PROD','TEST','FREE','DEVEL');" | mysql OSS
fi

NETWOR=$( echo "describe CephalixInstitutes" | mysql OSS | grep network | gawk '{ print $2 }'  )
if [ "$NETWORK" ]
then
	echo "alter table CephalixInstitutes change COLUMN netowrk internalNetwork VARCHAR(32) default NULL;" | mysql OSS
fi

TYPE=$( echo "describe CephalixNotices" | mysql OSS | grep type | gawk '{ print $2 }'  )
if [ "$TYPE" ]
then
	echo "alter table CephalixNotices change COLUMN type noticeType VARCHAR(16);" | mysql OSS
fi

DONE=$( echo "describe CephalixTickets" | mysql OSS | grep done | gawk '{ print $2 }'  )
if [ "$DONE" ]
then
	echo "alter table CephalixTickets change COLUMN done ticketStatus char(1) default 'N';" | mysql OSS
fi
STATUS=$( echo "describe CephalixTickets" | mysql OSS | grep status | gawk '{ print $2 }'  )
if [ "$STATUS" ]
then
	echo "alter table CephalixTickets change COLUMN status ticketStatus CHAR(1) DEFAULT 'N';" | mysql OSS
fi

TYPE=$( echo "describe CephalixTickets" | mysql OSS | grep type | gawk '{ print $2 }'  )
if [ "$TYPE" ]
then
	echo "alter table CephalixTickets change COLUMN type ticketType VARCHAR(16) default 'Error';" | mysql OSS
fi

TYPE=$( echo "describe CephalixArticles" | mysql OSS | grep type | gawk '{ print $2 }'  )
if [ "$TYPE" ]
then
	echo "alter table CephalixArticles change COLUMN type articleType CHAR(1) DEFAULT 'I';" | mysql OSS
fi

TYPE=$( echo "describe CephalixRepositories" | mysql OSS | grep type | gawk '{ print $2 }'  )
if [ "$TYPE" ]
then
	echo "alter table CephalixRepositories change COLUMN type repositoryType VARCHAR(16);" | mysql OSS
fi

TYPE=$( echo "describe CephalixOssCareMessages" | mysql OSS | grep type | gawk '{ print $2 }'  )
if [ "$TYPE" ]
then
	echo "alter table CephalixOssCareMessages change COLUMN type careMessageType VARCHAR(16);" | mysql OSS
fi

echo "ALTER TABLE CephalixSystemStatus ADD COLUMN IF NOT EXISTS recDate DATETIME DEFAULT NOW() AFTER lastUpdate;" | mysql OSS
echo "ALTER TABLE CephalixSystemStatus MODIFY lastUpdate DATETIME DEFAULT NOW() on update current_timestamp();" | mysql OSS
echo "DELETE FROM Enumerates WHERE name = 'noticeType'; " | mysql OSS
echo "INSERT INTO Enumerates VALUES(NULL,'noticeType','access',1);" | mysql OSS
echo "INSERT INTO Enumerates VALUES(NULL,'noticeType','todo',1);" | mysql OSS
echo "INSERT INTO Enumerates VALUES(NULL,'noticeType','work',1);" | mysql OSS
echo "INSERT INTO Enumerates VALUES(NULL,'noticeType','other',1);" | mysql OSS
echo "ALTER TABLE CephalixSystemStatus CHANGE recDate created datetime not null default now();" | mysql OSS
echo "ALTER TABLE CephalixSystemStatus CHANGE lastUpdate lastUpdate datetime not null default now();" | mysql OSS
echo "UPDATE CephalixInstitutes set instituteType='globalSchool'    where instituteType='global';" | mysql OSS
echo "UPDATE CephalixInstitutes set instituteType='primarySchool'   where instituteType='primary';" | mysql OSS
echo "UPDATE CephalixInstitutes set instituteType='realSchool'      where instituteType='real';" | mysql OSS
echo "UPDATE CephalixInstitutes set instituteType='secondarySchool' where instituteType='secondary_school';" | mysql OSS
echo "UPDATE CephalixInstitutes set instituteType='workSchool'      where instituteType='work';" | mysql OSS
echo "DELETE FROM Enumerates WHERE name = 'instituteType'; " | mysql OSS
echo "INSERT INTO Enumerates VALUES(NULL,'instituteType','workSchool',1); " | mysql OSS
echo "INSERT INTO Enumerates VALUES(NULL,'instituteType','globalSchool',1); " | mysql OSS
echo "INSERT INTO Enumerates VALUES(NULL,'instituteType','primarySchool',1); " | mysql OSS
echo "INSERT INTO Enumerates VALUES(NULL,'instituteType','gymnasium',1); " | mysql OSS
echo "INSERT INTO Enumerates VALUES(NULL,'instituteType','secondary',1); " | mysql OSS
echo "INSERT INTO Enumerates VALUES(NULL,'instituteType','secondarySchool',1); " | mysql OSS
echo "INSERT INTO Enumerates VALUES(NULL,'instituteType','administration',1); " | mysql OSS
echo "INSERT INTO Enumerates VALUES(NULL,'instituteType','realSchool',1); " | mysql OSS
echo "INSERT INTO Enumerates VALUES(NULL,'instituteType','special',1); " | mysql OSS
echo "INSERT INTO Enumerates VALUES(NULL,'instituteType','other',1); " | mysql OSS
echo "ALTER TABLE CephalixMappings change COLUMN ossId cranixId BIGINT UNSIGNED DEFAULT NULL;" | mysql OSS
echo "ALTER TABLE CephalixTickets  change COLUMN ossuserId cranixuserId BIGINT UNSIGNED DEFAULT NULL;" | mysql OSS

#Adapt the tepmaltes too
for i in /usr/share/cephalix/templates/autoyast-*xml
do
	sed -i 's/###network###/###internalNetwork###/' $i
	sed -i 's/###type###/###instituteType###/' $i
done

echo "INSERT INTO Enumerates VALUES(NULL,'apiAcl','hwconf.modify',6);" | mysql OSS

echo "RENAME TABLE OssNextID TO CrxNextID;"     | mysql OSS
echo "RENAME TABLE OSSConfig  TO CrxConfig"     | mysql OSS
echo "RENAME TABLE OSSMConfig TO CrxMConfig"    | mysql OSS
echo "RENAME TABLE OssResponses TO CrxResponse" | mysql OSS
echo "RENAME TABLE CephalixOssCares TO CephalixCares" | mysql OSS
echo "DROP TABLE CephalixOssCareMessages" | mysql OSS
echo "CREATE TABLE IF NOT EXISTS CephalixCareMessages (
        id       BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
        cephalixcare_id BIGINT UNSIGNED NOT NULL,
        recDate  DATETIME NOT NULL DEFAULT NOW(),
        type     enum('WARNING','REPORT') NOT NULL,
        description VARCHAR(128) NOT NULL,
        text     TEXT    NOT NULL,
        FOREIGN KEY(cephalixcare_id) REFERENCES CephalixCares(id) ON DELETE CASCADE,
        PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;
" | OSS
mysqldump --databases OSS > CRX.sql
sed -i '1,26s/OSS/CRX/' CRX.sql
mysql < CRX.sql
password=$( mktemp cranixXXXXXXXXXXXX )
sed -i s/javax.persistence.jdbc.password=.*$/javax.persistence.jdbc.password=${password}/ /opt/cranix-java/conf/cranix-api.properties
sed -i 's/=claxss/=cranix/' /opt/cranix-java/conf/cranix-api.properties
echo "grant all on CRX.* to 'cranix'@'localhost'  identified by '$password'" | mysql
echo "drop table CephalixOssCareMessages;" | mysql CRX
echo "drop table CephalixOssCares;" | mysql CRX
echo "CREATE TABLE IF NOT EXISTS CephalixCares (
        id       BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
        cephalixinstitute_id BIGINT UNSIGNED NOT NULL,
        description VARCHAR(1024) DEFAULT NULL,
        access      VARCHAR(1024) NOT NULL,
        contact     VARCHAR(256) NOT NULL,
        recDate     DATETIME NOT NULL DEFAULT NOW(),
        validity    DATETIME NOT NULL DEFAULT NOW(),
        FOREIGN KEY(cephalixinstitute_id) REFERENCES CephalixInstitutes(id) ON DELETE CASCADE,
        PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;

CREATE TABLE IF NOT EXISTS CephalixCareMessages (
        id       BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
        cephalixcare_id BIGINT UNSIGNED NOT NULL,
        recDate  DATETIME NOT NULL DEFAULT NOW(),
        type     enum('WARNING','REPORT') NOT NULL,
        description VARCHAR(128) NOT NULL,
        text     TEXT    NOT NULL,
        FOREIGN KEY(cephalixcare_id) REFERENCES CephalixCares(id) ON DELETE CASCADE,
        PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;" | | mysql CRX

touch /var/adm/cranix/migrated-to-cranix
