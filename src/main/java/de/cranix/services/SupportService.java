/* (c) 2021 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.services;

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

import de.cranix.dao.CrxResponse;
import de.cranix.dao.Session;
import de.cranix.dao.SupportRequest;
import de.cranix.services.SystemService;
import de.cranix.helper.CrxEntityManagerFactory;
import de.cranix.helper.OSSShellTools;
import static de.cranix.helper.CranixConstants.*;

public class SupportService extends Service {
	Logger logger = LoggerFactory.getLogger(SupportService.class);

	private String supportUrl;
        private String supportEmail;
        private String supportEmailFrom;

	public SupportService(Session session, EntityManager em) {
		super(session, em);
	}

	public CrxResponse create(SupportRequest supportRequest) {
		supportUrl = this.getConfigValue("SUPPORT_URL");
                if(supportUrl != null) {
                        supportUrl = supportUrl.trim();
                }
		if(supportUrl != null && supportUrl.equalsIgnoreCase("MAIL")) {
                        supportEmail     = this.getConfigValue("SUPPORT_MAIL_ADDRESS");
                        supportEmailFrom = supportRequest.getEmail(); 
                        if(supportEmailFrom.isEmpty()) {
				this.getConfigValue("SUPPORT_MAIL_FROM");
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
                        supportUrl = cranixSupportUrl;
                        supportEmail = null;
                        supportEmailFrom = null;
                }
		if( supportRequest.getRegcode() == null || supportRequest.getRegcode().isEmpty() )  {
			supportRequest.setRegcode(this.getConfigValue("REG_CODE"));
		}
		if( supportRequest.getProduct() == null || supportRequest.getProduct().isEmpty() )  {
			supportRequest.setProduct("CRANIX");
		}
		if( supportRequest.getCompany() == null || supportRequest.getCompany().isEmpty() )  {
			supportRequest.setCompany(this.getConfigValue("NAME"));
		}
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
		if(supportUrl != null && supportUrl.length() > 0) {
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
				logger.debug("Support Respons :" + suppres);
				if( suppres.getStatus() != null && suppres.getStatus().equals("OK") ) {
					parameters.add(suppres.getSubject());
					parameters.add(suppres.getTicketno());
					parameters.add(suppres.getEmail());
					parameters.add(suppres.getTicketResponseInfo());
					return new CrxResponse(session,"OK","Support request '%s' was created with ticket number '%s'. Answer will be sent to '%s'.",null,parameters);
				} else {
					parameters.add(suppres.getSubject());
					parameters.add(suppres.getTicketResponseInfo());
					return new CrxResponse(session,"ERROR","Support request '%s' was not created. The reason is:'%s'",null,parameters);
				}
			} catch (Exception e) {
				logger.error("GETObject :" + e.getMessage());
				return new CrxResponse(session,"ERROR","Can not send support request");
			}
		} else {
			// Support via email
			StringBuilder request = new StringBuilder();
			//Header
			//Subject:
			request.append("Subject: ").append(supportRequest.getSubject()).append("\n");
			//From:
			request.append("From: ").append(supportRequest.getFirstname()).append(" ").append(supportRequest.getLastname()).append(" <").append(supportEmailFrom).append(">");
			request.append("\n");
			//Start body
			request.append(supportRequest.getDescription());
			request.append("\n\nRegcode: ").append(supportRequest.getRegcode());
			request.append("\nSupporttype: ").append(supportRequest.getSupporttype());
			request.append("\n.");

			String[] program   = new String[2];
			StringBuffer reply = new StringBuffer();
			StringBuffer error = new StringBuffer();
			program[0] = "/usr/sbin/sendmail";
			program[1] = supportEmail;

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
