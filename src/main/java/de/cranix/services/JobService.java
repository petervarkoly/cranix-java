/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.services;

import java.io.File;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.cranix.dao.*;
import de.cranix.helper.CrxSystemCmd;
import static de.cranix.helper.CranixConstants.*;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

public class JobService extends Service {

	Logger logger = LoggerFactory.getLogger(JobService.class);

    static FileAttribute<Set<PosixFilePermission>> privatDirAttribute  = PosixFilePermissions.asFileAttribute( PosixFilePermissions.fromString("rwx------"));
    static FileAttribute<Set<PosixFilePermission>> privatFileAttribute = PosixFilePermissions.asFileAttribute( PosixFilePermissions.fromString("rw-------"));

	private static String basePath = "/home/groups/SYSADMINS/jobs/";

	public JobService(Session session,EntityManager em) {
		super(session,em);
	}

	public Job getById(Long jobId) {
		try {
			Job job = this.em.find(Job.class, jobId);
			Path JOB_COMMAND = Paths.get(basePath + String.valueOf(jobId));
			Path JOB_RESULT  = Paths.get(basePath + String.valueOf(jobId) + ".log");
			List<String> tmp = Files.readAllLines(JOB_COMMAND);
			job.setCommand(String.join(getNl(),tmp));
			tmp = Files.readAllLines(JOB_RESULT);
			job.setResult(String.join(getNl(),tmp));
			return job;
		} catch (Exception e) {
			logger.error("DeviceId:" + jobId + " " + e.getMessage(),e);
			return null;
		} finally {
		}
	}

	/**
	 * Creates a new job
	 * @param job The job to be created.
	 * @return The result in an CrxResponse object
	 * @see CrxResponse
	 */
	public CrxResponse createJob(Job job) {

		if( job.getDescription().length() > 128 ) {
			job.setDescription(job.getDescription().substring(0, 127));
		}
		/*
		 * Set job start time
		 */
		String scheduledTime = "now";
		if( job.isPromptly() ) {
			job.setStartTime(new Timestamp(System.currentTimeMillis()));
		} else {
			Date date = new Date(job.getStartTime().getTime());
			SimpleDateFormat fmt = new SimpleDateFormat("HH:mm yyyy-MM-dd");
			scheduledTime        = fmt.format(date);
		}

		/*
		 * Create the Job entity
		 */
		try {
			this.em.getTransaction().begin();
			this.em.persist(job);
			this.em.getTransaction().commit();
		} catch (Exception e) {
			logger.error("createJob" + e.getMessage(),e);
			return new CrxResponse(this.getSession(),"ERROR", e.getMessage());
		} finally {
		}

		/*
		 * Write the file
		 */
		StringBuilder path = new StringBuilder(basePath);
		File jobDir = new File( path.toString() );
		try {
			Files.createDirectories(jobDir.toPath(), privatDirAttribute );
			path.append(String.valueOf(job.getId()));
			Path jobFile     = Paths.get(path.toString());
			List<String> tmp =  new ArrayList<String>();
			tmp.add("( "+ cranixBaseDir + "tools/crx_date.sh");
			tmp.add(job.getCommand());
			tmp.add("E=$?");
			tmp.add("crx_api.sh PUT system/jobs/"+String.valueOf(job.getId())+"/exit/$E");
			tmp.add("echo $E");
			tmp.add( cranixBaseDir + "tools/crx_date.sh) &> " + path.toString()+ ".log");
			Files.write(jobFile, tmp );
		} catch (Exception e) {
			logger.error("createJob" + e.getMessage(),e);
			return new CrxResponse(this.getSession(),"ERROR", e.getMessage());
		}

		/*
		 * Start the job
		 */
		String[] program   = new String[4];
		StringBuffer reply = new StringBuffer();
		StringBuffer error = new StringBuffer();
		program[0] = "at";
		program[1] = "-f";
		program[2] = path.toString();
		program[3] = scheduledTime;
		CrxSystemCmd.exec(program, reply, error, null);
		logger.debug("create job  : " + path.toString() + " : " + job.getCommand());
		return new CrxResponse(this.getSession(),"OK","Job was created successfully",job.getId());
	}

	public CrxResponse setExitCode(Long jobId, Integer exitCode) {
		try {
			Job job = this.em.find(Job.class, jobId);
			job.setExitCode(exitCode);
			job.setEndTime(new Timestamp(System.currentTimeMillis()));
			this.em.getTransaction().begin();
			this.em.merge(job);
			this.em.getTransaction().commit();
		}  catch (Exception e) {
			logger.error("createJob" + e.getMessage(),e);
			return new CrxResponse(this.getSession(),"ERROR", e.getMessage());
		} finally {
		}
		return new CrxResponse(this.getSession(),"OK","Jobs exit code was set successfully");
	}

	public CrxResponse restartJob(Long jobId) {
		try {
			Job job = this.em.find(Job.class, jobId);
			job.setStartTime(new Timestamp(System.currentTimeMillis()));
			this.em.getTransaction().begin();
			this.em.merge(job);
			this.em.getTransaction().commit();
		} catch (Exception e) {
			logger.error("createJob" + e.getMessage(),e);
			return new CrxResponse(this.getSession(),"ERROR", e.getMessage());
		} finally {
		}
		String[] program   = new String[4];
		StringBuffer reply = new StringBuffer();
		StringBuffer error = new StringBuffer();
		program[0] = "at";
		program[1] = "-f";
		program[2] = basePath + String.valueOf(jobId);
		program[3] = "now" ;
		CrxSystemCmd.exec(program, reply, error, null);
		return new CrxResponse(this.getSession(),"OK","Job was restarted successfully",jobId);
	}

	@SuppressWarnings("unchecked")
	public List<Job> searchJobs(String description, Timestamp after, Timestamp befor) {
		Query query = null;
		if( after.equals(befor) ) {
			query = this.em.createNamedQuery("Job.getByDescription").setParameter("description", description);
		} else {
			query = this.em.createNamedQuery("Job.getByDescriptionAndTime")
					.setParameter("description", description)
					.setParameter("after", after)
					.setParameter("befor", befor);
		}
		List<Job> jobs =  query.getResultList();
		return jobs;
	}

	@SuppressWarnings("unchecked")
	public List<Job> getRunningJobs() {
		Query query = this.em.createNamedQuery("Job.getRunning");
		List<Job> jobs =  query.getResultList();
		return jobs;
	}

	@SuppressWarnings("unchecked")
	public List<Job> getFailedJobs() {
		Query query = this.em.createNamedQuery("Job.getFailed");
		List<Job> jobs =  query.getResultList();
		return jobs;
	}

	@SuppressWarnings("unchecked")
	public List<Job> getSucceededJobs() {
		Query query = this.em.createNamedQuery("Job.getSucceeded");
		List<Job> jobs =  query.getResultList();
		return jobs;
	}
}
