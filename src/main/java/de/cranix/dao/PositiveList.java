package de.cranix.dao;

import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * The persistent class for the PositiveList database table.
 * 
 */
@Entity
@Table(name="PositiveLists")
@NamedQueries({
	@NamedQuery(name="PositiveList.findAll", query="SELECT p FROM PositiveList p"),
	@NamedQuery(name="PositiveList.byName",  query="SELECT p FROM PositiveList p WHERE p.name = :name")
})
public class PositiveList extends AbstractEntity {

	@Column(name="description", length=64)
	@Size(max=64, message="Description must not be longer then 64 characters.")
	private String description;

	@Column(name="name", length=32)
	@Size(max=32, message="Name must not be longer then 32 characters.")
	private String name;
		
	@Column(name="subject", length=32)
	@Size(max=32, message="Subject must not be longer then 32 characters.")
	private String subject;

	@Transient
	private String domains;
	
	public PositiveList() {
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

	public String getSubject() {
		return this.subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setDomains(String domains) {
		this.domains = domains;
	}

	public String getDomains() {
		return this.domains;
	}

}
