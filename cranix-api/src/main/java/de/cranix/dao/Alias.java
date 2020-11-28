/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.dao;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import com.fasterxml.jackson.databind.ObjectMapper;
/**
 * The persistent class for the Aliases database table.
 * 
 */
@Entity
@Table(name="Aliases")
@NamedQueries( {
	@NamedQuery(name="Alias.findAll",	query="SELECT a FROM Alias a"),
	@NamedQuery(name="Alias.getByName",	query="SELECT a FROM Alias a where a.alias = :alias"),
})
@SequenceGenerator(name="seq", initialValue=1, allocationSize=100)
public class Alias implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="seq")
	private Long id;

	private String alias;

	@JsonIgnore
	private User user;

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
		if (obj.getClass().getName() == "java.lang.String" &&
				this.alias.equals(obj)) {
			return true;
		}
		if (getClass() != obj.getClass())
			return false;
		Alias other = (Alias) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public Alias() {
	}

	public Alias(User user, String alias) {
		this.user  = user;
		this.alias = alias;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAlias() {
		return this.alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}
