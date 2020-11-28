/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.api.resourceimpl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.WebApplicationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.cranix.api.resources.AdHocLanResource;
import de.cranix.dao.Category;
import de.cranix.dao.Device;
import de.cranix.dao.Group;
import de.cranix.dao.CrxResponse;
import de.cranix.dao.Room;
import de.cranix.dao.AdHocRoom;
import de.cranix.dao.Session;
import de.cranix.dao.User;
import de.cranix.dao.controller.*;

public class AdHocLanResourceImpl implements AdHocLanResource {

	Logger logger = LoggerFactory.getLogger(AdHocLanResource.class);
	final EntityManager em;


	public AdHocLanResourceImpl(EntityManager em) {
		super();
		this.em = em;
	}

	@Override
	public List<User> getUsersOfRoom(Session session, Long roomId) {
		Room room  = new RoomController(session,em).getById(roomId);
		for( Category category : room.getCategories() ) {
			if( category.getCategoryType().equals("AdHocAccess")) {
				return category.getUsers();
			}
		}
		return new ArrayList<User>();
	}

	@Override
	public List<Group> getGroupsOfRoom(Session session, Long roomId) {
		Room room  = new RoomController(session,em).getById(roomId);
		for( Category category : room.getCategories() ) {
			if( category.getCategoryType().equals("AdHocAccess")) {
				return category.getGroups();
			}
		}
		return new ArrayList<Group>();
	}

	@Override
	public List<User> getUsers(Session session) {
		AdHocLanController adHocLan = new AdHocLanController(session,em);
		return adHocLan.getUsers();
	}

	@Override
	public List<Group> getGroups(Session session) {
		AdHocLanController adHocLan = new AdHocLanController(session,em);
		List<Group> resp = adHocLan.getGroups();
		return resp;
	}

	@Override
	public List<AdHocRoom> getRooms(Session session) {
		List<AdHocRoom> resp = new ArrayList<AdHocRoom>();
		RoomController roomController = new RoomController(session,em);
		AdHocLanController adHocLan   = new AdHocLanController(session,em);
		if( roomController.isSuperuser() || session.getAcls().contains("adhoclan.manage")) {
			for( Room  room : roomController.getByType("AdHocAccess") ) {
				resp.add(adHocLan.roomToAdHoc(room));
			}
		} else {
			for( Room  room : roomController.getAllToRegister() ) {
				resp.add(adHocLan.roomToAdHoc(room));
			}
		}
		return resp;
	}

	@Override
	public List<AdHocRoom> getMyRooms(Session session) {
		RoomController roomController = new RoomController(session,em);
		List<AdHocRoom> resp= new ArrayList<AdHocRoom>();
		AdHocLanController adHocLan   = new AdHocLanController(session,em);
		for( Room  room : roomController.getAllToRegister() ) {
			resp.add(adHocLan.roomToAdHoc(room));
		}
		return resp;
	}
	@Override
	public CrxResponse add(Session session, AdHocRoom room) {
		CrxResponse resp = new AdHocLanController(session,em).add(room);
		return resp;
	}

	@Override
	public CrxResponse putObjectIntoRoom(Session session, Long roomId, String objectType, Long objectId) {
		CrxResponse resp = new AdHocLanController(session,em).putObjectIntoRoom(roomId,objectType,objectId);
		return resp;
	}

	@Override
	public CrxResponse deleteObjectInRoom(Session session, Long roomId, String objectType, Long objectId) {
		CrxResponse resp = new AdHocLanController(session,em).deleteObjectInRoom(roomId,objectType,objectId);
		return resp;
	}

	@Override
	public List<Device> getDevices(Session session) {
		List<Device> resp = session.getUser().getOwnedDevices();
		return resp;
	}

	@Override
	public CrxResponse deleteDevice(Session session, Long deviceId) {
		DeviceController deviceController = new DeviceController(session,em);
		CrxResponse resp;
		if( deviceController.isSuperuser() ) {
			resp = deviceController.delete(deviceId, true);
		} else {
			Device device = deviceController.getById(deviceId);
			if( deviceController.mayModify(device) ) {
				resp = deviceController.delete(deviceId, true);
			} else {
				resp = new CrxResponse(session,"ERROR", "This is not your device.");
			}
		}
		return resp;
	}

	@Override
	public CrxResponse addDevice(Session session, long roomId, String macAddress, String name) {
		CrxResponse resp = new RoomController(session,em).addDevice(roomId, macAddress, name);
		return resp;
	}

	@Override
	public CrxResponse modifyDevice(Session session, Long deviceId, Device device) {
		DeviceController deviceController = new DeviceController(session,em);
		try {
			Device oldDevice = em.find(Device.class, deviceId);
			if( oldDevice == null ) {
				return new CrxResponse(session,"ERROR","Can not find the device.");
			}
			if( deviceId != device.getId() ) {
				return new CrxResponse(session,"ERROR","Device ID mismatch.");
			}
			if( ! deviceController.mayModify(device) ) {
				return new CrxResponse(session,"ERROR", "This is not your device.");
			}
			em.getTransaction().begin();
			oldDevice.setMac(device.getMac());
			em.merge(oldDevice);
			em.getTransaction().commit();
			new DHCPConfig(session,em).Create();
		}  catch (Exception e) {
			logger.error(e.getMessage());
			return new CrxResponse(session,"ERROR", e.getMessage());
		} finally {
		}
		return new CrxResponse(session,"OK", "Device was modified successfully");
	}


	@Override
	public CrxResponse turnOn(Session session, Long roomId) {
		final RoomController roomController = new RoomController(session,em);
		Room room = roomController.getById(roomId);
		Category category = new Category();
		category.setCategoryType("AdHocAccess");
		category.setName(room.getName());
		category.setDescription(room.getDescription());
		category.setOwner(session.getUser());
		category.setPublicAccess(false);
		category.getRooms().add(room);
		CategoryController categoryController = new CategoryController(session,em);
		CrxResponse resp = categoryController.add(category);
		if( resp.getCode().equals("OK")  ) {
			room.setRoomType("AdHocAccess");
			room.getCategories().add(category);
			resp = roomController.modify(room);
		}
		return resp;
	}

	@Override
	public List<User> getAvailableUser(Session session, long roomId) {
		List<User> users = new ArrayList<User>();
		Category category = new AdHocLanController(session,em).getAdHocCategoryOfRoom(roomId);
		for( User user : new UserController(session,em).getAll() ) {
			if( !category.getUsers().contains(user) ) {
				users.add(user);
			}
		}
		return users;
	}

	@Override
	public List<Group> getAvailableGroups(Session session, long roomId) {
		List<Group> groups = new ArrayList<Group>();
		Category category = new AdHocLanController(session,em).getAdHocCategoryOfRoom(roomId);
		logger.debug("Category " +category);
		for( Group group : new GroupController(session,em).getAll() ) {
			if( !category.getGroups().contains(group) ) {
				groups.add(group);
			}
		}
		return groups;
	}

	@Override
	public AdHocRoom getRoomById(Session session, Long roomId) {
		final RoomController roomController = new RoomController(session,em);
		Room room = roomController.getById(roomId);
		if( room != null && !room.getRoomType().equals("AdHocAccess")) {
			return null;
		}
		AdHocRoom adhocRoom = new AdHocLanController(session,em).roomToAdHoc(room);
		return adhocRoom;
	}

	@Override
	public CrxResponse modify(Session session, Long roomId, AdHocRoom room) {
		if( room.getId() != roomId ) {
			throw new WebApplicationException(403);
		}
		final AdHocLanController ac =  new AdHocLanController(session,em);
		CrxResponse resp = ac.modify(room);
		return resp;
	}

	@Override
	public List<Device> getDevicesOfRoom(Session session, Long adHocRoomId) {
		Room room = em.find(Room.class, adHocRoomId);
		List<Device> devices = new ArrayList<Device>();
		if( room != null ) {
			for(Device device : room.getDevices() ) {
				if(device.getOwner() != null ) {
					device.setOwnerName(
							String.format("%s (%s, %s)",
									device.getOwner().getUid(),
									device.getOwner().getSurName(),
									device.getOwner().getGivenName()
									)
							);
				}
				devices.add(device);
			}
		}
		return devices;
	}

	@Override
	public CrxResponse delete(Session session, Long adHocRoomId) {
		CrxResponse resp = new RoomController(session,em).delete(adHocRoomId);
		return resp;
	}

}
