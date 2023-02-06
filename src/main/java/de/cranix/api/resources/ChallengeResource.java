package de.cranix.api.resources;

import de.cranix.dao.CrxChallenge;
import de.cranix.dao.CrxQuestion;
import de.cranix.dao.CrxResponse;
import de.cranix.dao.Session;
import de.cranix.helper.CrxEntityManagerFactory;
import de.cranix.helper.CrxSystemCmd;
import de.cranix.services.ChallengeService;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.List;
import java.util.Map;

import static de.cranix.api.resources.Resource.JSON_UTF8;
import static de.cranix.api.resources.Resource.TEXT;


@Path("challenges")
@Api(value = "challenges")
@Produces(JSON_UTF8)
public class ChallengeResource {

    Logger logger = LoggerFactory.getLogger(UserResource.class);

    public ChallengeResource() {
    }

    @GET
    @Path("all")
    @ApiOperation(value = "Gets all challenges.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")})
    @RolesAllowed("challenge.manage")
    public List<CrxChallenge> getAll(@ApiParam(hidden = true) @Auth Session session) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        List<CrxChallenge> resp = new ChallengeService(session, em).getAll();
        em.close();
        return resp;
    }

    @GET
    @Path("{id}")
    @ApiOperation(value = "Gets a challenge by id.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")})
    @RolesAllowed("challenge.manage")
    public CrxChallenge getById(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("id") Long id
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxChallenge resp = new ChallengeService(session, em).getById(id);
        em.close();
        return resp;
    }

    @GET
    @Path("subjects/{id}")
    @ApiOperation(value = "Gets a challenge by teaching subject.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")})
    @RolesAllowed("challenge.manage")
    public List<CrxChallenge> getBySubject(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("id") Long id
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        List<CrxChallenge> resp = new ChallengeService(session, em).getBySubject(id);
        em.close();
        return resp;
    }

    @POST
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
    @Path("{id}")
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

    @POST
    @Path("start")
    @ApiOperation(value = "Assign and start a challenge.")
    @RolesAllowed("challenge.manage")
    public CrxResponse assignAndStart(
            @ApiParam(hidden = true) @Auth Session session,
            CrxChallenge crxChallenge
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new ChallengeService(session, em).assignAndStart(crxChallenge);
        em.close();
        return resp;
    }

    @DELETE
    @Path("{challengeId}/{questionId}")
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
    @Path("{challengeId}/{questionId}/{answerId}")
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

    @PUT
    @Path("{challengeId}/archives")
    @ApiOperation(value = "Stop the challenge. Get the detailed results of a challenge and archive it. " +
            "The results of the challenge will be deleted from database." +
            "The challenge will be deassigned from the groups and users.")
    @RolesAllowed("challenge.manage")
    public Object stopAndArchive(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("challengeId") Long challengeId
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        Object resp = new ChallengeService(session, em).stopAndArchive(challengeId);
        em.close();
        return resp;
    }

    @GET
    @Path("{challengeId}/archives")
    @ApiOperation(value = "Get the list of the archives corresponding to the challenge.")
    @RolesAllowed("challenge.manage")
    public List<String> getListOfArchives(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("challengeId") Long challengeId
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        List<String> resp = new ChallengeService(session, em).getListOfArchives(challengeId);
        em.close();
        return resp;
    }

    @GET
    @Path("{challengeId}/archives/{date}")
    @Produces("*/*")
    @ApiOperation(value = "Gets the archived results of a challenge.")
    @RolesAllowed("challenge.manage")
    public Response getArchive(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("challengeId") Long challengeId,
            @PathParam("date") String date
    ){
        //TODO CHECK owner
        String[] program = new String[2];
        program[0] = "/usr/share/cranix/tools/pack_challenge.py";
        program[1] = ChallengeService.getArhivePath(challengeId).append("/").append(date).toString();
        StringBuffer reply = new StringBuffer();
        StringBuffer stderr = new StringBuffer();
        CrxSystemCmd.exec(program, reply, stderr, null);
        File challengeFile = new File(reply.toString().strip());
        logger.debug("challengeFile" + challengeFile.length() + "   " + challengeFile.getName());
        Response.ResponseBuilder response = Response.ok(challengeFile);
        response = response.header("Content-Disposition", "attachment; filename=" + challengeFile.getName());
        return response.build();
    }

    @GET
    @Path("{challengeId}/results")
    @ApiOperation(value = "Get the results of a challenge.")
    @Produces(TEXT)
    @RolesAllowed("challenge.manage")
    public String evaluate(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("challengeId") Long challengeId
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        String resp = new ChallengeService(session, em).evaluateAsHtml(challengeId);
        em.close();
        return resp;
    }

    @GET
    @Path("questions")
    @ApiOperation(value = "Get all questions.")
    @RolesAllowed("challenge.manage")
    public List<CrxQuestion> getAllQuestions(
            @ApiParam(hidden = true) @Auth Session session
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        List<CrxQuestion> resp = new ChallengeService(session, em).getAllQuestion();
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
}
