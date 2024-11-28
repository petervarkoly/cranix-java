package de.cranix.dao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.Size;

@Entity
@Table(name = "OneTimePasswords")
public class OneTimePassword extends AbstractEntity{
    @Column(name="otp", length = 16)
    @Size(max = 16, message = "OTP must not be longer then 16.")
    String otp = "";

    public OneTimePassword(){
        super();
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}
