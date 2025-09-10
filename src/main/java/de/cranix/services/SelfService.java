/* (c) 2021 Péter Varkoly <pvarkoly@cephalix.eu> - all rights reserved */
package de.cranix.services;

import de.cranix.dao.*;
import de.cranix.helper.CrxSystemCmd;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static de.cranix.helper.CranixConstants.cranixBaseDir;
import static de.cranix.helper.StaticHelpers.canUserWriteToDirectory;
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
            return new CrxResponse("ERROR", "Could not modify user parameter.");
        }
        return new CrxResponse("OK", "User parameters were modified successfully.");
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
            case "Win10":
            case "Win11":
                configFile = new File("/var/adm/cranix/vpn/" + vpnId + "-" + uid + ".ovpn");
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
            case "Win10":
            case "Win11":
                configFile = new File("/srv/www/admin/vpn-clients/openvpn-install-Win.msi");
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
            if (session == null) {
                logger.error("addDeviceToUser CAN-NOT-FIND-USER:" + userName + " MAC:" + MAC);
                return "CAN-NOT-FIND-USER: " + userName;
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
                        Device device = ownedDevices.get(ownedDevices.size() - 1);
                        device.setMac(MAC);
                        crxResponse = deviceService.modify(device);
                        return crxResponse.getCode() + " " + crxResponse.getValue() + " " + crxResponse.getParameters();
                    }
                } else {
                    resp = "You are not allowed to register devices.";
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
                resp = new CrxResponse("ERROR", "This is not your device.");
            }
        }
        return resp;
    }

    public CrxResponse modifyDevice(Long deviceId, Device device) {
        try {
            Device oldDevice = em.find(Device.class, deviceId);
            if (oldDevice == null) {
                return new CrxResponse("ERROR", "Can not find the device.");
            }
            if (deviceId != device.getId()) {
                return new CrxResponse("ERROR", "Device ID mismatch.");
            }
            if (!this.mayModify(device)) {
                return new CrxResponse("ERROR", "This is not your device.");
            }
            em.getTransaction().begin();
            oldDevice.setMac(device.getMac());
            em.merge(oldDevice);
            em.getTransaction().commit();
            new DHCPConfig(session, em).Create();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new CrxResponse("ERROR", e.getMessage());
        }
        return new CrxResponse("OK", "Device was modified successfully");
    }

    public Object myFiles(Map<String, String> actionsMap) {
        String path = "";
        String action = "list";
        String user = session.getUser().getUid();

        if (actionsMap.containsKey("path")) {
            path = actionsMap.get("path");
        }
        if (actionsMap.containsKey("action")) {
            action = actionsMap.get("action");
        }
        if (user.equals("Administrator")) {
            user = "root";
            if (path.isEmpty()) {
                path = this.getConfigValue("HOME_BASE");
            }
        }
        if (!path.isEmpty() && !path.startsWith(this.getConfigValue("HOME_BASE"))) {
            return "You must not operate in this area";
        }
        switch (action) {
            case "list": {
                String[] program = new String[5];
                program[0] = "/usr/bin/sudo";
                program[1] = "-u";
                program[2] = user;
                program[3] = "/usr/share/cranix/tools/getdir.py";
                program[4] = path;
                StringBuffer reply = new StringBuffer();
                StringBuffer stderr = new StringBuffer();
                CrxSystemCmd.exec(program, reply, stderr, null);
                return reply.toString();
            }
            case "createDir": {
                String dirName = actionsMap.get("newDirName");
                String[] program = new String[6];
                program[0] = "/usr/bin/sudo";
                program[1] = "-u";
                program[2] = user;
                program[3] = "/usr/bin/mkdir";
                program[4] = "-p";
                program[5] = Paths.get(path, dirName).toString();;
                StringBuffer reply = new StringBuffer();
                StringBuffer stderr = new StringBuffer();
                CrxSystemCmd.exec(program, reply, stderr, null);
                if (stderr.toString().isEmpty()) {
                    List<String> tmp = new ArrayList<>();
                    tmp.add(dirName); tmp.add(path);
                    return new CrxResponse("OK", "Directory %s was created successfully in %s", tmp);
                } else {
                    return new CrxResponse("ERROR", stderr.toString());
                }
            }
            case "delete": {
                String[] program = new String[6];
                program[0] = "/usr/bin/sudo";
                program[1] = "-u";
                program[2] = user;
                program[3] = "/usr/bin/rm";
                program[4] = "-rf";
                program[5] = path;
                StringBuffer reply = new StringBuffer();
                StringBuffer stderr = new StringBuffer();
                CrxSystemCmd.exec(program, reply, stderr, null);
                if (stderr.toString().isEmpty()) {
                    List<String> tmp = new ArrayList<>();
                    tmp.add(path);
                    return new CrxResponse("OK", "%s was removed successfully", tmp);
                } else {
                    return new CrxResponse("ERROR", stderr.toString());
                }
            }
            case "get": {
                File file = new File(path);
                String mimeType = null;
                try {
                    mimeType = Files.probeContentType(Path.of(path));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (mimeType == null) {
                    mimeType = "application/octet-stream";
                }
                ResponseBuilder response = Response.ok((Object) file)
                        .header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"")
                        .type(mimeType);
                return response.build();
            }
        }
        return "Unknown action";
    }

    public CrxResponse uploadFile(String dirPath, InputStream fileInputStream, FormDataContentDisposition contentDispositionHeader) {
        List<String> params = new ArrayList<>();
        String fileName =  new String(contentDispositionHeader.getFileName().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        logger.debug("DirPath:" + dirPath + " File Name:" + fileName);
        try {
            Path path = Paths.get(dirPath, fileName);
            if(!this.isSuperuser() && !canUserWriteToDirectory(session.getUser(),Paths.get(dirPath))){
                params.add(dirPath);
                return new CrxResponse("ERROR", "You may not write in %s",params);
            }
            Files.copy(fileInputStream, path, StandardCopyOption.REPLACE_EXISTING);
            String[] program = new String[3];
            program[0] = "/usr/bin/chown";
            program[1] = session.getUser().getUid();
            program[2] = path.toString();
            StringBuffer reply = new StringBuffer();
            StringBuffer stderr = new StringBuffer();
            CrxSystemCmd.exec(program, reply, stderr, null);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return new CrxResponse("ERROR", e.getMessage());
        }
        params.add(fileName); params.add(dirPath);
        return new CrxResponse("OK", "File %s was saved in %s",params);
    }
}
