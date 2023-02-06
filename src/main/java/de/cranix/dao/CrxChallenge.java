package de.cranix.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static de.cranix.helper.CranixConstants.roleStudent;

/**
 * The type CrxChallenge.
 */
@Entity
@Table(name = "CrxChallenges")
@NamedQueries({
        @NamedQuery(name="Challenge.findAll", query="SELECT c FROM CrxChallenge c")
})
public class CrxChallenge extends AbstractEntity {

    @NotNull
    @Lob
    @Column(name = "description")
    private String description;

    @NotNull
    @ManyToOne(cascade = CascadeType.MERGE)
    private TeachingSubject teachingSubject;

    @ManyToMany(mappedBy = "challenges", cascade = CascadeType.MERGE)
    private List<SubjectArea> subjectAreaList;

    @NotNull
    @Column(name = "value")
    private Integer value;

    @NotNull
    @Convert(converter=BooleanToStringConverter.class)
    @Column(name = "released", length = 1)
    private Boolean released;

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

    @PrePersist
    void preInsert() {
        if (this.value == null)
            this.value = 1;
        if(this.released == null)
            this.released = false;
    }

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

    public Boolean isReleased() {
        return released;
    }

    public void setReleased(Boolean released) {
        this.released = released;
    }

    public TeachingSubject getTeachingSubject() { return teachingSubject; }

    public void setTeachingSubject(TeachingSubject teachingSubject) {
        this.teachingSubject = teachingSubject;
    }

    public List<SubjectArea> getSubjectAreaList() { return subjectAreaList; }

    public void setSubjectAreaList(List<SubjectArea> subjectAreaList) {
        this.subjectAreaList = subjectAreaList;
    }

    public List<CrxQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(List<CrxQuestion> questions) {
        this.questions = questions;
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

    @JsonIgnore
    public List<User> getTestUsers() {
        List<User> testUsers = new ArrayList<User>();
        for (User user : this.users) {
            testUsers.add(user);
        }
        for (Group group : this.groups) {
            for (User user : group.getUsers()) {
                if (!testUsers.contains(user) && (!this.studentsOnly || user.getRole().equals(roleStudent))) {
                    testUsers.add(user);
                }
            }
        }
        return testUsers;
    }
}
