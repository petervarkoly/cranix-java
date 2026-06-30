package de.cranix.dao;
import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;
import static javax.persistence.TemporalType.TIMESTAMP;
@Entity
@Table(name="CrxNotices")
@NamedQueries({
        @NamedQuery(name="CrxNotice.findAll", query="SELECT n FROM CrxNotice n"),
        @NamedQuery(
                name="CrxNotice.getAllByObject",
                query="SELECT c FROM CrxNotice c WHERE c.objectType = :type AND c.objectId = :id"
        )
})
public class CrxNotice extends AbstractEntity{
    @Size(max=64, message="Title must not be longer then 64 characters.")
    @Column(name = "title", length = 64)
    private String title = "";

    /*
      At the moment we support following types of notices:
      performance: general notice
      grading: a grading notice
      late:
      absence: not excused absence
      excused-absence: exused absence
    */
    @Size(max=16, message="Notice type must not be longer then 16 characters.")
    @Column(name = "noticeType", length = 16)
    private String noticeType = "";

    @Convert(converter=BooleanToStringConverter.class)
	@Column(name = "private", columnDefinition = "CHAR(1) DEFAULT 'Y'")
	private Boolean isPrivate;

    @Column(name = "objectType")
    @Size(max = 12, message = "objectType must not be longer then 12 characters.")
    private String objectType = "";

    @Column(name = "objectId", columnDefinition ="BIGINT UNSIGNED")
    private Long objectId;

    @Column(name = "ptmId", columnDefinition ="BIGINT UNSIGNED")
    private Long ptmId;

    @Column(name = "text", columnDefinition = "TEXT")
    private String text = "";

    @Column(name = "grading")
    private Float grading = 0f;

    @Column(name = "weighting")
    private Float weighting = 1f;

    @Temporal(TIMESTAMP)
    @Column(name = "reminder")
    private Date reminder;

    // This variable contains the amount of minutes of the late
    @Column(name = "late")
    private Integer late;

    // This variable contains the start of absence or late: YYYY-MM-DD
    @Column(name = "absence1", length = 64)
    private String absence1;

    // This variable contains the end of absence as date or the lessen if absence is unexcused or the user is late
    @Column(name = "absence2", length = 64)
    private String absence2;

    @ManyToOne()
    @JoinColumn(
            name="teachingsubject_id",
            columnDefinition ="BIGINT UNSIGNED"
    )
    private TeachingSubject teachingSubject;

    @ManyToOne()
    @JoinColumn(
            name="subjectarea_id",
            columnDefinition ="BIGINT UNSIGNED"
    )
    private SubjectArea subjectArea;

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

    public Float getGrading() {return grading;}

    public void setGrading(Float grading) {this.grading = grading;}

    public Float getWeighting() {return weighting;}

    public void setWeighting(Float weighting) {this.weighting = weighting;}

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

    public TeachingSubject getTeachingSubject() {
        return teachingSubject;
    }

    public void setTeachingSubject(TeachingSubject teachingSubject) {
        this.teachingSubject = teachingSubject;
    }

    public SubjectArea getSubjectArea() {
        return subjectArea;
    }

    public void setSubjectArea(SubjectArea subjectArea) {
        this.subjectArea = subjectArea;
    }

    public Long getPtmId() {
        return ptmId;
    }

    public void setPtmId(Long ptmId) {
        this.ptmId = ptmId;
    }

    public String getAbsence1() {
        return absence1;
    }

    public void setAbsence1(String absence1) {
        this.absence1 = absence1;
    }


    public String getAbsence2() {
        return absence2;
    }

    public void setAbsence2(String absence2) {
        this.absence2 = absence2;
    }
}
