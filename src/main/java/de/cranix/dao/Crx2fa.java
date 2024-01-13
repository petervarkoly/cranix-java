package de.cranix.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.validation.constraints.Max;

import javax.validation.constraints.NotNull;
import java.util.List;


@Entity
@Table(name = "Crx2fas",
      uniqueConstraints = { @UniqueConstraint(columnNames = { "creator_id", "crx2faType" }) }
)
@NamedQueries({
        @NamedQuery(name="Crx2fa.findAll", query="SELECT c FROM Crx2fa c")
})
public class Crx2fa extends AbstractEntity {

    /**
     * Type of the Crx2fa:
     * This can be TOTP, SMS, EMAIL
     * At the moment only TOTP is provided.
     */
    @NotNull
    @Column(name = "crx2faType", length = 5)
    private String crx2faType = "TOTP";

    /** The address where the auth code should be sent.
    * In case of SMS it is the telephone number
    * In case of EMAIL it is a email-address
    * In case of TOTP it is a qrCode
    */
    @Column(name = "address", length = 2000)
    private String crx2faAddress = "";

    /**
     * The serial of the TOTP entry.
     */
    @Column(name="serial", length = 40)
    private String serial = "";

    /**
     * How long is a pin valid
     * By TOTP 30-60 seconds
     * By SMS or EMAIL 5-10 minutes
     * Value is in seconds
     */
    @Column(name = "timeStep")
    private Integer timeStep;
    /**
    * Who long is an authorization valid in hours
    */
    @Column(name = "validHours")
    @Max(value = 24, message = "A TOTP session must not be longer valid then 24.")
    private Integer validHours = 24;

    @OneToMany(mappedBy="myCrx2fa", cascade ={CascadeType.ALL}, orphanRemoval=true)
    @JsonIgnore
    List<Crx2faSession> crx2faSessionList;

    public String getCrx2faType() {
        return crx2faType;
    }

    public void setCrx2faType(String crx2faType) {
        this.crx2faType = crx2faType;
    }

    public String getCrx2faAddress() {
        return crx2faAddress;
    }

    public void setCrx2faAddress(String crx2faAddress) {
        this.crx2faAddress = crx2faAddress;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public Integer getTimeStep() {
        return timeStep;
    }

    public void setTimeStep(Integer timeStep) {
        this.timeStep = timeStep;
    }

    public Integer getValidHours() {
        return validHours;
    }

    public void setValidHours(Integer validHours) {
        this.validHours = validHours;
    }

    public List<Crx2faSession> getCrx2faSessionList() {
        return crx2faSessionList;
    }

    public void setCrx2faSessionList(List<Crx2faSession> crx2faSessionList) {
        this.crx2faSessionList = crx2faSessionList;
    }
}
