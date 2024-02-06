/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.dao;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import javax.validation.constraints.Size;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * The persistent class for the Acls database table.
 * 
 */
@Entity
@Table(name="Acls")
@NamedQueries({
	@NamedQuery(name="Acl.findAll", query="SELECT a FROM Acl a")
})
@SequenceGenerator(name="seq", initialValue=1, allocationSize=100)
public class Acl extends AbstractEntity {

	@Column(name = "acl")
	@Size(max=32, message="acl must not be longer then 32 characters.")
	private String acl;
	
	@Convert(converter=BooleanToStringConverter.class)
	@Column(name = "allowed", columnDefinition = "CHAR(1) DEFAULT 'Y'")
	private Boolean allowed;

	//bi-directional many-to-one association to User
	@ManyToOne
	@JsonIgnore
	@JoinColumn(name="user_id", columnDefinition ="BIGINT UNSIGNED", updatable = false)
	private User user;

	//bi-directional many-to-one association to Group
	@ManyToOne
	@JsonIgnore
	@JoinColumn(name="group_id", columnDefinition ="BIGINT UNSIGNED", updatable = false)
	private Group group;
	
	public Acl() {
	}

	public Acl(String name, boolean allowed) {
		this.acl     = name;
		this.allowed = allowed;
	}

	public String getAcl() {
		return this.acl;
	}

	public void setAcl(String acl) {
		this.acl = acl;
	}

	public boolean getAllowed() {
		return this.allowed;
	}

	public void setAllowed(boolean allowed) {
		this.allowed = allowed;
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	public User getUser(){
		return this.user;
	}
	
	public void setGroup(Group group){
		this.group = group;
	}
	
	public Group getGroup(){
		return this.group;
	}

	public Long getUserId() {
		return this.user != null ? this.user.getId() : null;
	}

	public Long getGroupId() {
		return this.group != null ? this.group.getId() : null;
	}

}
