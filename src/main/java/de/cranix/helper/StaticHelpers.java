package de.cranix.helper;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.cranix.dao.Device;
import de.cranix.dao.Group;
import de.cranix.dao.HWConf;
import de.cranix.dao.Room;
import de.cranix.dao.User;
import static de.cranix.helper.CranixConstants.*;

public class StaticHelpers {

	static Logger logger = LoggerFactory.getLogger(StaticHelpers.class);

	static public String createRandomPassword()
	{
		String[] salt = new String[3];
		salt[0] = "ABCDEFGHIJKLMNOPQRSTVWXYZ";
		salt[1] = "1234567890";
		salt[2] = "abcdefghijklmnopqrstvwxyz";
		Random rand = new Random();
		StringBuilder builder = new StringBuilder();
		int saltIndex  = 2;
		int beginIndex = Math.abs(rand.nextInt() % salt[saltIndex].length());
		builder.append(salt[saltIndex].substring(beginIndex, beginIndex + 1));
		saltIndex  = 1;
		beginIndex = Math.abs(rand.nextInt() % salt[saltIndex].length());
		builder.append(salt[saltIndex].substring(beginIndex, beginIndex + 1));
		saltIndex  = 0;
		beginIndex = Math.abs(rand.nextInt() % salt[saltIndex].length());
		builder.append(salt[saltIndex].substring(beginIndex, beginIndex + 1));
		for (int i = 3; i < 8; i++)
		{
			saltIndex  = Math.abs(rand.nextInt() % 3 );
			beginIndex = Math.abs(rand.nextInt() % salt[saltIndex].length());
			builder.append(salt[saltIndex].substring(beginIndex, beginIndex + 1));
		}
		return builder.toString();
	}

	static public String normalize(String input) {
		return  Normalizer.normalize(
			input.replace("ß","s"),
			Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
		//Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
		//return pattern.matcher(output).replaceAll("");
	}

	static public String normalizeTelex(String input) {
		logger.debug("normalizeTelex input: " + input.replace("ß","ss").replace("ö", "oe").replace("ü", "ue").replace("ä", "ae"));
		return  Normalizer.normalize(
				input.replace("ß","ss").
						replace("ö", "oe").
						replace("ü", "ue").
						replace("ä", "ae"),
				Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
		//Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
		//return pattern.matcher(output).replaceAll("");
	}
	/**
	 * Start a plugin for an object by creating modifying or deleting
	 * @param pluginName The name of the plugin to be called: add_user, modify_user ...
	 * @param object The corresponding object.
	 */
	static public void startPlugin(String pluginName, Object object){
		StringBuilder data = new StringBuilder();
		String[] program   = new String[2];
		StringBuffer reply = new StringBuffer();
		StringBuffer error = new StringBuffer();
		program[0] = cranixBaseDir + "plugins/plugin_handler.sh";
		program[1] = pluginName;
		switch(object.getClass().getName()) {
		case "de.cranix.dao.User":
			User user = (User)object;
			String myGroups = "";
			for(Group g : user.getGroups()) {
				myGroups.concat(g.getName() + " ");
			}
			switch(pluginName) {
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
				if( user.isMustChange() ) {
					data.append("mpassword: yes");
				}
				data.append(String.format("groups: %s%n", myGroups));
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
			Group group = (Group)object;
			switch(pluginName){
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
			Device device = (Device)object;
			data.append(String.format("name: %s%n", device.getName()));
			data.append(String.format("ip: %s%n", device.getIp()));
			data.append(String.format("mac: %s%n", device.getMac()));
			if( ! device.getWlanIp().isEmpty() ) {
				data.append(String.format("wlanip: %s%n", device.getWlanIp()));
				data.append(String.format("wlanmac: %s%n", device.getWlanMac()));
			}
			if( device.getHwconf() != null ) {
				data.append(String.format("hwconf: %s%n", device.getHwconf().getName()));
				data.append(String.format("hwconfid: %s%n", device.getHwconf().getId()));
			}
			break;
		case "de.cranix.dao.HWconf":
			HWConf hwconf = (HWConf)object;
			data.append(String.format("name: %s%n", hwconf.getName()));
			data.append(String.format("id: %d%n", hwconf.getId()));
			data.append(String.format("devicetype: %s%n", hwconf.getDeviceType()));
			break;
		case "de.cranix.dao.Room":
			Room room = (Room)object;
			data.append(String.format("name: %s%n", room.getName()));
			data.append(String.format("description: %s%n", room.getDescription()));
			data.append(String.format("startip: %s%n", room.getStartIP()));
			data.append(String.format("netmask: %s%n", room.getNetMask()));
			if(room.getHwconf() != null ) {
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
		logger.debug(pluginName + " : " + data.toString() + " : " + error + " : " + ret);
	}

	static public void changeMemberPlugin(String type, Group group, List<User> users){
		//type can be only add or remove
		StringBuilder data = new StringBuilder();
		String[] program   = new String[2];
		StringBuffer reply = new StringBuffer();
		StringBuffer error = new StringBuffer();
		program[0] = cranixBaseDir + "plugins/plugin_handler.sh";
		program[1] = "change_member";
		data.append(String.format("changetype: %s%n",type));
		data.append(String.format("group: %s%n", group.getName()));
		List<String> uids = new ArrayList<String>();
		for( User user : users ) {
			uids.add(user.getUid());
		}
		data.append(String.format("users: %s%n", String.join(",",uids)));
		CrxSystemCmd.exec(program, reply, error, data.toString());
		logger.debug("change_member  : " + data.toString() + " : " + error);
	}

	static public void changeMemberPlugin(String type, Group group, User user){
		//type can be only add or remove
		StringBuilder data = new StringBuilder();
		String[] program   = new String[2];
		StringBuffer reply = new StringBuffer();
		StringBuffer error = new StringBuffer();
		program[0] = cranixBaseDir + "plugins/plugin_handler.sh";
		program[1] = "change_member";
		data.append(String.format("changetype: %s%n",type));
		data.append(String.format("group: %s%n", group.getName()));
		data.append(String.format("users: %s%n", user.getUid()));
		CrxSystemCmd.exec(program, reply, error, data.toString());
		logger.debug("change_member  : " + data.toString() + " : " + error);
	}

	static public String  createLiteralJson(Object object) {
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
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
		return fmt.format(new Date(times));
	}

	static public String convertJavaDate(Long times) {
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
		return fmt.format(new Date(times));
	}
}
