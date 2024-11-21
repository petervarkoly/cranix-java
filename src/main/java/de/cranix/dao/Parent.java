package de.cranix.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="Parents")
public class Parent extends User {

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
        this.setGivenName(givenName);
        this.setSurName(surName)
        this.emailAddress = emailAddress;
        this.children = children;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public List<User> getChildren() {
        return children;
    }

    public void setChildren(List<User> children) {
        this.children = children;
    }

    public List<ParentRequest> getRequests() {
        return requests;
    }

    public void setRequests(List<ParentRequest> requests) {
        this.requests = requests;
    }
}
