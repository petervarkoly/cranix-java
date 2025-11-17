package de.cranix.dao;


import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.validation.constraints.Size;

@Entity
@Table(
        name="VirtualAliases",
        uniqueConstraints = { @UniqueConstraint(columnNames = { "user_id", "virtualAlias" }) }
)
@NamedQueries( {
        @NamedQuery(name="VirtualAlias.findAll",	query="SELECT a FROM VirualAlias a"),
        @NamedQuery(name="VirtualAlias.getByName",	query="SELECT a FROM VirualAlias a where a.alias = :alias"),
})
@SequenceGenerator(name="seq", initialValue=1, allocationSize=100)
public class VirtualAlias extends AbstractEntity {

    @Size(max=64, message="alias must not be longer then 64 characters.")
    @Column(name="virtualAlias", length=64)
    private String virtualAlias;

    @JsonIgnore
    @JoinColumn(name = "user_id", columnDefinition ="BIGINT UNSIGNED NOT NULL")
    private User user;

    public VirtualAlias() {
    }

    public VirtualAlias(Session session, User user, String virtualAlias) {
        this.creator = session.getUser();
        this.user  = user;
        this.virtualAlias = virtualAlias;
    }

    public String getVirtualAlias() {
        return this.virtualAlias;
    }

    public void setVirtualAlias(String virtualAlias) {
        this.virtualAlias = virtualAlias;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public int hashCode() {
        return virtualAlias.toLowerCase().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        VirtualAlias other = (VirtualAlias) obj;
        return  this.virtualAlias.toLowerCase().equals(other.getVirtualAlias().toLowerCase());
    }
}
