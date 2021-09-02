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
@Table(name="HWConfs")
@NamedQueries({
	@NamedQuery(name="HWConf.findAll",   query="SELECT h FROM HWConf h"),
	@NamedQuery(name="HWConf.findAllId", query="SELECT h.id FROM HWConf h"),
	@NamedQuery(name="HWConf.getByName", query="SELECT h FROM HWConf h WHERE h.name = :name"),
	@NamedQuery(name="HWConf.getByType", query="SELECT h FROM HWConf h WHERE h.deviceType = :deviceType")
})
@SequenceGenerator(name="seq", initialValue=1, allocationSize=100)
public class HWConf implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="seq")
	private Long id;

	@Size(max=64, message="Description must not be longer then 64 characters.")
	private String description;

	@Size(max=32, message="Name must not be longer then 32 characters.")
	private String name;

	private String deviceType;

	//bi-directional many-to-one association to Device
	@OneToMany(mappedBy="hwconf")
	@JsonIgnore
	private List<Device> devices = new ArrayList();

	//bi-directional many-to-one association to Partition
	@OneToMany(mappedBy="hwconf", cascade={CascadeType.REMOVE, CascadeType.PERSIST}, orphanRemoval = true )
	private List<Partition> partitions = new ArrayList();

	//bi-directional many-to-one association to Room
	@OneToMany(mappedBy="hwconf")
	@JsonIgnore
	private List<Room> rooms;

	//bi-directional many-to-many association to Category
	@ManyToMany(mappedBy="hwconfs")
	@JsonIgnore
	private List<Category> categories;

    //bi-directional many-to-one association to User
	@ManyToOne
	@JsonIgnore
	private User creator;

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
		HWConf other = (HWConf) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public HWConf() {
		this.categories = new ArrayList<Category>();
		this.devices    = new ArrayList<Device>();
		this.rooms      = new ArrayList<Room>();
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
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
