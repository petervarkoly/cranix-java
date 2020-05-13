/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.dao.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.cranix.dao.Device;
import de.cranix.dao.CrxResponse;
import de.cranix.dao.PositiveList;
import de.cranix.dao.ProxyRule;
import de.cranix.dao.Room;
import de.cranix.dao.Session;
import de.cranix.dao.User;
import de.cranix.dao.tools.OSSShellTools;
import static de.cranix.dao.internal.CranixConstants.*;

@SuppressWarnings( "unchecked" )
public class ProxyController extends Controller {

	Logger logger = LoggerFactory.getLogger(ProxyController.class);

	private Path DESCIPTION = Paths.get("/var/lib/squidGuard/db/BL/global_usage");
	private Map<String,String>   desc     = new HashMap<>();
	private Map<String,String>   longDesc = new HashMap<>();;
	private List<String>         configFile;

	public ProxyController(Session session,EntityManager em) {
		super(session,em);
		try {
			configFile = Files.readAllLines(this.DESCIPTION);
		}
		catch( IOException e ) {
			e.printStackTrace();
		}
		String  lang        = this.getConfigValue("LANGUAGE");
		String  key         = "";
		String  value       = "";
		String  longValue   = "";
		String  valueEN     = "";
		String  longValueEN = "";
		String  nameStart   = "NAME "+ lang +":";
		String  descStart   = "DESC "+ lang +":";
		String  tmp[];
		for ( String line : configFile ){
			if( line.startsWith("NAME:")) {
				if( !key.isEmpty() ) {
					if( !value.isEmpty() ) {
						desc.put(key, value);
					} else {
						desc.put(key, valueEN);
					}
					if( !longValue.isEmpty() ) {
						longDesc.put(key, longValue);
					} else {
						longDesc.put(key, longValueEN);
					}
				}
				key         = line.split(":\\s+")[1].replaceFirst("/", "-");
				value       = "";
				longValue   = "";
				valueEN     = "";
				longValueEN = "";
			}
			if( line.startsWith("NAME EN:")) {
				valueEN     = line.split(":\\s")[1];
			}
			if( line.startsWith(nameStart)) {
				tmp = line.split(":\\s");
				if( tmp.length == 2 ) {
					value     = tmp[1];
				}
			}
			if( line.startsWith("DESC EN:")) {
				longValueEN     = line.split(":\\s")[1];
			}
			if( line.startsWith(descStart)) {
				tmp = line.split(":\\s");
				if( tmp.length == 2 ) {
					longValue     = tmp[1];
				}
			}
		}
		if( !key.isEmpty() ) {
			if( !value.isEmpty() ) {
				desc.put(key, value);
			} else {
				desc.put(key, valueEN);
			}
			if( !longValue.isEmpty() ) {
				longDesc.put(key, longValue);
			} else {
				longDesc.put(key, longValueEN);
			}
		}
	}

	/*
	 * Reads the default proxy acl setting
	 * @return The default proxy acl setting
	 */
	public List<ProxyRule> readDefaults(String role) {
		List<ProxyRule> acl = new ArrayList<ProxyRule>();
		String[] program   = new String[2];
		program[0] = cranixBaseDir + "tools/squidGuard.pl";
		program[1] = "read";
		StringBuffer reply = new StringBuffer();
		StringBuffer error = new StringBuffer();
		OSSShellTools.exec(program, reply, error, "");
		for( String line : reply.toString().split("\\n") ) {
			String[] values = line.split(" ");
			if( role.equals(values[0])) {
				for(int i=1; i < values.length; i++ ) {
					String key = values[i].split(":")[0];
					boolean enabled = values[i].split(":")[1].equals("true");
					if( !key.equals("good") && !key.equals("bad") && !key.equals("cephalix") ) {
						ProxyRule proxyRule = new ProxyRule(
							key,
							enabled,
							( desc.containsKey(key)     ? desc.get(key) : key),
							( longDesc.containsKey(key) ? longDesc.get(key) : key )
							);
						acl.add(proxyRule);
					}
				}
			}
		}
		return acl;
	}

	public Map<String, List<ProxyRule>> readDefaults() {
		List<String> roles = new SystemController(this.session,this.em).getEnumerates("role");
		roles.add("default");
		Map<String, List<ProxyRule>> acls = new HashMap<String, List<ProxyRule>>();
		for (String role : roles ) {
			acls.put(role, readDefaults(role));
		}
		return acls;
	}

	public CrxResponse setDefaults(Map<String, List<ProxyRule>> acls) {
		List<String> roles = new SystemController(this.session,this.em).getEnumerates("role");
		roles.add("default");
		StringBuilder output = new StringBuilder();
		for (String role : roles ) {
			output.append(role).append(":").append("cephalix:true").append(this.getNl());
			output.append(role).append(":").append("good:true").append(this.getNl());
			output.append(role).append(":").append("bad:false").append(this.getNl());
			List<ProxyRule> rules = acls.get(role);
			rules.sort(Comparator.comparing(ProxyRule::getDescription));
			for(ProxyRule proxyRule : rules ) {
				output.append(role).append(":").append(proxyRule.getName()).append(":");
				if( proxyRule.isEnabled() ) {
					output.append("true");
				} else {
					output.append("false");
				}
				output.append(this.getNl());
			}
		}
		String[] program   = new String[2];
		program[0] = cranixBaseDir + "tools/squidGuard.pl";
		program[1] = "write";
		StringBuffer reply = new StringBuffer();
		StringBuffer error = new StringBuffer();
		logger.debug(output.toString());
		OSSShellTools.exec(program, reply, error, output.toString());
		return new CrxResponse(this.session,"OK","Proxy Setting was saved succesfully.");
	}
	/*
	 * Writes the default proxy setting
	 * @param acls The list of the default acl setting
	 * @return An CrxResponse object with the result
	 */
	public CrxResponse setDefaults(String role, List<ProxyRule> acl) {
		StringBuilder output = new StringBuilder();
		output.append(role).append(":").append("cephalix:true").append(this.getNl());
		output.append(role).append(":").append("good:true").append(this.getNl());
		output.append(role).append(":").append("bad:false").append(this.getNl());
		for(ProxyRule proxyRule : acl ) {
			output.append(role).append(":").append(proxyRule.getName()).append(":");
			if( proxyRule.isEnabled() ) {
				output.append("true");
			} else {
				output.append("false");
			}
			output.append(this.getNl());
		}
		String[] program   = new String[2];
		program[0] = cranixBaseDir + "tools/squidGuard.pl";
		program[1] = "write";
		StringBuffer reply = new StringBuffer();
		StringBuffer error = new StringBuffer();
		logger.debug(output.toString());
		OSSShellTools.exec(program, reply, error, output.toString());
		return new CrxResponse(this.session,"OK","Proxy Setting was saved succesfully.");
	}

	public PositiveList getPositiveListById( Long positiveListId ) {
		try {
			return this.em.find(PositiveList.class, positiveListId);
		} catch (Exception e) {
			logger.debug("PositiveList:" + positiveListId + " " + e.getMessage(),e);
			return null;
		} finally {
		}
	}

	public PositiveList getPositiveListByName( String name ) {
		try {
			Query query = this.em.createNamedQuery("PositiveList.byName").setParameter("name", name);
			return (PositiveList) query.getResultList().get(0);
		} catch (Exception e) {
			return null;
		} finally {
		}
	}

	/*
	 * Creates or modify a positive list
	 * @param positiveList The positive list to be saved
	 * @return An CrxResponse object with the result
	 */
	public CrxResponse editPositiveList(PositiveList positiveList) {
		logger.debug(positiveList.toString());
		PositiveList oldPositiveList = this.getPositiveListById(positiveList.getId());
		try {
			positiveList.setOwner(session.getUser());
			this.em.getTransaction().begin();
			if( oldPositiveList == null ) {
				User user = this.session.getUser();
				int count = user.getOwnedPositiveLists().size();
				positiveList.setName(user.getUid() + String.valueOf(count));
				positiveList.setOwner(user);
				this.em.persist(positiveList);
				user.getOwnedPositiveLists().add(positiveList);
				this.em.merge(user);
			} else {
				oldPositiveList.setDescription(positiveList.getDescription());
				oldPositiveList.setSubject(positiveList.getSubject());
				this.em.merge(oldPositiveList);
			}
			this.em.getTransaction().commit();
			String[] program   = new String[3];
			program[0] = cranixBaseDir + "tools/squidGuard.pl";
			program[1] = "writePositiveList";
			program[2] = positiveList.getName();
			StringBuffer reply = new StringBuffer();
			StringBuffer error = new StringBuffer();
			OSSShellTools.exec(program, reply, error, positiveList.getDomains());
			return new CrxResponse(this.getSession(),"OK", "Postive list was created/modified succesfully.",positiveList.getId());
		} catch (Exception e) {
			logger.error("add " + e.getMessage(),e);
			return new CrxResponse(this.getSession(),"ERROR", e.getMessage());
		} finally {
		}
	}

	public CrxResponse deletePositiveList(Long positiveListId) {
		PositiveList positiveList = this.getPositiveListById(positiveListId);
		try {
			Files.deleteIfExists(Paths.get("/var/lib/squidGuard/db/PL/" + positiveList.getName() + "/domains"));
			this.em.getTransaction().begin();
			this.em.remove(positiveList);
			this.em.getTransaction().commit();
			String[] program   = new String[2];
			program[0] = cranixBaseDir + "tools/squidGuard.pl";
			program[1] = "write";
			StringBuffer reply = new StringBuffer();
			StringBuffer error = new StringBuffer();
			String acls = "dummy:" + positiveList.getName() + ":delete\n";
			OSSShellTools.exec(program, reply, error, acls);
		}  catch (Exception e) {
			logger.error("delete " + e.getMessage(),e);
			return new CrxResponse(this.getSession(),"ERROR", e.getMessage());
		} finally {
		}
		return new CrxResponse(this.getSession(),"OK", "Postive list was deleted succesfully.");
	}

	/*
	 * Reads a positive list
	 * @param name The name of the positive list
	 * @return The domain list of the positive list
	 */
	public PositiveList getPositiveList(Long id) {
		PositiveList positiveList = this.getPositiveListById(id);
		try {
			positiveList.setDomains(
					String.join(
							this.getNl(),
							Files.readAllLines(Paths.get("/var/lib/squidGuard/db/PL/" + positiveList.getName() + "/domains"))
							)
					);
		}
		catch( IOException e ) {
			e.printStackTrace();
		}
		return positiveList;
	}

	/*
	 * Reads the available positive lists
	 * @return The list of positive lists the user can use
	 */
	public List<PositiveList> getAllPositiveLists() {
		try {
			Query query = this.em.createNamedQuery("PositiveList.findAll");
			return query.getResultList();
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return new ArrayList<>();
		} finally {
		}
	}

	/*
	 * Reads the available positive lists
	 * @return The list of positive lists the user can use
	 */
	public List<PositiveList> getMyPositiveLists() {
		return this.session.getUser().getOwnedPositiveLists();
	}

	public List<PositiveList> getPositiveListsInRoom(Long roomId) {
		List<PositiveList> positiveLists = new ArrayList<PositiveList>();
		Room room  = new RoomController(this.session,this.em).getById(roomId);
		String[] program   = new String[3];
		program[0] = cranixBaseDir + "tools/squidGuard.pl";
		program[1] = "readRoom";
		program[2] = room.getName();
		StringBuffer reply = new StringBuffer();
		StringBuffer error = new StringBuffer();
		StringBuilder acls = new StringBuilder();
		OSSShellTools.exec(program, reply, error, acls.toString());
		for( String roomName : reply.toString().split(" ") ) {
			roomName = roomName.trim();
			PositiveList positiveList = this.getPositiveListByName(roomName);
			if( positiveList != null ) {
				positiveLists.add(positiveList);
			}
		}
		return positiveLists;
	}
	/**
	 * Sets positive lists in a room
	 * @param roomId The room id.
	 * @param positiveListIds list of positiveList ids which have to be set in this room
	 * @return An CrxResponse object with the result
	 */
	public CrxResponse setAclsInRoom(Long roomId, List<Long> positiveListIds) {

		DeviceController deviceController = new DeviceController(this.session,this.em);;
		Room room         = new RoomController(this.session,this.em).getById(roomId);
		StringBuilder ips = new StringBuilder();
		for(List<Long> loggedOn : new EducationController(this.session,this.em).getRoom(roomId)) {
			Device device = deviceController.getById(loggedOn.get(1));
			if( device.getIp() != null && !device.getIp().isEmpty() ) {
				ips.append(device.getIp()).append(this.getNl());
			}
			if( device.getWlanIp() != null && !device.getWlanIp().isEmpty() ) {
				ips.append(device.getWlanIp()).append(this.getNl());
			}
		}
		String[] program  = new String[3];
		program[0] = cranixBaseDir + "tools/squidGuard.pl";
		program[1] = "writeIpSource";
		program[2] = room.getName();
		StringBuffer reply = new StringBuffer();
		StringBuffer error = new StringBuffer();
		OSSShellTools.exec(program, reply, error, ips.toString());
		StringBuilder acls = new StringBuilder();
		for( Long id : positiveListIds ) {
			PositiveList positiveList = this.getPositiveList(id);
			acls.append(room.getName()).append(":").append(positiveList.getName()).append(":true\n");
		}
		acls.append(room.getName()).append(":all:false\n");
		program  = new String[2];
		program[0] = cranixBaseDir + "tools/squidGuard.pl";
		program[1] = "write";
		OSSShellTools.exec(program, reply, error, acls.toString());
		return new CrxResponse(this.session,"OK","Proxy Setting was saved succesfully in your room.");
	}

	/**
	 * Removes all positive list in a room and the default settings occurs
	 * @param roomId The room id
	 */
	public CrxResponse deleteAclsInRoom(Long roomId) {
		String[] program   = new String[2];
		program[0] = cranixBaseDir + "tools/squidGuard.pl";
		program[1] = "write";
		StringBuffer reply = new StringBuffer();
		StringBuffer error = new StringBuffer();
		Room room  = new RoomController(this.session,this.em).getById(roomId);
		String acls = room.getName() + ":remove-this-list:true\n";
		OSSShellTools.exec(program, reply, error, acls);
		return new CrxResponse(this.session,"OK","Positive lists was succesfully deactivated in your room.");
	}
}
