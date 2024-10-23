package de.cranix.services;

import de.cranix.dao.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static de.cranix.helper.StaticHelpers.aMinusB;

public class CalendarService extends Service {

    Logger logger = LoggerFactory.getLogger(CalendarService.class);

    public CalendarService() {
        super();
    }

    public CalendarService(Session session, EntityManager em) {
        super(session, em);
    }

    public CrxResponse add(CrxCalendar event) {
        event.setCreator(session.getUser());
        event.setCreated(new Date());
        event.setUuid(UUID.randomUUID().toString().toUpperCase());
        logger.debug("CalendarService.add ", event);
        try {
            em.getTransaction().begin();
            em.persist(event);
            em.flush();
            em.getTransaction().commit();
            em.getEntityManagerFactory().getCache().evictAll();
            /*User user = em.find(User.class,session.getUserId());
            user.getCreatedEvents().add(event);
            em.merge(user);
            for (Long id : event.getUserIds()) {
                User user1 = em.find(User.class,id);
                user1.addEvent(event);
                em.merge(user1);
            }
            for (Long id : event.getGroupIds()) {
                Group group = em.find(Group.class,id);
                group.addEvent(event);
                em.merge(group);
            }*/
        } catch (Exception e) {
            logger.error("CalendarService.add error: ", e.getMessage());
            return new CrxResponse("ERROR", e.getMessage());
        }

        return new CrxResponse("OK", "Event was created successfully");
    }

    public CrxResponse delete(Long id) {
        try {
            CrxCalendar event = em.find(CrxCalendar.class, id);
            em.getTransaction();
            for (Long userId: event.getUserIds()) {
                User user = em.find(User.class,userId);
                user.removeEvent(event);
                em.merge(user);
            }
            for (Long groupId : event.getGroupIds()) {
                Group group = em.find(Group.class,groupId);
                group.removeEvent(event);
                em.merge(group);
            }
            em.remove(event);
            em.getTransaction().commit();
            return new CrxResponse("OK", "Event was removed successfully");
        } catch (Exception e) {
            return new CrxResponse("ERROR", e.getMessage());
        }
    }

    public CrxResponse modify(CrxCalendar event) {
        try {
            em.getTransaction().begin();
            CrxCalendar oldEvent = em.find(CrxCalendar.class, event.getId());
            oldEvent.setTitle(event.getTitle());
            oldEvent.setDescription(event.getDescription());
            oldEvent.setLocation(event.getLocation());
            oldEvent.setAllDay(event.getAllDay());
            oldEvent.setStartTime(event.getStartTime());
            oldEvent.setEndTime(event.getEndTime());
            oldEvent.setRruleFreq(event.getRruleFreq());
            oldEvent.setRruleInterval(event.getRruleInterval());
            oldEvent.setRruleUntil(event.getRruleUntil());
            //New user
            for (Long id : aMinusB(event.getUserIds(), oldEvent.getUserIds())) {
                User object = em.find(User.class, id);
                object.addEvent(oldEvent);
                em.merge(object);
            }
            //Old user
            for (Long id : aMinusB(oldEvent.getUserIds(), event.getUserIds())) {
                User object = em.find(User.class, id);
                object.removeEvent(oldEvent);
                em.merge(object);
            }
            //New group
            for (Long id : aMinusB(event.getGroupIds(), oldEvent.getGroupIds())) {
                Group object = em.find(Group.class, id);
                object.addEvent(oldEvent);
                em.merge(object);
            }
            //Old group
            for (Long id : aMinusB(oldEvent.getGroupIds(), event.getGroupIds())) {
                Group object = em.find(Group.class, id);
                object.removeEvent(oldEvent);
                em.merge(object);
            }
            em.merge(oldEvent);
            em.getTransaction().commit();
            return new CrxResponse("OK", "Event was modified successully");
        } catch (Exception e) {
            return new CrxResponse("ERROR", e.getMessage());
        }
    }

    public List<CrxCalendar> getAll() {
        List<CrxCalendar> myEvents = new ArrayList<>();
        User me = em.find(User.class, session.getUserId());
        for (CrxCalendar event : me.getEvents()) {
            if (!myEvents.contains(event)) {
                event.setColor(me.getColor());
                event.setCategory("individual");
                myEvents.add(event);
            }
        }
        for (Group group : me.getGroups()) {
            for (CrxCalendar event : group.getEvents()) {
                if (!myEvents.contains(event)) {
                    event.setColor(group.getColor());
                    event.setCategory(group.getName());
                    myEvents.add(event);
                }
            }
        }
        for(CrxCalendar event: me.getCreatedEvents()) {
            if (!myEvents.contains(event)) {
                event.setColor(me.getColor());
                event.setCategory("private");
                myEvents.add(event);
            }
        }
        return myEvents;
    }
}
