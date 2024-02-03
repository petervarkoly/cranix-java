package de.cranix.api.resources;


import com.fasterxml.jackson.databind.ObjectMapper;
import de.cranix.dao.*;
import de.cranix.helper.CrxEntityManagerFactory;
import de.cranix.services.Config;
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
import static de.cranix.helper.CranixConstants.cranix2faConfig;

@Path("2fas")
@Api(value = "2fas")
@Produces(JSON_UTF8)
public class Crx2faResource {

    Logger logger = LoggerFactory.getLogger(Crx2faResource.class);

    public Crx2faResource() {
    }



    /**
     * Delivers a CRANIX 2FA to be able to scan it.
     * @param session
     * @param crx2fa
     * @return
     */
    @GET
    @Path("types")
    @RolesAllowed("2fa.use")
    public String[] getTypes(
            @ApiParam(hidden = true) @Auth Session session
    ){
        Config config = new Config(cranix2faConfig,"CRX2FA_");
        String[] resp = config.getConfigValue("TYPES").split(" ");
        return  resp;
    }

    /**
     * Delivers all CRX2FAs for the sysadmins.
     * @param session
     * @return
     */
    @GET
    @Path("all")
    @RolesAllowed("2fa.manage")
    public List<Crx2fa> getAll(
            @ApiParam(hidden = true) @Auth Session session
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        List<Crx2fa> resp = new Crx2faService(session,em).getAll();
        em.close();
        return resp;
    }

    /**
     * Create a new CRANIX 2FA configuration. The user can only set the timStep. How long an OTP is valid.
     * @param session
     * @param crx2fa
     * @return
     */
    @POST
    @RolesAllowed("2fa.use")
    public CrxResponse add(
            @ApiParam(hidden = true) @Auth Session session,
            Crx2fa crx2fa
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new Crx2faService(session,em).add(crx2fa);
        em.close();
        return resp;
    }

    /**
     * Update the crx2fa. Basicaly only the live time of a session can be modified
     * @param session
     * @param crx2fa
     * @return
     */
    @PATCH
    @RolesAllowed({"2fa.use","2fa.manage"})
    public CrxResponse modify(
            @ApiParam(hidden = true) @Auth Session session,
            Crx2fa crx2fa
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new Crx2faService(session,em).modify(crx2fa);
        em.close();
        return resp;
    }

    /**
     * Delivers the users CRX2FAs
     * @param session
     * @param crx2fa
     * @return
     */
    @GET
    @RolesAllowed("2fa.use")
    public List<Crx2fa> get(
            @ApiParam(hidden = true) @Auth Session session
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        List<Crx2fa> resp = new Crx2faService(session,em).getMyCrx2fas();
        em.close();
        return resp;
    }

    /**
     * Deletes a CRANIX 2fa configuration
     * @param session
     * @param id
     * @return
     */
    @DELETE
    @Path("{id}")
    @RolesAllowed({"2fa.use","2fa.manage"})
    public CrxResponse delete(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("id") Long id
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new Crx2faService(session,em).delete(id);
        em.close();
        return resp;
    }

    /**
     * Resets the bad login counter of a CRANIX 2fa configuration
     * @param session
     * @param id
     * @return
     */
    @PUT
    @Path("{id}")
    @RolesAllowed({"2fa.manage"})
    public CrxResponse reset(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("id") Long id
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new Crx2faService(session,em).reset(id);
        em.close();
        return resp;
    }

    @POST
    @Path("applyAction")
    @RolesAllowed("2fa.manage")
    public List<CrxResponse> applyAction(
            @ApiParam(hidden = true) @Auth Session session,
            CrxActionMap actionMap
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        List<CrxResponse> resp = new Crx2faService(session,em).applyAction(actionMap);
        em.close();
        return resp;
    }

    /**
     * Checks the OTP pin
     * @param req
     * @param pinCheck is a map containing the id of the selected crx2fa and the created session token.
     * @return
     */
    @POST
    @Path("sendpin")
    public CrxResponse sendPin(
            @Context HttpServletRequest req,
            Map<String ,String> pinSend
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        try {
            logger.debug(new ObjectMapper().writeValueAsString(pinSend));
        } catch (Exception e) {
            logger.debug("ERROR" + pinSend);
        }
        CrxResponse res = new Crx2faService(null,em).sendPin(
                Long.parseLong(pinSend.get("crx2faId")),
                req.getRemoteAddr(),
                pinSend.get("token"));
        em.close();
        return res;
    }

    /**
     * Checks the OTP pin
     * @param req
     * @param pinCheck is a map containing the id of the selected crx2fa and the created session token and the pin to check.
     * @return
     */
    @POST
    @Path("checkpin")
    public Crx2faSession checkPin(
            @Context HttpServletRequest req,
            Map<String ,String> pinCheck
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        try {
            logger.debug(new ObjectMapper().writeValueAsString(pinCheck));
        } catch (Exception e) {
            logger.debug("ERROR" + pinCheck);
        }
        Crx2faSession res = new Crx2faService(null,em).checkPin(
                Long.parseLong(pinCheck.get("crx2faId")),
                req.getRemoteAddr(),
                pinCheck.get("pin"),
                pinCheck.get("token"));
        em.close();
        if(res == null) {
            throw new WebApplicationException(401);
        }
        return res;
    }

    /**
     * Delete all expired crx2fa sessions
     * @param session
     */
    @PUT
    @Path("cleanup")
    @RolesAllowed("2fa.manage")
    public void cleanUp(@ApiParam(hidden = true) @Auth Session session) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        new Crx2faService(null,em).cleanUp();
        em.close();
    }
}
