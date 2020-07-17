/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
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
public class RoomSmartControl implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="ACCESSINROOM_ID_GENERATOR", sequenceName="SEQ")
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="ACCESSINROOM_ID_GENERATOR")
	private Long id;
	
	//bi-directional many-to-one association to room
	@Column(name = "room_id")
	private Long roomId;
	
	@ManyToOne
	@JoinColumn(name = "room_id", insertable = false, updatable = false)
	@JsonIgnore
	private Room room;
	    
	//bi-directional many-to-one association to room
	@Column(name = "user_id")
	private Long ownerId;
	
	@ManyToOne
	@JoinColumn(name = "user_id", insertable = false, updatable = false)
	@JsonIgnore
	private User owner;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "startTime")
	private Date startTime; 
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "endTime")
	private Date endTime; 
	   
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
		RoomSmartControl other = (RoomSmartControl) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public RoomSmartControl() {
	}
	
	public RoomSmartControl(Long roomId, Long ownerId, Long duration) {
		this.ownerId = ownerId;
		this.roomId = roomId;
		this.startTime = new Date();
		this.endTime   = new Date( System.currentTimeMillis( ) + duration * 60 * 1000 );
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Room getRoom() {
		return this.room;
	}

	public void setRoomId(Long roomId) {
		this.roomId = roomId;
	}

	public User getOwner() {
		return this.owner;
	}

	public void setUserId(Long ownerId) {
		this.ownerId = ownerId;
	}
	
	public Date getStartTime() {
		return this.startTime;
	}
	
	public Date getEndTime() {
		return this.endTime;
	}
}
