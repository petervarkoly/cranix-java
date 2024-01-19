/* (c) Peter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.*;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * The persistent class for the Devices database table.
 */
@Entity
@Table(
	name = "Devices",
	uniqueConstraints = {
		@UniqueConstraint(columnNames = { "name" }),
		@UniqueConstraint(columnNames = { "IP" })
	}
)
@NamedQueries({
        @NamedQuery(name = "Device.findAll", query = "SELECT d FROM Device d"),
        @NamedQuery(name = "Device.findAllId", query = "SELECT d.id FROM Device d"),
        @NamedQuery(name = "Device.getByIP", query = "SELECT d FROM Device d where d.ip = :IP OR d.wlanIp = :IP"),
        @NamedQuery(name = "Device.getByMainIP", query = "SELECT d FROM Device d where d.ip = :IP"),
        @NamedQuery(name = "Device.getByMAC", query = "SELECT d FROM Device d where d.mac = :MAC OR d.wlanMac = :MAC"),
        @NamedQuery(name = "Device.getByName", query = "SELECT d FROM Device d where d.name = :name"),
        @NamedQuery(name = "Device.search", query = "SELECT d FROM Device d where d.name LIKE :search OR d.ip LIKE :search OR d.wlanIp LIKE :search OR d.mac LIKE :search OR d.wlanMac LIKE :search"),
})
@SequenceGenerator(name = "seq", initialValue = 1, allocationSize = 100)
public class Device extends AbstractEntity {

    @Column(name = "name", updatable = false, length = 32)
    @Size(max = 32, message = "name must not be longer then 32 characters.")
    @Pattern.List({
		/*@Pattern(
                 regexp = "^[^,~:@#$%\\^'\\.\\(\\)/\\\\\\{\\}_\\s\\*\\?<>\\|]+$",
                 flags = Pattern.Flag.CASE_INSENSITIVE,
                 message = "Device name must not contains following signs: ',~:$%^/\\.(){}#;_' and spaces."),*/
            @Pattern(
                    regexp = "[a-z0-9-].*",
                    flags = Pattern.Flag.CASE_INSENSITIVE,
                    message = "Device name contains invalid characters. Use only a-z0-9-"),
            @Pattern(
                    regexp = "^[^-].*",
                    flags = Pattern.Flag.CASE_INSENSITIVE,
                    message = "Device name must not start with '-'."),
            @Pattern(
                    regexp = ".*[^-]$",
                    flags = Pattern.Flag.CASE_INSENSITIVE,
                    message = "Device name must not ends with '-'.")
    })
    private String name;

    @Column(name = "place")
    private Integer place;

    @Column(name = "roomRow")
    private Integer row;

    @Column(name = "IP", length = 16)
    @Size(max = 16, message = "IP must not be longer then 16 characters.")
    private String ip;

    @Column(name = "MAC", length = 17)
    @Size(max = 17, message = "MAC must not be longer then 17 characters.")
    private String mac;

    @Column(name = "wlanIp", length = 16)
    @Size(max = 16, message = "WLAN-IP must not be longer then 16 characters.")
    private String wlanIp;

    @Column(name = "wlanMac", length = 17)
    @Size(max = 17, message = "WLAN-MAC must not be longer then 17 characters.")
    private String wlanMac;

    @Column(name = "serial", length = 32)
    @Size(max = 32, message = "Serial must not be longer then 32 characters.")
    private String serial;

    @Column(name = "inventary", length = 32)
    @Size(max = 32, message = "Inventary must not be longer then 32 characters.")
    private String inventary;

    @Column(name = "locality", length = 32)
    @Size(max = 32, message = "Locality must not be longer then 32 characters.")
    private String locality;

    @Column(name = "counter")
    private Long counter;

    //bi-directional many-to-many association to Category
    @ManyToMany(mappedBy = "devices", cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JsonIgnore
    private List<Category> categories = new ArrayList<Category>();

    //bi-directional many-to-many association to Device
    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(
            name = "AvailablePrinters",
            joinColumns = {@JoinColumn(name = "device_id", columnDefinition ="BIGINT UNSIGNED NOT NULL AUTO_INCREMENT")},
            inverseJoinColumns = {@JoinColumn(name = "printer_id")}
    )
    @JsonIgnore
    private List<Printer> availablePrinters = new ArrayList<Printer>();

    //bi-directional many-to-many association to Device
    @ManyToOne
    @JoinTable(
            name = "DefaultPrinter",
            joinColumns = {@JoinColumn(name = "device_id", columnDefinition ="BIGINT UNSIGNED NOT NULL AUTO_INCREMENT")},
            inverseJoinColumns = {@JoinColumn(name = "printer_id")}
    )
    @JsonIgnore
    private Printer defaultPrinter;

    @OneToMany(mappedBy = "device")
    @JsonIgnore
    private List<Printer> printerQueue = new ArrayList<Printer>();

    //bi-directional many-to-many association to Device
    @ManyToMany(mappedBy = "devices", cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JsonIgnore
    private List<SoftwareLicense> softwareLicenses = new ArrayList<SoftwareLicense>();

    //bi-directional many-to-one association to SoftwareStatus
    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<SoftwareStatus> softwareStatus = new ArrayList<SoftwareStatus>();

    //bi-directional many-to-one association to HWConf
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "hwconf_id", columnDefinition ="BIGINT UNSIGNED NOT NULL AUTO_INCREMENT")
    private HWConf hwconf;

    @Column(name = "hwconf_id", insertable = false, updatable = false)
    private Long hwconfId;

    //bi-directional many-to-one association to Room
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "room_id", columnDefinition ="BIGINT UNSIGNED NOT NULL AUTO_INCREMENT")
    private Room room;

    //bi-directional many-to-one association to Device
    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Session> sessions = new ArrayList<Session>();

    @Column(name = "room_id", insertable = false, updatable = false)
    private Long roomId;

    //bi-directional many-to-many association to User
    @ManyToMany(mappedBy = "loggedOn")
    @JsonIgnore
    private List<User> loggedIn = new ArrayList<User>();

    @Transient
    private String ownerName;

    @Transient
    private Long loggedInId = 0L;

    @Transient
    private String loggedInName = "nobody";

    @Transient
    private char[] screenShot;

    public Device() {
        this.hwconfId = null;
        this.name = "";
        this.ip = "";
        this.mac = "";
        this.wlanIp = "";
        this.wlanMac = "";
    }

    public Long getHwconfId() {
        return this.hwconfId;
    }

    public void setHwconfId(Long hwconfId) {
        this.hwconfId = hwconfId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPlace() {
        return this.place;
    }

    public void setPlace(int place) {
        this.place = place;
    }

    public String getIp() {
        return this.ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getMac() {
        return this.mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public int getRow() {
        return this.row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public String getSerial() {
        return this.serial;
    }

    public void setSerial(String value) {
        this.serial = value;
    }

    public String getInventary() {
        return this.inventary;
    }

    public void setInventary(String value) {
        this.inventary = value;
    }

    public String getLocality() {
        return this.locality;
    }

    public void setLocality(String value) {
        this.locality = value;
    }

    public List<Printer> getAvailablePrinters() {
        return this.availablePrinters;
    }

    public void setAvailablePrinters(List<Printer> availablePrinters) {
        this.availablePrinters = availablePrinters;
    }

    public Printer getDefaultPrinter() {
        return this.defaultPrinter;
    }

    public void setDefaultPrinter(Printer defaultPrinter) {
        this.defaultPrinter = defaultPrinter;
    }

    public HWConf getHwconf() {
        return this.hwconf;
    }

    public void setHwconf(HWConf hwconf) {
        this.hwconf = hwconf;
        this.hwconfId = hwconf.getId();
        if (!hwconf.getDevices().contains(this)) {
            hwconf.getDevices().add(this);
        }
    }

    public Room getRoom() {
        return this.room;
    }

    public void setRoom(Room room) {
        this.room = room;
        this.roomId = room.getId();
        if (!room.getDevices().contains(this)) {
            room.getDevices().add(this);
        }
    }

    public List<User> getLoggedIn() {
        return this.loggedIn;
    }

    public void setLoggedIn(List<User> loggedIn) {
        this.loggedIn = loggedIn;
    }

    public List<Category> getCategories() {
        return this.categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public String getWlanIp() {
        return wlanIp;
    }

    public void setWlanIp(String wlanip) {
        this.wlanIp = wlanip;
    }

    public String getWlanMac() {
        return wlanMac;
    }

    public void setWlanMac(String wlanmac) {
        this.wlanMac = wlanmac;
    }

    public List<SoftwareLicense> getSoftwareLicenses() {
        return softwareLicenses;
    }

    public void setSoftwareLicenses(List<SoftwareLicense> softwareLicenses) {
        this.softwareLicenses = softwareLicenses;
    }

    public List<SoftwareStatus> getSoftwareStatus() {
        return softwareStatus;
    }

    public void setSoftwareStatus(List<SoftwareStatus> softwareStatus) {
        this.softwareStatus = softwareStatus;
    }

    public Long getCounter() {
        if (counter == null) {
            return 0L;
        }
        return counter;
    }

    public void setCounter(Long counter) {
        this.counter = counter;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public List<Printer> getPrinterQueue() {
        return printerQueue;
    }

    public void setPrinterQueue(List<Printer> printerQueue) {
        this.printerQueue = printerQueue;
    }

    public List<Session> getSessions() {
        return sessions;
    }

    public void setSessions(List<Session> sessions) {
        this.sessions = sessions;
    }

    public String getOwnerName() {
        User owner = this.getCreator();
        if( owner != null ) {
            return owner.getFullName();
        }
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public char[] getScreenShot() {
        return screenShot;
    }

    public void setScreenShot(char[] screenShot) {
        this.screenShot = screenShot;
    }

    public Long getLoggedInId() {
        return loggedInId;
    }

    public void setLoggedInId(Long loggedInId) {
        this.loggedInId = loggedInId;
    }

    public String getLoggedInName() {
        return loggedInName;
    }

    public void setLoggedInName(String loggedInName) {
        this.loggedInName = loggedInName;
    }

    public boolean isFatClient() {
        return this.hwconf != null &&
                this.hwconf.getDeviceType() != null &&
                this.hwconf.getDeviceType().equals("FatClient");
    }

}
