/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.dao;

import java.io.Serializable;

import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * The persistent class for the AccessInRoom database table.
 *
 */
@Entity
@Table(name="AccessInRooms")
@NamedQueries( {
    @NamedQuery(name="AccessInRoom.findAll",            query="SELECT a FROM AccessInRoom a"),
    @NamedQuery(name="AccessInRoom.findByRoom",         query="SELECT a FROM AccessInRoom a WHERE a.room = :room"),
    @NamedQuery(name="AccessInRoom.findByType",         query="SELECT a FROM AccessInRoom a WHERE a.accessType = :accessType"),
    @NamedQuery(name="AccessInRoom.findActualAccesses", query="SELECT a FROM AccessInRoom a WHERE a.pointInTime = :time")
})

public class AccessInRoom implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="ACCESSINROOM_ID_GENERATOR", sequenceName="SEQ")
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="ACCESSINROOM_ID_GENERATOR")
	private Long id;

	//uni-directional many-to-one association to Room
	@ManyToOne
	@JsonIgnore
	private Room room;

	/*
	 * The type of the access control. This can be ACT (action) or FWC or firewall access control
	 */
	private String accessType;

	/*
	 * The action to be executed when accesType is ACT
	 */
	private String action;

	@Column(name="room_id", insertable = false, updatable = false)
	private Long roomId;

	@Transient
	private String roomName;

	/*
	 * If the corresponding access should be applied Mondays
	 */
	@Convert(converter=BooleanToStringConverter.class)
	private Boolean monday;

	/*
	 * If the corresponding access should be applied Tuesdays
	 */
	@Convert(converter=BooleanToStringConverter.class)
	private Boolean tuesday;

	/*
	 * If the corresponding access should be applied Wednesdays
	 */
	@Convert(converter=BooleanToStringConverter.class)
	private Boolean wednesday;

	/*
	 * If the corresponding access should be applied Thursdays
	 */
	@Convert(converter=BooleanToStringConverter.class)
	private Boolean thursday;

	/*
	 * If the corresponding access should be applied Fridays
	 */
	@Convert(converter=BooleanToStringConverter.class)
	private Boolean friday;

	/*
	 * If the corresponding access should be applied Saturdays
	 */
	@Convert(converter=BooleanToStringConverter.class)
	private Boolean saturday;

	/*
	 * If the corresponding access should be applied Sundays
	 */
	@Convert(converter=BooleanToStringConverter.class)
	private Boolean sunday;

	/*
	 * If the corresponding access should be applied on holidays
	 */
	@Convert(converter=BooleanToStringConverter.class)
	private Boolean holiday;

	/*
	 * If the direct internet access is allowed or should be allowed.
	 */
	@Convert(converter=BooleanToStringConverter.class)
	private Boolean direct;

	/*
	 * If is allowed log in or should be allowed.
	 */
	@Convert(converter=BooleanToStringConverter.class)
	private Boolean login;

	/*
	 * If the access to the portal and mailserver is allowed or should be allowed.
	 */
	@Convert(converter=BooleanToStringConverter.class)
	private Boolean portal;

	/*
	 * If the access to the printserver is allowed or should be allowed.
	 */
	@Convert(converter=BooleanToStringConverter.class)
	private Boolean printing;

	/*
	 * If the direct internet access via proxy is allowed or should be allowed.
	 */
	@Convert(converter=BooleanToStringConverter.class)
	private Boolean proxy;

	private String  pointInTime;

	//bi-directional many-to-one association to User
	@ManyToOne
	@JsonIgnore
	private User creator;

	@Transient
	private Boolean allowSessionIp = false;

	public AccessInRoom() {
		this.pointInTime = "06:00";
		this.monday   = true;
		this.tuesday   = true;
		this.wednesday= true;
		this.thursday = true;
		this.friday   = true;
		this.saturday = false;
		this.sunday   = false;
		this.holiday  = false;
		this.direct   = false;
		this.login    = true;
		this.portal   = true;
		this.printing = true;
		this.proxy    = true;
		this.action  = "";
	}

	public AccessInRoom(Room room) {
		this.pointInTime = "06:00";
		this.accessType  = "DEF";
		this.monday   = true;
		this.tuesday  = true;
		this.wednesday= true;
		this.thursday = true;
		this.friday   = true;
		this.saturday = false;
		this.sunday   = false;
		this.holiday  = false;
		this.direct   = false;
		this.login    = true;
		this.portal   = true;
		this.printing = true;
		this.proxy    = true;
		this.action   = "";
		this.room     = room;
		this.creator  = room.getCreator();
		room.addAccessInRoom(this);
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
		AccessInRoom other = (AccessInRoom) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (Exception e) {
			return "{ \"ERROR\" : \"CAN NOT MAP THE OBJECT\" }";
		}
	}


	public Room getRoom() {
		return this.room;
	}

	public void setRoom(Room room) {
		this.room = room;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAccessType() {
		return this.accessType;
	}

	public void setAccessType(String accessType) {
		this.accessType = accessType;
	}

	public String getAction() {
		return this.action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public Boolean getDirect() {
		return this.direct;
	}

	public void setDirect(Boolean direct) {
		this.direct = direct;
	}

	public Boolean getFriday() {
		return this.friday;
	}

	public void setFriday(Boolean friday) {
		this.friday = friday;
	}

	public Boolean getHoliday() {
		return this.holiday;
	}

	public void setHoliday(Boolean holiday) {
		this.holiday = holiday;
	}

	public Boolean getLogin() {
		return this.login;
	}

	public void setLogin(Boolean login) {
		this.login = login;
	}

	public Boolean getMonday() {
		return this.monday;
	}

	public void setMonday(Boolean monday) {
		this.monday = monday;
	}

	public String getPointInTime() {
		return this.pointInTime;
	}

	public void setPointInTime(String pointInTime) {
		if(  pointInTime.length() == 4 ) {
			this.pointInTime = "0" + pointInTime;
		} else {
			this.pointInTime = pointInTime;
		}
	}

	public Boolean getPortal() {
		return this.portal;
	}

	public void setPortal(Boolean portal) {
		this.portal = portal;
	}

	public Boolean getPrinting() {
		return this.printing;
	}

	public void setPrinting(Boolean printing) {
		this.printing = printing;
	}

	public Boolean getProxy() {
		return this.proxy;
	}

	public void setProxy(Boolean proxy) {
		this.proxy = proxy;
	}

	public Long getRoomId() {
		return this.roomId;
	}

	public void setRoomId(Long roomId) {
		this.roomId = roomId;
	}

	public Boolean getSaturday() {
		return this.saturday;
	}

	public void setSaturday(Boolean saturday) {
		this.saturday = saturday;
	}

	public Boolean getSunday() {
		return this.sunday;
	}

	public void setSunday(Boolean sunday) {
		this.sunday = sunday;
	}

	public Boolean getThursday() {
		return this.thursday;
	}

	public void setThursday(Boolean thursday) {
		this.thursday = thursday;
	}

	public Boolean getTuesday() {
		return this.tuesday;
	}

	public void setTuesday(Boolean tuesday) {
		this.tuesday = tuesday;
	}

	public Boolean getWednesday() {
		return this.wednesday;
	}

	public void setWednesday(Boolean wednesday) {
		this.wednesday = wednesday;
	}

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public void correctTime() {
		if(  this.pointInTime.length() == 4 ) {
			this.pointInTime = "0" + pointInTime;
		}
	}

	public String getRoomName() {
		return roomName;
	}

	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}

	public Boolean getAllowSessionIp() {
		return allowSessionIp;
	}

	public void setAllowSessionIp(Boolean allowSessionIp) {
		this.allowSessionIp = allowSessionIp;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
}
