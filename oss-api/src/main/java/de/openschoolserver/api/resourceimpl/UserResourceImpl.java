/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.openschoolserver.api.resourceimpl;

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

import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.openschoolserver.api.resources.UserResource;
import de.openschoolserver.dao.Category;
import de.openschoolserver.dao.Group;
import de.openschoolserver.dao.Session;
import de.openschoolserver.dao.User;
import de.openschoolserver.dao.UserImport;
import de.openschoolserver.dao.controller.Controller;
import de.openschoolserver.dao.controller.GroupController;
import de.openschoolserver.dao.controller.UserController;
import de.openschoolserver.dao.internal.CommonEntityManagerFactory;
import de.openschoolserver.dao.tools.OSSShellTools;
import de.openschoolserver.dao.OssResponse;

public class UserResourceImpl implements UserResource {

	Logger logger = LoggerFactory.getLogger(UserResourceImpl.class);

	public UserResourceImpl() {
	}

	@Override
	public User getById(Session session, long userId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final UserController userController = new UserController(session,em);
		final User user = userController.getById(userId);
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
		if (users == null) {
			throw new WebApplicationException(404);
		}
		return users;
	}

	@Override
	public OssResponse insert(Session session, User user) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final UserController userController = new UserController(session,em);
		return userController.add(user);
	}

	@Override
	public OssResponse add(Session session, User user) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		OssResponse ossResponse =  new UserController(session,em).add(user);
		if( ossResponse.getCode().equals("OK")) {
			sync(session);
		}
		return ossResponse;
	}

	@Override
	public List<OssResponse> add(Session session, List<User> users) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<OssResponse> ossResponses =  new UserController(session,em).add(users);
		sync(session);
		return ossResponses;
	}

	@Override
	public OssResponse delete(Session session, long userId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final UserController userController = new UserController(session,em);
		return userController.delete(userId);
	}

	@Override
	public OssResponse modify(Session session, User user) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final UserController userController = new UserController(session,em);
		return userController.modify(user);
	}

	@Override
	public List<User> search(Session session, String search) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final UserController userController = new UserController(session,em);
		final List<User> users = userController.search(search);
		if (users == null) {
            throw new WebApplicationException(404);
		}
		return users;
	}

	@Override
	public List<Group> getAvailableGroups(Session session, long userId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final UserController userController = new UserController(session,em);
		final List<Group> groups = userController.getAvailableGroups(userId);
		if (groups == null) {
            throw new WebApplicationException(404);
		}
		return groups;
	}

	@Override
	public List<Group> groups(Session session, long userId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final UserController userController = new UserController(session,em);
		final List<Group> groups =  userController.getGroups(userId);
		if (groups == null) {
            throw new WebApplicationException(404);
		}
		return groups;
	}

	@Override
	public OssResponse setMembers(Session session, long userId, List<Long> groupIds) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		return new UserController(session,em).setGroups(userId,groupIds);
	}

	@Override
	public OssResponse removeMember(Session session, long groupId, long userId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final GroupController groupController = new GroupController(session,em);
		return groupController.removeMember(groupId,userId);
	}

	@Override
	public OssResponse addToGroups(Session session, long userId, List<Long> groups) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		StringBuilder error = new StringBuilder();
		final GroupController groupController = new GroupController(session,em);
		for( Long groupId : groups ) {
			OssResponse ossResponse = groupController.addMember(groupId,userId);
			if( !ossResponse.getCode().equals("OK")  ) {
				error.append(ossResponse.getValue()).append("<br>");
			}
		}
		if( error.length() > 0 ) {
			return new OssResponse(session,"ERROR",error.toString());
		}
		return new OssResponse(session,"OK","User was added to the additional group.");
	}

	@Override
	public OssResponse addMember(Session session, long groupId, long userId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final GroupController groupController = new GroupController(session,em);
		return groupController.addMember(groupId,userId);
	}

	@Override
	public OssResponse syncFsQuotas(Session session, List<List<String>> Quotas) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final UserController userController = new UserController(session,em);
		return userController.syncFsQuotas(Quotas);
	}

	@Override
	public List<User> getUsers(Session session, List<Long> userIds) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final UserController userController = new UserController(session,em);
		return userController.getUsers(userIds);
	}

	@Override
	public String getUserAttribute(Session session, String uid, String attribute) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final UserController userController = new UserController(session,em);
		User user = userController.getByUid(uid);
		if( user == null) {
			return "";
		}
		switch(attribute.toLowerCase()) {
		case "role":
			return user.getRole();
		case "uuid":
			return user.getUuid();
		case "givenname":
			return user.getGivenName();
		case "surname":
			return user.getSurName();
		case "groups":
			List<String> groups = new ArrayList<String>();
			for( Group group : user.getGroups() ) {
				groups.add(group.getName());
			}
			return String.join(userController.getNl(), groups);
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
			return String.join(userController.getNl(), configs);
		}
	}

	@Override
	public List<Category> getGuestUsers(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		return new UserController(session,em).getGuestUsers();
	}

	@Override
	public Category getGuestUsersCategory(Session session, Long guestUsersId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		return new UserController(session,em).getGuestUsersCategory(guestUsersId);
	}

	@Override
	public OssResponse deleteGuestUsers(Session session, Long guestUsersId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		return new UserController(session,em).deleteGuestUsers(guestUsersId);
	}

	@Override
	public OssResponse addGuestUsers(Session session, String name, String description, Long roomId, Long count,
			Date validUntil) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		return new UserController(session,em).addGuestUsers(name, description, roomId, count, validUntil);
	}

	@Override
	public String getGroups(Session session, String userName) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		return new UserController(session,em).getGroupsOfUser(userName,"workgroup");
	}

	@Override
	public String getClasses(Session session, String userName) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		return new UserController(session,em).getGroupsOfUser(userName,"class");
	}

	@Override
	public String addToGroup(Session session, String userName, String groupName) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		return new GroupController(session,em).addMember(groupName, userName).getCode();
	}


	@Override
	public String addGroupToUser(Session session, String userName, String groupName) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		return new GroupController(session,em).setOwner(groupName, userName).getCode();
	}

	@Override
	public String removeFromGroup(Session session, String userName, String groupName) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		return new GroupController(session,em).removeMember(groupName, userName).getCode();
	}

	@Override
	public String delete(Session session, String userName) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		return new UserController(session,em).delete(userName).getCode();
	}

	@Override
	public String createUid(Session session, String givenName, String surName, Date birthDay) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		return new UserController(session,em).createUid(givenName,surName,birthDay);
	}

	@Override
	public OssResponse importUser(Session session, String role, String lang, String identifier, boolean test,
			String password, boolean mustchange, boolean full, boolean allClasses, boolean cleanClassDirs,
			boolean resetPassword, InputStream fileInputStream, FormDataContentDisposition contentDispositionHeader) {
		File file = null;
		try {
			file = File.createTempFile("oss", "importUser", new File("/opt/oss-java/tmp/"));
			Files.copy(fileInputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return new OssResponse(session,"ERROR", "Import file can not be saved" + e.getMessage());
		}
		List<String> parameters = new ArrayList<String>();
		parameters.add("/usr/sbin/oss_import_user_list.pl");
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
		if( mustchange ) {
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
		String[] program = new String[parameters.size()];
		program = parameters.toArray(program);

		StringBuffer reply  = new StringBuffer();
		StringBuffer stderr = new StringBuffer();
		OSSShellTools.exec(program, reply, stderr, null);
		return new OssResponse(session,"OK", "Import was started.");
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
	public OssResponse restartImport(Session session, String startTime) {
		UserImport userImport = getImport(session, startTime);
		if( userImport != null ) {
			Controller controller    = new Controller(session,null);
			StringBuilder importFile = controller.getImportDir(startTime);
			importFile.append("/userlist.txt");
			List<String> parameters = new ArrayList<String>();
			parameters.add("/usr/sbin/oss_import_user_list.pl");
			parameters.add("--input");
			parameters.add(importFile.toString());
			parameters.add("--role");
			parameters.add(userImport.getRole());
			parameters.add("--lang");
			parameters.add(userImport.getLang());
			parameters.add("--idetntifier");
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
			if( userImport.isFull() ) {
				parameters.add("--allClasses");
			}
			if( userImport.isCleanClassDirs() ) {
				parameters.add("--cleanClassDirs");
			}
			if( userImport.isResetPassword() ) {
				parameters.add("--resetPassword");
			}
			String[] program = new String[parameters.size()];
			program = parameters.toArray(program);

			StringBuffer reply  = new StringBuffer();
			StringBuffer stderr = new StringBuffer();
			OSSShellTools.exec(program, reply, stderr, null);
			return new OssResponse(session,"OK", "Import was started.");
		}
		return new OssResponse(session,"ERROR", "CAn not find the import.");
	}

	@Override
	public OssResponse deleteImport(Session session, String startTime) {
		Controller controller    = new Controller(session,null);
		StringBuilder importDir  = controller.getImportDir(startTime);
		if( startTime == null || startTime.isEmpty() ) {
			return new OssResponse(session,"ERROR", "Invalid import name.");
		}
		String[] program = new String[3];
		program[0] = "rm";
		program[1] = "-rf";
		program[2] = importDir.toString();
		StringBuffer reply  = new StringBuffer();
		StringBuffer stderr = new StringBuffer();
		OSSShellTools.exec(program, reply, stderr, null);
		return new OssResponse(session,"OK", "Import was deleted.");
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
	public OssResponse syncMsQuotas(Session session, List<List<String>> Quotas) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		return new UserController(session,em).syncMsQuotas(Quotas);
	}

	@Override
	public String getUidsByRole(Session session, String role) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final UserController userController = new UserController(session,em);
		List<String> users = new ArrayList<String>();
		for( User user : userController.getByRole(role) ) {
			users.add(user.getUid());
		}
		return String.join(userController.getNl(),users);
	}

	@Override
	public OssResponse allTeachersInAllClasses(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final UserController userController = new UserController(session,em);
		final GroupController groupController = new GroupController(session,em);
		for( User user : userController.getByRole("teachers") ) {
			for( Group group : groupController.getByType("class")) {
				groupController.addMember(group, user);
			}
		}
		return new OssResponse(session,"OK", "All teachers was put into all classes.");
	}

	@Override
	public OssResponse allClasses(Session session, long userId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		User user = new UserController(session,em).getById(userId);
		final GroupController groupController = new GroupController(session,em);
		for( Group group : groupController.getByType("class")) {
			groupController.addMember(group, user);
		}
		return new OssResponse(session,"OK", "User was put into all classes.");
	}

	@Override
	public String addToAllClasses(Session session, String userName) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		User user = new UserController(session,em).getByUid(userName);
		final GroupController groupController = new GroupController(session,em);
		for( Group group : groupController.getByType("class")) {
			groupController.addMember(group, user);
		}
		return "OK";
	}

	@Override
	public OssResponse sync(Session session) {
		//TODO make it over plugin
		String[] program = new String[1];
		program[0] = "/usr/sbin/oss_refresh_squidGuard_user.sh";
		StringBuffer reply  = new StringBuffer();
		StringBuffer stderr = new StringBuffer();
		OSSShellTools.exec(program, reply, stderr, null);
		return new OssResponse(session,"OK", "Import was started.");
	}
}
