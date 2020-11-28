/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.dao;

import java.io.Serializable;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * The persistent class for the SoftwareStatus database table.
 *
 */
@Entity
@Table(name="SoftwareStatus")
@NamedQueries({
	@NamedQuery(name="SoftwareStatus.findAll",		query="SELECT s FROM SoftwareStatus s"),
	@NamedQuery(name="SoftwareStatus.findByStatus", query="SELECT s FROM SoftwareStatus s WHERE s.status = :STATUS"),
	@NamedQuery(name="SoftwareStatus.getAllForOne", query="SELECT ss, sv FROM SoftwareStatus ss JOIN SoftwareVersion sv ON ss.softwarversion_id=sv.id WHERE ss.deviceId= :DEVICE AND sv.softwareId= :SOFTWARE"),
	@NamedQuery(name="SoftwareStatus.getForOne",	query="SELECT ss, sv FROM SoftwareStatus ss JOIN SoftwareVersion sv ON ss.softwareversion_id=sv.id WHERE ss.deviceId= :DEVICE AND sv.softwareId= :SOFTWARE AND sv.version = :VERSION"),
})

public class SoftwareStatus implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="SOFTWARESTATUS_ID_GENERATOR" )
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="SOFTWARESTATUS_ID_GENERATOR")
	private Long id;

	/**
	 * The state of the installation can have following values:<br>
	 * I  -> installed<br>
	 * IS -> installation scheduled<br>
 	 * US -> update scheduled<br>
	 * MD -> manuell deinstalled<br>
	 * DS -> deinstallation scheduled<br>
	 * DF -> deinstallation failed<br>
	 * IF -> installation failed<br>
	 * FR -> installed version is frozen: This must not be updated.<br>
	 */
	private String status;

	/**
	 * Bidirectional many to one association to a software version object.
	 */
	@ManyToOne
	@JsonIgnore
	private SoftwareVersion softwareVersion;

	/**
	 * Bidirectional many to one read only association to a software version object.ZZZZZ
	 */
	@Column(name = "softwareversion_id", insertable = false, updatable = false)
	private Long softwareversionId;

	@ManyToOne
	@JsonIgnore
	private Device device;

	@Column(name = "device_id", insertable = false, updatable = false)
	private Long deviceId;

	@Transient
	private String roomName;

	@Transient
	private String deviceName;

	@Transient
	private String softwareName;

	@Transient
	private boolean manually;

	@Transient
	private Long softwareId;

	@Transient
	private String version;

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
		SoftwareStatus other = (SoftwareStatus) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public SoftwareStatus() {
	}

	public SoftwareStatus(Device d, SoftwareVersion sv, String status) {
		this.device = d;
		this.softwareVersion = sv;
		this.status = status;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getStatus() {
		return this.status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public SoftwareVersion getSoftwareVersion() {
		return this.softwareVersion;
	}

	public void setSoftwareVersion(SoftwareVersion softwareVersion) {
		this.softwareVersion = softwareVersion;
	}

	public Device getDevice() {
		return this.device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	public Long getSoftwareversionId() {
		return softwareversionId;
	}

	public void setSoftwareversionId(Long versionId) {
		this.softwareversionId = versionId;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getSoftwareName() {
		return softwareName;
	}

	public void setSoftwareName(String softwareName) {
		this.softwareName = softwareName;
	}

	public boolean isManually() {
		return manually;
	}

	public void setManually(boolean manually) {
		this.manually = manually;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}


	public Long getDeviceId() {
		return deviceId;
	}


	public void setDeviceId(Long deviceId) {
		this.deviceId = deviceId;
	}


	public Long getSoftwareId() {
		return softwareId;
	}


	public void setSoftwareId(Long softwareId) {
		this.softwareId = softwareId;
	}


	/**
	 * @return the roomName
	 */
	public String getRoomName() {
		return roomName;
	}


	/**
	 * @param roomName the roomName to set
	 */
	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}


	/**
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
}
