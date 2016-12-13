package de.openschoolserver.dao;

import java.io.Serializable;
import javax.persistence.*;
import java.util.Set;


/**
 * The persistent class for the Devices database table.
 * 
 */
@Entity
@Table(name="Devices")
@NamedQuery(name="Device.findAll", query="SELECT d FROM Device d")
public class Device implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private int id;

	private String cn;

	private int column;

	@Column(name="IP")
	private String ip;

	@Column(name="MAC")
	private String mac;

	private int row;

	private String wstype;

	//bi-directional many-to-many association to Device
	@ManyToMany
	@JoinTable(
		name="AvailablePrinters"
		, joinColumns={
			@JoinColumn(name="device_id")
			}
		, inverseJoinColumns={
			@JoinColumn(name="printer_id")
			}
		)
	private Set<Device> availablePrinters;

	//bi-directional many-to-many association to Device
	@ManyToMany(mappedBy="availablePrinters")
	private Set<Device> availableForDevices;

	//bi-directional many-to-many association to Device
	@ManyToMany
	@JoinTable(
		name="DefaultPrinters"
		, joinColumns={
			@JoinColumn(name="device_id")
			}
		, inverseJoinColumns={
			@JoinColumn(name="printer_id")
			}
		)
	private Set<Device> defaultPrinter;

	//bi-directional many-to-many association to Device
	@ManyToMany(mappedBy="defaultPrinter")
	private Set<Device> defaultForDevices;

	//bi-directional many-to-one association to HWConf
	@ManyToOne
	private HWConf hwconf;

	//bi-directional many-to-one association to Room
	@ManyToOne
	private Room room;

	//bi-directional many-to-one association to User
	@ManyToOne
	private User owner;

	//bi-directional many-to-many association to Room
	@ManyToMany(mappedBy="availablePrinters")
	private Set<Room> availableInRooms;

	//bi-directional many-to-many association to Room
	@ManyToMany(mappedBy="defaultPrinter")
	private Set<Room> defaultInRooms;

	//bi-directional many-to-one association to TestUser
	@OneToMany(mappedBy="device")
	private Set<TestUser> testUsers;

	//bi-directional many-to-many association to User
	@ManyToMany(mappedBy="loggedOn")
	private Set<User> loggedIn;

	public Device() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCn() {
		return this.cn;
	}

	public void setCn(String cn) {
		this.cn = cn;
	}

	public int getColumn() {
		return this.column;
	}

	public void setColumn(int column) {
		this.column = column;
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

	public String getWstype() {
		return this.wstype;
	}

	public void setWstype(String wstype) {
		this.wstype = wstype;
	}

	public Set<Device> getAvailablePrinters() {
		return this.availablePrinters;
	}

	public void setAvailablePrinters(Set<Device> availablePrinters) {
		this.availablePrinters = availablePrinters;
	}

	public Set<Device> getAvailableForDevices() {
		return this.availableForDevices;
	}

	public void setAvailableForDevices(Set<Device> availableForDevices) {
		this.availableForDevices = availableForDevices;
	}

	public Set<Device> getDefaultPrinter() {
		return this.defaultPrinter;
	}

	public void setDefaultPrinter(Set<Device> defaultPrinter) {
		this.defaultPrinter = defaultPrinter;
	}

	public Set<Device> getDefaultForDevices() {
		return this.defaultForDevices;
	}

	public void setDefaultForDevices(Set<Device> defaultForDevices) {
		this.defaultForDevices = defaultForDevices;
	}

	public HWConf getHwconf() {
		return this.hwconf;
	}

	public void setHwconf(HWConf hwconf) {
		this.hwconf = hwconf;
	}

	public Room getRoom() {
		return this.room;
	}

	public void setRoom(Room room) {
		this.room = room;
	}

	public User getOwner() {
		return this.owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public Set<Room> getAvailableInRooms() {
		return this.availableInRooms;
	}

	public void setAvailableInRooms(Set<Room> availableInRooms) {
		this.availableInRooms = availableInRooms;
	}

	public Set<Room> getDefaultInRooms() {
		return this.defaultInRooms;
	}

	public void setDefaultInRooms(Set<Room> defaultInRooms) {
		this.defaultInRooms = defaultInRooms;
	}

	public Set<TestUser> getTestUsers() {
		return this.testUsers;
	}

	public void setTestUsers(Set<TestUser> testUsers) {
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

	public Set<User> getLoggedIn() {
		return this.loggedIn;
	}

	public void setLoggedIn(Set<User> loggedIn) {
		this.loggedIn = loggedIn;
	}

}