package de.cranix.api.resources;

import de.cranix.dao.*;
import de.cranix.helper.CrxEntityManagerFactory;
import de.cranix.services.PTMService;
import de.cranix.services.ParentService;
import de.cranix.services.SessionService;
import de.cranix.services.UserService;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;

import java.util.List;

import static de.cranix.api.resources.Resource.JSON_UTF8;

@Path("parents")
@Api(value = "parents")
@Produces(JSON_UTF8)
public class ParentResource {

    /*
    * Handle parent requests
    * */
    @POST
    @Path("parentRequest")
    @ApiOperation(value = "Creates a new parent request")
    public CrxResponse createParentRequest(
           @Context HttpServletRequest req,
           ParentRequest parentRequest
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        Session session = new SessionService(em).getLocalhostSession();
        CrxResponse resp = new ParentService(session,em).createParentRequest(parentRequest, req);
        em.close();
        return resp;
    }
    /*
    * Handle parents
    * */
    @POST
    @RolesAllowed("parent.manage")
    @ApiOperation(value = "Creates a new parent.")
    public CrxResponse add(
            @ApiParam(hidden = true) @Auth Session session,
            User parent
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new ParentService(session,em).add(parent);
        em.close();
        return resp;
    }

    @PATCH
    @RolesAllowed("parent.manage")
    @ApiOperation(value = "Modify a parent.")
    public CrxResponse modify(
            @ApiParam(hidden = true) @Auth Session session,
            User parent
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new ParentService(session,em).modify(parent);
        em.close();
        return resp;
    }

    @DELETE
    @Path("{id}")
    @RolesAllowed("parent.manage")
    @ApiOperation(value = "Deletes a parent.")
    public CrxResponse delete(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("id") Long id
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new ParentService(session,em).delete(id);
        em.close();
        return resp;
    }

    @GET
    @RolesAllowed("parent.manage")
    @ApiOperation(value = "Gets the parents.")
    public List<User> get(
            @ApiParam(hidden = true) @Auth Session session
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        List<User> resp = new UserService(session,em).getByRole("parents");
        em.close();
        return resp;
    }

    @GET
    @Path("{id}")
    @RolesAllowed("ptm.manage")
    @ApiOperation(value = "Gets one parent  by id.")
    public User getById(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("id") Long id
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        User resp = new UserService(session,em).getById(id);
        em.close();
        if(resp.getRole().equals("parents")) {
            return resp;
        } else {
            return null;
        }
    }


    @POST
    @Path("{id}/children")
    @RolesAllowed("parent.manage")
    @ApiOperation(value = "Deletes a parent.")
    public CrxResponse setChildren(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("id") Long id,
            List<User> children
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new ParentService(session,em).setChildren(id, children);
        em.close();
        return resp;
    }

    @GET
    @Path("{id}/children")
    @RolesAllowed("parent.manage")
    @ApiOperation(value = "Gets the parents.")
    public List<User> getChildren(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("id") Long id
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        User parent = new UserService(session,em).getById(id);
        em.close();
        if(parent.getRole().equals("parents")) {
            return parent.getChildren();
        } else {
            return null;
        }
    }

    /**
     * Fuctions to manage parent teacher meetings
     */

    @POST
    @Path("ptms")
    @RolesAllowed("ptm.manage")
    @ApiOperation(value = "Creates a new parent teacher meeting.")
    public CrxResponse addPtm(
            @ApiParam(hidden = true) @Auth Session session,
            ParentTeacherMeeting parentTeacherMeeting
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new PTMService(session,em).add(parentTeacherMeeting);
        em.close();
        return resp;
    }

    @PATCH
    @Path("ptms")
    @RolesAllowed("ptm.manage")
    @ApiOperation(value = "Modify a new parent teacher meeting.")
    public CrxResponse modifyPtm(
            @ApiParam(hidden = true) @Auth Session session,
            ParentTeacherMeeting parentTeacherMeeting
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new PTMService(session,em).modify(parentTeacherMeeting);
        em.close();
        return resp;
    }

    @DELETE
    @Path("ptms/{id}")
    @RolesAllowed("ptm.manage")
    @ApiOperation(value = "Deletes a parent teacher meeting.")
    public CrxResponse deletePtm(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("id") Long id
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new PTMService(session,em).delete(id);
        em.close();
        return resp;
    }

    @GET
    @Path("ptms")
    @RolesAllowed({"ptm.manage","ptm.registerRoom","ptm.registerEvent"})
    @ApiOperation(value = "Gets the next parent teacher meeting.")
    public List<ParentTeacherMeeting> getPtms(
            @ApiParam(hidden = true) @Auth Session session
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        List<ParentTeacherMeeting> resp = new PTMService(session,em).get();
        em.close();
        return resp;
    }


    @GET
    @Path("ptms/former")
    @RolesAllowed({"ptm.manage","ptm.registerRoom","ptm.registerEvent"})
    @ApiOperation(value = "Gets the next parent teacher meeting.")
    public List<ParentTeacherMeeting> getPtms(
            @ApiParam(hidden = true) @Auth Session session
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        List<ParentTeacherMeeting> resp = new PTMService(session,em).getFormer();
        em.close();
        return resp;
    }

    @GET
    @Path("ptms/{id}")
    @RolesAllowed("ptm.manage")
    @ApiOperation(value = "Gets one parent teacher meeting by id.")
    public ParentTeacherMeeting getPtmById(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("id") Long id
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        ParentTeacherMeeting resp = new PTMService(session,em).getById(id);
        em.close();
        return resp;
    }

    @GET
    @Path("ptms/{id}/rooms")
    @RolesAllowed("ptm.manage")
    @ApiOperation(value = "Gets the free rooms of a parent teacher meeting.")
    public List<Room> getFreeRooms(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("id") Long id
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        List<Room> resp = new PTMService(session,em).getFreeRooms(id);
        em.close();
        return resp;
    }

    @GET
    @Path("ptms/{id}/teachers")
    @RolesAllowed("ptm.manage")
    @ApiOperation(value = "Gets the free teachers of a parent teacher meeting.")
    public List<User> getFreeTeachers(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("id") Long id
    ){
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        List<User> resp = new PTMService(session,em).getFreeTeachers(id);
        em.close();
        return resp;
    }

    @POST
    @Path("ptms/{id}/rooms")
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
    @Path("ptms/rooms/{id}")
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
    @Path("ptms/events")
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
    @Path("ptms/events/{id}")
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
