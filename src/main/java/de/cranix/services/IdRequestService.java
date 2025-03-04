package de.cranix.services;

import de.cranix.dao.CrxResponse;
import de.cranix.dao.IdRequest;
import de.cranix.dao.Session;
import de.cranix.dao.User;
import de.cranix.helper.CrxSystemCmd;
import org.json.JSONObject;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static de.cranix.helper.CranixConstants.*;
import static de.cranix.helper.StaticHelpers.createLiteralJson;

public class IdRequestService extends Service{

    private static String crxIDUrl;
    private static String crxIDPicturesDir;
    private static String crxIDRequestsDir;
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
        }
    }

    public CrxResponse add(IdRequest idRequest){
        IdRequest oldIdRequest = getMyIdRequest();
        if(oldIdRequest != null) {
            if( oldIdRequest.equals(idRequest)) {
                return this.savePicture(idRequest);
            }
            return new CrxResponse("ERROR","You have already created an ID request. You must not create a new one.");
        }
        IdRequest newIdRequest = new IdRequest(session);
        try {
            em.getTransaction().begin();
            em.persist(newIdRequest);
            em.getTransaction().commit();
        } catch (Exception e){
            return new CrxResponse("ERROR","Could not create id request:" + e.getMessage());
        }
        newIdRequest.setPicture(idRequest.getPicture());
        savePicture(newIdRequest);
        return new CrxResponse("OK","Id request was created successfully");
    }

    public CrxResponse savePicture(IdRequest idRequest){
        File outputFile = new File(crxIDPicturesDir + "/" + idRequest.getUuid());
        try {
            Files.write(outputFile.toPath(), idRequest.getPicture());
        } catch (IOException e) {
            return new CrxResponse("ERROR","Could not create id request:" + e.getMessage());
        }
        return new CrxResponse("OK","Picture was saved successfully");
    }

    private void getPicture(IdRequest idRequest){
        File inputFile = new File(crxIDPicturesDir + "/" + idRequest.getUuid());
        if(inputFile.exists()) {
            try {
                idRequest.setPicture(Files.readAllBytes(inputFile.toPath()));
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
            Files.write(outputFile.toPath(), reply.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            return new CrxResponse("ERROR","Could not create id request:" + e.getMessage());
        }
        return new CrxResponse("OK","Id was created successfully.");
    }
}
