/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.dao;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * The persistent class for the Responses database table.
 */
@Entity
@Table(name = "Responses")
@NamedQuery(name = "CrxResponse.findAll", query = "SELECT r FROM CrxResponse r")
public class CrxResponse extends AbstractEntity {
    @ManyToOne
    Session session;

    /*
     * The error code for machine work
     */
    private String code;
    /*
     * Human readable code. Can contains '%s' as place holder.
     */
    private String value;
    /*
     * The values for the place holders.
     */
    @Transient
    private List<String> parameters;
    /*
     * This id will be set to the id of a object which was created or deleted or manipulated if any
     */
    private Long objectId;
    @Column(name = "session_id", insertable = false, updatable = false)
    private java.math.BigInteger sessionId;

    public CrxResponse() {
    }


    public CrxResponse(Session session, String code, String value) {
        this.session = session;
        this.code = code;
        this.value = value;
        this.parameters = new ArrayList<String>();
        this.objectId = null;
    }

    public CrxResponse(Session session, String code, String value, List<String> parameters) {
        this.session = session;
        this.code = code;
        this.value = value;
        this.parameters = parameters;
        this.objectId = null;
    }

    public CrxResponse(Session session, String code, String value, Long objectId) {
        this.session = session;
        this.code = code;
        this.value = value;
        this.parameters = new ArrayList<String>();
        this.objectId = objectId;
    }

    public CrxResponse(Session session, String code, String value, Long objectId, List<String> parameters) {
        this.session = session;
        this.code = code;
        this.value = value;
        this.parameters = parameters;
        this.objectId = objectId;
    }

    public CrxResponse(Session session, String code, String value, Long objectId, String parameter) {
        this.session = session;
        this.code = code;
        this.value = value;
        this.parameters = new ArrayList<String>();
        this.parameters.add(parameter);
        this.objectId = objectId;
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (Exception e) {
            return "{ \"ERROR\" : \"CAN NOT MAP THE OBJECT\" }";
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CrxResponse other = (CrxResponse) obj;
        if (id == null) {
			return other.id == null;
        } else return id.equals(other.id);
	}

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getObjectId() {
        return this.objectId;
    }

    public void setObjectId(Long id) {
        this.objectId = id;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public java.math.BigInteger getSessionId() {
        return this.sessionId;
    }

    public void setSessionId(java.math.BigInteger sessionId) {
        this.sessionId = sessionId;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }

}
