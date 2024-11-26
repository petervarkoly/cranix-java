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

    @Column(name = "className", length = 32)
    private String className = "";

    @Column(name = "birthDay", length = 10)
    private String birthDay = "";

    @Convert(converter=BooleanToStringConverter.class)
    @Column(name = "proceeded", columnDefinition = "CHAR(1) DEFAULT 'Y'")
    private Boolean proceeded = false;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "parent_id", columnDefinition ="BIGINT UNSIGNED NOT NULL")
    private Parent parent;

    public ParentRequest() {super();}

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

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(String birthDay) {
        this.birthDay = birthDay;
    }

    public Boolean getProceeded() {
        return proceeded;
    }

    public void setProceeded(Boolean proceeded) {
        this.proceeded = proceeded;
    }

    public Parent getParent() {
        return parent;
    }

    public void setParent(Parent parent) {
        this.parent = parent;
    }
}
