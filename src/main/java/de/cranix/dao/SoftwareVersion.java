/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.dao;

import java.io.Serializable;
import javax.persistence.*;

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
	@NamedQuery(name="SoftwareVersion.findAll",       query="SELECT s FROM SoftwareVersion s"),
	@NamedQuery(name="SoftwareVersion.get",			  query="SELECT s FROM SoftwareVersion s WHERE s.softwareId = :SOFTWARE and s.version = :VERSION"),
	@NamedQuery(name="SoftwareVersion.getBySoftware", query="SELECT s FROM SoftwareVersion s WHERE s.softwareId = :SOFTWARE"),
	
})
public class SoftwareVersion implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="SOFTWAREVERSION_ID_GENERATOR" )
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="SOFTWAREVERSION_ID_GENERATOR")
	private Long id;

	private String version;

	//bi-directional many-to-one association, cascade=CascadeType.REMOVEn to SoftwareStatus
	@OneToMany(mappedBy="softwareVersion", cascade=CascadeType.ALL)
	@JsonIgnore
	private List<SoftwareStatus> softwareStatuses;

	//bi-directional many-to-one association to Software
	@ManyToOne
	@JsonIgnore
	private Software software;
	
	@Column(name = "software_id", insertable = false, updatable = false)
	private Long softwareId;

	/*
	 * C -> current this is the most recent version and does exists on the server and can be installed
	 * R -> replaced this version does not exists on the server but is installed on some clients
	 * D -> deprecated this is an older version which does exists on the server and can be installed
	 * U -> unknown this version of software was not installed from the cranix
	 */
	private String status;
	
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
		SoftwareVersion other = (SoftwareVersion) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public SoftwareVersion() {
	}

	public SoftwareVersion(Software software, String version, String status) {
		this.software = software;
		this.version  = version;
		this.status   = status;
		software.getSoftwareVersions().add(this);
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getVersion() {
		return this.version;
	}

	public void setVersion(String version) {
		this.version = version;
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
