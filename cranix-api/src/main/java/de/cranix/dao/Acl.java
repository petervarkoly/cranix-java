/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.dao;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
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
public class Acl implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="seq")
	private Long id;

	private String acl;
	
	@Convert(converter=BooleanToStringConverter.class)
	private Boolean allowed;
	
	//bi-directional many-to-one association to User
	@ManyToOne
	@JsonIgnore
	private User user;
	
	@Column(name = "user_id", insertable = false, updatable = false)
	private Long userId;

	//bi-directional many-to-one association to Group
	@ManyToOne
	@JsonIgnore
	private Group group;
	
	@Column(name = "group_id", insertable = false, updatable = false)
	private Long groupId;

	//bi-directional many-to-one association to User
	@ManyToOne
	@JsonIgnore
	private User creator;
		
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
		Acl other = (Acl) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (Exception e) {
			return "{ \"ERROR\" : \"CAN NOT MAP THE OBJECT\" }";
		}
	}
	

	public Acl() {
	}

	public Acl(String name, boolean allowed) {
		this.acl     = name;
		this.allowed = allowed;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getGroupId() {
		return groupId;
	}

	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}
}
