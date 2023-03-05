package de.cranix.dao;

import java.io.Serializable;
import java.lang.Long;
import java.lang.String;
import javax.persistence.*;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Entity implementation class for Entity: SoftwareFullName
 *
 */
@Entity
@Table(name="SoftwareFullNames")
@NamedQueries({
	@NamedQuery(name="SoftwareFullName.findAll",    query="SELECT s FROM SoftwareFullName s"),
	@NamedQuery(name="SoftwareFullName.getByName",  query="SELECT s FROM SoftwareFullName s WHERE s.fullName = :fullName"),
	@NamedQuery(name="SoftwareFullName.findByName", query="SELECT s FROM SoftwareFullName s WHERE s.fullName LIKE :fullName")
})
public class SoftwareFullName extends AbstractEntity {

	@Size(min = 1, max = 128)
	@Column(name = "fullName", length = 128)
	private String fullName;

	//bi-directional many-to-one association to Software
	@ManyToOne
	@JsonIgnore
	@JoinColumn(name="software_id")
	private Software software;

	public SoftwareFullName() {
		super();
	}

	public SoftwareFullName(Software software, String fullName) {
		super();
		this.software = software;
		this.fullName = fullName.length() > 128 ? fullName.substring(0,128) : fullName;
	}

	public Software getSoftware() {
		return this.software;
	}

	public void setSoftware(Software software) {
		this.software = software;
	}
	public String getFullName() {
		return this.fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName.length() > 128 ? fullName.substring(0,128) : fullName;
	}
}
