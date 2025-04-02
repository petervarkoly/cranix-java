/* (c) 2021 Peter Varkoly <pvarkoly@cephalix.eu> - all rights reserved */
package de.cranix.api.resources;

import de.cranix.dao.*;
import de.cranix.helper.CrxEntityManagerFactory;
import de.cranix.services.IdRequestService;
import de.cranix.services.InformationService;
import de.cranix.services.RoomService;
import de.cranix.services.SelfService;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.*;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;

import static de.cranix.api.resources.Resource.JSON_UTF8;
import static de.cranix.api.resources.Resource.TEXT;
import static de.cranix.helper.CranixConstants.vpnOsList;

@Path("selfmanagement")
@Api(value = "selfmanagement")
@Produces(JSON_UTF8)
public class SelfManagementResource {

    public SelfManagementResource() {
    }

    @GET
    @Path("me")
    @ApiOperation(value = "Get my own datas")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "User not found"),
            @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
    @RolesAllowed("myself.search")
    public User getBySession(@ApiParam(hidden = true) @Auth Session session) {
        return session.getUser();
    }

    @POST
    @Path("modify")
    @ApiOperation(value = "Modify my own datas")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @PermitAll
    public CrxResponse modifyMySelf(
            @ApiParam(hidden = true) @Auth Session session,
            User user
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new SelfService(session, em).modifyMySelf(user);
        em.close();
        return resp;
    }

    /*
     * VPN Management
     */

    /**
     * Checks if a user is allowed to use vpn connection to the school
     *
     * @param session
     * @return true/false
     */
    @GET
    @Path("vpn/have")
    @ApiOperation(value = "Checks if a user is allowed to use vpn connection to the school")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "User not found"),
            @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
    @RolesAllowed("myself.search")
    public Boolean haveVpn(@ApiParam(hidden = true) @Auth Session session) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        Boolean resp = new SelfService(session, em).haveVpn();
        em.close();
        return resp;
    }


    /**
     * Delivers the list of supported clients OS for the VPN.
     *
     * @param session
     * @return List of the supported OS
     */
    @GET
    @Path("vpn/OS")
    @ApiOperation(value = "Delivers the list of supported clients OS for the VPN.")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "User not found"),
            @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
    @RolesAllowed("myself.search")
    public String[] vpnOS(@ApiParam(hidden = true) @Auth Session session) {
        return vpnOsList;
    }

    /**
     * Delivers the configuration for a given operating system.
     *
     * @param OS The operating system: Win, Mac or Linux
     * @return The configuration as an installer or tar archive.
     */
    @GET
    @Path("vpn/config/{OS}")
    @Produces("*/*")
    @ApiOperation(value = "Delivers the configuration for a given operating system.",
            notes = "OS The operating system: the list of the supported os will be delivered by GET selfmanagement/vpn/OS")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "You are not allowed to use VPN."),
            @ApiResponse(code = 404, message = "User not found"),
            @ApiResponse(code = 500, message = "Server broken, please contact adminstrator."),
            @ApiResponse(code = 501, message = "Can not create your configuration. Please contact adminstrator.")})
    @RolesAllowed("myself.search")
    public Response getConfig(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("OS") String OS
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        Response resp = new SelfService(session, em).getConfig(OS);
        em.close();
        return resp;
    }

    /**
     * Delivers the configuration for a given operating system.
     *
     * @param OS The operating system: Win, Mac or Linux
     * @return The configuration as an installer or tar archive.
     */
    @GET
    @Path("vpn/installer/{OS}")
    @Produces("application/x-dosexec")
    @ApiOperation(value = "Delivers the installer for a given operating system.",
            notes = "OS The operating system: the list of the supported os will be delivered by GET selfmanagement/vpn/OS")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "You are not allowed to use VPN."),
            @ApiResponse(code = 404, message = "User not found"),
            @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
    @RolesAllowed("myself.search")
    public Response getInstaller(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("OS") String OS
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        Response resp = new SelfService(session, em).getInstaller(OS);
        em.close();
        return resp;
    }

    /**
     * This function is for the radius plugin to automatic registration of BYOD devices.
     * This call is allowed only from localhost.
     *
     * @param ui  UriInfo
     * @param req HttpServletRequest
     * @param mac The mac address of the device to register
     * @param uid The uid of the owner of the device
     * @return The result of the registration
     **/
    @PUT
    @Path("addDeviceToUser/{MAC}/{userName}")
    @Produces(TEXT)
    @ApiOperation(value = "Set the logged on user on a device defined by MAC. All other users logged on users will be removed.")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Cant be called only from localhost."),
            @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
    public String addDeviceToUser(
            @Context UriInfo ui,
            @Context HttpServletRequest req,
            @PathParam("MAC") String mac,
            @PathParam("userName") String uid
    ) {
        Session session = new Session();
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        String resp = new SelfService(session, em).addDeviceToUser(ui, req, mac, uid);
        em.close();
        return resp;
    }

    /**
     * getRooms Delivers a list of all AdHocRooms
     *
     * @param session
     * @return
     */
    @GET
    @Path("rooms")
    @ApiOperation(value = "Gets all defined AdHocLan Rooms which a user may use. Superuser get the list of all AdHocLan rooms.")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "No room was found"),
            @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
    @PermitAll
    public List<Room> getMyRooms(@ApiParam(hidden = true) @Auth Session session) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        List<Room> resp = new RoomService(session, em).getAllToRegister();
        em.close();
        return resp;
    }

    /**
     * getDevices Delivers a list of the owned devices
     *
     * @param session
     * @return
     */
    @GET
    @Path("devices")
    @ApiOperation(value = "Gets all owned AdHocLan Devices of a user.")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "No category was found"),
            @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
    @PermitAll
    public List<Device> getDevices(@ApiParam(hidden = true) @Auth Session session) {
        return session.getUser().getOwnedDevices();
    }

    @DELETE
    @Path("devices/{deviceId}")
    @ApiOperation(value = "Delets an owned AdHocLan Devices of a user.")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "No category was found"),
            @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
    @PermitAll
    public CrxResponse deleteDevice(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("deviceId") Long deviceId
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new SelfService(session, em).deleteDevice(deviceId);
        em.close();
        return resp;
    }

    @POST
    @Path("devices/{deviceId}")
    @ApiOperation(value = "Modify an owned AdHocLan Devices of a user.")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "No category was found"),
            @ApiResponse(code = 500, message = "Server broken, please contact adminstrator")})
    @PermitAll
    public CrxResponse modifyDevice(
            @ApiParam(hidden = true) @Auth Session session,
            @PathParam("deviceId") Long deviceId,
            Device device
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new SelfService(session, em).modifyDevice(deviceId, device);
        em.close();
        return resp;
    }

    @POST
    @Path("devices/add")
    @ApiOperation(value = "Create a new device. This api call can be used only for registering own devices.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @PermitAll
    public CrxResponse addDevice(
            @ApiParam(hidden = true) @Auth Session session,
            Device device
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new RoomService(session, em).addDevice(device.getRoomId(), device.getMac(), device.getName());
        em.close();
        return resp;
    }

    @POST
    @Path("taskResponses")
    @ApiOperation(value = "Create a new device. This api call can be used only for registering own devices.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @PermitAll
    public CrxResponse addTaskResponse(
            @ApiParam(hidden = true) @Auth Session session,
            TaskResponse taskResponse
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse resp = new InformationService(session, em).addTaskResponse(taskResponse);
        em.close();
        return resp;
    }


    @PATCH
    @Path("taskResponses")
    @ApiOperation(value = "Modify task response. This call only modifies the text of the task response.<br>" +
            " Only the owner of the task response is allowed to do it")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @PermitAll
    public CrxResponse modifyTaskResponse(
            @ApiParam(hidden = true) @Auth Session session,
            TaskResponse taskResponse
    ) {
        EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
        CrxResponse response = new InformationService(session,em).modifyTaskResponse(taskResponse);
        em.close();
        return response;
    }

    @GET
    @Path("taskResponses")
    @ApiOperation(value = "Delivers all owned task responses.")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Server broken, please contact administrator")
    })
    @PermitAll
    public List<TaskResponse> addTaskResponse(
            @ApiParam(hidden = true) @Auth Session session
    ) {
        return session.getUser().getTaskResponses();
    }
}
