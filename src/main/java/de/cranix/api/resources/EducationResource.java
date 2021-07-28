/* (c) 2020 Péter Varkoly <pvarkoly@cephalix.eu> - all rights reserved  */
package de.cranix.api.resources;


import static de.cranix.api.resources.Resource.*;
import static de.cranix.helper.CranixConstants.*;

import io.dropwizard.auth.Auth;
import io.swagger.annotations.*;
import javax.annotation.security.RolesAllowed;
import javax.annotation.security.PermitAll;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.cranix.dao.*;
import de.cranix.services.*;
import de.cranix.helper.CrxEntityManagerFactory;

@Path("education")
@Api(value = "education")
public class EducationResource {

	Logger logger = LoggerFactory.getLogger(EducationResource.class);

	public EducationResource() { }

	/******************************/
	/* Functions to handle rooms  */
	/******************************/

	@GET
	@Path("myRooms")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the of the rooms the session user may control.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	public List<Room> getMyRooms( @ApiParam(hidden = true) @Auth Session session) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<Room> resp = new EducationService(session,em).getMyRooms();
		em.close();
		return resp;
	}

	@GET
	@Path("rooms/{roomId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the state of a room. This call delivers a list of list with the logged in users. " +
			      "A logged in user list has the format: [ userId , deviceId ] "
		    )
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	public List<List<Long>>  getRoom(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("roomId") Long roomId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<List<Long>> resp = new EducationService(session,em).getRoom(roomId);
		em.close();
		return resp;
	}

	@GET
	@Path("rooms/{roomId}/details")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the state of a room. This call delivers all Informations about a room."
		    )
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	public SmartRoom  getRoomDetails(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("roomId") Long roomId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		SmartRoom resp = new SmartRoom(session,em,roomId);
		em.close();
		return resp;
	}

	@GET
	@Path("rooms/{roomId}/control/{minutes}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get the control for a room for an amount of time."
		    )
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	public CrxResponse  getRoomControl(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("roomId")  Long roomId,
		@PathParam("minutes") Long minutes
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new EducationService(session,em).getRoomControl(roomId,minutes);
		em.close();
		return resp;
	}

	@GET
	@Path("rooms/{roomId}/accessStatus")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the actual access in a room")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	public AccessInRoom getAccessStatus(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("roomId") long roomId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		AccessInRoom resp = new RoomService(session,em).getAccessStatus(roomId);
		em.close();
		return resp;
	}

	@POST
	@Path("rooms/{roomId}/accessStatus")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Sets the actual access in a room")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	public CrxResponse setAccessStatus(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("roomId") long roomId,
		AccessInRoom access
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		access.setAllowSessionIp(true);
		CrxResponse resp = new RoomService(session,em).setAccessStatus(roomId, access);
		em.close();
		return resp;
	}

	@GET
	@Path("rooms/{roomId}/actions")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delivers a list of available actions for a room.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	public List<String> getAvailableRoomActions(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("roomId") Long roomId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<String> resp = new EducationService(session,em).getAvailableRoomActions(roomId);
		em.close();
		return resp;
	}

	/*
	 * Deprecated use applyAction instead of
	 */
	@PUT
	@Path("rooms/{roomId}/{action}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Manage a room. Valid actions are download, open, close, reboot, shutdown, wol, logout, lockInput, unlockInput, .")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	public CrxResponse manageRoom(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("roomId") Long roomId,
		@PathParam("action") String action
	) {
		try {
			logger.debug("EducationResourceImpl.manageRoom:" + roomId + " action:" + action);
		}  catch (Exception e) {
			logger.error("EducationResourceImpl.manageRoom error:" + e.getMessage());
		}
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new EducationService(session,em).manageRoom(roomId,action, null);
		em.close();
		return resp;
	}

	@POST
	@Path("rooms/applyAction")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Manage a device. Valid actions are open, close, reboot, shutdown, wol, logout, cleanuploggedin."
			+ "This version of call allows to send a map with some parametrs:"
			+ "graceTime : seconds to wait befor execute action."
			+ "message : the message to shown befor/during execute the action.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	public List<CrxResponse> manageRooms(
		@ApiParam(hidden = true) @Auth Session session,
		CrxActionMap crxActionMap
	)  {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		RoomService roomService = new RoomService(session,em);
		List<CrxResponse> responses = new ArrayList<CrxResponse>();
		logger.debug("crxActionMap" + crxActionMap);
		if( crxActionMap.getName().equals("delete") ) {
			responses.add(new CrxResponse(session,"ERROR","You must not delete rooms"));

		} else {
			for( Long id: crxActionMap.getObjectIds() ) {
				responses.add(roomService.manageRoom(id,crxActionMap.getName(),null));
			}
		}
		em.close();
		return responses;
	}

	@POST
	@Path("rooms/upload")
	@Produces(JSON_UTF8)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation( value = "Puts data to te member of the smart rooms" )
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	public List<CrxResponse> uploadFileToRooms(
		@ApiParam(hidden = true) @Auth Session session,
		@FormDataParam("objectIds")    String  roomIds,
		@FormDataParam("cleanUp")      Boolean cleanUp,
		@FormDataParam("studentsOnly") Boolean studentsOnly,
		@FormDataParam("file")	 final InputStream fileInputStream,
		@FormDataParam("file")	 final FormDataContentDisposition contentDispositionHeader
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<CrxResponse> responses = new EducationService(session,em).uploadFileToRooms(
		       roomIds,
		       cleanUp,
		       studentsOnly,
		       fileInputStream,
		       contentDispositionHeader
		);
		em.close();
		return responses;
	}

	@POST
	@Path("rooms/collect")
	@Produces(JSON_UTF8)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation( value = "Puts data to te member of the smart rooms" )
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	public List<CrxResponse> collectFileFromRooms(
		@ApiParam(hidden = true) @Auth Session session,
		@FormDataParam("objectIds")     String  roomIds,
		@FormDataParam("projectName")   String projectName,
		@FormDataParam("studentsOnly")  Boolean studentsOnly,
		@FormDataParam("sortInDirs")    Boolean sortInDirs,
		@FormDataParam("cleanUpExport") Boolean cleanUpExport
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<CrxResponse> responses = new EducationService(session,em).collectFileFromRooms(
		       roomIds,
		       projectName,
		       sortInDirs,
		       cleanUpExport,
		       studentsOnly
		);
		em.close();
		return responses;
	}

	/******************************/
	/* Functions to handle groups */
	/******************************/

	@POST
	@Path("groups/add")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Create a new workgroup.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.groups")
	public CrxResponse  addGroup(
		@ApiParam(hidden = true) @Auth Session session,
		Group group
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new EducationService(session,em).createGroup(group);
		em.close();
		return resp;
	}

	@POST
	@Path("groups/{groupId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Modify a workgroup.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.groups")
	public CrxResponse  modifyGroup(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("groupId") Long groupId,
		Group group
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new EducationService(session,em).modifyGroup(groupId, group);
		em.close();
		return resp;
	}

	@GET
	@Path("groups/{groupId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets a workgroup.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.groups")
	public Group  getGroup(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("groupId") Long groupId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		Group resp = new GroupService(session,em).getById(groupId);
		em.close();
		return resp;
	}

	@DELETE
	@Path("groups/{groupId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delete a workgroup.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.groups")
	public CrxResponse  deleteGroup(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("groupId") Long groupId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new EducationService(session,em).deleteGroup(groupId);
		em.close();
		return resp;
	}

	@GET
	@Path("groups/all")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the workgroups and classes of a usrer.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	public List<Group>  getMyGroups(
		@ApiParam(hidden = true) @Auth Session session
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<Group> resp = new EducationService(session,em).getMyGroups();
		em.close();
		return resp;
	}

	@POST
	@Path("groups/applyAction")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Apply an action for the members of some groups once.",
			notes = "The following actions are available:<br>"
				+ "setPassword -> stringValue has to contain the password and Boolean if the users have to reset the password after first login.<br>"
				+ "setFilesystemQuota -> longValue has to contain the new quota value.<br>"
				+ "setMailSystemQuota -> longValue has to contain the new quota value.<br>"
				+ "disableLogin -> booleanValue has to contain the new value.<br>"
				+ "disableInternet -> booleanValue has to contain the new value.<br>"
				+ "copyTemplate -> Copy the home of the template user.<br>"
				+ "mandatoryProfile -> boolenValue has to contain the new value.<br>"
						+ "removeProfiles -> Clean up the profile directories.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.groups")
	public List<CrxResponse> groupsApplyAction(
		@ApiParam(hidden = true) @Auth Session session,
		CrxActionMap crxActionMap
	) {
		EntityManager       em = CrxEntityManagerFactory.instance().createEntityManager();
		List<CrxResponse> resp = new EducationService(session,em).groupsApplyAction(crxActionMap);
		em.close();
		return resp;
	}

	@GET
	@Path("groups/{groupId}/availableMembers")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get users which are not member in this group.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "Group not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("education.groups")
	public List<User> getAvailableMembers(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("groupId") long groupId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<User> resp = new EducationService(session,em).getAvailableMembers(groupId);
		em.close();
		return resp;
	}


	@GET
	@Path("groups/{groupId}/members")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get users which are member in this group.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "Group not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("education.groups")
	public List<User> getMembers(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("groupId") long groupId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<User> resp = new EducationService(session,em).getMembers(groupId);
		em.close();
		return resp;
	}

	@POST
	@Path("groups/{groupId}/members")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Sets the member of this group.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "Group not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("education.groups")
	public CrxResponse setMembers(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("groupId") Long groupId,
		List<Long> users
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new GroupService(session,em).setMembers(groupId,users);
		em.close();
		return resp;
	}

	@DELETE
	@Path("groups/{groupId}/{userId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Deletes a member of a group by userId.")
	@ApiResponses(value = {
	@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("education.groups")
	public CrxResponse deleteMember(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("groupId") long groupId,
		@PathParam("userId") long userId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new GroupService(session,em).removeMember(groupId, userId);
		em.close();
		return resp;
	}

	@PUT
	@Path("groups/{groupId}/{userId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Add a member to a group by userId.")
	@ApiResponses(value = {
	@ApiResponse(code = 404, message = "Group not found"),
	@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("education.groups")
	public CrxResponse addMember(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("groupId") long groupId,
		@PathParam("userId") long userId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new GroupService(session,em).addMember(groupId, userId);
		em.close();
		return resp;
	}

	@POST
	@Path("groups/upload")
	@Produces(JSON_UTF8)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation( value = "Puts data to te member of groups. groupIds contains a comma separated list of groups." )
	@ApiResponses(value = {
	   @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.groups")
	public List<CrxResponse> uploadFileToGroups(@ApiParam(hidden = true) @Auth Session session,
		@FormDataParam("objectIds")    String objectIds,
		@FormDataParam("cleanUp")      Boolean cleanUp,
		@FormDataParam("studentsOnly") Boolean studentsOnly,
		@FormDataParam("file")	 final InputStream fileInputStream,
		@FormDataParam("file")	 final FormDataContentDisposition contentDispositionHeader
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<CrxResponse> responses = new EducationService(session,em).uploadFileToGroups(
			objectIds,
			cleanUp,
			studentsOnly,
			fileInputStream,
			contentDispositionHeader);
		em.close();
		return responses;
	}

	@POST
	@Path("groups/collect")
	@Produces(JSON_UTF8)
	@ApiOperation( value = "Collects data from the students member of the corresponding group." )
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.groups")
	public List<CrxResponse> collectFileFromGroups(@ApiParam(hidden = true) @Auth Session session,
		@FormDataParam("objectIds")    String objectIds,
		@FormDataParam("projectName")  String projectName,
		@FormDataParam("sortInDirs")   Boolean sortInDirs,
		@FormDataParam("cleanUpExport")Boolean cleanUpExport,
		@FormDataParam("studentsOnly") Boolean studentsOnly
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<CrxResponse> resp = new EducationService(session,em).collectFileFromGroups(
				objectIds,
				projectName,
				sortInDirs,
				cleanUpExport,
				studentsOnly);
		em.close();
		return resp;
	}

	/*
	 * Deprecated should not be used.
	 */
	@GET
	@Path("users/all")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets a list of users by the userids." )
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	public List<Student> getUsers( @ApiParam(hidden = true) @Auth Session session) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<Student> resp = new ArrayList<Student>();
		for( User user : new UserService(session,em).getByRole(roleStudent) ) {
			for( Group group : user.getGroups() ) {
				if( !group.getGroupType().equals("primary") ) {
					Student student = new Student(user);
					student.setGroupName(group.getName());
					student.setGroupId(group.getId());
					resp.add(student);
				}
			}
		}
		em.close();
		return resp;
	}

	@GET
	@Path("users/{userId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delivers a user by id.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.users")
	public User getUserById(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("userId") Long userId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		User resp = new UserService(session,em).getById(userId);
		em.close();
		return resp;
	}

	@DELETE
	@Path("users/{userId}/{deviceId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Log out a user from a device. If device is -1 user will be logged out from all devices." )
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.users")
	public CrxResponse logOut(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("userId") Long userId,
		@PathParam("deviceId") Long deviceId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new DeviceService(session,em).removeLoggedInUser(deviceId, userId);
		em.close();
		return resp;
	}

	@PUT
	@Path("users/{userId}/{deviceId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Log in a user to a device." )
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.users")
	public CrxResponse logIn(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("userId") Long userId,
		@PathParam("deviceId") Long deviceId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new DeviceService(session,em).addLoggedInUser(deviceId, userId);
		em.close();
		return resp;
	}

	@GET
	@Path("users/{userId}/actions")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delivers a list of available actions for a user.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.users")
	public List<String> getAvailableUserActions(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("userId") Long userId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<String> resp = new EducationService(session,em).getAvailableUserActions(userId);
		em.close();
		return resp;
	}

	@POST
	@Path("users/upload")
	@Produces(JSON_UTF8)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation( value = "Puts data to selected users." )
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.users")
	public List<CrxResponse> uploadFileToUsers(@ApiParam(hidden = true) @Auth Session session,
		@FormDataParam("objectIds") final String userIds,
		@FormDataParam("cleanUp")   Boolean cleanUp,
		@FormDataParam("file")      final InputStream fileInputStream,
		@FormDataParam("file")      final FormDataContentDisposition contentDispositionHeader
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<CrxResponse> resp = new EducationService(session,em).uploadFileToUsers(userIds,cleanUp,fileInputStream,contentDispositionHeader);
		em.close();
		return resp;
	}

	@POST
	@Path("users/collect")
	@Produces(JSON_UTF8)
	@ApiOperation( value = "Collect file from selected users" )
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.users")
	public List<CrxResponse> collectFileFromUsers(
		@ApiParam(hidden = true) @Auth Session session,
		@FormDataParam("objectIds")     String userIds,
		@FormDataParam("projectName")   String projectName,
		@FormDataParam("sortInDirs")    Boolean sortInDirs,
		@FormDataParam("cleanUpExport") Boolean cleanUpExport
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<CrxResponse> responses = new EducationService(session,em).collectFileFromUsers(
			userIds,
			projectName,
			sortInDirs,
			cleanUpExport);
		em.close();
		return responses;
	}

	@POST
	@Path("users/applyAction")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Apply an action for a lot of user once.",
			notes = "The following actions are available:<br>"
				+ "setPassword -> stringValue has to contain the password and booleanValue if the users have to reset the password after first login.<br>"
				+ "setFilesystemQuota -> longValue has to contain the new quota value.<br>"
				+ "setMailSystemQuota -> longValue has to contain the new quota value.<br>"
				+ "disableLogin -> booleanValue has to contain the new value.<br>"
				+ "disableInternet -> booleanValue has to contain the new value.<br>"
				+ "copyTemplate -> Copy the home of the template user.<br>"
				+ "mandatoryProfile -> boolenValue has to contain the new value.<br>"
				+ "removeProfiles -> Clean up the profile directories.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.users")
	public List<CrxResponse> applyAction(
		@ApiParam(hidden = true) @Auth Session session,
		CrxActionMap crxActionMap
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<CrxResponse> resp = new EducationService(session,em).applyAction(crxActionMap);
		em.close();
		return resp;
	}


	/****************************/
	/* Actions on devices       */
	/****************************/
	@POST
	@Path("devices")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delivers a list of devices asked by the device ids.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	public List<Device> getDevicesById(
		@ApiParam(hidden = true) @Auth Session session,
		List<Long> deviceIds
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<Device> resp = new DeviceService(session,em).getDevices(deviceIds);
		em.close();
		return resp;
	}

	@POST
	@Path("devices/{deviceId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Updates a device. Only row and place can be changed here.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	public CrxResponse modifyDevice(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("deviceId") Long deviceId,
		Device device
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new EducationService(session,em).modifyDevice(deviceId, device);
		em.close();
		return resp;
	}

	/*
	 * Deprecated should not be used.
	 */
	@POST
	@Path("rooms/{roomId}/devices/{deviceId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Deprecated should not be used. Will be removed. Use POST education/devices/{deviceId} instead of<br>"
			      + "Updates a device. Only row and place can be changed here. The roomid is neccessary becouse of devices of smart rooms need to be handle on a other way.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	public CrxResponse modifyDeviceOfRoom(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("roomId")   Long roomId,
		@PathParam("deviceId") Long deviceId,
		Device device
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		Room room = new RoomService(session,em).getById(roomId);
		CrxResponse resp = null;
		if( (room.getCategories() != null) && (room.getCategories().size() > 0 ) && room.getCategories().get(0).getCategoryType().equals("smartRoom") ) {
			DeviceService deviceConrtoller = new DeviceService(session,em);
			Device oldDevice = deviceConrtoller.getById(deviceId);
			resp = deviceConrtoller.setConfig(oldDevice, "smartRoom-" + roomId + "-coordinates", String.format("%d,%d", device.getRow(),device.getPlace()));
		} else {
			resp = modifyDevice(session, deviceId, device);
		}
		em.close();
		return resp;
	}

	@GET
	@Path("devices/{deviceId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delivers a device by id.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	public Device getDeviceById(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("deviceId") Long deviceId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		Device resp = new DeviceService(session,em).getById(deviceId);
		em.close();
		return resp;
	}

	@GET
	@Path("devices/{deviceId}/actions")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delivers a list of available actions for a device.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	public List<String> getAvailableDeviceActions(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("deviceId") Long deviceId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<String> resp = new EducationService(session,em).getAvailableDeviceActions(deviceId);
		em.close();
		return resp;
	}

	/*
	 * Deprecated
	 */
	@PUT
	@Path("devices/{deviceId}/{action}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Deprecated. Will be removed. Use education/devices/applyAction instead of<br>"
			      + "Manage a device. Valid actions are open, close, reboot, shutdown, wol, logout, cleanuploggedin.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	public CrxResponse manageDevice(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("deviceId") Long deviceId,
		@PathParam("action") String action
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new DeviceService(session,em).manageDevice(deviceId,action,null);
		em.close();
		return resp;
	}

	@POST
	@Path("devices/applyAction")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Manage a device. Valid actions are open, close, reboot, shutdown, wol, logout, cleanuploggedin."
			+ "This version of call allows to send a map with some parametrs:"
			+ "graceTime : seconds to wait befor execute action."
			+ "message : the message to shown befor/during execute the action.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	public List<CrxResponse> manageDevices(
		@ApiParam(hidden = true) @Auth Session session,
		CrxActionMap crxActionMap
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<CrxResponse> resp = new EducationService(session,em).manageDevices(crxActionMap);
		em.close();
		return resp;
	}

	@POST
	@Path("devices/upload")
	@Produces(JSON_UTF8)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation( value = "Puts data to te member of the smart rooms" )
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	public List<CrxResponse> uploadFileToDevices(@ApiParam(hidden = true) @Auth Session session,
		@FormDataParam("objectIds")    String  deviceIds,
		@FormDataParam("cleanUp")      Boolean cleanUp,
		@FormDataParam("studentsOnly") Boolean studentsOnly,
		@FormDataParam("file") final   InputStream fileInputStream,
		@FormDataParam("file") final   FormDataContentDisposition contentDispositionHeader
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<CrxResponse> resp = new EducationService(session,em).uploadFileToDevices(
			deviceIds,
			cleanUp,
			studentsOnly,
			fileInputStream,
			contentDispositionHeader);
		em.close();
		return resp;
	}

	@POST
	@Path("devices/collect")
	@Produces(JSON_UTF8)
	@ApiOperation( value = "Collects data from the students member of the corresponding group." )
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	public List<CrxResponse> collectFileFromDevices(@ApiParam(hidden = true) @Auth Session session,
		@FormDataParam("objectIds")    String deviceIds,
		@FormDataParam("projectName")  String projectName,
		@FormDataParam("sortInDirs")   Boolean sortInDirs,
		@FormDataParam("cleanUpExport")Boolean cleanUpExport,
		@FormDataParam("studentsOnly") Boolean studentsOnly
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<CrxResponse> resp = new EducationService(session,em).collectFileFromDevices(
			deviceIds,
			projectName,
			sortInDirs,
			cleanUpExport,
			studentsOnly
		       );
		em.close();
		return resp;
	}

	@GET
	@Path("rooms/{roomId}/defaultPrinter")
	@Produces(JSON_UTF8)
	@ApiOperation( value = "Gets the default printer in the room." )
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	public Printer getDefaultPrinter(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("roomId") Long roomId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		Printer resp = new RoomService(session,em).getById(roomId).getDefaultPrinter();
		em.close();
		return resp;
	}

	@GET
	@Path("rooms/{roomId}/availablePrinters")
	@Produces(JSON_UTF8)
	@ApiOperation( value = "Gets the list fo the available printers in the room." )
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	public List<Printer> getAvailablePrinters(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("roomId") Long roomId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<Printer> resp = new RoomService(session,em).getById(roomId).getAvailablePrinters();
		em.close();
		return resp;
	}


	/*******************************************/
	/* Functions to handle proxy settings.     */
	/*******************************************/
	@GET
	@Path("proxy/positiveLists/all")
	@Produces(JSON_UTF8)
	@ApiOperation( value = "Gets all positive lists." )
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.proxy")
	public List<PositiveList> getPositiveLists( @ApiParam(hidden = true) @Auth Session session) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<PositiveList> resp = new ProxyService(session,em).getAllPositiveLists();
		em.close();
		return resp;
	}

	@GET
	@Path("proxy/myPositiveLists")
	@Produces(JSON_UTF8)
	@ApiOperation( value = "Gets owned positive lists." )
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.proxy")
	public List<PositiveList> getMyPositiveLists( @ApiParam(hidden = true) @Auth Session session) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<PositiveList> resp = session.getUser().getOwnedPositiveLists();
		em.close();
		return resp;
	}

	@POST
	@Path("proxy/positiveLists/add")
	@Produces(JSON_UTF8)
	@ApiOperation( value = "Creates a new positive list." )
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.proxy")
	public CrxResponse addPositiveList(
		@ApiParam(hidden = true) @Auth Session session,
		PositiveList positiveList
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new ProxyService(session,em).editPositiveList(positiveList);
		em.close();
		return resp;
	}

	@GET
	@Path("proxy/positiveLists/{positiveListId}")
	@Produces(JSON_UTF8)
	@ApiOperation( value = "Gets the content of a positive list." )
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.proxy")
	public PositiveList getPositiveListById(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("positiveListId") Long positiveListId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		PositiveList resp = new ProxyService(session,em).getPositiveList(positiveListId);
		em.close();
		return resp;
	}

	@DELETE
	@Path("proxy/positiveLists/{positiveListId}")
	@Produces(JSON_UTF8)
	@ApiOperation( value = "Deletes a positive list." )
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.proxy")
	public CrxResponse deletePositiveListById(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("positiveListId") Long positiveListId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new ProxyService(session,em).deletePositiveList(positiveListId);
		em.close();
		return resp;
	}

	//TODO these must be safer. Only the own controlled room may be modified
	@POST
	@Path("proxy/rooms/{roomId}")
	@Produces(JSON_UTF8)
	@ApiOperation( value = "Activates positive lists in a room." )
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.proxy")
	public CrxResponse activatePositiveListsInRoom(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("roomId") Long roomId,
		List<Long> positiveListIds
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new ProxyService(session,em).setAclsInRoom(roomId, positiveListIds);
		em.close();
		return resp;
	}

	@DELETE
	@Path("proxy/rooms/{roomId}")
	@Produces(JSON_UTF8)
	@ApiOperation( value = "Deactivates positive lists in a room." )
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.proxy")
	public CrxResponse deActivatePositiveListsInRoom(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("roomId") Long roomId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new ProxyService(session,em).deleteAclsInRoom(roomId);
		em.close();
		return resp;
	}

	@GET
	@Path("proxy/rooms/{roomId}")
	@Produces(JSON_UTF8)
	@ApiOperation( value = "Gets the active positive lists in a room." )
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.proxy")
	public List<PositiveList> getPositiveListsInRoom(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("roomId") Long roomId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<PositiveList> resp = new ProxyService(session,em).getPositiveListsInRoom(roomId);
		em.close();
		return resp;
	}

	/*
	 * Manage gast user
	 */
	@POST
	@Path("guestUsers/add")
	@Produces(JSON_UTF8)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation(value = "Creates a new guest users accounts.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("education.guestusers")
	public CrxResponse addGuestUsers(
		@ApiParam(hidden = true) @Auth Session session,
		@FormDataParam("name")	      String  name,
		@FormDataParam("description") String  description,
		@FormDataParam("roomId")	  Long    roomId,
		@FormDataParam("count")       Integer count,
		@FormDataParam("validUntil")  Date    validUntil
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		GuestUsers guestUsers = new GuestUsers(name,description,count,roomId,validUntil);
		CrxResponse resp = new UserService(session,em).addGuestUsers(guestUsers);
		em.close();
		return resp;
	}

	@POST
	@Path("guestUsers")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Creates a new guest users accounts.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("education.guestusers")
	public CrxResponse createGuestUsers(
		@ApiParam(hidden = true) @Auth Session session,
		GuestUsers guestUsers
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new UserService(session,em).addGuestUsers(guestUsers);
		em.close();
		return resp;
	}

	@GET
	@Path("guestUsers")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets all actual gast users. Systadmins get the lists all guest users. Normal users gets the own gast users.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "User not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("education.guestusers")
	public List<GuestUsers> getGuestUsers(
		@ApiParam(hidden = true) @Auth Session session
	) {
	       EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
	       List<GuestUsers> resp = new UserService(session,em).getGuestUsers();
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
	@RolesAllowed("education.guestusers")
	public Category getGuestUsersCategory(
		@ApiParam(hidden = true)      @Auth    Session session,
		@PathParam("guestUsersId")     Long    guestUsersId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		Category resp = new UserService(session,em).getGuestUsersCategory(guestUsersId);
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
	@RolesAllowed("education.guestusers")
	public CrxResponse  deleteGuestUsers(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("guestUsersId")     Long    guestUsersId
	) {
	       EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
	       CrxResponse resp = new UserService(session,em).deleteGuestUsers(guestUsersId);
	       em.close();
	       return resp;
	}

	@GET
	@Path("guestUsers/rooms")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the list of rooms can be reserved for guest users. At the moment this are all the rooms which can be controlled.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "User not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("education.guestusers")
	public List<Room> getGuestRooms( @ApiParam(hidden = true) @Auth Session session) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<Room> resp = new RoomService(session,em).getAllWithTeacherControl();
		em.close();
		return resp;
	}
}
