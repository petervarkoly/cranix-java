package de.cranix.api.resources;

import de.cranix.dao.Course;
import de.cranix.dao.CrxCalendar;
import de.cranix.dao.CrxResponse;
import de.cranix.dao.Session;
import de.cranix.helper.CrxEntityManagerFactory;
import de.cranix.services.CourseService;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.ws.rs.*;

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
    @ApiOperation(value = "Get all courses")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed({"course.search","course.manage"})
    public List<Course> getAll(
            @ApiParam(hidden = true) @Auth Session session
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        final List<Course> ret = new CourseService(session, em).getAll();
        em.close();
        return ret;
    }

    @POST
    @ApiOperation(value="Creates a new course")
    @RolesAllowed({"course.manage"})
    public CrxResponse add(
            @ApiParam(hidden = true) @Auth Session session,
            Course course
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        final CrxResponse ret = new CourseService(session, em).add(course);
        em.close();
        return ret;
    }

    @PATCH
    @ApiOperation(value="Modifies a course")
    @RolesAllowed({"course.manage"})
    public CrxResponse modify(
            @ApiParam(hidden = true) @Auth Session session,
            Course course
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        final CrxResponse ret = new CourseService(session, em).modify(course);
        em.close();
        return ret;
    }

    @DELETE
    @Path("{courseId}")
    @ApiOperation(value="Deletes a course")
    @RolesAllowed({"course.manage"})
    public CrxResponse delete(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("courseId") Long id
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        final CrxResponse ret = new CourseService(session, em).delete(id);
        em.close();
        return ret;
    }

    @POST
    @Path("{courseId}/appointments")
    @ApiOperation(value="Add an appointment to the course")
    @RolesAllowed({"course.manage"})
    public CrxResponse addAppointment(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("courseId") Long id,
            CrxCalendar appointment
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        final CrxResponse ret = new CourseService(session, em).addAppointment(id, appointment);
        em.close();
        return ret;
    }

    @PUT
    @Path("{courseId}")
    @ApiOperation(value="Sends notifications for a course")
    @RolesAllowed({"course.manage"})
    public CrxResponse sendNotifications(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("courseId") Long id
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        final CrxResponse ret = new CourseService(session, em).sendNotifications(id);
        em.close();
        return ret;
    }

    @GET
    @Path("{courseId}")
    @ApiOperation(value="Delivers a course by id.")
    @PermitAll
    public Course getById(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("courseId") Long courseId
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        final  Course ret = new CourseService(session, em).getById(courseId);
        em.close();
        return ret;
    }

    @GET
    @Path("{courseId}/free")
    @ApiOperation(value="Get free appointments in a course.")
    @PermitAll
    public List<CrxCalendar> getFree(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("courseId") Long courseId
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        final  List<CrxCalendar> ret = new CourseService(session, em).getFreeAppointments(courseId, session.getUser());
        em.close();
        return ret;
    }

    // Manipulate appointments in a course.
    @PUT
    @Path("appointments/{appointmenId}")
    @ApiOperation(value="Register to a course")
    @RolesAllowed({"course.manage"})
    public CrxResponse register(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("appointmenId") Long appointmenId
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        final CrxResponse ret = new CourseService(session, em).register(appointmenId);
        em.close();
        return ret;
    }

    @PUT
    @Path("appointments/{appointmenId}/{userId}")
    @ApiOperation(value="Register a user to a course")
    @RolesAllowed({"course.manage"})
    public CrxResponse register(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("appointmenId") Long appointmenId,
            @PathParam("userId") Long userId
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        final CrxResponse ret = new CourseService(session, em).register(appointmenId, userId);
        em.close();
        return ret;
    }

    @DELETE
    @Path("appointments/{appointmenId}")
    @ApiOperation(value="Remove registration from a course")
    @RolesAllowed({"course.manage"})
    public CrxResponse deRegister(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("appointmenId") Long appointmenId
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        final CrxResponse ret = new CourseService(session, em).deRegister(appointmenId);
        em.close();
        return ret;
    }

    @DELETE
    @Path("appointments/{appointmenId}/{userId}")
    @ApiOperation(value="Remove user registration from a course")
    @RolesAllowed({"course.manage"})
    public CrxResponse deRregister(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("appointmenId") Long appointmenId,
            @PathParam("userId") Long userId
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        final CrxResponse ret = new CourseService(session, em).deRegister(appointmenId, userId);
        em.close();
        return ret;
    }


}
