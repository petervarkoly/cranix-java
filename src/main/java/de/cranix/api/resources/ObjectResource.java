/* (c) 2021 Péter Varkoly <pvarkoly@cephalix.eu> - all rights reserved */
package de.cranix.api.resources;

import static de.cranix.api.resources.Resource.*;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.ws.rs.*;

import de.cranix.dao.*;
import de.cranix.helper.CrxEntityManagerFactory;
import de.cranix.services.Service;

import de.cranix.services.SubjectAreaService;
import de.cranix.services.TeachingSubjectService;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.List;

@Path("objects")
@Api(value = "objects")
@Produces(JSON_UTF8)
public class ObjectResource {

	public ObjectResource() {}

	@POST
	@Path("mconfig")
	@ApiOperation(value = "Creates a new mconfig for an object.")
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
	@ApiOperation(value = "Creates a new config for an object.")
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
	@Path("mconfig/{id}")
	@ApiOperation(value = "Deletes a mconfig object.")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Missing data for request"),
			@ApiResponse(code = 500, message = "Server broken, please contact administrator") })
	@RolesAllowed("objects.manage")
	public CrxResponse deleteMconfig(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("id") Long id)
	{
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new Service(session,em).deleteMConfig(id);
		em.close();
		return resp;
	}

	@DELETE
	@Path("config/{id}")
	@ApiOperation(value = "Deletes a config object.")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Missing data for request"),
			@ApiResponse(code = 500, message = "Server broken, please contact administrator") })
	@RolesAllowed("objects.manage")
	public CrxResponse deleteConfig(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("id") Long id)
	{
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new Service(session,em).deleteConfig(id);
		em.close();
		return resp;
	}

	@GET
	@Path("mconfig/{type}/{id}/{key}")
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
	@ApiOperation(value = "Gets a config for an object.")
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

	@GET
	@Produces(TEXT)
	@Path("config/{type}/{id}/{key}/value")
	@ApiOperation(value = "Gets the value of a config for an object.")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Missing data for request"),
			@ApiResponse(code = 500, message = "Server broken, please contact administrator") })
	@RolesAllowed("objects.manage")
	public String getConfigValue(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("type") String type,
			@PathParam("id") Long id,
			@PathParam("key") String key)
	{
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		String resp = new Service(session,em).getConfig(type,id,key).getValue();
		em.close();
		return resp;
	}

	@DELETE
	@Path("config/{type}/{id}/{key}")
	@ApiOperation(value = "Deletes the a config of an object.")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Missing data for request"),
			@ApiResponse(code = 500, message = "Server broken, please contact administrator") })
	@RolesAllowed("objects.manage")
	public CrxResponse deleteConfig(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("type") String type,
			@PathParam("id") Long id,
			@PathParam("key") String key)
	{
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new Service(session,em).deleteConfig(type,id,key);
		em.close();
		return resp;
	}

	@PUT
	@Path("config/{type}/{id}/{key}/{value}")
	@ApiOperation(value = "Sets the config value of an object. If the config does not exist this will be created." +
			"Else this object will be created. ")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Missing data for request"),
			@ApiResponse(code = 500, message = "Server broken, please contact administrator") })
	@RolesAllowed("objects.manage")
	public CrxResponse setConfig(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("type") String type,
			@PathParam("id") Long id,
			@PathParam("key") String key,
			@PathParam("value") String value)
	{
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new Service(session,em).setConfig(type,id,key,value);
		em.close();
		return resp;
	}

	/*
	Functions to manage teaching subjects and areas
	 */
	@GET
	@Path("subjects")
	@ApiOperation(value = "Gets the values of mconfig for an object.")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Missing data for request"),
			@ApiResponse(code = 500, message = "Server broken, please contact administrator") })
	@PermitAll
	public List<TeachingSubject> getSubjects(
			@ApiParam(hidden = true) @Auth Session session)
	{
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<TeachingSubject> resp = new TeachingSubjectService(session,em).getAll();
		em.close();
		return resp;
	}

	@POST
	@Path("subjects")
	@ApiOperation(value = "Add a teaching subject.")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Missing data for request"),
			@ApiResponse(code = 500, message = "Server broken, please contact administrator") })
	@RolesAllowed("subject.manage")
	public CrxResponse addSubject(
			@ApiParam(hidden = true) @Auth Session session,
			TeachingSubject teachingSubject)
	{
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new TeachingSubjectService(session,em).add(teachingSubject);
		em.close();
		return resp;
	}

	@PATCH
	@Path("subjects")
	@ApiOperation(value = "Modify a teaching subject.")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Missing data for request"),
			@ApiResponse(code = 500, message = "Server broken, please contact administrator") })
	@RolesAllowed("subject.manage")
	public CrxResponse modifySubject(
			@ApiParam(hidden = true) @Auth Session session,
			TeachingSubject teachingSubject)
	{
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new TeachingSubjectService(session,em).modify(teachingSubject);
		em.close();
		return resp;
	}

	@DELETE
	@Path("subjects/{id}")
	@ApiOperation(value = "Gets the subject areas of a teaching subject.")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Missing data for request"),
			@ApiResponse(code = 500, message = "Server broken, please contact administrator") })
	@PermitAll
	public CrxResponse deleteSubjectAreas(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("id") Long id
	)
	{
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new TeachingSubjectService(session,em).delete(id);
		em.close();
		return resp;
	}

	@POST
	@Path("subjects/{id}")
	@ApiOperation(value = "Add a subject area to a teaching subject.")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Missing data for request"),
			@ApiResponse(code = 500, message = "Server broken, please contact administrator") })
	@RolesAllowed("subject.manage")
	public CrxResponse addSubjectArea(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("id") Long id,
			SubjectArea subjectArea)
	{
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new SubjectAreaService(session,em).add(id,subjectArea);
		em.close();
		return resp;
	}

	@PATCH
	@Path("subjects/areas")
	@ApiOperation(value = "Add a subject area to a teaching subject.")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Missing data for request"),
			@ApiResponse(code = 500, message = "Server broken, please contact administrator") })
	@RolesAllowed("subject.manage")
	public CrxResponse addSubjectArea(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("id") Long id,
			SubjectArea subjectArea)
	{
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new SubjectAreaService(session,em).modify(subjectArea);
		em.close();
		return resp;
	}

	@DELETE
	@Path("subjects/areas/{id}")
	@ApiOperation(value = "Add a subject area to a teaching subject.")
	@ApiResponses(value = {
			@ApiResponse(code = 400, message = "Missing data for request"),
			@ApiResponse(code = 500, message = "Server broken, please contact administrator") })
	@RolesAllowed("subject.manage")
	public CrxResponse deleteSubjectArea(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("id") Long id)
	{
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new SubjectAreaService(session,em).delete(id);
		em.close();
		return resp;
	}
}
