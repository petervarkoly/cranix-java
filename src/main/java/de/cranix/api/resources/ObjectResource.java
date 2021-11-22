/* (c) 2021 Péter Varkoly <pvarkoly@cephalix.eu> - all rights reserved */
package de.cranix.api.resources;

import static de.cranix.api.resources.Resource.*;

import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.ws.rs.*;

import de.cranix.dao.CrxConfig;
import de.cranix.dao.CrxMConfig;
import de.cranix.dao.CrxResponse;
import de.cranix.dao.Session;
import de.cranix.helper.CrxEntityManagerFactory;
import de.cranix.services.Service;

import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.List;

@Path("objects")
@Api(value = "objects")
public class ObjectResource {

	public ObjectResource() {}

	@POST
	@Path("mconfig")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Create a new mconfig for an object request.")
	@ApiResponses(value = {
		@ApiResponse(code = 400, message = "Missing data for request"),
		@ApiResponse(code = 500, message = "Server broken, please contact administrator") })
	@RolesAllowed("objects.manage")
	public CrxResponse createMconfig(
			@ApiParam(hidden = true) @Auth Session session,
			CrxMConfig mConfig)
       	{
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new Service(session,em).addMConfig(mConfig);
		em.close();
		return resp;
	}

	@POST
	@Path("config")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Create a new mconfig for an object request.")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Missing data for request"),
			@ApiResponse(code = 500, message = "Server broken, please contact administrator") })
	@RolesAllowed("objects.manage")
	public CrxResponse createConfig(
			@ApiParam(hidden = true) @Auth Session session,
			CrxConfig config)
	{
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new Service(session,em).addConfig(config);
		em.close();
		return resp;
	}

	@DELETE
	@Path("mconfig")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Create a new mconfig for an object request.")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Missing data for request"),
			@ApiResponse(code = 500, message = "Server broken, please contact administrator") })
	@RolesAllowed("objects.manage")
	public CrxResponse deleteMconfig(
			@ApiParam(hidden = true) @Auth Session session,
			CrxMConfig mConfig)
	{
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new Service(session,em).deleteMConfig(mConfig);
		em.close();
		return resp;
	}

	@DELETE
	@Path("config")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Create a new mconfig for an object request.")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Missing data for request"),
			@ApiResponse(code = 500, message = "Server broken, please contact administrator") })
	@RolesAllowed("objects.manage")
	public CrxResponse deleteConfig(
			@ApiParam(hidden = true) @Auth Session session,
			CrxConfig config)
	{
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new Service(session,em).deleteConfig(config);
		em.close();
		return resp;
	}

	@GET
	@Path("mconfig/{type}/{id}/{key}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the values of mconfig for an object.")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Missing data for request"),
			@ApiResponse(code = 500, message = "Server broken, please contact administrator") })
	@RolesAllowed("objects.manage")
	public List<CrxMConfig> getMconfig(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("type") String type,
			@PathParam("id") Long id,
			@PathParam("key") String key)
	{
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<CrxMConfig> resp = new Service(session,em).getMConfigs(type,id,key);
		em.close();
		return resp;
	}

	@GET
	@Path("config/{type}/{id}/{key}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the values of mconfig for an object.")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Missing data for request"),
			@ApiResponse(code = 500, message = "Server broken, please contact administrator") })
	@RolesAllowed("objects.manage")
	public CrxConfig getConfig(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("type") String type,
			@PathParam("id") Long id,
			@PathParam("key") String key)
	{
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxConfig resp = new Service(session,em).getConfig(type,id,key);
		em.close();
		return resp;
	}
}
