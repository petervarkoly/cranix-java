#!/bin/bash

CLONEPROXY=$( echo "select * from Enumerates where name='deviceType' and value='cloneProxy'" | mysql CRX )
if [ -z "$CLONEPROXY" ]; then
	echo "INSERT INTO Enumerates VALUES(NULL,'deviceType','cloneProxy',1);" | mysql CRX
	echo "INSERT INTO HWConfs VALUES(NULL,'CloneProxy','Clone Proxy for WLAN Devices','cloneProxy',1);" | mysql CRX
	touch /run/cranix-db-changed
fi
