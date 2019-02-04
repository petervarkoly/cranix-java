/* (c) Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.openschoolserver.api.resourceimpl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.openschoolserver.api.resources.SystemResource;
import de.openschoolserver.dao.Acl;
import de.openschoolserver.dao.DnsRecord;
import de.openschoolserver.dao.Job;
import de.openschoolserver.dao.OssResponse;
import de.openschoolserver.dao.ProxyRule;
import de.openschoolserver.dao.Session;
import de.openschoolserver.dao.Translation;
import de.openschoolserver.dao.controller.SystemController;
import de.openschoolserver.dao.internal.CommonEntityManagerFactory;
import de.openschoolserver.dao.tools.OSSShellTools;
import de.openschoolserver.dao.controller.ProxyController;
import de.openschoolserver.dao.controller.SessionController;
import de.openschoolserver.dao.controller.Controller;
import de.openschoolserver.dao.controller.JobController;

public class SystemResourceImpl implements SystemResource {

	Logger logger = LoggerFactory.getLogger(SystemResourceImpl.class);

	private EntityManager em;

	public SystemResourceImpl() {
		super();
		em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
	}

	protected void finalize()
	{
	   em.close();
	}
	@Override
	public Object getStatus(Session session) {
		SystemController systemController = new SystemController(session,em);
		return systemController.getStatus();
	}

	@Override
	public Object getDiskStatus(Session session) {
		String[] program    = new String[1];
		StringBuffer reply  = new StringBuffer();
		StringBuffer stderr = new StringBuffer();
		program[0] = "/usr/share/oss/tools/check_partitions.sh";
		OSSShellTools.exec(program, reply, stderr, null);
		return reply.toString();
	}

	@Override
	public OssResponse customize(Session session, InputStream fileInputStream,
			FormDataContentDisposition contentDispositionHeader) {
		String fileName = contentDispositionHeader.getFileName();
		File file = new File("/srv/www/admin/assets/" + fileName );
		try {
			Files.copy(fileInputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			return new OssResponse(session,"ERROR", e.getMessage());
		}
		return new OssResponse(session,"OK", "File was saved succesfully.");
	}

	@Override
	public List<String> getEnumerates(Session session, String type) {
		SystemController systemController = new SystemController(session,em);
		return systemController.getEnumerates(type);
	}

	@Override
	public OssResponse addEnumerate(Session session, String type, String value) {
		SystemController systemController = new SystemController(session,em);
		return systemController.addEnumerate(type, value);
	}

	@Override
	public OssResponse deleteEnumerate(Session session, String type, String value) {
		SystemController systemController = new SystemController(session,em);
		return systemController.deleteEnumerate(type, value);
	}

	@Override
	public List<Map<String, String>> getConfig(Session session) {
		SystemController systemController = new SystemController(session,em);
		return systemController.getConfig();
	}

	@Override
	public String getConfig(Session session, String key) {
		SystemController systemController = new SystemController(session,em);
		return systemController.getConfigValue(key);
	}

	@Override
	public OssResponse setConfig(Session session, String key, String value) {
		SystemController systemController = new SystemController(session,em);
		if( systemController.setConfigValue(key, value) ) {
			return new OssResponse(session,"OK","Global configuration value was set succesfully.");
		} else {
			return new OssResponse(session,"ERROR","Global configuration value could not be set.");
		}
	}

	@Override
	public OssResponse setConfig(Session session, Map<String, String> config) {
		SystemController systemController = new SystemController(session,em);
		try {
			if( systemController.setConfigValue(config.get("key"), config.get("value")) ) {
				return new OssResponse(session,"OK","Global configuration value was set succesfully.");
			} else {
				return new OssResponse(session,"ERROR","Global configuration value could not be set.");
			}
		} catch(Exception e) {
			return new OssResponse(session,"ERROR","Global configuration value could not be set.");
		}
	}

	@Override
	public Map<String, String> getFirewallIncomingRules(Session session) {
		SystemController systemController = new SystemController(session,em);
		return systemController.getFirewallIncomingRules();
	}

	@Override
	public OssResponse setFirewallIncomingRules(Session session, Map<String, String> incommingRules) {
		SystemController systemController = new SystemController(session,em);
		return systemController.setFirewallIncomingRules(incommingRules);
	}

	@Override
	public List<Map<String, String>> getFirewallOutgoingRules(Session session) {
		SystemController systemController = new SystemController(session,em);
		return systemController.getFirewallOutgoingRules();
	}

	@Override
	public OssResponse setFirewallOutgoingRules(Session session, List<Map<String, String>> outgoingRules) {
		SystemController systemController = new SystemController(session,em);
		return systemController.setFirewallOutgoingRules(outgoingRules);
	}

	@Override
	public List<Map<String, String>> getFirewallRemoteAccessRules(Session session) {
		SystemController systemController = new SystemController(session,em);
		return systemController.getFirewallRemoteAccessRules();
	}

	@Override
	public OssResponse setFirewallRemoteAccessRules(Session session, List<Map<String, String>> remoteAccessRules) {
		SystemController systemController = new SystemController(session,em);
		return systemController.setFirewallRemoteAccessRules(remoteAccessRules);
		}

	@Override
	public String translate(Session session, Translation translation) {
		return new SystemController(session,em).translate(translation.getLang(), translation.getString());
	}

	@Override
	public OssResponse addTranslation(Session session, Translation translation) {
		return new SystemController(session,em).addTranslation(translation);
	}

	@Override
	public List<Translation> getMissedTranslations(Session session, String lang) {
		return new SystemController(session,em).getMissedTranslations(lang);
	}

	@Override
	public OssResponse register(Session session) {
		return new SystemController(session,em).registerSystem();
	}

	@Override
	public List<Map<String, String>> searchPackages(Session session, String filter) {
		return new SystemController(session,em).searchPackages(filter);
	}

	@Override
	public OssResponse installPackages(Session session, List<String> packages) {
		return new SystemController(session,em).installPackages(packages);
	}

	@Override
	public OssResponse updatePackages(Session session, List<String> packages) {
		return new SystemController(session,em).updatePackages(packages);
	}

	@Override
	public OssResponse updateSyste(Session session) {
		return new SystemController(session,em).updateSystem();
	}

	@Override
	public  List<ProxyRule> getProxyDefault(Session session, String role) {
		return new ProxyController(session,em).readDefaults(role);
	}

	@Override
	public OssResponse setProxyDefault(Session session, String role, List<ProxyRule> acl) {
		return new ProxyController(session,em).setDefaults(role, acl);
	}

	@Override
	public Map<String, List<ProxyRule>> getProxyDefaults(Session session) {
		return new ProxyController(session,em).readDefaults();
	}

	@Override
	public OssResponse setProxyDefaults(Session session, String role, Map<String, List<ProxyRule>> acls) {
		return new ProxyController(session,em).setDefaults(acls);
	}

	@Override
	public List<String> getTheCustomList(Session session, String list) {
		try {
			return	Files.readAllLines(Paths.get("/var/lib/squidGuard/db/custom/" +list + "/domains"));
		}
		catch( IOException e ) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public OssResponse setTheCustomList(Session session, String list, List<String> domains) {
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
			OSSShellTools.exec(program, reply, error, null);
			new Controller(session,em).systemctl("try-restart", "squid");
			return new OssResponse(session,"OK","Custom list was written successfully");
		} catch( IOException e ) {
			e.printStackTrace();
		}
		return new OssResponse(session,"ERROR","Could not write custom list.");
	}

	@Override
	public OssResponse createJob(Session session, Job job) {
		return new JobController(session,em).createJob(job);
	}

	@Override
	public List<Job> searchJob(Session session, Job job ) {
		return new JobController(session,em).searchJobs(job.getDescription(), job.getStartTime(), job.getEndTime());
	}

	@Override
	public Job getJob(Session session, Long jobId) {
		return new JobController(session,em).getById(jobId);
	}

	@Override
	public OssResponse setJobExitValue(Session session, Long jobId, Integer exitValue) {
		return new JobController(session,em).setExitCode(jobId, exitValue);
	}

	@Override
	public OssResponse restartJob(Session session, Long jobId) {
		return new JobController(session,em).restartJob(jobId);
	}

	/*
	 * (non-Javadoc)
	 * ACL management
	 */
	@Override
	public List<Acl> getAcls(Session session) {
		return new SystemController(session,em).getAvailableAcls();
	}

	@Override
	public List<Acl> getAclsOfGroup(Session session, Long groupId) {
		return new SystemController(session,em).getAclsOfGroup(groupId);
	}

	@Override
	public OssResponse setAclOfGroup(Session session, Long groupId, Acl acl) {
		return new SystemController(session,em).setAclToGroup(groupId,acl);
	}

	@Override
	public List<Acl> getAvailableAclsForGroup(Session session, Long groupId) {
		return new SystemController(session,em).getAvailableAclsForGroup(groupId);
	}

	@Override
	public List<Acl> getAclsOfUser(Session session, Long userId) {
		return new SystemController(session,em).getAclsOfUser(userId);
	}

	@Override
	public OssResponse setAclOfUser(Session session, Long userId, Acl acl) {
		return new SystemController(session,em).setAclToUser(userId,acl);
	}

	@Override
	public List<Acl> getAvailableAclsForUser(Session session, Long userId) {
		return new SystemController(session,em).getAvailableAclsForUser(userId);
	}

	@Override
	public String getName(UriInfo ui, HttpServletRequest req) {
		Session session  = new SessionController(em).getLocalhostSession();
		return new SystemController(session,em).getConfigValue("NAME");
	}

	@Override
	public String getType(UriInfo ui, HttpServletRequest req) {
		Session session  = new SessionController(em).getLocalhostSession();
		return new SystemController(session,em).getConfigValue("TYPE");
	}

	@Override
	public List<Job> getRunningJobs(Session session) {
		return new JobController(session,em).getRunningJobs();
	}

	@Override
	public List<Job> getFailedJobs(Session session) {
		return new JobController(session,em).getFailedJobs();
	}

	@Override
	public List<Job> getSucceededJobs(Session session) {
		return new JobController(session,em).getSucceededJobs();
	}

	@Override
	public String[] getDnsDomains(Session session) {
		return new SystemController(session,em).getDnsDomains();
	}

	@Override
	public OssResponse addDnsDomain(Session session, String domainName) {
		return new SystemController(session,em).addDnsDomain(domainName);
	}

	@Override
	public OssResponse deleteDnsDomain(Session session, String domainName) {
		return new SystemController(session,em).deleteDnsDomain(domainName);
	}
	@Override
	public List<DnsRecord> getRecords(Session session, String domainName) {
		return new SystemController(session,em).getRecords(domainName);
	}

	@Override
	public OssResponse addDnsRecord(Session session, DnsRecord dnsRecord) {
		return new SystemController(session,em).addDnsRecord(dnsRecord);
	}

	@Override
	public OssResponse deleteDnsRecord(Session session, DnsRecord dnsRecord) {
		return new SystemController(session,em).deleteDnsRecord(dnsRecord);
	}

	@Override
	public OssResponse findObject(Session session, String objectType, LinkedHashMap<String,Object> object) {
		return new SystemController(session,em).findObject(objectType, object);
	}

	@Override
	public Response getFile(Session session, String path) {
		logger.debug("getFile" + path);
		File file = new File(path);
		ResponseBuilder response = Response.ok((Object) file);
		response.header("Content-Disposition","attachment; filename=\""+ file.getName() + "\"");
		return response.build();
	}

	@Override
	public Object getServicesStatus(Session session) {
		String[] program    = new String[1];
		StringBuffer reply  = new StringBuffer();
		StringBuffer stderr = new StringBuffer();
		program[0] = "/usr/share/oss/tools/check_services.sh";
		OSSShellTools.exec(program, reply, stderr, null);
		return reply.toString();
	}

	@Override
	public OssResponse setServicesStatus(Session session, String name, String what, String value) {
		String[] program    = new String[3];
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
		logger.debug(program[0] + " " + program[1] + " " + program[2]);
		if( OSSShellTools.exec(program, reply, stderr, null) == 0 ) {
			return new OssResponse(session,"OK","Service state was set successfully.");
		} else {
			return new OssResponse(session,"ERROR",stderr.toString());
		}
	}
}
