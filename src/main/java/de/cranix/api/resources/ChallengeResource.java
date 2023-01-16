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
import javax.ws.rs.*;
import java.util.List;
import java.util.Map;

import static de.cranix.api.resources.Resource.JSON_UTF8;


@Path("challenges")
@Api(value = "challenges")
@Produces(JSON_UTF8)
public class ChallengeResource {
    public ChallengeResource() {
    }

    @GET
    @Path("challenges/all")
    @ApiOperation(value = "Gets all challenges created by the user.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")})
    @PermitAll
    public List<CrxChallenge> getAll(@ApiParam(hidden = true) @Auth Session session) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        List<CrxChallenge> resp = new ChallengeService(session, em).getChallenges();
        em.close();
        return resp;
    }

    @GET
    @Path("challenges/{id}")
    @ApiOperation(value = "Gets all challenges created by the user.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")})
    @PermitAll
    public CrxChallenge getById(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("id") Long id
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxChallenge resp = new ChallengeService(session, em).getById(id);
        em.close();
        return resp;
    }

    @POST
    @Path("challenges")
    @ApiOperation(value = "Creates a new challenge.")
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

    @PATCH
    @Path("challenges")
    @ApiOperation(value = "Modify a challenge.")
    @RolesAllowed("challenge.manage")
    public CrxResponse modify(
            @ApiParam(hidden = true) @Auth Session session,
            CrxChallenge crxChallenge
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new ChallengeService(session, em).modify(crxChallenge);
        em.close();
        return resp;
    }

    @DELETE
    @Path("challenges/{id}")
    @ApiOperation(value = "Delete a challenge.")
    @RolesAllowed("challenge.manage")
    public CrxResponse delete(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("id") Long id
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new ChallengeService(session, em).delete(id);
        em.close();
        return resp;
    }

    @DELETE
    @Path("challenges/{challengeId}/{questionId}")
    @ApiOperation(value = "Delete a question.")
    @RolesAllowed("challenge.manage")
    public CrxResponse deleteQuestion(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("challengeId") Long challengeId,
            @PathParam("questionId") Long questionId
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new ChallengeService(session, em).deleteQuestion(challengeId,questionId);
        em.close();
        return resp;
    }

    @DELETE
    @Path("challenges/{challengeId}/{questionId}/{answerId}")
    @ApiOperation(value = "Delete an answer.")
    @RolesAllowed("challenge.manage")
    public CrxResponse deleteAnswer(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("challengeId") Long challengeId,
            @PathParam("questionId") Long questionId,
            @PathParam("answerId") Long answerId
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new ChallengeService(session, em).deleteAnswer(challengeId,questionId,answerId);
        em.close();
        return resp;
    }

    /*
    * Functions to the challenge results
    */
    @GET
    @Path("todos/all")
    @ApiOperation(value = "Gets all actual challenges corresponding to the user.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")})
    @PermitAll
    public List<CrxChallenge> todo(@ApiParam(hidden = true) @Auth Session session) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        List<CrxChallenge> resp = new ChallengeService(session, em).getTodos();
        em.close();
        return resp;
    }

    @POST
    @Path("todos/{challengeId}")
    @ApiOperation(value = "Save the results of a challenge.")
    @PermitAll
    public CrxResponse answer(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("challengeId") Long challengeId,
            Map<Long,Boolean> answers
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new ChallengeService(session, em).saveChallengeAnswers(challengeId,answers);
        em.close();
        return resp;
    }

    @GET
    @Path("todos/{challengeId}")
    @ApiOperation(value = "Get the saved results of a challenge of the session user.")
    @PermitAll
    public Object getMyResults(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("challengeId") Long challengeId
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        Object resp = new ChallengeService(session, em).getMyResults(challengeId);
        em.close();
        return resp;
    }

    @GET
    @Path("todos/{challengeId}/results")
    @ApiOperation(value = "Get the results of a challenge.")
    @PermitAll
    public Object evaluate(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("challengeId") Long challengeId
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        Object resp = new ChallengeService(session, em).evaluate(challengeId);
        em.close();
        return resp;
    }

    @GET
    @Path("todos/{challengeId}/archive/{cleanUp}")
    @ApiOperation(value = "Get the detailed results of a challenge and archive it. If cleanUp is 1 the challenge will be deleted")
    @PermitAll
    public Object archive(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("challengeId") Long challengeId,
            @PathParam("cleanUp") Integer cleanUp
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        Object resp = new ChallengeService(session, em).archiveResults(challengeId, cleanUp == 1);
        em.close();
        return resp;
    }
}
