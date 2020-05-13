/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved  */
package de.cranix.dao.controller;

import java.io.File;


import static de.cranix.dao.internal.CranixConstants.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.FileSystems;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.*;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.cranix.dao.*;
import de.cranix.dao.tools.OSSShellTools;
import static de.cranix.dao.internal.CranixConstants.*;

public class EducationController extends UserController {

	Logger logger = LoggerFactory.getLogger(EducationController.class);

	public EducationController(Session session,EntityManager em) {
		super(session,em);
	}

	/*
	 * Return the Category to a smart room
	 */
	public Category getCategoryToRoom(Long roomId){
		Room room;
		try {
			room = this.em.find(Room.class, roomId);
		} catch (Exception e) {
			logger.error(e.getMessage());
			return null;
		} finally {
		}
		for( Category category : room.getCategories() ) {
			if( room.getName().equals(category.getName()) && category.getCategoryType().equals("smartRoom")) {
				return category;
			}
		}
		return null;
	}

	public List<Room> getMySmartRooms() {
		List<Room> smartRooms = new ArrayList<Room>();
		for( Category category  : new CategoryController(this.session,this.em).getByType("smartRoom") ) {
			if( category.getOwner() != null && category.getOwner().equals(session.getUser())
					) {
				logger.debug("getMySamrtRooms" + category);
				if( category.getRooms() != null && category.getRooms().size() > 0 ) {
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
	 * @return The list of the found rooms.
	 */
	public List<Room> getMyRooms() {
		List<Room> rooms = new ArrayList<Room>();
		if( this.session.getRoom() == null || this.session.getRoom().getRoomControl().equals("no")){
			for( Room room : new RoomController(this.session,this.em).getAllToUse() ) {
				switch(room.getRoomControl()) {
				case "no":
				case "inRoom":
					if( this.session.getUser().getRole().equals(roleSysadmin) &&
						getProperty("de.cranix.dao.Education.Rooms.sysadminsConrtolInRoom").equals("yes")	) {
						rooms.add(room);
					}
					break;
				case "allTeachers":
					rooms.add(room);
					break;
				case "teachers":
					if( this.checkMConfig(room, "teachers", Long.toString((this.session.getUserId())))) {
						rooms.add(room);
					}
				}
			}
		} else {
			rooms.add(this.session.getRoom());
			if( this.session.getRoom().getRoomControl().equals("inRoom")) {
				return rooms;
			}
		}
		rooms.addAll(this.getMySmartRooms());
		return rooms;
	}

	/**
	 * Return the list of room ids in which a user may actually control the access.<br>
	 * @return The list of the ids of the found rooms.
	 * @see getMyRooms
	 */
	public List<Long> getMyRoomsId(){
		List<Long> roomIds = new ArrayList<Long>();
		for( Room room : this.getMyRooms()) {
			roomIds.add(room.getId());
		}
		return roomIds;
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
		User   owner       = this.session.getUser();
		/* Define the room */
		Room     room      = new Room();
		room.setName(smartRoom.getName());
		room.setDescription(smartRoom.getDescription());
		room.setRoomType("smartRoom");
		room.setRows(7);
		room.setPlaces(7);
		room.setCreator(owner);
		room.getCategories().add(smartRoom);
		smartRoom.setRooms(new ArrayList<Room>());
		smartRoom.getRooms().add(room);
		smartRoom.setOwner(owner);
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
			return new CrxResponse(this.getSession(),"ERROR", e.getMessage());
		}
		try {
			this.em.getTransaction().begin();
			/*
			 * Add groups to the smart room
			 */
			GroupController groupController = new GroupController(this.session,this.em);
			for( Long id : smartRoom.getGroupIds()) {
				Group group = groupController.getById(id);
				smartRoom.getGroups().add(group);
				group.getCategories().add(smartRoom);
				this.em.merge(room);
				this.em.merge(smartRoom);
			}
			/*
			 * Add users to the smart room
			 */
			for( Long id : smartRoom.getUserIds()) {
				User user = this.getById(Long.valueOf(id));
				if(smartRoom.getStudentsOnly() && ! user.getRole().equals(roleStudent)){
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
			DeviceController deviceController = new DeviceController(this.session,this.em);
			for( Long id: smartRoom.getDeviceIds()) {
				Device device = deviceController.getById(Long.valueOf(id));
				smartRoom.getDevices().add(device);
				device.getCategories().add(smartRoom);
				this.em.merge(device);
				this.em.merge(smartRoom);
			}
			this.em.getTransaction().commit();
		} catch (Exception e) {
			logger.error(e.getMessage());
			return new CrxResponse(this.getSession(),"ERROR", e.getMessage());
		} finally {
		}
		return new CrxResponse(this.getSession(),"OK","Smart Room was created succesfully.");
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
			return new CrxResponse(this.getSession(),"ERROR", e.getMessage());
		} finally {
		}
		return new CrxResponse(this.getSession(),"OK","Smart Room was modified succesfully.");
	}

	public CrxResponse deleteSmartRoom(Long roomId) {
		try {
			this.em.getTransaction().begin();
			Room room         = this.em.find(Room.class, roomId);
			for( Category category : room.getCategories() ) {
				if( category.getCategoryType().equals("smartRoom") && category.getName().equals(room.getName()) ) {
					User owner = category.getOwner();
					if( owner != null ) {
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
			return new CrxResponse(this.getSession(),"ERROR", e.getMessage());
		} finally {
		}
		return new CrxResponse(this.getSession(),"OK","Smart Room was deleted succesfully.");
	}


	/**
	 * Get the list of users which are logged in a room or smart room
	 * If a user of a smart room is not logged on the device id is 0L;
	 * @param roomId The id of the wanted room.
	 * @return The id list of the logged on users: [ [ <userId>,<deviceId> ], [ <userId>, <deviceId] ... ]
	 */
	public List<List<Long>> getRoom(long roomId) {
		List<List<Long>> loggedOns = new ArrayList<List<Long>>();
		List<Long> loggedOn;
		RoomController roomController = new RoomController(this.session,this.em);
		Room room = roomController.getById(roomId);
		User me   = this.session.getUser();
		if( room.getRoomType().equals("smartRoom")) {
			Category category = room.getCategories().get(0);
			for( Group group : category.getGroups() ) {
				for( User user : group.getUsers() ) {
					if(	category.getStudentsOnly() && ! user.getRole().equals(roleStudent) ||
							user.equals(me)	){
						continue;
					}
					if( user.getLoggedOn().isEmpty() ) {
						loggedOn = new ArrayList<Long>();
						loggedOn.add(user.getId());
						loggedOn.add(0L);
						loggedOns.add(loggedOn);
					} else {
						for( Device device : user.getLoggedOn() ) {
							loggedOn = new ArrayList<Long>();
							loggedOn.add(user.getId());
							loggedOn.add(device.getId());
							loggedOns.add(loggedOn);
						}
					}
				}
			}
			for( User user : category.getUsers() ) {
				if( user.equals(me) ) {
					continue;
				}
				if( user.getLoggedOn().isEmpty() ) {
					loggedOn = new ArrayList<Long>();
					loggedOn.add(user.getId());
					loggedOn.add(0L);
					loggedOns.add(loggedOn);
				} else {
					for( Device device : user.getLoggedOn() ) {
						loggedOn = new ArrayList<Long>();
						loggedOn.add(user.getId());
						loggedOn.add(device.getId());
						loggedOns.add(loggedOn);
					}
				}
			}
			for( Device device : category.getDevices() ) {
				/*
				 * If nobody is logged in set user id to 0L
				 */
				if( device.getLoggedIn().isEmpty() ) {
					loggedOn = new ArrayList<Long>();
					loggedOn.add(0L);
					loggedOn.add(device.getId());
					loggedOns.add(loggedOn);
				} else {
					for( User user : device.getLoggedIn() ) {
						if( ! user.equals(me) ) {
							loggedOn = new ArrayList<Long>();
							loggedOn.add(user.getId());
							loggedOn.add(device.getId());
							loggedOns.add(loggedOn);
						}
					}
				}
			}
		} else {
			for( Device device : room.getDevices() ) {
				/*
				 * If nobody is logged in set user id to 0L
				 */
				if( device.getLoggedIn().isEmpty() ) {
					loggedOn = new ArrayList<Long>();
					loggedOn.add(0L);
					loggedOn.add(device.getId());
					loggedOns.add(loggedOn);
				} else {
					for( User user : device.getLoggedIn() ) {
						if( ! user.equals(me) ) {
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

	public List<CrxResponse> uploadFileTo(String what,
			Long objectId,
			List<Long> objectIds,
			InputStream fileInputStream,
			FormDataContentDisposition contentDispositionHeader,
			Boolean studentsOnly,
			Boolean cleanUp ) {
		String fileName = contentDispositionHeader.getFileName();
		File file = null;
		List<CrxResponse> responses = new ArrayList<CrxResponse>();
		try {
			file = File.createTempFile("oss_uploadFile", ".ossb", new File(cranixTmpDir));
			Files.copy(fileInputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			responses.add(new CrxResponse(this.getSession(),"ERROR", e.getMessage()));
			return responses;
		}
		switch(what) {
		case "users":
			for( Long id : objectIds) {
				User user = this.getById(id);
				if( user != null ) {
					responses.add(this.saveFileToUserImport(user, file, fileName, cleanUp));
				} else {
					responses.add(new CrxResponse(this.getSession(),"ERROR","User with id %s does not exists.",null,id.toString() ));
				}
			}
			break;
		case "user":
			User user = this.getById(objectId);
			if( user != null ) {
				responses.add(this.saveFileToUserImport(user, file, fileName, cleanUp));
			} else {
				responses.add(new CrxResponse(this.getSession(),"ERROR","User with id %s does not exists.",null,objectId.toString() ));
			}
			break;
		case "group":
			Group group = new GroupController(this.session,this.em).getById(objectId);
			if( group != null ) {
				for( User myUser : group.getUsers() ) {
					if( !this.session.getUser().equals(myUser) &&
						( !studentsOnly  || myUser.getRole().equals(roleStudent) || myUser.getRole().equals(roleGuest) )) {
						responses.add(this.saveFileToUserImport(myUser, file, fileName, cleanUp));
					}
				}
			} else {
				responses.add(new CrxResponse(this.getSession(),"ERROR","Group with id %s does not exists.",null,objectId.toString() ));
			}
			break;
		case "device":
			Device device = new DeviceController(this.session,this.em).getById(objectId);
			if( device != null ) {
				for( User myUser : device.getLoggedIn() ) {
					responses.add(this.saveFileToUserImport(myUser, file, fileName, cleanUp));
				}
			} else {
				responses.add(new CrxResponse(this.getSession(),"ERROR","Device with id %s does not exists.",null,objectId.toString() ));
			}
			break;
		case "room":
			DeviceController deviceController = new DeviceController(this.session,this.em);
			for( List<Long> loggedOn : this.getRoom(objectId) ) {
				User myUser = this.getById(loggedOn.get(0));
				if( myUser == null ) {
					//no user logged in we try the workstation user
					Device dev = deviceController.getById(loggedOn.get(1));
					if( dev != null ){
						myUser=this.getByUid(dev.getName());
					}
				}
				if( myUser != null ) {
					responses.add(this.saveFileToUserImport(myUser, file, fileName, cleanUp));
				}
			}
		}
		file.delete();
		return responses;
	}

	public CrxResponse saveFileToUserImport(User user, File file, String fileName, Boolean cleanUp) {
		if( cleanUp == null ) {
			cleanUp = true;
		}
		UserPrincipalLookupService lookupService = FileSystems.getDefault().getUserPrincipalLookupService();
		List<String> parameters = new ArrayList<String>();
		if( user == null ) {
			return new CrxResponse(this.getSession(),"ERROR","No user defined.");
		} else {
			logger.debug("File " + fileName + " saved to " + user.getUid());
		}
		parameters.add(fileName);
		parameters.add(user.getUid());
		try {
			UserPrincipal owner = lookupService.lookupPrincipalByName(user.getUid());
			String homeDir = this.getHomeDir(user);
			//TODO make it configurable
			if( user.getRole().equals(roleWorkstation) && cleanUp ) {
				StringBuffer reply = new StringBuffer();
				StringBuffer error = new StringBuffer();
				String[] program   = new String[3];
				program[0] = "/usr/bin/rm";
				program[1] = "-rf";
				program[2] = homeDir;
				OSSShellTools.exec(program, reply, error, null);
				File homeDirF = new File( homeDir );
				Files.createDirectories(homeDirF.toPath(), privatDirAttribute );
				Files.setOwner(homeDirF.toPath(), owner);
			}
			String importDir = homeDir + "Import/";
			// Create the directory first.
			File importDirF = new File( importDir );
			Files.createDirectories(importDirF.toPath(), privatDirAttribute );

			// Copy temp file to the right place
			File newFile = new File( importDir +  fileName );
			Files.copy(file.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

			// Set owner
			Files.setOwner(importDirF.toPath(), owner);
			Files.setOwner(newFile.toPath(), owner);

			//Clean up export of target
			//TODO MAKE IT BOOLEAN
			String export  = homeDir + "Export/";
			File exportDir = new File( export );
			Files.createDirectories(exportDir.toPath(), privatDirAttribute );
			Files.setOwner(exportDir.toPath(), owner);
			if( cleanUp && ( user.getRole().equals(roleStudent) || user.getRole().equals(roleWorkstation) || user.getRole().equals(roleGuest) ) ) {
				//Clean up Export by students workstations and guests
				for( String mist : exportDir.list() ) {
					File f = new File(mist);
					if( !f.isDirectory() ) {
						f.delete();
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			parameters.add(e.getMessage());
			return new CrxResponse(this.getSession(),"ERROR","File %s could not be saved to user: %s: %s",null,parameters);
		}
		return new CrxResponse(this.getSession(),"OK","File '%s' successfully saved to user '%s'.",null,parameters);
	}


	public CrxResponse createGroup(Group group) {
		GroupController groupController = new GroupController(this.session,this.em);
		group.setGroupType("workgroup");
		group.setOwner(session.getUser());
		return groupController.add(group);
	}

	public CrxResponse modifyGroup(long groupId, Group group) {
		GroupController groupController = new GroupController(this.session,this.em);
		Group emGroup = groupController.getById(groupId);
		if( this.session.getUser().equals(emGroup.getOwner())) {
			return groupController.modify(group);
		} else {
			return new CrxResponse(this.getSession(),"ERROR", "You are not the owner of this group.");
		}
	}

	public CrxResponse deleteGroup(long groupId) {
		GroupController groupController = new GroupController(this.session,this.em);
		Group emGroup = groupController.getById(groupId);
		if( this.session.getUser().equals(emGroup.getOwner())) {
			return groupController.delete(groupId);
		} else {
			return new CrxResponse(this.getSession(),"ERROR", "You are not the owner of this group.");
		}
	}

	public List<String> getAvailableRoomActions(long roomId) {
		Room room = new RoomController(this.session,this.em).getById(roomId);
		List<String> actions = new ArrayList<String>();
		for( String action : this.getProperty("de.cranix.dao.EducationController.RoomActions").split(",") ) {
			if(! this.checkMConfig(room, "disabledActions", action )) {
				actions.add(action);
			}
		}
		return actions;
	}

	public List<String> getAvailableUserActions(long userId) {
		User user = this.getById(userId);
		List<String> actions = new ArrayList<String>();
		for( String action : this.getProperty("de.cranix.dao.EducationController.UserActions").split(",") ) {
			if(! this.checkMConfig(user, "disabledActions", action )) {
				actions.add(action);
			}
		}
		return actions;
	}

	public List<String> getAvailableDeviceActions(long deviceId) {
		Device device = new DeviceController(this.session,this.em).getById(deviceId);
		List<String> actions = new ArrayList<String>();
		for( String action : this.getProperty("de.cranix.dao.EducationController.DeviceActions").split(",") ) {
			if(! this.checkMConfig(device, "disabledActions", action )) {
				actions.add(action);
			}
		}
		return actions;
	}


	public CrxResponse manageRoom(long roomId, String action, Map<String, String> actionContent) {
		CrxResponse ossResponse = null;
		List<String> errors = new ArrayList<String>();
		DeviceController dc = new DeviceController(this.session,this.em);

		/*
		* This is a very special action
		*/
		if( action.startsWith("organize")) {
			return new RoomController(this.session,this.em).organizeRoom(roomId);
		}

		Room room = new RoomController(this.session,this.em).getById(roomId);
		if( action.equals("setPassword" ) ) {
			StringBuffer reply = new StringBuffer();
			StringBuffer error = new StringBuffer();
			String[] program = new String[5];
			program[0] = "/usr/bin/samba-tool";
			program[1] = "domain";
			program[2] = "passwordsettings";
			program[3] = "set";
			program[4] = "--complexity=off";
			OSSShellTools.exec(program, reply, error, null);
			program[1] = "user";
			program[2] = "setpassword";
			program[4] = "--newpassword=" + actionContent.get("password");
			for( Device device : room.getDevices() ) {
				error = new StringBuffer();
				reply = new StringBuffer();
				program[3] = device.getName();
				OSSShellTools.exec(program, reply, error, null);
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
				OSSShellTools.exec(program, reply, error, null);
			}
			return new CrxResponse(this.getSession(), "OK", "The password of the selected users was reseted.");
		}

		logger.debug("manageRoom called " + roomId + " action:");
		if( action.equals("download") && actionContent == null ) {
			actionContent = new HashMap<String,String>();
			actionContent.put("projectName", this.nowString() + "." + room.getName() );
		}
		for( List<Long> loggedOn : this.getRoom(roomId) ) {
			logger.debug("manageRoom " + roomId + " user:" + loggedOn.get(0) + " device:" +  loggedOn.get(1));
			//Do not control the own workstation
			if( this.session.getUser().getId().equals(loggedOn.get(0)) ||
				(this.session.getDevice() != null && this.session.getDevice().getId().equals(loggedOn.get(1))))
			{
				continue;
			}
			ossResponse = dc.manageDevice(loggedOn.get(1), action, actionContent);
			if( ossResponse.getCode().equals("ERROR")) {
				errors.add(ossResponse.getValue());
			}
		}
		if( errors.isEmpty() ) {
			return new CrxResponse(this.getSession(),"OK", "Room control was applied.");
		} else {
			return new CrxResponse(this.getSession(),"ERROR",String.join("<br>", errors));
		}
	}

	public Long getRoomActualController(long roomId) {
		try {
			Query query = this.em.createNamedQuery("SmartControl.getAllActiveInRoom");
			query.setParameter("roomId", roomId);
			if( query.getResultList().isEmpty() ) {
				//The room is free
				return null;
			}
			RoomSmartControl rsc = (RoomSmartControl) query.getResultList().get(0);
			return rsc.getOwner().getId();
		} catch (Exception e) {
		  logger.error(e.getMessage());
		  return null;
		} finally {
		}
	}

	/**
	 * getRoomControl
	 * @param roomId	The roomId which should be controlled
	 * @param minutes   How long do you want to control the room
	 * @return          An CrxResponse object
	 */
	public CrxResponse getRoomControl(long roomId, long minutes) {

		//Create the list of the controllers
		StringBuilder controllers = new StringBuilder();
		controllers.append(this.getConfigValue("SERVER")).append(";").append(this.session.getDevice().getIp());
		if( this.session.getDevice().getWlanIp() != null ) {
			controllers.append(";").append(this.session.getDevice().getWlanIp());
		}

		// Get the list of the devices
		DeviceController dc = new DeviceController(this.session,this.em);
		List<String>  devices = new ArrayList<String>();
		String domain         = "." + this.getConfigValue("DOMAIN");
		for( List<Long> loggedOn : this.getRoom(roomId) ) {
			//Do not control the own workstation
			if( this.session.getDevice().getId().equals(loggedOn.get(1))) {
				continue;
			}
			devices.add(dc.getById(loggedOn.get(1)).getName() + domain );
		}
		String[] program    = new String[5];
		StringBuffer reply  = new StringBuffer();
		StringBuffer stderr = new StringBuffer();
		program[0] = "/usr/bin/salt";
		program[1] = "-L";
		program[2] = String.join(",", devices);
		program[3] = "oss_client.set_controller_ip";
		program[4] = controllers.toString();
		OSSShellTools.exec(program, reply, stderr, null);

		RoomSmartControl roomSmartControl = new RoomSmartControl(roomId,this.session.getUserId(),minutes);
		try {
			this.em.getTransaction().begin();
			this.em.persist(roomSmartControl);
			this.em.getTransaction().commit();
		} catch (Exception e) {
			logger.error(e.getMessage());
			return new CrxResponse(this.getSession(),"ERROR", e.getMessage());
		} finally {
		}
		return new CrxResponse(this.getSession(),"OK", "Now you have the control for the selected room.");
	}
}
