package de.cranix.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.TemporalType.TIMESTAMP;

@Entity
@Table( name="ParentRequests" )
public class ParentRequest extends AbstractEntity{

    @Column(name = "givenName", length = 64)
    private String givenName = "";

    @Column(name = "surName", length = 64)
    private String surName = "";

    @Column(name = "childGivenName", length = 64)
    private String childGivenName = "";

    @Column(name = "childSurName", length = 64)
    private String childSurName = "";

    @Column(name = "childClasses", length = 32)
    private String childClasses = "";

    @Column(name = "childBirthDay", length = 10)
    private String childBirthDay = "";

    @Convert(converter=BooleanToStringConverter.class)
    @Column(name = "proceeded", columnDefinition = "CHAR(1) DEFAULT 'Y'")
    private Boolean proceeded = false;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "parent_id", columnDefinition ="BIGINT UNSIGNED")
    private User parent;

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getSurName() {
        return surName;
    }

    public void setSurName(String surName) {
        this.surName = surName;
    }

    public Boolean getProceeded() {
        return proceeded;
    }

    public void setProceeded(Boolean proceeded) {
        this.proceeded = proceeded;
    }

    public User getParent() {
        return parent;
    }

    public void setParent(User parent) {
        this.parent = parent;
    }

    public String getChildGivenName() {
        return childGivenName;
    }

    public void setChildGivenName(String childGivenName) {
        this.childGivenName = childGivenName;
    }

    public String getChildSurName() {
        return childSurName;
    }

    public void setChildSurName(String childSurName) {
        this.childSurName = childSurName;
    }

    public String getChildClasses() {
        return childClasses;
    }

    public void setChildClasses(String childClasses) {
        this.childClasses = childClasses;
    }

    public String getChildBirthDay() {
        return childBirthDay;
    }

    public void setChildBirthDay(String childBirthDay) {
        this.childBirthDay = childBirthDay;
    }
}
