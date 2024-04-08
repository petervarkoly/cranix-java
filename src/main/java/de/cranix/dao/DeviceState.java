package de.cranix.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.io.Serializable;

import static javax.persistence.TemporalType.TIMESTAMP;

@Entity
@Table(
        name="DeviceStates"
)
public class DeviceState implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(
            name = "id",
            columnDefinition ="BIGINT UNSIGNED NOT NULL AUTO_INCREMENT"
    )
    private Long id;

    @Column(
            name = "created",
            updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
    )
    private String created;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "device_id", columnDefinition = "BIGINT UNSIGNED NOT NULL")
    private Device device;

    @Transient
    private Long deviceId = 0L;

    @Column( name = "state")
    private Integer state;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Long getDeviceId(){
        return this.device != null ? this.device.getId() : this.deviceId;
    }
}
