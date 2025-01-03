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
            if (parentTeacherMeeting.getTemplateId() != null) {
                ParentTeacherMeeting oldPTM = this.em.find(ParentTeacherMeeting.class, parentTeacherMeeting.getTemplateId());
                for (PTMTeacherInRoom ptmTeacherInRoom : oldPTM.getPtmTeacherInRoomList()) {
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
                    } catch (Exception e) {
                        logger.error("add register room failed:" + ptmTeacherInRoom.getTeacher().getUid() + " " + ptmTeacherInRoom.getRoom().getName());
                    }

                }
            }
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

    public List<ParentTeacherMeeting> get() {
        try {
            Query query = this.em.createNamedQuery("PTMs.findActual");
            if (!query.getResultList().isEmpty()) {
                if (this.session.getAcls().contains("ptm.manage")) {
                    return (List<ParentTeacherMeeting>) query.getResultList();
                }
                List<ParentTeacherMeeting> ptms = new ArrayList<>();
                for (ParentTeacherMeeting ptm : (List<ParentTeacherMeeting>) query.getResultList()) {
                    for (Group g : ptm.getClasses()) {
                        if (this.session.getUser().getGroups().contains(g)) {
                            ptms.add(ptm);
                            break;
                        }
                    }
                }
                return ptms;
            }
        } catch (Exception e) {
            logger.error("get:" + e.getMessage());
        }
        return new ArrayList<>();
    }

    public List<ParentTeacherMeeting> getFormer() {
        try {
            Query query = this.em.createNamedQuery("PTMs.findFormer");
            if (!query.getResultList().isEmpty()) {
                return (List<ParentTeacherMeeting>) query.getResultList();
            }
        } catch (Exception e) {
            logger.error("get:" + e.getMessage());
        }
        return new ArrayList<>();
    }

    public List<ParentTeacherMeeting> getAll() {
        try {
            Query query = this.em.createNamedQuery("PTMs.findAll");
            return (List<ParentTeacherMeeting>) query.getResultList();
        } catch (Exception e) {
            logger.error("getAll:" + e.getMessage());
        }
        return new ArrayList<>();
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
            for (PTMTeacherInRoom teacherInRoom : parentTeacherMeeting.getPtmTeacherInRoomList()) {
                reservedRooms.add(teacherInRoom.getRoom());
            }
            List<Room> freeRooms = new ArrayList<>();
            for (Room room : new RoomService(session, em).getAll()) {
                if (room.getRoomType().equals("AdHocAccess") || room.getRoomType().equals("technicalRoom")) {
                    continue;
                }
                if (this.getConfigValue("PTM_ALLOW_MULTI_USE_OF_ROOMS").equals("yes") || !reservedRooms.contains(room)) {
                    freeRooms.add(room);
                }
            }
            return freeRooms;
        } catch (Exception e) {
            logger.error("getFreeRooms:" + id + "ERROR" + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<User> getFreeTeachers(Long id) {
        try {
            ParentTeacherMeeting parentTeacherMeeting = this.em.find(ParentTeacherMeeting.class, id);
            List<User> reservedUsers = new ArrayList<>();
            for (PTMTeacherInRoom teacherInRoom : parentTeacherMeeting.getPtmTeacherInRoomList()) {
                reservedUsers.add(teacherInRoom.getTeacher());
            }
            List<User> freeUsers = new ArrayList<>();
            for (User teacher : new UserService(session, em).getByRole("teachers")) {
                if (!reservedUsers.contains(teacher)) {
                    freeUsers.add(teacher);
                }
            }
            return freeUsers;
        } catch (Exception e) {
            logger.error("getFreeRooms:" + id + "ERROR" + e.getMessage());
            return new ArrayList<>();
        }
    }

    public CrxResponse registerRoom(Long id, PTMTeacherInRoom ptmTeacherInRoom) {
        ParentTeacherMeeting parentTeacherMeeting = this.em.find(ParentTeacherMeeting.class, id);
        if (parentTeacherMeeting == null) {
            return new CrxResponse("ERROR", "Can not find parent teacher meeting.");
        }
	if(ptmTeacherInRoom.getTeacher() == null && isAllowed("ptm.registerRoom")) {
		ptmTeacherInRoom.setTeacher(this.session.getUser());
	} else {
            return new CrxResponse("ERROR", "Can not register the room.");
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
        oldEvent.setStudent(event.getStudent());
        if( event.getParent() != null) {
            User student = this.em.find(User.class, event.getStudent().getId());
            if(!student.getParents().isEmpty()){
                oldEvent.setParent(student.getParents().get(0));
            }
        } else {
            oldEvent.setParent(event.getParent());
        }
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

    public CrxResponse setBlockEvent(Long id, Boolean block) {
        PTMEvent oldEvent = this.em.find(PTMEvent.class, id);
        if (oldEvent == null) {
            return new CrxResponse("ERROR", "Can not find the PTM event.");
        }
        oldEvent.setBlocked(block);
        try {
            this.em.getTransaction().begin();
            this.em.merge(oldEvent);
            this.em.getTransaction().commit();
            if(block)  {
                return new CrxResponse("OK", "Event was blocked");
            }
            return new CrxResponse("OK", "Event was released");
        } catch (Exception e) {
            return new CrxResponse("ERROR", e.getMessage());
        }
    }

    public CrxResponse modify(ParentTeacherMeeting parentTeacherMeeting) {
        try {
            logger.debug("modify:" + parentTeacherMeeting);
            ParentTeacherMeeting oldPTM = this.em.find(ParentTeacherMeeting.class, parentTeacherMeeting.getId());
            this.em.getTransaction().begin();
            oldPTM.setTitle(parentTeacherMeeting.getTitle());
            if (oldPTM.getPtmTeacherInRoomList().isEmpty()) {
                oldPTM.setStart(parentTeacherMeeting.getStart());
                oldPTM.setEnd(parentTeacherMeeting.getEnd());
                oldPTM.setStartRegistration(parentTeacherMeeting.getStartRegistration());
                oldPTM.setEndRegistration(parentTeacherMeeting.getEndRegistration());
            }
            this.em.merge(oldPTM);
            this.em.getTransaction().commit();
            return new CrxResponse("OK", "The parent teacher meeting was modified successfully.");
        } catch (Exception e) {
            return new CrxResponse("ERROR", e.getMessage());
        }
    }
}
