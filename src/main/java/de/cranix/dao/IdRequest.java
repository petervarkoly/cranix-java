package de.cranix.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.UUID;

import static javax.persistence.TemporalType.TIMESTAMP;

@Entity
@Table(
        name = "IdRequests",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"uuid"})}
)
@NamedQueries({
        @NamedQuery(name = "IdRequests.findAll", query = "SELECT r FROM IdRequest r")
})
public class IdRequest extends AbstractEntity {

    @Column(name = "uuid", updatable = false, length = 64)
    @Size(max = 64, message = "UUID must not be longer then 64 characters.")
    private String uuid = UUID.randomUUID().toString();

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "allowed", columnDefinition = "CHAR(1) DEFAULT 'N'")
    private Boolean allowed = false;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "validUntil", columnDefinition = "timestamp")
    private Date validUntil;

    @OneToOne
    @JoinColumn(name = "user_id", columnDefinition = "BIGINT UNSIGNED")
    private User requester;

    @Transient
    private byte[] picture;

    public IdRequest() { super(); }

    public IdRequest(Session session) {
        super();
        this.setRequester(session.getUser());
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Boolean getAllowed() {
        return allowed;
    }

    public void setAllowed(Session session, Boolean allowed, Date validUntil) {
        setModified(new Date());
        this.creator = session.getUser();
        this.allowed = allowed;
        this.validUntil= validUntil;
    }

    public User getRequester() {
        return requester;
    }

    public void setRequester(User requester) {
        this.requester = requester;
    }

    public byte[] getPicture() {
        return picture;
    }

    public void setPicture(byte[] picture) {
        this.picture = picture;
    }

    public void setAllowed(Boolean allowed) {
        this.allowed = allowed;
    }

    public Date getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
    }

    public Long getReviewerId(){
        return getCreatorId();
    }
}
