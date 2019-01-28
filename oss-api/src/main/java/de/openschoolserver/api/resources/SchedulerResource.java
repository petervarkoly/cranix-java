package de.openschoolserver.api.resources;
import static de.openschoolserver.api.resources.Resource.JSON_UTF8;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.POST;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import de.openschoolserver.dao.OssResponse;
import de.openschoolserver.dao.Session;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Path("scheduler")
@Api(value = "scheduler")
public interface SchedulerResource {
	
	/*
     * DELETE scheduler/guestuser
     */
    @DELETE
	@Path("rooms/{roomId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Deletes the expiered guest users.")
	@ApiResponses(value = {
			@ApiResponse(code = 404, message = "No category was found"),
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("scheduler.manage")
	OssResponse deleteExpieredGuestUser(
			@ApiParam(hidden = true) @Auth Session session
	);

}
