/* (c) 2016 EXTIS GmbH - all rights reserved */
package de.cranix.dao;

import java.util.UUID;


public class SessionToken {

    public static String createSessionToken(String schoolId) {

        String token = UUID.randomUUID().toString();
        if (schoolId != null) {
            token = schoolId + "_" + token;
        }
        return token;
    }

    public static String extractSchoolId(String sessionToken) {
        String id = null;
        if (sessionToken != null) {
            int inx = sessionToken.indexOf("_");
            if (inx > 0) {
                id = sessionToken.substring(0, inx);
            }
        }
        return id;
    }
}
