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
public class Job implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="CEPHALIXJOBS_ID_GENERATOR" )
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="CEPHALIXJOBS_ID_GENERATOR")
	private Long id;

	private String description;

	private Timestamp startTime;
	
	private Timestamp endTime;

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
		this.startTime   = startTime;
		this.command     = command;
		this.promptly    = promptly;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Timestamp getStartTime() {
		return this.startTime;
	}

	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
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

	public Timestamp getEndTime() {
		return endTime;
	}

	public void setEndTime(Timestamp endTime) {
		this.endTime = endTime;
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
		Job other = (Job) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
