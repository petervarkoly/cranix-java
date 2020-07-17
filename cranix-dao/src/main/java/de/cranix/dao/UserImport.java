package de.cranix.dao;

import com.fasterxml.jackson.databind.ObjectMapper;

public class UserImport {

	private String  role;
	private String  lang;
	private String  identifier;
	private String  input;
	private	boolean test;
	private	boolean debug;
	private String  password;
	private boolean mustChange;
	private	boolean full;
	private boolean allClasses;
	private boolean cleanClassDirs;
	private boolean resetPassword;
	private boolean appendBirthdayToPassword;
	private String  startTime;
	private String  result;
	public UserImport() {
		// TODO Auto-generated constructor stub
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public String getLang() {
		return lang;
	}
	public void setLang(String lang) {
		this.lang = lang;
	}
	public boolean isTest() {
		return test;
	}
	public void setTest(boolean test) {
		this.test = test;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public boolean isMustchange() {
		return mustChange;
	}
	public void setMustchange(boolean mustChange) {
		this.mustChange = mustChange;
	}
	public boolean isFull() {
		return full;
	}
	public void setFull(boolean full) {
		this.full = full;
	}
	public boolean isAllClasses() {
		return allClasses;
	}
	public void setAllClasses(boolean allClasses) {
		this.allClasses = allClasses;
	}
	public boolean isCleanClassDirs() {
		return cleanClassDirs;
	}
	public void setCleanClassDirs(boolean cleanClassDirs) {
		this.cleanClassDirs = cleanClassDirs;
	}
	public boolean isResetPassword() {
		return resetPassword;
	}
	public void setResetPassword(boolean resetPassword) {
		this.resetPassword = resetPassword;
	}
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	@Override
	public String toString() {
		try {
			return new ObjectMapper().writeValueAsString(this);
		} catch (Exception e) {
			return "{ \"ERROR\" : \"CAN NOT MAP THE OBJECT\" }";
		}
	}
	public String getInput() {
		return input;
	}
	public void setInput(String input) {
		this.input = input;
	}
	public boolean isDebug() {
		return debug;
	}
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	public boolean isAppendBirthdayToPassword() {
		return appendBirthdayToPassword;
	}
	public void setAppendBirthdayToPassword(boolean appendBirthdayToPassword) {
		this.appendBirthdayToPassword = appendBirthdayToPassword;
	}
}
