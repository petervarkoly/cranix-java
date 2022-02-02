/* (c) 2020 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.services;

import de.cranix.dao.*;
import de.cranix.helper.CrxSystemCmd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

import static de.cranix.helper.CranixConstants.*;

@SuppressWarnings("unchecked")
public class Service extends Config {

    Logger logger = LoggerFactory.getLogger(Service.class);

    protected Session session;
    protected EntityManager em;
    private Map<String, String> properties;
    private static List<String> systemNames;

    public Service(Session session, EntityManager em) {
        super();
        this.session = session;
        this.em = em;
        properties = new HashMap<String, String>();
        String[] tmp;
        try {
            File file = new File(cranixPropFile);
            FileInputStream fileInput = new FileInputStream(file);
            Properties props = new Properties();
            props.load(fileInput);
            fileInput.close();
            Enumeration<Object> enuKeys = props.keys();
            while (enuKeys.hasMoreElements()) {
                String key = (String) enuKeys.nextElement();
                String value = props.getProperty(key);
                properties.put(key, value);
            }
            if (systemNames == null) {
                systemNames = new ArrayList<String>();
                for (String line : Files.readAllLines(Paths.get("/etc/passwd"))) {
                    tmp = line.split(":");
                    if (tmp.length > 1) {
                        systemNames.add(tmp[0].toLowerCase());
                    }
                }
                for (String line : Files.readAllLines(Paths.get("/etc/group"))) {
                    tmp = line.split(":");
                    if (tmp.length > 1) {
                        systemNames.add(tmp[0].toLowerCase());
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Session getSession() {
        return this.session;
    }

    public String getProperty(String property) {
        if (properties.containsKey(property)) {
            return properties.get(property);
        }
        return "";
    }

    public String getNl() {
        return System.getProperty("line.separator");
    }

    public boolean isNameUnique(String name) {
        if (this.getConfigValue("WORKGROUP").equals(name)) {
            return false;
        }
        if (systemNames.contains(name.toLowerCase())) {
            return false;
        }
        Query query = this.em.createNamedQuery("User.getByUid");
        query.setParameter("uid", name);
        List<User> user = (List<User>) query.getResultList();
        if (!user.isEmpty()) {
            logger.debug("Found user " + user);
            return false;
        }
        query = this.em.createNamedQuery("Group.getByName");
        query.setParameter("name", name);
        List<Group> group = (List<Group>) query.getResultList();
        if (!group.isEmpty()) {
            logger.debug("Found group " + group);
            return false;
        }
        query = this.em.createNamedQuery("Device.getByName");
        query.setParameter("name", name);
        List<Device> device = (List<Device>) query.getResultList();
        if (!device.isEmpty()) {
            logger.debug("Found device " + device);
            return false;
        }
        query = this.em.createNamedQuery("Room.getByName");
        query.setParameter("name", name);
        List<Room> room = (List<Room>) query.getResultList();
        if (!room.isEmpty()) {
            logger.debug("Found room " + room);
            return false;
        }
        return true;
    }

    public CrxResponse checkPassword(String password) {
        String[] program = new String[1];
        StringBuffer reply = new StringBuffer();
        StringBuffer stderr = new StringBuffer();
        program[0] = cranixBaseDir + "tools/check_password_complexity.sh";
        CrxSystemCmd.exec(program, reply, stderr, password);
        if (!reply.toString().isEmpty()) {
            List<String> parameters = new ArrayList<String>();
            String[] error = reply.toString().split("##");
            if (error.length > 1) {
                parameters.add(error[1]);
                return new CrxResponse(this.getSession(), "ERROR", error[0], null, parameters);
            } else {
                return new CrxResponse(this.getSession(), "ERROR", reply.toString());
            }
        }
        return null;
    }

    public boolean checkNonASCII(String name) {
        return !Pattern.matches("[a-zA-Z0-9\\.\\-_]+", name);
    }

    public boolean checkBadHostName(String name) {
        if (!name.matches("[a-zA-Z0-9\\-]+")) {
            logger.debug("Bad name match '" + name + "'");
            return true;
        }
        return name.matches(".*-wlan");
    }

    public String isMacUnique(String name) {
        Query query = this.em.createNamedQuery("Device.getByMAC");
        query.setParameter("MAC", name);
        List<Device> devices = (List<Device>) query.getResultList();
        if (!devices.isEmpty()) {
            return devices.get(0).getName();
        }
        return "";
    }

    public String isIPUnique(String name) {
        Query query = this.em.createNamedQuery("Device.getByIP");
        query.setParameter("IP", name);
        List<Device> devices = (List<Device>) query.getResultList();
        if (!devices.isEmpty()) {
            return devices.get(0).getName();
        }
        return "";
    }

    public boolean isUserAliasUnique(String name) {
        Query query = this.em.createNamedQuery("User.getByUid");
        query.setParameter("uid", name);
        boolean result = query.getResultList().isEmpty();
        if (result) {
            if (this.getConfigValue("ALLOW_MULTIPLE_ALIASES").toLowerCase().equals("no")) {
                query = this.em.createNamedQuery("Alias.getByName");
                query.setParameter("alias", name);
                result = query.getResultList().isEmpty();
            }
        }
        return result;
    }

    public boolean isSuperuser() {
        if (properties.containsKey("de.cranix.dao.Session.superusers")) {
            for (String s : properties.get("de.cranix.dao.Session.superusers").split(",")) {
                if (s.startsWith("@")) {
                    for (Group g : this.session.getUser().getGroups()) {
                        if (g.getName().equals(s.substring(1))) {
                            logger.debug("isSuperuser by group" + s + " " + this.session.getUser().getUid());
                            return true;
                        }
                    }
                } else if (s.equals(this.session.getUser().getUid())) {
                    logger.debug("isSuperuser by uid" + this.session.getUser().getUid());
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isSuperuser(Session session) {
        if (properties.containsKey("de.cranix.dao.Session.superusers")) {
            for (String s : properties.get("de.cranix.dao.Session.superusers").split(",")) {
                if (s.startsWith("@")) {
                    for (Group g : session.getUser().getGroups()) {
                        if (g.getName().equals(s.substring(1))) {
                            return true;
                        }
                    }
                } else if (s.equals(session.getUser().getUid())) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean mayAdd(Object object) {
        if (this.session.getUser().getId() == 6) {
            return true;
        }

        List<String> neededRights = new ArrayList<String>();
        switch (object.getClass().getName()) {
            case "de.cranix.dao.Acl":
                neededRights.add("acl.add");
                break;
            case "de.cranix.dao.AccessInRoom":
                neededRights.add("room.add");
                break;
            case "de.cranix.dao.Announcement":
            case "de.cranix.dao.Contact":
            case "de.cranix.dao.FAQ":
                neededRights.add("information.add");
                break;
            case "de.cranix.dao.Category":
                neededRights.add("category.add");
                break;
            case "de.cranix.dao.Device":
                neededRights.add("device.add");
                break;
            case "de.cranix.dao.Group":
                Group group = (Group) object;
                neededRights.add("group.add");
                neededRights.add("group.add." + group.getGroupType());
                break;
            case "de.cranix.dao.HWConf":
                neededRights.add("hwconf.add");
                break;
            case "de.cranix.dao.CrxConfig":
                neededRights.add("crxconfig.add");
                break;
            case "de.cranix.dao.CrxMConfig":
                neededRights.add("crxmconfig.add");
                break;
            case "de.cranix.dao.Room":
                neededRights.add("room.add");
                break;
            case "de.cranix.dao.RoomSmartControl":
                break;
            case "de.cranix.dao.Partition":
                neededRights.add("hwconf.add");
                break;
            case "de.cranix.dao.Software":
                neededRights.add("software.add");
                break;
            case "de.cranix.dao.SoftwareLicence":
                neededRights.add("softwarelicence.add");
                break;
            case "de.cranix.dao.User":
                User user = (User) object;
                neededRights.add("user.add");
                neededRights.add("user.add." + user.getRole());
                break;
        }
        if (this.isSuperuser()) {
            //Super User must not add the objects of CEPHALIX
            //TODO 6 need be evaluated eventually
            return true;
        }
        logger.debug("mayadd needed Rights:" + neededRights + " user: " + session.getUser() + " object: " + object);
        for (String right : neededRights) {
            if (this.session.getAcls() != null) {
                if (this.session.getAcls().contains(right)) {
                    return true;
                }
            }
        }
        //TODO some other acls based on object
        return false;
    }

    public boolean mayModify(Object object) {
        if (this.session.getUser().getId() == 6) {
            return true;
        }

        User owner = null;
        Long ownerId = null;
        List<String> neededRights = new ArrayList<String>();
        switch (object.getClass().getName()) {
            case "de.cranix.dao.Acl":
                Acl Acl = (Acl) object;
                owner = Acl.getCreator();
                neededRights.add("acl.modify");
                break;
            case "de.cranix.dao.AccessInRoom":
                AccessInRoom AccessInRoom = (AccessInRoom) object;
                owner = AccessInRoom.getCreator();
                neededRights.add("room.modify");
                break;
            case "de.cranix.dao.Announcement":
                Announcement an = (Announcement) object;
                owner = an.getOwner();
                neededRights.add("information.modify");
                break;
            case "de.cranix.dao.Category":
                Category cat = (Category) object;
                owner = cat.getOwner();
                neededRights.add("category.modify");
                break;
            case "de.cranix.dao.Contact":
                Contact con = (Contact) object;
                owner = con.getOwner();
                neededRights.add("information.modify");
                break;
            case "de.cranix.dao.FAQ":
                FAQ faq = (FAQ) object;
                owner = faq.getOwner();
                neededRights.add("information.modify");
                break;
            case "de.cranix.dao.Device":
                Device Device = (Device) object;
                owner = Device.getOwner();
                neededRights.add("device.modify");
                break;
            case "de.cranix.dao.Group":
                Group group = (Group) object;
                owner = group.getOwner();
                neededRights.add("group.modify");
                neededRights.add("group.modify." + group.getGroupType());
                break;
            case "de.cranix.dao.HWConf":
                HWConf HWConf = (HWConf) object;
                owner = HWConf.getCreator();
                neededRights.add("hwconf.modify");
                break;
            case "de.cranix.dao.CrxConfig":
                CrxConfig crxConfig = (CrxConfig) object;
                owner = crxConfig.getCreator();
                neededRights.add("crxconfig.modify");
                break;
            case "de.cranix.dao.CrxMConfig":
                CrxMConfig crxMConfig = (CrxMConfig) object;
                owner = crxMConfig.getCreator();
                neededRights.add("crxmconfig.modify");
                break;
            case "de.cranix.dao.Room":
                Room room = (Room) object;
                owner = room.getCreator();
                neededRights.add("room.modify");
                break;
            case "de.cranix.dao.RoomSmartControl":
                RoomSmartControl rsc = (RoomSmartControl) object;
                owner = rsc.getOwner();
                break;
            case "de.cranix.dao.Partition":
                Partition partition = (Partition) object;
                owner = partition.getCreator();
                neededRights.add("hwconf.modify");
                break;
            case "de.cranix.dao.Software":
                Software software = (Software) object;
                owner = software.getCreator();
                neededRights.add("software.modify");
                break;
            case "de.cranix.dao.SoftwareLicence":
                SoftwareLicense softwareLicense = (SoftwareLicense) object;
                owner = softwareLicense.getCreator();
                neededRights.add("softwarelicence.modify");
                break;
            case "de.cranix.dao.User":
                User user = (User) object;
                owner = user.getCreator();
                neededRights.add("user.modify");
                if (session.getUser().getRole().equals(roleTeacher) &&
                        user.getRole().equals(roleStudent)) {
                    neededRights.add("education.users");
                }
                neededRights.add("user.modify." + user.getRole());
                break;
        }
        if (ownerId == null) {
            ownerId = 1L;
        }
        if (owner != null) {
            ownerId = owner.getId();
        }
        if (this.isSuperuser() && ownerId != 6) {
            //Super User must not modify the objects of CEPHALIX
            //TODO 6 need be evaluated eventually
            logger.debug("mayModify is Superuser");
            return true;
        }
        if (owner != null && this.session.getUser().equals(owner)) {
            logger.debug("mayModify is owner");
            return true;
        }
        logger.debug("mayModify needed Rights:" + neededRights + " user: " + session.getUser() + " object: " + object);
        for (String right : neededRights) {
            if (this.session.getAcls() != null) {
                if (this.session.getAcls().contains(right)) {
                    logger.debug("mayModify is allowed to modify");
                    return true;
                }
            }
        }
        logger.debug("mayModify is not allowed to modify");
        //TODO some other acls based on object
        return false;
    }

    public boolean mayDelete(Object object) {
        if (this.session.getUser().getId() == 6) {
            return true;
        }
        User owner = null;
        Long ownerId = null;
        List<String> neededRights = new ArrayList<String>();
        switch (object.getClass().getName()) {
            case "de.cranix.dao.Acl":
                Acl Acl = (Acl) object;
                owner = Acl.getCreator();
                neededRights.add("acl.delete");
                break;
            case "de.cranix.dao.AccessInRoom":
                AccessInRoom AccessInRoom = (AccessInRoom) object;
                owner = AccessInRoom.getCreator();
                neededRights.add("room.delete");
                break;
            case "de.cranix.dao.Announcement":
                Announcement an = (Announcement) object;
                owner = an.getOwner();
                neededRights.add("information.delete");
                break;
            case "de.cranix.dao.Category":
                Category cat = (Category) object;
                owner = cat.getOwner();
                neededRights.add("category.delete");
                break;
            case "de.cranix.dao.Contact":
                Contact con = (Contact) object;
                owner = con.getOwner();
                neededRights.add("information.delete");
                break;
            case "de.cranix.dao.FAQ":
                FAQ faq = (FAQ) object;
                owner = faq.getOwner();
                neededRights.add("information.delete");
                break;
            case "de.cranix.dao.Device":
                Device Device = (Device) object;
                owner = Device.getOwner();
                neededRights.add("device.delete");
                break;
            case "de.cranix.dao.Group":
                Group group = (Group) object;
                owner = group.getOwner();
                neededRights.add("group.delete");
                neededRights.add("group.delete." + group.getGroupType());
                break;
            case "de.cranix.dao.HWConf":
                HWConf HWConf = (HWConf) object;
                owner = HWConf.getCreator();
                neededRights.add("hwconf.delete");
                break;
            case "de.cranix.dao.CrxConfig":
                CrxConfig crxConfig = (CrxConfig) object;
                owner = crxConfig.getCreator();
                neededRights.add("crxconfig.delete");
                break;
            case "de.cranix.dao.CrxMConfig":
                CrxMConfig crxMConfig = (CrxMConfig) object;
                owner = crxMConfig.getCreator();
                neededRights.add("crxmconfig.delete");
                break;
            case "de.cranix.dao.Room":
                Room room = (Room) object;
                owner = room.getCreator();
                neededRights.add("room.delete");
                break;
            case "de.cranix.dao.RoomSmartControl":
                RoomSmartControl rsc = (RoomSmartControl) object;
                owner = rsc.getOwner();
                break;
            case "de.cranix.dao.Partition":
                Partition partition = (Partition) object;
                owner = partition.getCreator();
                neededRights.add("hwconf.delete");
                break;
            case "de.cranix.dao.Software":
                Software software = (Software) object;
                owner = software.getCreator();
                neededRights.add("software.delete");
                break;
            case "de.cranix.dao.SoftwareLicence":
                SoftwareLicense softwareLicense = (SoftwareLicense) object;
                owner = softwareLicense.getCreator();
                neededRights.add("softwarelicence.delete");
                break;
            case "de.cranix.dao.User":
                User user = (User) object;
                ownerId = user.getCreator().getId();
                neededRights.add("user.delete");
                neededRights.add("user.delete." + user.getRole());
                break;
        }
        if (ownerId == null) {
            ownerId = 1L;
        }
        if (owner != null) {
            ownerId = owner.getId();
        }
        if (this.isSuperuser() && ownerId != 6) {
            //Super User must not delete the objects of CEPHALIX
            //TODO 6 need be evaluated eventually
            return true;
        }
        if (owner != null && this.session.getUser().equals(owner)) {
            return true;
        }
        logger.debug("maydelete needed Rights:" + neededRights + " user: " + session.getUser() + " object: " + object);
        for (String right : neededRights) {
            if (this.session.getAcls() != null) {
                if (this.session.getAcls().contains(right)) {
                    return true;
                }
            }
        }
        //TODO some other acls based on object
        return false;
    }

    public boolean isProtected(Object object) {
        if (object != null) {
            switch (object.getClass().getName()) {
                case "de.cranix.dao.User":
                    if (properties.containsKey("de.cranix.dao.User.protected")) {
                        User u = (User) object;
                        for (String s : properties.get("de.cranix.dao.User.protected").split(",")) {
                            if (s.equals(u.getUid()))
                                return true;
                        }
                    }
                    return false;
                case "de.cranix.dao.Room":
                    if (properties.containsKey("de.cranix.dao.Room.protected")) {
                        Room r = (Room) object;
                        for (String s : properties.get("de.cranix.dao.Room.protected").split(",")) {
                            if (s.equals(r.getName()))
                                return true;
                        }
                    }
                    return false;
                case "de.cranix.dao.Device":
                    if (properties.containsKey("de.cranix.dao.Device.protected")) {
                        Device d = (Device) object;
                        for (String s : properties.get("de.cranix.dao.Device.protected").split(",")) {
                            if (s.equals(d.getName()))
                                return true;
                        }
                    }
                    return false;
                case "de.cranix.dao.Group":
                    if (properties.containsKey("de.cranix.dao.Group.protected")) {
                        Group g = (Group) object;
                        for (String s : properties.get("de.cranix.dao.Group.protected").split(",")) {
                            if (s.equals(g.getName()))
                                return true;
                        }
                    }
                    return false;
                case "de.cranix.dao.HWConf":
                    if (properties.containsKey("de.cranix.dao.HWConf.protected")) {
                        HWConf hw = (HWConf) object;
                        for (String s : properties.get("de.cranix.dao.HWConf.protected").split(",")) {
                            if (s.equals(hw.getName()))
                                return true;
                        }
                    }
                    return false;
            }
        }
        return false;

    }

    public List<String> allowedModules(User user) {
        List<String> modules = new ArrayList<String>();
        //Is it allowed by the groups.
        for (Group group : user.getGroups()) {
            for (Acl acl : group.getAcls()) {
                if (acl.getAllowed()) {
                    modules.add(acl.getAcl());
                }
            }
        }
        //Is it allowed by the user
        for (Acl acl : user.getAcls()) {
            if (acl.getAllowed() && !modules.contains(acl.getAcl())) {
                modules.add(acl.getAcl());
            } else if (modules.contains(acl.getAcl())) {
                //It is forbidden by the user
                modules.remove(acl.getAcl());
            }
        }
        return modules;
    }

    public boolean isAllowed(User user, String acl) {
        return this.allowedModules(user).contains(acl);
    }

    public boolean isAllowed(String acl) {
        return this.isAllowed(session.getUser(), acl);
    }

    public boolean systemctl(String action, String service) {
        String[] program = new String[3];
        program[0] = "systemctl";
        program[1] = action;
        program[2] = service;
        StringBuffer reply = new StringBuffer();
        StringBuffer error = new StringBuffer();
        CrxSystemCmd.exec(program, reply, error, null);
        return error.length() == 0;
    }

    public CrxMConfig getMConfigObject(Object object, String key, String value) {
        Long id = null;
        Query query = this.em.createNamedQuery("CrxMConfig.check");
        switch (object.getClass().getName()) {
            case "de.cranix.dao.Group":
                query.setParameter("type", "Group");
                id = ((Group) object).getId();
                break;
            case "de.cranix.dao.User":
                query.setParameter("type", "User");
                id = ((User) object).getId();
                break;
            case "de.cranix.dao.Room":
                query.setParameter("type", "Room");
                id = ((Room) object).getId();
                break;
            case "de.cranix.dao.Device":
                query.setParameter("type", "Device");
                id = ((Device) object).getId();
                break;
        }
        query.setParameter("id", id).setParameter("keyword", key).setParameter("value", value);
        if (query.getResultList().isEmpty()) {
            return null;
        }
        return (CrxMConfig) query.getResultList().get(0);
    }

    public CrxMConfig getMConfigObject(CrxMConfig crxMConfig) {
        Query query = this.em.createNamedQuery("CrxMConfig.check");
        query.setParameter("type", crxMConfig.getObjectType())
                .setParameter("id", crxMConfig.getObjectId())
                .setParameter("keyword", crxMConfig.getKeyword())
                .setParameter("value", crxMConfig.getValue());
        if (query.getResultList().isEmpty()) {
            return null;
        }
        return (CrxMConfig) query.getResultList().get(0);
    }

    public List<CrxMConfig> getMConfigObjects(
            String objectType,
            String keyword,
            String value
    ) {
        Query query = this.em.createNamedQuery("CrxMConfig.getAllObject");
        query.setParameter("type", objectType)
                .setParameter("keyword", keyword )
                .setParameter("value", value);
        if (query.getResultList().isEmpty()) {
            return new ArrayList<>();
        }
        return (List<CrxMConfig>) query.getResultList();
    }
    public boolean checkMConfig(Object object, String key, String value) {
        if (this.getMConfigObject(object, key, value) == null) {
            return false;
        }
        return true;
    }

    public boolean checkMConfig(CrxMConfig crxMConfig) {
        if (this.getMConfigObject(crxMConfig) == null) {
            return false;
        }
        return true;
    }

    public CrxConfig getConfigObject(CrxConfig crxConfig) {
        Query query = this.em.createNamedQuery("CrxConfig.get");
        query.setParameter("type", crxConfig.getObjectType())
                .setParameter("id", crxConfig.getObjectId())
                .setParameter("keyword", crxConfig.getKeyword());
        logger.debug("getConfigObject " + crxConfig + " " + query.getResultList());
        if (query.getResultList().isEmpty()) {
            return null;
        }
        return (CrxConfig) query.getResultList().get(0);
    }

    public CrxConfig getConfigObject(Object object, String key) {
        Long id = null;
        Query query = this.em.createNamedQuery("CrxConfig.get");
        switch (object.getClass().getName()) {
            case "de.cranix.dao.Group":
                query.setParameter("type", "Group");
                id = ((Group) object).getId();
                break;
            case "de.cranix.dao.User":
                query.setParameter("type", "User");
                id = ((User) object).getId();
                break;
            case "de.cranix.dao.Room":
                query.setParameter("type", "Room");
                id = ((Room) object).getId();
                break;
            case "de.cranix.dao.Device":
                query.setParameter("type", "Device");
                id = ((Device) object).getId();
                break;
        }
        query.setParameter("id", id).setParameter("keyword", key);
        if (query.getResultList().isEmpty()) {
            return null;
        }
        return (CrxConfig) query.getResultList().get(0);
    }

    public boolean checkConfig(Object object, String key) {
        if (this.getConfigObject(object, key) == null) {
            return false;
        }
        return true;
    }

    public boolean checkConfig(CrxConfig crxConfig) {
        if (this.getConfigObject(crxConfig) == null) {
            return false;
        }
        return true;
    }

    /**
     * Checks if a value of an enumerate is allowed.
     *
     * @param name  The of the enumerate.
     * @param value The value of the enumerate
     * @return Boolean True if the enumerate value is allowed.
     * False if the enumerate is not allowed and in case of error.
     */
    public boolean checkEnumerate(String name, String value) {
        Query query = this.em.createNamedQuery("Enumerate.get");
        try {
            query.setParameter("name", name).setParameter("value", value);
            return !query.getResultList().isEmpty();
        } catch (Exception e) {
            logger.error("checkEnumerate: " + e.getMessage());
            return false;
        } finally {
        }
    }

    /**
     * Returns the list of the member of an enumerate.
     *
     * @param name Name of the enumerates: roomType, groupType, deviceType ...
     * @return The list of the member of the enumerate
     * @see addEnumerate
     */
    public List<String> getEnumerates(String name) {
        try {
            Query query = this.em.createNamedQuery("Enumerate.getByName").setParameter("name", name);
            List<String> results = new ArrayList<String>();
            for (Enumerate e : (List<Enumerate>) query.getResultList()) {
                results.add(e.getValue());
            }
            return results;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    public CrxConfig getConfig(String type, Long id, String key) {
        CrxConfig crxConfig = null;
        Query query = this.em.createNamedQuery("CrxConfig.get");
        query.setParameter("type", type).setParameter("id",id).setParameter("keyword", key);
        if (!query.getResultList().isEmpty()) {
            crxConfig = (CrxConfig) query.getResultList().get(0);
        }
        return crxConfig;
    }

    public List<CrxMConfig> getMConfigs(String objectType, Long id, String key) {
        Query query = this.em.createNamedQuery("CrxMConfig.get");
        query.setParameter("type", objectType).setParameter("keyword", key).setParameter("id", id);
        List<CrxMConfig> crxMConfigs = (List<CrxMConfig>) query.getResultList();
        return crxMConfigs;
    }

    public List<String> getMConfigs(Object object, String key) {
        Long id = null;
        Query query = this.em.createNamedQuery("CrxMConfig.get");
        switch (object.getClass().getName()) {
            case "de.cranix.dao.Group":
                query.setParameter("type", "Group");
                id = ((Group) object).getId();
                break;
            case "de.cranix.dao.User":
                query.setParameter("type", "User");
                id = ((User) object).getId();
                break;
            case "de.cranix.dao.Room":
                query.setParameter("type", "Room");
                id = ((Room) object).getId();
                break;
            case "de.cranix.dao.Device":
                query.setParameter("type", "Device");
                id = ((Device) object).getId();
                break;
        }
        query.setParameter("id", id).setParameter("keyword", key);
        ArrayList<String> values = new ArrayList<String>();
        for (CrxMConfig config : (List<CrxMConfig>) query.getResultList()) {
            values.add(config.getValue());
        }
        return values;
    }

    public List<CrxMConfig> getAllMConfig(Object object) {
        Long id = null;
        Query query = this.em.createNamedQuery("CrxMConfig.getAllById");
        switch (object.getClass().getName()) {
            case "de.cranix.dao.Group":
                query.setParameter("type", "Group");
                id = ((Group) object).getId();
                break;
            case "de.cranix.dao.User":
                query.setParameter("type", "User");
                id = ((User) object).getId();
                break;
            case "de.cranix.dao.Room":
                query.setParameter("type", "Room");
                id = ((Room) object).getId();
                break;
            case "de.cranix.dao.Device":
                query.setParameter("type", "Device");
                id = ((Device) object).getId();
                break;
        }
        query.setParameter("id", id);
        return (List<CrxMConfig>) query.getResultList();
    }

    public List<CrxMConfig> getMConfigObjects(Object object, String key) {
        Long id = null;
        Query query = this.em.createNamedQuery("CrxMConfig.get");
        switch (object.getClass().getName()) {
            case "de.cranix.dao.Group":
                query.setParameter("type", "Group");
                id = ((Group) object).getId();
                break;
            case "de.cranix.dao.User":
                query.setParameter("type", "User");
                id = ((User) object).getId();
                break;
            case "de.cranix.dao.Room":
                query.setParameter("type", "Room");
                id = ((Room) object).getId();
                break;
            case "de.cranix.dao.Device":
                query.setParameter("type", "Device");
                id = ((Device) object).getId();
                break;
        }
        query.setParameter("id", id).setParameter("keyword", key);
        ArrayList<CrxMConfig> values = new ArrayList<CrxMConfig>();
        for (CrxMConfig config : (List<CrxMConfig>) query.getResultList()) {
            values.add(config);
        }
        return values;
    }

    public StringBuilder getImportDir(String startTime) {
        StringBuilder importDir = new StringBuilder();
        importDir.append(getConfigValue("HOME_BASE")).append("/groups/SYSADMINS/userimports/").append(startTime);
        return importDir;
    }

    public String getConfig(Object object, String key) {
        Long id = null;
        Query query = this.em.createNamedQuery("CrxConfig.get");
        switch (object.getClass().getName()) {
            case "de.cranix.dao.Group":
                query.setParameter("type", "Group");
                id = ((Group) object).getId();
                break;
            case "de.cranix.dao.User":
                query.setParameter("type", "User");
                id = ((User) object).getId();
                break;
            case "de.cranix.dao.Room":
                query.setParameter("type", "Room");
                id = ((Room) object).getId();
                break;
            case "de.cranix.dao.Device":
                query.setParameter("type", "Device");
                id = ((Device) object).getId();
                break;
        }
        query.setParameter("id", id).setParameter("keyword", key);
        if (query.getResultList().isEmpty()) {
            return null;
        }
        CrxConfig config = (CrxConfig) query.getResultList().get(0);
        return config.getValue();
    }

    public List<CrxConfig> getAllConfig(Object object) {
        Long id = null;
        Query query = this.em.createNamedQuery("CrxConfig.getAllById");
        switch (object.getClass().getName()) {
            case "de.cranix.dao.Group":
                query.setParameter("type", "Group");
                id = ((Group) object).getId();
                break;
            case "de.cranix.dao.User":
                query.setParameter("type", "User");
                id = ((User) object).getId();
                break;
            case "de.cranix.dao.Room":
                query.setParameter("type", "Room");
                id = ((Room) object).getId();
                break;
            case "de.cranix.dao.Device":
                query.setParameter("type", "Device");
                id = ((Device) object).getId();
                break;
        }
        query.setParameter("id", id);
        return (List<CrxConfig>) query.getResultList();
    }

    public CrxResponse addMConfig(CrxMConfig mconfig) {
        if (this.checkMConfig(mconfig)) {
            return new CrxResponse(this.getSession(), "ERROR", "This mconfig value already exists.");
        }
        mconfig.setCreator(this.session.getUser());
        try {
            this.em.getTransaction().begin();
            this.em.persist(mconfig);
            this.em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("addMConfig: " + e.getMessage());
            return new CrxResponse(this.getSession(), "ERROR", e.getMessage());
        }
        return new CrxResponse(this.getSession(), "OK", "Mconfig was created", mconfig.getId());
    }

    public CrxResponse addMConfig(Object object, String key, String value) {
        if (this.checkMConfig(object, key, value)) {
            return new CrxResponse(this.getSession(), "ERROR", "This mconfig value already exists.");
        }
        CrxMConfig mconfig = new CrxMConfig();
        switch (object.getClass().getName()) {
            case "de.cranix.dao.Group":
                mconfig.setObjectType("Group");
                mconfig.setObjectId(((Group) object).getId());
                break;
            case "de.cranix.dao.User":
                mconfig.setObjectType("User");
                mconfig.setObjectId(((User) object).getId());
                break;
            case "de.cranix.dao.Room":
                mconfig.setObjectType("Room");
                mconfig.setObjectId(((Room) object).getId());
                break;
            case "de.cranix.dao.Device":
                mconfig.setObjectType("Device");
                mconfig.setObjectId(((Device) object).getId());
                break;
        }
        mconfig.setKeyword(key);
        mconfig.setValue(value);
        mconfig.setCreator(this.session.getUser());
        try {
            this.em.getTransaction().begin();
            this.em.persist(mconfig);
            this.em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("addMConfig: " + e.getMessage());
            return new CrxResponse(this.getSession(), "ERROR", e.getMessage());
        }
        return new CrxResponse(this.getSession(), "OK", "Mconfig was created", mconfig.getId());
    }

    public CrxResponse addConfig(Object object, String key, String value) {
        if (this.checkConfig(object, key)) {
            return new CrxResponse(this.getSession(), "ERROR", "This config already exists.");
        }
        CrxConfig config = new CrxConfig();
        switch (object.getClass().getName()) {
            case "de.cranix.dao.Group":
                config.setObjectType("Group");
                config.setObjectId(((Group) object).getId());
                break;
            case "de.cranix.dao.User":
                config.setObjectType("User");
                config.setObjectId(((User) object).getId());
                break;
            case "de.cranix.dao.Room":
                config.setObjectType("Room");
                config.setObjectId(((Room) object).getId());
                break;
            case "de.cranix.dao.Device":
                config.setObjectType("Device");
                config.setObjectId(((Device) object).getId());
                break;
        }
        config.setKeyword(key);
        config.setValue(value);
        config.setCreator(this.session.getUser());
        try {
            this.em.getTransaction().begin();
            this.em.persist(config);
            this.em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("addConfig: " + e.getMessage());
            return new CrxResponse(this.getSession(), "ERROR", e.getMessage());
        }
        return new CrxResponse(this.getSession(), "OK", "Config was created");
    }

    public CrxResponse addConfig(CrxConfig config) {
        if (this.checkConfig(config)) {
            return new CrxResponse(this.getSession(), "ERROR", "This config already exists.");
        }
        config.setCreator(this.session.getUser());
        try {
            this.em.getTransaction().begin();
            this.em.persist(config);
            this.em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("addConfig: " + e.getMessage());
            return new CrxResponse(this.getSession(), "ERROR", e.getMessage());
        }
        return new CrxResponse(this.getSession(), "OK", "Config was created");
    }

    public CrxResponse setConfig(Object object, String key, String value) {
        if (!this.checkConfig(object, key)) {
            return this.addConfig(object, key, value);
        }
        CrxConfig config = this.getConfigObject(object, key);
        try {
            config = this.em.find(CrxConfig.class, config.getId());
            config.setValue(value);
            this.em.getTransaction().begin();
            this.em.merge(config);
            this.em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("setConfig: " + e.getMessage());
            return new CrxResponse(this.getSession(), "ERROR", e.getMessage());
        }
        return new CrxResponse(this.getSession(), "OK", "Config was updated");
    }

    public CrxResponse deleteConfig(CrxConfig config) {
        try {
            CrxConfig crxConfig = this.getConfigObject(config);
            this.em.getTransaction().begin();
            this.em.remove(crxConfig);
            this.em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("deleteConfig: " + e.getMessage());
            return new CrxResponse(this.getSession(), "ERROR", e.getMessage());
        }
        return new CrxResponse(this.getSession(), "OK", "Config was deleted");
    }

    public CrxResponse deleteMConfig(Object object, Long configId) {
        try {
            CrxMConfig config = this.em.find(CrxMConfig.class, configId);
            this.em.getTransaction().begin();
            this.em.merge(config);
            this.em.remove(config);
            this.em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("deleteConfig: " + e.getMessage());
            return new CrxResponse(this.getSession(), "ERROR", e.getMessage());
        }
        return new CrxResponse(this.getSession(), "OK", "Config was deleted");
    }

    public CrxResponse deleteMConfig(CrxMConfig config) {
        try {
            CrxMConfig crxConfig = this.getMConfigObject(config);
            this.em.getTransaction().begin();
            this.em.remove(crxConfig);
            this.em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("deleteConfig: " + e.getMessage());
            return new CrxResponse(this.getSession(), "ERROR", e.getMessage());
        }
        return new CrxResponse(this.getSession(), "OK", "Config was deleted");
    }

    public CrxResponse deleteConfig(Object object, String key) {
        CrxConfig config = this.getConfigObject(object, key);
        if (config == null) {
            return new CrxResponse(this.getSession(), "ERROR", "Config does not exists.");
        }
        try {
            this.em.getTransaction().begin();
            config = this.em.find(CrxConfig.class, config.getId());
            this.em.merge(config);
            this.em.remove(config);
            this.em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("deleteConfig: " + e.getMessage());
            return new CrxResponse(this.getSession(), "ERROR", e.getMessage());
        } finally {
        }
        return new CrxResponse(this.getSession(), "OK", "Config was deleted");
    }

    public CrxResponse deleteMConfig(Object object, String key, String value) {
        CrxMConfig config = this.getMConfigObject(object, key, value);
        if (config == null) {
            return new CrxResponse(this.getSession(), "ERROR", "MConfig does not exists.");
        }
        try {
            this.em.getTransaction().begin();
            config = this.em.find(CrxMConfig.class, config.getId());
            this.em.merge(config);
            this.em.remove(config);
            this.em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("deleteMConfig: " + e.getMessage());
            return new CrxResponse(this.getSession(), "ERROR", e.getMessage());
        } finally {
        }
        return new CrxResponse(this.getSession(), "OK", "Config was deleted");
    }

    public void deletAllConfigs(Object object) {
        //Remove corresponding CrxConfig
        for (CrxConfig o : this.getAllConfig(object)) {
            this.em.remove(o);
        }
        //Remove corresponding CrxMConfig
        for (CrxMConfig o : this.getAllMConfig(object)) {
            this.em.remove(o);
        }
    }
}
