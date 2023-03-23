package de.cranix.services;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public interface MailService {
    public void sendMail(
            String to,
            String subject,
            String text
    );

    public void sendMail(
            String to,
            String subject,
            String text,
            String attachment,
            String attachmentName);

    public void sendMail(
            List<String> to,
            String subject,
            String text,
            String attachment,
            String attachmentName);

    public JSONArray getAllMessages();

    public JSONObject getMessage(String messageId);

    public void saveAttachmentOfMail(JSONObject message, Long ticketId, Long articleId);

    public void moveToArchive(String messageId);

    public void close();
}