package de.cranix.api.resources;

import de.cranix.dao.*;
import de.cranix.helper.CrxEntityManagerFactory;
import de.cranix.services.CrxNoticeService;
import de.cranix.services.CrxTicketService;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.*;

import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.ws.rs.*;

import java.util.List;

import static de.cranix.api.resources.Resource.JSON_UTF8;

@Path("crxTickets")
@Api(value = "crxTickets")
@Produces(JSON_UTF8)
public class CrxTicketResource {

    @POST
    @ApiOperation(value = "Adds a ticket.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
    @RolesAllowed({"crxticket.worker","crxticket.use","crxticket.manager"})
    public CrxResponse add(
            @ApiParam(hidden = true) @Auth Session session,
            CrxTicket ticket
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse response = new CrxTicketService(session, em).add(ticket);
        em.close();
        return response;
    }

    @DELETE
    @ApiOperation(value = "Closes a ticket")
    @Path("{id}")
    @RolesAllowed({"crxticket.worker","crxticket.use","crxticket.manager"})
    public CrxResponse close(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("id") Long id
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse response = new CrxTicketService(session, em).close(id);
        em.close();
        return response;
    }

    @DELETE
    @ApiOperation(value = "Deletes a ticket")
    @Path("{id}/delete")
    @RolesAllowed({"crxticket.manager"})
    public CrxResponse delete(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("id") Long id
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse response = new CrxTicketService(session, em).delete(id);
        em.close();
        return response;
    }

    @GET
    @ApiOperation(value = "Gets the article of a ticket")
    @Path("{id}")
    @RolesAllowed({"crxticket.worker","crxticket.use","crxticket.manager"})
    public List<CrxTicketArticle> getArticles(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("id") Long id
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxTicket ticket = new CrxTicketService(session, em).getById(id);
        em.close();
        return ticket.getCrxTicketArticleList();
    }

    @GET
    @ApiOperation(value = "Gets the created tickets of a user in the states represented by the string status.")
    @RolesAllowed({"crxticket.worker","crxticket.use","crxticket.manager"})
    @Path("created/{status}")
    public List<CrxTicket> getMyTickets(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("status") String status
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        List<CrxTicket> response = new CrxTicketService(session, em).getMyTickets(status);
        em.close();
        return response;
    }

    @GET
    @ApiOperation(value = "Gets the tickets of a user in the states represented by the string status.")
    @Path("my/{status}")
    @RolesAllowed({"crxticket.worker","crxticket.use","crxticket.manager"})
    public List<CrxTicket> getTicketsForMe(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("status") String status
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        List<CrxTicket> response = new CrxTicketService(session, em).getTicketsForMe(status);
        em.close();
        return response;
    }

    @POST
    @ApiOperation("Add an article to a ticket")
    @Path("{id}")
    @RolesAllowed({"crxticket.worker","crxticket.use","crxticket.manager"})
    public CrxResponse addArticle(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("id") Long id,
            CrxTicketArticle article
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse response = new CrxTicketService(session, em).addArticle(id, article);
        em.close();
        return response;
    }

    @GET
    @Path("status")
    @ApiOperation("Returns the status of the tickets concerning the session user.")
    @RolesAllowed({"crxticket.worker","crxticket.use","crxticket.manager"})
    public Object getStatus(
            @ApiParam(hidden = true) @Auth Session session
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        Object response = new CrxTicketService(session, em).getStatus();
        em.close();
        return response;
    }

    @PUT
    @ApiOperation("Assigns a ticket to a user")
    @Path("{ticketId}/{userId}")
    @RolesAllowed({"crxticket.worker","crxticket.manager"})
    public CrxResponse assigne(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("ticketId") Long ticketId,
            @PathParam("userId") Long userId
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse response = new CrxTicketService(session, em).assignTicket(ticketId,userId);
        em.close();
        return response;
    }
}
