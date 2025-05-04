package de.cranix.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

import static javax.persistence.TemporalType.DATE;
import static javax.persistence.TemporalType.TIMESTAMP;

@MappedSuperclass
public abstract class AbstractEntity implements Serializable {
    protected static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(
            name = "id",
            columnDefinition ="BIGINT UNSIGNED NOT NULL AUTO_INCREMENT"
    )
    protected Long id;

    @ManyToOne(optional = false)
    @JoinColumn(
            name="creator_id",
            columnDefinition ="BIGINT UNSIGNED"
    )
    @JsonIgnore
    protected User creator;

    protected Long creatorId;

    @Column(
            name = "created",
            updatable = false,
            columnDefinition = "timestamp DEFAULT CURRENT_TIMESTAMP"
    )
    @Temporal(TIMESTAMP)
    protected Date created = new Date();

    @Column(
            name = "modified",
            columnDefinition = "timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
    )
    @Temporal(TIMESTAMP)
    protected Date modified = new Date();

    /**
     * Gets created.
     *
     * @return the created
     */
    public Date getCreated() {
        return created;
    }

    /**
     * Sets created.
     *
     * @param created the created
     */
    public void setCreated(Date created) {
        this.created = created;
    }

    /**
     * Gets modified.
     *
     * @return the modified
     */
    public Date getModified() {
        return modified;
    }

    /**
     * Sets modified.
     *
     * @param modified the modified
     */
    public void setModified(Date modified) {
        this.modified = modified;
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    @JsonProperty
    public Long getId() {
        return id;
    }

    /**
     * Sets id.
     *
     * @param id the id
     */
    @JsonIgnore
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets creator.
     *
     * @return the creator
     */
    public User getCreator() {
        return creator;
    }

    /**
     * Sets creator.
     *
     * @param creator the creator
     */
    public void setCreator(User creator)
    {
        this.creator = creator;
    }

    public Long getCreatorId() {
        if(this.creatorId == null) {
            return this.creator != null ? this.creator.getId() : null;
        }
        return creatorId;
    }

    public void setCreatorId(Long id){
        this.creatorId = id;
    }

    /*
    * Constructor set the creation time to now
     */
    public AbstractEntity() { }

    /*
     * Constructor set the creation time to now and the creator to session user.
     */
    public AbstractEntity(Session session) {
        this.creator = session.getUser();
    }

    /**
     * To string string.
     *
     * @return the string
     */
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
        AbstractEntity other = (AbstractEntity) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
}
