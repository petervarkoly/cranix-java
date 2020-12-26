 /* (c) 2020 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.api.resources;

import static de.cranix.api.resources.Resource.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.POST;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import de.cranix.dao.Clone;
import de.cranix.dao.CrxResponse;
import de.cranix.dao.Device;
import de.cranix.dao.HWConf;
import de.cranix.dao.Partition;
import de.cranix.dao.Room;
import de.cranix.dao.Session;
import de.cranix.dao.controller.CloneToolController;
import de.cranix.dao.controller.Config;
import de.cranix.dao.controller.RoomController;
import de.cranix.dao.controller.SessionController;
import de.cranix.dao.controller.DeviceController;
import de.cranix.dao.internal.CommonEntityManagerFactory;

@Path("clonetool")
@Api(value = "clonetool")
public class CloneToolResource {

	public CloneToolResource() {}

	/*
	 * Calls without authorization
	 */

	@GET
	@Path("hwconf")
	@Produces(TEXT)
	@ApiOperation(value = "Gets the id of the hardware configuration based on the IP-address of http request.")
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Device not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	public String getHWConf(
		@Context UriInfo ui,
	        @Context HttpServletRequest req
	) {
                EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
                Session session  = new SessionController(em).getLocalhostSession();
                Device device = new DeviceController(session,em).getByIP(req.getRemoteAddr());
                em.close();
                if( device != null && device.getHwconf() != null ) {
                        return Long.toString(device.getHwconf().getId());
                }
                return "";
        }

	@PUT
	@Path("resetMinion")
	@Produces(TEXT)
	@ApiOperation(value = "Removes the pubkey of the minion based on the IP-address of http request..")
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Device not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	public String resetMinion(
		@Context UriInfo ui,
	        @Context HttpServletRequest req
	) {
                EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
                Session session  = new SessionController(em).getLocalhostSession();
                Device device    = new DeviceController(session,em).getByIP(req.getRemoteAddr());
                String resp      = "";
                if( device != null ) {
                        resp = new CloneToolController(session,em).resetMinion(device.getId());
                }
                em.close();
                return resp;
        }

	@GET
	@Path("hostName")
	@Produces(TEXT)
	@ApiOperation(value = "Gets the fully qualified host name of the requester.")
	@ApiResponses(value = {
	        @ApiResponse(code = 401, message = "No regcode was found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	public String getHostname(
	        @Context UriInfo ui,
	        @Context HttpServletRequest req
	) {
                EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
                Session session  = new SessionController(em).getLocalhostSession();
                Device device = new DeviceController(session,em).getByIP(req.getRemoteAddr());
                em.close();
                if( device != null ) {
                        return device.getName();
                }
                return "";
        }


	@GET
	@Path("fqhn")
	@Produces(TEXT)
	@ApiOperation(value = "Gets the fully qualified host name of the requester.")
	@ApiResponses(value = {
	        @ApiResponse(code = 401, message = "No regcode was found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	public String getFqhn(
	        @Context UriInfo ui,
	        @Context HttpServletRequest req
	) {
                EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
                Session session  = new SessionController(em).getLocalhostSession();
                DeviceController deviceController = new DeviceController(session,em);
                Device device = deviceController.getByIP(req.getRemoteAddr());
                String resp = "";
                if( device != null ) {
                        resp = device.getName().concat(".").concat(deviceController.getConfigValue("DOMAIN"));
                }
                em.close();
                return resp;
        }

	@GET
	@Path("domainName")
	@Produces(TEXT)
	@ApiOperation(value = "Gets the fully qualified host name of the requester.")
	@ApiResponses(value = {
	        @ApiResponse(code = 401, message = "No regcode was found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	public String getDomainName(
	        @Context UriInfo ui,
	        @Context HttpServletRequest req
	) {
		return new Config().getConfigValue("DOMAIN");
	}

	@GET
	@Path("{hwconfId}/partitions")
	@Produces(TEXT)
	@ApiOperation(value = "Gets a space separated list of recorded partitions to a given hardware configuration.")
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Device not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	public String getPartitions(
		@Context UriInfo ui,
	        @Context HttpServletRequest req,
	        @PathParam("hwconfId") Long hwconfId
	) {
                EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
                Session session  = new SessionController(em).getLocalhostSession();
                String resp = new CloneToolController(session,em).getPartitions(hwconfId);
                em.close();
                return resp;
        }

	@GET
	@Path("{hwconfId}/{partitionName}/{key}")
	@Produces(TEXT)
	@ApiOperation(value = "Gets the value of a key to a given partition." +
			      "The key may be: OS, Description, Join, Format, Itool" )
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Device not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	public String getConfigurationValue(
		@Context UriInfo ui,
	        @Context HttpServletRequest req,
	        @PathParam("hwconfId") Long hwconfId,
	        @PathParam("partitionName") String partitionName,
	        @PathParam("key") String key
	) {
                EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
                Session session  = new SessionController(em).getLocalhostSession();
                String resp = new CloneToolController(session,em).getConfigurationValue(hwconfId,partitionName,key);
                em.close();
                return resp;
        }

	@DELETE
	@Path("devicesByIP/{deviceIP}/cloning")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Deletes the boot configuration for the automatical partitioning for a workstations")
	@RolesAllowed("hwconf.manage")
	public CrxResponse stopCloningOnDevice(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("deviceIP") String deviceIP
	) {
                EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
                Device device = new DeviceController(session,em).getByIP(deviceIP);
                CrxResponse resp = new CloneToolController(session,em).stopCloning("device",device.getId());
                em.close();
                return resp;
        }

	/*
	 * Calls with authorization
	 */
	@GET
	@Path("all")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets all hardware configuration.")
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Device not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@PermitAll
	public List<HWConf> getAllHWConf(
	        @ApiParam(hidden = true) @Auth Session session
	) {
                EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
                List<HWConf> resp = new CloneToolController(session,em).getAllHWConf();
                em.close();
                return resp;
        }

	@GET
	@Path("{hwconfId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets a hardware configuration.")
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Device not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("hwconf.search")
	public HWConf getById(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("hwconfId") Long hwconfId
	)  {
                EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
                final HWConf hwconf = new CloneToolController(session,em).getById(hwconfId);
                em.close();
                if (hwconf == null) {
                        throw new WebApplicationException(404);
                }
                return hwconf;
        }

	@GET
	@Path("byMac/{MAC}")
	@Produces(TEXT)
	@ApiOperation(value = "Gets the hwconf by MAC.")
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Device not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@PermitAll
	public String getByMac(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("MAC") String mac
	) {
                EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
                Device device = new DeviceController(session,em).getByMAC(mac);
                em.close();
                if( device != null && device.getHwconf() != null ) {
                        return Long.toString(device.getHwconf().getId());
                }
                return "";
        }

	@GET
	@Path("{hwconfId}/description")
	@Produces(TEXT)
	@ApiOperation(value = "Gets the description of a hardware configuration.")
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Device not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@PermitAll
	public String getDescription(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("hwconfId") Long hwconfId
	) {
                EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
                String resp = new CloneToolController(session,em).getDescription(hwconfId);
                em.close();
                return resp;
        }

	@GET
	@Path("{hwconfId}/deviceType")
	@Produces(TEXT)
	@ApiOperation(value = "Gets the deviceType of a hardware configuration.")
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Device not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@PermitAll
	public String getDeviceType(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("hwconfId") Long hwconfId
	) {
                EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
                String resp = new CloneToolController(session,em).getDeviceType(hwconfId);
                em.close();
                return resp;
        }

	@GET
	@Path("{hwconfId}/{partitionName}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the configuration of a partition to a given hardware configuration.")
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Device not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@PermitAll
	public Partition getPartition(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("hwconfId") Long hwconfId,
	        @PathParam("partitionName") String partitionName
	) {
                EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
                Partition resp = new CloneToolController(session,em).getPartition(hwconfId, partitionName);
                em.close();
                return resp;
        }

	@GET
	@Path("roomsToRegister")
	@Produces(TEXT)
	@ApiOperation(value = "Gets a list of rooms to register." +
			      "The format is id name##id name" )
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Device not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("hwconf.manage")
	public String getRoomsToRegister(
	        @ApiParam(hidden = true) @Auth Session session
	) {
                EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
                StringBuilder roomList = new StringBuilder();
                for( Room room : new RoomController(session,em).getAllToRegister() ) {
                        roomList.append(room.getId()).append("##").append(room.getName()).append(" ");
                }
                em.close();
                return roomList.toString();
        }

	@GET
	@Path("rooms/{roomId}/availableIPAddresses")
	@Produces(TEXT)
	@ApiOperation(value = "Get count available ip-adresses of the room. The string list will contains the proposed name too: 'IP-Addres Proposed-Name'")
		@ApiResponses(value = {
		@ApiResponse(code = 404, message = "There is no more IP address in this room."),
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("hwconf.add")
	public String getAvailableIPAddresses(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId") long roomId
	) {
                EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
                StringBuilder roomList = new StringBuilder();
                for( String name : new RoomController(session,em).getAvailableIPAddresses(roomId, 0) ) {
                        roomList.append(name.replaceFirst(" ","/")).append(" ").append(name.split(" ")[1]).append(" ");
                }
                em.close();
                return roomList.toString();
        }

	@PUT
	@Path("rooms/{roomId}/{macAddress}/{IP}/{name}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Create a new device. This api call can be used only for registering own devices.")
	@ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	public CrxResponse addDevice(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("roomId") long roomId,
	        @PathParam("macAddress") String macAddress,
	        @PathParam("IP") String IP,
	        @PathParam("name") String name
	) {
                EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
                RoomController rc = new RoomController(session,em);
                Room room = rc.getById(roomId);
                Device device = new Device();
                device.setName(name);
                device.setMac(macAddress);
                device.setIp(IP);
                if( room.getHwconf() != null ) {
                        device.setHwconfId(room.getHwconf().getId());
                }
                ArrayList<Device> devices = new ArrayList<Device>();
                devices.add(device);
                CrxResponse resp = rc.addDevices(roomId, devices).get(0);
                em.close();
                return resp;
        }

	@PUT
	@Path("{hwconfId}/{partitionName}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Create a new not configured partition to a given hardware configuration." +
			      "Only the name (like sdaXXX) is given. The other parameter must be set with an other put calls." )
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Device not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("hwconf.manage")
	public CrxResponse addPartition(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("hwconfId") Long hwconfId,
	        @PathParam("partitionName") String partitionName
	) {
                EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
                CrxResponse resp = new CloneToolController(session,em).addPartitionToHWConf(hwconfId, partitionName );
                em.close();
                return resp;
        }

	@PUT
	@Path("{hwconfId}/{partitionName}/{key}/{value}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Sets the value of a key to a given partition." +
			      "The keys may be: OS, Description, Join, Format, Itool" )
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Device not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("hwconf.manage")
	public CrxResponse setConfigurationValue(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("hwconfId") Long hwconfId,
	        @PathParam("partitionName") String partitionName,
	        @PathParam("key") String key,
	        @PathParam("value") String value
	) {
                EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
                CrxResponse resp = new CloneToolController(session,em).setConfigurationValue(hwconfId,partitionName,key,value);
                em.close();
                return resp;
        }

	@DELETE
	@Path("{hwconfId}/{partitionName}/{key}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delets a key an the correspondig value from a partition." +
			      "The key may be: OS, Description, Join, Format, Itool" )
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Device not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("hwconf.manage")
	public CrxResponse deleteConfigurationValue(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("hwconfId") Long hwconfId,
	        @PathParam("partitionName") String partitionName,
	        @PathParam("key") String key
	) {
                EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
                CrxResponse resp = new CloneToolController(session,em).deleteConfigurationValue(hwconfId,partitionName,key);
                em.close();
                return resp;
        }


	/*
	 * Deprecated functions implemented in HwconfResource 
	 * Theses functions will be deleted
	 */	
	@POST
	@Path("hwconf")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Creates a new hardware configuration.")
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Device not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("hwconf.add")
	public CrxResponse addHWConf(
	        @ApiParam(hidden = true) @Auth Session session,
	        HWConf hwconf
	) {
                EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
                CrxResponse resp = new CloneToolController(session,em).addHWConf(hwconf);
                em.close();
                return resp;
        }

	@POST
	@Path("{hwconfId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Updates a hardware configuration.")
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Device not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("hwconf.add")
	public CrxResponse modifyHWConf(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("hwconfId") Long hwconfId,
		    HWConf hwconf
	) {
                EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
                CrxResponse resp = new CloneToolController(session,em).modifyHWConf(hwconfId, hwconf);
                em.close();
                return resp;
        }

	/*
	 * POST clonetool/{hwconfId}/addPartition
	 */
	@POST
	@Path("{hwconfId}/addPartition")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Create a new partition to a given hardware configuration.")
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Device not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("hwconf.manage")
	public CrxResponse addPartition(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("hwconfId") Long hwconfId,
	        Partition partition
	) {
                EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
                CrxResponse resp = new CloneToolController(session,em).addPartitionToHWConf(hwconfId, partition);
                em.close();
                return resp;
        }

	/*
	 * PUT clonetool/{hwconfId}/{partitionName}/{key}/{value}
	 */
	/*
	 * DELETE clonetool/{hwconfId}
	 */
	@DELETE
	@Path("{hwconfId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Updates a hardware configuration.")
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Device not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("hwconf.manage")
	public CrxResponse delete(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("hwconfId") Long hwconfId
	) {
                EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
                CrxResponse resp = new CloneToolController(session,em).delete(hwconfId);
                em.close();
                return resp;
        }

	@DELETE
	@Path("{hwconfId}/{partitionName}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delets a partition to a given hardware configuration.")
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Device not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("hwconf.add")
	public CrxResponse deletePartition(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("hwconfId") Long hwconfId,
	        @PathParam("partitionName") String partitionName
	) {
                EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
                CrxResponse resp = new CloneToolController(session,em).deletePartition(hwconfId,partitionName);
                em.close();
                return resp;
        }

	@POST
	@Path("{hwconfId}/cloning")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Creates the boot configuration for the automatical partitioning." +
			"This call have to provide a hash with following informations" +
			" devices    : [ IDs of devices ] " +
			" partitions : [ IDs of partitions ] " +
			" multicast  :  true/fals"
	)
	@RolesAllowed("hwconf.manage")
	public CrxResponse startCloning(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("hwconfId") Long hwconfId,
		Clone parameters
	) {
                EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
                CrxResponse resp = new CloneToolController(session,em).startCloning(hwconfId,parameters);
                em.close();
                return resp;
        } 

	@PUT
	@Path("{hwconfId}/cloning/{multiCast}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Creates the boot configuration for the automatical partitioning for all workstations in a hwconf." +
			  "Multicast can be 0 or 1"
	)
	@RolesAllowed("hwconf.manage")
	public CrxResponse startCloning(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("hwconfId")  Long hwconfId,
		@PathParam("multiCast") int multiCast
	)  {
                EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
                return new CloneToolController(session,em).startCloning("hwconf", hwconfId, multiCast);
        }

	@PUT
	@Path("rooms/{roomId}/cloning/{multiCast}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Creates the boot configuration for the automatical partitioning for all workstations in a room." +
			  "Multicast can be 0 or 1"
	)
	@RolesAllowed("hwconf.manage")
	public CrxResponse startCloningInRoom(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("roomId") Long roomId,
		@PathParam("multiCast") int multiCast
	) {
                EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
                CrxResponse resp = new CloneToolController(session,em).startCloning("room", roomId, multiCast);
                em.close();
                return resp;
        }

	@PUT
	@Path("devices/{deviceId}/cloning")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Creates the boot configuration for the automatical partitioning for a workstations")
	@RolesAllowed("hwconf.manage")
	public CrxResponse startCloningOnDevice(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("deviceId") Long deviceId
	) {
                EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
                CrxResponse resp = new CloneToolController(session,em).startCloning("device", deviceId, 0);
                em.close();
                return resp;
        }

	@DELETE
	@Path("{hwconfId}/cloning")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Removes the boot configuration for the automatical partitioning.")
	@RolesAllowed("hwconf.manage")
	public CrxResponse stopCloning(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("hwconfId") Long hwconfId
	) {
                EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
                CrxResponse resp = new CloneToolController(session,em).stopCloning("hwconf",hwconfId);
                em.close();
                return resp;
        }

	@DELETE
	@Path("rooms/{roomId}/cloning")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Removes the boot configuration for the automatical partitioning for all workstations in a room." )
	@RolesAllowed("hwconf.manage")
	public CrxResponse stopCloningInRoom(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("roomId") Long roomId
	) {
                EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
                CrxResponse resp = new CloneToolController(session,em).stopCloning("room",roomId);
                em.close();
                return resp;
        }

	@DELETE
	@Path("devices/{deviceId}/cloning")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Creates the boot configuration for the automatical partitioning for a workstations")
	@RolesAllowed("hwconf.manage")
	public CrxResponse stopCloningOnDevice(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("deviceId") Long deviceId
	) {
                EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
                CrxResponse resp = new CloneToolController(session,em).stopCloning("device",deviceId);
                em.close();
                return resp;
        }


	@GET
	@Path("multicastDevices")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the list of the network devices for multicast cloning.")
	@RolesAllowed("hwconf.manage")
	public String[] getMulticastDevices(
		@ApiParam(hidden = true) @Auth Session session
	) {
                Config config = new Config("/etc/sysconfig/dhcpd","DHCPD_");
                return config.getConfigValue("INTERFACE").split("\\s+");
        }

	/**
	 * Start multicast cloning process of a partition on a device
	 * @param session
	 * @param partitionId
	 * @param networkDevice
	 * @return
	 */
	@PUT
	@Path("partitions/{partitionId}/multicast/{networkDevice}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Start multicast imaging with a given partition.")
	@RolesAllowed("hwconf.manage")
	public CrxResponse startMulticast(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("partitionId") Long partitionId,
		@PathParam("networkDevice") String networkDevice
	) {
                EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
                CrxResponse resp = new CloneToolController(session,em).startMulticast(partitionId,networkDevice);
                em.close();
                return resp;
        }

	/**
	 * Sets the parameters of an existing partition
	 * @param session
	 * @param partitionId
	 * @param partition
	 * @return
	 */
	@POST
	@Path("partitions/{partitionId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Sets the parameters of an existing partition.")
	@RolesAllowed("hwconf.manage")
	public CrxResponse modifyPartition(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("partitionId") Long partitionId,
		Partition partition
	) {
                EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
                CrxResponse resp = new CloneToolController(session,em).modifyPartition(partitionId, partition);
                em.close();
                return resp;
        }

	@PUT
	@Path("devices/{deviceId}/resetMinion")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Removes the pubkey of the minion.")
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Device not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@PermitAll
	public String resetMinion(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("deviceId") Long deviceId
	) {
                EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
                String resp = new CloneToolController(session,em).resetMinion(deviceId);
                em.close();
                return resp;
        }
}
