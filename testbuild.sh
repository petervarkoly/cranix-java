#!/bin/bash -x
REPO="/home/groups/OSC/home:varkoly:CRANIX-4-2/cranix-java"
HERE=$( pwd )
if [ ! -e ../cranix-java ]; then
	echo "Please get first cranix-java"
	exit 1
fi

mvn clean package install

if [ "$1" ]; then
        PORT=22
        if [ "$2" ]; then
           PORT=$2
        fi
	scp -P $PORT cranix-api/target/cranix-api-4.2-with-dependencies/cranix-api-4.2/lib/cranix* root@$1:/opt/cranix-java/lib/
	ssh -p $PORT root@$1 systemctl restart cranix-api
fi
echo  -n "Do you want to check in (y/n)?"
read Y
if [ "$Y" != "y" ]; then
        exit
        sudo umount cranix-api cranix-dao
fi

#Take care, that osc is up to date
cd ${REPO}
osc up
cd $HERE

#Prepare the content of the package
if [ -e cranix-java ]; then
    rm -r cranix-java
fi
mkdir -p cranix-java/bin
mv    cranix-api/target/cranix-api-4.2-with-dependencies/cranix-api-4.2/lib/ cranix-java/
chmod 644 cranix-java/lib/*
rsync -a cranix-dao/data/          cranix-java/data/
rsync -a bin/                      cranix-java/bin/
rsync -a conf/                     cranix-java/conf/
cp ${HERE}/cranix-java/data/school-INSERT.sql.in   ${HERE}/cranix-java/data/school-INSERT.sql
cp ${HERE}/cranix-java/data/business-INSERT.sql.in ${HERE}/cranix-java/data/business-INSERT.sql
cd cranix-api/src/main/java/de/cranix/api/resources/
./find-rolles.pl >>                ${HERE}/cranix-java/data/school-INSERT.sql
./find-rolles.pl >>                ${HERE}/cranix-java/data/business-INSERT.sql
cd ${HERE}
tar cjf ${REPO}/cranix-java.tar.bz2 cranix-java
xterm -e git log --raw &
cd ${REPO}
osc vc
osc ci
cd $HERE
