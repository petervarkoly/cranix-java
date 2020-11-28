/**
 * 
 */
package de.cranix.dao;

import java.util.ArrayList;
import java.util.List;

/**
 * @author petervarkoly
 *
 */
public class AdHocRoom extends Room {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean studentsOnly = false;
	private List<User> users     = new ArrayList<User>();
	private List<Group> groups   = new ArrayList<Group>();
	private List<Long> userIds   = new ArrayList<Long>();
	private List<Long> groupIds  = new ArrayList<Long>();
	private Integer devicesProUser = null;

	/**
	 * 
	 */
	public AdHocRoom() {
		// TODO Auto-generated constructor stub
	}
	public AdHocRoom(Room room) {
		super.setId(room.getId());
		super.setName(room.getName());
		super.setDescription(room.getDescription());
		super.setNetMask(room.getNetMask());
		super.setPlaces(room.getPlaces());
		this.setDevicesProUser(room.getPlaces());
		super.setStartIP(room.getStartIP());
		super.setHwconf(room.getHwconf());
		super.setRoomControl(room.getRoomControl());
		super.setRoomType(room.getRoomType());
		super.convertNmToCount();
	}
	
	public boolean isStudentsOnly() {
		return studentsOnly;
	}

	public void setStudentsOnly(boolean studentsOnly) {
		this.studentsOnly = studentsOnly;
	}

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
		this.userIds = new ArrayList<Long>();
		for( User user : users ) {
			this.userIds.add(user.getId());
		}

	}

	public List<Group> getGroups() {
		return groups;
	}

	public void setGroups(List<Group> groups) {
		this.groups = groups;
		this.groupIds = new ArrayList<Long>();
		for( Group group : groups ) {
			this.groupIds.add(group.getId());
		}
	}

	public List<Long> getUserIds() {
		return userIds;
	}

	public void setUserIds(List<Long> userIds) {
		this.userIds = userIds;
	}

	public List<Long> getGroupIds() {
		return groupIds;
	}

	public void setGroupIds(List<Long> groupIds) {
		this.groupIds = groupIds;
	}

	public Integer getDevicesProUser() {
		return this.devicesProUser;
	}

	public void setDevicesProUser(Integer value) {
		this.devicesProUser = value;
		this.setPlaces(value);
	}
}
