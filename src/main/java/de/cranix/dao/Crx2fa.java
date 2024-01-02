package de.cranix.dao;

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

    public String getSerial() { return serial; }

    public void setSerial(String serial) { this.serial = serial; }
}
