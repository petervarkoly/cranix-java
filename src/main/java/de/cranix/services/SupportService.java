/* (c) 2021 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.services;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.cranix.dao.CrxResponse;
import de.cranix.dao.Session;
import de.cranix.dao.SupportRequest;
import de.cranix.helper.CrxSystemCmd;

import static de.cranix.helper.CranixConstants.*;

public class SupportService extends Service {
    Logger logger = LoggerFactory.getLogger(SupportService.class);

    private String supportUrl;
    private String supportEmailFrom;
    private String regCode;

    public SupportService(Session session, EntityManager em) {
        super(session, em);
        supportUrl = this.getConfigValue("SUPPORT_URL");
        regCode = this.getConfigValue("REG_CODE");
        if (supportUrl == null) {
            supportUrl = cranixSupportUrl;
        }
        supportUrl = supportUrl.trim();
    }

    public CrxResponse create(SupportRequest supportRequest) {
        supportEmailFrom = supportRequest.getEmail();
        if (supportRequest.getRegcode() == null || supportRequest.getRegcode().isEmpty()) {
            supportRequest.setRegcode(regCode);
        }
        if (supportRequest.getProduct() == null || supportRequest.getProduct().isEmpty()) {
            supportRequest.setProduct("CRANIX");
        }
        if (supportRequest.getCompany() == null || supportRequest.getCompany().isEmpty()) {
            supportRequest.setCompany(this.getConfigValue("NAME"));
        }
        List<String> parameters = new ArrayList<String>();
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
            return new CrxResponse("ERROR", e.getMessage());
        }

        String[] program = new String[12];
        StringBuffer reply = new StringBuffer();
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
        program[10] = "@" + file.getPath();
        program[11] = supportUrl;
        CrxSystemCmd.exec(program, reply, stderr, null);
        logger.debug("Support reply" + reply.toString());
        logger.debug("Support error" + stderr.toString());
        try {
            ObjectMapper mapper = new ObjectMapper();
            SupportRequest suppres = mapper.readValue(IOUtils.toInputStream(reply.toString(), "UTF-8"), SupportRequest.class);
            logger.debug("Support Respons :" + suppres);
            if (suppres.getStatus() != null && suppres.getStatus().equals("OK")) {
                parameters.add(suppres.getSubject());
                parameters.add(suppres.getTicketno());
                parameters.add(suppres.getEmail());
                parameters.add(suppres.getTicketResponseInfo());
                return new CrxResponse("OK", "Support request '%s' was created with ticket number '%s'. Answer will be sent to '%s'.", null, parameters);
            } else {
                parameters.add(suppres.getSubject());
                parameters.add(suppres.getTicketResponseInfo());
                return new CrxResponse("ERROR", "Support request '%s' was not created. The reason is:'%s'", null, parameters);
            }
        } catch (Exception e) {
            logger.error("GETObject :" + e.getMessage());
            return new CrxResponse("ERROR", "Can not send support request");
        }
    }

    public Object getAll(String status) {
        String url = "http://zadmin:9080/api/tickets/regcodes/" + regCode + "/cranixUserId/" + this.session.getUser().getId() + "/" + status;
        if(isSuperuser()){
            url = "http://zadmin:9080/api/tickets/regcodes/" + regCode + "/status/" + status;
        }
        String[] program = new String[10];
        StringBuffer reply = new StringBuffer();
        StringBuffer stderr = new StringBuffer();
        program[0] = "/usr/bin/curl";
        program[1] = "--insecure";
        program[2] = "-s";
        program[3] = "-X";
        program[4] = "GET";
        program[5] = "--header";
        program[6] = "Content-Type: application/json";
        program[7] = "--header";
        program[8] = "Accept: application/json";
        program[9] = url;
        CrxSystemCmd.exec(program, reply, stderr, null);
        return reply.toString();
    }

    public Object getArticle(Long ticketId) {
        String url = "http://zadmin:9080/api/tickets/regcodes/" + regCode + "/" + ticketId;
        String[] program = new String[10];
        StringBuffer reply = new StringBuffer();
        StringBuffer stderr = new StringBuffer();
        program[0] = "/usr/bin/curl";
        program[1] = "--insecure";
        program[2] = "-s";
        program[3] = "-X";
        program[4] = "GET";
        program[5] = "--header";
        program[6] = "Content-Type: application/json";
        program[7] = "--header";
        program[8] = "Accept: application/json";
        program[9] = url;
        CrxSystemCmd.exec(program, reply, stderr, null);
        return reply.toString();
    }

    public Object addArticle(Long ticketId, Object article) {
        File file;
        try {
            file = File.createTempFile("support", ".json", new File(cranixTmpDir));
            PrintWriter writer = new PrintWriter(file.getPath(), "UTF-8");
            writer.print(article);
            writer.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return new CrxResponse("ERROR", e.getMessage());
        }
        String url = "http://zadmin:9080/api/tickets/regcodes/" + regCode + "/" + ticketId;
        String[] program = new String[11];
        StringBuffer reply = new StringBuffer();
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
        program[10] = "@" + file.getPath();
        program[11] = url;
        CrxSystemCmd.exec(program, reply, stderr, null);
        return reply.toString();
    }
}
