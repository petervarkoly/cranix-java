/* (c) 2021 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.api.resources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.cranix.dao.*;
import de.cranix.helper.CrxEntityManagerFactory;
import de.cranix.helper.CrxSystemCmd;
import de.cranix.services.GroupService;
import de.cranix.services.Service;
import de.cranix.services.UserService;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.*;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static de.cranix.api.resources.Resource.JSON_UTF8;
import static de.cranix.api.resources.Resource.TEXT;
import static de.cranix.helper.CranixConstants.cranixTmpDir;

@Path("users")
@Api(value = "users")
public class UserResource {

	Logger logger = LoggerFactory.getLogger(UserResource.class);

	public UserResource() {
	}

	@GET
	@Path("all")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get all users")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	@JsonIgnoreProperties({"groups"})
	public List<User> getAll(
		@ApiParam(hidden = true) @Auth Session session
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		final List<User> users = new UserService(session, em).getAll();
		em.close();
		if (users == null) {
			throw new WebApplicationException(404);
		}
		return users;
	}

	@POST
	@Path("add")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Create new user and syncing it to squidGuard.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("user.add")
	public CrxResponse add(
		@ApiParam(hidden = true) @Auth Session session,
		User user
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse crxResponse = new UserService(session, em).add(user);
		em.close();
		if (crxResponse.getCode().equals("OK")) {
			sync(session);
		}
		return crxResponse;
	}

	@POST
	@Path("insert")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Create new user without syncing it to squidGuard. This function will be used only by import.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("user.add")
	public CrxResponse insert(
		@ApiParam(hidden = true) @Auth Session session,
		User user
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse crxResponse = new UserService(session, em).add(user);
		em.close();
		return crxResponse;
	}

	@POST
	@Path("addList")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Create new users")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("user.add")
	public List<CrxResponse> add(
		@ApiParam(hidden = true) @Auth Session session,
		List<User> users
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<CrxResponse> crxResponses = new UserService(session, em).add(users);
		sync(session);
		em.close();
		return crxResponses;
	}

	@GET
	@Path("{userId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get user by id")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "User not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.search")
	public User getById(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("userId") Long userId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		final User user = new UserService(session, em).getById(userId);
		em.close();
		if (user == null) {
			throw new WebApplicationException(404);
		}
		return user;
	}

	@GET
	@Path("text/{uid}/devices/{className}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get the devices of a user from an AdHocLan ClassRoom")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "User not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.search")
	public List<Device> getUserDevicesInRoomClassRoom(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("uid") String uid,
		@PathParam("className") String className
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		final List<Device> resp = new UserService(session, em).getUserDevicesInRoomClassRoom(uid,className);
		em.close();
		return resp;
	}

	@DELETE
	@Path("{userId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "delete user by id")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "User not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.delete")
	public CrxResponse delete(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("userId") Long userId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse crxResponse = new UserService(session, em).delete(userId);
		em.close();
		return crxResponse;
	}

	@POST
	@Path("modify")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "modify an existing user")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("user.modify")
	public CrxResponse modify(
		@ApiParam(hidden = true) @Auth Session session,
		User user
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		final UserService userService = new UserService(session, em);
		CrxResponse crxResponse = userService.modify(user);
		em.close();
		return crxResponse;
	}

	@POST
	@Path("{userId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "modify an existing user")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("user.modify")
	public CrxResponse modify(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("userId") Long userId,
		User user
	) {
		user.setId(userId);
		return this.modify(session, user);
	}

	@GET
	@Path("{userId}/groups")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get groups the user is member in it.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "User not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.search")
	public List<Group> groups(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("userId") Long userId
	) {
		return this.getById(session, userId).getGroups();
	}

	@GET
	@Path("{userId}/availableGroups")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get groups the user is not member in it.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "User not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.search")
	public List<Group> getAvailableGroups(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("userId") Long userId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		final List<Group> groups = new UserService(session, em).getAvailableGroups(userId);
		em.close();
		if (groups == null) {
			throw new WebApplicationException(404);
		}
		return groups;
	}

	@GET
	@Path("byRole/{role}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get users from a rolle")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("user.search")
	public List<User> getByRole(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("role") String role
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		final List<User> users = new UserService(session, em).getByRole(role);
		em.close();
		if (users == null) {
			throw new WebApplicationException(404);
		}
		return users;
	}


	@POST
	@Path("{userId}/groups")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Put the user to this groups as member additionaly.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "Group not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.manage")
	public CrxResponse addToGroups(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("userId") Long userId,
		List<Long> groups
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		StringBuilder error = new StringBuilder();
		final GroupService groupService = new GroupService(session, em);
		for (Long groupId : groups) {
			CrxResponse crxResponse = groupService.addMember(groupId, userId);
			if (!crxResponse.getCode().equals("OK")) {
			error.append(crxResponse.getValue()).append("<br>");
			}
		}
		em.close();
		if (error.length() > 0) {
			return new CrxResponse(session, "ERROR", error.toString());
		}
		return new CrxResponse(session, "OK", "User was added to the additional group.");
	}

	@POST
	@Path("{userId}/groups/set")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Put the user to this groups as member. The user will be removed from all other group.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "Group not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.manage")
	public CrxResponse setGroups(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("userId") Long userId,
		List<Long> groupIds
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse crxResponse = new UserService(session, em).setGroups(userId, groupIds);
		em.close();
		return crxResponse;
	}


	@DELETE
	@Path("{userId}/{groupId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Removes the user from a group.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "User not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.manage")
	public CrxResponse removeMember(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("groupId") Long groupId,
		@PathParam("userId") Long userId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		final GroupService groupService = new GroupService(session, em);
		CrxResponse crxResponse = groupService.removeMember(groupId, userId);
		em.close();
		return crxResponse;
	}

	@PUT
	@Path("{userId}/{groupId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Add user to a group.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "User not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.manage")
	public CrxResponse addMember(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("groupId") Long groupId,
		@PathParam("userId") Long userId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		final GroupService groupService = new GroupService(session, em);
		CrxResponse crxResponse = groupService.addMember(groupId, userId);
		em.close();
		return crxResponse;
	}

	@GET
	@Path("byUid/{uid}/{attribute}")
	@Produces(TEXT)
	@ApiOperation(value = "Reads some attributes from a user. Available attributes are: role uuid givenname surname groups.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "User not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.search")
	public String getUserAttribute(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("uid") String uid,
		@PathParam("attribute") String attribute
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		final UserService userService = new UserService(session, em);
		String resp;
		User user = userService.getByUid(uid);
		if (user == null) {
			return "";
		}
		switch (attribute.toLowerCase()) {
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
				resp = userService.getHomeDir(user);
				break;
			case "groups":
				List<String> groups = new ArrayList<String>();
				for (Group group : user.getGroups()) {
					groups.add(group.getName());
				}
				resp = String.join(userService.getNl(), groups);
				break;
			default:
			//This is a config or mconfig. We have to merge it from the groups from actual room and from the user
			//I think it is senceless
			List<String> configs = new ArrayList<String>();
			//Group configs
			for (Group group : user.getGroups()) {
				if (userService.getConfig(group, attribute) != null) {
				configs.add(userService.getConfig(group, attribute));
				}
				for (String config : userService.getMConfigs(group, attribute)) {
				if (config != null) {
					configs.add(config);
				}
				}
			}
			//Room configs.
			if (session.getRoom() != null) {
				if (userService.getConfig(session.getRoom(), attribute) != null) {
				configs.add(userService.getConfig(session.getRoom(), attribute));
				}
				for (String config : userService.getMConfigs(session.getRoom(), attribute)) {
				if (config != null) {
					configs.add(config);
				}
				}
			}
			if (userService.getConfig(user, attribute) != null) {
				configs.add(userService.getConfig(user, attribute));
			}
			for (String config : userService.getMConfigs(user, attribute)) {
				if (config != null) {
				configs.add(config);
				}
			}
			resp = String.join(userService.getNl(), configs);
		}
		em.close();
		return resp;
	}

	@GET
	@Path("uidsByRole/{role}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get users from a rolle")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("user.search")
	public String getUidsByRole(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("role") String role
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		final UserService userService = new UserService(session, em);
		List<String> users = new ArrayList<String>();
		for (User user : userService.getByRole(role)) {
			users.add(user.getUid());
		}
		String resp = String.join(userService.getNl(), users);
		em.close();
		return resp;
	}

	/*
	 * Mange gast user
	 */
	@GET
	@Path("guestUsers")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets all actual gast users. Systadmins get the lists all guest users. Normal users gets the own gast users.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "User not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.guestusers")
	public List<GuestUsers> getGuestUsers(
		@ApiParam(hidden = true) @Auth Session session
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<GuestUsers> resp = new UserService(session, em).getGuestUsers();
		em.close();
		return resp;
	}

	@GET
	@Path("guestUsers/{guestUsersId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets a guest users category.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "User not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.guestusers")
	public Category getGuestUsersCategory(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("guestUsersId") Long guestUsersId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		Category resp = new UserService(session, em).getGuestUsersCategory(guestUsersId);
		em.close();
		return resp;
	}

	@DELETE
	@Path("guestUsers/{guestUsersId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delete a guest users category.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "User not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.guestusers")
	public CrxResponse deleteGuestUsers(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("guestUsersId") Long guestUsersId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new UserService(session, em).deleteGuestUsers(guestUsersId);
		em.close();
		return resp;
	}

	@POST
	@Path("guestUsers/add")
	@Produces(JSON_UTF8)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation(value = "Creates a new printer.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.guestusers")
	public CrxResponse addGuestUsers(
		@ApiParam(hidden = true) @Auth Session session,
		@FormDataParam("name") String name,
		@FormDataParam("description") String description,
		@FormDataParam("roomId") Long roomId,
		@FormDataParam("count") Integer count,
		@FormDataParam("validUntil") Date validUntil
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		GuestUsers guestUsers = new GuestUsers(name, description, count, roomId, validUntil);
		CrxResponse resp = new UserService(session, em).addGuestUsers(guestUsers);
		em.close();
		return resp;
	}

	/*
	 * Some api calls with text arguments
	 */
	@DELETE
	@Path("text/{userName}")
	@Produces(TEXT)
	@ApiOperation(value = "Delets a user presented by name.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "User not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.delete")
	public String delete(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("userName") String userName
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse crxResponse = new UserService(session, em).delete(userName);
		String resp = crxResponse.getCode();
		if (crxResponse.getCode().equals("ERROR")) {
			resp = resp + " " + crxResponse.getValue();
		}
		em.close();
		return resp;
	}

	@GET
	@Path("text/{userName}/groups")
	@Produces(TEXT)
	@ApiOperation(value = "Delivers a new line separated list of group of the user.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "User not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.manage")
	public String getGroups(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("userName") String userName
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		String resp = new UserService(session, em).getGroupsOfUser(userName, "workgroup");
		em.close();
		return resp;
	}

	@GET
	@Path("text/{userName}/classes")
	@Produces(TEXT)
	@ApiOperation(value = "Delivers a new line separated list of classes of the user.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "User not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.manage")
	public String getClasses(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("userName") String userName
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		String resp = new UserService(session, em).getGroupsOfUser(userName, "class");
		em.close();
		return resp;
	}

	@PUT
	@Path("text/{userName}/groups/{groupName}")
	@Produces(TEXT)
	@ApiOperation(value = "Add a user to a group.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "User not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.manage")
	public String addToGroup(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("userName") String userName,
		@PathParam("groupName") String groupName
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse crxResponse = new GroupService(session, em).addMember(groupName, userName);
		String resp = crxResponse.getCode();
		if (crxResponse.getCode().equals("ERROR")) {
			resp = resp + " " + crxResponse.getValue();
		}
		em.close();
		return resp;
	}

	@DELETE
	@Path("text/{userName}/groups/{groupName}")
	@Produces(TEXT)
	@ApiOperation(value = "Removes a user from a group.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "User not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.manage")
	public String removeFromGroup(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("userName") String userName,
		@PathParam("groupName") String groupName
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse crxResponse = new GroupService(session, em).removeMember(groupName, userName);
		String resp = crxResponse.getCode();
		if (crxResponse.getCode().equals("ERROR")) {
			resp = resp + " " + crxResponse.getValue();
		}
		em.close();
		return resp;
	}

	@PUT
	@Path("text/{userName}/allClassess")
	@Produces(TEXT)
	@ApiOperation(value = "Add a user to all classes.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "User not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.manage")
	public String addToAllClasses(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("userName") String userName
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		User user = new UserService(session, em).getByUid(userName);
		final GroupService groupService = new GroupService(session, em);
		for (Group group : groupService.getByType("class")) {
			groupService.addMember(group, user);
		}
		em.close();
		return "OK";
	}

	@GET
	@Path("text/createUid/{givenName}/{surName}/{birthDay}")
	@Produces(TEXT)
	@ApiOperation(value = "Creates an uid from givenname and surname.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "User not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.manage")
	public String createUid(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("givenName") String givenName,
		@PathParam("surName") String surName,
		@PathParam("birthDay") String birthDay
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		String resp = new UserService(session, em).createUid(givenName, surName, birthDay);
		em.close();
		return resp;
	}

	/*
	 * Import handling
	 */
	@POST
	@Path("import")
	@Produces(JSON_UTF8)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation(value = "Import a list of users. The parameters:"
		+ "* role The role of the users to import."
		+ "* lang The language of the header."
		+ "* identifier The attribute tu identify the user: sn-gn-bd, uid, uuid"
		+ "* test Test run only."
		+ "* password Set this a password for all new user."
		+ "* mustChange New user has to change the password by first login."
		+ "* The next parameters has onyl effect when role == students"
		+ "* full Does this file contains all students."
		+ "* allClasses Classes which are not in the list must be deleted."
		+ "* cleanClassDirs Clean all class directories."
		+ "* resetPassword Also old user will get new password.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.add")
	public CrxResponse importUser(
		@ApiParam(hidden = true) @Auth Session session,
		@FormDataParam("role") String role,
		@FormDataParam("lang") String lang,
		@FormDataParam("identifier") String identifier,
		@FormDataParam("test") Boolean test,
		@FormDataParam("password") String password,
		@FormDataParam("mustChange") Boolean mustChange,
		@FormDataParam("full") Boolean full,
		@FormDataParam("allClasses") Boolean allClasses,
		@FormDataParam("cleanClassDirs") Boolean cleanClassDirs,
		@FormDataParam("resetPassword") Boolean resetPassword,
		@FormDataParam("appendBirthdayToPassword") Boolean appendBirthdayToPassword,
		@FormDataParam("appendClassToPassword") Boolean appendClassToPassword,
		@FormDataParam("file") final InputStream fileInputStream,
		@FormDataParam("file") final FormDataContentDisposition contentDispositionHeader
	) {
		File file = null;
		try {
			file = File.createTempFile("crx", "importUser", new File(cranixTmpDir));
			Files.copy(fileInputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return new CrxResponse(session, "ERROR", "Import file can not be saved" + e.getMessage());
		}
		try {
			Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
		} catch (IOException ioe) {
			logger.debug("Import file is not UTF-8 try ISO-8859-1");
			try {
			List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.ISO_8859_1);
			List<String> utf8lines = new ArrayList<String>();
			for (String line : lines) {
				byte[] utf8 = new String(line.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.ISO_8859_1).getBytes(StandardCharsets.UTF_8);
				utf8lines.add(new String(utf8, StandardCharsets.UTF_8));
			}
			Files.write(file.toPath(), utf8lines);

			} catch (IOException ioe2) {
			return new CrxResponse(session, "ERROR", "Import file is not UTF-8 coded.");
			}
		}
		if (password != null && !password.isEmpty()) {
			UserService uc = new UserService(session, null);
			Boolean checkPassword = uc.getConfigValue("CHECK_PASSWORD_QUALITY").toLowerCase().equals("yes");
			uc.setConfigValue("CHECK_PASSWORD_QUALITY", "no");
			CrxResponse passwordResponse = uc.checkPassword(password);
			if (passwordResponse != null) {
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
		parameters.add("/usr/sbin/crx_import_user_list.py");
		parameters.add("--input");
		parameters.add(file.getAbsolutePath());
		parameters.add("--role");
		parameters.add(role);
		parameters.add("--lang");
		parameters.add(lang);
		if (identifier != null && !identifier.isEmpty()) {
			parameters.add("--identifier");
			parameters.add(identifier);
		}
		if (test) {
			parameters.add("--test");
		}
		if (password != null && !password.isEmpty()) {
			parameters.add("--password");
			parameters.add(password);
		}
		if (mustChange) {
			parameters.add("--mustChange");
		}
		if (full) {
			parameters.add("--full");
		}
		if (allClasses) {
			parameters.add("--allClasses");
		}
		if (cleanClassDirs) {
			parameters.add("--cleanClassDirs");
		}
		if (resetPassword) {
			parameters.add("--resetPassword");
		}
		if (appendBirthdayToPassword) {
			parameters.add("--appendBirthdayToPassword");
		}
		if (appendClassToPassword) {
			parameters.add("--appendClassToPassword");
		}
		if (logger.isDebugEnabled()) {
			parameters.add("--debug");
		}
		String[] program = new String[parameters.size()];
		program = parameters.toArray(program);

		logger.debug("Start import:" + parameters);
		StringBuffer reply = new StringBuffer();
		StringBuffer stderr = new StringBuffer();
		CrxSystemCmd.exec(program, reply, stderr, null);
		return new CrxResponse(session, "OK", "Import was started.");
	}

	@GET
	@Path("imports")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get the list of imports.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "User not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.manage")
	public List<UserImport> getImports(@ApiParam(hidden = true) @Auth Session session) {
		Service controller = new Service(session, null);
		StringBuilder importDir = controller.getImportDir("");
		List<UserImport> imports = new ArrayList<UserImport>();
		File importDirObject = new File(importDir.toString());
		if (importDirObject.isDirectory()) {
			for (String file : importDirObject.list()) {
			UserImport userImport = getImport(session, file.replaceAll(importDir.append("/").toString(), ""));
			if (userImport != null) {
				imports.add(userImport);
			}
			}
		}
		return imports;
	}

	@GET
	@Path("imports/{startTime}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get the list of imports.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "User not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.manage")
	public UserImport getImport(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("startTime") String startTime
	) {
		Service controller = new Service(session, null);
		String content;
		UserImport userImport;
		String importLog = controller.getImportDir(startTime).append("/import.log").toString();
		String importJson = controller.getImportDir(startTime).append("/parameters.json").toString();
		ObjectMapper mapper = new ObjectMapper();
		logger.debug("getImport 1:" + startTime);
		try {
			content = String.join("", Files.readAllLines(Paths.get(importJson)));
			userImport = mapper.readValue(IOUtils.toInputStream(content, "UTF-8"), UserImport.class);
		} catch (IOException e) {
			logger.debug("getImport 2:" + e.getMessage());
			return null;
		}
		try {
			content = String.join("", Files.readAllLines(Paths.get(importLog), StandardCharsets.UTF_8));
			userImport.setResult(content);
		} catch (IOException e) {
			logger.debug("getImport 3:" + importLog + " " + content + "####" + e.getMessage());
		}
		return userImport;
	}

	@PUT
	@Path("imports/{startTime}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Restart an user import.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "User import not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.manage")
	public CrxResponse restartImport(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("startTime") String startTime
	) {
		UserImport userImport = getImport(session, startTime);
		if (userImport != null) {
			Service controller = new Service(session, null);
			StringBuilder importFile = controller.getImportDir(startTime);
			importFile.append("/userlist.txt");
			List<String> parameters = new ArrayList<String>();
			parameters.add("/sbin/startproc");
			parameters.add("-l");
			parameters.add("/var/log/import-user.log");
			parameters.add("/usr/sbin/crx_import_user_list.py");
			parameters.add("--input");
			parameters.add(importFile.toString());
			parameters.add("--role");
			parameters.add(userImport.getRole());
			parameters.add("--lang");
			parameters.add(userImport.getLang());
			parameters.add("--identifier");
			parameters.add(userImport.getIdentifier());
			if (!userImport.getPassword().isEmpty()) {
				parameters.add("--password");
				parameters.add(userImport.getPassword());
			}
			if (userImport.isMustChange()) {
				parameters.add("--mustChange");
			}
			if (userImport.isFull()) {
				parameters.add("--full");
			}
			if (userImport.isAllClasses()) {
				parameters.add("--allClasses");
			}
			if (userImport.isCleanClassDirs()) {
				parameters.add("--cleanClassDirs");
			}
			if (userImport.isResetPassword()) {
				parameters.add("--resetPassword");
			}
			if (userImport.isAppendBirthdayToPassword()) {
				parameters.add("--appendBirthdayToPassword");
			}
			if (userImport.isAppendClassToPassword()) {
				parameters.add("--appendClassToPassword");
			}
			if (logger.isDebugEnabled()) {
				parameters.add("--debug");
			}
			logger.debug("restartImport userImport:" + userImport);
			logger.debug("restartImport parameters:" + parameters);

			String[] program = new String[parameters.size()];
			program = parameters.toArray(program);

			logger.debug("Start import:" + parameters);
			StringBuffer reply = new StringBuffer();
			StringBuffer stderr = new StringBuffer();
			CrxSystemCmd.exec(program, reply, stderr, null);
			logger.debug("restartImport reply: " + reply.toString());
			logger.debug("restartImport error: " + reply.toString());
			return new CrxResponse(session, "OK", "Import was started.");
		}
		return new CrxResponse(session, "ERROR", "CAn not find the import.");
	}

	@DELETE
	@Path("imports/{startTime}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Remove an user import.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "User import not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.manage")
	public CrxResponse deleteImport(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("startTime") String startTime
	) {
	Service controller = new Service(session, null);
	StringBuilder importDir = controller.getImportDir(startTime);
	if (startTime == null || startTime.isEmpty()) {
		return new CrxResponse(session, "ERROR", "Invalid import name.");
	}
		String[] program = new String[3];
		program[0] = "rm";
		program[1] = "-rf";
		program[2] = importDir.toString();
		StringBuffer reply = new StringBuffer();
		StringBuffer stderr = new StringBuffer();
		CrxSystemCmd.exec(program, reply, stderr, null);
		return new CrxResponse(session, "OK", "Import was deleted.");
	}

	@DELETE
	@Path("imports/running")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Stops the running import.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "There is no running import found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.manage")
	public CrxResponse stopRunningImport(@ApiParam(hidden = true) @Auth Session session) {
		String[] program = new String[2];
		program[0] = "killall";
		program[1] = "crx_import_user_list.py";
		StringBuffer reply = new StringBuffer();
		StringBuffer stderr = new StringBuffer();
		CrxSystemCmd.exec(program, reply, stderr, null);
		program = new String[3];
		program[0] = "rm";
		program[1] = "-f";
		program[2] = "/run/crx_import_user";
		CrxSystemCmd.exec(program, reply, stderr, null);
		return new CrxResponse(session, "OK", "Import was stopped.");
	}

	@GET
	@Path("imports/running")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get the running import.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.manage")
	public UserImport getRunningImport(
		@ApiParam(hidden = true) @Auth Session session
	) {
		List<String> runningImport;
		try {
			runningImport = Files.readAllLines(Paths.get("/run/crx_import_user"));
			if (!runningImport.isEmpty()) {
			return getImport(session, runningImport.get(0));
			}
		} catch (IOException e) {
			return null;
		}
		return null;
	}

	@GET
	@Path("imports/{startTime}/{type}")
	@Produces("*/*")
	@ApiOperation(value = "Delivers result of the import as a zip archive of the pdf or txt files.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("user.manage")
	public Response getImportAsArchive(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("startTime") String startTime,
		@PathParam("type") String type
	) {
		StringBuilder importDir = new Service(session, null).getImportDir(startTime);
		String[] program = new String[3];
		program[0] = "/usr/share/cranix/tools/pack_import.sh";
		program[1] = startTime;
		program[2] = type;
		StringBuffer reply = new StringBuffer();
		StringBuffer stderr = new StringBuffer();
		CrxSystemCmd.exec(program, reply, stderr, null);
		File importFile = new File(importDir.append("/userimport.zip").toString());
		logger.debug("getImportAsArchive:" + importFile.getName());
		ResponseBuilder response = Response.ok(importFile);
		response = response.header("Content-Disposition", "attachment; filename=" + importFile.getName());
		return response.build();
	}

	/*
	 * Some additional stuff
	 */
	@PUT
	@Path("allTeachersInAllClasses")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get the list of imports.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "User not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.search")
	public CrxResponse allTeachersInAllClasses(@ApiParam(hidden = true) @Auth Session session) {
	EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
	final UserService userService = new UserService(session, em);
	final GroupService groupService = new GroupService(session, em);
	for (User user : userService.getByRole("teachers")) {
		for (Group group : groupService.getByType("class")) {
		groupService.addMember(group, user);
		}
	}
		em.close();
		return new CrxResponse(session, "OK", "All teachers was put into all classes.");
	}

	@PUT
	@Path("text/{userName}/alias/{alias}")
	@Produces(TEXT)
	@ApiOperation(value = "Add an alias to a user. Helper stuff only.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "User not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.manage")
	public String addUserAlias(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("userName") String userName,
		@PathParam("alias") String alias
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		UserService uc = new UserService(session, em);
		String resp = "Alias not unique";
		if (uc.isUserAliasUnique(alias)) {
			User user = uc.getByUid(userName);
			if (user != null) {
			Alias newAlias = new Alias(user, alias);
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

	@PUT
	@Path("text/{userName}/defaultAlias")
	@Produces(TEXT)
	@ApiOperation(value = "Add an alias to a user. Helper stuff only.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "User not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.manage")
	public String addUserDefaultAlias(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("userName") String userName
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		UserService uc = new UserService(session, em);
		User user = uc.getByUid(userName);
		CrxResponse crxResponse = uc.addDefaultAliase(user);
		String resp = crxResponse.getCode();
		if (crxResponse.getCode().equals("ERROR")) {
			resp = resp + " " + crxResponse.getValue();
		}
		em.close();
		return resp;
	}

	@PUT
	@Path("{userId}/allClasses")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Add user to a group.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "User not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.manage")
	public CrxResponse allClasses(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("userId") Long userId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		User user = new UserService(session, em).getById(userId);
		final GroupService groupService = new GroupService(session, em);
		for (Group group : groupService.getByType("class")) {
			groupService.addMember(group, user);
		}
		em.close();
		return new CrxResponse(session, "OK", "User was put into all classes.");
	}

	@POST
	@Path("addUsersToGroups")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Add more user to more groups. The parameter ids is a list of two List<Long>:"
		+ "The first list is the list of users."
		+ "The second list is the list of groups into to user need to be added.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "User not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.manage")
	public CrxResponse addUsersToGroups(
		@ApiParam(hidden = true) @Auth Session session,
		List<List<Long>> ids
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		final GroupService groupService = new GroupService(session, em);
		List<User> users = new UserService(session, em).getUsers(ids.get(0));
		for (Long groupId : ids.get(1)) {
			Group group = groupService.getById(groupId);
			groupService.addMembers(group, users);
		}
		em.close();
		return new CrxResponse(session, "OK", "Users was inserted in the required groups.");
	}

	@POST
	@Path("syncFsQuotas")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Synchronize the file system quota values into the JPA")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "User not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.manage")
	public CrxResponse syncFsQuotas(
		@ApiParam(hidden = true) @Auth Session session,
		List<List<String>> Quotas
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		final UserService userService = new UserService(session, em);
		CrxResponse crxResponse = userService.syncFsQuotas(Quotas);
		em.close();
		return crxResponse;
	}

	@POST
	@Path("syncMsQuotas")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Synchronize the file system quota values into the JPA")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "User not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.manage")
	public CrxResponse syncMsQuotas(
		@ApiParam(hidden = true) @Auth Session session,
		List<List<String>> Quotas
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new UserService(session, em).syncMsQuotas(Quotas);
		em.close();
		return resp;
	}

	@POST
	@Path("sync")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Synchronize group lists into squid.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "User not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.manage")
	public CrxResponse sync(@ApiParam(hidden = true) @Auth Session session) {
		String[] program = new String[1];
		program[0] = "/usr/sbin/crx_refresh_squidGuard_user.sh";
		StringBuffer reply = new StringBuffer();
		StringBuffer stderr = new StringBuffer();
		CrxSystemCmd.exec(program, reply, stderr, null);
		return new CrxResponse(session, "OK", "Import was started.");
	}

	@POST
	@Path("applyAction")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Apply actions on selected users.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.manage")
	public List<CrxResponse> applyAction(
		@ApiParam(hidden = true) @Auth Session session,
		CrxActionMap crxActionMap
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<CrxResponse> responses = new UserService(session, em).applyAction(crxActionMap);
		em.close();
		return responses;
	}
}
