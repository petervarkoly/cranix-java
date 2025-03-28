package de.cranix.services;

import de.cranix.dao.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

import static de.cranix.helper.CranixConstants.cranixBaseDir;
import static de.cranix.helper.CranixConstants.cranixTmpDir;
import static de.cranix.helper.CranixConstants.cranixPTMConfig;
import static de.cranix.helper.CranixConstants.privatDirAttribute;

public class PTMService extends Service {

    Logger logger = LoggerFactory.getLogger(PTMService.class);
    final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    /*
     * Following variables are supported in cranixPTMConfig
     * ALLOW_MULTI_USE_OF_ROOMS
     * SEND_NOTIFICATION_TO_STUDENTS
     */
    static Config ptmConfig;

    static Map<Long, Date> lastChange = new HashMap<Long, Date>();

    public PTMService() {
        super();
        if (ptmConfig == null) ptmConfig = new Config(cranixPTMConfig, "");
    }

    public PTMService(Session session, EntityManager em) {
        super(session, em);
        if (ptmConfig == null) ptmConfig = new Config(cranixPTMConfig, "");
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
            return new CrxResponse("OK", "Parent teacher meeting was created successfully.", parentTeacherMeeting.getId());
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
                    if (!ptm.getReleased()) continue;
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

    private boolean haveSameClass(User user1, User user2) {
        for (Long clasId : user1.getClassIds()) {
            if (user2.getClassIds().contains(clasId)) {
                return true;
            }
        }
        return false;
    }

    public ParentTeacherMeeting getById(Long id) {
        try {
            ParentTeacherMeeting parentTeacherMeeting = this.em.find(ParentTeacherMeeting.class, id);
            if (this.session.getAcls().contains("ptm.manage")) {
                return parentTeacherMeeting;
            }
            if (this.session.getAcls().contains("ptm.use") && parentTeacherMeeting.getReleased()) {
                return parentTeacherMeeting;
            }
            if (parentTeacherMeeting.getReleased()) {
                for (PTMTeacherInRoom ptmTeacherInRoom : parentTeacherMeeting.getPtmTeacherInRoomList()) {
                    if (ptmTeacherInRoom.getRoom() != null  && haveSameClass(this.session.getUser(), ptmTeacherInRoom.getTeacher())) {
                        for (PTMEvent ptmEvent : ptmTeacherInRoom.getEvents()) {
                            if (ptmEvent.getStudent() != null) {
                                ptmEvent.getStudent().setBirthDay("");
                            }
                        }
                    } else {
                        ptmTeacherInRoom.setEvents(new ArrayList<>());
                    }
                }
                return parentTeacherMeeting;
            }
            return null;
        } catch (Exception e) {
            logger.error("GET PTM:" + id + "ERROR" + e.getMessage());
            return null;
        }
    }

    public List<Room> getFreeRooms(Long id) {
        Boolean allowMultipleUseOfRooms = this.ptmConfig.getConfigValue("ALLOW_MULTI_USE_OF_ROOMS").equals("yes");
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
                if (allowMultipleUseOfRooms || !reservedRooms.contains(room)) {
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
        if (ptmTeacherInRoom.getTeacher() == null && isAllowed("ptm.registerRoom")) {
            ptmTeacherInRoom.setTeacher(this.session.getUser());
        } else if (ptmTeacherInRoom.getTeacher() == null) {
            return new CrxResponse("ERROR", "Can not register the room.");
        }
        if (ptmTeacherInRoom.getId() == 0) {
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
                lastChange.put(id, new Date());
                return new CrxResponse("OK", "Room was registered for parent teacher meeting successfully.");
            } catch (Exception e) {
                return new CrxResponse("ERROR", e.getMessage());
            }
        } else {
            PTMTeacherInRoom ptmTeacherInRoom1 = this.em.find(PTMTeacherInRoom.class, ptmTeacherInRoom.getId());
            ptmTeacherInRoom1.setRoom(ptmTeacherInRoom.getRoom());
            //ptmTeacherInRoom1.setTeacher(ptmTeacherInRoom.getTeacher());
            try {
                this.em.getTransaction().begin();
                this.em.merge(ptmTeacherInRoom1);
                this.em.getTransaction().commit();
                lastChange.put(id, new Date());
                return new CrxResponse("OK", "Room was changed for parent teacher meeting successfully.");
            } catch (Exception e) {
                return new CrxResponse("ERROR", e.getMessage());
            }
        }
    }

    public CrxResponse cancelRoomRegistration(Long id) {
        PTMTeacherInRoom ptmTeacherInRoom = this.em.find(PTMTeacherInRoom.class, id);
        if (ptmTeacherInRoom == null) {
            return new CrxResponse("ERROR", "Can not find the PTM Teacher in Room.");
        }
        ParentTeacherMeeting parentTeacherMeeting = ptmTeacherInRoom.getParentTeacherMeeting();
        parentTeacherMeeting.getPtmTeacherInRoomList().remove(ptmTeacherInRoom);
        try {
            this.em.getTransaction().begin();
            this.em.merge(parentTeacherMeeting);
            this.em.remove(ptmTeacherInRoom);
            this.em.getTransaction().commit();
            lastChange.put(ptmTeacherInRoom.getParentTeacherMeeting().getId(), new Date());
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
        if (oldEvent.getBlocked()) {
            return new CrxResponse("ERROR", "This appointment is blocked.");
        }
        if (oldEvent.getStudent() != null) {
            return new CrxResponse("ERROR", "This appointment is already reserved.");
        }
        for (PTMEvent ptmEvent : oldEvent.getTeacherInRoom().getEvents()) {
            if (ptmEvent.equals(event)) continue;
            if (ptmEvent.getStudent() != null && ptmEvent.getStudent().equals(event.getStudent())) {
                return new CrxResponse("ERROR", "There is already an event reserved for the student by this teacher.");
            }
        }
        oldEvent.setStudent(event.getStudent());
        if (event.getParent() == null) {
            User student = this.em.find(User.class, event.getStudent().getId());
            if (!student.getParents().isEmpty()) {
                oldEvent.setParent(student.getParents().get(0));
            }
        } else {
            oldEvent.setParent(event.getParent());
        }
        try {
            this.em.getTransaction().begin();
            this.em.merge(oldEvent);
            this.em.getTransaction().commit();
            lastChange.put(oldEvent.getTeacherInRoom().getParentTeacherMeeting().getId(), new Date());
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
            lastChange.put(oldEvent.getTeacherInRoom().getParentTeacherMeeting().getId(), new Date());
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
            lastChange.put(oldEvent.getTeacherInRoom().getParentTeacherMeeting().getId(), new Date());
            if (block) {
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
            oldPTM.setStartRegistration(parentTeacherMeeting.getStartRegistration());
            oldPTM.setEndRegistration(parentTeacherMeeting.getEndRegistration());
            oldPTM.setReleased(parentTeacherMeeting.getReleased());
            if (oldPTM.getPtmTeacherInRoomList().isEmpty()) {
                oldPTM.setStart(parentTeacherMeeting.getStart());
                oldPTM.setEnd(parentTeacherMeeting.getEnd());
            }
            this.em.merge(oldPTM);
            this.em.getTransaction().commit();
            return new CrxResponse("OK", "The parent teacher meeting was modified successfully.");
        } catch (Exception e) {
            return new CrxResponse("ERROR", e.getMessage());
        }
    }

    public CrxResponse sendNotifications(Long id) {
        ParentTeacherMeeting parentTeacherMeeting = this.em.find(ParentTeacherMeeting.class, id);
        File dirName = new File(cranixTmpDir + "PTMs/" + dateFormat.format(parentTeacherMeeting.getStart()) + "/");
        Boolean sendNotificationToStudents = this.ptmConfig.getConfigValue("SEND_NOTIFICATION_TO_STUDENTS").equals("yes");
        try {
            Files.createDirectories(dirName.toPath(), privatDirAttribute);
            for (Group group : parentTeacherMeeting.getClasses()) {
                for (User student : group.getUsers()) {
                    if (!student.getRole().equals("students")) continue;
                    logger.debug("Student:" + student.getUid());
                    if (sendNotificationToStudents) {
                        sendNotification(parentTeacherMeeting, student, null, dirName.getPath());
                    }
                    for (User parent : student.getParents()) {
                        sendNotification(parentTeacherMeeting, student, parent, dirName.getPath());
                    }
                }
            }
            Job sendMails = new Job(
                    "Send notification for the PTM on " + dateFormat.format(parentTeacherMeeting.getStart()),
                    null,
                    cranixBaseDir + "tools/PTM/send_mails " + dirName,
                    true
            );
            new JobService(session, em).createJob(sendMails);
            return new CrxResponse("OK", "Sending notifications was started.");
        } catch (Exception e) {
            logger.error("sendNotifications:" + e.getMessage());
            return new CrxResponse("ERROR", e.getMessage());
        }
    }

    void sendNotification(ParentTeacherMeeting parentTeacherMeeting, User student, User parent, String dirName) {
        String fileName;
        String mailAddress;
        String template;
        String message;
        SessionService sessionService = new SessionService(em);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            if (parent == null) {
                template = Files.readString(Paths.get(cranixBaseDir + "templates/PTM/LetterStudentTemplate.html"));
            } else {
                template = Files.readString(Paths.get(cranixBaseDir + "templates/PTM/LetterParentTemplate.html"));
            }
            final String gotoPath = "trusted/registerPTM/" + parentTeacherMeeting.getId();
            Session parentSession = null;
            logger.debug("student:" + student.getUid());
            for (Session session1 : student.getSessions()) {
                if (session1.getGotoPath() != null && session1.getGotoPath().equals(gotoPath)) {
                    parentSession = session1;
                    break;
                }
            }
            if (parentSession == null) {
                parentSession = sessionService.createSession(
                        student,
                        parentTeacherMeeting.getStartRegistration(),
                        parentTeacherMeeting.getEndRegistration(),
                        gotoPath
                );
            }
            if (parent == null) {
                message = template
                        .replaceAll("#TOKEN#", parentSession.getToken())
                        .replaceAll("#SURNAME#", student.getSurName())
                        .replaceAll("#GIVENNAME#", student.getGivenName())
                        .replaceAll("#DATE#", dateFormat.format(parentTeacherMeeting.getStart()))
                        .replaceAll("#FROM#", timeFormat.format(parentTeacherMeeting.getStart()))
                        .replaceAll("#UNTIL#", timeFormat.format(parentTeacherMeeting.getEnd()))
                        .replaceAll("#REGSART#", dateTimeFormat.format(parentTeacherMeeting.getStartRegistration()))
                        .replaceAll("#REGEND#", dateTimeFormat.format(parentTeacherMeeting.getEndRegistration()));
                fileName = dirName + "/" + student.getUid();
                if (student.getEmailAddress() == null || student.getEmailAddress().isEmpty()) {
                    mailAddress = student.getUid();
                } else {
                    mailAddress = student.getEmailAddress();
                }
            } else {
                message = template
                        .replaceAll("#TOKEN#", parentSession.getToken())
                        .replaceAll("#SURNAME#", student.getSurName())
                        .replaceAll("#GIVENNAME#", student.getGivenName())
                        .replaceAll("#DATE#", dateFormat.format(parentTeacherMeeting.getStart()))
                        .replaceAll("#PARENT_SURNAME#", parent.getSurName())
                        .replaceAll("#PARENT_GIVENNAME#", parent.getGivenName())
                        .replaceAll("#FROM#", timeFormat.format(parentTeacherMeeting.getStart()))
                        .replaceAll("#UNTIL#", timeFormat.format(parentTeacherMeeting.getEnd()))
                        .replaceAll("#REGSART#", dateTimeFormat.format(parentTeacherMeeting.getStartRegistration()))
                        .replaceAll("#REGEND#", dateTimeFormat.format(parentTeacherMeeting.getEndRegistration()));
                fileName = dirName + "/" + parent.getUuid();
                mailAddress = parent.getEmailAddress();
            }
            Files.write(Paths.get(fileName), message.getBytes());
            Files.write(Paths.get(fileName + ".mailAddress"), mailAddress.getBytes());
            //Create subject
            String subjectTemplate = Files.readString(Paths.get(cranixBaseDir + "templates/PTM/LetterSubjectTemplate"));
            String subject = subjectTemplate.replaceAll("#SURNAME#", student.getSurName())
                    .replaceAll("#GIVENNAME#", student.getGivenName())
                    .replaceAll("#DATE#", dateFormat.format(parentTeacherMeeting.getStart()));
            Files.write(Paths.get(fileName + ".subject"), subject.getBytes());
        } catch (Exception e) {
            logger.error("sendNotification" + e.getMessage());
        }
    }

    public Date getLastChange(Long id) {
        if (lastChange.containsKey(id)) {
            return lastChange.get(id);
        }
        return null;
    }

    public Map<String, String> getPtmSettings() {
        Map<String, String> result = new HashMap<>();
        for(Map<String, String> tmp: this.ptmConfig.getConfig()){
         result.put(tmp.get("key"),tmp.get("value"));
        }
        try {
            result.put("LetterSubjectTemplate", Files.readString(Paths.get(cranixBaseDir + "templates/PTM/LetterSubjectTemplate")));
            result.put("LetterStudentTemplate", Files.readString(Paths.get(cranixBaseDir + "templates/PTM/LetterStudentTemplate")));
            result.put("LetterParentTemplate", Files.readString(Paths.get(cranixBaseDir + "templates/PTM/LetterParentTemplate")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return  result;
    }

    public CrxResponse setPtmSettings(Map<String, String> settings) {
        for(String key: settings.keySet()){
            try {
                switch (key) {
                    case "LetterSubjectTemplate":
                        Files.writeString(Paths.get(cranixBaseDir + "templates/PTM/LetterSubjectTemplate"), settings.get(key));
                        break;
                    case "LetterStudentTemplate":
                        Files.writeString(Paths.get(cranixBaseDir + "templates/PTM/LetterStudentTemplate"), settings.get(key));
                        break;
                    case "LetterParentTemplate":
                        Files.writeString(Paths.get(cranixBaseDir + "templates/PTM/LetterParentTemplate"), settings.get(key));
                        break;
                    default:
                        this.ptmConfig.setConfigValue(key, settings.get(key));
                }
            } catch (IOException e){
                logger.error(e.getMessage());
                return new CrxResponse("ERROR",e.getMessage());
            }
        }
        return new CrxResponse("OK","PTM Settings was saved successfully");
    }
}
