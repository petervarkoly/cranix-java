package de.cranix.services;

import de.cranix.dao.*;
import de.cranix.helper.CrxSystemCmd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.cranix.helper.CranixConstants.*;

/**
 * The type Challenge service.
 */
public class ChallengeService extends Service {


    private static final Path customChallengeStylePath = Paths.get(cranixBaseDir + "templates/customChallengeStyle.html");
    private static final Path challengeStylePath = Paths.get(cranixBaseDir + "templates/challengeStyle.html");
    private static
    String challengeTableStyle =
            "<html>\n" +
                    "  <style>\n" +
                    "  \n" +
                    "  .thVertical {\n" +
                    "    writing-mode: vertical-lr;\n" +
                    "    min-width: 50px;\n" +
                    "    text-align:center;\n" +
                    "  }\n" +
                    "  \n" +
                    "  table, th, td {\n" +
                    "    border: 1px solid black;\n" +
                    "    border-collapse: collapse;\n" +
                    "  }\n" +
                    "  .questionLine {\n" +
                    "    background-color: gray;\n" +
                    "  }\n" +
                    "  .sumLine {\n" +
                    "    background-color: green;\n" +
                    "  }\n" +
                    "  .resultLine {\n" +
                    "    background-color: lightgray;\n" +
                    "  }\n" +
                    "  .answerLine {\n" +
                    "    background-color: yellow;\n" +
                    "  }\n" +
                    "  </style>\n";
    /**
     * The Logger.
     */
    Logger logger = LoggerFactory.getLogger(ChallengeService.class);

    /**
     * Instantiates a new Challenge service.
     *
     * @param session the session
     * @param em      the EntityManager
     */
    public ChallengeService(Session session, EntityManager em) {
        super(session, em);
        try {
            challengeTableStyle = Files.readString(customChallengeStylePath);
            try {
                challengeTableStyle = Files.readString(challengeStylePath);
            } catch (Exception e) {
                logger.debug("ChallengeService no customChallengeTableStyle");
            }
        } catch (Exception e) {
            logger.error("ChallengeService no challengeTableStyle");
        }
    }

    public static StringBuilder getArhivePath(Long channelId) {
        return new StringBuilder(cranixAdm).append("challenges/").append(channelId.toString());
    }

    /**
     * Add crx response.
     *
     * @param challenge the challenge
     * @return the crx response
     */
    public CrxResponse add(CrxChallenge challenge) {
        logger.debug("add:" + challenge);
	if( challenge.getTeachingSubject() == null ){
           return new CrxResponse(this.session, "ERROR", "You have to define the subject the challenge belongs to.");
	}
        this.em.getTransaction().begin();
        this.adaptSubject(challenge);
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
    public CrxResponse modify(CrxChallenge challenge) {
        CrxResponse resp = this.isModifiable(challenge.getId());
        if (resp != null) {
            return resp;
        }
        this.em.getTransaction().begin();
        this.adaptSubject(challenge);
        challenge.setCreator(this.session.getUser());
        this.em.merge(challenge);
        this.adapt(challenge);
        this.em.getTransaction().commit();
        return new CrxResponse(this.session, "OK", "Challenge was successfully modified.", challenge.getId());
    }

    public CrxResponse assignAndStart(CrxChallenge challenge) {
        CrxResponse resp = this.isModifiable(challenge.getId());
        if (resp != null) {
            return resp;
        }
        this.em.getTransaction().begin();
        challenge.setCreator(this.session.getUser());
        this.assign(challenge);
        challenge.setReleased(true);
        this.em.merge(challenge);
        this.adapt(challenge);
        this.em.getTransaction().commit();
        return new CrxResponse(this.session, "OK", "Challenge was successfully assigned and stated.");
    }

    private void adaptSubject(CrxChallenge challenge) {
        if(challenge.getId() != null){
            CrxChallenge oldChallenge = this.getById(challenge.getId());
            if( oldChallenge != null) {
                TeachingSubject teachingSubject = oldChallenge.getTeachingSubject();
                if(!teachingSubject.equals(challenge.getTeachingSubject())) {
                teachingSubject.getCrxChallenges().remove(oldChallenge);
                this.em.merge(teachingSubject);
                }
            }
        }
        TeachingSubject teachingSubject = this.em.find(TeachingSubject.class,challenge.getTeachingSubject().getId());
        if( teachingSubject != null && !teachingSubject.getCrxChallenges().contains(challenge) ) {
            teachingSubject.getCrxChallenges().add(challenge);
            this.em.merge(teachingSubject);
        }
    }
    /*
     * Assign challenge to the groups and users.
     */
    private void assign(CrxChallenge challenge) {
        for (User u : challenge.getUsers()) {
            User user = this.em.find(User.class, u.getId());
            if (!user.getTodos().contains(challenge)) {
                user.getTodos().add(challenge);
                this.em.merge(user);
            }
        }
        for (Group g : challenge.getGroups()) {
            Group group = this.em.find(Group.class, g.getId());
            if (!group.getTodos().contains(challenge)) {
                group.getTodos().add(challenge);
                this.em.merge(group);
            }
        }
    }

    private void deAssign(CrxChallenge challenge) {
        challenge.setReleased(false);
        for (User u : challenge.getUsers()) {
            User user = this.em.find(User.class, u.getId());
            if (user.getTodos().contains(challenge)) {
                user.getTodos().remove(challenge);
                this.em.merge(user);
            }
        }
        for (Group g : challenge.getGroups()) {
            Group group = this.em.find(Group.class, g.getId());
            if (group.getTodos().contains(challenge)) {
                group.getTodos().remove(challenge);
                this.em.merge(group);
            }
        }
        challenge.setGroups(new ArrayList<Group>());
        challenge.setUsers(new ArrayList<User>());
        this.em.merge(challenge);
    }

    private void adapt(CrxChallenge challenge) {
        for (CrxQuestion crxQuestion : challenge.getQuestions()) {
            crxQuestion.setCreator(this.session.getUser());
            crxQuestion.setChallenge(challenge);
            this.em.merge(crxQuestion);
            for (CrxQuestionAnswer answer : crxQuestion.getCrxQuestionAnswers()) {
                answer.setCreator(this.session.getUser());
                answer.setCrxQuestion(crxQuestion);
                this.em.merge(answer);
            }
        }
    }

    public List<CrxChallenge> getAll() {
        try {
            Query query = this.em.createNamedQuery("Challenge.findAll");
            return (List<CrxChallenge>) query.getResultList();
        } catch (Exception e) {
            logger.error("getAll: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<CrxQuestion> getAllQuestion() {
        try {
            Query query = this.em.createNamedQuery("Question.findAll");
            return (List<CrxQuestion>) query.getResultList();
        } catch (Exception e) {
            logger.error("getAllQuestion: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<CrxQuestion> getQuestionsOfSubject(Long subjectId) {
        try {
            TeachingSubject teachingSubject = this.em.find(TeachingSubject.class,subjectId);
            ArrayList<CrxQuestion> questions = new ArrayList<>();
            for(CrxChallenge challenge: teachingSubject.getCrxChallenges()) {
                questions.addAll(challenge.getQuestions());
            }
            return questions;
        } catch (Exception e) {
            logger.error("getAllQuestion: " + e.getMessage());
            return new ArrayList<>();
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
        if (resp != null) {
            return resp;
        }
        CrxChallenge challenge = this.getById(challengeId);
        CrxQuestion question;
        try {
            question = this.em.find(CrxQuestion.class, questionId);
        } catch (Exception e) {
            return new CrxResponse(this.getSession(), "ERROR", "Can not find question.");
        }
        this.em.getTransaction().begin();
        challenge.getQuestions().remove(question);
        this.em.remove(question);
        this.em.merge(challenge);
        this.em.getTransaction().commit();
        return new CrxResponse(this.getSession(), "OK", "Question was removed.");
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
        if (resp != null) {
            return resp;
        }
        CrxChallenge challenge = this.getById(challengeId);
        CrxQuestion question;
        CrxQuestionAnswer answer;
        try {
            question = this.em.find(CrxQuestion.class, questionId);
            answer = this.em.find(CrxQuestionAnswer.class, answerId);
        } catch (Exception e) {
            return new CrxResponse(this.getSession(), "ERROR", "Can not find challenge.");
        }
        this.em.getTransaction().begin();
        question.getCrxQuestionAnswers().remove(answer);
        this.em.remove(answer);
        this.em.merge(question);
        this.em.merge(challenge);
        this.em.getTransaction().commit();
        return new CrxResponse(this.getSession(), "OK", "Answer was removed.");
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
        if (resp != null) {
            return resp;
        }
        CrxChallenge challenge = this.getById(id);
        String[] program = new String[3];
        program[0] = "rm";
        program[1] = "-rf";
        program[2] = getArhivePath(id).toString();
        StringBuffer reply = new StringBuffer();
        StringBuffer stderr = new StringBuffer();
        CrxSystemCmd.exec(program, reply, stderr, null);
        this.em.getTransaction().begin();
        this.em.remove(challenge);
        this.em.getTransaction().commit();
        return new CrxResponse(this.getSession(), "OK", "Challenge was removed successfully.");
    }

    /**
     * Evaluate a challenge. Gets the actual results.
     *
     * @param id the id
     * @return the object
     */
    private Object evaluate(Long id) {
        CrxChallenge challenge = this.getById(id);
        Map<Long, Map<Long, Integer>> results = new HashMap<>();
        for (CrxQuestion question : challenge.getQuestions()) {
            Map<Long, Integer> result = new HashMap<>();
            logger.debug("question" + question);
            for (CrxQuestionAnswer answer : question.getCrxQuestionAnswers()) {
                for (CrxChallengeAnswer challengeAnswer : answer.getChallengeAnswers()) {
                    Long creatorId = challengeAnswer.getCreator().getId();
                    if (!result.containsKey(creatorId)) {
                        result.put(creatorId, 0);
                    }
                    if (question.getAnswerType().equals(CrxQuestion.ANSWER_TYPE.One)) {
                        if (challengeAnswer.getCorrect() && answer.getCorrect()) {
                            result.put(creatorId, question.getValue());
                        }
                    }
                    if (question.getAnswerType().equals(CrxQuestion.ANSWER_TYPE.Multiple)) {
                        Integer actualValue = result.get(creatorId);
                        if (Boolean.compare(challengeAnswer.getCorrect(), answer.getCorrect()) == 0) {
                            result.put(creatorId, actualValue + question.getValue());
                        }
                    }
                }
            }
            for (Long uid : result.keySet()) {
                if (!results.containsKey(uid)) {
                    results.put(uid, new HashMap<Long, Integer>());
                }
                results.get(uid).put(question.getId(), result.get(uid));
            }
        }
        logger.debug("results: " + results);
        for (Long uid : results.keySet()) {
            results.get(uid).put(0L, 0);
            for (Long questionId : results.get(uid).keySet()) {
                if (questionId == 0L) {
                    continue;
                }
                logger.debug("res:" + uid + ":" + questionId);
                Integer res = results.get(uid).get(questionId) + results.get(uid).get(0L);
                results.get(uid).put(0L, res);
            }
        }
        return results;
    }

    /**
     * Evaluate a challenge an delivers the result as html table.
     *
     * @param id the id
     * @return the string
     */
    public String evaluateAsHtml(Long id) {
        CrxResponse resp = this.isManagable(id);
        if (resp != null) {
            return resp.getValue();
        }
        UserService userService = new UserService(this.session, this.em);
        CrxChallenge challenge = this.getById(id);
        StringBuilder htmlResult = new StringBuilder();
        htmlResult.append(challengeTableStyle);
        htmlResult.append("<table>\n");
        htmlResult.append("  <tr>\n");
        htmlResult.append("    <th>Question</th>\n");
        Map<Long, Map<Long, Integer>> results = (Map<Long, Map<Long, Integer>>) this.evaluate(id);
        for (Long userId : results.keySet()) {
            htmlResult.append("    <th  class=\"thVertical\">").append(
                    userService.getById(userId).getFullName()
            ).append("</th>\n");
        }
        htmlResult.append("  </tr>\n");
        for (CrxQuestion question : challenge.getQuestions()) {
            htmlResult.append("  <tr>\n");
            htmlResult.append("    <td>").append(question.getQuestion()).append("</td>\n");
            for (Long userId : results.keySet()) {
                htmlResult.append("    <td>").append(
                        results.get(userId).get(question.getId())
                ).append("</td>\n");
            }
            htmlResult.append("  </tr>\n");
        }
        htmlResult.append("  <tr>\n");
        htmlResult.append("    <td>").append("Sum:").append("</td>\n");
        for (Long userId : results.keySet()) {
            htmlResult.append("    <td>").append(
                    results.get(userId).get(0L)
            ).append("</td>\n");
        }
        htmlResult.append("  </tr>\n");
        htmlResult.append("</table>\n</html>");
        return htmlResult.toString();
    }

    /**
     * Archive the results of a challenge and clean up in the database.
     *
     * @param id the id
     * @return the object
     */
    public Object stopAndArchive(Long id) {
        CrxResponse resp = this.isManagable(id);
        if (resp != null) {
            return resp;
        }
        CrxChallenge challenge = this.getById(id);
        this.em.getTransaction().begin();
        List<User> testUsers = challenge.getTestUsers();
        Map<Long, Map<Long, Integer>> results = (Map<Long, Map<Long, Integer>>) this.evaluate(id);
        Map<Integer, Long> placeToId = new HashMap<Integer, Long>();
        Map<Long, Integer> idToPlace = new HashMap<Long, Integer>();
        List<String> line = new ArrayList<String>();
        List<List<String>> resultTable = new ArrayList<>();
        Integer i = 2;
        line.add("Question/Answer Text");
        line.add("Correct answer");
        for (User user : testUsers) {
            idToPlace.put(user.getId(), i);
            placeToId.put(i, user.getId());
            i++;
            line.add(user.getFullName() + " " + user.getBirthDay() + " " + user.getClasses());
        }
        resultTable.add(line);
        Integer resultValue = 0;
        for (CrxQuestion question : challenge.getQuestions()) {
            line = new ArrayList<String>();
            line.add(question.getQuestion());
            Integer questionValue = 0;
            if (question.getAnswerType().equals(CrxQuestion.ANSWER_TYPE.One)) {
                questionValue = question.getValue();
            } else if (question.getAnswerType().equals(CrxQuestion.ANSWER_TYPE.Multiple)) {
                questionValue = question.getValue() * question.getCrxQuestionAnswers().size();
            }
            line.add(question.getAnswerType().toString());
            resultTable.add(line);
            for (CrxQuestionAnswer answer : question.getCrxQuestionAnswers()) {
                line = new ArrayList<String>();
                for (i = 0; i < testUsers.size() + 2; i++) {
                    line.add("");
                }
                line.set(0, answer.getAnswer());
                line.set(1, answer.getCorrect() ? "Y" : "N");
                for (CrxChallengeAnswer challengeAnswer : answer.getChallengeAnswers()) {
                    logger.debug("challengeAnswer" + challengeAnswer);
                    line.set(
                            idToPlace.get(challengeAnswer.getCreator().getId()),
                            challengeAnswer.getCorrect() ? "Y" : "N"
                    );
                    this.em.remove(challengeAnswer);
                }
                answer.setChallengeAnswers(new ArrayList<>());
                resultTable.add(line);
                this.em.merge(answer);
            }
            line = new ArrayList<String>();
            line.add("Result:");
            line.add(questionValue.toString());
            resultValue += questionValue;
            for (i = 0; i < testUsers.size(); i++) {
                Long userId = placeToId.get(i + 2);
                if (results.containsKey(userId)) {
                    line.add(results.get(userId).get(question.getId()).toString());
                } else {
                    line.add("");
                }
            }
            resultTable.add(line);
        }
        line = new ArrayList<String>();
        line.add("Sum:");
        line.add(resultValue.toString());
        for (i = 0; i < testUsers.size(); i++) {
            Long userId = placeToId.get(i + 2);
            if (results.containsKey(userId)) {
                line.add(results.get(userId).get(0L).toString());
            } else {
                line.add("");
            }
        }
        resultTable.add(line);
        try {
            Object res = this.createArchive(challenge, resultTable);
            this.deAssign(challenge);
            this.em.getTransaction().commit();
            return res;
        } catch (IOException e) {
            this.em.getTransaction().rollback();
            return new CrxResponse(this.session, "ERROR", e.getMessage());
        }
    }

    /*
     * Creates the archive files for the challenge.
     */
    private Object createArchive(CrxChallenge challenge, List<List<String>> resultTable) throws IOException {
        //Create base directory: /var/adm/cranix/challenges/<challengeId>
        String nowString = this.nowString();
        StringBuilder challengeFile = getArhivePath(challenge.getId()).append("/").append(nowString);
        Files.createDirectories(Paths.get(challengeFile.toString()), privatDirAttribute);
        //Save the result table as json: /var/adm/cranix/challenges/<challengeId>/<NOW.STRING>/results.json
        Path challengePath = Paths.get(challengeFile.toString() + "/RESULTS.json");
        Files.write(challengePath, resultTable.toString().getBytes(StandardCharsets.UTF_8));

        //Save the results of all user as html: /var/adm/cranix/challenges/<challengeId>/<NOW.STRING>/results.html
        String res = (String) arrayToHtml(resultTable, 0, nowString);
        challengePath = Paths.get(challengeFile.toString() + "/RESULTS.html");
        Files.write(challengePath, res.getBytes(StandardCharsets.UTF_8));
        for (int i = 2; i < resultTable.get(0).size(); i++) {
            String res1 = (String) arrayToHtml(resultTable, i, nowString);
            String user = resultTable.get(0).get(i).split(" ")[0];
            challengePath = Paths.get(challengeFile.toString() + "/" + user + ".html");
            Files.write(challengePath, res1.getBytes(StandardCharsets.UTF_8));
        }
        return res;
    }

    private Object arrayToHtml(List<List<String>> resultTable, int columnIndex, String nowString) {
        Boolean isAnswer = false;
        StringBuilder htmlResult = new StringBuilder();
        htmlResult.append(challengeTableStyle);
        htmlResult.append("<input type=\"text\" id=\"dateOfArchive\" readonly value=\"").append(nowString).append("\">\n");
        htmlResult.append("<table>\n");
        htmlResult.append("  <tr>\n");
        for (int j = 0; j < resultTable.get(0).size(); j++) {
            if (j < 2 || columnIndex == 0 || j == columnIndex) {
                htmlResult.append("    <th>").append(resultTable.get(0).get(j)).append("</th>\n");
            }
        }
        htmlResult.append("  </tr>\n");
        for (Integer i = 1; i < resultTable.size(); i++) {
            isAnswer = false;
            if (resultTable.get(i).get(1).equals("One") || resultTable.get(i).get(1).equals("Multiple")) {
                htmlResult.append("  <tr class=\"questionLine\">\n");
            } else if (resultTable.get(i).get(0).equals("Sum:")) {
                htmlResult.append("  <tr class=\"sumLine\">\n");
            } else if (resultTable.get(i).get(0).equals("Result:")) {
                htmlResult.append("  <tr class=\"resultLine\">\n");
            } else {
                htmlResult.append("  <tr class=\"answerLine\">\n");
                isAnswer = true;
            }
            for (int j = 0; j < resultTable.get(i).size(); j++) {
                if (j < 2 || columnIndex == 0 || j == columnIndex) {
                    String field = resultTable.get(i).get(j);
                    if (isAnswer && (j > 1)) {
                        if (resultTable.get(i).get(1).equals(field)) {
                            htmlResult.append("    <td class=\"okTd\">").append(field).append("</td>\n");
                        } else {
                            htmlResult.append("    <td class=\"badTd\">").append(field).append("</td>\n");
                        }
                    } else {
                        htmlResult.append("    <td class=\"center\">").append(field).append("</td>\n");
                    }
                }
            }
            htmlResult.append("  </tr>\n");
        }
        htmlResult.append("</table>\n");
        htmlResult.append("</html>\n");
        return htmlResult.toString();
    }

    public List<String> getListOfArchives(Long challengeId) {
        //Create base directory: /var/adm/cranix/challenges/<challengeId>
        StringBuilder challengeFile = getArhivePath(challengeId);
        List<String> archives = new ArrayList<String>();
        for (String file : new File(challengeFile.toString()).list()) {
            archives.add(file);
        }
        return archives;
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
                    if (challengeAnswer.getCreator().getId().equals(creatorId)) {
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
        for (CrxQuestion question : challenge.getQuestions()) {
            for (CrxQuestionAnswer answer : question.getCrxQuestionAnswers()) {
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
    public List<CrxChallenge> getTodos() {
        List<CrxChallenge> result = new ArrayList<CrxChallenge>();
        User user = this.em.find(User.class, this.session.getUser().getId());
        for (CrxChallenge challenge : user.getTodos()) {
            if (challenge.isReleased()) {
                result.add(clearRightResults(challenge));
            }
        }
        for (Group group : user.getGroups()) {
            for (CrxChallenge challenge : group.getTodos()) {
                if (challenge.isReleased()) {
                    result.add(clearRightResults(challenge));
                }
            }
        }
        return result;
    }


    private CrxResponse saveChallengeAnswer(CrxQuestionAnswer questionAnswer, Boolean answer) {
        // First we search if an answer was already given. If so we will update this.
        for (CrxChallengeAnswer challengeAnswer : questionAnswer.getChallengeAnswers()) {
            if (challengeAnswer.getCreator().equals(this.session.getUser())) {
                this.em.getTransaction().begin();
                challengeAnswer.setCorrect(answer);
                this.em.merge(challengeAnswer);
                this.em.getTransaction().commit();
                return new CrxResponse(this.getSession(), "OK", "Answer was saved correct.");
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
        return new CrxResponse(this.getSession(), "OK", "Answer was saved correct.");
    }

    /**
     * Save challenge answers crx response.
     *
     * @param crxChallengeId the crx challenge id
     * @param answers        the answers
     * @return the crx response
     */
    public CrxResponse saveChallengeAnswers(Long crxChallengeId, Map<Long, Boolean> answers) {
        CrxChallenge challenge = this.getById(crxChallengeId);
        if (!challenge.isReleased()) {
            return new CrxResponse(this.getSession(), "ERROR", "This challenge is not available now.");
        }
        for (Long answerId : answers.keySet()) {
            CrxQuestionAnswer questionAnswer = this.em.find(CrxQuestionAnswer.class, answerId);
            if (!questionAnswer.getCrxQuestion().getChallenge().equals(challenge)) {
                logger.debug("saveChallengeAnswers:" + questionAnswer.getCrxQuestion().getChallenge() + answerId);
                return new CrxResponse(this.getSession(), "ERROR", "Answers does not belongs to challenge.");
            }
            saveChallengeAnswer(questionAnswer, answers.get(answerId));
        }
        return new CrxResponse(this.getSession(), "OK", "Answers were saved correct.");
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
        if (!challenge.isReleased()) {
            return new CrxResponse(this.getSession(), "ERROR", "This challenge is not available now.");
        }
        Map<Long, Boolean> results = new HashMap<>();
        for (CrxQuestion question : challenge.getQuestions()) {
            for (CrxQuestionAnswer answer : question.getCrxQuestionAnswers()) {
                for (CrxChallengeAnswer challengeAnswer : answer.getChallengeAnswers()) {
                    logger.debug("challengeAnswer" + challengeAnswer.getCreator());
                    if (challengeAnswer.getCreator().equals(this.session.getUser())) {
                        results.put(
                                answer.getId(), challengeAnswer.getCorrect()
                        );
                    }
                }
            }
        }
        return results;
    }

    private CrxResponse isModifiable(Long challengeId) {
        CrxChallenge challenge = this.getById(challengeId);
        if (challenge == null) {
            return new CrxResponse(this.getSession(), "ERROR", "Could not find the challenge.");
        }
        if (challenge.isReleased()) {
            return new CrxResponse(this.getSession(), "ERROR", "The challenge is now available. You must not change it.");
        }
        if (!this.session.getUser().equals(challenge.getCreator()) && !this.isSuperuser()) {
            return new CrxResponse(this.getSession(), "ERROR", "Only the owner may modify a challenge.");
        }
        return null;
    }


    private CrxResponse isManagable(Long challengeId) {
        CrxChallenge challenge = this.getById(challengeId);
        if (challenge == null) {
            return new CrxResponse(this.getSession(), "ERROR", "Could not find the challenge.");
        }
        logger.debug("isManagable" + this.session.getUser() + challenge.getCreator());
        if (!this.session.getUser().equals(challenge.getCreator()) && !this.isSuperuser()) {
            return new CrxResponse(this.getSession(), "ERROR", "Only the owner may manage a challenge.");
        }
        return null;
    }

    public List<CrxChallenge> getBySubject(Long id) {
        TeachingSubject teachingSubject = this.em.find(TeachingSubject.class, id);
        if( teachingSubject != null ) {
            return teachingSubject.getCrxChallenges();
        }
        return new ArrayList<CrxChallenge>();
    }
}
