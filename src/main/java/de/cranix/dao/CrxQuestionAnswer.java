package de.cranix.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * The type CrxQuestionAnswer.
 */
@Entity
@Table(name = "CrxQuestionAnswers")
public class CrxQuestionAnswer extends AbstractEntity {

    @NotNull
    @Lob
    @Column(name = "answer")
    private String answer;

    @NotNull
    @Convert(converter=BooleanToStringConverter.class)
    @Column(name = "correct", length = 1)
    private Boolean correct;

    @NotNull
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name="crxquestion_id", columnDefinition ="BIGINT UNSIGNED NOT NULL")
    private CrxQuestion crxQuestion;

    @OneToMany(mappedBy="crxQuestionAnswer", cascade=CascadeType.REMOVE, orphanRemoval=true)
    @JsonIgnore
    private List<CrxChallengeAnswer> challengeAnswers = new ArrayList<CrxChallengeAnswer>();

    @PrePersist
    void preInsert() {
        if (this.correct == null)
            this.correct = false;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public Boolean getCorrect() {
        return correct;
    }

    public void setCorrect(Boolean correct) {
        this.correct = correct;
    }

    public CrxQuestion getCrxQuestion() {
        return crxQuestion;
    }

    public void setCrxQuestion(CrxQuestion crxQuestion) {
        this.crxQuestion = crxQuestion;
    }

    public List<CrxChallengeAnswer> getChallengeAnswers() {
        return challengeAnswers;
    }

    public void setChallengeAnswers(List<CrxChallengeAnswer> challengeAnswers) {
        this.challengeAnswers = challengeAnswers;
    }
}
