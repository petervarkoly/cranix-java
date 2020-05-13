/* (c) 2017 Peter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.api.resources;


import static de.cranix.api.resources.Resource.*;

import io.dropwizard.auth.Auth;
import io.swagger.annotations.*;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import de.cranix.dao.Room;
import de.cranix.dao.AccessInRoom;
import de.cranix.dao.Device;
import de.cranix.dao.HWConf;
import de.cranix.dao.CrxMConfig;
import de.cranix.dao.CrxActionMap;
import de.cranix.dao.CrxResponse;
import de.cranix.dao.Printer;
import de.cranix.dao.Session;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Path("rooms")
@Api(value = "rooms")
public interface RoomResource {

	/*
	 * GET rooms/<roomId>
	 */
	@GET
	@Path("{roomId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get a room by id")
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Room not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("room.search")
	Room getById(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId") Long roomId
	);

	/**
	 * GET rooms/all
	 */
	@GET
	@Path("all")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get all rooms")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.search")
	List<Room> getAll(
	        @ApiParam(hidden = true) @Auth Session session
	);

	/**
	 * GET rooms/all
	 */
	@GET
	@Path("allNames")
	@Produces(TEXT)
	@ApiOperation(value = "Get all rooms")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.search")
	String getAllNames(
	        @ApiParam(hidden = true) @Auth Session session
	);

	/**
	 * GET rooms/allWithControl
	 */
	@GET
	@Path("allWithControl")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get all rooms which can be controlled")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.search")
	List<Room> allWithControl(
	        @ApiParam(hidden = true) @Auth Session session
	);

	/**
	 * GET rooms/allWithFirewallControl
	 */
	@GET
	@Path("allWithFirewallControl")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get all rooms which can be controlled")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.search")
	List<Room> allWithFirewallControl(
	        @ApiParam(hidden = true) @Auth Session session
	);

	/*
	 * GET rooms/toRegister
	 */
	@GET
	@Path("toRegister")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get all rooms where devices can be registered")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	List<Room> getRoomsToRegister(
	        @ApiParam(hidden = true) @Auth Session session
	);

	/*
	 * GET rooms/search/{search}
	 */
	@GET
	@Path("search/{search}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Search for room by name and descripton and type with substring.")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one user was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	//@PermitAll
	@RolesAllowed("room.search")
	List<Room> search(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("search") String search
	);

	/*
	 * POST rooms/add { hash }
	 */
	@POST
	@Path("add")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Create new room")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.add")
	CrxResponse add(
	        @ApiParam(hidden = true) @Auth Session session,
	        Room room
	);

	@POST
	@Path("import")
	@Produces(JSON_UTF8)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation(value =	"Import rooms from a CSV file. This MUST have following format:\\n" ,
		notes = "* Separator is the semicolon ';'.<br>" +
		"* A header line must be provided.<br>" +
		"* The header line is case insensitive.<br>" +
		"* The fields name and hwconf are mandatory.<br>" +
		"* Allowed fields are: description, count, control, network, type, places, rows.<br>")
	@ApiResponses(value = {
	            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.add")
	CrxResponse importRooms(
	@ApiParam(hidden = true) @Auth Session session,
	        @FormDataParam("file") final InputStream fileInputStream,
	        @FormDataParam("file") final FormDataContentDisposition contentDispositionHeader
	);

	/*
	 * POST rooms/add { hash }
	 */
	@POST
	@Path("modify")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Modify a room")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.add")
	CrxResponse modify(
	        @ApiParam(hidden = true) @Auth Session session,
	        Room room
	);

	/**
	 *
	 * @param session
	 * @param roomId
	 * @param room
	 * @return
	 */
	@POST
	@Path("{roomId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Modify a room")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.add")
	CrxResponse modify(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId") Long roomId,
	        Room room
	);


	/**
	 * Deletes a room by id with all devices with in.
	 * @param session
	 * @param roomId
	 * @return
	 */
	@DELETE
	@Path("{roomId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delete room by id")
	@ApiResponses(value = {
	    @ApiResponse(code = 404, message = "Room not found"),
	    @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("room.delete")
	CrxResponse delete(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId") Long roomId
	);

	/*
	 * GET rooms/{roomId}/hwConf
	 */
	@GET
	@Path("{roomId}/hwConf")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get hardware configuration of the room")
	    @ApiResponses(value = {
	   @ApiResponse(code = 404, message = "There is no more IP address in this room."),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.search")
	HWConf getHwConf(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId") Long roomId
	);

	/*
	 * SET rooms/{roomId}/{hwConfId}
	 */
	@PUT
	@Path("{roomId}/{hwConfId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Set hardware configuration of the room")
	    @ApiResponses(value = {
	   @ApiResponse(code = 404, message = "There is no more IP address in this room."),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.modify")
	CrxResponse setHwConf(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId")   Long roomId,
	        @PathParam("hwConfId") Long hwConfId
	);

	/*
	 * GET rooms/{roomId}/availableIPAddresses
	 */
	@GET
	@Path("{roomId}/availableIPAddresses")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "get all available ip-adresses of the room")
	    @ApiResponses(value = {
	   @ApiResponse(code = 404, message = "There is no more IP address in this room."),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.add")
	List<String> getAvailableIPAddresses(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId") Long roomId
	);

	/*
	 * GET rooms/{roomId}/getAvailableIPAddresses
	 */
	@GET
	@Path("{roomId}/availableIPAddresses/{count}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get count available ip-adresses of the room. The string list will contains the proposed name too: 'IP-Addres Proposed-Name'")
	    @ApiResponses(value = {
	    @ApiResponse(code = 404, message = "There is no more IP address in this room."),
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.add")
	List<String> getAvailableIPAddresses(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId") Long roomId,
	        @PathParam("count") Long count
	);

	/*
	 * GET rooms/getNextRoomIP/ { netMask : 26, netWork : "10.12.0.0.16" }
	 */
	@GET
	@Path("getNextRoomIP/{network}/{netmask}")
	@Produces(TEXT)
	@ApiOperation(value = "Delivers the next free ip address for a room.")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.add")
	String getNextRoomIP(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("network") String network,
	        @PathParam("netmask") int netmask
	);

	/*
	 * GET rooms/{roomId}/loggedInUsers
	 */
	@GET
	@Path("{roomId}/loggedInUsers")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delivers the list of the users which are logged in in a room.")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.manage")
	List<Map<String, String>> getLoggedInUsers(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId") Long roomId
	);

	/*
	* POST room/getRooms
	*/
	@POST
	@Path("getRooms")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets a list of room objects to the list of roomIds.")
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Group not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("room.search")
	List<Room> getRooms(
	        @ApiParam(hidden = true) @Auth Session session,
	        List<Long> roomIds
	);

	/*
	 * GET rooms/{roomId}/accessList
	 */
	@GET
	@Path("{roomId}/accessList")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the access scheduler in a room")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.add")
	List<AccessInRoom> getAccessList(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId") Long roomId
	);

	/*
	 * POST rooms/{roomId}/accessList { List<Hash> }
	 */
	@POST
	@Path("{roomId}/accessList")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Add an access list in a room",
	    notes = "<br>"
	    + "pointInTime have to have following format: HH:MM<br>"
	    + "accessType can be FW or ACT<br>"
	    + "If accessType is FW portal printing proxy direct can be set.<br>"
	    + "If accessType is ACT action can be shutdown,reboot,logout,close,open,wol<br>"	)
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.add")
	CrxResponse addAccessList(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId") Long roomId,
	        AccessInRoom   accessList
	);

	/*
	 * DELETE rooms/accessList/{accessInRoomId}
	 */
	@DELETE
	@Path("accessList/{accessInRoomId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delets an access list in a room")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.add")
	CrxResponse deleteAccessList(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("accessInRoomId") Long accessInRoomId
	);

	/*
	 * PUT rooms/scheduledAccess
	 */
	@PUT
	@Path("setScheduledAccess")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Sets access in all rooms corresponding to the access lists and the actual time.")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.add")
	CrxResponse setScheduledAccess(
	    @ApiParam(hidden = true) @Auth Session session
	);

	/*
	 * PUT rooms/setDefaultAccess
	 */
	@PUT
	@Path("setDefaultAccess")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Sets default access in all rooms.")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.add")
	CrxResponse setDefaultAccess(
	    @ApiParam(hidden = true) @Auth Session session
	);

	/*
	 * GET rooms/accessStatus
	 */
	@GET
	@Path("accessStatus")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the actual access status in all rooms. This can take a very long time. Do not use it!")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.add")
	List<AccessInRoom> getAccessStatus(
	   @ApiParam(hidden = true) @Auth Session session
	);

	/*
	 * GET rooms/{roomId}/accessStatus
	 */
	@GET
	@Path("{roomId}/accessStatus")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the actual access in a room")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.manage")
	AccessInRoom getAccessStatus(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("roomId") Long roomId
	);

	/*
	 * POST rooms/{roomId}/accessStatus
	 */
	@POST
	@Path("{roomId}/accessStatus")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Sets the actual access in a room")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.manage")
	CrxResponse setAccessStatus(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId") Long roomId,
	        AccessInRoom access
	);

	// Functions to manage Devices in Rooms
	/*
	 * POST rooms/{roomId}/devices { hash }
	 */
	@POST
	@Path("{roomId}/devices")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Create new devices")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one device was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.add")
	CrxResponse addDevices(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId") Long roomId,
	        List<Device> devices
	);

	/*
	 * PUT rooms/{roomId}/device/{macAddress}/{name}
	 */
	@PUT
	@Path("{roomId}/device/{macAddress}/{name}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Create a new device. This api call can be used only for registering own devices.")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one device was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	CrxResponse addDevice(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId") Long roomId,
	        @PathParam("macAddress") String macAddress,
	        @PathParam("name") String name
	);

	/*
	 * GET rooms/{roomId}/getDevices
	 */
	@GET
	@Path("{roomId}/devices")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets a list of the devices in room.")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one device was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.search")
	List<Device> getDevices(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId") Long roomId
	);

	/*
	 * POST  rooms/{roomId}/deleteDevices [ deviceId, deviceId]
	 */
	@POST
	@Path("{roomId}/deleteDevices")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "delete devices by id")
	@ApiResponses(value = {
	    @ApiResponse(code = 404, message = "Device not found"),
	    @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("device.delete")
	CrxResponse deleteDevices(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId") Long roomId,
	        List<Long> deviceId
	);

	/*
	 * DELETE {roomId}/deleteDevice/{deviceId}
	 */
	@DELETE
	@Path("{roomId}/device/{deviceId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delete a device defined by deviceId")
	@ApiResponses(value = {
	    @ApiResponse(code = 404, message = "Device not found"),
	    @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("device.delete")
	CrxResponse deleteDevice(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId") Long roomId,
	        @PathParam("deviceId") Long deviceId
	);

	/*
	 * Printer control
	 */
	@POST
	@Path("{roomId}/printers")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Modify the printers of one room.",
			notes = "The printers object has following format<br>"
					+ "{"
					+ "  defaultPrinter:  [id],"
					+ "  availablePrinters: [ id1, id2 ]"
					+ "}")
	@ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.modify")
	CrxResponse setPrinters(
	@ApiParam(hidden = true) @Auth Session session,
			@PathParam("roomId") Long roomId,
			Map<String, List<Long>> printers
	);

	@PUT
	@Path("{roomId}/defaultPrinter/{deviceId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Sets the default printer in a room.")
	@ApiResponses(value = {
	    @ApiResponse(code = 404, message = "Device not found"),
	    @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("room.manage")
	CrxResponse setDefaultPrinter(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId") Long roomId,
	        @PathParam("deviceId") Long deviceId
	);

	@PUT
	@Path("text/{roomName}/defaultPrinter/{printerName}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Sets the default printer in a room.")
	@ApiResponses(value = {
	    @ApiResponse(code = 404, message = "Device not found"),
	    @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("room.manage")
	CrxResponse setDefaultPrinter(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomName") String roomName,
	        @PathParam("printerName") String printerName
	);

	@DELETE
	@Path("{roomId}/defaultPrinter")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Deletes the default printer in a room.")
	@ApiResponses(value = {
	    @ApiResponse(code = 404, message = "Device not found"),
	    @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("room.manage")
	CrxResponse deleteDefaultPrinter(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId") Long roomId
	);

	@GET
	@Path("{roomId}/defaultPrinter")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delivers the default printer in a room.")
	@ApiResponses(value = {
	    @ApiResponse(code = 404, message = "Device not found"),
	    @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("room.manage")
	Printer getDefaultPrinter(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId") Long roomId
	);

	@POST
	@Path("{roomId}/availablePrinters")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Sets the available printers in a room.")
	@ApiResponses(value = {
	    @ApiResponse(code = 404, message = "Device not found"),
	    @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("room.manage")
	CrxResponse setAvailablePrinters(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId") Long roomId,
	        List<Long> printerIds
	);

	@PUT
	@Path("{roomId}/availablePrinters/{prinerId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Adds an available printers in a room.")
	@ApiResponses(value = {
	    @ApiResponse(code = 404, message = "Device not found"),
	    @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("room.manage")
	CrxResponse addAvailablePrinters(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId") Long roomId,
	        @PathParam("prinerId") Long prinerId
	);

	@DELETE
	@Path("{roomId}/availablePrinters/{prinerId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Deletes an avilable printer in a room.")
	@ApiResponses(value = {
	    @ApiResponse(code = 404, message = "Device not found"),
	    @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("room.manage")
	CrxResponse deleteAvailablePrinters(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId")   Long roomId,
	        @PathParam("prinerId") Long prinerId
	);

	@GET
	@Path("{roomId}/availablePrinters")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the list of available printers in a room.")
	@ApiResponses(value = {
	    @ApiResponse(code = 404, message = "Device not found"),
	    @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("room.search")
	List<Printer> getAvailablePrinters(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId") Long roomId
	);

	/*
	* GET rooms/{roomId}/actions
	*/
	@GET
	@Path("{roomId}/actions")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delivers a list of available actions for a device.")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.manage")
	List<String> getAvailableRoomActions(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId") Long roomId
	);

	/*
	 *   rooms/{roomId}/actions/{action}
	 */
	@PUT
	@Path("{roomId}/actions/{action}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Manage a device. Valid actions are open, close, reboot, shutdown, wol, logout, openProxy, closeProxy, organizeRoom.")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.manage")
	CrxResponse manageRoom(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId") Long roomId,
	        @PathParam("action") String action
	);

	/*
	 * POST rooms/{roomId}/actionWithMap/{action}
	 */
	@POST
	@Path("{roomId}/actionWithMap/{action}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Manage a device. Valid actions are open, close, reboot, shutdown, wol, logout, openProxy, closeProxy."
	     + "This version of call allows to send a map with some parametrs:"
	     + "graceTime : seconds to wait befor execute action."
	     + "message : the message to shown befor/during execute the action.")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.manage")
	CrxResponse manageRoom(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId") Long roomId,
	        @PathParam("action") String action,
	        Map<String, String> actionContent
	);

	/*
	 * DHCP-Management
	 */
	/**
	 * Gets the active dhcp parameter of a room
	 * @param session
	 * @param roomId
	 * @return a list of CrxMConfig objects representing the DHCP parameters
	 */
	@GET
	@Path("{roomId}/dhcp")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the active dhcp parameter of a room:",
			notes = "How to evaluate the CrxMConfig object:<br>"
			+ "id: ID of the dhcp parameter object<br>"
			+ "objectType: Device, but in this case it can be ignored.<br>"
			+ "objectId: the room id<br>"
			+ "keyword: this can be dhcpOption or dhcpStatement<br>"
			+ "value: the value of the dhcpOption or dhcpStatement."
			)
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.dhcp")
	List<CrxMConfig> getDHCP(
			@ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId") Long roomId
	        );

	@POST
	@Path("{roomId}/dhcp")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Adds a new dhcp parameter to a room:",
			notes = "How to setup the CrxMConfig object:<br>"
					+ "keyword: this can be dhcpOptions or dhcpStatements<br>"
					+ "value: the value of the dhcpOption or dhcpStatement.<br>"
					+ "Other parameter can be ignored.")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.dhcp")
	CrxResponse addDHCP(
			@ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId") Long roomId,
	        CrxMConfig dhcpParameter
	        );

	@DELETE
	@Path("{roomId}/dhcp/{parameterId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "ADeletes dhcp parameter to a room")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.dhcp")
	CrxResponse deleteDHCP(
			@ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId") Long roomId,
	        @PathParam("parameterId") Long parameterId
	        );
	/**
	 * Apply actions on a list of rooms.
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
	@RolesAllowed("room.manage")
	CrxResponse applyAction(
			@ApiParam(hidden = true) @Auth Session session,
			CrxActionMap actionMap
			);
}
