package de.cranix.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
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

    @OneToMany(mappedBy="challenge", cascade=CascadeType.ALL )
    private List<CrxQuestion> questions = new ArrayList<CrxQuestion>();

    @ManyToMany(mappedBy="challenges", cascade=CascadeType.ALL)
    @JsonIgnore
    private List<Category> categories = new ArrayList<Category>();

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
}
