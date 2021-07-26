/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.dao;

import java.io.Serializable;
import java.util.List;

import javax.persistence.*;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The persistent class for the Groups database table.
 * 
 */
@Entity
@Table(name="Groups")
@NamedQueries({
	@NamedQuery(name="Group.findAll",   query="SELECT g FROM Group g"),
	@NamedQuery(name="Group.findAllId", query="SELECT g.id FROM Group g"),
	@NamedQuery(name="Group.getByName", query="SELECT g FROM Group g WHERE g.name = :name OR g.description = :name"),
	@NamedQuery(name="Group.getByExactName", query="SELECT g FROM Group g WHERE g.name = :name"),
	@NamedQuery(name="Group.getByType", query="SELECT g FROM Group g WHERE g.groupType = :groupType"),
	@NamedQuery(name="Group.search",    query="SELECT g FROM Group g WHERE g.name LIKE :search OR g.description LIKE :search OR g.groupType LIKE :search"),
})
@SequenceGenerator(name="seq", initialValue=1, allocationSize=100)
public class Group implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="seq")
	private Long id;

	@Column(name = "name", updatable = false)
	@Pattern.List({
		@Pattern(
                        regexp = "^[^/\\\\#,;=]+$",
                        flags = Pattern.Flag.CASE_INSENSITIVE,
                        message = "Group name must not contains: '/' '\\' '#' ',' ';' '='."),
		@Pattern(
                regexp = "^[^-\\.].*",
                flags = Pattern.Flag.CASE_INSENSITIVE,
                message = "Group name must not start with '-' '.'.")
	})
	@Size(max=32, message="Name must not be longer then 32 characters.")
	private String name;

	@Size(max=64, message="Description must not be longer then 64 characters.")
	private String description;

	private String groupType;

	//bi-directional many-to-many association to Category
	@ManyToMany(mappedBy="groups")
	@JsonIgnore
	private List<Category> categories;

	//bi-directional many-to-one association to Acls
	@OneToMany(mappedBy="group",cascade ={CascadeType.ALL})
	@JsonIgnore
	private List<Acl> acls;

	//bi-directional many-to-many association to User
	@ManyToMany(mappedBy="groups")
	@JsonIgnore
	private List<User> users;

	//bi-directional many-to-one association to User
	@ManyToOne
	@JsonIgnore
	private User owner;

	@Column(name="owner_id", insertable=false, updatable=false)
	private Long ownerId;

	public Group() {
		this.id   = null;
		this.name = "";
		this.description = "";
		this.groupType = "";
		this.owner = null;
	}

	public Group(String name, String description, String groupType) {
		this.id   = null;
		this.name = name;
		this.description = description;
		this.groupType = groupType;
		this.owner = null;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
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
		Group other = (Group) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getGroupType() {
		return this.groupType;
	}

	public void setGroupType(String groupType) {
		this.groupType = groupType;
	}

	public List<User> getUsers() {
		return this.users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}
	public List<Acl> getAcls() {
		return this.acls;
	}

	public void setAcls(List<Acl> acls) {
		this.acls = acls;
	}

	public void addAcl(Acl acl) {
		getAcls().add(acl);
		acl.setGroup(this);	
	}

	public void removeAcl(Acl acl) {
		getAcls().remove(acl);
		acl.setGroup(null);
	}

	public void addUser(User user) {
		this.users.add(user);
		user.getGroups().add(this);
	}

	public void removeUser(User user) {
		this.users.remove(user);
		user.getGroups().remove(this);
	}

	public List<Category> getCategories() {
		return this.categories;
	}

	public void setCategories(List<Category> categories) {
		this.categories = categories;
	}

	public User getOwner() {
		return this.owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public Long getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(Long ownerId) {
		this.ownerId = ownerId;
	}
}
