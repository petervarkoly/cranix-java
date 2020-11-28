/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.dao;

import java.io.Serializable;
import java.util.List;

import javax.persistence.*;
import javax.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * The persistent class for the SoftwareLicenses database table.
 * 
 */
@Entity
@Table(name="SoftwareLicenses")
@NamedQuery(name="SoftwareLicense.findAll", query="SELECT s FROM SoftwareLicense s")
public class SoftwareLicense implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="SOFTWARELICENSES_ID_GENERATOR" )
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="SOFTWARELICENSES_ID_GENERATOR")
	private Long id;

	/**
	 * The amount of the devices the license can be used for.
	 */
	private int count;

	/**
	 * The amount of the devices the license can be used for.
	 */
	@Transient
	private int used;

	/**
	 * The type of the license. This can be F for licenses saved in files or C for Licenses passed by command line.
	 */
	private Character licenseType;

	/**
	 * By C licenses this is the value of the license.
	 * By F licenses this is the name of the file in which the license was saved.
	 */
	@Size(max=1024, message="License must not be longer then 1024 characters.")
	private String value;
	
	//bi-directional many-to-many association to Device
	@ManyToMany( cascade ={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH} )
	@JoinTable(        
		name="LicenseToDevice",
	    	joinColumns={ @JoinColumn(name="license_id") },
	    	inverseJoinColumns={ @JoinColumn(name="device_id") }
	)
	@JsonIgnore
	private List<Device> devices;

	//bi-directional many-to-one association to Software
	@ManyToOne
	@JsonIgnore
	private Software software;
	
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
		SoftwareLicense other = (SoftwareLicense) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public SoftwareLicense() {
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getCount() {
		return this.count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public Character getLicenseType() {
		return this.licenseType;
	}

	public void setLicenseType(Character licenseType) {
		this.licenseType = licenseType;
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Software getSoftware() {
		return this.software;
	}

	public void setSoftware(Software software) {
		this.software = software;
	}
	
	public List<Device> getDevices() {
		return this.devices;
	}

	public boolean addDevice(Device device) {
		if( this.devices.size()+1 <= this.count) {
			this.devices.add(device);
			return true;
		} else {
			return false;
		}
	}
	
	public void removeDevice(Device device) {
		this.devices.remove(device);
	}

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public void setDevices(List<Device> devices) {
		this.devices = devices;
	}


	public int getUsed() {
		return used;
	}


	public void setUsed(int used) {
		this.used = used;
	}
}
