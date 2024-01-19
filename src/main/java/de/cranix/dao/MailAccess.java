package de.cranix.dao;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "MailAccess")
@NamedQueries({
        @NamedQuery(name="MailAccess.findAll", query="SELECT m FROM MailAccess m")
})public class MailAccess extends AbstractEntity{

    @Column(name="address", length=64, unique = true)
    @NotEmpty @NotNull
    String address = "";

    @Column(name="action", length = 10)
    @NotEmpty @NotNull
    String action = "";
    public MailAccess(){
        super();
    }
    public MailAccess(User user, String address, String action) {
        this.setCreator(user);
        this.address = address;
        this.action = action;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
