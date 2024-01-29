/* (c) 2017-2023 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.dao;

import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The persistent class for the CrxConfig database table.
 */
@Entity
@Table(
	name="CrxConfig",
	uniqueConstraints = { @UniqueConstraint(columnNames = { "objectType", "objectId", "keyword", "value" }) }
)
@NamedQueries({
	@NamedQuery(name="CrxConfig.getAllById",    query="SELECT c FROM CrxConfig c WHERE c.objectType = :type AND c.objectId = :id"),
        @NamedQuery(name="CrxConfig.getAllByKey",   query="SELECT c FROM CrxConfig c WHERE c.objectType = :type AND c.keyword  = :keyword"),
        @NamedQuery(name="CrxConfig.get",           query="SELECT c FROM CrxConfig c WHERE c.objectType = :type AND c.objectId = :id AND c.keyword = :keyword"),
        @NamedQuery(name="CrxConfig.check",         query="SELECT c FROM CrxConfig c WHERE c.objectType = :type AND c.objectId = :id AND c.keyword = :keyword AND c.value = :value")
})
@SequenceGenerator(name="seq", initialValue=1, allocationSize=100)
public class CrxConfig extends AbstractEntity {

	@Column(name = "objectType")
        @Size(max=12, message="objectType must not be longer then 12 characters.")
        private String objectType;

	@Column(name = "objectId")
        private Long  objectId;

	@Column(name = "keyword")
        @Size(max=64, message="keyword must not be longer then 64 characters.")
        private String keyword;

	@Column(name = "value")
        @Size(max=128, message="value must not be longer then 128 characters.")
        private String value;
        
        public CrxConfig() {}

	public String getObjectType() {
	    return this.objectType;
	}

	public void setObjectType(String objectType) {
	    this.objectType = objectType;
	}

	public Long getObjectId() {
	    return this.objectId;
	}

	public void setObjectId(Long objectId) {
	    this.objectId = objectId;
	}

	public String getKeyword() {
	    return this.keyword;
	}

	public void setKeyword(String keyword) {
	    this.keyword = keyword;
	}

	public String getValue() {
	    return this.value;
	}

	public void setValue(String value) {
	    this.value = value;
	}
}
