/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved  */
package de.openschoolserver.dao.controller;

import java.io.InputStream;


import java.util.*;
import javax.persistence.EntityManager;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.openschoolserver.dao.*;

public class EducationController extends Controller {

	Logger logger = LoggerFactory.getLogger(EducationController.class);
	
	public EducationController(Session session) {
		super(session);
		// TODO Auto-generated constructor stub
	}

	/*
	 * Return the Category to a virtual room
	 */
	
	public Long getCategoryToRoom(Long roomId){
		EntityManager   em = getEntityManager();
		Room room;
		try {
			room = em.find(Room.class, roomId);
		} catch (Exception e) {
			logger.error(e.getMessage());
			return null;
		} finally {
			em.close();
		}
		for( Category category : room.getCategories() ) {
			if( room.getName().equals(category.getName()) && category.getCategoryType().equals("virtualRoom")) {
				return category.getId();
			}
		}
		return null;
	}

	/*
	 * Return the list of ids of rooms in which a user may actually control the access.
	 */
	public List<Long> getMyRooms() {
		List<Long> rooms = new ArrayList<Long>();
		if( this.session.getRoom().getRoomControl().equals("no_control")){
			for( Room room : new RoomController(this.session).getAll() ) {
				switch(room.getRoomControl()) {
				case "no_control":
					break;
				case "all_teacher_control":
					rooms.add(room.getId());
					break;
				case "teacher_control":
					if( this.checkMConfig(room, "teacher_control", Long.toString((this.session.getUserId())))) {
						rooms.add(room.getId());
					}
				}
			}
		} else {
			rooms.add(this.session.getRoomId());
		}
		for( Category category : this.session.getUser().getCategories() ) {
			for( Room room : category.getRooms() ) {
				if( room.getRoomType().equals("virtualRoom")) {
					rooms.add(room.getId());
				}
			}
		}
		return rooms;
	}

	/*
	 * Create the a new virtual room from a hash:
	 * {
	 *     "name"  : <Virtual room name>,
	 *     "description : <Descripton of the room>,
	 *     "studentsOnly : true/false
	 * }
	 */
	public Response createVirtualRoom(Category virtualRoom) {
		EntityManager   em = getEntityManager();
		User   owner       = this.session.getUser();
		/* Define the room */
		Room     room      = new Room();
		room.setName(virtualRoom.getName());
		room.setDescription(virtualRoom.getDescription());
		room.setRoomType("virtualRoom");
		room.getCategories().add(virtualRoom);
		virtualRoom.getRooms().add(room);
		virtualRoom.setOwner(owner);
		virtualRoom.setCategoryType("virtualRoom");
		owner.getCategories().add(virtualRoom);

		try {
			em.getTransaction().begin();
			em.persist(room);
			em.persist(virtualRoom);
			em.merge(owner);
			em.getTransaction().commit();
		} catch (Exception e) {
			logger.error(e.getMessage());
			em.close();
			return new Response(this.getSession(),"ERROR", e.getMessage());
		}
		try {
			em.getTransaction().begin();
			/*
			 * Add groups to the virtual room
			 */
			GroupController groupController = new GroupController(this.session);
			for( Long id : virtualRoom.getGroupIds()) {
				Group group = groupController.getById(id);
				virtualRoom.getGroups().add(group);
				group.getCategories().add(virtualRoom);
				em.merge(room);
				em.merge(virtualRoom);
			}
			/*
			 * Add users to the virtual room
			 */
			UserController  userController  = new UserController(this.session);
			for( Long id : virtualRoom.getUserIds()) {
				User user = userController.getById(Long.valueOf(id));
				if(virtualRoom.getStudentsOnly() && ! user.getRole().equals("studetns")){
					continue;
				}
				virtualRoom.getUsers().add(user);
				user.getCategories().add(virtualRoom);
				em.merge(user);
				em.merge(virtualRoom);
			}
			/*
			 * Add devices to the virtual room
			 */
			DeviceController deviceController = new DeviceController(this.session);
			for( Long id: virtualRoom.getDeviceIds()) {
				Device device = deviceController.getById(Long.valueOf(id));
				virtualRoom.getDevices().add(device);
				device.getCategories().add(virtualRoom);
				em.merge(device);
				em.merge(virtualRoom);
			}
			em.getTransaction().commit();
		} catch (Exception e) {
			logger.error(e.getMessage());
			return new Response(this.getSession(),"ERROR", e.getMessage());
		} finally {
			em.close();
		}
		return new Response(this.getSession(),"OK","Virtual Room was created succesfully"); 
	}

	public Response modifyVirtualRoom(long roomId, Category virtualRoom) {
		EntityManager   em = getEntityManager();
		try {
			em.getTransaction().begin();
			Room room = virtualRoom.getRooms().get(0);
			room.setName(virtualRoom.getName());
			room.setDescription(virtualRoom.getDescription());
			em.merge(virtualRoom);
			em.merge(room);
			em.getTransaction().commit();
		} catch (Exception e) {
			logger.error(e.getMessage());
			return new Response(this.getSession(),"ERROR", e.getMessage());
		} finally {
			em.close();
		}
		return new Response(this.getSession(),"OK","Virtual Room was modified succesfully");
	}
	
	public Response deleteVirtualRoom(Long roomId) {
		EntityManager   em = getEntityManager();
		try {
			em.getTransaction().begin();
			Room room         = new RoomController(this.session).getById(roomId);
			Category category = room.getCategories().get(0);
			em.merge(room);
			em.remove(room);
			em.merge(category);
			em.remove(category);
			em.getTransaction().commit();
		} catch (Exception e) {
			logger.error(e.getMessage());
			return new Response(this.getSession(),"ERROR", e.getMessage());
		} finally {
			em.close();
		}
		return new Response(this.getSession(),"OK","Virtual Room was deleted succesfully");
	}

	
	/*
	 * Get the list of users which are logged in a room or virtual room
	 */
	public List<List<Long>> getRoom(long roomId) {
		List<List<Long>> loggedOns = new ArrayList<List<Long>>();
		List<Long> loggedOn;
		RoomController roomController = new RoomController(this.session);
		Room room = roomController.getById(roomId);
		User me   = this.session.getUser();
		if( room.getRoomType().equals("virtualRoom")) {
			Category category = room.getCategories().get(0);
			for( Group group : category.getGroups() ) {
				for( User user : group.getUsers() ) {
					if(	category.getStudentsOnly() && ! user.getRole().equals("studetns") ||
						user.equals(me)	){
						continue;
					}
					for( Device device : user.getLoggedOn() ) {
						loggedOn = new ArrayList<Long>();
						loggedOn.add(user.getId());
						loggedOn.add(device.getId());
						loggedOns.add(loggedOn);
					}
				}
			}
			for( User user : category.getUsers() ) {
				if( user.equals(me) ) {
					continue;
				}
				for( Device device : user.getLoggedOn() ) {
						loggedOn = new ArrayList<Long>();
						loggedOn.add(user.getId());
						loggedOn.add(device.getId());
						loggedOns.add(loggedOn);
				}
			}
			for( Device device : category.getDevices() ) {
				for( User user : device.getLoggedIn() ) {
					if( ! user.equals(me) ) {
						loggedOn = new ArrayList<Long>();
						loggedOn.add(user.getId());
						loggedOn.add(device.getId());
						loggedOns.add(loggedOn);
					}
				}
			}
		} else {
			for( Device device : room.getDevices() ) {
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
		return loggedOns;
	}

	public Response uploadFileToRoom(long roomId, InputStream fileInputStream,
			FormDataContentDisposition contentDispositionHeader) {
		// TODO Auto-generated method stub
		return null;
	}

	public Response uploadFileToDevice(long deviceId, InputStream fileInputStream,
			FormDataContentDisposition contentDispositionHeader) {
		// TODO Auto-generated method stub
		return null;
	}

	public Response uploadFileToUser(long userId, InputStream fileInputStream,
			FormDataContentDisposition contentDispositionHeader) {
		// TODO Auto-generated method stub
		return null;
	}

	public Response uploadFileToGroup(long groupId, InputStream fileInputStream,
			FormDataContentDisposition contentDispositionHeader) {
		// TODO Auto-generated method stub
		return null;
	}
}