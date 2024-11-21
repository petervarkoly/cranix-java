package de.cranix.api.resources;

import de.cranix.dao.*;
import de.cranix.helper.CrxEntityManagerFactory;
import de.cranix.services.PTMService;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.ws.rs.*;

import java.util.List;

import static de.cranix.api.resources.Resource.JSON_UTF8;

@Path("ptms")
@Api(value = "ptms")
@Produces(JSON_UTF8)
public class ParentTeacherMeetingResource {

    @POST
    @RolesAllowed("ptm.manage")
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

    @PATCH
    @RolesAllowed("ptm.manage")
    @ApiOperation(value = "Modify a new parent teacher meeting.")
    public CrxResponse modify(
            @ApiParam(hidden = true) @Auth Session session,
            ParentTeacherMeeting parentTeacherMeeting
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new PTMService(session,em).modify(parentTeacherMeeting);
        em.close();
        return resp;
    }

    @DELETE
    @Path("{id}")
    @RolesAllowed("ptm.manage")
    @ApiOperation(value = "Deletes a parent teacher meeting.")
    public CrxResponse delete(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("id") Long id
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new PTMService(session,em).delete(id);
        em.close();
        return resp;
    }

    @GET
    @RolesAllowed({"ptm.manage","ptm.registerRoom","ptm.registerEvent"})
    @ApiOperation(value = "Gets the next parent teacher meeting.")
    public ParentTeacherMeeting get(
            @ApiParam(hidden = true) @Auth Session session
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        ParentTeacherMeeting resp = new PTMService(session,em).get();
        em.close();
        return resp;
    }

    @GET
    @Path("{id}")
    @RolesAllowed("ptm.manage")
    @ApiOperation(value = "Gets one parent teacher meeting by id.")
    public ParentTeacherMeeting getById(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("id") Long id
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        ParentTeacherMeeting resp = new PTMService(session,em).getById(id);
        em.close();
        return resp;
    }

    @GET
    @Path("{id}/rooms")
    @RolesAllowed("ptm.manage")
    @ApiOperation(value = "Gets one parent teacher meeting by id.")
    public List<Room> getFreeRooms(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("id") Long id
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        List<Room> resp = new PTMService(session,em).getFreeRooms(id);
        em.close();
        return resp;
    }

    @POST
    @Path("{id}/rooms")
    @RolesAllowed({"ptm.manage","ptm.registerRoom"})
    @ApiOperation(value = "Register a teacher in a room.")
    public CrxResponse registerRoom(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("id") Long id,
            PTMTeacherInRoom ptmTeacherInRoom
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new PTMService(session,em).registerRoom(id, ptmTeacherInRoom);
        em.close();
        return resp;
    }

    @DELETE
    @Path("rooms/{id}")
    @RolesAllowed({"ptm.manage","ptm.registerRoom"})
    @ApiOperation(value = "Deletes a room registration.")
    public CrxResponse cancelRoomRegistration(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("id") Long id
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new PTMService(session,em).cancelRoomRegistration(id);
        em.close();
        return resp;
    }

    @POST
    @Path("events")
    @RolesAllowed({"ptm.manage","ptm.registerRoom"})
    @ApiOperation(value = "Register a teacher in a room.")
    public CrxResponse registerEvent(
            @ApiParam(hidden = true) @Auth Session session,
            PTMEvent ptmEvent
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new PTMService(session,em).registerEvent(ptmEvent);
        em.close();
        return resp;
    }

    @DELETE
    @Path("events/{id}")
    @RolesAllowed({"ptm.manage","ptm.registerRoom"})
    @ApiOperation(value = "Deletes a room registration.")
    public CrxResponse cancelEvent(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("id") Long id
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new PTMService(session,em).cancelEvent(id);
        em.close();
        return resp;
    }
}
