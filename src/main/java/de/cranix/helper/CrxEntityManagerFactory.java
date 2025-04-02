/* (c) 2020 Péter Varkoly <peter@varkoly.de> - all rights reserved */

package de.cranix.helper;

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

import static de.cranix.helper.CranixConstants.*;

public class CrxEntityManagerFactory {

    private static Map<String, Object> properties;
    private static EntityManagerFactory semf;

    private CrxEntityManagerFactory() {
    }

    public static Map<String, Object> getProperties() {
        if (properties == null) {
            properties = new HashMap<String, Object>();
            properties.put(PersistenceUnitProperties.TARGET_DATABASE, "MySql");
            //    properties.put(PersistenceUnitProperties.JDBC_DRIVER, "com.mysql.jdbc.Driver");
            properties.put(PersistenceUnitProperties.CLASSLOADER, CrxEntityManagerFactory.class.getClassLoader());

            properties.put("eclipselink.logging.level", "WARNING");
            properties.put("eclipselink.logging.timestamp", "true");
            properties.put("eclipselink.logging.session", "true");
            properties.put("eclipselink.logging.thread", "true");
            properties.put("eclipselink.logging.exceptions", "true");
            properties.put("javax.persistence.jdbc.url", "jdbc:mysql://localhost:3306/CRX?serverTimezone=Europe/Berlin&zeroDateTimeBehavior=convertToNull");
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

    public static EntityManagerFactory instance() {
        if (semf == null) {
            semf = Persistence.createEntityManagerFactory("CRX", getProperties());
        }
        if (semf == null) {
            System.err.println("getEntityManagerFactory : EntityManagerFactory still null."); //TODO
        }
        return semf;
    }
}
