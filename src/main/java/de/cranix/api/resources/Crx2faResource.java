package de.cranix.api.resources;


import de.cranix.dao.Crx2fa;
import de.cranix.dao.Crx2faSession;
import de.cranix.dao.CrxResponse;
import de.cranix.dao.Session;
import de.cranix.helper.CrxEntityManagerFactory;
import de.cranix.services.Crx2faService;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;

import java.util.List;
import java.util.Map;

import static de.cranix.api.resources.Resource.JSON_UTF8;
import static de.cranix.api.resources.Resource.TEXT;

@Path("2fa")
@Api(value = "2fa")
@Produces(JSON_UTF8)
public class Crx2faResource {

    Logger logger = LoggerFactory.getLogger(Crx2faResource.class);

    public Crx2faResource() {
    }

    @POST
    @RolesAllowed("2fa.use")
    CrxResponse add(
            @ApiParam(hidden = true) @Auth Session session,
            Crx2fa crx2fa
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new Crx2faService(session,em).add(crx2fa);
        em.close();
        return resp;
    }

    @PATCH
    @RolesAllowed("2fa.use")
    CrxResponse add(
            @ApiParam(hidden = true) @Auth Session session,
            Crx2fa crx2fa
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new Crx2faService(session,em).modify(crx2fa);
        em.close();
        return resp;
    }
    @GET
    @RolesAllowed("2fa.use")
    Crx2fa get(
            @ApiParam(hidden = true) @Auth Session session,
            Crx2fa crx2fa
    ){
        return  session.getUser().getCrx2fa();
    }

    @DELETE
    @Path("{id}")
    @RolesAllowed("2fa.use")
    CrxResponse delete(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("id") Long id
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new Crx2faService(session,em).delete(id);
        em.close();
        return resp;
    }

    /* Functions to handle CRANIX 2FA sessions */
    @POST
    @Path("sessions")
    @RolesAllowed("2fa.use")
    Crx2faSession addSession(
            @ApiParam(hidden = true) @Auth Session session,
            @Context HttpServletRequest req,
            Crx2fa crx2fa
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        Crx2faSession resp = new Crx2faService(session,em).addSession(crx2fa, req.getRemoteAddr());
        em.close();
        return resp;
    }

    @POST
    @Path("sessions")
    @RolesAllowed("2fa.use")
    Crx2faSession checkSession(
            @ApiParam(hidden = true) @Auth Session session,
            @Context HttpServletRequest req,
            Crx2faSession crx2faSession

    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        Crx2faSession resp = new Crx2faService(session,em).checkSession(crx2faSession, req.getRemoteAddr());
        em.close();
        return resp;
    }

    @POST
    @Path("checkpin")
    Crx2faSession checkPin(
            @Context HttpServletRequest req,
            Map<String ,String> pinCheck
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        Crx2faSession res = new Crx2faService(null,em).checkPin(req.getRemoteAddr(),pinCheck.get("pin"),pinCheck.get("token"));
        em.close();
        if(res == null) {
            throw new WebApplicationException(401);
        }
        return res;
    }
}
