package de.cranix.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
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

    @JsonIgnore
    @OneToMany(mappedBy = "subjectArea")
    private List<CrxChallenge> challenges;

    private Long teachingsubjectId;

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

    public Long getTeachingsubjectId(){
        if( this.teachingsubjectId == null ) {
            this.teachingsubjectId = this.teachingSubject == null ? null : this.teachingSubject.getId();
        }
        return this.teachingsubjectId;
    }
}