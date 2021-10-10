#!/bin/bash

/usr/sbin/crx_api.sh PUT  system/enumerates/apiAcl/hwconf.delete
/usr/sbin/crx_api.sh POST system/acls/groups/1 '{"acl":"hwconf.delete","allowed":true,"userId":null,"groupId":1}'
/usr/sbin/crx_api.sh PUT  system/enumerates/apiAcl/system.superuser
