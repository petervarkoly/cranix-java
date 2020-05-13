/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.api.resourceimpl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.cranix.api.resources.UserResource;
import de.cranix.dao.Alias;
import de.cranix.dao.Category;
import de.cranix.dao.Group;
import de.cranix.dao.GuestUsers;
import de.cranix.dao.CrxActionMap;
import de.cranix.dao.Session;
import de.cranix.dao.User;
import de.cranix.dao.UserImport;
import de.cranix.dao.controller.Controller;
import de.cranix.dao.controller.GroupController;
import de.cranix.dao.controller.UserController;
import de.cranix.dao.internal.CommonEntityManagerFactory;
import de.cranix.dao.tools.OSSShellTools;
import de.cranix.dao.CrxResponse;
import static de.cranix.dao.internal.CranixConstants.*;

public class UserResourceImpl implements UserResource {

	Logger logger = LoggerFactory.getLogger(UserResourceImpl.class);

	public UserResourceImpl() {
	}

	@Override
	public User getById(Session session, Long userId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final UserController userController = new UserController(session,em);
		final User user = userController.getById(userId);
		em.close();
		 if (user == null) {
	            throw new WebApplicationException(404);
		}
		return user;
	}

	@Override
	public List<User> getByRole(Session session, String role) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final UserController userController = new UserController(session,em);
		final List<User> users = userController.getByRole(role);
		em.close();
		if (users == null) {
			throw new WebApplicationException(404);
		}
		return users;
	}

	@Override
	public List<User> getAll(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final UserController userController = new UserController(session,em);
		final List<User> users = userController.getAll();
		em.close();
		if (users == null) {
			throw new WebApplicationException(404);
		}
		return users;
	}

	@Override
	public CrxResponse insert(Session session, User user) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse ossResponse =  new UserController(session,em).add(user);
		em.close();
		return ossResponse;
	}

	@Override
	public CrxResponse add(Session session, User user) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse ossResponse =  new UserController(session,em).add(user);
		em.close();
		if( ossResponse.getCode().equals("OK")) {
			sync(session);
		}
		return ossResponse;
	}

	@Override
	public List<CrxResponse> add(Session session, List<User> users) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<CrxResponse> ossResponses =  new UserController(session,em).add(users);
		sync(session);
		em.close();
		return ossResponses;
	}

	@Override
	public CrxResponse delete(Session session, Long userId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse ossResponse = new UserController(session,em).delete(userId);
		em.close();
		return ossResponse;
	}

	@Override
	public CrxResponse modify(Session session, User user) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final UserController userController = new UserController(session,em);
		CrxResponse ossResponse = userController.modify(user);
		em.close();
		return ossResponse;
	}

	@Override
	public CrxResponse modify(Session session, Long userId, User user) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		user.setId(userId);
		final UserController userController = new UserController(session,em);
		CrxResponse ossResponse = userController.modify(user);
		em.close();
		return ossResponse;
	}

	@Override
	public List<User> search(Session session, String search) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final UserController userController = new UserController(session,em);
		final List<User> users = userController.search(search);
		em.close();
		if (users == null) {
            throw new WebApplicationException(404);
		}
		return users;
	}

	@Override
	public List<Group> getAvailableGroups(Session session, Long userId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final UserController userController = new UserController(session,em);
		final List<Group> groups = userController.getAvailableGroups(userId);
		em.close();
		if (groups == null) {
            throw new WebApplicationException(404);
		}
		return groups;
	}

	@Override
	public List<Group> groups(Session session, Long userId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final UserController userController = new UserController(session,em);
		final List<Group> groups =  userController.getGroups(userId);
		em.close();
		if (groups == null) {
            throw new WebApplicationException(404);
		}
		return groups;
	}

	@Override
	public CrxResponse setMembers(Session session, Long userId, List<Long> groupIds) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse ossResponse =  new UserController(session,em).setGroups(userId,groupIds);
		em.close();
		return ossResponse;
	}

	@Override
	public CrxResponse removeMember(Session session, Long groupId, Long userId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final GroupController groupController = new GroupController(session,em);
		CrxResponse ossResponse = groupController.removeMember(groupId,userId);
		em.close();
		return ossResponse;
	}

	@Override
	public CrxResponse addToGroups(Session session, Long userId, List<Long> groups) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		StringBuilder error = new StringBuilder();
		final GroupController groupController = new GroupController(session,em);
		for( Long groupId : groups ) {
			CrxResponse ossResponse = groupController.addMember(groupId,userId);
			if( !ossResponse.getCode().equals("OK")  ) {
				error.append(ossResponse.getValue()).append("<br>");
			}
		}
		em.close();
		if( error.length() > 0 ) {
			return new CrxResponse(session,"ERROR",error.toString());
		}
		return new CrxResponse(session,"OK","User was added to the additional group.");
	}

	@Override
	public CrxResponse addMember(Session session, Long groupId, Long userId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final GroupController groupController = new GroupController(session,em);
		CrxResponse ossResponse = groupController.addMember(groupId,userId);
		em.close();
		return ossResponse;
	}

	@Override
	public CrxResponse syncFsQuotas(Session session, List<List<String>> Quotas) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final UserController userController = new UserController(session,em);
		CrxResponse ossResponse = userController.syncFsQuotas(Quotas);
		em.close();
		return ossResponse;
	}

	@Override
	public List<User> getUsers(Session session, List<Long> userIds) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final UserController userController = new UserController(session,em);
		List<User> users = userController.getUsers(userIds);
		em.close();
		return users;
	}

	@Override
	public String getUserAttribute(Session session, String uid, String attribute) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final UserController userController = new UserController(session,em);
		String resp;
		User user = userController.getByUid(uid);
		if( user == null) {
			return "";
		}
		switch(attribute.toLowerCase()) {
		case "id":
			resp = String.valueOf(user.getId());
			break;
		case "role":
			resp = user.getRole();
			break;
		case "uuid":
			resp = user.getUuid();
			break;
		case "givenname":
			resp = user.getGivenName();
			break;
		case "surname":
			resp = user.getSurName();
			break;
		case "home":
			resp = userController.getHomeDir(user);
			break;
		case "groups":
			List<String> groups = new ArrayList<String>();
			for( Group group : user.getGroups() ) {
				groups.add(group.getName());
			}
			resp = String.join(userController.getNl(), groups);
			break;
		default:
			//This is a config or mconfig. We have to merge it from the groups from actual room and from the user
			List<String> configs = new ArrayList<String>();
			//Group configs
			for( Group group : user.getGroups() ) {
				if( userController.getConfig(group, attribute) != null ) {
					configs.add(userController.getConfig(group, attribute));
				}
				for( String config : userController.getMConfigs(group, attribute) ) {
					if( config != null ) {
						configs.add(config);
					}
				}
			}
			//Room configs.
			if( session.getRoom() != null ) {
				if( userController.getConfig(session.getRoom(), attribute) != null ) {
					configs.add(userController.getConfig(session.getRoom(), attribute));
				}
				for( String config : userController.getMConfigs(session.getRoom(), attribute) ) {
					if( config != null ) {
						configs.add(config);
					}
				}
			}
			if( userController.getConfig(user, attribute) != null ) {
				configs.add(userController.getConfig(user, attribute));
			}
			for( String config : userController.getMConfigs(user, attribute) ) {
				if( config != null ) {
					configs.add(config);
				}
			}
			resp = String.join(userController.getNl(), configs);
		}
		em.close();
		return resp;
	}

	@Override
	public List<Category> getGuestUsers(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<Category> resp = new UserController(session,em).getGuestUsers();
		em.close();
		return resp;
	}

	@Override
	public Category getGuestUsersCategory(Session session, Long guestUsersId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		Category resp = new UserController(session,em).getGuestUsersCategory(guestUsersId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse deleteGuestUsers(Session session, Long guestUsersId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new UserController(session,em).deleteGuestUsers(guestUsersId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse addGuestUsers(Session session, String name, String description, Long roomId, Long count,
			Date validUntil) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		GuestUsers guestUsers = new GuestUsers(name,description,count,roomId,validUntil);
		CrxResponse resp = new UserController(session,em).addGuestUsers(guestUsers);
		em.close();
		return resp;
	}

	@Override
	public String getGroups(Session session, String userName) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		String resp = new UserController(session,em).getGroupsOfUser(userName,"workgroup");
		em.close();
		return resp;
	}

	@Override
	public String getClasses(Session session, String userName) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		String resp = new UserController(session,em).getGroupsOfUser(userName,"class");
		em.close();
		return resp;
	}

	@Override
	public String addToGroup(Session session, String userName, String groupName) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse ossResponse = new GroupController(session,em).addMember(groupName, userName);
		String resp = ossResponse.getCode();
	    if( ossResponse.getCode().equals("ERROR") ) {
			resp = resp + " " + ossResponse.getValue();
		}
		em.close();
		return resp;
	}


	@Override
	public String addGroupToUser(Session session, String userName, String groupName) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse ossResponse = new GroupController(session,em).setOwner(groupName, userName);
		String resp = ossResponse.getCode();
	        if( ossResponse.getCode().equals("ERROR") ) {
			resp = resp + " " + ossResponse.getValue();
		}
		em.close();
		return resp;
	}

	@Override
	public String addUserAlias(Session session, String userName, String alias) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		UserController uc = new UserController(session,em);
		String resp = "Alias not unique";
		if( uc.isUserAliasUnique(alias) ) {
			User user = uc.getByUid(userName);
			if( user != null ) {
				Alias newAlias = new Alias(user,alias);
				em.getTransaction().begin();
				em.persist(newAlias);
				user.getAliases().add(newAlias);
				em.merge(user);
				em.getTransaction().commit();
				resp = "Alias was created";
			} else {
				resp = "User can not be found";
			}
		}
		em.close();
		return resp;
	}

	@Override
	public String addUserDefaultAlias(Session session, String userName) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		UserController uc = new UserController(session,em);
		User user = uc.getByUid(userName);
		CrxResponse ossResponse = uc.addDefaultAliase(user);
		String resp = ossResponse.getCode();
	        if( ossResponse.getCode().equals("ERROR") ) {
			resp = resp + " " + ossResponse.getValue();
		}
		em.close();
		return resp;
	}
	@Override
	public String removeFromGroup(Session session, String userName, String groupName) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse ossResponse = new GroupController(session,em).removeMember(groupName, userName);
		String resp = ossResponse.getCode();
	        if( ossResponse.getCode().equals("ERROR") ) {
			resp = resp + " " + ossResponse.getValue();
		}
		em.close();
		return resp;
	}

	@Override
	public String delete(Session session, String userName) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse ossResponse = new UserController(session,em).delete(userName);
		String resp = ossResponse.getCode();
	        if( ossResponse.getCode().equals("ERROR") ) {
			resp = resp + " " + ossResponse.getValue();
		}
		em.close();
		return resp;
	}

	@Override
	public String createUid(Session session, String givenName, String surName, Date birthDay) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		String resp = new UserController(session,em).createUid(givenName,surName,birthDay);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse importUser(
			Session session,
			String role,
			String lang,
			String identifier,
			boolean test,
			String password,
			boolean mustChange,
			boolean full,
			boolean allClasses,
			boolean cleanClassDirs,
			boolean resetPassword,
			boolean appendBirthdayToPassword,
			InputStream fileInputStream,
			FormDataContentDisposition contentDispositionHeader) {
		File file = null;
		try {
			file = File.createTempFile("oss", "importUser", new File(cranixTmpDir));
			Files.copy(fileInputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return new CrxResponse(session,"ERROR", "Import file can not be saved" + e.getMessage());
		}
		try {
				Files.readAllLines(file.toPath(), Charset.forName("UTF-8"));
		} catch (IOException ioe) {
				logger.error(ioe.getMessage(), ioe);
				return new CrxResponse(session,"ERROR", "Import file is not UTF-8 coded.");
		}
		if( password != null && !password.isEmpty() ) {
			UserController uc = new UserController(session,null);
			boolean checkPassword = uc.getConfigValue("CHECK_PASSWORD_QUALITY").toLowerCase().equals("yes");
			uc.setConfigValue("CHECK_PASSWORD_QUALITY", "no");
			CrxResponse passwordResponse = uc.checkPassword(password);
			if( passwordResponse != null ) {
				if (checkPassword) {
					uc.setConfigValue("CHECK_PASSWORD_QUALITY", "yes");
				}
				logger.error("Reset Password" + passwordResponse);
				return passwordResponse;
			}
		}
		List<String> parameters = new ArrayList<String>();
		parameters.add("/sbin/startproc");
		parameters.add("-l");
		parameters.add("/var/log/import-user.log");
		parameters.add("/usr/sbin/oss_import_user_list.py");
		parameters.add("--input");
		parameters.add(file.getAbsolutePath());
		parameters.add("--role");
		parameters.add(role);
		parameters.add("--lang");
		parameters.add(lang);
		if( identifier != null && !identifier.isEmpty() ) {
			parameters.add("--identifier");
			parameters.add(identifier);
		}
		if( test ) {
			parameters.add("--test");
		}
		if( password != null && ! password.isEmpty() ) {
			parameters.add("--password");
			parameters.add(password);
		}
		if( mustChange ) {
			parameters.add("--mustchange");
		}
		if( full ) {
			parameters.add("--full");
		}
		if( allClasses ) {
			parameters.add("--allClasses");
		}
		if( cleanClassDirs ) {
			parameters.add("--cleanClassDirs");
		}
		if( resetPassword ) {
			parameters.add("--resetPassword");
		}
		if( appendBirthdayToPassword ) {
			parameters.add("--appendBirthdayToPassword");
		}
		if( logger.isDebugEnabled() ) {
			parameters.add("--debug");
		}
		String[] program = new String[parameters.size()];
		program = parameters.toArray(program);

		logger.debug("Start import:" + parameters);
		StringBuffer reply  = new StringBuffer();
		StringBuffer stderr = new StringBuffer();
		OSSShellTools.exec(program, reply, stderr, null);
		return new CrxResponse(session,"OK", "Import was started.");
	}

	@Override
	public List<UserImport> getImports(Session session) {
		Controller controller    = new Controller(session,null);
		StringBuilder importDir  = controller.getImportDir("");
		List<UserImport> imports = new ArrayList<UserImport>();
		File importDirObject = new File(importDir.toString());
		if( importDirObject.isDirectory() ) {
			for( String file :  importDirObject.list() ) {
				UserImport userImport = getImport(session,file.replaceAll(importDir.append("/").toString(), ""));
				if( userImport != null ) {
					imports.add(userImport);
				}
			}
		}
		return imports;
	}

	@Override
	public UserImport getImport(Session session, String startTime) {
		Controller controller    = new Controller(session,null);
		String content;
		UserImport userImport;
		String importLog  = controller.getImportDir(startTime).append("/import.log").toString();
		String importJson = controller.getImportDir(startTime).append("/parameters.json").toString();
		ObjectMapper mapper = new ObjectMapper();
		logger.debug("getImport 1:"+ startTime);
		try {
			content = String.join("", Files.readAllLines(Paths.get(importJson)));
			userImport =  mapper.readValue(IOUtils.toInputStream(content, "UTF-8"),UserImport.class);
		} catch( IOException e ) {
			logger.debug("getImport 2:"+ e.getMessage());
			return null;
		}
		try {
			content = String.join("", Files.readAllLines(Paths.get(importLog),Charset.forName("UTF-8")));
			userImport.setResult(content);
		} catch( IOException e ) {
			logger.debug("getImport 3:"+ importLog + " " + content + "####" + e.getMessage());
		}
		return userImport;
	}

	@Override
	public CrxResponse restartImport(Session session, String startTime) {
		UserImport userImport = getImport(session, startTime);
		if( userImport != null ) {
			Controller controller    = new Controller(session,null);
			StringBuilder importFile = controller.getImportDir(startTime);
			importFile.append("/userlist.txt");
			List<String> parameters = new ArrayList<String>();
			parameters.add("/sbin/startproc");
			parameters.add("-l");
			parameters.add("/var/log/import-user.log");
			parameters.add("/usr/sbin/oss_import_user_list.py");
			parameters.add("--input");
			parameters.add(importFile.toString());
			parameters.add("--role");
			parameters.add(userImport.getRole());
			parameters.add("--lang");
			parameters.add(userImport.getLang());
			parameters.add("--identifier");
			parameters.add(userImport.getIdentifier());
			if( ! userImport.getPassword().isEmpty() ) {
				parameters.add("--password");
				parameters.add(userImport.getPassword());
			}
			if( userImport.isMustchange()  ) {
				parameters.add("--mustchange");
			}
			if( userImport.isFull() ) {
				parameters.add("--full");
			}
			if( userImport.isAllClasses() ) {
				parameters.add("--allClasses");
			}
			if( userImport.isCleanClassDirs() ) {
				parameters.add("--cleanClassDirs");
			}
			if( userImport.isResetPassword() ) {
				parameters.add("--resetPassword");
			}
			if( logger.isDebugEnabled() ) {
				parameters.add("--debug");
			}
			logger.debug("restartImport userImport:" + userImport);
			logger.debug("restartImport parameters:" + parameters);
			
			String[] program = new String[parameters.size()];
			program = parameters.toArray(program);

			logger.debug("Start import:" + parameters);
			StringBuffer reply  = new StringBuffer();
			StringBuffer stderr = new StringBuffer();
			OSSShellTools.exec(program, reply, stderr, null);
			logger.debug("restartImport reply: " + reply.toString());
			logger.debug("restartImport error: " + reply.toString());
			return new CrxResponse(session,"OK", "Import was started.");
		}
		return new CrxResponse(session,"ERROR", "CAn not find the import.");
	}

	@Override
	public CrxResponse deleteImport(Session session, String startTime) {
		Controller controller    = new Controller(session,null);
		StringBuilder importDir  = controller.getImportDir(startTime);
		if( startTime == null || startTime.isEmpty() ) {
			return new CrxResponse(session,"ERROR", "Invalid import name.");
		}
		String[] program = new String[3];
		program[0] = "rm";
		program[1] = "-rf";
		program[2] = importDir.toString();
		StringBuffer reply  = new StringBuffer();
		StringBuffer stderr = new StringBuffer();
		OSSShellTools.exec(program, reply, stderr, null);
		return new CrxResponse(session,"OK", "Import was deleted.");
	}

	@Override
	public UserImport getRunningImport(Session session) {
		List<String> runningImport;
		try {
			runningImport = Files.readAllLines(Paths.get("/run/oss_import_user"));
			if( !runningImport.isEmpty() ) {
				return getImport(session,runningImport.get(0));
			}
		} catch( IOException e ) {
			return null;
		}
		return null;
	}

	@Override
	public Response getImportAsPdf(Session session, String startTime) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response getImportAsTxt(Session session, String startTime) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CrxResponse syncMsQuotas(Session session, List<List<String>> Quotas) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new UserController(session,em).syncMsQuotas(Quotas);
		em.close();
		return resp;
	}

	@Override
	public String getUidsByRole(Session session, String role) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final UserController userController = new UserController(session,em);
		List<String> users = new ArrayList<String>();
		for( User user : userController.getByRole(role) ) {
			users.add(user.getUid());
		}
		String resp = String.join(userController.getNl(),users);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse allTeachersInAllClasses(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final UserController userController = new UserController(session,em);
		final GroupController groupController = new GroupController(session,em);
		for( User user : userController.getByRole("teachers") ) {
			for( Group group : groupController.getByType("class")) {
				groupController.addMember(group, user);
			}
		}
		em.close();
		return new CrxResponse(session,"OK", "All teachers was put into all classes.");
	}

	@Override
	public CrxResponse allClasses(Session session, Long userId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		User user = new UserController(session,em).getById(userId);
		final GroupController groupController = new GroupController(session,em);
		for( Group group : groupController.getByType("class")) {
			groupController.addMember(group, user);
		}
		em.close();
		return new CrxResponse(session,"OK", "User was put into all classes.");
	}

	@Override
	public String addToAllClasses(Session session, String userName) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		User user = new UserController(session,em).getByUid(userName);
		final GroupController groupController = new GroupController(session,em);
		for( Group group : groupController.getByType("class")) {
			groupController.addMember(group, user);
		}
		em.close();
		return "OK";
	}

	@Override
	public CrxResponse sync(Session session) {
		//TODO make it over plugin
		String[] program = new String[1];
		program[0] = "/usr/sbin/oss_refresh_squidGuard_user.sh";
		StringBuffer reply  = new StringBuffer();
		StringBuffer stderr = new StringBuffer();
		OSSShellTools.exec(program, reply, stderr, null);
		return new CrxResponse(session,"OK", "Import was started.");
	}

	@Override
	public CrxResponse addUsersToGroups(Session session, List<List<Long>> ids) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final GroupController groupController = new GroupController(session,em);
		List<User> users = new UserController(session,em).getUsers(ids.get(0));
		for(Long groupId: ids.get(1)) {
			Group group = groupController.getById(groupId);
			groupController.addMembers(group, users);
		}
		em.close();
		return new CrxResponse(session,"OK", "Users was inserted in the required groups.");
	}

	@Override
	public CrxResponse applyAction(Session session, CrxActionMap actionMap) {
		// TODO Auto-generated method stub
		return null;
	}

}
