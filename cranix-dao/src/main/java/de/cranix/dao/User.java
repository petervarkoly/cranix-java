/* (c) 2018 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.dao;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.annotations.CacheType;

import de.cranix.dao.tools.SslCrypto;


/**
 * The persistent class for the Users database table.
 */
@Entity
@Table(name="Users")
@NamedQueries({
	@NamedQuery(name="User.findAll", query="SELECT u FROM User u WHERE NOT u.role = 'internal'"),
	@NamedQuery(name="User.findAllId", query="SELECT u.id FROM User u WHERE NOT u.role = 'internal'"),
	@NamedQuery(name="User.findAllStudents", query="SELECT u FROM User u WHERE u.role = 'students' "),
	@NamedQuery(name="User.findAllTeachers", query="SELECT u FROM User u WHERE u.role = 'teachers' "),
	@NamedQuery(name="User.getByRole",  query="SELECT u FROM User u WHERE u.role = :role "),
	@NamedQuery(name="User.getByUid",   query="SELECT u FROM User u WHERE u.uid = :uid "),
	@NamedQuery(name="User.findByName",   query="SELECT u FROM User u WHERE u.givenName = :givenName and u.surName = :surName"),
	@NamedQuery(name="User.findByNameAndRole",   query="SELECT u FROM User u WHERE u.givenName = :givenName and u.surName = :surName and u.role = :role"),
	@NamedQuery(name="User.search", query="SELECT u FROM User u WHERE u.uid LIKE :search OR u.givenName LIKE :search OR u.surName LIKE :search")
})
@Cache(
		  type=CacheType.SOFT, // Cache everything until the JVM decides memory is low.
		  size=64000
)
@SequenceGenerator(name="seq", initialValue=1, allocationSize=100)
public class User implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="seq")
	Long id;

	@Size(max=64, message="Givenname must not be longer then 64 characters.")
	private String givenName;

	private String role;

	@Size(max=64, message="Surname must not be longer then 64 characters.")
	private String surName;

	@Column(name="uid", updatable=false)
	@Pattern.List({
		@Pattern(
                        regexp = "^[^/\\\\#,;=]+$",
                        flags = Pattern.Flag.CASE_INSENSITIVE,
                        message = "Uid must not contains: '/' '\\' '#' ',' ';' '='."),
		@Pattern(
                regexp = "^[^-\\.].*",
                flags = Pattern.Flag.CASE_INSENSITIVE,
                message = "Uid must not start with '-' '.'.")
	})
	@Size(max=32, message="Uid must not be longer then 32 characters.")
	private String uid;

	@Size(max=64, message="UUID must not be longer then 64 characters.")
	private String uuid;

	@Temporal(TemporalType.DATE)
	@Past
	private Date birthDay;

	//bi-directional many-to-one association to Alias
	@OneToMany(mappedBy="user", cascade ={CascadeType.ALL})
	@JsonIgnore
	private List<Alias> aliases = new ArrayList<Alias>();

	//bi-directional many-to-one association to Acls
	@OneToMany(mappedBy="user", cascade ={CascadeType.ALL})
	@JsonIgnore
	private List<Acl> acls = new ArrayList<Acl>();

	//bi-directional many-to-one association to Acls
	@OneToMany(mappedBy="creator")
	@JsonIgnore
	private List<Acl> createdAcls = new ArrayList<Acl>();

	//bi-directional many-to-one association to Rooms
	@OneToMany(mappedBy="creator")
	@JsonIgnore
	private List<Partition> createdPartitions = new ArrayList<Partition>();

	//bi-directional many-to-one association to Rooms
	@OneToMany(mappedBy="creator")
	@JsonIgnore
	private List<User> createdUsers = new ArrayList<User>();

	//bi-directional many-to-one association to Device
	@OneToMany(mappedBy="user", cascade ={CascadeType.ALL})
	@JsonIgnore
	private List<Session> sessions = new ArrayList<Session>();

	@Transient
	List<String> mailAliases = new ArrayList<String>();

	public List<Partition> getCreatedPartitions() {
		return createdPartitions;
	}

	public void setCreatedPartitions(List<Partition> createdPartitions) {
		this.createdPartitions = createdPartitions;
	}

	public List<User> getCreatedUsers() {
		return createdUsers;
	}

	public void setCreatedUsers(List<User> createdUsers) {
		this.createdUsers = createdUsers;
	}

	//bi-directional many-to-one association to Rooms
	@OneToMany(mappedBy="creator")
	@JsonIgnore
	private List<Room> createdRooms = new ArrayList<Room>();

	public List<Room> getCreatedRooms() {
		return createdRooms;
	}

	public void setCreatedRooms(List<Room> createdRooms) {
		this.createdRooms = createdRooms;
	}

	//bi-directional many-to-one association to HWConfs
	@OneToMany(mappedBy="creator")
	@JsonIgnore
	private List<HWConf> createdHWConfs = new ArrayList<HWConf>();

	public List<HWConf> getCreatedHWConfs() {
		return createdHWConfs;
	}

	public void setCreatedHWConfs(List<HWConf> createdHWConfs) {
		this.createdHWConfs = createdHWConfs;
	}

	//bi-directional many-to-one association to Alias
	@OneToMany(mappedBy="creator")
	@JsonIgnore
	private List<AccessInRoom> createdAccessInRoom = new ArrayList<AccessInRoom>();

	//bi-directional many-to-one association to CrxConfig
	@OneToMany(mappedBy="creator")
	@JsonIgnore
	private List<CrxConfig> createdCrxConfig = new ArrayList<CrxConfig>();

	//bi-directional many-to-one association to CrxConfig
	@OneToMany(mappedBy="creator")
	@JsonIgnore
	private List<CrxMConfig> createdCrxMConfig = new ArrayList<CrxMConfig>();

	//bi-directional many-to-one association to Device
	@OneToMany(mappedBy="owner")
	@JsonIgnore
	private List<Device> ownedDevices = new ArrayList<Device>();

	//bi-directional many-to-one association to Device
	@OneToMany(mappedBy="owner")
	@JsonIgnore
	private List<PositiveList> ownedPositiveLists = new ArrayList<PositiveList>();

	//bi-directional many-to-one association to groups
	@OneToMany(mappedBy="owner")
	@JsonIgnore
	private List<Group> ownedGroups = new ArrayList<Group>();

	//bi-directional many-to-one association to Device
	@OneToMany(mappedBy="owner")
	@JsonIgnore
	private List<Category> ownedCategories = new ArrayList<Category>();

	//bi-directional many-to-one association to TestFile
	@OneToMany(mappedBy="user")
	@JsonIgnore
	private List<TestFile> testFiles = new ArrayList<TestFile>();

	//bi-directional many-to-one association to TestUser
	@OneToMany(mappedBy="user")
	@JsonIgnore
	private List<TestUser> testUsers = new ArrayList<TestUser>();

	//bi-directional many-to-one association to Test
	@OneToMany(mappedBy="user")
	@JsonIgnore
	private List<Test> tests = new ArrayList<Test>();

	//bi-directional many-to-one association to RoomSmartControl
	@OneToMany(mappedBy="owner", cascade ={CascadeType.ALL}, orphanRemoval=true)
	@JsonIgnore
	private List<RoomSmartControl> smartControls = new ArrayList<RoomSmartControl>();

	//bi-directional many-to-one association to Device
	@OneToMany(mappedBy="owner")
	@JsonIgnore
	private List<FAQ> myFAQs = new ArrayList<FAQ>();

	//bi-directional many-to-one association to Device
	@OneToMany(mappedBy="owner")
	@JsonIgnore
	private List<Contact> myContacts = new ArrayList<Contact>();

	//bi-directional many-to-one association to Device
	@OneToMany(mappedBy="owner")
	@JsonIgnore
	private List<Announcement> myAnnouncements = new ArrayList<Announcement>();

	//bi-directional many-to-many association to Category
	@ManyToMany(mappedBy="users")
	@JsonIgnore
	private List<Category> categories = new ArrayList<Category>();

	//bi-directional many-to-many association to Device
	@ManyToMany
	@JoinTable(
		name="LoggedOn",
		joinColumns={ @JoinColumn(name="user_id") },
		inverseJoinColumns={@JoinColumn(name="device_id")}
	)
	@JsonIgnore
	private List<Device> loggedOn = new ArrayList<Device>();

	//bi-directional many-to-many association to Group
	@ManyToMany( cascade ={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH} )
	@JoinTable(
		name="GroupMember",
		joinColumns={@JoinColumn(name="user_id")},
		inverseJoinColumns={@JoinColumn(name="group_id")}
	)
	@JsonIgnore
	private List<Group> groups = new ArrayList<Group>();

	@Transient
	private String classes;

	//bi-directional many-to-many association to Announcements
	@ManyToMany( cascade ={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH} )
	@JoinTable(
		name="HaveSeen",
		joinColumns={@JoinColumn(name="user_id")},
		inverseJoinColumns={@JoinColumn(name="announcement_id")}
	)
	//@JsonManagedReference
	@JsonIgnore
	private List<Announcement> readAnnouncements = new ArrayList<Announcement>();

	private Integer fsQuotaUsed;
	private Integer fsQuota;
	private Integer msQuotaUsed;
	private Integer msQuota;

	//bi-directional many-to-one association to User
	@ManyToOne
	@JsonIgnore
	private User creator;

	@JsonIgnore
	private String initialPassword;

	@Transient
	private String password ="";

	@Transient
	private boolean mustChange = false;

	public User() {
		this.id  = null;
		this.uid = "";
		this.uuid = "";
		this.surName = "";
		this.givenName = "";
		this.password = "";
		this.role = "";
		this.fsQuota = 0;
		this.fsQuotaUsed = 0;
		this.msQuota = 0;
		this.msQuotaUsed = 0;
		this.birthDay = new Date(System.currentTimeMillis());
		this.mustChange = false;
	}

	public boolean isMustChange() {
		return mustChange;
	}

	public void setMustChange(boolean mustChange) {
		this.mustChange = mustChange;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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
		User other = (User) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	public String getGivenName() {
		return this.givenName;
	}

	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	public String getRole() {
		return this.role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getSurName() {
		return this.surName;
	}

	public void setSurName(String surname) {
		this.surName = surname;
	}

	public String getUid() {
		return this.uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getUuid() {
		return this.uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public Date getBirthDay() {
		return this.birthDay;
	}

	public void setBirthDay(Date birthday) {
		this.birthDay = birthday;
	}

	public List<Alias> getAliases() {
		return this.aliases;
	}

	public void setAliases(List<Alias> aliases) {
		this.aliases = aliases;
	}

	public void addAlias(Alias alias) {
		getAliases().add(alias);
		alias.setUser(this);
	}

	public void removeAlias(Alias alias) {
		getAliases().remove(alias);
		alias.setUser(null);
	}

	public List<Acl> getAcls() {
		return this.acls;
	}

	public void setAcls(List<Acl> acls) {
		this.acls = acls;
	}

	public void addAcl(Acl acl) {
		getAcls().add(acl);
		acl.setUser(this);
	}

	public void removeAcl(Acl acl) {
		getAcls().remove(acl);
		acl.setUser(null);
	}

	public List<Category> getOwnedCategories() {
		return this.ownedCategories;
	}

	public List<Device> getOwnedDevices() {
		return this.ownedDevices;
	}

	public void setOwnedDevices(List<Device> ownedDevices) {
		this.ownedDevices = ownedDevices;
	}

	public List<Group> getOwnedGroups() {
		return this.ownedGroups;
	}

	public void setOwnedGroups(List<Group> ownedGroups) {
		this.ownedGroups = ownedGroups;
	}

	public Device addOwnedDevice(Device ownedDevice) {
		getOwnedDevices().add(ownedDevice);
		ownedDevice.setOwner(this);
		return ownedDevice;
	}

	public Device removeOwnedDevice(Device ownedDevice) {
		getOwnedDevices().remove(ownedDevice);
		ownedDevice.setOwner(null);
		return ownedDevice;
	}

	public List<TestFile> getTestFiles() {
		return this.testFiles;
	}

	public void setTestFiles(List<TestFile> testFiles) {
		this.testFiles = testFiles;
	}

	public TestFile addTestFile(TestFile testFile) {
		getTestFiles().add(testFile);
		testFile.setUser(this);
		return testFile;
	}

	public TestFile removeTestFile(TestFile testFile) {
		getTestFiles().remove(testFile);
		testFile.setUser(null);
		return testFile;
	}

	public List<TestUser> getTestUsers() {
		return this.testUsers;
	}

	public void setTestUsers(List<TestUser> testUsers) {
		this.testUsers = testUsers;
	}

	public TestUser addTestUser(TestUser testUser) {
		getTestUsers().add(testUser);
		testUser.setUser(this);
		return testUser;
	}

	public TestUser removeTestUser(TestUser testUser) {
		getTestUsers().remove(testUser);
		testUser.setUser(null);
		return testUser;
	}

	public List<Test> getTests() {
		return this.tests;
	}

	public void setTests(List<Test> tests) {
		this.tests = tests;
	}

	public Test addTest(Test test) {
		getTests().add(test);
		test.setUser(this);
		return test;
	}

	public Test removeTest(Test test) {
		getTests().remove(test);
		test.setUser(null);
		return test;
	}

	public List<Device> getLoggedOn() {
		return this.loggedOn;
	}

	public void setLoggedOn(List<Device> loggedOn) {
		this.loggedOn = loggedOn;
	}

	public List<Group> getGroups() {
		return this.groups;
	}

	public void setGroups(List<Group> groups) {
		this.groups = groups;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return this.password;
	}

	public void setFsQuotaUsed(Integer quota) {
		this.fsQuotaUsed = quota;
	}

	public void setFsQuota(Integer quota) {
		this.fsQuota = quota;
	}

	public void setMsQuotaUsed(Integer quota) {
		this.msQuotaUsed = quota;
	}

	public void setMsQuota(Integer quota) {
		this.msQuota = quota;
	}

	public Integer getFsQuotaUsed() {
		return this.fsQuotaUsed;
	}

	public Integer getFsQuota() {
		return this.fsQuota;
	}

	public Integer getMsQuotaUsed() {
		return this.msQuotaUsed;
	}

	public Integer getMsQuota() {
		return this.msQuota;
	}
	public List<Category> getCategories() {
		return this.categories;
	}

	public void setCategories(List<Category> categories) {
		this.categories = categories;
	}

	public List<RoomSmartControl> getSmartControls() {
		return this.smartControls;
	}
	public List<Announcement> getReadAnnouncements() {
		return this.readAnnouncements;
	}

	public void setReadAnnouncements(List<Announcement> announcements) {
		this.readAnnouncements = announcements;
	}

	public List<Announcement> getMyAnnouncements() {
		return this.myAnnouncements;
	}

	public void setAnnouncement(List<Announcement> values) {
		this.myAnnouncements = values;
	}

	public List<Contact> getMyContacts() {
		return this.myContacts;
	}

	public void setMyContacts(List<Contact> values) {
		this.myContacts = values;
	}

	public List<FAQ> getMyFAQs() {
		return this.myFAQs;
	}

	public void setMyFAQs(List<FAQ> values) {
		this.myFAQs = values;
	}

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public void setOwnedCategories(List<Category> ownedCategories) {
		this.ownedCategories = ownedCategories;
	}

	public void setSmartControls(List<RoomSmartControl> smartControls) {
		this.smartControls = smartControls;
	}

	public void setMyAnnouncements(List<Announcement> myAnnouncements) {
		this.myAnnouncements = myAnnouncements;
	}

	public String getInitialPassword() {
		return SslCrypto.deCrypt(this.initialPassword);
	}

	public void setInitialPassword(String initialPassword) {
		this.initialPassword = SslCrypto.enCrypt(initialPassword);
	}

	public List<PositiveList> getOwnedPositiveLists() {
		return this.ownedPositiveLists;
	}

	public void setOwnedPositiveLists(List<PositiveList> ownedPositiveLists) {
		this.ownedPositiveLists = ownedPositiveLists;
	}

	public List<Acl> getCreatedAcls() {
		return createdAcls;
	}

	public void setCreatedAcls(List<Acl> createdAcls) {
		this.createdAcls = createdAcls;
	}

	public List<AccessInRoom> getCreatedAccessInRoom() {
		return createdAccessInRoom;
	}

	public void setCreatedAccessInRoom(List<AccessInRoom> createdAccessInRoom) {
		this.createdAccessInRoom = createdAccessInRoom;
	}

	public List<Session> getSessions() {
		return sessions;
	}

	public void setSessions(List<Session> sessions) {
		this.sessions = sessions;
	}

	public String getClasses() {
		List<String> classesL = new ArrayList<String>();
		for( Group group : this.getGroups()) {
			if (group.getGroupType().equals("class")) {
				classesL.add(group.getName());
			}
		}
		classes = String.join(",", classesL);
		return classes;
	}

	public void setClasses(String classes) {
		this.classes = classes;
	}

	public List<String> getMailAliases() {
		return mailAliases;
	}

	public void setMailAliases(List<String> mailAliases) {
		this.mailAliases = mailAliases;
	}

	public List<CrxConfig> getCreatedCrxConfig() {
		return createdCrxConfig;
	}

	public void setCreatedCrxConfig(List<CrxConfig> createdCrxConfig) {
		this.createdCrxConfig = createdCrxConfig;
	}

	public List<CrxMConfig> getCreatedCrxMConfig() {
		return createdCrxMConfig;
	}

	public void setCreatedCrxMConfig(List<CrxMConfig> createdCrxMConfig) {
		this.createdCrxMConfig = createdCrxMConfig;
	}
}
