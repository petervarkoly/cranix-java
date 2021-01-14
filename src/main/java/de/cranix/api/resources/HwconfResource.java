 /* (c) 2020 Péter Varkoly <pvarkoly@cephalix.eu> - all rights reserved */
package de.cranix.api.resources;

import static de.cranix.api.resources.Resource.*;

import java.util.List;
import java.util.ArrayList;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.ws.rs.WebApplicationException;
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
import de.cranix.services.CloneToolService;
import de.cranix.helper.CrxEntityManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("hwconfs")
@Api(value = "hwconfs")
public class HwconfResource {

	Logger logger = LoggerFactory.getLogger(HwconfResource.class);

	public HwconfResource() { }

	@GET
	@Path("all")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets all hardware configuration.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "Device not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@PermitAll
	public List<HWConf> getAll( @ApiParam(hidden = true) @Auth Session session) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<HWConf> resp = new CloneToolService(session,em).getAllHWConf();
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
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		final HWConf hwconf = new CloneToolService(session,em).getById(hwconfId);
		em.close();
		if (hwconf == null) {
			throw new WebApplicationException(404);
		}
		return hwconf;
	}

	@POST
	@Path("add")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Creates a new hardware configuration.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "Device not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("hwconf.add")
	public CrxResponse add(
		@ApiParam(hidden = true) @Auth Session session,
		HWConf hwconf
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new CloneToolService(session,em).addHWConf(hwconf);
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
	@RolesAllowed("hwconf.modify")
	public CrxResponse modify(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("hwconfId") Long hwconfId,
		HWConf hwconf
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new CloneToolService(session,em).modifyHWConf(hwconfId, hwconf);
		em.close();
		return resp;
	}

	@POST
	@Path("import")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Import a list of ne hardware configurations.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "Device not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("hwconf.add")
	public CrxResponse importHWConfs(
		@ApiParam(hidden = true) @Auth Session session,
		List<HWConf> hwconfs
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CloneToolService  cloneToolService = new CloneToolService(session,em);
		CrxResponse crxResponse = null;
		for( HWConf hwconf : hwconfs ) {
			crxResponse = cloneToolService.addHWConf(hwconf);
			if( crxResponse.getCode().equals("ERROR")) {
				break;
			}
		}
		em.close();
		return crxResponse;
	}

	@POST
	@Path("{hwconfId}/addPartition")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Create a new partition to a given hardware configuration.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "Device not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("hwconf.modify")
	public CrxResponse addPartition(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("hwconfId") Long hwconfId,
		Partition partition
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new CloneToolService(session,em).addPartitionToHWConf(hwconfId, partition);
		em.close();
		return resp;
	}

	@DELETE
	@Path("{hwconfId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Updates a hardware configuration.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "Device not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("hwconf.delete")
	public CrxResponse delete(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("hwconfId") Long hwconfId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new CloneToolService(session,em).delete(hwconfId);
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
	@RolesAllowed("hwconf.modify")
	public CrxResponse deletePartition(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("hwconfId") Long hwconfId,
		@PathParam("partitionName") String partitionName
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new CloneToolService(session,em).deletePartition(hwconfId,partitionName);
		em.close();
		return resp;
	}


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
	public CrxResponse startRecover(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("hwconfId") Long hwconfId,
		Clone parameters
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new CloneToolService(session,em).startCloning(hwconfId,parameters);
		em.close();
		return resp;
	}

	@PUT
	@Path("{hwconfId}/recover/{multiCast}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Creates the boot configuration for the automatical partitioning for all workstations in a hwconf." +
			  "Multicast can be 0 or 1"
	)
	@RolesAllowed("hwconf.manage")
	public CrxResponse startRecover(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("hwconfId")  Long hwconfId,
		@PathParam("multiCast") int multiCast
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new CloneToolService(session,em).startCloning("hwconf", hwconfId, multiCast);
		em.close();
		return resp;
	}

	@DELETE
	@Path("{hwconfId}/recover")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Removes the boot configuration for the automatical partitioning.")
	@RolesAllowed("hwconf.manage")
	public CrxResponse stopRecover(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("hwconfId") Long hwconfId
	) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                CrxResponse resp = new CloneToolService(session,em).stopCloning("hwconf",hwconfId);
                em.close();
                return resp;
        }

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
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                CrxResponse resp = new CloneToolService(session,em).startMulticast(partitionId,networkDevice);
                em.close();
                return resp;
        }

	@POST
	@Path("partitions/{partitionId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Sets the parameters of an existing partition.")
	@RolesAllowed("hwconf.modify")
	public CrxResponse modifyPartition(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("partitionId") Long partitionId,
		Partition partition
	) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                CrxResponse resp = new CloneToolService(session,em).modifyPartition(partitionId, partition);
                em.close();
                return resp;
        }

	@POST
	@Path("applyAction")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Apply actions on selected hwconfs.")
	@ApiResponses(value = {
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("hwconf.manage")
	public List<CrxResponse> applyAction(
		@ApiParam(hidden = true) @Auth Session session,
		CrxActionMap crxActionMap
	) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                List<CrxResponse> responses = new ArrayList<CrxResponse>();
                CloneToolService  cloneToolService = new CloneToolService(session,em);
                logger.debug(crxActionMap.toString());
                for( Long id : crxActionMap.getObjectIds() ) {
                        switch(crxActionMap.getName().toLowerCase()) {
                                case "delete":  responses.add(cloneToolService.delete(id));
                                                break;
/*                                case "cleanup": responses.add(cloneToolService.cleanUp(id));
                                                break;*/
                                case "startclone": responses.add(cloneToolService.startCloning("hwconf",id,0));
                                                break;
                                case "stopclone": responses.add(cloneToolService.stopCloning("hwconf",id));
                                                break;
                        }
                }
                em.close();
                return responses;

        }

}
