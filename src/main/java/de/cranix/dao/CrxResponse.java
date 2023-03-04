/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.dao;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.*;
import javax.validation.constraints.Size;
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
    @Column(name = "code")
    @Size(max = 64, message = "code must not be longer then 64 characters")
    private String code;
    /*
     * Human readable code. Can contains '%s' as place holder.
     */
    @Column(name = "value")
    @Size(max = 1024, message = "value must not be longer then 64 characters")
    private String value;
    /*
     * The values for the place holders.
     */
    @Transient
    private List<String> parameters;

    /*
     * This id will be set to the id of a object which was created or deleted or manipulated if any
     */
    @Transient
    private Long objectId;

    @Column(name = "session_id", insertable = false, updatable = false)
    private Long sessionId;

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

    public Long getSessionId() {
        return this.sessionId;
    }

    public void setSessionId(Long sessionId) {
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
