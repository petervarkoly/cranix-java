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

    @Column(name = "comment", length = 128)
    @Size(max = 128, message = "Comment must not be longer then 128 characters")
    private String comment = "";

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "validUntil", columnDefinition = "timestamp")
    private Date validUntil;

    @Column(name = "reviwerId", columnDefinition =  "BIGINT UNSIGNED")
    private Long reviewerId;

    @Column(name = "avatar", columnDefinition = "text")
    private String avatar;

    @Transient
    String appleUrl;

    @Transient
    String googleUrl;

    @Transient
    private String picture;

    public IdRequest() { super(); }

    public IdRequest(Session session) {
        super(session);
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
    public void setAllowed(Boolean allowed) {
        this.allowed = allowed;
    }
    public void setAllowed(
            Session session,
            Boolean allowed,
            String comment,
            Date validUntil
    ) {
        setModified(new Date());
        this.reviewerId = session.getUserId();
        this.allowed = allowed;
        this.validUntil= validUntil;
        this.comment = comment.length() > 128 ? comment.substring(0,128): comment;
    }

    public Long getReviewerId() {return reviewerId;}
    public void setReviewerId(Long reviewerId) {this.reviewerId = reviewerId; }

    public String getPicture() {
        return picture;
    }
    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getComment() {return  comment; }
    public void setComment(String comment) {
        if(comment != null) {
            this.comment = comment.length() > 128 ? comment.substring(0,128): comment;
        }
    }

    public Date getValidUntil() { return validUntil; }
    public void setValidUntil(Date validUntil) { this.validUntil = validUntil; }

    public String getAppleUrl() { return appleUrl; }
    public void setAppleUrl(String appleUrl) { this.appleUrl = appleUrl; }

    public String getGoogleUrl() { return googleUrl; }
    public void setGoogleUrl(String googleUrl) { this.googleUrl = googleUrl; }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}