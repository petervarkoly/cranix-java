 /* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.api.resources;

import static de.cranix.api.resources.Resource.*;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
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
import de.cranix.dao.HWConf;
import de.cranix.dao.CrxResponse;
import de.cranix.dao.Partition;
import de.cranix.dao.Session;
import java.util.List;

@Path("clonetool")
@Api(value = "clonetool")
public interface CloneToolResource {

	/*
	 * Get clonetool/hwconf
	 */
	@GET
	@Path("hwconf")
	@Produces(TEXT)
	@ApiOperation(value = "Gets the id of the hardware configuration based on the IP-address of http request.")
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Device not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	String getHWConf(
			@Context UriInfo ui,
	        @Context HttpServletRequest req
	);

	/*
	 * PUT clonetool/resetMinion
	 */
	@PUT
	@Path("resetMinion")
	@Produces(TEXT)
	@ApiOperation(value = "Removes the pubkey of the minion based on the IP-address of http request..")
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Device not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	String resetMinion(
			@Context UriInfo ui,
	        @Context HttpServletRequest req
	);

	/*
	 * PUT clonetool/devices/{deviceId}/resetMinion
	 */
	@PUT
	@Path("devices/{deviceId}/resetMinion")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Removes the pubkey of the minion.")
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Device not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@PermitAll
	String resetMinion(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("deviceId") Long deviceId
	);


	/*
	 * Get clonetool/devices/{deviceId}/isMaster
	 */
	@GET
	@Path("devices/{deviceId}/isMaster")
	@Produces(TEXT)
	@ApiOperation(value = "Returns 'true' if the workstation of the deviceId is master. Returns empty if not.")
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Device not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@PermitAll
	String isMaster(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("deviceId") Long deviceId
	);

	/*
	 * Get clonetool/devices/{deviceId}/setMaster/{isMaster}
	 */
	@PUT
	@Path("devices/{deviceId}/setMaster/{isMaster}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Sets or resets master marking on a workstation. isMaster can be 1 or 0.")
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Device not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("hwconf.manage")
	CrxResponse setMaster(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("deviceId") Long deviceId,
	        @PathParam("isMaster") int isMaster
	);

	/*
	 * Get clonetool/devices/{deviceIisMaster
	 */
	@PUT
	@Path("devices/setMaster/{isMaster}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Returns 'true' if the workstation of the session is master. Returns empty if not.")
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Device not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("hwconf.manage")
	CrxResponse setMaster(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("isMaster") int isMaster
	);

	/*
	 * Get clonetool/all
	 */
	@GET
	@Path("all")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets all hardware configuration.")
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Device not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("hwconf.search")
	List<HWConf> getAllHWConf(
	        @ApiParam(hidden = true) @Auth Session session
	);

	/*
	 * Get clonetool/{hwconfId}
	 */
	@GET
	@Path("{hwconfId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets a hardware configuration.")
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Device not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("hwconf.search")
	HWConf getById(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("hwconfId") Long hwconfId
	);

	/*
     * GET clonetool/{hwconfId}/master
    */
	@GET
	@Path("{hwconfId}/master")
	@Produces(TEXT)
	@ApiOperation(value = "Delivers the id of the master device of this HWConf.")
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Device not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@PermitAll
	Long getMaster(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("hwconfId") Long hwconfId
	);

	/*
     * GET clonetool/{hwconfId}/description
    */
	@GET
	@Path("{hwconfId}/description")
	@Produces(TEXT)
	@ApiOperation(value = "Gets the description of a hardware configuration.")
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Device not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@PermitAll
	String getDescription(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("hwconfId") Long hwconfId
	);

	/*
     * GET clonetool/{hwconfId}/partitions
    */
	@GET
	@Path("{hwconfId}/partitions")
	@Produces(TEXT)
	@ApiOperation(value = "Gets a space separated list of recorded partitions to a given hardware configuration.")
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Device not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	String getPartitions(
			@Context UriInfo ui,
	        @Context HttpServletRequest req,
	        @PathParam("hwconfId") Long hwconfId
	);

	/*
	 * Get clonetool/{hwconfId}/{partitionName}
	 */
	@GET
	@Path("{hwconfId}/{partitionName}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the configuration of a partition to a given hardware configuration.")
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Device not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@PermitAll
	Partition getPartition(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("hwconfId") Long hwconfId,
	        @PathParam("partitionName") String partitionName
	);

	/*
	 * GET clonetool/{hwconfId}/{partitionName}/{key}
	 */
	@GET
	@Path("{hwconfId}/{partitionName}/{key}")
	@Produces(TEXT)
	@ApiOperation(value = "Gets the value of a key to a given partition." +
			      "The key may be: OS, Description, Join, Format, Itool" )
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Device not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	String getConfigurationValue(
			@Context UriInfo ui,
	        @Context HttpServletRequest req,
	        @PathParam("hwconfId") Long hwconfId,
	        @PathParam("partitionName") String partitionName,
	        @PathParam("key") String key
	);

	/*
	 * GET clonetool/roomsToRegister
	 */
	@GET
	@Path("roomsToRegister")
	@Produces(TEXT)
	@ApiOperation(value = "Gets a list of rooms to register." +
			      "The format is id name##id name" )
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Device not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("hwconf.manage")
	String getRoomsToRegister(
	        @ApiParam(hidden = true) @Auth Session session
	);

	/*
     * GET rooms/{roomId}/getAvailableIPAddresses
     */
    @GET
    @Path("rooms/{roomId}/availableIPAddresses")
    @Produces(TEXT)
    @ApiOperation(value = "Get count available ip-adresses of the room. The string list will contains the proposed name too: 'IP-Addres Proposed-Name'")
        @ApiResponses(value = {
        @ApiResponse(code = 404, message = "There is no more IP address in this room."),
        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed("hwconf.add")
    String getAvailableIPAddresses(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("roomId") long roomId
    );

    /*
     * PUT rooms/{roomId}/device/{macAddress}/{name}
     */
    @PUT
    @Path("rooms/{roomId}/{macAddress}/{IP}/{name}")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Create a new device. This api call can be used only for registering own devices.")
    @ApiResponses(value = {
            // TODO so oder anders? @ApiResponse(code = 404, message = "At least one device was not found"),
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @PermitAll
    CrxResponse addDevice(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("roomId") long roomId,
            @PathParam("macAddress") String macAddress,
            @PathParam("IP") String IP,
            @PathParam("name") String name
    );

	// POST and PUSH methodes.

	/*
	 * POST clonetool/ { hwconf hash }
	 */
	@POST
	@Path("hwconf")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Creates a new hardware configuration.")
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Device not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("hwconf.add")
	CrxResponse addHWConf(
	        @ApiParam(hidden = true) @Auth Session session,
	        HWConf hwconf
	);

	/**
	 * Import a list of ne hardware configurations.
	 * @param session
	 * @param hwconfs
	 * @return
	 */
	@POST
	@Path("hwconf/import")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Import a list of ne hardware configurations.")
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Device not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("hwconf.add")
	CrxResponse importHWConfs(
	        @ApiParam(hidden = true) @Auth Session session,
	        List<HWConf> hwconfs
	);

	/*
	 * Post clonetool/{hwconfId}
	 */
	@POST
	@Path("{hwconfId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Updates a hardware configuration.")
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Device not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("hwconf.add")
	CrxResponse modifyHWConf(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("hwconfId") Long hwconfId,
		    HWConf hwconf
	);

	/*
	 * PUT clonetool/{hwconfId}/{partition}
	 */
	@PUT
	@Path("{hwconfId}/{partitionName}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Create a new not configured partition to a given hardware configuration." +
						  "Only the name (sdaXXX) is given. The other parameter must be set with an other put calls." )
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Device not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("hwconf.manage")
	CrxResponse addPartition(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("hwconfId") Long hwconfId,
	        @PathParam("partitionName") String partitionName
	);

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
	CrxResponse addPartition(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("hwconfId") Long hwconfId,
	        Partition partition
	);

	/*
	 * PUT clonetool/{hwconfId}/{partitionName}/{key}/{value}
	 */
	@PUT
	@Path("{hwconfId}/{partitionName}/{key}/{value}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Sets the value of a key to a given partition." +
			      "The keys may be: OS, Description, Join, Format, Itool" )
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Device not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("hwconf.manage")
	CrxResponse setConfigurationValue(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("hwconfId") Long hwconfId,
	        @PathParam("partitionName") String partitionName,
	        @PathParam("key") String key,
	        @PathParam("value") String value
	);

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
	CrxResponse delete(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("hwconfId") Long hwconfId
	);

	/*
	 * DELETE clonetool/{hwconfId}/{partitionName}
	 */
	@DELETE
	@Path("{hwconfId}/{partitionName}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delets a partition to a given hardware configuration.")
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Device not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("hwconf.add")
	CrxResponse deletePartition(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("hwconfId") Long hwconfId,
	        @PathParam("partitionName") String partitionName
	);

	/*
	 * DELETE clonetool/{hwconfId}/{partitionName}/{key}
	 */
	@DELETE
	@Path("{hwconfId}/{partitionName}/{key}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delets a key an the correspondig value from a partition." +
			      "The key may be: OS, Description, Join, Format, Itool" )
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Device not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("hwconf.manage")
	CrxResponse deleteConfigurationValue(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("hwconfId") Long hwconfId,
	        @PathParam("partitionName") String partitionName,
	        @PathParam("key") String key
	);

	/*
	 *
	 */
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
	CrxResponse startCloning(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("hwconfId") Long hwconfId,
			Clone parameters
			);

	/*
	 *
	 */
	@PUT
	@Path("{hwconfId}/cloning/{multiCast}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Creates the boot configuration for the automatical partitioning for all workstations in a hwconf." +
						  "Multicast can be 0 or 1"
						)
	@RolesAllowed("hwconf.manage")
	CrxResponse startCloning(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("hwconfId")  Long hwconfId,
			@PathParam("multiCast") int multiCast
			);

	/*
	 *
	 */
	@PUT
	@Path("rooms/{roomId}/cloning/{multiCast}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Creates the boot configuration for the automatical partitioning for all workstations in a room." +
				  "Multicast can be 0 or 1"
						)
	@RolesAllowed("hwconf.manage")
	CrxResponse startCloningInRoom(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("roomId") Long roomId,
			@PathParam("multiCast") int multiCast
			);

	/*
	 *
	 */
	@PUT
	@Path("devices/{deviceId}/cloning")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Creates the boot configuration for the automatical partitioning for a workstations"
						)
	@RolesAllowed("hwconf.manage")
	CrxResponse startCloningOnDevice(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("deviceId") Long deviceId
			);


	/*
	 *
	 */
	@DELETE
	@Path("{hwconfId}/cloning")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Removes the boot configuration for the automatical partitioning.")
	@RolesAllowed("hwconf.manage")
	CrxResponse stopCloning(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("hwconfId") Long hwconfId
			);

	/*
	 *
	 */
	@DELETE
	@Path("rooms/{roomId}/cloning")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Removes the boot configuration for the automatical partitioning for all workstations in a room." )
	@RolesAllowed("hwconf.manage")
	CrxResponse stopCloningInRoom(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("roomId") Long roomId
			);

	/*
	 *
	 */
	@DELETE
	@Path("devices/{deviceId}/cloning")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Creates the boot configuration for the automatical partitioning for a workstations"
						)
	@RolesAllowed("hwconf.manage")
	CrxResponse stopCloningOnDevice(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("deviceId") Long deviceId
			);

	/*
	 * Delete the boot configurations for autocloning.
	 */
	@DELETE
	@Path("devicesByIP/{deviceIP}/cloning")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Deletes the boot configuration for the automatical partitioning for a workstations")
	@RolesAllowed("hwconf.manage")
	CrxResponse stopCloningOnDevice(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("deviceIP") String deviceIP
			);

	@GET
	@Path("multicastDevices")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the list of the network devices for multicast cloning.")
	@RolesAllowed("hwconf.manage")
	String[] getMulticastDevices(
			@ApiParam(hidden = true) @Auth Session session
			);

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
	CrxResponse startMulticast(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("partitionId") Long partitionId,
			@PathParam("networkDevice") String networkDevice
			);

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
	CrxResponse modifyPartition(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("partitionId") Long partitionId,
			Partition partition
			);

	/*
	 * Some anonyme calls
	 */
	/*
	 * Calls without authorization
	 */
	@GET
	@Path("hostName")
	@Produces(TEXT)
	@ApiOperation(value = "Gets the fully qualified host name of the requester.")
	@ApiResponses(value = {
	        @ApiResponse(code = 401, message = "No regcode was found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	String getHostname(
	        @Context UriInfo ui,
	        @Context HttpServletRequest req
	);


	@GET
	@Path("fqhn")
	@Produces(TEXT)
	@ApiOperation(value = "Gets the fully qualified host name of the requester.")
	@ApiResponses(value = {
	        @ApiResponse(code = 401, message = "No regcode was found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	String getFqhn(
	        @Context UriInfo ui,
	        @Context HttpServletRequest req
	);

	@GET
	@Path("domainName")
	@Produces(TEXT)
	@ApiOperation(value = "Gets the fully qualified host name of the requester.")
	@ApiResponses(value = {
	        @ApiResponse(code = 401, message = "No regcode was found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	String getDomainName(
	        @Context UriInfo ui,
	        @Context HttpServletRequest req
	);

	/*
	 * Get clonetool/isMaster
	 */
	@GET
	@Path("isMaster")
	@Produces(TEXT)
	@ApiOperation(value = "Returns 'true' if the workstation of the session is master. Returns empty if not.")
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Device not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	String isMaster(
	        @Context UriInfo ui,
	        @Context HttpServletRequest req
	);
}
