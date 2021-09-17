#!/bin/bash

. /etc/sysconfig/cranix
#Avoid creating identical partitions
PARTINDX=$( echo "SELECT * FROM information_schema.statistics WHERE table_schema = 'CRX' AND table_name = 'Partitions' AND column_name = 'name';" | mysql CRX )
if [ -z "$PARTINDX" ]; then
	echo "CREATE UNIQUE INDEX partitions on Partitions(hwconf_id,name);"   | mysql CRX
fi
#Dropp badly created index
echo "DROP INDEX partitions on Categories;" | mysql CRX &> /dev/null;
#Avoid creating identical categories
CATINDX=$( echo "SELECT * FROM information_schema.statistics WHERE table_schema = 'CRX' AND table_name = 'Categories' AND column_name = 'name';" | mysql CRX )
if [ -z "$CATINDX" ]; then
	echo "CREATE UNIQUE INDEX categories on Categories(name,categoryType);" | mysql CRX
fi
#Avoid creating identical software packages
SOFTINDX=$( echo "SELECT * FROM information_schema.statistics WHERE table_schema = 'CRX' AND table_name = 'Softwares' AND column_name = 'name';" | mysql CRX )
if [ -z "$SOFTINDX" ]; then
	echo "CREATE UNIQUE INDEX softwares_name on Softwares(name);" | mysql CRX
fi
#Avoid creating printer name duplication
PRINTINDX=$( echo "SELECT * FROM information_schema.statistics WHERE table_schema = 'CRX' AND table_name = 'Printers' AND column_name = 'name';" | mysql CRX )
if [ -z "$PRINTINDX" ]; then
	echo "CREATE UNIQUE INDEX printers_name on Printers(name);" | mysql CRX
fi
#Avoid creating private devices with the same IP-Adrrsess
IPINDEX=$( echo "SELECT * FROM information_schema.statistics WHERE table_schema = 'CRX' AND table_name = 'Devices' AND column_name = 'IP';" | mysql CRX )
if [ -z "$IPINDEX" ]; then
        echo "DELETE FROM Devices WHERE IP = '${CRANIX_SERVER}' AND id > 1" | mysql CRX
        echo "CREATE UNIQUE INDEX device_ip on Devices(IP);" | mysql CRX
        IPINDEX=$( echo "SELECT * FROM information_schema.statistics WHERE table_schema = 'CRX' AND table_name = 'Devices' AND column_name = 'IP';" | mysql CRX )
        if  [ -z "$IPINDEX" ]; then
                SUPPORT='{"email":"noreply@cephalix.eu","subject":"Can not create device_ip index","description":"Can not create device_ip index","regcode":"'${CRANIX_REG_CODE}'"}'
                curl -s -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' -d "${SUPPORT}" ${CRANIX_SUPPORT_URL}
        fi
fi

