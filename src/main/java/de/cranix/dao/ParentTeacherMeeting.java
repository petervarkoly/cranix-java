package de.cranix.dao;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static javax.persistence.TemporalType.TIMESTAMP;

@Entity
@Table(name = "ParentTeacherMeetings")
public class ParentTeacherMeeting extends AbstractEntity{

    @Column(
            name = "start",
            columnDefinition = "timestamp"
    )
    @Temporal(TIMESTAMP)
    private Date start = new Date();

    @Column(
            name = "end",
            columnDefinition = "timestamp"
    )
    @Temporal(TIMESTAMP)
    private Date end = new Date();

    @Column( name = "duration")
    private Integer duration = 10;

    @Column(
            name = "startRegistration",
            columnDefinition = "timestamp"
    )
    @Temporal(TIMESTAMP)
    private Date startRegistration = new Date();

    @Column(
            name = "endRegistration",
            columnDefinition = "timestamp"
    )
    @Temporal(TIMESTAMP)
    private Date endRegistration = new Date();

    @OneToMany(mappedBy = "parentTeacherMeeting", cascade = CascadeType.ALL)
    List<PTMTeacherInRoom> ptmTeacherInRoomList = new ArrayList<>();

    public ParentTeacherMeeting(Session session) { super(session);}

    public ParentTeacherMeeting(
            Session session,
            Date start,
            Date end,
            Integer duration,
            Date startRegistration,
            Date endRegistration
    ) {
        super(session);
        this.start =start;
        this.end = end;
        this.duration = duration;
        this.startRegistration = startRegistration;
        this.endRegistration = endRegistration;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Date getStartRegistration() {
        return startRegistration;
    }

    public void setStartRegistration(Date startRegistration) {
        this.startRegistration = startRegistration;
    }

    public Date getEndRegistration() {
        return endRegistration;
    }

    public void setEndRegistration(Date endRegistration) {
        this.endRegistration = endRegistration;
    }

    public List<PTMTeacherInRoom> getPtmTeacherInRoomList() {
        return ptmTeacherInRoomList;
    }

    public void setPtmTeacherInRoomList(List<PTMTeacherInRoom> ptmTeacherInRoomList) {
        this.ptmTeacherInRoomList = ptmTeacherInRoomList;
    }
}