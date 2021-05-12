package de.cranix.services;

import de.cranix.dao.CrxResponse;
import de.cranix.dao.Device;
import de.cranix.dao.Session;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.persistence.EntityManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.cranix.helper.CranixConstants.cranixMdmConfig;
import static de.cranix.helper.StaticHelpers.convertJavaTime;
import static de.cranix.helper.StaticHelpers.createLiteralJson;

public class CrxMdmService extends Service {

    Logger logger = LoggerFactory.getLogger(RoomService.class);

    static HttpClient http = null;
    static String enrollmentsUrl = "";
    static String devicesUrl = "";

    public CrxMdmService(Session session, EntityManager em) {
        super(session, em);
        setConfig(cranixMdmConfig, "MDM_");
        if (http == null) {
            CookieStore httpCookieStore = new BasicCookieStore();
            HttpClientBuilder builder = HttpClientBuilder.create().setDefaultCookieStore(httpCookieStore);
            http = builder.build();
            StringBuilder url = new StringBuilder(getConfigValue("URL"));
            url.append("/gofer/security-login?j_username=").append(getConfigValue("ADMIN"));
            url.append("&j_password=").append(getConfigValue("PASSWORD"));
            url.append("&j_organization=").append(getConfigValue("ORG"));
            HttpUriRequest request = new HttpGet(url.toString());
            logger.debug("Login " + url.toString());
            try {
                http.execute(request);
            } catch (IOException e) {
                e.printStackTrace();
            }
            url = new StringBuilder(getConfigValue("URL"));
            enrollmentsUrl = url.append("/relution/api/v1/enrollments/").toString();
            devicesUrl = url.append("/relution/api/v1/devices/").toString();
        }
    }

    public List<Map<String, String>> getEnrollments() {
        List<Map<String, String>> response = new ArrayList<>();
        HttpUriRequest request = new HttpGet(enrollmentsUrl);
        try {
            HttpResponse httpResponse = http.execute(request);
            JsonObject ret = Json.createReader(httpResponse.getEntity().getContent()).readObject();
            for (JsonValue device : ret.get("results").asJsonArray()) {
                Map<String, String> tmp = new HashMap<>();
                tmp.put("uuid:", device.asJsonObject().get("uuid").toString());
                tmp.put("name:", device.asJsonObject().get("name").toString());
                tmp.put("link:", device.asJsonObject().get("link").toString());
                tmp.put("validTo:", convertJavaTime(Long.getLong(device.asJsonObject().get("link").toString())));
                tmp.put("qrCodeLink", device.asJsonObject().get("qrCode").asJsonObject().get("link").toString());
                response.add(tmp);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    public CrxResponse addEnrollment(Long deviceId) {
        Device device = this.em.find(Device.class,deviceId);
        return this.addEnrollment(device);
    }


    public CrxResponse addEnrollment(Device device) {
        List<String> parameters = new ArrayList<>();
        parameters.add(device.getName());
        //Create the enrollment as a hash
        Map<String, String> enrollment = new HashMap<>();
        enrollment.put("organizationUuid", getConfigValue("ORG_UUID"));
        enrollment.put("user", "");
        if (device.getOwner() != null) {
            enrollment.put("user", getConfig(device.getOwner(), "mdmUuid"));
            enrollment.put("ownership", "BYOD");
        } else {
            enrollment.put("ownership", "COD");
        }
        if (enrollment.get("user").isEmpty()) {
            enrollment.put("user", "ADMIN_UUID");
        }
        enrollment.put("platform", device.getHwconf().getPartitions().get(0).getOs());
        enrollment.put("comment", device.getMac());

        //Create the post request
        HttpPost request = new HttpPost(enrollmentsUrl);
        try {
            request.setEntity(new StringEntity(createLiteralJson(enrollment)));
            HttpResponse httpResponse = http.execute(request);
            JsonObject ret = Json.createReader(httpResponse.getEntity().getContent()).readObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new CrxResponse(session, "OK", "Enrollment was created succesfully", parameters);
    }
}
