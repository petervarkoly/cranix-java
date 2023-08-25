package de.cranix.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Entity
@Table(name = "TeachingSubjects")
@NamedQueries({
        @NamedQuery(name="TeachingSubject.findAll", query="SELECT ts FROM TeachingSubject ts"),
        @NamedQuery(name="TeachingSubject.findByName",   query="SELECT ts FROM TeachingSubject ts WHERE ts.name = :name")
})
public class TeachingSubject extends AbstractEntity {

    @NotNull
    @Column(name = "name", length = 64, unique=true)
    @Size(max = 64, message = "Name of a teaching subject must not be longer then 64 characters")
    private String name;

    @OneToMany(mappedBy="teachingSubject", cascade=CascadeType.ALL)
    private List<SubjectArea> subjectAreaList;

    @JsonIgnore
    @OneToMany(mappedBy="teachingSubject", cascade=CascadeType.MERGE)
    private List<CrxChallenge> crxChallenges;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<SubjectArea> getSubjectAreaList() {
        return subjectAreaList;
    }

    public void setSubjectAreaList(List<SubjectArea> subjectAreaList) {
        this.subjectAreaList = subjectAreaList;
    }

    public List<CrxChallenge> getCrxChallenges() {
        return crxChallenges;
    }

    public void setCrxChallenges(List<CrxChallenge> crxChallenges) {
        this.crxChallenges = crxChallenges;
    }
}
