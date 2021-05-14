package de.cranix.services;

import de.cranix.dao.AccessInRoom;
import de.cranix.dao.Room;
import de.cranix.helper.CrxSystemCmd;
import org.ini4j.Ini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AccessService extends Config {

    Logger logger = LoggerFactory.getLogger(AdHocLanService.class);
    Ini sambaConf;
    HashMap<String,HashMap<String,String>> fwStatus = new HashMap<>();
    String proxy;
    String portal;

    public AccessService() {
        super();
        try {
            sambaConf = new Ini(new File("/etc/samba/smb.conf"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        proxy  = this.getConfigValue("PROXY");
        portal = this.getConfigValue("MAILSERVER");

        String nameString      = "^(\\S+)";
        String richRuleString  = "^\\s+rich rules:";
        String valueString     = "^\\s+(\\S+): (\\S+)";
        String addressString   = "address=\"([0-9\\.]+)\"";
        Pattern namePattern     = Pattern.compile(nameString);
        Pattern richRulePattern = Pattern.compile(richRuleString);
        Pattern valuePattern    = Pattern.compile(valueString);
        Pattern addressPattern  = Pattern.compile(addressString);
        String key = "";
        HashMap<String, String> map = null;
        Boolean rule = false;
        List<String> rules = new ArrayList<String>();
        String[] program = new String[2];
        program[0] = "/usr/bin/firewall-cmd";
        program[1] = "--list-all-zones";
        StringBuffer reply = new StringBuffer();
        StringBuffer error = new StringBuffer();
        CrxSystemCmd.exec(program, reply, error, null);
        for(String line: reply.toString().split("\n") ) {
            logger.debug(line);
            Matcher nameMatcher = namePattern.matcher(line);
            if( nameMatcher.find() ) {
                if(map != null) {
                    map.put("rule",String.join(" ",rules));
                    fwStatus.put(key,map);
                }
                key = nameMatcher.group(1);
                map = new HashMap<String, String>();
                rule  = false;
                rules = new ArrayList<String>();
                continue;
            }
            if( rule ) {
                Matcher addressMatcher = addressPattern.matcher(line);
                if( addressMatcher.find()) {
                    rules.add(addressMatcher.group(1));
                }
                continue;
            }
            Matcher richRuleMatcher = richRulePattern.matcher(line);
            if( richRuleMatcher.find()) {
                rule = true;
                continue;
            }
            Matcher valueMatcher = valuePattern.matcher(line);
            if( valueMatcher.find() ) {
                map.put(valueMatcher.group(1), valueMatcher.group(2));
            }
        }
        map.put("rule",String.join(" ",rules));
        fwStatus.put(key,map);
    }

    public AccessInRoom getAccessStatus(Room room) {
        AccessInRoom accessInRoom = new AccessInRoom();
        accessInRoom.setRoomId(room.getId());
        accessInRoom.setRoomName(room.getName());
        accessInRoom.setAccessType("FW");
        String network = room.getStartIP() + "/" + room.getNetMask();
        /* Read samba config */
        String loginDeny = sambaConf.fetch("global", "hosts deny");
        String printDeny = sambaConf.fetch("printers", "hosts deny");
        accessInRoom.setLogin(isAllowed(loginDeny, network));
        accessInRoom.setPrinting(isAllowed(printDeny, network));
        /* Read FW settings*/
        accessInRoom.setProxy(!fwStatus.get(room.getName()).get("rule").contains(proxy));
        accessInRoom.setPortal(!fwStatus.get(room.getName()).get("rule").contains(portal));
        accessInRoom.setDirect(fwStatus.get(room.getName()).get("masquerade").equals("yes"));
        return accessInRoom;
    }

    static boolean isAllowed(String denied, String network) {
        if (denied != null) {
            for (String net : denied.split("\\s+")) {
                if (net.equals(network)) {
                    return false;
                }
            }
        }
        return true;
    }

    /*
     * Sets the actual access status in a room
     * Only FW and samba settings
     */
    public void setAccessStatus(Room room, AccessInRoom access, Boolean allowedDirect) {
        String network = room.getStartIP() + "/" + room.getNetMask();
        access.setRoom(room);
        String loginDeny = sambaConf.fetch("global", "hosts deny");
        String printDeny = sambaConf.fetch("printers", "hosts deny");
        if (access.getLogin()) {
            allowSmb("global",network);
        } else {
            denySmb("global",network);
        }
        if (access.getPrinting()) {
            allowSmb("printers",network);
        } else {
            denySmb("printers",network);
        }
        try {
            sambaConf.store();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] program = new String[4];
        //TODO own IP
        program[0] = "/usr/bin/firewall-cmd";
        program[1] = "--zone=" + room.getName();
        StringBuffer reply = new StringBuffer();
        StringBuffer error = new StringBuffer();
        //Direct access
        if (allowedDirect) {
            if (access.getDirect())
                program[3] = "--add-masquerade";
            else
                program[3] = "--remove-masquerade";
            CrxSystemCmd.exec(program, reply, error, null);
        }
        // Portal Access
        program[3] = "portal";
        if (access.getPortal())
            program[1] = "1";
        else
            program[1] = "0";
        CrxSystemCmd.exec(program, reply, error, null);

        // Proxy Access
        program[3] = "proxy";
        if (access.getProxy())
            program[1] = "1";
        else
            program[1] = "0";
        CrxSystemCmd.exec(program, reply, error, null);
    }

    void denySmb(String section, String network) {
        String loginDeny = sambaConf.fetch(section, "hosts deny");
        if( isAllowed(loginDeny, network) ) {
            if(loginDeny == null ) {
                sambaConf.add(section, "hosts deny",network);
            } else {
                sambaConf.put( section, "hosts deny",loginDeny + " " + network);
            }
        }
    }

    void allowSmb(String section, String network) {
        String loginDeny = sambaConf.fetch(section, "hosts deny");
        if( !isAllowed(loginDeny, network) ) {
            List<String> lDenied = new ArrayList<>();
            String[] denied = loginDeny.split("\\s+");
            for (String d : denied) {
                if (!d.equals(network)) {
                    lDenied.add(d);
                }
            }
            if( lDenied.size() == 0 ) {
                sambaConf.remove(section, "hosts deny");
            } else {
                sambaConf.put(section, "hosts deny",String.join(" ",lDenied));
            }
        }
    }
}
