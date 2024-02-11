/* (c) 2024 Péter Varkoly <peter@varkoly.de> - all rights reserved */
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
	@Column(name = "id", columnDefinition ="BIGINT UNSIGNED NOT NULL AUTO_INCREMENT")
	@GeneratedValue(strategy = IDENTITY)
	private Long id;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "createdate")
	private Date createDate;


	//@OneToOne
	@ManyToOne
	@JsonIgnore
	@JoinColumn(name="device_id", columnDefinition ="BIGINT UNSIGNED")
	private Device device;

	@ManyToOne
	@JsonIgnore
	@JoinColumn(name="user_id", columnDefinition ="BIGINT UNSIGNED NOT NULL")
	private User user;

	@OneToOne
	@JoinColumn(name="crx2fasession_id", columnDefinition ="BIGINT UNSIGNED")
	private Crx2faSession crx2faSession;

	@ManyToOne
	@JsonIgnore
	@JoinColumn(name="room_id", columnDefinition ="BIGINT UNSIGNED")
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
	private String dnsName;

	@Transient
	private List<String> acls;

	@Transient
	private String fullName;

	@Transient
	private String name;


	@Transient List<String> crx2fas = new ArrayList<>();

	public Session(String name) {
		this.name = name;
	}

	public Session(String token, User user, String password, String ip) {
		this.user = user;
		this.password = password;
		this.token = token;
		this.schoolId="dummy";
	}

	public Session() {
		this.user    = null;
		this.room    = null;
		this.dnsName = null;
		this.mac     = null;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		return prime * result + ((id == null) ? 0 : id.hashCode());
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
		if( this.device != null ) {
			data.append("deviceId: '" + String.valueOf(this.device.getId())).append("' ");
		} else {
			data.append("deviceId: 'null' ");
		}
		data.append("userId: '" + String.valueOf(this.user.getId())).append("' ");
		data.append("token: '" + this.token).append("' ");
		data.append("mac: '" + this.mac).append("' ");
		data.append("role: '" + this.role).append("' ");
		return data.toString();
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
		return this.user.getId();
	}

	public Long getDeviceId() {
		return this.device == null ? null: this.device.getId();
	}

	public Long getRoomId() {
		return this.room == null ? null: this.room.getId();
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

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
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

	public List<String> getAcls() {
		if( acls == null ) {
			return getUserAcls(this.user);
		}
		return acls;
	}

	public void setAcls(List<String> acls) {
		this.acls = acls;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public Boolean getMustChange() {
		return mustChange;
	}

	public void setMustChange(Boolean mustChange) {
		this.mustChange = mustChange;
	}

	/**
	 * @return the roomName
	 */
	public String getRoomName() {
		if( this.room != null ){
			return this.room.getName();
		}
		return "";
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

	public Crx2faSession getCrx2faSession() { return crx2faSession;	}

	public void setCrx2faSession(Crx2faSession crx2faSession) {
		this.crx2faSession = crx2faSession;
	}

	public List<String> getCrx2fas() { return crx2fas; }

	public void setCrx2fas(List<String> crx2fas) {this.crx2fas = crx2fas;}

	public static List<String> getUserAcls(User user){
		List<String> modules = new ArrayList<String>();
		//Modules with right permit all is allowed for all authorized users.
		modules.add("permitall");
		//Is it allowed by the groups.
		for( Group group : user.getGroups() ) {
		    for( Acl acl : group.getAcls() ) {
		        if( acl.getAllowed() ) {
		            modules.add(acl.getAcl());
		        }
		    }
		}
		//Is it allowed by the user
		for( Acl acl : user.getAcls() ){
		    if( acl.getAllowed() && !modules.contains(acl.getAcl())) {
		        modules.add(acl.getAcl());
		    } else if( !acl.getAllowed() && modules.contains(acl.getAcl()) ) {
		        //It is forbidden by the user
		        modules.remove(acl.getAcl());
		    }
		}
		return modules;
	}
}
