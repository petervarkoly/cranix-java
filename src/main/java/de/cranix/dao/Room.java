/* (c) 2021 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.dao;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.persistence.*;
import javax.validation.constraints.Size;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * The persistent class for the Rooms database table.
 *
 */
@Entity
@Table(name="Rooms")
@NamedQueries ({
	@NamedQuery(name="Room.findAll",		   query="SELECT r FROM Room r WHERE r.roomType != 'smartRoom'"),
	@NamedQuery(name="Room.findAllWithControl",	   query="SELECT r FROM Room r WHERE r.roomType != 'smartRoom' AND r.roomControl != 'no'"),
	@NamedQuery(name="Room.findAllWithTeacherControl", query="SELECT r FROM Room r WHERE r.roomType != 'smartRoom' AND r.roomControl != 'no' AND r.roomControl != 'sysadminsOnly'"),
	@NamedQuery(name="Room.findAllWithFirewallControl",query="SELECT r FROM Room r WHERE r.roomType != 'smartRoom' AND r.roomControl = 'no'"),
	@NamedQuery(name="Room.findAllToUse",	           query="SELECT r FROM Room r WHERE r.roomType != 'smartRoom' AND r.name != 'ANON_DHCP' AND r.roomType != 'ANON_DHCP'"),
	@NamedQuery(name="Room.findAllToRegister",	   query="SELECT r FROM Room r WHERE r.roomType != 'smartRoom' AND r.name != 'ANON_DHCP' AND r.roomType != 'ANON_DHCP'"),
	@NamedQuery(name="Room.getByName",		   query="SELECT r FROM Room r WHERE r.name = :name"),
	@NamedQuery(name="Room.getByDescription",	   query="SELECT r FROM Room r WHERE r.description = :description"),
	@NamedQuery(name="Room.getByType",		   query="SELECT r FROM Room r WHERE r.roomType = :type"),
	@NamedQuery(name="Room.getByControl",	           query="SELECT r FROM Room r WHERE r.roomControl = :control"),
	@NamedQuery(name="Room.getByIp",		   query="SELECT r FROM Room r WHERE r.startIP = :ip"),
	@NamedQuery(name="Room.search",		           query="SELECT r FROM Room r WHERE r.name LIKE :search OR r.description LIKE :search OR r.roomType LIKE :search AND r.roomType != 'smartRoom'"),
	@NamedQuery(name="Room.findAllId",		   query="SELECT r.id FROM Room r WHERE r.roomType != 'smartRoom' AND r.roomType != 'adHocAccess'"),
	@NamedQuery(name="Room.findAllToUseId",	           query="SELECT r.id FROM Room r WHERE r.roomType != 'smartRoom' AND r.name != 'ANON_DHCP' AND r.roomType != 'ANON_DHCP'"),
	@NamedQuery(name="Room.getDeviceCount",	           query="SELECT COUNT( d ) FROM  Device d WHERE d.room.id = :id")
})
@SequenceGenerator(name="seq", initialValue=1, allocationSize=100)
public class Room implements Serializable {
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("serial")
	public static Map<Integer, Integer> nmToCount = new HashMap<Integer, Integer>() {{
		put(0,0);
		put(31,2);
		put(30,4);
		put(29,8);
		put(28,16);
		put(27,32);
		put(26,64);
		put(25,128);
		put(24,256);
		put(23,512);
		put(22,1024);
		put(21,2048);
		put(20,4096);
		put(19,8192);
	}};

	@SuppressWarnings("serial")
	public static Map<Integer, Integer> countToNm = new HashMap<Integer, Integer>() {{
		put(0,0);
		put(2,31);
		put(4,30);
		put(8,29);
		put(16,28);
		put(32,27);
		put(64,26);
		put(128,25);
		put(256,24);
		put(512,23);
		put(1024,22);
		put(2048,21);
		put(4096,20);
		put(8192,19);
	}};

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="seq")
	private Long id;

	@Column(name = "name", updatable = false)
	@Size(max=32, message="Name must not be longer then 10 characters.")
	private String name;

	private Integer places;

	@Column(name = "roomRows")
	private Integer rows;

	@Size(max=64, message="Description must not be longer then 64 characters.")
	private String description;

	@Column(name = "netMask", updatable = false)
	private Integer netMask;

	@Column(name = "startIP", updatable = false)
	private String startIP = "";

	private String roomType = "ComputerRoom";

	private String roomControl = "inRoom";

	//bi-directional many-to-many association to Category
	@ManyToMany(mappedBy="rooms")
	@JsonIgnore
	private List<Category> categories = new ArrayList<Category>();

	//bi-directional many-to-one association to AccessInRoom
	@OneToMany(mappedBy="room", cascade=CascadeType.ALL )
	@JsonIgnore
	private List<AccessInRoom> accessInRooms = new ArrayList<AccessInRoom>();

	//bi-directional many-to-one association to Device
	@OneToMany(mappedBy="room", cascade=CascadeType.ALL )
	@JsonIgnore
	private List<Device> devices = new ArrayList<Device>();

	//bi-directional many-to-one association to Device
	@OneToMany(mappedBy="room",cascade=CascadeType.ALL )
	@JsonIgnore
	private List<Session> sessions = new ArrayList<Session>();

	//bi-directional many-to-many association to Device
	@ManyToMany( cascade ={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH} )
	@JoinTable(
		name="AvailablePrinters",
		joinColumns={ @JoinColumn(name="room_id") },
		inverseJoinColumns={@JoinColumn(name="printer_id")}
		)
	@JsonIgnore
	private List<Printer> availablePrinters = new ArrayList<Printer>();

	//bi-directional many-to-one association to Device
	@ManyToOne( cascade ={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH} )
	@JoinTable(
		name="DefaultPrinter",
		joinColumns={ @JoinColumn(name="room_id") },
		inverseJoinColumns={ @JoinColumn(name="printer_id") }
		)
	@JsonIgnore
	private Printer defaultPrinter;

	//bi-directional many-to-one association to Test
	@OneToMany(mappedBy="room")
	@JsonIgnore
	private List<Test> tests = new ArrayList<Test>();

	//bi-directional many-to-one association to RoomSmartControl
	@OneToMany(mappedBy="room")
	@JsonIgnore
	private List<RoomSmartControl> smartControls = new ArrayList<RoomSmartControl>();

	@Transient
	private String network = "";

	//bi-directional many-to-one association to HWConf
	@ManyToOne
	@JsonIgnore
	private HWConf hwconf;

	@Column(name="hwconf_id", insertable=false, updatable=false)
	private Long hwconfId;

	//bi-directional many-to-one association to User
	@ManyToOne
	@JsonIgnore
	private User creator;

	@Transient
	private Integer devCount;

	//If this is set netbios conventions will be ignored.
	public boolean isIgnoreNetbios() {
		return ignoreNetbios;
	}

	public void setIgnoreNetbios(boolean ignoreNetbios) {
		this.ignoreNetbios = ignoreNetbios;
	}

	@Transient
	private boolean ignoreNetbios = false;

	public Room() {
		this.convertNmToCount();
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
		Room other = (Room) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public void convertNmToCount() {
		this.devCount = nmToCount.get(this.netMask);
	}

	public void convertCountToNm() {
		this.netMask = countToNm.get(this.devCount);
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

	public Integer getPlaces() {
		return this.places;
	}

	public void setPlaces(Integer places) {
		this.places = places;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getNetMask() {
		return this.netMask;
	}

	public void setNetMask(Integer netMask) {
		this.netMask = netMask;
	}

	public String getStartIP() {
		return this.startIP;
	}

	public void setStartIP(String startIP) {
		this.startIP = startIP;
	}

	public String getRoomType() {
		return this.roomType;
	}

	public void setRoomType(String roomtype) {
		this.roomType = roomtype;
	}

	public String getRoomControl() {
		return this.roomControl;
	}

	public void setRoomControl(String roomcontrol) {
		this.roomControl = roomcontrol;
	}

	public List<AccessInRoom> getAccessInRooms() {
		return this.accessInRooms;
	}

	public void setAccessInRoom(List<AccessInRoom> accessinrooms) {
		this.accessInRooms = accessinrooms;
	}

	public AccessInRoom addAccessInRoom(AccessInRoom accessinroom) {
		getAccessInRooms().add(accessinroom);
		accessinroom.setRoom(this);
		return accessinroom;
	}

	public AccessInRoom removeAccessInRoome(AccessInRoom accessinroom) {
		getAccessInRooms().remove(accessinroom);
		accessinroom.setRoom(null);
		return accessinroom;
	}

	public List<Device> getDevices() {
		return this.devices;
	}

	public void setDevices(List<Device> devices) {
		this.devices = devices;
	}

	public Device addDevice(Device device) {
		getDevices().add(device);
		device.setRoom(this);
		return device;
	}

	public Device removeDevice(Device device) {
		getDevices().remove(device);
		device.setRoom(null);
		return device;
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
	}

	public List<Test> getTests() {
		return this.tests;
	}

	public void setTests(List<Test> tests) {
		this.tests = tests;
	}

	public Test addTest(Test test) {
		getTests().add(test);
		test.setRoom(this);
		return test;
	}

	public Test removeTest(Test test) {
		getTests().remove(test);
		test.setRoom(null);
		return test;
	}

	public String getNetwork() {
		return this.network;
	}

	public void setNetwork(String network) {
		this.network = network;
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

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public void setAccessInRooms(List<AccessInRoom> accessInRooms) {
		this.accessInRooms = accessInRooms;
	}

	public void setSmartControls(List<RoomSmartControl> smartControls) {
		this.smartControls = smartControls;
	}

	public void setHwconfId(Long hwconfId) {
		this.hwconfId = hwconfId;
	}

	public List<Session> getSessions() {
		return sessions;
	}

	public void setSessions(List<Session> sessions) {
		this.sessions = sessions;
	}

	/**
	 * @return the roomRows
	 */
	public Integer getRows() {
		return rows;
	}

	/**
	 * @param roomRows the roomRows to set
	 */
	public void setRows(Integer roomRows) {
		this.rows = roomRows;
	}

	/**
	 * @return the devCount
	 */
	public Integer getDevCount() {
		return devCount;
	}

	/**
	 * @param devCount the devCount to set
	 */
	public void setDevCount(Integer devCount) {
		this.devCount = devCount;
	}

	/**
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
}
