package de.cranix.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static javax.persistence.TemporalType.TIMESTAMP;

@Entity
@Table(
	name="CrxCalendar",
	uniqueConstraints = { @UniqueConstraint(columnNames = { "uuid" }) }
)
public class CrxCalendar extends AbstractEntity {

    @Column( name = "uuid", updatable = false)
    @Size(max = 40, message = "uuid must not be longer then 40 characters." )
    private String uuid = "";

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "allDay", columnDefinition = "CHAR(1) DEFAULT 'Y'")
    protected Boolean allDay = false;

    @Column(
            name = "startTime",
            columnDefinition = "timestamp DEFAULT CURRENT_TIMESTAMP"
    )
    @Temporal(TIMESTAMP)
    protected Date startTime = new Date();

    @Column(
            name = "endTime",
            columnDefinition = "timestamp DEFAULT CURRENT_TIMESTAMP"
    )
    @Temporal(TIMESTAMP)
    protected Date endTime = new Date();

    @Column(name = "title")
    @Size(max = 64, message = "title must not be longer then 64 characters.")
    protected String title = "";

    @Column(name = "description")
    @Size(max = 255, message = "description must not be longer then 255 characters.")
    protected String description = "";

    @Column(name = "location")
    @Size(max = 128, message = "description must not be longer then 128 characters.")
    protected String location = "";

    @Column(name = "rruleFreq")
    @Size(max = 16, message = "rruleFreq must not be longer then 16 characters.")
    protected String rruleFreq = "";

    @Column(name = "rruleInterval")
    @Size(max = 16, message = "rruleFreq must not be longer then 16 characters.")
    protected String rruleInterval = "";

    @Column(
            name = "rruleUntil",
            columnDefinition = "timestamp"
    )
    @Temporal(TIMESTAMP)
    protected Date rruleUntil = null;

    @ManyToMany()
    @JoinTable(
            name = "GroupEvents",
            joinColumns = {@JoinColumn(name = "event_id", columnDefinition = "BIGINT UNSIGNED NOT NULL")},
            inverseJoinColumns = {@JoinColumn(name = "group_id", columnDefinition = "BIGINT UNSIGNED NOT NULL")}
    )
    private List<Group> groups = new ArrayList<Group>();

    @ManyToMany()
    @JoinTable(
            name = "UserEvents",
            joinColumns = {@JoinColumn(name = "event_id", columnDefinition = "BIGINT UNSIGNED NOT NULL")},
            inverseJoinColumns = {@JoinColumn(name = "user_id", columnDefinition = "BIGINT UNSIGNED NOT NULL")}
    )
    private List<User> users = new ArrayList<User>();

    @Transient
    private String category = "";

    @Transient
    private String color = "";

    @Transient
    private List<Long> userIds = null;

    @Transient
    private List<Long> groupIds = null;

    public String getUuid() { return uuid; }

    public void setUuid(String uuid) { this.uuid = uuid; }

    public Boolean getAllDay() {
        return allDay;
    }

    public void setAllDay(Boolean allDay) {
        this.allDay = allDay;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() { return location; }

    public void setLocation(String location) { this.location = location; }

    public String getRruleFreq() {
        return rruleFreq;
    }

    public void setRruleFreq(String rruleFreq) {
        this.rruleFreq = rruleFreq;
    }

    public String getRruleInterval() {
        return rruleInterval;
    }

    public void setRruleInterval(String rruleInterval) {
        this.rruleInterval = rruleInterval;
    }

    public Date getRruleUntil() {
        return rruleUntil;
    }

    public void setRruleUntil(Date rruleUntil) {
        this.rruleUntil = rruleUntil;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public List<Long> getUserIds() {
        if (userIds == null) {
            userIds = new ArrayList<>();
            for (User user : users) {
                userIds.add(user.getId());
            }
        }
        return userIds;
    }

    public void setUserIds(List<Long> userIds) {
        this.userIds = userIds;
    }

    public List<Long> getGroupIds() {
        if (groupIds == null) {
            groupIds = new ArrayList<>();
            for (Group group : groups) {
                groupIds.add(group.getId());
            }
        }
        return groupIds;
    }

    public void setGroupIds(List<Long> groupIds) {
        this.groupIds = groupIds;
    }
}
