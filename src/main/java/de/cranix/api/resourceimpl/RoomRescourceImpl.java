/* (c) 2020 Péter Varkoly <peter@varkoly.de> - all rights reserved */
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
import de.cranix.services.DHCPConfig;
import de.cranix.services.EducationService;
import de.cranix.services.RoomService;
import de.cranix.services.SoftwareService;
import de.cranix.helper.CommonEntityManagerFactory;
import de.cranix.api.resources.RoomResource;

import javax.persistence.EntityManager;
import javax.persistence.Query;
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
		Room room = new RoomService(session,em).getById(roomId);
		em.close();
		if (room == null) {
			throw new WebApplicationException(404);
		}
		return room;
	}

	@Override
	public List<Room> getAll(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final RoomService roomService = new RoomService(session,em);
		final List<Room> rooms = roomService.getAll();
		em.close();
		return rooms;
	}
	
	@Override
	public String getAllNames(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final RoomService roomService = new RoomService(session,em);
		StringBuilder rooms = new StringBuilder();
		for( Room room : roomService.getAllToUse() ) {
			rooms.append(room.getName()).append(roomService.getNl());
		}
		em.close();
		return rooms.toString();
	}
	
	@Override
	public List<Room> allWithControl(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final RoomService roomService = new RoomService(session,em);
		final List<Room> rooms = roomService.getAllWithControl();
		em.close();
		return rooms;
	}
	
	@Override
	public List<Room> allWithFirewallControl(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final RoomService roomService = new RoomService(session,em);
		final List<Room> rooms = roomService.getAllWithFirewallControl();
		em.close();
		return rooms;
	}
	
	@Override
	public CrxResponse delete(Session session, Long roomId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final RoomService roomService = new RoomService(session,em);
		CrxResponse resp = roomService.delete(roomId);
		em.close();
		return resp;
	}
	
	@Override
	public CrxResponse add(Session session, Room room) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final RoomService roomService = new RoomService(session,em);
		CrxResponse resp = roomService.add(room);
		em.close();
		return resp;
	}
	
	@Override
	public List<String> getAvailableIPAddresses(Session session, Long roomId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final RoomService roomService = new RoomService(session,em);
		final List<String> ips = roomService.getAvailableIPAddresses(roomId);
		em.close();
		if ( ips == null) {
			throw new WebApplicationException(404);
		}
		return ips;
	}

	@Override
	public List<String> getAvailableIPAddresses(Session session, Long roomId, Long count) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final RoomService roomService = new RoomService(session,em);
		final List<String> ips = roomService.getAvailableIPAddresses(roomId,count);
		em.close();
		if ( ips == null) {
		    throw new WebApplicationException(404);
		}
		return ips;
	}

	@Override
	public String getNextRoomIP(Session session, String network, int netMask) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final RoomService roomService = new RoomService(session,em);
		String[] l = network.split("\\.");
		network = l[0] + "." + l[1] + "." +l[2] + "."  + l[3] + "/" + l[4];
		final String nextIP = roomService.getNextRoomIP(network, netMask);
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
		final RoomService roomService = new RoomService(session,em);
		final List<Map<String, String>> users = roomService.getLoggedInUsers(roomId);
		em.close();
		if ( users == null) {
			throw new WebApplicationException(404);
		}
		return users;
	}

	@Override
	public List<AccessInRoom> getAccessList(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<AccessInRoom> accesses = null;
		try {
                        Query query = em.createNamedQuery("AccessInRoom.findAll");
                        accesses = query.getResultList();
                } catch (Exception e) {
                        logger.error(e.getMessage());
                } finally {
                }
		em.close();
		return accesses;
	}

	@Override
	public List<AccessInRoom> getAccessList(Session session, Long roomId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final RoomService roomService = new RoomService(session,em);
		final List<AccessInRoom> accesses = roomService.getAccessList(roomId);
		em.close();
		if ( accesses == null) {
			throw new WebApplicationException(404);
		}
		return accesses;
	}

	@Override
	public CrxResponse addAccessList(Session session, Long roomId, AccessInRoom accessList) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new RoomService(session,em).addAccessList(roomId,accessList);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse deleteAccessList(Session session, Long accessInRoomId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new RoomService(session,em).deleteAccessList(accessInRoomId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse setScheduledAccess(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final RoomService roomService = new RoomService(session,em);
		CrxResponse resp = roomService.setScheduledAccess();
		em.close();
		return resp;
	}

	@Override
	public CrxResponse setDefaultAccess(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final RoomService roomService = new RoomService(session,em);
		CrxResponse resp = roomService.setDefaultAccess();
		em.close();
		return resp;
	}

	@Override
	public List<AccessInRoom> getAccessStatus(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final RoomService roomService = new RoomService(session,em);
		final List<AccessInRoom> accesses = roomService.getAccessStatus();
		em.close();
		return accesses;
	}

	@Override
	public AccessInRoom getAccessStatus(Session session, Long roomId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		AccessInRoom resp = new RoomService(session,em).getAccessStatus(roomId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse setAccessStatus(Session session, Long roomId, AccessInRoom access) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new RoomService(session,em).setAccessStatus(roomId, access);
		em.close();
		return resp;
	}

	@Override
	public List<CrxResponse> addDevices(Session session, Long roomId, List<Device> devices) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final RoomService roomService = new RoomService(session,em);
		List<CrxResponse> crxResponse = roomService.addDevices(roomId,devices);
		em.close();
		return crxResponse;
	}

	@Override
	public CrxResponse addDevice(Session session, Long roomId, String macAddress, String name) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final RoomService roomService = new RoomService(session,em);
		CrxResponse resp = roomService.addDevice(roomId,macAddress,name);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse deleteDevices(Session session, Long roomId, List<Long> deviceIds) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final RoomService roomService = new RoomService(session,em);
		CrxResponse resp = roomService.deleteDevices(roomId,deviceIds);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse deleteDevice(Session session, Long roomId, Long deviceId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final RoomService roomService = new RoomService(session,em);
		List<Long> deviceIds = new ArrayList<Long>();
		deviceIds.add(deviceId);
		return roomService.deleteDevices(roomId,deviceIds);
	}

	@Override
	public List<Device> getDevices(Session session, Long roomId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final RoomService roomService = new RoomService(session,em);
		List<Device> resp = roomService.getDevices(roomId);
		em.close();
		return resp;
	}

	@Override
	public HWConf getHwConf(Session session, Long roomId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final RoomService roomService = new RoomService(session,em);
		HWConf resp = roomService.getHWConf(roomId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse setHwConf(Session session, Long roomId, Long hwconfId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final RoomService roomService = new RoomService(session,em);
		CrxResponse resp = roomService.setHWConf(roomId,hwconfId);
		em.close();
		return resp;
	}

	@Override
	public List<Room> search(Session session, String search) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final RoomService roomService = new RoomService(session,em);
		List<Room> resp = roomService.search(search);
		em.close();
		return resp;
	}

	@Override
	public List<Room> getRoomsToRegister(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final RoomService roomService = new RoomService(session,em);
		List<Room> resp = roomService.getAllToRegister();
		em.close();
		return resp;
	}

	@Override
	public List<Room> getRooms(Session session, List<Long> roomIds) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final RoomService roomService = new RoomService(session,em);
		List<Room> resp = roomService.getRooms(roomIds);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse modify(Session session, Room room) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new RoomService(session,em).modify(room);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse modify(Session session, Long roomId, Room room) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		room.setId(roomId);
		CrxResponse resp = new RoomService(session,em).modify(room);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse setDefaultPrinter(Session session, Long roomId, Long printerIds) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new RoomService(session,em).setDefaultPrinter(roomId, printerIds);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse setDefaultPrinter(Session session, String roomName, String printerName) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new RoomService(session,em).setDefaultPrinter(roomName, printerName);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse deleteDefaultPrinter(Session session, Long roomId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new RoomService(session,em).deleteDefaultPrinter(roomId);
		em.close();
		return resp;
	}

	@Override
	public Printer getDefaultPrinter(Session session, Long roomId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		Printer resp = new RoomService(session,em).getById(roomId).getDefaultPrinter();
		em.close();
		return resp;
	}

	@Override
	public CrxResponse setAvailablePrinters(Session session, Long roomId, List<Long> printerIds) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new RoomService(session,em).setAvailablePrinters(roomId, printerIds);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse addAvailablePrinters(Session session, Long roomId, Long printerId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new RoomService(session,em).addAvailablePrinter(roomId, printerId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse deleteAvailablePrinters(Session session, Long roomId, Long printerId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new RoomService(session,em).deleteAvailablePrinter(roomId, printerId);
		em.close();
		return resp;
	}

	@Override
	public List<Printer> getAvailablePrinters(Session session, Long roomId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<Printer> resp = new RoomService(session,em).getById(roomId).getAvailablePrinters();
		em.close();
		return resp;
	}

	@Override
	public List<String> getAvailableRoomActions(Session session, Long roomId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<String> resp = new EducationService(session,em).getAvailableRoomActions(roomId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse manageRoom(Session session, Long roomId, String action) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new EducationService(session,em).manageRoom(roomId,action, null);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse manageRoom(Session session, Long roomId, String action, Map<String, String> actionContent) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new EducationService(session,em).manageRoom(roomId,action, actionContent);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse importRooms(Session session, InputStream fileInputStream,
			FormDataContentDisposition contentDispositionHeader) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new RoomService(session,em).importRooms(fileInputStream, contentDispositionHeader);
		em.close();
		return resp;
	}

	@Override
	public List<CrxMConfig> getDHCP(Session session, Long roomId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<CrxMConfig> dhcpParameters = new ArrayList<CrxMConfig>();
		RoomService roomService = new RoomService(session,em);
		Room room = roomService.getById(roomId);
		for(CrxMConfig config : roomService.getMConfigObjects(room, "dhcpStatements") ) {
			dhcpParameters.add(config);
		}
		for(CrxMConfig config : roomService.getMConfigObjects(room, "dhcpOptions") ) {
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
		RoomService roomService = new RoomService(session,em);
		Room room = roomService.getById(roomId);
		CrxResponse crxResponse = roomService.addMConfig(room, dhcpParameter.getKeyword(), dhcpParameter.getValue());
		if( crxResponse.getCode().equals("ERROR") ) {
			return crxResponse;
		}
		Long dhcpParameterId = crxResponse.getObjectId();
		crxResponse = new DHCPConfig(session,em).Test();
		if( crxResponse.getCode().equals("ERROR") ) {
			roomService.deleteMConfig(null, dhcpParameterId);
			return crxResponse;
		}
		new DHCPConfig(session,em).Create();
		em.close();
		return new CrxResponse(session,"OK","DHCP Parameter was added succesfully");
	}

	@Override
	public CrxResponse deleteDHCP(Session session, Long roomId, Long parameterId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		RoomService roomService = new RoomService(session,em);
		Room room = roomService.getById(roomId);
		CrxResponse resp = roomService.deleteMConfig(room,parameterId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse setPrinters(Session session, Long roomId, Map<String, List<Long>> printers) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		RoomService roomService = new RoomService(session,em);
		CrxResponse resp = roomService.setPrinters(roomId, printers);
		em.close();
		return resp;
	}

	@Override
	public List<CrxResponse> applyAction(Session session, CrxActionMap actionMap) {
                EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		RoomService roomService = new RoomService(session,em);
                List<CrxResponse> responses = new ArrayList<CrxResponse>();
                for( Long id: actionMap.getObjectIds() ) {
                        responses.add(roomService.manageRoom(id,actionMap.getName(),null));
                }
                if( actionMap.getName().equals("delete") ) {
                        new DHCPConfig(session,em).Create();
                        new SoftwareService(session,em).applySoftwareStateToHosts();

                }

                em.close();
                return responses;
	}
}
