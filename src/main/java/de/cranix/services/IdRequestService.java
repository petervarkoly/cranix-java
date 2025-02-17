package de.cranix.services;

import de.cranix.dao.CrxResponse;
import de.cranix.dao.IdRequest;
import de.cranix.dao.Session;
import de.cranix.dao.User;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static de.cranix.helper.CranixConstants.cranixTmpDir;
import static de.cranix.helper.StaticHelpers.createLiteralJson;

public class IdRequestService extends Service{

    public IdRequestService(){
        super();
    }
    public IdRequestService(Session session, EntityManager em){
        super(session,em);
    }

    public CrxResponse add(IdRequest idRequest){
        IdRequest oldIdRequest = getMyIdRequest();
        if(oldIdRequest != null) {
            if( oldIdRequest.equals(idRequest)) {
                return this.modify(idRequest);
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
        File outputFile = new File(cranixTmpDir + "pictures/" + newIdRequest.getUuid());
        try {
            Files.write(outputFile.toPath(), idRequest.getPicture());
        } catch (IOException e) {
            return new CrxResponse("ERROR","Could not create id request:" + e.getMessage());
        }
        return new CrxResponse("OK","Id request was created successfully");
    }

    public CrxResponse modify(IdRequest idRequest){
        File outputFile = new File(cranixTmpDir + "pictures/" + idRequest.getUuid());
        try {
            Files.write(outputFile.toPath(), idRequest.getPicture());
        } catch (IOException e) {
            return new CrxResponse("ERROR","Could not create id request:" + e.getMessage());
        }
        return new CrxResponse("OK","Id request was created successfully");
    }

    private void getPicture(IdRequest idRequest){
        File inputFile = new File(cranixTmpDir + "pictures/" + idRequest.getUuid());
        try {
            idRequest.setPicture(Files.readAllBytes(inputFile.toPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public IdRequest getById(Long id){
        IdRequest idRequest = em.find(IdRequest.class, id);
        getPicture(idRequest);
        return idRequest;
    }

    public List<IdRequest> getAll(){
        return this.em.createNamedQuery("IdRequests.findAll").getResultList();
    }

    public CrxResponse delete(Long id){
        IdRequest idRequest = em.find(IdRequest.class, id);
        File inputFile = new File(cranixTmpDir + "pictures/" + idRequest.getUuid());
        inputFile.delete();
        em.getTransaction().begin();
        em.remove(idRequest);
        em.getTransaction().commit();
        return new CrxResponse("OK","Id request was removed successfully.");
    }

    public CrxResponse setAllowedSatus(IdRequest idRequest){
        IdRequest oldIdRequest = em.find(IdRequest.class, idRequest.getId());
        oldIdRequest.setAllowed(session, idRequest.getAllowed(), idRequest.getValidUntil());
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
            return me.getCreatedRequests().get(0);
        }
        return null;
    }

    private CrxResponse createId(IdRequest idRequest){
        getPicture(idRequest);
        String jsonPath = cranixTmpDir + "pictures/" + idRequest.getUuid() + ".json";
        File outputFile = new File(jsonPath);
        try {
            Files.write(outputFile.toPath(), createLiteralJson(idRequest).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            return new CrxResponse("ERROR","Could not create id request:" + e.getMessage());
        }
        //TODO
        return new CrxResponse("OK","Id was created successfully.");
    }
}
