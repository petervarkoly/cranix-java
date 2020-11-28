/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
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
public class Announcement implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * The technical id of the announcement
	 */
	@Id
	@SequenceGenerator(name="ANNOUNCEMENTS_ID_GENERATOR" )
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="ANNOUNCEMENTS_ID_GENERATOR")
	private Long id;

	/**
	 * The issue of the announcement. The maximal length is 128
	 */
	@Size(max=128, message="Issue must not be longer then 64 characters.")
	private String issue;

	/*+
	 * Keywords to the announcement.
	 */
	@Size(max=128, message="Keywords must not be longer then 64 characters.")
	private String keywords;

	/**
	 * The content of the announcement. Maximal length is 16MB
	 */
	private String text;

	@Size(max=128, message="Title must not be longer then 64 characters.")
	private String title;


	@Temporal(TemporalType.TIMESTAMP)
	private Date validFrom;

	@Temporal(TemporalType.TIMESTAMP)
	private Date validUntil;

	//bi-directional many-to-many association to User
	@ManyToMany(mappedBy="readAnnouncements",cascade ={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
	private List<User> haveSeenUsers;

	//bi-directional many-to-many association to Category
	@ManyToMany(mappedBy="announcements",cascade ={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
	private List<Category> categories;

	@Transient
	private List<Long> categoryIds;

	//bi-directional many-to-one association to User
	@ManyToOne
	@JsonIgnore
	private User owner;

	@Column(name="owner_id", insertable=false, updatable=false)
	private Long ownerId;

	public Announcement() {
		this.haveSeenUsers = new ArrayList<User>();
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public User getOwner() {
		return this.owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public List<Long> getCategoryIds() {
		return categoryIds;
	}

	public void setCategoryIds(List<Long> categoryIds) {
		this.categoryIds = categoryIds;
	}

	public Long getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(Long ownerId) {
		this.ownerId = ownerId;
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
		Announcement other = (Announcement) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
