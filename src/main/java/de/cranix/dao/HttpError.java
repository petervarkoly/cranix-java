package de.cranix.dao;

public class HttpError {
	
	private Long code;
	private String message;
	
	public HttpError() {
	}
	public Long getCode() {
		return code;
	}
	public void setCode(Long code) {
		this.code = code;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}

}
