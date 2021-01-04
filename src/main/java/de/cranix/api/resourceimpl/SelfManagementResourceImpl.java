/* (c) 2020 Peter Varkoly <peter@varkoly.de> all rights reserved*/
package de.cranix.api.resourceimpl;

import java.io.File;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.List;
import de.cranix.api.resources.SelfManagementResource;
import de.cranix.dao.CrxResponse;
import de.cranix.dao.Device;
import de.cranix.dao.Group;
import de.cranix.dao.Room;
import de.cranix.dao.Session;
import de.cranix.dao.User;
import de.cranix.services.DHCPConfig;
import de.cranix.services.Config;
import de.cranix.services.DeviceService;
import de.cranix.services.RoomService;
import de.cranix.services.SessionService;
import de.cranix.services.UserService;
import de.cranix.helper.CrxEntityManagerFactory;
import de.cranix.helper.OSSShellTools;
import static de.cranix.helper.StaticHelpers.*;
import static de.cranix.helper.CranixConstants.*;

public class SelfManagementResourceImpl implements SelfManagementResource {

	Logger logger = LoggerFactory.getLogger(SelfManagementResource.class);

	public SelfManagementResourceImpl() {
		super();
	}

	@Override
	public User getBySession(Session session) {
		return session.getUser();
	}

	@Override
	public CrxResponse modifyMySelf(Session session, User user) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		UserService userService = new UserService(session,em);
		User oldUser = session.getUser();
		CrxResponse  crxResponse = null;
		logger.debug("modifyMySelf" + user);
		if( user.getPassword() != null && !user.getPassword().isEmpty() ) {
			crxResponse = userService.checkPassword(user.getPassword());
			logger.debug("Check-Password:" + crxResponse );
			if( crxResponse != null  && crxResponse.getCode().equals("ERROR")) {
				return crxResponse;
			}
			oldUser.setPassword(user.getPassword());
		}
		if( userService.isAllowed("myself.manage") ) {
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
			return new CrxResponse(session,"ERROR","Could not modify user parameter.");
		} finally {
			em.close();
		}
		return new CrxResponse(session,"OK","User parameters were modified successfully.");
	}

	@Override
	public Boolean haveVpn(Session session) {
		File vpn = new File(cranixBaseDir + "tools/vpn");
		if( vpn == null || !vpn.exists() ) {
			return false;
		}
		for( Group g : session.getUser().getGroups() ) {
			if( g.getName().equals("VPNUSERS")) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Response getConfig(Session session, String OS) {
		if( ! haveVpn(session)) {
			throw new WebApplicationException(401);
		}
		Config config   = new Config("/etc/sysconfig/cranix-vpn","");
		String vpnId    = config.getConfigValue("VPN_ID");
		File configFile = null;
		String uid  =  session.getUser().getUid();
		switch(OS) {
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
		if( ! configFile.exists() ) {
			StringBuffer reply = new StringBuffer();
			StringBuffer error = new StringBuffer();
			String[]   program = new String[2];
			program[0] = cranixBaseDir + "tools/vpn/create-config.sh";
			program[1] = uid;
			OSSShellTools.exec(program, reply, error, null);
		}
		ResponseBuilder response = Response.ok((Object) configFile);
		response = response.header("Content-Disposition","attachment; filename="+ configFile.getName());
		return response.build();
	}

	@Override
	public Response getInstaller(Session session, String OS) {
		if( ! haveVpn(session)) {
			throw new WebApplicationException(401);
		}
		File configFile = null;
		String contentType = "application/x-dosexec";
		switch(OS) {
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
		ResponseBuilder response = Response.ok((Object) configFile);
		response = response.header("Content-Disposition","attachment; filename="+ configFile.getName());
		return response.build();
	}

	@Override
	public String[] vpnOS(Session session) {
		String[] osList = { "Win7","Win10","Mac","Linux" };
		return osList;
	}

	@Override
	public String addDeviceToUser
	(
			UriInfo ui,
			HttpServletRequest req,
			String MAC,
			String userName) {
		if( !req.getRemoteAddr().equals("127.0.0.1")) {
			return "ERROR Connection is allowed only from local host.";
		}
		EntityManager em     = CrxEntityManagerFactory.instance().createEntityManager();
		Session session      = new Session();
		SessionService sc = new SessionService(session,em);
		String  resp         = "";
		try {
			session.setIp(req.getRemoteAddr());
			session = sc.createInternalUserSession(userName);
			final DeviceService deviceService = new DeviceService(session,em);
			if( deviceService.getByMAC(MAC) != null ) {
				resp = "ALREADY-REGISTERED";
			} else {
				final RoomService roomService = new RoomService(session,em);
				List<Room> rooms = roomService.getRoomToRegisterForUser(session.getUser());
				if( rooms != null && rooms.size() > 0 ) {
					String devName = MAC.substring(8).replaceAll(":", "");
					CrxResponse crxResponse = roomService.addDevice(rooms.get(0).getId(), MAC, devName);
					resp =  crxResponse.getCode() + " " + crxResponse.getValue() + " " + crxResponse.getParameters();
				} else  {
					resp = "You can not register devices.";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if( session != null ) {
				sc.deleteSession(session);
			}
			em.close();
		}
		return resp;
	}

        @Override
        public List<Room> getMyRooms(Session session) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                RoomService roomService = new RoomService(session,em);
                List<Room> resp= roomService.getAllToRegister();
                em.close();
                return resp;
        }

        @Override
        public List<Device> getDevices(Session session) {
                return session.getUser().getOwnedDevices();
        }

        @Override
        public CrxResponse deleteDevice(Session session, Long deviceId) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                DeviceService deviceService = new DeviceService(session,em);
                CrxResponse resp;
                if( deviceService.isSuperuser() ) {
                        resp = deviceService.delete(deviceId, true);
                } else {
                        Device device = deviceService.getById(deviceId);
                        if( deviceService.mayModify(device) ) {
                                resp = deviceService.delete(deviceId, true);
                        } else {
                                resp = new CrxResponse(session,"ERROR", "This is not your device.");
                        }
                }
                em.close();
                return resp;
        }

        @Override
        public CrxResponse modifyDevice(Session session, Long deviceId, Device device) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                DeviceService deviceService = new DeviceService(session,em);
                try {
                        Device oldDevice = em.find(Device.class, deviceId);
                        if( oldDevice == null ) {
                                return new CrxResponse(session,"ERROR","Can not find the device.");
                        }
                        if( deviceId != device.getId() ) {
                                return new CrxResponse(session,"ERROR","Device ID mismatch.");
                        }
                        if( ! deviceService.mayModify(device) ) {
                                return new CrxResponse(session,"ERROR", "This is not your device.");
                        }
                        em.getTransaction().begin();
                        oldDevice.setMac(device.getMac());
                        em.merge(oldDevice);
                        em.getTransaction().commit();
                        new DHCPConfig(session,em).Create();
                }  catch (Exception e) {
                        logger.error(e.getMessage());
                        return new CrxResponse(session,"ERROR", e.getMessage());
                } finally {
                        em.close();
                }
                return new CrxResponse(session,"OK", "Device was modified successfully");
        }

        @Override
        public CrxResponse addDevice(Session session, Device device) {
                EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
                CrxResponse resp = new RoomService(session,em).addDevice(device.getRoomId(), device.getMac(), device.getName());
                em.close();
                return resp;
        }

}
