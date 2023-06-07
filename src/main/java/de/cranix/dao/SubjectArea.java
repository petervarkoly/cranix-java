package de.cranix.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;

@Entity
@Table(name ="SubjectAreas")
@NamedQueries({
        @NamedQuery(name="SubjectArea.findAll", query="SELECT sa FROM SubjectArea sa"),
        @NamedQuery(name="SubjectArea.findByName",   query="SELECT sa FROM SubjectArea sa WHERE sa.name = :name")
})
public class SubjectArea extends AbstractEntity{
    @NotNull
    @Column(name = "name", length = 64)
    @Size(max = 64, message = "Name of a teaching subject must not be longer then 64 characters")
    private String name;

    @NotNull
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name="teachingsubject_id")
    private TeachingSubject teachingSubject;

    @ManyToMany
    @JsonIgnore
    @JoinTable(
            name="ChallengesInArea",
            joinColumns={ @JoinColumn(name="subjectarea_id") },
            inverseJoinColumns={ @JoinColumn(name="crxchallenge_id") }
    )
    private ArrayList<CrxChallenge> challenges;

    @ManyToMany
    @JsonIgnore
    @JoinTable(
            name="QuestionInArea",
            joinColumns={ @JoinColumn(name="subjectarea_id") },
            inverseJoinColumns={ @JoinColumn(name="crxquestion_id") }
    )
    private ArrayList<CrxQuestion> questions;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TeachingSubject getTeachingSubject() {
        return teachingSubject;
    }

    public void setTeachingSubject(TeachingSubject teachingSubject) {
        this.teachingSubject = teachingSubject;
    }

    public ArrayList<CrxChallenge> getChallenges() {
        return challenges;
    }

    public void setChallenges(ArrayList<CrxChallenge> challenges) {
        this.challenges = challenges;
    }

    public ArrayList<CrxQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(ArrayList<CrxQuestion> questions) {
        this.questions = questions;
    }
}
