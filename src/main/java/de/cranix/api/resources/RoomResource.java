/* (c) 2021 Peter Varkoly <pvarkoly@cephalix.eu> - all rights reserved */
package de.cranix.api.resources;


import static de.cranix.api.resources.Resource.*;

import io.dropwizard.auth.Auth;
import io.swagger.annotations.*;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import de.cranix.dao.AccessInRoom;
import de.cranix.dao.CrxMConfig;
import de.cranix.dao.CrxActionMap;
import de.cranix.dao.CrxResponse;
import de.cranix.dao.Device;
import de.cranix.dao.HWConf;
import de.cranix.dao.Printer;
import de.cranix.dao.Room;
import de.cranix.dao.Session;
import de.cranix.helper.CrxEntityManagerFactory;
import de.cranix.services.RoomService;
import de.cranix.services.EducationService;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Path("rooms")
@Api(value = "rooms")
public class RoomResource {

	public RoomResource() {}

	@POST
	@Path("add")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Create new room")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.add")
	public CrxResponse add(
		@ApiParam(hidden = true) @Auth Session session,
		Room room
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new RoomService(session,em).add(room);
		em.close();
		return resp;
	}

	@DELETE
	@Path("{roomId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delete room by id")
	@ApiResponses(value = {
	    @ApiResponse(code = 404, message = "Room not found"),
	    @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("room.delete")
	public CrxResponse delete(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("roomId") Long roomId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new RoomService(session,em).delete(roomId);
		em.close();
		return resp;
	}

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
	public CrxResponse importRooms(
	@ApiParam(hidden = true) @Auth Session session,
		@FormDataParam("file") final InputStream fileInputStream,
		@FormDataParam("file") final FormDataContentDisposition contentDispositionHeader
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new RoomService(session,em).importRooms(fileInputStream, contentDispositionHeader);
		em.close();
		return resp;
	}

	@POST
	@Path("{roomId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Modify a room")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.add")
	public CrxResponse modify(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("roomId") Long roomId,
		Room room
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		room.setId(roomId);
		CrxResponse resp = new RoomService(session,em).modify(room);
		em.close();
		return resp;
	}

	@POST
	@Path("modify")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Modify a room")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.add")
	public CrxResponse modify(
		@ApiParam(hidden = true) @Auth Session session,
		Room room
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new RoomService(session,em).modify(room);
		em.close();
		return resp;
	}

	@GET
	@Path("{roomId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get a room by id")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "Room not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("room.search")
	public Room getById(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("roomId") Long roomId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		Room room = new RoomService(session,em).getById(roomId);
		em.close();
		if (room == null) {
			throw new WebApplicationException(404);
		}
		return room;
	}

	@GET
	@Path("all")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get all rooms")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	public List<Room> getAll( @ApiParam(hidden = true) @Auth Session session) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<Room> rooms = new RoomService(session,em).getAll();
		em.close();
		return rooms;
	}

	@GET
	@Path("allWithControl")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get all rooms which can be controlled")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.search")
	public List<Room> allWithControl( @ApiParam(hidden = true) @Auth Session session) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<Room> rooms = new RoomService(session,em).getAllWithControl();
		em.close();
		return rooms;
	}

	@GET
	@Path("allWithFirewallControl")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get all rooms which can be controlled")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.search")
	public List<Room> allWithFirewallControl( @ApiParam(hidden = true) @Auth Session session) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<Room> rooms = new RoomService(session,em).getAllWithFirewallControl();
		em.close();
		return rooms;
	}

	@GET
	@Path("toRegister")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get all rooms where devices can be registered")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	public List<Room> getRoomsToRegister( @ApiParam(hidden = true) @Auth Session session) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		final RoomService roomService = new RoomService(session,em);
		List<Room> resp = roomService.getAllToRegister();
		em.close();
		return resp;
	}


	@PUT
	@Path("{roomId}/{hwconfId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Set hardware configuration of the room")
	    @ApiResponses(value = {
	   @ApiResponse(code = 404, message = "There is no more IP address in this room."),
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.modify")
	public CrxResponse setHwConf(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("roomId")   Long roomId,
		@PathParam("hwconfId") Long hwconfId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new RoomService(session,em).setHWConf(roomId,hwconfId);
		em.close();
		return resp;
	}

	@GET
	@Path("{roomId}/availableIPAddresses")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "get all available ip-adresses of the room")
	    @ApiResponses(value = {
	   @ApiResponse(code = 404, message = "There is no more IP address in this room."),
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.add")
	public List<String> getAvailableIPAddresses(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("roomId") Long roomId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		final List<String> ips = new RoomService(session,em).getAvailableIPAddresses(roomId);
		em.close();
		if ( ips == null) {
			throw new WebApplicationException(404);
		}
		return ips;
	}


	@GET
	@Path("{roomId}/availableIPAddresses/{count}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get count available ip-adresses of the room. The string list will contains the proposed name too: 'IP-Addres Proposed-Name'")
	    @ApiResponses(value = {
	    @ApiResponse(code = 404, message = "There is no more IP address in this room."),
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.add")
	public List<String> getAvailableIPAddresses(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("roomId") Long roomId,
		@PathParam("count") Long count
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		final List<String> ips = new RoomService(session,em).getAvailableIPAddresses(roomId,count);
		em.close();
		if ( ips == null) {
		    throw new WebApplicationException(404);
		}
		return ips;
	}

	@GET
	@Path("getNextRoomIP/{network}/{netmask}")
	@Produces(TEXT)
	@ApiOperation(value = "Delivers the next free ip address for a room.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.add")
	public String getNextRoomIP(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("network") String network,
		@PathParam("netmask") int netmask
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		final RoomService roomService = new RoomService(session,em);
		String[] l = network.split("\\.");
		network = l[0] + "." + l[1] + "." +l[2] + "."  + l[3] + "/" + l[4];
		final String nextIP = roomService.getNextRoomIP(network, netmask);
		em.close();
		if ( nextIP == null) {
			throw new WebApplicationException(404);
		}
		return nextIP;
	}

	@GET
	@Path("{roomId}/loggedInUsers")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delivers the list of the users which are logged in in a room.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.manage")
	public List<Map<String, String>> getLoggedInUsers(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("roomId") Long roomId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		final List<Map<String, String>> users = new RoomService(session,em).getLoggedInUsers(roomId);
		em.close();
		if ( users == null) {
			throw new WebApplicationException(404);
		}
		return users;
	}

	@GET
	@Path("accessList")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the access scheduler in all rooms")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.manage")
	public List<AccessInRoom> getAccessList( @ApiParam(hidden = true) @Auth Session session) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		final List<AccessInRoom> accesses = new RoomService(session,em).getAccessList();
		em.close();
		return accesses;
	}

	@GET
	@Path("{roomId}/accessList")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the access scheduler in a room")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.add")
	public List<AccessInRoom> getAccessList(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("roomId") Long roomId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		final List<AccessInRoom> accesses = new RoomService(session,em).getAccessList(roomId);
		em.close();
		if ( accesses == null) {
			throw new WebApplicationException(404);
		}
		return accesses;
	}

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
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.add")
	public CrxResponse addAccessList(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("roomId") Long roomId,
		AccessInRoom   accessList
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new RoomService(session,em).addAccessList(roomId,accessList);
		em.close();
		return resp;
	}

	@DELETE
	@Path("accessList/{accessInRoomId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delets an access list in a room")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.add")
	public CrxResponse deleteAccessList(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("accessInRoomId") Long accessInRoomId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new RoomService(session,em).deleteAccessList(accessInRoomId);
		em.close();
		return resp;
	}

	@PUT
	@Path("setScheduledAccess")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Sets access in all rooms corresponding to the access lists and the actual time.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.manage")
	public CrxResponse setScheduledAccess( @ApiParam(hidden = true) @Auth Session session) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new RoomService(session,em).setScheduledAccess();
		em.close();
		return resp;
	}

	@PUT
	@Path("setDefaultAccess")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Sets default access in all rooms.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.manage")
	public CrxResponse setDefaultAccess( @ApiParam(hidden = true) @Auth Session session) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new RoomService(session,em).setDefaultAccess();
		em.close();
		return resp;
	}

	@GET
	@Path("accessStatus")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the actual access status in all rooms. This can take a very long time. Do not use it!")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.manage")
	public List<AccessInRoom> getAccessStatus( @ApiParam(hidden = true) @Auth Session session) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		final List<AccessInRoom> accesses = new RoomService(session,em).getAccessStatus();
		em.close();
		return accesses;
	}

	@GET
	@Path("{roomId}/accessStatus")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the actual access in a room")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.manage")
	public AccessInRoom getAccessStatus(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("roomId") Long roomId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		AccessInRoom resp = new RoomService(session,em).getAccessStatus(roomId);
		em.close();
		return resp;
	}

	@POST
	@Path("{roomId}/accessStatus")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Sets the actual access in a room")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.manage")
	public CrxResponse setAccessStatus(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("roomId") Long roomId,
		AccessInRoom access
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new RoomService(session,em).setAccessStatus(roomId, access);
		em.close();
		return resp;
	}

	@POST
	@Path("accessList")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Sets the actual access in a room")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.manage")
	public CrxResponse setAccessStatus(
		@ApiParam(hidden = true) @Auth Session session,
		AccessInRoom access
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new RoomService(session,em).modifyAccessInRoom(access);
		em.close();
		return resp;
	}

	@POST
	@Path("{roomId}/devices")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Create new devices")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.add")
	public List<CrxResponse> addDevices(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("roomId") Long roomId,
		List<Device> devices
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<CrxResponse> crxResponse = new RoomService(session,em).addDevices(roomId,devices);
		em.close();
		return crxResponse;
	}

	@PUT
	@Path("{roomId}/device/{macAddress}/{name}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Create a new device. This api call can be used only for registering own devices.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	public CrxResponse addDevice(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("roomId") Long roomId,
		@PathParam("macAddress") String macAddress,
		@PathParam("name") String name
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new RoomService(session,em).addDevice(roomId,macAddress,name);
		em.close();
		return resp;
	}

	@GET
	@Path("{roomId}/devices")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets a list of the devices in room.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.search")
	public List<Device> getDevices(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("roomId") Long roomId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<Device> resp = new RoomService(session,em).getDevices(roomId);
		em.close();
		return resp;
	}

	@DELETE
	@Path("{roomId}/device/{deviceId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delete a device defined by deviceId")
	@ApiResponses(value = {
	    @ApiResponse(code = 404, message = "Device not found"),
	    @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("device.delete")
	public CrxResponse deleteDevice(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("roomId") Long roomId,
		@PathParam("deviceId") Long deviceId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<Long> deviceIds = new ArrayList<Long>();
		deviceIds.add(deviceId);
		CrxResponse resp = new RoomService(session,em).deleteDevices(roomId,deviceIds);
		em.close();
		return resp;
	}

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
	public CrxResponse setPrinters(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("roomId") Long roomId,
		Map<String, List<Long>> printers
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		RoomService roomService = new RoomService(session,em);
		CrxResponse resp = roomService.setPrinters(roomId, printers);
		em.close();
		return resp;
	}

	@PUT
	@Path("{roomId}/defaultPrinter/{deviceId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Sets the default printer in a room.")
	@ApiResponses(value = {
	    @ApiResponse(code = 404, message = "Device not found"),
	    @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("room.manage")
	public CrxResponse setDefaultPrinter(
		@ApiParam(hidden = true) @Auth  Session session,
		@PathParam("roomId")		Long roomId,
		@PathParam("deviceId")		Long printerId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new RoomService(session,em).setDefaultPrinter(roomId, printerId);
		em.close();
		return resp;
	}

	@DELETE
	@Path("{roomId}/defaultPrinter")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Deletes the default printer in a room.")
	@ApiResponses(value = {
	    @ApiResponse(code = 404, message = "Device not found"),
	    @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("room.manage")
	public CrxResponse deleteDefaultPrinter(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("roomId") Long roomId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new RoomService(session,em).deleteDefaultPrinter(roomId);
		em.close();
		return resp;
	}

	@GET
	@Path("{roomId}/defaultPrinter")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delivers the default printer in a room.")
	@ApiResponses(value = {
	    @ApiResponse(code = 404, message = "Device not found"),
	    @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("room.manage")
	public Printer getDefaultPrinter(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("roomId") Long roomId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		Printer resp = new RoomService(session,em).getById(roomId).getDefaultPrinter();
		em.close();
		return resp;
	}

	@POST
	@Path("{roomId}/availablePrinters")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Sets the available printers in a room.")
	@ApiResponses(value = {
	    @ApiResponse(code = 404, message = "Device not found"),
	    @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("room.manage")
	public CrxResponse setAvailablePrinters(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("roomId") Long roomId,
		List<Long> printerIds
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new RoomService(session,em).setAvailablePrinters(roomId, printerIds);
		em.close();
		return resp;
	}

	@PUT
	@Path("{roomId}/availablePrinters/{prinerId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Adds an available printers in a room.")
	@ApiResponses(value = {
	    @ApiResponse(code = 404, message = "Device not found"),
	    @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("room.manage")
	public CrxResponse addAvailablePrinters(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("roomId")   Long roomId,
		@PathParam("prinerId") Long printerId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new RoomService(session,em).addAvailablePrinter(roomId, printerId);
		em.close();
		return resp;
	}

	@DELETE
	@Path("{roomId}/availablePrinters/{prinerId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Deletes an avilable printer in a room.")
	@ApiResponses(value = {
	    @ApiResponse(code = 404, message = "Device not found"),
	    @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("room.manage")
	public CrxResponse deleteAvailablePrinters(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("roomId")   Long roomId,
		@PathParam("prinerId") Long printerId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new RoomService(session,em).deleteAvailablePrinter(roomId, printerId);
		em.close();
		return resp;
	}

	@GET
	@Path("{roomId}/availablePrinters")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the list of available printers in a room.")
	@ApiResponses(value = {
	    @ApiResponse(code = 404, message = "Device not found"),
	    @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("room.search")
	public List<Printer> getAvailablePrinters(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("roomId")     Long roomId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<Printer> resp = new RoomService(session,em).getById(roomId).getAvailablePrinters();
		em.close();
		return resp;
	}

	/*
	 * may be deprecated
	 */
	@PUT
	@Path("text/{roomName}/defaultPrinter/{printerName}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Sets the default printer in a room.")
	@ApiResponses(value = {
	    @ApiResponse(code = 404, message = "Device not found"),
	    @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("room.manage")
	public CrxResponse setDefaultPrinter(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("roomName") String roomName,
		@PathParam("printerName") String printerName
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new RoomService(session,em).setDefaultPrinter(roomName, printerName);
		em.close();
		return resp;
	}

	@POST
	@Path("applyAction")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Apply actions on selected rooms.")
	@ApiResponses(value = {
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("room.manage")
	public List<CrxResponse> applyAction(
		@ApiParam(hidden = true) @Auth Session session,
		CrxActionMap actionMap
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<CrxResponse> resp = new RoomService(session,em).applyAction(actionMap);
		em.close();
		return resp;
	}

	@GET
	@Path("{roomId}/actions")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delivers a list of available actions for a device.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.manage")
	public List<String> getAvailableRoomActions(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("roomId") Long roomId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<String> resp = new EducationService(session,em).getAvailableRoomActions(roomId);
		em.close();
		return resp;
	}

	@PUT
	@Path("{roomId}/actions/{action}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Manage a device. Valid actions are open, close, reboot, shutdown, wol, logout, openProxy, closeProxy, organizeRoom.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.manage")
	public CrxResponse manageRoom(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("roomId") Long roomId,
		@PathParam("action") String action
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new EducationService(session,em).manageRoom(roomId,action, null);
		em.close();
		return resp;
	}

	@POST
	@Path("{roomId}/actionWithMap/{action}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Manage a device. Valid actions are open, close, reboot, shutdown, wol, logout, openProxy, closeProxy."
	     + "This version of call allows to send a map with some parametrs:"
	     + "graceTime : seconds to wait befor execute action."
	     + "message : the message to shown befor/during execute the action.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.manage")
	public CrxResponse manageRoom(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("roomId") Long roomId,
		@PathParam("action") String action,
		Map<String, String> actionContent
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new EducationService(session,em).manageRoom(roomId,action, actionContent);
		em.close();
		return resp;
	}

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
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.dhcp")
	public List<CrxMConfig> getDHCP(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("roomId") Long roomId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<CrxMConfig> resp = new RoomService(session,em).getDHCP(roomId);
		em.close();
		return resp;
	}

	@POST
	@Path("{roomId}/dhcp")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Adds a new dhcp parameter to a room:",
			notes = "How to setup the CrxMConfig object:<br>"
					+ "keyword: this can be dhcpOptions or dhcpStatements<br>"
					+ "value: the value of the dhcpOption or dhcpStatement.<br>"
					+ "Other parameter can be ignored.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.dhcp")
	public CrxResponse addDHCP(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("roomId") Long roomId,
		CrxMConfig dhcpParameter
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new RoomService(session,em).addDHCP(roomId,dhcpParameter);
		em.close();
		return resp;
	}

	@DELETE
	@Path("{roomId}/dhcp/{parameterId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "ADeletes dhcp parameter to a room")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.dhcp")
	public CrxResponse deleteDHCP(
		@ApiParam(hidden = true)  @Auth Session session,
		@PathParam("roomId")      Long roomId,
		@PathParam("parameterId") Long parameterId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new RoomService(session,em).deleteMConfig(roomId,parameterId);
		em.close();
		return resp;
	}

}
