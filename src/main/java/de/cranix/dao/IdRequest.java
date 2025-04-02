package de.cranix.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

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
public class IdRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(
            name = "id",
            columnDefinition ="BIGINT UNSIGNED NOT NULL AUTO_INCREMENT"
    )
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(
            name="creator_id",
            columnDefinition ="BIGINT UNSIGNED"
    )
    private User creator;

    @Column(
            name = "created",
            updatable = false,
            columnDefinition = "timestamp DEFAULT CURRENT_TIMESTAMP"
    )
    @Temporal(TIMESTAMP)
    private Date created = new Date();

    @Column(
            name = "modified",
            columnDefinition = "timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"
    )
    @Temporal(TIMESTAMP)
    private Date modified = new Date();

    @Column(name = "uuid", updatable = false, length = 64)
    @Size(max = 64, message = "UUID must not be longer then 64 characters.")
    private String uuid = UUID.randomUUID().toString();

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "allowed", columnDefinition = "CHAR(1) DEFAULT 'N'")
    private Boolean allowed = false;

    @Column(name = "comment", length = 128)
    @Size(max = 128, message = "Comment must not be longer then 128 characters")
    private String comment = "";

    @Column(name = "validUntil", columnDefinition = "date")
    private String validUntil;

    @Column(name = "reviwerId", columnDefinition =  "BIGINT UNSIGNED")
    private Long reviewerId;

    @Column(name = "avatar", columnDefinition = "text")
    private String avatar;

    @Column(name = "appleUrl", length = 255)
    String appleUrl;

    @Column(name = "googleUrl", columnDefinition = "text")
    String googleUrl;

    @Transient
    private String picture;

    public IdRequest() { }

    public IdRequest(Session session) {
        this.creator = session.getUser();
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (Exception e) {
            return "{ \"ERROR\" : \"CAN NOT MAP THE OBJECT\" }";
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        IdRequest other = (IdRequest) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
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
            String validUntil
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

    public String getValidUntil() { return validUntil; }
    public void setValidUntil(String validUntil) { this.validUntil = validUntil; }

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

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }
}