/* (c) Péter Varkoly <peter@varkoly.de> - all rights reserved  */
package de.cranix.api.resourceimpl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.cranix.dao.internal.CranixConstants.*;
import de.cranix.api.resources.EducationResource;
import de.cranix.api.resources.Resource;
import de.cranix.dao.AccessInRoom;
import de.cranix.dao.Category;
import de.cranix.dao.Device;
import de.cranix.dao.Group;
import de.cranix.dao.GuestUsers;
import de.cranix.dao.CrxActionMap;
import de.cranix.dao.CrxResponse;
import de.cranix.dao.PositiveList;
import de.cranix.dao.Printer;
import de.cranix.dao.Room;
import de.cranix.dao.Session;
import de.cranix.dao.SmartRoom;
import de.cranix.dao.User;
import de.cranix.dao.controller.*;
import de.cranix.dao.internal.CommonEntityManagerFactory;

public class EducationResourceImpl implements Resource, EducationResource {

	Logger logger = LoggerFactory.getLogger(EducationResourceImpl.class);

	public EducationResourceImpl() {
	}

	@Override
	public CrxResponse createSmartRoom(Session session, Category smartRoom) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new EducationController(session,em).createSmartRoom(smartRoom);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse modifySmartRoom(Session session, Long roomId, Category smartRoom) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new EducationController(session,em).modifySmartRoom(roomId, smartRoom);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse deleteSmartRoom(Session session, Long roomId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new EducationController(session,em).deleteSmartRoom(roomId);
		em.close();
		return resp;
	}

	@Override
	public List<Room> getMySmartRooms(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<Room> resp = new EducationController(session,em).getMySmartRooms();
		em.close();
		return resp;

	}

	@Override
	public List<Room> getMyRooms(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<Room> resp = new EducationController(session,em).getMyRooms();
		em.close();
		return resp;
	}

	@Override
	public List<List<Long>> getRoom(Session session, Long roomId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<List<Long>> resp = new EducationController(session,em).getRoom(roomId);
		em.close();
		return resp;
	}

	@Override
	public List<String> getAvailableRoomActions(Session session, Long roomId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<String> resp = new EducationController(session,em).getAvailableRoomActions(roomId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse manageRoom(Session session, Long roomId, String action) {
		try {
			logger.debug("EducationResourceImpl.manageRoom:" + roomId + " action:" + action);
		}  catch (Exception e) {
			logger.error("EducationResourceImpl.manageRoom error:" + e.getMessage());
		}
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new EducationController(session,em).manageRoom(roomId,action, null);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse manageRoom(Session session, Long roomId, String action, Map<String, String> actionContent) {
		try {
			logger.debug("EducationResourceImpl.manageRoom:" + roomId + " action:" + action);
		}  catch (Exception e) {
			logger.error("EducationResourceImpl.manageRoom error:" + e.getMessage());
		}
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new EducationController(session,em).manageRoom(roomId,action, actionContent);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse addGroup(Session session, Group group) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new EducationController(session,em).createGroup(group);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse modifyGroup(Session session, Long groupId, Group group) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new EducationController(session,em).modifyGroup(groupId, group);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse deleteGroup(Session session, Long groupId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new EducationController(session,em).deleteGroup(groupId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse logOut(Session session, Long userId, Long deviceId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new DeviceController(session,em).removeLoggedInUser(deviceId, userId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse logIn(Session session, Long userId, Long deviceId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new DeviceController(session,em).addLoggedInUser(deviceId, userId);
		em.close();
		return resp;
	}

	@Override
	public List<String> getAvailableUserActions(Session session, Long userId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<String> resp = new EducationController(session,em).getAvailableUserActions(userId);
		em.close();
		return resp;
	}

	@Override
	public List<String> getAvailableDeviceActions(Session session, Long deviceId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<String> resp = new EducationController(session,em).getAvailableDeviceActions(deviceId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse manageDevice(Session session, Long deviceId, String action) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new DeviceController(session,em).manageDevice(deviceId,action,null);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse manageDevice(Session session, Long deviceId, String action, Map<String, String> actionContent) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new DeviceController(session,em).manageDevice(deviceId,action,actionContent);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse addUserToSmartRoom(Session session, Long roomId, Long userId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		EducationController educationController = new EducationController(session,em);
		CrxResponse resp = new CategoryController(session,em).addMember(educationController.getCategoryToRoom(roomId).getId(), "user", userId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse addDeviceToSmartRoom(Session session, Long roomId, Long deviceId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		EducationController educationController = new EducationController(session,em);
		CrxResponse resp = new CategoryController(session,em).addMember(educationController.getCategoryToRoom(roomId).getId(),"device", deviceId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse deleteUserFromSmartRoom(Session session, Long roomId, Long userId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		EducationController educationController = new EducationController(session,em);
		CrxResponse resp = new CategoryController(session,em).deleteMember(educationController.getCategoryToRoom(roomId).getId(), "user", userId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse deleteDeviceFromSmartRoom(Session session, Long roomId, Long deviceId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		EducationController educationController = new EducationController(session,em);
		CrxResponse resp = new CategoryController(session,em).deleteMember(educationController.getCategoryToRoom(roomId).getId(),"device", deviceId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse addGroupToSmartRoom(Session session, Long roomId, Long groupId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		EducationController educationController = new EducationController(session,em);
		CrxResponse resp = new CategoryController(session,em).addMember(educationController.getCategoryToRoom(roomId).getId(),"group",groupId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse deleteGroupFromSmartRoom(Session session, Long roomId, Long groupId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		EducationController educationController = new EducationController(session,em);
		CrxResponse resp = new CategoryController(session,em).deleteMember(educationController.getCategoryToRoom(roomId).getId(),"group",groupId);
		em.close();
		return resp;
	}

	@Override
	public List<CrxResponse> uploadFileToRoom(Session session,
			Long roomId,
			Boolean cleanUp,
			InputStream fileInputStream,
			FormDataContentDisposition contentDispositionHeader) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<CrxResponse> resp = new EducationController(session,em).uploadFileTo("room",roomId,null,fileInputStream,contentDispositionHeader,false, cleanUp);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse uploadFileToUser(Session session,
			Long userId,
			Boolean cleanUp,
			InputStream fileInputStream,
			FormDataContentDisposition contentDispositionHeader) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new EducationController(session,em).uploadFileTo("user",userId,null,fileInputStream,contentDispositionHeader,false, cleanUp).get(0);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse uploadFileToDevice(Session session,
			Long deviceId,
			Boolean cleanUp,
			InputStream fileInputStream,
			FormDataContentDisposition contentDispositionHeader) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new EducationController(session,em).uploadFileTo("device",deviceId,null,fileInputStream,contentDispositionHeader,false, cleanUp).get(0);
		em.close();
		return resp;
	}

	@Override
	public List<CrxResponse> uploadFileToGroup(Session session,
			Long groupId,
			Boolean cleanUp,
			Boolean studentsOnly,
			InputStream fileInputStream,
			FormDataContentDisposition contentDispositionHeader) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<CrxResponse> resp = new EducationController(session,em).uploadFileTo("group",groupId,null,fileInputStream,contentDispositionHeader,studentsOnly, cleanUp);
		em.close();
		return resp;
	}

	@Override
	public List<CrxResponse> uploadFileToGroups(Session session,
			String groupIds,
			Boolean cleanUp,
			Boolean studentsOnly,
			InputStream fileInputStream,
			FormDataContentDisposition contentDispositionHeader) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<CrxResponse> responses = new ArrayList<CrxResponse>();
		EducationController ec = new EducationController(session,em);
		for(String sgroupId : groupIds.split(",")) {
			Long groupId = Long.valueOf(sgroupId);
			if( groupId != null ) {
				responses.addAll(ec.uploadFileTo("group",groupId,null,fileInputStream,contentDispositionHeader,studentsOnly, cleanUp));
			}
		}
		em.close();
		return responses;
	}

	@Override
	public List<CrxResponse> uploadFileToUsers(Session session,
			String sUserIds,
			Boolean cleanUp,
			InputStream fileInputStream,
			FormDataContentDisposition contentDispositionHeader ) {
		List<Long> userIds = new ArrayList<Long>();
		for( String id : sUserIds.split(",")) {
			userIds.add(Long.valueOf(id));
		}
		logger.debug("uploadFileToUsers: " + sUserIds + " " + userIds);
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<CrxResponse> resp = new EducationController(session,em).uploadFileTo("users",0l,userIds,fileInputStream,contentDispositionHeader,false, cleanUp);
		em.close();
		return resp;
	}

	@Override
	public List<CrxResponse> collectFileFromUsers(Session session, String projectName, boolean sortInDirs,
			boolean cleanUpExport, String userIds) {
		List<CrxResponse> responses = new ArrayList<CrxResponse>();
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		UserController userController = new UserController(session,em);
		for( String id : userIds.split(",")) {
			User user = userController.getById(Long.valueOf(id));
			if( user != null ) {
				responses.add(userController.collectFileFromUser(user, projectName,  sortInDirs, cleanUpExport));
			}
		}
		em.close();
		return responses;
	}

	@Override
	public CrxResponse getRoomControl(Session session, Long roomId, Long minutes) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new EducationController(session,em).getRoomControl(roomId,minutes);
		em.close();
		return resp;
	}

	@Override
	public List<String> getAvailableGroupActions(Session session, Long groupId) {
		// TODO Auto-generated method stub
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<String> resp = null;
		em.close();
		return resp;
	}

	@Override
	public List<PositiveList> getPositiveLists(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<PositiveList> resp = new ProxyController(session,em).getAllPositiveLists();
		em.close();
		return resp;
	}

	@Override
	public List<PositiveList> getMyPositiveLists(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<PositiveList> resp = session.getUser().getOwnedPositiveLists();
		em.close();
		return resp;
	}

	@Override
	public CrxResponse addPositiveList(Session session, PositiveList positiveList) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new ProxyController(session,em).editPositiveList(positiveList);
		em.close();
		return resp;
	}

	@Override
	public PositiveList getPositiveListById(Session session, Long positiveListId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		PositiveList resp = new ProxyController(session,em).getPositiveList(positiveListId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse deletePositiveListById(Session session, Long positiveListId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new ProxyController(session,em).deletePositiveList(positiveListId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse activatePositiveListsInRoom(Session session, Long roomId, List<Long> positiveListIds) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new ProxyController(session,em).setAclsInRoom(roomId, positiveListIds);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse deActivatePositiveListsInRoom(Session session, Long roomId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new ProxyController(session,em).deleteAclsInRoom(roomId);
		em.close();
		return resp;
	}

	@Override
	public List<PositiveList> getPositiveListsInRoom(Session session, Long roomId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<PositiveList> resp = new ProxyController(session,em).getPositiveListsInRoom(roomId);
		em.close();
		return resp;
	}

	@Override
	public Printer getDefaultPrinter(Session session, Long roomId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		Printer resp = new RoomController(session,em).getById(roomId).getDefaultPrinter();
		em.close();
		return resp;
	}

	@Override
	public List<Printer> getAvailablePrinters(Session session, Long roomId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<Printer> resp = new RoomController(session,em).getById(roomId).getAvailablePrinters();
		em.close();
		return resp;
	}

	@Override
	public List<User> getUserMember(Session session, Long roomId) {
		List<User> users = new ArrayList<User>();
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		Category category = new EducationController(session,em).getCategoryToRoom(roomId);
		if( category != null ) {
			for ( User member : category.getUsers() ) {
				users.add(member);
			}
		}
		em.close();
		return users;
	}

	@Override
	public List<Group> getGroupMember(Session session, Long roomId) {
		List<Group> groups = new ArrayList<Group>();
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		Category category = new EducationController(session,em).getCategoryToRoom(roomId);
		if( category != null ) {
			for ( Group member : category.getGroups() ) {
				groups.add(member);
			}
		}
		em.close();
		return groups;
	}

	@Override
	public List<Device> getDeviceMember(Session session, Long roomId) {
		List<Device> devices = new ArrayList<Device>();
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		Category category  = new EducationController(session,em).getCategoryToRoom(roomId);
		if( category != null ) {
			for ( Device member : category.getDevices() ) {
				devices.add(member);
			}
		} else {
			RoomController roomController = new RoomController(session,em);
			return roomController.getDevices(roomId);
		}
		em.close();
		return devices;
	}

	@Override
	public CrxResponse collectFileFromDevice(Session session, Long deviceId, String projectName) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		Device device = new DeviceController(session,em).getById(deviceId);
		CrxResponse resp = new UserController(session,em).collectFile(device.getLoggedIn(), projectName);
		em.close();
		return resp;
	}

	@Override
	public List<CrxResponse> collectFileFromRoom(Session session, Long roomId, String projectName, boolean sortInDirs, boolean cleanUpExport) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		UserController userController     = new UserController(session,em);
		DeviceController deviceController = new DeviceController(session,em);
		List<CrxResponse> responses       = new ArrayList<CrxResponse>();
		for( List<Long> logged : new EducationController(session,em).getRoom(roomId) ) {
			User   user   = userController.getById(logged.get(0));
			Device device =  deviceController.getById(logged.get(1));
			if( user == null ) {
				user = userController.getByUid(device.getName());
			}
			if( user != null ) {
				responses.add(userController.collectFileFromUser(user,projectName,sortInDirs,cleanUpExport));
			}
		}
		em.close();
		return responses;
	}

	@Override
	public List<CrxResponse> collectFileFromGroup(Session session,
			Long groupId,
			String projectName,
			boolean sortInDirs,
			boolean cleanUpExport,
			boolean studentsOnly
			) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		UserController userController = new UserController(session,em);
		Group          group          = new GroupController(session,em).getById(groupId);
		List<CrxResponse> responses   = new ArrayList<CrxResponse>();
		for( User user : group.getUsers() ) {
			if( !studentsOnly ||  user.getRole().equals(roleStudent) || user.getRole().equals(roleGuest)) {
				if( user.getRole().equals(roleTeacher) ) {
					responses.add(userController.collectFileFromUser(user, projectName, sortInDirs, false));
				} else {
					responses.add(userController.collectFileFromUser(user, projectName, sortInDirs, cleanUpExport));
				}
			}
		}
		em.close();
		return responses;
	}

	@Override
	public List<CrxResponse> collectFileFromGroups(Session session,
			String groupIds,
			String projectName,
			boolean sortInDirs,
			boolean cleanUpExport,
			boolean studentsOnly
			) {
		List<CrxResponse> responses   = new ArrayList<CrxResponse>();
		for(String sgroupId : groupIds.split(",")) {
			Long groupId = Long.valueOf(sgroupId);
			if(groupId != null ) {
				responses.addAll(collectFileFromGroup(session,Long.valueOf(sgroupId),projectName,sortInDirs,cleanUpExport,studentsOnly));
			}
		}
		return responses;
	}

	@Override
	public List<CrxResponse> applyAction(Session session, CrxActionMap ossActionMap) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<CrxResponse> responses = new ArrayList<CrxResponse>();
		UserController userController = new UserController(session,em);
		logger.debug(ossActionMap.toString());
		switch(ossActionMap.getName()) {
		case "setPassword":
			return  userController.resetUserPassword(
					ossActionMap.getObjectIds(),
					ossActionMap.getStringValue(),
					ossActionMap.isBooleanValue());
		case "setFilesystemQuota":
			return  userController.setFsQuota(
					ossActionMap.getObjectIds(),
					ossActionMap.getLongValue());
		case "setMailsystemQuota":
			return  userController.setMsQuota(
					ossActionMap.getObjectIds(),
					ossActionMap.getLongValue());
		case "disableLogin":
			return  userController.disableLogin(
					ossActionMap.getObjectIds(),
					ossActionMap.isBooleanValue());
		case "disableInternet":
			return  userController.disableInternet(
					ossActionMap.getObjectIds(),
					ossActionMap.isBooleanValue());
		case "mandatoryProfile":
			return  userController.mandatoryProfile(
					ossActionMap.getObjectIds(),
					ossActionMap.isBooleanValue());
		case "copyTemplate":
			return  userController.copyTemplate(
					ossActionMap.getObjectIds(),
					ossActionMap.getStringValue());
		case "removeProfiles":
			return  userController.removeProfile(ossActionMap.getObjectIds());
		case "deleteUser":
			SessionController sessionController = new SessionController(session,em);
			if( sessionController.authorize(session,"user.delete") || sessionController.authorize(session,"student.delete") ) {
				return  userController.deleteStudents(ossActionMap.getObjectIds());
			} else {
				responses.add(new CrxResponse(session,"ERROR","You have no right to execute this action."));
				return responses;
			}
		}
		responses.add(new CrxResponse(session,"ERROR","Unknown action"));
		em.close();
		return responses;
	}

	@Override
	public List<Category> getGuestUsers(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<Category> resp = new UserController(session,em).getGuestUsers();
		em.close();
		return resp;
	}

	@Override
	public Category getGuestUsersCategory(Session session, Long guestUsersId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		Category resp = new UserController(session,em).getGuestUsersCategory(guestUsersId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse deleteGuestUsers(Session session, Long guestUsersId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new UserController(session,em).deleteGuestUsers(guestUsersId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse addGuestUsers(Session session, String name, String description, Long roomId, Long count,
			Date validUntil) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		GuestUsers guestUsers = new GuestUsers(name,description,count,roomId,validUntil);
		CrxResponse resp = new UserController(session,em).addGuestUsers(guestUsers);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse createGuestUsers(Session session, GuestUsers guestUser ) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new UserController(session,em).addGuestUsers(guestUser);
		em.close();
		return resp;
	}

	@Override
	public List<Room> getGuestRooms(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<Room> resp = new RoomController(session,em).getAllWithTeacherControl();
		em.close();
		return resp;
	}

	@Override
	public List<User> getUsersById(Session session, List<Long> userIds) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<User> resp = new UserController(session,em).getUsers(userIds);
		em.close();
		return resp;
	}

	@Override
	public Group getGroup(Session session, Long groupId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		Group resp = new GroupController(session,em).getById(groupId);
		em.close();
		return resp;
	}

	@Override
	public List<Group> getMyGroups(Session session) {
		List<Group> groups = new ArrayList<Group>();
		for( Group group : session.getUser().getGroups() ) {
			if( !group.getGroupType().equals("primary") ) {
				groups.add(group);
			}
		}
		for( Group group : session.getUser().getOwnedGroups() ) {
			if( !group.getGroupType().equals("primary") &&
			    !groups.contains(group)) {
				groups.add(group);
			}
		}
		return groups;
	}

	@Override
	public List<Group> getMyAvailableClasses(Session session) {
		List<Group> groups = new ArrayList<Group>();
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		for( Group group : new GroupController(session,em).getByType("class") ) {
			if( !session.getUser().getGroups().contains(group)) {
				groups.add(group);
			}
		}
		em.close();
		return groups;
	}

	@Override
	public List<User> getAvailableMembers(Session session, long groupId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<User> resp = new ArrayList<User>();
		Group group = em.find(Group.class, groupId);
		if( group != null ) {
			UserController uc = new UserController(session,em);
			resp = uc.getByRole(roleStudent);
			resp.addAll(uc.getByRole(roleTeacher));
			resp.removeAll(group.getUsers());
			em.close();
		}
		return resp;
	}

	@Override
	public List<User> getMembers(Session session, long groupId) {
		List<User> users = new ArrayList<User>();
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		Group group = new GroupController(session,em).getById(groupId);
		Boolean myGroup = group.getOwner().equals(session.getUser());
		for( User user :  new GroupController(session,em).getMembers(groupId) ) {
			if( myGroup || user.getRole().equals(roleStudent) || user.getRole().equals(roleGuest) ) {
				users.add(user);
			}
		}
		em.close();
		return users;
	}

	@Override
	public CrxResponse deleteMember(Session session, long groupId, long userId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new GroupController(session,em).removeMember(groupId, userId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse addMember(Session session, long groupId, long userId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new GroupController(session,em).addMember(groupId, userId);
		em.close();
		return resp;
	}

	@Override
	public AccessInRoom getAccessStatus(Session session, long roomId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		AccessInRoom resp = new RoomController(session,em).getAccessStatus(roomId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse setAccessStatus(Session session, long roomId, AccessInRoom access) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		access.setAllowSessionIp(true);
		CrxResponse resp = new RoomController(session,em).setAccessStatus(roomId, access);
		em.close();
		return resp;
	}

	@Override
	public List<Device> getDevicesById(Session session, List<Long> deviceIds) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<Device> resp = new DeviceController(session,em).getDevices(deviceIds);
		em.close();
		return resp;
	}

	@Override
	public User getUserById(Session session, Long userId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		User resp = new UserController(session,em).getById(userId);
		em.close();
		return resp;
	}

	@Override
	public Device getDeviceById(Session session, Long deviceId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		Device resp = new DeviceController(session,em).getById(deviceId);
		em.close();
		return resp;
	}

	@Override
	public List<User> getAvailableUserMember(Session session, Long roomId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<User> members = this.getUserMember(session, roomId);
		List<User> availableMembers = new ArrayList<User>();
		for( User user : new UserController(session,em).getAll() ) {
			if(!members.contains(user)) {
				availableMembers.add(user);
			}
		}
		em.close();
		return availableMembers;
	}

	@Override
	public List<Group> getAvailableGroupMember(Session session, Long roomId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<Group> members = this.getGroupMember(session, roomId);
		List<Group> availableMembers = new ArrayList<Group>();
		for( Group group : new GroupController(session,em).getAll() ) {
			if( !members.contains(group)) {
				availableMembers.add(group);
			}
		}
		em.close();
		return availableMembers;
	}

	@Override
	public List<Device> getAvailableDeviceMember(Session session, Long roomId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<Device> members = this.getDeviceMember(session, roomId);
		List<Device> availableMembers = new ArrayList<Device>();
		for( Device device : new DeviceController(session,em).getAll() ) {
			if( !members.contains(device)) {
				availableMembers.add(device);
			}
		}
		em.close();
		return availableMembers;
	}

	@Override
	public CrxResponse modifyDevice(Session session, Long deviceId, Device device) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		DeviceController deviceConrtoller = new DeviceController(session,em);
		Device oldDevice = deviceConrtoller.getById(deviceId);
		oldDevice.setRow(device.getRow());
		oldDevice.setPlace(device.getPlace());
		if( deviceConrtoller.getDevicesOnMyPlace(oldDevice).size() > 0 ) {
			return new CrxResponse(session,"ERROR","Place is already occupied.");
		}
		try {
			em.getTransaction().begin();
			em.merge(oldDevice);
			em.getTransaction().commit();
		} catch (Exception e) {
			return new CrxResponse(session,"ERROR", e.getMessage());
		} finally {
			em.close();
		}
		return new CrxResponse(session,"OK","Device was repositioned.");
	}

	@Override
	public CrxResponse modifyDeviceOfRoom(Session session, Long roomId, Long deviceId, Device device) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		Room room = new RoomController(session,em).getById(roomId);
		CrxResponse resp = null;
		if( (room.getCategories() != null) && (room.getCategories().size() > 0 ) && room.getCategories().get(0).getCategoryType().equals("smartRoom") ) {
			DeviceController deviceConrtoller = new DeviceController(session,em);
			Device oldDevice = deviceConrtoller.getById(deviceId);
			resp = deviceConrtoller.setConfig(oldDevice, "smartRoom-" + roomId + "-coordinates", String.format("%d,%d", device.getRow(),device.getPlace()));
		} else {
			resp = modifyDevice(session, deviceId, device);
		}
		em.close();
		return resp;
	}

	@Override
	public SmartRoom getRoomDetails(Session session, Long roomId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		SmartRoom resp = new SmartRoom(session,em,roomId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse manageGroup(Session session, Long groupId, String action) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		GroupController gc = new GroupController(session,em);
		Group group = gc.getById(groupId);
		CrxResponse resp = null;
		switch(action.toLowerCase()) {
		case "turnsmartroom":
			resp = gc.createSmartRoomForGroup(group, true, true);
		}
		em.close();
		return resp;
	}

	@Override
	public CrxResponse manageGroup(Session session, Long groupId, String action, Map<String, String> actionContent) {
		// TODO Auto-generated method stub
		return null;
	}
}
