#!/bin/bash
export HOME="/root"


SIZE=$( echo "describe SoftwareVersions" | mysql OSS | grep version | gawk '{ print $2 }' )
if [ $SIZE != 'varchar(128)' ]
then
	echo "ALTER TABLE SoftwareVersions MODIFY COLUMN version VARCHAR(128)" | mysql OSS
fi
