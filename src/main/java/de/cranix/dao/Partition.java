/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.dao;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.*;
import javax.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * The persistent class for the Partitions database table.
 *
 */
@Entity
@Table(name="Partitions")
@NamedQueries({
	@NamedQuery(name="Partition.findAll",   query="SELECT p FROM Partition p"),
	@NamedQuery(name="Partition.findAllId", query="SELECT p.id FROM Partition p"),
	@NamedQuery(name="Partition.getPartitionByName", query="SELECT p FROM Partition p WHERE p.hwconf.id = :hwconfId AND p.name = :name")
})
@SequenceGenerator(name="seq", initialValue=1, allocationSize=100)
public class Partition implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="seq")
	private Long id;

	@Size(max=64, message="Description must not be longer then 64 characters.")
	private String description;

	@Size(max=16, message="Format must not be longer then 16 characters.")
	private String format;

	private String joinType;

	@Size(max=32, message="Name must not be longer then 32 characters.")
	private String name;

	@Column(name="OS")
	@Size(max=16, message="OS must not be longer then 16 characters.")
	private String os;

	@Size(max=16, message="Tool must not be longer then 16 characters.")
	private String tool;

	//bi-directional many-to-one association to HWConf
	@ManyToOne
	@JsonIgnore
	private HWConf hwconf;

    //bi-directional many-to-one association to User
	@ManyToOne
	@JsonIgnore
	private User creator;

	@Transient
	private Timestamp lastCloned;

	public Partition() {
	}

	public Partition(String name) {
		this.name = name;
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
		Partition other = (Partition) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}


	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getFormat() {
		return this.format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getJoinType() {
		return this.joinType;
	}

	public void setJoinType(String joinType) {
		this.joinType = joinType;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOs() {
		return this.os;
	}

	public void setOs(String os) {
		this.os = os;
	}

	public String getTool() {
		return this.tool;
	}

	public void setTool(String tool) {
		this.tool = tool;
	}

	public HWConf getHwconf() {
		return this.hwconf;
	}

	public void setHwconf(HWConf hwconf) {
		this.hwconf = hwconf;
	}

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public Timestamp getLastCloned() {
		return lastCloned;
	}

	public void setLastCloned(Timestamp lastCloned) {
		this.lastCloned = lastCloned;
	}

}
