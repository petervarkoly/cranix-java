package de.openschoolserver.api.resourceimpl;

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

import de.openschoolserver.api.resources.SelfManagementResource;
import de.openschoolserver.dao.Group;
import de.openschoolserver.dao.OssResponse;
import de.openschoolserver.dao.Room;
import de.openschoolserver.dao.Session;
import de.openschoolserver.dao.User;
import de.openschoolserver.dao.controller.Config;
import de.openschoolserver.dao.controller.DeviceController;
import de.openschoolserver.dao.controller.RoomController;
import de.openschoolserver.dao.controller.SessionController;
import de.openschoolserver.dao.controller.UserController;
import de.openschoolserver.dao.internal.CommonEntityManagerFactory;
import de.openschoolserver.dao.tools.OSSShellTools;
import static de.openschoolserver.dao.tools.StaticHelpers.*;

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
	public OssResponse modifyMySelf(Session session, User user) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		UserController userController = new UserController(session,em);
		User oldUser = session.getUser();
		OssResponse  ossResponse = null;
		logger.debug("modifyMySelf" + user);
		if( user.getPassword() != null && !user.getPassword().isEmpty() ) {
			ossResponse = userController.checkPassword(user.getPassword());
			logger.debug("Check-Password:" + ossResponse );
			if( ossResponse != null  && ossResponse.getCode().equals("ERROR")) {
				return ossResponse;
			}
			oldUser.setPassword(user.getPassword());
		}
		if( userController.isAllowed("myself.manage") ) {
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
			return new OssResponse(session,"ERROR","Could not modify user parameter.");
		} finally {
			em.close();
		}
		return new OssResponse(session,"OK","User parameters were set successfully.");
	}

	@Override
	public Boolean haveVpn(Session session) {
		File vpn = new File("/usr/share/oss/tools/vpn");
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
			program[0] = "/usr/share/oss/tools/vpn/create-config.sh";
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
			session.setIP(req.getRemoteAddr());
			session = sc.createInternalUserSession(userName);
			final DeviceController deviceController = new DeviceController(session,em);
			if( deviceController.getByMAC(MAC) != null ) {
				resp = "ALREADY-REGISTERED";
			} else {
				final RoomController roomController = new RoomController(session,em);
				List<Room> rooms = roomController.getRoomToRegisterForUser(session.getUser());
				if( rooms != null && rooms.size() > 0 ) {
					Integer count = session.getUser().getOwnedDevices().size();
					OssResponse ossResponse = roomController.addDevice(rooms.get(0).getId(), MAC, count.toString());
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
