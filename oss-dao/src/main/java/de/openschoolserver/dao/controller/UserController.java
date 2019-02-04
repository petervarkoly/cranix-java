/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved  */
package de.openschoolserver.dao.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import static de.openschoolserver.dao.internal.OSSConstants.*;
import de.extis.core.util.UserUtil;
import de.openschoolserver.dao.*;
import de.openschoolserver.dao.tools.OSSShellTools;

@SuppressWarnings("unchecked")
public class UserController extends Controller {

	Logger logger = LoggerFactory.getLogger(UserController.class);


	public UserController(Session session,EntityManager em) {
		super(session,em);
	}

	public User getById(long userId) {
		try {
			User user = em.find(User.class, userId);
			if( user != null ) {
				for( Alias alias : user.getAliases() ) {
					user.getMailAliases().add(alias.getAlias());
				}
			}
			return user;
		} catch (Exception e) {
			logger.debug("getByID: " + e.getMessage());
			return null;
		} finally {
		}
	}

	public List<User> getByRole(String role) {
		try {
			Query query = em.createNamedQuery("User.getByRole");
			query.setParameter("role", role);
			return query.getResultList();
		} catch (Exception e) {
			logger.error("getByRole: " + e.getMessage());
			return new ArrayList<>();
		} finally {
		}
	}

	public User getByUid(String uid) {
		try {
			Query query = em.createNamedQuery("User.getByUid");
			query.setParameter("uid", uid);
			List<User> result = query.getResultList();
			if (result != null && result.size() > 0) {
				return result.get(0);
			} else {
				logger.debug("getByUid: uid not found. uid=" + uid);
			}
		} catch (Exception e) {
			logger.error("getByUid: uid=" + uid + " " + e.getMessage());
			return null;
		} finally {
		}
		return null;
	}

	public List<User> search(String search) {
		try {
			Query query = em.createNamedQuery("User.search");
			query.setParameter("search", search + "%");
			return query.getResultList();
		} catch (Exception e) {
			logger.error("search: " + e.getMessage());
			return new ArrayList<>();
		} finally {
		}
	}

	public List<User> findByName(String givenName, String surName) {
		try {
			Query query = em.createNamedQuery("User.findByName");
			query.setParameter("givenName", givenName);
			query.setParameter("surName", surName);
			return query.getResultList();
		} catch (Exception e) {
			logger.error("findByName: " + e.getMessage());
			return new ArrayList<>();
		} finally {
		}
	}

	public List<User> findByNameAndRole(String givenName, String surName, String role) {
		try {
			Query query = em.createNamedQuery("User.findByNameAndRole");
			query.setParameter("givenName", givenName);
			query.setParameter("surName", surName);
			query.setParameter("role", role);
			return query.getResultList();
		} catch (Exception e) {
			logger.error("findByNameAndRole: " + e.getMessage());
			return new ArrayList<>();
		} finally {
		}
	}

	public List<User> getAll() {
		List<User> users = new ArrayList<User>();
		boolean userManage = this.isAllowed("user.manage");
		try {
			Query query = em.createNamedQuery("User.findAll");
			for (User user : (List<User>) query.getResultList()) {
				if (userManage || user.getRole().equals(roleStudent)
					|| ( user.getRole().equals(roleGuest) && user.getCreator().equals(session.getUser()) ) ) {
					List<String> classes = new ArrayList<String>();
					for( Group group : user.getGroups()) {
						if (group.getGroupType().equals("class")) {
							classes.add(group.getName());
						}
					}
					user.setClasses(String.join(",", classes));
					users.add(user);
				}
			}
		} catch (Exception e) {
			logger.error("getAll: " + e.getMessage());
		} finally {
		}
		return users;
	}

	public String createUid(String givenName, String surName, Date birthDay) {
		String userId = UserUtil.createUserId(givenName, surName, birthDay, true,
				"telex".equals(this.getConfigValue("STRING_CONVERT_TYPE")) , this.getConfigValue("LOGIN_SCHEME"));
		String newUserId = this.getConfigValue("LOGIN_PREFIX") + userId;
		Integer i = 1;
		while (!this.isNameUnique(newUserId)) {
			newUserId = this.getConfigValue("LOGIN_PREFIX") + userId + i;
			i++;
		}
		return newUserId;
	}

	public OssResponse add(User user) {
		logger.debug("User to create:" + user);
		// Check role
		if (user.getRole() == null) {
			return new OssResponse(this.getSession(), "ERROR", "You have to define the role of the user.");
		}
		// Check Birthday
		if (user.getBirthDay() == null) {
			if (user.getRole().equals("sysadmins") || user.getRole().equals("templates")) {
				user.setBirthDay(this.now());
			} else {
				return new OssResponse(this.getSession(), "ERROR", "You have to define the birthday.");
			}
		}
		// Create uid if not given
		if (user.getUid() == null || user.getUid().isEmpty()) {
			String userId = UserUtil.createUserId(user.getGivenName(), user.getSurName(), user.getBirthDay(), true,
					"telex".equals(this.getConfigValue("STRING_CONVERT_TYPE")), this.getConfigValue("LOGIN_SCHEME"));
			user.setUid(this.getConfigValue("LOGIN_PREFIX") + userId);
			Integer i = 1;
			while (!this.isNameUnique(user.getUid())) {
				user.setUid(this.getConfigValue("LOGIN_PREFIX") + userId + i);
				i++;
			}
		} else {
			user.setUid(user.getUid().toLowerCase());
			// First we check if the parameter are unique.
			// workstation users have a user called as itself
			if (!user.getRole().equals("workstations") && !this.isNameUnique(user.getUid())) {
				return new OssResponse(this.getSession(), "ERROR", "User name is not unique.");
			}
			// Check if uid contains non allowed characters
			if (this.checkNonASCII(user.getUid())) {
				return new OssResponse(this.getSession(), "ERROR", "Uid contains not allowed characters.");
			}
		}
		// Check the user password
		if (user.getRole().equals("workstations") || user.getRole().equals("guest")) {
			user.setPassword(user.getUid());
		} else if (user.getPassword() == null || user.getPassword().isEmpty()) {
			user.setPassword(createRandomPassword());
		} else {
			OssResponse ossResponse = this.checkPassword(user.getPassword());
			if (ossResponse != null) {
				return ossResponse;
			}
		}
		if (user.getFsQuota() == null) {
			user.setFsQuota(Integer.getInteger(this.getConfigValue("FILE_QUOTA")));
		}
		if (user.getMsQuota() == null) {
			user.setMsQuota(Integer.getInteger(this.getConfigValue("MAIL_QUOTA")));
		}
		// Make backup from password. password field is transient!
		user.setInitialPassword(user.getPassword());
		user.setCreator(this.session.getUser());
		// Check user parameter
		StringBuilder errorMessage = new StringBuilder();
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		for (ConstraintViolation<User> violation : factory.getValidator().validate(user)) {
			errorMessage.append(violation.getMessage()).append(getNl());
		}
		if (errorMessage.length() > 0) {
			return new OssResponse(this.getSession(), "ERROR", errorMessage.toString());
		}
		for( String alias : user.getMailAliases() ) {
			if( isUserAliasUnique(alias) ) {
				user.addAlias(new Alias(user,alias));
			}
		}
		try {
			this.beginTransaction();
			em.persist(user);
			em.merge(user);
			em.getTransaction().commit();
			logger.debug("Created user" + user);
		} catch (Exception e) {
			logger.error("add: " + e.getMessage());
			return new OssResponse(this.getSession(), "ERROR", e.getMessage());
		} finally {
		}
		this.startPlugin("add_user", user);
		GroupController groupController = new GroupController(session,em);
		Group group = new GroupController(session,em).getByName(user.getRole());
		if (group != null) {
			groupController.addMember(group, user);
		}
		List<String> parameters = new ArrayList<String>();
		parameters.add(user.getUid());
		parameters.add(user.getGivenName());
		parameters.add(user.getSurName());
		parameters.add(user.getPassword());
		return new OssResponse(this.getSession(), "OK", "%s ( %s %s ) was created with password '%s'", user.getId(),
				parameters);
	}

	public List<OssResponse> add(List<User> users) {
		List<OssResponse> results = new ArrayList<OssResponse>();
		for (User user : users) {
			results.add(this.add(user));
		}
		return results;
	}

	public OssResponse modify(User user) {
		User oldUser = this.getById(user.getId());
		if (user.getPassword() != null && !user.getPassword().isEmpty()) {
			OssResponse ossResponse = this.checkPassword(user.getPassword());
			if (ossResponse != null) {
				return ossResponse;
			}
		}
		oldUser.setGivenName(user.getGivenName());
		oldUser.setSurName(user.getSurName());
		oldUser.setBirthDay(user.getBirthDay());
		oldUser.setPassword(user.getPassword());
		oldUser.setFsQuota(user.getFsQuota());
		oldUser.setMsQuota(user.getMsQuota());
		List<Alias> newAliases = new ArrayList<Alias>();
		ArrayList<String> oldAliases = new ArrayList<String>();
		for( Alias alias : oldUser.getAliases() ) {
			oldAliases.add(alias.getAlias());
		}
		for( String alias : user.getMailAliases() ) {
			if( !oldAliases.contains(alias) && !this.isUserAliasUnique(alias) ) {
				return new OssResponse(this.getSession(), "ERROR", "Alias '%s' is not unique",null,alias);
			}
			newAliases.add(new Alias(oldUser,alias));
		}
		oldUser.setAliases(newAliases);
		// Check user parameter
		StringBuilder errorMessage = new StringBuilder();
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		for (ConstraintViolation<User> violation : factory.getValidator().validate(oldUser)) {
			errorMessage.append(violation.getMessage()).append(getNl());
		}
		if (errorMessage.length() > 0) {
			return new OssResponse(this.getSession(), "ERROR", errorMessage.toString());
		}
		try {
			this.beginTransaction();
			em.merge(oldUser);
			em.getTransaction().commit();
		} catch (Exception e) {
			logger.error(e.getMessage());
			return new OssResponse(this.getSession(), "ERROR", e.getMessage());
		} finally {
		}
		this.startPlugin("modify_user", oldUser);
		return new OssResponse(this.getSession(), "OK", "User was modified succesfully");
	}

	public List<OssResponse> deleteStudents(List<Long> userIds) {
		List<OssResponse> responses = new ArrayList<OssResponse>();
		for( Long userId : userIds ) {
			User user = this.getById(userId);
			if( user != null && user.getRole().equals(roleStudent)) {
				responses.add(this.delete(user));
			}
		}
		return responses;
	}

	public OssResponse delete(long userId) {
		return this.delete(this.getById(userId));
	}

	public OssResponse delete(String uid) {
		return this.delete(this.getByUid(uid));
	}

	public OssResponse delete(User user) {
		if( user == null ) {
			return new OssResponse(this.getSession(),"ERROR", "Can not find the user.");
		}
		if(this.isProtected(user)) {
			return new OssResponse(this.getSession(), "ERROR", "This user must not be deleted.");
		}
		if( !this.mayModify(user)) {
			return new OssResponse(this.getSession(),"ERROR", "You must not delete this user.");
		}
		this.startPlugin("delete_user", user);
		//TODO make it configurable
		User admin = getById(1L);
		this.beginTransaction();
		if( user.getRole().equals(roleStudent) || user.getRole().equals(roleWorkstation) ){
			this.deleteCreatedObjects(user);
		} else {
			this.inheritCreatedObjects(user,admin);
		}
		List<Device> devices = user.getOwnedDevices();
		boolean restartDHCP = !devices.isEmpty();
		if (!em.contains(user)) {
			user = em.merge(user);
		}
		for( Group group : user.getGroups() ) {
			group.getUsers().remove(user);
			em.merge(group);
		}
		if( restartDHCP ) {
			DeviceController dc = new DeviceController(session,em);;
			for( Device device : devices ) {
				dc.delete(device.getId(),false);
			}
		}
		em.remove(user);
		em.getTransaction().commit();
		if (restartDHCP) {
			DHCPConfig dhcpConfig = new DHCPConfig(session,em);
			dhcpConfig.Create();
		}
		return new OssResponse(this.getSession(), "OK", "User was deleted");
	}

	public List<Group> getAvailableGroups(long userId) {

		User user = this.getById(userId);
		Query query = em.createNamedQuery("Group.findAll");
		List<Group> allGroups = query.getResultList();
		allGroups.removeAll(user.getGroups());
		return allGroups;
	}

	public List<Group> getGroups(long userId) {
		User user = this.getById(userId);
		return user.getGroups();
	}

	public OssResponse setGroups(long userId, List<Long> groupIds) {
		List<Group> groupsToRemove = new ArrayList<Group>();
		List<Group> groupsToAdd = new ArrayList<Group>();
		List<Group> groups = new ArrayList<Group>();
		for (Long groupId : groupIds) {
			groups.add(em.find(Group.class, groupId));
		}
		User user = this.getById(userId);
		for (Group group : groups) {
			if (!user.getGroups().contains(group)) {
				groupsToAdd.add(group);
			}
		}
		for (Group group : user.getGroups()) {
			if (!user.getRole().equals(group.getName())) {
				// User must not be removed from it's primary group.
				continue;
			}
			if (!groups.contains(group)) {
				groupsToRemove.add(group);
			}
		}
		try {
			this.beginTransaction();
			for (Group group : groupsToAdd) {
				group.getUsers().add(user);
				user.getGroups().add(group);
				em.merge(group);
			}
			for (Group group : groupsToRemove) {
				group.getUsers().remove(user);
				user.getGroups().remove(group);
				em.merge(group);
			}
			em.merge(user);
			em.getTransaction().commit();
		} catch (Exception e) {
			return new OssResponse(this.getSession(), "ERROR", e.getMessage());
		} finally {
		}
		for (Group group : groupsToAdd) {
			this.changeMemberPlugin("addmembers", group, user);
		}
		for (Group group : groupsToRemove) {
			this.changeMemberPlugin("removemembers", group, user);
		}
		return new OssResponse(this.getSession(), "OK", "The groups of the user was set.");
	}

	public OssResponse syncFsQuotas(List<List<String>> quotas) {
		User user;
		try {
			this.beginTransaction();
			for (List<String> quota : quotas) {
				if (quota.isEmpty())
					continue;
				user = this.getByUid(quota.get(0));
				if (user != null) {
					user.setFsQuotaUsed(Integer.valueOf(quota.get(1)));
					user.setFsQuota(Integer.valueOf(quota.get(2)));
					em.merge(user);
				}
			}
			em.getTransaction().commit();
		} catch (Exception e) {
			return new OssResponse(this.getSession(), "ERROR", e.getMessage());
		} finally {
		}
		return new OssResponse(this.getSession(), "OK", "The filesystem quotas was synced succesfully");
	}

	public OssResponse syncMsQuotas(List<List<String>> quotas) {
		User user;
		try {
			this.beginTransaction();
			for (List<String> quota : quotas) {
				if (quota.isEmpty())
					continue;
				user = this.getByUid(quota.get(0));
				if (user != null) {
					user.setMsQuotaUsed(Integer.valueOf(quota.get(1)));
					user.setMsQuota(Integer.valueOf(quota.get(2)));
					em.merge(user);
				}
			}
			em.getTransaction().commit();
		} catch (Exception e) {
			return new OssResponse(this.getSession(), "ERROR", e.getMessage());
		} finally {
		}
		return new OssResponse(this.getSession(), "OK", "The mailsystem quotas was synced succesfully");
	}


	public List<User> getUsers(List<Long> userIds) {
		List<User> users = new ArrayList<User>();
		try {
			logger.debug(new ObjectMapper().writeValueAsString(userIds));
		} catch (Exception e) {
			logger.debug("{ \"ERROR\" : \"getUsers CAN NOT MAP THE OBJECT\" }");
		}
		if (userIds == null) {
			return users;
		}
		for (Long id : userIds) {
			if (id != null) {
				User u = this.getById(id);
				if (u != null) {
					users.add(u);
				}
			}
		}
		return users;
	}

	public List<OssResponse> resetUserPassword(List<Long> userIds, String password, boolean mustChange) {
		logger.debug("resetUserPassword: " + password);
		List<OssResponse> responses = new ArrayList<OssResponse>();
		if( password.length() < 7 ) {
			responses.add(new OssResponse(this.getSession(),"ERROR","Password must contains minimum 7 character."));
			return responses;
		}
		StringBuffer reply = new StringBuffer();
		StringBuffer error = new StringBuffer();
		String[] program = new String[5];
		program[0] = "/usr/bin/samba-tool";
		program[1] = "domain";
		program[2] = "passwordsettings";
		program[3] = "set";
		program[4] = "--complexity=off";
		OSSShellTools.exec(program, reply, error, null);

		if (mustChange) {
			program = new String[6];
			program[0] = "/usr/bin/samba-tool";
			program[5] = "--must-change-at-next-login";
		}
		program[1] = "user";
		program[2] = "setpassword";
		program[4] = "--newpassword=" + password;

		for (Long id : userIds) {
			User user = this.getById(id);
			if( user == null ) {
				logger.error("resetUserPassword: Can not find user with id: %s",null,id);
				continue;
			}
		/* We allow it
		 * 	if( user.getRole().equals(roleWorkstation) ) {
		 *		logger.error("resetUserPassword: Must not change workstation users password.");
		 *		continue;
		 *	}
		  */
			error = new StringBuffer();
			reply = new StringBuffer();
			program[3] = user.getUid();
			OSSShellTools.exec(program, reply, error, null);
			logger.debug(program[0] + " " + program[1] + " " + program[2] + " " + program[3] + " " + program[4] + " R:"
					+ reply.toString() + " E:" + error.toString());
			responses.add(new OssResponse(this.getSession(), "OK", "The password of '%s' was reseted.",null,user.getUid()));
		}
		if (this.getConfigValue("CHECK_PASSWORD_QUALITY").toLowerCase().equals("yes")) {
			program = new String[5];
			program[0] = "/usr/bin/samba-tool";
			program[1] = "domain";
			program[2] = "passwordsettings";
			program[3] = "set";
			program[4] = "--complexity=on";
			OSSShellTools.exec(program, reply, error, null);
		}
		return responses;
	}

	public List<OssResponse> copyTemplate(List<Long> userIds, String stringValue) {
		List<OssResponse> responses = new ArrayList<OssResponse>();
		StringBuffer reply = new StringBuffer();
		StringBuffer error = new StringBuffer();
		String[] program = new String[2];
		if (stringValue != null) {
			program = new String[3];
			program[2] = stringValue;
		}
		program[0] = "/usr/sbin/oss_copy_template_home.sh";
		for (Long id : userIds) {
			User user = this.getById(id);
			if( user != null ) {
				program[1] = user.getUid();
				OSSShellTools.exec(program, reply, error, null);
				responses.add(new OssResponse(this.getSession(), "OK", "The template for '%s' was copied.",null,user.getUid()));
			}
		}
		return responses;
	}

	public List<OssResponse> removeProfile(List<Long> userIds) {
		List<OssResponse> responses = new ArrayList<OssResponse>();
		StringBuffer reply = new StringBuffer();
		StringBuffer error = new StringBuffer();
		String[] program = new String[2];
		program[0] = "/usr/sbin/oss_remove_profile.sh";
		for (Long id : userIds) {
			User user = this.getById(id);
			if( user != null ) {
				program[1] = user.getUid();
				OSSShellTools.exec(program, reply, error, null);
				responses.add(new OssResponse(this.getSession(), "OK", "The windows profile of '%s' was removed.",null,user.getUid()));
			}
		}
		return responses;
	}

	public List<OssResponse> disableLogin(List<Long> userIds, boolean disable) {
		List<OssResponse> responses = new ArrayList<OssResponse>();
		StringBuffer reply = new StringBuffer();
		StringBuffer error = new StringBuffer();
		String[] program = new String[4];
		program[0] = "/usr/bin/samba-tool";
		program[1] = "user";
		if (disable) {
			program[2] = "disable";
		} else {
			program[2] = "enable";
		}
		for (Long id : userIds) {
			User user = this.getById(id);
			if( user != null ) {
				program[3] = user.getUid();
				OSSShellTools.exec(program, reply, error, null);
				if( disable ) {
					responses.add(new OssResponse(this.getSession(), "OK", "The '%s' was disabled.",null,user.getUid()));
				} else {
					responses.add(new OssResponse(this.getSession(), "OK", "The '%s' was enabled.",null,user.getUid()));
				}
			}
		}
		return responses;
	}

	public List<OssResponse> setFsQuota(List<Long> userIds, Long fsQuota) {
		List<OssResponse> responses = new ArrayList<OssResponse>();
		StringBuffer reply = new StringBuffer();
		StringBuffer error = new StringBuffer();
		String[] program = new String[3];
		program[0] = "/usr/sbin/oss_set_quota.sh";
		program[2] = String.valueOf(fsQuota);
		Integer quota = Integer.valueOf(program[2]);
		for (Long id : userIds) {
			User user = this.getById(id);
			if( user != null ) {
				program[1] = user.getUid();
				user.setFsQuota(quota);
				em.merge(user);
				OSSShellTools.exec(program, reply, error, null);
				responses.add(new OssResponse(this.getSession(), "OK", "The file system quota for '%s' was set.",null,user.getUid()));
			}
		}
		return responses;
	}

	public List<OssResponse> setMsQuota(List<Long> userIds, Long msQuota) {
		List<OssResponse> responses = new ArrayList<OssResponse>();
		StringBuffer reply = new StringBuffer();
		StringBuffer error = new StringBuffer();
		String[] program = new String[3];
		program[0] = "/usr/sbin/oss_set_mquota.pl";
		program[2] = String.valueOf(msQuota);
		Integer quota = Integer.valueOf(program[2]);
		for (Long id : userIds) {
			User user = this.getById(id);
			if( user != null ) {
				program[1] = user.getUid();
				user.setMsQuota(quota);
				em.merge(user);
				OSSShellTools.exec(program, reply, error, null);
				responses.add(new OssResponse(this.getSession(), "OK", "The mail system quota for '%s' was set.",null,user.getUid()));
			}
		}
		return responses;
	}

	public OssResponse collectFile(List<User> users, String projectName) {
		StringBuilder data = new StringBuilder();
		StringBuffer reply = new StringBuffer();
		StringBuffer error = new StringBuffer();
		String[] program = new String[11];
		program[0] = "/usr/sbin/oss_collect_files.sh";
		program[1] = "-t";
		program[2] = this.session.getUser().getUid();
		program[3] = "-f";
		program[5] = "-p";
		program[6] = projectName;
		program[7] = "-c";
		program[8] = "y";
		program[9] = "-d";
		program[10] = "y";
		for (User user : users) {
			program[4] = user.getUid();
			OSSShellTools.exec(program, reply, error, data.toString());
		}
		return new OssResponse(this.getSession(), "OK",
				"The files from the export directories of selected users were collected.");
	}

	public OssResponse collectFileByIds(List<Long> userIds, String projectName) {
		StringBuilder data = new StringBuilder();
		StringBuffer reply = new StringBuffer();
		StringBuffer error = new StringBuffer();
		String[] program = new String[11];
		program[0] = "/usr/sbin/oss_collect_files.sh";
		program[1] = "-t";
		program[2] = this.session.getUser().getUid();
		program[3] = "-f";
		program[5] = "-p";
		program[6] = projectName;
		program[7] = "-c";
		program[8] = "y";
		program[9] = "-d";
		program[10] = "y";
		for (Long id : userIds) {
			program[4] = this.getById(id).getUid();
			OSSShellTools.exec(program, reply, error, data.toString());
		}
		return new OssResponse(this.getSession(), "OK",
				"The files from the export directories of selected users were collected.");
	}

	public OssResponse collectFileFromUser(User user, String project, boolean sortInDirs, boolean cleanUpExport) {
		String[] program = new String[11];
		StringBuffer reply = new StringBuffer();
		StringBuffer stderr = new StringBuffer();
		program[0] = "/usr/sbin/oss_collect_files.sh";
		program[1] = "-t";
		program[2] = this.session.getUser().getUid();
		program[3] = "-f";
		program[4] = user.getUid();
		program[5] = "-p";
		program[6] = project;
		program[7] = "-c";
		program[9] = "-d";
		if (cleanUpExport) {
			program[8] = "y";
		} else {
			program[8] = "n";
		}
		if (sortInDirs) {
			program[10] = "y";
		} else {
			program[10] = "n";
		}
		OSSShellTools.exec(program, reply, stderr, null);
		if (stderr.toString().isEmpty()) {
			if(logger.isDebugEnabled()) {
				StringBuilder st = new StringBuilder();
				for ( int faktor = 0; faktor < program.length; faktor ++ ) {
					st.append(program[faktor]).append(" ");
				}
				logger.debug("Collect Program:" + st.toString());
				logger.debug("Collected project " + project + " from " + user.getUid());
			}
			return new OssResponse(this.getSession(), "OK", "File was collected from: %s", null, user.getUid());
		}
		logger.error("Can not collect project " + project + " from " + user.getUid() + stderr.toString());

		return new OssResponse(this.getSession(), "ERROR", stderr.toString());
	}

	public List<OssResponse> disableInternet(List<Long> userIds, boolean disable) {
		List<OssResponse> responses = new ArrayList<OssResponse>();
		for (Long userId : userIds) {
			User user = this.getById(userId);
			if( user != null ) {
				if (disable) {
					this.setConfig(this.getById(userId), "internetDisabled", "yes");
					responses.add(new OssResponse(this.getSession(), "OK", "'%s' was disabled.",null,user.getUid()));
				} else {
					responses.add(new OssResponse(this.getSession(), "OK", "'%s' was enabled.",null,user.getUid()));
					this.deleteConfig(this.getById(userId), "internetDisabled");
				}
			}
		}
		return responses;
	}

	/**
	 * Get the list of created guest users categories
	 * @return The list of the guest users categories
	 */
	public List<Category> getGuestUsers() {
		final CategoryController categoryController = new CategoryController(session,em);
		if (categoryController.isSuperuser()) {
			return categoryController.getByType("guestUsers");
		}
		List<Category> categories = new ArrayList<Category>();
		for (Category category : categoryController.getByType("guestUsers")) {
			if (category.getOwner().equals(session.getUser())) {
				categories.add(category);
			}
		}
		return categories;
	}

	/**
	 * Gets a guest user category by id.
	 * @param guestUsersId The technical id of the category.
	 * @return The list of the guest users.
	 */
	public Category getGuestUsersCategory(Long guestUsersId) {
		return new CategoryController(session,em).getById(guestUsersId);
	}

	/**
	 * Delete a guest user category. Only the guest members will be deleted.
	 * @param guestUsersId The technical id of the category.
	 * @return The result as an OssResponse
	 */
	public OssResponse deleteGuestUsers(Long guestUsersId) {
		final CategoryController categoryController = new CategoryController(session,em);
		final GroupController groupController = new GroupController(session,em);
		Category category = categoryController.getById(guestUsersId);
		for (User user : category.getUsers()) {
			if (user.getRole().equals("guest")) {
				this.delete(user);
			}
		}
		category.setUsers(new ArrayList<User>());
		for (Group group : category.getGroups()) {
			if (group.getGroupType().equals("guest")) {
				groupController.delete(group);
			}
		}
		category.setGroups(new ArrayList<Group>());
		return categoryController.delete(category);
	}

	public OssResponse addGuestUsers(String name, String description, Long roomId, Long count, Date validUntil) {
		final CategoryController categoryController = new CategoryController(session,em);
		final GroupController groupController = new GroupController(session,em);
		Category category = new Category();
		category.setCategoryType("guestUsers");
		category.setName(name);
		category.setDescription(description);
		category.setValidFrom(categoryController.now());
		category.setValidUntil(validUntil);
		OssResponse ossResponse = categoryController.add(category);
		if (ossResponse.getCode().equals("ERROR")) {
			return ossResponse;
		}
		category = categoryController.getById(ossResponse.getObjectId());

		Group group = new Group();
		group.setGroupType("guest");
		group.setName(name);
		group.setDescription(description);
		ossResponse = groupController.add(group);
		if (ossResponse.getCode().equals("ERROR")) {
			categoryController.delete(category.getId());
			return ossResponse;
		}

		group = groupController.getById(ossResponse.getObjectId());
		try {
			this.beginTransaction();
			category.setGroups(new ArrayList<Group>());
			category.getGroups().add(group);
			group.setCategories(new ArrayList<Category>());
			group.getCategories().add(category);
			em.merge(category);
			em.merge(group);
			em.getTransaction().commit();
		} catch (Exception e) {
			logger.error(e.getMessage());
			return null;
		}
		for (Long i = 1l; i < count + 1; i++) {
			String userName = String.format("%s%02d", name, i);
			User user = new User();
			user.setUid(userName);
			user.setSurName("GuestUser");
			user.setGivenName(userName);
			user.setRole("guest");
			ossResponse = this.add(user);
			logger.debug("Create user ossResponse:" + ossResponse);
			user = this.getById(ossResponse.getObjectId());
			ossResponse = groupController.addMember(group, user);
			logger.debug("Create user " + ossResponse);
			categoryController.addMember(category.getId(), "user", user.getId());
		}
		ossResponse.setObjectId(category.getId());
		ossResponse.setValue("Guest Users were created succesfully");
		ossResponse.setCode("OK");
		return ossResponse;
	}

	public String getGroupsOfUser(String userName, String groupType) {
		User user = this.getByUid(userName);
		if( user ==  null ) {
			return "";
		}
		List<String> groups = new ArrayList<String>();
		for(Group group : user.getGroups() ) {
			if( group.getGroupType().equals(groupType)) {
				groups.add(group.getName());
			}
		}
		return String.join(this.getNl(), groups);
	}

	public void inheritCreatedObjects(User creator, User newCreator) {
		try {
			//Acls
			for( Acl o : creator.getCreatedAcls() ) {
				o.setCreator(newCreator);
				em.merge(o);
			}
			creator.setCreatedAcls(null);
			//AccessInRoom
			for( AccessInRoom o : creator.getCreatedAccessInRoom() ) {
				o.setCreator(newCreator);
				em.merge(o);
			}
			creator.setCreatedAccessInRoom(null);
			//Announcement
			for( Announcement o : creator.getMyAnnouncements() ) {
				o.setOwner(newCreator);
				em.merge(o);
			}
			creator.setMyAnnouncements(null);
			//Categories
			for( Category o : creator.getOwnedCategories() ) {
				o.setOwner(newCreator);
				em.merge(o);
			}
			creator.setOwnedCategories(null);
			//Contacts
			for( Contact o : creator.getMyContacts() ) {
				o.setOwner(newCreator);
				em.merge(o);
			}
			creator.setMyContacts(null);
			//Groups
			for( Group o : creator.getOwnedGroups() ) {
				o.setOwner(newCreator);
				em.merge(o);
			}
			creator.setOwnedGroups(null);
			//HWConfs
			for( HWConf o : creator.getCreatedHWConfs() ) {
				o.setCreator(newCreator);
				em.merge(o);
			}
			creator.setCreatedHWConfs(null);
			//PositiveList
			for( PositiveList o : creator.getOwnedPositiveLists() ) {
				o.setOwner(newCreator);
				em.merge(o);
			}
			creator.setOwnedPositiveLists(null);
			//Partitions
			for( Partition o : creator.getCreatedPartitions()) {
				o.setCreator(newCreator);
				em.merge(o);
			}
			creator.setCreatedPartitions(null);
			//Rooms
			for( Room o : creator.getCreatedRooms() ) {
				o.setCreator(newCreator);
				em.merge(o);
			}
			creator.setCreatedRooms(null);
			//User
			for( User o : creator.getCreatedUsers() ) {
				o.setCreator(newCreator);
				em.merge(o);
			}
			creator.setCreatedUsers(null);
			//Sessions will be deleted
			for( Session o : creator.getSessions() ) {
				em.remove(o);
			}
			em.merge(creator);
			em.merge(newCreator);
		} catch (Exception e) {
			logger.error("inheritCreatedObjects:" + e.getMessage());
		}
	}

	public void deleteCreatedObjects(User creator) {
		try {
			//Acls
			for( Acl o : creator.getCreatedAcls() ) {
				em.remove(o);
			}
			creator.setCreatedAcls(null);
			//AccessInRoom
			for( AccessInRoom o : creator.getCreatedAccessInRoom() ) {
				em.remove(o);
			}
			creator.setCreatedAccessInRoom(null);
			//Announcement
			for( Announcement o : creator.getMyAnnouncements() ) {
				em.remove(o);
			}
			creator.setMyAnnouncements(null);
			//Categories
			for( Category o : creator.getOwnedCategories() ) {
				em.remove(o);
			}
			creator.setOwnedCategories(null);
			//Contacts
			for( Contact o : creator.getMyContacts() ) {
				em.remove(o);
			}
			creator.setMyContacts(null);
			//Groups
			for( Group o : creator.getOwnedGroups() ) {
				em.remove(o);
			}
			creator.setOwnedGroups(null);
			//HWConfs
			for( HWConf o : creator.getCreatedHWConfs() ) {
				em.remove(o);
			}
			creator.setCreatedHWConfs(null);
			//PositiveList
			for( PositiveList o : creator.getOwnedPositiveLists() ) {
				em.remove(o);
			}
			creator.setOwnedPositiveLists(null);
			//Rooms
			for( Room o : creator.getCreatedRooms() ) {
				em.remove(o);
			}
			creator.setCreatedRooms(null);
			//User
			for( User o : creator.getCreatedUsers() ) {
				em.remove(o);
			}
			creator.setCreatedUsers(null);
			//Sessions will be deleted
			for( Session o : creator.getSessions() ) {
				em.remove(o);
			}
			creator.setSessions(null);
			em.merge(creator);
		} catch (Exception e) {
			logger.error("delete owned objects:" + e.getMessage());
		}
	}

}
