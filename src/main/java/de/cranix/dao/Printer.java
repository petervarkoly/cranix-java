/* (c) 2024 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.dao;

import java.io.Serializable;
import java.util.List;

import javax.persistence.*;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(
	name="Printers",
        uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }) }
)
@NamedQueries({
	@NamedQuery(name="Printer.findAll",   query="SELECT p FROM Printer p"),
	@NamedQuery(name="Printer.findAllId", query="SELECT p.id FROM Printer p"),
	@NamedQuery(name="Printer.getByName", query="SELECT p FROM Printer p WHERE p.name = :name")
})
@SequenceGenerator(name="seq", initialValue=1, allocationSize=100)
public class Printer extends AbstractEntity  {

	@Column(name = "name")
        @Size(max=32, message="name must not be longer then 32 characters.")
	private String    name;

	//bi-directional many-to-one association to HWConf
	@ManyToOne
	@JsonIgnore
	@JoinColumn(name="device_id", columnDefinition ="BIGINT UNSIGNED NOT NULL AUTO_INCREMENT")
	private Device    device;

	//bi-directional many-to-many association to Device
	@ManyToMany(mappedBy="availablePrinters",cascade ={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
	@JsonIgnore
	private List<Device> availableForDevices;

	//bi-directional many-to-many association to Device
	@OneToMany(mappedBy="defaultPrinter")
	@JsonIgnore
	private List<Device> defaultForDevices;

	//bi-directional many-to-many association to Room
	@ManyToMany(mappedBy="availablePrinters", cascade ={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
	@JsonIgnore
	private List<Room> availableInRooms;

	//bi-directional many-to-many association to Room
	@OneToMany(mappedBy="defaultPrinter")
	@JsonIgnore
	private List<Room> defaultInRooms;

	// Transient variable:

	@Transient
	private String    manufacturer;

	@Transient
	private String    model;

	@Transient
	private String    mac;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	@Transient
	private String  ip;

	@Transient
	private Long      roomId;

	@Transient
	private String     deviceName;

	@Transient
	private boolean   windowsDriver;

	/*
	 * State variables
	 */
	@Transient
	private String    state;

	@Transient
	private boolean   acceptingJobs;

	@Transient
	private int       activeJobs;

	public Printer() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public boolean isAcceptingJobs() {
		return acceptingJobs;
	}

	public void setAcceptingJobs(boolean acceptingJobs) {
		this.acceptingJobs = acceptingJobs;
	}

	public int getActiveJobs() {
		return activeJobs;
	}

	public void setActiveJobs(int activeJobs) {
		this.activeJobs = activeJobs;
	}

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public Long getRoomId() {
		return roomId;
	}

	public void setRoomId(Long roomId) {
		this.roomId = roomId;
	}

	public boolean isWindowsDriver() {
		return windowsDriver;
	}

	public void setWindowsDriver(boolean windowsDriver) {
		this.windowsDriver = windowsDriver;
	}

	public List<Device> getAvailableForDevices() {
		return availableForDevices;
	}

	public void setAvailableForDevices(List<Device> availableForDevices) {
		this.availableForDevices = availableForDevices;
	}

	public List<Device> getDefaultForDevices() {
		return defaultForDevices;
	}

	public void setDefaultForDevices(List<Device> defaultForDevices) {
		this.defaultForDevices = defaultForDevices;
	}

	public List<Room> getAvailableInRooms() {
		return availableInRooms;
	}

	public void setAvailableInRooms(List<Room> availableInRooms) {
		this.availableInRooms = availableInRooms;
	}

	public List<Room> getDefaultInRooms() {
		return defaultInRooms;
	}

	public void setDefaultInRooms(List<Room> defaultInRooms) {
		this.defaultInRooms = defaultInRooms;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
}
