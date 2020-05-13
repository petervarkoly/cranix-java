/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.api.resourceimpl;

import de.cranix.dao.AccessInRoom;
import de.cranix.dao.Device;
import de.cranix.dao.HWConf;
import de.cranix.dao.CrxMConfig;
import de.cranix.dao.CrxActionMap;
import de.cranix.dao.CrxResponse;
import de.cranix.dao.Printer;
import de.cranix.dao.Room;
import de.cranix.dao.Session;
import de.cranix.dao.controller.DHCPConfig;
import de.cranix.dao.controller.EducationController;
import de.cranix.dao.controller.RoomController;
import de.cranix.dao.internal.CommonEntityManagerFactory;
import de.cranix.api.resources.RoomResource;

import javax.persistence.EntityManager;
import javax.ws.rs.WebApplicationException;
import java.util.List;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoomRescourceImpl implements RoomResource {

	Logger logger = LoggerFactory.getLogger(RoomRescourceImpl.class);

	public RoomRescourceImpl() {
	}

	@Override
	public Room getById(Session session, Long roomId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		Room room = new RoomController(session,em).getById(roomId);
		em.close();
		if (room == null) {
			throw new WebApplicationException(404);
		}
		return room;
	}

	@Override
	public List<Room> getAll(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final RoomController roomController = new RoomController(session,em);
		final List<Room> rooms = roomController.getAllToUse();
		em.close();
		return rooms;
	}
	
	@Override
	public String getAllNames(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final RoomController roomController = new RoomController(session,em);
		StringBuilder rooms = new StringBuilder();
		for( Room room : roomController.getAllToUse() ) {
			rooms.append(room.getName()).append(roomController.getNl());
		}
		em.close();
		return rooms.toString();
	}
	
	@Override
	public List<Room> allWithControl(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final RoomController roomController = new RoomController(session,em);
		final List<Room> rooms = roomController.getAllWithControl();
		em.close();
		return rooms;
	}
	
	@Override
	public List<Room> allWithFirewallControl(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final RoomController roomController = new RoomController(session,em);
		final List<Room> rooms = roomController.getAllWithFirewallControl();
		em.close();
		return rooms;
	}
	
	@Override
	public CrxResponse delete(Session session, Long roomId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final RoomController roomController = new RoomController(session,em);
		CrxResponse resp = roomController.delete(roomId);
		em.close();
		return resp;
	}
	
	@Override
	public CrxResponse add(Session session, Room room) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final RoomController roomController = new RoomController(session,em);
		CrxResponse resp = roomController.add(room);
		em.close();
		return resp;
	}
	
	@Override
	public List<String> getAvailableIPAddresses(Session session, Long roomId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final RoomController roomController = new RoomController(session,em);
		final List<String> ips = roomController.getAvailableIPAddresses(roomId);
		em.close();
		if ( ips == null) {
			throw new WebApplicationException(404);
		}
		return ips;
	}

	@Override
	public List<String> getAvailableIPAddresses(Session session, Long roomId, Long count) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final RoomController roomController = new RoomController(session,em);
		final List<String> ips = roomController.getAvailableIPAddresses(roomId,count);
		em.close();
		if ( ips == null) {
		    throw new WebApplicationException(404);
		}
		return ips;
	}

	@Override
	public String getNextRoomIP(Session session, String network, int netMask) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final RoomController roomController = new RoomController(session,em);
		String[] l = network.split("\\.");
		network = l[0] + "." + l[1] + "." +l[2] + "."  + l[3] + "/" + l[4];
		final String nextIP = roomController.getNextRoomIP(network, netMask);
		em.close();
		logger.debug("getNextRoomIP: " + network);
		if ( nextIP == null) {
			throw new WebApplicationException(404);
		}
		return nextIP;
	}

	@Override
	public List<Map<String, String>> getLoggedInUsers(Session session, Long roomId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final RoomController roomController = new RoomController(session,em);
		final List<Map<String, String>> users = roomController.getLoggedInUsers(roomId);
		em.close();
		if ( users == null) {
			throw new WebApplicationException(404);
		}
		return users;
	}

	@Override
	public List<AccessInRoom> getAccessList(Session session, Long roomId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final RoomController roomController = new RoomController(session,em);
		final List<AccessInRoom> accesses = roomController.getAccessList(roomId);
		em.close();
		if ( accesses == null) {
			throw new WebApplicationException(404);
		}
		return accesses;
	}

	@Override
	public CrxResponse addAccessList(Session session, Long roomId, AccessInRoom accessList) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new RoomController(session,em).addAccessList(roomId,accessList);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse deleteAccessList(Session session, Long accessInRoomId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new RoomController(session,em).deleteAccessList(accessInRoomId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse setScheduledAccess(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final RoomController roomController = new RoomController(session,em);
		CrxResponse resp = roomController.setScheduledAccess();
		em.close();
		return resp;
	}

	@Override
	public CrxResponse setDefaultAccess(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final RoomController roomController = new RoomController(session,em);
		CrxResponse resp = roomController.setDefaultAccess();
		em.close();
		return resp;
	}

	@Override
	public List<AccessInRoom> getAccessStatus(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final RoomController roomController = new RoomController(session,em);
		final List<AccessInRoom> accesses = roomController.getAccessStatus();
		em.close();
		return accesses;
	}

	@Override
	public AccessInRoom getAccessStatus(Session session, Long roomId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		AccessInRoom resp = new RoomController(session,em).getAccessStatus(roomId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse setAccessStatus(Session session, Long roomId, AccessInRoom access) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new RoomController(session,em).setAccessStatus(roomId, access);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse addDevices(Session session, Long roomId, List<Device> devices) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final RoomController roomController = new RoomController(session,em);
		CrxResponse ossResponse = roomController.addDevices(roomId,devices);
		em.close();
		return ossResponse;
	}

	@Override
	public CrxResponse addDevice(Session session, Long roomId, String macAddress, String name) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final RoomController roomController = new RoomController(session,em);
		CrxResponse resp = roomController.addDevice(roomId,macAddress,name);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse deleteDevices(Session session, Long roomId, List<Long> deviceIds) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final RoomController roomController = new RoomController(session,em);
		CrxResponse resp = roomController.deleteDevices(roomId,deviceIds);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse deleteDevice(Session session, Long roomId, Long deviceId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final RoomController roomController = new RoomController(session,em);
		List<Long> deviceIds = new ArrayList<Long>();
		deviceIds.add(deviceId);
		return roomController.deleteDevices(roomId,deviceIds);
	}

	@Override
	public List<Device> getDevices(Session session, Long roomId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final RoomController roomController = new RoomController(session,em);
		List<Device> resp = roomController.getDevices(roomId);
		em.close();
		return resp;
	}

	@Override
	public HWConf getHwConf(Session session, Long roomId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final RoomController roomController = new RoomController(session,em);
		HWConf resp = roomController.getHWConf(roomId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse setHwConf(Session session, Long roomId, Long hwConfId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final RoomController roomController = new RoomController(session,em);
		CrxResponse resp = roomController.setHWConf(roomId,hwConfId);
		em.close();
		return resp;
	}

	@Override
	public List<Room> search(Session session, String search) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final RoomController roomController = new RoomController(session,em);
		List<Room> resp = roomController.search(search);
		em.close();
		return resp;
	}

	@Override
	public List<Room> getRoomsToRegister(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final RoomController roomController = new RoomController(session,em);
		List<Room> resp = roomController.getAllToRegister();
		em.close();
		return resp;
	}

	@Override
	public List<Room> getRooms(Session session, List<Long> roomIds) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final RoomController roomController = new RoomController(session,em);
		List<Room> resp = roomController.getRooms(roomIds);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse modify(Session session, Room room) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new RoomController(session,em).modify(room);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse modify(Session session, Long roomId, Room room) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		room.setId(roomId);
		CrxResponse resp = new RoomController(session,em).modify(room);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse setDefaultPrinter(Session session, Long roomId, Long printerIds) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new RoomController(session,em).setDefaultPrinter(roomId, printerIds);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse setDefaultPrinter(Session session, String roomName, String printerName) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new RoomController(session,em).setDefaultPrinter(roomName, printerName);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse deleteDefaultPrinter(Session session, Long roomId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new RoomController(session,em).deleteDefaultPrinter(roomId);
		em.close();
		return resp;
	}

	@Override
	public Printer getDefaultPrinter(Session session, Long roomId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		Printer resp = new RoomController(session,em).getById(roomId).getDefaultPrinter();
		em.close();
		return resp;
	}

	@Override
	public CrxResponse setAvailablePrinters(Session session, Long roomId, List<Long> printerIds) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new RoomController(session,em).setAvailablePrinters(roomId, printerIds);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse addAvailablePrinters(Session session, Long roomId, Long printerId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new RoomController(session,em).addAvailablePrinter(roomId, printerId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse deleteAvailablePrinters(Session session, Long roomId, Long printerId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new RoomController(session,em).deleteAvailablePrinter(roomId, printerId);
		em.close();
		return resp;
	}

	@Override
	public List<Printer> getAvailablePrinters(Session session, Long roomId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<Printer> resp = new RoomController(session,em).getById(roomId).getAvailablePrinters();
		em.close();
		return resp;
	}

	@Override
	public List<String> getAvailableRoomActions(Session session, Long roomId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<String> resp = new EducationController(session,em).getAvailableRoomActions(roomId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse manageRoom(Session session, Long roomId, String action) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new EducationController(session,em).manageRoom(roomId,action, null);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse manageRoom(Session session, Long roomId, String action, Map<String, String> actionContent) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new EducationController(session,em).manageRoom(roomId,action, actionContent);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse importRooms(Session session, InputStream fileInputStream,
			FormDataContentDisposition contentDispositionHeader) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new RoomController(session,em).importRooms(fileInputStream, contentDispositionHeader);
		em.close();
		return resp;
	}

	@Override
	public List<CrxMConfig> getDHCP(Session session, Long roomId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<CrxMConfig> dhcpParameters = new ArrayList<CrxMConfig>();
		RoomController roomController = new RoomController(session,em);
		Room room = roomController.getById(roomId);
		for(CrxMConfig config : roomController.getMConfigObjects(room, "dhcpStatements") ) {
			dhcpParameters.add(config);
		}
		for(CrxMConfig config : roomController.getMConfigObjects(room, "dhcpOptions") ) {
			dhcpParameters.add(config);
		}
		em.close();
		return dhcpParameters;
	}

	@Override
	public CrxResponse addDHCP(Session session, Long roomId, CrxMConfig dhcpParameter) {
		if( !dhcpParameter.getKeyword().equals("dhcpStatements") && !dhcpParameter.getKeyword().equals("dhcpOptions") ) {
			return new CrxResponse(session,"ERROR","Bad DHCP parameter.");
		}
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		RoomController roomController = new RoomController(session,em);
		Room room = roomController.getById(roomId);
		CrxResponse ossResponse = roomController.addMConfig(room, dhcpParameter.getKeyword(), dhcpParameter.getValue());
		if( ossResponse.getCode().equals("ERROR") ) {
			return ossResponse;
		}
		Long dhcpParameterId = ossResponse.getObjectId();
		ossResponse = new DHCPConfig(session,em).Test();
		if( ossResponse.getCode().equals("ERROR") ) {
			roomController.deleteMConfig(null, dhcpParameterId);
			return ossResponse;
		}
		new DHCPConfig(session,em).Create();
		em.close();
		return new CrxResponse(session,"OK","DHCP Parameter was added succesfully");
	}

	@Override
	public CrxResponse deleteDHCP(Session session, Long roomId, Long parameterId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		RoomController roomController = new RoomController(session,em);
		Room room = roomController.getById(roomId);
		CrxResponse resp = roomController.deleteMConfig(room,parameterId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse setPrinters(Session session, Long roomId, Map<String, List<Long>> printers) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		RoomController roomController = new RoomController(session,em);
		CrxResponse resp = roomController.setPrinters(roomId, printers);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse applyAction(Session session, CrxActionMap actionMap) {
		// TODO Auto-generated method stub
		return null;
	}
}
