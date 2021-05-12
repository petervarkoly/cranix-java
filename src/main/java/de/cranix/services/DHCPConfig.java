/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.services;

import de.cranix.dao.*;
import de.cranix.helper.CrxSystemCmd;
import de.cranix.helper.IPv4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static de.cranix.helper.CranixConstants.cranixBaseDir;

@SuppressWarnings("unchecked")
public class DHCPConfig extends Service {
    private static final Logger LOG = LoggerFactory.getLogger(DHCPConfig.class);
    private final String LOCK          = "/run/crx-dhcpd.lock";
    private final String TMPCONF       = "/run/crx-dhcpd.conf";
    private final Path   DHCP_CONFIG   = Paths.get("/etc/dhcpd.conf");
    private final Path   SALT_GROUPS   = Paths.get("/etc/salt/master.d/groups.conf");
    private final Path   DHCP_TEMPLATE = Paths.get(cranixBaseDir + "templates/dhcpd.conf");
    private List<String> dhcpConfigFile;
    private List<String> saltGroupFile;
    final String domainName = "." + getConfigValue("DOMAIN");
    private Long wait = 2L;

    public DHCPConfig(Session session, EntityManager em) {
        super(session, em);
        try {
			wait = Long.valueOf(getProperty("de.cranix.services.DHCPConfig.wait"));
		} catch ( NumberFormatException e ) {

		}

        try {
            try {
                dhcpConfigFile = Files.readAllLines(DHCP_TEMPLATE);
            } catch (java.nio.file.NoSuchFileException e) {
                LOG.error(e.getMessage());
                dhcpConfigFile = new ArrayList<String>();
            }
            saltGroupFile = new ArrayList<String>();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void Restart() {
        if (this.getConfigValue("USE_DHCP").equals("yes")) {
            File lock = new File(LOCK);
            if (!lock.exists()) {
                try {
                    lock.createNewFile();
                    TimeUnit.SECONDS.sleep(wait);
                    this.systemctl("try-restart", "dhcpd");
                } catch (Exception e) {
					e.printStackTrace();
                } finally {
                    lock.delete();
                }
            }
        }
    }

    public CrxResponse Test() {
        Write(Paths.get(TMPCONF));
        String[] program = new String[4];
        StringBuffer reply = new StringBuffer();
        StringBuffer stderr = new StringBuffer();
        program[0] = "/usr/sbin/dhcpd";
        program[1] = "-t";
        program[2] = "-cf";
        program[3] = TMPCONF;
        int result = CrxSystemCmd.exec(program, reply, stderr, null);
        try {
            Files.deleteIfExists(Paths.get(TMPCONF));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (result == 0) {

            return new CrxResponse(session, "OK", "DHCPD configuration is ok");
        } else {
            return new CrxResponse(session, "ERROR", stderr.toString());
        }
    }

    public void Create() {
            Write(DHCP_CONFIG);
            Restart();
    }

    private void Write(Path path) {
        Query query = this.em.createNamedQuery("Room.findAllToRegister");
        saltGroupFile.add("nodegroups:");
        for (Room room : (List<Room>) query.getResultList()) {
            logger.debug("Write DHCP Room" + room.getName());
            if (room.getDevices().isEmpty()) {
                continue;
            }
            dhcpConfigFile.add("group {");
            dhcpConfigFile.add("  #Room" + room.getName());
            //TODO add dhcp options and statements from RoomConfig
            for (String dhcpstatement : this.getMConfigs(room, "dhcpStatements")) {
                dhcpConfigFile.add("    " + dhcpstatement + ";");
            }
            for (String dhcpOption : this.getMConfigs(room, "dhcpOptions")) {
                dhcpConfigFile.add("    option " + dhcpOption + ";");
            }
            WriteRoom(room);
            dhcpConfigFile.add("}");
        }
        try {
            Files.write(path, dhcpConfigFile);
            //Build groups by hwconf
            query = this.em.createNamedQuery("HWConf.findAll");
            for (HWConf hwconf : (List<HWConf>) query.getResultList()) {
                List<String> line = new ArrayList<String>();
                for (Device device : hwconf.getDevices()) {
                    line.add(device.getName() + domainName);
                }
                if (!line.isEmpty()) {
                    saltGroupFile.add("  hwconf-" + hwconf.getName() + ": 'L@" + String.join(",", line) + "'");
                }
            }
            Files.write(SALT_GROUPS, saltGroupFile);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
    }

    private void WriteRoom(Room room) {
        List<String> line = new ArrayList<String>();
        for (Device device : room.getDevices()) {
            //Do not create configuration for devices without mac adress.
            logger.debug("Write DHCP Device" + device.getName());
            if (device.getMac().isEmpty()) {
                continue;
            }
            //Dirty fix to avoid duplicate entries in dhcpd.conf
            if (line.contains(device.getName() + domainName)) {
                continue;
            }
            line.add(device.getName() + domainName);
            dhcpConfigFile.add("    host " + device.getName() + " {");
            dhcpConfigFile.add("      hardware ethernet " + device.getMac() + ";");
            dhcpConfigFile.add("      fixed-address " + device.getIp() + ";");
            //TODO add dhcp options and statements from DeviceConfif
            for (String dhcpstatement : this.getMConfigs(device, "dhcpStatements")) {
                dhcpConfigFile.add("      " + dhcpstatement + ";");
            }
            for (String dhcpOption : this.getMConfigs(device, "dhcpOptions")) {
                dhcpConfigFile.add("      option " + dhcpOption + ";");
            }
            dhcpConfigFile.add("    }");
            if (IPv4.validateIPAddress(device.getWlanIp())) {
                dhcpConfigFile.add("    host " + device.getName() + "-wlan {");
                dhcpConfigFile.add("      hardware ethernet " + device.getWlanMac() + ";");
                dhcpConfigFile.add("      fixed-address " + device.getWlanIp() + ";");
                for (String dhcpstatement : this.getMConfigs(room, "dhcpStatements")) {
                    dhcpConfigFile.add("    " + dhcpstatement + ";");
                }
                for (String dhcpOption : this.getMConfigs(device, "dhcpOptions")) {
                    dhcpConfigFile.add("      option " + dhcpOption + ";");
                }
                dhcpConfigFile.add("    }");
                line.add(device.getName() + "-wlan" + domainName);
            }
        }
        if (!line.isEmpty()) {
            saltGroupFile.add("  room-" + room.getName() + ": 'L@" + String.join(",", line) + "'");
        }
    }
}
