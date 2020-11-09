#!/bin/bash

SUPPORTACL=$( echo "select * from Enumerates where name='apiAcl' and value='system.support'" | mysql CRX )
if [ -z "$SUPPORTACL" ]; then
	echo "INSERT INTO Enumerates VALUES(NULL,'apiAcl','system.support',1);" | mysql CRX
	echo "INSERT INTO Acls VALUES(NULL,NULL,1,'system.support','Y',1);" | mysql CRX
	touch /run/cranix-db-changed
fi
