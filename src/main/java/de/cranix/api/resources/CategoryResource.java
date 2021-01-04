/* (c) 2020 Péter Varkoly <peter@varkoly.de> - all rights reserved */
/**
 * @author Peter Varkoly <peter@varkoly.de>
 *
 */
package de.cranix.api.resources;

import static de.cranix.api.resources.Resource.JSON_UTF8;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
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
import de.cranix.services.CategoryService;
import de.cranix.helper.CommonEntityManagerFactory;

import java.util.List;

@Path("categories")
@Api(value = "categories")
public class CategoryResource {

	public CategoryResource() {
	}

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
	public List<Category> getAll( @ApiParam(hidden = true) @Auth Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<Category> resp = new CategoryService(session,em).getAll();
		em.close();
		return resp;
	}

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
	public Category getById(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("categoryId") long categoryId
	)  {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		Category resp = new CategoryService(session,em).getById(categoryId);
		em.close();
		return resp;
	}

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
	public List<Long> getMember(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("categoryId") long categoryId,
		@PathParam("memberType") String memberType
	) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<Long> resp = new CategoryService(session,em).getMembers(categoryId,memberType);
		em.close();
		return resp;
	}

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
	public List<Long> getAvailableMember(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("categoryId") long categoryId,
		@PathParam("memberType") String memberType
	) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<Long> resp = new CategoryService(session,em).getAvailableMembers(categoryId,memberType);
		em.close();
		return resp;
	}

	/*
	 * POST categories/add { hash }
	 */
	@POST
	@Path("add")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Create new category")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("category.add")
	public CrxResponse add(
		@ApiParam(hidden = true) @Auth Session session,
		Category category
	) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new CategoryService(session,em).add(category);
		em.close();
		return resp;
	}
	
	/*
	 * POST categories/modify { hash }
	 */
	@POST
	@Path("{categoryId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Modify a category")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("category.modify")
	public CrxResponse modify(
		@ApiParam(hidden = true) @Auth Session session,
		Category category
	) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new CategoryService(session,em).modify(category);
		em.close();
		return resp;
	}
	
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
	public CrxResponse addMember(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("categoryId") long categoryId,
		@PathParam("memberType") String memberType,
		@PathParam("memberId")   long memberId
	) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new CategoryService(session,em).addMember(categoryId, memberType, memberId);
		em.close();
		return resp;
	}

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
	public CrxResponse delete(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("categoryId") long categoryId
	) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new CategoryService(session,em).delete(categoryId);
		em.close();
		return resp;
	}

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
	public CrxResponse removeMember(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("categoryId") long categoryId,
		@PathParam("memberType") String memberType,
		@PathParam("memberId") long memberId
	) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new CategoryService(session,em).deleteMember(categoryId, memberType, memberId);
		em.close();
		return resp;
	}
}
