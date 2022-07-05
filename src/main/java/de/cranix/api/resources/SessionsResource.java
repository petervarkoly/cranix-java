/* (c) 2021 Peter Varkoly <pvarkoly@cephalix.eu> - all rights reserved */
package de.cranix.api.resources;

import static de.cranix.api.resources.Resource.*;

import io.dropwizard.auth.Auth;
import io.swagger.annotations.*;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;
import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import de.cranix.dao.Acl;
import de.cranix.dao.Group;
import de.cranix.dao.Printer;
import de.cranix.dao.Session;
import de.cranix.helper.CrxEntityManagerFactory;
import de.cranix.services.SessionService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("sessions")
@Api(value = "sessions")
@SwaggerDefinition(
	securityDefinition = @SecurityDefinition(
		apiKeyAuthDefinitions = {
			@ApiKeyAuthDefinition( key = "apiKeyAuth", name = "Authorization", in = ApiKeyAuthDefinition.ApiKeyLocation.HEADER)
	})
)
public class SessionsResource {

	Logger logger = LoggerFactory.getLogger(SessionsResource.class);

	public SessionsResource() {}

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Creates a new session and delivers the session.")
	@ApiResponses(value = {
		@ApiResponse(code = 401, message = "Login is incorrect"),
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	public Session createSession(
		@Context UriInfo ui,
		@FormParam("username") String username,
		@FormParam("password") String password,
		@FormParam("device") String device,
		@Context HttpServletRequest req
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();

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

	@POST
	@Path("create")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Creates a new session and delivers the token.",
	    notes = "Following parameter are required:<br>"
	    + "'username' The login name of the user."
	    + "'password' The password of the user."
	)
	@ApiResponses(value = {
		@ApiResponse(code = 401, message = "Login is incorrect"),
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	public Session createSession(
		@Context UriInfo ui,
		@Context HttpServletRequest req,
		Map<String,String> loginDatas
	) {
		if( loginDatas.containsKey("username") && loginDatas.containsKey("password") ) {
			return createSession(ui,loginDatas.get("username"),loginDatas.get("password"),"dummy",req);
		}
		throw new WebApplicationException(401);
	}

	@POST
	@Path("login")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(TEXT)
	@ApiOperation(value = "Creates a new session and delivers the token.")
	@ApiResponses(value = {
		@ApiResponse(code = 401, message = "Login is incorrect"),
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	public String createToken(
		@Context UriInfo ui,
		@FormParam("username") String username,
		@FormParam("password") String password,
		@FormParam("device") String device,
		@Context HttpServletRequest req
	) {
		Session session = createSession(ui, username, password, device, req);
		if( session == null) {
			return "";
		} else {
			return session.getToken();
		}
	}

	@GET
	@Produces(JSON_UTF8)
	@ApiOperation(value = "get session status")
	@ApiResponses(value = {
		@ApiResponse(code = 401, message = "Token is not valid or no token given"),
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	public Session getStatus(
		@ApiParam(hidden = true) @Auth Session session
	) {
		return session;
	}

	@DELETE
	@Path("{token}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "delete session")
	@ApiResponses(value = {
		@ApiResponse(code = 401, message = "Token is not valid or no token given"),
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	public void deleteSession(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("token") String token
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
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

	@GET
	@Path("{key}")
	@Produces(TEXT)
	@ApiOperation(value = "Get some session values. Available keys are: defaultPrinter, availablePrinters, dnsName, domainName.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	public String getSessionValue(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("key") String key
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
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

	@GET
	@Path("logonScript/{OS}")
	@Produces(TEXT)
	@ApiOperation(value = "Get the logo on script for the user and operating system.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	public String logonScript(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("OS") String OS
	 ) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		String resp = new SessionService(session,em).logonScript(OS);
		em.close();
		return resp;
	}
}
