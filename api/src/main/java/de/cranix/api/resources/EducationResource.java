/* (c) Péter Varkoly <peter@varkoly.de> - all rights reserved  */
package de.cranix.api.resources;


import static de.cranix.api.resources.Resource.*;

import io.dropwizard.auth.Auth;
import io.swagger.annotations.*;
import javax.annotation.security.RolesAllowed;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import de.cranix.dao.CrxResponse;
import de.cranix.dao.Group;
import de.cranix.dao.GuestUsers;
import de.cranix.dao.CrxActionMap;
import de.cranix.dao.AccessInRoom;
import de.cranix.dao.Category;
import de.cranix.dao.PositiveList;
import de.cranix.dao.Printer;
import de.cranix.dao.Room;
import de.cranix.dao.Session;
import de.cranix.dao.SmartRoom;
import de.cranix.dao.User;
import de.cranix.dao.Device;

@Path("education")
@Api(value = "education")
public interface EducationResource {

	/******************************/
	/* Functions to handle rooms  */
	/******************************/

	/*
	 * POST education/rooms
	 */
	@POST
	@Path("rooms")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Create a new smart room. A smart Room is a category with CategoryType smart room. " +
	                      "The map can contains a description attribute. " +
	                      "The map must contains either users or groups or devices. ")
	@ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	CrxResponse  createSmartRoom(
	        @ApiParam(hidden = true) @Auth Session session,
	        Category smartRoom
	        );

	/*
	 * POST education/rooms/{roomId}
	 */
	@POST
	@Path("rooms/{roomId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Modfy a smart room. Only name and description can be modified here. To modify the member there are some PUT and DELETE calls.")
	@ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	CrxResponse  modifySmartRoom(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId") Long roomId,
	        Category smartRoom
	        );

	/*
	 *  PUT education/rooms/{roomId}/users/{userId}
	 */
	@PUT
	@Path("rooms/{roomId}/users/{userId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Add a user to a smart room." )
	@ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	CrxResponse addUserToSmartRoom(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId") Long roomId,
	        @PathParam("userId") Long userId
	        );

	/*
	 *  PUT education/rooms/{roomId}/devices/{deviceId}
	 */
	@PUT
	@Path("rooms/{roomId}/devices/{deviceId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Add a device to a smart room." )
	@ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	CrxResponse addDeviceToSmartRoom(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId")   Long roomId,
	        @PathParam("deviceId") Long deviceId
	        );

	/*
	 *  PUT education/rooms/{roomId}/groups/{groupId}
	 */
	@PUT
	@Path("rooms/{roomId}/groups/{groupId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Add a group to a smart room." )
	@ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	CrxResponse addGroupToSmartRoom(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId") Long roomId,
	        @PathParam("groupId") Long roupId
	        );


	/*
	 *  DELETE education/rooms/{roomId}/users/{userId}
	 */
	@DELETE
	@Path("rooms/{roomId}/users/{userId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delete a user from a smart room." )
	@ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	CrxResponse deleteUserFromSmartRoom(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId") Long roomId,
	        @PathParam("userId") Long userId
	        );

	/*
	 *  DELETE education/rooms/{roomId}/    devices/{deviceId}
	 */
	@DELETE
	@Path("rooms/{roomId}/devices/{deviceId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delete a device from a smart room." )
	@ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	CrxResponse deleteDeviceFromSmartRoom(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId")   Long roomId,
	        @PathParam("deviceId") Long deviceId
	        );

	/*
	 *  DELETE education/rooms/{roomId}/groups/{groupId}
	 */
	@DELETE
	@Path("rooms/{roomId}/groups/{groupId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delete a device from a smart room." )
	@ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	CrxResponse deleteGroupFromSmartRoom(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId")  Long roomId,
	        @PathParam("groupId") Long groupId
	        );

	/*
	 * DELETE education/rooms/{roomId}
	 */
	@DELETE
	@Path("rooms/{roomId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Deletes a new smart room.")
	@ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	CrxResponse  deleteSmartRoom(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId") Long roomId
	        );


	/*
	 *  GET education/rooms
	 */
	@GET
	@Path("rooms")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the list of the smart rooms the user has created.")
	@ApiResponses(value = {
	            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	List<Room> getMySmartRooms(
	        @ApiParam(hidden = true) @Auth Session session
	);

	/*
	 *  GET education/rooms
	 */
	@GET
	@Path("myRooms")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the of the rooms the session user may control.")
	@ApiResponses(value = {
	            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	List<Room> getMyRooms(
	        @ApiParam(hidden = true) @Auth Session session
	);

	/*
	 * GET education/rooms/{roomId}
	 */
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
	List<List<Long>>  getRoom(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId") Long roomId
	        );

	/*
	 * GET education/rooms/{roomId}
	 */
	@GET
	@Path("rooms/{roomId}/details")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the state of a room. This call delivers all Informations about a room."
	            )
	@ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	SmartRoom  getRoomDetails(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId") Long roomId
	        );

	/*
	 * GET education/rooms/{roomId}/users
	 * Delivers the list of the member users of a smart room
	 */
	@GET
	@Path("rooms/{roomId}/users")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delivers the user members in a smart room" )
	@ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	List<User>  getUserMember(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId") Long roomId
	        );

	/*
	 * GET education/rooms/{roomId}/users
	 * Delivers the list of the member users of a smart room
	 */
	@GET
	@Path("rooms/{roomId}/users/available")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delivers the user members in a smart room" )
	@ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	List<User>  getAvailableUserMember(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId") Long roomId
	        );

	/*
	 * GET education/rooms/{roomId}/groups
	 */
	@GET
	@Path("rooms/{roomId}/groups")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delivers the group members in a smart room" )
	@ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	List<Group>  getGroupMember(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId") Long roomId
	        );

	/*
	 * GET education/rooms/{roomId}/groups/available
	 */
	@GET
	@Path("rooms/{roomId}/groups/available")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delivers the group members in a smart room" )
	@ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	List<Group>  getAvailableGroupMember(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId") Long roomId
	        );

	/*
	 * GET education/rooms/{roomId}/devices
	 */
	@GET
	@Path("rooms/{roomId}/devices")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delivers the device members in a smart room" )
	@ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	List<Device>  getDeviceMember(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId") Long roomId
	        );

	/*
	 * GET education/rooms/{roomId}/devices/available
	 */
	@GET
	@Path("rooms/{roomId}/devices/available")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delivers the device members in a smart room" )
	@ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	List<Device>  getAvailableDeviceMember(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId") Long roomId
	        );

	/*
	 * GET education/rooms/{roomId}/control/minutes
	 */
	@GET
	@Path("rooms/{roomId}/control/{minutes}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get the control for a room for an amount of time."
	            )
	@ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	CrxResponse  getRoomControl(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId")  Long roomId,
	        @PathParam("minutes") Long minutes
	        );

	/*
	 * GET rooms/{roomId}/accessStatus
	 */
	@GET
	@Path("rooms/{roomId}/accessStatus")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the actual access in a room")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	AccessInRoom getAccessStatus(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId") long roomId
	);

	/*
	 * POST education/rooms/{roomId}/accessStatus
	 */
	@POST
	@Path("rooms/{roomId}/accessStatus")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Sets the actual access in a room")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	CrxResponse setAccessStatus(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId") long roomId,
	        AccessInRoom access
	);

	/*
	 * GET education/rooms/{roomId}/actions
	 */
	@GET
	@Path("rooms/{roomId}/actions")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delivers a list of available actions for a room.")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	List<String> getAvailableRoomActions(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId") Long roomId
	);

	/*
	 * PUT education/rooms/{roomId}/{action}
	 */
	@PUT
	@Path("rooms/{roomId}/{action}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Manage a room. Valid actions are download, open, close, reboot, shutdown, wol, logout, lockInput, unlockInput, .")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	CrxResponse manageRoom(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId") Long roomId,
	        @PathParam("action") String action
	);

	/*
	 * POST education/rooms/{roomId}/{action}
	 */
	@POST
	@Path("rooms/{roomId}/actionWithMap/{action}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Manage a room. Valid actions are open, close, reboot, shutdown, wol, logout, openProxy, closeProxy, setPassword",
				notes = "This version of call allows to send a map with some parametrs:<br>"
	                + "graceTime : seconds to wait befor execute action.<br>"
	                + "message : the message to shown befor/during execute the action.<br>"
	                + "password : the new password for the workstation users. This should be sent by action 'setPassword'")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	CrxResponse manageRoom(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId") Long roomId,
	        @PathParam("action") String action,
	        Map<String, String> actionContent
	);

	@POST
	@Path("rooms/{roomId}/upload")
	@Produces(JSON_UTF8)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation( value = "Puts data to te member of the smart rooms" )
	@ApiResponses(value = {
	            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	List<CrxResponse> uploadFileToRoom(@ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId") Long roomId,
	        @FormDataParam("cleanUp") Boolean cleanUp,
	        @FormDataParam("file")    final InputStream fileInputStream,
	        @FormDataParam("file")    final FormDataContentDisposition contentDispositionHeader
	);

	@POST
	@Path("rooms/{roomId}/collect")
	@Produces(JSON_UTF8)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation( value = "Puts data to te member of the smart rooms" )
	@ApiResponses(value = {
	            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	List<CrxResponse> collectFileFromRoom(@ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId") Long roomId,
	        @FormDataParam("projectName") String projectName,
	        @FormDataParam("sortInDirs")  boolean sortInDirs,
	        @FormDataParam("cleanUpExport") boolean cleanUpExport
	);


	/******************************/
	/* Functions to handle groups */
	/******************************/

	/*
	 * POST education/groups
	 */
	@POST
	@Path("groups")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Create a new workgroup.")
	@ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.groups")
	CrxResponse  addGroup(
	        @ApiParam(hidden = true) @Auth Session session,
	           Group group
	        );

	/*
	 * POST education/groups/{groupId}
	 */
	@POST
	@Path("groups/{groupId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Modify a workgroup.")
	@ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.groups")
	CrxResponse  modifyGroup(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("groupId") Long groupId,
	        Group group
	        );

	/*
	 * GET education/groups/{groupId}
	 */
	@GET
	@Path("groups/{groupId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets a workgroup.")
	@ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.groups")
	Group  getGroup(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("groupId") Long groupId
	        );

	/*
	 * GET education/groups
	 */
	@GET
	@Path("groups")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the workgroups and classes of a usrer.")
	@ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.groups")
	List<Group>  getMyGroups(
	        @ApiParam(hidden = true) @Auth Session session
	);

	/*
	 * GET education/groups
	 */
	@GET
	@Path("availableClasses")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the workgroups and classes of a usrer.")
	@ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.groups")
	List<Group>  getMyAvailableClasses(
	        @ApiParam(hidden = true) @Auth Session session
	);

	/*
	 * GET groups/<groupId>/availableMembers
	 */
	@GET
	@Path("groups/{groupId}/availableMembers")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get users which are not member in this group.")
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Group not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("education.groups")
	List<User> getAvailableMembers(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("groupId") long groupId
	);


	/*
	 * GET groups/<groupId>/members
	 */
	@GET
	@Path("groups/{groupId}/members")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get users which are member in this group.")
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Group not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("education.groups")
	List<User> getMembers(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("groupId") long groupId
	);

	/*
	 * DELETE groups/<groupId>/<userId>
	 */
	@DELETE
	@Path("groups/{groupId}/{userId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Deletes a member of a group by userId.")
	@ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("education.groups")
	CrxResponse deleteMember(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("groupId") long groupId,
	        @PathParam("userId") long userId
	);

	/*
	 * PUT groups/<groupId>/<userId>
	 */
	@PUT
	@Path("groups/{groupId}/{userId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Add a member to a group by userId.")
	@ApiResponses(value = {
	    @ApiResponse(code = 404, message = "Group not found"),
	    @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("education.groups")
	CrxResponse addMember(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("groupId") long groupId,
	        @PathParam("userId") long userId
	);
	/*
	 * DELETE education/groups/{groupId}
	 */
	@DELETE
	@Path("groups/{groupId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delete a workgroup.")
	@ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.groups")
	CrxResponse  deleteGroup(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("groupId") Long groupId
	       );


	/*
	 * GET education/groups/{groupId}/actions
	 */
	@GET
	@Path("groups/{groupId}/actions")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delivers a list of available actions for a group.")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.groups")
	List<String> getAvailableGroupActions(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("groupId") Long groupId
	);

	/*
	 * * PUT education/groups/{groupId}/actions/{actionNam}
	*/
	@PUT
	@Path("groups/{groupId}/actions/{action}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Apply an actions for a group.")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.groups")
	CrxResponse manageGroup(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("groupId") Long groupId,
	        @PathParam("action")  String action
	);

	/*
	 * POST education/rooms/{roomId}/{action}
	 */
	@POST
	@Path("groups/{groupId}/actionWithMap/{action}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Manage a device. Valid actions are open, close, reboot, shutdown, wol, logout, openProxy, closeProxy."
	                + "This version of call allows to send a map with some parametrs:"
	                + "graceTime : seconds to wait befor execute action."
	                + "message : the message to shown befor/during execute the action.")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	CrxResponse manageGroup(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("groupId") Long groupId,
	        @PathParam("action") String action,
	        Map<String, String> actionContent
	);

	@POST
	@Path("groups/{groupId}/upload")
	@Produces(JSON_UTF8)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation( value = "Puts data to te member of a group" )
	@ApiResponses(value = {
	            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	List<CrxResponse> uploadFileToGroup(@ApiParam(hidden = true) @Auth Session session,
	        @PathParam("groupId")  Long  groupId,
	        @FormDataParam("cleanUp") Boolean cleanUp,
	        @FormDataParam("studentsOnly") Boolean studentsOnly,
	        @FormDataParam("file") final InputStream fileInputStream,
	        @FormDataParam("file") final FormDataContentDisposition contentDispositionHeader
	        );

	@POST
	@Path("groups/upload")
	@Produces(JSON_UTF8)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation( value = "Puts data to te member of groups. groupIds contains a comma separated list of groups." )
	@ApiResponses(value = {
	       @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	List<CrxResponse> uploadFileToGroups(@ApiParam(hidden = true) @Auth Session session,
	        @FormDataParam("groupIds")     String groupIds,
	        @FormDataParam("cleanUp") Boolean cleanUp,
	        @FormDataParam("studentsOnly") Boolean studentsOnly,
	        @FormDataParam("file") final InputStream fileInputStream,
	        @FormDataParam("file") final FormDataContentDisposition contentDispositionHeader
	        );


	@POST
	@Path("groups/{groupId}/collect")
	@Produces(JSON_UTF8)
	@ApiOperation( value = "Collects data from member of the corresponding group.")
	@ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.groups")
	List<CrxResponse> collectFileFromGroup(@ApiParam(hidden = true) @Auth Session session,
	        @PathParam("groupId") Long roomId,
	        @FormDataParam("projectName")  String projectName,
	        @FormDataParam("sortInDirs")   boolean sortInDirs,
	        @FormDataParam("cleanUpExport")boolean cleanUpExport,
	        @FormDataParam("studentsOnly") boolean studentsOnly
        );

	@POST
	@Path("groups/collect")
	@Produces(JSON_UTF8)
	@ApiOperation( value = "Collects data from the students member of the corresponding group." )
	@ApiResponses(value = {
	            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.groups")
	List<CrxResponse> collectFileFromGroups(@ApiParam(hidden = true) @Auth Session session,
		@FormDataParam("groupIds")     String groupIds,
		@FormDataParam("projectName")  String projectName,
		@FormDataParam("sortInDirs")   boolean sortInDirs,
		@FormDataParam("cleanUpExport")boolean cleanUpExport,
		@FormDataParam("studentsOnly") boolean studentsOnly
	);
	/************************************************************/
	/* Actions on logged in users and smart rooms and groups. */
	/************************************************************/

	@POST
	@Path("users")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets a list of users by the userids." )
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.users")
	List<User> getUsersById(
	                @ApiParam(hidden = true) @Auth Session session,
	                List<Long> userIds
	                );

	/*
	 * GET education/users/{userId}
	 */
	@GET
	@Path("users/{userId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delivers a user by id.")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.users")
	User getUserById(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("userId") Long userId
	);

	/*
	 * DELETE education/users/{userId}/{deviceId}
	 */
	@DELETE
	@Path("users/{userId}/{deviceId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Log out a user from a device. If device is -1 user will be logged out from all devices." )
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.users")
	CrxResponse logOut(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("userId") Long userId,
	        @PathParam("deviceId") Long deviceId
	        );

	/*
	 * PUT education/users/{userId}/{deviceId}
	 */
	@PUT
	@Path("users/{userId}/{deviceId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Log in a user to a device." )
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.users")
	CrxResponse logIn(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("userId") Long userId,
	        @PathParam("deviceId") Long roomId
	        );

	/*
	 * GET education/users/{userId}/actions
	 */
	@GET
	@Path("users/{userId}/actions")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delivers a list of available actions for a user.")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.users")
	List<String> getAvailableUserActions(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("userId") Long userId
	);

	@POST
	@Path("users/{userId}/upload")
	@Produces(JSON_UTF8)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation( value = "Puts data to the member of the smart rooms" )
	@ApiResponses(value = {
	            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.users")
	CrxResponse uploadFileToUser(@ApiParam(hidden = true) @Auth Session session,
	        @PathParam("userId")   Long  userId,
	        @FormDataParam("cleanUp") Boolean cleanUp,
	        @FormDataParam("file") final InputStream fileInputStream,
	        @FormDataParam("file") final FormDataContentDisposition contentDispositionHeader
	        );

	@POST
	@Path("users/upload")
	@Produces(JSON_UTF8)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation( value = "Puts data to selected users." )
	@ApiResponses(value = {
	            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.users")
	List<CrxResponse> uploadFileToUsers(@ApiParam(hidden = true) @Auth Session session,
			@FormDataParam("userIds") final String userIds,
			@FormDataParam("cleanUp") Boolean cleanUp,
	        @FormDataParam("file") final InputStream fileInputStream,
	        @FormDataParam("file") final FormDataContentDisposition contentDispositionHeader
	        );

	@POST
	@Path("users/collect")
	@Produces(JSON_UTF8)
	@ApiOperation( value = "Collect file from selected users" )
	@ApiResponses(value = {
	            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.users")
	List<CrxResponse> collectFileFromUsers(@ApiParam(hidden = true) @Auth Session session,
		@FormDataParam("projectName") String projectName,
		@FormDataParam("sortInDirs")  boolean sortInDirs,
		@FormDataParam("cleanUpExport") boolean cleanUpExport,
		@FormDataParam("userIds") final String userIds
	        );

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
	List<CrxResponse> applyAction(@ApiParam(hidden = true) @Auth Session session,
	                CrxActionMap ossActionMap
	                );


	/****************************/
	/* Actions on devices       */
	/****************************/
	 /*
	 * POST education/devices
	 */
	@POST
	@Path("devices")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delivers a list of devices asked by the device ids.")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	List<Device> getDevicesById(
	        @ApiParam(hidden = true) @Auth Session session,
	        List<Long> deviceIds
	);

	/*
	 * POST education/devices/{deviceId}
	 */
	@POST
	@Path("devices/{deviceId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Updates a device. Only row and place can be changed here.")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	CrxResponse modifyDevice(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("deviceId") Long deviceId,
	        Device device
	);


	/*
	 * POST education/devices/{deviceId}
	 */
	@POST
	@Path("rooms/{roomId}/devices/{deviceId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Updates a device. Only row and place can be changed here. The roomid is neccessary becouse of devices of smart rooms need to be handle on a other way.")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	CrxResponse modifyDeviceOfRoom(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId")   Long roomId,
	        @PathParam("deviceId") Long deviceId,
	        Device device
	);

	@GET
	@Path("devices/{deviceId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delivers a device by id.")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	Device getDeviceById(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("deviceId") Long deviceId
	);

	@GET
	@Path("devices/{deviceId}/actions")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delivers a list of available actions for a device.")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	List<String> getAvailableDeviceActions(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("deviceId") Long deviceId
	);

	/*
	 * PUT education/devices/{deviceId}/{action}
	 */
	@PUT
	@Path("devices/{deviceId}/{action}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Manage a device. Valid actions are open, close, reboot, shutdown, wol, logout, cleanuploggedin.")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	CrxResponse manageDevice(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("deviceId") Long deviceId,
	        @PathParam("action") String action
	);

	/*
	 * POST education/rooms/{roomId}/{action}
	 */
	@POST
	@Path("devices/{deviceId}/actionWithMap/{action}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Manage a device. Valid actions are open, close, reboot, shutdown, wol, logout, cleanuploggedin."
	                + "This version of call allows to send a map with some parametrs:"
	                + "graceTime : seconds to wait befor execute action."
	                + "message : the message to shown befor/during execute the action.")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	CrxResponse manageDevice(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("deviceId") Long roomId,
	        @PathParam("action") String action,
	        Map<String, String> actionContent
	);


	@POST
	@Path("devices/{deviceId}/upload")
	@Produces(JSON_UTF8)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation( value = "Puts data to te member of the smart rooms" )
	@ApiResponses(value = {
	            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	CrxResponse uploadFileToDevice(@ApiParam(hidden = true) @Auth Session session,
	        @PathParam("deviceId") Long deviceId,
	        @FormDataParam("cleanUp") Boolean cleanUp,
	        @FormDataParam("file") final InputStream fileInputStream,
	        @FormDataParam("file") final FormDataContentDisposition contentDispositionHeader
	);

	@GET
	@Path("devices/{deviceId}/collect/{projectName}")
	@Produces(JSON_UTF8)
	@ApiOperation( value = "Collects data from a user logged on the corresponding device." )
	@ApiResponses(value = {
	            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	CrxResponse collectFileFromDevice(
	    @ApiParam(hidden = true) @Auth Session session,
	    @PathParam("deviceId") Long deviceId,
	    @PathParam("projectName") String projectName
	);

	/*
	 * Get informations from the printers in the room
	 */
	@GET
	@Path("rooms/{roomId}/defaultPrinter")
	@Produces(JSON_UTF8)
	@ApiOperation( value = "Gets the default printer in the room." )
	@ApiResponses(value = {
	            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	Printer getDefaultPrinter(
	    @ApiParam(hidden = true) @Auth Session session,
	    @PathParam("roomId") Long roomId
	);

	/*
	 * Get informations from the printers in the room
	 */
	@GET
	@Path("rooms/{roomId}/availablePrinters")
	@Produces(JSON_UTF8)
	@ApiOperation( value = "Gets the list fo the available printers in the room." )
	@ApiResponses(value = {
	            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.rooms")
	List<Printer> getAvailablePrinters(
	    @ApiParam(hidden = true) @Auth Session session,
	    @PathParam("roomId") Long roomId
	);


	/*******************************************/
	/* Functions to handle proxy settings.     */
	/*******************************************/
	@GET
	@Path("proxy/positiveLists")
	@Produces(JSON_UTF8)
	@ApiOperation( value = "Gets all positive lists." )
	@ApiResponses(value = {
	            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.proxy")
	List<PositiveList> getPositiveLists(
	    @ApiParam(hidden = true) @Auth Session session
	);

	@GET
	@Path("proxy/myPositiveLists")
	@Produces(JSON_UTF8)
	@ApiOperation( value = "Gets owned positive lists." )
	@ApiResponses(value = {
	            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.proxy")
	List<PositiveList> getMyPositiveLists(
	    @ApiParam(hidden = true) @Auth Session session
	);

	@POST
	@Path("proxy/positiveLists")
	@Produces(JSON_UTF8)
	@ApiOperation( value = "Creates a new positive list." )
	@ApiResponses(value = {
	            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.proxy")
	CrxResponse addPositiveList(
	    @ApiParam(hidden = true) @Auth Session session,
	    PositiveList positiveList
	);

	@GET
	@Path("proxy/positiveLists/{positiveListId}")
	@Produces(JSON_UTF8)
	@ApiOperation( value = "Gets the content of a positive list." )
	@ApiResponses(value = {
	            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.proxy")
	PositiveList getPositiveListById(
	    @ApiParam(hidden = true) @Auth Session session,
	    @PathParam("positiveListId") Long positiveListId
	);

	@DELETE
	@Path("proxy/positiveLists/{positiveListId}")
	@Produces(JSON_UTF8)
	@ApiOperation( value = "Deletes a positive list." )
	@ApiResponses(value = {
	            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.proxy")
	CrxResponse deletePositiveListById(
	    @ApiParam(hidden = true) @Auth Session session,
	    @PathParam("positiveListId") Long positiveListId
	);

	//TODO these must be safer. Only the own controlled room may be modified
	@POST
	@Path("proxy/rooms/{roomId}")
	@Produces(JSON_UTF8)
	@ApiOperation( value = "Activates positive lists in a room." )
	@ApiResponses(value = {
	            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.proxy")
	CrxResponse activatePositiveListsInRoom(
	    @ApiParam(hidden = true) @Auth Session session,
	    @PathParam("roomId") Long roomId,
	    List<Long> postiveListIds
	);

	@GET
	@Path("proxy/rooms/{roomId}")
	@Produces(JSON_UTF8)
	@ApiOperation( value = "Activates positive lists in a room." )
	@ApiResponses(value = {
	            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.proxy")
	List<PositiveList> getPositiveListsInRoom(
	    @ApiParam(hidden = true) @Auth Session session,
	    @PathParam("roomId") Long roomId
	);

	@DELETE
	@Path("proxy/rooms/{roomId}")
	@Produces(JSON_UTF8)
	@ApiOperation( value = "Deactivates positive lists in a room." )
	@ApiResponses(value = {
	            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("education.proxy")
	CrxResponse deActivatePositiveListsInRoom(
	    @ApiParam(hidden = true) @Auth Session session,
	    @PathParam("roomId") Long roomId
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
	 @RolesAllowed("education.guestusers")
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
	 @RolesAllowed("education.guestusers")
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
	 @RolesAllowed("education.guestusers")
	 CrxResponse  deleteGuestUsers(
	             @ApiParam(hidden = true) @Auth Session session,
	             @PathParam("guestUsersId")     Long    guestUsersId
	 );

	@POST
	@Path("guestUsers/add")
	@Produces(JSON_UTF8)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation(value = "Creates a new guest users accounts.")
	@ApiResponses(value = {
	                @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("education.guestusers")
	CrxResponse addGuestUsers(
	                @ApiParam(hidden = true) @Auth Session session,
	                @FormDataParam("name")          String  name,
	                @FormDataParam("description")   String  description,
	                @FormDataParam("roomId")        Long    roomId,
	                @FormDataParam("count")         Long    count,
	                @FormDataParam("validUntil")    Date    validUntil
	                );

	@POST
	@Path("guestUsers/create")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Creates a new guest users accounts.")
	@ApiResponses(value = {
	                @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("education.guestusers")
	CrxResponse createGuestUsers(
	                @ApiParam(hidden = true) @Auth Session session,
	                GuestUsers guestUsers
	                );

	@GET
	@Path("guestUsers/rooms")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the list of rooms can be reserved for guest users. At the moment this are all the rooms which can be controlled.")
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "User not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("education.guestusers")
	List<Room> getGuestRooms(
	            @ApiParam(hidden = true) @Auth Session session
	);

}
