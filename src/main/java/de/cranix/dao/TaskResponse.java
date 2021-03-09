/* (c) 2021 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.dao;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.validation.constraints.Size;


/**
 * The persistent class of the table Tasks
 * @author varkoly
 *
 */
@Entity
@Table(name="TaskResponses")
@NamedQueries({
        @NamedQuery(name="TaskResponse.findAll", query="SELECT t FROM TaskResponse t")
})
public class TaskResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * The technical id of the task
     */
    @Id
    @SequenceGenerator(name="TASKS_ID_GENERATOR" )
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="TASKS_ID_GENERATOR")
    private Long id;

    //bi-directional many-to-one association to User
    @ManyToOne
    private User owner;

    @Column(name="owner_id", insertable=false, updatable=false)
    private Long ownerId;

    @ManyToOne
    @JsonIgnore
    private Announcement parent;

    @Column(name="parent_id", insertable=false, updatable=false)
    private Long parentId;

    /**
     * The content of the task response. Maximal length is 16MB
     */
    @Lob
    @Column(name = "text")
    private String text;

    @Column(name = "rating", length = 8192)
    private String rating;

    public void TaskResponse() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Announcement getParent() {
        return parent;
    }

    public void setParent(Announcement parent) {
        this.parent = parent;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public User getOwner() {
        return this.owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getTitle() {
        return ( this.parent == null ) ? null : this.parent.getTitle();
    }
    public Date getValidUntil() {
        return ( this.parent == null ) ? null : this.parent.getValidUntil();
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
        TaskResponse other = (TaskResponse) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
}
