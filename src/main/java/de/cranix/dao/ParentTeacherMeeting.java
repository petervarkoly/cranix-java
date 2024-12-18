package de.cranix.dao;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static javax.persistence.TemporalType.TIMESTAMP;

@Entity
@Table(name = "ParentTeacherMeetings")
@NamedQueries({
        @NamedQuery(name = "PTMs.findAll", query = "SELECT p FROM ParentTeacherMeeting p"),
        @NamedQuery(name = "PTMs.findActual", query = "SELECT p FROM ParentTeacherMeeting p WHERE p.start > CURRENT_TIMESTAMP"),
        @NamedQuery(name = "PTMs.findFormer", query = "SELECT p FROM ParentTeacherMeeting p WHERE p.start < CURRENT_TIMESTAMP")
})
public class ParentTeacherMeeting extends AbstractEntity {

    @Column(name = "title", length=64)
    private String title = "";

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

    @Column(name = "duration")
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

    @ManyToMany()
    @JoinTable(
            name = "ClassInPtm",
            joinColumns = {@JoinColumn(name = "ptm_id", columnDefinition = "BIGINT UNSIGNED NOT NULL")},
            inverseJoinColumns = {@JoinColumn(name = "group_id", columnDefinition = "BIGINT UNSIGNED NOT NULL")}
    )
    private List<Group> classes = new ArrayList<Group>();

    @OneToMany(mappedBy = "parentTeacherMeeting", cascade = CascadeType.ALL)
    List<PTMTeacherInRoom> ptmTeacherInRoomList = new ArrayList<>();

    @Transient
    private Long templateId;

    public ParentTeacherMeeting() {
        super();
    }

    public ParentTeacherMeeting(Session session) {
        super(session);
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

    public String getTitle() {return title;}

    public void setTitle(String title) {this.title = title;}

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public List<Group> getClasses() { return classes; }

    public void setClasses(List<Group> classes) { this.classes = classes; }
}
