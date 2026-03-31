/* (c) 2020 Péter Varkoly <peter@varkoly.de> - all rights reserved */
/* (c) 2018 EXTIS GmbH - all rights reserved */
package de.cranix.api.resources;

import static de.cranix.api.resources.Resource.*;

import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.ws.rs.*;

import de.cranix.dao.CrxResponse;
import de.cranix.dao.Session;
import de.cranix.dao.SupportRequest;
import de.cranix.helper.CrxEntityManagerFactory;
import de.cranix.services.SupportService;

import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.List;


@Produces(JSON_UTF8)
@Path("support")
@Api(value = "support")
public class SupportResource {

    public SupportResource() {
    }

    @POST
    @Path("create")
    @ApiOperation(value = "Create a support request.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Missing data for request"),
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")})
    @RolesAllowed({"system.support"})
    public CrxResponse create(
            @ApiParam(hidden = true) @Auth Session session,
            SupportRequest supportRequest) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new SupportService(session, em).create(supportRequest);
        em.close();
        return resp;
    }

	@GET
	@Path("{status}")
	@ApiOperation(value = "Get all tickets corresponding to the institute and the session user")
	@RolesAllowed({"system.support"})
	public Object getAll(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("status") String status

	){
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		Object resp = new SupportService(session, em).getAll(status);
		em.close();
		return resp;
	}

	@GET
	@Path("tickets/{ticketId}")
	@ApiOperation(value = "Get all articles corresponding to the ticket.")
	@RolesAllowed({"system.support"})
	public Object getAll(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("ticketId") Long ticketId

	){
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		Object resp = new SupportService(session, em).getArticle(ticketId);
		em.close();
		return resp;
	}

	@POST
	@Path("tickets/{ticketId}")
	@ApiOperation(value = "Creates an article to an ticket.")
	@RolesAllowed({"system.support"})
	public Object getAll(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("ticketId") Long ticketId,
			Object article
	){
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		Object resp = new SupportService(session, em).addArticle(ticketId, article);
		em.close();
		return resp;
	}
}
