package de.cranix.services;

import de.cranix.dao.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

import static de.cranix.helper.CranixConstants.*;

public class CourseService extends Service {

    Logger logger = LoggerFactory.getLogger(CourseService.class);
    final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    static Map<Long, Date> lastChange = new HashMap<Long, Date>();

    public CrxResponse add(Course course){
        try {
            course.setCreator(this.session.getUser());
            this.em.getTransaction().begin();
            this.em.persist(course);
            this.em.getTransaction().commit();
            lastChange.put(course.getId(),new Date());
        }catch (Exception e){
            logger.error("add" + e.getMessage());
            return new CrxResponse("ERROR",e.getMessage());
        }
        return new CrxResponse("OK","Course was created successfully.");
    }

    public CrxResponse modify(Course course){
        try {
            /* Course oldCourse = this.em.find(Course.class, course.getId());
            oldCourse.setDescription(course.getDescription());
            oldCourse.setStart(course.getStart());
            oldCourse.setEnd(course.getEnd());
            oldCourse.setEndRegistration(course.getEndRegistration());
            oldCourse.setStartRegistration(course.getStartRegistration());
            oldCourse.setReleased(course.getReleased()); */
            this.em.getTransaction().begin();
            this.em.merge(course);
            this.em.getTransaction().commit();
            lastChange.put(course.getId(),new Date());
        }catch (Exception e){
            logger.error("add" + e.getMessage());
            return new CrxResponse("ERROR",e.getMessage());
        }
        return new CrxResponse("OK","Course was created successfully.");
    }

    public CrxResponse delete(Long id){
        try{
            Course course = this.em.find(Course.class, id);
            if(course == null) {
                return new CrxResponse("ERROR","Can not find course to delete.");
            }
            this.em.getTransaction().begin();
            this.em.remove(course);
            this.em.getTransaction().commit();
        } catch (Exception e){
            logger.error("delete" + e.getMessage());
            return new CrxResponse("ERROR",e.getMessage());
        }
        return new CrxResponse("OK","Course was deleted successfully.");
    }

    private boolean amIResponsible(Course course){
        for(CrxCalendar calendar: course.getAppointments()){
            if(calendar.getCreator().equals(session.getUser())){
                return true;
            }
        }
        return false;
    }

    public List<Course> getAll(){
        List<Course> courses = new ArrayList<>();
        Query query = this.em.createNamedQuery("Courses.findAll");
        for(Course course: (List<Course>)query.getResultList()){
            if(this.isSuperuser() || course.getCreator().equals(session.getUser()) || amIResponsible(course)){
                courses.add(course);
            }
        }
        return courses;
    }

    public Course getFreeAppointments(Long id, User user){
        List<CrxCalendar> appointments = new ArrayList<>();
        List<CrxCalendar> myAppointments = new ArrayList<>();
        Course course = this.em.find(Course.class, id);
        if(course != null){
            if(user != null) {
                for (CrxCalendar calendar : course.getAppointments()) {
                    for (User user1 : calendar.getUsers()) {
                        if (user.equals(user1)) {
                            myAppointments.add(calendar);
                        }
                    }
                }
            }
            if(myAppointments.size() < course.getCountOfRegistrations()) {
                for (CrxCalendar calendar : course.getAppointments()) {
                    if (
                            (calendar.getUsers().size() < course.getCountOfParticipants())
                                    && !calendar.overlapsWith(myAppointments)
                    ) {
                        appointments.add(calendar);
                    }
                }
            }
        }
        Course ret = new Course();
        ret.setTitle(course.getTitle());
        ret.setStartDate(course.getStartDate());
        ret.setEndDate(course.getEndDate());
        appointments.addAll(myAppointments);
        ret.setAppointments(appointments);
        return ret;
    }

    public CrxResponse register(Long appointment_id){
        return register(appointment_id, this.session.getUserId());
    }
    public CrxResponse register(Long appointment_id, Long userId){
        try {
            User user = this.em.find(User.class, userId);
            CrxCalendar appointment = this.em.find(CrxCalendar.class, appointment_id);
            if (appointment == null) {
                return new CrxResponse("ERROR","Can not find Appointment to register");
            }
            appointment.addUser(user);
            this.em.getTransaction().begin();
            this.em.merge(appointment);
            this.em.getTransaction().commit();
            lastChange.put(appointment.getCourseId(),new Date());
        }
        catch (Exception e){
            logger.error("register" + e.getMessage());
            return new CrxResponse("ERROR",e.getMessage());
        }
        return new CrxResponse("OK","Your registration was successfully.");
    }

    public CrxResponse deRegister(Long appointment_id){
        return deRegister(appointment_id, this.session.getUserId());
    }
    public CrxResponse deRegister(Long appointment_id, Long userId){
        try {
            User user = this.em.find(User.class, userId);
            CrxCalendar appointment = this.em.find(CrxCalendar.class, appointment_id);
            if (appointment == null) {
                return new CrxResponse("ERROR","Can not find Appointment to register");
            }
            appointment.getUsers().remove(user);
            this.em.getTransaction().begin();
            this.em.merge(appointment);
            this.em.getTransaction().commit();
            lastChange.put(appointment.getCourseId(),new Date());
        }
        catch (Exception e){
            logger.error("register" + e.getMessage());
            return new CrxResponse("ERROR",e.getMessage());
        }
        return new CrxResponse("OK","Your registration was cancelled successfully.");
    }

    public void sendNotification(Course course, User user, String dirName){
        SessionService sessionService = new SessionService(em);
        String gotoPath = "trusted/registerCourse/" + course.getId();
        Session courseSession = null;
        logger.debug("student: " + user.getUid());
        for (Session session1 : user.getSessions()) {
            if (session1.getGotoPath() != null && session1.getGotoPath().equals(gotoPath)) {
                courseSession = session1;
                break;
            }
        }
        if (courseSession == null) {
            courseSession = sessionService.createSession(
                    user,
                    course.getStartRegistration(),
                    course.getEndRegistration(),
                    gotoPath
            );
        }
        try {
            String template = Files.readString(Paths.get(cranixBaseDir + "templates/COURSES/Course.html"));
            String message = template
                    .replaceAll("#TOKEN#", courseSession.getToken())
                    .replaceAll("#TITLE#", course.getTitle())
                    .replaceAll("#SURNAME#", user.getSurName())
                    .replaceAll("#GIVENNAME#", user.getGivenName())
                    .replaceAll("#FROM#", course.getStartDate())
                    .replaceAll("#UNTIL#", course.getEndDate())
                    .replaceAll("#REGSART#", dateTimeFormat.format(course.getStartRegistration()))
                    .replaceAll("#REGEND#", dateTimeFormat.format(course.getEndRegistration()));
            String fileName = dirName + "/" + user.getUid();
	    logger.debug("fileName: "+ fileName);
            String mailAddress = user.getEmailAddress();
            Files.write(Paths.get(fileName), message.getBytes());
            Files.write(Paths.get(fileName + ".mailAddress"), mailAddress.getBytes());
            //Create subject
            String subjectTemplate = Files.readString(Paths.get(cranixBaseDir + "templates/COURSES/CourseSubject"));
            String subject = subjectTemplate.replaceAll("#TITLE#", course.getTitle());
            Files.write(Paths.get(fileName + ".subject"), subject.getBytes());
        }catch (Exception e){
            logger.error("sendNotification: " + e.getMessage());
        }
    }
    public CrxResponse sendNotifications(Long id) {
        Course course = this.em.find(Course.class, id);
        File dirName = new File(cranixTmpDir + "COURSES/" + course.getId()+ "/");
        try {
            Files.createDirectories(dirName.toPath(), privatDirAttribute);
            for (Group group : course.getGroups()) {
                for (User user : group.getUsers()) {
                        sendNotification(course, user, dirName.getPath());
                }
            }
            for (User user : course.getUsers()) {
                sendNotification(course, user, dirName.getPath());
            }
            Job sendMails = new Job(
                    "Send notification for the Course " + dateFormat.format(course.getStartDate()),
                    null,
                    cranixBaseDir + "tools/COURSES/send_mails " + dirName.getPath(),
                    true
            );
            new JobService(session, em).createJob(sendMails);
            return new CrxResponse("OK", "Sending notifications was started.");
        } catch (Exception e) {
            logger.error("sendNotifications:" + e.getMessage());
            return new CrxResponse("ERROR", e.getMessage());
        }
    }
    public CourseService(Session session, EntityManager em){
        super(session,em);
    }

    public Date getLastChange(Long id) {
        Course course = this.em.find(Course.class, id);
        if( course == null){
            return null;
        }
        if (!lastChange.containsKey(id)) {
            lastChange.put(id, new Date());
        }
        return lastChange.get(id);
    }

    public Course getById(Long courseId) {
        return this.em.find(Course.class, courseId);
    }

    public CrxResponse addAppointment(Long id, CrxCalendar appointment) {
        Course course = this.em.find(Course.class, id);
        course.getAppointments().add(appointment);
        try {
            this.em.getTransaction().begin();
            this.em.merge(course);
            this.em.getTransaction().commit();
        } catch (Exception e){
            return new CrxResponse("ERROR",e.getMessage());
        }
        return new CrxResponse("OK", "Appointment was created successfully");
    }

    public CrxResponse deleteAppointment(Long appointmenId) {
        CrxCalendar appointment = this.em.find(CrxCalendar.class, appointmenId);
        if(appointment == null) {
            return new CrxResponse("ERROR","Can not find Appointment");
        }
        try {
            Course course = appointment.getCourse();
            course.removeAppointment(appointment);
            this.em.getTransaction().begin();
            this.em.merge(course);
            this.em.getTransaction().commit();
        } catch (Exception e){
            return new CrxResponse("ERROR",e.getMessage());
        }
        return new CrxResponse("OK", "Appointment was deleted successfully");
    }
}
