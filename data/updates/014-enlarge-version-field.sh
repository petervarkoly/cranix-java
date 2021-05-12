#!/bin/bash
export HOME="/root"


SIZE=$( echo "describe SoftwareVersions" | mysql CRX | grep version | gawk '{ print $2 }' )
if [ $SIZE != 'varchar(128)' ]
then
	echo "ALTER TABLE SoftwareVersions MODIFY COLUMN version VARCHAR(128)" | mysql CRX
	touch /run/cranix-db-changed
fi
