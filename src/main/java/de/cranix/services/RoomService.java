/* (c) 2021 Péter Varkoly <pvarkoly@cephalix.eu> - all rights reserved */
package de.cranix.services;

import de.cranix.dao.*;
import de.cranix.helper.IPv4Net;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static de.cranix.helper.CranixConstants.cranixTmpDir;
import static de.cranix.helper.CranixConstants.roleStudent;
import static de.cranix.helper.StaticHelpers.createLiteralJson;
import static de.cranix.helper.StaticHelpers.startPlugin;

@SuppressWarnings("unchecked")
public class RoomService extends Service {

    Logger logger = LoggerFactory.getLogger(RoomService.class);

    @SuppressWarnings("serial")
    static Map<Integer, Integer> nmToRowsPlaces = new HashMap<Integer, Integer>() {{
        put(31, 2);
        put(30, 2);
        put(29, 3);
        put(28, 4);
        put(27, 6);
        put(26, 8);
        put(25, 11);
        put(24, 16);
        put(23, 23);
        put(22, 32);
        put(21, 46);
        put(20, 64);
        put(19, 91);
    }};

    static public List<Long> getDeviceIds(Room room) {
        List<Long> ids = new ArrayList<>();
        for( Device device : room.getDevices() ){
                ids.add(device.getId());
        }
        return ids;
    }

    public RoomService(Session session, EntityManager em) {
        super(session, em);
    }

    public boolean isNameUnique(String name) {
        try {
            Query query = this.em.createNamedQuery("Room.getByName");
            query.setParameter("name", name);
            List<Room> rooms = (List<Room>) query.getResultList();
            return rooms.isEmpty();
        } catch (Exception e) {
            logger.error("isNameUnique: " + e.getMessage());
            return false;
        }
    }

    public boolean isDescriptionUnique(String description) {
        try {
            Query query = this.em.createNamedQuery("Room.getByDescription");
            query.setParameter("description", description);
            List<Room> rooms = query.getResultList();
            return rooms.isEmpty();
        } catch (Exception e) {
            logger.error("isDescriptionUnique: " + e.getMessage());
            return false;
        }
    }

    public Room getById(long roomId) {

        try {
            Room room = this.em.find(Room.class, roomId);
            if (room != null) {
                room.convertNmToCount();
            }
            return room;
        } catch (Exception e) {
            logger.error("getById: " + e.getMessage());
            return null;
        }
    }

    public List<Room> getAllToUse() {
        List<Room> rooms = new ArrayList<Room>();
        try {
            Query query = this.em.createNamedQuery("Room.findAllToUse");
            rooms = query.getResultList();
        } catch (Exception e) {
            logger.error("getAllToUse: " + e.getMessage());
        }
        for (Room room : rooms) {
            room.convertNmToCount();
        }
        rooms.sort(Comparator.comparing(Room::getName));
        return rooms;
    }

    public List<Room> getAll() {
        List<Room> rooms = new ArrayList<Room>();
        try {
            Query query = this.em.createNamedQuery("Room.findAll");
            rooms = query.getResultList();
        } catch (Exception e) {
            logger.error("getAll: " + e.getMessage());
        }
        for (Room room : rooms) {
            room.convertNmToCount();
        }
        rooms.sort(Comparator.comparing(Room::getName));
        return rooms;
    }

    public List<Room> getAllWithControl() {
        List<Room> rooms = new ArrayList<Room>();
        try {
            Query query = this.em.createNamedQuery("Room.findAllWithControl");
            rooms = query.getResultList();
        } catch (Exception e) {
            logger.error("getAllWithControl: " + e.getMessage());
        }
        for (Room room : rooms) {
            room.convertNmToCount();
        }
        rooms.sort(Comparator.comparing(Room::getName));
        return rooms;
    }

    public List<Room> getAllWithTeacherControl() {
        List<Room> rooms = new ArrayList<Room>();
        try {
            Query query = this.em.createNamedQuery("Room.findAllWithTeacherControl");
            rooms = query.getResultList();
        } catch (Exception e) {
            logger.error("getAllWithTeacherControl: " + e.getMessage());
        }
        for (Room room : rooms) {
            room.convertNmToCount();
        }
        rooms.sort(Comparator.comparing(Room::getName));
        return rooms;
    }

    public List<Room> getAllWithFirewallControl() {
        List<Room> rooms = new ArrayList<Room>();
        try {
            Query query = this.em.createNamedQuery("Room.findAllWithFirewallControl");
            for (Room room : (List<Room>) query.getResultList()) {
                rooms.add(room);
            }
        } catch (Exception e) {
            logger.error("getAllWithFirewallControl: " + e.getMessage());
        }
        for (Room room : rooms) {
            room.convertNmToCount();
        }
        rooms.sort(Comparator.comparing(Room::getName));
        return rooms;
    }

    public List<Room> getByType(String roomType) {
        List<Room> rooms = new ArrayList<Room>();
        try {
            Query query = this.em.createNamedQuery("Room.getByType").setParameter("type", roomType);
            rooms = query.getResultList();
        } catch (Exception e) {
            logger.error("getByType: " + e.getMessage());
        }
        for (Room room : rooms) {
            room.convertNmToCount();
        }
        rooms.sort(Comparator.comparing(Room::getName));
        return rooms;
    }


    public Room getByIP(String ip) {
        try {
            Query query = this.em.createNamedQuery("Room.getByIp").setParameter("ip", ip);
            Room room = (Room) query.getResultList().get(0);
            if (room != null) {
                room.convertNmToCount();
            }
            return room;
        } catch (Exception e) {
            logger.error("getByIP: " + e.getMessage());
            return null;
        }
    }

    public Room getByName(String name) {
        try {
            Query query = this.em.createNamedQuery("Room.getByName").setParameter("name", name);
            Room room = (Room) query.getResultList().get(0);
            if (room != null) {
                room.convertNmToCount();
            }
            return room;
        } catch (Exception e) {
            logger.error("getByName: " + e.getMessage());
            return null;
        }
    }

    /**
     * Delivers a list of rooms in which a user may register his own devices.
     * These can be AdHocAccess room in which the user is member itself or one of
     * the group of the user is member in the AdHocAccess room.
     * Furthermore Guestusers can have have rooms with AdHocAccess.
     *
     * @param user The user
     * @return The list of the rooms
     */
    public List<Room> getRoomToRegisterForUser(User user) {
        List<Room> rooms = new ArrayList<Room>();
        for (Category category : user.getCategories()) {
            if (category.getCategoryType().equals("AdHocAccess") &&
                    (!category.getStudentsOnly() || user.getRole().equals(roleStudent)) &&
                    !category.getRooms().isEmpty()) {
                rooms.add(category.getRooms().get(0));
            }
        }
        for (Group group : user.getGroups()) {
            for (Category category : group.getCategories()) {
                logger.debug("getAllToRegister: " + category);
                if (category.getCategoryType().equals("AdHocAccess") &&
                        (!category.getStudentsOnly() || user.getRole().equals(roleStudent)) &&
                        !category.getRooms().isEmpty()) {
                    rooms.add(category.getRooms().get(0));
                }
                //Guest groups can have adHocRoom too
                if (user.getRole().equals("guest") && category.getCategoryType().equals("guestUsers")) {
                    if (!category.getRooms().isEmpty()) {
                        if (category.getRooms().get(0).getRoomType().equals("AdHocAccess")) {
                            rooms.add(category.getRooms().get(0));
                        }
                    }
                }
            }
        }
        for (Room room : rooms) {
            room.convertNmToCount();
        }
        return rooms;
    }

    /**
     * Return a list the rooms in which the session user can register devices
     *
     * @return For super user all rooms will be returned
     * For normal user the list his AdHocAccess rooms of those of his groups
     */
    public List<Room> getAllToRegister() {
        List<Room> rooms = new ArrayList<Room>();
        try {
            if (this.isSuperuser() || this.session.getAcls().contains("device.add")) {
                logger.debug("Is superuser" + this.session.getUser().getUid());
                Query query = this.em.createNamedQuery("Room.findAllToRegister");
                rooms = query.getResultList();
            } else {
                rooms = this.getRoomToRegisterForUser(this.session.getUser());
            }
        } catch (Exception e) {
            logger.error("getAllToRegister: " + e.getMessage());
        }
        for (Room room : rooms) {
            room.convertNmToCount();
        }
        rooms.sort(Comparator.comparing(Room::getName));
        return rooms;
    }

    /**
     * Search devices given by a substring
     *
     * @param search The string which will be searched
     * @return The list of the devices have been found
     */
    public List<Room> search(String search) {
        try {
            Query query = this.em.createNamedQuery("Device.search");
            query.setParameter("search", search + "%");
            return (List<Room>) query.getResultList();
        } catch (Exception e) {
            logger.error("search: " + e.getMessage());
            return null;
        }
    }

    public CrxResponse add(Room room) {
        if (room.getRoomType().equals("smartRoom")) {
            return new CrxResponse("ERROR", "Smart Rooms can only be created by Education Service.");
        }
        HWConf hwconf = new HWConf();
        CloneToolService cloneToolService = new CloneToolService(this.session, this.em);
        HWConf firstFatClient = cloneToolService.getByType("FatClient").get(0);
        logger.debug("First HWConf:" + firstFatClient.toString());

        //Check parameters
        StringBuilder errorMessage = new StringBuilder();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        for (ConstraintViolation<Room> violation : factory.getValidator().validate(room)) {
            errorMessage.append(violation.getMessage()).append(getNl());
        }
        if (errorMessage.length() > 0) {
            return new CrxResponse("ERROR", errorMessage.toString());
        }

        // First we check if the parameter are unique.
        if (!this.isNameUnique(room.getName())) {
            return new CrxResponse("ERROR", "Room name is not unique.");
        }
        if (room.getDescription() != null && !room.getDescription().isEmpty() && !this.isDescriptionUnique(room.getDescription())) {
            return new CrxResponse("ERROR", "Room description is not unique.");
        }
        //Only adHocRooms name can be longer then 10 char
        if (!room.isIgnoreNetbios() && !room.getRoomType().equals("AdHocAccess") && room.getName().length() > 10) {
            return new CrxResponse("ERROR", "Room name is to long.");
        }
        //If no devCount was set we calculate the net mask
        if (room.getDevCount() != null) {
            room.convertCountToNm();
        }
        // If no network was configured we will use net school network.
        if (room.getNetwork() == null || room.getNetwork().isEmpty()) {
            room.setNetwork(this.getConfigValue("NETWORK") + "/" + this.getConfigValue("NETMASK"));
        }
        logger.debug("Add Room:" + room);

        // If the starIp is not given we have to search the next room IP
        if (room.getStartIP() == null || room.getStartIP().isEmpty()) {
            String nextRoomIP = getNextRoomIP(room.getNetwork(), room.getNetMask());
            if (nextRoomIP.isEmpty()) {
                return new CrxResponse("ERROR", "The room can not be created. There is not enough IP-Adresses for its size.");
            }
            room.setStartIP(nextRoomIP);
        }

        //	Set default control mode
        if (room.getRoomControl() == null || room.getRoomControl().isEmpty()) {
            room.setRoomControl("inRoom");
        }
        // Check HWConf
        hwconf = cloneToolService.getById(room.getHwconfId());
        if (hwconf == null) {
            if (room.getHwconf() != null) {
                hwconf = room.getHwconf();
            } else {
                hwconf = firstFatClient;
            }
        }
        room.setHwconf(hwconf);
        logger.debug("User creating Room:" + this.session.getUser() + session.getUser());
        room.setCreator(this.session.getUser());
        hwconf.getRooms().add(room);
        if (room.getRoomControl() != null && !room.getRoomControl().equals("no")) {
            new AccessInRoom(room, !this.getConfigValue("INTERNET_FILTER").equals("proxy"));
        }
        try {
            logger.debug("Create Room:" + room);
            this.em.getTransaction().begin();
            this.em.persist(room);
            this.em.merge(hwconf);
            this.em.getTransaction().commit();
            if (!room.getAccessInRooms().isEmpty()) {
                room.getAccessInRooms().get(0).setRoom((room));
            }
        } catch (Exception e) {
            logger.error("Error by creating Room:" + e.getMessage());
            return new CrxResponse("ERROR", e.getMessage());
        }
        startPlugin("add_room", room);
        return new CrxResponse("OK", "Room was created successfully.", room.getId());
    }

    public CrxResponse delete(long roomId, boolean atomic ) {
        Room room = this.getById(roomId);
        if (room == null) {
            return new CrxResponse("ERROR", "Can not find room with id %s.", null, String.valueOf(roomId));
        }
        if (!this.mayModify(room)) {
            return new CrxResponse("ERROR", "You must not delete this room.");
        }
        DeviceService devService = new DeviceService(this.session, this.em);
        if (this.isProtected(room)) {
            return new CrxResponse("ERROR", "This room must not be deleted.");
        }
        List<Device> toDelete = new ArrayList<Device>();
        for (Device device : room.getDevices()) {
            toDelete.add(device);
        }
        for (Device device : toDelete) {
            logger.debug("Deleting " + device.getName());
            devService.delete(device, false);
        }
        //The categories connected to a room must handled too
        List<Category> categoriesToModify = new ArrayList<Category>();
        for (Category category : room.getCategories()) {
            if (category.getCategoryType().equals("smartRoom")) {
                new CategoryService(this.session, this.em).delete(category);
            } else {
                categoriesToModify.add(category);
            }
        }
        try {
            this.em.getTransaction().begin();
            room = this.em.find(Room.class, roomId);
            for (Category category : categoriesToModify) {
                if (category.getCategoryType().equals("AdHocAccess")) {
                    for (Object o : category.getFaqs()) {
                        this.em.remove(o);
                    }
                    for (Object o : category.getAnnouncements()) {
                        this.em.remove(o);
                    }
                    for (Object o : category.getContacts()) {
                        this.em.remove(o);
                    }
                    this.em.remove(category);
                } else {
                    category.getRooms().remove(room);
                    this.em.merge(category);
                }
            }
            this.deletAllConfigs(room);
            this.em.remove(room);
            this.em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("delete: " + e.getMessage());
            return new CrxResponse("ERROR", e.getMessage());
        }
        if( atomic ) {
            new DHCPConfig(session, em).Create();
            new SoftwareService(this.session, this.em).rewriteTopSls();
        }
        startPlugin("delete_room", room);
        return new CrxResponse("OK", "Room was removed successfully.");
    }


    /**
     * Get the list of all available IP-addresses from a room
     *
     * @param roomId The room id
     * @return The list of the IP-addresses in human readable form as List<String>
     */
    public List<String> getAvailableIPAddresses(long roomId) {
        Room room = this.getById(roomId);
        IPv4Net net = new IPv4Net(room.getStartIP() + "/" + room.getNetMask());
        List<String> allIPs = net.getAvailableIPs(0);
        List<String> usedIPs = new ArrayList<String>();
        IPv4Net subNetwork = null;
        for (String subnet : this.getEnumerates("network")) {
            subNetwork = new IPv4Net(subnet);
            if (subNetwork.contains(room.getStartIP())) {
                break;
            }
        }
        if (subNetwork != null) {
            usedIPs.add(subNetwork.getBase());
            usedIPs.add(subNetwork.getLast());
            for (Device dev : room.getDevices()) {
                usedIPs.add(dev.getIp());
                if (dev.getWlanIp() != "") {
                    usedIPs.add(dev.getWlanIp());
                }
            }
            allIPs.removeAll(usedIPs);
        }
        return allIPs;
    }

    public List<String> getAvailableIPAddresses(long roomId, long count) {
        Room room = this.getById(roomId);
        return this.getAvailableIPAddresses(room, count);
    }

    /**
     * Get the list of "count" available IP-addresses from a room with the corresponding default name.
     *
     * @param room  The room
     * @param count The count of the free IP-addresses of interest. If count == 0 all free IP-addresses will be delivered
     * @return The list of the IP-addresses and predefined host names as List<String>
     */
    public List<String> getAvailableIPAddresses(Room room, long count) {
        logger.debug("getAvailableIPAddresses: Room:" + room + " RoomId:" + room.getId());
        List<String> availableIPs = new ArrayList<String>();
        IPv4Net net = new IPv4Net(room.getStartIP() + "/" + room.getNetMask());
        IPv4Net subNetwork = null;
        for (String subnet : this.getEnumerates("network")) {
            subNetwork = new IPv4Net(subnet);
            if (subNetwork.contains(room.getStartIP())) {
                break;
            }
        }
        if (subNetwork != null) {
            String firstIP = subNetwork.getBase();
            String lastIP = subNetwork.getLast();
            int i = 0;
            for (String IP : net.getAvailableIPs(0)) {
                if (IP.equals(lastIP) || IP.equals(firstIP)) {
                    continue;
                }
                String name = this.isIPUnique(IP);
                if (name.isEmpty()) {
                    availableIPs.add(String.format("%s %s-pc%02d", IP, room.getName().replace("_", "-").toLowerCase(), i));
                }
                if (count > 0 && availableIPs.size() == count) {
                    break;
                }
                i++;
            }
        }
        return availableIPs;
    }

    /*
     * Delivers the next room IP address in a given subnet with the given netmask.
     * If the subnet is "" the default school network is meant.
     */

    /**
     * Delivers the next room IP address in a given subnet with the given netmask.
     *
     * @param subnet      The subnet in which we need the new room. If the subnet is empty use the default network this.getConfigValue("NETWORK") + "/" + this.getConfigValue("NETMASK)
     * @param roomNetMask The network mask of the new room. This determines how much devices can be registered in this room.
     * @return The start IP address which found. If there is no more free place in the network, an empty string will be returned.
     * @throws NumberFormatException
     */
    public String getNextRoomIP(String subnet, int roomNetMask) throws NumberFormatException {
        if (subnet == null || subnet.isEmpty()) {
            subnet = this.getConfigValue("NETWORK") + "/" + this.getConfigValue("NETMASK");
        }
        IPv4Net subNetwork = new IPv4Net(subnet);
        if (roomNetMask < subNetwork.getNetmaskNumeric()) {
            throw new NumberFormatException("The network netmask must be less then the room netmask:" + roomNetMask + ">" + subNetwork.getNetmaskNumeric());
        }
        Query query = this.em.createNamedQuery("Room.findAll");
        List<Room> rooms = (List<Room>) query.getResultList();
        String nextNet = subNetwork.getBase();

        if (subNetwork.contains(this.getConfigValue("FIRST_ROOM_NET"))) {
            nextNet = this.getConfigValue("FIRST_ROOM_NET");
        }

        boolean used = true;
        IPv4Net net = new IPv4Net(nextNet + "/" + roomNetMask);
        logger.debug("getNextRoomIP subnetworkBase:" + subNetwork.getBase() +
                " networkBase: " + net.getBase() +
                " roomNetMask: " + roomNetMask
        );
        if (net.getBase().equals(subNetwork.getBase())) {
            nextNet = net.getNext();
            net = new IPv4Net(nextNet + "/" + roomNetMask);
        }
        String lastIp = net.getBroadcastAddress();

        while (used) {
            used = false;
            logger.debug("getNextRoomIP nextNet:" + nextNet + " lastIp:" + lastIp);
            for (Room room : rooms) {
                logger.debug("  Room:" + room.getStartIP() + "/" + room.getNetMask());
                IPv4Net roomNet = new IPv4Net(room.getStartIP() + "/" + room.getNetMask());
                if (roomNet.contains(nextNet) || roomNet.contains(lastIp)) {
                    nextNet = net.getNext();
                    net = new IPv4Net(nextNet + "/" + roomNetMask);
                    lastIp = net.getBroadcastAddress();
                    used = true;
                    break;
                }
            }
            if (!subNetwork.contains(nextNet)) {
                return "";
            }
        }
        return nextNet;
    }

    /*
     * Returns a list of the users logged in in the room
     */
    public List<Map<String, String>> getLoggedInUsers(long roomId) {
        List<Map<String, String>> users = new ArrayList<>();
        Room room = this.getById(roomId);
        for (Device device : room.getDevices()) {
            for (User user : device.getLoggedIn()) {
                Map<String, String> userMap = new HashMap<>();
                userMap.put("device", device.getName());
                userMap.put("deviceId", String.valueOf(device.getId()));
                userMap.put("uid", user.getUid());
                userMap.put("userId", String.valueOf(user.getId()));
                userMap.put("surName", user.getSurName());
                userMap.put("givenName", user.getGivenName());
                users.add(userMap);
            }
        }
        return users;
    }

    /*
     * Returns the list of accesses in a room
     */
    public List<AccessInRoom> getAccessList(long roomId) {
        Room room = this.getById(roomId);
        if (room != null) {
            return room.getAccessInRooms();
        }
        return new ArrayList();
    }

    /*
     * Returns the default accesses in a room
     */
    public AccessInRoom getDefaultAccess(long roomId) {
        Room room = this.getById(roomId);
        if (room != null) {
            for (AccessInRoom accessInRoom : room.getAccessInRooms()) {
                if (accessInRoom.getAccessType().equals("DEF")) {
                    return accessInRoom;
                }
            }
        }
        return null;
    }


    /*
     * Sets the actual access status in a room
     */
    public void setAccessStatus(Room room, AccessInRoom access) {
        logger.debug("setAccessStatus Access: " + access + " Room " + room);
        if (room.getRoomControl() != null && room.getRoomControl().equals("no")) {
            return;
        }
        if (access.getAccessType() != null && access.getAccessType().equals("ACT")) {
            DeviceService dc = new DeviceService(this.session, this.em);
            for (Device device : room.getDevices()) {
                dc.manageDevice(device, access.getAction(), null);
            }
        } else {
            new AccessService().setAccessStatus(room, access, this.isAllowed("room.direct"));
        }
    }

    /*
     * Sets the actual access status in a room
     */
    public CrxResponse setAccessStatus(long roomId, AccessInRoom access) {
        Room room = this.getById(roomId);
        this.setAccessStatus(room, access);
        return new CrxResponse("OK", "Access state in %s was set successfully.", null, room.getName());
    }

    public CrxResponse setDefaultAccess(long roomId) {
        Room room = this.getById(roomId);
        new AccessService().setDefaultAccess(room);
        return new CrxResponse("OK", "Default access state in %s was set successfully.", null, room.getName());
    }

    public CrxResponse setDefaultAccess() {
        new AccessService().setDefaultAccess();
        return new CrxResponse("OK", "The default access was set in all rooms.");
    }

    /*
     * Sets the scheduled access status in all rooms
     */
    public CrxResponse setScheduledAccess() {
        Calendar rightNow = Calendar.getInstance();
        String actTime = String.format("%02d:%02d", rightNow.get(Calendar.HOUR_OF_DAY), rightNow.get(Calendar.MINUTE));
        int day = rightNow.get(Calendar.DAY_OF_WEEK);
        Query query = this.em.createNamedQuery("AccessInRoom.findActualAccesses");
        query.setParameter("time", actTime);
        logger.debug("setScheduledAccess: " + actTime + " Day: " + day);
        for (AccessInRoom access : (List<AccessInRoom>) query.getResultList()) {
            switch (day) {
                case 1:
                    if (!access.getSunday()) {
                        continue;
                    }
                    break;
                case 2:
                    if (!access.getMonday()) {
                        continue;
                    }
                    break;
                case 3:
                    if (!access.getTuesday()) {
                        continue;
                    }
                    break;
                case 4:
                    if (!access.getWednesday()) {
                        continue;
                    }
                    break;
                case 5:
                    if (!access.getThursday()) {
                        continue;
                    }
                    break;
                case 6:
                    if (!access.getFriday()) {
                        continue;
                    }
                    break;
                case 7:
                    if (!access.getSaturday()) {
                        continue;
                    }
                    break;
            }
            Room room = access.getRoom();
            this.setAccessStatus(room, access);
        }
        return new CrxResponse("OK", "Scheduled access states where set successfully.");
    }

    /*
     * Gets the actual access status in a room
     */
    public AccessInRoom getAccessStatus(Room room) {
        return new AccessService().getAccessStatus(room);
    }

    /*
     * Sets the actual access status in a room.
     * Room is given by roomId
     */
    public AccessInRoom getAccessStatus(long roomId) {
        Room room = this.getById(roomId);
        return this.getAccessStatus(room);
    }

    /**
     * Gets the actual access status in all rooms.
     * Room is given by roomId
     */
    public AccessInRoom[] getAccessStatus() {
        return new AccessService().getAccessStatus();
    }

    /*
     * Creates new devices in the room
     */
    public List<CrxResponse> addDevices(long roomId, List<Device> devices) {
        List<CrxResponse> responses = new ArrayList<>();
        Room room = this.getById(roomId);
        if(room == null){
            responses.add(new CrxResponse("ERROR","Can not find the room"));
            return responses;
        }
        boolean needWriteSalt = false;
        DeviceService deviceService = new DeviceService(this.session, this.em);
        for( Device device : devices) {
            device.setRoom(room);
            responses.add(deviceService.add(device,false));
            if(device.isFatClient()){
                needWriteSalt = true;
            }
        }
        if (needWriteSalt) {
            new SoftwareService(this.session, this.em).applySoftwareStateToHosts(devices);
        }
        new DHCPConfig(session, em).Create();
        return responses;
    }

    /*
     * Deletes devices in the room
     */
    public CrxResponse deleteDevices(long roomId, List<Long> deviceIDs) {
        Room room = this.em.find(Room.class, roomId);
        DeviceService deviceService = new DeviceService(this.session, this.em);
        boolean needWriteSalt = false;
        try {
            for (Long deviceId : deviceIDs) {
                Device device = this.em.find(Device.class, deviceId);
                if (room.getDevices().contains(device)) {
                    if (device.isFatClient()) {
                        needWriteSalt = true;
                    }
                    deviceService.delete(device, false);
                }
            }
            this.em.getEntityManagerFactory().getCache().evictAll();
        } catch (Exception e) {
            logger.error("deleteDevices" + e.getMessage());
            return new CrxResponse("ERROR", e.getMessage());
        }
        new DHCPConfig(session, em).Create();
        if (needWriteSalt) {
            new SoftwareService(this.session, this.em).rewriteTopSls();
        }
        return new CrxResponse("OK ", "Devices were deleted successfully.");
    }

    public HWConf getBYODHwconf() {
                Query query = this.em.createNamedQuery("HWConf.getByName");
                query.setParameter("name", "BYOD");
                return (HWConf) query.getResultList().get(0);
    }

    public List<Device> getDevices(long roomId) {
        return this.getById(roomId).getDevices();
    }

    public CrxResponse addDevice(long roomId, String macAddress, String name) {
        // First we check if there is enough IP-Addresses in this room
        List<String> ipAddress = this.getAvailableIPAddresses(roomId, 1);
        if (ipAddress.isEmpty()) {
            return new CrxResponse("ERROR", "There are no more free ip addresses in this room.");
        }
        logger.debug("IPAddr" + ipAddress);
        Device device = new Device();
        Room room  = this.em.find(Room.class, roomId);
        User owner = this.session.getUser();
        HWConf hwconf = room.getHwconf();
        logger.debug("DEVICE " + macAddress + " " + name);
        if (!owner.getRole().contains("sysadmins")) {
            //non sysadmin user want to register his workstation
            if (!this.getAllToRegister().contains(room)) {
                return new CrxResponse("ERROR", "You have no rights to register devices in this room.");
            }
            //Check if the count of the registered devices is lower then the allowed mount
            int ownedDevicesInRoom = 0;
            for (Device device1 : owner.getOwnedDevices()) {
                if (device1.getRoom().equals(room)) {
                    ownedDevicesInRoom++;
                }
            }
            if (ownedDevicesInRoom >= room.getPlaces()) {
                return new CrxResponse("ERROR", "You must not register more devices in this room.");
            }
            if (hwconf == null) {
                Query query = this.em.createNamedQuery("HWConf.getByName");
                query.setParameter("name", "BYOD");
                hwconf = this.getBYODHwconf();
            }
            device.setMac(macAddress);
            device.setName(owner.getUid().replaceAll("_", "-").replaceAll("\\.", "") + "-" + name.toLowerCase().trim());
            device.setIp(ipAddress.get(0).split(" ")[0]);
            device.setHwconf(hwconf);
            device.setCreator(owner);
        } else {
            device.setMac(macAddress);
            device.setIp(ipAddress.get(0).split(" ")[0]);
            if (name.equals("nextFreeName")) {
                device.setName(ipAddress.get(0).split(" ")[1]);
            } else {
                device.setName(name.toLowerCase().trim());
            }
            device.setHwconf(room.getHwconf());
            logger.debug("Sysadmin register:" + device.getMac() + "#" + device.getIp() + "#" + device.getName());
        }
        //Check if the Device settings are OK
        DeviceService deviceService = new DeviceService(this.session, this.em);
        logger.debug("DEVICE " + device);
        CrxResponse crxResponse = deviceService.check(device, room);
        if (crxResponse.getCode().equals("ERROR")) {
            return crxResponse;
        }
        device.setRoom(room);
        try {
            this.em.getTransaction().begin();
            this.em.persist(device);
            if (hwconf != null) {
                device.setHwconf(hwconf);
                this.em.merge(hwconf);
            }
            this.em.merge(room);
            if (!owner.getRole().contains("sysadmins")) {
                owner.getOwnedDevices().add(device);
                this.em.merge(owner);
            }
            this.em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("addDevice: " + e.getMessage());
            return new CrxResponse("ERROR", "Error by registering: " + e.getMessage());
        }
        //Start plugin and create DHCP and salt configuration
        startPlugin("add_device", device);
        new DHCPConfig(session, em).Create();
        return new CrxResponse("OK", "Device was created successfully.", device.getId());
    }

    public HWConf getHWConf(long roomId) {
        try {
            return this.em.find(Room.class, roomId).getHwconf();
        } catch (Exception e) {
            logger.error("getHWConf: " + e.getMessage());
            return null;
        }
    }

    public CrxResponse setHWConf(long roomId, long hwconfId) {
        try {
            this.em.getTransaction().begin();
            Room room = this.em.find(Room.class, roomId);
            room.setHwconf(em.find(HWConf.class, hwconfId));
            this.em.merge(room);
            this.em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("setHWConf: " + e.getMessage());
            return new CrxResponse("ERROR", e.getMessage());
        }
        return new CrxResponse("OK", "The hardware configuration of the room was set successfully.");
    }

    public CrxResponse modify(Room room) {
        Room oldRoom = this.getById(room.getId());
        HWConf hwconf = new CloneToolService(this.session, this.em).getById(room.getHwconfId());
        oldRoom.setDescription(room.getDescription());
        oldRoom.setHwconf(hwconf);
        oldRoom.setRoomType(room.getRoomType());
        oldRoom.setRows(room.getRows());
        oldRoom.setRoomControl(room.getRoomControl());
        oldRoom.setPlaces(room.getPlaces());
        try {
            this.em.getTransaction().begin();
            if (oldRoom.getRoomControl().equals("no")) {
                for (AccessInRoom o : oldRoom.getAccessInRooms()) {
                    this.em.remove(o);
                }
                oldRoom.setAccessInRoom(null);
            }
            this.em.merge(oldRoom);
            this.em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("modify: " + e.getMessage());
            return new CrxResponse("ERROR", e.getMessage());
        }
        startPlugin("modify_room", oldRoom);
        return new CrxResponse("OK", "The room was modified successfully.");
    }

    public List<Room> getRooms(List<Long> roomIds) {
        List<Room> rooms = new ArrayList<Room>();
        for (Long id : roomIds) {
            rooms.add(this.getById(id));
        }
        return rooms;
    }


    /*
     * Control of printer in this room
     */
    public void setDefaultPrinter(Room room, Printer printer) {
        if (room.getDefaultPrinter() != null && room.getDefaultPrinter().equals(printer)) {
            return;
        }
        room.setDefaultPrinter(printer);
        if( !printer.getDefaultInRooms().contains(room)) {
            printer.getDefaultInRooms().add(room);
        }
        try {
            this.em.getTransaction().begin();
            this.em.merge(room);
            this.em.merge(printer);
            this.em.getTransaction().commit();
            Map<String, String> tmpMap = new HashMap<>();
            tmpMap.put("name", printer.getName());
            tmpMap.put("action", "enable");
            tmpMap.put("network", room.getStartIP() +"/" +room.getNetMask() );
            startPlugin("manage_printer_queue", createLiteralJson(tmpMap));
        } catch (Exception e) {
            logger.error("setDefaultPrinter: " + e.getMessage());
        }
    }

    public void setDefaultPrinter(Room room, Long deviceId) {
        Printer printer = new PrinterService(this.session, this.em).getById(deviceId);
        if( printer != null ) {
            this.setDefaultPrinter(room, printer);
        }
    }

    public void deleteDefaultPrinter(Room room) {
        Printer printer = room.getDefaultPrinter();
        if (printer != null) {
            room.setDefaultPrinter(null);
            printer.getDefaultInRooms().remove(room);
            try {
                this.em.getTransaction().begin();
                this.em.merge(room);
                this.em.merge(printer);
                this.em.getTransaction().commit();
				Map<String, String> tmpMap = new HashMap<>();
				tmpMap.put("name", printer.getName());
				tmpMap.put("action", "disable");
				tmpMap.put("network", room.getStartIP() +"/" +room.getNetMask() );
				startPlugin("manage_printer_queue", createLiteralJson(tmpMap));
            } catch (Exception e) {
                logger.error("deleteDefaultPrinter: " + e.getMessage());
            }
        }
        return;
    }

    public void addAvailablePrinter(long roomId, long printerId) {
        try {
            Printer printer = this.em.find(Printer.class, printerId);
            Room room = this.em.find(Room.class, roomId);
            if (room.getAvailablePrinters().contains(printer)) {
                return;
            }
            room.getAvailablePrinters().add(printer);
            printer.getAvailableInRooms().add(room);
            this.em.getTransaction().begin();
            this.em.merge(room);
            this.em.merge(printer);
            this.em.getTransaction().commit();
            Map<String, String> tmpMap = new HashMap<>();
            tmpMap.put("name", printer.getName());
            tmpMap.put("action", "enable");
            tmpMap.put("network", room.getStartIP() +"/" +room.getNetMask() );
            startPlugin("manage_printer_queue", createLiteralJson(tmpMap));
        } catch (Exception e) {
            logger.error("addAvailablePrinters: " + e.getMessage());
        }
        return;
    }

    public void deleteAvailablePrinter(long roomId, long printerId) {
        try {
            Printer printer = this.em.find(Printer.class, printerId);
            Room room = this.em.find(Room.class, roomId);
            if (room == null || printer == null) {
                return;
            }
            this.em.getTransaction().begin();
            room.getAvailablePrinters().remove(printer);
            printer.getAvailableInRooms().remove(room);
            this.em.merge(room);
            this.em.merge(printer);
            this.em.getTransaction().commit();
            Map<String, String> tmpMap = new HashMap<>();
            tmpMap.put("name", printer.getName());
            tmpMap.put("action", "disable");
            tmpMap.put("network", room.getStartIP() +"/" +room.getNetMask() );
            startPlugin("manage_printer_queue", createLiteralJson(tmpMap));
        } catch (Exception e) {
            logger.error("deleteAvailablePrinter: " + e.getMessage());
        }
        return;
    }

    public CrxResponse setPrinters(Long roomId, Map<String, List<Long>> printers) {
        Room room = this.getById(roomId);
        List<Long> toAdd = new ArrayList<Long>();
        List<Long> toRemove = new ArrayList<Long>();
        this.deleteDefaultPrinter(room);
        if (!printers.get("defaultPrinter").isEmpty()) {
            this.setDefaultPrinter(room, printers.get("defaultPrinter").get(0));
        }
        try {
            for (Printer printer : room.getAvailablePrinters()) {
                if (!printers.get("availablePrinters").contains(printer.getId())) {
                    toRemove.add(printer.getId());
                }
            }
            for (Long printerId : printers.get("availablePrinters")) {
                boolean found = false;
                for (Printer printer : room.getAvailablePrinters()) {
                    if (printer.getId().equals(printerId)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    toAdd.add(printerId);
                }
            }
            for (Long printerId : toRemove) {
                this.deleteAvailablePrinter(roomId, printerId);
            }
            for (Long printerId : toAdd) {
                this.addAvailablePrinter(roomId, printerId);
            }
        } catch (Exception e) {
            logger.debug("setPrinters " + e.getMessage());
        }
        return new CrxResponse("OK", "Printers of the room was set.");
    }

    public CrxResponse manageRoom(long roomId, String action, Map<String, String> actionContent) {
        CrxResponse crxResponse = null;
        List<String> errors = new ArrayList<String>();
        DeviceService dc = new DeviceService(this.session, this.em);
        for (Long deviceId : getDeviceIds(this.getById(roomId))) {
            Device device = dc.getById(deviceId);
            //Do not control the own workstation
            if (this.session.getDevice() != null && this.session.getDevice().getId().equals(device.getId())) {
                continue;
            }
            crxResponse = dc.manageDevice(device.getId(), action, actionContent);
            if (crxResponse.getCode().equals("ERROR")) {
                errors.add(crxResponse.getValue());
            }
        }
        if (errors.isEmpty()) {
            new CrxResponse("OK", "Room control was applied.");
        } else {
            return new CrxResponse("ERROR", String.join("<br>", errors));
        }
        return null;
    }

    public CrxResponse organizeRoom(long roomId) {
        Room room = this.getById(roomId);
        if (room.getRoomType().equals("smartRoom")) {
            return new CrxResponse("OK", "Smart room can not get reorganized");
        }
        boolean changed = false;
        List<Integer> coordinates;
        if (room.getRoomType().equals("AdHocAccess")) {
            room.setPlaces(4);
            room.setRows(4);

        }
        int availablePlaces = room.getPlaces() * room.getRows();
        int workstationCount = room.getDevices().size();
        while (workstationCount > availablePlaces) {
            room.setPlaces(room.getPlaces() + 1);
            room.setRows(room.getRows() + 1);
            availablePlaces = room.getPlaces() * room.getRows();
            changed = true;
        }
        if (changed && !room.getRoomType().equals("AdHocAccess")) {
            try {
                this.em.getTransaction().begin();
                this.em.merge(room);
                this.em.getTransaction().commit();
            } catch (Exception e) {
                logger.error("organizeRoom 1: " + e.getMessage());
                return new CrxResponse("ERROR", e.getMessage());
            }
        }
        for (Device device : room.getDevices()) {
            changed = false;
            if (device.getRow() == 0) {
                device.setRow(1);
                changed = true;
            }
            if (device.getPlace() == 0) {
                device.setPlace(1);
                changed = true;
            }
            if (changed || this.getDevicesOnMyPlace(room, device).size() > 1) {
                coordinates = this.getNextFreePlace(room);
                device.setRow(coordinates.get(0));
                device.setPlace(coordinates.get(1));
                try {
                    this.em.getTransaction().begin();
                    this.em.merge(device);
                    this.em.getTransaction().commit();
                } catch (Exception e) {
                    logger.error("organizeRoom 2: " + e.getMessage());
                    return new CrxResponse("ERROR", e.getMessage());
                }
            }
        }
        return new CrxResponse("OK", "Room was reorganized");
    }

    public List<Device> getDevicesOnMyPlace(Room room, Device device) {
        return this.getDevicesByCoordinates(room, device.getRow(), device.getPlace());
    }

    public List<Device> getDevicesByCoordinates(Room room, int row, int place) {
        List<Device> devices = new ArrayList<Device>();
        for (Device device : room.getDevices()) {
            if (device.getRow() == row && device.getPlace() == place) {
                devices.add(device);
            }
        }
        return devices;
    }

    public List<Integer> getNextFreePlace(Room room) {
        List<Integer> coordinates = new ArrayList<Integer>();
        int row = 1;
        int place = 1;
        while (this.getDevicesByCoordinates(room, row, place).size() > 0) {
            if (place < room.getPlaces()) {
                place++;
            } else {
                place = 1;
                row++;
            }
        }
        coordinates.add(row);
        coordinates.add(place);
        return coordinates;
    }

    /*
     * Sets the list of accesses in a room
     */
    public CrxResponse setAccessList(long roomId, List<AccessInRoom> AccessList) {
        Room room = this.getById(roomId);
        try {
            this.em.getTransaction().begin();
            for (AccessInRoom air : room.getAccessInRooms()) {
                room.removeAccessInRoome(air);
            }
            for (AccessInRoom air : AccessList) {
                air.correctTime();
                air.setRoom(room);
                room.addAccessInRoom(air);
            }
            this.em.merge(room);
            this.em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("setAccessList: " + e.getMessage());
            return new CrxResponse("ERROR", e.getMessage());
        }
        return new CrxResponse("OK", "Access was created successfully");
    }

    public CrxResponse addAccessList(long roomId, AccessInRoom accessList) {
        try {
            Room room = this.em.find(Room.class, roomId);
            if (room.getRoomControl() != null && room.getRoomControl().equals("no")) {
                return new CrxResponse("ERROR", "You must not set access control in a room with no room control.");
            }
            this.em.getTransaction().begin();
            accessList.correctTime();
            accessList.setRoom(room);
            accessList.setCreator(this.session.getUser());
            this.em.persist(accessList);
            room.getAccessInRooms().add(accessList);
            this.em.merge(room);
            this.em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("addAccessList: " + e.getMessage());
            return new CrxResponse("ERROR", e.getMessage());
        }
        return new CrxResponse("OK", "Access was created successfully");
    }

    public CrxResponse deleteAccessList(long accessInRoomId) {
        try {
            AccessInRoom accessList = this.em.find(AccessInRoom.class, accessInRoomId);
            if (!this.mayModify(accessList)) {
                return new CrxResponse("ERROR", "You must not delete this accessList.");
            }
            Room room = accessList.getRoom();
            this.em.getTransaction().begin();
            room.getAccessInRooms().remove(accessList);
            this.em.remove(accessList);
            this.em.merge(room);
            this.em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("deleteAccessList: " + e.getMessage());
            return new CrxResponse("ERROR", e.getMessage());
        }
        return new CrxResponse("OK", "Access was deleted successfully");
    }

    public CrxResponse importRooms(InputStream fileInputStream, FormDataContentDisposition contentDispositionHeader) {
        File file = null;
        List<String> importFile;
        try {
            file = File.createTempFile("crx_uploadFile", ".crxb", new File(cranixTmpDir));
            Files.copy(fileInputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            importFile = Files.readAllLines(file.toPath());
        } catch (IOException e) {
            logger.error("File error:" + e.getMessage(), e);
            return new CrxResponse("ERROR", e.getMessage());
        }
        CloneToolService cloneToolService = new CloneToolService(this.session, this.em);
	HWConf defHwconf = cloneToolService.getById(4l);
        Map<String, Integer> header = new HashMap<>();
        CrxResponse crxResponse;
        String headerLine = importFile.get(0);
        Integer i = 0;
        for (String field : headerLine.split(";")) {
            header.put(field.toLowerCase(), i);
            i++;
        }
        if (!header.containsKey("name") || !header.containsKey("hwconf")) {
            return new CrxResponse("ERROR", "Fields name and hwconf are mandatory.");
        }
        i = 1;
        for (String line : importFile.subList(1, importFile.size())) {
            String[] values = line.split(";");
            Room room = new Room();
            if (values.length != header.size()) {
                logger.error("Value count mismatch in room import file in line:" + i);
                continue;
            }
            i++;
            if (header.containsKey("name")) {
                room.setName(values[header.get("name")]);
            }
            if (header.containsKey("description") && !values[header.get("description")].isEmpty()) {
                room.setDescription(values[header.get("description")]);
            } else {
                room.setDescription(values[header.get("name")]);
            }
            if (header.containsKey("count") && !values[header.get("count")].isEmpty()) {
                int count = Integer.valueOf(values[header.get("count")]);
                if (!Room.countToNm.containsKey(count)) {
                    return new CrxResponse("ERROR", "Bad computer count. Allowed values are 4,8,16,32,64,128.256,512,1024,2048,4096");
                }
                room.setNetMask(Room.countToNm.get(count));
            }
            if (header.containsKey("rows") && !values[header.get("rows")].isEmpty()) {
                room.setRows(Integer.parseInt(values[header.get("rows")]));
            } else {
                room.setRows(nmToRowsPlaces.get(room.getNetMask()));
            }
            if (header.containsKey("places") && !values[header.get("places")].isEmpty()) {
                room.setPlaces(Integer.parseInt(values[header.get("places")]));
            } else {
                room.setPlaces(nmToRowsPlaces.get(room.getNetMask()));
            }
            if (header.containsKey("control") && !values[header.get("control")].isEmpty()) {
                if (!checkEnumerate("roomControl", values[header.get("control")])) {
                    room.setRoomControl("teachers");
                } else {
                    room.setRoomControl(values[header.get("control")]);
                }
            }
            if (header.containsKey("type") && !values[header.get("type")].isEmpty()) {
                room.setRoomType(values[header.get("type")]);
            }
            if (header.containsKey("network") && !values[header.get("network")].isEmpty()) {
                room.setNetwork(values[header.get("network")]);
            }
            if (header.containsKey("startip") && !values[header.get("startip")].isEmpty()) {
                room.setStartIP(values[header.get("startip")]);
            }
            if (header.containsKey("hwconf") && !values[header.get("hwconf")].isEmpty()) {
                HWConf hwconf = cloneToolService.getByName(values[header.get("hwconf")]);
                if (hwconf == null) {
                    room.setHwconf(defHwconf);
                } else {
                    room.setHwconf(hwconf);
                }
            }
            crxResponse = this.add(room);
            if (crxResponse.getCode().equals("ERROR")) {
                return crxResponse;
            }
        }
        return new CrxResponse("OK", "Rooms were imported successfully.");
    }

    public List<CrxMConfig> getDHCP(Long roomId) {
        List<CrxMConfig> dhcpParameters = new ArrayList<CrxMConfig>();
        Room room = this.getById(roomId);
        for (CrxMConfig config : this.getMConfigObjects(room, "dhcpStatements")) {
            dhcpParameters.add(config);
        }
        for (CrxMConfig config : this.getMConfigObjects(room, "dhcpOptions")) {
            dhcpParameters.add(config);
        }
        return dhcpParameters;
    }

    public CrxResponse addDHCP(Long roomId, CrxMConfig dhcpParameter) {
        if (!dhcpParameter.getKeyword().equals("dhcpStatements") && !dhcpParameter.getKeyword().equals("dhcpOptions")) {
            return new CrxResponse("ERROR", "Bad DHCP parameter.");
        }
        Room room = this.getById(roomId);
        CrxResponse crxResponse = this.addMConfig(room, dhcpParameter.getKeyword(), dhcpParameter.getValue());
        if (crxResponse.getCode().equals("ERROR")) {
            return crxResponse;
        }
        Long dhcpParameterId = crxResponse.getObjectId();
        crxResponse = new DHCPConfig(session, em).Test();
        if (crxResponse.getCode().equals("ERROR")) {
            this.deleteMConfig(null, dhcpParameterId);
            return crxResponse;
        }
        new DHCPConfig(session, em).Create();
        return new CrxResponse("OK", "DHCP Parameter was added successfully");
    }

    public CrxResponse deleteDHCP(Long roomId, Long parameterId) {
        Room room = this.getById(roomId);
        return this.deleteMConfig(room, parameterId);
    }

    public List<CrxResponse> applyAction(CrxActionMap actionMap) {
        List<CrxResponse> responses = new ArrayList<CrxResponse>();
        if (actionMap.getName().equals("delete")) {
            for (Long id : actionMap.getObjectIds()) {
                responses.add(this.delete(id,false));
            }
            new DHCPConfig(session, em).Create();
            new SoftwareService(session, em).rewriteTopSls();
        } else {
            for (Long id : actionMap.getObjectIds()) {
                responses.add(this.manageRoom(id, actionMap.getName(), null));
            }
        }
        return responses;
    }

    public List<AccessInRoom> getAccessList() {
        List<AccessInRoom> accesses = new ArrayList<>();
        try {
            Query query = em.createNamedQuery("AccessInRoom.findAll");
            for (AccessInRoom air : (List<AccessInRoom>) query.getResultList()) {
                logger.debug("AIR: " + air);
                if (air.getAccessType().equals("ACT")) {
                    air.setPrinting(null);
                    air.setLogin(null);
                    air.setPortal(null);
                    air.setProxy(null);
                    air.setDirect(null);
                }
                accesses.add(air);
            }
        } catch (Exception e) {
            logger.error("getAccessList: " + e.getMessage());
        }
        return accesses;
    }

    public CrxResponse modifyAccessInRoom(AccessInRoom accessInRoom) {
        Room room = this.getById(accessInRoom.getRoomId());
        accessInRoom.setRoom(room);
        try {
            this.em.getTransaction().begin();
            this.em.merge(accessInRoom);
            this.em.getTransaction().commit();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new CrxResponse("ERROR", e.getMessage());
        }
        return new CrxResponse("OK", "Access was modified successfully");
    }
}
