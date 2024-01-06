package de.cranix.services;

import de.cranix.dao.*;
import de.cranix.helper.CrxEntityManagerFactory;
import de.cranix.helper.CrxSystemCmd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.json.JSONObject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.ws.rs.WebApplicationException;
import java.util.List;

public class Crx2faService extends Service{
    Logger logger = LoggerFactory.getLogger(Crx2faService.class);
    Integer timeStep;

    String crx2faUrl = "https://admin.cephalix.eu/api/customers/crx2fa/token";
    String crx2faCheck = "https://crx2fa.cepahlix.eu/validate/check?serial=%s&otponly=1&pass=%s";
    public Crx2faService(Session session, EntityManager em)
    {
        super(session, em);
        try {
            this.timeStep = Integer.valueOf(this.getConfigValue("2FA_TIME_STEP"));
        } catch (NumberFormatException e) {
            this.timeStep = 30;
        }
    }

    public CrxResponse add(Crx2fa crx2fa){
        User user = this.em.find(User.class, this.session.getUser().getId());
        try {
            Crx2faRequest request = new Crx2faRequest();
            request.setRegCode(this.getConfigValue("REG_CODE"));
            request.setUid(user.getUid());
            request.setUserId(user.getId());
            request.setTimeStep(crx2fa.getTimeStep());
            this.logger.debug("request:" + request);
            JSONObject response = sendRequest(request, "POST");
            this.logger.debug("response:" + response);
            crx2fa.setCrx2faAddress(response.getString("qrcode"));
            crx2fa.setCreator(user);
            crx2fa.setSerial(response.getString("serial"));
            this.em.getTransaction().begin();
            this.em.persist(crx2fa);
            user.addCrx2fa(crx2fa);
            this.em.merge(user);
            this.em.getTransaction().commit();
            this.session.setUser(user);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new CrxResponse(this.getSession(), "ERROR", e.getMessage());
        }
        return new CrxResponse(this.getSession(), "OK", "CRANIX 2FA was created successfully.");
    }

    public CrxResponse delete(Long id){
        User user = this.em.find(User.class, this.session.getUser().getId());
        Crx2fa crx2fa = this.em.find(Crx2fa.class,id);
        if( crx2fa == null) {
            return new CrxResponse(this.getSession(), "ERROR", "Can not find the CRANIX CFA");
        }
        Crx2faRequest request = new Crx2faRequest();
        request.setRegCode(this.getConfigValue("REG_CODE"));
        request.setSerial(crx2fa.getSerial());
        try {
            sendRequest(request, "DELETE");
            this.em.getTransaction().begin();
            this.em.remove(crx2fa);
            this.em.getTransaction().commit();
            this.em.refresh(user);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new CrxResponse(this.getSession(), "ERROR", e.getMessage());
        }
        return new CrxResponse(this.getSession(), "OK", "CRANIX 2FA was deleted successfully.");
    }

    public CrxResponse modify(Crx2fa crx2fa){
        User user = this.em.find(User.class, this.session.getUser().getId());
        Crx2fa oldCrx2fa = this.em.find(Crx2fa.class, crx2fa.getId());
        if( oldCrx2fa == null) {
            return new CrxResponse(this.getSession(), "ERROR", "Can not find the CRANIX 2fa");
        }
        if( !oldCrx2fa.getCreator().equals(user)) {
            return new CrxResponse(this.getSession(), "ERROR", "You ar not the owner of this crx 2fa");
        }
        try {
            oldCrx2fa.setValidHours(crx2fa.getValidHours());
            if(!oldCrx2fa.getCrx2faType().equals("TOTP")) {
                oldCrx2fa.setTimeStep(crx2fa.getTimeStep());
            }
            this.em.getTransaction().begin();
            this.em.merge(oldCrx2fa);
            this.em.getTransaction().commit();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new CrxResponse(this.getSession(), "ERROR", e.getMessage());
        }
        return new CrxResponse(this.getSession(), "OK", "CRANIX 2FA was modified successfully.");
    }

    public CrxResponse reset(Long id) {
        Crx2fa crx2fa = this.em.find(Crx2fa.class,id);
        if( crx2fa == null) {
            return new CrxResponse(this.getSession(), "ERROR", "Can not find the CRANIX CFA");
        }
        Crx2faRequest request = new Crx2faRequest();
        request.setRegCode(this.getConfigValue("REG_CODE"));
        request.setSerial(crx2fa.getSerial());
        sendRequest(request, "PATCH");
        return new CrxResponse(this.getSession(), "OK", "CRANIX 2FA was deleted successfully.");
    }

    public CrxResponse sendPin(Long crx2faId, String ip, String token) {
        Crx2fa crx2fa = this.em.find(Crx2fa.class, crx2faId);
        SessionService sessionService = new SessionService(this.em);
        Session session1 = sessionService.getByToken(token);
        if(!session1.getIp().equals(ip)) {
            return new CrxResponse(session,"ERROR","A jó büdös életbe!!");
        }
        switch (crx2fa.getCrx2faType()) {
            case "SMS":
                //TODO
                break;
            case "MAIL":
                //TODO
                break;
            default:
                return new CrxResponse(session,"ERROR","Unsupported 2FA type");
        }
        return new CrxResponse(session,"OK","PIN was send for your address.");
    }
    /**
     * Checks the OTP pin.
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
        if(!session1.getIp().equals(ip)) {
            return null;
        }
        User user = em.find(User.class, session1.getUserId());
        switch (crx2fa.getCrx2faType()) {
            case "TOTP":
                String serial = crx2fa.getSerial();
                String[] program = new String[14];
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
                JSONObject resp = new JSONObject(reply.toString());
                Boolean value = ((JSONObject) resp.get("result")).getBoolean("value");
                crx2faSession = new Crx2faSession(user,crx2fa,ip);
                if (!value) {
                    return null;
                }
                break;
            case "SMS":
            case "MAIL":
                for(Crx2faSession tmp: user.getCrx2faSessions()) {
                    if(tmp.getClientIP().equals(ip) && tmp.getPin().equals(pin)){
                        crx2faSession = tmp;
                        break;
                    }
                }
                break;
        }
        if( crx2faSession != null ) {
            this.em.getTransaction().begin();
            this.em.persist(crx2faSession);
            this.em.merge(user);
            this.em.getTransaction().commit();
            return crx2faSession;
        }
        return null;
    }

    private JSONObject sendRequest(Crx2faRequest object, String  request){
        String[] program = new String[11];
        StringBuffer reply = new StringBuffer();
        StringBuffer stderr = new StringBuffer();
        program[0] = "/usr/bin/curl";
        program[1] = "-s";
        program[2] = "-X";
        program[3] = request;
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
            for(Crx2faSession crx2faSession: (List<Crx2faSession>) query.getResultList()){
                if(!crx2faSession.isValid()){
                    User user = crx2faSession.getCreator();
                    this.em.getTransaction().begin();
                    em.remove(crx2faSession);
                    this.em.getTransaction().commit();
                    em.refresh(user);
                }
            }
        } catch (Exception e) {
            logger.error("getByRole: " + e.getMessage());
        }
    }
}
