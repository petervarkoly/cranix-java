#!/bin/bash
if [ -e /var/adm/cranix/adapt-cephalix-to-cranix ]; then
	exit 0
fi
export HOME="/root"
CRANIX=$( echo "show tables" | mysql CRX | grep CrxNextID )
if [ "${CRANIX}" ];  then
	exit 0
fi

TYPE=$( echo "describe CephalixInstitutes" | mysql CRX | grep type | gawk '{ print $2 }'  )
if [ "$TYPE" ]
then
	echo "alter table CephalixInstitutes change COLUMN type instituteType VARCHAR(16) default 'school';" | mysql CRX
fi

NETWORK=$( echo "describe CephalixInstitutes" | mysql CRX | grep network | gawk '{ print $2 }'  )
if [ "$TYPE" ]
then
	echo "alter table CephalixInstitutes change COLUMN network internalNetwork VARCHAR(32) default NULL;" | mysql CRX
fi

STATUS=$( echo "describe CephalixInstitutes" | mysql CRX | grep status | gawk '{ print $2 }'  )
if [ "$TYPE" ]
then
	echo "alter table CephalixInstitutes change COLUMN status instituteStatus enum('PROD','TEST','FREE','DEVEL');" | mysql CRX
fi

NETWOR=$( echo "describe CephalixInstitutes" | mysql CRX | grep network | gawk '{ print $2 }'  )
if [ "$NETWORK" ]
then
	echo "alter table CephalixInstitutes change COLUMN netowrk internalNetwork VARCHAR(32) default NULL;" | mysql CRX
fi

TYPE=$( echo "describe CephalixNotices" | mysql CRX | grep type | gawk '{ print $2 }'  )
if [ "$TYPE" ]
then
	echo "alter table CephalixNotices change COLUMN type noticeType VARCHAR(16);" | mysql CRX
fi

DONE=$( echo "describe CephalixTickets" | mysql CRX | grep done | gawk '{ print $2 }'  )
if [ "$DONE" ]
then
	echo "alter table CephalixTickets change COLUMN done ticketStatus char(1) default 'N';" | mysql CRX
fi
STATUS=$( echo "describe CephalixTickets" | mysql CRX | grep status | gawk '{ print $2 }'  )
if [ "$STATUS" ]
then
	echo "alter table CephalixTickets change COLUMN status ticketStatus CHAR(1) DEFAULT 'N';" | mysql CRX
fi

TYPE=$( echo "describe CephalixTickets" | mysql CRX | grep type | gawk '{ print $2 }'  )
if [ "$TYPE" ]
then
	echo "alter table CephalixTickets change COLUMN type ticketType VARCHAR(16) default 'Error';" | mysql CRX
fi

TYPE=$( echo "describe CephalixArticles" | mysql CRX | grep type | gawk '{ print $2 }'  )
if [ "$TYPE" ]
then
	echo "alter table CephalixArticles change COLUMN type articleType CHAR(1) DEFAULT 'I';" | mysql CRX
fi

TYPE=$( echo "describe CephalixRepositories" | mysql CRX | grep type | gawk '{ print $2 }'  )
if [ "$TYPE" ]
then
	echo "alter table CephalixRepositories change COLUMN type repositoryType VARCHAR(16);" | mysql CRX
fi

TYPE=$( echo "describe CephalixOssCareMessages" | mysql CRX | grep type | gawk '{ print $2 }'  )
if [ "$TYPE" ]
then
	echo "alter table CephalixOssCareMessages change COLUMN type careMessageType VARCHAR(16);" | mysql CRX
fi

echo "ALTER TABLE CephalixSystemStatus ADD COLUMN IF NOT EXISTS recDate DATETIME DEFAULT NOW() AFTER lastUpdate;" | mysql CRX
echo "ALTER TABLE CephalixSystemStatus MODIFY lastUpdate DATETIME DEFAULT NOW() on update current_timestamp();" | mysql CRX
echo "DELETE FROM Enumerates WHERE name = 'noticeType'; " | mysql CRX
echo "INSERT INTO Enumerates VALUES(NULL,'noticeType','access',1);" | mysql CRX
echo "INSERT INTO Enumerates VALUES(NULL,'noticeType','todo',1);" | mysql CRX
echo "INSERT INTO Enumerates VALUES(NULL,'noticeType','work',1);" | mysql CRX
echo "INSERT INTO Enumerates VALUES(NULL,'noticeType','other',1);" | mysql CRX
echo "ALTER TABLE CephalixSystemStatus CHANGE recDate created datetime not null default now();" | mysql CRX
echo "ALTER TABLE CephalixSystemStatus CHANGE lastUpdate lastUpdate datetime not null default now();" | mysql CRX
echo "UPDATE CephalixInstitutes set instituteType='globalSchool'    where instituteType='global';" | mysql CRX
echo "UPDATE CephalixInstitutes set instituteType='primarySchool'   where instituteType='primary';" | mysql CRX
echo "UPDATE CephalixInstitutes set instituteType='realSchool'      where instituteType='real';" | mysql CRX
echo "UPDATE CephalixInstitutes set instituteType='secondarySchool' where instituteType='secondary_school';" | mysql CRX
echo "UPDATE CephalixInstitutes set instituteType='workSchool'      where instituteType='work';" | mysql CRX
echo "DELETE FROM Enumerates WHERE name = 'instituteType'; " | mysql CRX
echo "INSERT INTO Enumerates VALUES(NULL,'instituteType','workSchool',1); " | mysql CRX
echo "INSERT INTO Enumerates VALUES(NULL,'instituteType','globalSchool',1); " | mysql CRX
echo "INSERT INTO Enumerates VALUES(NULL,'instituteType','primarySchool',1); " | mysql CRX
echo "INSERT INTO Enumerates VALUES(NULL,'instituteType','gymnasium',1); " | mysql CRX
echo "INSERT INTO Enumerates VALUES(NULL,'instituteType','secondary',1); " | mysql CRX
echo "INSERT INTO Enumerates VALUES(NULL,'instituteType','secondarySchool',1); " | mysql CRX
echo "INSERT INTO Enumerates VALUES(NULL,'instituteType','administration',1); " | mysql CRX
echo "INSERT INTO Enumerates VALUES(NULL,'instituteType','realSchool',1); " | mysql CRX
echo "INSERT INTO Enumerates VALUES(NULL,'instituteType','special',1); " | mysql CRX
echo "INSERT INTO Enumerates VALUES(NULL,'instituteType','other',1); " | mysql CRX
echo "ALTER TABLE CephalixMappings change COLUMN ossId cranixId BIGINT UNSIGNED DEFAULT NULL;" | mysql CRX
echo "ALTER TABLE CephalixTickets  change COLUMN ossuserId cranixuserId BIGINT UNSIGNED DEFAULT NULL;" | mysql CRX

#Adapt the tepmaltes too
for i in /usr/share/cephalix/templates/autoyast-*xml
do
	sed -i 's/###network###/###internalNetwork###/' $i
	sed -i 's/###type###/###instituteType###/' $i
done

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;" | mysql CRX

mkdir -p  /var/adm/cranix/
touch /var/adm/cranix/adapt-cephalix-to-cranix
