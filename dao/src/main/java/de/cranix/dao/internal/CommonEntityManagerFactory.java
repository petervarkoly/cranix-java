/* (c) 202 Péter Varkoly <peter@varkoly.de> - all rights reserved */
/* (c) 2016 EXTIS GmbH - all rights reserved */

package de.cranix.dao.internal;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import static de.cranix.dao.internal.CranixConstants.*;

public class CommonEntityManagerFactory {

    public static final int DB_VERSION = 1;

    private static HashMap<String, CommonEntityManagerFactory> commonEmf = new HashMap<String, CommonEntityManagerFactory>();
    public static HashMap<Long, String> threadKeys = new HashMap<Long, String>();



    private Map<String, Object> properties;
    private EntityManagerFactory emf;
   //TODO public static Logger logger = Logger.getLogger(CommonEntityManagerFactory.class.getName());

    private CommonEntityManagerFactory() {

    }

    public Map<String, Object> getProperties() {
        if (properties == null) {
            properties = new HashMap<String, Object>();
            properties.put(PersistenceUnitProperties.TARGET_DATABASE, "MySql");
        //    properties.put(PersistenceUnitProperties.JDBC_DRIVER, "com.mysql.jdbc.Driver");



            properties.put(PersistenceUnitProperties.CLASSLOADER, CommonEntityManagerFactory.class.getClassLoader());

            properties.put("eclipselink.logging.level", "ALL");
            properties.put("eclipselink.logging.timestamp", "true");
            properties.put("eclipselink.logging.session", "true");
            properties.put("eclipselink.logging.thread", "true");
            properties.put("eclipselink.logging.exceptions", "true");
	    //properties.put("eclipselink.persistencexml", "/opt/cranix-java/conf/persistence.xml");
            /*
             * <property name="eclipselink.logging.level.sql" value="FINE"/>
             * <property name="eclipselink.logging.parameters" value="true"/>
             * <property name="eclipselink.logging.logger"
             * value="ServerLogger"/>
             * <property name="eclipselink.logging.logger"
             * value="DefaultLogger"/>
             */
	    try {
			File file = new File(cranixPropFile);
			FileInputStream fileInput = new FileInputStream(file);
			Properties props = new Properties();
			props.load(fileInput);
			fileInput.close();

			Enumeration<Object> enuKeys = props.keys();
			while (enuKeys.hasMoreElements()) {
				String key = (String) enuKeys.nextElement();
				String value = props.getProperty(key);
				properties.put(key, value);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        }
        return properties;
    }

    public EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            Map<String, Object> props = getProperties();

            emf = Persistence.createEntityManagerFactory("OSS", props);

            if (emf == null) {
            	System.err.println("getEntityManagerFactory : EntityManagerFactory still null."); //TODO
            }
        }
        return emf;
    }

    public static CommonEntityManagerFactory instance(String key) {
        if (!commonEmf.containsKey(key)) {
            commonEmf.put(key, new CommonEntityManagerFactory());
        }
        return commonEmf.get(key);
    }

    public  CommonEntityManagerFactory instanceI(String key) {
        if (!commonEmf.containsKey(key)) {
            commonEmf.put(key, new CommonEntityManagerFactory());
        }
        return commonEmf.get(key);
    }
}
