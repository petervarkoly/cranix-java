/* (c) Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.cranix.dao.*;
import de.cranix.helper.CrxSystemCmd;
import org.apache.http.client.fluent.Request;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.ElementFilter;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.ws.rs.WebApplicationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.cranix.helper.CranixConstants.cranixBaseDir;
import static de.cranix.helper.StaticHelpers.reloadFirewall;

@SuppressWarnings("unchecked")
public class SystemService extends Service {

    Logger logger = LoggerFactory.getLogger(SystemService.class);

    public SystemService(Session session, EntityManager em) {
        super(session, em);
    }

    static Pattern dnsRecordName = Pattern.compile("Name=(.+?), Records");
    static Pattern dnsRecordType = Pattern.compile("(.+?): (.+?) \\(flags");
    static Pattern portForward = Pattern.compile("port=(\\d+):proto=tcp:toport=(\\d+):toaddr=(.*)$");
    static Pattern servicePattern = Pattern.compile("services: (.*)");
    static Pattern destinationAddress = Pattern.compile("destination address=\"([0-9\\./]+)\"");
    static Pattern sourceAddress = Pattern.compile("source address=\"([0-9\\./]+)\"");
    static Pattern protocolPattern = Pattern.compile("protocol value=(\\S+)");

    /**
     * Delivers a list of the status of the system
     *
     * @return Hash of status lists:
     * [
     * {
     * "name"			: "groups"
     * "primary"		: 5,
     * "class"			: 40,
     * "workgroups"	: 122
     * },
     * {
     * "name"			: "users",
     * "students"		: 590,
     * "students-loggedOn"	205,
     * ...
     * }
     * ....
     * ]
     */
    public Map<String, List<Map<String, String>>> getStatus() {
        //Initialize of some variable
        Map<String, List<Map<String, String>>> systemStatus = new HashMap<>();
        List<Map<String, String>> statusMapList;
        Map<String, String> statusMap;
        Query query;
        Integer count;

        //TODO System Load, HD, License, ....

        //Groups;
        statusMapList = new ArrayList<Map<String, String>>();
        for (String groupType : this.getEnumerates("groupType")) {
            query = this.em.createNamedQuery("Group.getByType").setParameter("groupType", groupType);
            count = query.getResultList().size();
            statusMap = new HashMap<>();
            statusMap.put("name", groupType);
            statusMap.put("count", count.toString());
            statusMapList.add(statusMap);
        }
        systemStatus.put("groups", statusMapList);

        //Users
        statusMapList = new ArrayList<Map<String, String>>();
        for (String role : this.getEnumerates("role")) {
            query = this.em.createNamedQuery("User.getByRole").setParameter("role", role);
            count = query.getResultList().size();
            Integer loggedOn = 0;
            for (User u : (List<User>) query.getResultList()) {
                loggedOn += u.getLoggedOn().size();
            }
            statusMap = new HashMap<>();
            statusMap.put("name", role);
            statusMap.put("count", count.toString());
            statusMap.put("loggedOn", loggedOn.toString());
            statusMapList.add(statusMap);
        }
        systemStatus.put("users", statusMapList);

        //Rooms
        statusMapList = new ArrayList<Map<String, String>>();
        for (String roomType : this.getEnumerates("roomType")) {
            query = this.em.createNamedQuery("Room.getByType").setParameter("type", roomType);
            count = query.getResultList().size();
            statusMap = new HashMap<>();
            statusMap.put("name", roomType);
            statusMap.put("count", count.toString());
            statusMapList.add(statusMap);
        }
        query = this.em.createNamedQuery("Room.getByType").setParameter("type", "adHocAccess");
        count = query.getResultList().size();
        statusMap = new HashMap<>();
        statusMap.put("name", "adHocAccess");
        statusMap.put("count", count.toString());
        statusMapList.add(statusMap);
        systemStatus.put("rooms", statusMapList);

        Integer deviceCount = new DeviceService(this.session, this.em).getAll().size();
        statusMapList = new ArrayList<Map<String, String>>();
        CloneToolService ctc = new CloneToolService(this.session, this.em);
        for (HWConf hwconf : ctc.getAllHWConf()) {
            count = hwconf.getDevices().size();
            deviceCount -= count;
            statusMap = new HashMap<>();
            statusMap.put("name", hwconf.getName());
            statusMap.put("count", count.toString());
            statusMapList.add(statusMap);
        }
        statusMap = new HashMap<>();
        statusMap.put("name", "non_typed");
        statusMap.put("count", deviceCount.toString());
        statusMapList.add(statusMap);
        systemStatus.put("devices", statusMapList);
        //Software
        SoftwareService softwareService = new SoftwareService(this.session, this.em);
        systemStatus.put("softwares", softwareService.statistic());

        //Disk usage
        StringBuilder data = new StringBuilder();
        String[] program = new String[1];
        StringBuffer reply = new StringBuffer();
        StringBuffer error = new StringBuffer();
        program[0] = "/usr/bin/df";
        int ret = CrxSystemCmd.exec(program, reply, error, "");
        for (String line : reply.toString().split("\\n")) {
            String[] fields = line.split("\\s+");
            if (fields[0].startsWith("/dev/")) {
                statusMapList = new ArrayList<Map<String, String>>();
                statusMap = new HashMap<>();
                statusMap.put("name", "used");
                statusMap.put("count", fields[2]);
                statusMapList.add(statusMap);
                statusMap = new HashMap<>();
                statusMap.put("name", "free");
                statusMap.put("count", fields[3]);
                statusMapList.add(statusMap);
                systemStatus.put(fields[0], statusMapList);
                this.session.setMac(fields[2]);
            }
        }
        return systemStatus;
    }

    /**
     * Add a new enumerate
     *
     * @param name  Name of the enumerates: roomType, groupType, deviceType ...
     * @param value The new value
     */
    public CrxResponse addEnumerate(String name, String value) {
        Query query = this.em.createNamedQuery("Enumerate.get").setParameter("name", name).setParameter("value", value);
        if (!query.getResultList().isEmpty()) {
            return new CrxResponse(this.getSession(), "ERROR", "Entry alread does exists");
        }
        Enumerate en = new Enumerate(name, value, this.session.getUser());
        try {
            this.em.getTransaction().begin();
            this.em.persist(en);
            this.em.getTransaction().commit();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new CrxResponse(this.getSession(), "ERROR", e.getMessage());
        }
        return new CrxResponse(this.getSession(), "OK", "Enumerate was created succesfully.");
    }

    /**
     * Deletes an existing enumerate
     *
     * @param name  Name of the enumerates: roomType, groupType, deviceType ...
     * @param value The new value
     */
    public CrxResponse deleteEnumerate(String name, String value) {
        Query query = this.em.createNamedQuery("Enumerate.getByName").setParameter("name", name).setParameter("value", value);
        try {
            Enumerate en = (Enumerate) query.getSingleResult();
            this.em.getTransaction().begin();
            this.em.remove(en);
            this.em.getTransaction().commit();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new CrxResponse(this.getSession(), "ERROR", e.getMessage());
        }
        return new CrxResponse(this.getSession(), "OK", "Enumerate was removed successfully.");
    }

    ////////////////////////////////////////////////////////
    // Functions for setting firewall
    ///////////////////////////////////////////////////////

    public Map<String, String> getFirewallIncomingRules() {
        String[] program = new String[4];
        StringBuffer reply = new StringBuffer();
        StringBuffer error = new StringBuffer();
        program[0] = "/usr/bin/firewall-cmd";
        program[1] = "--zone=external";
        program[2] = "--list-services";
        program[3] = "--permanent";
        Map<String, String> statusMap;
        statusMap = new HashMap<>();
        CrxSystemCmd.exec(program, reply, error, null);
        for (String service : reply.toString().split("\\s")) {
            if( !service.isBlank() ) {
                statusMap.put(service, "true");
            }
        }
        program[0] = "/usr/bin/firewall-cmd";
        program[1] = "--zone=external";
        program[2] = "--list-ports";
        program[3] = "--permanent";
        CrxSystemCmd.exec(program, reply, error, null);
        List<String> ports = new ArrayList<String>();
        for (String service : reply.toString().split("\\s")) {
            if( !service.isBlank() ) {
                ports.add(service);
            }
        }
        statusMap.put("other", String.join(" ",ports));
        return statusMap;
    }

    public CrxResponse setFirewallIncomingRules(Map<String, String> firewallExt) {
        String[] program = new String[1];
        program[0] = cranixBaseDir + "tools/set_fw_incomming.py";
        StringBuffer reply = new StringBuffer();
        StringBuffer error = new StringBuffer();
        CrxSystemCmd.exec(program, reply, error, firewallExt.toString());
        return new CrxResponse(this.getSession(), "OK", "Firewall incoming access rules were set successfully.");
    }

    public List<Map<String, String>> getFirewallOutgoingRules() {
        Matcher matcher;
        RoomService roomService = new RoomService(this.session, this.em);
        DeviceService deviceService = new DeviceService(this.session, this.em);
        List<Map<String, String>> firewallList = new ArrayList<>();
        Map<String, String> statusMap;
        String[] program = new String[3];
        StringBuffer reply = new StringBuffer();
        StringBuffer error = new StringBuffer();
        program[0] = "/usr/bin/firewall-cmd";
        program[1] = "--zone=external";
        program[2] = "--list-rich-rules";
        CrxSystemCmd.exec(program, reply, error, null);
        for( String line: reply.toString().split("\\n")) {
            statusMap = new HashMap<>();
            //Evaluate source
            matcher = sourceAddress.matcher(line);
            if( !matcher.find()) {
                logger.error("getFirewallOutgoingRules bad rule:" + line);
                continue;
            }
            String[] host = matcher.group(1).split("/");
            if (host.length == 1 || host[1].equals("32")) {
                Device device = deviceService.getByMainIP(host[0]);
                if (device == null) {
                    logger.debug("not found");
                    continue;
                }
                statusMap.put("id", Long.toString(device.getId()));
                statusMap.put("name", device.getName());
                statusMap.put("type", "host");
            } else {
                Room room = roomService.getByIP(host[0]);
                if (room == null || !room.getRoomControl().equals("no")) {
                    continue;
                }
                statusMap.put("id", Long.toString(room.getId()));
                statusMap.put("name", room.getName());
                statusMap.put("type", "room");
            }
            // Evaluate destination
            matcher = destinationAddress.matcher(line);
            if( matcher.find() ) {
                statusMap.put("dest",matcher.group(1));
            } else {
                statusMap.put("dest","0/0");
            }
            // Evaluate protocol
            matcher = protocolPattern.matcher(line);
            if( matcher.find() ){
                statusMap.put("prot",matcher.group(1));
            } else {
                statusMap.put("prot","all");
            }
            firewallList.add(statusMap);
        }
        return firewallList;
    }

    public CrxResponse addFirewallOutgoingRule(Map<String, String> firewalRule) {
        try {
            logger.debug(new ObjectMapper().writeValueAsString(firewalRule));
            String[] program = new String[4];
            StringBuffer reply = new StringBuffer();
            StringBuffer error = new StringBuffer();
            program[0] = "/usr/bin/firewall-cmd";
            program[1] = "--permanent";
            program[2] = "--zone=external";
            String source = "";
            Long id = Long.getLong(firewalRule.get("id"));
            if (firewalRule.get("type").equals("room")) {
                Room room = new RoomService(this.session, this.em).getById(id);
                source = String.format("%s/%s",room.getStartIP(),room.getNetMask());
            } else {
                Device device = new DeviceService(this.session, this.em).getById(id);
                source = String.format("%s/32",device.getIp());
            }
            StringBuilder richRule = new StringBuilder();
            richRule.append("--add-rich-rule=\"rule family=ipv4 source address=");
            richRule.append(source);
            if( !firewalRule.get("prot").equals("all")) {
                richRule.append(" protocol value=");
                richRule.append(firewalRule.get("prot"));
            }
            if( firewalRule.get("dest").equals("0/0")) {
                program[3] = String.format("--add-rich-rule=\"rule family=ipv4 source address=%s masquerade\"",source);
            } else {
                program[3] = String.format(
                        "--add-rich-rule=\"rule family=ipv4 source address=%s destination address=%s masquerade\"",
                        source, firewalRule.get("dest")
                );
            }
            CrxSystemCmd.exec(program, reply, error, null);
            logger.debug("addFirewallOutgoingRule error:", error.toString());
            reloadFirewall();
            return new CrxResponse(this.getSession(), "OK", "Firewall outgoing access rule was add successfully.");
        } catch (Exception e) {
            logger.debug("{ \"ERROR\" : \"CAN NOT MAP THE OBJECT\" }");
            return new CrxResponse(this.getSession(), "ERROR", e.getMessage());
        }
    }

    public CrxResponse deleteFirewallOutgoingRule(Map<String, String> firewalRule) {
        try {
            logger.debug(new ObjectMapper().writeValueAsString(firewalRule));
            String[] program = new String[4];
            StringBuffer reply = new StringBuffer();
            StringBuffer error = new StringBuffer();
            program[0] = "/usr/bin/firewall-cmd";
            program[1] = "--permanent";
            program[2] = "--zone=external";
            String source = "";
            Long id = Long.getLong(firewalRule.get("id"));
            if (firewalRule.get("type").equals("room")) {
                Room room = new RoomService(this.session, this.em).getById(id);
                source = String.format("%s/%s",room.getStartIP(),room.getNetMask());
            } else {
                Device device = new DeviceService(this.session, this.em).getById(id);
                source = String.format("%s/32",device.getIp());
            }
            StringBuilder richRule = new StringBuilder();
            richRule.append("--add-rich-rule=\"rule family=ipv4 source address=");
            richRule.append(source);
            if( !firewalRule.get("prot").equals("all")) {
                richRule.append(" protocol value=");
                richRule.append(firewalRule.get("prot"));
            }
            if( firewalRule.get("dest").equals("0/0")) {
                program[3] = String.format("--remove-rich-rule=\"rule family=ipv4 source address=%s masquerade\"",source);
            } else {
                program[3] = String.format(
                        "--remove-rich-rule=\"rule family=ipv4 source address=%s destination address=%s masquerade\"",
                        source, firewalRule.get("dest")
                );
            }
            CrxSystemCmd.exec(program, reply, error, null);
            logger.debug("deleteFirewallOutgoingRule error:", error.toString());
            reloadFirewall();
            return new CrxResponse(this.getSession(), "OK", "Firewall outgoing access rule was deleted successfully.");
        } catch (Exception e) {
            logger.debug("{ \"ERROR\" : \"CAN NOT MAP THE OBJECT\" }");
            return new CrxResponse(this.getSession(), "ERROR", e.getMessage());
        }
    }

    public List<Map<String, String>> getFirewallRemoteAccessRules() {
        String[] program = new String[3];
        StringBuffer reply = new StringBuffer();
        StringBuffer error = new StringBuffer();
        program[0] = "/usr/bin/firewall-cmd";
        program[1] = "--zone=external";
        program[2] = "--list-forward-ports";
        CrxSystemCmd.exec(program, reply, error, null);
        List<Map<String, String>> firewallList = new ArrayList<>();
        Map<String, String> statusMap;
        DeviceService deviceService = new DeviceService(this.session, this.em);
        for( String line: reply.toString().split("\\n")) {
            Matcher matcher = portForward.matcher(line);
            if (matcher.find()) {
                Device device = deviceService.getByIP(matcher.group(3));
                if (device != null) {
                    statusMap = new HashMap<String, String>();
                    statusMap.put("extport", matcher.group(1));
                    statusMap.put("port", matcher.group(2));
                    statusMap.put("name", device.getName());
                    statusMap.put("id", device.getId().toString());
                    firewallList.add(statusMap);
                } else {
                    logger.error("getFirewallRemoteAccessRules can not find host:"+ matcher.group(3));
                }
            } else {
                logger.error("getFirewallRemoteAccessRules bad rule: "+ line);
            }
        }
        return firewallList;
    }

    public CrxResponse addFirewallRemoteAccessRule(Map<String, String> remoteRule) {
        String[] program = new String[4];
        StringBuffer reply = new StringBuffer();
        StringBuffer error = new StringBuffer();
        program[0] = "/usr/bin/firewall-cmd";
        program[1] = "--permanent";
        program[2] = "--zone=external";
        Device device = new DeviceService(this.session, this.em).getById(
                Long.getLong(remoteRule.get("id"))
        );
        if (device == null) {
            return new CrxResponse(this.getSession(), "ERROR", "Firewall remote access rule could not set.");
        }
        program[3] = String.format("--add-forward-port=port=%s:proto=tcp:toport=%s:toaddr=%s",
                remoteRule.get("extport"), remoteRule.get("port"), device.getIp()
        );
        CrxSystemCmd.exec(program, reply, error, null);
        logger.debug("addFirewallRemoteAccessRule error:", error.toString());
        reloadFirewall();
        return new CrxResponse(this.getSession(), "OK", "Firewall remote access rule was add successfully.");
    }

    public CrxResponse deleteFirewallRemoteAccessRule(Map<String, String> remoteRule) {
        String[] program = new String[4];
        StringBuffer reply = new StringBuffer();
        StringBuffer error = new StringBuffer();
        program[0] = "/usr/bin/firewall-cmd";
        program[1] = "--permanent";
        program[2] = "--zone=external";
        Device device = new DeviceService(this.session, this.em).getById(
                Long.getLong(remoteRule.get("id"))
        );
        if (device == null) {
            return new CrxResponse(this.getSession(), "ERROR", "Firewall remote access rule could not set.");
        }
        program[3] = String.format("--remove-forward-port=port=%s:proto=tcp:toport=%s:toaddr=%s",
                remoteRule.get("extport"), remoteRule.get("port"), device.getIp()
        );
        CrxSystemCmd.exec(program, reply, error, null);
        logger.debug("deleteFirewallRemoteAccessRule error:", error.toString());
        reloadFirewall();
        return new CrxResponse(this.getSession(), "OK", "Firewall remote access rule was add successfully.");
    }

    /*
     * Functions for package management of the system
     */

    public Date getValidityOfRegcode() {
        StringBuilder url = new StringBuilder();
        url.append(this.getConfigValue("UPDATE_URL")).append("/api/customers/regcodes/").append(this.getConfigValue("REG_CODE"));
        try {
            String response = Request.Get(url.toString()).execute().returnContent().asString();
            Long milis = Long.parseLong(response);
            return new Date(milis);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new WebApplicationException(500);
        }
    }

    public boolean validateRegcode() {
        Date valid = this.getValidityOfRegcode();
        if (valid == null) {
            return false;
        }
        return valid.after(this.now());
    }

    public CrxResponse registerSystem() {
        if (!this.validateRegcode()) {
            return new CrxResponse(this.getSession(), "ERROR", "Registration Code is invalid.");
        }
        String[] program = new String[1];
        StringBuffer reply = new StringBuffer();
        StringBuffer error = new StringBuffer();
        program[0] = cranixBaseDir + "tools/register.sh";
        CrxSystemCmd.exec(program, reply, error, null);
        if (error.toString().isEmpty()) {
            return new CrxResponse(this.getSession(), "OK", "System was registered succesfully.");
        } else {
            return new CrxResponse(this.getSession(), "ERROR", error.toString());
        }
    }

    public List<Map<String, String>> searchPackages(String filter) {
        List<Map<String, String>> packages = new ArrayList<Map<String, String>>();
        Map<String, String> software = null;
        String[] program = new String[3];
        StringBuffer reply = new StringBuffer();
        StringBuffer error = new StringBuffer();
        program[0] = "zypper";
        program[1] = "-x";
        program[2] = filter;
        CrxSystemCmd.exec(program, reply, error, null);
        try {
            Document doc = new SAXBuilder().build(reply.toString());
            Element rootNode = doc.getRootElement();
            for (Element node : rootNode.getChild("search-result").getChild("solvable-list").getChildren("solvable")) {
                if (!node.getAttribute("kind").getValue().equals("package")) {
                    continue;
                }
                software = new HashMap<String, String>();
                software.put("name", node.getAttributeValue("name"));
                software.put("summary", node.getAttributeValue("summary"));
                software.put("status", node.getAttributeValue("status"));
                packages.add(software);
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new WebApplicationException(500);
        } catch (JDOMException e) {
            logger.error(e.getMessage());
            throw new WebApplicationException(500);
        }
        return packages;
    }

    public CrxResponse installPackages(List<String> packages) {
        String[] program = new String[3 + packages.size()];
        StringBuffer reply = new StringBuffer();
        StringBuffer error = new StringBuffer();
        program[0] = "zypper";
        program[1] = "-n";
        program[2] = "install";
        int i = 3;
        for (String prog : packages) {
            program[i] = prog;
            i++;
        }
        if (CrxSystemCmd.exec(program, reply, error, null) == 0) {
            return new CrxResponse(this.getSession(), "OK", "Packages were installed succesfully.");
        } else {
            return new CrxResponse(this.getSession(), "ERROR", error.toString());
        }
    }

    /**
     * List of the available updates
     *
     * @return The list of packages can be updated
     */
    public List<Map<String, String>> listUpdates() {
        Map<String, String> update;
        List<Map<String, String>> updates = new ArrayList<Map<String, String>>();
        String[] program = new String[3];
        StringBuffer reply = new StringBuffer();
        StringBuffer stderr = new StringBuffer();
        program[0] = "/usr/bin/zypper";
        program[1] = "-nx";
        program[2] = "lu";
        program[3] = "-r";
        if (this.getConfigValue("TYPE").equals("cephalix")) {
            program[4] = "CEPHALIX";
        } else {
            program[4] = "OSS";
        }
        CrxSystemCmd.exec(program, reply, stderr, null);
        try {
            Document doc = new SAXBuilder().build(new StringReader(reply.toString()));
            logger.debug(reply.toString());
            Element rootNode = doc.getRootElement();
            Iterator<Element> processDescendants = rootNode.getDescendants(new ElementFilter("update"));
            while (processDescendants.hasNext()) {
                Element node = processDescendants.next();
                update = new HashMap<String, String>();
                update.put("name", node.getAttributeValue("name").substring(8));
                /*software.put("description", node.getAttributeValue("kind"));*/
                update.put("version-old", node.getAttributeValue("edition-old"));
                update.put("version", node.getAttributeValue("edition"));
                updates.add(update);
            }
        } catch (IOException e) {
            logger.error("1 " + reply.toString());
            logger.error("1 " + stderr.toString());
            logger.error("1 " + e.getMessage());
            throw new WebApplicationException(500);
        } catch (JDOMException e) {
            logger.error("2 " + reply.toString());
            logger.error("2 " + stderr.toString());
            logger.error("2 " + e.getMessage());
            throw new WebApplicationException(500);
        }
        return updates;
    }

    /**
     * Update selected packages
     *
     * @param packages The list of packages to update
     * @return The result of Update as CrxResponse object
     */
    public CrxResponse updatePackages(List<String> packages) {
        String[] program = new String[1 + packages.size()];
        StringBuffer reply = new StringBuffer();
        StringBuffer error = new StringBuffer();
        program[0] = "/usr/sbin/crx_update.sh";
        int i = 3;
        for (String prog : packages) {
            program[i] = prog;
            i++;
        }
        if (CrxSystemCmd.exec(program, reply, error, null) == 0) {
            return new CrxResponse(this.getSession(), "OK", "Packages were updated succesfully.");
        } else {
            return new CrxResponse(this.getSession(), "ERROR", error.toString());
        }
    }

    /**
     * Update all packages
     *
     * @return The result of Update as CrxResponse object
     */
    public CrxResponse updateSystem() {
        String[] program = new String[1];
        StringBuffer reply = new StringBuffer();
        StringBuffer error = new StringBuffer();
        program[0] = "/usr/sbin/crx_update.sh";
        if (CrxSystemCmd.exec(program, reply, error, null) == 0) {
            return new CrxResponse(this.getSession(), "OK", "System was updated succesfully.");
        } else {
            return new CrxResponse(this.getSession(), "ERROR", error.toString());
        }
    }

    /**
     * Returns an ACL searched by the technical id.
     *
     * @param aclId
     * @return The found acl.
     */
    public Acl getAclById(Long aclId) {
        try {
            return this.em.find(Acl.class, aclId);
        } catch (Exception e) {
            return null;
        }
    }

    public List<Acl> getAvailableAcls() {
        List<Acl> acls = new ArrayList<Acl>();
        for (String aclName : this.getEnumerates("apiAcl")) {
            acls.add(new Acl(aclName, false));
        }
        return acls;
    }

    public List<Acl> getAclsOfGroup(Long groupId) {
        return new GroupService(this.session, this.em).getById(groupId).getAcls();
    }

    public List<Acl> getAvailableAclsForGroup(Long groupId) {
        List<Acl> acls = new ArrayList<Acl>();
        List<Acl> ownAcls = this.getAclsOfGroup(groupId);
        for (String aclName : this.getEnumerates("apiAcl")) {
            boolean have = false;
            for (Acl ownAcl : ownAcls) {
                if (ownAcl.getAcl().equals(aclName)) {
                    have = true;
                    break;
                }
            }
            if (!have) {
                acls.add(new Acl(aclName, false));
            }
        }
        return acls;
    }


    public CrxResponse setAclToGroup(Long groupId, Acl acl) {
        Group group = new GroupService(this.session, this.em).getById(groupId);
        Acl oldAcl;
        logger.debug("Group acl to set: " + acl);
        try {
            oldAcl = this.em.find(Acl.class, acl.getId());
        } catch (Exception e) {
            oldAcl = null;
        }
        if (oldAcl == null) {
            for (Acl gacl : group.getAcls()) {
                if (gacl.getAcl().equals(acl.getAcl())) {
                    oldAcl = gacl;
                    break;
                }
            }
        }
        try {
            this.em.getTransaction().begin();
            if (oldAcl != null) {
                if (acl.getAllowed()) {
                    oldAcl.setAllowed(true);
                    this.em.merge(oldAcl);
                } else {
                    group.getAcls().remove(oldAcl);
                    this.em.merge(group);
                    this.em.remove(oldAcl);
                }
            } else {
                acl.setGroup(group);
                acl.setCreator(this.session.getUser());
                this.em.persist(acl);
                group.addAcl(acl);
                this.em.merge(group);
            }
            this.em.getTransaction().commit();
        } catch (Exception e) {
            logger.debug("ERROR in setAclToGroup:" + e.getMessage());
            return new CrxResponse(session, "ERROR", e.getMessage());
        }
        return new CrxResponse(session, "OK", "ACL was set succesfully.");
    }

    public List<Acl> getAclsOfUser(Long userId) {
        User user = new UserService(this.session, this.em).getById(userId);
        List<Acl> acls = user.getAcls();
        List<String> aclNames = new ArrayList<String>();
        for (Acl acl : user.getAcls()) {
            aclNames.add(acl.getAcl());
        }
        for (Group group : user.getGroups()) {
            for (Acl acl : group.getAcls()) {
                if (!aclNames.contains(acl.getAcl())) {
                    acls.add(acl);
                }
            }
        }
        acls.sort(Comparator.comparing(Acl::getAcl));
        return acls;
    }

    public boolean hasUsersGroupAcl(User user, Acl acl) {
        for (Group group : user.getGroups()) {
            for (Acl groupAcl : group.getAcls()) {
                if (acl.getAcl().equals(groupAcl.getAcl()) &&
                        (acl.getAllowed() == groupAcl.getAllowed())) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<Acl> getAvailableAclsForUser(Long userId) {
        List<Acl> acls = new ArrayList<Acl>();
        List<Acl> ownAcls = this.getAclsOfUser(userId);
        for (String aclName : this.getEnumerates("apiAcl")) {
            boolean have = false;
            for (Acl ownAcl : ownAcls) {
                if (ownAcl.getAcl().equals(aclName)) {
                    have = true;
                    break;
                }
            }
            if (!have) {
                acls.add(new Acl(aclName, false));
            }
        }
        acls.sort(Comparator.comparing(Acl::getAcl));
        return acls;
    }

    public CrxResponse setAclToUser(Long userId, Acl acl) {
        User user = new UserService(this.session, this.em).getById(userId);
        Acl oldAcl = null;
        logger.debug("User acl to set: " + acl);
        try {
            oldAcl = this.em.find(Acl.class, acl.getId());
        } catch (Exception e) {
            oldAcl = null;
        }
        try {
            this.em.getTransaction().begin();
            if (oldAcl != null && oldAcl.getUser() != null && oldAcl.getUser().equals(user)) {
                logger.debug("User old to modify: " + oldAcl);
                if (!this.hasUsersGroupAcl(user, acl)) {
                    user.getAcls().remove(oldAcl);
                    this.em.remove(oldAcl);
                    this.em.merge(user);
                } else {
                    oldAcl.setAllowed(acl.getAllowed());
                    this.em.merge(oldAcl);
                }
            } else {
                logger.debug("This is a new acl.");
                acl.setGroup(null);
                acl.setUser(user);
                acl.setCreator(this.session.getUser());
                this.em.persist(acl);
                user.addAcl(acl);
                this.em.merge(user);
            }
            this.em.getTransaction().commit();
        } catch (Exception e) {
            logger.debug("ERROR in setAclToUser:" + e.getMessage());
            return new CrxResponse(session, "ERROR", e.getMessage());
        }
        return new CrxResponse(session, "OK", "ACL was set succesfully.");
    }

    public String[] getDnsDomains() {
        String[] program = new String[1];
        StringBuffer reply = new StringBuffer();
        StringBuffer error = new StringBuffer();
        program[0] = "/usr/sbin/crx_get_dns_domains.sh";
        CrxSystemCmd.exec(program, reply, error, null);
        return reply.toString().split("\\n");
    }

    public CrxResponse addDnsDomain(String domainName) {
        String[] program = new String[7];
        StringBuffer reply = new StringBuffer();
        StringBuffer error = new StringBuffer();
        program[0] = "/usr/bin/samba-tool";
        program[1] = "dns";
        program[2] = "zonecreate";
        program[3] = "localhost";
        program[4] = domainName;
        program[5] = "-U";
        program[6] = "register%" + this.getProperty("de.cranix.dao.User.Register.Password");
        CrxSystemCmd.exec(program, reply, error, null);
        //TODO evaluate error
        return new CrxResponse(session, "OK", "DNS Zone was created succesfully.");
    }

    public List<DnsRecord> getRecords(String domainName) {
        List<DnsRecord> dnsRecords = new ArrayList<DnsRecord>();
        String[] program = new String[2];
        StringBuffer reply = new StringBuffer();
        StringBuffer error = new StringBuffer();
        program[0] = "/usr/sbin/crx_dump_dns_domain.sh";
        program[1] = domainName;
        CrxSystemCmd.exec(program, reply, error, null);

        String name = null;
        String type = null;
        String data = null;
        DeviceService dc = new DeviceService(this.session, this.em);
        for (String line : reply.toString().split(this.getNl())) {
            Matcher matcher = dnsRecordName.matcher(line);
            while (matcher.find()) {
                name = matcher.group(1);
                continue;
            }
            matcher = dnsRecordType.matcher(line);
            while (matcher.find()) {
                if (name == null) {
                    continue;
                }
                type = matcher.group(1);
                data = matcher.group(2);
                Device device = dc.getByName(name);
                if (device != null && device.getIp().equals(data)) {
                    continue;
                }
                DnsRecord dnsRecord = new DnsRecord(domainName, type, name, data);
                dnsRecords.add(dnsRecord);
            }
        }
        return dnsRecords;
    }

    public CrxResponse addDnsRecord(DnsRecord dnsRecord) {
        String[] program = new String[10];
        StringBuffer reply = new StringBuffer();
        StringBuffer error = new StringBuffer();
        program[0] = "/usr/bin/samba-tool";
        program[1] = "dns";
        program[2] = "add";
        program[3] = "localhost";
        program[4] = dnsRecord.getDomainName();
        program[5] = dnsRecord.getRecordName();
        program[6] = dnsRecord.getRecordType();
        program[7] = dnsRecord.getRecordData();
        program[8] = "-U";
        program[9] = "register%" + this.getProperty("de.cranix.dao.User.Register.Password");
        CrxSystemCmd.exec(program, reply, error, null);
        //TODO evaluate error
        logger.debug("addDnsRecord reply" + reply.toString());
        logger.debug("addDnsRecord error" + error.toString());
        if (error.toString().isEmpty()) {
            return new CrxResponse(session, "OK", "DNS record was created succesfully.");
        } else {
            return new CrxResponse(session, "ERROR", error.toString());
        }
    }

    public CrxResponse deleteDnsRecord(DnsRecord dnsRecord) {
        String[] program = new String[10];
        StringBuffer reply = new StringBuffer();
        StringBuffer error = new StringBuffer();
        program[0] = "/usr/bin/samba-tool";
        program[1] = "dns";
        program[2] = "delete";
        program[3] = "localhost";
        program[4] = dnsRecord.getDomainName();
        program[5] = dnsRecord.getRecordName();
        program[6] = dnsRecord.getRecordType();
        program[7] = dnsRecord.getRecordData();
        program[8] = "-U";
        program[9] = "register%" + this.getProperty("de.cranix.dao.User.Register.Password");
        CrxSystemCmd.exec(program, reply, error, null);
        logger.debug("deleteDnsRecord reply" + reply.toString());
        logger.debug("deleteDnsRecord error" + error.toString());
        if (error.toString().isEmpty()) {
            return new CrxResponse(session, "OK", "DNS record was created succesfully.");
        } else {
            logger.error(
                    dnsRecord.getDomainName() + "#" +
                            dnsRecord.getRecordName() + "#" +
                            dnsRecord.getRecordType() + "#" +
                            dnsRecord.getRecordData() + "# " + error.toString());
            return new CrxResponse(session, "ERROR", error.toString());
        }
    }

    public CrxResponse deleteDnsDomain(String domainName) {
        String[] program = new String[7];
        StringBuffer reply = new StringBuffer();
        StringBuffer error = new StringBuffer();
        program[0] = "/usr/bin/samba-tool";
        program[1] = "dns";
        program[2] = "zonedelete";
        program[3] = "localhost";
        program[4] = domainName;
        program[5] = "-U";
        program[6] = "register%" + this.getProperty("de.cranix.dao.User.Register.Password");
        CrxSystemCmd.exec(program, reply, error, null);
        //TODO evaluate error
        return new CrxResponse(session, "OK", "DNS Zone was created succesfully.");
    }

    public CrxResponse findObject(String objectType, LinkedHashMap<String, Object> object) {
        Long objectId = null;
        String name = null;
        //TODO Implement all searches
        switch (objectType.toLowerCase()) {
            case "acl":

                break;
            case "accessinroom":
                break;
            case "announcement":
                break;
            case "contact":
                break;
            case "category":
                name = (String) object.get("name");
                Category category = new CategoryService(this.session, this.em).getByName(name);
                if (category != null) {
                    objectId = category.getId();
                }
                break;
            case "faq":
                break;
            case "device":
                break;
            case "group":
                break;
            case "hwconf":
                break;
            case "crxonfig":
                break;
            case "crxmonfig":
                break;
            case "room":
                break;
            case "software":
                name = (String) object.get("name");
                Software software = new SoftwareService(this.session, this.em).getByName(name);
                if (software != null) {
                    objectId = software.getId();
                }
                break;
            case "softwarelicence":
                break;
            case "user":
                break;
        }
        if (objectId == null) {
            return new CrxResponse(session, "ERROR", "Object was not found.");
        } else {
            return new CrxResponse(session, "OK", "Object was found.", objectId);
        }
    }

}
