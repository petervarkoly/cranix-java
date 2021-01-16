/* (c) 2020 Peter Varkoly - all rights reserved */
/* (c) 2018 EXTIS GmbH - all rights reserved */
package de.cranix.dao;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SupportRequest {
	public enum SUPPORT_TYPE {
		Error, FeatureRequest, Feedback, ProductOrder, OfferInq, SalesInq
	};

	private String email		= "";
	private String subject		= "";
	private String description	= "";
	private String regcode		= "";

	private String product		= "CRANIX";
	private String firstname	= "";
	private String lastname		= "";
	private String company		= "";
	private SUPPORT_TYPE supporttype = SUPPORT_TYPE.Error; 

	private String regcodeValidUntil	= "";
	private String status			= "";
	private String requestDate		= "";
	private String ticketno			= "";
	private String ticketResponseInfo	= "";

	@Override
	public String toString() {
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (Exception e) {
			return "{ \"ERROR\" : \"CAN NOT MAP THE OBJECT\" }";
		}
	}

	public SupportRequest() {

	}
	public String getTicketResponseInfo() {
		return ticketResponseInfo;
	}

	public void setTicketResponseInfo(String ticketResponseInfo) {
		this.ticketResponseInfo = ticketResponseInfo;
	}
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getRegcode() {
		return regcode;
	}

	public void setRegcode(String regcode) {
		this.regcode = regcode;
	}

	public String getRegcodeValidUntil() {
		return regcodeValidUntil;
	}

	public void setRegcodeValidUntil(String regcodeValidUntil) {
		this.regcodeValidUntil = regcodeValidUntil;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getRequestDate() {
		return requestDate;
	}

	public void setRequestDate(String requestDate) {
		this.requestDate = requestDate;
	}

	public String getTicketno() {
		return ticketno;
	}

	public void setTicketno(String ticketno) {
		this.ticketno = ticketno;
	}

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public SUPPORT_TYPE getSupporttype() {
		return supporttype;
	}

	public void setSupporttype(SUPPORT_TYPE supporttype) {
		this.supporttype = supporttype;
	}

}
