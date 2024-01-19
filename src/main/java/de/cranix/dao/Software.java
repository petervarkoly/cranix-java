/* (c) 2024 Péter Varkoly <peter@varkoly.de> - all rights reserved */
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
@Table(
	name = "Softwares",
        uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }) }
)
@NamedQueries({
	@NamedQuery(name="Software.findAll",   query="SELECT s FROM Software s"),
	@NamedQuery(name="Software.findAllId", query="SELECT s.id FROM Software s"),
	@NamedQuery(name="Software.getByName", query="SELECT s FROM Software s WHERE s.name = :name"),
	@NamedQuery(name="Software.getByNameOrDescription", query="SELECT s FROM Software s WHERE s.name = :name OR s.description = :desc")
})
public class Software extends AbstractEntity {

	@Size(min = 1, message="Name must not be shorter then 1 character.")
	@Column(name = "name", length = 128)
	private String name;

	@Size(min = 1, message="Description must not be shorter then 1 character.")
	@Column(name = "description", length = 128)
	private String description;

	@Convert(converter=BooleanToStringConverter.class)
	@Column(name = "manually", columnDefinition = "CHAR(1) DEFAULT 'Y'")
	private Boolean manually;

	@Column(name = "weight")
	private Integer weight;

	/* bi-directional many-to-one associations */
	@OneToMany(mappedBy="software")
	@JsonIgnore
	private List<SoftwareLicense> softwareLicenses;

	@OneToMany(mappedBy="software", cascade = CascadeType.ALL)
	private List<SoftwareVersion> softwareVersions;

	@OneToMany(mappedBy="software", cascade = CascadeType.ALL)
	private List<SoftwareFullName> softwareFullNames;

	/* bi-directional many-to-many associations */
	@ManyToMany(mappedBy="softwares")
	@JsonIgnore
	private List<Category> categories;

	@ManyToMany(mappedBy="removedSoftwares")
	@JsonIgnore
	private List<Category> removedFromCategories;

	@ManyToMany()
	@JoinTable(
		name="SoftwareRequirements",
		joinColumns={ @JoinColumn(name="software_id", columnDefinition ="BIGINT UNSIGNED NOT NULL AUTO_INCREMENT")	},
		inverseJoinColumns={ @JoinColumn(name="requirement_id", columnDefinition ="BIGINT UNSIGNED NOT NULL AUTO_INCREMENT") }
	)
	@JsonIgnore
	private List<Software> softwareRequirements = new ArrayList<Software>();

	@ManyToMany(mappedBy="softwareRequirements")
	@JsonIgnore
	private List<Software> requiredBy = new ArrayList<Software>();

	@Transient
	private boolean sourceAvailable = true;

	public Software() {
		this.manually = false;
		this.weight   = 50;
		this.softwareRequirements = new ArrayList<Software>();
		this.requiredBy           = new ArrayList<Software>();
		this.softwareFullNames    = new ArrayList<SoftwareFullName>();
		this.softwareVersions     = new ArrayList<SoftwareVersion>();
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name.length() > 128 ? name.substring(0,128) : name;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description.length() > 128 ? description.substring(0,128) : description;
	}

	public Integer getWeight() {
		return this.weight;
	}

	public void setWeight(Integer weight) {
		this.weight = weight;
	}

	public Boolean getManually() {
		return this.manually;
	}

	public void setManually(Boolean manually) {
		this.manually = manually;
	}

	public List<SoftwareLicense> getSoftwareLicenses() {
		return this.softwareLicenses;
	}

	public void setSoftwareLicenses(List<SoftwareLicense> softwareLicenses) {
		this.softwareLicenses = softwareLicenses;
	}

	public SoftwareLicense addSoftwareLicense(SoftwareLicense softwareLicense) {
		getSoftwareLicenses().add(softwareLicense);
		softwareLicense.setSoftware(this);
		return softwareLicense;
	}

	public SoftwareLicense removeSoftwareLicense(SoftwareLicense softwareLicense) {
		getSoftwareLicenses().remove(softwareLicense);
		softwareLicense.setSoftware(null);
		return softwareLicense;
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
