/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.dao;

import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.Size;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * The persistent class for the Enumerates database table.
 * 
 */
@Entity
@Table(name="Enumerates")
@NamedQueries({
	@NamedQuery(name="Enumerate.findAll", query="SELECT e FROM Enumerate e"),
	@NamedQuery(name="Enumerate.getByType", query="SELECT e FROM Enumerate e WHERE e.name = :type"),
	@NamedQuery(name="Enumerate.get", query="SELECT e FROM Enumerate e WHERE e.name = :type AND e.value = :value" )
})
@SequenceGenerator(name="seq", initialValue=1, allocationSize=100)
public class Enumerate implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="seq")
	private Long id;

	@Size(max=32, message="Name must not be longer then 32 characters.")
	private String name;

	@Size(max=32, message="Value must not be longer then 32 characters.")
	private String value;

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
		Enumerate other = (Enumerate) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public Enumerate() {
	}
	
	public Enumerate(String type, String value) {
		this.name	= type;
		this.value	= value;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
