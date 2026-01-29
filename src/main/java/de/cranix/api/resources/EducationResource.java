/* (c) 2020 Péter Varkoly <pvarkoly@cephalix.eu> - all rights reserved  */
package de.cranix.api.resources;


import static de.cranix.api.resources.Resource.*;

import io.dropwizard.auth.Auth;
import io.swagger.annotations.*;
import javax.annotation.security.RolesAllowed;
import javax.annotation.security.PermitAll;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
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
@Produces(JSON_UTF8)
@Api(value = "education")
public class EducationResource {

	Logger logger = LoggerFactory.getLogger(EducationResource.class);

	public EducationResource() { }

	/******************************/
	/* Functions to handle rooms  */
	/******************************/

	@GET
	@Path("myRooms")
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
	@ApiOperation(
		value = "Gets the state of a room.",
		notes = "This call delivers a list of list with the logged in users. " +
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
	@ApiOperation(value = "Gets the actual access in a room")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	public Object getAccessStatus(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("roomId") long roomId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		Object resp = new RoomService(session,em).getAccessStatus(roomId);
		em.close();
		return resp;
	}

	@POST
	@Path("rooms/{roomId}/accessStatus")
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
			responses.add(new CrxResponse("ERROR","You must not delete rooms"));

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
	@ApiOperation(value = "Create a new workgroup.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.group")
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
	@ApiOperation(value = "Modify a workgroup.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.group")
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
	@ApiOperation(value = "Gets a workgroup.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.group")
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
	@ApiOperation(value = "Delete a workgroup.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.group")
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
	@ApiOperation(value = "Gets the workgroups that are owned by the session user and classes in which he is member of.")
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
	@ApiOperation(
		value = "Apply an action for the members of some groups once.",
		notes = "The following actions are available:<br>"
			+ "setPassword -> stringValue has to contain the password and Boolean if the users have to reset the password after first login.<br>"
			+ "setFilesystemQuota -> longValue has to contain the new quota value.<br>"
			+ "setMailSystemQuota -> longValue has to contain the new quota value.<br>"
			+ "disableLogin -> booleanValue has to contain the new value.<br>"
			+ "disableInternet -> booleanValue has to contain the new value.<br>"
			+ "copyTemplate -> Copy the home of the template user.<br>"
			+ "mandatoryProfile -> boolenValue has to contain the new value.<br>"
			+ "removeProfiles -> Clean up the profile directories."
	)
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.group")
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
	@ApiOperation(value = "Get users which are not member in this group.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "Group not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("education.group")
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
	@ApiOperation(value = "Get users which are member in this group.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "Group not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("education.group")
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
	@ApiOperation(value = "Sets the member of this group.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "Group not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("education.group")
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
	@ApiOperation(value = "Deletes a member of a group by userId.")
	@ApiResponses(value = {
	@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("education.group")
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
	@ApiOperation(value = "Add a member to a group by userId.")
	@ApiResponses(value = {
	@ApiResponse(code = 404, message = "Group not found"),
	@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("education.group")
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
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation( value = "Puts data to te member of groups. groupIds contains a comma separated list of groups." )
	@ApiResponses(value = {
	   @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.group")
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
	@ApiOperation( value = "Collects data from the students member of the corresponding group." )
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.group")
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

	@GET
	@Path("users/all")
	@ApiOperation(value = "Gets a list of users by the userids." )
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	public List<Student> getStudents( @ApiParam(hidden = true) @Auth Session session) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<Student> resp = new EducationService(session,em).getStudents();
		em.close();
		return resp;
	}

	@GET
	@Path("users/{userId}")
	@ApiOperation(value = "Delivers a user by id.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.user")
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
	@ApiOperation(value = "Log out a user from a device. If device is -1 user will be logged out from all devices." )
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.user")
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
	@ApiOperation(value = "Log in a user to a device." )
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.user")
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
	@ApiOperation(value = "Delivers a list of available actions for a user.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.user")
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
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation( value = "Puts data to selected users." )
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.user")
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
	@ApiOperation( value = "Collect file from selected users" )
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.user")
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
	@ApiOperation(
		value = "Apply an action for a lot of user once.",
		notes = "The following actions are available:<br>"
			+ "setPassword -> stringValue has to contain the password and booleanValue if the users have to reset the password after first login.<br>"
			+ "setFilesystemQuota -> longValue has to contain the new quota value.<br>"
			+ "setMailSystemQuota -> longValue has to contain the new quota value.<br>"
			+ "disableLogin -> booleanValue has to contain the new value.<br>"
			+ "disableInternet -> booleanValue has to contain the new value.<br>"
			+ "copyTemplate -> Copy the home of the template user.<br>"
			+ "mandatoryProfile -> boolenValue has to contain the new value.<br>"
			+ "removeProfiles -> Clean up the profile directories."
	)
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.user")
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
	@Path("devices/{deviceId}")
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

	@GET
	@Path("devices/{deviceId}/actions")
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

	@POST
	@Path("devices/applyAction")
	@ApiOperation(
		value = "Manage a device.",
		notes = "Valid actions are open, close, reboot, shutdown, wol, logout, cleanuploggedin."
			+ "This version of call allows to send a map with some parametrs:"
			+ "graceTime : seconds to wait befor execute action."
			+ "message : the message to shown befor/during execute the action."
	)
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
	 * Manage guest user
	 */
	@POST
	@Path("guestUsers")
	@ApiOperation(value = "Creates a new guest users accounts.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("education.guestUser")
	public CrxResponse createGuestUsers(
		@ApiParam(hidden = true) @Auth Session session,
		GuestUsers guestUsers
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new GuestUserService(session,em).add(guestUsers);
		em.close();
		return resp;
	}

	@GET
	@Path("guestUsers/all")
	@ApiOperation(value = "Gets all actual gast users. Sysadmins get the lists all guest users. Normal users gets the owned gast users.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "User not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("education.guestUser")
	public List<GuestUsers> getGuestUsers(
		@ApiParam(hidden = true) @Auth Session session
	) {
	       EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
	       List<GuestUsers> resp = new GuestUserService(session,em).getAll();
	       em.close();
	       return resp;
	}

	@GET
	@Path("guestUsers/{guestUsersId}")
	@ApiOperation(value = "Gets a guest users category.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "User not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("education.guestUser")
	public GuestUsers getGuestUsersCategory(
		@ApiParam(hidden = true)      @Auth    Session session,
		@PathParam("guestUsersId")     Long    guestUsersId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		GuestUsers resp = new GuestUserService(session,em).getById(guestUsersId);
		em.close();
		return resp;
	}

	@DELETE
	@Path("guestUsers/{guestUsersId}")
	@ApiOperation(value = "Delete a guest users category.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "User not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("education.guestUser")
	public CrxResponse  deleteGuestUsers(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("guestUsersId")     Long    guestUsersId
	) {
	       EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
	       CrxResponse resp = new GuestUserService(session,em).delete(guestUsersId);
	       em.close();
	       return resp;
	}

	@GET
	@Path("guestUsers/rooms")
	@ApiOperation(value = "Gets the list of rooms can be reserved for guest users. At the moment this are all the rooms which can be controlled.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "User not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("education.guestUser")
	public List<Room> getGuestRooms( @ApiParam(hidden = true) @Auth Session session) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<Room> resp = new RoomService(session,em).getAllWithTeacherControl();
		em.close();
		return resp;
	}
}
