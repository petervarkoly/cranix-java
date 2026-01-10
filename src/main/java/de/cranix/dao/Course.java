package de.cranix.dao;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static javax.persistence.TemporalType.TIMESTAMP;

@Entity
@Table(name = "Courses")
@NamedQueries({
        @NamedQuery(name = "Courses.findAll", query = "SELECT c FROM Courses c")
})
public class Course extends AbstractEntity {

    @Column(name = "description", length = 64)
    @Size(max=64, message="Description must not be longer then 64 characters.")
    private String description = "";

    @Column(name = "countOfParticipants")
    private Integer countOfParticipants = 5;

    @Column(name = "countOfRegistrations")
    private Integer countOfRegistrations = 1;

    @Column( name = "start", columnDefinition = "timestamp")
    @Temporal(TIMESTAMP)
    private Date start;

    @Column( name = "end", columnDefinition = "timestamp")
    @Temporal(TIMESTAMP)
    private Date end;

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

    @Convert(converter=BooleanToStringConverter.class)
	@Column(name = "released", columnDefinition = "CHAR(1) DEFAULT 'N'")
	private Boolean released = false;

    @ManyToMany()
    @JoinTable(
            name = "GroupsInCourses",
            joinColumns = {@JoinColumn(name = "course_id", columnDefinition = "BIGINT UNSIGNED NOT NULL")},
            inverseJoinColumns = {@JoinColumn(name = "group_id", columnDefinition = "BIGINT UNSIGNED NOT NULL")}
    )
    private List<Group> groups = new ArrayList<Group>();

    @ManyToMany()
    @JoinTable(
            name = "UsersInCourses",
            joinColumns = {@JoinColumn(name = "course_id", columnDefinition = "BIGINT UNSIGNED NOT NULL")},
            inverseJoinColumns = {@JoinColumn(name = "user_id", columnDefinition = "BIGINT UNSIGNED NOT NULL")}
    )
    private List<User> users = new ArrayList<User>();

    @OneToMany(cascade ={CascadeType.ALL}, orphanRemoval = true)
    @JoinTable(
            name = "AppointmentsInCourse",
            joinColumns = {@JoinColumn(name = "cours_id", columnDefinition ="BIGINT UNSIGNED NOT NULL")},
            inverseJoinColumns = {@JoinColumn(name = "crxcalendar_id", columnDefinition ="BIGINT UNSIGNED NOT NULL")}

    )
    private List<CrxCalendar> appointments = new ArrayList<>();

    public Course(){
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCountOfParticipants() {
        return countOfParticipants;
    }

    public void setCountOfParticipants(Integer countOfParticipants) {
        this.countOfParticipants = countOfParticipants;
    }

    public Integer getCountOfRegistrations() {
        return countOfRegistrations;
    }

    public void setCountOfRegistrations(Integer countOfRegistrations) {
        this.countOfRegistrations = countOfRegistrations;
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

    public Boolean getReleased() {
        return released;
    }

    public void setReleased(Boolean released) {
        this.released = released;
    }

    public List<CrxCalendar> getAppointments() {
        return appointments;
    }

    public void setAppointments(List<CrxCalendar> appointments) {
        this.appointments = appointments;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
