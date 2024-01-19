/* (c) 2024 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.dao;

import java.io.Serializable;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The persistent class for the RoomConfig database table.
 *
 */
@Entity
@Table(name="RoomSmartControlls")
@NamedQueries({
	@NamedQuery(name="SmartControl.findAll", query="SELECT s FROM RoomSmartControl s") /*,
	@NamedQuery(name="SmartControl.getAllActive", query="SELECT s FROM RoomSmartControl s WHERE s.endTime < NOW" ),
	@NamedQuery(name="SmartControl.getAllActiveInRoom", query="SELECT s FROM RoomSmartControl s WHERE s.endTime < NOW AND s.room_id = :roomId" ),
	@NamedQuery(name="SmartControl.getAllActiveOfUser", query="SELECT s FROM RoomSmartControl s WHERE s.endTime < NOW AND s.user_id = :userId" )*/
})
@SequenceGenerator(name="seq", initialValue=1, allocationSize=100)
public class RoomSmartControl extends AbstractEntity {
	
	@ManyToOne
	@JoinColumn(name = "room_id", columnDefinition ="BIGINT UNSIGNED NOT NULL AUTO_INCREMENT", insertable = false, updatable = false)
	@JsonIgnore
	private Room room;
	
	//TODO rename startTime CREATED	
	//@Temporal(TemporalType.TIMESTAMP)
	//@Column(name = "startTime")
	//private Date startTime;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "endTime")
	private Date endTime;
	
	public RoomSmartControl() {
	}
	
	public RoomSmartControl(Room room, User owner, Long duration) {
		this.setCreator(owner);
		this.setRoom(room);
		this.setCreated(new Date());
		this.endTime = new Date( System.currentTimeMillis( ) + duration * 60 * 1000 );
	}
	
	public Room getRoom() {
		return this.room;
	}
	
	public Long getRoomId() {
		return this.room.getId();
	}
	
	public void setRoom(Room room) {
		this.room = room;
	}
	
	public void setEndTime(Date date) {
		this.endTime = date;
	}
	
	public Date getEndTime() {
		return this.endTime;
	}
}
