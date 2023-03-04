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
public class TaskResponse extends AbstractEntity {

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name="parent_id")
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
}
