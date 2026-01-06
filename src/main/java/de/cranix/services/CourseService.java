package de.cranix.services;

import de.cranix.dao.Course;
import de.cranix.dao.CrxCalendar;
import de.cranix.dao.CrxResponse;
import de.cranix.dao.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

public class CourseService extends Service {

    Logger logger = LoggerFactory.getLogger(CourseService.class);

    public CrxResponse add(Course course){
        try {
            course.setCreator(this.session.getUser());
            this.em.getTransaction().begin();
            this.em.persist(course);
            this.em.getTransaction().commit();
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
                return new CrxResponse("ERROR","Can not find course to delete.")
            }
            this.em.getTransaction().begin();
            this.em.remove(course);
            this.em.getTransaction().commit();
        }catch (Exception e){
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
    
    public List<CrxCalendar> getFreeAppointments(Long id){
        List<CrxCalendar> appointments = new ArrayList<>();
        Course course = this.em.find(Course.class, id);
        if(course != null){
            for(CrxCalendar calendar: course.getAppointments()){
                if(calendar.getUsers().size() < course.getCountOfParticipants()){
                    appointments.add(calendar)
                }
            }
        }
        return appointments;
    }

    public CrxResponse register(Long appointment_id){
        try {
            CrxCalendar appointment = this.em.find(CrxCalendar.class, appointment_id);
            if (appointment == null) {
                return new CrxResponse("ERROR","Can not find Appointment to register");
            }
            appointment.addUser(this.session.getUser());
            this.em.getTransaction().begin();
            this.em.merge(appointment);
            this.em.getTransaction().commit();
        }
        catch (Exception e){
            logger.error("register" + e.getMessage());
            return new CrxResponse("ERROR",e.getMessage());
        }
        return new CrxResponse("OK","Your registration was successfully.");
    }

    public CourseService(Session session, EntityManager em){
        super(session,em);
    }
}
