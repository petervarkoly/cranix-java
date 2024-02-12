package de.cranix.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

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
    @JoinColumn(name="teachingsubject_id", columnDefinition ="BIGINT UNSIGNED NOT NULL")
    private TeachingSubject teachingSubject;

    @ManyToMany
    @JsonIgnore
    @JoinTable(
            name="ChallengesInArea",
            joinColumns={ @JoinColumn(name="subjectarea_id", columnDefinition ="BIGINT UNSIGNED NOT NULL") },
            inverseJoinColumns={ @JoinColumn(name="crxCHallenge_id", columnDefinition ="BIGINT UNSIGNED NOT NULL") }
    )
    private List<CrxChallenge> challenges;

    @ManyToMany
    @JsonIgnore
    @JoinTable(
            name="QuestionInArea",
            joinColumns={ @JoinColumn(name="subjectarea_id", columnDefinition ="BIGINT UNSIGNED NOT NULL") },
            inverseJoinColumns={ @JoinColumn(name="crxquestion_id", columnDefinition ="BIGINT UNSIGNED NOT NULL") }
    )
    private List<CrxQuestion> questions;

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

    public List<CrxChallenge> getChallenges() {
        return challenges;
    }

    public void setChallenges(List<CrxChallenge> challenges) {
        this.challenges = challenges;
    }

    public List<CrxQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(List<CrxQuestion> questions) {
        this.questions = questions;
    }
}
