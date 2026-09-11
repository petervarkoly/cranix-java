package de.cranix.api.resources;

import de.cranix.services.CrxNoteService;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.*;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.persistence.EntityManager;
import de.cranix.helper.CrxEntityManagerFactory;
import de.cranix.dao.*;

import java.util.List;

import static de.cranix.api.resources.Resource.JSON_UTF8;

@Path("{path: (?i)crxnotes}")
@Api(value = "crxNotes")
@Produces(JSON_UTF8)
public class CrxNoteResource {

    @POST
    @ApiOperation(value = "Adds a note.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
    @RolesAllowed("crxnote.use")
    public CrxResponse add(
            @ApiParam(hidden = true) @Auth Session session,
            CrxNote note
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse response = new CrxNoteService(session, em).add(note);
        em.close();
        return response;
    }

    @GET
    @ApiOperation(value = "Gets all owned notes.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
    @RolesAllowed("crxnote.use")
    public List<CrxNote> get(
            @ApiParam(hidden = true) @Auth Session session
    )
    {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        List<CrxNote> response = new CrxNoteService(session,em).get();
        em.close();
        return response;
    }

    @POST
    @Path("filter")
    @ApiOperation(value = "Gets all owned notes.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
    @RolesAllowed("crxnote.use")
    public List<CrxNote> getByFilter(
            @ApiParam(hidden = true) @Auth Session session,
            CrxNote filter
    )
    {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        List<CrxNote> response = new CrxNoteService(session,em).getByFilter(filter);
        em.close();
        return response;
    }

    @GET
    @Path("{noteId}")
    @ApiOperation(value = "Gets a note.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
    @RolesAllowed("crxnote.use")
    public CrxNote getById(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("crxnoteId") Long noteId
    )
    {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxNote response = new CrxNoteService(session,em).getById(noteId);
        em.close();
        return response;
    }

    @DELETE
    @Path("{noteId}")
    @ApiOperation(value = "Deletes a note.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
    @RolesAllowed("crxnote.use")
    public CrxResponse delete(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("crxnoteId") Long noteId
    )
    {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse response = new CrxNoteService(session,em).remove(noteId);
        em.close();
        return response;
    }

    @PATCH
    @ApiOperation(value = "Modify a note.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
    @RolesAllowed("crxnote.use")
    public CrxResponse patch(
            @ApiParam(hidden = true) @Auth Session session,
            CrxNote note
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse response = new CrxNoteService(session, em).patch(note);
        em.close();
        return response;
    }
}