#!/bin/bash
. /etc/sysconfig/cranix
if [ ${CRANIX_TYPE} = "cephalix" ]; then
	/usr/bin/systemctl stop cephalix-api
else
	/usr/bin/systemctl stop cranix-api
fi
sleep 10
echo "ALTER TABLE Sessions DROP FOREIGN KEY FK_Sessions_crx2fasession_id" | mariadb CRX
echo "ALTER TABLE Sessions DROP crx2fasession_id;" | mariadb CRX
echo "ALTER TABLE Sessions MODIFY validUntil timestamp;" | mariadb CRX
echo "ALTER TABLE Sessions MODIFY validFrom timestamp;" | mariadb CRX
echo "ALTER TABLE Crx2faSessions DROP FOREIGN KEY  FK_Sessions_session_id;" | mariadb CRX
echo "ALTER TABLE Crx2faSessions DROP session_id;" | mariadb CRX
echo "ALTER TABLE Users RENAME COLUMN telefonNumber TO telephoneNumber;" | mariadb CRX
echo "ALTER TABLE PositiveLists DROP subject;" | mariadb CRX
echo "update Users set role='sysadmins' where uid='cephalix';" | mariadb  CRX
sleep 10
if [ ${CRANIX_TYPE} = "cephalix" ]; then
	/usr/bin/systemctl start cephalix-api
else
	/usr/bin/systemctl start cranix-api
fi

