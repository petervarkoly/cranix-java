/* (c) 2020 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.api.resources;


import de.cranix.dao.*;
import de.cranix.helper.CrxEntityManagerFactory;
import de.cranix.services.GroupService;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.*;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static de.cranix.api.resources.Resource.JSON_UTF8;
import static de.cranix.api.resources.Resource.TEXT;

@Path("groups")
@Api(value = "groups")
public class GroupResource {

    Logger logger = LoggerFactory.getLogger(GroupResource.class);

    public GroupResource() {
    }

    @POST
    @Path("add")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Create new group")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed("group.add")
    public CrxResponse add(
            @ApiParam(hidden = true) @Auth Session session,
            Group group
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new GroupService(session, em).add(group);
        em.close();
        return resp;
    }

    @POST
    @Path("{groupId}")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Modify an existing group")
    @ApiResponses(value = {
            // TODO so oder anders? @ApiResponse(code = 404, message = "At least one group was not found"),
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed("group.modify")
    public CrxResponse modify(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("groupId") Long groupId,
            Group group
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        group.setId(groupId);
        CrxResponse resp = new GroupService(session, em).modify(group);
        em.close();
        return resp;
    }

    @DELETE
    @Path("{groupId}")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Deletes group by id")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Group not found"),
            @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
    @RolesAllowed("group.delete")
    public CrxResponse delete(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("groupId") Long groupId
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new GroupService(session, em).delete(groupId);
        em.close();
        return resp;
    }

    @GET
    @Path("all")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Get all groups")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @PermitAll
    public List<Group> getAll(@ApiParam(hidden = true) @Auth Session session) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        List<Group> resp = new GroupService(session, em).getAll();
        em.close();
        return resp;
    }

    @GET
    @Path("{groupId}")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Get group by id")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Group not found"),
            @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
    @RolesAllowed("group.search")
    public Group getById(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("groupId") Long groupId
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        Group group = new GroupService(session, em).getById(groupId);
        em.close();
        return group;
    }

    @GET
    @Path("{groupId}/members")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Get users which are member in this group.")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Group not found"),
            @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
    @RolesAllowed("group.manage")
    public List<User> getMembers(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("groupId") Long groupId
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        List<User> resp = new GroupService(session, em).getMembers(groupId);
        em.close();
        return resp;
    }


    @GET
    @Path("{groupId}/availableMembers")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Get users which are not member in this group.")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Group not found"),
            @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
    @RolesAllowed("group.manage")
    public List<User> getAvailableMembers(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("groupId") Long groupId
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        List<User> users = new GroupService(session, em).getAvailableMember(groupId);
        em.close();
        return users;
    }

    @GET
    @Path("byType/{type}")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Get groups from a type")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed("group.search")
    public List<Group> getByType(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("type") String type
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        List<Group> resp = new GroupService(session, em).getByType(type);
        em.close();
        return resp;
    }


    @POST
    @Path("import")
    @Produces(JSON_UTF8)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation(value = "Import groups from a CSV file. This MUST have following format:\\n",
            notes = "* Separator is the semicolon ';'.<br>" +
                    "* No header line must be provided.<br>" +
                    "* Fields: name;description;group type;member.<br>" +
                    "* Group Type: San be class, primary or workgroup.<br>" +
                    "* Member: Space separated list of user names (uid).<br>" +
                    "* uid: The user must exist.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed("group.add")
    public CrxResponse importGroups(
            @ApiParam(hidden = true) @Auth Session session,
            @FormDataParam("file") final InputStream fileInputStream,
            @FormDataParam("file") final FormDataContentDisposition contentDispositionHeader
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new GroupService(session, em).importGroups(fileInputStream, contentDispositionHeader);
        em.close();
        return resp;
    }

    @POST
    @Path("{groupId}/members")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Sets the member of this group.")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Group not found"),
            @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
    @RolesAllowed("group.modify")
    public CrxResponse setMembers(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("groupId") Long groupId,
            List<Long> users
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new GroupService(session, em).setMembers(groupId, users);
        em.close();
        return resp;
    }

    @DELETE
    @Path("{groupId}/{userId}")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Deletes a member of a group by userId.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
    @RolesAllowed("group.modify")
    public CrxResponse removeMember(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("groupId") Long groupId,
            @PathParam("userId") Long userId
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new GroupService(session, em).removeMember(groupId, userId);
        em.close();
        return resp;
    }

    @PUT
    @Path("{groupId}/{userId}")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Add a member to a group by userId.")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Group not found"),
            @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
    @RolesAllowed("group.modify")
    public CrxResponse addMember(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("groupId") Long groupId,
            @PathParam("userId") Long userId
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new GroupService(session, em).addMember(groupId, userId);
        em.close();
        return resp;
    }

    /**
     * Apply actions on a list of groups.
     *
     * @param session
     * @return The result in an CrxResponse object
     * @see CrxResponse
     */
    @POST
    @Path("applyAction")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Apply actions on selected groups.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
    @RolesAllowed("group.manage")
    public List<CrxResponse> applyAction(
            @ApiParam(hidden = true) @Auth Session session,
            CrxActionMap actionMap
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        List<CrxResponse> resp = new GroupService(session, em).applyAction(actionMap);
        em.close();
        return resp;
    }

    @GET
    @Path("text/byType/{type}")
    @Produces(TEXT)
    @ApiOperation(value = "Get groups from a type")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed("group.search")
    public String getByTypeText(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("type") String type
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        List<String> groups = new ArrayList<String>();
        final GroupService gc = new GroupService(session, em);
        for (Group group : gc.getByType(type)) {
            groups.add(group.getName());
        }
        String resp = String.join(gc.getNl(), groups);
        em.close();
        return resp;
    }

    @DELETE
    @Path("text/{groupName}")
    @Produces(TEXT)
    @ApiOperation(value = "Deletes a group presented by name.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @RolesAllowed("group.search")
    public String delete(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("groupName") String groupName
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        String resp = new GroupService(session, em).delete(groupName).getCode();
        em.close();
        return resp;
    }

    @GET
    @Path("text/{groupName}/members")
    @Produces(TEXT)
    @ApiOperation(value = "Get users which are member in this group.")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Group not found"),
            @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
    @RolesAllowed("group.manage")
    public String getMembersText(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("groupName") String groupName
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        List<String> member = new ArrayList<String>();
        final GroupService gc = new GroupService(session, em);
        Group group = gc.getByName(groupName);
        for (User user : group.getUsers()) {
            member.add(user.getUid());
        }
        String resp = String.join(gc.getNl(), member);
        em.close();
        return resp;
    }

    @PUT
    @Path("text/{groupName}/owner/{userName}")
    @Produces(TEXT)
    @ApiOperation(value = "Set the owner of the group.")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Group not found"),
            @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
    @RolesAllowed("group.manage")
    public String setOwner(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("groupName") String groupName,
            @PathParam("userName") String userName
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse crxResponse = new GroupService(session, em).setOwner(groupName,userName);
        em.close();
        return crxResponse.getCode();
    }

    @GET
    @Path("text/{groupName}/owner")
    @Produces(TEXT)
    @ApiOperation(value = "Get the owner of the group.")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Group not found"),
            @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
    @RolesAllowed("group.manage")
    public String getOwner(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("groupName") String groupName
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        String resp = new GroupService(session, em).getOwner(groupName);
        em.close();
        return resp;
    }
}
