package de.cranix.api.resources;

import de.cranix.dao.CrxResponse;
import de.cranix.dao.IdRequest;
import de.cranix.dao.Session;
import de.cranix.helper.CrxEntityManagerFactory;
import de.cranix.services.IdRequestService;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.*;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.ws.rs.*;

import java.util.List;

import static de.cranix.api.resources.Resource.JSON_UTF8;

@Path("idRequests")
@Api(value = "idRequests")
@Produces(JSON_UTF8)
public class IdRequestResource {


    @GET
    @ApiOperation(value = "Get all id requests")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed("id_request.manage")
    public List<IdRequest> get(
            @ApiParam(hidden = true) @Auth Session session
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        List<IdRequest> response = new IdRequestService(session,em).getAll();
        em.close();
        return response;
    }

    @GET
    @Path("{id}")
    @ApiOperation(value = "Get all id requests")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed("id_request.manage")
    public IdRequest getById(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("id")  Long id
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        IdRequest response = new IdRequestService(session,em).getById(id);
        em.close();
        return response;
    }

    @POST
    @ApiOperation(value = "Sets the allowed status of an ID request")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed("id_request.manage")
    public CrxResponse setAllowedStatus(
            @ApiParam(hidden = true) @Auth Session session,
            IdRequest idRequest
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse response = new IdRequestService(session,em).setAllowedSatus(idRequest);
        em.close();
        return response;
    }

    @POST
    @Path("idRequest")
    @ApiOperation(value = "Creates an ID request.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed("id_request.use")
    public CrxResponse createIdRequest(
            @ApiParam(hidden = true) @Auth Session session,
            IdRequest idRequest
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse response = new IdRequestService(session,em).add(idRequest);
        em.close();
        return response;
    }

    @GET
    @Path("idRequest")
    @ApiOperation(value = "Gets the own ID request.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @PermitAll
    public IdRequest getMyIdRequest(
            @ApiParam(hidden = true) @Auth Session session
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        IdRequest response = new IdRequestService(session,em).getMyIdRequest();
        em.close();
        return response;
    }
}
