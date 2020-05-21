/* (c) 2020 Peter Varkoly <peter@varkoly.de> - all rights reserved */
/* (c) 2016 EXTIS GmbH - all rights reserved */
package de.cranix.api.resources;


import static de.cranix.api.resources.Resource.*;

import io.dropwizard.auth.Auth;
import io.swagger.annotations.*;
import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import de.cranix.dao.Session;
import java.util.List;
import java.util.Map;

@Path("sessions")
@Api(value = "sessions")
@SwaggerDefinition(securityDefinition = @SecurityDefinition(apiKeyAuthDefinitions = {@ApiKeyAuthDefinition(
	       key = "apiKeyAuth", name = "Authorization", in = ApiKeyAuthDefinition.ApiKeyLocation.HEADER)
		}))
public interface SessionsResource {

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Creates a new session and delivers the session.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "Login is incorrect"),
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    Session createSession(
            @Context UriInfo ui,
            @FormParam("username") String username,
            @FormParam("password") String password,
            @FormParam("device") String device,
            @Context HttpServletRequest req
    );

    @POST
    @Path("create")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Creates a new session and delivers the token.",
	notes = "Following parameter are required:<br>"
	+ "'username' The login name of the user."
	+ "'password' The password of the user."
    )
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "Login is incorrect"),
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    Session createSession(
            @Context UriInfo ui,
            @Context HttpServletRequest req,
            Map<String,String> loginDatas
    );

    @POST
    @Path("login")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(TEXT)
    @ApiOperation(value = "Creates a new session and delivers the token.")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "Login is incorrect"),
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    String createToken(
            @Context UriInfo ui,
            @FormParam("username") String username,
            @FormParam("password") String password,
            @FormParam("device") String device,
            @Context HttpServletRequest req
    );

    @GET
    @Produces(JSON_UTF8)
    @ApiOperation(value = "get session status")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "Token is not valid or no token given"),
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @PermitAll
    Session getStatus(
            @ApiParam(hidden = true) @Auth Session session
    );

    @DELETE
    @Path("{token}")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "delete session")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "Token is not valid or no token given"),
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @PermitAll
    void deleteSession(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("token") String token
    );

    @GET
    @Path("{key}")
    @Produces(TEXT)
    @ApiOperation(value = "Get some session values. Available keys are: defaultPrinter, availablePrinters, dnsName, domainName.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @PermitAll
    String getSessionValue(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("key") String key
    );

    @GET
    @Path("allowedModules")
    @Produces(JSON_UTF8)
    @ApiOperation(value = "Get the list of allowed modules for the session user.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @PermitAll
    List<String> allowedModules(
            @ApiParam(hidden = true) @Auth Session session
    );

    @GET
    @Path("logonScript/{OS}")
    @Produces(TEXT)
    @ApiOperation(value = "Get the logo on script for the user and operating system.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @PermitAll
    String logonScript(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("OS") String OS
     );
}
