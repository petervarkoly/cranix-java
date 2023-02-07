#!/bin/bash
SUBJECTS="Chemie
Deutsch
English
Ethik
Französisch
Geschichte
Geographie
Informatik
Latein
Literatur
Mathematik
Musik
Philosophie
Physik
Religion
Sport
Technik"

/usr/share/cranix/tools/wait-for-api.sh

for i in ${SUBJECTS}
do
     	echo "{\"id\":null,\"name\":\"${i}\"}" > /tmp/add_subject.json
	/usr/sbin/crx_api_post_file.sh objects/subjects  /tmp/add_subject.json
done
