#!/bin/bash

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
