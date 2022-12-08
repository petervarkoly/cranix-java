package de.cranix.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Calendar;

import static javax.persistence.TemporalType.DATE;

@MappedSuperclass
public abstract class AbstractEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @ManyToOne()
    @JsonIgnore
    private User creator;

    @Column(name = "created", updatable = false, columnDefinition = "timestamp DEFAULT CURRENT_TIMESTAMP")
    @Temporal(DATE)
    private Calendar created;

    @Column(name = "modified", columnDefinition = "timestamp ON UPDATE CURRENT_TIMESTAMP ")
    @Temporal(DATE)
    private Calendar modified;

    /**
     * Gets created.
     *
     * @return the created
     */
    public Calendar getCreated() {
        return created;
    }

    /**
     * Sets created.
     *
     * @param created the created
     */
    public void setCreated(Calendar created) {
        this.created = created;
    }

    /**
     * Gets modified.
     *
     * @return the modified
     */
    public Calendar getModified() {
        return modified;
    }

    /**
     * Sets modified.
     *
     * @param modified the modified
     */
    public void setModified(Calendar modified) {
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
    public void setCreator(User creator) {
        this.creator = creator;
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
