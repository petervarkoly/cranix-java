/* (c) 2024 Péter Varkoly <pvarkoly@cephalix.eu> - all rights reserved */
/**
 * @author Peter Varkoly <pvarkoly@cephalix.eu>
 */
package de.cranix.api.resources;

import de.cranix.dao.*;
import de.cranix.helper.CrxEntityManagerFactory;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.*;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import java.io.InputStream;
import java.util.List;

import static de.cranix.api.resources.Resource.JSON_UTF8;

import de.cranix.services.CalendarService;

@Path("calendar")
@Api(value = "calendar")
@Produces(JSON_UTF8)
public class CrxCalendarResource {
    Logger logger = LoggerFactory.getLogger(UserResource.class);

    public CrxCalendarResource() {
    }

    @GET
    @Path("courseScheduler")
    @ApiOperation(value = "Get all calendar entries of the session user.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed({"calendar.manage"})
    public List<String[]> getCourseScheduler(
            @ApiParam(hidden = true) @Auth Session session
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        final List<String[]> scheduler = new CalendarService(session, em).getCourseScheduler();
        em.close();
        return scheduler;
    }

    @POST
    @Path("courseScheduler")
    @ApiOperation(value = "Get all calendar entries of the session user.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed({"calendar.manage"})
    public CrxResponse getCourseScheduler(
            @ApiParam(hidden = true) @Auth Session session,
            List<String[]> newScheduler
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        final CrxResponse response = new CalendarService(session, em).setCourseScheduler(newScheduler);
        em.close();
        return response;
    }

    @GET
    @ApiOperation(value = "Get all calendar entries of the session user.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed({"calendar.manage", "calendar.use", "calendar.read"})
    public List<CrxCalendar> getMyAll(
            @ApiParam(hidden = true) @Auth Session session
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        final List<CrxCalendar> events = new CalendarService(session, em).getMyAll();
        em.close();
        if (events == null) {
            throw new WebApplicationException(404);
        }
        return events;
    }

    @GET
    @Path("all")
    @ApiOperation(value = "Get all calendar entries of the session user.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed({"calendar.manage"})
    public List<CrxCalendar> getAll(
            @ApiParam(hidden = true) @Auth Session session
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        final List<CrxCalendar> events = new CalendarService(session, em).getAll();
        em.close();
        if (events == null) {
            throw new WebApplicationException(404);
        }
        return events;
    }

    @POST
    @Path("filter")
    @ApiOperation(value = "Get all calendar entries of the session user.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed({"calendar.manage", "calendar.use", "calendar.read"})
    public List<CrxCalendar> getMyFiltered(
            @ApiParam(hidden = true) @Auth Session session,
            FilterObject map
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        final List<CrxCalendar> events = new CalendarService(session, em).getMyFiltered(map);
        em.close();
        if (events == null) {
            throw new WebApplicationException(404);
        }
        return events;
    }

    @POST
    @ApiOperation(value = "Create new event.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed({"calendar.manage", "calendar.use"})
    public CrxResponse add(
            @ApiParam(hidden = true) @Auth Session session,
            CrxCalendar event
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse crxResponse = new CalendarService(session, em).add(event);
        em.close();
        return crxResponse;
    }

    @PATCH
    @ApiOperation(value = "Modify an existing event.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed({"calendar.manage", "calendar.use"})
    public CrxResponse modify(
            @ApiParam(hidden = true) @Auth Session session,
            CrxCalendar event
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse crxResponse = new CalendarService(session, em).modify(event);
        em.close();
        return crxResponse;
    }

    @GET
    @Path("{eventId}")
    @ApiOperation(value = "Deletes an existing event.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed({"calendar.manage", "calendar.use", "calendar.read"})
    public CrxCalendar getById(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("eventId") Long eventId
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxCalendar crxCalendar = new CalendarService(session, em).getById(eventId);
        em.close();
        return crxCalendar;
    }

    @DELETE
    @Path("{eventId}")
    @ApiOperation(value = "Deletes an existing event.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed({"calendar.manage", "calendar.use"})
    public CrxResponse modify(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("eventId") Long eventId
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse crxResponse = new CalendarService(session, em).delete(eventId);
        em.close();
        return crxResponse;
    }

    @PUT
    @Path("sync")
    @ApiOperation(value = "Sync events to the filesystem")
    @RolesAllowed({"calendar.manage"})
    public CrxResponse sync(
            @ApiParam(hidden = true) @Auth Session session
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse crxResponse = new CalendarService(session, em).exportCalendar();
        em.close();
        return crxResponse;
    }

    @POST
    @Path("import")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation(
            value = "Import Timetable from CSV file",
            notes = "* Separator is the colon ''.<br>" +
                    "* A header line must not be provided.<br>" +
                    "* List and order of the fields is obligatory:<br>" +
                    "* ID,Class,Teacher,Course,Room,Day,Hour<br>"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed({"calendar.manage"})
    public CrxResponse importDevices(
            @ApiParam(hidden = true) @Auth Session session,
            @FormDataParam("start") String start,
            @FormDataParam("end") String end,
            @FormDataParam("file") final InputStream fileInputStream,
            @FormDataParam("file") final FormDataContentDisposition contentDispositionHeader
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new CalendarService(session, em).importTimetable(fileInputStream, start, end);
        em.close();
        return resp;
    }

}

