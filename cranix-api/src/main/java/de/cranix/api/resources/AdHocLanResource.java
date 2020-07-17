/* (c) 2020 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.api.resources;

import static de.cranix.api.resources.Resource.JSON_UTF8;


import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.POST;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import de.cranix.dao.Device;
import de.cranix.dao.Group;
import de.cranix.dao.CrxResponse;
import de.cranix.dao.AdHocRoom;
import de.cranix.dao.Session;
import de.cranix.dao.User;

import java.util.List;

@Path("adhocrooms")
@Api(value = "adhocrooms")
public interface AdHocLanResource {

	/**
	 * getRooms Delivers a list of all AdHocRooms
	 * @param session
	 * @return
	 */
	@GET
	@Path("all")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets all defined AdHocLan Rooms which a user may use. Superuser get the list of all AdHocLan rooms.")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "No room was found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@PermitAll
	List<AdHocRoom> getRooms(
			@ApiParam(hidden = true) @Auth Session session
			);

	/*
	 * POST addhoclan/add { hash }
	 */
	@POST
	@Path("add")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Create new AddHocLan room")
	@ApiResponses(value = {
			// TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("adhoclan.manage")
	CrxResponse add(
			@ApiParam(hidden = true) @Auth Session session,
			AdHocRoom room
	);

	/**
	 * Delets an adhoc room inkl all devices.
	 * @param session
	 * @param id The id of the room to be deleted.
	 * @return
	 */
	@DELETE
	@Path("{id}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delets a whole adhoc room inkl devices.")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "No room was found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("adhoclan.manage")
	CrxResponse delete(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("id")		Long id
			);

	/*
	 * Get adhoclan/{id}/{objectType}
	 */
	@GET
	@Path("{id}/users")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets all defined groups or users or devices in a giwen AdHocLan room. Object types can be Group or User")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "No category was found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("adhoclan.manage")
	List<User> getUsersOfRoom(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("id")	 Long id
			);

	@GET
	@Path("{id}/groups")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets all defined groups or users or devices in a giwen AdHocLan room. Object types can be Group or User")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "No category was found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("adhoclan.manage")
	List<Group> getGroupsOfRoom(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("id")	 Long id
			);

	/*
	 * GET categories/<id>/available/<memeberType>
	 */
	@GET
	@Path("{id}/available/users")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get the non member users of an AdHocLan room.")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "Category not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("adhoclan.manage")
	List<User> getAvailableUser(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("id") long id
			);

	@GET
	@Path("{id}/available/groups")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get the non member groups of an AdHocLan room.")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "Category not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("adhoclan.manage")
	List<Group> getAvailableGroups(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("id") long id
			);

	/*
	 * Get adhoclan/users
	 */
	@GET
	@Path("users")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets all users which may use AdHocLan Devices.")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "No category was found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("adhoclan.manage")
	List<User> getUsers(
			@ApiParam(hidden = true) @Auth Session session
			);

	/*
	 * Get adhoclan/groups
	 */
	@GET
	@Path("groups")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets all Groups which have AdHocLan access.")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "No category was found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("adhoclan.manage")
	List<Group> getGroups(
			@ApiParam(hidden = true) @Auth Session session
			);

	/*
	 * PUT addhoclan/rooms/{roomId}
	 */
	@PUT
	@Path("rooms/{roomId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Define a room as AdHocLan room")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "No category was found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("adhoclan.manage")
	CrxResponse turnOn(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("roomId")		Long roomId
			);

	/*
	 * POST addhoclan/{id}
	 */
	@GET
	@Path("{id}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets an AdHocLan room.")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "No category was found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("adhoclan.manage")
	AdHocRoom getRoomById(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("id")		Long id
			);

	/*
	 * POST addhoclan/{id}
	 */
	@POST
	@Path("{id}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Modify an AdHocLan room")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "No category was found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("adhoclan.manage")
	CrxResponse modify(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("id")		Long id,
			AdHocRoom room
			);

	/*
	 * PUT addhoclan/{id}/{objectType}/{objectId}
	 */
	@PUT
	@Path("{id}/{objectType}/{objectId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Add a new group or user to a giwen AdHocLan room")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "No category was found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("adhoclan.manage")
	CrxResponse putObjectIntoRoom(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("id")		Long id,
			@PathParam("objectType")	String onjectType,
			@PathParam("objectId")		Long objectId
			);

	/*
	 * PUT addhoclan/{id}/{objectType}/{objectId}
	 */
	@DELETE
	@Path("{id}/{objectType}/{objectId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Removes a group or user from an AdHocLan room")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "No category was found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("adhoclan.manage")
	CrxResponse deleteObjectInRoom(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("id")		Long id,
			@PathParam("objectType")	String onjectType,
			@PathParam("objectId")		Long objectId
			);
	/**
	 * @param session
	 * @param id
	 * @return
	 */
	@GET
	@Path("{id}/devices")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets all devices in an add hoc room.")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "No room was found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("adhoclan.manage")
	List<Device> getDevicesOfRoom(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("id")		Long id
			);

	/*
	 * Functions for all user
	 */
	/**
	 * getRooms Delivers a list of all AdHocRooms
	 * @param session
	 * @return
	 */
	@GET
	@Path("myRooms")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets all defined AdHocLan Rooms which a user may use. Superuser get the list of all AdHocLan rooms.")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "No room was found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@PermitAll
	List<AdHocRoom> getMyRooms(
			@ApiParam(hidden = true) @Auth Session session
			);

	/**
	 * getDevices Delivers a list of the owned devices
	 * @param session
	 * @return
	 */
	@GET
	@Path("devices")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets all owned AdHocLan Devices of a user.")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "No category was found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@PermitAll
	List<Device> getDevices(
			@ApiParam(hidden = true) @Auth Session session
			);

	/*
	 * Get adhoclan/devices/{deviceId}
	 */
	@DELETE
	@Path("devices/{deviceId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delets an owned AdHocLan Devices of a user.")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "No category was found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@PermitAll
	CrxResponse deleteDevice(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("deviceId")		 Long	deviceId
			);

	@POST
	@Path("devices/{deviceId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Modify an owned AdHocLan Devices of a user.")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "No category was found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@PermitAll
	CrxResponse modifyDevice(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("deviceId")		 Long	deviceId,
			Device device
			);

	@PUT
	@Path("{id}/device/{MAC}/{name}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Create a new device. This api call can be used only for registering own devices.")
	@ApiResponses(value = {
			// TODO so oder anders? @ApiResponse(code = 404, message = "At least one device was not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	CrxResponse addDevice(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("id")		long id,
			@PathParam("MAC")			String macAddress,
			@PathParam("name")			String name
	);
}
