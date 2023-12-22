package de.cranix.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Column;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;


public class Crx2fa extends AbstractEntity {


    /* The address where the auth code should be sent.
    * In case of SMS it is the telephone number
    */
    @Column(name = "qrcode", length = 2000)
    String qrcode = "";

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

    public void setSerial(String serial) {
        this.serial = serial;
    }
}
