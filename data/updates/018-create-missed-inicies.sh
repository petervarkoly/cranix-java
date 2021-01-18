#!/bin/bash

PARTINDX=$( echo "SELECT * FROM information_schema.statistics WHERE table_schema = 'CRX' AND table_name = 'Partitions' AND column_name = 'name';" | mysql CRX )
if [ -z "$PARTINDX" ]; then
	echo "CREATE UNIQUE INDEX partitions on Partitions(hwconf_id,name);"   | mysql CRX
	touch /run/cranix-db-changed
fi
CATINDX=$( echo "SELECT * FROM information_schema.statistics WHERE table_schema = 'CRX' AND table_name = 'Categories' AND column_name = 'name';" | mysql CRX )
if [ -z "$CATINDX" ]; then
	echo "CREATE UNIQUE INDEX partitions on Categories(name,categoryType);" | mysql CRX
	touch /run/cranix-db-changed
fi
