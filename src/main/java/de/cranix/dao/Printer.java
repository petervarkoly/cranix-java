/* (c) 2024 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.dao;

import java.io.Serializable;
import java.util.List;

import javax.persistence.*;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(
        name = "Printers",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"name"})}
)
@NamedQueries({
        @NamedQuery(name = "Printer.findAll", query = "SELECT p FROM Printer p"),
        @NamedQuery(name = "Printer.findAllId", query = "SELECT p.id FROM Printer p"),
        @NamedQuery(name = "Printer.getByName", query = "SELECT p FROM Printer p WHERE p.name = :name")
})
@SequenceGenerator(name = "seq", initialValue = 1, allocationSize = 100)
public class Printer extends AbstractEntity {

    @Column(name = "name")
    @Size(max = 32, message = "name must not be longer then 32 characters.")
    private String name;

    //bi-directional many-to-one association to HWConf
    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "device_id", columnDefinition = "BIGINT UNSIGNED")
    private Device device;

    // Transient variable:
    @Transient
    private String manufacturer;

    @Transient
    private String model;

    @Transient
    private String mac;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Transient
    private String ip;

    @Transient
    private Long roomId;

    @Transient
    private String deviceName;

    @Transient
    private boolean windowsDriver;

    /*
     * State variables
     */
    @Transient
    private String state;

    @Transient
    private boolean acceptingJobs;

    @Transient
    private int activeJobs;

    public Printer() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean isAcceptingJobs() {
        return acceptingJobs;
    }

    public void setAcceptingJobs(boolean acceptingJobs) {
        this.acceptingJobs = acceptingJobs;
    }

    public int getActiveJobs() {
        return activeJobs;
    }

    public void setActiveJobs(int activeJobs) {
        this.activeJobs = activeJobs;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public boolean isWindowsDriver() {
        return windowsDriver;
    }

    public void setWindowsDriver(boolean windowsDriver) {
        this.windowsDriver = windowsDriver;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}
