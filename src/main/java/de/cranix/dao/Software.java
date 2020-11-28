/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.dao;

import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * The persistent class for the Software database table.
 *
 */
@Entity
@Table(name = "Softwares")
@NamedQueries({
	@NamedQuery(name="Software.findAll",   query="SELECT s FROM Software s"),
	@NamedQuery(name="Software.findAllId", query="SELECT s.id FROM Software s"),
	@NamedQuery(name="Software.getByName", query="SELECT s FROM Software s WHERE s.name = :name"),
	@NamedQuery(name="Software.getByNameOrDescription", query="SELECT s FROM Software s WHERE s.name = :name OR s.description = :desc")
})
public class Software implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="SOFTWARE_ID_GENERATOR" )
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="SOFTWARE_ID_GENERATOR")
	private Long id;

	@Size(max=128, message="Description must not be longer then 128 characters.")
	private String description;

	@Convert(converter=BooleanToStringConverter.class)
	private Boolean manually;

	@Size(max=128, message="Name must not be longer then 128 characters.")
	private String name;

	private Integer weight;

	@Transient
	private boolean sourceAvailable = true;

	//bi-directional many-to-one association to SoftwareLicens
	@OneToMany(mappedBy="software")
	@JsonIgnore
	private List<SoftwareLicense> softwareLicenses;

	//bi-directional many-to-one association to SoftwareVersion
	@OneToMany(mappedBy="software", cascade = CascadeType.PERSIST)
	private List<SoftwareVersion> softwareVersions;

	//bi-directional many-to-one association to SoftwareVersion
	@OneToMany(mappedBy="software", cascade = CascadeType.PERSIST)
	private List<SoftwareFullName> softwareFullNames;

	//bi-directional many-to-many association to Category
	@ManyToMany(mappedBy="softwares")
	@JsonIgnore
	private List<Category> categories;

	//bi-directional many-to-many association to Category
	@ManyToMany(mappedBy="removedSoftwares")
	@JsonIgnore
	private List<Category> removedFromCategories;

	//bi-directional many-to-one association to User
	@ManyToOne
	@JsonIgnore
	private User creator;

	//bi-directional many-to-many association to Device
	@ManyToMany()
	@JoinTable(
			name="SoftwareRequirements",
			joinColumns={ @JoinColumn(name="software_id")	},
			inverseJoinColumns={ @JoinColumn(name="requirement_id") }
	)
	@JsonIgnore
	private List<Software> softwareRequirements = new ArrayList<Software>();

	//bi-directional many-to-many association to Device
	@ManyToMany(mappedBy="softwareRequirements")
	@JsonIgnore
	private List<Software> requiredBy = new ArrayList<Software>();


	public Software() {
		this.manually = false;
		this.weight   = 50;
		this.softwareRequirements = new ArrayList<Software>();
		this.requiredBy           = new ArrayList<Software>();
		this.softwareFullNames    = new ArrayList<SoftwareFullName>();
		this.softwareVersions     = new ArrayList<SoftwareVersion>();
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
		Software other = (Software) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getWeight() {
		return this.weight;
	}

	public void setWeight(Integer weight) {
		this.weight = weight;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Boolean getManually() {
		return this.manually;
	}

	public void setManually(Boolean manually) {
		this.manually = manually;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<SoftwareLicense> getSoftwareLicenses() {
		return this.softwareLicenses;
	}

	public void setSoftwareLicenses(List<SoftwareLicense> softwareLicenses) {
		this.softwareLicenses = softwareLicenses;
	}

	public SoftwareLicense addSoftwareLicens(SoftwareLicense softwareLicens) {
		getSoftwareLicenses().add(softwareLicens);
		softwareLicens.setSoftware(this);

		return softwareLicens;
	}

	public SoftwareLicense removeSoftwareLicens(SoftwareLicense softwareLicens) {
		getSoftwareLicenses().remove(softwareLicens);
		softwareLicens.setSoftware(null);

		return softwareLicens;
	}

	public List<SoftwareVersion> getSoftwareVersions() {
		return this.softwareVersions;
	}

	public void setSoftwareVersions(List<SoftwareVersion> softwareVersions) {
		this.softwareVersions = softwareVersions;
	}

	public SoftwareVersion addSoftwareVersion(SoftwareVersion softwareVersion) {
		getSoftwareVersions().add(softwareVersion);
		softwareVersion.setSoftware(this);

		return softwareVersion;
	}

	public SoftwareVersion removeSoftwareVersion(SoftwareVersion softwareVersion) {
		getSoftwareVersions().remove(softwareVersion);
		softwareVersion.setSoftware(null);

		return softwareVersion;
	}

	public List<Category> getCategories() {
	    return this.categories;
	}

	public void setCategories(List<Category> categories) {
	    this.categories = categories;
	}

	public List<Category> getRemovedFromCategories() {
	    return this.removedFromCategories;
	}

	public void setRemovedFromCategories(List<Category> categories) {
	    this.removedFromCategories = categories;
	}

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public boolean isSourceAvailable() {
		return sourceAvailable;
	}

	public void setSourceAvailable(boolean downloaded) {
		this.sourceAvailable = downloaded;
	}

	public List<Software> getSoftwareRequirements() {
		return softwareRequirements;
	}

	public void setSoftwareRequirements(List<Software> softwareRequirements) {
		this.softwareRequirements = softwareRequirements;
	}

	public List<Software> getRequiredBy() {
		return requiredBy;
	}

	public void setRequiredBy(List<Software> requiredBy) {
		this.requiredBy = requiredBy;
	}

	public List<SoftwareFullName> getSoftwareFullNames() {
		return softwareFullNames;
	}

	public void setSoftwareFullNames(List<SoftwareFullName> softwareFullNames) {
		this.softwareFullNames = softwareFullNames;
	}
}
