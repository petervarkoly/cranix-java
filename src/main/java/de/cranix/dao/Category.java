/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.dao;

import java.io.Serializable;


import javax.persistence.*;
import javax.validation.constraints.Size;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * The persistent class for the Categories database table.
 * 
 */
@Entity
@Table(name="Categories")
@NamedQueries({
	@NamedQuery(name="Category.findAll",          query="SELECT c FROM Category c"),
	@NamedQuery(name="Category.getByName",        query="SELECT c FROM Category c where c.name = :name"),
	@NamedQuery(name="Category.getByDescription", query="SELECT c FROM Category c where c.description = :description"),
	@NamedQuery(name="Category.getByType",        query="SELECT c FROM Category c where c.categoryType = :type"),
	@NamedQuery(name="Category.search",           query="SELECT c FROM Category c WHERE c.name LIKE :search OR c.description = :search"),
	@NamedQuery(name="Category.expired",		  query="SELECT c FROM Category c WHERE c.validUntil < CURRENT_TIMESTAMP"),
	@NamedQuery(name="Category.expiredByType",	  query="SELECT c FROM Category c WHERE c.validUntil < CURRENT_TIMESTAMP AND c.categoryType = :type")
})

public class Category implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="CATEGORIES_ID_GENERATOR" )
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="CATEGORIES_ID_GENERATOR")
	@Column(name = "id")
	private Long id;

	@Size(max=64, message="Uid must not be longer then 64 characters..")
	private String description;

	@Size(max=32, message="Name must not be longer then 32 characters..")
	private String name;

	private String categoryType;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date validFrom;

	@Temporal(TemporalType.TIMESTAMP)
	private Date validUntil;

	//bi-directional many-to-one association to User
	@ManyToOne
	@JsonIgnore
	private User owner;

	@Column(name="owner_id", insertable=false, updatable=false)
	private Long ownerId;

	//bi-directional many-to-many association to Device
	@JsonIgnore
	@ManyToMany(cascade ={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
	@JoinTable(        
			name="DeviceInCategories",
			joinColumns={ @JoinColumn(name="category_id") },
			inverseJoinColumns={ @JoinColumn(name="device_id") }
			)
	private List<Device> devices;

	//bi-directional many-to-many association to Group
	@JsonIgnore
	@ManyToMany(cascade ={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
	@JoinTable(
			name="GroupInCategories", 
			joinColumns={ @JoinColumn(name="category_id") },
			inverseJoinColumns={ @JoinColumn(name="group_id") }
			)
	private List<Group> groups = new ArrayList<Group>();

	//bi-directional many-to-many association to Group
	@JsonIgnore
	@ManyToMany(cascade ={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
	@JoinTable(
			name="HWConfInCategories", 
			joinColumns={ @JoinColumn(name="category_id") },
			inverseJoinColumns={ @JoinColumn(name="hwconf_id") }
			)
	private List<HWConf> hwconfs;

	//bi-directional many-to-many association to Room
	@JsonIgnore
	@ManyToMany(cascade ={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
	@JoinTable(
			name="RoomInCategories", 
			joinColumns={ @JoinColumn(name="category_id") },
			inverseJoinColumns={ @JoinColumn(name="room_id") }
			)
	private List<Room> rooms;

	//bi-directional many-to-many association to Software
	@JsonIgnore
	@ManyToMany(cascade ={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
	@JoinTable(
			name="SoftwareInCategories", 
			joinColumns={ @JoinColumn(name="category_id") },
			inverseJoinColumns={ @JoinColumn(name="software_id") }
			)
	private List<Software> softwares;

	//bi-directional many-to-many association to Software
	@JsonIgnore
	@ManyToMany(cascade ={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
	@JoinTable(
			name="SoftwareRemovedFromCategories", 
			joinColumns={ @JoinColumn(name="category_id") },
			inverseJoinColumns={ @JoinColumn(name="software_id") }
			)
	private List<Software> removedSoftwares;

	//bi-directional many-to-many association to User
	@JsonIgnore
	@ManyToMany(cascade ={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
	@JoinTable(
			name="UserInCategories", 
			joinColumns={ @JoinColumn(name="category_id") },
			inverseJoinColumns={ @JoinColumn(name="user_id") }
			)
	private List<User> users = new ArrayList<User>();

	//bi-directional many-to-many association to Announcement
	@ManyToMany(cascade ={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
	@JoinTable(
			name="AnnouncementInCategories", 
			joinColumns={ @JoinColumn(name="category_id") },
			inverseJoinColumns={ @JoinColumn(name="announcement_id") }
			)
	@JsonIgnore
	private List<Announcement> announcements;

	//bi-directional many-to-many association to Contact
	@ManyToMany(cascade ={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
	@JoinTable(
			name="ContactInCategories", 
			joinColumns={ @JoinColumn(name="category_id") },
			inverseJoinColumns={ @JoinColumn(name="contact_id") }
			)
	@JsonIgnore
	private List<Contact> contacts;

	//bi-directional many-to-many association to FAQ
	@ManyToMany(cascade ={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
	@JoinTable(
			name="FAQInCategories", 
			joinColumns={ @JoinColumn(name="category_id") },
			inverseJoinColumns={ @JoinColumn(name="faq_id") }
			)
	@JsonIgnore
	private List<FAQ> faqs;

	@Transient
	private List<Long> deviceIds;

	@Transient
	private List<Long> hwconfIds;

	@Transient
	private List<Long> roomIds;

	@Transient
	private List<Long> userIds;

	@Transient
	private List<Long> groupIds;

	@Transient
	private List<Long> softwareIds;

	@Transient
	private List<Long> announcementIds;

	@Transient
	private List<Long> contactIds;

	@Transient
	private List<Long> faqIds;

	@Convert(converter=BooleanToStringConverter.class)
	boolean studentsOnly;

	@Convert(converter=BooleanToStringConverter.class)
	boolean publicAccess;
	

	@Override
	public String toString() {
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (Exception e) {
			return "{ \"ERROR\" : \"CAN NOT MAP THE OBJECT\" }";
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Category other = (Category) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public Category() {
		this.announcementIds = new ArrayList<Long>();
		this.contactIds = new ArrayList<Long>();
		this.deviceIds  = new ArrayList<Long>();
		this.faqIds     = new ArrayList<Long>();
		this.groupIds   = new ArrayList<Long>();
		this.hwconfIds  = new ArrayList<Long>();
		this.roomIds    = new ArrayList<Long>();
		this.softwareIds= new ArrayList<Long>();
		this.userIds    = new ArrayList<Long>();
		this.validFrom  = new Date(System.currentTimeMillis());
		this.rooms    = new ArrayList<Room>();
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCategoryType() {
		return this.categoryType;
	}

	public void setCategoryType(String categoryType) {
		this.categoryType = categoryType;
	}

	public User getOwner() {
		return this.owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public Long getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(Long ownerId) {
		this.ownerId = ownerId;
	}

	public boolean getStudentsOnly() {
		return this.studentsOnly;
	}

	public void setStudentsOnly( boolean studentsOnly) {
		this.studentsOnly = studentsOnly;
	}

	public boolean isPublicAccess() {
		return publicAccess;
	}

	public void setPublicAccess(boolean publicAccess) {
		this.publicAccess = publicAccess;
	}

	public void setValidFrom(Date validFrom) {
		this.validFrom = validFrom;
	}

	public Date getValidUntil() {
		return validUntil;
	}

	public Date getValidFrom() {
		return validFrom;
	}

	public void setValidUntil(Date validUntil) {
		this.validUntil = validUntil;
	}

	/**
	 * Functions to handle the member of categories
	 */
	//Announcrements
	public List<Announcement> getAnnouncements() {
		return this.announcements;
	}

	public void setAnnouncements(List<Announcement> announcements) {
		this.announcements = announcements;
	}

	public List<Long> getAnnouncementIds() {
		return this.announcementIds;
	}

	public void setAnnouncementIds(List<Long> ids) {
		this.announcementIds = ids;
	}

	//Contacts
	public List<Contact> getContacts() {
		return this.contacts;
	}

	public void setContacts(List<Contact> contacts) {
		this.contacts = contacts;
	}

	public List<Long> getContactIds() {
		return this.contactIds;
	}

	public void setContactIds(List<Long> ids) {
		this.contactIds = ids;
	}
	//Devices
	public List<Device> getDevices() {
		return this.devices;
	}

	public void setDevices(List<Device> devices) {
		this.devices = devices;
	}

	public List<Long> getDeviceIds() {
		return this.deviceIds;
	}

	public void setDeviceIds(List<Long> ids) {
		this.deviceIds = ids;
	}

	//Faqs
	public List<FAQ> getFaqs() {
		return this.faqs;
	}

	public void setFaqs(List<FAQ> faqs) {
		this.faqs = faqs;
	}

	public List<Long> getFaqIds() {
		return faqIds;
	}

	public void setFaqIds(List<Long> faqIds) {
		this.faqIds = faqIds;
	}

	//Groups
	public List<Group> getGroups() {
		return this.groups;
	}

	public void setGroups(List<Group> groups) {
		this.groups = groups;
	}

	public List<Long> getGroupIds() {
		return this.groupIds;
	}

	public void setGroupIds(List<Long> ids) {
		this.groupIds = ids;
	}

	//Hwconfs
	public List<HWConf> getHwconfs() {
		return hwconfs;
	}

	public void setHwconfs(List<HWConf> hwconfs) {
		this.hwconfs = hwconfs;
	}

	public List<Long> getHwconfIds() {
		return this.hwconfIds;
	}

	public void setHwconfIds(List<Long> ids) {
		this.hwconfIds = ids;
	}

	//Rooms
	public List<Room> getRooms() {
		return this.rooms;
	}

	public void setRooms(List<Room> rooms) {
		this.rooms = rooms;
	}

	public List<Long> getRoomIds() {
		return this.roomIds;
	}

	public void setRoomIds(List<Long> ids) {
		this.roomIds = ids;
	}

	//Softwares
	public List<Software> getSoftwares() {
		return this.softwares;
	}

	public void setSoftwares(List<Software> softwares) {
		this.softwares = softwares;
	}

	public List<Long> getSoftwareIds() {
		return this.softwareIds;
	}

	public void setSoftwareIds(List<Long> ids) {
		this.softwareIds = ids;
	}

	//RemovedSoftwares
	public List<Software> getRemovedSoftwares() {
		return this.removedSoftwares;
	}

	public void setRemovedSoftwares(List<Software> softwares) {
		this.removedSoftwares = softwares;
	}

	//Users
	public List<User> getUsers() {
		return this.users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	public List<Long> getUserIds() {
		return this.userIds;
	}

	public void setUserIds(List<Long> ids) {
		this.userIds = ids;
	}

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	public boolean isStudentsOnly() {
		return studentsOnly;
	}

	/**
	 * Function to initialize the xxxxxIds attributes
	 */
	public void setIds() {
		this.announcementIds = new ArrayList<Long>();
		this.contactIds      = new ArrayList<Long>();
		this.deviceIds       = new ArrayList<Long>();
		this.faqIds          = new ArrayList<Long>();
		this.groupIds        = new ArrayList<Long>();
		this.hwconfIds       = new ArrayList<Long>();
		this.roomIds         = new ArrayList<Long>();
		this.softwareIds     = new ArrayList<Long>();
		this.userIds         = new ArrayList<Long>();
		if( this.announcements != null ) {
			for (Announcement a : this.announcements) {
				this.announcementIds.add(a.getId());
			}
		}
		if( this.contacts != null ) {
			for (Contact c : this.contacts) {
				this.contactIds.add(c.getId());
			}
		}
		if( this.devices != null ) {
			for (Device d : this.devices) {
				this.deviceIds.add(d.getId());
			}
		}
		if( this.faqs != null ) {
			for (FAQ f: this.faqs) {
				this.faqIds.add(f.getId());
			}
		}
		if( this.groups != null ) {
			for (Group g: this.groups) {
				this.groupIds.add(g.getId());
			}
		}
		if( this.hwconfs != null ) {
			for (HWConf h: this.hwconfs) {
				this.hwconfIds.add(h.getId());
			}
		}
		if( this.rooms != null ) {
			for (Room r: this.rooms) {
				this.roomIds.add(r.getId());
			}
		}
		if( this.softwares != null ) {
			for (Software s: this.softwares) {
				this.softwareIds.add(s.getId());
			}
		}
		if( this.users != null ) {
			for (User u: this.users) {
				this.userIds.add(u.getId());
			}
		}
	}
}
