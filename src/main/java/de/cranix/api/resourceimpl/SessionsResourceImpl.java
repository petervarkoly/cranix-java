/* (c) 2020 Péter Varkoly <peter@varkoly.de> - all rights reserved */
/* (c) 2016 EXTIS GmbH - all rights reserved */
package de.cranix.api.resourceimpl;
import de.cranix.api.resources.SessionsResource;
import de.cranix.dao.Session;
import de.cranix.services.SessionService;
import de.cranix.helper.CommonEntityManagerFactory;
import de.cranix.dao.Group;
import de.cranix.dao.Printer;
import de.cranix.dao.Acl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class SessionsResourceImpl implements SessionsResource {

	Logger logger = LoggerFactory.getLogger(SessionsResourceImpl.class);

	public SessionsResourceImpl() {
	}

	@Override
	public Session createSession(UriInfo ui, String username, String password, String device, HttpServletRequest req) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();

		logger.debug("user:" + username + " password:" + password);
		if(username == null || password == null ) {
			throw new WebApplicationException(400);
		}
		if( device == null) {
			device = "dummy";
		}

		//Compatibility reason admin -> Administrator
		if( username.equals("admin") || username.equals("administrator") ) {
			username = "Administrator";
		}

		Session session =  new Session(username);
		session.setIp(req.getRemoteAddr());
		SessionService sessionService = new SessionService(session,em);
		session = sessionService.createSessionWithUser(username, password, device);
		em.close();
		if( session != null ) {
			logger.debug(session.toString());
		} else {
			throw new WebApplicationException(401);
		}
		return session;
	}

	@Override
	public Session createSession(UriInfo ui, HttpServletRequest req, Map<String, String> loginDatas) {
		if( loginDatas.containsKey("username") && loginDatas.containsKey("password") ) {
			return createSession(ui,loginDatas.get("username"),loginDatas.get("password"),"dummy",req);
		}
		throw new WebApplicationException(401);
	}

	@Override
	public Session getStatus(Session session) {
		session.setAcls(this.allowedModules(session));
		return session;
	}

	@Override
	public void deleteSession(Session session, String token) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final SessionService sessionService = new SessionService(session,em);
		if( session == null || ! session.getToken().equals(token) ) {
			em.close();
			logger.info("deletion of session denied " + token);
			throw new WebApplicationException(401);
		}
		sessionService.deleteSession(session);
		em.close();
		logger.debug("deleted session " + token);
	}

	@Override
	public String createToken(UriInfo ui, String username, String password, String device, HttpServletRequest req) {
		Session session = createSession(ui, username, password, device, req);
		if( session == null) {
			return "";
		} else {
			return session.getToken();
		}
	}

	@Override
	public String getSessionValue(Session session,String key){
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		Printer defaultPrinter  = null;
		List<Printer> availablePrinters = null;
		List<String> data = new ArrayList<String>();
		final SessionService sessionService = new SessionService(session,em);
		String resp = "";
		switch(key.toLowerCase()) {
		case "defaultprinter":
			if( session.getDevice() != null ) {
				defaultPrinter = session.getDevice().getDefaultPrinter();
				if( defaultPrinter == null ) {
					defaultPrinter = session.getRoom().getDefaultPrinter();
				}
				if( defaultPrinter != null ) {
					resp =  defaultPrinter.getName();
				}
			}
			break;
		case "availableprinters":
			if( session.getDevice() != null) {
				availablePrinters = session.getDevice().getAvailablePrinters();
				if( availablePrinters == null ) {
					availablePrinters = session.getRoom().getAvailablePrinters();
				}
				if( availablePrinters != null ) {
					for( Printer printer : availablePrinters ) {
						data.add(printer.getName());
					}
					resp = String.join(" ", data);
				}
			}
			break;
		case "dnsname":
			if( session.getDevice() != null) {
				resp = session.getDevice().getName();
			}
			break;
		case "domainname":
			resp = sessionService.getConfigValue("DOMAIN");
		}
		em.close();
		return resp;
	}

	@Override
	public List<String> allowedModules(Session session) {
		List<String> modules = new ArrayList<String>();
		//Is it allowed by the groups.
		for( Group group : session.getUser().getGroups() ) {
			for( Acl acl : group.getAcls() ) {
				if( acl.getAllowed() ) {
					modules.add(acl.getAcl());
				}
			}
		}
		//Is it allowed by the user
		for( Acl acl : session.getUser().getAcls() ){
			if( acl.getAllowed() && !modules.contains(acl.getAcl())) {
				modules.add(acl.getAcl());
			} else if( modules.contains(acl.getAcl()) ) {
				//It is forbidden by the user
				modules.remove(acl.getAcl());
			}
		}
		return modules;
	}

	@Override
	public String logonScript(Session session, String OS) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		String resp = new SessionService(session,em).logonScript(OS);
		em.close();
		return resp;
	}
}
