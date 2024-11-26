package de.cranix.services;

import de.cranix.dao.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PTMService extends Service {

    Logger logger = LoggerFactory.getLogger(PTMService.class);

    public PTMService() {
        super();
    }

    public PTMService(Session session, EntityManager em) {
        super(session, em);
    }

    public CrxResponse add(ParentTeacherMeeting parentTeacherMeeting) {
        try {
            parentTeacherMeeting.setCreator(this.session.getUser());
            this.em.getTransaction().begin();
            this.em.persist(parentTeacherMeeting);
            this.em.getTransaction().commit();
            return new CrxResponse("OK", "Parent teacher meeting was created successfully.");
        } catch (Exception e) {
            return new CrxResponse("ERROR", e.getMessage());
        }
    }

    public CrxResponse delete(Long id) {
        try {
            ParentTeacherMeeting parentTeacherMeeting = this.em.find(ParentTeacherMeeting.class, id);
            this.em.getTransaction().begin();
            this.em.remove(parentTeacherMeeting);
            this.em.getTransaction().commit();
            return new CrxResponse("OK", "Parent teacher meeting was removed successfully.");
        } catch (Exception e) {
            return new CrxResponse("ERROR", e.getMessage());
        }
    }

    public ParentTeacherMeeting get() {
        try {
            Query query = this.em.createNamedQuery("PTMs.findAll");
            if (!query.getResultList().isEmpty()) {
                return (ParentTeacherMeeting) query.getResultList().get(0);
            }
        } catch (Exception e) {
            logger.error("get:" + e.getMessage());
        }
        return null;
    }

    public List<ParentTeacherMeeting> getAll() {
        try {
            Query query = this.em.createNamedQuery("PTMs.findAll");
            return (List<ParentTeacherMeeting>) query.getResultList();
        } catch (Exception e) {
            logger.error("getAll:" + e.getMessage());
        }
        return null;
    }

    public ParentTeacherMeeting getById(Long id) {
        try {
            ParentTeacherMeeting parentTeacherMeeting = this.em.find(ParentTeacherMeeting.class, id);
            return parentTeacherMeeting;
        } catch (Exception e) {
            logger.error("GET PTM:" + id + "ERROR" + e.getMessage());
            return null;
        }
    }

    public List<Room> getFreeRooms(Long id) {
        try {
            ParentTeacherMeeting parentTeacherMeeting = this.em.find(ParentTeacherMeeting.class, id);
            List<Room> reservedRooms = new ArrayList<>();
            for(PTMTeacherInRoom teacherInRoom: parentTeacherMeeting.getPtmTeacherInRoomList()) {
                reservedRooms.add(teacherInRoom.getRoom());
            }
            List<Room> freeRooms = new ArrayList<>();
            for(Room room : new RoomService(session, em).getAll()) {
                if(!reservedRooms.contains(room) && !room.getRoomType().equals("adHoc")){
                    freeRooms.add(room);
                }
            }
            return freeRooms;
        }catch (Exception e) {
            logger.error("getFreeRooms:" + id + "ERROR" + e.getMessage());
            return null;
        }
    }

    public CrxResponse registerRoom(Long id, PTMTeacherInRoom ptmTeacherInRoom) {
        ParentTeacherMeeting parentTeacherMeeting = this.em.find(ParentTeacherMeeting.class, id);
        if (parentTeacherMeeting == null) {
            return new CrxResponse("ERROR", "Can not find parent teacher meeting.");
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
            return new CrxResponse("OK", "You was registered for the parent teacher meeting successfully.");
        } catch (Exception e) {
            return new CrxResponse("ERROR", e.getMessage());
        }
    }

    public CrxResponse cancelRoomRegistration(Long id) {
        PTMTeacherInRoom ptmTeacherInRoom = this.em.find(PTMTeacherInRoom.class, id);
        if (ptmTeacherInRoom == null) {
            return new CrxResponse("ERROR", "Can not find the PTM event.");
        }
        ParentTeacherMeeting parentTeacherMeeting = ptmTeacherInRoom.getParentTeacherMeeting();
        parentTeacherMeeting.getPtmTeacherInRoomList().remove(parentTeacherMeeting);
        try {
            this.em.getTransaction().begin();
            this.em.merge(parentTeacherMeeting);
            this.em.remove(ptmTeacherInRoom);
            this.em.getTransaction().commit();
        } catch (Exception e) {
            return new CrxResponse("ERROR", e.getMessage());
        }
        return new CrxResponse("OK", "Room reservation was cancelled successfully.");
    }

    public CrxResponse registerEvent(PTMEvent event) {
        PTMEvent oldEvent = this.em.find(PTMEvent.class, event.getId());
        if (oldEvent == null) {
            return new CrxResponse("ERROR", "Can not find the PTM event.");
        }
        oldEvent.setParent(event.getParent());
        oldEvent.setStudent(event.getStudent());
        try {
            this.em.getTransaction().begin();
            this.em.merge(oldEvent);
            this.em.getTransaction().commit();
            return new CrxResponse("OK", "You was registered for the parent teacher meeting successfully.");
        } catch (Exception e) {
            return new CrxResponse("ERROR", e.getMessage());
        }
    }

    public CrxResponse cancelEvent(Long id) {
        PTMEvent oldEvent = this.em.find(PTMEvent.class, id);
        if (oldEvent == null) {
            return new CrxResponse("ERROR", "Can not find the PTM event.");
        }
        oldEvent.setParent(null);
        oldEvent.setStudent(null);
        try {
            this.em.getTransaction().begin();
            this.em.merge(oldEvent);
            this.em.getTransaction().commit();
            return new CrxResponse("OK", "You was registered for the parent teacher meeting successfully.");
        } catch (Exception e) {
            return new CrxResponse("ERROR", e.getMessage());
        }
    }

    public CrxResponse modify(ParentTeacherMeeting parentTeacherMeeting) {
        try {
            this.em.getTransaction().begin();
            this.em.merge(parentTeacherMeeting);
            this.em.getTransaction().commit();
            return new CrxResponse("OK", "The parent teacher meeting was modified successfully.");
        } catch (Exception e) {
            return new CrxResponse("ERROR", e.getMessage());
        }
    }
}
