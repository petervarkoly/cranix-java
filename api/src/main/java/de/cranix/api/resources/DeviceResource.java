/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.api.resources;


import io.dropwizard.auth.Auth;




import io.swagger.annotations.*;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import de.cranix.dao.Device;
import de.cranix.dao.CrxMConfig;
import de.cranix.dao.CrxActionMap;
import de.cranix.dao.Session;
import de.cranix.dao.CrxResponse;
import de.cranix.dao.Printer;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static de.cranix.api.resources.Resource.*;

@Path("devices")
@Api(value = "devices")
public interface DeviceResource {

	/*
	 * GET devices/<deviceId>
	 */
	@GET
	@Path("{deviceId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get device by id")
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Device not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("device.manage")
	Device getById(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("deviceId") Long deviceId
	);

	/*
	* GET devices/byHWConf/{hwconfId}
	*/
	@GET
	@Path("byHWConf/{hwconfId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get device by hwconfId.")
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Device not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("device.manage")
	List<Device> getByHWConf(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("hwconfId") Long id
	);

	/*
	 * GET devices/getAll
	 */
	@GET
	@Path("all")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get all devices")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one device was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.manage")
	List<Device> getAll(
	        @ApiParam(hidden = true) @Auth Session session
	);

	@GET
	@Path("allNames")
	@Produces(TEXT)
	@ApiOperation(value = "Get the names of all devices")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one device was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.manage")
	String getAllNames(
	        @ApiParam(hidden = true) @Auth Session session
	);


	/*
	 * GET devices/getAll
	 */
	@GET
	@Path("allUsedDevices/{saltClientOnly}")
	@Produces(TEXT)
	@ApiOperation(value = "Get the FQHNs of all devices on which a user is logged in. If saltClientOnly set 1 only salt clients will be listed.")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one device was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.manage")
	String getAllUsedDevices(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("saltClientOnly") Long saltClientOnly
	);

	/*
	 * GET search/{search}
	 */
	@GET
	@Path("search/{search}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Search for device by name or IP or MAC address by substring.")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one user was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.search")
	List<Device> search(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("search") String search
	);

	@POST
	@Path("getDevices")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Gets a list of device objects to the list of deviceIds.")
	@ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@PermitAll
	List<Device> getDevices(
	        @ApiParam(hidden = true) @Auth Session session,
	        List<Long> deviceIds
	);

	/*
	 * GET devices/byIP/<IPAddress>
	 */
	@GET
	@Path("byIP/{IP}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get device by IP address")
	    @ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.search")
	Device getByIP(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("IP") String IP
	);

	/*
	 * GET devices/hostnameByIP/<IPAddress>
	 */
	@GET
	@Path("hostnameByIP/{IP}")
	@Produces(TEXT)
	@ApiOperation(value = "Get device name by ip address")
	    @ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	String getHostnameByIP(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("IP") String IP
	);

	/*
	 * GET devices/hostnameByIP/<IPAddress>
	 */
	@GET
	@Path("owner/{IP}")
	@Produces(TEXT)
	@ApiOperation(value = "Get the uid of the device owners by the ip address of the device.")
	    @ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	String getOwnerByIP(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("IP") String IP
	);

	/*
	 * GET devices/byMAC/<MACAddress>
	 */
	@GET
	@Path("byMAC/{MAC}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get device by MAC address")
	    @ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.search")
	Device getByMAC(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("MAC") String MAC
	);

	/*
	 * GET devices/hostnameByIP/<IPAddress>
	 */
	@GET
	@Path("hostnameByMAC/{MAC}")
	@Produces(TEXT)
	@ApiOperation(value = "Get device by MAC address")
	    @ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	String getHostnameByMAC(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("MAC") String MAC
	);

	/*
	 * GET devices/byName/<Name>
	 */
	@GET
	@Path("byName/{Name}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get device by Name")
	    @ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.search")
	Device getByName(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("Name") String Name
	);

	/*
	 * GET devices/{deviceId}/defaultPrinter
	 */
	@GET
	@Path("{deviceId}/defaultPrinter")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get default printer Name")
	    @ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	Printer getDefaultPrinter(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("deviceId") Long deviceId
	);

	/*
	 * GET devices/{deviceId}/defaultPrinter
	 */
	@GET
	@Path("byIP/{IP}/defaultPrinter")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get default printer Name")
	    @ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	String getDefaultPrinter(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("IP") String IP
	);

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
	CrxResponse setPrinters(
	@ApiParam(hidden = true) @Auth Session session,
			@PathParam("deviceId") Long deviceId,
			Map<String, List<Long>> printers
	);

	/*
	 * PUT devices/{deviceId}/defaultPrinter/{printerId}
	 */
	@PUT
	@Path("{deviceId}/defaultPrinter/{printerId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Set default printer for the device.")
	    @ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.manage")
	CrxResponse setDefaultPrinter(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("deviceId") Long deviceId,
	        @PathParam("printerId") Long printerId
	);

	/*
	 * DELETE devices/{deviceId}/defaultPrinter
	 */
	@DELETE
	@Path("{deviceId}/defaultPrinter")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "REmove the default printer from the device.")
	    @ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.manage")
	CrxResponse deleteDefaultPrinter(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("deviceId") Long deviceId
	);

	/*
	 * GET devices/{deviceId}/availablePrinters
	 */
	@GET
	@Path("{deviceId}/availablePrinters")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get the list of name of the available printers")
	    @ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	List<Printer> getAvailablePrinters(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("deviceId") Long deviceId
	);

	/*
	 * GET devices/{deviceId}/availablePrinters
	 */
	@GET
	@Path("byIP/{IP}/availablePrinters")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get the list of name of the available printers")
	    @ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	String getAvailablePrinters(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("IP") String IP
	);

	/*
	 * PUT devices/{deviceId}/availablePrinters
	 */
	@PUT
	@Path("{deviceId}/availablePrinters/{printerId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Add an available printer to the device.")
	    @ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.manage")
	CrxResponse addAvailablePrinters(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("deviceId") Long deviceId,
	        @PathParam("printerId") Long printerId
	);

	/*
	 * DELETE devices/{deviceId}/availablePrinters
	 */
	@DELETE
	@Path("{deviceId}/availablePrinters/{printerId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Remove an available printer from the device.")
	    @ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.manage")
	CrxResponse deleteAvailablePrinters(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("deviceId") Long deviceId,
	        @PathParam("printerId") Long printerId
	);

	/*
	 * GET devices/loggedInUsers/{IP-Address}
	 */
	@GET
	@Path("loggedInUsers/{IP}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get the logged on users on a device defined by IP.")
	    @ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.manage")
	List<String> getLoggedInUsers(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("IP") String IP
	);

	/*
	 * GET devices/loggedIn/{IP-Address}
	 */
	@GET
	@Path("loggedIn/{IP}")
	@Produces(TEXT)
	@ApiOperation(value = "Get the first logged on user on a device defined by IP.")
	    @ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	String getFirstLoggedInUser(
	        @PathParam("IP") String IP
	);
	/*
	 * PUT devices/loggedInUsers/{IP-Address}/{userName}
	 */
	@PUT
	@Path("loggedInUsers/{IP}/{userName}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Set the logged on user on a device defined by IP. All other users logged on users will be removed.")
	    @ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.manage")
	CrxResponse setLoggedInUsers(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("IP") String IP,
	        @PathParam("userName") String userName
	);

	@PUT
	@Path("loggedInUserByMac/{MAC}/{userName}")
	@Produces(TEXT)
	@ApiOperation(value = "Set the logged on user on a device defined by MAC. All other users logged on users will be removed." )
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Device not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	String setLoggedInUserByMac(
			@Context UriInfo ui,
	        @Context HttpServletRequest req,
	        @PathParam("MAC") String partitionName,
	        @PathParam("userName") String key
	);

	/*
	 * DELETE devices/loggedInUsers/{IP-Address}/{userName}
	 */
	@DELETE
	@Path("loggedInUsers/{IP}/{userName}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get the logged on users on a device defined by IP.")
	    @ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.manage")
	CrxResponse deleteLoggedInUser(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("IP") String IP,
	        @PathParam("userName") String userName
	);

	@DELETE
	@Path("loggedInUserByMac/{MAC}/{userName}")
	@Produces(TEXT)
	@ApiOperation(value = "Set the logged on user on a device defined by MAC. All other users logged on users will be removed." )
	@ApiResponses(value = {
	        @ApiResponse(code = 404, message = "Device not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	String deleteLoggedInUserByMac(
			@Context UriInfo ui,
	        @Context HttpServletRequest req,
	        @PathParam("MAC") String partitionName,
	        @PathParam("userName") String key
	);

	/*
	 * GET devices/{deviceId}/loggedInUsers
	 */
	@GET
	@Path("{deviceId}/loggedInUsers")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Get the logged on users on a device defined by the deviceId.")
	    @ApiResponses(value = {
	    @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.manage")
	List<String> getLoggedInUsers(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("deviceId") Long deviceId
	);

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
	void refreshConfig(
	        @ApiParam(hidden = true) @Auth Session session
	);

	@POST
	@Path("modify")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Modify the configuration of one device.")
	@ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.modify")
	CrxResponse modify(
	@ApiParam(hidden = true) @Auth Session session,
	        Device device
	);

	@POST
	@Path("forceModify")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Modify the configuration of one device.")
	@ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.modify")
	CrxResponse forceModify(
	@ApiParam(hidden = true) @Auth Session session,
	        Device device
	);

	@POST
	@Path("{deviceId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Modify the configuration of one device.")
	@ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.modify")
	CrxResponse modify(
	@ApiParam(hidden = true) @Auth Session session,
			@PathParam("deviceId") Long deviceId,
	        Device device
	);

	/*
	 * DELETE
	 */
	@DELETE
	@Path("{deviceId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Removes a device.")
	@ApiResponses(value = {
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.delete")
	CrxResponse delete(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("deviceId") Long deviceId
	);

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
	CrxResponse importDevices(
	@ApiParam(hidden = true) @Auth Session session,
	        @FormDataParam("file") final InputStream fileInputStream,
	        @FormDataParam("file") final FormDataContentDisposition contentDispositionHeader
	);

	/*
	* GET devices/{deviceId}/actions
	*/
	@GET
	@Path("{deviceId}/actions")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Delivers a list of available actions for a device.")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.manage")
	List<String> getAvailableDeviceActions(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("deviceId") Long deviceId
	);

	/*
	 * PUT devices/{deviceId}/{action}
	 */
	@PUT
	@Path("{deviceId}/actions/{action}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Manage a device. Valid actions are open, close, reboot, shutdown, wol, logout, unlockInput, lockInput, cleanUpLoggedIn.")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.manage")
	CrxResponse manageDevice(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("deviceId") Long deviceId,
	        @PathParam("action") String action
	);

	/*
	 * PUT devices/byName/{deviceName}/{action}
	 */
	@PUT
	@Path("byName/{deviceName}/actions/{action}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Manage a device. Valid actions are open, close, reboot, shutdown, wol, logout, unlockInput, lockInput, cleanUpLoggedIn.")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.manage")
	CrxResponse manageDevice(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("deviceName") String deviceName,
	        @PathParam("action") String action
	);

	/*
	 * POST devices/{deviceId}/actionWithMap/{action}
	 */
	@POST
	@Path("{deviceId}/actionWithMap/{action}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Manage a device. Valid actions are open, close, reboot, shutdown, wol, logout."
			+ "This version of call allows to send a map with some parametrs:"
			+ "graceTime : seconds to wait befor execute action."
			+ "message : the message to shown befor/during execute the action.")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.manage")
	CrxResponse manageDevice(
	        @ApiParam(hidden = true) @Auth Session session,
	        @PathParam("deviceId") Long deviceId,
	        @PathParam("action") String action,
	        Map<String, String> actionContent
	);

	@DELETE
	@Path("cleanUpLoggedIn")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Cleans up all logged in users on all devices")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one room was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("device.manage")
	CrxResponse cleanUpLoggedIn(
	        @ApiParam(hidden = true) @Auth Session session
	);

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
	List<CrxMConfig> getDHCP(
			@ApiParam(hidden = true) @Auth Session session,
	        @PathParam("deviceId") Long deviceId
	        );

	@POST
	@Path("{deviceId}/dhcp")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Adds a new dhcp parameter to a device",
			notes = "How to setup the CrxMConfig object:<br>"
					+ "keyword: this can be dhcpOptions or dhcpStatements<br>"
					+ "value: the value of the dhcpOption or dhcpStatement.<br>"
					+ "Other parameter can be ignored.")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one device was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.dhcp")
	CrxResponse addDHCP(
			@ApiParam(hidden = true) @Auth Session session,
	        @PathParam("deviceId") Long deviceId,
	        CrxMConfig dhcpParameter
	        );

	@DELETE
	@Path("{deviceId}/dhcp/{parameterId}")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Deletes dhcp parameter to a device")
	@ApiResponses(value = {
	        // TODO so oder anders? @ApiResponse(code = 404, message = "At least one device was not found"),
	        @ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("room.dhcp")
	CrxResponse deleteDHCP(
			@ApiParam(hidden = true) @Auth Session session,
	        @PathParam("deviceId") Long deviceId,
	        @PathParam("parameterId") Long parameterId
	        );

	/**
	 * Apply actions on a list of devices.
	 * @param session
	 * @return The result in an CrxResponse object
	 * @see CrxResponse
	 */
	@POST
	@Path("applyAction")
	@Produces(JSON_UTF8)
	@ApiOperation(value = "Apply actions on selected institutes.")
	@ApiResponses(value = {
			@ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
	@RolesAllowed("device.manage")
	CrxResponse applyAction(
			@ApiParam(hidden = true) @Auth Session session,
			CrxActionMap actionMap
			);
}
