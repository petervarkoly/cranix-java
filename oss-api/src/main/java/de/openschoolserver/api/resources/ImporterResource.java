package de.openschoolserver.api.resources;

import static de.openschoolserver.api.resources.Resource.JSON_UTF8;



import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import javax.ws.rs.core.MediaType;

import de.claxss.importlib.ImporterDescription;
import de.openschoolserver.dao.Session;
import de.openschoolserver.dao.User;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import java.io.InputStream;

@Path("importer")
@Api(value = "importer")
public interface ImporterResource {

	 /*
   	 * GET groups/<groupId>/members
   	 */
       @GET
       @Path("{objecttype}/availableimporters")
       @Produces(JSON_UTF8)
       @ApiOperation(value = "Get the list of available import descriptions for an object type like PERSON, SCHOOLCLASS, GROUP, SUBJECT, ROOM, PLAN, SCHOOL")
       @ApiResponses(value = {
               @ApiResponse(code = 404, message = "Group not found"),
               @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
       @RolesAllowed("device.manage")
       List<ImporterDescription> getAvailableImporters(
               @ApiParam(hidden = true) @Auth Session session,
               @PathParam("objecttype") String objecttype
       );
       
       @GET
	    @Path("processImportUser")
	    @Produces(JSON_UTF8)
	    @ApiOperation(
	            value = "process the import user data"
	    )
	    @ApiResponses(value = {
	            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	    })
       @RolesAllowed("device.manage")
	    List<User> processImportUser(
	            @ApiParam(hidden = true) @Auth Session session            
	    );
       
       @POST
       @Path("uploadImport")
       @Produces(JSON_UTF8)
       @Consumes(MediaType.MULTIPART_FORM_DATA)
       @ApiOperation(
	            value = "imports data upload"
	    ) @ApiResponses(value = {
	        
	            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	    })
       String uploadImport(@ApiParam(hidden = true) @Auth Session session,

               @FormDataParam("file") final InputStream fileInputStream,
               @FormDataParam("file") final FormDataContentDisposition contentDispositionHeader);
       
}
