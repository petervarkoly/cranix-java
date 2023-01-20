package de.cranix.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The type CrxChallenge.
 */
@Entity
@Table(name = "CrxChallenges")
public class CrxChallenge extends AbstractEntity {

    @NotNull
    @Lob
    @Column(name = "description")
    private String description;

    @NotNull
    @Column(name = "value")
    private Integer value;

    @NotNull
    @Column(name = "validFrom")
    @Temporal(TemporalType.TIMESTAMP)
    private Date validFrom;

    @NotNull
    @Column(name = "validUntil")
    @Temporal(TemporalType.TIMESTAMP)
    private Date validUntil;

    @OneToMany(mappedBy="challenge", cascade=CascadeType.ALL, orphanRemoval=true )
    private List<CrxQuestion> questions = new ArrayList<CrxQuestion>();

    @NotNull
    @Convert(converter=BooleanToStringConverter.class)
    @Column(name = "studentsOnly", length = 1)
    private Boolean studentsOnly;

    @ManyToMany()
    @JoinTable(
            name="GroupsOfChallenges",
            joinColumns={ @JoinColumn(name="crxchallenge_id") },
            inverseJoinColumns={ @JoinColumn(name="group_id") }
    )
    private List<Group> groups = new ArrayList<Group>();

    @ManyToMany()
    @JoinTable(
            name="UsersOfChallenges",
            joinColumns={ @JoinColumn(name="crxchallenge_id") },
            inverseJoinColumns={ @JoinColumn(name="user_id") }
    )
    private List<User> users = new ArrayList<User>();

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public List<CrxQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(List<CrxQuestion> questions) {
        this.questions = questions;
    }

    public Date getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

    public Date getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
    }

    public Boolean getStudentsOnly() {
        return studentsOnly;
    }

    public void setStudentsOnly(Boolean studentsOnly) {
        this.studentsOnly = studentsOnly;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
