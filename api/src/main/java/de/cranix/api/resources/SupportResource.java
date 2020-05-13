/* (c) 2018 EXTIS GmbH - all rights reserved */
package de.cranix.api.resources;

import static de.cranix.api.resources.Resource.*;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import de.cranix.dao.CrxResponse;
import de.cranix.dao.Session;
import de.cranix.dao.SupportRequest;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Path("support")
@Api(value = "support")
public interface SupportResource {
	@POST
	@Path("create")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "create a support request ")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Missing data for request"),
			@ApiResponse(code = 500, message = "Server broken, please contact administrator") })
	@RolesAllowed("device.manage")
	CrxResponse create(@ApiParam(hidden = true) @Auth Session session, SupportRequest supportRequest);

}
