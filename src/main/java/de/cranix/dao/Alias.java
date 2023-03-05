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
@Table(name="Aliases")
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
	@JoinColumn(name = "user_id")
	private User user;

	public Alias() {
	}

	public Alias(User user, String alias) {
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

}
