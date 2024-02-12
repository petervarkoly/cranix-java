package de.cranix.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * The type CrxQuestion.
 */
@Entity
@Table(name = "CrxQuestions")
@NamedQueries({
        @NamedQuery(name="Question.findAll", query="SELECT q FROM CrxQuestion q")
})
public class CrxQuestion extends AbstractEntity {
    public enum ANSWER_TYPE {
                Text, Multiple, One
    };

    @NotNull
    @Lob
    @Column(name = "question")
    private String question;

    @NotNull
    @Column(name = "value")
    private Integer value;

    @NotNull
    @Size(min = 1, max = 16)
    @Column(name = "answerType")
    private ANSWER_TYPE answerType;

    @NotNull
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name="crxchallenge_id",columnDefinition ="BIGINT UNSIGNED")
    private CrxChallenge challenge;

    @OneToMany(mappedBy="crxQuestion", cascade=CascadeType.ALL, orphanRemoval=true)
    private List<CrxQuestionAnswer> crxQuestionAnswers = new ArrayList<CrxQuestionAnswer>();

    @PrePersist
    void preInsert() {
        if (this.value == null)
            this.value = 1;
        if(this.answerType == null)
            this.answerType = ANSWER_TYPE.Multiple;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public ANSWER_TYPE getAnswerType() {
        return answerType;
    }

    public void setAnswerType(ANSWER_TYPE answerType) {
        this.answerType = answerType;
    }

    public CrxChallenge getChallenge() {
        return challenge;
    }

    public void setChallenge(CrxChallenge challenge) {
        this.challenge = challenge;
    }

    public List<CrxQuestionAnswer> getCrxQuestionAnswers() {
        return crxQuestionAnswers;
    }

    public void setCrxQuestionAnswers(List<CrxQuestionAnswer> crxQuestionAnswers) {
        this.crxQuestionAnswers = crxQuestionAnswers;
    }
}
