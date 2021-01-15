/* (c) 2020 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.api.resources;

import static de.cranix.api.resources.Resource.JSON_UTF8;

import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.POST;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.cranix.dao.*;
import de.cranix.services.*;
import de.cranix.helper.CrxEntityManagerFactory;

@Path("adhocrooms")
@Api(value = "adhocrooms")
public class AdHocLanResource {

	Logger logger = LoggerFactory.getLogger(AdHocLanResource.class);

	public AdHocLanResource() {
		super();
	}

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
	public List<AdHocRoom> getAll( @ApiParam(hidden = true) @Auth Session session) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<AdHocRoom> resp = new AdHocLanService(session,em).getAll();
		em.close();
		return resp;
	}

	@POST
	@Path("add")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Create new AddHocLan room")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("adhoclan.manage")
	public CrxResponse add(
		@ApiParam(hidden = true) @Auth Session session,
		AdHocRoom room
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new AdHocLanService(session,em).add(room);
		em.close();
		return resp;
	}

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
	public CrxResponse delete(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("id")		Long id
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new RoomService(session,em).delete(id);
		em.close();
		return resp;
	}

	@GET
	@Path("{id}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets an AdHocLan room by id.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "No category was found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("adhoclan.manage")
	public AdHocRoom getById(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("id")		Long id
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		final RoomService roomService = new RoomService(session,em);
		Room room = roomService.getById(id);
		if( room != null && !room.getRoomType().equals("AdHocAccess")) {
			return null;
		}
		AdHocRoom adhocRoom = new AdHocLanService(session,em).roomToAdHoc(room);
		em.close();
		return adhocRoom;
	}

	@POST
	@Path("{id}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Modify an AdHocLan room")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "No category was found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("adhoclan.manage")
	public CrxResponse modify(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("id")		Long id,
		AdHocRoom room
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new AdHocLanService(session,em).modify(room);
		em.close();
		return resp;
	}

	@GET
	@Path("{id}/users")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets all defined groups or users or devices in a giwen AdHocLan room. Object types can be Group or User")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "No category was found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("adhoclan.manage")
	public List<User> getUsersOfRoom(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("id")	 Long id
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		Room room  = new RoomService(session,em).getById(id);
		em.close();
		if( room != null ) {
			for( Category category : room.getCategories() ) {
				if( category.getCategoryType().equals("AdHocAccess")) {
					return category.getUsers();
				}
			}
		}
		return new ArrayList<User>();
	}

	@GET
	@Path("{id}/groups")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets all defined groups or users or devices in a giwen AdHocLan room. Object types can be Group or User")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "No category was found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("adhoclan.manage")
	public List<Group> getGroupsOfRoom(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("id")	 Long id
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		Room	room = new RoomService(session,em).getById(id);
		em.close();
		if( room != null ) {
			for( Category category : room.getCategories() ) {
				if( category.getCategoryType().equals("AdHocAccess")) {
					return category.getGroups();
				}
			}
		}
		return new ArrayList<Group>();
	}

	@GET
	@Path("{id}/available/users")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get the non member users of an AdHocLan room.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "Category not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("adhoclan.manage")
	public List<User> getAvailableUser(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("id") Long id
	) {
		EntityManager em  = CrxEntityManagerFactory.instance().createEntityManager();
		List<User> users  = new ArrayList<User>();
		Category category = new AdHocLanService(session,em).getAdHocCategoryOfRoom(id);
		if( category != null ) {
			logger.debug("getAvailableUser Category " + category);
			for( User user : new UserService(session,em).getAll() ) {
				if( !category.getUsers().contains(user) ) {
					users.add(user);
				}
			}
		}
		em.close();
		return users;
	}

	@GET
	@Path("{id}/available/groups")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get the non member groups of an AdHocLan room.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "Category not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("adhoclan.manage")
	public List<Group> getAvailableGroups(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("id") Long id
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<Group> groups = new ArrayList<Group>();
		Category category = new AdHocLanService(session,em).getAdHocCategoryOfRoom(id);
		if( category != null ) {
			logger.debug("getAvailableGroups Category " + category);
			for( Group group : new GroupService(session,em).getAll() ) {
				if( !category.getGroups().contains(group) ) {
					groups.add(group);
				}
			}
		}
		em.close();
		return groups;
	}

	@PUT
	@Path("{id}/{objectType}/{objectId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Add a new group or user to a giwen AdHocLan room")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "No category was found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("adhoclan.manage")
	public CrxResponse putObjectIntoRoom(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("id")		Long id,
		@PathParam("objectType")	String objectType,
		@PathParam("objectId")		Long objectId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new AdHocLanService(session,em).putObjectIntoRoom(id,objectType,objectId);
		em.close();
		return resp;
	}

	@DELETE
	@Path("{id}/{objectType}/{objectId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Removes a group or user from an AdHocLan room")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "No category was found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("adhoclan.manage")
	public CrxResponse deleteObjectInRoom(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("id")		Long id,
		@PathParam("objectType")	String objectType,
		@PathParam("objectId")		Long objectId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new AdHocLanService(session,em).deleteObjectInRoom(id,objectType,objectId);
		em.close();
		return resp;
	}

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
	public List<Device> getDevicesOfRoom(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("id")		Long id
	) {
		Room room = this.getById(session,id);
		List<Device> devices = new ArrayList<Device>();
		if( room != null ) {
			for(Device device : room.getDevices() ) {
				if(device.getOwner() != null ) {
					device.setOwnerName(
						String.format("%s (%s, %s)",
							device.getOwner().getUid(),
							device.getOwner().getSurName(),
							device.getOwner().getGivenName()
						)
					);
				}
				devices.add(device);
			}
		}
		return devices;
	}

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
	public List<AdHocRoom> getMyRooms( @ApiParam(hidden = true) @Auth Session session ) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		RoomService roomService = new RoomService(session,em);
		List<AdHocRoom> resp= new ArrayList<AdHocRoom>();
		AdHocLanService adHocLan   = new AdHocLanService(session,em);
		for( Room  room : roomService.getAllToRegister() ) {
			resp.add(adHocLan.roomToAdHoc(room));
		}
		em.close();
		return resp;
	}

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
	public List<Device> getDevices( @ApiParam(hidden = true) @Auth Session session) {
		return session.getUser().getOwnedDevices();
	}

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
	public CrxResponse deleteDevice(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("deviceId")		 Long	deviceId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new DeviceService(session,em).delete(deviceId, true);
		em.close();
		return resp;
	}

	@POST
	@Path("devices/{deviceId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Modify an owned AdHocLan Devices of a user.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "No category was found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@PermitAll
	public CrxResponse modifyDevice(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("deviceId")	 Long  deviceId,
			Device device
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		device.setId(deviceId);
		CrxResponse resp = new DeviceService(session,em).modify(device);
		em.close();
		return resp;
	}

	@PUT
	@Path("{id}/device/{MAC}/{name}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Create a new device. This api call can be used only for registering own devices.")
	@ApiResponses(value = {
			// TODO so oder anders? @ApiResponse(code = 404, message = "At least one device was not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	public CrxResponse addDevice(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("id")	Long id,
			@PathParam("MAC")	String macAddress,
			@PathParam("name")	String name
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new RoomService(session,em).addDevice(id, macAddress, name);
		em.close();
		return resp;
	}
}