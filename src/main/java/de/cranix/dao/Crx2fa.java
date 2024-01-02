package de.cranix.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.validation.constraints.Max;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;


public class Crx2fa extends AbstractEntity {

    /*
     Type of the Crx2fa:
     This can be TOTP, SMS, EMAIL
     At the moment only TOTP is provided.
     */
    @NotNull
    @Column(name = "type", length = 5)
    String crx2faType = "TOTP";

    /* The address where the auth code should be sent.
    * In case of SMS it is the telephone number
    * In case of EMAIL it is a email-address
    * In case of TOTP it is a qrCode
    */
    @Column(name = "address", length = 2000)
    String crx2faAddress = "";

    @Column(name="serial", length = 32)
    String serial ="";

    /*
    * Who long is an authorization valid in minutes
    * */
    @Column(name = "valid")
    @Max(value = 24, message = "A TOTP session must not be longer valid then 24.")
    Integer validHours = 24;

    @NotNull
    @Column(name = "serial", length = 40)
    String serial;

    public Integer getValidHours() {
        return validHours;
    }

    public void setValidHours(Integer validHours) {
        this.validHours = validHours;
    }
    public String getCrqode() {
        return qrcode;
    }

    public void setCrqode(String crqode) {
        this.qrcode = crqode;
    }

    public String getSerial() {
        return serial;
    }

    public String getSerial() { return serial; }

    public void setSerial(String serial) { this.serial = serial; }
}
