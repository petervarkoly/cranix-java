package de.cranix.dao;

import java.io.Serializable;
import java.lang.Long;
import java.lang.String;
import javax.persistence.*;

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
public class SoftwareFullName implements Serializable {

	@Id
	@SequenceGenerator(name="SOFTWARELICENSES_ID_GENERATOR" )
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="SOFTWARELICENSES_ID_GENERATOR")
	private Long id;

	//bi-directional many-to-one association to Software
	@ManyToOne
	@JsonIgnore
	private Software software;

	private String fullName;

	private static final long serialVersionUID = 1L;

	public SoftwareFullName() {
		super();
	}

	public SoftwareFullName(Software software, String fullName) {
		super();
		this.software = software;
		this.fullName = fullName;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
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
		this.fullName = fullName;
	}
}
