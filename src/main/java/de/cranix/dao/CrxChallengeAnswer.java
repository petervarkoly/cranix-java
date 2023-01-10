package de.cranix.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * The type CrxChallengeAnswer.
 */
@Entity
@Table(name = "CrxChallengeAnswers")
public class CrxChallengeAnswer extends AbstractEntity {

    @NotNull
    @Convert(converter=BooleanToStringConverter.class)
    @Column(name = "correct", length = 1)
    private Boolean correct;

    @ManyToOne
    @JsonIgnore
    private CrxQuestionAnswer crxQuestionAnswer;

    @Column(name = "CRXQUESTIONANSWER_ID", insertable = false, updatable = false)
    private Long questionId;


    public Boolean getCorrect() {
        return correct;
    }

    public void setCorrect(Boolean correct) {
        this.correct = correct;
    }

    public CrxQuestionAnswer getCrxQuestionAnswer() {
        return crxQuestionAnswer;
    }

    public void setCrxQuestionAnswer(CrxQuestionAnswer crxQuestionAnswer) {
        this.crxQuestionAnswer = crxQuestionAnswer;
    }

    public Long getQuestionId() {
        return this.questionId;
    }

    public void setQuestionId(Long id) {
        this.questionId = id;
    }
}
