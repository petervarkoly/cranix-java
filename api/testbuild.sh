#!/bin/bash
HERE=$( pwd )
REPO="/home/groups/OSC/home:varkoly:CRANIX-4-2/cranix-java/"
#sudo /usr/sbin/update-alternatives --set java /usr/lib64/jvm/jre-11-openjdk/bin/java
gradle --offline build
#gradle clean build
tar xf build/distributions/de.cranix.api-4.2.tar

if [ "$1" ]; then
        PORT=22
        if [ "$2" ]; then
           PORT=$2
        fi
	scp -P $PORT de.cranix.api-4.2/lib/de.cranix.* root@$1:/opt/cranix-java/lib/
	ssh -p $PORT root@$1 systemctl restart cranix-api
fi
echo  -n "Do you want to check in (y/n)?"
read Y
if [ "$Y" != "y" ]; then
	exit
fi
cd ${REPO}
osc up
cd $HERE
if [ -e cranix-java ]; then
    rm -r cranix-java
fi
mv de.cranix.api-4.2  cranix-java
chmod 644 cranix-java/lib/*
tar cjf ${REPO}/cranix-java.tar.bz2 cranix-java
cp ${HERE}/../dao/data/school-INSERT.sql.in   ${HERE}/../dao/data/school-INSERT.sql
cp ${HERE}/../dao/data/business-INSERT.sql.in ${HERE}/../dao/data/business-INSERT.sql
cd ${HERE}/src/main/java/de/cranix/api/resources/
./find-rolles.pl >> ${HERE}/../dao/data/school-INSERT.sql
./find-rolles.pl >> ${HERE}/../dao/data/business-INSERT.sql
cd ${HERE}
cd ../dao/
tar cjf ${REPO}/data.tar.bz2 data
cd ${HERE}
CLASSPATH=$( grep "^CLASSPATH=" cranix-java/bin/de.cranix.api )
sed "s#@CLASSPATH@#$CLASSPATH#" start-cranix-api > ${REPO}/start-cranix-api
rm -r cranix-java
xterm -e git log --raw  &

cd ${REPO}/
osc vc
osc ci
cd $HERE
