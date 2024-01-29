/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.dao;

import java.io.Serializable;
import javax.persistence.*;

import javax.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * The persistent class for the SoftwareStatus database table.
 *
 */
@Entity
@Table(
		name="SoftwareStatus",
		uniqueConstraints = { @UniqueConstraint(columnNames = {"softwareversion_id", "device_id"})}
)
@NamedQueries({
	@NamedQuery(name="SoftwareStatus.findAll",		query="SELECT s FROM SoftwareStatus s"),
	@NamedQuery(name="SoftwareStatus.findByStatus", query="SELECT s FROM SoftwareStatus s WHERE s.status = :STATUS"),
})
public class SoftwareStatus extends AbstractEntity {

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
	@Column(name = "status")
	@Size(max = 2, message = "status must not be longer the 2 characters")
	private String status;

	/**
	 * Bidirectional many to one association to a software version object.
	 */
	@ManyToOne
	@JsonIgnore
	@JoinColumn(name="softwareversion_id", columnDefinition ="BIGINT UNSIGNED NOT NULL")
	private SoftwareVersion softwareVersion;

	@ManyToOne
	@JsonIgnore
	@JoinColumn(name="device_id", columnDefinition ="BIGINT UNSIGNED NOT NULL")
	private Device device;

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

	public SoftwareStatus() {
	}

	public SoftwareStatus(Device d, SoftwareVersion sv, String status) {
		this.device = d;
		this.softwareVersion = sv;
		this.status = status;
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
		return this.device.getId();
	}

	public Long getSoftwareId() {
		return softwareId;
	}

	public void setSoftwareId(Long id) {
		this.softwareId = id;
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
}
