package de.cranix.api.resources;

import de.cranix.dao.Course;
import de.cranix.dao.Session;
import de.cranix.helper.CrxEntityManagerFactory;
import de.cranix.services.CourseService;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import java.util.List;

import static de.cranix.api.resources.Resource.JSON_UTF8;

@Path("courses")
@Api(value = "courses")
@Produces(JSON_UTF8)
public class CourseResource {

    Logger logger = LoggerFactory.getLogger(CourseResource.class);

    public CourseResource() {
    }

    @GET
    @Path("all")
    @ApiOperation(value = "Get all users")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed({"user.search","user.manage"})
    public List<Course> getAll(
            @ApiParam(hidden = true) @Auth Session session
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        final List<Course> ret = new CourseService(session, em).getAll();
        return ret;
    }

}
