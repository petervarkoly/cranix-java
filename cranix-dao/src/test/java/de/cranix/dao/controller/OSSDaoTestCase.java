package de.cranix.dao.controller;



import de.cranix.dao.Session;
import de.cranix.dao.controller.SessionController;
import junit.framework.TestCase;

public class OSSDaoTestCase extends TestCase {

    protected SessionController c = null;

    protected Session getValidAdminSession() {
    	String user =     "admin";
    	String password = "dummy";
    	
        Session sessionAdmin1 = c.createSessionWithUser(user, password,""); //TODO login
        return sessionAdmin1;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
       
    
        Session session =  new Session();
        session.setIp("10.0.0.1");
         c = new SessionController(session,null);
     
    }

    public void testDummy() {
        assertNotNull(c);
    }

}
