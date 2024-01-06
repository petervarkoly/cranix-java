package de.cranix.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
public class Crx2faSession extends AbstractEntity{

    /**
     * Who long is an authorization valid in hours
     */
    @Column(name = "validHours")
    Integer validHours = 24;

    /**
     * The created pin of the SMS or MAIL OTP
     */
    @Column(name="pin", length =  6)
    @Size(max=6)
    String pin;

    /**
     * The IP-Address of the client
     */
    @NotNull
    @NotEmpty
    @Column(name="clientIP",length = 128)
    @Size(max = 128)
    String clientIP;

    @ManyToOne
    @JsonIgnore
    Crx2fa myCrx2fa;

    public Crx2faSession(){}
    public Crx2faSession(User user, Crx2fa crx2fa, String clientIPAddress) {
        this.setCreator(user);
        this.setCreatorId(user.getId());
        this.myCrx2fa = crx2fa;
        this.clientIP = clientIPAddress;
        if(!this.myCrx2fa.getCrx2faType().equals("TOTP")) {
            int rand = new Random().nextInt(900000) + 100000;
            this.pin = String.valueOf(rand);
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
    @Transient
    public Date getValidUntil(){
        return new Date(this.getCreated().getTime() + this.validHours * 360000L);
    }
    @Transient
    public boolean isValid() {
        return (
                (this.getCreated().getTime() + this.validHours * 360000L) >
                        System.currentTimeMillis()
        );
    }
}
