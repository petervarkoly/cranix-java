package de.cranix.services;

import de.cranix.dao.*;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static de.cranix.helper.StaticHelpers.aMinusB;

public class CalendarService extends Service {

    Logger logger = LoggerFactory.getLogger(CalendarService.class);
    String timeZone = ";TZID=Europe/Berlin:";
    String calendarPath = "/var/lib/radicale/collections/collection-root/";
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");

    public CalendarService() {
        super();
    }

    public CalendarService(Session session, EntityManager em) {
        super(session, em);
    }

    public CrxCalendar getById(Long id) {
        try {
            return this.em.find(CrxCalendar.class, id);
        } catch (Exception e) {
            logger.debug("CalendarService getByID: " + e.getMessage());
            return null;
        }
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
        exportEvent(event);
        return new CrxResponse("OK", "Event was created successfully");
    }

    public CrxResponse delete(Long id) {
        try {
            CrxCalendar event = em.find(CrxCalendar.class, id);
            removeEvent(event);
            em.getTransaction().begin();
            /* for (Long userId: event.getUserIds()) {
                User user = em.find(User.class,userId);
                user.removeEvent(event);
                em.merge(user);
            }
            for (Long groupId : event.getGroupIds()) {
                Group group = em.find(Group.class,groupId);
                group.removeEvent(event);
                em.merge(group);
            }
            if( event.getRoom() != null ){
                event.getRoom().removeEvent(event);
                em.merge(event.getRoom());
            }*/
            em.remove(event);
            em.getTransaction().commit();
            em.getEntityManagerFactory().getCache().evictAll();
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
            oldEvent.setStart(event.getStart());
            oldEvent.setEnd(event.getEnd());
            oldEvent.setRrule(event.getRrule());
            oldEvent.setModified(new Date());
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
            if (oldEvent.getRoom() == null && event.getRoom() != null) {
                oldEvent.setRoom(event.getRoom());
            } else if (oldEvent.getRoom() != null && !oldEvent.getRoom().equals(event.getRoom())) {
                oldEvent.getRoom().removeEvent(event);
                em.merge(oldEvent.getRoom());
                oldEvent.setRoom(event.getRoom());
            }
            em.merge(oldEvent);
            em.getTransaction().commit();
            exportEvent(event);
            return new CrxResponse("OK", "Event was modified successully");
        } catch (Exception e) {
            logger.error("modify:" + e.getMessage());
            return new CrxResponse("ERROR", e.getMessage());
        }
    }

    public List<CrxCalendar> getMyAll() {
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
        for (CrxCalendar event : me.getCreatedEvents()) {
            if (!myEvents.contains(event)) {
                event.setColor(me.getColor());
                event.setCategory("private");
                myEvents.add(event);
            }
        }
        return myEvents;
    }

    public List<CrxCalendar> getAll() {
        try {
            Query query = this.em.createNamedQuery("Group.findAll");
            return query.getResultList();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public CrxResponse exportCalendar() {
        File calendarDir = new File(calendarPath);
        if (!calendarDir.isDirectory()) {
            return new CrxResponse("ERROR", "Calendar can not be exported. Install cranix-calendar!");
        }
        for (CrxCalendar event : this.getAll()) {
            exportEvent(event);
        }
        return new CrxResponse("OK", "Calendar was exported");
    }

    private void removeEvent(CrxCalendar event) {
        File calendarDir = new File(calendarPath);
        if (!calendarDir.isDirectory()) {
            return;
        }
        for (Group group : event.getGroups()) {
            File myFile = new File(
                    calendarPath + "GROUPS/" + Base64.getEncoder().encodeToString(group.getName().getBytes()) +
                            "/" + event.getUuid() + ".ics");
            myFile.delete();
        }
        for (User user : event.getUsers()) {
            File myFile = new File(calendarPath + user.getUid() + "/" + event.getUuid() + ".ics");
            myFile.delete();
        }
        if (event.getCreator() != null) {
            File myFile = new File(calendarPath + event.getCreator().getUid() + "/" + event.getUuid() + ".ics");
            myFile.delete();
        }
    }

    public void exportEvent(CrxCalendar event) {
        File calendarDir = new File(calendarPath);
        if (!calendarDir.isDirectory()) {
            return;
        }
        List<String> entry = new ArrayList<>();
        entry.add("BEGIN:VCALENDAR");
        entry.add("VERSION:2.0");
        entry.add("PRODID:CRANIX-RADICAL-Calendar");
        entry.add("PUBLISH");
        entry.add("BEGIN:VEVENT");
        entry.add("UID:" + event.getUuid());
        if (event.getRoom() != null) {
            entry.add("LOCATION:" + event.getRoom().getDescription());
        }
        entry.add("SUMMARY:" + event.getTitle());
        if (event.getDescription() != null) {
            entry.add("DESCRIPTION:" + event.getDescription());
        }
        if (event.getRrule() == null || event.getRrule().isEmpty()) {
            entry.add("DTSTART" + timeZone + simpleDateFormat.format(event.getStart()));
        } else {
            entry.add(event.getRrule());
        }
        entry.add("DTEND" + timeZone + simpleDateFormat.format(event.getEnd()));
        entry.add("DTSTAMP:" + simpleDateFormat.format(event.getCreated()));
        entry.add("END:VEVENT");
        entry.add("END:VCALENDAR");

        for (Group group : event.getGroups()) {
            try {
                FileWriter myWriter = new FileWriter(
                        calendarPath + "GROUPS/" + Base64.getEncoder().encodeToString(group.getName().getBytes()) +
                                "/" + event.getUuid() + ".ics");
                entry.set(5, "UID:" + event.getUuid() + '-' + group.getName());
                for (String string : entry) {
                    myWriter.write(string);
                    myWriter.write("\r\n");
                }
                myWriter.close();
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
        for (User user : event.getUsers()) {
            try {
                FileWriter myWriter = new FileWriter(
                        calendarPath + user.getUid() + "/" + event.getUuid() + ".ics");
                entry.set(5, "UID:" + event.getUuid() + '-' + user.getUid());
                for (String string : entry) {
                    myWriter.write(string);
                    myWriter.write("\r\n");
                }
                myWriter.close();
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
        if (event.getCreator() != null) {
            try {
                FileWriter myWriter = new FileWriter(
                        calendarPath + event.getCreator().getUid() + "/" + event.getUuid() + ".ics");
                for (String string : entry) {
                    myWriter.write(string);
                    myWriter.write("\r\n");
                }
                myWriter.close();
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
    }

    public List<CrxCalendar> getMyFiltered(FilterObject map) {
        List<CrxCalendar> events = new ArrayList<>();
        for (CrxCalendar event : getMyAll()) {
            if (map.isShowPrivate() && event.getCategory().equals("private")) {
                events.add(event);
                continue;
            }
            if (map.isShowIndividual() && event.getCategory().equals("individual")) {
                events.add(event);
                continue;
            }
            if (map.getRooms().contains(event.getRoom())) {
                events.add(event);
                continue;
            }
            for (Group group : map.getGroups()) {
                if (event.getGroups().contains(group)) {
                    events.add(event);
                    break;
                }
            }
        }
        return events;
    }

    static String courses ="08:00:08:45#" +
            "09:00:09:45#" +
            "10:00:10:45#" +
            "11:00:11:45#" +
            "12:00:12:45#" +
            "13:00:13:45#" +
            "14:00:14:45#" +
            "15:00:15:45#" +
            "16:00:16:45#" +
            "17:00:17:45#" +
            "18:00:18:45#" +
            "19:00:19:45";

    public CrxResponse importTimetable(InputStream fileInputStream, String start, String end) {
        RoomService roomService = new RoomService(this.session, this.em);
        UserService userService = new UserService(this.session, this.em);
        GroupService groupService = new GroupService(this.session, this.em);
        DateFormat df = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        List<String[]> hours = new ArrayList<>();
        for(String line: courses.split("#")) {
            logger.debug(line);
            hours.add(line.split(":"));
        }
        logger.debug("Hours: " + hours.get(0) + hours.get(1));
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                CrxCalendar event = new CrxCalendar();
                event.setUuid(UUID.randomUUID().toString().toUpperCase());
                event.setTitle(fields[3]);
                User user = userService.getByUid(fields[2]);
                if( user != null ) event.getUsers().add(user);
                Group group = groupService.getByName(fields[1]);
                if( group != null ) event.getGroups().add(group);
                Room room = roomService.getByName(fields[4]);
                if( room != null ) event.setRoom(room);
                event.setLocation(fields[4]);
                event.setDescription(fields[3] + " in " + fields[4] + " " + fields[2]);
                logger.debug("Fields: " + fields);
                Integer lesson = Integer.parseInt(fields[6]);
                StringBuilder rrule = new StringBuilder();
                rrule.append("DTSTART:").append(start).append("\\n");
                rrule.append("RRULE:FREQ=WEEKLY;INTERVAL=1;WKST=MO;UNTIL=").append(end)
                        .append("BYDAY").append(fields[5])
                        .append("BYHOUR").append(hours.get(lesson)[0])
                        .append("BYMINUTE").append(hours.get(lesson)[1])
                        .append("BYSECOND=0");
                event.setRrule(rrule.toString());
                try {
                    Date startDate = df.parse(start.substring(0,11)+hours.get(lesson)[0]+hours.get(lesson)[1]+"00Z");
                    Date endDate = df.parse(start.substring(0,11)+hours.get(lesson)[2]+hours.get(lesson)[3]+"00Z");
                    event.setStart(startDate);
                    event.setEnd(endDate);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                this.em.getTransaction().begin();
                this.em.persist(event);
                this.em.getTransaction().commit();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.em.getEntityManagerFactory().getCache().evictAll();
        return new CrxResponse("OK", "Timetable was imported successfully.");
    }
}
