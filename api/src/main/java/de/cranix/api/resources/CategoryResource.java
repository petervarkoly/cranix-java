/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
/**
 * @author Peter Varkoly <peter@varkoly.de>
 *
 */
package de.cranix.api.resources;

import static de.cranix.api.resources.Resource.JSON_UTF8;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.POST;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import de.cranix.dao.Category;
import de.cranix.dao.CrxResponse;
import de.cranix.dao.Session;

import java.util.List;

@Path("categories")
@Api(value = "categories")
public interface CategoryResource {
	/*
	 * Get categories/all
	 */
	@GET
	@Path("all")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets all categories.")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "No category was found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@PermitAll
	List<Category> getAll(
			@ApiParam(hidden = true) @Auth Session session
			);

	/*
	 * GET categories/<categoryId>
	 */
	@GET
	@Path("{categoryId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get category by id")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "Category not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("category.search")
	Category getById(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("categoryId") long categoryId
			);

	/*
	 * GET categories/<categoryId>/<memeberType>
	 */
	@GET
	@Path("{categoryId}/{memberType}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get the member of a category defined by id. Member type can be Device, Group, HWConf, Room, Sofwtware, User, FAQ, Announcement, Contact")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "Category not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("category.search")
	List<Long> getMember(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("categoryId") long categoryId,
			@PathParam("memberType") String memberType

			);

	/*
	 * GET categories/<categoryId>/available/<memeberType>
	 */
	@GET
	@Path("{categoryId}/available/{memberType}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get the non member of a category defined by id. Member type can be Device, Group, HWConf, Room, Sofwtware, User, FAQ, Announcement, Contact")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "Category not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("category.search")
	List<Long> getAvailableMember(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("categoryId") long categoryId,
			@PathParam("memberType") String memberType
			);

	/*
	 * GET categories/search/{search}
	 */
	@GET
	@Path("search/{search}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Search for category by name and description.")
	@ApiResponses(value = {
			// TODO so oder anders? @ApiResponse(code = 404, message = "At least one user was not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	//@PermitAll
	@RolesAllowed("category.search")
	List<Category> search(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("search") String search
			);

	/*
   	 * POST categories/getCAtegories
   	 */
       @POST
       @Path("getCategories")
       @Produces(JSON_UTF8)
       @ApiOperation(value = "Gets a list of category objects to the list of categoryIds.")
       @ApiResponses(value = {
               @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
       @RolesAllowed("category.search")
       List<Category> getCategories(
               @ApiParam(hidden = true) @Auth Session session,
               List<Long> categoryIds
       );
       
	/*
	 * POST categories/add { hash }
	 */
	@POST
	@Path("add")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Create new category")
	@ApiResponses(value = {
			// TODO so oder anders? @ApiResponse(code = 404, message = "At least one user was not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	//@PermitAll
	@RolesAllowed("category.add")
	CrxResponse add(
			@ApiParam(hidden = true) @Auth Session session,
			Category category
			);
	
	/*
	 * POST categories/modify { hash }
	 */
	@POST
	@Path("{categoryId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Modify a category")
	@ApiResponses(value = {
			// TODO so oder anders? @ApiResponse(code = 404, message = "At least one user was not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	//@PermitAll
	@RolesAllowed("category.modify")
	CrxResponse modify(
			@ApiParam(hidden = true) @Auth Session session,
			Category category
			);

	/*
	 * PUT categories/<categoryId>/<memeberType>/<memberId>
	 */
	@PUT
	@Path("{categoryId}/{memberType}/{memberId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Add member to category defined by id. Member type can be Device, Group, HWConf, Room, Sofwtware, User, Announcement, FAQ or Contact")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "Category not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("category.modify")
	CrxResponse addMember(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("categoryId") long categoryId,
			@PathParam("memberType") String memberType,
			@PathParam("memberId")   long memberId
			);

	/*
	 * DELETE categories/<categoryId>
	 */
	@DELETE
	@Path("{categoryId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delets a category defined by id.")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "Category not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("category.delete")
	CrxResponse delete(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("categoryId") long categoryId
			);

	/*
	 * DELETE categories/<categoryId>/<memeberType>/<memberId>
	 */
	@DELETE
	@Path("{categoryId}/{memberType}/{memberId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Remove a member of a category defined by id. Member type can be Device, Group, HWConf, Room, Sofwtware, User, FAQ, Announcement, Contact")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "Category not found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("category.modify")
	CrxResponse removeMember(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("categoryId") long categoryId,
			@PathParam("memberType") String memberType,
			@PathParam("memberId") long memberId
			);

}
