/* (c) 2023 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.dao;

import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
/**
 * The persistent class for the OSSMConfir database table.
 *
 */
@Entity
@Table(
	name="CrxMConfig",
	uniqueConstraints = { @UniqueConstraint(columnNames = { "objectType", "objectId", "keyword", "value" }) }
)
@NamedQueries({
	@NamedQuery(name="CrxMConfig.getAllForKey",query="SELECT c FROM CrxMConfig c WHERE c.keyword = :keyword"),
        @NamedQuery(name="CrxMConfig.getAllById",  query="SELECT c FROM CrxMConfig c WHERE c.objectType = :type AND c.objectId = :id"),
        @NamedQuery(name="CrxMConfig.getAllByKey", query="SELECT c FROM CrxMConfig c WHERE c.objectType = :type AND c.keyword  = :keyword"),
        @NamedQuery(name="CrxMConfig.get",         query="SELECT c FROM CrxMConfig c WHERE c.objectType = :type AND c.objectId = :id AND c.keyword = :keyword"),
        @NamedQuery(name="CrxMConfig.getAllObject",query="SELECT c FROM CrxMConfig c WHERE c.objectType = :type AND c.keyword = :keyword AND c.value = :value"),
        @NamedQuery(name="CrxMConfig.check",       query="SELECT c FROM CrxMConfig c WHERE c.objectType = :type AND c.objectId = :id AND c.keyword = :keyword AND c.value = :value")
})
@SequenceGenerator(name="seq", initialValue=1, allocationSize=100)
public class CrxMConfig extends AbstractEntity {

	@Column(name = "objectType")
        @Size(max=12, message="objectType must not be longer then 12 characters.")
        private String objectType;

        @Column(name = "objectId", columnDefinition ="BIGINT UNSIGNED")
        private Long  objectId;

        @Column(name = "keyword")
        @Size(max=64, message="keyword must not be longer then 64 characters.")
        private String keyword;

        @Column(name = "value")
        @Size(max=128, message="value must not be longer then 128 characters.")
        private String value;

        public CrxMConfig() {
        }

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
