package de.cranix.dao;

public class DnsRecord {
	
	private String domainName;
	private String recordType;
	private String recordName;
	private String recordData;

	public DnsRecord() {
	}

	public DnsRecord(String domainName,String recordType, String recordName, String recordData) {
		this.domainName = domainName;
		this.recordType = recordType;
		this.recordName = recordName;
		this.recordData = recordData;
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public String getRecordType() {
		return recordType;
	}

	public void setRecordType(String recordType) {
		this.recordType = recordType;
	}

	public String getRecordName() {
		return recordName;
	}

	public void setRecordName(String recordName) {
		this.recordName = recordName;
	}

	public String getRecordData() {
		return recordData;
	}

	public void setRecordData(String recordData) {
		this.recordData = recordData;
	}

}
