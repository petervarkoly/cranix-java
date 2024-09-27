/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.dao;

import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;


/**
 * The persistent class for the Contacts database table.
 *
 */
@Entity
@Table(name="Contacts")
@NamedQueries({
	@NamedQuery(name="Contact.findAll", query="SELECT c FROM Contact c")
})
public class Contact extends AbstractEntity {

	@Column(name = "email")
	@Size(max=128, message="Email must not be longer then 128 characters.")
	private String email;

	@Column(name = "issue")
	@Size(max=128, message="Issue must not be longer then 128 characters.")
	private String issue;

	@Column(name = "name")
	@Size(max=128, message="Name must not be longer then 128 characters.")
	private String name;

	@Column(name = "phone")
	@Size(max=128, message="Phone must not be longer then 128 characters.")
	private String phone;

	@Column(name = "title")
	@Size(max=128, message="Title must not be longer then 128 characters.")
	private String title;


	//bi-directional many-to-many association to Category
	@ManyToMany(mappedBy="contacts",cascade ={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
	private List<Category> categories;

	@Transient
	private List<Long> categoryIds;

	public Contact() {
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getIssue() {
		return this.issue;
	}

	public void setIssue(String issue) {
		this.issue = issue;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhone() {
		return this.phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}


	public List<Category> getCategories() {
		return this.categories;
	}

	public void setCategories(List<Category> categories) {
		this.categories = categories;
	}

	public List<Long> getCategoryIds() {
		return categoryIds;
	}

	public void setCategoryIds(List<Long> categoryIds) {
		this.categoryIds = categoryIds;
	}

}
