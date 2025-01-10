package de.cranix.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.cranix.dao.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.*;

import static de.cranix.helper.CranixConstants.cranixBaseDir;

public class StaticHelpers {

	static Logger logger = LoggerFactory.getLogger(StaticHelpers.class);

	static public SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm yyyy-MM-dd");
	static public SimpleDateFormat simpleDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	static public SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd");

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
						data.append(String.format("name: %s%n", group.getName()));
						data.append(String.format("description: %s%n", group.getDescription()));
						data.append(String.format("grouptype: %s%n", group.getGroupType()));
						break;
					case "delete_group":
						data.append(String.format("name: %s%n", group.getName()));
						break;
				}
				break;
			case "de.cranix.dao.Device":
				Device device = (Device) object;
				data.append(String.format("name: %s%n", device.getName()));
				data.append(String.format("ip: %s%n", device.getIp()));
				data.append(String.format("mac: %s%n", device.getMac()));
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
				data.append(String.format("name: %s%n", hwconf.getName()));
				data.append(String.format("id: %d%n", hwconf.getId()));
				data.append(String.format("devicetype: %s%n", hwconf.getDeviceType()));
				break;
			case "de.cranix.dao.Room":
				Room room = (Room) object;
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
		data.append(String.format("group: %s%n", group.getName()));
		List<String> uids = new ArrayList<String>();
		for (User user : users) {
			uids.add(user.getUid());
		}
		data.append(String.format("users: %s%n", String.join(",", uids)));
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
		data.append(String.format("group: %s%n", group.getName()));
		data.append(String.format("users: %s%n", user.getUid()));
		CrxSystemCmd.exec(program, reply, error, data.toString());
		logger.debug("change_member  : " + data.toString() + " : " + error);
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
}
