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
@Table(
	name="Partitions",
	uniqueConstraints = { @UniqueConstraint(columnNames = { "hwconf_id", "name" }) }
)
@NamedQueries({
	@NamedQuery(name="Partition.findAll",   query="SELECT p FROM Partition p"),
	@NamedQuery(name="Partition.findAllId", query="SELECT p.id FROM Partition p"),
	@NamedQuery(name="Partition.getPartitionByName", query="SELECT p FROM Partition p WHERE p.hwconf.id = :hwconfId AND p.name = :name")
})
@SequenceGenerator(name="seq", initialValue=1, allocationSize=100)
public class Partition extends AbstractEntity {

	@Column(name="name", length=32)
	private String name;

	@Column(name="description", length=64)
	private String description;

	@Column(name="format", length=16)
	private String format;

	@Column(name="joinType", length=16)
	private String joinType;

	@Column(name="OS", length=16)
	private String os;

	@Column(name="tool", length=16)
	private String tool;

	//bi-directional many-to-one association to HWConf
	@ManyToOne
	@JsonIgnore
	@JoinColumn(name="hwconf_id", columnDefinition ="BIGINT UNSIGNED NOT NULL")
	private HWConf hwconf;

	@Transient
	private Timestamp lastCloned;

	public Partition() {
	}

	public Partition(String name) {
		this.name = name.length() > 32 ? name.substring(0,32) : name ;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description.length() > 64 ? description.substring(0,64) : description ;
	}

	public String getFormat() {
		return this.format;
	}

	public void setFormat(String format) {
		this.format = format.length() > 16 ? format.substring(0,16) : format ;
	}

	public String getJoinType() {
		return this.joinType;
	}

	public void setJoinType(String joinType) {
		this.joinType = joinType.length() > 16 ? joinType.substring(0,16) : joinType ;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name.length() > 32 ? name.substring(0,32) : name ;
	}

	public String getOs() {
		return this.os;
	}

	public void setOs(String os) {
		this.os = os.length() > 16 ? os.substring(0,16) : os ;
	}

	public String getTool() {
		return this.tool;
	}

	public void setTool(String tool) {
		this.tool = tool.length() > 16 ? tool.substring(0,16) : tool ;
	}

	public HWConf getHwconf() {
		return this.hwconf;
	}

	public void setHwconf(HWConf hwconf) {
		this.hwconf  = hwconf;
	}

	public Timestamp getLastCloned() {
		return lastCloned;
	}

	public void setLastCloned(Timestamp lastCloned) {
		this.lastCloned = lastCloned;
	}

}
