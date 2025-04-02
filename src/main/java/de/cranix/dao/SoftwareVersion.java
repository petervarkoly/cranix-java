/* (c) 2017-2023 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.dao;

import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;


/**
 * The persistent class for the SoftwareVersion database table.
 * 
 */
@Entity
@Table(name = "SoftwareVersions")
@NamedQueries({
	@NamedQuery(name="SoftwareVersion.findAll", query="SELECT s FROM SoftwareVersion s")
})
public class SoftwareVersion extends AbstractEntity {

	@Column(name = "version", length = 128)
	private String version;

	//bi-directional many-to-one association, cascade=CascadeType.REMOVEn to SoftwareStatus
	@OneToMany(mappedBy="softwareVersion", cascade=CascadeType.ALL)
	@JsonIgnore
	private List<SoftwareStatus> softwareStatuses;

	//bi-directional many-to-one association to Software
	@ManyToOne
	@JsonIgnore
	@JoinColumn(name="software_id", columnDefinition ="BIGINT UNSIGNED NOT NULL")
	private Software software;
	
	/*
	 * C -> current this is the most recent version and does exist on the server and can be installed
	 * R -> replaced this version does not exist on the server but is installed on some clients
	 * D -> deprecated this is an older version which does exists on the server and can be installed
	 * U -> unknown this version of software was not installed from the cranix
	 */
	@Column(name = "status", length = 1)
        @Size(max=1, message="status must not be longer then 1 characters.")
	private String status;
	
	public SoftwareVersion() {
	}

	public SoftwareVersion(Software software, String version, String status) {
		this.software = software;
		this.version  = version;
		this.status   = status;
		software.getSoftwareVersions().add(this);
	}

	public String getVersion() {
		return this.version;
	}

	public void setVersion(String version) {
		if(version != null) {
			this.version = version.length() > 128 ? version.substring(0, 128) : version;
		}
	}

	public List<SoftwareStatus> getSoftwareStatuses() {
		return this.softwareStatuses;
	}

	public void setSoftwareStatuses(List<SoftwareStatus> softwareStatuses) {
		this.softwareStatuses = softwareStatuses;
	}

	public SoftwareStatus addSoftwareStatus(SoftwareStatus softwareStatus) {
		getSoftwareStatuses().add(softwareStatus);
		softwareStatus.setSoftwareVersion(this);

		return softwareStatus;
	}

	public SoftwareStatus removeSoftwareStatus(SoftwareStatus softwareStatus) {
		getSoftwareStatuses().remove(softwareStatus);
		softwareStatus.setSoftwareVersion(null);

		return softwareStatus;
	}

	public Software getSoftware() {
		return this.software;
	}

	public void setSoftware(Software software) {
		this.software = software;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
