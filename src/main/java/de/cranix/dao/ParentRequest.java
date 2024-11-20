package de.cranix.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

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

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "parent_id", columnDefinition ="BIGINT UNSIGNED NOT NULL")
    private Parent parent;
}
