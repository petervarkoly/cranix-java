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
import java.util.List;

import static de.cranix.helper.CranixConstants.*;
import static de.cranix.helper.StaticHelpers.getYear;

public class IdRequestService extends Service {

    private static String crxIDUrl;
    private static String crxIDPicturesDir;
    private static String crxIDRequestsDir;
    private static String crxIDValid;

    public IdRequestService() {
        super();
    }

    public IdRequestService(Session session, EntityManager em) {
        super(session, em);
        if (crxIDUrl == null) {
            Config config = new Config(cranixIDConfig, "CRXID_");
            this.crxIDUrl = config.getConfigValue("URL");
            this.crxIDRequestsDir = config.getConfigValue("REQUESTS_DIR");
            this.crxIDPicturesDir = config.getConfigValue("PICTURES_DIR");
            this.crxIDValid = config.getConfigValue("VALID");
        }
    }

    public CrxResponse add(IdRequest idRequest) {
        if (idRequest.getPicture() == null || idRequest.getPicture().isEmpty()) {
            return new CrxResponse("ERROR", "You have to provide a picture.");
        }
        User me = this.em.find(User.class, this.session.getUserId());
        IdRequest oldIdRequest = me.getCreatedRequest();
        if (oldIdRequest != null) {
            oldIdRequest.setPicture(idRequest.getPicture());
            savePicture(oldIdRequest);
            oldIdRequest.setAvatar(idRequest.getAvatar());
            oldIdRequest.setComment("RE: " + oldIdRequest.getComment());
            em.getTransaction().begin();
            em.merge(oldIdRequest);
            em.getTransaction().commit();
            return new CrxResponse("OK", "Id request was modified successfully");
        }
        IdRequest newIdRequest = new IdRequest(session);
        newIdRequest.setPicture(idRequest.getPicture());
        savePicture(newIdRequest);
        String validUntil = getYear() + "-" + crxIDValid;
        try {
            newIdRequest.setValidUntil(validUntil);
            me.setCreatedRequest(newIdRequest);
            em.getTransaction().begin();
            em.persist(me);
            em.getTransaction().commit();
        } catch (Exception e) {
            return new CrxResponse("ERROR", "Could not create id request:" + e.getMessage());
        }
        return new CrxResponse("OK", "Id request was created successfully");
    }

    public CrxResponse savePicture(IdRequest idRequest) {
        String[] program = new String[2];
        StringBuffer reply = new StringBuffer();
        StringBuffer stderr = new StringBuffer();
        program[0] = cranixBaseDir + "tools/idrequest/convert_id_picture.sh";
        program[1] = idRequest.getUuid();
        CrxSystemCmd.exec(program, reply, stderr, idRequest.getPicture());
        idRequest.setAvatar("data:image/jpg;base64," + reply);
        return new CrxResponse("OK", "Picture was saved successfully");
    }

    private void getPicture(IdRequest idRequest) {
        File inputFile = new File(crxIDPicturesDir + "/" + idRequest.getUuid());
        if (inputFile.exists()) {
            try {
                idRequest.setPicture(Files.readString(inputFile.toPath()));
            } catch (IOException e) {
                logger.error("getPicture" + e.getMessage());
            }
        }
    }

    public IdRequest getById(Long id) {
        IdRequest idRequest = em.find(IdRequest.class, id);
        getPicture(idRequest);
        return idRequest;
    }

    public List<IdRequest> getAll() {
        return this.em.createNamedQuery("IdRequests.findAll").getResultList();
    }

    public CrxResponse delete(Long id) {
        IdRequest idRequest = em.find(IdRequest.class, id);
        em.getTransaction().begin();
        em.remove(idRequest);
        em.getTransaction().commit();
        File inputFile = new File(crxIDPicturesDir + "/" + idRequest.getUuid());
        inputFile.delete();
        inputFile = new File(crxIDRequestsDir + "/" + idRequest.getUuid());
        inputFile.delete();
        return new CrxResponse("OK", "Id request was removed successfully.");
    }

    public CrxResponse setIdRequest(IdRequest idRequest) {
        if (idRequest.getAllowed()) {
            CrxResponse crxResponse = createId(idRequest);
            if( crxResponse.getCode() == "ERROR") {
                return crxResponse;
            }
        }
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

        return new CrxResponse("OK", "Id was modified successfully.");
    }

    public IdRequest getMyIdRequest() {
        User me = this.em.find(User.class, this.session.getUserId());
        if (me.getCreatedRequest() != null) {
            IdRequest idRequest = me.getCreatedRequest();
            getPicture(idRequest);
            return idRequest;
        }
        return null;
    }

    private CrxResponse createId(IdRequest idRequest) {
        IdRequest oldIdRequest = this.em.find(IdRequest.class, idRequest.getId());
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
        try {
            JSONObject urls = new JSONObject(reply.toString());
            oldIdRequest.setAppleUrl(urls.getString("appleUrl"));
            oldIdRequest.setGoogleUrl(urls.getString("googleUrl"));
            em.getTransaction().begin();
            em.merge(oldIdRequest);
            em.getTransaction().commit();
            return new CrxResponse("OK", "Id was created successfully.");
        } catch (Exception e) {
            logger.error("createId:" + e.getMessage());
            return new CrxResponse("ERROR", "Creating id failed: " + e.getMessage());
        }
    }
}
