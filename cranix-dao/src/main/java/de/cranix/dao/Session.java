/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
/* (c) 2016 EXTIS GmbH - all rights reserved */
package de.cranix.dao;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;


import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "Sessions")
@NamedQueries({
	@NamedQuery(name = "Session.getByToken", query = "SELECT s FROM Session s WHERE s.token=:token")
})
public class Session implements Principal {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = IDENTITY)
	private int id;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "createdate")
	private Date createDate;


	@Column(name="device_id", insertable = false, updatable = false )
	private Long deviceId;

	//@OneToOne
	@ManyToOne
	@JsonIgnore
	private Device device;

	@Column(name = "user_id", insertable = false, updatable = false )
	private Long userId;

	@ManyToOne
	@JsonIgnore
	private User user;

	@Column(name = "room_id", insertable = false, updatable = false)
	private Long roomId;

	@ManyToOne
	@JsonIgnore
	private Room room;

	@Column(name = "ip")
	private String ip;

	@Column(name = "token")
	private String token;

	/**
	 * Transient variables to make the life in front end more simply.
	 */
	@Transient
	private String role = "dummy";

	@Transient
	private String password = "dummy";

	@Transient
	private Boolean mustChange = false;

	@Transient
	private String schoolId = "dummy";

	@Transient
	private String mac;

	@Transient
	private String roomName;

	@Transient
	private String dnsName;

	@JsonIgnore
	private transient Object temporaryUploadData;

	@Transient
	private List<String> acls;

	@Transient
	private String commonName;

	public Object getTemporaryUploadData() {
		return temporaryUploadData;
	}

	public void setTemporaryUploadData(Object temporaryUploadData) {
		this.temporaryUploadData = temporaryUploadData;
	}

	public Session(String token, Long userid, String password, String ip) {
		this.userId = userid;
		this.password = password;
		this.token = token;
		this.schoolId="dummy";
	}

	public Session() {
		this.deviceId = null;
		this.roomId   = null;
		this.dnsName  = null;
		this.mac	  = null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
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
		Session other = (Session) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder data = new StringBuilder();
		if( this.deviceId != null ) {
			data.append("deviceId: '" + String.valueOf(this.deviceId)).append("' ");
		} else {
			data.append("deviceId: 'null' ");
		}
		data.append("userId: '" + String.valueOf(this.userId)).append("' ");
		data.append("token: '" + this.token).append("' ");
		data.append("mac: '" + this.mac).append("' ");
		data.append("role: '" + this.role).append("' ");
		return data.toString();
	}

	@Override
	public String getName() {
		return "dummy";
	}

	public String getSchoolId() {
		return this.schoolId;
	}

	public void setSchoolId(String schoolId) {
		this.schoolId = schoolId;
	}

	public Room getRoom() {
		return this.room;
	}

	public void setRoom(Room room) {
		this.room = room;
	}

	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRole() {
		return this.role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public Long getUserId() {
		return this.userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getDeviceId() {
		return this.deviceId;
	}

	public void setDeviceId(Long deviceId) {
		this.deviceId = deviceId;
	}

	public Long getRoomId() {
		return this.roomId;
	}

	public void setRoomId(Long roomId) {
		this.roomId = roomId;
	}

	public Date getCreateDate() {
		return this.createDate;
	}

	public Device getDevice() {
		return this.device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getToken() {
		return this.token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getMac() {
		return this.mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public String getDNSName() {
		return this.dnsName;
	}

	public void setDNSName(String dnsName) {
		this.dnsName = dnsName;
	}

	public List<String> getAcls() {
		if( acls == null) {
			return this.getUserAcls();
		}
		return acls;
	}

	public void setAcls(List<String> acls) {
		this.acls = acls;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public String getCommonName() {
		return commonName;
	}

	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}

	public Boolean getMustChange() {
		return mustChange;
	}

	public void setMustChange(Boolean mustChange) {
		this.mustChange = mustChange;
	}

	public List<String> getUserAcls(){
		List<String> modules = new ArrayList<String>();
		//Modules with right permit all is allowed for all authorized users.
		modules.add("permitall");
		//Is it allowed by the groups.
		for( Group group : this.user.getGroups() ) {
			for( Acl acl : group.getAcls() ) {
				if( acl.getAllowed() ) {
					modules.add(acl.getAcl());
				}
			}
		}
		//Is it allowed by the user
		for( Acl acl : this.user.getAcls() ){
			if( acl.getAllowed() && !modules.contains(acl.getAcl())) {
				modules.add(acl.getAcl());
			} else if( modules.contains(acl.getAcl()) ) {
				//It is forbidden by the user
				modules.remove(acl.getAcl());
			}
		}
		return modules;
	}

	/**
	 * @return the roomName
	 */
	public String getRoomName() {
		return roomName;
	}

	/**
	 * @param roomName the roomName to set
	 */
	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}

	/**
	 * @return the dnsName
	 */
	public String getDnsName() {
		return dnsName;
	}

	/**
	 * @param dnsName the dnsName to set
	 */
	public void setDnsName(String dnsName) {
		this.dnsName = dnsName;
	}

	/**
	 * @return the ip
	 */
	public String getIp() {
		return ip;
	}

	/**
	 * @param ip the ip to set
	 */
	public void setIp(String ip) {
		this.ip = ip;
	}

}
