package de.cranix.services;

import de.cranix.dao.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.util.Date;

public class PTMService extends Service{

    Logger logger = LoggerFactory.getLogger(PTMService.class);

    public PTMService(){super();}

    public PTMService(Session session, EntityManager em) {
        super(session,em);
    }

    public CrxResponse add(ParentTeacherMeeting parentTeacherMeeting) {
        try {
            parentTeacherMeeting.setCreator(this.session.getUser());
            this.em.getTransaction().begin();
            this.em.persist(parentTeacherMeeting);
            this.em.getTransaction().commit();
            return new CrxResponse("OK","Parent teacher meeting was created successfully.");
        } catch (Exception e) {
            return new CrxResponse("ERROR",e.getMessage());
        }
    }

    public CrxResponse delete(Long id) {
        try {
            ParentTeacherMeeting parentTeacherMeeting = this.em.find(ParentTeacherMeeting.class, id);
            this.em.getTransaction().begin();
            this.em.remove(parentTeacherMeeting);
            this.em.getTransaction().commit();
            return new CrxResponse("OK","Parent teacher meeting was removed successfully.");
        } catch (Exception e) {
            return new CrxResponse("ERROR",e.getMessage());
        }
    }

    public ParentTeacherMeeting get(Long id) {
        try {
            ParentTeacherMeeting parentTeacherMeeting = this.em.find(ParentTeacherMeeting.class, id);
            return parentTeacherMeeting;
        } catch (Exception e) {
            logger.error("GET PTM:" + id+ "ERROR" + e.getMessage());
            return null;
        }
    }

    public CrxResponse registerRoom(Long id, PTMTeacherInRoom ptmTeacherInRoom) {
        ParentTeacherMeeting parentTeacherMeeting = this.em.find(ParentTeacherMeeting.class, id);
        if( parentTeacherMeeting == null ){
            return new CrxResponse("ERROR","Can not find parent teacher meeting.");
        }
        PTMTeacherInRoom newPTMTiT = new PTMTeacherInRoom(
                this.session,
                ptmTeacherInRoom.getTeacher(),
                ptmTeacherInRoom.getRoom(),
                parentTeacherMeeting
        );
        try {
            this.em.getTransaction().begin();
            parentTeacherMeeting.getPtmTeacherInRoomList().add(newPTMTiT);
            this.em.merge(parentTeacherMeeting);
            this.em.getTransaction().commit();
            return new CrxResponse("OK","You was registered for the parent teacher meeting successfully.");
        } catch (Exception e) {
            return new CrxResponse("ERROR",e.getMessage());
        }
    }

    public CrxResponse registerEvent(PTMEvent event){
        PTMEvent oldEvent = this.em.find(PTMEvent.class, event.getId());
        if(oldEvent == null){
            return new CrxResponse("ERROR","Can not find the PTM event.");
        }
        oldEvent.setParent(event.getParent());
        try {
            this.em.getTransaction().begin();
            this.em.merge(oldEvent);
            this.em.getTransaction().commit();
            return new CrxResponse("OK","You was registered for the parent teacher meeting successfully.");
        } catch (Exception e) {
            return new CrxResponse("ERROR",e.getMessage());
        }
    }
}