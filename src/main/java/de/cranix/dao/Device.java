/* (c) Peter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * The persistent class for the Devices database table.
 *
 */
@Entity
@Table(name="Devices")
@NamedQueries( {
	@NamedQuery(name="Device.findAll",      query="SELECT d FROM Device d"),
	@NamedQuery(name="Device.findAllId",    query="SELECT d.id FROM Device d"),
	@NamedQuery(name="Device.getByIP",      query="SELECT d FROM Device d where d.ip = :IP OR d.wlanIp = :IP"),
	@NamedQuery(name="Device.getByMainIP",  query="SELECT d FROM Device d where d.ip = :IP"),
	@NamedQuery(name="Device.getByMAC",     query="SELECT d FROM Device d where d.mac = :MAC OR d.wlanMac = :MAC"),
	@NamedQuery(name="Device.getByName",    query="SELECT d FROM Device d where d.name = :name"),
	@NamedQuery(name="Device.search",       query="SELECT d FROM Device d where d.name LIKE :search OR d.ip LIKE :search OR d.wlanIp LIKE :search OR d.mac LIKE :search OR d.wlanMac LIKE :search" ),
})
@SequenceGenerator(name="seq", initialValue=1, allocationSize=100)
public class Device implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="seq")
	private Long id;

	@Column(name = "name", updatable = false, length = 32)
	@Size(max=32)
	@Pattern.List({
		/*@Pattern(
                 regexp = "^[^,~:@#$%\\^'\\.\\(\\)/\\\\\\{\\}_\\s\\*\\?<>\\|]+$",
                 flags = Pattern.Flag.CASE_INSENSITIVE,
                 message = "Device name must not contains following signs: ',~:$%^/\\.(){}#;_' and spaces."),*/
		@Pattern(
			regexp = "[a-z0-9-].*",
			flags = Pattern.Flag.CASE_INSENSITIVE,
			message = "Device name contains invalid characters. Use only a-z0-9-"),
		@Pattern(
			regexp = "^[^-].*",
			flags = Pattern.Flag.CASE_INSENSITIVE,
			message = "Device name must not start with '-'."),
		@Pattern(
			regexp = ".*[^-]$",
			flags = Pattern.Flag.CASE_INSENSITIVE,
			message = "Device name must not ends with '-'.")
	})
	private String name;

	@Column(name = "place")
	private int place;

	@Column(name = "roomRow")
	private int row;

	@Column(name="IP", length = 16)
	@Size(max=16, message="IP must not be longer then 16 characters.")
	private String ip;

	@Column(name="MAC", length = 17)
	@Size(max=17, message="MAC must not be longer then 17 characters.")
	private String mac;

	@Column(name="wlanIp", length = 16)
	@Size(max=16, message="WLAN-IP must not be longer then 16 characters.")
	private String wlanIp;

	@Column(name="wlanMac", length = 17)
	@Size(max=17, message="WLAN-MAC must not be longer then 17 characters.")
	private String wlanMac;

	@Column(name="serial", length = 32)
	@Size(max=32, message="Serial must not be longer then 32 characters.")
	private String serial;

	@Column(name="inventary", length = 32)
	@Size(max=32, message="Inventary must not be longer then 32 characters.")
	private String inventary;

	@Column(name="locality", length = 32)
	@Size(max=32, message="Locality must not be longer then 32 characters.")
	private String locality;

	@Column(name="counter")
	private Long   counter;

	//bi-directional many-to-many association to Category
	@ManyToMany(mappedBy="devices", cascade ={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH} )
	@JsonIgnore
	private List<Category> categories = new ArrayList<Category>();

	//bi-directional many-to-many association to Device
	@ManyToMany(cascade ={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
	@JoinTable(
			name="AvailablePrinters",
			joinColumns={ @JoinColumn(name="device_id")	},
			inverseJoinColumns={ @JoinColumn(name="printer_id") }
	)
	@JsonIgnore
	private List<Printer> availablePrinters = new ArrayList<Printer>();

	//bi-directional many-to-many association to Device
	@ManyToOne
	@JoinTable(
			name="DefaultPrinter",
			joinColumns={@JoinColumn(name="device_id")},
			inverseJoinColumns={@JoinColumn(name="printer_id")}
		)
	@JsonIgnore
	private Printer defaultPrinter;

	@OneToMany(mappedBy="device")
	@JsonIgnore
	private List<Printer> printerQueue = new ArrayList<Printer>();

	//bi-directional many-to-many association to Device
	@ManyToMany(mappedBy="devices", cascade ={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
	@JsonIgnore
	private List<SoftwareLicense> softwareLicenses = new ArrayList<SoftwareLicense>() ;

	//bi-directional many-to-one association to SoftwareStatus
	@OneToMany(mappedBy="device", cascade =CascadeType.ALL)
	@JsonIgnore
	private List<SoftwareStatus> softwareStatus = new ArrayList<SoftwareStatus>();

	//bi-directional many-to-one association to HWConf
	@ManyToOne
	@JsonIgnore
	private HWConf hwconf;

	@Column(name="hwconf_id", insertable=false, updatable=false)
	private Long hwconfId;

	//bi-directional many-to-one association to Room
	@ManyToOne
	@JsonIgnore
	private Room room;

	//bi-directional many-to-one association to Device
	@OneToMany(mappedBy="device", cascade =CascadeType.ALL )
	@JsonIgnore
	private List<Session> sessions = new ArrayList<Session>();

	@Column(name="room_id", insertable=false, updatable=false)
	private Long roomId;

	//bi-directional many-to-one association to User
	@ManyToOne
	@JsonIgnore
	private User owner;

	@Column(name="owner_id", insertable=false, updatable=false)
	private Long ownerId;

	@Transient
	private String ownerName;

	//bi-directional many-to-one association to TestUser
	@OneToMany(mappedBy="device")
	@JsonIgnore
	private List<TestUser> testUsers;

	//bi-directional many-to-many association to User
	@ManyToMany(mappedBy="loggedOn")
	@JsonIgnore
	private List<User> loggedIn = new ArrayList<User>();

	@Transient
	private Long loggedInId = 0L;

	@Transient
	private String loggedInName = "nobody";

	@Transient
	private char[] screenShot;

	public Device() {
		this.hwconfId = null;
		this.name = "";
		this.ip = "";
		this.mac = "";
		this.wlanIp   = "";
		this.wlanMac  = "";
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
		Device other = (Device) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public Long getHwconfId() {
		return this.hwconfId;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPlace() {
		return this.place;
	}

	public void setPlace(int place) {
		this.place = place;
	}

	public String getIp() {
		return this.ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getMac() {
		return this.mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public int getRow() {
		return this.row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public String getSerial() {
		return this.serial;
	}

	public void setSerial(String value) {
		this.serial = value;
	}

	public String getInventary() {
		return this.inventary;
	}

	public void setInventary(String value) {
		this.inventary = value;
	}

	public String getLocality() {
		return this.locality;
	}

	public void setLocality(String value) {
		this.locality = value;
	}

	public List<Printer> getAvailablePrinters() {
		return this.availablePrinters;
	}

	public void setAvailablePrinters(List<Printer> availablePrinters) {
		this.availablePrinters = availablePrinters;
	}

	public Printer getDefaultPrinter() {
		return this.defaultPrinter;
	}

	public void setDefaultPrinter(Printer defaultPrinter) {
		this.defaultPrinter = defaultPrinter;
	}

	public HWConf getHwconf() {
		return this.hwconf;
	}

	public void setHwconf(HWConf hwconf) {
		this.hwconf   = hwconf;
		this.hwconfId = hwconf.getId();
		if( ! hwconf.getDevices().contains(this) ) {
			hwconf.getDevices().add(this);
		}
	}

	public Room getRoom() {
		return this.room;
	}

	public void setRoom(Room room) {
		this.room = room;
		this.roomId = room.getId();
		if( ! room.getDevices().contains(this) ) {
			room.getDevices().add(this);
		}
	}

	public User getOwner() {
		return this.owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public List<TestUser> getTestUsers() {
		return this.testUsers;
	}

	public void setTestUsers(List<TestUser> testUsers) {
		this.testUsers = testUsers;
	}

	public TestUser addTestUser(TestUser testUser) {
		getTestUsers().add(testUser);
		testUser.setDevice(this);

		return testUser;
	}

	public TestUser removeTestUser(TestUser testUser) {
		getTestUsers().remove(testUser);
		testUser.setDevice(null);

		return testUser;
	}

	public List<User> getLoggedIn() {
		return this.loggedIn;
	}

	public void setLoggedIn(List<User> loggedIn) {
		this.loggedIn = loggedIn;
	}


	public List<Category> getCategories() {
	        return this.categories;
	}

	public void setCategories(List<Category> categories) {
	        this.categories = categories;
	}

	public String getWlanIp() {
		return wlanIp;
	}

	public void setWlanIp(String wlanip) {
		this.wlanIp = wlanip;
	}

	public String getWlanMac() {
		return wlanMac;
	}

	public void setWlanMac(String wlanmac) {
		this.wlanMac = wlanmac;
	}

	public List<SoftwareLicense> getSoftwareLicenses() {
		return softwareLicenses;
	}

	public void setSoftwareLicenses(List<SoftwareLicense> softwareLicenses) {
		this.softwareLicenses = softwareLicenses;
	}

	public List<SoftwareStatus> getSoftwareStatus() {
		return softwareStatus;
	}

	public void setSoftwareStatus(List<SoftwareStatus> softwareStatus) {
		this.softwareStatus = softwareStatus;
	}

	public void setHwconfId(Long hwconfId) {
		this.hwconfId = hwconfId;
	}

	public Long getCounter() {
		return counter;
	}

	public void setCounter(Long counter) {
		this.counter = counter;
	}

	public Long getRoomId() {
		return roomId;
	}

	public void setRoomId(Long roomId) {
		this.roomId = roomId;
	}

	public List<Printer> getPrinterQueue() {
		return printerQueue;
	}

	public void setPrinterQueue(List<Printer> printerQueue) {
		this.printerQueue = printerQueue;
	}

	public List<Session> getSessions() {
		return sessions;
	}

	public void setSessions(List<Session> sessions) {
		this.sessions = sessions;
	}

	public Long getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(Long ownerId) {
		this.ownerId = ownerId;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}

	public char[] getScreenShot() {
		return screenShot;
	}

	public void setScreenShot(char[] screenShot) {
		this.screenShot = screenShot;
	}

	public Long getLoggedInId() {
		return loggedInId;
	}

	public void setLoggedInId(Long loggedInId) {
		this.loggedInId = loggedInId;
	}

	public void setLoggedInName(String loggedInName) {
		this.loggedInName = loggedInName;
	}

	public String getLoggedInName() {
		return loggedInName;
	}

}
