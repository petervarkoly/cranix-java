/* (c) 2017-2024 Péter Varkoly <peter@varkoly.de> - all rights reserved */
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
public class SoftwareLicense extends AbstractEntity {

	/**
	 * The amount of the devices the license can be used for.
	 */
	@Column(name = "count")
	private Integer count;

	/**
	 * The type of the license. This can be F for licenses saved in files or C for Licenses passed by command line.
	 */
	@Column(name = "licenseType")
	private Character licenseType;

	/**
	 * By C licenses this is the value of the license.
	 * By F licenses this is the name of the file in which the license was saved.
	 */
	@Size(max=1024, message="License must not be longer then 1024 characters.")
	@Column(name = "value", length = 1024)
	private String value;
	
	//bi-directional many-to-many association to Device
	@ManyToMany( cascade ={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH} )
	@JoinTable(        
		name="LicenseToDevice",
	    	joinColumns={ @JoinColumn(name="license_id", columnDefinition ="BIGINT UNSIGNED NOT NULL AUTO_INCREMENT") },
	    	inverseJoinColumns={ @JoinColumn(name="device_id", columnDefinition ="BIGINT UNSIGNED NOT NULL AUTO_INCREMENT") }
	)
	@JsonIgnore
	private List<Device> devices;

	//bi-directional many-to-one association to Software
	@ManyToOne
	@JsonIgnore
	@JoinColumn(name="software_id", columnDefinition ="BIGINT UNSIGNED NOT NULL AUTO_INCREMENT")
	private Software software;
	
	/**
	 * The amount of the devices the license can be used for.
	 */
	@Transient
	private Integer used;

	public SoftwareLicense() {
	}

	public Integer getCount() {
		return this.count;
	}

	public void setCount(Integer count) {
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

	public void setDevices(List<Device> devices) {
		this.devices = devices;
	}


	public Integer getUsed() {
		return used;
	}

	public void setUsed(Integer used) {
		this.used = used;
	}
}
