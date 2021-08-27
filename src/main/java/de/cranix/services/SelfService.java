/* (c) 2021 Péter Varkoly <pvarkoly@cephalix.eu> - all rights reserved */
package de.cranix.services;

import de.cranix.dao.*;
import de.cranix.helper.CrxSystemCmd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.util.List;

import static de.cranix.helper.CranixConstants.cranixBaseDir;
import static de.cranix.helper.StaticHelpers.startPlugin;

public class SelfService extends Service {

    Logger logger = LoggerFactory.getLogger(SelfService.class);

    public SelfService(Session session, EntityManager em) {
        super(session, em);
    }

    public CrxResponse modifyMySelf(User user) {
        UserService userService = new UserService(session, em);
        User oldUser = session.getUser();
        CrxResponse crxResponse = null;
        logger.debug("modifyMySelf" + user);
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            crxResponse = userService.checkPassword(user.getPassword());
            logger.debug("Check-Password:" + crxResponse);
            if (crxResponse != null && crxResponse.getCode().equals("ERROR")) {
                return crxResponse;
            }
            oldUser.setPassword(user.getPassword());
        }
        if (userService.isAllowed("myself.manage")) {
            oldUser.setGivenName(user.getGivenName());
            oldUser.setSurName(user.getSurName());
            oldUser.setBirthDay(user.getBirthDay());
            oldUser.setFsQuota(user.getFsQuota());
            oldUser.setMsQuota(user.getMsQuota());
        }
        try {
            em.getTransaction().begin();
            em.merge(oldUser);
            em.getTransaction().commit();
            startPlugin("modify_user", oldUser);
        } catch (Exception e) {
            return new CrxResponse(session, "ERROR", "Could not modify user parameter.");
        }
        return new CrxResponse(session, "OK", "User parameters were modified successfully.");
    }

    public Boolean haveVpn() {
        File vpn = new File(cranixBaseDir + "tools/vpn");
        if (vpn == null || !vpn.exists()) {
            return false;
        }
        for (Group g : session.getUser().getGroups()) {
            if (g.getName().equals("VPNUSERS")) {
                return true;
            }
        }
        return false;
    }

    public Response getConfig(String OS) {
        if (!this.haveVpn()) {
            throw new WebApplicationException(401);
        }
        Config config = new Config("/etc/sysconfig/cranix-vpn", "");
        String vpnId = config.getConfigValue("VPN_ID");
        File configFile = null;
        String uid = session.getUser().getUid();
        switch (OS) {
            case "Win7":
            case "Win10":
                configFile = new File("/var/adm/cranix/vpn/crx-vpn-installer-" + vpnId + "-" + uid + ".exe");
                break;
            case "Mac":
                configFile = new File("/var/adm/cranix/vpn/" + vpnId + "-" + uid + ".tar.bz2");
                break;
            case "Linux":
                configFile = new File("/var/adm/cranix/vpn/" + vpnId + "-" + uid + ".tgz");
                break;
        }
        if (!configFile.exists()) {
            StringBuffer reply = new StringBuffer();
            StringBuffer error = new StringBuffer();
            String[] program = new String[2];
            program[0] = cranixBaseDir + "tools/vpn/create-config.sh";
            program[1] = uid;
            CrxSystemCmd.exec(program, reply, error, null);
        }
        ResponseBuilder response = Response.ok(configFile);
        response = response.header("Content-Disposition", "attachment; filename=" + configFile.getName());
        return response.build();
    }

    public Response getInstaller(String OS) {
        if (!this.haveVpn()) {
            throw new WebApplicationException(401);
        }
        File configFile = null;
        String contentType = "application/x-dosexec";
        switch (OS) {
            case "Win7":
                configFile = new File("/srv/www/admin/vpn-clients/openvpn-install-Win7.exe");
                break;
            case "Win10":
                configFile = new File("/srv/www/admin/vpn-clients/openvpn-install-Win10.exe");
                break;
            case "Mac":
                configFile = new File("/srv/www/admin/vpn-clients/Tunnelblick.dmg");
                contentType = "application/zlib";
                break;
        }
        ResponseBuilder response = Response.ok(configFile);
        response = response.header("Content-Disposition", "attachment; filename=" + configFile.getName());
        return response.build();
    }

    public String addDeviceToUser(
            UriInfo ui,
            HttpServletRequest req,
            String MAC,
            String userName) {
        if (!req.getRemoteAddr().equals("127.0.0.1")) {
            return "ERROR Connection is allowed only from local host.";
        }
        SessionService sc = new SessionService(session, em);
        String resp = "";
        CrxResponse crxResponse;
        try {
            session.setIp(req.getRemoteAddr());
            session = sc.createInternalUserSession(userName);
            if(session == null ) {
                logger.error("addDeviceToUser CAN-NOT-FIND-USER:" + userName + " MAC:" + MAC);
                return "CAN-NOT-FIND-USER: "+ userName;
            }
            DeviceService deviceService = new DeviceService(session, em);
            if (deviceService.getByMAC(MAC) != null) {
                resp = "ALREADY-REGISTERED";
            } else {
                RoomService roomService = new RoomService(session, em);
                List<Room> rooms = roomService.getRoomToRegisterForUser(session.getUser());
                if (rooms != null && rooms.size() > 0) {
                    String devName = MAC.substring(8).replaceAll(":", "");
                    for (Room room : rooms) {
                        crxResponse = roomService.addDevice(room.getId(), MAC, devName);
                        if (crxResponse.getCode().equals("OK")) {
                            return crxResponse.getCode() + " " + crxResponse.getValue() + " " + crxResponse.getParameters();
                        }
                    }
                    /* Now we have a problem we could not register the device in none of the rooms */
					List<Device> ownedDevices = this.session.getUser().getOwnedDevices();
                    if (ownedDevices.size() > 0 && this.getConfigValue("AUTO_UPDATE_MAC_ADDRESS").equals("yes")) {
                        Device device = ownedDevices.get( ownedDevices.size() -1 );
                        device.setMac(MAC);
                        crxResponse = deviceService.modify(device);
                        return crxResponse.getCode() + " " + crxResponse.getValue() + " " + crxResponse.getParameters();
                    }
                } else {
                    resp = "You can not register devices.";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) {
                sc.deleteSession(session);
            }
        }
        return resp;
    }

    public CrxResponse deleteDevice(Long deviceId) {
        DeviceService deviceService = new DeviceService(session, em);
        CrxResponse resp;
        if (deviceService.isSuperuser()) {
            resp = deviceService.delete(deviceId, true);
        } else {
            Device device = deviceService.getById(deviceId);
            if (deviceService.mayModify(device)) {
                resp = deviceService.delete(deviceId, true);
            } else {
                resp = new CrxResponse(session, "ERROR", "This is not your device.");
            }
        }
        return resp;
    }

    public CrxResponse modifyDevice(Long deviceId, Device device) {
        try {
            Device oldDevice = em.find(Device.class, deviceId);
            if (oldDevice == null) {
                return new CrxResponse(session, "ERROR", "Can not find the device.");
            }
            if (deviceId != device.getId()) {
                return new CrxResponse(session, "ERROR", "Device ID mismatch.");
            }
            if (!this.mayModify(device)) {
                return new CrxResponse(session, "ERROR", "This is not your device.");
            }
            em.getTransaction().begin();
            oldDevice.setMac(device.getMac());
            em.merge(oldDevice);
            em.getTransaction().commit();
            new DHCPConfig(session, em).Create();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new CrxResponse(session, "ERROR", e.getMessage());
        }
        return new CrxResponse(session, "OK", "Device was modified successfully");
    }

}
