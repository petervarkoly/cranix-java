package de.cranix.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="Parents")
public class Parent extends AbstractEntity {

    @Column(name = "givenName", length = 64)
    private String givenName = "";

    @Column(name = "surName", length = 64)
    private String surName = "";

    @Column(name = "emailAddress", length = 64)
    private String emailAddress = "";

    @Column(name = "otp", length = 64)
    private String otp = "";

    @ManyToMany
    @JoinTable(
            name = "MyChildren",
            joinColumns = {@JoinColumn(name = "parent_id", columnDefinition = "BIGINT UNSIGNED NOT NULL")},
            inverseJoinColumns = {@JoinColumn(name = "child_id", columnDefinition = "BIGINT UNSIGNED NOT NULL")}
    )
    @JsonIgnore
    private List<User> children = new ArrayList<User>();

    @OneToMany(mappedBy = "parent")
    List<ParentRequest> requests = new ArrayList<>();

    public Parent(){ super();}

    public Parent(Session session){
        super(session);
    }
    public Parent(
            Session session,
            String givenName,
            String surName,
            String emailAddress,
            List<User> children
    ){
        super(session);
        this.givenName = givenName;
        this.surName = surName;
        this.emailAddress = emailAddress;
        this.children = children;
    }
}
