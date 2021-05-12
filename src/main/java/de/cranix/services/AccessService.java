package de.cranix.services;

import de.cranix.dao.AccessInRoom;
import de.cranix.dao.Room;
import de.cranix.helper.CrxSystemCmd;
import org.ini4j.Ini;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AccessService {

    Ini sambaConf;
    String fwStatus;

    public AccessService() {
        try {
            sambaConf = new Ini(new File("/etc/samba/smb.conf"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] program = new String[4];
        program[0] = "/usr/sbin/iptables";
        program[1] = "-L";
        program[2] = "-n";
        program[3] = "-v";
        StringBuffer reply = new StringBuffer();
        StringBuffer error = new StringBuffer();
        CrxSystemCmd.exec(program, reply, error, null);
        fwStatus = reply.toString();
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
        accessInRoom.setProxy(!fwStatus.contains("proxy-" + network));
        accessInRoom.setPortal(!fwStatus.contains("portal-" + network));
        accessInRoom.setDirect(isDirect(network));
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

    boolean isDirect(String network) {
        Pattern pattern = Pattern.compile("MASQUERADE.*all.*" + network);
        Matcher matcher = pattern.matcher(fwStatus);
        return matcher.find();
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
        program[0] = "/usr/sbin/crx_set_access_state.sh";
        program[2] = network;
        StringBuffer reply = new StringBuffer();
        StringBuffer error = new StringBuffer();
        //Direct access
        if (allowedDirect) {
            program[3] = "direct";
            if (access.getDirect())
                program[1] = "1";
            else
                program[1] = "0";
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
