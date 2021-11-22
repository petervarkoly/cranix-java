/* 2020 (C) Peter Varkoly <pvarkoly@cephalix.eu> - all rights reserved */
package de.cranix.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import de.cranix.dao.Session;
import de.cranix.dao.User;
import de.cranix.services.DeviceService;
import de.cranix.services.UserService;
import de.cranix.dao.Room;
import de.cranix.dao.Device;
import de.cranix.dao.Group;
import de.cranix.dao.Acl;
import de.cranix.helper.*;
import static de.cranix.helper.CranixConstants.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.dropwizard.auth.AuthenticationException;

@SuppressWarnings("unchecked")
public class SessionService extends Service {

	Logger logger = LoggerFactory.getLogger(SessionService.class);

	public SessionService(Session session,EntityManager em) {
		super(session,em);
	}

	public SessionService(EntityManager em) {
		super(null,em);
	}

	public static Map<String, Session> sessions = new ConcurrentHashMap<String, Session>();

	private static String createSessionToken(String prefix) {
		String token =  prefix + "_" + UUID.randomUUID().toString();
		return token.length() > 60 ? token.substring(0,60) : token ;
        }

	static public void removeAllSessionsFromCache() {
		sessions.clear();
	}

	/**
	 * Remove a session from the static variable and from database too.
	 * @param session
	 */
	public void deleteSession(Session session) {
		if (session != null &&
			!session.getToken().equals(this.getProperty("de.cranix.api.auth.localhost")) ) {
			//The local token must not be removed.
			sessions.remove(session.getToken());
			remove(session); // delete from database
		}
	}

	public Session createSessionWithUser(String username, String password, String deviceType) {
		UserService userService = new UserService(this.session,this.em);
		DeviceService deviceService = new DeviceService(this.session,this.em);
		Room room = null;
		String[]   program = new String[5];
		StringBuffer reply = new StringBuffer();
		StringBuffer error = new StringBuffer();
		program[0] = "/usr/bin/smbclient";
		program[1] = "-L";
		program[2] = "admin";
		program[3] = "-A";
		try {
			File file = File.createTempFile("login", ".cred", new File(cranixTmpDir));
			List<String> credentials = new ArrayList<String>();
			credentials.add("username=" + username);
			credentials.add("password=" + password);
			credentials.add("domain=" + this.getConfigValue("WORKGROUP"));
			Files.write(file.toPath(), credentials);
			program[4] = file.getAbsolutePath();
			CrxSystemCmd.exec(program, reply, error, null);
			if( ! logger.isDebugEnabled() ) {
				Files.delete(file.toPath());
			}
			logger.debug("Login reply:" + reply.toString());
			logger.debug("Login error:" + error.toString());
			if( reply.toString().contains("NT_STATUS_PASSWORD_MUST_CHANGE") ) {
				this.session.setMustChange(true);
			}else if( reply.toString().contains("NT_STATUS_")) {
				return null;
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return null;
		}

		//TODO what to do with deviceType
		User user = userService.getByUid(username);
		if( user == null ) {
			return null;
		}

		/**
		 * For debug reason.
		 * Create Variable CRANIX_USER_<USERNAME>_SESSION_IP="<IP-Of-The-Device>" in /etc/sysconfig/cranix
		 * And the user will logg in on this device everytime
		 **/
		if( !this.getConfigValue("USER_" + username + "_SESSION_IP").isEmpty() ) {
			this.getSession().setIp(this.getConfigValue("USER_" + username + "_SESSION_IP"));
		}
		String IP = this.getSession().getIp();
		Device device = deviceService.getByIP(IP);
		if( device != null ) {
			room = device.getRoom();
		}

		String token = createSessionToken(username);
		while( this.getByToken(token) != null ) {
			token = createSessionToken(username);
		}
		this.session.setToken(token);
		this.session.setUserId(user.getId());
		this.session.setRole(user.getRole());
		this.session.setUser(user);
		if( room != null ) {
			this.session.setRoomId(room.getId());
			this.session.setRoom(room);
			this.session.setRoomName(room.getName());
		}
		if( device != null ) {
			this.session.setDeviceId(device.getId());
			this.session.setMac(device.getMac());
			this.session.setIp(device.getIp());
			this.session.setDNSName(device.getName());
			this.session.setDevice(device);
		} else {
			//Evaluate the MAC Address
			if( !IP.contains("127.0.0.1") && IPv4.validateIPAddress(IP) && room == null ) {
				reply = new StringBuffer();
				error = new StringBuffer();
				program = new String[3];
				program[0] = "/sbin/arp";
				program[1] = "-n";
				program[2] = IP;
				CrxSystemCmd.exec(program, reply, error, null);
				String[] lines = reply.toString().split("\\n");
				if( lines.length >1 ) {
					String[] fields = lines[1].split("\\s+");
					if( fields.length > 2 ) {
						this.session.setMac(fields[2]);
					}
				}
			}
		}

		this.session.setCommonName(user.getGivenName() + " " + user.getSurName());
		List<String> modules = this.session.getUserAcls();
		if( !this.isSuperuser() ) {
			RoomService  roomService = new RoomService(this.session,this.em);;
			if( ! roomService.getAllToRegister().isEmpty() ) {
				modules.add("adhoclan.mydevices");
			}
		}

		this.session.setAcls(modules);
		this.session.setPassword(password);
		sessions.put(token, this.session);
		save(session);
		return this.session;
	}

	public Session createInternalUserSession(String username) {
		UserService userService = new UserService(this.session,this.em);
		User user = userService.getByUid(username);
		if( user == null ) {
			return null;
		}
		String token = createSessionToken("dummy");
		while( this.getByToken(token) != null ) {
			token = createSessionToken("dummy");
		}
		this.session.setToken(token);
		this.session.setUserId(user.getId());
		this.session.setRole(user.getRole());
		this.session.setUser(user);
		this.session.setCommonName(user.getGivenName() + " " + user.getSurName());
		List<String> modules = session.getUserAcls();
		if( !this.isSuperuser() ) {
			RoomService  roomService = new RoomService(this.session,this.em);;
			if( ! roomService.getAllToRegister().isEmpty() ) {
				modules.add("adhoclan.mydevices");
			}
		}
		this.session.setAcls(modules);
		sessions.put(token, this.session);
		save(session);
		return this.session;
	}

	private void save(Session obj) {
		if (em != null) {
			try {
				this.em.getTransaction().begin();
				if (obj.getId() > 0) {
					this.em.merge(obj);
				} else {
					this.em.persist(obj);
				}
				User user = obj.getUser();
				Device device = obj.getDevice();
				if( device != null ) {
					if( ! user.getLoggedOn().contains(device) ) {
						user.getLoggedOn().add(device);
						device.getLoggedIn().add(user);
						this.em.merge(device);
					}
				}
				user.getSessions().add(obj);
				this.em.merge(user);
				this.em.flush();
				this.em.refresh(obj);
				this.em.getTransaction().commit();
			} catch (Exception e) {
				logger.error(e.getMessage());
			} finally {
			}
		}
	}

	private Session find(Long id) {
		Session data = null;
		if (em != null) {
			try {
				this.em.getTransaction().begin();
				data = this.em.find(Session.class, id);
				this.em.getTransaction().commit();
			} catch (Exception e) {
				logger.error(e.getMessage());
			} finally {
				if ((em != null) && (em.isOpen())) {
				}
			}
		}
		return data;
	}

	private void remove(Session session) {
		if (em != null) {
			try {
				this.em.getTransaction().begin();
				Session foundSession = this.em.find(Session.class, session.getId());
				if (foundSession != null) {
					Device device = foundSession.getDevice();
					if( device != null ) {
						User user = foundSession.getUser();
						user.getLoggedOn().remove(device);
						device.getLoggedIn().remove(user);
						this.em.merge(device);
						this.em.merge(user);
					}
					this.em.remove(foundSession);
				}
				this.em.getTransaction().commit();
			} catch (Exception e) {
				logger.error(e.getMessage());
			} finally {
				if ((em != null) && (em.isOpen())) {
				}
			}
		}
	}

	public Session getByToken(String token) {
		Session data = null;
		if (em != null) {
			try {
				Query q = this.em.createNamedQuery("Session.getByToken").setParameter("token", token).setMaxResults(1);
				List<Session> sessions = q.getResultList();
				if ((sessions != null) && (sessions.size() > 0)) {
					data = sessions.get(0);
				}
			} catch (Exception e) {
				logger.error(e.getMessage());
			} finally {
				if ((em != null) && (em.isOpen())) {
				}
			}
		}
		return data;
	}

	public Session validateToken(String token) throws AuthenticationException {
		Session session = sessions.get(token);
		if (session == null) {
			session = getByToken(token);
			if (session != null) {
				sessions.put(token, session);
			} else {
				return null;
			}
		}
		if( !isSuperuser(session)) {
			Long timeout = 90L;
			try {
				timeout = Long.valueOf(this.getConfigValue("SESSION_TIMEOUT"));
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
			timeout = timeout * 60000;
			if( logger.isDebugEnabled() ) {
				logger.info(String.format(
						"timeout: %d now: %d session time: %d",
						timeout,
						now().getTime(),
						session.getCreateDate().getTime() ));
			}
			if( (now().getTime() - session.getCreateDate().getTime())  > timeout ) {
				logger.info("Session was timed out." + session);
				deleteSession(session);
				//throw new AuthenticationException("Session expired.");
				return null;
			} else {
				updateSession(session);
			}
		}
		return session; // todo handle correct validation
	}

	public void updateSession(Session session) {
		try {
			this.em.getTransaction().begin();
			session.setCreateDate(now());
			this.em.merge(session);
			this.em.getTransaction().commit();
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
		}
	}

	public boolean authorize(Session session, String requiredRole){

		/**
		 * If the required role is the role of the user then he is authorized.
		 */
		if( session.getUser().getRole().contains(requiredRole)) {
			return true;
		}

		for( String acl : session.getAcls() ){
			if( acl.startsWith(requiredRole)) {
				return true;
			}
		}
		/**
		 * A required role can be allowed or denied by the user
		for( Acl acl : session.getUser().getAcls() ){
			if( acl.getAcl().startsWith(requiredRole)) {
				return acl.getAllowed();
			}
		}
		*
		 * A required role can be allowed or denied by one of the groups of a user.
		for( Group group : session.getUser().getGroups() ) {
			for( Acl acl : group.getAcls() ) {
				if( acl.getAcl().startsWith(requiredRole)) {
					return acl.getAllowed();
				}
			}
		}*/
		return false;
	}

	public Session getLocalhostSession() {
		String token = this.getProperty("de.cranix.api.auth.localhost");
		if( token != null ) {
			return this.getByToken(token);
		}
		return null;
	}

	public String logonScript(String OS) {
		//TODO make logon server configurable
		String[]   program = new String[6];
		StringBuffer reply = new StringBuffer();
		StringBuffer error = new StringBuffer();
		List<String> batFile = new ArrayList<String>();
		batFile.add("net use z: \\\\admin\\"+ this.session.getUser().getUid()
					+ " /persisten:no /user:"
					+ this.getConfigValue("WORKGROUP")+"\\"
					+ this.session.getUser().getUid() + " \""
					+ this.session.getPassword() + "\"");
		program[0] = cranixBaseDir + "plugins/shares/netlogon/open/100-create-logon-script.sh";
		program[1] = this.session.getUser().getUid();
		program[2] = this.session.getIp();
		program[3] = OS;
		if( this.session.getDevice() != null ) {
			program[4] = this.session.getDevice().getName();
		} else {
			program[4] = "dummy";
		}
		program[5] = this.getConfigValue("DOMAIN");
		CrxSystemCmd.exec(program, reply, error, null);
		File file = new File("/var/lib/samba/sysvol/" + this.getConfigValue("DOMAIN") + "/scripts/" +  this.session.getUser().getUid() + ".bat");
		if( file.exists() ) {
			try {
				String tmp = System.getProperty("line.separator");
				System.setProperty("line.separator", winLineSeparator);
				for(String line : Files.readAllLines(file.toPath()) ) {
					if( line.startsWith("net use z:") ||
						line.contains("netlogon")) {
						continue;
					}
					if( line.startsWith("net use") || line.startsWith("rundll32 printui.dll")) {
						batFile.add(line);
					}
				}
				System.setProperty("line.separator", tmp);
			} catch (Exception e) {
				logger.error("logonScript" + e.getMessage());
			}
		}
		return String.join(winLineSeparator,batFile);
	}
}
