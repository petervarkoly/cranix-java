#/bin/bash -x
#

if [ "$( echo "describe TeachingSubjects" | mysql CRX | grep auto_increment )" ]; then
        exit
fi
export HOME="/root/"
DATE=$( /usr/share/cranix/tools/crx_date.sh )
BACKUP="/var/adm/cranix/backup/${DATE}"
mkdir -p ${BACKUP}

TABLES="ChallengesInArea
QuestionInArea
GroupsOfChallenges
UsersOfChallenges
CrxChallengeAnswers
CrxQuestionAnswers
CrxQuestions
CrxChallenges
SubjectAreas
TeachingSubjects
"

. /etc/sysconfig/cranix
unit="cranix-api"
if [ "$CRANIX_TYPE" == 'cephalix' ]; then
        unit="cephalix-api"
fi
/usr/bin/systemctl stop ${unit}
for i in ${TABLES}
do
        mysqldump --no-create-info CRX $i > /${BACKUP}/$i.sql
        if [ $? != 0 ]; then
                echo "Can not make backup from $i table"
                exit 1
        fi
done
sed -i s/English/Englisch/ ${BACKUP}/TeachingSubjects.sql
sed -i 's/,NULL)/)/g' ${BACKUP}/CrxQuestions.sql
sed -i 's/1,NULL/1/g' ${BACKUP}/CrxQuestions.sql

for i in ${TABLES}
do
        echo "DROP TABLE $i" | mysql CRX
done
/usr/bin/systemctl start ${unit}
/usr/share/cranix/tools/wait-for-api.sh
sleep 10
/usr/sbin/crx_api.sh GET challenges/all > /dev/null
/usr/bin/systemctl stop ${unit}

TABLES="TeachingSubjects
SubjectAreas
CrxChallenges
CrxQuestions
QuestionInArea
ChallengesInArea
CrxChallengeAnswers
CrxQuestionAnswers
"
for i in ${TABLES}
do
        mysql CRX < /${BACKUP}/$i.sql
done

/usr/bin/systemctl start ${unit}
/usr/share/cranix/tools/wait-for-api.sh
sleep 10

