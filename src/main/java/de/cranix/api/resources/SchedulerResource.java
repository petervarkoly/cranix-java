/* (c) 2021 Peter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.api.resources;

import de.cranix.dao.CrxResponse;
import de.cranix.dao.Session;
import de.cranix.helper.CrxEntityManagerFactory;
import de.cranix.services.GuestUserService;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.*;

import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import static de.cranix.api.resources.Resource.JSON_UTF8;

@Path("scheduler")
@Api(value = "scheduler")
public class SchedulerResource {

    public SchedulerResource() {
    }

    @DELETE
    @Path("rooms/{roomId}")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Deletes the expiered guest users.")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "No category was found"),
            @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
    @RolesAllowed("scheduler.manage")
    public CrxResponse deleteExpiredGuestUser(@ApiParam(hidden = true) @Auth Session session) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        Integer counter = new GuestUserService(session, em).deleteExpiredGuestUser();
        em.close();
        if (counter == 0) {
            return new CrxResponse("OK", "No guest user accounts to delete.");
        }
        return new CrxResponse("OK", "%s guest user groups was deleted.", null, counter.toString());
    }
}
