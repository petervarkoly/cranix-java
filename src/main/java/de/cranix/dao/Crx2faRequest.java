package de.cranix.dao;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Crx2faRequest {
    private String uid ="";

    private Long userId;

    private Integer timeStep;

    private String regCode = "";

    private String address = "";

    private String serial = "";

    /**
     * The type of the crx2fa to be created:
     * TOTP
     * SMS send an SMS
     * MAIL send an e-mail
     */
    private String type = "";

    /**
     * The action which have to be executed.
     * This can be:
     * CREATE
     * DELETE
     * RESET
     */
    private String action = "";

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getTimeStep() {
        return timeStep;
    }

    public void setTimeStep(Integer timeStep) {
        this.timeStep = timeStep;
    }

    public String getRegCode() {
        return regCode;
    }

    public void setRegCode(String regCode) {
        this.regCode = regCode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (Exception e) {
            return "{ \"ERROR\" : \"CAN NOT MAP THE OBJECT\" }";
        }
    }
}
