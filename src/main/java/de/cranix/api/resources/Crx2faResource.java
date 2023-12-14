package de.cranix.api.resources;


import de.cranix.dao.Crx2fa;
import de.cranix.dao.CrxResponse;
import de.cranix.dao.Session;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import java.util.List;

import static de.cranix.api.resources.Resource.JSON_UTF8;

@Path("2fa")
@Api(value = "2fa")
@Produces(JSON_UTF8)
public class Crx2faResource {

    Logger logger = LoggerFactory.getLogger(Crx2faResource.class);

    public Crx2faResource() {
    }

    @POST
    @RolesAllowed("2fa.use")
    CrxResponse add(
            @ApiParam(hidden = true) @Auth Session session,
            Crx2fa crx2fa
    ){

    }
    @GET
    @RolesAllowed("2fa.use")
    List<Crx2fa> get(
            @ApiParam(hidden = true) @Auth Session session,
            Crx2fa crx2fa
    ){
        return  session.getUser().getCrx2fas();
    }
}
