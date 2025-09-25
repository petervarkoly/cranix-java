#!/bin/bash -x
REPO=~/OSC/home:pvarkoly:CRANIX/cranix-java
HERE=$( pwd )
#mvn clean package install
mvn package install

if [ "$1" ]; then
        PORT=22
        if [ "$2" ]; then
           PORT=$2
        fi
	scp -P $PORT target/cranix-15.6.jar root@$1:/opt/cranix-java/lib/
	ssh -p $PORT root@$1 systemctl restart cranix-api
fi
echo  -n "Do you want to check in (y/n)?"
read Y
if [ "$Y" != "y" ]; then
        exit
fi

#Take care, that osc is up to date
cd ${REPO}
osc up
cd $HERE

#Prepare the content of the package
if [ -e cranix-java ]; then
    rm -r cranix-java
fi
mkdir -p cranix-java/lib
mv    target/cranix-15.6.jar        cranix-java/lib/
chmod 644 cranix-java/lib/*
rsync -a data/                     cranix-java/data/
mkdir -p cranix-java/data/updates/
rsync -a bin/                      cranix-java/bin/
rsync -a conf/                     cranix-java/conf/
cd src/main/java/de/cranix/api/resources/
./find-rolles.pl >>  ${HERE}/cranix-java/data/school-inserts.sql
./find-rolles.pl >>  ${HERE}/cranix-java/data/business-inserts.sql
./adapt-rolles.pl >  ${HERE}/cranix-java/data/updates/adapt-rolles.sh
chmod 755 ${HERE}/cranix-java/data/updates/adapt-rolles.sh
cd ${HERE}
tar cjf ${REPO}/cranix-java.tar.bz2 cranix-java
xterm -e git log --raw &
cd ${REPO}
osc vc
osc ci
cd $HERE
