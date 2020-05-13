/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.api.resources;


import static de.cranix.api.resources.Resource.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.*;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.*;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import de.cranix.dao.User;
import de.cranix.dao.UserImport;
import de.cranix.dao.Category;
import de.cranix.dao.Group;
import de.cranix.dao.CrxActionMap;
import de.cranix.dao.CrxResponse;
import de.cranix.dao.Session;

@Path("users")
@Api(value = "users")
public interface UserResource {

	/*
	 * GET users/<userId>
	 */
	@GET
	@Path("{userId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get user by id")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "User not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.search")
	User getById(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("userId") Long userId
			);

	/*
	 * GET users/<userId>
	 */
	@GET
	@Path("{userId}/groups")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get groups the user is member in it.")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "User not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.search")
	List<Group> groups(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("userId") Long userId
			);

	/*
	 * GET users/<userId>
	 */
	@GET
	@Path("{userId}/availableGroups")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get groups the user is not member in it.")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "User not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.search")
	List<Group> getAvailableGroups(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("userId") Long userId
			);

	/*
	 * GET users/byRole/<role>
	 */
	@GET
	@Path("byRole/{role}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get users from a rolle")
	@ApiResponses(value = {
			@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("user.search")
	List<User> getByRole(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("role") String role
			);

	/*
	 * GET users/getAll
	 */
	@GET
	@Path("all")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get all users")
	@ApiResponses(value = {
			// TODO so oder anders? @ApiResponse(code = 404, message = "At least one user was not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("user.search")
	@JsonIgnoreProperties({"groups"})
	List<User> getAll(
			@ApiParam(hidden = true) @Auth Session session
			);

	/*
	 * GET search/{search}
	 */
	@GET
	@Path("search/{search}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Search for user by uid and name.")
	@ApiResponses(value = {
			// TODO so oder anders? @ApiResponse(code = 404, message = "At least one user was not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	//@PermitAll
	@RolesAllowed("user.search")
	List<User> search(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("search") String search
			);

	/*
	 * POST user/getUsers
	 */
	@POST
	@Path("getUsers")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets a list of user objects to the list of userIds.")
	@ApiResponses(value = {
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.search")
	List<User> getUsers(
			@ApiParam(hidden = true) @Auth Session session,
			List<Long> userIds
			);

	/*
	 * POST users/add { hash }
	 */
	@POST
	@Path("add")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Create new user and syncing it to squidGuard.")
	@ApiResponses(value = {
			// TODO so oder anders? @ApiResponse(code = 404, message = "At least one user was not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("user.add")
	CrxResponse add(
			@ApiParam(hidden = true) @Auth Session session,
			User user
			);

	/*
	 * POST users/insert { hash }
	 */
	@POST
	@Path("insert")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Create new user without syncing it to squidGuard.")
	@ApiResponses(value = {
			// TODO so oder anders? @ApiResponse(code = 404, message = "At least one user was not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("user.add")
	CrxResponse insert(
			@ApiParam(hidden = true) @Auth Session session,
			User user
			);

	/*
	 * POST users/add [ { hash }, { user } ]
	 */
	@POST
	@Path("addList")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Create new users")
	@ApiResponses(value = {
			// TODO so oder anders? @ApiResponse(code = 404, message = "At least one user was not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	//@PermitAll
	@RolesAllowed("user.add")
	List<CrxResponse> add(
			@ApiParam(hidden = true) @Auth Session session,
			List<User> users
			);

	/*
	 * POST users/modify { hash }
	 */
	@POST
	@Path("modify")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "modify an existing user")
	@ApiResponses(value = {
			// TODO so oder anders? @ApiResponse(code = 404, message = "At least one user was not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	//@PermitAll
	@RolesAllowed("user.modify")
	CrxResponse modify(
			@ApiParam(hidden = true) @Auth Session session,
			User user
			);

	@POST
	@Path("{userId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "modify an existing user")
	@ApiResponses(value = {
			// TODO so oder anders? @ApiResponse(code = 404, message = "At least one user was not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	//@PermitAll
	@RolesAllowed("user.modify")
	CrxResponse modify(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("userId") Long userId,
			User user
			);
	/*
	 * DELETE users/<userId>
	 */
	@DELETE
	@Path("{userId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "delete user by id")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "User not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.delete")
	CrxResponse delete(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("userId") Long userId
			);


	/*
	 * POST users/<userId>/groups
	 */
	@POST
	@Path("{userId}/groups")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Put the user to this groups as member additionaly.")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "Group not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.manage")
	CrxResponse addToGroups(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("userId") Long userId,
			List<Long> groups
			);

	@POST
	@Path("{userId}/groups/set")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Put the user to this groups as member. The user will be removed from all other group.")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "Group not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.manage")
	CrxResponse setMembers(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("userId") Long userId,
			List<Long> groups
			);


	/*
	 * DELETE users/<userId>/<groupId>
	 */
	@DELETE
	@Path("{userId}/{groupId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Removes the user from a group.")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "User not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.manage")
	CrxResponse removeMember(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("groupId") Long groupId,
			@PathParam("userId") Long userId
			);

	/*
	 * PUT users/<groupId>/<userId>
	 */
	@PUT
	@Path("{userId}/{groupId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Add user to a group.")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "User not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.manage")
	CrxResponse addMember(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("groupId") Long groupId,
			@PathParam("userId") Long userId
			);

	/*
	 * PUT users/<groupId>/<userId>
	 */
	@PUT
	@Path("{userId}/allClasses")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Add user to a group.")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "User not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.manage")
	CrxResponse allClasses(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("userId") Long userId
			);

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
	CrxResponse addUsersToGroups(
			@ApiParam(hidden = true) @Auth Session session,
			List<List<Long>> ids
			);

	/*
	 * POST syncFsQuotas
	 */
	@POST
	@Path("syncFsQuotas")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Synchronize the file system quota values into the JPA")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "User not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.manage")
	CrxResponse syncFsQuotas(
			@ApiParam(hidden = true) @Auth Session session,
			List<List<String>> Quotas
			);

	/*
	 * POST syncFsQuotas
	 */
	@POST
	@Path("syncMsQuotas")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Synchronize the file system quota values into the JPA")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "User not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.manage")
	CrxResponse syncMsQuotas(
			@ApiParam(hidden = true) @Auth Session session,
			List<List<String>> Quotas
			);



	/**
	 * PUT users/sync Synchronize user to other systems. In first case the role membership for squid is concerned
	 * This must be called if new user was created or removed or after import of users.
	 * @param session
	 * @return The result in an ossResponse object.
	 */
	@POST
	@Path("sync")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Synchronize the file system quota values into the JPA")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "User not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.manage")
	CrxResponse sync(
			@ApiParam(hidden = true) @Auth Session session
			);

	/*
	 * GET users/byUid/{uid}/{attribute}
	 * Get's an attribute from a user
	 */
	@GET
	@Path("byUid/{uid}/{attribute}")
	@Produces(TEXT)
	@ApiOperation(value = "Reads some attributes from a user. Available attributes are: role uuid givenname surname groups.")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "User not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.search")
	String getUserAttribute(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("uid")  String uid,
			@PathParam("attribute") String attribute
			);

	/*
	 * GET users/byRole/<role>
	 */
	@GET
	@Path("uidsByRole/{role}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get users from a rolle")
	@ApiResponses(value = {
			@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("user.search")
	String getUidsByRole(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("role") String role
			);


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
	List<Category> getGuestUsers(
			@ApiParam(hidden = true) @Auth Session session
			);

	@GET
	@Path("guestUsers/{guestUsersId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets a guest users category.")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "User not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.guestusers")
	Category getGuestUsersCategory(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("guestUsersId")     Long    guestUsersId
			);

	@DELETE
	@Path("guestUsers/{guestUsersId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delete a guest users category.")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "User not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.guestusers")
	CrxResponse  deleteGuestUsers(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("guestUsersId")     Long    guestUsersId
			);

	@POST
	@Path("guestUsers/add")
	@Produces(JSON_UTF8)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation(value = "Creates a new printer.")
	@ApiResponses(value = {
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.guestusers")
	CrxResponse addGuestUsers(
			@ApiParam(hidden = true) @Auth Session session,
			@FormDataParam("name")          String  name,
			@FormDataParam("description")   String  description,
			@FormDataParam("roomId") Long    roomId,
			@FormDataParam("count")  Long    count,
			@FormDataParam("validUntil")    Date    validUntil
			);

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
	String  delete(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("userName")     String    userName
			);

	@GET
	@Path("text/{userName}/groups")
	@Produces(TEXT)
	@ApiOperation(value = "Delivers a new line separated list of group of the user.")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "User not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.manage")
	String  getGroups(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("userName")     String    userName
			);

	@GET
	@Path("text/{userName}/classes")
	@Produces(TEXT)
	@ApiOperation(value = "Delivers a new line separated list of classes of the user.")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "User not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.manage")
	String  getClasses(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("userName")     String    userName
			);

	@PUT
	@Path("text/{userName}/groups/{groupName}")
	@Produces(TEXT)
	@ApiOperation(value = "Add a user to a group.")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "User not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.manage")
	String  addToGroup(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("userName")     String    userName,
			@PathParam("groupName")    String    groupName
			);

	@PUT
	@Path("text/{userName}/allClassess")
	@Produces(TEXT)
	@ApiOperation(value = "Add a user to all classes.")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "User not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.manage")
	String  addToAllClasses(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("userName")     String    userName
			);

	@POST
	@Path("text/{userName}/groups/{groupName}")
	@Produces(TEXT)
	@ApiOperation(value = "Set the user as owner of a group. Helper stuff only.")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "User not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.manage")
	String  addGroupToUser(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("userName")     String    userName,
			@PathParam("groupName")    String    groupName
			);

	@PUT
	@Path("text/{userName}/alias/{alias}")
	@Produces(TEXT)
	@ApiOperation(value = "Add an alias to a user. Helper stuff only.")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "User not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.manage")
	String  addUserAlias(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("userName")     String    userName,
			@PathParam("alias")        String    alias
			);

	@PUT
	@Path("text/{userName}/defaultAlias")
	@Produces(TEXT)
	@ApiOperation(value = "Add an alias to a user. Helper stuff only.")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "User not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.manage")
	String  addUserDefaultAlias(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("userName")	String	userName
			);

	@DELETE
	@Path("text/{userName}/groups/{groupName}")
	@Produces(TEXT)
	@ApiOperation(value = "Removes a user from a group.")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "User not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.manage")
	String removeFromGroup(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("userName")     String    userName,
			@PathParam("groupName")    String    groupName
			);

	@GET
	@Path("text/createUid/{givenName}/{surName}/{birthDay}")
	@Produces(TEXT)
	@ApiOperation(value = "Creates an uid from givenname and surname.")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "User not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.manage")
	String createUid(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("givenName")  String    givenName,
			@PathParam("surName")    String    surName,
			@PathParam("birthDay")   Date      birthDay
			);

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
	CrxResponse importUser(
			@ApiParam(hidden = true) @Auth   Session session,
			@FormDataParam("role")           String  role,
			@FormDataParam("lang")      String  lang,
			@FormDataParam("identifier")  String  identifier,
			@FormDataParam("test")  boolean test,
			@FormDataParam("password")       String  password,
			@FormDataParam("mustChange")     boolean mustChange,
			@FormDataParam("full")  boolean full,
			@FormDataParam("allClasses")     boolean allClasses,
			@FormDataParam("cleanClassDirs") boolean cleanClassDirs,
			@FormDataParam("resetPassword")  boolean resetPassword,
			@FormDataParam("appendBirthdayToPassword")  boolean appendBirthdayToPassword,
			@FormDataParam("file") final InputStream fileInputStream,
			@FormDataParam("file") final FormDataContentDisposition contentDispositionHeader
			);

	@GET
	@Path("imports")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get the list of imports.")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "User not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.manage")
	List<UserImport> getImports(
			@ApiParam(hidden = true) @Auth Session session
			);

	@GET
	@Path("imports/{startTime}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get the list of imports.")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "User not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.manage")
	UserImport getImport(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("startTime")  String    startTime
			);

	@PUT
	@Path("imports/{startTime}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get the list of imports.")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "User not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.manage")
	CrxResponse restartImport(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("startTime")  String    startTime
			);

	@DELETE
	@Path("imports/{startTime}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get the list of imports.")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "User not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.manage")
	CrxResponse deleteImport(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("startTime")  String    startTime
			);

	@GET
	@Path("imports/running")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get the list of imports.")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "User not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.manage")
	UserImport getRunningImport(
			@ApiParam(hidden = true) @Auth Session session
			);

	@GET
	@Path("imports/{startTime}/pdf")
	@Produces("*/*")
	@ApiOperation(value = "Delivers result of the import as PDF file.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("user.manage")
	Response getImportAsPdf(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("startTime")  String    startTime
	);

	@GET
	@Path("imports/{startTime}/txt")
	@Produces("*/*")
	@ApiOperation(value = "Delivers result of the import as txt file.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("user.manage")
	Response getImportAsTxt(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("startTime")  String    startTime
	);

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
	CrxResponse allTeachersInAllClasses(
			@ApiParam(hidden = true) @Auth Session session
			);
	/**
	 * Apply actions on a list of users.
	 * @param session
	 * @return The result in an CrxResponse object
	 * @see CrxResponse
	 */
	@POST
	@Path("applyAction")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Apply actions on selected institutes.")
	@ApiResponses(value = {
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("user.manage")
	CrxResponse applyAction(
			@ApiParam(hidden = true) @Auth Session session,
			CrxActionMap actionMap
			);

}
