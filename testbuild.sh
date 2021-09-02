#!/bin/bash -x
REPO="/home/OSC/home:pvarkoly:CRANIX:leap15.2/cranix-java"
HERE=$( pwd )
mvn package install

if [ "$1" ]; then
        PORT=22
        if [ "$2" ]; then
           PORT=$2
        fi
	scp -P $PORT target/cranix-4.3.jar root@$1:/opt/cranix-java/lib/
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
mv    target/cranix-4.3.jar        cranix-java/lib/
chmod 644 cranix-java/lib/*
rsync -a data/                     cranix-java/data/
rsync -a bin/                      cranix-java/bin/
rsync -a conf/                     cranix-java/conf/
mv cranix-java/data/school-INSERT.sql.in   cranix-java/data/school-INSERT.sql
mv cranix-java/data/business-INSERT.sql.in cranix-java/data/business-INSERT.sql
cd src/main/java/de/cranix/api/resources/
./find-rolles.pl >>                ${HERE}/cranix-java/data/school-INSERT.sql
./find-rolles.pl >>                ${HERE}/cranix-java/data/business-INSERT.sql
cd ${HERE}
tar cjf ${REPO}/cranix-java.tar.bz2 cranix-java
xterm -e git log --raw &
cd ${REPO}
osc vc
osc ci
cd $HERE
