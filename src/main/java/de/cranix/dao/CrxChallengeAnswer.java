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
@Table(
        name = "CrxChallengeAnswers",
        uniqueConstraints = { @UniqueConstraint(columnNames = { "creator_id", "crxquestionanswer_id" }) }
)
public class CrxChallengeAnswer extends AbstractEntity {

    @NotNull
    @Convert(converter=BooleanToStringConverter.class)
    @Column(name = "correct", length = 1)
    private Boolean correct;

    @NotNull
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name="crxquestionanswer_id", columnDefinition ="BIGINT UNSIGNED NOT NULL")
    private CrxQuestionAnswer crxQuestionAnswer;

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
}
