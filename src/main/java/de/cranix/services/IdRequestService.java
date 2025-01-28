package de.cranix.services;

import de.cranix.dao.CrxResponse;
import de.cranix.dao.IdRequest;
import de.cranix.dao.Session;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static de.cranix.helper.CranixConstants.cranixTmpDir;

public class IdRequestService extends Service{
    public IdRequestService(){
        super();
    }
    public IdRequestService(Session session, EntityManager em){
        super(session,em);
    }

    public CrxResponse add(IdRequest idRequest){
        try {
            em.getTransaction().begin();
            em.persist(idRequest);
            em.getTransaction().commit();
        } catch (Exception e){
            return new CrxResponse("ERROR","Could not create id request:" + e.getMessage());
        }
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

    public CrxResponse setAllowedSatus(Long id, Boolean allowed){
        IdRequest idRequest = em.find(IdRequest.class, id);
        idRequest.setAllowed(allowed, session);
        em.getTransaction().begin();
        em.merge(idRequest);
        em.getTransaction().commit();
        return new CrxResponse("OK","Id was modified successfully.");
    }
}
