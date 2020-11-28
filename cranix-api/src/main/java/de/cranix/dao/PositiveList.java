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
public class PositiveList implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="POSITIVELIST_ID_GENERATOR" )
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="POSITIVELIST_ID_GENERATOR")
	private Long id;

	@Size(max=64, message="Description must not be longer then 64 characters.")
	private String description;

	@Size(max=32, message="Name must not be longer then 32 characters.")
	private String name;
		
	@Size(max=32, message="Subject must not be longer then 32 characters.")
	private String subject;

	//bi-directional many-to-one association to User
	@ManyToOne
	@JsonIgnore
	private User owner;
	
	@Transient
	private String domains;
	
	public PositiveList() {
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

	public String getSubject() {
		return this.subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public String getDomains() {
		return domains;
	}

	public void setDomains(String domains) {
		this.domains = domains;
	}

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
		PositiveList other = (PositiveList) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
