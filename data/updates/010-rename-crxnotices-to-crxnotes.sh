#!/bin/bash
. /etc/sysconfig/cranix
if [ ${CRANIX_TYPE} = "cephalix" ]; then
	/usr/bin/systemctl stop cephalix-api
else
	/usr/bin/systemctl stop cranix-api
fi
sleep 2

OLD=$( echo "SHOW TABLES LIKE 'CrxNotices';" | mysql CRX | tail -n 1 )
NEW=$( echo "SHOW TABLES LIKE 'CrxNotes';"   | mysql CRX | tail -n 1 )
if [ -n "$OLD" ]; then
	if [ -z "$NEW" ]; then
		echo "RENAME TABLE CrxNotices TO CrxNotes;" | mysql CRX
	else
		ROWS=$( echo "SELECT COUNT(*) FROM CrxNotes;" | mysql CRX | tail -n 1 )
		if [ "$ROWS" = "0" ]; then
			echo "DROP TABLE CrxNotes;" | mysql CRX
			echo "RENAME TABLE CrxNotices TO CrxNotes;" | mysql CRX
		else
			echo "INSERT INTO CrxNotes SELECT * FROM CrxNotices;" | mysql CRX
			echo "DROP TABLE CrxNotices;" | mysql CRX
		fi
	fi
fi

NT=$( echo "DESCRIBE CrxNotes;" | mysql CRX | grep "^noticeType" | gawk '{ print $1 }' )
if [ -n "$NT" ]; then
	echo "ALTER TABLE CrxNotes RENAME COLUMN noticeType TO noteType;" | mysql CRX
fi

echo "UPDATE Enumerates SET name='noteType' WHERE name='noticeType';" | mysql CRX
echo "UPDATE Enumerates SET value='crxnote.use' WHERE name='apiAcl' AND value='crxnotice.use';" | mysql CRX
echo "UPDATE Acls SET acl='crxnote.use' WHERE acl='crxnotice.use';" | mysql CRX

if [ ${CRANIX_TYPE} = "cephalix" ]; then
	/usr/bin/systemctl start cephalix-api
else
	/usr/bin/systemctl start cranix-api
fi
sleep 10