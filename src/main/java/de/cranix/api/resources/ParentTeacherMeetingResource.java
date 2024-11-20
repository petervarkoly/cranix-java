package de.cranix.api.resources;

import de.cranix.dao.CrxResponse;
import de.cranix.dao.ParentTeacherMeeting;
import de.cranix.dao.Session;
import de.cranix.helper.CrxEntityManagerFactory;
import de.cranix.services.PTMService;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import static de.cranix.api.resources.Resource.JSON_UTF8;

@Path("ptms")
@Api(value = "ptms")
@Produces(JSON_UTF8)
public class ParentTeacherMeetingResource {

    @POST
    @RolesAllowed("ptm.mamage")
    @ApiOperation(value = "Creates a new parent teacher meeting.")
    public CrxResponse add(
            @ApiParam(hidden = true) @Auth Session session,
            ParentTeacherMeeting parentTeacherMeeting
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new PTMService(session,em).add(parentTeacherMeeting);
        em.close();
        return resp;
    }
}
