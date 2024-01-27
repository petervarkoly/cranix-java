/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved  */
package de.cranix.services;

import de.cranix.dao.*;
import de.cranix.helper.CrxSystemCmd;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.cranix.helper.CranixConstants.*;

public class EducationService extends UserService {

    Logger logger = LoggerFactory.getLogger(EducationService.class);

    public EducationService(Session session, EntityManager em) {
        super(session, em);
    }

    /*
     * Return the Category to a smart room
     */
    public Category getCategoryToRoom(Long roomId) {
        Room room;
        try {
            room = this.em.find(Room.class, roomId);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
        for (Category category : room.getCategories()) {
            if (room.getName().equals(category.getName()) && category.getCategoryType().equals("smartRoom")) {
                return category;
            }
        }
        return null;
    }

    public List<Room> getMySmartRooms() {
        List<Room> smartRooms = new ArrayList<Room>();
        for (Category category : new CategoryService(this.session, this.em).getByType("smartRoom")) {
            if (category.getCreator() != null && category.getCreator().equals(session.getUser())) {
                logger.debug("getMySamrtRooms" + category);
                if (category.getRooms() != null && category.getRooms().size() > 0) {
                    smartRooms.add(category.getRooms().get(0));
                }
            }
        }
        return smartRooms;
    }

    /**
     * Return the list of rooms in which a user may actually control the access.<br>
     * A superuser may control all rooms except of smartRooms of the teachers and rooms with no control. Normal teachers may control:
     * <li>the room in which he is actual logged in if this room may be controlled.
     * <li>rooms with allTeachers control.
     * <li>rooms with teachers control if he is in the list of the controller.
     * <li>the own smartRooms
     *
     * @return The list of the found rooms.
     */
    public List<Room> getMyRooms() {
        List<Room> rooms = new ArrayList<Room>();
        if (this.session.getRoom() == null ||
                this.session.getRoom().getRoomControl().equals("no") ||
                this.getProperty("de.cranix.dao.Education.Rooms.mayControlFromInRoom").equals("yes")
        ) {
            for (Room room : new RoomService(this.session, this.em).getAllToUse()) {
                switch (room.getRoomControl()) {
                    case "no":
                        break;
                    case "inRoom":
                        if ( this.getProperty("de.cranix.dao.Education.Rooms.mayControlInRoom").equals("yes") ||
                                room.equals(this.session.getRoom())
                        ) {
                            rooms.add(room);
                        }
                        break;
                    case "allTeachers":
                        rooms.add(room);
                        break;
                    case "teachers":
                        if (this.checkMConfig(room, "teachers", Long.toString((this.session.getUserId())))) {
                            rooms.add(room);
                        }
                }
            }
        } else {
            rooms.add(this.session.getRoom());
            if (this.session.getRoom().getRoomControl().equals("inRoom")) {
                return rooms;
            }
        }
        rooms.addAll(this.getMySmartRooms());
        return rooms;
    }

    /*
     * Create the a new smart room from a hash:
     * {
     *     "name"  : <Smart room name>,
     *     "description : <Descripton of the room>,
     *     "studentsOnly : true/false
     * }
     */
    public CrxResponse createSmartRoom(Category smartRoom) {
        User owner = this.session.getUser();
        /* Define the room */
        Room room = new Room();
        room.setName(smartRoom.getName());
        room.setDescription(smartRoom.getDescription());
        room.setRoomType("smartRoom");
        room.setRows(7);
        room.setPlaces(7);
        room.setCreator(owner);
        room.getCategories().add(smartRoom);
        smartRoom.setRooms(new ArrayList<Room>());
        smartRoom.getRooms().add(room);
        smartRoom.setCreator(owner);
        smartRoom.setCategoryType("smartRoom");
        owner.getCategories().add(smartRoom);

        try {
            this.em.getTransaction().begin();
            this.em.persist(room);
            this.em.persist(smartRoom);
            this.em.merge(owner);
            this.em.getTransaction().commit();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new CrxResponse(this.getSession(), "ERROR", e.getMessage());
        }
        try {
            this.em.getTransaction().begin();
            /*
             * Add groups to the smart room
             */
            GroupService groupService = new GroupService(this.session, this.em);
            for (Long id : smartRoom.getGroupIds()) {
                Group group = groupService.getById(id);
                smartRoom.getGroups().add(group);
                group.getCategories().add(smartRoom);
                this.em.merge(room);
                this.em.merge(smartRoom);
            }
            /*
             * Add users to the smart room
             */
            for (Long id : smartRoom.getUserIds()) {
                User user = this.getById(Long.valueOf(id));
                if (smartRoom.getStudentsOnly() && !user.getRole().equals(roleStudent)) {
                    continue;
                }
                smartRoom.getUsers().add(user);
                user.getCategories().add(smartRoom);
                this.em.merge(user);
                this.em.merge(smartRoom);
            }
            /*
             * Add devices to the smart room
             */
            DeviceService deviceService = new DeviceService(this.session, this.em);
            for (Long id : smartRoom.getDeviceIds()) {
                Device device = deviceService.getById(Long.valueOf(id));
                smartRoom.getDevices().add(device);
                device.getCategories().add(smartRoom);
                this.em.merge(device);
                this.em.merge(smartRoom);
            }
            this.em.getTransaction().commit();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new CrxResponse(this.getSession(), "ERROR", e.getMessage());
        }
        return new CrxResponse(this.getSession(), "OK", "Smart Room was created succesfully.");
    }

    public CrxResponse modifySmartRoom(long roomId, Category smartRoom) {
        try {
            this.em.getTransaction().begin();
            Room room = smartRoom.getRooms().get(0);
            room.setName(smartRoom.getName());
            room.setDescription(smartRoom.getDescription());
            this.em.merge(smartRoom);
            this.em.merge(room);
            this.em.getTransaction().commit();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new CrxResponse(this.getSession(), "ERROR", e.getMessage());
        }
        return new CrxResponse(this.getSession(), "OK", "Smart Room was modified succesfully.");
    }

    public CrxResponse deleteSmartRoom(Long roomId) {
        try {
            this.em.getTransaction().begin();
            Room room = this.em.find(Room.class, roomId);
            for (Category category : room.getCategories()) {
                if (category.getCategoryType().equals("smartRoom") && category.getName().equals(room.getName())) {
                    User owner = category.getCreator();
                    if (owner != null) {
                        owner.getCategories().remove(category);
                        this.em.merge(owner);
                    }
                    this.em.remove(category);
                }
            }
            this.em.remove(room);
            this.em.getTransaction().commit();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new CrxResponse(this.getSession(), "ERROR", e.getMessage());
        }
        return new CrxResponse(this.getSession(), "OK", "Smart Room was deleted succesfully.");
    }


    /**
     * Get the list of users which are logged in a room or smart room
     * If a user of a smart room is not logged on the device id is 0L;
     *
     * @param roomId The id of the wanted room.
     * @return The id list of the logged on users: [ [ <userId>,<deviceId> ], [ <userId>, <deviceId] ... ]
     */
    public List<List<Long>> getRoom(long roomId) {
        List<List<Long>> loggedOns = new ArrayList<List<Long>>();
        List<Long> loggedOn;
        RoomService roomService = new RoomService(this.session, this.em);
        Room room = roomService.getById(roomId);
        User me = this.session.getUser();
        if (room.getRoomType().equals("smartRoom")) {
            Category category = room.getCategories().get(0);
            for (Group group : category.getGroups()) {
                for (User user : group.getUsers()) {
                    if (category.getStudentsOnly() && !user.getRole().equals(roleStudent) ||
                            user.equals(me)) {
                        continue;
                    }
                    if (user.getLoggedOn().isEmpty()) {
                        loggedOn = new ArrayList<Long>();
                        loggedOn.add(user.getId());
                        loggedOn.add(0L);
                        loggedOns.add(loggedOn);
                    } else {
                        for (Device device : user.getLoggedOn()) {
                            loggedOn = new ArrayList<Long>();
                            loggedOn.add(user.getId());
                            loggedOn.add(device.getId());
                            loggedOns.add(loggedOn);
                        }
                    }
                }
            }
            for (User user : category.getUsers()) {
                if (user.equals(me)) {
                    continue;
                }
                if (user.getLoggedOn().isEmpty()) {
                    loggedOn = new ArrayList<Long>();
                    loggedOn.add(user.getId());
                    loggedOn.add(0L);
                    loggedOns.add(loggedOn);
                } else {
                    for (Device device : user.getLoggedOn()) {
                        loggedOn = new ArrayList<Long>();
                        loggedOn.add(user.getId());
                        loggedOn.add(device.getId());
                        loggedOns.add(loggedOn);
                    }
                }
            }
            for (Device device : category.getDevices()) {
                /*
                 * If nobody is logged in set user id to 0L
                 */
                if (device.getLoggedIn().isEmpty()) {
                    loggedOn = new ArrayList<Long>();
                    loggedOn.add(0L);
                    loggedOn.add(device.getId());
                    loggedOns.add(loggedOn);
                } else {
                    for (User user : device.getLoggedIn()) {
                        if (!user.equals(me)) {
                            loggedOn = new ArrayList<Long>();
                            loggedOn.add(user.getId());
                            loggedOn.add(device.getId());
                            loggedOns.add(loggedOn);
                        }
                    }
                }
            }
        } else {
            for (Device device : room.getDevices()) {
                /*
                 * If nobody is logged in set user id to 0L
                 */
                if (device.getLoggedIn().isEmpty()) {
                    loggedOn = new ArrayList<Long>();
                    loggedOn.add(0L);
                    loggedOn.add(device.getId());
                    loggedOns.add(loggedOn);
                } else {
                    for (User user : device.getLoggedIn()) {
                        if (!user.equals(me)) {
                            loggedOn = new ArrayList<Long>();
                            loggedOn.add(user.getId());
                            loggedOn.add(device.getId());
                            loggedOns.add(loggedOn);
                        }
                    }
                }
            }
        }
        return loggedOns;
    }

    public List<CrxResponse> uploadFileToUsers(
        String sUserIds,
        Boolean cleanUp,
        InputStream fileInputStream,
        FormDataContentDisposition contentDispositionHeader) {
        List<Long> userIds = new ArrayList<Long>();
        for (String id : sUserIds.split(",")) {
            userIds.add(Long.valueOf(id));
        }
        logger.debug("uploadFileToUsers: " + sUserIds + " " + userIds + " cleanUp " + cleanUp);
        List<CrxResponse> resp = this.uploadFileTo("users", 0l, userIds, fileInputStream, contentDispositionHeader, false, cleanUp);
        return resp;
    }

    public List<CrxResponse> uploadFileToGroups(
        String groupIds,
        Boolean cleanUp,
        Boolean studentsOnly,
        InputStream fileInputStream,
        FormDataContentDisposition contentDispositionHeader) {
        List<CrxResponse> responses = new ArrayList<CrxResponse>();
	logger.debug("uploadFileToGroups:" + groupIds + " cleanUp:" + cleanUp + " studentsOnly:" + studentsOnly);
        for (String sgroupId : groupIds.split(",")) {
            Long groupId = Long.valueOf(sgroupId);
            if (groupId != null) {
                responses.addAll(this.uploadFileTo("group", groupId, null, fileInputStream, contentDispositionHeader, studentsOnly, cleanUp));
            }
        }
        return responses;
    }

    public List<CrxResponse> uploadFileToDevices(
        String objectIds,
        Boolean cleanUp,
        Boolean studentsOnly,
        InputStream fileInputStream,
        FormDataContentDisposition contentDispositionHeader) {
        List<CrxResponse> responses = new ArrayList<CrxResponse>();
        for (String sObjectId : objectIds.split(",")) {
            Long objectId = Long.valueOf(sObjectId);
            if (objectId != null) {
                responses.addAll(this.uploadFileTo("device", objectId, null, fileInputStream, contentDispositionHeader, studentsOnly, cleanUp));
            }
        }
        return responses;
    }

    public List<CrxResponse> uploadFileToRooms(
        String objectIds,
        Boolean cleanUp,
        Boolean studentsOnly,
        InputStream fileInputStream,
        FormDataContentDisposition contentDispositionHeader) {
        List<CrxResponse> responses = new ArrayList<CrxResponse>();
        for (String sObjectId : objectIds.split(",")) {
            Long objectId = Long.valueOf(sObjectId);
            if (objectId != null) {
                responses.addAll(this.uploadFileTo("room", objectId, null, fileInputStream, contentDispositionHeader, studentsOnly, cleanUp));
            }
        }
        return responses;
    }

    public List<CrxResponse> uploadFileTo(String what,
                                          Long objectId,
                                          List<Long> objectIds,
                                          InputStream fileInputStream,
                                          FormDataContentDisposition contentDispositionHeader,
                                          Boolean studentsOnly,
                                          Boolean cleanUp) {
        File file = null;
        List<CrxResponse> responses = new ArrayList<CrxResponse>();
        String fileName = "";
        fileName = new String(contentDispositionHeader.getFileName().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        try {
            file = File.createTempFile("crx_uploadFile", ".crxb", new File(cranixTmpDir));
            Files.copy(fileInputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            responses.add(new CrxResponse(this.getSession(), "ERROR", e.getMessage()));
            return responses;
        }
        switch (what) {
            case "users":
                for (Long id : objectIds) {
                    User user = this.getById(id);
                    if (user != null) {
                        responses.add(this.saveFileToUserImport(user, file, fileName, cleanUp));
                    } else {
                        responses.add(new CrxResponse(this.getSession(), "ERROR", "User with id %s does not exists.", null, id.toString()));
                    }
                }
                break;
            case "user":
                User user = this.getById(objectId);
                if (user != null) {
                    responses.add(this.saveFileToUserImport(user, file, fileName, cleanUp));
                } else {
                    responses.add(new CrxResponse(this.getSession(), "ERROR", "User with id %s does not exists.", null, objectId.toString()));
                }
                break;
            case "group":
                Group group = new GroupService(this.session, this.em).getById(objectId);
                if (group != null) {
                    for (User myUser : group.getUsers()) {
                        if (!this.session.getUser().equals(myUser) &&
                                (!studentsOnly || myUser.getRole().equals(roleStudent) || myUser.getRole().equals(roleGuest))) {
                            responses.add(this.saveFileToUserImport(myUser, file, fileName, cleanUp));
                        }
                    }
                } else {
                    responses.add(new CrxResponse(this.getSession(), "ERROR", "Group with id %s does not exists.", null, objectId.toString()));
                }
                break;
            case "device":
                Device device = new DeviceService(this.session, this.em).getById(objectId);
                if (device != null) {
                    for (User myUser : device.getLoggedIn()) {
                        responses.add(this.saveFileToUserImport(myUser, file, fileName, cleanUp));
                    }
                } else {
                    responses.add(new CrxResponse(this.getSession(), "ERROR", "Device with id %s does not exists.", null, objectId.toString()));
                }
                break;
            case "room":
                DeviceService deviceService = new DeviceService(this.session, this.em);
                for (List<Long> loggedOn : this.getRoom(objectId)) {
                    User myUser = this.getById(loggedOn.get(0));
                    if (myUser == null) {
                        //no user logged in we try the workstation user
                        Device dev = deviceService.getById(loggedOn.get(1));
                        if (dev != null) {
                            myUser = this.getByUid(dev.getName());
                        }
                    }
                    if (myUser != null) {
                        responses.add(this.saveFileToUserImport(myUser, file, fileName, cleanUp));
                    }
                }
        }
	if (!logger.isDebugEnabled()) {
            file.delete();
	}
        return responses;
    }

    public CrxResponse saveFileToUserImport(User user, File file, String fileName, Boolean cleanUp) {
        if (cleanUp == null) {
            cleanUp = true;
        }
        UserPrincipalLookupService lookupService = FileSystems.getDefault().getUserPrincipalLookupService();
        UserPrincipal owner;
        List<String> parameters = new ArrayList<String>();
        if (user == null) {
            return new CrxResponse(this.getSession(), "ERROR", "No user defined.");
        } else {
            logger.debug("saveFileToUserImport File " + fileName + " saved to " + user.getUid() + " cleanUp:" + cleanUp);
        }
        parameters.add(fileName);
        parameters.add(user.getUid());
        String homeDir = this.getHomeDir(user);
	String importDir = homeDir + "Import/";
	File importDirF;
	File newFile;
        try {
            owner = lookupService.lookupPrincipalByName(user.getUid());
            //For workstations users the whole home directory will be removed
            if (user.getRole().equals(roleWorkstation) && cleanUp) {
		logger.debug("saveFileToUserImport cleanUp workstations home dir");
                StringBuffer reply = new StringBuffer();
                StringBuffer error = new StringBuffer();
                String[] program = new String[3];
                program[0] = "/usr/bin/rm";
                program[1] = "-rf";
                program[2] = homeDir;
                CrxSystemCmd.exec(program, reply, error, null);
                File homeDirF = new File(homeDir);
                Files.createDirectories(homeDirF.toPath(), privatDirAttribute);
                Files.setOwner(homeDirF.toPath(), owner);
            } else if ((user.getRole().equals(roleStudent) || user.getRole().equals(roleGuest)) && cleanUp) {
                //For students and guest users only the Import directory will be removed
		logger.debug("saveFileToUserImport cleanUp students Import dir");
                StringBuffer reply = new StringBuffer();
                StringBuffer error = new StringBuffer();
                String[] program = new String[3];
                program[0] = "/usr/bin/rm";
                program[1] = "-rf";
                program[2] = homeDir + "/Import/";
                CrxSystemCmd.exec(program, reply, error, null);
		logger.debug("saveFileToUserImport error:" + error.toString());
            }
        } catch (Exception e) {
            logger.error("saveFileToUserImport 1.:" + e.getMessage());
            parameters.add(e.getMessage());
            return new CrxResponse(this.getSession(), "ERROR", "File %s could not be saved to user: %s: %s 1.", null, parameters);
        }
	try {
            // Create the directory first.
            importDirF = new File(importDir);
            Files.createDirectories(importDirF.toPath(), privatDirAttribute);
        } catch (Exception e) {
            logger.error("saveFileToUserImport 2.:" + e.getMessage());
            parameters.add(e.getMessage());
            return new CrxResponse(this.getSession(), "ERROR", "File %s could not be saved to user: %s: %s 2.", null, parameters);
        }

	try {
            // Copy temp file to the right place
            newFile = new File(importDir + fileName);
            Files.copy(file.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            logger.error("saveFileToUserImport 3.:" + e.getMessage());
            parameters.add(e.getMessage());
            return new CrxResponse(this.getSession(), "ERROR", "File %s could not be saved to user: %s: %s 3.", null, parameters);
        }

	try {
            // Set owner
            Files.setOwner(importDirF.toPath(), owner);
            Files.setOwner(newFile.toPath(), owner);
        } catch (Exception e) {
            logger.error("saveFileToUserImport 4.:" + e.getMessage());
            parameters.add(e.getMessage());
            return new CrxResponse(this.getSession(), "ERROR", "File %s could not be saved to user: %s: %s 4.", null, parameters);
        }

	try {
            //Clean up export of target
            String export = homeDir + "Export/";
            File exportDir = new File(export);
            Files.createDirectories(exportDir.toPath(), privatDirAttribute);
            Files.setOwner(exportDir.toPath(), owner);
            if (cleanUp && (user.getRole().equals(roleStudent) || user.getRole().equals(roleGuest))) {
                //Clean up Export by students and guests by workstations the home was deleted
                for (String mist : exportDir.list()) {
                    File f = new File(mist);
                    if (!f.isDirectory()) {
                        f.delete();
                    }
                }
            }
        } catch (Exception e) {
            logger.error("saveFileToUserImport 5.:" + e.getMessage());
            parameters.add(e.getMessage());
            return new CrxResponse(this.getSession(), "ERROR", "File %s could not be saved to user: %s: %s 5.", null, parameters);
        }
        return new CrxResponse(this.getSession(), "OK", "File '%s' successfully saved to user '%s'.", null, parameters);
    }


    public CrxResponse createGroup(Group group) {
        GroupService groupService = new GroupService(this.session, this.em);
        group.setGroupType("workgroup");
        group.setCreator(session.getUser());
        return groupService.add(group);
    }

    public CrxResponse modifyGroup(long groupId, Group group) {
        GroupService groupService = new GroupService(this.session, this.em);
        Group emGroup = groupService.getById(groupId);
        if (this.session.getUser().equals(emGroup.getCreator())) {
            return groupService.modify(group);
        } else {
            return new CrxResponse(this.getSession(), "ERROR", "You are not the owner of this group.");
        }
    }

    public CrxResponse deleteGroup(long groupId) {
        GroupService groupService = new GroupService(this.session, this.em);
        Group emGroup = groupService.getById(groupId);
        if (this.session.getUser().equals(emGroup.getCreator())) {
            return groupService.delete(groupId);
        } else {
            return new CrxResponse(this.getSession(), "ERROR", "You are not the owner of this group.");
        }
    }

    public List<String> getAvailableRoomActions(long roomId) {
        Room room = new RoomService(this.session, this.em).getById(roomId);
        List<String> actions = new ArrayList<String>();
        for (String action : this.getProperty("de.cranix.dao.EducationService.RoomActions").split(",")) {
            if (!this.checkMConfig(room, "disabledActions", action)) {
                actions.add(action);
            }
        }
        return actions;
    }

    public List<String> getAvailableUserActions(long userId) {
        User user = this.getById(userId);
        List<String> actions = new ArrayList<String>();
        for (String action : this.getProperty("de.cranix.dao.EducationService.UserActions").split(",")) {
            if (!this.checkMConfig(user, "disabledActions", action)) {
                actions.add(action);
            }
        }
        return actions;
    }

    public List<String> getAvailableDeviceActions(long deviceId) {
        Device device = new DeviceService(this.session, this.em).getById(deviceId);
        List<String> actions = new ArrayList<String>();
        for (String action : this.getProperty("de.cranix.dao.EducationService.DeviceActions").split(",")) {
            if (!this.checkMConfig(device, "disabledActions", action)) {
                actions.add(action);
            }
        }
        return actions;
    }


    public CrxResponse manageRoom(long roomId, String action, Map<String, String> actionContent) {
        CrxResponse crxResponse = null;
        List<String> errors = new ArrayList<String>();
        DeviceService dc = new DeviceService(this.session, this.em);

        /*
         * This is a very special action
         */
        if (action.startsWith("organize")) {
            return new RoomService(this.session, this.em).organizeRoom(roomId);
        }

        Room room = new RoomService(this.session, this.em).getById(roomId);
        if (action.equals("setPassword")) {
            StringBuffer reply = new StringBuffer();
            StringBuffer error = new StringBuffer();
            String[] program = new String[5];
            program[0] = "/usr/bin/samba-tool";
            program[1] = "domain";
            program[2] = "passwordsettings";
            program[3] = "set";
            program[4] = "--complexity=off";
            CrxSystemCmd.exec(program, reply, error, null);
            program[1] = "user";
            program[2] = "setpassword";
            program[4] = "--newpassword=" + actionContent.get("password");
            for (Device device : room.getDevices()) {
                error = new StringBuffer();
                reply = new StringBuffer();
                program[3] = device.getName();
                CrxSystemCmd.exec(program, reply, error, null);
                logger.debug(program[0] + " " + program[1] + " " + program[2] + " " + program[3] + " " + program[4] + " R:"
                        + reply.toString() + " E:" + error.toString());
            }
            if (this.getConfigValue("CHECK_PASSWORD_QUALITY").toLowerCase().equals("yes")) {
                program = new String[5];
                program[0] = "/usr/bin/samba-tool";
                program[1] = "domain";
                program[2] = "passwordsettings";
                program[3] = "set";
                program[4] = "--complexity=on";
                CrxSystemCmd.exec(program, reply, error, null);
            }
            return new CrxResponse(this.getSession(), "OK", "The password of the selected users was reseted.");
        }

        logger.debug("manageRoom called " + roomId + " action:");
        if (action.equals("download") && actionContent == null) {
            actionContent = new HashMap<String, String>();
            actionContent.put("projectName", this.nowString() + "." + room.getName());
        }
        for (List<Long> loggedOn : this.getRoom(roomId)) {
            logger.debug("manageRoom " + roomId + " user:" + loggedOn.get(0) + " device:" + loggedOn.get(1));
            //Do not control the own workstation
            if (this.session.getUser().getId().equals(loggedOn.get(0)) ||
                    (this.session.getDevice() != null && this.session.getDevice().getId().equals(loggedOn.get(1)))) {
                continue;
            }
            crxResponse = dc.manageDevice(loggedOn.get(1), action, actionContent);
            if (crxResponse.getCode().equals("ERROR")) {
                errors.add(crxResponse.getValue());
            }
        }
        if (errors.isEmpty()) {
            return new CrxResponse(this.getSession(), "OK", "Room control was applied.");
        } else {
            return new CrxResponse(this.getSession(), "ERROR", String.join("<br>", errors));
        }
    }

    public List<CrxResponse> manageDevices(CrxActionMap actionMap) {
        DeviceService deviceService = new DeviceService(this.session, this.em);
        List<CrxResponse> responses = new ArrayList<CrxResponse>();
        logger.debug("actionMap" + actionMap);
        if (actionMap.getName().equals("delete")) {
            responses.add(new CrxResponse(this.session, "ERROR", "You must not delete devices"));

        } else {
            for (Long id : actionMap.getObjectIds()) {
                responses.add(deviceService.manageDevice(id, actionMap.getName(), null));
            }
        }
        return responses;
    }

    public Long getRoomActualService(long roomId) {
        try {
            Query query = this.em.createNamedQuery("SmartControl.getAllActiveInRoom");
            query.setParameter("roomId", roomId);
            if (query.getResultList().isEmpty()) {
                //The room is free
                return null;
            }
            RoomSmartControl rsc = (RoomSmartControl) query.getResultList().get(0);
            return rsc.getCreator().getId();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    /**
     * getRoomControl
     *
     * @param roomId  The roomId which should be controlled
     * @param minutes How long do you want to control the room
     * @return An CrxResponse object
     */
    public CrxResponse getRoomControl(long roomId, long minutes) {

        //Create the list of the controllers
        StringBuilder controllers = new StringBuilder();
        controllers.append(this.getConfigValue("SERVER")).append(";").append(this.session.getDevice().getIp());
        if (this.session.getDevice().getWlanIp() != null) {
            controllers.append(";").append(this.session.getDevice().getWlanIp());
        }

        // Get the list of the devices
        DeviceService dc = new DeviceService(this.session, this.em);
        List<String> devices = new ArrayList<String>();
        String domain = "." + this.getConfigValue("DOMAIN");
        for (List<Long> loggedOn : this.getRoom(roomId)) {
            //Do not control the own workstation
            if (this.session.getDevice().getId().equals(loggedOn.get(1))) {
                continue;
            }
            devices.add(dc.getById(loggedOn.get(1)).getName() + domain);
        }
        String[] program = new String[5];
        StringBuffer reply = new StringBuffer();
        StringBuffer stderr = new StringBuffer();
        program[0] = "/usr/bin/salt";
        program[1] = "-L";
        program[2] = String.join(",", devices);
        program[3] = "crx_client.set_controller_ip";
        program[4] = controllers.toString();
        CrxSystemCmd.exec(program, reply, stderr, null);

        RoomSmartControl roomSmartControl = new RoomSmartControl(
		this.em.find(Room.class, roomId),
		this.session.getUser(), minutes
	);
        try {
            this.em.getTransaction().begin();
            this.em.persist(roomSmartControl);
            this.em.getTransaction().commit();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new CrxResponse(this.getSession(), "ERROR", e.getMessage());
        }
        return new CrxResponse(this.getSession(), "OK", "Now you have the control for the selected room.");
    }

    public List<CrxResponse> collectFileFromUsers(
            String userIds,
            String projectName,
            Boolean sortInDirs,
            Boolean cleanUpExport) {
        List<CrxResponse> responses = new ArrayList<CrxResponse>();
        UserService userService = new UserService(this.session, this.em);
        for (String id : userIds.split(",")) {
            User user = userService.getById(Long.valueOf(id));
            if (user != null) {
                responses.add(userService.collectFileFromUser(user, projectName, sortInDirs, cleanUpExport));
            }
        }
        return responses;
    }

    public List<CrxResponse> collectFileFromDevices(
            String deviceIds,
            String projectName,
            Boolean sortInDirs,
            Boolean cleanUpExport,
            Boolean studentsOnly
    ) {
        List<CrxResponse> responses = new ArrayList<CrxResponse>();
        UserService userService = new UserService(this.session, this.em);
        DeviceService deviceService = new DeviceService(this.session, this.em);
        for (String sDeviceId : deviceIds.split(",")) {
            Long deviceId = Long.valueOf(sDeviceId);
            Device device = deviceService.getById(deviceId);
            if (device.getLoggedIn() == null || device.getLoggedIn().isEmpty()) {
                User user = userService.getByUid(device.getName());
                responses.add(userService.collectFileFromUser(user, projectName, sortInDirs, cleanUpExport));
            } else {
                for (User user : device.getLoggedIn()) {
                    if (!studentsOnly || user.getRole().equals(roleStudent) || user.getRole().equals(roleGuest) || user.getRole().equals(roleWorkstation)) {
                        responses.add(userService.collectFileFromUser(user, projectName, sortInDirs, cleanUpExport));
                    }
                }
            }
        }
        return responses;
    }

    public List<CrxResponse> collectFileFromRooms(
            String roomIds,
            String projectName,
            Boolean sortInDirs,
            Boolean cleanUpExport,
            Boolean studentsOnly
    ) {
        UserService userService = new UserService(this.session, this.em);
        DeviceService deviceService = new DeviceService(this.session, this.em);
        List<CrxResponse> responses = new ArrayList<CrxResponse>();
        for (String sRoomId : roomIds.split(",")) {
            Long roomId = Long.valueOf(sRoomId);
            for (List<Long> logged : this.getRoom(roomId)) {
                User user = userService.getById(logged.get(0));
                Device device = deviceService.getById(logged.get(1));
                if (user == null) {
                    user = userService.getByUid(device.getName());
                }
                if (user != null) {
                    if (!studentsOnly || user.getRole().equals(roleStudent) || user.getRole().equals(roleGuest) || user.getRole().equals(roleWorkstation)) {
                        logger.debug("user" + user);
                        logger.debug("projectName" + projectName);
                        logger.debug("sortInDirs" + sortInDirs);
                        logger.debug("cleanUpExport" + cleanUpExport);
                        CrxResponse resp = userService.collectFileFromUser(user, projectName, sortInDirs, cleanUpExport);
                        logger.debug("response" + resp);
                        responses.add(resp);
                    }
                }
            }
        }
        return responses;
    }

    public List<CrxResponse> collectFileFromGroups(
            String groupIds,
            String projectName,
            Boolean sortInDirs,
            Boolean cleanUpExport,
            Boolean studentsOnly
    ) {
        UserService userService = new UserService(this.session, this.em);
        GroupService groupService = new GroupService(this.session, this.em);
        List<CrxResponse> responses = new ArrayList<CrxResponse>();
        for (String sGroupId : groupIds.split(",")) {
            Long groupId = Long.valueOf(sGroupId);
            Group group = new GroupService(this.session, this.em).getById(groupId);
            for (User user : group.getUsers()) {
                if (!studentsOnly || user.getRole().equals(roleStudent) || user.getRole().equals(roleGuest)) {
                    if (user.getRole().equals(roleTeacher)) {
                        responses.add(userService.collectFileFromUser(user, projectName, sortInDirs, false));
                    } else {
                        responses.add(userService.collectFileFromUser(user, projectName, sortInDirs, cleanUpExport));
                    }
                }
            }
        }
        return responses;
    }

    public List<Group> getMyGroups() {
        List<Group> groups = new ArrayList<Group>();
        for (Group group : this.session.getUser().getGroups()) {
            if (!group.getGroupType().equals("primary")) {
                groups.add(group);
            }
        }
        for (Group group : this.session.getUser().getOwnedGroups()) {
            if (!group.getGroupType().equals("primary") && !groups.contains(group)) {
                groups.add(group);
            }
        }
        for (Group group : new GroupService(this.session, this.em).getByType("class")) {
            if (!groups.contains(group)) {
                groups.add(group);
            }
        }
        return groups;
    }

    public List<User> getAvailableMembers(long groupId) {
        List<User> users = new ArrayList<User>();
        Group group = em.find(Group.class, groupId);
        if (group != null) {
            UserService uc = new UserService(this.session, this.em);
            users = uc.getByRole(roleStudent);
            users.addAll(uc.getByRole(roleTeacher));
            users.removeAll(group.getUsers());
        }
        return users;
    }

    public List<User> getMembers(long groupId) {
        List<User> users = new ArrayList<User>();
        Group group = em.find(Group.class, groupId);
        if (group != null) {
            Boolean myGroup = group.getCreator().equals(this.session.getUser());
            for (User user : group.getUsers()) {
                if (myGroup || user.getRole().equals(roleStudent) || user.getRole().equals(roleGuest)) {
                    users.add(user);
                }
            }
        }
        return users;
    }

    public List<CrxResponse> applyAction(CrxActionMap crxActionMap) {
        List<CrxResponse> responses = new ArrayList<CrxResponse>();
        UserService userService = new UserService(this.session, this.em);
        logger.debug(crxActionMap.toString());
        switch (crxActionMap.getName()) {
            case "setPassword":
                return userService.resetUserPassword(
                        crxActionMap.getObjectIds(),
                        crxActionMap.getStringValue(),
                        crxActionMap.isBooleanValue());
            case "setFilesystemQuota":
                return userService.setFsQuota(
                        crxActionMap.getObjectIds(),
                        crxActionMap.getLongValue());
            case "setMailsystemQuota":
                return userService.setMsQuota(
                        crxActionMap.getObjectIds(),
                        crxActionMap.getLongValue());
            case "disableLogin":
                return userService.disableLogin(
                        crxActionMap.getObjectIds(),
                        true);
            case "enableLogin":
                return userService.disableLogin(
                        crxActionMap.getObjectIds(),
                        false);
            case "disableInternet":
                return userService.disableInternet(
                        crxActionMap.getObjectIds(),
                        true);
            case "enableInternet":
                return userService.disableInternet(
                        crxActionMap.getObjectIds(),
                        false);
            case "mandatoryProfile":
                return userService.mandatoryProfile(
                        crxActionMap.getObjectIds(),
                        true);
            case "rwProfile":
                return userService.mandatoryProfile(
                        crxActionMap.getObjectIds(),
                        false);
            case "copyTemplate":
                return userService.copyTemplate(
                        crxActionMap.getObjectIds(),
                        crxActionMap.getStringValue());
            case "removeProfiles":
                return userService.removeProfile(crxActionMap.getObjectIds());
            case "deleteUser":
                SessionService sessionService = new SessionService(this.session, this.em);
                if (sessionService.authorize(this.session, "user.delete") || sessionService.authorize(this.session, "student.delete")) {
                    return userService.deleteStudents(crxActionMap.getObjectIds());
                } else {
                    responses.add(new CrxResponse(session, "ERROR", "You have no right to execute this action."));
                    return responses;
                }
        }
        responses.add(new CrxResponse(session, "ERROR", "Unknown action"));
        return responses;
    }

    public List<CrxResponse> groupsApplyAction(CrxActionMap crxActionMap) {
        List<Long> userIds = new ArrayList<Long>();
        GroupService gc = new GroupService(this.session, this.em);
        for (Long id : crxActionMap.getObjectIds()) {
            Group g = gc.getById(id);
            for (User u : g.getUsers()) {
                if (u.getRole().equals(roleStudent) || u.getRole().equals(roleGuest)) {
                    userIds.add(u.getId());
                }
            }
        }
        crxActionMap.setObjectIds(userIds);
        return this.applyAction(crxActionMap);
    }

    public CrxResponse modifyDevice(Long deviceId, Device device) {
        DeviceService deviceConrtoller = new DeviceService(this.session, this.em);
        Device oldDevice = deviceConrtoller.getById(deviceId);
        oldDevice.setRow(device.getRow());
        oldDevice.setPlace(device.getPlace());
        if (deviceConrtoller.getDevicesOnMyPlace(oldDevice).size() > 0) {
            return new CrxResponse(this.session, "ERROR", "Place is already occupied.");
        }
        try {
            this.em.getTransaction().begin();
            this.em.merge(oldDevice);
            this.em.getTransaction().commit();
        } catch (Exception e) {
            return new CrxResponse(this.session, "ERROR", e.getMessage());
        }
        return new CrxResponse(this.session, "OK", "Device was repositioned.");
    }

    public List<Student> getStudents() {
        boolean added = false;
        List<Student> resp = new ArrayList<>();
        for( User user : new UserService(session,em).getByRole(roleStudent) ) {
            added = false;
            for( Group group : user.getGroups() ) {
                if( !group.getGroupType().equals("primary") ) {
                    Student student = new Student(user);
                    student.setGroupName(group.getName());
                    student.setGroupId(group.getId());
                    student.setGroupType(group.getGroupType());
                    resp.add(student);
                    added = true;
                }
            }
            if( ! added ) {
                resp.add(new Student(user));
            }
        }
        return  resp;
    }
}
