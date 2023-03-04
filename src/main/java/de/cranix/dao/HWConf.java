/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.dao;

import java.io.Serializable;

import javax.persistence.*;
import javax.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * The persistent class for the HWConfs database table.
 * 
 */
@Entity
@Table(
	name="HWConfs",
	uniqueConstraints = { @UniqueConstraint(columnNames = { "hwconf_id", "name" }) }
)
@NamedQueries({
	@NamedQuery(name="HWConf.findAll",   query="SELECT h FROM HWConf h"),
	@NamedQuery(name="HWConf.findAllId", query="SELECT h.id FROM HWConf h"),
	@NamedQuery(name="HWConf.getByName", query="SELECT h FROM HWConf h WHERE h.name = :name"),
	@NamedQuery(name="HWConf.getByType", query="SELECT h FROM HWConf h WHERE h.deviceType = :deviceType")
})
@SequenceGenerator(name="seq", initialValue=1, allocationSize=100)
public class HWConf extends AbstractEntity {

	@Column(name = "name")
	@Size(max=32, message="name must not be longer then 32 characters.")
	private String name;

	@Column(name = "description")
	@Size(max=64, message="description must not be longer then 64 characters.")
	private String description;

	@Column(name = "deviceType")
	@Size(max=16, message="deviceType must not be longer then 64 characters.")
	private String deviceType;

	/* bi-directional many-to-one associations */
	@OneToMany(mappedBy="hwconf")
	@JsonIgnore
	private List<Device> devices = new ArrayList();

	@OneToMany(mappedBy="hwconf", cascade={CascadeType.REMOVE, CascadeType.PERSIST}, orphanRemoval = true )
	private List<Partition> partitions = new ArrayList();

	@OneToMany(mappedBy="hwconf")
	@JsonIgnore
	private List<Room> rooms;

	/* bi-directional many-to-many associations */
	@ManyToMany(mappedBy="hwconfs")
	@JsonIgnore
	private List<Category> categories;

	public HWConf() {
		this.categories = new ArrayList<Category>();
		this.devices    = new ArrayList<Device>();
		this.rooms      = new ArrayList<Room>();
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public List<Device> getDevices() {
		return this.devices;
	}

	public void setDevices(List<Device> devices) {
		this.devices = devices;
	}

	public Device addDevice(Device device) {
		getDevices().add(device);
		device.setHwconf(this);
		return device;
	}

	public Device removeDevice(Device device) {
		getDevices().remove(device);
		device.setHwconf(null);
		return device;
	}

	public List<Partition> getPartitions() {
		return this.partitions;
	}

	public void setPartitions(List<Partition> partitions) {
		this.partitions = partitions;
	}

	public Partition addPartition(Partition partition) {
		getPartitions().add(partition);
		partition.setHwconf(this);
		return partition;
	}

	public Partition removePartition(Partition partition) {
		getPartitions().remove(partition);
		return partition;
	}

	public List<Room> getRooms() {
		return this.rooms;
	}

	public void setRooms(List<Room> rooms) {
		this.rooms = rooms;
	}

	public Room addRoom(Room room) {
		getRooms().add(room);
		room.setHwconf(this);
		return room;
	}

	public Room removeRoom(Room room) {
		getRooms().remove(room);
		room.setHwconf(null);
		return room;
	}

	public List<Category> getCategories() {
	    return this.categories;
	}

	public void setCategories(List<Category> categories) {
	    this.categories = categories;
	}

	public boolean isDomainjoin() {
		for( Partition partition : this.partitions ) {
			if( partition.getJoinType() != null && ( partition.getJoinType().equals("Domain") || partition.getJoinType().equals("Simple")) ) {
				return true;
			}
		}
		return false;
	}
}
