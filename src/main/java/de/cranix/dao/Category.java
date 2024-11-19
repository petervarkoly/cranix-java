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
@Table(
	name="Categories",
        uniqueConstraints = { @UniqueConstraint(columnNames = { "name","categoryType" }) }
)
@NamedQueries({
	@NamedQuery(name="Category.findAll",          query="SELECT c FROM Category c"),
	@NamedQuery(name="Category.getByName",        query="SELECT c FROM Category c where c.name = :name"),
	@NamedQuery(name="Category.getByDescription", query="SELECT c FROM Category c where c.description = :description"),
	@NamedQuery(name="Category.getByType",        query="SELECT c FROM Category c where c.categoryType = :type"),
	@NamedQuery(name="Category.search",           query="SELECT c FROM Category c WHERE c.name LIKE :search OR c.description = :search"),
	@NamedQuery(name="Category.expired",	      query="SELECT c FROM Category c WHERE c.validUntil < CURRENT_TIMESTAMP"),
	@NamedQuery(name="Category.expiredByType",    query="SELECT c FROM Category c WHERE c.validUntil < CURRENT_TIMESTAMP AND c.categoryType = :type")
})
public class Category extends AbstractEntity {

	@Column(name="description", length=64)
	@Size(max=64, message="Description must not be longer then 64 characters..")
	private String description;

	@Column(name="name", length=32)
	@Size(max=32, message="Name must not be longer then 32 characters..")
	private String name;

	@Column(name="categoryType", length=64)
	@Size(max=64, message="categoryType must not be longer then 64 characters..")
	private String categoryType;
	
	@Column(name="validFrom")
	@Temporal(TemporalType.TIMESTAMP)
	private Date validFrom;

	@Column(name="validUntil")
	@Temporal(TemporalType.TIMESTAMP)
	private Date validUntil;

	//bi-directional many-to-many association to Device
	@JsonIgnore
	@ManyToMany(cascade ={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
	@JoinTable(
		name="DeviceInCategories",
		joinColumns={ @JoinColumn(name="category_id", columnDefinition ="BIGINT UNSIGNED NOT NULL") },
		inverseJoinColumns={ @JoinColumn(name="device_id", columnDefinition ="BIGINT UNSIGNED NOT NULL") }
	)
	private List<Device> devices  = new ArrayList<Device>();

	@Transient
	private List<Long> deviceIds;

	//bi-directional many-to-many association to Group
	@JsonIgnore
	@ManyToMany(cascade ={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
	@JoinTable(
		name="GroupInCategories",
		joinColumns={ @JoinColumn(name="category_id", columnDefinition ="BIGINT UNSIGNED NOT NULL") },
		inverseJoinColumns={ @JoinColumn(name="group_id", columnDefinition ="BIGINT UNSIGNED NOT NULL") }
	)
	private List<Group> groups = new ArrayList<Group>();

	@Transient
	private List<Long> groupIds;

	//bi-directional many-to-many association to Group
	@JsonIgnore
	@ManyToMany(cascade ={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
	@JoinTable(
		name="HWConfInCategories",
		joinColumns={ @JoinColumn(name="category_id", columnDefinition ="BIGINT UNSIGNED NOT NULL") },
		inverseJoinColumns={ @JoinColumn(name="hwconf_id", columnDefinition ="BIGINT UNSIGNED NOT NULL") }
	)
	private List<HWConf> hwconfs = new ArrayList<>();

	@Transient
	private List<Long> hwconfIds;

	//bi-directional many-to-many association to Room
	@JsonIgnore
	@ManyToMany(cascade ={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
	@JoinTable(
		name="RoomInCategories",
		joinColumns={ @JoinColumn(name="category_id", columnDefinition ="BIGINT UNSIGNED NOT NULL") },
		inverseJoinColumns={ @JoinColumn(name="room_id", columnDefinition ="BIGINT UNSIGNED NOT NULL") }
	)
	private List<Room> rooms = new ArrayList<>();

	@Transient
	private List<Long> roomIds;

	//bi-directional many-to-many association to Software
	@JsonIgnore
	@ManyToMany(cascade ={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
	@JoinTable(
		name="SoftwareInCategories",
		joinColumns={ @JoinColumn(name="category_id", columnDefinition ="BIGINT UNSIGNED NOT NULL") },
		inverseJoinColumns={ @JoinColumn(name="software_id", columnDefinition ="BIGINT UNSIGNED NOT NULL") }
	)
	private List<Software> softwares = new ArrayList<>();

	@Transient
	private List<Long> softwareIds;

	//bi-directional many-to-many association to Software
	@JsonIgnore
	@ManyToMany(cascade ={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
	@JoinTable(
		name="SoftwareRemovedFromCategories",
		joinColumns={ @JoinColumn(name="category_id", columnDefinition ="BIGINT UNSIGNED NOT NULL") },
		inverseJoinColumns={ @JoinColumn(name="software_id", columnDefinition ="BIGINT UNSIGNED NOT NULL") }
	)
	private List<Software> removedSoftwares = new ArrayList<>();

	//bi-directional many-to-many association to User
	@JsonIgnore
	@ManyToMany(cascade ={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
	@JoinTable(
		name="UserInCategories",
		joinColumns={ @JoinColumn(name="category_id", columnDefinition ="BIGINT UNSIGNED NOT NULL") },
		inverseJoinColumns={ @JoinColumn(name="user_id", columnDefinition ="BIGINT UNSIGNED NOT NULL") }
	)
	private List<User> users = new ArrayList<User>();

	@Transient
	private List<Long> userIds;

	//bi-directional many-to-many association to Announcement
	@ManyToMany(cascade ={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
	@JoinTable(
		name="AnnouncementInCategories",
		joinColumns={ @JoinColumn(name="category_id", columnDefinition ="BIGINT UNSIGNED NOT NULL") },
		inverseJoinColumns={ @JoinColumn(name="announcement_id", columnDefinition ="BIGINT UNSIGNED NOT NULL") }
	)
	@JsonIgnore
	private List<Announcement> announcements = new ArrayList<>();

	@Transient
	private List<Long> announcementIds;


	//bi-directional many-to-many association to Contact
	@ManyToMany(cascade ={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
	@JoinTable(
		name="ContactInCategories",
		joinColumns={ @JoinColumn(name="category_id", columnDefinition ="BIGINT UNSIGNED NOT NULL") },
		inverseJoinColumns={ @JoinColumn(name="contact_id", columnDefinition ="BIGINT UNSIGNED NOT NULL") }
	)
	@JsonIgnore
	private List<Contact> contacts = new ArrayList<>();

	@Transient
	private List<Long> contactIds;

	//bi-directional many-to-many association to FAQ
	@ManyToMany(cascade ={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
	@JoinTable(
		name="FAQInCategories",
		joinColumns={ @JoinColumn(name="category_id", columnDefinition ="BIGINT UNSIGNED NOT NULL") },
		inverseJoinColumns={ @JoinColumn(name="faq_id", columnDefinition ="BIGINT UNSIGNED NOT NULL") }
	)
	@JsonIgnore
	private List<FAQ> faqs = new ArrayList<>();

	@Transient
	private List<Long> faqIds;

	@Column(name = "studentsOnly", columnDefinition = "CHAR(1) DEFAULT 'Y'")
	@Convert(converter=BooleanToStringConverter.class)
	boolean studentsOnly = false;

	@Column(name = "publicAccess", columnDefinition = "CHAR(1) DEFAULT 'Y'")
	@Convert(converter=BooleanToStringConverter.class)
	boolean publicAccess = false;
	
	public Category() {
		this.validFrom  = new Date(System.currentTimeMillis());
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
		if( this.announcementIds == null ) {
			this.announcementIds = new ArrayList<Long>();
			for (Announcement a : this.announcements) {
				this.announcementIds.add(a.getId());
			}
		}
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
		if( this.contactIds == null ) {
			this.contactIds = new ArrayList<Long>();
			for (Contact c : this.contacts) {
				this.contactIds.add(c.getId());
			}
		}
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
		if( this.deviceIds == null ) {
			this.deviceIds = new ArrayList<Long>();
			for (Device d : this.devices) {
				this.deviceIds.add(d.getId());
			}
		}
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
		if( this.faqIds == null ) {
			this.faqIds = new ArrayList<Long>();
			for (FAQ f: this.faqs) {
				this.faqIds.add(f.getId());
			}
		}
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
		if( this.groupIds == null ) {
			this.groupIds = new ArrayList<Long>();
			for (Group g: this.groups) {
				this.groupIds.add(g.getId());
			}
		}
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
		if( this.hwconfIds == null ) {
			this.hwconfIds = new ArrayList<Long>();
			for (HWConf h: this.hwconfs) {
				this.hwconfIds.add(h.getId());
			}
		}
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
		if( this.roomIds == null ) {
			this.roomIds = new ArrayList<Long>();
			for (Room r: this.rooms) {
				this.roomIds.add(r.getId());
			}
		}
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
		if( this.softwareIds == null ) {
			this.softwareIds = new ArrayList<Long>();
			for (Software s: this.softwares) {
				this.softwareIds.add(s.getId());
			}
		}
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
		if( this.userIds == null ) {
			this.userIds = new ArrayList<Long>();
			for (User u: this.users) {
				this.userIds.add(u.getId());
			}
		}
		return this.userIds;
	}

	public void setUserIds(List<Long> ids) {
		this.userIds = ids;
	}
}
