package de.cranix.api.resources;

import de.cranix.dao.CrxResponse;
import de.cranix.dao.Session;
import de.cranix.helper.CrxEntityManagerFactory;
import de.cranix.services.CrxMdmService;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.*;

import javax.annotation.security.PermitAll;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.List;
import java.util.Map;

import static de.cranix.api.resources.Resource.JSON_UTF8;

@Path("mdm")
@Api(value = "mdm")
public class MdmResource {

    public MdmResource() {
        super();
    }

    @GET
    @Path("enrollments")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Gets all created enrollments.")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "No room was found"),
            @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
    @PermitAll
    public List<Map<String, String>> getEnrollments(@ApiParam(hidden = true) @Auth Session session) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        List<Map<String, String>> resp = new CrxMdmService(session, em).getEnrollments();
        em.close();
        return resp;
    }

    @POST
    @Path("enrollments")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Create an enrollment for a devices.")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "No room was found"),
            @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
    @PermitAll
    public CrxResponse addEnrollment(
            @ApiParam(hidden = true) @Auth Session session,
            Long deviceId
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new CrxMdmService(session, em).addEnrollment(deviceId);
        em.close();
        return resp;
    }
}
