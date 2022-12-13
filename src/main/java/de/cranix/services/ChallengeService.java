package de.cranix.services;

import de.cranix.dao.*;
import org.eclipse.jetty.server.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;
import javax.persistence.EntityManager;
import java.util.*;

public class ChallengeService extends Service {

    Logger logger = LoggerFactory.getLogger(ChallengeService.class);

    public ChallengeService(Session session, EntityManager em) {
        super(session, em);
    }

    public CrxResponse add(CrxChallenge challenge) {
        CategoryService categoryService = new CategoryService(this.session,this.em);;
        this.em.getTransaction().begin();
        challenge.setCreator(this.session.getUser());
        this.em.persist(challenge);
        for(CrxQuestion crxQuestion: challenge.getQuestions()) {
            crxQuestion.setCreator(this.session.getUser());
            crxQuestion.setChallenge(challenge);
            this.em.merge(crxQuestion);
            for(CrxQuestionAnswer answer: crxQuestion.getCrxQuestionAnswers()) {
                answer.setCreator(this.session.getUser());
                answer.setCrxQuestion(crxQuestion);
                this.em.merge(answer);
            }
        }
        Category category = challenge.getCategories().get(0);
        for(Long id: category.getGroupIds()) {
            categoryService.addMember(category, "Group", id);
        }
        for(Long id: category.getUserIds()) {
            categoryService.addMember(category, "User", id);
        }
        for(Long id: category.getRoomIds()) {
            categoryService.addMember(category, "Room", id);
        }
        this.session.getUser().getChallenges().add(challenge);
        this.em.merge(this.session.getUser());
        this.em.getTransaction().commit();
        return new CrxResponse(this.session, "OK", "Challenge was successfully added", challenge.getId());
    }

    public CrxChallenge getById(long id) {
        try {
            return this.em.find(CrxChallenge.class, id);
        } catch (Exception e) {
            return null;
        }
    }

    public CrxResponse delete(Long id) {
        CrxChallenge challenge = this.getById(id);
        if (challenge == null) {
            return new CrxResponse(this.getSession(), "ERROR", "Could not find the challenge");
        }
        this.em.getTransaction().begin();
        this.em.remove(challenge);
        this.em.getTransaction().commit();
        return new CrxResponse(this.getSession(), "OK", "Challenge was removed successfully.");
    }

    public Object evaluate(Long id) {
        CrxChallenge challenge = this.getById(id);
        if (challenge == null) {
            return new CrxResponse(this.getSession(), "ERROR", "Could not find the challenge");
        }
        Map<Long, Map<Long, Integer>> results = new HashMap<>();
        for (CrxQuestion question : challenge.getQuestions()) {
            Map<Long, Integer> result = new HashMap<>();
            for (CrxQuestionAnswer answer : question.getCrxQuestionAnswers()) {
                for (CrxChallengeAnswer challengeAnswer : answer.getChallengeAnswers()) {
                    Long creatorId = challengeAnswer.getCreator().getId();
                    if (!result.containsKey(creatorId)) {
                        result.put(creatorId, 0);
                    }
                    if( question.getAnswerType().equals(CrxQuestion.ANSWER_TYPE.One)) {
                        if (challengeAnswer.getCorrect() && answer.getCorrect()) {
                            result.put(creatorId, question.getValue());
                        }
                    }
                    if( question.getAnswerType().equals(CrxQuestion.ANSWER_TYPE.Multiple)) {
                        Integer actualValue = result.get(answer.getCreator().getId());
                        if (challengeAnswer.getCorrect() ^ answer.getCorrect()) {
                            result.put(creatorId, actualValue - question.getValue());
                        } else {
                            result.put(creatorId, actualValue + question.getValue());
                        }
                    }
                }
            }
            for( Long uid: result.keySet() ){
                if( !results.containsKey(uid) ){
                    results.put(uid, new HashMap<Long, Integer>() );
                }
                results.get(uid).put(question.getId(), result.get(uid));
            }
        }
        return results;
    }

    public Object getResultOfUser(Long id, Long creatorId) {
        CrxChallenge challenge = this.getById(id);
        if (challenge == null) {
            return new CrxResponse(this.getSession(), "ERROR", "Could not find the challenge");
        }
        Map<Long, Map<Long, Boolean>> results = new HashMap<>();
        for (CrxQuestion question : challenge.getQuestions()) {
            results.put(question.getId(), new HashMap<Long, Boolean>());
            for (CrxQuestionAnswer answer : question.getCrxQuestionAnswers()) {
                for (CrxChallengeAnswer challengeAnswer : answer.getChallengeAnswers()) {
                    if( challengeAnswer.getCreator().getId().equals(creatorId) ) {
                        results.get(question.getId()).put(
                                answer.getId(), challengeAnswer.getCorrect()
                        );
                    }
                }
            }
        }
        return results;
    }

    public CrxResponse saveChallengeAnswer(Long questionAnswerId, Boolean answer){
        CrxQuestionAnswer questionAnswer= this.em.find(CrxQuestionAnswer.class, questionAnswerId);
        CrxChallengeAnswer challengeAnswer = new CrxChallengeAnswer();
        challengeAnswer.setCreator(this.session.getUser());
        challengeAnswer.setCrxQuestionAnswer(questionAnswer);
        challengeAnswer.setCorrect(answer);
        this.em.getTransaction().begin();
        this.em.persist(challengeAnswer);
        questionAnswer.getChallengeAnswers().add(challengeAnswer);
        this.em.getTransaction().commit();
        return new CrxResponse(this.getSession(),"OK", "Answer was saved correct.");
    }

    public CrxResponse saveChallengeAnswers(Map<Long,Boolean> answers){
        for( Long answerId: answers.keySet()){
            saveChallengeAnswer(answerId, answers.get(answerId));
        }
        return new CrxResponse(this.getSession(),"OK", "Answers were saved correct.");
    }

    public List<CrxChallenge> getTodos(){
        List<CrxChallenge> result = new ArrayList<CrxChallenge>();
        for(Category category: this.session.getUser().getCategories()){
            for(CrxChallenge challenge: category.getChallenges()){
                result.add(challenge);
            }
        }
        for(Group group: this.session.getUser().getGroups()){
            for(Category category: group.getCategories()) {
                for (CrxChallenge challenge : category.getChallenges()) {
                    result.add(challenge);
                }
            }
        }
        return result;
    }
}
