/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.dao;

import java.util.List;

public class Clone {
	
	private boolean multicast;
	
	private List<Long> deviceIds;
	
	private List<Long> partitionIds;

	public Clone() {
		// TODO Auto-generated constructor stub
	}

	public boolean isMultiCast() {
		return this.multicast;
	}

	public void setMultiCast(boolean multicast) {
		this.multicast = multicast;
	}

	public List<Long> getDeviceIds() {
		return this.deviceIds;
	}

	public void setDeviceIds(List<Long> deviceIds) {
		this.deviceIds = deviceIds;
	}

	public List<Long> getPartitionIds() {
		return this.partitionIds;
	}

	public void setPartitionIds(List<Long> partitionIds) {
		this.partitionIds = partitionIds;
	}

}
