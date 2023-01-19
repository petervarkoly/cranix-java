package de.cranix.services;

import de.cranix.dao.*;
import org.eclipse.jetty.server.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.util.*;
import static de.cranix.helper.CranixConstants.roleStudent;

/**
 * The type Challenge service.
 */
public class ChallengeService extends Service {

    /**
     * The Logger.
     */
    Logger logger = LoggerFactory.getLogger(ChallengeService.class);

    private static final
    String challengeTableStyle =
            "<style>\n" +
            "\n" +
            ".thVertical {\n" +
            "  writing-mode: vertical-lr;\n" +
            "  min-width: 50px;\n" +
            "  text-align:center;\n" +
            "}\n" +
            "\n" +
            "table, th, td {\n" +
            "  border: 1px solid black;\n" +
            "  border-collapse: collapse;\n" +
            "} </style>\n";

    /**
     * Instantiates a new Challenge service.
     *
     * @param session the session
     * @param em      the em
     */
    public ChallengeService(Session session, EntityManager em) {
        super(session, em);
    }

    /**
     * Add crx response.
     *
     * @param challenge the challenge
     * @return the crx response
     */
    public CrxResponse add(CrxChallenge challenge) {
        this.em.getTransaction().begin();
        challenge.setCreator(this.session.getUser());
        this.em.persist(challenge);
        this.adapt(challenge);
        this.session.getUser().getChallenges().add(challenge);
        this.em.merge(this.session.getUser());
        this.em.getTransaction().commit();
        return new CrxResponse(this.session, "OK", "Challenge was successfully added.", challenge.getId());
    }

    /**
     * Modify crx response.
     *
     * @param challenge the challenge
     * @return the crx response
     */
    public CrxResponse modify(CrxChallenge challenge){
        CrxResponse resp = this.isModifiable(challenge.getId());
        if(resp != null) {
            return resp;
        }
        this.em.getTransaction().begin();
        if( challenge.getCreator() == null ){
            challenge.setCreator(this.session.getUser());
        }
        this.em.merge(challenge);
        this.adapt(challenge);
        this.em.getTransaction().commit();
        return new CrxResponse(this.session, "OK", "Challenge was successfully modified.", challenge.getId());
    }

    private void adapt(CrxChallenge challenge){
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
        for(User user: challenge.getUsers()) {
            if(! user.getTodos().contains(challenge) ){
                user.getTodos().add(challenge);
                this.em.merge(user);
            }
        }
        for(Group group: challenge.getGroups()) {
            if(! group.getTodos().contains(challenge) ){
                group.getTodos().add(challenge);
                this.em.merge(group);
            }
        }
    }

    /**
     * Gets the challenges created by the session user.
     *
     * @return the challenges
     */
    public List<CrxChallenge> getChallenges() {
        User user = this.em.find(User.class, this.getSession().getUserId());
        return user.getChallenges();
        /**
         List<CrxChallenge> challengeList = new ArrayList<CrxChallenge>();
         for(CrxChallenge challenge: user.getChallenges() ) {
         challenge.setQuestions(new ArrayList<CrxQuestion>());
         challengeList.add(challenge);
         }
         return challengeList;
         **/
    }

    /**
     * Delete an CrxQuestion. Modify can not be used to remove an existing question. This method implements it.
     *
     * @param challengeId the challenge id
     * @param questionId  the question id
     * @return the crx response
     */
    /*
    *
     */
    public CrxResponse deleteQuestion(Long challengeId, Long questionId) {
        CrxResponse resp = this.isModifiable(challengeId);
        if(resp != null) {
            return resp;
        }
        CrxChallenge challenge = this.getById(challengeId);
        CrxQuestion question;
        try {
            question = this.em.find(CrxQuestion.class, questionId);
        } catch (Exception e) {
            return new CrxResponse(this.getSession(),"ERROR", "Can not find question.");
        }
        this.em.getTransaction().begin();
        challenge.getQuestions().remove(question);
        this.em.remove(question);
        this.em.merge(challenge);
        this.em.getTransaction().commit();
        return new CrxResponse(this.getSession(),"OK", "Question was removed.");
    }

    /**
     * Delete an CrxQuestionAnswer. Modify can not be used to remove an existing question answer. This method implements it.
     *
     * @param challengeId the challenge id
     * @param questionId  the question id
     * @param answerId    the answer id
     * @return the crx response
     */
    public CrxResponse deleteAnswer(Long challengeId, Long questionId, Long answerId) {
        CrxResponse resp = this.isModifiable(challengeId);
        if(resp != null) {
            return resp;
        }
        CrxChallenge challenge = this.getById(challengeId);
        CrxQuestion question;
        CrxQuestionAnswer answer;
        try {
            question = this.em.find(CrxQuestion.class, questionId);
            answer = this.em.find(CrxQuestionAnswer.class, answerId);
        } catch (Exception e) {
            return new CrxResponse(this.getSession(),"ERROR", "Can not find challenge.");
        }
        this.em.getTransaction().begin();
        question.getCrxQuestionAnswers().remove(answer);
        this.em.remove(answer);
        this.em.merge(question);
        this.em.merge(challenge);
        this.em.getTransaction().commit();
        return new CrxResponse(this.getSession(),"OK", "Answer was removed.");
    }

    /**
     * Gets by id.
     *
     * @param id the id
     * @return the by id
     */
    public CrxChallenge getById(long id) {
        try {
            return this.em.find(CrxChallenge.class, id);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Delete CrxChallenge.
     *
     * @param id the id
     * @return the crx response
     */
    public CrxResponse delete(Long id) {
        CrxResponse resp = this.isModifiable(id);
        if(resp != null) {
            return resp;
        }
        CrxChallenge challenge = this.getById(id);
        this.em.getTransaction().begin();
        this.em.remove(challenge);
        this.em.getTransaction().commit();
        return new CrxResponse(this.getSession(), "OK", "Challenge was removed successfully.");
    }

    /**
     * Evaluate a challenge.
     *
     * @param id the id
     * @return the object
     */
    public Object evaluate(Long id) {
        CrxResponse resp = this.isModifiable(id);
        if(resp != null) {
            return resp;
        }
        CrxChallenge challenge = this.getById(id);
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
        logger.debug("results: " + results);
        for( Long uid: results.keySet() ){
            results.get(uid).put(0L, 0);
            for( Long questionId: results.get(uid).keySet() ){
                logger.debug("res:" + uid + ":" + questionId);
                Integer res = results.get(uid).get(questionId) + results.get(uid).get(0L);
                results.get(uid).put(0L, res);
            }
        }
        return results;
    }
    public String evaluateAsHtml(Long id) {
        UserService userService = new UserService(this.session,this.em);
        CrxResponse resp = this.isModifiable(id);
        if (resp != null) {
            return resp.toString();
        }
        StringBuilder htmlResult = new StringBuilder();
        htmlResult.append(challengeTableStyle);
        htmlResult.append("<table");
        htmlResult.append("  <tr>\n");
        htmlResult.append("    <th>Question</th>\n");
        Map<Long, Map<Long, Integer>> results = (Map<Long, Map<Long, Integer>>) this.evaluate(id);
        for(Long userId: results.keySet()) {
            htmlResult.append("    <th  class=\"thVertical\">").append(
                    userService.getById(userId).getFullName()
            ).append("</th>\n");
        }
        htmlResult.append("  </tr>\n");
        CrxChallenge challenge = this.getById(id);
        for (CrxQuestion question : challenge.getQuestions()) {
            htmlResult.append("  <tr>\n");
            htmlResult.append("    <td>").append(question.getQuestion()).append("</td>\n");
            for(Long userId: results.keySet()) {
                htmlResult.append("    <td>").append(
                        results.get(userId).get(question.getId())
                ).append("</td>\n");
            }
            htmlResult.append("  </tr>\n");
        }
        htmlResult.append("  <tr>\n");
        htmlResult.append("    <td>").append("Sum:").append("</td>\n");
        for(Long userId: results.keySet()) {
            htmlResult.append("    <td>").append(
                    results.get(userId).get(0L)
            ).append("</td>\n");
        }
        htmlResult.append("  </tr>\n");
        htmlResult.append("</table>");
        return htmlResult.toString();
    }

        /**
         * Archive the results of a challenge.
         *
         * @param id      the id
         * @param cleanUp the clean up
         * @return the object
         */
    public Object archiveResults(Long id, Boolean cleanUp){
        CrxResponse resp = this.isModifiable(id);
        if(resp != null) {
            return resp;
        }
        CrxChallenge challenge = this.getById(id);
        if (challenge == null) {
            return new CrxResponse(this.getSession(), "ERROR", "Could not find the challenge");
        }
        if( !this.session.getUser().equals(challenge.getCreator())) {
            return new CrxResponse(this.getSession(), "ERROR", "Only the creator may evaluate a challenge");
        }
        if(cleanUp) {
            // We start a transaction if the challenge have to be cleaned up.
            this.em.getTransaction().begin();
        }
        List<User> testUsers = new ArrayList<User>();
        for(User user: challenge.getUsers()){
            testUsers.add(user);
        }
        for(Group group: challenge.getGroups()){
            for(User user: group.getUsers()){
                if(!testUsers.contains(user) && ( !challenge.getStudentsOnly() || user.getRole().equals(roleStudent))){
                    testUsers.add(user);
                }
            }
        }
        Map<Long, Map<Long, Integer>> results = (Map<Long, Map<Long, Integer>>) this.evaluate(id);
        Map<Integer, Long> placeToId = new HashMap<Integer, Long>();
        Map<Long, Integer> idToPlace = new HashMap<Long, Integer>();
        List<String> line = new ArrayList<String>();
        List<List<String>> resultTable = new ArrayList<>();
        Integer i = 2;
        line.add("Question/Answer Text");
        line.add("Correct answer");
        for(User user: testUsers){
            idToPlace.put(user.getId(), i);
            placeToId.put(i, user.getId());
            i++;
            line.add(user.getFullName() + " " + user.getBirthDay() + " " + user.getClasses());
        }
        resultTable.add(line);
        for (CrxQuestion question : challenge.getQuestions()) {
            line = new ArrayList<String>();
            line.add(question.getQuestion());
            line.add(question.getAnswerType().toString());
            for(i = 0; i < testUsers.size(); i++) {
                Long userId = placeToId.get(i+2);
                if( results.containsKey(userId)) {
                    line.add(results.get(userId).get(question.getId()).toString());
                } else {
                    line.add("");
                }
            }
            resultTable.add(line);
            for (CrxQuestionAnswer answer : question.getCrxQuestionAnswers()) {
                line = new ArrayList<String>();
                for(i = 0; i < testUsers.size() +2 ; i++) {
                    line.add("");
                }
                line.set(0,answer.getAnswer());
                line.set(1,answer.getCorrect() ? "Y":"N");
                for( CrxChallengeAnswer challengeAnswer: answer.getChallengeAnswers()) {
                    logger.debug("challengeAnswer" + challengeAnswer);
                    line.set(
                            idToPlace.get(challengeAnswer.getCreator().getId()),
                            challengeAnswer.getCorrect() ? "Y":"N"
                    );
                    if(cleanUp) {
                        answer.getChallengeAnswers().remove(challengeAnswer);
                        this.em.remove(challengeAnswer);
                    }
                }
                resultTable.add(line);
                if(cleanUp) {
                    this.em.merge(answer);
                }
            }
        }
        if(cleanUp) {
            this.em.getTransaction().commit();
        }
        return  resultTable;
    }
    /**
     * Gets result of user.
     *
     * @param id        the id
     * @param creatorId the creator id
     * @return the result of user
     */
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

    /*
    Functions to work on todos
     */

    private CrxChallenge clearRightResults(CrxChallenge challenge) {
        for(CrxQuestion question: challenge.getQuestions()){
            for(CrxQuestionAnswer answer: question.getCrxQuestionAnswers()){
                answer.setChallengeAnswers(null);
                answer.setCorrect(false);
            }
        }
        return challenge;
    }

    /**
     * Get todos list.
     *
     * @return the list
     */
    public List<CrxChallenge> getTodos(){
        List<CrxChallenge> result = new ArrayList<CrxChallenge>();
        for(CrxChallenge challenge: this.session.getUser().getTodos()){
            if(this.isAvailable(challenge)) {
                result.add(clearRightResults(challenge));
            }
        }
        for(Group group: this.session.getUser().getGroups()){
            for(CrxChallenge challenge: group.getTodos()) {
                if(this.isAvailable(challenge)) {
                    result.add(clearRightResults(challenge));
                }
            }
        }
        return result;
    }


    private CrxResponse saveChallengeAnswer(CrxQuestionAnswer questionAnswer, Boolean answer){
        // First we search if an answer was already given. If so we will update this.
        for(CrxChallengeAnswer challengeAnswer: questionAnswer.getChallengeAnswers() ){
            if(challengeAnswer.getCreator().equals(this.session.getUser())) {
                this.em.getTransaction().begin();
                challengeAnswer.setCorrect(answer);
                this.em.merge(challengeAnswer);
                this.em.getTransaction().commit();
                return new CrxResponse(this.getSession(),"OK", "Answer was saved correct.");
            }
        }
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

    /**
     * Save challenge answers crx response.
     *
     * @param crxChallengeId the crx challenge id
     * @param answers        the answers
     * @return the crx response
     */
    public CrxResponse saveChallengeAnswers(Long crxChallengeId, Map<Long,Boolean> answers){
        CrxChallenge challenge = this.getById(crxChallengeId);
        if(!this.isAvailable(challenge)) {
            return new CrxResponse(this.getSession(), "ERROR", "This challenge is not available now.");
        }
        for( Long answerId: answers.keySet()){
            CrxQuestionAnswer questionAnswer= this.em.find(CrxQuestionAnswer.class, answerId);
            if( !questionAnswer.getCrxQuestion().getChallenge().equals(challenge) ) {
                return new CrxResponse(this.getSession(),"ERROR", "Answers does not belongs to challenge.");
            }
            saveChallengeAnswer(questionAnswer, answers.get(answerId));
        }
        return new CrxResponse(this.getSession(),"OK", "Answers were saved correct.");
    }

    /**
     * Gets my results.
     *
     * @param id the id
     * @return the my results
     */
    public Object getMyResults(Long id) {
        CrxChallenge challenge = this.getById(id);
        if (challenge == null) {
            return new CrxResponse(this.getSession(), "ERROR", "Could not find the challenge.");
        }
        if(!this.isAvailable(challenge)) {
            return new CrxResponse(this.getSession(), "ERROR", "This challenge is not available now.");
        }
        Map<Long, Boolean> results = new HashMap<>();
        for (CrxQuestion question : challenge.getQuestions()) {
            for (CrxQuestionAnswer answer : question.getCrxQuestionAnswers()) {
                for (CrxChallengeAnswer challengeAnswer : answer.getChallengeAnswers()) {
                    logger.debug("challengeAnswer" + challengeAnswer.getCreator());
                    if( challengeAnswer.getCreator().equals(this.session.getUser()) ) {
                        results.put(
                                answer.getId(), challengeAnswer.getCorrect()
                        );
                    }
                }
            }
        }
        return results;
    }

    private CrxResponse isModifiable(Long challengeId){
        CrxChallenge challenge = this.getById(challengeId);
        if( challenge == null ) {
            return new CrxResponse(this.getSession(), "ERROR", "Could not find the challenge.");
        }
        if( this.isAvailable(challenge)) {
            return new CrxResponse(this.getSession(), "ERROR", "The challenge is now available. You must not change it.");
        }
        if( !this.session.getUser().equals(challenge.getCreator())) {
            return new CrxResponse(this.getSession(), "ERROR", "Only the owner may evaluate a challenge.");
        }
        return null;
    }

    private Boolean isAvailable(CrxChallenge challenge){
        Date now = new Date();
        return (challenge.getValidFrom().before(now) && challenge.getValidUntil().after(now));
    }
}
