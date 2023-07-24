#!/bin/bash
export HOME="/root/"
echo 'UPDATE TeachingSubjects set name="Englisch" where name="English";' | /usr/bin/mysql CRX
if [ -e /usr/lib/systemd/system/cranix-api.service ]; then
  /usr/bin/systemctl restart cranix-api
else
  /usr/bin/systemctl restart cephalix-api
fi
/usr/share/cranix/tools/wait-for-api.sh
sleep 2

SUBJECTS="Allgemeinwissen
Biologie
Chemie
Deutsch
Englisch
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
Soziologie
Sport
Technik
Wirtschaft"
for i in ${SUBJECTS}
do
     	echo "{\"id\":null,\"name\":\"${i}\"}" > /tmp/add_subject.json
	/usr/sbin/crx_api_post_file.sh objects/subjects  /tmp/add_subject.json
done
