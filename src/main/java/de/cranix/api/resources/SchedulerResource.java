/* (c) 2021 Peter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.api.resources;
import static de.cranix.api.resources.Resource.JSON_UTF8;

import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import de.cranix.dao.Category;
import de.cranix.dao.CrxResponse;
import de.cranix.dao.Session;
import de.cranix.services.UserService;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import de.cranix.helper.CrxEntityManagerFactory;

@Path("scheduler")
@Api(value = "scheduler")
public class SchedulerResource {

	public SchedulerResource() {}

	@DELETE
	@Path("rooms/{roomId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Deletes the expiered guest users.")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "No category was found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("scheduler.manage")
	public CrxResponse deleteExpieredGuestUser( @ApiParam(hidden = true) @Auth Session session) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		UserService   uc = new UserService(session,em);
		Query query = em.createNamedQuery("Category.expiredByType").setParameter("type", "guestUser");
		Integer counter = 0;
		for(Category category : (List<Category>) query.getResultList() ) {
			uc.deleteGuestUsers(category.getId());
			counter++;
		}
		em.close();
		if( counter == 0 ) {
			return new CrxResponse(session,"OK","No guest user accounts to delete.");
		}
		return new CrxResponse(session,"OK","%s guest user groups was deleted.",null,counter.toString());
	}
}
