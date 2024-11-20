package de.cranix.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "PTMEvents")
public class PTMEvent extends AbstractEntity {

    @ManyToOne
    @JoinColumn(name = "ptmtir_id", columnDefinition = "BIGINT UNSIGNED NOT NULL")
    private PTMTeacherInRoom teacherInRoom;

    @ManyToOne
    @JoinColumn(name = "parent_id", columnDefinition = "BIGINT UNSIGNED NOT NULL")
    private Parent parent;

    @Column(name = "start")
    private Date start;

    @Column(name = "end")
    private Date end;

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

    public Parent getParent() {
        return parent;
    }

    public void setParent(Parent parent) {
        this.parent = parent;
    }
}
