package de.cranix.services;

import de.cranix.dao.*;
import de.cranix.helper.CrxSystemCmd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.json.JSONObject;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static de.cranix.helper.CranixConstants.cranix2faConfig;

public class Crx2faService extends Service {
    Logger logger = LoggerFactory.getLogger(Crx2faService.class);
    private static String crx2faUrl;
    private static String crx2faCheck;

    public Crx2faService(Session session, EntityManager em) {
        super(session, em);
        if( crx2faUrl == null ) {
            Config config = new Config(cranix2faConfig, "CRX2FA_");
            this.crx2faUrl = config.getConfigValue("URL");
            this.crx2faCheck = config.getConfigValue("CHECK_URL");
        }
    }

    public List<Crx2fa> geAll(){
        Query query = this.em.createNamedQuery("Crx2fa.findAll");
        return query.getResultList();
    }

    public CrxResponse add(Crx2fa crx2fa) {
        User user = this.em.find(User.class, this.session.getUser().getId());
        crx2fa.setCreated(new Date(System.currentTimeMillis()));
        try {
            if(crx2fa.getCrx2faType().equals("TOTP")) {
                Crx2faRequest crx2faRequest = new Crx2faRequest();
                crx2faRequest.setRegCode(this.getConfigValue("REG_CODE"));
                crx2faRequest.setUid(user.getUid());
                crx2faRequest.setUserId(user.getId());
                crx2faRequest.setTimeStep(crx2fa.getTimeStep());
                crx2faRequest.setType("TOTP");
                crx2faRequest.setAction("CREATE");
                this.logger.debug("request:" + crx2faRequest);
                JSONObject response = sendRequest(crx2faRequest);
                this.logger.debug("response:" + response);
                crx2fa.setCrx2faAddress(response.getString("qrcode"));
                crx2fa.setSerial(response.getString("serial"));
            } else {
                crx2fa.setSerial("");
            }
            crx2fa.setCreator(user);
            this.em.getTransaction().begin();
            this.em.persist(crx2fa);
            user.addCrx2fa(crx2fa);
            this.em.merge(user);
            this.em.getTransaction().commit();
            this.session.setUser(user);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new CrxResponse("ERROR", e.getMessage());
        }
        return new CrxResponse("OK", "CRANIX 2FA was created successfully.");
    }

    public CrxResponse delete(Long id) {
        Crx2fa crx2fa = this.em.find(Crx2fa.class, id);
        //TODO check if it is allowed
        User user = crx2fa.getCreator();
        if (crx2fa == null) {
            return new CrxResponse("ERROR", "Can not find the CRANIX CFA");
        }
        if(this.session.getCrx2faSession()!=null && this.session.getCrx2faSession().getMyCrx2fa().equals(crx2fa)) {
            return new CrxResponse("ERROR", "You must not remove this CFA. You are just using it.");
        }
        if( crx2fa.getCrx2faType().equals("TOTP")) {
            Crx2faRequest crx2faRequest = new Crx2faRequest();
            crx2faRequest.setRegCode(this.getConfigValue("REG_CODE"));
            crx2faRequest.setSerial(crx2fa.getSerial());
            crx2faRequest.setAction("DELETE");
            sendRequest(crx2faRequest);
        }
        try {
            user.getCrx2fas().remove(crx2fa);
            this.em.getTransaction().begin();
            this.em.remove(crx2fa);
            this.em.merge(user);
            this.em.getTransaction().commit();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new CrxResponse("ERROR", e.getMessage());
        }
        return new CrxResponse("OK", "CRANIX 2FA was deleted successfully.");
    }

    public CrxResponse modify(Crx2fa crx2fa) {
        Crx2fa oldCrx2fa = this.em.find(Crx2fa.class, crx2fa.getId());
        //TODO check if it is allowed
        User user = oldCrx2fa.getCreator();
        if (oldCrx2fa == null) {
            return new CrxResponse("ERROR", "Can not find the CRANIX 2fa");
        }
        if (!oldCrx2fa.getCreator().equals(user)) {
            return new CrxResponse("ERROR", "You ar not the owner of this crx 2fa");
        }
        try {
            oldCrx2fa.setValidHours(crx2fa.getValidHours());
            if (!oldCrx2fa.getCrx2faType().equals("TOTP")) {
                oldCrx2fa.setTimeStep(crx2fa.getTimeStep());
                oldCrx2fa.setCrx2faAddress(crx2fa.getCrx2faAddress());
            }
            this.em.getTransaction().begin();
            this.em.merge(oldCrx2fa);
            this.em.getTransaction().commit();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new CrxResponse("ERROR", e.getMessage());
        }
        return new CrxResponse("OK", "CRANIX 2FA was modified successfully.");
    }

    public CrxResponse reset(Long id) {
        Crx2fa crx2fa = this.em.find(Crx2fa.class, id);
        if (crx2fa == null) {
            return new CrxResponse("ERROR", "Can not find the CRANIX CFA");
        }
        Crx2faRequest crx2faRequest = new Crx2faRequest();
        crx2faRequest.setRegCode(this.getConfigValue("REG_CODE"));
        crx2faRequest.setSerial(crx2fa.getSerial());
        crx2faRequest.setAction("RESET");
        sendRequest(crx2faRequest);
        return new CrxResponse("OK", "CRANIX 2FA was reseted successfully.");
    }

    public CrxResponse sendPin(Long crx2faId, String ip, String token) {
        logger.debug("sendPin" + crx2faId + ":" + ip + ":" + token);
        Crx2fa crx2fa = this.em.find(Crx2fa.class, crx2faId);
        SessionService sessionService = new SessionService(this.em);
        Session session1 = sessionService.getByToken(token);
        if (!session1.getIp().equals(ip)) {
            return new CrxResponse("ERROR", "A jó büdös életbe!!");
        }
        User user = em.find(User.class, session1.getUserId());
        Crx2faSession crx2faSession = new Crx2faSession(user, crx2fa, ip);
        Crx2faRequest crx2faRequest = new Crx2faRequest();
        crx2faRequest.setRegCode(this.getConfigValue("REG_CODE"));
        crx2faRequest.setType(crx2fa.getCrx2faType());
        crx2faRequest.setAddress(crx2fa.getCrx2faAddress());
        crx2faRequest.setSerial(crx2faSession.getPin());
        crx2faRequest.setUid(session1.getUser().getUid());
        crx2faRequest.setAction("SEND");
        //TODO check response
        sendRequest(crx2faRequest);
        this.em.getTransaction().begin();
        this.em.persist(crx2faSession);
        this.em.getTransaction().commit();
        this.em.refresh(user);
        return new CrxResponse("OK", "PIN was sent for your address.");
    }

    /**
     * Checks the OTP pin.
     *
     * @param ip
     * @param pin
     * @param token
     * @return a Crx2faSession if the OTP is ok. If not throws a web error.
     */
    public Crx2faSession checkPin(Long crx2faId, String ip, String pin, String token) {
        Crx2faSession crx2faSession = null;
        Crx2fa crx2fa = this.em.find(Crx2fa.class, crx2faId);
        SessionService sessionService = new SessionService(this.em);
        Session session1 = sessionService.getByToken(token);
        // The request for check pin is not coming from the same ip as the session token.
        if (!session1.getIp().equals(ip)) {
            return null;
        }
        logger.debug("checkPin:" + session1);
        User user = em.find(User.class, session1.getUserId());
        logger.debug("checkPin:" + user);
        switch (crx2fa.getCrx2faType()) {
            case "TOTP":
                String serial = crx2fa.getSerial();
                String[] program = new String[7];
                StringBuffer reply = new StringBuffer();
                StringBuffer stderr = new StringBuffer();
                program[0] = "/usr/bin/curl";
                program[1] = "-s";
                program[2] = "-X";
                program[3] = "GET";
                program[4] = "--header";
                program[5] = "Accept: application/json";
                program[6] = String.format(crx2faCheck, serial, pin);
                CrxSystemCmd.exec(program, reply, stderr, null);
                logger.debug("Ceck TOTP:" + String.format(crx2faCheck, serial, pin));
                logger.debug(reply.toString());
                JSONObject result = new JSONObject(reply.toString()).getJSONObject("result");
                logger.debug(result.toString());
                if (!result.getBoolean("value")) {
                    return null;
                }
                crx2faSession = new Crx2faSession(user, crx2fa, ip);
                this.em.getTransaction().begin();
                this.em.persist(crx2faSession);
                this.em.getTransaction().commit();
                break;
            case "SMS":
            case "MAIL":
                for (Crx2faSession tmp : user.getCrx2faSessions()) {
                    logger.debug("checkPin SMS/MAIL:" + tmp);
                    /*
                    logger.debug(" crx2fa " + tmp.getMyCrx2fa().equals(crx2fa));
                    logger.debug(" isAvailable " + tmp.isAvailable());
                    logger.debug(" ip " + tmp.getClientIP().equals(ip));
                    logger.debug(" valid " + tmp.isValid());
                    logger.debug(" pinOk " + tmp.getPin().equals(pin));*/
                    //tmp.isAvailable() &&
                    if (
                            tmp.getMyCrx2fa().equals(crx2fa) &&
                                    tmp.getPin() != null &&
                                    tmp.getClientIP().equals(ip) &&
                                    tmp.isValid() &&
                                    tmp.getPin().equals(pin)
                    ) {
                        crx2faSession = tmp;
                        crx2faSession.setChecked(true);
                        this.em.getTransaction().begin();
                        this.em.merge(crx2faSession);
                        this.em.getTransaction().commit();
                    }
                }
                break;
        }
        if (crx2faSession != null) {
            session1.setCrx2faSession(crx2faSession);
            sessionService.sessions.put(token, session1);
            this.em.getTransaction().begin();
            this.em.merge(session1);
            this.em.refresh(user);
            this.em.getTransaction().commit();
        }
        return crx2faSession;
    }

    private JSONObject sendRequest(Crx2faRequest object) {
        String[] program = new String[11];
        StringBuffer reply = new StringBuffer();
        StringBuffer stderr = new StringBuffer();
        program[0] = "/usr/bin/curl";
        program[1] = "-s";
        program[2] = "-X";
        program[3] = "POST";
        program[4] = "--header";
        program[5] = "Content-Type: application/json";
        program[6] = "--header";
        program[7] = "Accept: application/json";
        program[8] = "-d";
        program[9] = object.toString();
        program[10] = crx2faUrl;
        CrxSystemCmd.exec(program, reply, stderr, null);
        return new JSONObject(reply.toString());
    }

    public void cleanUp() {
        try {
            Query query = this.em.createNamedQuery("Crx2faSessions.findAll");
            for (Crx2faSession crx2faSession : (List<Crx2faSession>) query.getResultList()) {
                if (!crx2faSession.isValid()) {
                    User user = crx2faSession.getCreator();
                    user.getCrx2faSessions().remove(crx2faSession);
                    Session session1 = crx2faSession.getSession();
                    session1.setCrx2faSession(null);
                    this.em.getTransaction().begin();
                    em.merge(session1);
                    em.merge(user);
                    em.remove(crx2faSession);
                    this.em.getTransaction().commit();
                    em.refresh(user);
                }
            }
        } catch (Exception e) {
            logger.error("getByRole: " + e.getMessage());
        }
    }

    public List<CrxResponse> applyAction(CrxActionMap actionMap) {
        List<CrxResponse> responses = new ArrayList<>();
        for(Long id: actionMap.getObjectIds()){
            switch (actionMap.getName().toUpperCase()){
                case "RESET":
                    responses.add(this.reset(id));
                    break;
                case "DELETE":
                    responses.add(this.delete(id));
            }
        }
        return responses;
    }

    public List<Crx2fa> getMyCrx2fas() {
        User user = this.em.find(User.class,this.session.getUser().getId());
        return user.getCrx2fas();
    }
}
