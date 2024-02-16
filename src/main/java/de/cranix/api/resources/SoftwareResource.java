/* (c) 2021 Peter Varkoly <pvarkoly@cephalix.eu> - all rights reserved */
package de.cranix.api.resources;

import de.cranix.dao.*;
import de.cranix.services.*;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.*;
import org.apache.commons.lang3.SerializationUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.cranix.dao.Category;
import de.cranix.dao.Device;
import de.cranix.dao.HWConf;
import de.cranix.dao.CrxBaseObject;
import de.cranix.dao.CrxResponse;
import de.cranix.dao.Room;
import de.cranix.dao.Session;
import de.cranix.dao.Software;
import de.cranix.dao.SoftwareLicense;
import de.cranix.dao.SoftwareStatus;
import de.cranix.dao.SoftwareVersion;
import de.cranix.helper.CrxEntityManagerFactory;

import static de.cranix.api.resources.Resource.JSON_UTF8;
import static de.cranix.api.resources.Resource.TEXT;

@Path("softwares")
@Api(value = "softwares")
public class SoftwareResource {

	Logger logger = LoggerFactory.getLogger(SoftwareResource.class);

	public SoftwareResource() {}

	@GET
	@Path("all")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets all Softwares.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "No category was found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("software.manage")
	public List<Software> getAll( @ApiParam(hidden = true) @Auth Session session) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                List<Software> resp = new SoftwareService(session,em).getAll();
                em.close();
                return resp;
        }

	@GET
	@Path("allInstallable")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets all Softwares.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "No category was found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("software.manage")
	public List<Software> getAllInstallable( @ApiParam(hidden = true) @Auth Session session) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                List<Software> resp = new SoftwareService(session,em).getAllInstallable();
                em.close();
                return resp;
        }

	@GET
	@Path("{softwareId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get software by id")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "Software not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("software.manage")
	public Software getById(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("softwareId") long softwareId
	) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                Software resp = new SoftwareService(session,em).getById(softwareId);
                em.close();
                return resp;
        }

	@POST
	@Path("add")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Add a new version of software. If the software does not exists this will be create.<br>" +
			"If the software does exists all older versions will be set to 'R'eplaced and the current version to 'C'.<br>" +
			"The software version must be given! Now we only provides one actual version.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("software.add")
	public CrxResponse add(
		@ApiParam(hidden = true) @Auth Session session,
		Software software
	) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                CrxResponse resp = new SoftwareService(session,em).add(software,true);
                em.close();
                return resp;
        }

	@POST
	@Path("{softwareId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Modify a software")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("software.modify")
	public CrxResponse modify(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("softwareId") long softwareId,
		Software software
	) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
				software.setId(softwareId);
                CrxResponse resp = new SoftwareService(session,em).modify(software);
                em.close();
                return resp;
        }

	@DELETE
	@Path("{softwareId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delets a software defined by id.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "Software not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("software.delete")
	public CrxResponse delete(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("softwareId") long softwareId
	) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                CrxResponse resp = new SoftwareService(session,em).delete(softwareId);
                em.close();
                return resp;
        }

	@POST
	@Path("addRequirements")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Add a software requirement by name")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("software.modify")
	public CrxResponse addRequirements(
		@ApiParam(hidden = true) @Auth Session session,
		List<String> requirement
	) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                CrxResponse resp = new SoftwareService(session,em).addRequirements(requirement);
                em.close();
                return resp;
        }

	@PUT
	@Path("{softwareId}/{requirementId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Add a software requirement to a software")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("software.modify")
	public CrxResponse addRequirements(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("softwareId") long softwareId,
		@PathParam("requirementId") long requirementId
	) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                CrxResponse resp = new SoftwareService(session,em).addRequirements(softwareId,requirementId);
                em.close();
                return resp;
        }

	@DELETE
	@Path("{softwareId}/{requirementId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Remove a software requirement from a software")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("software.modify")
	public CrxResponse deleteRequirements(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("softwareId") long softwareId,
		@PathParam("requirementId") long requirementId
	) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                CrxResponse resp = new SoftwareService(session,em).deleteRequirements(softwareId,requirementId);
                em.close();
                return resp;
        }

	//############################
	// Manage software licenses  #
	//############################
	@POST
	@Path("{softwareId}/license")
	@Produces(JSON_UTF8)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation(value = "Creates licence(s) to a software")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("software.modify")
	public CrxResponse addLicenseToSoftware(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("softwareId") long softwareId,
		@FormDataParam("licenseType") Character licenseType,
		@FormDataParam("count") Integer count,
		@FormDataParam("value") String value,
		@FormDataParam("file") final InputStream fileInputStream,
		@FormDataParam("file") final FormDataContentDisposition contentDispositionHeader
	) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                SoftwareLicense softwareLicense = new SoftwareLicense();
                softwareLicense.setValue(value);
                softwareLicense.setCount(count);
                softwareLicense.setLicenseType(licenseType);
                CrxResponse resp = new SoftwareService(session,em).addLicenseToSoftware(
                                softwareLicense,
                                softwareId,
                                fileInputStream,
                                contentDispositionHeader);
                em.close();
                return resp;
        }

	@GET
	@Path("{softwareId}/license")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the licences to a software")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("software.modify")
	public List<SoftwareLicense> getSoftwareLicenses(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("softwareId") long softwareId
	) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                List<SoftwareLicense> licenses = new ArrayList<SoftwareLicense>();
                for( SoftwareLicense license : new SoftwareService(session,em).getById(softwareId).getSoftwareLicenses() ) {
                        license.setUsed(license.getDevices().size());
                        licenses.add(license);
                }
                em.close();
                return licenses;
        }

	@POST
	@Path("licenses/{licenseId}")
	@Produces(JSON_UTF8)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation(value = "Modifies an existing license.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("software.modify")
	public CrxResponse modifyLicense(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("licenseId") long licenseId,
		@FormDataParam("licenseType") Character licenseType,
		@FormDataParam("count") Integer count,
		@FormDataParam("value") String value,
		@FormDataParam("file") final InputStream fileInputStream,
		@FormDataParam("file") final FormDataContentDisposition contentDispositionHeader
	) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                SoftwareService  softwareService = new SoftwareService(session,em);
                SoftwareLicense softwareLicense = softwareService.getSoftwareLicenseById(licenseId);
                softwareLicense.setCount(count);
                softwareLicense.setValue(value);
                softwareLicense.setLicenseType(licenseType);
                CrxResponse resp = softwareService.modifySoftwareLicense(
                                softwareLicense,fileInputStream,contentDispositionHeader);
                em.close();
                return resp;
        }

	@DELETE
	@Path("licenses/{licenseId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Deletes an existing licence.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("software.modify")
	public CrxResponse deleteLicense(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("licenseId") long licenseId
	) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                CrxResponse resp = new SoftwareService(session,em).deleteLicence(licenseId);
                em.close();
                return resp;
        }

	/* ########################################
	 * Functions to manage software download  #
	 * ########################################
	 */
	@GET
	@Path("available")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the available softwares from the CEPHALIX repository.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "No category was found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator"),
		@ApiResponse(code = 600, message = "Connection to CEPHALIX software repository server is broken.")})
	@RolesAllowed("software.download")
	public List<Map<String, String>> getAvailable( @ApiParam(hidden = true) @Auth Session session) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                List<Map<String, String>> resp = new SoftwareService(session,em).getAvailableSoftware();
                em.close();
                return resp;
        }

	@POST
	@Path("download")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Downloads softwares from the CEPHALIX repository.",
			notes = "The call must provide a list of softwares to be downloaded: "
			+ "[ \"MSWhatever\", \"AnOtherProgram\" ] "
			+ "The requirements will be solved automaticaly.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "No category was found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator"),
		@ApiResponse(code = 600, message = "Connection to CEPHALIX software repository server is broken.")})
	@RolesAllowed("software.download")
	public CrxResponse download(
		@ApiParam(hidden = true) @Auth Session session,
		List<String> softwares
	) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                CrxResponse resp = new SoftwareService(session,em).downloadSoftwares(softwares);
                em.close();
                return resp;
        }

	@PUT
	@Path("download/{softwareName}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Downloads a software from the CEPHALIX repository.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "No category was found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator"),
		@ApiResponse(code = 600, message = "Connection to CEPHALIX software repository server is broken.")})
	@RolesAllowed("software.download")
	public CrxResponse downloadOne(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("softwareName") String softwareName
	) {
                List<String> softwares = new ArrayList<String>();
                softwares.add(softwareName);
		return this.download(session,softwares);
        }

	@POST
	@Path("deleteDownloadedSoftwares")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delets software downloaded from the CEPHALIX repository.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "No category was found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator"),
		@ApiResponse(code = 600, message = "Connection to CEPHALIX software repository server is broken.")})
	@RolesAllowed("software.download")
	public CrxResponse deleteDownloadedSoftwares(
		@ApiParam(hidden = true) @Auth Session session,
		List<String> softwares
	) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                CrxResponse resp = new SoftwareService(session,em).deleteDownloadedSoftwares(softwares);
                em.close();
                return resp;
        }

	@GET
	@Path("downloadStatus")
	@Produces(TEXT)
	@ApiOperation(value = "Gets the names of the packages being dowloaded. Empty string means no download proceded.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")
	})
	@RolesAllowed("software.download")
	public String downloadStatus( @ApiParam(hidden = true) @Auth Session session) {
                try {
                        return  String.join(" ", Files.readAllLines(Paths.get("/run/lock/cranix-api/crx_download_packages")));
                } catch( IOException e ) {
                        return "";
                }
        }

	@POST
	@Path("listDownloadedSoftware")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "List the status of the downloaded software. ",
			notes = "This call delivers a list of maps of the downloaded software.<br>"
			+ "[ {<br>"
			+ "	name : name of the software,<br>"
			+ "	versions : the version of the software,<br>"
			+ "	update : this field contains the version of the available update,<br>"
			+ "	updateDescription : this field contains the desription of the available update,<br>"
			+ "} ]")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "No category was found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("software.install")
	public List<Map<String, String>> listDownloadedSoftware( @ApiParam(hidden = true) @Auth Session session) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                List<Map<String, String>> resp = new SoftwareService(session,em).listDownloadedSoftware();
                em.close();
                return resp;
        }

	@GET
	@Path("listUpdatesForSoftwarePackages")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "List the status of the downloaded software. ",
			notes = "This call delivers a list of maps of the downloaded software.<br>"
			+ "[ {<br>"
			+ "	name : name of the software,<br>"
			+ "	versions : the new version of the software,<br>"
			+ "} ]")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "No category was found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("software.install")
	public List<Map<String, String>> listUpdatesForSoftwarePackages( @ApiParam(hidden = true) @Auth Session session) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                List<Map<String, String>> resp = new SoftwareService(session,em).listUpdatesForSoftwarePackages();
                em.close();
                return resp;
        } 

	@POST
	@Path("updatesSoftwares")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Downloads softwares from the CEPHALIX repository.",
			notes = "The call must provide a list of softwares to be downloaded: "
			+ "[ \"MSWhatever\", \"AnOtherProgram\" ] "
			+ "The requirements will be solved automaticaly.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "No category was found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator"),
		@ApiResponse(code = 600, message = "Connection to CEPHALIX software repository server is broken.")})
	@RolesAllowed("software.download")
	public CrxResponse updatesSoftwares(
		@ApiParam(hidden = true) @Auth Session session,
		List<String> softwares
	) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                CrxResponse resp = new SoftwareService(session,em).updateSoftwares(softwares);
                em.close();
                return resp;
        }

	@PUT
	@Path("saveState")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Creates the salt state files for the minions.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "Software not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("software.modify")
	public CrxResponse apply( @ApiParam(hidden = true) @Auth Session session ) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                CrxResponse resp = new SoftwareService(session,em).applySoftwareStateToHosts();
                em.close();
                return resp;
        }

	@PUT
	@Path("applyState")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Applies the high states created in the salt state files for the minions.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "Software not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("software.modify")
	public CrxResponse applyState(
		@ApiParam(hidden = true) @Auth Session session
	) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                DeviceService deviceService = new DeviceService(session,em);
                for( Device device : deviceService.getAll() ) {
                        deviceService.manageDevice(device, "applyState", null);
                }
                em.close();
                return new CrxResponse(session,"OK","Salt High State was applied on all running minions.");
        }

	@POST
	@Path("installations")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Creates a software installation")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("software.install")
	public CrxResponse createInstallation(
		@ApiParam(hidden = true) @Auth Session session,
			Category category
	) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                SoftwareSetService sc = new SoftwareSetService(session,em);
                CrxResponse response = new SoftwareSetService(session,em).addSet(category);
                em.close();
				return response;

	} 

	@POST
	@Path("installations/{installationId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Modifies a software installation")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("software.install")
	public CrxResponse modifyInstallation(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("installationId") Long installationId,
		Category category
	) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                CrxResponse response = new SoftwareSetService(session,em).modifySet(installationId, category);
                em.close();
                return response;
        }

	@DELETE
	@Path("installations/{installationId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delets a defined installation.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "Software not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("software.install")
	public CrxResponse deleteInstalation(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("installationId") long installationId
	) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                CategoryService categoryService = new CategoryService(session,em);
                CrxResponse crxResponse = categoryService.delete(installationId);
                if( crxResponse.getCode().equals("OK") ) {
                        crxResponse = this.apply(session);
                }
                em.close();
                return crxResponse;
        }

	@GET
	@Path("installations")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets all installations")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("software.install")
	public List<Category> getInstallations( @ApiParam(hidden = true) @Auth Session session) {
	       	EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                List<Category> resp = new CategoryService(session,em).getByType("installation");
                em.close();
                return resp;
        }

	@GET
	@Path("installations/{installationId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets an installations by id.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("software.install")
	public Category getInstallation(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("installationId") long installationId
	) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                Category resp = new CategoryService(session,em).getById(installationId);
                em.close();
                return resp;
        }

	@PUT
	@Path("installations/{installationId}/softwares/{softwareId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Adds a software to an installation.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "Software not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("software.install")
	public CrxResponse addSoftwareToInstalation(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("installationId") long installationId,
		@PathParam("softwareId") long softwareId
	) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                CrxResponse resp = new SoftwareService(session,em).addSoftwareToCategory(softwareId,installationId);
                em.close();
                return resp;
        }

	@PUT
	@Path("installations/{installationId}/devices/{deviceId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Adds a device to an installation.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "Software not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("software.install")
	public CrxResponse addDeviceToInstalation(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("installationId") long installationId,
		@PathParam("deviceId") long deviceId
	) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                CategoryService categoryService = new CategoryService(session,em);
                CrxResponse resp = categoryService.addMember(installationId, "Device", deviceId);
                em.close();
                return resp;
        }

	@PUT
	@Path("installations/{installationId}/rooms/{roomId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Adds a room to an installation.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "Software not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("software.install")
	public CrxResponse addRoomToInstalation(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("installationId") long installationId,
		@PathParam("roomId") long roomId
	) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                CategoryService categoryService = new CategoryService(session,em);
                CrxResponse resp = categoryService.addMember(installationId, "Room", roomId);
                em.close();
                return resp;
        }

	@PUT
	@Path("installations/{installationId}/hwconfs/{hwconfId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Adds a hwconf to an installation.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "Software not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("software.install")
	public CrxResponse addHWConfToInstalation(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("installationId") long installationId,
		@PathParam("hwconfId") long hwconfId
	) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                CategoryService categoryService = new CategoryService(session,em);
                CrxResponse resp = categoryService.addMember(installationId, "HWConf", hwconfId);
                em.close();
                return resp;
        }

	@DELETE
	@Path("installations/{installationId}/softwares/{softwareId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delets a software from an installation.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "Software not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("software.install")
	public CrxResponse deleteSoftwareFromInstalation(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("installationId") long installationId,
		@PathParam("softwareId") long softwareId
	) { 
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager(); 
                SoftwareService softwareService = new SoftwareService(session,em); 
                CrxResponse resp = softwareService.deleteSoftwareFromCategory(softwareId,installationId);
                em.close();
                return resp;
        }

	@DELETE
	@Path("installations/{installationId}/devices/{deviceId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delets a device from an installation.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "Software not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("software.install")
	public CrxResponse deleteDeviceFromInstalation(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("installationId") long installationId,
		@PathParam("deviceId") long deviceId
	) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                CategoryService categoryService = new CategoryService(session,em);
                CrxResponse resp = categoryService.deleteMember(installationId, "Device", deviceId);
                em.close();
                return resp;
        }

	@DELETE
	@Path("installations/{installationId}/rooms/{roomId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delets a room from an installation.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "Software not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("software.install")
	public CrxResponse deleteRoomFromInstalation(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("installationId") long installationId,
		@PathParam("roomId") long roomId
	) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                CategoryService categoryService = new CategoryService(session,em);
                CrxResponse resp = categoryService.deleteMember(installationId, "Room", roomId);
                em.close();
                return resp;
        }

	@DELETE
	@Path("installations/{installationId}/hwconfs/{hwconfId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Deletes a hwconf from an installation.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "Software not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("software.install")
	public CrxResponse deleteHWConfFromInstalation(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("installationId") long installationId,
		@PathParam("hwconfId") long hwconfId
	) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                CategoryService categoryService = new CategoryService(session,em);
                CrxResponse resp = categoryService.deleteMember(installationId, "HWConf", hwconfId);
                em.close();
                return resp;
        }

	@GET
	@Path("installations/{installationId}/softwares")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the list of softwares in an installation.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "Software not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("software.install")
	public List<CrxBaseObject> getSoftwares(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("installationId") long installationId
	) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                Category category = new CategoryService(session,em).getById(installationId);
                List<CrxBaseObject> objects = new ArrayList<CrxBaseObject>();
                for( Software object : category.getSoftwares() ) {
                        objects.add(new CrxBaseObject(object.getId(),object.getName()));
                }
                em.close();
                return objects;
        }

	@GET
	@Path("installations/{installationId}/devices")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the list of devices in an installation.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "Software not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("software.install")
	public List<CrxBaseObject> getDevices(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("installationId") long installationId
	) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                Category category = new CategoryService(session,em).getById(installationId);
                List<CrxBaseObject> objects = new ArrayList<CrxBaseObject>();
                for( Device object : category.getDevices() ) {
                        objects.add(new CrxBaseObject(object.getId(),object.getName()));
                }
                em.close();
                return objects;
        }

	@GET
	@Path("installations/{installationId}/rooms")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the list of rooms in an installation.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "Software not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("software.install")
	public List<CrxBaseObject> getRooms(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("installationId") long installationId
	) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                Category category = new CategoryService(session,em).getById(installationId);
                List<CrxBaseObject> objects = new ArrayList<CrxBaseObject>();
                for( Room object : category.getRooms() ) {
                        objects.add(new CrxBaseObject(object.getId(),object.getName()));
                }
                em.close();
                return objects;
        }

	@GET
	@Path("installations/{installationId}/hwconfs")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the list of hwconfs in an installation.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "Software not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("software.install")
	public List<CrxBaseObject> getHwconfs(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("installationId") long installationId
	) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                Category category = new CategoryService(session,em).getById(installationId);
                List<CrxBaseObject> objects = new ArrayList<CrxBaseObject>();
                for( HWConf object : category.getHwconfs() ) {
                        objects.add(new CrxBaseObject(object.getId(),object.getName()));
                }
                em.close();
                return objects;
        }

	@GET
	@Path("installations/{installationId}/available/softwares")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the list of softwares in an installation.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "Software not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("software.install")
	public List<CrxBaseObject> getAvailableSoftwares(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("installationId") long installationId
	) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                SoftwareService sc = new SoftwareService(session,em);
                List<CrxBaseObject> objects = new ArrayList<CrxBaseObject>();
                Category installationSet = new CategoryService(session,em).getById(installationId);
                for( Software software : sc.getAllInstallable() ) {
                        if( !installationSet.getSoftwares().contains(software) ) {
                                objects.add(new CrxBaseObject(software.getId(),software.getName()));
                        }
                }
                em.close();
                return objects;
        }

	@GET
	@Path("installations/{installationId}/available/devices")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the list of devices in an installation.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "Software not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("software.install")
	public List<CrxBaseObject> getAvailableDevices(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("installationId") long installationId
	) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                List<CrxBaseObject> objects = new ArrayList<CrxBaseObject>();
                DeviceService dc = new DeviceService(session,em);
                for( Long deviceId : new CategoryService(session,em).getAvailableMembers(installationId, "Device") ) {
                        Device device = dc.getById(deviceId);
                        if( device != null && device.isFatClient() ) {
                                objects.add(new CrxBaseObject(device.getId(),device.getName()));
                        }
                }
                em.close();
                return objects;
        }

	@GET
	@Path("installations/{installationId}/available/rooms")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the list of rooms in an installation.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "Software not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("software.install")
	public List<CrxBaseObject> getAvailableRooms(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("installationId") long installationId
	) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                List<CrxBaseObject> objects = new ArrayList<CrxBaseObject>();
                RoomService rc = new RoomService(session,em);
                for( Long roomId : new CategoryService(session,em).getAvailableMembers(installationId, "Room") ) {
                        Room room = rc.getById(roomId);
                        if( room != null  &&  room.getHwconf() != null && room.getHwconf().getDeviceType().equals("FatClient") ) {
                                objects.add(new CrxBaseObject(room.getId(),room.getName()));
                        }
                }
                em.close();
                return objects;
        }

	@GET
	@Path("installations/{installationId}/available/hwconfs")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the list of hwconfs in an installation.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "Software not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("software.install")
	public List<CrxBaseObject> getAvailableHWConfs(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("installationId") long installationId
	) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                List<CrxBaseObject> objects = new ArrayList<CrxBaseObject>();
                CloneToolService cc = new CloneToolService(session,em);
                for( Long hwconfId : new CategoryService(session,em).getAvailableMembers(installationId, "HWConf") ) {
                        HWConf hwconf = cc.getById(hwconfId);
                        if( hwconf != null && hwconf.getDeviceType().equals("FatClient") ) {
                                objects.add(new CrxBaseObject(hwconf.getId(),hwconf.getName()));
                        }
                }
                em.close();
                return objects;
        }

	/* #######################################################
	 * Functions for the plugin by starting the clients.	 #
	 * In this case only the device name is accessible.	  #
	 * #######################################################
	 *
	 * PUT softwares/devicesByName/{deviceName}/{softwareName}/{version}
	 */
	@PUT
	@Path("devicesByName/{deviceName}/{softwareName}/{version}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Set a software on a device as installed in a given version."
			+ " This will be called by the tool read_installed_software.pl by starting the clients.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "No category was found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("software.install")
	public CrxResponse setSoftwareInstalledOnDevice(
		@ApiParam(hidden = true) @Auth Session session,
		@ApiParam(value = "Name of the device", required = true) @PathParam("deviceName") String deviceName,
		@ApiParam(value = "Name of the software", required = true) @PathParam("softwareName") String softwareName,
		@ApiParam(value = "Software version", required = true) @PathParam("version") String version
	) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                CrxResponse resp = new SoftwareService(session,em).setSoftwareStatusOnDeviceByName(
                                deviceName,
				softwareName,
				softwareName,
				version,
                                "I");
                em.close();
                return resp;
        }

	@POST
	@Path("devicesByName/{deviceName}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Set a software on a device as installed in a given version."
			+ " This will be called by the tool read_installed_software.pl by starting the clients.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "No category was found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("software.install")
	public CrxResponse setSoftwareInstalledOnDevice(
		@ApiParam(hidden = true) @Auth Session session,
		@ApiParam(value = "Name of the device", required = true) @PathParam("deviceName") String deviceName,
			Map<String, String> software
	) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                CrxResponse resp = new SoftwareService(session,em).setSoftwareStatusOnDeviceByName(
                                deviceName,
                                software.get("name"),
                                software.get("description"),
                                software.get("version"),
                                "I");
                em.close();
                return resp;
        }

	@GET
	@Path("devicesByName/{deviceName}/licences")
	@Produces(TEXT)
	@ApiOperation(value = "Set a software on a device as installed in a given version.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "No category was found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("software.install")
	public String getSoftwareLicencesOnDevice(
		@ApiParam(hidden = true) @Auth Session session,
		@ApiParam(value = "Name of the device", required = true) @PathParam("deviceName") String deviceName
	) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                SoftwareService softwareService = new SoftwareService(session,em);
                String resp = softwareService.getSoftwareLicencesOnDevice(deviceName);
                em.close();
                return resp;
        }

	/* ##########################################
	 * Functions to get the installation status
	 * ########################################## */
	@GET
	@Path("status")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "The state of the installation of software(s) on all devices.",
			notes = "A call with ID < 1 as softwareId a list of all softwares in all version on a device. "
			+ "The delivered list has following format:<br>"
			+ "[ {<br>"
			+ "&nbsp;&nbsp;&nbsp;softwareName : Name of the software<br>"
			+ "&nbsp;&nbsp;&nbsp;deviceName   : Name of the device<br>"
			+ "&nbsp;&nbsp;&nbsp;softwareversionId : Id of the SoftwareVersion<br>"
			+ "&nbsp;&nbsp;&nbsp;version	: Version of the software<br>"
			+ "&nbsp;&nbsp;&nbsp;status	 : Installation status of this version<br>"
			+ "&nbsp;&nbsp;&nbsp;manually   : Was the softwar installed manually<br>"
			+ "} ]<br>"
			+ "There are following installation states:<br>"
			+ "I  -> installed<br>"
			+ "IS -> installation scheduled<br>"
			+ "MD -> manuell deinstalled<br>"
			+ "DS -> deinstallation scheduled<br>"
			+ "DF -> deinstallation failed<br>"
			+ "IF -> installation failed"
	)
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "No category was found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("software.install")
	public List<SoftwareStatus> softwareStatus( @ApiParam(hidden = true) @Auth Session session) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                List<SoftwareStatus> resp = new SoftwareService(session,em).getAllStatus();
                em.close();
                return resp;
        }

	@GET
	@Path("devices/{deviceId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "The state of the installation of software(s) on a device.",
			notes = "A call with ID < 1 as softwareId a list of all softwares in all version on a device. "
			+ "The delivered list has following format:<br>"
			+ "[ {<br>"
			+ "&nbsp;&nbsp;&nbsp;softwareName : Name of the software<br>"
			+ "&nbsp;&nbsp;&nbsp;deviceName   : Name of the device<br>"
			+ "&nbsp;&nbsp;&nbsp;softwareversionId : Id of the SoftwareVersion<br>"
			+ "&nbsp;&nbsp;&nbsp;version	: Version of the software<br>"
			+ "&nbsp;&nbsp;&nbsp;status	 : Installation status of this version<br>"
			+ "&nbsp;&nbsp;&nbsp;manually   : Was the softwar installed manually<br>"
			+ "} ]<br>"
			+ "There are following installation states:<br>"
			+ "I  -> installed<br>"
			+ "IS -> installation scheduled<br>"
			+ "MD -> manuell deinstalled<br>"
			+ "DS -> deinstallation scheduled<br>"
			+ "DF -> deinstallation failed<br>"
			+ "IF -> installation failed"
	)
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "No category was found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("software.install")
	public List<SoftwareStatus> getAllSoftwareStatusOnDevice(
		@ApiParam(hidden = true) @Auth Session session,
		@ApiParam(value = "ID of the device", required = true) @PathParam("deviceId") Long deviceId
	) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                List<SoftwareStatus> resp = new SoftwareService(session,em).getAllSoftwareStatusOnDeviceById(deviceId);
                em.close();
                return resp;
        }

	@GET
	@Path("devices/{deviceId}/{softwareId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "The state of the installation of software(s) on a device.",
			notes = "A call with ID < 1 as softwareId a list of all softwares in all version on a device. "
			+ "The delivered list has following format:<br>"
			+ "[ {<br>"
			+ "&nbsp;&nbsp;&nbsp;softwareName : Name of the software<br>"
			+ "&nbsp;&nbsp;&nbsp;deviceName   : Name of the device<br>"
			+ "&nbsp;&nbsp;&nbsp;softwareversionId : Id of the SoftwareVersion<br>"
			+ "&nbsp;&nbsp;&nbsp;version	: Version of the software<br>"
			+ "&nbsp;&nbsp;&nbsp;status	 : Installation status of this version<br>"
			+ "&nbsp;&nbsp;&nbsp;manually   : Was the softwar installed manually<br>"
			+ "} ]<br>"
			+ "There are following installation states:<br>"
			+ "I  -> installed<br>"
			+ "IS -> installation scheduled<br>"
			+ "MD -> manuell deinstalled<br>"
			+ "DS -> deinstallation scheduled<br>"
			+ "DF -> deinstallation failed<br>"
			+ "IF -> installation failed"
	)
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "No category was found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("software.install")
	public List<SoftwareStatus> getSoftwareStatusOnDevice(
		@ApiParam(hidden = true) @Auth Session session,
		@ApiParam(value = "ID of the device", required = true) @PathParam("deviceId") Long deviceId,
		@ApiParam(value = "ID of the software", required = true) @PathParam("sofwtwareId") Long softwareId
	) {
                if( softwareId == null) {
                        return this.getAllSoftwareStatusOnDevice(session, deviceId);
                }
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                List<SoftwareStatus> resp = new SoftwareService(session,em).getSoftwareStatusOnDeviceById(deviceId, softwareId);
                em.close();
                return resp;
        }

	@GET
	@Path("{softwareId}/status")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "the state of the installation of software(s) on all devices.",
			notes = "The delivered list has following format:<br>"
			+ "[ {<br>"
			+ "&nbsp;&nbsp;&nbsp;softwareName : Name of the software<br>"
			+ "&nbsp;&nbsp;&nbsp;deviceName   : Name of the device<br>"
			+ "&nbsp;&nbsp;&nbsp;softwareversionId : Id of the SoftwareVersion<br>"
			+ "&nbsp;&nbsp;&nbsp;version	: Version of the software<br>"
			+ "&nbsp;&nbsp;&nbsp;status	 : Installation status of this version<br>"
			+ "&nbsp;&nbsp;&nbsp;manually   : Was the softwar installed manually<br>"
			+ "} ]<br>"
			+ "There are following installation states:<br>"
			+ "I  -> installed<br>"
			+ "IS -> installation scheduled<br>"
			+ "MD -> manuell deinstalled<br>"
			+ "DS -> deinstallation scheduled<br>"
			+ "DF -> deinstallation failed<br>"
			+ "IF -> installation failed"
	)
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "No category was found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("software.install")
	public List<SoftwareStatus> getSoftwareStatus(
		@ApiParam(hidden = true) @Auth Session session,
		@ApiParam(value = "ID of the software", required = true) @PathParam("softwareId") Long softwareId
	) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                SoftwareService softwareService = new SoftwareService(session,em);
                List<SoftwareStatus> softwareStatus = new ArrayList<SoftwareStatus>();
                Software software = softwareService.getById(softwareId);
                for( SoftwareVersion sv : software.getSoftwareVersions() ) {
                        for( SoftwareStatus st : sv.getSoftwareStatuses() ) {
                                st.setSoftwareName(software.getName());
                                st.setDeviceName(st.getDevice().getName());
                                st.setVersion(sv.getVersion());
                                st.setManually(software.getManually());
                                softwareStatus.add(st);
                        }
                }
                em.close();
                return softwareStatus;
        }

	@GET
	@Path("rooms/{roomId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "the state of the installation status in a room.",
			notes = "The delivered list has following format:<br>"
			+ "[ {<br>"
			+ "&nbsp;&nbsp;&nbsp;softwareName : Name of the software<br>"
			+ "&nbsp;&nbsp;&nbsp;deviceName   : Name of the device<br>"
			+ "&nbsp;&nbsp;&nbsp;softwareversionId : Id of the SoftwareVersion<br>"
			+ "&nbsp;&nbsp;&nbsp;version	: Version of the software<br>"
			+ "&nbsp;&nbsp;&nbsp;status	 : Installation status of this version<br>"
			+ "&nbsp;&nbsp;&nbsp;manually   : Was the softwar installed manually<br>"
			+ "} ]<br>"
			+ "There are following installation states:<br>"
			+ "I  -> installed<br>"
			+ "IS -> installation scheduled<br>"
			+ "MD -> manuell deinstalled<br>"
			+ "DS -> deinstallation scheduled<br>"
			+ "DF -> deinstallation failed<br>"
			+ "IF -> installation failed"
	)
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "No category was found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("software.install")
	public List<SoftwareStatus> getRoomsStatus(
		@ApiParam(hidden = true) @Auth Session session,
		@ApiParam(value = "ID of the software", required = true) @PathParam("roomId") Long roomId
	) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                List<SoftwareStatus> ss = new ArrayList<SoftwareStatus>();
                Room room = new RoomService(session,em).getById(roomId);
                SoftwareService sc = new SoftwareService(session,em);
                for( Device device : room.getDevices() ) {
                        ss.addAll(sc.getAllSoftwareStatusOnDevice(device));
                }
                em.close();
                return ss;
        }

	@GET
	@Path("hwconfs/{hwconfId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "the state of the installation status of a hwconf.",
			notes = "The delivered list has following format:<br>"
			+ "[ {<br>"
			+ "&nbsp;&nbsp;&nbsp;softwareName : Name of the software<br>"
			+ "&nbsp;&nbsp;&nbsp;deviceName   : Name of the device<br>"
			+ "&nbsp;&nbsp;&nbsp;softwareversionId : Id of the SoftwareVersion<br>"
			+ "&nbsp;&nbsp;&nbsp;version	: Version of the software<br>"
			+ "&nbsp;&nbsp;&nbsp;status	 : Installation status of this version<br>"
			+ "&nbsp;&nbsp;&nbsp;manually   : Was the softwar installed manually<br>"
			+ "} ]<br>"
			+ "There are following installation states:<br>"
			+ "I  -> installed<br>"
			+ "IS -> installation scheduled<br>"
			+ "MD -> manuell deinstalled<br>"
			+ "DS -> deinstallation scheduled<br>"
			+ "DF -> deinstallation failed<br>"
			+ "IF -> installation failed"
	)
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "No category was found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("software.install")
	public List<SoftwareStatus> getHWConsStatus(
		@ApiParam(hidden = true) @Auth Session session,
		@ApiParam(value = "ID of the software", required = true) @PathParam("hwconfId") Long hwconfId
	) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                List<SoftwareStatus> ss = new ArrayList<SoftwareStatus>();
                SoftwareService sc = new SoftwareService(session,em);
                HWConf hwconf = new CloneToolService(session,em).getById(hwconfId);
                for( Device device : hwconf.getDevices() ) {
                        ss.addAll(sc.getAllSoftwareStatusOnDevice(device));
                }
                em.close();
                return ss;
        }
}
