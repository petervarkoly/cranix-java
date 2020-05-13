package de.cranix.api.resourceimpl;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.persistence.EntityManager;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.cranix.api.resources.SupportResource;
import de.cranix.dao.CrxResponse;
import de.cranix.dao.Session;
import de.cranix.dao.SupportRequest;
import de.cranix.dao.controller.SystemController;
import de.cranix.dao.internal.CommonEntityManagerFactory;
import de.cranix.dao.tools.OSSShellTools;
import static de.cranix.dao.internal.CranixConstants.*;

public class SupportResourceImpl implements SupportResource {
	Logger logger = LoggerFactory.getLogger(SupportResourceImpl.class);

	private String supportUrl;
	private String supportEmail;
	private String supportEmailFrom;
	private boolean isLinux = true;
	public SupportResourceImpl() {
		String os = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
		isLinux = !((os.indexOf("mac") >= 0) || (os.indexOf("darwin") >= 0));
	}

	private void loadConf(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		SystemController sc = new SystemController(session,em);
		supportUrl = sc.getConfigValue("SUPPORT_URL");
		if (supportUrl != null) {
			supportUrl = supportUrl.trim();
		}
		if (supportUrl != null && supportUrl.equalsIgnoreCase("MAIL")) {
			supportEmail = sc.getConfigValue("SUPPORT_MAIL_ADDRESS");
			supportEmailFrom = sc.getConfigValue("SUPPORT_MAIL_FROM");
			if (supportEmailFrom!=null) {
				supportEmailFrom = supportEmailFrom.trim();
				if (supportEmailFrom.length()==0) {
					supportEmailFrom = null;
				}
			}
			supportUrl = null;
		} else if (supportUrl != null && supportUrl.length() > 0) {
			supportEmail = null;
			supportEmailFrom = null;
		} else {
			// not configured therefore use default
			supportUrl = "https://support.extis.de/support";
			supportEmail = null;
			supportEmailFrom = null;
		}
	}

	@Override
	public CrxResponse create(Session session, SupportRequest supportRequest) {
		loadConf(session);
		List<String> parameters  = new ArrayList<String>();
		logger.debug("URL: " + supportUrl);
		logger.debug(supportRequest.toString());
		File file = null;
		try {
			file = File.createTempFile("support", ".json", new File(cranixTmpDir));
			PrintWriter writer = new PrintWriter(file.getPath(), "UTF-8");
			writer.print(supportRequest.toString());
			writer.close();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return new CrxResponse(session,"ERROR", e.getMessage());
		}
		if (supportUrl != null && supportUrl.length() > 0) {
			String[] program    = new String[12];
			StringBuffer reply  = new StringBuffer();
			StringBuffer stderr = new StringBuffer();
			program[0] = "/usr/bin/curl";
			program[1] = "--insecure";
			program[2] = "-s";
			program[3] = "-X";
			program[4] = "POST";
			program[5] = "--header";
			program[6] = "Content-Type: application/json";
			program[7] = "--header";
			program[8] = "Accept: application/json";
			program[9] = "-d";
			program[10] = "@"+file.getPath();
			program[11] = supportUrl;
			OSSShellTools.exec(program, reply, stderr, null);
			logger.debug("Support reply" + reply.toString());
			logger.debug("Support error" + stderr.toString());
			try {
				ObjectMapper mapper = new ObjectMapper();
				SupportRequest suppres = mapper.readValue(IOUtils.toInputStream(reply.toString(), "UTF-8"), SupportRequest.class);
				parameters.add(supportRequest.getSubject());
				parameters.add(suppres.getTicketno());
				parameters.add(supportRequest.getEmail());
				parameters.add(suppres.getTicketResponseInfo());
				logger.debug("Support Respons :" + suppres);
				return new CrxResponse(session,"OK","Support request '%s' was created with ticket number '%s'. Answer will be sent to '%s'.",null,parameters);
			} catch (Exception e) {
				logger.error("GETObject :" + e.getMessage());
				return new CrxResponse(session,"ERROR","Can not sent supprt request");
			}
		} else {
			// use classic email
			StringBuilder request = new StringBuilder();
			request.append(supportRequest.getDescription()).append("\n\nRegcode: ").append(supportRequest.getRegcode());
			request.append("\nProduct: ").append(supportRequest.getProduct());

			request.append("\nVon: ").append(supportRequest.getFirstname()).append(" ")
					.append(supportRequest.getLastname());
			if (supportRequest.getCompany() != null && supportRequest.getCompany().trim().length() > 0) {
				request.append(" ").append(supportRequest.getCompany());
			}
			request.append(" ").append(supportEmail);
			request.append("\nArt: ").append(supportRequest.getSupporttype());
			request.append("\n."); // Important: end of message

			int args = 4 + (isLinux?1:0) + (supportEmailFrom!=null?(isLinux?1:0):0);
			String[] program = new String[args];
			StringBuffer reply = new StringBuffer();
			StringBuffer error = new StringBuffer();
			int i = 0;
			program[i++] = "/usr/bin/mail";
			if (isLinux) {
				program[i++] = "-R " + supportRequest.getEmail();
			}
			program[i++] = "-s " + supportRequest.getSubject();
			if (isLinux && supportEmailFrom != null) {
				program[i++] = "-r " + supportEmailFrom;
			}
			program[i++] = "-c " + supportRequest.getEmail();// cc address

			program[i++] = supportEmail;

			int result = OSSShellTools.exec(program, reply, error, request.toString());
			if (result == 0) {
				parameters.add(supportRequest.getSubject());
				parameters.add(supportRequest.getEmail());
					return new CrxResponse(session,"OK","Support request '%s' was sent.  Answer will be sent to '%s'.",null,parameters);
			} else {
				logger.error("Error sending support mail: " + error.toString());
				parameters.add(supportRequest.getSubject());
				parameters.add(error.toString());
				return new CrxResponse(session,"ERROR","Sopport request '%s' could not be sent. Reason '%s'",null, parameters);
			}
		}
	}
}
