/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.openschoolserver.dao.controller;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import de.openschoolserver.dao.Device;
import de.openschoolserver.dao.Room;
import de.openschoolserver.dao.Session;
import de.openschoolserver.dao.User;
import de.openschoolserver.dao.tools.*;

public class DeviceController extends Controller {

	public DeviceController(Session session) {
		super(session);
	}

	/*
	 * Return a device found by the ID
	 */
	public Device getById(int deviceId) {
		EntityManager em = getEntityManager();

		try {
			return em.find(Device.class, deviceId);
		} catch (Exception e) {
			// logger.error(e.getMessage());
			System.err.println(e.getMessage()); //TODO
			return null;
		} finally {
			em.close();
		}
	}

	/*
	 * Delivers a list of devices wit the given device type
	 */
	public List<Device> getByTpe(String type) {
		EntityManager em = getEntityManager();
		try {
			Query query = em.createNamedQuery("Device.getDeviceByType");
			query.setParameter("deviceType", type);
			return query.getResultList();
		} catch (Exception e) {
			// logger.error(e.getMessage());
			System.err.println(e.getMessage()); //TODO
			return null;
		} finally {
			em.close();
		}
	}

	/*
	 * Delivers a list of all existing devices
	 */
	public List<Device> getAll() {
		EntityManager em = getEntityManager();
		try {
			Query query = em.createNamedQuery("Device.findAll");
			return query.getResultList();
		} catch (Exception e) {
			// logger.error(e.getMessage());
			System.err.println(e.getMessage()); //TODO
			return null;
		} finally {
			em.close();
		}
	}

	/*
	 * Deletes a list of device given by the device Ids.
	 */
	public boolean delete(List<Integer> deviceIds) {
		EntityManager em = getEntityManager();
		try {
			for( Integer deviceId : deviceIds) {
				Device dev = em.find(Device.class, deviceId);
				em.remove(dev);
			}
			return true;
		} catch (Exception e) {
			// logger.error(e.getMessage());
			System.err.println(e.getMessage()); //TODO
			return false;
		} finally {
			em.close();
		}
	}

	/*
	 * Creates devices
	 */
	public boolean add(List<Device> devices) {
		EntityManager em = getEntityManager();
		try {
			for(Device dev: devices){
				em.getTransaction().begin();
				em.persist(dev);
				em.getTransaction().commit();
			}
			return true;
		} catch (Exception e) {
			System.err.println(e.getMessage()); //TODO
			return false;
		} finally {
			em.close();
		}
	}

	/*
	 * Find a device given by the IP address
	 */
	public Device getByIP(String IP) {
		EntityManager em = getEntityManager();
		try {
			Query query = em.createNamedQuery("Device.getByIP");
			query.setParameter("IP", IP);
			return (Device) query.getSingleResult();
		} catch (Exception e) {
			// logger.error(e.getMessage());
			System.err.println(e.getMessage()); //TODO
			return null;
		} finally {
			em.close();
		}
	}

	/*
	 * Find a device given by the MAC address
	 */
	public Device getByMAC(String MAC) {
		EntityManager em = getEntityManager();
		try {
			Query query = em.createNamedQuery("Device.getByMAC");
			query.setParameter("MAC", MAC);
			return (Device) query.getSingleResult();
		} catch (Exception e) {
			// logger.error(e.getMessage());
			System.err.println(e.getMessage()); //TODO
			return null;
		} finally {
			em.close();
		}
	}

	/*
	 * Find a device given by the name
	 */
	public Device getByName(String name) {
		EntityManager em = getEntityManager();
		try {
			Query query = em.createNamedQuery("Device.getByName");
			query.setParameter("name", name);
			return (Device) query.getSingleResult();
		} catch (Exception e) {
			// logger.error(e.getMessage());
			System.err.println(e.getMessage()); //TODO
			return null;
		} finally {
			em.close();
		}
	}

	/*
	 * Search devices given by a substring
	 */
	public Device search(String name) {
		EntityManager em = getEntityManager();
		try {
			Query query = em.createNamedQuery("Device.search");
			query.setParameter("name", name);
			return (Device) query.getSingleResult();
		} catch (Exception e) {
			// logger.error(e.getMessage());
			System.err.println(e.getMessage()); //TODO
			return null;
		} finally {
			em.close();
		}
	}

	/*
	 * Find the default printer for a device
	 * If no printer was defined by the device find this from the room
	 */
	public String getDefaultPrinter(int deviceId) {
		Device device = this.getById(deviceId);
		Device printer = device.getDefaultPrinter();
		if( printer != null)
		   return printer.getName();
		printer = device.getRoom().getDefaultPrinter();
		if( printer != null)
			   return printer.getName();
		return "";
	}

	/*
	 * Find the available printer for a device
	 * If no printer was defined by the device find these from the room
	 */
	public List<String> getAvailablePrinters(int deviceId) {
		Device device = this.getById(deviceId);
		List<String> printers   = new ArrayList<String>();
		for( Device printer : device.getAvailablePrinters() ) {
			printers.add(printer.getName());
		}
		if( printers.isEmpty() ){
			for(Device printer : device.getRoom().getAvailablePrinters()){
				printers.add(printer.getName());
			}
		}
		return printers;
	}
	
	/*
	 * Return the list of users which are logged in on this device
	 */
	public List<String> getLoggedInUsers(String IP) {
		Device device = this.getByIP(IP);
		List<String> users = new ArrayList<String>();
		for( User user : device.getLoggedIn() )
			users.add(user.getUid());
		return users;
	}
	

	/*
	 * Return the list of users which are logged in on this device
	 */
	public List<String> getLoggedInUsers(Integer deviceID) {
		Device device = this.getById(deviceID);
		List<String> users = new ArrayList<String>();
		for( User user : device.getLoggedIn() )
			users.add(user.getUid());
		return users;
	}
}