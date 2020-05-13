 /* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.api.resources;

import static de.cranix.api.resources.Resource.*;
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
import de.cranix.dao.Clone;
import de.cranix.dao.HWConf;
import de.cranix.dao.CrxActionMap;
import de.cranix.dao.CrxResponse;
import de.cranix.dao.Partition;
import de.cranix.dao.Session;
import java.util.List;

@Path("hwconfs")
@Api(value = "hwconfs")
public interface HwconfResource {


	/**
	 * Delivers the list of all hwconfs
	 * @param session
	 * @return
	 */
	@GET
	@Path("all")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets all hardware configuration.")
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Device not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("hwconf.manage")
	List<HWConf> getAllHWConf(
	        @ApiParam(hidden = true) @Auth Session session
	);

	/**
	 * Delivers a hwconf by id
	 * @param session
	 * @param hwconfId
	 * @return
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

	/**
	 * Delivers the id of the master of the hwconf
	 * @param session
	 * @param hwconfId
	 * @return
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

	/**
	 * Creates a new hwconf
	 * @param session
	 * @param hwconf
	 * @return
	 */
	@POST
	@Path("add")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Creates a new hardware configuration.")
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Device not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("hwconf.add")
	CrxResponse add(
	        @ApiParam(hidden = true) @Auth Session session,
	        HWConf hwconf
	);

	/**
	 * Import a list of a hardware configurations.
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

	/**
	 * Updates a hwconf
	 * @param session
	 * @param hwconfId
	 * @param hwconf
	 * @return
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

	/**
	 * Creates a new partition with all parameter in a hwconf 
	 * @param session
	 * @param hwconfId
	 * @param partition
	 * @return
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

	/**
	 * Removes a hwconf
	 * @param session
	 * @param hwconfId
	 * @return
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


	/**
	 * Start recovering of selected devices in a hwconf
	 * @param session
	 * @param hwconfId
	 * @param parameters
	 * @return
	 */
	@POST
	@Path("{hwconfId}/recover")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Creates the boot configuration for the automatical partitioning." +
						"This call have to provide a hash with following informations" +
						" devices    : [ IDs of devices ] " +
						" partitions : [ IDs of partitions ] " +
						" multicast  :  true/fals"
						)
	@RolesAllowed("hwconf.manage")
	CrxResponse startRecover(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("hwconfId") Long hwconfId,
			Clone parameters
			);

	/**
	 * Start recovering of all devices in a hwconf
	 * @param session
	 * @param hwconfId
	 * @param multiCast
	 * @return
	 */
	@PUT
	@Path("{hwconfId}/recover/{multiCast}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Creates the boot configuration for the automatical partitioning for all workstations in a hwconf." +
						  "Multicast can be 0 or 1"
						)
	@RolesAllowed("hwconf.manage")
	CrxResponse startRecover(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("hwconfId")  Long hwconfId,
			@PathParam("multiCast") int multiCast
			);

	/**
	 * Removes the boot configurations for a hwconf
	 * @param session
	 * @param hwconfId
	 * @return
	 */
	@DELETE
	@Path("{hwconfId}/recover")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Removes the boot configuration for the automatical partitioning.")
	@RolesAllowed("hwconf.manage")
	CrxResponse stopRecover(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("hwconfId") Long hwconfId
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
	/**
	 * Apply actions on a list of hwconfs.
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
	@RolesAllowed("cephalix.manage")
	CrxResponse applyAction(
			@ApiParam(hidden = true) @Auth Session session,
			CrxActionMap actionMap
			);

}
