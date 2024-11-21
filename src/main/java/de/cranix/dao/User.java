/* (c) 2024 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.dao;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;
import javax.persistence.*;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.annotations.CacheType;

import de.cranix.helper.SslCrypto;


/**
 * The persistent class for the Users database table.
 */
@Entity
@Table(
	name="Users",
	uniqueConstraints = { @UniqueConstraint(columnNames = { "uid" }) }
)
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
public class User extends AbstractEntity {
	SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");

	@Column(name="uid", updatable=false, length=32)
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
	private String uid = "";

	@Column(name="uuid", updatable=false, length=64)
	@Size(max=64, message="UUID must not be longer then 64 characters.")
	private String uuid = "";

	@Column(name="givenName", length=64)
	@Size(max=64, message="Givenname must not be longer then 64 characters.")
	private String givenName = "";

	@Column(name="surName", length=64)
	@Size(max=64, message="Surname must not be longer then 64 characters.")
	private String surName = "";

	@Column(name="role", length=16)
	@Size(max=16, message="Role must not be longer then 16 characters.")
	private String role = "";

	@Column(name="birthDay", columnDefinition = "DATE NOT NULL DEFAULT NOW()")
	private String birthDay = fmt.format(new Date());

	@Column(name="fsQuotaUsed")
	private Integer fsQuotaUsed = 0;

	@Column(name="fsQuota")
	private Integer fsQuota = 0;

	@Column(name="msQuotaUsed")
	private Integer msQuotaUsed = 0;

	@Column(name="msQuota")
	private Integer msQuota = 0;

	@Column(name="color", length=7)
	@Size(max=7, message="color must not be longer then 7 characters.")
	protected String color = "#4832a8";

	@JsonIgnore
	@Column(name="initialPassword", length=32)
	@Size(max=32, message="initialPassword must not be longer then 32 characters.")
	private String initialPassword = "";

	/* bi-directional many-to-one associations */
	@OneToMany(mappedBy="user", cascade ={CascadeType.ALL})
	@JsonIgnore
	private List<Alias> aliases = new ArrayList<Alias>();

	@OneToMany(mappedBy="user")
	@JsonIgnore
	private List<Acl> acls = new ArrayList<Acl>();

	@OneToMany(mappedBy="creator")
	@JsonIgnore
	private List<Acl> createdAcls = new ArrayList<Acl>();

	@OneToMany(mappedBy="creator")
	@JsonIgnore
	private List<Partition> createdPartitions = new ArrayList<Partition>();

	@OneToMany(mappedBy="creator")
	@JsonIgnore
	private List<User> createdUsers = new ArrayList<User>();

	@OneToMany(mappedBy="user", cascade ={CascadeType.ALL})
	@JsonIgnore
	private List<Session> sessions = new ArrayList<Session>();

	@OneToMany(mappedBy="creator")
	@JsonIgnore
	private List<Room> createdRooms = new ArrayList<Room>();

	//bi-directional many-to-one association to HWConfs
	@OneToMany(mappedBy="creator")
	@JsonIgnore
	private List<HWConf> createdHWConfs = new ArrayList<HWConf>();

	@OneToMany(mappedBy="creator")
	@JsonIgnore
	private List<AccessInRoom> createdAccessInRoom = new ArrayList<AccessInRoom>();

	@OneToMany(mappedBy="creator")
	@JsonIgnore
	private List<CrxConfig> createdCrxConfig = new ArrayList<CrxConfig>();

	@OneToMany(mappedBy="creator")
	@JsonIgnore
	private List<CrxMConfig> createdCrxMConfig = new ArrayList<CrxMConfig>();

	@OneToMany(mappedBy="creator")
	@JsonIgnore
	private List<Device> ownedDevices = new ArrayList<Device>();

	@OneToMany(mappedBy="creator")
	@JsonIgnore
	private List<PositiveList> ownedPositiveLists = new ArrayList<PositiveList>();

	@OneToMany(mappedBy="creator")
	@JsonIgnore
	private List<Group> ownedGroups = new ArrayList<Group>();

	@OneToMany(mappedBy="creator")
	@JsonIgnore
	private List<Category> ownedCategories = new ArrayList<Category>();

	@OneToMany(mappedBy="creator", cascade ={CascadeType.ALL}, orphanRemoval=true)
	@JsonIgnore
	private List<RoomSmartControl> smartControls = new ArrayList<RoomSmartControl>();

	@OneToMany(mappedBy="creator")
	@JsonIgnore
	private List<FAQ> myFAQs = new ArrayList<FAQ>();

	@OneToMany(mappedBy="creator")
	@JsonIgnore
	private List<Contact> myContacts = new ArrayList<Contact>();

	@OneToMany(mappedBy="creator")
	@JsonIgnore
	private List<Announcement> myAnnouncements = new ArrayList<Announcement>();

	@OneToMany(mappedBy="creator")
	@JsonIgnore
	public List<CrxChallenge> challenges = new ArrayList<CrxChallenge>();

	@OneToMany(mappedBy="creator", cascade ={CascadeType.ALL}, orphanRemoval=true)
	@JsonIgnore
	private List<TaskResponse> taskResponses = new ArrayList<TaskResponse>();

	@OneToMany(mappedBy="creator")
	@JsonIgnore
	private List<CrxCalendar> createdEvents = new ArrayList<CrxCalendar>();

	/* bi-directional many-to-many associations */
	@ManyToMany(mappedBy="users")
	@JsonIgnore
	private List<Category> categories = new ArrayList<Category>();

	@ManyToMany(mappedBy="users")
	@JsonIgnore
	private List<CrxChallenge> todos = new ArrayList<CrxChallenge>();

	@ManyToMany
	@JoinTable(
		name="LoggedOn",
		joinColumns={ @JoinColumn(name="user_id", columnDefinition ="BIGINT UNSIGNED NOT NULL") },
		inverseJoinColumns={@JoinColumn(name="device_id", columnDefinition ="BIGINT UNSIGNED NOT NULL")}
	)
	@JsonIgnore
	private List<Device> loggedOn = new ArrayList<Device>();

	@ManyToMany( cascade ={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH} )
	@JoinTable(
		name="GroupMember",
		joinColumns={@JoinColumn(name="user_id", columnDefinition ="BIGINT UNSIGNED NOT NULL")},
		inverseJoinColumns={@JoinColumn(name="group_id", columnDefinition ="BIGINT UNSIGNED NOT NULL")}
	)
	@JsonIgnore
	private List<Group> groups = new ArrayList<Group>();

	@ManyToMany( cascade ={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH} )
	@JoinTable(
		name="HaveSeen",
		joinColumns={@JoinColumn(name="user_id", columnDefinition ="BIGINT UNSIGNED NOT NULL")},
		inverseJoinColumns={@JoinColumn(name="announcement_id", columnDefinition ="BIGINT UNSIGNED NOT NULL")}
	)
	@JsonIgnore
	private List<Announcement> readAnnouncements = new ArrayList<Announcement>();

	/* transient attributes */
	@Transient
	private String classes;

	@Transient
	List<String> mailAliases = new ArrayList<String>();

	@Transient
	String fullName;

	@OneToMany(fetch = FetchType.EAGER, mappedBy="creator", cascade = {CascadeType.REMOVE}, orphanRemoval=true)
	@JsonIgnore
	private List<Crx2fa> crx2fas;
	public List<Crx2fa> getCrx2fas() {
		return crx2fas;
	}

	public void addCrx2fa(Crx2fa crx2fa) {
		if(!this.crx2fas.contains(crx2fa)) {
			this.crx2fas.add(crx2fa);
			crx2fa.setCreator(this);
		}
	}

	@OneToMany( mappedBy="creator", cascade = {CascadeType.REMOVE}, orphanRemoval=true)
	@JsonIgnore
	private List<Crx2faSession> crx2faSessions = new ArrayList<>();

	public List<Crx2faSession> getCrx2faSessions() {
		return this.crx2faSessions;
	}

	public void addCrx2faSession(Crx2faSession crx2faSession){
		if(!this.crx2faSessions.contains(crx2faSession)){
			this.crx2faSessions.add(crx2faSession);
			crx2faSession.setCreator(this);
		}
	}

	@ManyToMany(mappedBy="users")
	@JsonIgnore
	private List<CrxCalendar> events = new ArrayList<>();

	@Transient
	private String password ="";

	@Transient
	private boolean mustChange = false;

	public User() {	}
	public User(Session session) { this.setCreator(session.getUser()); }

	public boolean isMustChange() {
		return mustChange;
	}

	public void setMustChange(boolean mustChange) {
		this.mustChange = mustChange;
	}

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

	public String getBirthDay() {
		return this.birthDay;
	}

	public void setBirthDay(String birthday) {
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

	public List<CrxCalendar> getCreatedEvents() { return createdEvents;	}

	public void setCreatedEvents(List<CrxCalendar> createdEvents) { this.createdEvents = createdEvents;	}

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
		ownedDevice.setCreator(this);
		return ownedDevice;
	}

	public Device removeOwnedDevice(Device ownedDevice) {
		getOwnedDevices().remove(ownedDevice);
		ownedDevice.setCreator(null);
		return ownedDevice;
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

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
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

	public String getFullName() {
		StringBuilder fullName = new StringBuilder(this.uid);
		fullName.append(" (").append(this.surName).append(" ").append(this.givenName).append(")");
		return fullName.toString();
	}

	public List<CrxChallenge> getChallenges() {
		return challenges;
	}

	public void setChallenges(List<CrxChallenge> challenges) {
		this.challenges = challenges;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public List<CrxChallenge> getTodos() {
		return todos;
	}

	public void setTodos(List<CrxChallenge> todos) {
		this.todos = todos;
	}

	public List<Room> getCreatedRooms() {
		return createdRooms;
	}

	public void setCreatedRooms(List<Room> createdRooms) {
		this.createdRooms = createdRooms;
	}

	public List<HWConf> getCreatedHWConfs() {
		return createdHWConfs;
	}

	public void setCreatedHWConfs(List<HWConf> createdHWConfs) {
		this.createdHWConfs = createdHWConfs;
	}

	public List<TaskResponse> getTaskResponses() {
		return taskResponses;
	}

	public void setTaskResponses(List<TaskResponse> taskResponses) {
		this.taskResponses = taskResponses;
	}

	public void addTaskResponse(TaskResponse taskResponse) {
		if( !taskResponses.contains(taskResponse)) {
			taskResponses.add(taskResponse);
			taskResponse.setCreator(this);
		}
	}

	public void deleteTaskResponse(TaskResponse taskResponse) {
		if( taskResponses.contains(taskResponse)) {
			taskResponses.remove(taskResponse);
			taskResponse.setCreator(null);
		}
	}

	public void setCrx2fas(List<Crx2fa> crx2fas) {
		this.crx2fas = crx2fas;
	}

	public void setCrx2faSessions(List<Crx2faSession> crx2faSessions) {
		this.crx2faSessions = crx2faSessions;
	}

	public List<CrxCalendar> getEvents() {
		return events;
	}

	public void setEvents(List<CrxCalendar> events) {
		this.events = events;
	}

	public void addEvent(CrxCalendar event) {
		if(!events.contains(event)) {
			events.add(event);
			event.getUsers().add(this);
		}
	}

	public void removeEvent(CrxCalendar event) {
		if(events.contains((event))) {
			events.remove(event);
			event.getUsers().remove(this);
		}
	}
}
