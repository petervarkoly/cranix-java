package de.cranix.dao;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;


public class Crx2fa extends AbstractEntity {

    // Type of the Crx2fa. At the moment only SMS and EMAIL is provided.
    @NotNull
    @Column(name = "type", length = 10)
    @Pattern(regexp = "^SMS|EMAIL$")
    @Enumerated(EnumType.STRING)
    String crx2faType = "SMS";

    /* The address where the auth code should be sent.
    * In case of SMS it is the telephone number
    */
    @Column(name = "address", length = 255)
    String crx2faAddress = "";

    /*
    * Who long is an authorization valid in munutes
    * */
    @Column(name = "valid")
    Integer validMinutes = 1440;

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

    public Integer getValidMinutes() {
        return validMinutes;
    }

    public void setValidMinutes(Integer validMinutes) {
        this.validMinutes = validMinutes;
    }
}
