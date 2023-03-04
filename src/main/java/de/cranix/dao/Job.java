/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.dao;

import java.io.Serializable;

import javax.persistence.*;
import java.sql.Timestamp;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * The persistent class for the CephalixJobs database table.
 * 
 */
@Entity
@Table(name="Jobs")
@NamedQueries({
	@NamedQuery(name="Job.findAll",                 query="SELECT j FROM Job j"),
	@NamedQuery(name="Job.findAllByTime",           query="SELECT j FROM Job j WHERE j.startTime > :after AND j.startTime < :befor"),
	@NamedQuery(name="Job.getByDescriptionAndTime", query="SELECT j FROM Job j WHERE j.description LIKE :description AND j.startTime > :after AND j.startTime < :befor"),
	@NamedQuery(name="Job.getByDescription",        query="SELECT j FROM Job j WHERE j.description LIKE :description"),
	@NamedQuery(name="Job.getRunning",              query="SELECT j FROM Job j WHERE j.exitCode = NULL"),
	@NamedQuery(name="Job.getSucceeded",            query="SELECT j FROM Job j WHERE j.exitCode = 0"),
	@NamedQuery(name="Job.getFailed",               query="SELECT j FROM Job j WHERE j.exitCode > 0")
})
public class Job extends AbstractEntity {

	@Column(name="description", length=128)
	private String description;

	//TODO rename startTime CREATED
	//private Timestamp startTime;
	
	//TODO rename endTime MODIFIED
	//private Timestamp endTime;

	private Integer exitCode;
	
	@Transient
	private String command;
	
	@Transient
	private boolean promptly;
	
	@Transient
	private String result;

	public Job() {
	}

	public Job(String description, Timestamp startTime, String command, boolean promptly) {
		super();
		this.description = description;
		this.setCreated(startTime);
		this.command     = command;
		this.promptly    = promptly;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description.length() > 128 ? description.substring(0,128) : description;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public boolean isPromptly() {
		return promptly;
	}

	public void setPromptly(boolean promptly) {
		this.promptly = promptly;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public Integer getExitCode() {
		return exitCode;
	}

	public void setExitCode(Integer exitCode) {
		this.exitCode = exitCode;
	}
}
