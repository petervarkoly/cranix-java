/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.api.resources;


import io.dropwizard.auth.Auth;


import io.swagger.annotations.*;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import de.cranix.dao.User;
import de.cranix.dao.Group;
import de.cranix.dao.CrxActionMap;
import de.cranix.dao.Session;
import de.cranix.dao.CrxResponse;

import java.io.InputStream;
import java.util.List;

import static de.cranix.api.resources.Resource.*;

@Path("groups")
@Api(value = "groups")
public interface GroupResource {

	/*
	 * GET groups/<groupId>
	 */
	@GET
	@Path("{groupId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get group by id")
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Group not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("group.search")
	Group getById(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("groupId") Long groupId
	);

	/*
	* GET groups/<groupId>/members
	*/
	@GET
	@Path("{groupId}/members")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get users which are member in this group.")
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Group not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("group.manage")
	List<User> getMembers(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("groupId") Long groupId
	);

	/*
	* GET groups/text/<groupName>/members
	*/
	@GET
	@Path("text/{groupName}/members")
	@Produces(TEXT)
	@ApiOperation(value = "Get users which are member in this group.")
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Group not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("group.manage")
	String getMembersText(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("groupName") String groupName
	);

	/*
	* GET groups/<groupId>/availableMembers
	*/
	@GET
	@Path("{groupId}/availableMembers")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get users which are not member in this group.")
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Group not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("group.manage")
	List<User> getAvailableMembers(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("groupId") Long groupId
	);

	/*
	 * GET groups/byType/{type}
	 */
	@GET
	@Path("byType/{type}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get groups from a type")
	    @ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("group.search")
	List<Group> getByType(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("type") String type
	);

	/*
	 * GET groups/text/byType/{type}
	 */
	@GET
	@Path("text/byType/{type}")
	@Produces(TEXT)
	@ApiOperation(value = "Get groups from a type")
	    @ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("group.search")
	String getByTypeText(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("type") String type
	);

	/*
	 * GET groups/text/byType/{type}
	 */
	@DELETE
	@Path("text/{groupName}")
	@Produces(TEXT)
	@ApiOperation(value = "Deletes a group presented by name.")
	    @ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("group.search")
	String delete(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("groupName") String groupName
	);

	/*
	 * GET groups/all
	 */
	@GET
	@Path("all")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get all groups")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one group was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("group.search")
	List<Group> getAll(
	        @ApiParam(hidden = true) @Auth Session session
	);

	/*
	 * GET groups/search/{search}
	 */
	@GET
	@Path("search/{search}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Search for group by name and description.")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one group was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("group.search")
	List<Group> search(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("search") String search
	);

	/*
	 * POST groups/add { hash }
	 */
	@POST
	@Path("add")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Create new group")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one group was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("group.add")
	CrxResponse add(
	        @ApiParam(hidden = true) @Auth Session session,
	        Group group
	);

	/*
	 * POST groups/modify { hash }
	 */
	@POST
	@Path("modify")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Modify an existing group")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one group was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("group.modify")
	CrxResponse modify(
	        @ApiParam(hidden = true) @Auth Session session,
	        Group group
	);

	@POST
	@Path("{groupId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Modify an existing group")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one group was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("group.modify")
	CrxResponse modify(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("groupId") Long groupId,
	        Group group
	);

	@POST
	@Path("import")
	@Produces(JSON_UTF8)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation(value =	"Import groups from a CSV file. This MUST have following format:\\n" ,
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
	CrxResponse importGroups(
	@ApiParam(hidden = true) @Auth Session session,
	        @FormDataParam("file") final InputStream fileInputStream,
	        @FormDataParam("file") final FormDataContentDisposition contentDispositionHeader
	);

	/*
	* POST groups/getGroups
	*/
	@POST
	@Path("getGroups")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets a list of group objects to the list of groupIds.")
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Group not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("group.manage")
	List<Group> getGroups(
	        @ApiParam(hidden = true) @Auth Session session,
	        List<Long> groupIds
	);

	/*
	 * DELETE groups/<groupId>
	 */
	@DELETE
	@Path("{groupId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Deletes group by id")
	@ApiResponses(value = {
	    @ApiResponse(code = 404, message = "Group not found"),
	    @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("group.delete")
	CrxResponse delete(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("groupId") Long groupId
	);


	/*
	 * PUT groups/<groupId>
	 */
	@PUT
	@Path("{groupId}/cleanUpDirectory")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Deletes the group directory.")
	@ApiResponses(value = {
	    @ApiResponse(code = 404, message = "Group not found"),
	    @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("group.delete")
	CrxResponse cleanUpDirectory(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("groupId") Long groupId
	);

	/*
	* POST groups/<groupId>/members
	*/
       @POST
       @Path("{groupId}/members")
       @Produces(JSON_UTF8)
       @ApiOperation(value = "Sets the member of this group.")
       @ApiResponses(value = {
               @ApiResponse(code = 404, message = "Group not found"),
               @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
       @RolesAllowed("group.manage")
       CrxResponse setMembers(
               @ApiParam(hidden = true) @Auth Session session,
               @PathParam("groupId") Long groupId,
               List<Long> users
       );

       /*
        * DELETE groups/<groupId>/<userId>
        */
       @DELETE
       @Path("{groupId}/{userId}")
       @Produces(JSON_UTF8)
       @ApiOperation(value = "Deletes a member of a group by userId.")
       @ApiResponses(value = {
           @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
       @RolesAllowed("group.manage")
       CrxResponse removeMember(
               @ApiParam(hidden = true) @Auth Session session,
               @PathParam("groupId") Long groupId,
               @PathParam("userId") Long userId
       );

       /*
        * PUT groups/<groupId>/<userId>
        */
       @PUT
       @Path("{groupId}/{userId}")
       @Produces(JSON_UTF8)
       @ApiOperation(value = "Add a member to a group by userId.")
       @ApiResponses(value = {
           @ApiResponse(code = 404, message = "Group not found"),
           @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
       @RolesAllowed("group.manage")
       CrxResponse addMember(
               @ApiParam(hidden = true) @Auth Session session,
               @PathParam("groupId") Long groupId,
               @PathParam("userId") Long userId
       );

	/**
	 * Apply actions on a list of groups.
	 * @param session
	 * @return The result in an CrxResponse object
	 * @see CrxResponse
	 */
	@POST
	@Path("applyAction")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Apply actions on selected institutes.")
	@ApiResponses(value = {
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("group.manage")
	CrxResponse applyAction(
			@ApiParam(hidden = true) @Auth Session session,
			CrxActionMap actionMap
			);
}
