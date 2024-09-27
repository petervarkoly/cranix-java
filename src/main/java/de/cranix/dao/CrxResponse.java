/* (c) 2024 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.dao;

import java.util.ArrayList;
import java.util.List;


public class CrxResponse {

    private String code;

    private String value;

    private List<String> parameters;

    private Long objectId;

    public CrxResponse() {
    }


    public CrxResponse(String code, String value) {
        this.code = code;
        this.value = value;
        this.parameters = new ArrayList<String>();
        this.objectId = null;
    }

    public CrxResponse(String code, String value, List<String> parameters) {
        this.code = code;
        this.value = value;
        this.parameters = parameters;
        this.objectId = null;
    }

    public CrxResponse(String code, String value, Long objectId) {
        this.code = code;
        this.value = value;
        this.parameters = new ArrayList<String>();
        this.objectId = objectId;
    }

    public CrxResponse(String code, String value, Long objectId, List<String> parameters) {
        this.code = code;
        this.value = value;
        this.parameters = parameters;
        this.objectId = objectId;
    }

    public CrxResponse(String code, String value, Long objectId, String parameter) {
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
