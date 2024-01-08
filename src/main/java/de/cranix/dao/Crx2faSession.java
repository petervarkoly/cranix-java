package de.cranix.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.cranix.services.Crx2faService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

@Entity
@Table(name = "Crx2faSessions")
@NamedQueries(value = {
        @NamedQuery(name = "Crx2faSessions.findAll", query = "SELECT c FROM Crx2faSession c")
})
public class Crx2faSession extends AbstractEntity {

    @Transient
    @JsonIgnore
    Logger logger = LoggerFactory.getLogger(Crx2faSession.class);
    /**
     * Who long is an authorization valid in hours
     */
    @Column(name = "validHours")
    private Integer validHours = 24;

    /**
     * The created pin of the SMS or MAIL OTP
     */
    @Column(name = "pin", length = 6)
    @Size(max = 6)
    private String pin;

    @Column(name = "checked", length = 1)
    @Convert(converter = BooleanToStringConverter.class)
    private Boolean checked;

    /**
     * The IP-Address of the client
     */
    @NotNull
    @NotEmpty
    @Column(name = "clientIP", length = 128)
    @Size(max = 128)
    private String clientIP;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "crx2fa_id")
    private Crx2fa myCrx2fa;

    public Crx2faSession() {
    }

    public Crx2faSession(User user, Crx2fa crx2fa, String clientIPAddress) {
        this.setCreator(user);
        this.setCreatorId(user.getId());
        this.myCrx2fa = crx2fa;
        this.clientIP = clientIPAddress;
        if (this.myCrx2fa.getCrx2faType().equals("TOTP")) {
            this.checked = true;
        } else {
            int rand = new Random().nextInt(900000) + 100000;
            this.pin = String.valueOf(rand);
            this.checked = false;
        }
        this.setCreated(new Date(System.currentTimeMillis()));
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getClientIP() {
        return clientIP;
    }

    public void setClientIP(String clientIP) {
        this.clientIP = clientIP;
    }

    public Crx2fa getMyCrx2fa() {
        return myCrx2fa;
    }

    public void setMyCrx2fa(Crx2fa myCrx2fa) {
        this.myCrx2fa = myCrx2fa;
    }

    public Integer getValidHours() {
        return validHours;
    }

    public void setValidHours(Integer validHours) {
        this.validHours = validHours;
    }

    public Boolean getChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }

    @Transient
    public Date getValidUntil() {
        return new Date(this.getCreated().getTime() + this.validHours * 3600_000L);
    }

    @Transient
    public boolean isValid() {
        return (
                (this.getCreated().getTime() + this.validHours * 3600_000L) >
                        System.currentTimeMillis()
        );
    }

    /**
     * isAvailable means the sent pin is valid. The time is in timeStep.
     *
     * @return
     */
    @Transient
    public boolean isAvailable() {
        long a = this.getCreated().getTime() + this.getMyCrx2fa().getTimeStep() * 1000L;
        long b = System.currentTimeMillis();
        String value = a>b ? "true" : "false";

        logger.debug("isAvailable: " + this.getCreated().getTime() + "#" + this.getMyCrx2fa().getTimeStep() * 1000L + "#" + b);
        logger.debug("isAvailable: " +a+ "#" +b+"#" + value);
        return (
                (this.getCreated().getTime() + this.getMyCrx2fa().getTimeStep() * 1000L) >
                        System.currentTimeMillis()
        );
    }
}
