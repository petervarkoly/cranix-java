package de.cranix.api.resources;

import de.cephalix.services.CephalixService;
import de.cranix.services.CrxNoticeService;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.*;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.persistence.EntityManager;
import de.cranix.helper.CrxEntityManagerFactory;
import de.cranix.dao.*;

import java.util.List;

import static de.cranix.api.resources.Resource.JSON_UTF8;

@Path("crxNotices")
@Api(value = "crxNotices")
@Produces(JSON_UTF8)
public class CrxNoticeResource {

    @POST
    @ApiOperation(value = "Adds a notice.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
    @RolesAllowed("notice.use")
    public CrxResponse add(
            @ApiParam(hidden = true) @Auth Session session,
            CrxNotice notice
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse response = new CrxNoticeService(session, em).add(notice);
        em.close();
        return response;
    }

    @GET
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Gets all owned notices.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
    @RolesAllowed("notice.use")
    public List<CrxNotice> get(
            @ApiParam(hidden = true) @Auth Session session
    )
    {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        List<CrxNotice> response = new CrxNoticeService(session,em).get();
        em.close();
        return response;
    }

    @POST
    @Path("filter")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Gets all owned notices.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
    @RolesAllowed("notice.use")
    public List<CrxNotice> getByFilter(
            @ApiParam(hidden = true) @Auth Session session,
            CrxNotice filter
    )
    {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        List<CrxNotice> response = new CrxNoticeService(session,em).getByFilter(filter);
        em.close();
        return response;
    }

    @GET
    @Path("{noticeId}")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Deletes a notice.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
    @RolesAllowed("notice.use")
    public CrxNotice getById(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("noticeId") Long noticeId
    )
    {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxNotice response = new CrxNoticeService(session,em).getById(noticeId);
        em.close();
        return response;
    }

    @DELETE
    @Path("{noticeId}")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Deletes a notice.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
    @RolesAllowed("notice.use")
    public CrxResponse delete(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("noticeId") Long noticeId
    )
    {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse response = new CrxNoticeService(session,em).remove(noticeId);
        em.close();
        return response;
    }

    @PATCH
    @ApiOperation(value = "Modify a notice.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
    @RolesAllowed("notice.use")
    public CrxResponse patch(
            @ApiParam(hidden = true) @Auth Session session,
            CrxNotice notice
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse response = new CrxNoticeService(session, em).patch(notice);
        em.close();
        return response;
    }
}
