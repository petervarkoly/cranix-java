/* (c) 2020 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.api.resources;


import io.dropwizard.auth.Auth;
import io.swagger.annotations.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.cranix.dao.CrxActionMap;
import de.cranix.dao.CrxMConfig;
import de.cranix.dao.CrxResponse;
import de.cranix.dao.Device;
import de.cranix.dao.Printer;
import de.cranix.dao.Session;
import de.cranix.services.DHCPConfig;
import de.cranix.services.DeviceService;
import de.cranix.services.EducationService;
import de.cranix.services.SessionService;
import de.cranix.services.SoftwareService;
import de.cranix.helper.CommonEntityManagerFactory;

import static de.cranix.api.resources.Resource.*;

@Path("devices")
@Api(value = "devices")
public class DeviceResource {

	Logger logger = LoggerFactory.getLogger(DeviceResource.class);

	public DeviceResource() { }

	@GET
	@Path("{deviceId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get device by id")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "Device not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("device.manage")
	public Device getById(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("deviceId") Long deviceId
	) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final Device device = new DeviceService(session,em).getById(deviceId);
		em.close();
		if (device == null) {
			throw new WebApplicationException(404);
		}
		return device;
	}

	@GET
	@Path("all")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get all devices")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	public List<Device> getAll(
		@ApiParam(hidden = true) @Auth Session session
	) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final List<Device> devices = new DeviceService(session,em).getAll();
		em.close();
		if (devices == null) {
		    throw new WebApplicationException(404);
		}
		return devices;
	}

	@GET
	@Path("byHWConf/{hwconfId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get device by hwconfId.")
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "Device not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("device.manage")
	public List<Device> getByHWConf(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("hwconfId") Long id
	) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<Device> resp = new DeviceService(session,em).getByHWConf(id);
		em.close();
		return resp;
	}

	@GET
	@Path("byIP/{IP}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get device by IP address")
	    @ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.search")
	public Device getByIP(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("IP") String IP
	) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final Device device = new DeviceService(session,em).getByIP(IP);
		em.close();
		if (device == null) {
		    throw new WebApplicationException(404);
		}
		return device;
	}

	@GET
	@Path("byMAC/{MAC}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get device by MAC address")
	    @ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.search")
	public Device getByMAC(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("MAC") String MAC
	) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final Device device = new DeviceService(session,em).getByMAC(MAC);
		em.close();
		if (device == null) {
		    throw new WebApplicationException(404);
		}
		return device;
	}

	@GET
	@Path("byName/{Name}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get device by Name")
	    @ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.search")
	public Device getByName(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("Name") String Name
	) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final Device device = new DeviceService(session,em).getByName(Name);
		em.close();
		if (device == null) {
			throw new WebApplicationException(404);
		}
		return device;
	}

	@GET
	@Path("hostnameByIP/{IP}")
	@Produces(TEXT)
	@ApiOperation(value = "Get device name by ip address")
	    @ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	public String getHostnameByIP(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("IP") String IP
	) {
		Device device = this.getByIP(session, IP);
		if( device == null ) {
			return "";
		}
		return device.getName();
	}

	@GET
	@Path("hostnameByMAC/{MAC}")
	@Produces(TEXT)
	@ApiOperation(value = "Get device by MAC address")
	    @ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	public String getHostnameByMAC(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("MAC") String MAC
	)  {
		Device device = this.getByMAC(session, MAC);
		if( device == null ) {
			return "";
		}
		return device.getName();
	}

	@GET
	@Path("owner/{IP}")
	@Produces(TEXT)
	@ApiOperation(value = "Get the uid of the device owners by the ip address of the device.")
	    @ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	public String getOwnerByIP(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("IP") String IP
	) {
		Device device = this.getByIP(session, IP);
		if( device == null ) {
			return "";
		}
		if( device.getOwner() == null ) {
			return "";
		}
		return device.getOwner().getUid();
	}

	@GET
	@Path("{deviceId}/defaultPrinter")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get default printer Name")
	    @ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	public Printer getDefaultPrinter(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("deviceId") Long deviceId
	) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final Printer resp = new DeviceService(session,em).getDefaultPrinter(deviceId);
		em.close();
		return resp;
	}

	@GET
	@Path("byIP/{IP}/defaultPrinter")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get default printer Name")
	    @ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	public String getDefaultPrinter(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("IP") String IP
	) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final DeviceService deviceService = new DeviceService(session,em);
		final Device device =  deviceService.getByIP(IP);
		if( device == null ) {
			em.close();
			return "";
		}
		Printer printer = deviceService.getDefaultPrinter(device.getId());
		em.close();
		if( printer != null ) {
			return printer.getName();
		}
		return "";
	}

	@POST
	@Path("{deviceId}/printers")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Modify the printers of one device.",
			notes = "The printers object has following format<br>"
					+ "{"
					+ "  defaultPrinter:  [id],"
					+ "  availablePrinters: [ id1, id2 ]"
					+ "}")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.modify")
	public CrxResponse setPrinters(
	@ApiParam(hidden = true) @Auth Session session,
		@PathParam("deviceId") Long deviceId,
		Map<String, List<Long>> printers
	) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new DeviceService(session,em).setPrinters(deviceId,printers);
		em.close();
		return resp;
	}

	@GET
	@Path("{deviceId}/availablePrinters")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get the list of name of the available printers")
	    @ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	public List<Printer> getAvailablePrinters(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("deviceId") Long deviceId
	) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final DeviceService deviceService = new DeviceService(session,em);
		List<Printer> resp = deviceService.getAvailablePrinters(deviceId);
		em.close();
		return resp;
	}

	@GET
	@Path("byIP/{IP}/availablePrinters")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get the list of name of the available printers")
	    @ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	public String getAvailablePrinters(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("IP") String IP
	) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		DeviceService deviceService = new DeviceService(session,em);
		Device device = deviceService.getByIP(IP);
		if( device == null ) {
			em.close();
			return "";
		}
		List<String> printers = new ArrayList<String>();
		for( Printer printer : deviceService.getAvailablePrinters(device.getId()) ) {
			printers.add(printer.getName());
		}
		em.close();
		return String.join(" ", printers);
	}

	/*
	 * Deprecated use setPrinters instead of.
	 */
	@PUT
	@Path("{deviceId}/defaultPrinter/{printerId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Set default printer for the device.")
	    @ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.manage")
	public CrxResponse setDefaultPrinter(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("deviceId") Long deviceId,
		@PathParam("printerId") Long printerId
	) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final DeviceService deviceService = new DeviceService(session,em);
		CrxResponse resp = deviceService.setDefaultPrinter(deviceId,printerId);
		em.close();
		return resp;
	}

	/*
	 * Deprecated use setPrinters instead of.
	 */
	@DELETE
	@Path("{deviceId}/defaultPrinter")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "REmove the default printer from the device.")
	    @ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	public @RolesAllowed("device.manage")
	CrxResponse deleteDefaultPrinter(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("deviceId") Long deviceId
	) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new DeviceService(session,em).deleteDefaultPrinter(deviceId);
		em.close();
		return resp;
	}

	/*
	 * Deprecated use setPrinters instead of.
	 */
	@PUT
	@Path("{deviceId}/availablePrinters/{printerId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Add an available printer to the device.")
	    @ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.manage")
	public CrxResponse addAvailablePrinters(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("deviceId") Long deviceId,
		@PathParam("printerId") Long printerId
	) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new DeviceService(session,em).addAvailablePrinter(deviceId, printerId);
		em.close();
		return resp;
	}

	/*
	 * Deprecated use setPrinters instead of.
	 */
	@DELETE
	@Path("{deviceId}/availablePrinters/{printerId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Remove an available printer from the device.")
	    @ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.manage")
	public CrxResponse deleteAvailablePrinters(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("deviceId") Long deviceId,
		@PathParam("printerId") Long printerId
	) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new DeviceService(session,em).deleteAvailablePrinter(deviceId, printerId);
		em.close();
		return resp;
	}

	@GET
	@Path("loggedInUsers/{IP}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get the logged on users on a device defined by IP.")
	    @ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.manage")
	public List<String> getLoggedInUsers(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("IP") String IP
	) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<String> resp = new DeviceService(session,em).getLoggedInUsers(IP);
		em.close();
		return resp;
	}

	@GET
	@Path("{deviceId}/loggedInUsers")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get the logged on users on a device defined by the deviceId.")
	@ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.manage")
	public List<String> getLoggedInUsers(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("deviceId") Long deviceId
	) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final List<String> resp = new DeviceService(session,em).getLoggedInUsers(deviceId);
		em.close();
		return resp;
	}

	@GET
	@Path("loggedIn/{IP}")
	@Produces(TEXT)
	@ApiOperation(value = "Get the first logged on user on a device defined by IP.")
	    @ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	public String getFirstLoggedInUser( @PathParam("IP") String IP) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final Session session  = new SessionService(em).getLocalhostSession();
		final DeviceService deviceService = new DeviceService(session,em);
		final Device device = deviceService.getByIP(IP);
		if( device != null && !device.getLoggedIn().isEmpty() ) {
			if( ! deviceService.checkConfig(device.getLoggedIn().get(0), "disableInternet")) {
				em.close();
				return device.getLoggedIn().get(0).getUid();
			}
		}
		em.close();
		return "";
	}

	@PUT
	@Path("loggedInUsers/{IP}/{userName}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Set the logged on user on a device defined by IP. All other users logged on users will be removed.")
	    @ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.manage")
	public CrxResponse setLoggedInUsers(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("IP") String IP,
		@PathParam("userName") String userName
	) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final DeviceService deviceService = new DeviceService(session,em);
		CrxResponse resp = deviceService.setLoggedInUsers(IP, userName);
		em.close();
		return resp;
	}

	@PUT
	@Path("loggedInUserByMac/{MAC}/{userName}")
	@Produces(TEXT)
	@ApiOperation(value = "Set the logged on user on a device defined by MAC. All other users logged on users will be removed." )
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "Device not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	public String setLoggedInUserByMac(
		@Context UriInfo ui,
		@Context HttpServletRequest req,
		@PathParam("MAC") String MAC,
		@PathParam("userName") String userName
	) {
		if( !req.getRemoteAddr().equals("127.0.0.1")) {
			return "ERROR Connection is allowed only from local host.";
		}
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		Session session  = new SessionService(em).getLocalhostSession();
		CrxResponse resp = new DeviceService(session,em).setLoggedInUserByMac(MAC, userName);
		em.close();
		return resp.getCode() + " " + resp.getValue();
	}

	@DELETE
	@Path("loggedInUsers/{IP}/{userName}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get the logged on users on a device defined by IP.")
	    @ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.manage")
	public CrxResponse deleteLoggedInUser(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("IP") String IP,
		@PathParam("userName") String userName
	) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final DeviceService deviceService = new DeviceService(session,em);
		CrxResponse resp =  deviceService.removeLoggedInUser(IP, userName);
		em.close();
		return resp;
	}

	@DELETE
	@Path("loggedInUserByMac/{MAC}/{userName}")
	@Produces(TEXT)
	@ApiOperation(value = "Set the logged on user on a device defined by MAC. All other users logged on users will be removed." )
	@ApiResponses(value = {
		@ApiResponse(code = 404, message = "Device not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")
	})
	public String deleteLoggedInUserByMac(
		@Context UriInfo ui,
		@Context HttpServletRequest req,
		@PathParam("MAC") String MAC,
		@PathParam("userName") String userName
	) {
		if( !req.getRemoteAddr().equals("127.0.0.1")) {
			return "ERROR Connection is allowed only from local host.";
		}
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		Session session  = new SessionService(em).getLocalhostSession();
		final DeviceService deviceService = new DeviceService(session,em);
		CrxResponse resp =  deviceService.removeLoggedInUserByMac(MAC, userName);
		em.close();
		return resp.getCode() + " " + resp.getValue();
	}

	/*
	 * GET devices/refreshConfig
	 */
	@PUT
	@Path("refreshConfig")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Refresh the DHCP DNS and SALT Configuration.")
	@ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.add")
	public void refreshConfig(
		@ApiParam(hidden = true) @Auth Session session
	) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		new DHCPConfig(session,em).Create();
		em.close();
	}

	@POST
	@Path("{deviceId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Modify the configuration of one device.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.modify")
	public CrxResponse modify(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("deviceId") Long deviceId,
		Device device
	) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		device.setId(deviceId);
		CrxResponse resp =  new DeviceService(session,em).modify(device);
		em.close();
		return resp;
	}

	@POST
	@Path("forceModify")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Modify the configuration of one device.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.modify")
	public CrxResponse forceModify(
		@ApiParam(hidden = true) @Auth Session session,
		Device device
	) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp =  new DeviceService(session,em).forceModify(device);
		em.close();
		return resp;
	}

	@DELETE
	@Path("{deviceId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Removes a device.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.delete")
	public CrxResponse delete(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("deviceId") Long deviceId
	) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final DeviceService deviceService = new DeviceService(session,em);
		CrxResponse resp = deviceService.delete(deviceId,true);
		em.close();
		return resp;
	}

	@POST
	@Path("import")
	@Produces(JSON_UTF8)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation(value =	"Import devices from a CSV file. This MUST have following format:\\n" ,
	    	notes = "* Separator is the semicolon ';'.<br>" +
	    	"* A header line must be provided.<br>" +
	    	"* The header line is case insensitive.<br>" +
	    	"* The fields Room and MAC are mandatory.<br>" +
	    	"* The import is only allowed in existing rooms.<br>")
	@ApiResponses(value = {
		    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.manage")
	public List<CrxResponse> importDevices(
		@ApiParam(hidden = true) @Auth Session session,
		@FormDataParam("file") final InputStream fileInputStream,
		@FormDataParam("file") final FormDataContentDisposition contentDispositionHeader
	) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<CrxResponse> resp = new DeviceService(session,em).importDevices(fileInputStream, contentDispositionHeader);
		em.close();
		return resp;
	}

	@GET
	@Path("{deviceId}/actions")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delivers a list of available actions for a device.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.manage")
	public List<String> getAvailableDeviceActions(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("deviceId") Long deviceId
	) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<String> resp = new EducationService(session,em).getAvailableDeviceActions(deviceId);
		em.close();
		return resp;
	}

	/**
	 * Apply actions on a list of devices.
	 * @param session
	 * @return The result in an CrxResponse object
	 * @see CrxResponse
	 */
	@POST
	@Path("applyAction")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Apply actions on selected devices.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")
	})
	@RolesAllowed("device.manage")
	public List<CrxResponse> applyAction(
		@ApiParam(hidden = true) @Auth Session session,
		CrxActionMap actionMap
	) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		DeviceService deviceService = new DeviceService(session,em);
		List<CrxResponse> responses = new ArrayList<CrxResponse>();
		logger.debug("actionMap" + actionMap);
		for( Long id: actionMap.getObjectIds() ) {
			responses.add(deviceService.manageDevice(id,actionMap.getName(),null));
		}
		if( actionMap.getName().equals("delete") ) {
			new DHCPConfig(session,em).Create();
			new SoftwareService(session,em).applySoftwareStateToHosts();

		}
		em.close();
		return responses;
	}

	/*
	 * Deprecated use applyAction instead
	 */
	@PUT
	@Path("{deviceId}/actions/{action}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Manage a device. Valid actions are open, close, reboot, shutdown, wol, logout, unlockInput, lockInput, cleanUpLoggedIn.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.manage")
	public CrxResponse manageDevice(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("deviceId") Long deviceId,
		@PathParam("action") String action
	) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new DeviceService(session,em).manageDevice(deviceId,action,null);
		em.close();
		return resp;
	}

	@PUT
	@Path("byName/{deviceName}/actions/{action}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Manage a device. Valid actions are open, close, reboot, shutdown, wol, logout, unlockInput, lockInput, cleanUpLoggedIn.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.manage")
	public CrxResponse manageDevice(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("deviceName") String deviceName,
		@PathParam("action") String action
	)  {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new DeviceService(session,em).manageDevice(deviceName,action,null);
		em.close();
		return resp;
	}

	/*
	 * Deprecated use applyAction instead
	 */
	@POST
	@Path("{deviceId}/actionWithMap/{action}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Manage a device. Valid actions are open, close, reboot, shutdown, wol, logout."
			+ "This version of call allows to send a map with some parametrs:"
			+ "graceTime : seconds to wait befor execute action."
			+ "message : the message to shown befor/during execute the action.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.manage")
	public CrxResponse manageDevice(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("deviceId") Long deviceId,
		@PathParam("action") String action,
		Map<String, String> actionContent
	) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new DeviceService(session,em).manageDevice(deviceId,action,actionContent);
		em.close();
		return resp;
	}

	@DELETE
	@Path("cleanUpLoggedIn")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Cleans up all logged in users on all devices")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.manage")
	public CrxResponse cleanUpLoggedIn(
		@ApiParam(hidden = true) @Auth Session session
	) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new DeviceService(session,em).cleanUpLoggedIn();
		em.close();
		return resp;
	}

	/*
	 * DHCP-Management
	 */
	/**
	 * Gets the active dhcp parameter of a device
	 * @param session
	 * @param deviceId
	 * @return a list of CrxMConfig objects representing the DHCP parameters
	 */
	@GET
	@Path("{deviceId}/dhcp")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets the active dhcp parameter of a device:",
			notes =  "How to evaluate the CrxMConfig object:<br>"
			+ "id: ID of the dhcp parameter object<br>"
			+ "objectType: Device, but in this case it can be ignored.<br>"
			+ "objectId: the device id<br>"
			+ "keyword: this can be only dhcpOption or dhcpStatement<br>"
			+ "value: the value of the dhcpOption or dhcpStatement."
			)
	@ApiResponses(value = {
		// TODO so oder anders? @ApiResponse(code = 404, message = "At least one device was not found"),
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.dhcp")
	public List<CrxMConfig> getDHCP(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("deviceId") Long deviceId
	) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<CrxMConfig> dhcpParameters = new ArrayList<CrxMConfig>();
		final DeviceService deviceService = new DeviceService(session,em);
		final Device device = deviceService.getById(deviceId);
		for(CrxMConfig config : deviceService.getMConfigObjects(device, "dhcpStatements") ) {
			dhcpParameters.add(config);
		}
		for(CrxMConfig config : deviceService.getMConfigObjects(device, "dhcpOptions") ) {
			dhcpParameters.add(config);
		}
		em.close();
		return dhcpParameters;
	}

	@POST
	@Path("{deviceId}/dhcp")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Adds a new dhcp parameter to a device",
			notes = "How to setup the CrxMConfig object:<br>"
					+ "keyword: this can be dhcpOptions or dhcpStatements<br>"
					+ "value: the value of the dhcpOption or dhcpStatement.<br>"
					+ "Other parameter can be ignored.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.dhcp")
	public CrxResponse addDHCP(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("deviceId") Long deviceId,
		CrxMConfig dhcpParameter
	)  {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final CrxResponse crxResponse = new DeviceService(session,em).addDHCP(deviceId,dhcpParameter);
		return new CrxResponse(session,"OK","DHCP Parameter was added succesfully");
	}

	@DELETE
	@Path("{deviceId}/dhcp/{parameterId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Deletes dhcp parameter to a device")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.dhcp")
	public CrxResponse deleteDHCP(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("deviceId") Long deviceId,
		@PathParam("parameterId") Long parameterId
	) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final DeviceService deviceService = new DeviceService(session,em);
		Device    device = deviceService.getById(deviceId);
		CrxResponse resp = deviceService.deleteMConfig(device,parameterId);
		new DHCPConfig(session,em).Create();
		em.close();
		return resp;
	}
}
