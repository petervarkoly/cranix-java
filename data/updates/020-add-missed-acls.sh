#!/bin/bash

/usr/sbin/crx_api.sh PUT  system/enumerates/apiAcl/hwconf.delete
/usr/sbin/crx_api.sh POST system/acls/groups/1 '{"acl":"hwconf.delete","allowed":true,"userId":null,"groupId":1}'
/usr/sbin/crx_api.sh PUT  system/enumerates/apiAcl/system.superuser
/usr/sbin/crx_api.sh PUT  system/enumerates/apiAcl/printers.add
/usr/sbin/crx_api.sh POST system/acls/groups/1 '{"acl":"printers.add","allowed":true,"userId":null,"groupId":1}'
/usr/sbin/crx_api.sh PUT  system/enumerates/apiAcl/user.add.guests
/usr/sbin/crx_api.sh POST system/acls/groups/2 '{"acl":"user.add.guests","allowed":true,"userId":null,"groupId":2}'
/usr/sbin/crx_api.sh PUT  system/enumerates/apiAcl/group.add.guests
/usr/sbin/crx_api.sh POST system/acls/groups/2 '{"acl":"group.add.guests","allowed":true,"userId":null,"groupId":2}'
/usr/sbin/crx_api.sh PUT  system/enumerates/apiAcl/challenge.manage
/usr/sbin/crx_api.sh POST system/acls/groups/1 '{"acl":"challenge.manage","allowed":true,"userId":null,"groupId":1}'
/usr/sbin/crx_api.sh POST system/acls/groups/2 '{"acl":"challenge.manage","allowed":true,"userId":null,"groupId":2}'
/usr/sbin/crx_api.sh PUT  system/enumerates/apiAcl/objects.manage
/usr/sbin/crx_api.sh POST system/acls/groups/1 '{"acl":"objects.manage","allowed":true,"userId":null,"groupId":1}'
/usr/sbin/crx_api.sh PUT  system/enumerates/apiAcl/subject.manage
/usr/sbin/crx_api.sh POST system/acls/groups/1 '{"acl":"subject.manage","allowed":true,"userId":null,"groupId":1}'
/usr/sbin/crx_api.sh POST system/acls/groups/2 '{"acl":"subject.manage","allowed":true,"userId":null,"groupId":2}'
if [ -e /usr/lib/systemd/system/cranix-api.service ]; then
  /usr/bin/systemctl restart cranix-api
else
  /usr/bin/systemctl restart cephalix-api
fi
