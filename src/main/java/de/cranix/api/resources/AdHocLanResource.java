/* (c) 2022 Péter Varkoly <peter@varkoly.de> - all rights reserved */
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
import javax.ws.rs.POST;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.List;

import de.cranix.dao.*;
import de.cranix.services.*;
import de.cranix.helper.CrxEntityManagerFactory;

@Path("adhocrooms")
@Api(value = "adhocrooms")
@Produces(JSON_UTF8)
public class AdHocLanResource {

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
	@ApiOperation(
			value = "Gets all defined AdHocLan Rooms. " +
				"Normal user has access on AdHocLan rooms over the SelfManagement resource."
	)
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "No room was found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")
	})
	@RolesAllowed({"adhoclan.search","adhoclan.manage"})
	public List<AdHocRoom> getAll( @ApiParam(hidden = true) @Auth Session session) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<AdHocRoom> resp = new AdHocLanService(session,em).getAll();
		em.close();
		return resp;
	}

	@POST
	@Path("add")
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
	@ApiOperation(value = "Delets a whole adhoc room inclusive user and devices.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "No room was found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("adhoclan.manage")
	public CrxResponse delete(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("id")		Long id
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new RoomService(session,em).delete(id, true);
		em.close();
		return resp;
	}

	@GET
	@Path("{id}")
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
}
