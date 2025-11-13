/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.dao;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import javax.validation.constraints.Size;
import com.fasterxml.jackson.databind.ObjectMapper;
/**
 * The persistent class for the Aliases database table.
 * 
 */
@Entity
@Table(
		name="Aliases",
		uniqueConstraints = { @UniqueConstraint(columnNames = { "user_id", "alias" }) }
)
@NamedQueries( {
	@NamedQuery(name="Alias.findAll",	query="SELECT a FROM Alias a"),
	@NamedQuery(name="Alias.getByName",	query="SELECT a FROM Alias a where a.alias = :alias"),
})
@SequenceGenerator(name="seq", initialValue=1, allocationSize=100)
public class Alias extends AbstractEntity {

	@Size(max=64, message="alias must not be longer then 64 characters.")
	@Column(name="alias")
	private String alias;

	@JsonIgnore
	@JoinColumn(name = "user_id", columnDefinition ="BIGINT UNSIGNED NOT NULL")
	private User user;

	public Alias() {
	}

	public Alias(Session session, User user, String alias) {
		this.creator = session.getUser();
		this.user  = user;
		this.alias = alias;
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

	@Override
	public int hashCode() {
		return alias.toLowerCase().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Alias other = (Alias) obj;
		return  this.alias.toLowerCase().equals(other.getAlias().toLowerCase());
	}

}
