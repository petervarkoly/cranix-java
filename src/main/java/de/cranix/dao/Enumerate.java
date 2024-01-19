/* (c) 2024 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.dao;

import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * The persistent class for the Enumerates database table.
 *
 */
@Entity
@Table(name="Enumerates")
@NamedQueries({
	@NamedQuery(name="Enumerate.findAll", query="SELECT e FROM Enumerate e"),
	@NamedQuery(name="Enumerate.getByName", query="SELECT e FROM Enumerate e WHERE e.name = :name"),
	@NamedQuery(name="Enumerate.get", query="SELECT e FROM Enumerate e WHERE e.name = :name AND e.value = :value" )
})
@SequenceGenerator(name="seq", initialValue=1, allocationSize=100)
public class Enumerate extends AbstractEntity {

	@Column(name = "name")
	@Size(max=32, message="Name must not be longer then 32 characters.")
	private String name;

	@Column(name = "value")
	@Size(max=32, message="Value must not be longer then 32 characters.")
	private String value;

	public Enumerate() {
	}

	public Enumerate(String type, String value, User user) {
		this.name	= type;
		this.value	= value;
		this.setCreator(user);
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
