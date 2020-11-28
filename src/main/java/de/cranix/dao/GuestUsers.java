/**
 * 
 */
package de.cranix.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author petervarkoly
 *
 */
public class GuestUsers {

	public GuestUsers(String name, String description, Long count, List<Long> roomIds, String password,
			boolean createAdHocRoom, String roomControl, boolean privateGroup, Date validity) {
		super();
		this.name = name;
		this.description = description;
		this.count = count;
		this.roomIds = roomIds;
		this.password = password;
		this.createAdHocRoom = createAdHocRoom;
		this.roomControl = roomControl;
		this.privateGroup = privateGroup;
		this.validUntil = validity;
	}

	public GuestUsers(String name, String description, Long count, Long room, Date validity) {
			super();
			this.name = name;
			this.description = description;
			this.count = count;
			this.roomIds = new ArrayList<Long>();
			this.roomIds.add(room);
			this.password = "";
			this.createAdHocRoom = false;
			this.validUntil = validity;
			this.privateGroup = true;
			this.roomControl  = "allTeachers";
		}

	/**
	 * Class for guest user accounts
	 */
	public GuestUsers() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * The name of the guest group
	 */
	private String name = "";
	
	/**
	 * Description of the guest group
	 */
	private String description = "";
	
	/**
	 * Count of the user in the guest group
	 */
	private Long count = 1L;
	
	/**
	 * The list of the room ids where the member of the group may use the workstations
	 */
	private List<Long> roomIds = new ArrayList<Long>();
	
	/**
	 * The standard password of the guest user 
	 */
	private String password = "";
	
	/**
	 * Create an adhoc room for the guest user 
	 */
	private boolean createAdHocRoom = false;
	
	/**
	 * Room control
	 */
	private String roomControl = "no";
	
	/**
	 * 
	 */
	private boolean privateGroup = false;
	
	/**
	 * Validity of the guest group
	 */
	private Date validUntil;
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the count
	 */
	public Long getCount() {
		return count;
	}

	/**
	 * @param count the count to set
	 */
	public void setCount(Long count) {
		this.count = count;
	}

	/**
	 * @return the roomIds
	 */
	public List<Long> getRoomIds() {
		return roomIds;
	}

	/**
	 * @param roomIds the roomIds to set
	 */
	public void setRoomIds(List<Long> roomIds) {
		this.roomIds = roomIds;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the createAdHocRoom
	 */
	public boolean isCreateAdHocRoom() {
		return createAdHocRoom;
	}

	/**
	 * @param createAdHocRoom the createAdHocRoom to set
	 */
	public void setCreateAdHocRoom(boolean createAdHocRoom) {
		this.createAdHocRoom = createAdHocRoom;
	}

	/**
	 * @return the validity
	 */
	public Date getValidUntil() {
		return validUntil;
	}

	/**
	 * @param validity the validity to set
	 */
	public void setValidUntil(Date validity) {
		this.validUntil = validity;
	}

	/**
	 * @return the roomControl
	 */
	public String getRoomControl() {
		return roomControl;
	}

	/**
	 * @param roomControl the roomControl to set
	 */
	public void setRoomControl(String roomControl) {
		this.roomControl = roomControl;
	}

	/**
	 * @return the privateGroup
	 */
	public boolean isPrivateGroup() {
		return privateGroup;
	}

	/**
	 * @param privateGroup the privateGroup to set
	 */
	public void setPrivateGroup(boolean privateGroup) {
		this.privateGroup = privateGroup;
	}

}
