package de.cranix.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "PTMEvents")
public class PTMEvent extends AbstractEntity {

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "ptmtir_id", columnDefinition = "BIGINT UNSIGNED NOT NULL")
    private PTMTeacherInRoom teacherInRoom;

    @Column(name = "start")
    private Date start;

    @Column(name = "end")
    private Date end;

    @ManyToOne
    @JoinColumn(name = "parent_id", columnDefinition = "BIGINT UNSIGNED")
    private User parent;

    @ManyToOne
    @JoinColumn(name = "student_id", columnDefinition = "BIGINT UNSIGNED")
    private User student;

    public PTMEvent() {
        super();
    }

    public PTMEvent(Session session) {
        super(session);
    }

    public PTMEvent(
            Session session,
            PTMTeacherInRoom teacherInRoom,
            Date start,
            Date end
    ) {
        super(session);
        this.teacherInRoom = teacherInRoom;
        this.start = start;
        this.end = end;
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

    public PTMTeacherInRoom getTeacherInRoom() {
        return teacherInRoom;
    }

    public void setTeacherInRoom(PTMTeacherInRoom teacherInRoom) {
        this.teacherInRoom = teacherInRoom;
    }

    public User getParent() {
        return parent;
    }

    public void setParent(User parent) {
        this.parent = parent;
    }

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }
}
