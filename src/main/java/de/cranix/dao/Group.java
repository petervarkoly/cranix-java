/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.dao;

import java.io.Serializable;
import java.util.ArrayList;
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
@Table(
	name="Groups",
	uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }) }
)
@NamedQueries({
	@NamedQuery(name="Group.findAll",   query="SELECT g FROM Group g"),
	@NamedQuery(name="Group.findAllId", query="SELECT g.id FROM Group g"),
	@NamedQuery(name="Group.getByName", query="SELECT g FROM Group g WHERE g.name = :name OR g.description = :name"),
	@NamedQuery(name="Group.getByExactName", query="SELECT g FROM Group g WHERE g.name = :name"),
	@NamedQuery(name="Group.getByType", query="SELECT g FROM Group g WHERE g.groupType = :groupType"),
	@NamedQuery(name="Group.search",    query="SELECT g FROM Group g WHERE g.name LIKE :search OR g.description LIKE :search OR g.groupType LIKE :search"),
})
@SequenceGenerator(name="seq", initialValue=1, allocationSize=100)
public class Group extends AbstractEntity {

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
	private String name = "";

	@Column(name = "description")
	@Size(max=64, message="Description must not be longer then 64 characters.")
	private String description = "";

	@Column(name = "groupType")
	@Size(max=16, message="Description must not be longer then 16 characters.")
	private String groupType = "";

	@Column(name="color")
	@Size(max=7, message="color must not be longer then 7 characters.")
	protected String color = "#AABBCC";

	/* bi-directional one-to-many associations */
	@OneToMany(mappedBy="group")
	@JsonIgnore
	private List<Acl> acls = new ArrayList<>();

	/* bi-directional many-to-many associations */
	@ManyToMany(mappedBy="groups")
	@JsonIgnore
	private List<Category> categories = new ArrayList<>();

	@ManyToMany(mappedBy="groups")
	@JsonIgnore
	private List<CrxChallenge> todos = new ArrayList<CrxChallenge>();

	@ManyToMany(mappedBy="groups")
	@JsonIgnore
	private List<User> users = new ArrayList<>();

	@ManyToMany(mappedBy="groups")
	@JsonIgnore
	private List<CrxCalendar> events = new ArrayList<>();

	public Group() {
		this.setId(null);
		this.name = "";
		this.description = "";
		this.groupType = "";
	}

	public Group(String name, String description, String groupType) {
		this.setId(null);
		this.name = name;
		this.description = description;
		this.groupType = groupType;
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

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public List<CrxCalendar> getEvents() {
		return events;
	}

	public void setEvents(List<CrxCalendar> events) {
		this.events = events;
	}

	public void addEvent(CrxCalendar event) {
		if(! this.events.contains(event)) {
			this.events.add(event);
			event.getGroups().add(this);
		}
	}

	public void removeEvent(CrxCalendar event) {
		if( this.events.contains((event))) {
			this.events.remove(event);
			event.getGroups().remove(this);
		}
	}

	public List<Category> getCategories() {
		return this.categories;
	}

	public void setCategories(List<Category> categories) {
		this.categories = categories;
	}

	public List<CrxChallenge> getTodos() {
		return todos;
	}

	public void setTodos(List<CrxChallenge> todos) {
		this.todos = todos;
	}
}
