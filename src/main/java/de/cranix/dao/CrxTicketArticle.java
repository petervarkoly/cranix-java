package de.cranix.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.cephalix.dao.CephalixTicket;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name="CrxTicketArticles")
@NamedQueries({
        @NamedQuery(name="CrxTicketArticle.findAll", query="SELECT t FROM CrxTicketArticle t")
})
public class CrxTicketArticle extends AbstractEntity{

    @ManyToOne
    @JsonIgnore
    @JoinColumn(
            name = "crxxticket_id",
            columnDefinition ="BIGINT UNSIGNED NOT NULL",
            nullable = false, updatable = false
    )
    private CrxTicket crxTicket;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name="seen", columnDefinition = "CHAR(1) DEFAULT 'N'")
    private Boolean seen;

    @Column(name="reminder")
    @Temporal(TemporalType.TIMESTAMP)
    private Date reminder;

    @Column(name="workTime")
    private Integer workTime;

    @Column(name="text", columnDefinition = "LONGTEXT")
    private String text;

    public CrxTicketArticle(){
        super();
    }
    public CrxTicketArticle(Session session){
        super(session);
    }

    public CrxTicket getCrxTicket() {
        return crxTicket;
    }

    public void setCrxTicket(CrxTicket crxTicket) {
        this.crxTicket = crxTicket;
    }

    public Boolean getSeen() {
        return seen;
    }

    public void setSeen(Boolean seen) {
        this.seen = seen;
    }

    public Date getReminder() {
        return reminder;
    }

    public void setReminder(Date reminder) {
        this.reminder = reminder;
    }

    public Integer getWorkTime() {
        return workTime;
    }

    public void setWorkTime(Integer workTime) {
        this.workTime = workTime;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
