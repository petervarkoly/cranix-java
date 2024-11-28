#!/bin/bash

/usr/sbin/crx_api.sh PUT  system/enumerates/apiAcl/parent.manage
/usr/sbin/crx_api.sh POST system/acls/groups/1 '{"acl":"parent.manage","allowed":true,"userId":null,"groupId":1}'
/usr/sbin/crx_api.sh PUT  system/enumerates/apiAcl/ptm.manage
/usr/sbin/crx_api.sh POST system/acls/groups/1 '{"acl":"ptm.manage","allowed":true,"userId":null,"groupId":1}'
/usr/sbin/crx_api.sh PUT  system/enumerates/apiAcl/ptm.use
/usr/sbin/crx_api.sh POST system/acls/groups/1 '{"acl":"ptm.use","allowed":true,"userId":null,"groupId":1}'

