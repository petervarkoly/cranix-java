/* (c) 2021 Peter Varkoly <pvarkoly@cephalix.eu> - all rights reserved */
package de.cranix.api.resources;

import static de.cranix.helper.CranixConstants.*;
import static de.cranix.api.resources.Resource.*;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.*;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.cranix.dao.*;
import de.cranix.helper.CrxEntityManagerFactory;
import de.cranix.helper.CrxSystemCmd;
import de.cranix.services.JobService;
import de.cranix.services.ProxyService;
import de.cranix.services.SessionService;
import de.cranix.services.Service;
import de.cranix.services.SystemService;

@Path("system")
@Api(value = "system")
public class SystemResource {

	public SystemResource() {}

	@GET
	@Path("name")
	@Produces(TEXT)
	@ApiOperation(value = "Gets the name of the institute.")
	@ApiResponses(value = {
		@ApiResponse(code = 401, message = "No regcode was found"),
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	public String getName(
		@Context UriInfo ui,
		@Context HttpServletRequest req
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		Session session  = new SessionService(em).getLocalhostSession();
		String resp = new SystemService(session,em).getConfigValue("NAME");
		em.close();
		return resp;
	}

	@GET
	@Path("type")
	@Produces(TEXT)
	@ApiOperation(value = "Gets the type of the institute.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	public String getType(
		@Context UriInfo ui,
		@Context HttpServletRequest req
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		Session session  = new SessionService(em).getLocalhostSession();
		String resp = new SystemService(session,em).getConfigValue("TYPE");
		em.close();
		return resp;
	}

	@GET
	@Path("status")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the system status.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.status")
	public Object getStatus( @ApiParam(hidden = true) @Auth Session session) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		SystemService systemService = new SystemService(session,em);
		Object resp = systemService.getStatus();
		em.close();
		return resp;
	}

	@GET
	@Path("diskStatus")
	@Produces(JSON_UTF8)
	@ApiOperation(
		value = "Gets the status of the disk(s) in system.",
		notes = "The format of the response:<br>" +
			"{\"Device Name\":{\"size\":Size in MB,\"used\":Used amount in MB,\"mount\":\"Mount point\"},"
		)
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.status")
	public Object getDiskStatus( @ApiParam(hidden = true) @Auth Session session) {
		String[] program	= new String[1];
		StringBuffer reply  = new StringBuffer();
		StringBuffer stderr = new StringBuffer();
		program[0] = cranixBaseDir + "tools/check_partitions.sh";
		CrxSystemCmd.exec(program, reply, stderr, null);
		return reply.toString();
	}

	@GET
	@Path("services")
	@Produces(JSON_UTF8)
	@ApiOperation(
		value = "Gets the status of the monitored services.",
		notes = "The for mat of the response:<br>" +
			"[{\"service\":\"amavis\",\"enabled\":\"false\",\"active\":\"false\"},"
	)
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.status")
	public Object getServicesStatus( @ApiParam(hidden = true) @Auth Session session) {
		String[] program	= new String[1];
		StringBuffer reply  = new StringBuffer();
		StringBuffer stderr = new StringBuffer();
		program[0] = cranixBaseDir + "tools/check_services.sh";
		CrxSystemCmd.exec(program, reply, stderr, null);
		return reply.toString();
	}

	@PUT
	@Path("services/{name}/{what}/{value}")
	@Produces(JSON_UTF8)
	@ApiOperation(
		value = "Modify service.",
		notes = "* name is the name of the service.<br>" +
			"* what can be enabled or active.<br>" +
			"* value can be: true or false. By what = active restart is allowed if the original state was true."
	)
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.status")
	public CrxResponse setServicesStatus(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("name")  String name,
		@PathParam("what")  String what,
		@PathParam("value") String value
	) {
		String[] program	= new String[3];
		StringBuffer reply  = new StringBuffer();
		StringBuffer stderr = new StringBuffer();
		program[0] = "/usr/bin/systemctl";
		program[2] = name;
		if( what.equals("enabled") ) {
			if( value.toLowerCase().equals("true")) {
				program[1] = "enable";
			} else {
				program[1] = "disable";
			}
		} else {
			if( value.toLowerCase().equals("true")) {
				program[1] = "start";
			} else if(value.toLowerCase().equals("false") ) {
				program[1] = "stop";
			} else {
				program[1] = "restart";
			}
		}
		if( CrxSystemCmd.exec(program, reply, stderr, null) == 0 ) {
			return new CrxResponse(session,"OK","Service state was set successfully.");
		} else {
			return new CrxResponse(session,"ERROR",stderr.toString());
		}
	}

	@POST
	@Path("customize")
	@Produces(JSON_UTF8)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation(value = "Upload picture for crx logon site.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator") }
	)
	@RolesAllowed("system.customize")
	public CrxResponse customize(@ApiParam(hidden = true) @Auth Session session,
		@FormDataParam("file") final InputStream fileInputStream,
		@FormDataParam("file") final FormDataContentDisposition contentDispositionHeader
	) {
		String fileName = contentDispositionHeader.getFileName();
		File file = new File("/srv/www/admin/assets/" + fileName );
		try {
			Files.copy(fileInputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			return new CrxResponse(session,"ERROR", e.getMessage());
		}
		return new CrxResponse(session,"OK", "File was saved succesfully.");
	}

	@GET
	@Path("enumerates/{type}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "get session status")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	public List<String> getEnumerates(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("type") String type
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		SystemService systemService = new SystemService(session,em);
		List<String> resp = systemService.getEnumerates(type);
		em.close();
		return resp;
	}

	@PUT
	@Path("enumerates/{type}/{value}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Creates a new enumerate")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.enumerates")
	public CrxResponse addEnumerate(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("type") String type,
		@PathParam("value") String value
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		SystemService systemService = new SystemService(session,em);
		CrxResponse resp = systemService.addEnumerate(type, value);
		em.close();
		return resp;
	}

	@DELETE
	@Path("enumerates/{type}/{value}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Deletes an enumerate")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.enumerates")
	public CrxResponse deleteEnumerate(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("type") String type,
		@PathParam("value") String value
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		SystemService systemService = new SystemService(session,em);
		CrxResponse resp = systemService.deleteEnumerate(type, value);
		em.close();
		return resp;
	}

	// Global Configuration
	@GET
	@Path("configuration")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the whole system configuration in a list of maps.",
		notes =  "* A map has folloing format:<br>" +
			 "* {\"path\":\"Basic\",\"readOnly\":\"yes\",\"type\":\"string\",\"value\":\"DE\",\"key\":\"CCODE\"}")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.configuration")
	public List<Map<String, String>>  getConfig( @ApiParam(hidden = true) @Auth Session session)
	{
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		SystemService systemService = new SystemService(session,em);
		List<Map<String, String>> resp = systemService.getConfig();
		em.close();
		return resp;
	}

	@GET
	@Path("configuration/{key}")
	@Produces(TEXT)
	@ApiOperation(value = "Gets a system configuration value.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	public String getConfig(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("key") String key
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		SystemService systemService = new SystemService(session,em);
		String resp = systemService.getConfigValue(key);
		em.close();
		return resp;
	}

	@PUT
	@Path("configuration/{key}/{value}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Sets a system configuration.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.configuration")
	public CrxResponse setConfig(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("key") String key,
		@PathParam("value") String value
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		SystemService systemService = new SystemService(session,em);
		if( systemService.setConfigValue(key, value) ) {
			em.close();
			return new CrxResponse(session,"OK","Global configuration value was set succesfully.");
		} else {
			em.close();
			return new CrxResponse(session,"ERROR","Global configuration value could not be set.");
		}
	}

	@POST
	@Path("configuration")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Sets a system configuration in a map.<br>"
			+ "* The map must have following format:<br>"
			+ "* {key:<key>,value:<value>}")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.configuration")
	public CrxResponse setConfig(
		@ApiParam(hidden = true) @Auth Session session,
		Map<String, String> config
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		SystemService systemService = new SystemService(session,em);
		try {
			if( systemService.setConfigValue(config.get("key"), config.get("value")) ) {
				return new CrxResponse(session,"OK","Global configuration value was set succesfully.");
			} else {
				return new CrxResponse(session,"ERROR","Global configuration value could not be set.");
			}
		} catch(Exception e) {
			return new CrxResponse(session,"ERROR","Global configuration value could not be set.");
		} finally {
			em.close();
		}
	}

	// Firewall configuration
	@GET
	@Path("firewall/incomingRules")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the incoming firewall rules.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.firewall")
	public Map<String, String>  getFirewallIncomingRules( @ApiParam(hidden = true) @Auth Session session)
	   	{
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		SystemService systemService = new SystemService(session,em);
		Map<String, String> resp = systemService.getFirewallIncomingRules();
		em.close();
		return resp;
	}

	@POST
	@Path("firewall/incomingRules")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Sets the incoming firewall rules.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.firewall")
	public CrxResponse  setFirewallIncomingRules(
		@ApiParam(hidden = true) @Auth Session session,
		Map<String, String> incommingRules
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		SystemService systemService = new SystemService(session,em);
		CrxResponse resp = systemService.setFirewallIncomingRules(incommingRules);
		em.close();
		return resp;
	}

	@GET
	@Path("firewall/outgoingRules")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the outgoing firewall rules.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.firewall")
	public List<Map<String, String>>  getFirewallOutgoingRules( @ApiParam(hidden = true) @Auth Session session)
	{
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		SystemService systemService = new SystemService(session,em);
		List<Map<String, String>> resp = systemService.getFirewallOutgoingRules();
		em.close();
		return resp;
	}

	@POST
	@Path("firewall/outgoingRules")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Sets the outgoing firewall rules.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.firewall")
	public CrxResponse  setFirewallOutgoingRules(
		@ApiParam(hidden = true) @Auth Session session,
		List<Map<String, String>> outgoingRules
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		SystemService systemService = new SystemService(session,em);
		CrxResponse resp = systemService.setFirewallOutgoingRules(outgoingRules);
		em.close();
		return resp;
	}

	@GET
	@Path("firewall/remoteAccessRules")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the remote access firewall rules.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.firewall")
	public List<Map<String, String>>  getFirewallRemoteAccessRules( @ApiParam(hidden = true) @Auth Session session)
	{
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		SystemService systemService = new SystemService(session,em);
		List<Map<String, String>> resp =  systemService.getFirewallRemoteAccessRules();
		em.close();
		return resp;
	}

	@POST
	@Path("firewall/remoteAccessRules")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Sets the remote access firewall rules.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.firewall")
	public CrxResponse  setFirewallRemoteAccessRules(
		@ApiParam(hidden = true) @Auth Session session,
		List<Map<String, String>> remoteAccessRules
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		SystemService systemService = new SystemService(session,em);
		CrxResponse resp = systemService.setFirewallRemoteAccessRules(remoteAccessRules);
		em.close();
		return resp;
	}

	@PUT
	@Path("firewall/{state}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Sets the remote access firewall rules.")
	@ApiResponses(value = {
			@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.firewall")
	public CrxResponse  setFirewallStatus(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("state") String state
	) {
		switch (state) {
			case "stop": {
				return this.setServicesStatus(session,"SuSEfirewall2","active","false");
			}
			case "start": {
				return this.setServicesStatus(session,"SuSEfirewall2","active","true");
			}
			case "restart": {
				return this.setServicesStatus(session,"SuSEfirewall2","active","restart");
			}
		}
		return new CrxResponse(session,"ERROR","Bad firewall state.");
	}

	/*
	 * Registration
	 */
	@PUT
	@Path("register")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Register the server againts the update server.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.register")
	public CrxResponse register( @ApiParam(hidden = true) @Auth Session session)
	{
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new SystemService(session,em).registerSystem();
		em.close();
		return resp;
	}

	/*
	 * Package handling
	 */
	@GET
	@Path("packages/{filter}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Searches packages.")
	@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	@RolesAllowed("system.packages")
	public List<Map<String,String>> searchPackages(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("filter") String filter
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<Map<String, String>> resp = new SystemService(session,em).searchPackages(filter);
		em.close();
		return resp;
	}

	@POST
	@Path("packages")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Install packages.")
	@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	@RolesAllowed("system.packages")
	public CrxResponse installPackages(
		@ApiParam(hidden = true) @Auth Session session,
		List<String> packages
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new SystemService(session,em).installPackages(packages);
		em.close();
		return resp;
	}

	@POST
	@Path("packages/update")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Update packages.")
	@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	@RolesAllowed("system.packages")
	public CrxResponse updatePackages(
		@ApiParam(hidden = true) @Auth Session session,
		List<String> packages
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new SystemService(session,em).updatePackages(packages);
		em.close();
		return resp;
	}

	@PUT
	@Path("update")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Install all updates on the system.")
	@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	@RolesAllowed("system.update")
	public CrxResponse updateSystem( @ApiParam(hidden = true) @Auth Session session)
	{
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new SystemService(session,em).updateSystem();
		em.close();
		return resp;
	}

	/*
	 * Proxy default handling
	 */
	@GET
	@Path("proxy/default/{role}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delivers the default setting for proxy.")
	@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	@RolesAllowed("system.proxy")
	public List<ProxyRule> getProxyDefault(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("role") String role
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<ProxyRule> resp = new ProxyService(session,em).readDefaults(role);
		em.close();
		return resp;
	}

	@POST
	@Path("proxy/default/{role}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delivers the default setting for proxy.")
	@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	@RolesAllowed("system.proxy")
	public CrxResponse setProxyDefault(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("role") String role,
		List<ProxyRule> acl
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new ProxyService(session,em).setDefaults(role, acl);
		em.close();
		return resp;
	}

	@GET
	@Path("proxy/basic")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delivers the default setting for proxy.")
	@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	@RolesAllowed("system.proxy")
	public Object getProxyBasic( @ApiParam(hidden = true) @Auth Session session)
	{
		String[] program   = new String[2];
		program[0] = cranixBaseDir + "tools/squidGuard.pl";
		program[1] = "readJson";
		StringBuffer reply = new StringBuffer();
		StringBuffer error = new StringBuffer();
		CrxSystemCmd.exec(program, reply, error, "");
		return reply.toString();
	}

	@POST
	@Path("proxy/basic")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Writes the default setting for proxy.")
	@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	@RolesAllowed("system.proxy")
	public CrxResponse setProxyBasic(
		@ApiParam(hidden = true) @Auth Session session,
		String acls
	) {
		String[] program   = new String[2];
		program[0] = cranixBaseDir + "tools/squidGuard.pl";
		program[1] = "writeJson";
		StringBuffer reply = new StringBuffer();
		StringBuffer error = new StringBuffer();
		CrxSystemCmd.exec(program, reply, error, acls);
		//TODO check error
		return new CrxResponse(session,"OK","Proxy basic configuration was written succesfully.");
	}

	@GET
	@Path("proxy/lists")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delivers the proxy lists.")
	@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	@RolesAllowed({"system.proxy","system.unbound"})
	public List<Map<String,String>> getProxyLists( @ApiParam(hidden = true) @Auth Session session) {
		return new ProxyService(session,null).getLists();
	}

	@GET
	@Path("proxy/defaults")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delivers the default setting for proxy.")
	@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	@RolesAllowed("system.proxy")
	public Map<String,List<ProxyRule>> getProxyDefaults( @ApiParam(hidden = true) @Auth Session session) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		Map<String, List<ProxyRule>> resp = new ProxyService(session,em).readDefaults();
		em.close();
		return resp;
	}

	@POST
	@Path("proxy/defaults")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delivers the default setting for proxy.")
	@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	@RolesAllowed("system.proxy")
	public CrxResponse setProxyDefaults(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("role") String role,
		Map<String,List<ProxyRule>> acls
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new ProxyService(session,em).setDefaults(acls);
		em.close();
		return resp;
	}


	@GET
	@Path("proxy/custom/{list}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delivers the custom lists of the proxy: good or bad.")
	@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	@RolesAllowed({"system.proxy","system.unbound"})
	public List<String> getTheCustomList(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("list") String list
	) {
		try {
			return  Files.readAllLines(Paths.get("/var/lib/squidGuard/db/custom/" +list + "/domains"));
		}
		catch( IOException e ) {
			e.printStackTrace();
		}
		return null;
	}

	@POST
	@Path("proxy/custom/{list}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Sets the custom lists of the proxy: good or bad.")
	@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	@RolesAllowed({"system.proxy","system.unbound"})
	public CrxResponse setTheCustomList(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("list")	String list,
		List<String> domains
	) {
		try {
			Files.write(Paths.get("/var/lib/squidGuard/db/custom/" +list + "/domains"),domains);
			String[] program   = new String[5];
			StringBuffer reply = new StringBuffer();
			StringBuffer error = new StringBuffer();
			program[0] = "/usr/sbin/squidGuard";
			program[1] = "-c";
			program[2] = "/etc/squid/squidguard.conf";
			program[3] = "-C";
			program[4] = "custom/" +list + "/domains";
			CrxSystemCmd.exec(program, reply, error, null);
			new Service(session,null).systemctl("try-restart", "squid");
			return new CrxResponse(session,"OK","Custom list was written successfully");
		} catch( IOException e ) {
			e.printStackTrace();
		}
		return new CrxResponse(session,"ERROR","Could not write custom list.");
	}

	@PUT
	@Path("unbound")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Resets the unbound server reading the new configuration.")
	@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	@RolesAllowed("system.unbound")
	public CrxResponse resetUnbound( @ApiParam(hidden = true) @Auth Session session)
	{
		String[] program   = new String[1];
		program[0] = cranixBaseDir + "tools/unbound/create_unbound_redirects";
		StringBuffer reply = new StringBuffer();
		StringBuffer error = new StringBuffer();
		CrxSystemCmd.exec(program, reply, error, "");
		//TODO check error
		return new CrxResponse(session,"OK","DNS server configuration was written succesfully.");
	}

	/*
	 * Job management
	 */
	@POST
	@Path("jobs/add")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Creates a new job")
	@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	@RolesAllowed("system.jobs")
	public CrxResponse createJob(
		@ApiParam(hidden = true) @Auth Session session,
		Job job
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new JobService(session,em).createJob(job);
		em.close();
		return resp;
	}

	@POST
	@Path("jobs/search")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Searching for jobs by description and time.")
	@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	@RolesAllowed("system.jobs")
	public List<Job> searchJob(
		@ApiParam(hidden = true) @Auth Session session,
		Job job
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<Job>   resp = new JobService(session,em).searchJobs(job.getDescription(), job.getStartTime(), job.getEndTime());
		em.close();
		return resp;
	}

	@GET
	@Path("jobs/{jobId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the job with all parameters inclusive log.")
	@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	@RolesAllowed("system.jobs")
	public Job getJob(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("jobId") Long jobId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		Job	 resp = new JobService(session,em).getById(jobId);
		em.close();
		return resp;
	}

	@GET
	@Path("jobs/running")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the job with all parameters inclusive log.")
	@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	@RolesAllowed("system.jobs")
	public List<Job> getRunningJobs( @ApiParam(hidden = true) @Auth Session session) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<Job> resp = new JobService(session,em).getRunningJobs();
		em.close();
		return resp;
	}

	@GET
	@Path("jobs/failed")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the job with all parameters inclusive log.")
	@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	@RolesAllowed("system.jobs")
	public List<Job> getFailedJobs( @ApiParam(hidden = true) @Auth Session session) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<Job> resp = new JobService(session,em).getFailedJobs();
		em.close();
		return resp;
	}

	@GET
	@Path("jobs/succeeded")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the job with all parameters inclusive log.")
	@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	@RolesAllowed("system.jobs")
	public List<Job> getSucceededJobs( @ApiParam(hidden = true) @Auth Session session) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<Job> resp = new JobService(session,em).getSucceededJobs();
		em.close();
		return resp;
	}

	@PUT
	@Path("jobs/{jobId}/exit/{exitValue}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Set the exit value of a job.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.jobs")
	public CrxResponse setJobExitValue(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("jobId") Long jobId,
		@PathParam("exitValue") Integer exitValue
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new JobService(session,em).setExitCode(jobId, exitValue);
		em.close();
		return resp;
	}

	@PUT
	@Path("jobs/{jobId}/restart")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Set the exit value of a job.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.jobs")
	public CrxResponse restartJob(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("jobId") Long jobId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new JobService(session,em).restartJob(jobId);
		em.close();
		return resp;
	}

	/*
	 * Acl Management
	 */
	@GET
	@Path("acls")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get all existing acls.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.acls")
	public List<Acl> getAcls( @ApiParam(hidden = true) @Auth Session session) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<Acl> resp = new SystemService(session,em).getAvailableAcls();
		em.close();
		return resp;
	}

	@GET
	@Path("acls/groups/{groupId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get the acls of a group.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.acls")
	public List<Acl> getAclsOfGroup(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("groupId") Long groupId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<Acl> resp = new SystemService(session,em).getAclsOfGroup(groupId);
		em.close();
		return resp;
	}

	@GET
	@Path("acls/groups/{groupId}/available")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get the available acls for a group.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.acls")
	public List<Acl> getAvailableAclsForGroup(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("groupId") Long groupId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<Acl> resp = new SystemService(session,em).getAvailableAclsForGroup(groupId);
		em.close();
		return resp;
	}

	@POST
	@Path("acls/groups/{groupId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Set an ACL of a group. This can be an existing or a new acl.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.acls")
	public CrxResponse setAclOfGroup(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("groupId") Long groupId,
		Acl acl
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new SystemService(session,em).setAclToGroup(groupId,acl);
		em.close();
		return resp;
	}

	@DELETE
	@Path("acls/groups/{groupId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Removes all ACLs of a group.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.acls")
	public CrxResponse deleteAclsOfGroup(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("groupId") Long groupId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		Group group = em.find(Group.class, groupId);
		CrxResponse resp = new CrxResponse(session,"OK","Acls was deleted succesfully.");
		if( group != null ) {
			em.getTransaction().begin();
			for(Acl acl : group.getAcls() ) {
				em.remove(acl);
			}
			group.setAcls(new ArrayList<Acl>());
			em.merge(group);
			em.getTransaction().commit();
		} else {
			resp = new CrxResponse(session,"ERROR","Group can not be find.");
		}
		return resp;
	}

	@GET
	@Path("acls/users/{userId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get the acls of a user.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.acls")
	public List<Acl> getAclsOfUser(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("userId") Long userId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<Acl>   resp = new SystemService(session,em).getAclsOfUser(userId);
		em.close();
		return resp;
	}

	@GET
	@Path("acls/users/{userId}/available")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get the available acls for a user.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.acls")
	public List<Acl> getAvailableAclsForUser(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("userId") Long userId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<Acl> resp = new SystemService(session,em).getAvailableAclsForUser(userId);
		em.close();
		return resp;
	}

	@POST
	@Path("acls/users/{userId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Set an ACL of a user. This can be an existing or a new acl.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.acls")
	public CrxResponse setAclOfUser(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("userId") Long userId,
		Acl acl
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new SystemService(session,em).setAclToUser(userId,acl);
		em.close();
		return resp;
	}

	@DELETE
	@Path("acls/users/{userId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Removes all ACLs of a user.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.acls")
	public CrxResponse deleteAclsOfUser(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("userId") Long userId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		User user = em.find(User.class, userId);
		CrxResponse resp = new CrxResponse(session,"OK","Acls was deleted succesfully.");
		if( user != null ) {
			em.getTransaction().begin();
			for(Acl acl : user.getAcls() ) {
				em.remove(acl);
			}
			user.setAcls(new ArrayList<Acl>());
			em.merge(user);
			em.getTransaction().commit();
		} else {
			resp = new CrxResponse(session,"ERROR","Group can not be find.");
		}
		return resp;
	}

	@GET
	@Path("dns/domains")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delivers the list of the DNS-Domains the server is responsible for these.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.dns")
	public String[] getDnsDomains( @ApiParam(hidden = true) @Auth Session session) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		String[] resp = new SystemService(session,em).getDnsDomains();
		em.close();
		return resp;
	}

	@POST
	@Path("dns/domains")
	@Produces(JSON_UTF8)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation(value = "Creates a new DNS domain.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.dns")
	public CrxResponse addDnsDomain(
		@ApiParam(hidden = true) @Auth Session session,
		@FormDataParam("domainName")   String  domainName
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new SystemService(session,em).addDnsDomain(domainName);
		em.close();
		return resp;
	}

	@POST
	@Path("dns/domains/delete")
	@Produces(JSON_UTF8)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation(value = "Deleets an existing DNS domain.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.dns")
	public CrxResponse deleteDnsDomain(
		@ApiParam(hidden = true) @Auth Session session,
		@FormDataParam("domainName")   String  domainName
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new SystemService(session,em).deleteDnsDomain(domainName);
		em.close();
		return resp;
	}

	@POST
	@Path("dns/domains/records")
	@Produces(JSON_UTF8)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation(value = "Delivers the list of the dns records in a domain.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.dns")
	public List<DnsRecord> getRecords(
		@ApiParam(hidden = true) @Auth Session session,
		@FormDataParam("domainName")   String  domainName
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<DnsRecord> resp = new SystemService(session,em).getRecords(domainName);
		em.close();
		return resp;
	}

	@POST
	@Path("dns/domains/addRecord")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Creates a new DNS record. The following Record types are allowed: A|AAAA|PTR|CNAME|NS|MX|SRV|TXT")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.dns")
	public CrxResponse addDnsRecord(
		@ApiParam(hidden = true) @Auth Session session,
		DnsRecord dnsRecord
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new SystemService(session,em).addDnsRecord(dnsRecord);
		em.close();
		return resp;
	}

	@POST
	@Path("dns/domains/deleteRecord")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delets an existing DNS record.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.dns")
	public CrxResponse deleteDnsRecord(
		@ApiParam(hidden = true) @Auth Session session,
		DnsRecord dnsRecord
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new SystemService(session,em).deleteDnsRecord(dnsRecord);
		em.close();
		return resp;
	}

	@POST
	@Path("find/{objectType}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Searches for an object giben by the objectType and the object.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.configuration")
	public CrxResponse findObject(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("objectType") String objectType,
		LinkedHashMap<String,Object> object
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new SystemService(session,em).findObject(objectType, object);
		em.close();
		return resp;
	}

	@POST
	@Path("file")
	@Produces("*/*")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation(value = "Delivers a file from the file system.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.superuser")
	public Response getFile(
		@ApiParam(hidden = true) @Auth Session session,
		@FormDataParam("path")   String  path
	) {
		File file = new File(path);
		ResponseBuilder response = Response.ok((Object) file);
		response.header("Content-Disposition","attachment; filename=\""+ file.getName() + "\"");
		return response.build();
	}

	@GET
	@Path("addon")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the list of available addon.")
	@ApiResponses(value = {
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")
	})
	@RolesAllowed("system.addons")
	public List<String> getAddOns( @ApiParam(hidden = true) @Auth Session session) {
		List<String> res = new ArrayList<String>();
		File addonsDir = new File( cranixBaseDir + "addons" );
		if( addonsDir != null && addonsDir.exists() ) {
			for( String addon : addonsDir.list() ) {
				File tmp = new File(cranixBaseDir + "addons/" + addon);
				if( tmp.isDirectory() ) {
					res.add(addon);
				}
			}
		}
		return res;
	}

	@PUT
	@Path("addon/{name}/{action}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Executes an action for an addon.")
	@ApiResponses(value = {
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")
	})
	@RolesAllowed("system.addons")
	public CrxResponse applyActionForAddon(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("name")	String name,
			@PathParam("action")	String action
	) {
		String[] program	= new String[2];
		StringBuffer reply  = new StringBuffer();
		StringBuffer stderr = new StringBuffer();
		program[0] = cranixBaseDir + "addons/" + name + "/action.sh";
		program[1] =  action;
		if( CrxSystemCmd.exec(program, reply, stderr, null) == 0 ) {
			return new CrxResponse(session,"OK","Service state was set successfully.");
		} else {
			return new CrxResponse(session,"ERROR",stderr.toString());
		}
	}

	@GET
	@Path("addon/{name}/{key}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets some data from an addon.")
	@ApiResponses(value = {
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")
	})
	@RolesAllowed("system.addons")
	public String[] getDataFromAddon(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("name")	String name,
			@PathParam("key")	String key
	) {
		String[] program	= new String[2];
		StringBuffer reply  = new StringBuffer();
		StringBuffer stderr = new StringBuffer();
		program[0] = cranixBaseDir + "addons/" + name + "/getvalue.sh";
		program[1] = key;
		CrxSystemCmd.exec(program, reply, stderr, null);
		return reply.toString().split("\\s");
	}
}
