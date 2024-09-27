/* (c) 2021 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.dao;

import java.io.Serializable;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.validation.constraints.Size;


/**
 * The persistent class of the table Announcements
 * @author varkoly
 *
 */
@Entity
@Table(name="Announcements")
@NamedQueries({
	@NamedQuery(name="Announcement.findAll", query="SELECT a FROM Announcement a")
})
public class Announcement extends AbstractEntity  {

	/**
	 * The issue of the announcement. The maximal length is 128
	 */
	@Column(name = "issue")
	@Size(max=128, message="Issue must not be longer then 128 characters.")
	private String issue;

	/**
	 * Keywords to the announcement.
	 */
	@Column(name = "keywords")
	@Size(max=128, message="Keywords must not be longer then 128 characters.")
	private String keywords;

	/**
	 * The content of the announcement. Maximal length is 16MB
	 */
	@Column(name = "text", columnDefinition = "MEDIUMTEXT")
	private String text;

	@Column(name = "title")
	@Size(max=128, message="Title must not be longer then 128 characters.")
	private String title;


	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "validFrom")
	private Date validFrom;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "validUntil")
	private Date validUntil;

	//bi-directional many-to-many association to Category
	@JsonIgnore
	@OneToMany(mappedBy="parent",cascade ={CascadeType.ALL})
	private List<TaskResponse> taskResponses = new ArrayList<TaskResponse>();

	//bi-directional many-to-many association to User
	@ManyToMany(mappedBy="readAnnouncements",cascade ={CascadeType.MERGE, CascadeType.REFRESH})
	private List<User> haveSeenUsers;

	//bi-directional many-to-many association to Category
	@ManyToMany(mappedBy="announcements",cascade ={CascadeType.MERGE, CascadeType.REFRESH})
	private List<Category> categories;

	@Transient
	private List<Long> categoryIds;

	@Transient
	private Boolean seenByMe = false;

	public Announcement() {
		this.haveSeenUsers = new ArrayList<User>();
	}

	public String getIssue() {
		return this.issue;
	}

	public void setIssue(String issue) {
		this.issue = issue;
	}

	public String getKeywords() {
		return this.keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
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

	public Date getValidFrom() {
		return this.validFrom;
	}

	public void setValidFrom(Date validFrom) {
		this.validFrom = validFrom;
	}

	public Date getValidUntil() {
		return this.validUntil;
	}

	public void setValidUntil(Date validUntil) {
		this.validUntil = validUntil;
	}

	public List<User> getHaveSeenUsers() {
		return this.haveSeenUsers;
	}

	public void setHaveSeenUsers(List<User> haveSeenUsers) {
		this.haveSeenUsers = haveSeenUsers;
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

	public List<TaskResponse> getTaskResponses() {
		return taskResponses;
	}

	public void setTaskResponses(List<TaskResponse> taskResponses) {
		this.taskResponses = taskResponses;
	}
	public void addTasksResponses(TaskResponse taskResponse) {
		taskResponse.setParent(this);
		if( !this.taskResponses.contains(taskResponse) ) {
			this.taskResponses.add(taskResponse);
		}
	}
	public void deleteTasksResponses(TaskResponse taskResponse) {
		taskResponse.setParent(null);
		if( this.taskResponses.contains(taskResponse) ) {
			this.taskResponses.remove(taskResponse);
		}
	}

	public Boolean getSeenByMe() {
		return seenByMe;
	}

	public void setSeenByMe(Boolean seenByMe) {
		this.seenByMe = seenByMe;
	}
}
