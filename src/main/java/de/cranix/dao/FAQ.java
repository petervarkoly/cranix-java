/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.dao;

import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;


/**
 * The persistent class for the FAQs database table.
 *
 */
@Entity
@Table(name="FAQs")
@NamedQueries({
	@NamedQuery(name="FAQ.findAll", query="SELECT f FROM FAQ f")
})
public class FAQ extends AbstractEntity {

	@Column(name = "issue")
	@Size(max=128, message="Issue must not be longer then 128 characters.")
	private String issue;

	@Column(name = "text", columnDefinition = "MEDIUMTEXT")
	private String text;

	@Column(name = "title")
	@Size(max=128, message="Title must not be longer then 128 characters.")
	private String title;

	//bi-directional many-to-many association to Category
	@ManyToMany(mappedBy="faqs",cascade ={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
	private List<Category> categories;

	@Transient
	private List<Long> categoryIds;

	public FAQ() {
	}

	public String getIssue() {
		return this.issue;
	}

	public void setIssue(String issue) {
		this.issue = issue;
	}

	public String getText() {
		return this.text;
	}

	public void setText(String text) {
		this.text = text;
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
