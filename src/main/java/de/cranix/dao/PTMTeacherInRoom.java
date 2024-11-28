package de.cranix.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="PTMTeacherInRoom")
public class PTMTeacherInRoom extends AbstractEntity{

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "ptm_id", columnDefinition ="BIGINT UNSIGNED NOT NULL")
    private ParentTeacherMeeting parentTeacherMeeting;

    @ManyToOne
    @JoinColumn(name = "room_id", columnDefinition ="BIGINT UNSIGNED NOT NULL")
    private Room room;

    @ManyToOne
    @JoinColumn(name = "teacher_id", columnDefinition ="BIGINT UNSIGNED NOT NULL")
    private User teacher;

    @OneToMany(mappedBy = "teacherInRoom", cascade = {CascadeType.ALL})
    private List<PTMEvent> events = new ArrayList<>();

    public PTMTeacherInRoom(){
        super();
    }

    public PTMTeacherInRoom(Session session){
        super(session);
    }

    public PTMTeacherInRoom(
            Session session,
            User teacher,
            Room room,
            ParentTeacherMeeting parentTeacherMeeting
    ){
        super(session);
        this.teacher = teacher;
        this.room = room;
        this.parentTeacherMeeting = parentTeacherMeeting;
        Date eventStart = parentTeacherMeeting.getStart();
        Date eventEnd;
        while (eventStart.before(parentTeacherMeeting.getEnd())){
            eventEnd = new Date(eventStart.getTime() +  parentTeacherMeeting.getDuration() * 60000 - 1000);
            this.events.add(new PTMEvent(session,this,eventStart,eventEnd));
            eventStart = new Date(eventEnd.getTime()+ 1000);
        }
    }

    public ParentTeacherMeeting getParentTeacherMeeting() {
        return parentTeacherMeeting;
    }

    public void setParentTeacherMeeting(ParentTeacherMeeting parentTeacherMeeting) {
        this.parentTeacherMeeting = parentTeacherMeeting;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public User getTeacher() {
        return teacher;
    }

    public void setTeacher(User teacher) {
        this.teacher = teacher;
    }

    public List<PTMEvent> getEvents() {
        return events;
    }

    public void setEvents(List<PTMEvent> events) {
        this.events = events;
    }
}
