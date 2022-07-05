#!/bin/bash

/usr/sbin/crx_api.sh PUT  system/enumerates/apiAcl/hwconf.delete
/usr/sbin/crx_api.sh POST system/acls/groups/1 '{"acl":"hwconf.delete","allowed":true,"userId":null,"groupId":1}'
/usr/sbin/crx_api.sh PUT  system/enumerates/apiAcl/system.superuser
/usr/sbin/crx_api.sh PUT  system/enumerates/apiAcl/printers.add
/usr/sbin/crx_api.sh POST system/acls/groups/1 '{"acl":"printers.add","allowed":true,"userId":null,"groupId":1}'
/usr/sbin/crx_api.sh PUT  system/enumerates/apiAcl/user.add.guests
/usr/sbin/crx_api.sh POST system/acls/groups/2 '{"acl":"user.add.guests","allowed":true,"userId":null,"groupId":1}'
/usr/sbin/crx_api.sh PUT  system/enumerates/apiAcl/group.add.guests
/usr/sbin/crx_api.sh POST system/acls/groups/2 '{"acl":"group.add.guests","allowed":true,"userId":null,"groupId":1}'

