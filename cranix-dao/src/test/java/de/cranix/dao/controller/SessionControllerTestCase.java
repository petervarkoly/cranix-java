package de.cranix.dao.controller;

import de.cranix.dao.Session;

/* prerequisites:
 * 
 * 
 * Teacher 1
 * name: Teacher
 * firstname: Unittest
 * Role: Lehrkraft
 * Class: 10A as Teacher
 * loginname: ut_teacher
 *  password: ut_teacher1
 */

public class SessionControllerTestCase extends OSSDaoTestCase {

    private static final int INVALID_PERSON_ID = -1;
    
    public void SessionControllerTestCase() {
    	
    }

    public void testEmpty() {
        assertEquals(true, true);  
    }

    public void testLoginWithUserSuccess() {
        assertNotNull(c);
        String user = "admin";
        Session session = c.createSessionWithUser(user, "test", "testdevice");
        assertNotNull(session);
    }

    public void testLoginWithUserNoSuccess() {
        assertNotNull(c);
        Session session = c.createSessionWithUser(null, "test", "testdevice");
        assertNull(session);
    }

   /*
    public void testLoginSuccessTeacher() {

        assertNotNull(c);

        Session sessionTeacher1 = getValidTeacherSession();
        assertNotNull(sessionTeacher1);
        assertNotNull(sessionTeacher1.getToken());
        assertNotNull(sessionTeacher1.getIdentity());
        // assertEquals("localhost:8080/infoline",
        // sessionTeacher1.getIdentity().getID());
        System.out.println("TOKEN: " + sessionTeacher1.getToken());

        // validate token
        Session validatedSession = c.validateToken(sessionTeacher1.getToken());
        assertNotNull(validatedSession);
        assertEquals(validatedSession.getToken(), sessionTeacher1.getToken());
        Session notValidatedSession = c.validateToken("invalid");
        assertNull(notValidatedSession);

        // clear local cache
        c.removeAllSessionsFromCache();
        Session validatedSession1 = c.validateToken(sessionTeacher1.getToken());
        assertNotNull(validatedSession1);
        assertTrue(validatedSession.getId() == validatedSession1.getId());

        // try to load person with this token
        PersonController pc = new PersonController(validatedSession1.getIdentity());
        Person teacher = pc.getPersonByUserId(teacher1Login);
        assertNotNull(teacher);

        // remove session
        c.deleteSession(validatedSession);

        Session validatedSession2 = c.validateToken(validatedSession.getToken());
        assertNull(validatedSession2);
        Session validatedSession3 = c.validateToken(sessionTeacher1.getToken());
        assertNull(validatedSession3);

        c.deleteSession(sessionTeacher1);
    }

    public void testTeacherSessionHandling() {

        assertNotNull(c);

        Session sessionTeacher1 = getValidTeacherSession();
        assertNotNull(sessionTeacher1);
        assertNotNull(sessionTeacher1.getToken());
        assertNotNull(sessionTeacher1.getIdentity());
        // assertEquals("localhost:8080/infoline",
        // sessionTeacher1.getIdentity().getID());

        SessionController c2 = new SessionController(sessionTeacher1);

        // validate token
        Session validatedSession = c2.validateToken(sessionTeacher1.getToken());
        assertNotNull(validatedSession);
        assertEquals(validatedSession.getToken(), sessionTeacher1.getToken());
        Session notValidatedSession = c2.validateToken("invalid");
        assertNull(notValidatedSession);

        c.deleteSession(sessionTeacher1);
    }*/
}
