/* (c) 2020 Péter Varkoly <peter@varkoly.de> - all rights reserved */
/* (c) 2018 EXTIS GmbH - all rights reserved */
package de.cranix.api.resources;

import static de.cranix.api.resources.Resource.*;

import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import de.cranix.dao.CrxResponse;
import de.cranix.dao.Session;
import de.cranix.dao.SupportRequest;
import de.cranix.helper.CrxEntityManagerFactory;
import de.cranix.services.SupportService;

import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Path("support")
@Api(value = "support")
public class SupportResource {

	public SupportResource() {}

	@POST
	@Path("create")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Create a support request.")
	@ApiResponses(value = {
		@ApiResponse(code = 400, message = "Missing data for request"),
		@ApiResponse(code = 500, message = "Server broken, please contact administrator") })
	@RolesAllowed("system.support")
	public CrxResponse create(
			@ApiParam(hidden = true) @Auth Session session,
		       	SupportRequest supportRequest)
       	{
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new SupportService(session,em).create(supportRequest);
		em.close();
		return resp;
	}

}
