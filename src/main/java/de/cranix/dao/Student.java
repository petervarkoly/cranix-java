/* (c) 2018 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.dao;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.annotations.CacheType;

import de.cranix.helper.SslCrypto;


public class Student {

	private Long id;

	private String givenName;

	private String surName;

	private String uid;

	private String uuid;

	private Integer fsQuotaUsed;

	private Integer fsQuota;

	private Integer msQuotaUsed;

	private Integer msQuota;

	private String groupType;

	private String groupName;

	private Long groupId;

	public Student(User user)  {
		this.id          = user.getId();
		this.uid         = user.getUid();
		this.uuid        = user.getUuid();
		this.surName     = user.getSurName();
		this.givenName   = user.getGivenName();
		this.fsQuota     = user.getFsQuota();
		this.fsQuotaUsed = user.getFsQuotaUsed();
		this.msQuota     = user.getMsQuota();
		this.msQuotaUsed = user.getMsQuotaUsed();
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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
		Student other = (Student) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	public String getGivenName() {
		return this.givenName;
	}

	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	public String getSurName() {
		return this.surName;
	}

	public void setSurName(String surname) {
		this.surName = surname;
	}

	public String getUid() {
		return this.uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getUuid() {
		return this.uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public void setFsQuotaUsed(Integer quota) {
		this.fsQuotaUsed = quota;
	}

	public void setFsQuota(Integer quota) {
		this.fsQuota = quota;
	}

	public void setMsQuotaUsed(Integer quota) {
		this.msQuotaUsed = quota;
	}

	public void setMsQuota(Integer quota) {
		this.msQuota = quota;
	}

	public Integer getFsQuotaUsed() {
		return this.fsQuotaUsed;
	}

	public Integer getFsQuota() {
		return this.fsQuota;
	}

	public Integer getMsQuotaUsed() {
		return this.msQuotaUsed;
	}

	public Integer getMsQuota() {
		return this.msQuota;
	}

	public String getGroupName() {
		return this.groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public Long getGroupId() {
		return this.groupId;
	}

	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}

	public String getGroupType() {
		return this.groupType;
	}

	public void setGroupType(String groupType) {
		this.groupType = groupType;
	}

}
