 /* (c) 2020 Péter Varkoly <peter@varkoly.de> - all rights reserved  */
 package de.cranix.services;

 import com.fasterxml.jackson.databind.ObjectMapper;
 import de.cranix.dao.*;
 import de.cranix.helper.CrxSystemCmd;
 import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
 import org.jdom2.Document;
 import org.jdom2.Element;
 import org.jdom2.JDOMException;
 import org.jdom2.filter.ElementFilter;
 import org.jdom2.input.SAXBuilder;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;

 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 import javax.ws.rs.WebApplicationException;
 import java.io.*;
 import java.nio.file.*;
 import java.nio.file.attribute.UserPrincipal;
 import java.nio.file.attribute.UserPrincipalLookupService;
 import java.util.*;

 import static de.cranix.helper.CranixConstants.*;


 @SuppressWarnings("unchecked")
 public class SoftwareService extends Service {

     Logger logger = LoggerFactory.getLogger(SoftwareService.class);
     private static final String SALT_PACKAGE_DIR = "/srv/salt/packages/";
     private static final String SALT_SOURCE_DIR = "/srv/salt/win/repo-ng/";

     public SoftwareService(Session session, EntityManager em) {
         super(session, em);
     }

     /*
      * Functions to create and modify softwares
      */
     public Software getById(long softwareId) {
         try {
             Software software = this.em.find(Software.class, softwareId);
             File f = new File(SALT_SOURCE_DIR + software.getName());
             if (f.exists()) {
                 int count = 0;
                 for (String fileName : f.list()) {
                     if (fileName.equals("init.sls") || fileName.equals("install.xml") || fileName.equals("HASH.json")) {
                         continue;
                     }
                     count++;
                 }
                 software.setSourceAvailable(count > 0);
             } else {
                 software.setSourceAvailable(false);
             }
             return software;
         } catch (Exception e) {
             logger.error(e.getMessage());
             return null;
         } finally {
         }
     }

     public SoftwareVersion getSoftwareVersionById(long id) {
         try {
             return this.em.find(SoftwareVersion.class, id);
         } catch (Exception e) {
             logger.error(e.getMessage());
             return null;
         } finally {
         }
     }

     public SoftwareStatus getSoftwareStatusById(long id) {
         try {
             return this.em.find(SoftwareStatus.class, id);
         } catch (Exception e) {
             logger.error(e.getMessage());
             return null;
         } finally {
         }
     }

     public SoftwareLicense getSoftwareLicenseById(long id) {
         try {
             return this.em.find(SoftwareLicense.class, id);
         } catch (Exception e) {
             logger.error(e.getMessage());
             return null;
         } finally {
         }
     }

     public List<Software> search(String search) {
         try {
             Query query = this.em.createNamedQuery("SoftwareStatus.search").setParameter("search", search);
             return query.getResultList();
         } catch (Exception e) {
             logger.error(e.getMessage());
             return null;
         } finally {
         }
     }

     public CrxResponse add(Software software, Boolean replace) {
         logger.debug("Add software" + software);

         Software oldSoftware = this.getByName(software.getName());
         if (oldSoftware != null) {
             logger.debug("Old software found:" + oldSoftware);
             SoftwareVersion softwareVersion = software.getSoftwareVersions().get(0);
             try {
                 boolean newVersion = true;
                 this.em.getTransaction().begin();
                 if (replace) {
                     for (SoftwareVersion sv : oldSoftware.getSoftwareVersions()) {
                         if (!sv.getVersion().equals(softwareVersion.getVersion())) {
                             sv.setStatus("R");
                             this.em.merge(sv);
                         } else {
                             softwareVersion = sv;
                             softwareVersion.setStatus("C");
                             this.em.merge(sv);
                             newVersion = false;
                         }
                     }
                 }
                 if (newVersion) {
                     softwareVersion = new SoftwareVersion(
                             oldSoftware,
                             software.getSoftwareVersions().get(0).getVersion(),
                             "C"
                     );
                 }
                 /*
                  * Add new full name if this is not already configured
                  */
                 for (SoftwareFullName sfn : software.getSoftwareFullNames()) {
                     boolean newFullName = true;
                     for (SoftwareFullName osfn : oldSoftware.getSoftwareFullNames()) {
                         if (osfn.getFullName().equals(sfn.getFullName())) {
                             newFullName = false;
                             break;
                         }
                     }
                     if (newFullName) {
                         sfn.setSoftware(oldSoftware);
                         oldSoftware.getSoftwareFullNames().add(sfn);
                     }
                 }

                 if (software.getWeight() != null) {
                     oldSoftware.setWeight(software.getWeight());
                 }
                 if (software.getManually() != null) {
                     oldSoftware.setManually(software.getManually());
                 }
                 logger.debug("old software" + oldSoftware);
                 this.em.merge(oldSoftware);
                 this.em.getTransaction().commit();
             } catch (Exception e) {
                 logger.error("Updating the software:" + e.getMessage());
                 return new CrxResponse(this.getSession(), "ERROR", e.getMessage());
             }
             return new CrxResponse(this.getSession(), "OK", "New software version was created succesfully", softwareVersion.getId());
         }
         //This is an new software
         software.setCreator(this.session.getUser());
         if (software.getSoftwareVersions() != null && software.getSoftwareVersions().size() > 0) {
             software.getSoftwareVersions().get(0).setSoftware(software);
         }
         if (software.getSoftwareFullNames() != null && software.getSoftwareFullNames().size() > 0) {
             software.getSoftwareFullNames().get(0).setSoftware(software);
         }
         try {
             this.em.getTransaction().begin();
             this.em.persist(software);
             this.em.getTransaction().commit();
         } catch (Exception e) {
             logger.error(e.getMessage());
             return new CrxResponse(this.getSession(), "ERROR", e.getMessage());
         } finally {
         }
         return new CrxResponse(this.getSession(), "OK", "Software was created succesfully.", software.getId());
     }

     public CrxResponse delete(Long softwareId) {
         try {
             Software software = this.em.find(Software.class, softwareId);
             String softwareName = software.getName();
             if (!this.mayModify(software)) {
                 return new CrxResponse(this.getSession(), "ERROR", "You must not delete this software.");
             }
             this.em.getTransaction().begin();
             if (!this.em.contains(software)) {
                 logger.debug("em does not contains this software.");
                 software = this.em.merge(software);
             }
             this.em.remove(software);
             this.em.getTransaction().commit();
             this.em.getEntityManagerFactory().getCache().evictAll();
             String[] program = new String[5];
             StringBuffer reply = new StringBuffer();
             StringBuffer stderr = new StringBuffer();
             program[0] = "/usr/bin/zypper";
             program[1] = "-nD";
             program[2] = "/srv/salt/repos.d/";
             program[3] = "rm";
             program[4] = "oss-pkg-" + softwareName;
             CrxSystemCmd.exec(program, reply, stderr, null);
             logger.debug("Delete repy: " + reply.toString());
             logger.debug("Delete stderr: " + stderr.toString());
             //TODO Evaluate error message.
         } catch (Exception e) {
             logger.error(e.getMessage());
             e.printStackTrace();
             return new CrxResponse(this.getSession(), "ERROR", e.getMessage());
         } finally {
         }
         return new CrxResponse(this.getSession(), "OK", "Software was deleted succesfully");
     }

     public CrxResponse modify(Software software) {
         try {
             //Modifying only the software entry itself
             this.em.getTransaction().begin();
             this.em.merge(software);
             this.em.getTransaction().commit();
         } catch (Exception e) {
             logger.error(e.getMessage());
             return new CrxResponse(this.getSession(), "ERROR", e.getMessage());
         } finally {
         }
         return new CrxResponse(this.getSession(), "OK", "Software was created succesfully");
     }

     public List<Software> getAll() {
         Query query = this.em.createNamedQuery("Software.findAll");
         List<Software> softwares = new ArrayList<Software>();
         for (Software software : (List<Software>) query.getResultList()) {
             if (!software.getManually()) {
                 File f = new File(SALT_SOURCE_DIR + software.getName());
                 if (f.exists()) {
                     int count = 0;
                     for (String fileName : f.list()) {
                         if (fileName.equals("init.sls") || fileName.equals("install.xml") || fileName.equals("HASH.json")) {
                             continue;
                         }
                         count++;
                     }
                     software.setSourceAvailable(count > 0);
                 } else {
                     software.setSourceAvailable(false);
                 }
                 softwares.add(software);
             } else {
                 software.setSourceAvailable(false);
                 softwares.add(software);
             }
         }
         softwares.sort(Comparator.comparing(Software::getName));
         return softwares;
     }

     public List<Software> getAllInstallable() {
         Query query = this.em.createNamedQuery("Software.findAll");
         List<Software> softwares = new ArrayList<Software>();
         for (Software software : (List<Software>) query.getResultList()) {
             logger.debug("Manually:" + software.getManually());
             if (software.getManually() != true) {
                 software.setSourceAvailable(false);
                 logger.debug("getAllInstallable: " + software);
                 File f = new File(SALT_SOURCE_DIR + software.getName());
                 if (f.exists()) {
                     int count = 0;
                     for (String fileName : f.list()) {
                         if (fileName.equals("init.sls") || fileName.equals("install.xml") || fileName.equals("HASH.json")) {
                             continue;
                         }
                         count++;
                     }
                     if (count > 0) {
                         software.setSourceAvailable(true);
                     }
                     logger.debug("count:" + count);
                 } else {
                     logger.debug("no sources:" + f.getName());
                     f = new File(SALT_PACKAGE_DIR + software.getName() + ".sls");
                     if (f.exists()) {
                         software.setSourceAvailable(true);
                     }
                 }
                 softwares.add(software);
             }
         }
         softwares.sort(Comparator.comparing(Software::getName));
         return softwares;
     }

     public Software getByName(String name) {
         Query query = this.em.createNamedQuery("Software.getByName")
                 .setParameter("name", name);
         logger.debug("getByName try to find:" + name);
         try {
             if (query.getResultList().isEmpty()) {
                 return null;
             }
             return (Software) query.getResultList().get(0);
         } catch (Exception e) {
             logger.error("getByName Error to find:" + name);
             return null;
         }
     }

     /**
      * @param name Search software by name or description
      * @return The found software or null
      */
     public Software getByNameOrDescription(String name) {
         Query query = this.em.createNamedQuery("Software.getByNameOrDescription")
                 .setParameter("name", name)
                 .setParameter("desc", name);
         if (query.getResultList().isEmpty()) {
             return null;
         }
         return (Software) query.getResultList().get(0);
     }

     public Software getByNameOrDescription(String name, String description) {
         Query query = this.em.createNamedQuery("SoftwareFullName.getByName")
                 .setParameter("fullName", description);
         if (!query.getResultList().isEmpty()) {
             SoftwareFullName softwareFullName = (SoftwareFullName) query.getResultList().get(0);
             return softwareFullName.getSoftware();
         }
         query = this.em.createNamedQuery("Software.getByNameOrDescription")
                 .setParameter("name", name)
                 .setParameter("desc", name);
         if (query.getResultList().isEmpty()) {
             query = this.em.createNamedQuery("Software.getByNameOrDescription")
                     .setParameter("name", description)
                     .setParameter("desc", description);
         }
         if (query.getResultList().isEmpty()) {
             return null;
         }
         return (Software) query.getResultList().get(0);
     }

     public List<SoftwareVersion> getAllVersion() {
         Query query = this.em.createNamedQuery("SoftwareVersion.findAll");
         return (List<SoftwareVersion>) query.getResultList();
     }


     /*
      * Functions to interact with the CEPHALIX repository.
      */

     /**
      * Delivers a list from the softwares downlowded from the CEPHALIX server.
      *
      * @return A list of hashes in the format: [
      * { "name":"<package name>",
      * "version":"package version",
      * "description":"...",
      * "update":"version of the new package",
      * "updateDescription":"..." }
      */
     public List<Map<String, String>> listDownloadedSoftware() {
         Map<String, String> software;
         List<Map<String, String>> softwares = new ArrayList<>();
         Map<String, String> updates = new HashMap<String, String>();
         Map<String, String> updatesDescription = new HashMap<String, String>();
         String[] program = new String[9];
         StringBuffer reply = new StringBuffer();
         StringBuffer stderr = new StringBuffer();
         program[0] = "/usr/bin/zypper";
         program[1] = "-nx";
         program[2] = "-D";
         program[3] = "/srv/salt/repos.d/";
         program[4] = "lu";
         program[5] = "-t";
         program[6] = "package";
         program[7] = "-r";
         program[8] = "salt-packages";
         CrxSystemCmd.exec(program, reply, stderr, null);
         try {
             Document doc = new SAXBuilder().build(new StringReader(reply.toString()));
             Element rootNode = doc.getRootElement();
             if (!rootNode.getChildren("update-list").isEmpty()) {
                 if (!rootNode.getChild("update-list").getChildren("update").isEmpty()) {
                     for (Element node : rootNode.getChild("update-list").getChildren("update")) {
                         updates.put(node.getAttributeValue("name").substring(8), node.getAttributeValue("edition"));
                         updatesDescription.put(node.getAttributeValue("name").substring(8), node.getChildText("description"));
                     }
                 }
             }
         } catch (IOException e) {
             logger.error("1" + e.getMessage());
             //throw new WebApplicationException(500);
         } catch (JDOMException e) {
             logger.error("2" + e.getMessage());
             throw new WebApplicationException(500);
         }
         program = new String[5];
         program[0] = "rpm";
         program[1] = "-qa";
         program[2] = "oss-pkg-*";
         program[3] = "--qf";
         program[4] = "%{NAME}##%{SUMMARY}##%{VERSION}\\n";
		 reply = new StringBuffer();
         stderr = new StringBuffer();
         CrxSystemCmd.exec(program, reply, stderr, null);
         logger.debug("Reply" + reply.toString());
         for (String line : reply.toString().split("\\n")) {
             String[] values = line.split("##");
             String name = values[0].substring(8);
             software = new HashMap<String, String>();
             software.put("name", name);
             software.put("description", values[1]);
             software.put("version", values[2]);
             if (updates.containsKey(name)) {
                 software.put("update", updates.get(name));
                 software.put("updateDescription", updatesDescription.get(name));
             }
             softwares.add(software);
         }
         return softwares;
     }

     /**
      * Delivers a list from Software available on the CEPHALIX repository server.
      *
      * @return A list of hashes in the format: [ { "name":"<package name>", "version":"package version" }
      */
     public List<Map<String, String>> getAvailableSoftware() {
         Map<String, String> software;
         List<Map<String, String>> softwares = new ArrayList<Map<String, String>>();
         String[] program = new String[6];
         StringBuffer reply = new StringBuffer();
         StringBuffer stderr = new StringBuffer();
         program[0] = "/usr/bin/zypper";
         program[1] = "-nxD";
         program[2] = "/srv/salt/repos.d/";
         program[3] = "se";
         program[4] = "-sur";
         program[5] = "salt-packages";
         CrxSystemCmd.exec(program, reply, stderr, null);
         try {
             Document doc = new SAXBuilder().build(new StringReader(reply.toString()));
             logger.debug(reply.toString());
             Element rootNode = doc.getRootElement();
             if (rootNode.getChild("search-result") == null) {
                 return softwares;
                 //throw new WebApplicationException(600);
             }
             if (rootNode.getChild("search-result").getChild("solvable-list").getChildren().isEmpty() ||
                     rootNode.getChild("search-result").getChild("solvable-list").getChildren("solvable").isEmpty()) {
                 return softwares;
                 //throw new WebApplicationException(600);
             }
             List<Element> elements = rootNode.getChild("search-result").getChild("solvable-list").getChildren("solvable");
             for (Element node : elements) {
                 software = new HashMap<String, String>();
                 software.put("name", node.getAttributeValue("name").substring(8));
                 /*software.put("description", node.getAttributeValue("kind"));*/
                 software.put("version", node.getAttributeValue("edition"));
                 softwares.add(software);
             }
         } catch (IOException e) {
             logger.error("1 " + reply.toString());
             logger.error("1 " + stderr.toString());
             logger.error("1 " + e.getMessage());
             throw new WebApplicationException(500);
         } catch (JDOMException e) {
             logger.error("2 " + reply.toString());
             logger.error("2 " + stderr.toString());
             logger.error("2 " + e.getMessage());
             throw new WebApplicationException(500);
         }
         return softwares;
     }


     /**
      * Downloads softwares from the CEPHALIX repository server
      *
      * @param softwares List of softwares to download.
      * @return
      */
     public CrxResponse downloadSoftwares(List<String> softwares) {
         File file = null;
         try {
             file = File.createTempFile("crx_download_job", ".crxb", new File(cranixTmpDir));
         } catch (IOException e) {
             logger.error(e.getMessage(), e);
             return new CrxResponse(this.getSession(), "ERROR", e.getMessage());
         }
         StringBuilder command = new StringBuilder();
         command.append("/usr/sbin/crx_download_packages ");
         for (int i = 0; i < softwares.size(); i++) {
             command.append("oss-pkg-").append(softwares.get(i)).append(" ");
         }
         try (PrintWriter out = new PrintWriter(file.toPath().toString())) {
             out.println(command.toString());
         } catch (IOException e) {
             logger.error(e.getMessage(), e);
         }

         String[] program = new String[4];
         StringBuffer reply = new StringBuffer();
         StringBuffer stderr = new StringBuffer();
         program[0] = "at";
         program[1] = "-f";
         program[2] = file.toPath().toString();
         program[3] = "now";
         CrxSystemCmd.exec(program, reply, stderr, null);
         return new CrxResponse(this.getSession(), "OK", "Download of the softwares was started succesfully");
     }

     public CrxResponse refreshSoftwareRepositories() {
         String[] program = new String[6];
         StringBuffer reply = new StringBuffer();
         StringBuffer stderr = new StringBuffer();
         program[0] = "/usr/bin/zypper";
         program[1] = "-D";
         program[2] = "/srv/salt/repos.d/";
         program[3] = "ref";
         program[4] = "-r";
         program[5] = "salt-packages";
         CrxSystemCmd.exec(program, reply, stderr, null);
         return new CrxResponse(this.getSession(), "OK", "Software repositories was refreshed succesfully");
     }

     public List<Map<String, String>> listUpdatesForSoftwarePackages() {
         Map<String, String> software;
         List<Map<String, String>> softwares = new ArrayList<Map<String, String>>();
         this.refreshSoftwareRepositories();
         String[] program = new String[6];
         StringBuffer reply = new StringBuffer();
         StringBuffer stderr = new StringBuffer();
         program[0] = "/usr/bin/zypper";
         program[1] = "-nxD";
         program[2] = "/srv/salt/repos.d/";
         program[3] = "lu";
         program[4] = "-r";
         program[5] = "salt-packages";
         CrxSystemCmd.exec(program, reply, stderr, null);
         try {
             Document doc = new SAXBuilder().build(new StringReader(reply.toString()));
             logger.debug(reply.toString());
             Element rootNode = doc.getRootElement();
             Iterator<Element> processDescendants = rootNode.getDescendants(new ElementFilter("update"));
             while (processDescendants.hasNext()) {
                 Element node = processDescendants.next();
                 software = new HashMap<String, String>();
                 software.put("name", node.getAttributeValue("name").substring(8));
                 /*software.put("description", node.getAttributeValue("kind"));*/
                 software.put("version-old", node.getAttributeValue("edition-old"));
                 software.put("version", node.getAttributeValue("edition"));
                 softwares.add(software);
             }
         } catch (IOException e) {
             logger.error("1 " + reply.toString());
             logger.error("1 " + stderr.toString());
             logger.error("1 " + e.getMessage());
             throw new WebApplicationException(500);
         } catch (JDOMException e) {
             logger.error("2 " + reply.toString());
             logger.error("2 " + stderr.toString());
             logger.error("2 " + e.getMessage());
             throw new WebApplicationException(500);
         }
         return softwares;
     }

     public CrxResponse updateSoftwares(List<String> softwares) {
         File file = null;
         try {
             file = File.createTempFile("crx_update_softwares_job", ".crxb", new File(cranixTmpDir));
         } catch (IOException e) {
             logger.error(e.getMessage(), e);
             return new CrxResponse(this.getSession(), "ERROR", e.getMessage());
         }
         StringBuilder command = new StringBuilder();
         command.append("/usr/sbin/crx_update_packages ");
         for (int i = 0; i < softwares.size(); i++) {
             command.append("oss-pkg-").append(softwares.get(i)).append(" ");
         }
         try (PrintWriter out = new PrintWriter(file.toPath().toString())) {
             out.println(command.toString());
         } catch (IOException e) {
             logger.error(e.getMessage(), e);
         }

         String[] program = new String[4];
         StringBuffer reply = new StringBuffer();
         StringBuffer stderr = new StringBuffer();
         program[0] = "at";
         program[1] = "-f";
         program[2] = file.toPath().toString();
         program[3] = "now";
         CrxSystemCmd.exec(program, reply, stderr, null);
         return new CrxResponse(this.getSession(), "OK", "Update of the softwares was started succesfully");
     }

     public CrxResponse updateSoftwaresDirectly(List<String> softwares) {
         String[] program = new String[7 + softwares.size()];
         StringBuffer reply = new StringBuffer();
         StringBuffer stderr = new StringBuffer();
         program[0] = "/usr/bin/zypper";
         program[1] = "-nx";
         program[2] = "-D";
         program[3] = "/srv/salt/repos.d/";
         program[4] = "up";
         program[5] = "-r";
         program[6] = "salt-packages";
         for (int i = 0; i < softwares.size(); i++) {
             program[7 + i] = "oss-pkg-" + softwares.get(i);
         }
         CrxSystemCmd.exec(program, reply, stderr, null);
         return new CrxResponse(this.getSession(), "OK", "Softwares were updated succesfully");
     }

     public CrxResponse deleteDownloadedSoftwares(List<String> softwares) {
         String[] program = new String[5 + softwares.size()];
         StringBuffer reply = new StringBuffer();
         StringBuffer stderr = new StringBuffer();
         program[0] = "/usr/bin/zypper";
         program[1] = "-nx";
         program[2] = "-D";
         program[3] = "/srv/salt/repos.d/";
         program[4] = "rm";
         for (int i = 0; i < softwares.size(); i++) {
             program[5 + i] = softwares.get(i);
         }
         CrxSystemCmd.exec(program, reply, stderr, null);
         return new CrxResponse(this.getSession(), "OK", "Softwares were updated succesfully");
     }

     /*
      * Functions to deliver installation status
      */
     public List<Map<String, String>> statistic() {
         List<Map<String, String>> softwareStatusList = new ArrayList<Map<String, String>>();
         Map<String, String> statusMap = new HashMap<>();
         Query query;
         Integer count;

         query = this.em.createNamedQuery("SoftwareStatus.findByStatus").setParameter("STATUS", "I");
         count = query.getResultList().size();
         statusMap.put("name", "Installed");
         statusMap.put("count", count.toString());
         softwareStatusList.add(statusMap);

         query = this.em.createNamedQuery("SoftwareStatus.findByStatus").setParameter("STATUS", "IS");
         count = query.getResultList().size();
         statusMap = new HashMap<>();
         statusMap.put("name", "Installation Scheduled");
         statusMap.put("count", count.toString());
         softwareStatusList.add(statusMap);

         query = this.em.createNamedQuery("SoftwareStatus.findByStatus").setParameter("STATUS", "IF");
         count = query.getResultList().size();
         statusMap = new HashMap<>();
         statusMap.put("name", "Installation Failed");
         statusMap.put("count", count.toString());
         softwareStatusList.add(statusMap);

         query = this.em.createNamedQuery("SoftwareStatus.findByStatus").setParameter("STATUS", "MI");
         count = query.getResultList().size();
         statusMap = new HashMap<>();
         statusMap.put("name", "Manually Installed");
         statusMap.put("count", count.toString());
         softwareStatusList.add(statusMap);

         query = this.em.createNamedQuery("SoftwareStatus.findByStatus").setParameter("STATUS", "MD");
         count = query.getResultList().size();
         statusMap = new HashMap<>();
         statusMap.put("name", "Manually Deinstalled");
         statusMap.put("count", count.toString());
         softwareStatusList.add(statusMap);

         query = this.em.createNamedQuery("SoftwareStatus.findByStatus").setParameter("STATUS", "LM");
         count = query.getResultList().size();
         statusMap = new HashMap<>();
         statusMap.put("name", "Licence Missing");
         statusMap.put("count", count.toString());
         softwareStatusList.add(statusMap);

         return softwareStatusList;
     }

     /**
      * Return a list of all SofwareStatus objects to a given installation status.
      *
      * @return The list of searched SofwareStatus.
      * @see SoftwareStatus
      */
     public List<SoftwareStatus> getAllStatus() {
         Query query = this.em.createNamedQuery("SoftwareStatus.findAll");
         List<SoftwareStatus> sts = new ArrayList<SoftwareStatus>();
         for (SoftwareStatus st : (List<SoftwareStatus>) query.getResultList()) {
             if( st == null ) {
                 logger.error("SoftwareStatus null");
                 continue;
             }
             if( st.getSoftwareVersion() == null ) {
                 logger.error("SoftwareVersion null" + st);
                 continue;
             }
             if( st.getSoftwareVersion().getSoftware() == null ) {
                 logger.error("Software null" + st + st.getSoftwareVersion() );
                 continue;
             }
             st.setSoftwareName(st.getSoftwareVersion().getSoftware().getName());
             st.setVersion(st.getSoftwareVersion().getVersion());
             st.setDeviceName(st.getDevice().getName());
             st.setRoomName(st.getDevice().getRoom().getName());
             st.setSoftwareId(st.getSoftwareVersion().getSoftware().getId());
             sts.add(st);
         }
         return sts;
     }

     /**
      * This is the first step to start the installation. An installation category will be created
      *
      * @param        category A category object containing the name and description of the category
      * @return The result in a OssResult.
      */
     public CrxResponse createInstallationCategory(Category category) {
         CategoryService categoryService = new CategoryService(this.session, this.em);
         category.setCategoryType("installation");
         category.setPublicAccess(false);
         return categoryService.add(category);
     }

     /**
      * Add a software to an installation set (category with categoryType installation)
      *
      * @param softwareId The technical id of the software.
      * @param categoryId The technical if of the installation set.
      * @return The result in a OssResult.
      */
     public CrxResponse addSoftwareToCategory(Long softwareId, Long categoryId) {
         try {
             Software s = this.em.find(Software.class, softwareId);
             Category c = this.em.find(Category.class, categoryId);
             if (c.getSoftwares().contains(s)) {
                 return new CrxResponse(this.getSession(), "OK", "Software was already added to the installation.");
             }
             s.getCategories().add(c);
             c.getSoftwares().add(s);
             s.getRemovedFromCategories().remove(c);
             c.getRemovedSoftwares().remove(s);
             this.em.getTransaction().begin();
             this.em.merge(s);
             this.em.merge(c);
             this.em.getTransaction().commit();
         } catch (Exception e) {
             logger.error(e.getMessage());
			 return new CrxResponse(this.getSession(), "addSoftwareToCategory ERROR", e.getMessage());
         } finally {
         }
         return new CrxResponse(this.getSession(), "OK", "Software was added to the installation succesfully.");
     }

     /**
      * Delete a software from an installation set (category with categoryType installation)
      *
      * @param softwareId The technical id of the software.
      * @param categoryId The technical if of the installation set.
      * @return The result in a OssResult.
      */
     public CrxResponse deleteSoftwareFromCategory(Long softwareId, Long categoryId) {
         try {
             Software s = this.em.find(Software.class, softwareId);
             Category c = this.em.find(Category.class, categoryId);
             if (!c.getSoftwares().contains(s)) {
                 return new CrxResponse(this.getSession(), "OK", "Software is not member of the installation.");
             }
             s.getCategories().remove(c);
             c.getSoftwares().remove(s);
             s.getRemovedFromCategories().add(c);
             c.getRemovedSoftwares().add(s);
             this.em.getTransaction().begin();
             this.em.merge(s);
             this.em.merge(c);
             this.em.getTransaction().commit();
             for (Room r : c.getRooms()) {
                 for (Device d : r.getDevices()) {
                     this.modifySoftwareStatusOnDevice(d, s, "", "deinstallation_scheduled");
                 }
             }
         } catch (Exception e) {
             return new CrxResponse(this.getSession(), "ERROR", e.getMessage());
         } finally {
         }
         return new CrxResponse(this.getSession(), "OK", "SoftwareState was added to category succesfully");
     }

     /**
      * Add a software license to a software
      *
      * @param softwareLicense          The software license to be created.
      * @param softwareId               The technical id of the software.
      * @param fileInputStream          If licenseType is C this contains a list of licenses. If licenseType is F this contains the license file.
      * @param contentDispositionHeader If licenseType is F this contains the name of the license file.
      * @return The result in a OssResult.
      */
     public CrxResponse addLicenseToSoftware(SoftwareLicense softwareLicense,
                                             Long softwareId,
                                             InputStream fileInputStream,
                                             FormDataContentDisposition contentDispositionHeader
     ) {
         Software software = this.getById(softwareId);
         softwareLicense.setCreator(this.session.getUser());
         if (softwareLicense.getLicenseType().equals('F')) {
             try {
                 this.em.getTransaction().begin();
                 softwareLicense.setSoftware(software);
                 this.em.persist(softwareLicense);
                 software.getSoftwareLicenses().add(softwareLicense);
                 this.em.merge(software);
                 this.em.getTransaction().commit();
             } catch (Exception e) {
                 return new CrxResponse(this.getSession(), "ERROR", e.getMessage());
             } finally {
             }
             return this.uploadLicenseFile(softwareLicense, fileInputStream, contentDispositionHeader);
         }
         if (softwareLicense.getLicenseType().equals('C') && fileInputStream == null) {
             try {
                 this.em.getTransaction().begin();
                 softwareLicense.setSoftware(software);
                 this.em.persist(softwareLicense);
                 software.getSoftwareLicenses().add(softwareLicense);
                 this.em.merge(software);
                 this.em.getTransaction().commit();
             } catch (Exception e) {
                 return new CrxResponse(this.getSession(), "ERROR", e.getMessage());
             } finally {
             }
         } else {
             File file = null;
             try {
                 file = File.createTempFile("crx_uploadFile", ".crxb", new File(cranixTmpDir));
                 Files.copy(fileInputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                 this.em.getTransaction().begin();
                 for (String line : Files.readAllLines(file.toPath())) {
                     SoftwareLicense sl = new SoftwareLicense();
                     String[] lic = line.split(";");
                     sl.setLicenseType('C');
                     sl.setValue(lic[0]);
                     sl.setSoftware(software);
                     if (lic.length == 1) {
                         sl.setCount(1);
                     } else {
                         sl.setCount(Integer.parseInt(lic[1]));
                     }
                     this.em.persist(sl);
                     software.getSoftwareLicenses().add(sl);
                 }
                 this.em.merge(software);
                 this.em.getTransaction().commit();
                 Files.delete(file.toPath());
             } catch (IOException e) {
                 logger.error(e.getMessage(), e);
                 return new CrxResponse(this.getSession(), "ERROR", e.getMessage());
             } finally {
             }
         }
         return new CrxResponse(this.getSession(), "OK", "License was added to the software succesfully");
     }

     /*
      * Modify an existing license
      */
     public CrxResponse modifySoftwareLicense(
             SoftwareLicense softwareLicense,
             InputStream fileInputStream,
             FormDataContentDisposition contentDispositionHeader
     ) {
         SoftwareLicense oldLicense = this.getSoftwareLicenseById(softwareLicense.getId());
         if (oldLicense == null) {
             throw new WebApplicationException(404);
         }
         oldLicense.setCount(softwareLicense.getCount());
         oldLicense.setValue(softwareLicense.getValue());
         try {
             this.em.getTransaction().begin();
             this.em.merge(oldLicense);
             this.em.getTransaction().commit();
         } catch (Exception e) {
             return new CrxResponse(this.getSession(), "ERROR", e.getMessage());
         } finally {
         }
         if (softwareLicense.getLicenseType().equals('F') && fileInputStream != null) {
             return this.uploadLicenseFile(softwareLicense, fileInputStream, contentDispositionHeader);
         }
         return new CrxResponse(this.getSession(), "OK", "License was modified succesfully");
     }

     /*
      * Upload a license file to an existing license.
      */
     public CrxResponse uploadLicenseFile(
             SoftwareLicense softwareLicense,
             InputStream fileInputStream,
             FormDataContentDisposition contentDispositionHeader) {
         try {
             String fileName = contentDispositionHeader.getFileName();
             StringBuilder newFileName = new StringBuilder(SALT_PACKAGE_DIR);
             newFileName.append(softwareLicense.getSoftware().getName());
             File newFile = new File(newFileName.toString());
             Files.createDirectories(newFile.toPath());
             newFileName.append("/").append(fileName);
             newFile = new File(newFileName.toString());
             Files.copy(fileInputStream, newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
             softwareLicense.setValue(fileName);
             this.em.getTransaction().begin();
             this.em.merge(softwareLicense);
             this.em.getTransaction().commit();
         } catch (IOException e) {
             logger.error(e.getMessage(), e);
             throw new WebApplicationException(500);
         } finally {
         }
         return new CrxResponse(this.getSession(), "OK", "Software License File was uploaded succesfully");
     }

     public CrxResponse deleteLicence(long licenseId) {
         try {
             this.em.getTransaction().begin();
             SoftwareLicense sl = this.em.find(SoftwareLicense.class, licenseId);
             Software software = sl.getSoftware();
             software.getSoftwareLicenses().remove(sl);
             this.em.merge(software);
             this.em.refresh(sl);
             this.em.getTransaction().commit();
         } catch (Exception e) {
             logger.error(e.getMessage());
             return new CrxResponse(this.getSession(), "ERROR", e.getMessage());
         } finally {
         }
         return new CrxResponse(this.getSession(), "OK", "Software license was deleted successfully");
     }

     /*
      * Return the next free license
      */
     private SoftwareLicense getNextFreeLicenseId(Software software) {
         for (SoftwareLicense softwareLicense : software.getSoftwareLicenses()) {
             if (softwareLicense.getCount() > softwareLicense.getDevices().size()) {
                 return softwareLicense;
             }
         }
         return null;
     }

     /*
      * Add Licenses to a device
      */
     public CrxResponse addSoftwareLicenseToDevices(Software software, Device device) {

         for (SoftwareLicense myLicense : device.getSoftwareLicenses()) {
             if (myLicense.getSoftware().equals(software)) {
                 return new CrxResponse(this.getSession(), "OK", "License was already added to the device.");
             }
         }
         SoftwareLicense softwareLicense = this.getNextFreeLicenseId(software);
         if (softwareLicense == null) {
             return new CrxResponse(this.getSession(), "ERROR", "There is not enough licences.");
         } else {
             try {
                 this.em.getTransaction().begin();
                 device.getSoftwareLicenses().add(softwareLicense);
                 softwareLicense.getDevices().add(device);
                 this.em.getTransaction().commit();
             } catch (Exception e) {
                 logger.error(e.getMessage());
                 return new CrxResponse(this.getSession(), "ERROR", e.getMessage());
             } finally {
             }
         }
         return new CrxResponse(this.getSession(), "OK", "License was added to the device succesfully.");
     }

     /*
      *  Delete Licenses from a device
      */
     public CrxResponse deleteSoftwareLicenseFromDevice(Software software, Device device) {

         for (SoftwareLicense myLicense : device.getSoftwareLicenses()) {
             if (myLicense.getSoftware().equals(software)) {
                 try {
                     this.em.getTransaction().begin();
                     device.getSoftwareLicenses().remove(myLicense);
                     myLicense.getDevices().remove(device);
                     this.em.merge(device);
                     this.em.merge(myLicense);
                     this.em.getTransaction().commit();
                 } catch (Exception e) {
                     logger.error(e.getMessage());
                     return new CrxResponse(this.getSession(), "ERROR", e.getMessage());
                 } finally {
                 }
                 return new CrxResponse(this.getSession(), "OK", "License was removed from device.");
             }
         }
         return new CrxResponse(this.getSession(), "OK", "No license on thise device.");
     }
     /*
      * Sets the software status on a device to a given version and remove the other status.
      */

     /*
      * Sets the software status on a device to a given version and remove the other status.
      */
     public void setSoftwareStatusOnDevice(Device d, Software s, String version, String status) {
         List<SoftwareVersion> lsv;

         List<SoftwareStatus> lss = new ArrayList<SoftwareStatus>();
         try {
             for (SoftwareStatus ss : d.getSoftwareStatus()) {
                 if (ss == null
                         || ss.getSoftwareVersion() == null
                         || ss.getSoftwareVersion().getSoftware() == null) {
                     logger.error("setSoftwareStatusOnDevice ERROR"
                             + " Device:" + d
                             + " Software:" + s
                             + " Version:" + version
                             + " Status:" + status);
                     continue;
                 }
                 if (ss.getSoftwareVersion().getSoftware().equals(s)) {
                     lss.add(ss);
                 }
             }
             this.em.getTransaction().begin();
             if (!lss.isEmpty()) {
                 for (SoftwareStatus ss : lss) {
                     if (ss.getSoftwareVersion().getVersion().equals(version)) {
                         ss.setStatus(status);
                         this.em.merge(ss);
                     } else {
                         this.em.remove(ss);
                     }
                 }
             } else {
                 lsv = s.getSoftwareVersions();
                 if (lsv.isEmpty()) {
                     SoftwareVersion sv = new SoftwareVersion(s, version, "U");
                     this.em.persist(sv);
                     SoftwareStatus ss = new SoftwareStatus(d, sv, status);
                     this.em.persist(ss);
                     d.getSoftwareStatus().add(ss);
                     this.em.merge(d);
                 } else {
                     for (SoftwareVersion sv : lsv) {
                         SoftwareStatus ss = new SoftwareStatus(d, sv, status);
                         this.em.persist(ss);
                         d.getSoftwareStatus().add(ss);
                         this.em.merge(d);
                     }
                 }
             }
             this.em.getTransaction().commit();
         } catch (Exception e) {
             logger.error(e.getMessage());
         } finally {
         }
     }

     public List<SoftwareStatus> getSoftwareStatusListFromDevice(Device d, Software s, String version) {
         List<SoftwareStatus> lss = new ArrayList<SoftwareStatus>();
         for (SoftwareStatus ss : d.getSoftwareStatus()) {
             if (ss.getSoftwareVersion().getSoftware().equals(s)) {
                 if (version.isEmpty() || ss.getSoftwareVersion().getVersion().equals(version)) {
                     lss.add(ss);
                 }
             }
         }
         return lss;
     }

     /*
      * Modify the software status on a device. If there is no status nothing will be happenend.
      */
     public void modifySoftwareStatusOnDevice(Device d, Software s, String version, String status) {
         try {
             this.em.getTransaction().begin();
             for (SoftwareStatus ss : this.getSoftwareStatusListFromDevice(d, s, version)) {
                 ss.setStatus(status);
                 this.em.merge(ss);
             }
             this.em.getTransaction().commit();
         } catch (Exception e) {
             logger.error(e.getMessage());
         } finally {
         }
     }

     /*
      * Remove the software status from a device.
      */
     public void removeSoftwareStatusOnDevice(Device d, Software s, String version) {
         try {
             this.em.getTransaction().begin();
             for (SoftwareStatus ss : this.getSoftwareStatusListFromDevice(d, s, version)) {
                 this.em.remove(ss);
             }
             this.em.getTransaction().commit();
         } catch (Exception e) {
             logger.error(e.getMessage());
         } finally {
         }
     }

     /*
      * Reads the software status to a given version of a software on a device.
      */
     public String getSoftwareStatusOnDevice(Device d, Software s, String version) {
         List<SoftwareStatus> lss = this.getSoftwareStatusListFromDevice(d, s, version);
         if (lss.isEmpty()) {
             return "";
         }
         return lss.get(0).getStatus();
     }

     /*
      * Checks if there is a software status to a given version of a software on a device.
      *
      * @param   d     The concerning device
      * @param   s     The software
      * @param   state The status we are looking for
      */
     public boolean checkSoftwareStatusOnDevice(Device d, Software s, String state) {
         for (SoftwareStatus ss : d.getSoftwareStatus()) {
             if (ss.getStatus().equals(state) &&
                     ss.getSoftwareVersion().getSoftware().equals(s)) {
                 return true;
             }
         }
         return false;
     }

     /*
      * Checks if there is a software is installed on a device.
      *
      * @param   d     The concerning device
      * @param   s     The software
      */
     public boolean isSoftwareInstalledOnDevice(Device d, Software s) {
         for (SoftwareStatus ss : d.getSoftwareStatus()) {
             if ((ss.getStatus().equals("I") || ss.getStatus().equals("IM")) &&
                     ss.getSoftwareVersion().getSoftware().equals(s)) {
                 return true;
             }
         }
         return false;
     }

     /*
      * Checks if there is a software version status to a given version of a software on a device.
      *
      * @param   d     The concerning device
      * @param   sv    The software version
      * @param   state The status we are looking for
      */
     public boolean checkSoftwareVersionStatusOnDevice(Device d, SoftwareVersion sv, String state) {
         for (SoftwareStatus ss : d.getSoftwareStatus()) {
             if (ss.getStatus().equals(state) &&
                     ss.getSoftwareVersion().equals(sv)) {
                 return true;
             }
         }
         return false;
     }

     /*
      * Checks if there is a software status to a given version of a software on a device.
      */
     public boolean getSoftwareVersionOnDevice(Device d, Software s) {
         return !this.getSoftwareStatusListFromDevice(d, s, "").isEmpty();
     }

     /*
      * Set the software status on a device to a defined software version
      */
     public void setInstallUpdateOnDevice(Software software, SoftwareVersion softwareVersion, Device device) {
         try {
             boolean update = false;
             boolean newSoftware = true;
             this.em.getTransaction().begin();
             for (SoftwareStatus ss : device.getSoftwareStatus()) {
                 if (ss.getSoftwareVersion().getSoftware().equals(software)) {
                     if (!ss.getSoftwareVersion().equals(softwareVersion)) {
                         ss.setStatus("US");
                         update = true;
                     } else {
                         newSoftware = false;
                     }
                 }
             }
             if (newSoftware) {
                 SoftwareStatus softwareStatus = new SoftwareStatus(device, softwareVersion, "IS");
                 device.getSoftwareStatus().add(softwareStatus);
             }
             if (newSoftware || update) {
                 this.em.merge(device);
             }
             this.em.getTransaction().commit();
         } catch (Exception e) {
             logger.error("Error in setInstallUpdateOnDevice: " + device.getName() + " ERROR " + e.getMessage());
         }
     }

     public CrxResponse rewriteTopSls() {
         final String domainName = this.getConfigValue("DOMAIN");
         List<String> topSls = new ArrayList<String>();
         Path SALT_TOP_TEMPL = Paths.get(cranixBaseDir + "templates/top.sls");
         List<String> usedSlsFiles = new ArrayList<String>();
         if (Files.exists(SALT_TOP_TEMPL)) {
             try {
                 topSls = Files.readAllLines(SALT_TOP_TEMPL);
             } catch (java.nio.file.NoSuchFileException e) {
                 logger.error(e.getMessage());
             } catch (IOException e) {
                 logger.error(e.getMessage());
             }
         } else {
             topSls.add("base:");
         }
         for (Device device : new DeviceService(this.session, this.em).getAll()) {
             //We only create salt files for FatClients
             if (device.getHwconf() == null || !device.getHwconf().getDeviceType().equals("FatClient")) {
                 continue;
             }
             StringBuilder firstLine = new StringBuilder();
             firstLine.append("  ").append(device.getName()).append(".").append(domainName).append(":");
             topSls.add(firstLine.toString());
             topSls.add("    - crx_device_" + device.getName());
             usedSlsFiles.add("crx_device_" + device.getName() + ".sls");
         }
         //Remove not used device sls files
         File saltDir = new File("/srv/salt/");
         for (File f : saltDir.listFiles()) {
	     if( f.getName().startsWith("crx_device_") && !usedSlsFiles.contains(f.getName())) {
                 f.delete();
             }
         }
         if (topSls.size() > 0) {
             Path SALT_TOP = Paths.get("/srv/salt/top.sls");
             try {
                 Files.write(SALT_TOP, topSls);
             } catch (IOException e) {
                 e.printStackTrace();
             }
             logger.debug("restart salt-master");
             this.systemctl("try-restart", "salt-master");
             logger.debug("Services restarted");
         }
         return new CrxResponse(this.session,"OK","Salt configuration was rewritten.");
     }

     /*
      * Save the software status what shall be installed to host sls files.
      */
     public CrxResponse applySoftwareStateToHosts() {
         return applySoftwareStateToHosts(new DeviceService(this.session, this.em).getAll());
     }

     public CrxResponse applySoftwareStateToHosts(List<Device> devices) {
         DeviceService deviceService = new DeviceService(this.session, this.em);
         Map<String, List<String>>   softwaresToInstall = new HashMap<>();
         Map<String, List<Software>> softwaresToRemove = new HashMap<>();
         Map<String, Boolean>        softwareMustBeIncluded = new HashMap<>();
         List<String> toInstall      = new ArrayList<String>();
         List<Software> toRemove     = new ArrayList<Software>();
         final String domainName     = this.getConfigValue("DOMAIN");
         StringBuilder errorMessages = new StringBuilder();
         String registerPassword     = this.getProperty("de.cranix.dao.User.Register.Password");
         UserPrincipalLookupService lookupService = FileSystems.getDefault().getUserPrincipalLookupService();
         UserPrincipal saltPrincipial = null;
         try {
             saltPrincipial = lookupService.lookupPrincipalByName("salt");
         } catch (IOException e) {
             logger.error(e.getMessage());
             return new CrxResponse(session, "ERROR", "Can not get salt's user principal.");
         }
         //Collect the corresponding Rooms and Hwconfs
         List<HWConf> hwconfs = new ArrayList<HWConf>();
         List<Room>   rooms   = new ArrayList<Room>();
         List<String> domainJoinSls = new ArrayList<String>();
         domainJoinSls.add(domainName + ":");
         domainJoinSls.add("  system.join_domain:");
         domainJoinSls.add("    - username: register");
         domainJoinSls.add("    - password: " + registerPassword);
         domainJoinSls.add("    - restart: True");
         Path DOMAIN_JOIN = Paths.get("/srv/salt/domain_join.sls");
         try {
             Files.write(DOMAIN_JOIN, domainJoinSls);
             Files.setPosixFilePermissions(DOMAIN_JOIN, groupReadDirPermission);
             Files.setOwner(DOMAIN_JOIN, saltPrincipial);
         } catch (IOException e) {
             logger.error(e.getMessage());
         }
         for (Device device : devices) {
             if (device == null || device.getHwconf() == null || device.getRoom() == null) {
                 continue;
             }
             if (!hwconfs.contains(device.getHwconf())) {
                 hwconfs.add(device.getHwconf());
             }
             if (!rooms.contains(device.getRoom())) {
                 rooms.add(device.getRoom());
             }
         }

         /* Analyse all installable software if these are packaged */
         Query query = this.em.createNamedQuery("Software.findAll");
         for (Software software : (List<Software>) query.getResultList()) {
             if (!software.getManually()) {
                 StringBuilder filePath = new StringBuilder(SALT_PACKAGE_DIR);
                 filePath.append(software.getName()).append(".sls");
                 File file = new File(filePath.toString());
                 softwareMustBeIncluded.put(software.getName(), file.exists());
             }
         }
         //Evaluate device categories
         logger.debug("Process devices");
         for (Device device : devices) {
             if (device.getHwconf() == null || !device.getHwconf().getDeviceType().equals("FatClient")) {
                 continue;
             }
             toInstall = new ArrayList<String>();
             toRemove = new ArrayList<Software>();
             for (Category category : device.getCategories()) {
                 if (category.getCategoryType().equals("installation")) {
                     for (Software software : category.getRemovedSoftwares()) {
                         toRemove.add(software);
                     }
                 }
             }
             for (Category category : device.getCategories()) {
                 if (category.getCategoryType().equals("installation")) {
                     for (Software software : category.getSoftwares()) {
                         toRemove.remove(software);
                         for (Software requirements : software.getSoftwareRequirements()) {
                             toRemove.remove(requirements);
                             toInstall.add(String.format("%04d-%s", requirements.getWeight(), requirements.getName()));
                         }
                         toInstall.add(String.format("%04d-%s", software.getWeight(), software.getName()));
                     }
                 }
             }
             softwaresToInstall.put(device.getName(), toInstall);
             softwaresToRemove.put(device.getName(), toRemove);
         }
         try {
             logger.debug("Software map after devices:" + softwaresToInstall);
         } catch (Exception e) {
             logger.error(e.getMessage());
             return null;
         }

         //Evaluate room categories
         logger.debug("Process rooms");
         for (Room room : rooms) {
             toRemove = new ArrayList<Software>();
             toInstall = new ArrayList<String>();
             /* Search for software to be removed */
             for (Category category : room.getCategories()) {
                 if (category.getCategoryType().equals("installation")) {
                     for (Software software : category.getRemovedSoftwares()) {
                         toRemove.add(software);
                     }
                 }
             }
             /* Search for software to be installed */
             for (Category category : room.getCategories()) {
                 if (category.getCategoryType().equals("installation")) {
                     for (Software software : category.getSoftwares()) {
                         toRemove.remove(software);
                         for (Software requirements : software.getSoftwareRequirements()) {
                             toRemove.remove(requirements);
                             toInstall.add(String.format("%04d-%s", requirements.getWeight(), requirements.getName()));
                         }
                         toInstall.add(String.format("%04d-%s", software.getWeight(), software.getName()));
                     }

                 }
             }
             /* Assign result to the devices in room */
             for (Device device : room.getDevices()) {
                 if (!devices.contains(device) || device.getHwconf() == null || !device.getHwconf().getDeviceType().equals("FatClient")) {
                     continue;
                 }
                 softwaresToInstall.get(device.getName()).addAll(toInstall);
                 softwaresToRemove.get(device.getName()).addAll(toRemove);
             }
         }

         //Evaluate hwconf categories
         logger.debug("Process hwconfs");
         for (HWConf hwconf : hwconfs) {
             if (hwconf == null) {
                 continue;
             }
             logger.debug("HWConfs: " + hwconf.getName() + " " + hwconf.getDeviceType());
             if (!hwconf.getDeviceType().equals("FatClient")) {
                 continue;
             }
             toRemove = new ArrayList<Software>();
             toInstall = new ArrayList<String>();
             /* Search for software to be removed */
             for (Category category : hwconf.getCategories()) {
                 if (category.getCategoryType().equals("installation")) {
                     for (Software software : category.getRemovedSoftwares()) {
                         toRemove.add(software);
                     }
                 }
             }
             /* Search for software to be installed */
             for (Category category : hwconf.getCategories()) {
                 logger.debug("HWConfs Categories: " + category.getName() + " " + category.getCategoryType());
                 if (category.getCategoryType().equals("installation")) {
                     for (Software software : category.getSoftwares()) {
                         toRemove.remove(software);
                         for (Software requirements : software.getSoftwareRequirements()) {
                             toRemove.remove(requirements);
                             toInstall.add(String.format("%04d-%s", requirements.getWeight(), requirements.getName()));
                         }
                         toInstall.add(String.format("%04d-%s", software.getWeight(), software.getName()));
                     }
                 }
             }
             /* Assign result to the devices in hwconf */
             for (Device device : hwconf.getDevices()) {
                 if (!devices.contains(device)) {
                     continue;
                 }
                 softwaresToInstall.get(device.getName()).addAll(toInstall);
                 softwaresToRemove.get(device.getName()).addAll(toRemove);
             }
         }
         try {
             logger.debug("Software map:" + softwaresToInstall);
         } catch (Exception e) {
             logger.error(e.getMessage());
             return null;
         }

         //Write the hosts sls files
         logger.debug("Process collected datas:");
         for (Device device : deviceService.getAll()) {
             //We only create salt files for FatClients
             if (device.getHwconf() == null || !device.getHwconf().getDeviceType().equals("FatClient")) {
                 continue;
             }
             if (!devices.contains(device)) {
                 continue;
             }

             logger.debug("Processing:" + device.getName());
             List<String> deviceRemove = new ArrayList<String>();
             List<String> deviceInstall = new ArrayList<String>();
             List<String> deviceCrxInst = new ArrayList<String>();
             deviceCrxInst.add("  - ntp_conf");
             deviceRemove.add("packages.toremove:");
             deviceRemove.add("  pkg.removed:");
             deviceRemove.add("    - pkgs:");

             //Remove first the softwares.
             if (softwaresToRemove.containsKey(device.getName())) {
                 List<Software> removed = new ArrayList<Software>();
                 for (Software software : softwaresToRemove.get(device.getName())) {
                     if (!removed.contains(software)) {
                         this.setSoftwareStatusOnDevice(device, software, "", "DS");
                         deviceRemove.add("       - " + software.getName());
                         this.deleteSoftwareLicenseFromDevice(software, device);
                         removed.add(software);
                     }
                 }
             }
             if (softwaresToInstall.containsKey(device.getName())) {
                 softwaresToInstall.get(device.getName()).sort((String s1, String s2) -> {
                     return s2.compareTo(s1);
                 });
             }
             //Add packages to install
             List<String> normalizeSoftware = new ArrayList<String>();
             for (String softwareKey : softwaresToInstall.get(device.getName())) {
                 String softwareName = softwareKey.substring(5);
                 //Take care to install software only once
                 if (normalizeSoftware.contains(softwareName)) {
                     continue;
                 } else {
                     normalizeSoftware.add(softwareName);
                 }
                 Software software = this.getByName(softwareName);
                 if (software == null) {
                     continue;
                 }
                 SoftwareVersion softwareVersion = null;
                 for (SoftwareVersion sv : software.getSoftwareVersions()) {
                     if (sv.getStatus().equals("C")) {
                         softwareVersion = sv;
                         break;
                     }
                 }
                 //Allocate license to device
                 if (!software.getSoftwareLicenses().isEmpty()) {
                     if (!this.addSoftwareLicenseToDevices(software, device).getCode().equals("OK")) {
                         //There is no license we can not install this.
                         errorMessages.append("No license for ").append(softwareName).append(" on ").append(device.getName()).append(this.getNl());
                         this.setSoftwareStatusOnDevice(device, software, softwareVersion.getVersion(), "LM");
                         continue;
                     }
                 }
                 // Set the software version status on device if not the actual version is already installed
                 // The version status can be US or IS
                 if (!this.checkSoftwareVersionStatusOnDevice(device, softwareVersion, "I")) {
                     logger.debug("software " + softwareName + " must be installed or updated");
                     this.setInstallUpdateOnDevice(software, softwareVersion, device);
                 }
                 /* If a software has an own sls file outside of the package directory
                  * we have to include this files */
                 if (softwareMustBeIncluded.get(softwareName)) {
					/* for( SoftwareLicense sl : device.getSoftwareLicenses() ) {
						if( sl.getSoftware().equals(software) ) {
							deviceGrains.add(softwareName + "_KEY");
							deviceGrains.add("  grains.present:");
							deviceGrains.add("    - value: " + sl.getValue());
						}
					}*/
                     /*
                      * TODO to implement frozen versions.
                      */
                     deviceCrxInst.add("  - " + softwareName);
                 } else {
                     deviceInstall.add(softwareName + ":");
                     deviceInstall.add("  - pkg:");
                     deviceInstall.add("    - installed");
                 }
             }
             List<String> deviceSls = new ArrayList<String>();
             deviceSls.add(device.getName() + ":");
             deviceSls.add("  system.computer_name: []");
             if (device.getHwconf().isDomainjoin()) {
                 deviceCrxInst.add("  - domain_join");
             }
             if (deviceRemove.size() > 3) {
                 deviceSls.addAll(deviceRemove);
             }
             deviceSls.addAll(deviceInstall);
             //deviceSls.addAll(deviceGrains);
             if (deviceCrxInst.size() > 0) {
                 deviceSls.add("include:");
                 deviceSls.addAll(deviceCrxInst);
             }

             if (deviceSls.size() > 0) {
                 Path SALT_DEV = Paths.get("/srv/salt/crx_device_" + device.getName() + ".sls");
                 try {
                     Files.write(SALT_DEV, deviceSls);
                     Files.setPosixFilePermissions(SALT_DEV, groupReadDirPermission);
                     Files.setOwner(SALT_DEV, saltPrincipial);
                 } catch (IOException e) {
                     logger.error(e.getMessage());
                 }
             }
         }
         logger.debug("write top.sls");
         this.rewriteTopSls();
         if (errorMessages.length() > 0) {
             logger.error(errorMessages.toString());
             return new CrxResponse(this.getSession(), "ERROR", errorMessages.toString());
         }
         return new CrxResponse(this.getSession(), "OK", "Software State was saved succesfully");
     }

     /**
      * Sets the software installation status on a device and remove the status of older version if the status is installed.
      *
      * @param description The description (display name) of the software
      * @param    device            The corresponding device object
      * @param    softwareName    Name of the corresponding software package
      * @param    version            The version of the corresponding software
      * @param    status            The state to be set
      * @return An CrxResponse object will be responded
      */
     public CrxResponse setSoftwareStatusOnDevice(Device device, String softwareName, String description, String version, String status) {
         SoftwareStatus softwareStatus = null;
         Software software = this.getByNameOrDescription(softwareName, description);
         SoftwareVersion softwareVersion = null;
         logger.debug("setSoftwareStatusOnDevice called: " + softwareName + " ## " + version + " ## " + status);
         if (software == null) {
             // Software does not exist. It is a manually installed software.
             logger.debug("Create new software:" + softwareName);
             software = new Software();
             software.setName(softwareName);
             software.setManually(true);
             software.setDescription(description);
             try {
                 this.em.getTransaction().begin();
                 this.em.persist(software);
                 this.em.getTransaction().commit();
             } catch (Exception e) {
                 logger.error("Can not create software: " + e.getMessage());
                 return new CrxResponse(this.getSession(), "ERROR", "Can not create software: " + e.getMessage());
             }
         }
         for (SoftwareVersion sv : software.getSoftwareVersions()) {
             if (sv.getVersion().equals(version)) {
                 softwareVersion = this.getSoftwareVersionById(sv.getId());
                 break;
             }
         }

         if (softwareVersion == null) {
             //This software version does not exists. We have create it
             softwareVersion = new SoftwareVersion(software, version, "U");
             logger.debug("Create new software version:" + softwareName + " ## " + version);
             try {
                 this.em.getTransaction().begin();
                 this.em.merge(software);
                 this.em.getTransaction().commit();
             } catch (Exception e) {
                 logger.error("Can not create software version: " + e.getMessage());
                 return new CrxResponse(this.getSession(), "ERROR", "Can not create software version " + e.getMessage());
             }
         }

         //We are searching for the status of this version of the software on the device.
         List<SoftwareStatus> softwareStatusToRemove = new ArrayList<SoftwareStatus>();
         for (SoftwareStatus st : device.getSoftwareStatus()) {
             if (st == null) {
                 logger.error("setSoftwareStatusOnDevice SoftwareStatus of " + softwareName + " is NULL on device:" + device);
                 continue;
             }
             if (st.getSoftwareVersion().equals(softwareVersion)) {
                 logger.debug("equal:" + st.getSoftwareVersion().getVersion());
                 softwareStatus = st;
             } else if (status == "I" && st.getSoftwareVersion().getSoftware().equals(software)) {
                 //Remove the other versions of the software if this is installed.
                 softwareStatusToRemove.add(st);
             }
         }

         if (status == "I" && software.getManually()) {
             status = "IM";
         }
         if (softwareStatus == null) {
             //This software version has no status on this device. Let's create it.
             logger.debug("Create new software status:" + softwareName + " ## " + version + " ## " + status);
             softwareStatus = new SoftwareStatus(device, softwareVersion, status);
             try {
                 this.em.getTransaction().begin();
                 this.em.persist(softwareStatus);
                 device.getSoftwareStatus().add(softwareStatus);
                 softwareVersion.getSoftwareStatuses().add(softwareStatus);
                 this.em.merge(device);
                 this.em.merge(softwareVersion);
                 this.em.getTransaction().commit();
             } catch (Exception e) {
                 logger.error("Can not create software status:" + e.getMessage());
                 return new CrxResponse(this.getSession(), "ERROR", "Can not create software status:" + e.getMessage());
             }
         } else {
             softwareStatus.setStatus(status);
             try {
                 this.em.getTransaction().begin();
                 this.em.merge(softwareStatus);
                 this.em.getTransaction().commit();
             } catch (Exception e) {
                 logger.error("Can not modify software status:" + e.getMessage());
                 return new CrxResponse(this.getSession(), "ERROR", "Can not modify software status:" + e.getMessage());
             }
         }

         //Remove the old software statuses
         try {
             try {
                 logger.debug("Software status to remove:" + new ObjectMapper().writeValueAsString(softwareStatusToRemove));
             } catch (Exception e) {
                 logger.error(e.getMessage());
             }
             for (SoftwareStatus st : softwareStatusToRemove) {
                 this.em.getTransaction().begin();
                 softwareStatus = this.em.merge(st);
                 this.em.remove(softwareStatus);
                 this.em.getTransaction().commit();
             }
         } catch (Exception e) {
             logger.error("Can not remove software status:" + e.getMessage());
             return new CrxResponse(this.getSession(), "ERROR", "Can not remove software status:" + e.getMessage());
         }
         return new CrxResponse(this.getSession(), "OK", "Software State was saved succesfully");
     }

     public CrxResponse setSoftwareStatusOnDeviceById(Long deviceId, String softwareName, String description, String version, String status) {
         Device device = new DeviceService(this.session, this.em).getById(deviceId);
         return this.setSoftwareStatusOnDevice(device, softwareName, description, version, status);
     }

     public CrxResponse setSoftwareStatusOnDeviceByName(String deviceName, String softwareName, String description, String version, String status) {
         Device device = new DeviceService(this.session, this.em).getByName(deviceName);
         return this.setSoftwareStatusOnDevice(device, softwareName, description, version, status);
     }

     public CrxResponse cleunUpSoftwareStatusOnDevice(Device device, Software software) {
         try {
             this.em.getTransaction().begin();
             for (SoftwareStatus st : device.getSoftwareStatus()) {
                 if (st.getSoftwareVersion().getSoftware().equals(software)) {
                     this.em.merge(st);
                     this.em.remove(st);
                 }
             }
             this.em.getTransaction().commit();
         } catch (Exception e) {
             logger.error(e.getMessage());
             return new CrxResponse(this.getSession(), "ERROR", e.getMessage());
         } finally {
         }
         return new CrxResponse(this.getSession(), "OK", "All software states was removed from device.");
     }

     /**
      * Delete Software Status
      *
      * @param    device            The corresponding device object
      * @param    softwareName    Name of the corresponding software package
      * @param    version            The version of the corresponding software
      * @return An CrxResponse object will be returned
      */
     public CrxResponse deleteSoftwareStatusFromDevice(Device device, String softwareName, String version) {
         for (SoftwareStatus st : device.getSoftwareStatus()) {
             if (st.getSoftwareVersion().getVersion().equals(version) && st.getSoftwareVersion().getSoftware().getName().equals(softwareName)) {
                 try {
                     this.em.getTransaction().begin();
                     this.em.merge(st);
                     this.em.remove(st);
                     this.em.getTransaction().commit();
                     return new CrxResponse(this.getSession(), "OK", "Software State was removed succesfully");
                 } catch (Exception e) {
                     logger.error(e.getMessage());
                     return new CrxResponse(this.getSession(), "ERROR", e.getMessage());
                 } finally {
                 }
             }
         }
         return new CrxResponse(this.getSession(), "OK", "No Software State exists for this software version on this device.");
     }

     public CrxResponse deleteSoftwareStatusFromDeviceByName(String deviceName, String softwareName, String version) {
         Device device = new DeviceService(this.session, this.em).getByName(deviceName);
         return this.deleteSoftwareStatusFromDevice(device, softwareName, version);
     }

     public CrxResponse deleteSoftwareStatusFromDeviceById(Long deviceId, String softwareName, String version) {
         Device device = new DeviceService(this.session, this.em).getById(deviceId);
         return this.deleteSoftwareStatusFromDevice(device, softwareName, version);
     }

     public String getSoftwareStatusOnDeviceByName(String deviceName, String softwareName,
                                                   String version) {
         Device device = new DeviceService(this.session, this.em).getByName(deviceName);
         Software software = this.getByName(softwareName);
         return this.getSoftwareStatusOnDevice(device, software, version);
     }

     public String getSoftwareStatusOnDeviceById(Long deviceId, String softwareName, String version) {
         Device device = new DeviceService(this.session, this.em).getById(deviceId);
         Software software = this.getByName(softwareName);
         return this.getSoftwareStatusOnDevice(device, software, version);
     }

     public List<SoftwareStatus> getSoftwareStatusOnDevice(Device device, Long softwareId) {
         List<SoftwareStatus> softwareStatus = new ArrayList<SoftwareStatus>();
         for (SoftwareStatus st : device.getSoftwareStatus()) {
             st.setSoftwareName(st.getSoftwareVersion().getSoftware().getName());
             st.setDeviceName(device.getName());
             logger.debug("SoftwareStatus" + st);
             logger.debug("SoftwareVersion" + st.getSoftwareVersion());
             logger.debug("SoftwareVersion" + st.getSoftwareVersion().getSoftware());
             if (softwareId < 1 || st.getSoftwareVersion().getSoftware().getId() == softwareId) {
                 st.setVersion(st.getSoftwareVersion().getVersion());
                 st.setManually(st.getSoftwareVersion().getSoftware().getManually());
                 softwareStatus.add(st);
             }
         }
         return softwareStatus;
     }

     public List<SoftwareStatus> getAllSoftwareStatusOnDeviceById(Long deviceId) {
         return getAllSoftwareStatusOnDevice(new DeviceService(this.session, this.em).getById(deviceId));
     }

     public List<SoftwareStatus> getAllSoftwareStatusOnDevice(Device device) {
         List<SoftwareStatus> softwareStatus = new ArrayList<SoftwareStatus>();
         for (SoftwareStatus st : device.getSoftwareStatus()) {
             st.setSoftwareName(st.getSoftwareVersion().getSoftware().getName());
             st.setDeviceName(device.getName());
             logger.debug("SoftwareStatus" + st);
             logger.debug("SoftwareVersion" + st.getSoftwareVersion());
             logger.debug("SoftwareVersion" + st.getSoftwareVersion().getSoftware());
             st.setVersion(st.getSoftwareVersion().getVersion());
             st.setManually(st.getSoftwareVersion().getSoftware().getManually());
             softwareStatus.add(st);
         }
         return softwareStatus;
     }

     public List<SoftwareStatus> getSoftwareStatusOnDeviceById(Long deviceId, Long softwareId) {
         Device device = new DeviceService(this.session, this.em).getById(deviceId);
         return this.getSoftwareStatusOnDevice(device, softwareId);
     }

     public String getSoftwareLicencesOnDevice(String deviceName) {

         Device device = new DeviceService(this.session, this.em).getByName(deviceName);
         if (device == null) {
             logger.info("getSoftwareLicencesOnDevice: Device " + deviceName + " does not exists.");
             return "";
         }
         Map<String, String> licenses = new HashMap<String, String>();
         for (SoftwareLicense license : device.getSoftwareLicenses()) {
             licenses.put("LIC_" + license.getSoftware().getName(), license.getValue());
         }
         try {
             return new ObjectMapper().writeValueAsString(licenses);
         } catch (Exception e) {
             return "";
         }
     }

     public List<Software> getSoftwareStatusById(List<Long> softwareIds) {
         List<Software> softwares = new ArrayList<Software>();
         for (Long i : softwareIds) {
             softwares.add(getById(i));
         }
         return softwares;
     }

     public CrxResponse addRequirements(Software software, Software requirement) {
         try {
             this.em.getTransaction().begin();
             software.getSoftwareRequirements().add(requirement);
             software.getRequiredBy().add(software);
             this.em.merge(software);
             this.em.merge(requirement);
             this.em.getTransaction().commit();
         } catch (Exception e) {
             logger.error(e.getMessage());
             return new CrxResponse(this.getSession(), "ERROR", e.getMessage());
         } finally {
         }
         return new CrxResponse(this.getSession(), "OK", "Software requirement was added successfully");
     }

     public CrxResponse addRequirements(List<String> requirement) {
         return this.addRequirements(this.getByName(requirement.get(0)), this.getByName(requirement.get(1)));
     }

     public CrxResponse addRequirements(long softwareId, long requirementId) {
         return this.addRequirements(this.getById(softwareId), this.getById(requirementId));
     }

     public CrxResponse deleteRequirements(long softwareId, long requirementId) {
         // TODO Auto-generated method stub
         return null;
     }

 }
