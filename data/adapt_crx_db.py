#!/usr/bin/python3
import sys
import os
import re

adapt_ids = []
drop_foreign = []
drop_key = []
drop_index = []
command = """
ALLG=$( echo "SELECT id FROM TeachingSubjects where name='Allgemeinwissen';" | mysql CRX | tail -n 1 )
echo "UPDATE CrxChallenges set teachingsubject_id=$ALLG where teachingsubject_id NOT IN (SELECT id FROM TeachingSubjects)" | mysql CRX
"""
print(command)
some_others = [
        "ALTER TABLE Devices MODIFY COLUMN wlanIp varchar(16) DEFAULT NULL;",
        "UPDATE Devices SET wlanIp NULL where wlanIp = '';",
        "UPDATE CrxQuestions SET crxchallenge_id=NULL WHERE crxchallenge_id NOT IN (SELECT id FROM CrxChallenges);"
        ]
with open(sys.argv[1],'r') as file:
    table_name = ""
    key_name = ""
    for lin in file.readlines():
        match = re.search("CREATE TABLE .(\w+). ",lin)
        if match:
            table_name = match.group(1)
            adapt_ids.append("ALTER TABLE {0} MODIFY COLUMN id  bigint(20) unsigned NOT NULL AUTO_INCREMENT;".format(table_name))
            continue
        match = re.search("^  KEY `(\w+)` \(`\w+`\),",lin)
        if match:
            key_name = match.group(1)
            drop_key.append("ALTER TABLE {0} DROP KEY {1};".format(table_name, key_name))
        match = re.search("^  CONSTRAINT `(\w+)` FOREIGN",lin)
        if match:
            key_name = match.group(1)
            drop_foreign.append("ALTER TABLE {0} DROP FOREIGN KEY {1};".format(table_name, key_name))
        match = re.search("^  UNIQUE KEY `(\w+)` \(",lin)
        if match:
            key_name = match.group(1)
            drop_index.append("ALTER TABLE {0} DROP INDEX {1};".format(table_name,key_name))
        match = re.search("^  `(\w+_id)` bigint\(20\) (\w+) ",lin)
        if match and match.group(2) != "unsigned":
            key_name = match.group(1)
            adapt_ids.append("ALTER TABLE {0} MODIFY COLUMN {1} bigint(20) unsigned;".format(table_name, key_name))
for com in drop_foreign + drop_key + drop_index + adapt_ids + some_others:
    print('echo "{}"| mysql CRX'.format(com))
