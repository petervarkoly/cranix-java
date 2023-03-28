#!/bin/bash
#

TABLES="Groups
Devices
PositiveLists
Announcements
TaskResponses
FAQs
Contacts
Categories
"
for i in $TABLES
do
	echo "UPDATE $i set CREATOR_ID=OWNER_ID WHERE NOT ISNULL(OWNER_ID)" | mysql CRX
	echo "UPDATE $i set CREATOR_ID=1 WHERE ISNULL(OWNER_ID)" | mysql CRX
	echo "ALTER TABLE $i DROP FOREIGN KEY FK_${i}_OWNER_ID" | mysql CRX
	echo "ALTER TABLE $i ADD  FOREIGN KEY (CREATOR_ID) REFERENCES Users (id)" | mysql CRX
	echo "ALTER TABLE $i DROP COLUMN OWNER_ID" | mysql CRX
done
