package de.cranix.dao;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;

@Entity
@Table(name="CrxNotices")
@NamedQueries({
        @NamedQuery(name="CrxNotice.findAll", query="SELECT n FROM CrxNotice n"),
        @NamedQuery(
                name="CrxNotice.getAllByObject",
                query="SELECT c FROM CrxNotice c WHERE c.objectType = :type AND c.objectId = :id"
        ),
        @NamedQuery(
                name="CrxNotice.getAllByObjectAndIssue",
                query="SELECT c FROM CrxNotice c WHERE c.objectType = :type AND c.objectId = :id AND c.issueType = :issueType"
        ),
})
public class CrxNotice extends AbstractEntity{
    @Size(max=64, message="Title must not be longer then 64 characters.")
    @Column(name = "title", length = 64)
    private String title = "";

    @Size(max=16, message="Notice type must not be longer then 12 characters.")
    @Column(name = "noticeType", length = 16)
    private String noticeType = "";

    @Column(name = "text", columnDefinition = "TEXT")
    private String text = "";

    @Column(name = "grading", columnDefinition="TINYINT UNSIGNED")
    private Integer grading = 0;

    @Column(name = "weighting", columnDefinition="TINYINT UNSIGNED")
    private Integer weighting = 1;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "reminder")
    private Date reminder;

    @Column(name = "objectType")
    @Size(max = 12, message = "objectType must not be longer then 12 characters.")
    private String objectType = "";

    @Column(name = "objectId", columnDefinition ="BIGINT UNSIGNED")
    private Long objectId;

    @Column(name = "issueType")
    @Size(max = 12, message = "issueType must not be longer then 12 characters.")
    private String issueType = "";

    @Column(name = "issueId", columnDefinition ="BIGINT UNSIGNED")
    private Long issueId;

    CrxNotice (){
        super();
    }

    CrxNotice(Session session){
        super(session);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNoticeType() {
        return noticeType;
    }

    public void setNoticeType(String noticeType) {
        this.noticeType = noticeType;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getGrading() {return grading;}

    public void setGrading(Integer grading) {this.grading = grading;}

    public Integer getWeighting() {return weighting;}

    public void setWeighting(Integer weighting) {this.weighting = weighting;}

    public Date getReminder() {
        return reminder;
    }

    public void setReminder(Date reminder) {
        this.reminder = reminder;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    public String getIssueType() {return issueType;}

    public void setIssueType(String issueType) {this.issueType = issueType;}

    public Long getIssueId() {return issueId;}

    public void setIssueId(Long issueId) {this.issueId = issueId;}
}
