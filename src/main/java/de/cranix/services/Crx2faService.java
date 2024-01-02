package de.cranix.services;

import de.cranix.dao.*;
import de.cranix.helper.CrxEntityManagerFactory;
import de.cranix.helper.CrxSystemCmd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.json.JSONObject;
import javax.persistence.EntityManager;
import javax.ws.rs.WebApplicationException;

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
            request.setTimeStep(this.timeStep);
            Crx2faRequest response = sendRequest(request, "POST");
            crx2fa.setCrx2faAddress(response.getQrCode());
            crx2fa.setCreator(user);
            crx2fa.setSerial(response.getSerial());
            this.em.getTransaction().begin();
            this.em.persist(crx2fa);
            user.addCrx2fas(crx2fa);
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

    public Crx2faSession createSession(String remoteIp){
        Crx2fa crx2fa = this.session.getUser().getCrx2fa();
        Crx2faSession crx2faSession = new Crx2faSession(crx2fa, remoteIp);
        try {
            this.em.getTransaction().begin();
            this.em.merge(crx2faSession);
            this.em.getTransaction().commit();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new Crx2faSession();
        }
        return  crx2faSession;
    }

    public Crx2faSession checkPin(String ip, String pin, String token) {
        SessionService sessionService = new SessionService(this.em);
        Session session1 = sessionService.getByToken(token);
        if(!session1.getIp().equals(ip)) {
            throw new WebApplicationException(402);
        }
        //TODO now we only provide one OTP method
        User user = em.find(User.class, session1.getUserId());
        Crx2fa crx2fa = user.getCrx2fas().get(0);
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
        program[6] = String.format(crx2faCheck,serial,pin);
        CrxSystemCmd.exec(program, reply, stderr, null);
        JSONObject resp = new JSONObject(reply.toString());
        Boolean value = ((JSONObject) resp.get("result")).getBoolean("value");
        if( !value ) {
            throw new WebApplicationException(401);
        }
        Crx2faSession crx2faSession = new Crx2faSession(user,crx2fa,ip);
        this.em.getTransaction().begin();
        this.em.persist(crx2faSession);
        this.em.merge(user);
        this.em.getTransaction().commit();
        return  crx2faSession;
    }
    public Crx2faSession checkSession(Crx2faSession crx2faSession, String remoteIp){

    }


    private String send2fa(Crx2faSession crx2faSession) {

    }

    private Crx2faRequest sendRequest(Crx2faRequest object, String  request){
        String[] program = new String[14];
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
        program[8] = "--header";
        program[9] = "-d";
        program[10] = object.toString();
        program[11] = crx2faUrl;
        CrxSystemCmd.exec(program, reply, stderr, null);
        return new JSONObject(reply.toString());
    }
}
