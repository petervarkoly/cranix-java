package de.cranix.api.resources;

import de.cranix.dao.CrxChallenge;
import de.cranix.dao.CrxResponse;
import de.cranix.dao.Session;
import de.cranix.helper.CrxEntityManagerFactory;
import de.cranix.services.ChallengeService;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.*;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.List;

import static de.cranix.api.resources.Resource.JSON_UTF8;

@Path("challenges")
@Api(value = "challenges")
@Produces(JSON_UTF8)
public class ChallengeResource {
    public ChallengeResource() {
    }

    @GET
    @Path("all")
    @ApiOperation(value = "Gets all challenges created by the user.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
    @PermitAll
    public List<CrxChallenge> getAll(@ApiParam(hidden = true) @Auth Session session) {
        return session.getUser().getChallenges();
    }

    @GET
    @Path("todo")
    @ApiOperation(value = "Gets all challenges created by the user.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
    @PermitAll
    public List<CrxChallenge> todo(@ApiParam(hidden = true) @Auth Session session) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        List<CrxChallenge> resp = new ChallengeService(session, em).getTodos();
        em.close();
        return resp;
    }

    @POST
    @Path("add")
    @ApiOperation(value = "Creates a ne challenge.")
    @RolesAllowed("challenge.manage")
    public CrxResponse add(
            @ApiParam(hidden = true) @Auth Session session,
            CrxChallenge crxChallenge
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new ChallengeService(session, em).add(crxChallenge);
        em.close();
        return resp;
    }

}
