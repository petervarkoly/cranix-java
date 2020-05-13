package de.cranix.api.resourceimpl;

import java.io.File;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.cranix.api.resources.SelfManagementResource;
import de.cranix.dao.Group;
import de.cranix.dao.CrxResponse;
import de.cranix.dao.Room;
import de.cranix.dao.Session;
import de.cranix.dao.User;
import de.cranix.dao.controller.Config;
import de.cranix.dao.controller.DeviceController;
import de.cranix.dao.controller.RoomController;
import de.cranix.dao.controller.SessionController;
import de.cranix.dao.controller.UserController;
import de.cranix.dao.internal.CommonEntityManagerFactory;
import de.cranix.dao.tools.OSSShellTools;
import static de.cranix.dao.tools.StaticHelpers.*;
import static de.cranix.dao.internal.CranixConstants.*;

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
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		UserController userController = new UserController(session,em);
		User oldUser = session.getUser();
		CrxResponse  ossResponse = null;
		logger.debug("modifyMySelf" + user);
		if( userController.isAllowed("myself.manage") ) {
			if( user.getPassword() != null && !user.getPassword().isEmpty() ) {
				ossResponse = userController.checkPassword(user.getPassword());
				logger.debug("Check-Password:" + ossResponse );
				if( ossResponse != null  && ossResponse.getCode().equals("ERROR")) {
					return ossResponse;
				}
				oldUser.setPassword(user.getPassword());
			}
			oldUser.setGivenName(user.getGivenName());
			oldUser.setSurName(user.getSurName());
			oldUser.setBirthDay(user.getBirthDay());
			oldUser.setFsQuota(user.getFsQuota());
			oldUser.setMsQuota(user.getMsQuota());
			try {
				em.getTransaction().begin();
				em.merge(oldUser);
				em.getTransaction().commit();
				startPlugin("modify_user", oldUser);
			} catch (Exception e) {
				return null;
			} finally {
				em.close();
			}
			ossResponse = new CrxResponse(session,"OK","User parameters were set successfully.");
		} else {
			if( user.getPassword() != null && !user.getPassword().isEmpty() ) {
				ossResponse = userController.checkPassword(user.getPassword());
				em.close();
				if( ossResponse != null  && ossResponse.getCode().equals("ERROR")) {
					logger.debug("checkPassword:" + ossResponse);
					return ossResponse;
				}
				StringBuffer reply = new StringBuffer();
				StringBuffer error = new StringBuffer();
				String[]   program = new String[5];
				program[0] = "/usr/bin/samba-tool";
				program[1] = "user";
				program[2] = "setpassword";
				program[3] = session.getUser().getUid();
				program[4] = "--newpassword=" + user.getPassword();
				OSSShellTools.exec(program, reply, error, null);
				logger.debug("sambatool:" + reply.toString() + " Error" + error.toString() );
				ossResponse = new CrxResponse(session,"OK","User parameters were set successfully.");
			}
		}
		return ossResponse;
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
		Config config   = new Config("/etc/sysconfig/oss-vpn","");
		String vpnId    = config.getConfigValue("VPN_ID");
		File configFile = null;
		String uid  =  session.getUser().getUid();
		switch(OS) {
		case "Win7":
		case "Win10":
			configFile = new File("/var/adm/oss/vpn/oss-vpn-installer-" + vpnId + "-" + uid + ".exe");
			break;
		case "Mac":
			configFile = new File("/var/adm/oss/vpn/" + vpnId + "-" + uid + ".tar.bz2");
			break;
		case "Linux":
			configFile = new File("/var/adm/oss/vpn/" + vpnId + "-" + uid + ".tgz");
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
		EntityManager em     = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		Session session      = new Session();
		SessionController sc = new SessionController(session,em);
		String  resp         = "";
		try {
			session.setIp(req.getRemoteAddr());
			session = sc.createInternalUserSession(userName);
			final DeviceController deviceController = new DeviceController(session,em);
			if( deviceController.getByMAC(MAC) != null ) {
				resp = "ALREADY-REGISTERED";
			} else {
				final RoomController roomController = new RoomController(session,em);
				List<Room> rooms = roomController.getRoomToRegisterForUser(session.getUser());
				if( rooms != null && rooms.size() > 0 ) {
					Integer count = session.getUser().getOwnedDevices().size();
					CrxResponse ossResponse = roomController.addDevice(rooms.get(0).getId(), MAC, count.toString());
					resp =  ossResponse.getCode() + " " + ossResponse.getValue() + " " + ossResponse.getParameters();
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
}
