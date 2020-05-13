/* (c) 2017 Peter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.api.resources;

import static de.cranix.api.resources.Resource.*;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.*;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.cranix.dao.Acl;
import de.cranix.dao.DnsRecord;
import de.cranix.dao.Job;
import de.cranix.dao.CrxResponse;
import de.cranix.dao.ProxyRule;
import de.cranix.dao.Session;
import de.cranix.dao.Translation;

@Path("system")
@Api(value = "system")
public interface SystemResource {

	@GET
	@Path("name")
	@Produces(TEXT)
	@ApiOperation(value = "Gets the name of the institute.")
	@ApiResponses(value = {
	        @ApiResponse(code = 401, message = "No regcode was found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	String getName(
	        @Context UriInfo ui,
	        @Context HttpServletRequest req
	);

	@GET
	@Path("type")
	@Produces(TEXT)
	@ApiOperation(value = "Gets the type of the institute.")
	@ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	String getType(
	        @Context UriInfo ui,
	        @Context HttpServletRequest req
	);

	@GET
	@Path("status")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the system status.")
	@ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.status")
	Object getStatus(
	    @ApiParam(hidden = true) @Auth Session session
	);

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
	Object getDiskStatus(
	    @ApiParam(hidden = true) @Auth Session session
	);

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
	Object getServicesStatus(
	    @ApiParam(hidden = true) @Auth Session session
	);

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
	CrxResponse setServicesStatus(
	    @ApiParam(hidden = true) @Auth Session session,
	    @PathParam("name")  String name,
	    @PathParam("what")  String what,
	    @PathParam("value") String value
	);

	//Customize the lookout of the start side
	@POST
	@Path("customize")
	@Produces(JSON_UTF8)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation(value = "Upload picture for oss logon site.")
	@ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator") }
	)
	@RolesAllowed("system.customize")
	CrxResponse customize(@ApiParam(hidden = true) @Auth Session session,
	        @FormDataParam("file") final InputStream fileInputStream,
	        @FormDataParam("file") final FormDataContentDisposition contentDispositionHeader
	);
	//Handling of enumerates

	@GET
	@Path("enumerates/{type}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "get session status")
	@ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	List<String> getEnumerates(
	    @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("type") String type
	);

	@PUT
	@Path("enumerates/{type}/{value}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Creates a new enumerate")
	@ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.enumerates")
	CrxResponse addEnumerate(
	    @ApiParam(hidden = true) @Auth Session session,
	    @PathParam("type") String type,
	    @PathParam("value") String value
	);

	@DELETE
	@Path("enumerates/{type}/{value}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Deletes an enumerate")
	@ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.enumerates")
	CrxResponse deleteEnumerate(
	    @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("type") String type,
	        @PathParam("value") String value
	);

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
	List<Map<String, String>>  getConfig(
	    @ApiParam(hidden = true) @Auth Session session
	    );

	@GET
	@Path("configuration/{key}")
	@Produces(TEXT)
	@ApiOperation(value = "Gets a system configuration value.")
	@ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	String getConfig(
	    @ApiParam(hidden = true) @Auth Session session,
	    @PathParam("key") String key
	    );

	@PUT
	@Path("configuration/{key}/{value}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Sets a system configuration.")
	@ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.configuration")
	CrxResponse setConfig(
	    @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("key") String key,
	        @PathParam("value") String value
	);

	@POST
	@Path("configuration")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Sets a system configuration in a map."
	+ "* The map must have following format:"
	+ "* {key:<key>,value:<value>}")
	@ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.configuration")
	CrxResponse setConfig(
	    @ApiParam(hidden = true) @Auth Session session,
	   Map<String, String> config
	);

	// Firewall configuration
	@GET
	@Path("firewall/incomingRules")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the incoming firewall rules.")
	@ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.firewall")
	Map<String, String>  getFirewallIncomingRules(
	    @ApiParam(hidden = true) @Auth Session session
	    );

	@POST
	@Path("firewall/incomingRules")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Sets the incoming firewall rules.")
	@ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.firewall")
	CrxResponse  setFirewallIncomingRules(
	    @ApiParam(hidden = true) @Auth Session session,
	    Map<String, String> incomingRules
	    );

	@GET
	@Path("firewall/outgoingRules")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the incoming firewall rules.")
	@ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.firewall")
	List<Map<String, String>>  getFirewallOutgoingRules(
	    @ApiParam(hidden = true) @Auth Session session
	    );

	@POST
	@Path("firewall/outgoingRules")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Sets the incoming firewall rules.")
	@ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.firewall")
	CrxResponse  setFirewallOutgoingRules(
	    @ApiParam(hidden = true) @Auth Session session,
	    List<Map<String, String>> incomingRules
	    );

	@GET
	@Path("firewall/remoteAccessRules")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the incoming firewall rules.")
	@ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.firewall")
	List<Map<String, String>>  getFirewallRemoteAccessRules(
	    @ApiParam(hidden = true) @Auth Session session
	    );

	@POST
	@Path("firewall/remoteAccessRules")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Sets the incoming firewall rules.")
	@ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.firewall")
	CrxResponse  setFirewallRemoteAccessRules(
	    @ApiParam(hidden = true) @Auth Session session,
	    List<Map<String, String>> incomingRules
	    );

	/*
	 * Translations stuff
	 */
	@POST
	@Path("translate")
	@Produces(TEXT)
	@ApiOperation(value = "Translate a text into a given language")
	@ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	String translate(
	    @ApiParam(hidden = true) @Auth Session session,
	    Translation translation
	);

	@POST
	@Path("translations")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Add or updates a translation.")
	@ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.translation")
	CrxResponse addTranslation(
	    @ApiParam(hidden = true) @Auth Session session,
	    Translation    translation
	);

	@GET
	@Path("missedTranslations/{lang}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get the list of the missed translations to a language")
	@ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.translation")
	List<Translation> getMissedTranslations(
	    @ApiParam(hidden = true) @Auth Session session,
	    @PathParam("lang") String lang
	);

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
	CrxResponse register(
	    @ApiParam(hidden = true) @Auth Session session
	);

	/*
	 * Package handling
	 */
	@GET
	@Path("packages/{filter}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Searches packages.")
	@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	@RolesAllowed("system.packages")
	List<Map<String,String>> searchPackages(
	    @ApiParam(hidden = true) @Auth Session session,
	    @PathParam("filter") String filter
	    );

	@POST
	@Path("packages")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Install packages.")
	@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	@RolesAllowed("system.packages")
	CrxResponse installPackages(
	    @ApiParam(hidden = true) @Auth Session session,
	    List<String> packages
	    );

	@POST
	@Path("packages/update")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Update packages.")
	@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	@RolesAllowed("system.packages")
	CrxResponse updatePackages(
	    @ApiParam(hidden = true) @Auth Session session,
	    List<String> packages
	    );


	@PUT
	@Path("update")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Install all updates on the system.")
	@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	@RolesAllowed("system.update")
	CrxResponse updateSyste(
	    @ApiParam(hidden = true) @Auth Session session
	    );

	/*
	 * Proxy default handling
	 */
	@GET
	@Path("proxy/default/{role}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delivers the default setting for proxy.")
	@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	@RolesAllowed("system.proxy")
	List<ProxyRule> getProxyDefault(
	    @ApiParam(hidden = true) @Auth Session session,
	    @PathParam("role") String role
	    );

	@POST
	@Path("proxy/default/{role}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delivers the default setting for proxy.")
	@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	@RolesAllowed("system.proxy")
	CrxResponse setProxyDefault(
	    @ApiParam(hidden = true) @Auth Session session,
	    @PathParam("role") String role,
	    List<ProxyRule> acl
	    );

	/*
	 * Proxy default handling
	*/
	@GET
	@Path("proxy/defaults")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delivers the default setting for proxy.")
	@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	@RolesAllowed("system.proxy")
	Map<String,List<ProxyRule>> getProxyDefaults(
	    @ApiParam(hidden = true) @Auth Session session
	);

	@POST
	@Path("proxy/defaults")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delivers the default setting for proxy.")
	@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	@RolesAllowed("system.proxy")
	CrxResponse setProxyDefaults(
	    @ApiParam(hidden = true) @Auth Session session,
	    @PathParam("role") String role,
	    Map<String,List<ProxyRule>> acls
	    );


	@GET
	@Path("proxy/custom/{list}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delivers the custom lists of the proxy: good or bad.")
	@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	@RolesAllowed("system.proxy")
	List<String> getTheCustomList(
	    @ApiParam(hidden = true) @Auth Session session,
	    @PathParam("list") String list
	    );

	@POST
	@Path("proxy/custom/{list}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Sets the custom lists of the proxy: good or bad.")
	@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	@RolesAllowed("system.proxy")
	CrxResponse setTheCustomList(
	    @ApiParam(hidden = true) @Auth Session session,
	    @PathParam("list")        String list,
	    List<String> domains
	    );

	/*
	 * Job management
	 */
	@POST
	@Path("jobs/add")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Creates a new job")
	@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	@RolesAllowed("system.jobs")
	CrxResponse createJob(
	    @ApiParam(hidden = true) @Auth Session session,
	    Job job
	);

	@POST
	@Path("jobs/search")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Searching for jobs by description and time.")
	@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	@RolesAllowed("system.jobs")
	List<Job> searchJob(
	    @ApiParam(hidden = true) @Auth Session session,
	    Job job
	);

	@GET
	@Path("jobs/{jobId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the job with all parameters inclusive log.")
	@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	@RolesAllowed("system.jobs")
	Job getJob(
	    @ApiParam(hidden = true) @Auth Session session,
	    @PathParam("jobId") Long jobId
	);

	@GET
	@Path("jobs/running")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the job with all parameters inclusive log.")
	@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	@RolesAllowed("system.jobs")
	List<Job> getRunningJobs(
	    @ApiParam(hidden = true) @Auth Session session
	);

	@GET
	@Path("jobs/failed")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the job with all parameters inclusive log.")
	@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	@RolesAllowed("system.jobs")
	List<Job> getFailedJobs(
	    @ApiParam(hidden = true) @Auth Session session
	);

	@GET
	@Path("jobs/succeeded")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the job with all parameters inclusive log.")
	@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	@RolesAllowed("system.jobs")
	List<Job> getSucceededJobs(
	    @ApiParam(hidden = true) @Auth Session session
	);

	@PUT
	@Path("jobs/{jobId}/exit/{exitValue}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Set the exit value of a job.")
	@ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.jobs")
	CrxResponse setJobExitValue(
	    @ApiParam(hidden = true) @Auth Session session,
	    @PathParam("jobId") Long jobId,
	    @PathParam("exitValue") Integer exitValue
	);

	@PUT
	@Path("jobs/{jobId}/restart")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Set the exit value of a job.")
	@ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.jobs")
	CrxResponse restartJob(
	    @ApiParam(hidden = true) @Auth Session session,
	    @PathParam("jobId") Long jobId
	);

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
	List<Acl> getAcls(
	    @ApiParam(hidden = true) @Auth Session session
	);

	@GET
	@Path("acls/groups/{groupId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get the acls of a group.")
	@ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.acls")
	List<Acl> getAclsOfGroup(
	    @ApiParam(hidden = true) @Auth Session session,
	    @PathParam("groupId") Long groupId
	);

	@GET
	@Path("acls/groups/{groupId}/available")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get the available acls for a group.")
	@ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.acls")
	List<Acl> getAvailableAclsForGroup(
	    @ApiParam(hidden = true) @Auth Session session,
	    @PathParam("groupId") Long groupId
	);

	@POST
	@Path("acls/groups/{groupId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Set an ACL of a group. This can be an existing or a new acl.")
	@ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.acls")
	CrxResponse setAclOfGroup(
	    @ApiParam(hidden = true) @Auth Session session,
	    @PathParam("groupId") Long groupId,
	    Acl acl
	);

	@DELETE
	@Path("acls/groups/{groupId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Removes all ACLs of a group.")
	@ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.acls")
	CrxResponse deleteAclsOfGroup(
	    @ApiParam(hidden = true) @Auth Session session,
	    @PathParam("groupId") Long groupId
	);

	@GET
	@Path("acls/users/{userId}/available")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get the available acls for a user.")
	@ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.acls")
	List<Acl> getAvailableAclsForUser(
	    @ApiParam(hidden = true) @Auth Session session,
	    @PathParam("userId") Long userId
	);

	@GET
	@Path("acls/users/{userId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get the acls of a user.")
	@ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.acls")
	List<Acl> getAclsOfUser(
	    @ApiParam(hidden = true) @Auth Session session,
	    @PathParam("userId") Long userId
	);

	@POST
	@Path("acls/users/{userId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Set an ACL of a user. This can be an existing or a new acl.")
	@ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.acls")
	CrxResponse setAclOfUser(
	    @ApiParam(hidden = true) @Auth Session session,
	    @PathParam("userId") Long userId,
	    Acl acl
	);

	@DELETE
	@Path("acls/user/{userId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Removes all ACLs of a user.")
	@ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.acls")
	CrxResponse deleteAclsOfUser(
	    @ApiParam(hidden = true) @Auth Session session,
	    @PathParam("userId") Long userId
	);

	/**
	 * Delivers the list of the DNS-Domains the server is responsible for these.
	 * @param session
	 * @return The list of the domains.
	 */
	@GET
	@Path("dns/domains")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delivers the list of the DNS-Domains the server is responsible for these.")
	@ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.dns")
	String[] getDnsDomains(
	    @ApiParam(hidden = true) @Auth Session session
	);

	/**
	 * Creates a new DNS domain
	 * @param session
	 * @param domainName The DNS domain name.
	 * @return The result in OssReponse object.
	 */
	@POST
	@Path("dns/domains")
	@Produces(JSON_UTF8)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation(value = "Creates a new DNS domain.")
	@ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.dns")
	CrxResponse addDnsDomain(
	    @ApiParam(hidden = true) @Auth Session session,
	    @FormDataParam("domainName")   String  domainName
	);

	/**
	 * Creates a new DNS domain
	 * @param session
	 * @param domainName The DNS domain name.
	 * @return The result in OssReponse object.
	 */
	@POST
	@Path("dns/domains/delete")
	@Produces(JSON_UTF8)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation(value = "Deleets an existing DNS domain.")
	@ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.dns")
	CrxResponse deleteDnsDomain(
	    @ApiParam(hidden = true) @Auth Session session,
	    @FormDataParam("domainName")   String  domainName
	);

	/**
	 * Delivers the list of the dns records in a domain
	 * @param session
	 * @param domainName The DNS domain name.
	 * @return The result in OssReponse object.
	 */
	@POST
	@Path("dns/domains/records")
	@Produces(JSON_UTF8)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation(value = "Delivers the list of the dns records in a domain.")
	@ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.dns")
	List<DnsRecord> getRecords(
	    @ApiParam(hidden = true) @Auth Session session,
	    @FormDataParam("domainName")   String  domainName
	);

	/**
	 * Creates a new DNS record in a domain.
	 * @param session
	 * @param domainName The DNS domain name.
	 * @param recordType A|AAAA|PTR|CNAME|NS|MX|SRV|TXT
	 * @param recordName
	 * @param recordData
	 * @return
	 */
	@POST
	@Path("dns/domains/addRecord")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Creates a new DNS record. The following Record types are allowed: A|AAAA|PTR|CNAME|NS|MX|SRV|TXT")
	@ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.dns")
	CrxResponse addDnsRecord(
	    @ApiParam(hidden = true) @Auth Session session,
	    DnsRecord dnsRecord
	);

	/**
	 * Deletes an existing DNS record in a domain.
	 * @param session
	 * @param domainName The DNS domain name.
	 * @param recordType A|AAAA|PTR|CNAME|NS|MX|SRV|TXT
	 * @param recordName
	 * @param recordData
	 * @return
	 */
	@POST
	@Path("dns/domains/deleteRecord")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delets an existing DNS record.")
	@ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.dns")
	CrxResponse deleteDnsRecord(
	    @ApiParam(hidden = true) @Auth Session session,
	    DnsRecord dnsRecord
	);

	@POST
	@Path("find/{objectType}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Searches for an object giben by the objectType and the object.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.configuration")
	CrxResponse findObject(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("objectType") String objectType,
		LinkedHashMap<String,Object> object
	);

	@POST
	@Path("file")
	@Produces("*/*")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation(value = "Delivers a file from the file system.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("system.superuser")
	Response getFile(
		@ApiParam(hidden = true) @Auth Session session,
		@FormDataParam("path")   String  path
	);

	@GET
	@Path("addon")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the list of available addon.")
	@ApiResponses(value = {
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")
	})
	@RolesAllowed("system.addons")
	List<String> getAddOns(
			@ApiParam(hidden = true) @Auth Session session
	);

	@PUT
	@Path("addon/{name}/{action}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Executes an action for an addon.")
	@ApiResponses(value = {
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")
	})
	@RolesAllowed("system.addons")
	CrxResponse applyActionForAddon(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("name")	String name,
			@PathParam("action")	String action
	);

	@GET
	@Path("addon/{name}/{key}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets some data from an addon.")
	@ApiResponses(value = {
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")
	})
	@RolesAllowed("system.addons")
	String[] getDataFromAddon(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("name")	String name,
			@PathParam("key")	String key
	);
}
