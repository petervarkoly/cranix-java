package de.cranix.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.cranix.dao.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.*;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.*;

import static de.cranix.helper.CranixConstants.cranixBaseDir;
import static de.cranix.helper.CranixConstants.DISALLOWED_PASSWORDS;

public class StaticHelpers {

	static Logger logger = LoggerFactory.getLogger(StaticHelpers.class);

	static public SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm yyyy-MM-dd");
	static public SimpleDateFormat simpleDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	static public SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd");
	static public SimpleDateFormat simpleYear = new SimpleDateFormat("yyyy");

	static public String createRandomPassword() {
		String[] salt = new String[3];
		salt[0] = "ABCDEFGHIJKLMNOPQRSTVWXYZ";
		salt[1] = "1234567890";
		salt[2] = "abcdefghijklmnopqrstvwxyz";
		Random rand = new Random();
		StringBuilder builder = new StringBuilder();
		int saltIndex = 2;
		int beginIndex = Math.abs(rand.nextInt() % salt[saltIndex].length());
		builder.append(salt[saltIndex].charAt(beginIndex));
		saltIndex = 1;
		beginIndex = Math.abs(rand.nextInt() % salt[saltIndex].length());
		builder.append(salt[saltIndex].charAt(beginIndex));
		saltIndex = 0;
		beginIndex = Math.abs(rand.nextInt() % salt[saltIndex].length());
		builder.append(salt[saltIndex].charAt(beginIndex));
		for (int i = 3; i < 8; i++) {
			saltIndex = Math.abs(rand.nextInt() % 3);
			beginIndex = Math.abs(rand.nextInt() % salt[saltIndex].length());
			builder.append(salt[saltIndex].charAt(beginIndex));
		}
		return builder.toString();
	}

	static public String cleanString(String s){
		return s.replaceAll("^\"", "").replaceAll("\"$", "");
	}

	static public String normalize(String input) {
		return Normalizer.normalize(
				input.replace("ß", "s"),
				Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
		//Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
		//return pattern.matcher(output).replaceAll("");
	}

	static public String normalizeTelex(String input) {
		return  Normalizer.normalize(
				input.replace("ß","ss").
						replace("ö", "oe").
						replace("ü", "ue").
						replace("ä", "ae"),
				Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
	}
	/**
	 * Start a plugin for an object by creating modifying or deleting
	 *
	 * @param pluginName The name of the plugin to be called: add_user, modify_user ...
	 * @param object	 The corresponding object.
	 */
	static public void startPlugin(String pluginName, Object object) {
		StringBuilder data = new StringBuilder();
		String[] program = new String[2];
		StringBuffer reply = new StringBuffer();
		StringBuffer error = new StringBuffer();
		program[0] = cranixBaseDir + "plugins/plugin_handler.sh";
		program[1] = pluginName;
		switch (object.getClass().getName()) {
			case "de.cranix.dao.User":
				User user = (User) object;
				String myGroups = "";
				for (Group g : user.getGroups()) {
					myGroups.concat(g.getName() + " ");
				}
				switch (pluginName) {
					case "add_user":
					case "modify_user":
						data.append(String.format("id: %d%n", user.getId()));
						data.append(String.format("givenname: %s%n", user.getGivenName()));
						data.append(String.format("surname: %s%n", user.getSurName()));
						data.append(String.format("birthday: %s%n", user.getBirthDay()));
						data.append(String.format("password: %s%n", user.getPassword()));
						data.append(String.format("uid: %s%n", user.getUid()));
						data.append(String.format("uuid: %s%n", user.getUuid()));
						data.append(String.format("role: %s%n", user.getRole()));
						data.append(String.format("fsquota: %d%n", user.getFsQuota()));
						data.append(String.format("msquota: %d%n", user.getMsQuota()));
						data.append(String.format("groups: %s%n", myGroups));
						if (user.isMustChange()) {
							data.append(String.format("mpassword: yes%n"));
						}
						break;
					case "delete_user":
						data.append(String.format("id: %d%n", user.getId()));
						data.append(String.format("uid: %s%n", user.getUid()));
						data.append(String.format("uuid: %s%n", user.getUuid()));
						data.append(String.format("role: %s%n", user.getRole()));
						data.append(String.format("groups: %s%n", myGroups));
						break;
				}
				break;
			case "de.cranix.dao.Group":
				//TODO
				Group group = (Group) object;
				switch (pluginName) {
					case "add_group":
					case "modify_group":
						data.append(String.format("id: %d%n", group.getId()));
						data.append(String.format("name: %s%n", group.getName()));
						data.append(String.format("description: %s%n", group.getDescription()));
						data.append(String.format("grouptype: %s%n", group.getGroupType()));
						break;
					case "delete_group":
						data.append(String.format("id: %d%n", group.getId()));
						data.append(String.format("name: %s%n", group.getName()));
						break;
				}
				break;
			case "de.cranix.dao.Device":
				Device device = (Device) object;
				data.append(String.format("id: %d%n", device.getId()));
				data.append(String.format("name: %s%n", device.getName()));
				data.append(String.format("ip: %s%n", device.getIp()));
				data.append(String.format("mac: %s%n", device.getMac()));
				data.append(String.format("roomname: %s%n", device.getRoom().getName()));
				if (device.getWlanIp() != null  && !device.getWlanIp().isEmpty()) {
					data.append(String.format("wlanip: %s%n", device.getWlanIp()));
					data.append(String.format("wlanmac: %s%n", device.getWlanMac()));
				}
				if (device.getHwconf() != null) {
					data.append(String.format("hwconf: %s%n", device.getHwconf().getName()));
					data.append(String.format("hwconfid: %s%n", device.getHwconfId()));
				}
				break;
			case "de.cranix.dao.HWconf":
				HWConf hwconf = (HWConf) object;
				data.append(String.format("id: %d%n", hwconf.getId()));
				data.append(String.format("name: %s%n", hwconf.getName()));
				data.append(String.format("id: %d%n", hwconf.getId()));
				data.append(String.format("devicetype: %s%n", hwconf.getDeviceType()));
				break;
			case "de.cranix.dao.Room":
				Room room = (Room) object;
				data.append(String.format("id: %d%n", room.getId()));
				data.append(String.format("name: %s%n", room.getName()));
				data.append(String.format("description: %s%n", room.getDescription()));
				data.append(String.format("startip: %s%n", room.getStartIP()));
				data.append(String.format("netmask: %s%n", room.getNetMask()));
				if (room.getHwconf() != null) {
					data.append(String.format("hwconf: %s%n", room.getHwconf().getName()));
				}
				break;
			default:
				try {
					data.append(object);
				} catch (Exception e) {
					logger.error("pluginHandler : Cephalix****:" + e.getMessage());
				}
		}
		int ret = CrxSystemCmd.exec(program, reply, error, data.toString());
		logger.debug(pluginName + " : " + data.toString() + " : " + reply + " : " + error + " : " + ret);
	}

	static public void changeMemberPlugin(String type, Group group, List<User> users) {
		//type can be only add or remove
		StringBuilder data = new StringBuilder();
		String[] program = new String[2];
		StringBuffer reply = new StringBuffer();
		StringBuffer error = new StringBuffer();
		program[0] = cranixBaseDir + "plugins/plugin_handler.sh";
		program[1] = "change_member";
		data.append(String.format("changetype: %s%n", type));
		data.append(String.format("id: %s%n", group.getId()));
		data.append(String.format("group: %s%n", group.getName()));
		List<String> uids = new ArrayList<String>();
		List<String> ids = new ArrayList<String>();
		for (User user : users) {
			uids.add(user.getUid());
			ids.add(user.getId().toString());
		}
		data.append(String.format("users: %s%n", String.join(",", uids)));
		data.append(String.format("userIds: %s%n", String.join(",", ids)));
		CrxSystemCmd.exec(program, reply, error, data.toString());
		logger.debug("change_member  : " + data.toString() + " : " + error);
	}

	static public void changeMemberPlugin(String type, Group group, User user) {
		//type can be only add or remove
		StringBuilder data = new StringBuilder();
		String[] program = new String[2];
		StringBuffer reply = new StringBuffer();
		StringBuffer error = new StringBuffer();
		program[0] = cranixBaseDir + "plugins/plugin_handler.sh";
		program[1] = "change_member";
		data.append(String.format("changetype: %s%n", type));
		data.append(String.format("id: %s%n", group.getId()));
		data.append(String.format("group: %s%n", group.getName()));
		data.append(String.format("users: %s%n", user.getUid()));
		data.append(String.format("userIds: %s%n", user.getId()));
		CrxSystemCmd.exec(program, reply, error, data.toString());
		logger.debug("change_member  : " + data + " : " + error);
	}

	static public String createLiteralJson(Object object) {
		String jSon = "";
		ObjectMapper mapper = new ObjectMapper();
		try {
			jSon = mapper.writeValueAsString(object);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		//return jSon + System.getProperty("line.separator");
		return jSon;
	}

	@SuppressWarnings("unchecked")
	static public Map<String, Object> convertObjectToMap(Object object) {
		return new ObjectMapper().convertValue(object, Map.class);
	}

	static public String convertJavaTime(Long times) {
		return simpleDateFormat.format(new Date(times));
	}

	static public String convertJavaDate(Long times) {
		return simpleDateFormat.format(new Date(times));
	}

	static public String getYear(Long times) {
		return simpleYear.format(new Date(times));
	}
	static public String getYear() {
		return simpleYear.format(new Date());
	}



	static public void reloadFirewall() {
		String[] program = new String[2];
		StringBuffer reply = new StringBuffer();
		StringBuffer error = new StringBuffer();
		program[0] = "/usr/bin/firewall-cmd";
		program[1] = "--reload";
		CrxSystemCmd.exec(program, reply, error, null);
	}

	static public List<Long> aMinusB(List<Long>a, List<Long>b){
		List<Long> c = new ArrayList<>();
		for(Long x: a){
			if( !b.contains(x)){
				c.add(x);
			}
		}
		return c;
	}

	static public String getRandomColor(){
		Random random = new Random();
		// Generiere zufällige Werte für R, G und B
		int r = random.nextInt(256); // Zufälliger Wert zwischen 0 und 255
		int g = random.nextInt(256);
		int b = random.nextInt(256);
		// Konvertiere die Werte in hexadezimale Formate und formatiere sie
		return String.format("#%02X%02X%02X", r, g, b);
	}

	public static boolean canUserWriteToDirectory(User user, Path directory) throws IOException {
		// Prüfe, ob das Dateisystem POSIX-Berechtigungen unterstützt
		if (!Files.getFileStore(directory).supportsFileAttributeView(PosixFileAttributeView.class)) {
			System.out.println("Das Dateisystem unterstützt keine POSIX-Berechtigungen.");
			return false; // Oder handle dies anders, z.B. eine Exception werfen
		}

		// 1. Hole die Benutzer- und Gruppeninformationen des Verzeichnisses
		FileOwnerAttributeView ownerView = Files.getFileAttributeView(directory, FileOwnerAttributeView.class);
		UserPrincipal owner = ownerView.getOwner();
		GroupPrincipal group = (GroupPrincipal) Files.getAttribute(directory, "posix:group");

		// 2. Hole die POSIX-Berechtigungen
		Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(directory);
		List<String> groups = new ArrayList<>();
		for(Group g : user.getGroups()){
			groups.add(g.getName());
		}
		// 3. Vergleiche den Benutzernamen und prüfe die Berechtigungen
		if (owner.getName().equals(user.getUid())) {
			return permissions.contains(PosixFilePermission.OWNER_WRITE);
		} else if (group != null && groups.contains(group.getName())) {
			// Hinweis: Eine komplexere Prüfung wäre hier erforderlich, um alle Gruppen des Benutzers zu finden
			// Aber diese einfache Prüfung ist für die meisten Fälle ausreichend.
			return permissions.contains(PosixFilePermission.GROUP_WRITE);
		} else {
			return permissions.contains(PosixFilePermission.OTHERS_WRITE);
		}
	}

}
