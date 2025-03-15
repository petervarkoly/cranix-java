package de.cranix.services;

import de.cranix.dao.CrxResponse;
import de.cranix.dao.IdRequest;
import de.cranix.dao.Session;
import de.cranix.dao.User;
import de.cranix.helper.CrxSystemCmd;
import org.json.JSONObject;

import javax.persistence.EntityManager;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static de.cranix.helper.CranixConstants.*;
import static de.cranix.helper.StaticHelpers.getYear;
import static de.cranix.helper.StaticHelpers.simpleDate;

public class IdRequestService extends Service{

    private static String crxIDUrl;
    private static String crxIDPicturesDir;
    private static String crxIDRequestsDir;
    private static String crxIDValid;
    public IdRequestService(){
        super();
    }
    public IdRequestService(Session session, EntityManager em){
        super(session,em);
        if( crxIDUrl == null ) {
            Config config = new Config(cranixIDConfig, "CRXID_");
            this.crxIDUrl = config.getConfigValue("URL");
            this.crxIDRequestsDir = config.getConfigValue("REQUESTS_DIR");
            this.crxIDPicturesDir = config.getConfigValue("PICTURES_DIR");
            this.crxIDValid = config.getConfigValue("VALID");
        }
    }

    public CrxResponse add(IdRequest idRequest){
        User me = this.em.find(User.class,this.session.getUserId());
        if(idRequest.getPicture() == null || idRequest.getPicture().isEmpty()){
            return new CrxResponse("ERROR","You have to provide a picture.");
        }
        IdRequest oldIdRequest = getMyIdRequest();
        if(oldIdRequest != null) {
            if( oldIdRequest.equals(idRequest)) {
                this.savePicture(idRequest);
                oldIdRequest.setAvatar(idRequest.getAvatar());
                em.getTransaction().begin();
                em.merge(oldIdRequest);
                em.getTransaction().commit();
            } else {
                return new CrxResponse("ERROR","You have already created an ID request. You must not create a new one.");
            }
        }
        IdRequest newIdRequest = new IdRequest(session);
        newIdRequest.setPicture(idRequest.getPicture());
        savePicture(newIdRequest);
        String validUntil = getYear() + "-" + crxIDValid;
        try {
            newIdRequest.setValidUntil(simpleDate.parse(validUntil));
            me.setCreatedRequests(List.of(newIdRequest));
            em.getTransaction().begin();
            em.persist(me);
            em.getTransaction().commit();
        } catch (ParseException e) {
            return new CrxResponse("ERROR","Could not create id request:" + e.getMessage());
        } catch (Exception e){
            return new CrxResponse("ERROR","Could not create id request:" + e.getMessage());
        }
        return new CrxResponse("OK","Id request was created successfully");
    }

    public CrxResponse savePicture(IdRequest idRequest){
        String[] program = new String[2];
        StringBuffer reply = new StringBuffer();
        StringBuffer stderr = new StringBuffer();
        program[0] = cranixBaseDir +"tools/idrequest/convert_id_picture.sh";
        program[1] = idRequest.getUuid();
        CrxSystemCmd.exec(program, reply, stderr, idRequest.getPicture());
        idRequest.setAvatar("data:image/jpg;base64," + reply);
        return new CrxResponse("OK","Picture was saved successfully");
    }

    private void getPicture(IdRequest idRequest){
        File inputFile = new File(crxIDPicturesDir + "/" + idRequest.getUuid());
        if(inputFile.exists()) {
            try {
                idRequest.setPicture(Files.readString(inputFile.toPath()));
            } catch (IOException e) {
                logger.error("getPicture" + e.getMessage());
            }
        }
    }

    private void getUrls(IdRequest idRequest){
        File idUrlsFile = new File(crxIDRequestsDir + "/" + idRequest.getUuid());
        if(idUrlsFile.exists()){
            try {
                JSONObject urls = new JSONObject(Files.readString(idUrlsFile.toPath()));
                idRequest.setAppleUrl(urls.getString("appleUrl"));
                idRequest.setGoogleUrl(urls.getString("googleUrl"));
            } catch (Exception e){
                logger.error("getMyIdRequest" + e.getMessage());
            }
        }
    }
    public IdRequest getById(Long id){
        IdRequest idRequest = em.find(IdRequest.class, id);
        getPicture(idRequest);
        getUrls(idRequest);
        return idRequest;
    }

    public List<IdRequest> getAll(){
        return this.em.createNamedQuery("IdRequests.findAll").getResultList();
    }

    public CrxResponse delete(Long id){
        IdRequest idRequest = em.find(IdRequest.class, id);
        em.getTransaction().begin();
        em.remove(idRequest);
        em.getTransaction().commit();
        File inputFile = new File(crxIDPicturesDir + "/" + idRequest.getUuid());
        inputFile.delete();
        inputFile = new File(crxIDRequestsDir + "/" + idRequest.getUuid());
        inputFile.delete();
        return new CrxResponse("OK","Id request was removed successfully.");
    }

    public CrxResponse setAllowedSate(IdRequest idRequest){
        IdRequest oldIdRequest = em.find(IdRequest.class, idRequest.getId());
        oldIdRequest.setAllowed(
                session,
                idRequest.getAllowed(),
                idRequest.getAllowed() ? "" : idRequest.getComment(),
                idRequest.getValidUntil()
        );
        em.getTransaction().begin();
        em.merge(oldIdRequest);
        em.getTransaction().commit();
        if(idRequest.getAllowed()){
            return createId(idRequest);
        }
        return new CrxResponse("OK","Id was modified successfully.");
    }

    public IdRequest getMyIdRequest() {
        User me = this.em.find(User.class,this.session.getUserId());
        if( !me.getCreatedRequests().isEmpty()) {
            IdRequest idRequest = me.getCreatedRequests().get(0);
            getPicture(idRequest);
            getUrls(idRequest);
            return idRequest;
        }
        return null;
    }

    private CrxResponse createId(IdRequest idRequest){
        getPicture(idRequest);
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
        program[9] = idRequest.toString();
        program[10] = crxIDUrl;
        CrxSystemCmd.exec(program, reply, stderr, null);
        File outputFile = new File(crxIDRequestsDir + "/" + idRequest.getUuid());
        try {
            Files.writeString(outputFile.toPath(), reply.toString());
        } catch (IOException e) {
            return new CrxResponse("ERROR","Could not create id request:" + e.getMessage());
        }
        return new CrxResponse("OK","Id was created successfully.");
    }
}
