/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.api.resourceimpl;

import de.cranix.dao.HWConf;


import de.cranix.dao.Clone;
import de.cranix.dao.Device;
import de.cranix.dao.Partition;
import de.cranix.dao.CrxResponse;
import de.cranix.dao.Session;
import de.cranix.dao.Room;
import de.cranix.dao.controller.CloneToolController;
import de.cranix.dao.controller.Config;
import de.cranix.dao.controller.RoomController;
import de.cranix.dao.controller.SessionController;
import de.cranix.dao.internal.CommonEntityManagerFactory;
import de.cranix.dao.controller.DeviceController;
import de.cranix.api.resources.CloneToolResource;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CloneToolResourceImpl implements CloneToolResource {

	public CloneToolResourceImpl() {
	}

	@Override
	public String getHWConf(UriInfo ui, HttpServletRequest req) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		Session session  = new SessionController(em).getLocalhostSession();
		DeviceController deviceController = new DeviceController(session,em);
		Device device = deviceController.getByIP(req.getRemoteAddr());
		em.close();
		if( device != null && device.getHwconf() != null ) {
			return Long.toString(device.getHwconf().getId());
		}
		return "";
	}

	@Override
	public String resetMinion(UriInfo ui, HttpServletRequest req) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		Session session  = new SessionController(em).getLocalhostSession();
		DeviceController deviceController = new DeviceController(session,em);
		Device device = deviceController.getByIP(req.getRemoteAddr());
		String resp = "";
		if( device != null ) {
			resp = new CloneToolController(session,em).resetMinion(device.getId());
		}
		em.close();
		return resp;
	}


	@Override
	public String isMaster(Session session, Long deviceId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final DeviceController deviceController = new DeviceController(session,em);
		Device device = deviceController.getById(deviceId);
		String resp   = "";
		if( device != null &&  deviceController.checkConfig(device,"isMaster" ) ) {
			resp = "true";
		}
		em.close();
		return resp;
	}

	@Override
	public Long getMaster(Session session, Long hwconfId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CloneToolController cloneToolController = new CloneToolController(session,em);
		HWConf hwconf = cloneToolController.getById(hwconfId);
		Long resp = null;
		if( hwconf != null ) {
			for( Device device : hwconf.getDevices() ) {
				if( cloneToolController.checkConfig(device, "isMaster") ) {
					resp = device.getId();
					break;
				}
			}
		}
		em.close();
		return resp;
	}

	@Override
	public CrxResponse setMaster(Session session, Long deviceId, int isMaster) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final DeviceController deviceController = new DeviceController(session,em);
		Device device = deviceController.getById(deviceId);
		CrxResponse resp = new CrxResponse(session,"OK","Nothing to change.");
		if( device == null ) {
			resp = new CrxResponse(session,"ERRO","Device was not found.");
		} else {
			if( deviceController.checkConfig(device,"isMaster" ) && isMaster == 0) {
				resp = deviceController.deleteConfig(device, "isMaster");
			} else {
				if( ! deviceController.checkConfig(device,"isMaster" ) && isMaster == 1 ) {
					for( Device dev : device.getHwconf().getDevices() ) {
						if( !dev.equals(device) ) {
							deviceController.deleteConfig(dev,"isMaster");
						}
					}
					resp = deviceController.setConfig(device, "isMaster","true");
				}
			}
		}
		em.close();
		return resp;
	}

	@Override
	public CrxResponse setMaster(Session session, int isMaster) {
		return this.setMaster(session, session.getDevice().getId(), isMaster);
	}

	@Override
	public HWConf getById(Session session, Long hwconfId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final HWConf hwconf = new CloneToolController(session,em).getById(hwconfId);
		em.close();
		if (hwconf == null) {
			throw new WebApplicationException(404);
		}
		return hwconf;
	}

	@Override
	public String getPartitions(UriInfo ui,
	        HttpServletRequest req, Long hwconfId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		Session session  = new SessionController(em).getLocalhostSession();
		String resp = new CloneToolController(session,em).getPartitions(hwconfId);
		em.close();
		return resp;
	}

	@Override
	public String getDescription(Session session, Long hwconfId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		String resp = new CloneToolController(session,em).getDescription(hwconfId);
		em.close();
		return resp;
	}

	@Override
	public Partition getPartition(Session session, Long hwconfId, String partition) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		Partition resp = new CloneToolController(session,em).getPartition(hwconfId, partition);
		em.close();
		return resp;
	}

	@Override
	public String getConfigurationValue(UriInfo ui,
	        HttpServletRequest req,
	        Long hwconfId,
	        String partition,
	        String key) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		Session session  = new SessionController(em).getLocalhostSession();
		String resp = new CloneToolController(session,em).getConfigurationValue(hwconfId,partition,key);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse addHWConf(Session session, HWConf hwconf) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new CloneToolController(session,em).addHWConf(hwconf);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse modifyHWConf(Session session, Long hwconfId, HWConf hwconf) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new CloneToolController(session,em).modifyHWConf(hwconfId, hwconf);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse addPartition(Session session, Long hwconfId, Partition partition) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new CloneToolController(session,em).addPartitionToHWConf(hwconfId, partition);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse addPartition(Session session, Long hwconfId, String partitionName) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new CloneToolController(session,em).addPartitionToHWConf(hwconfId, partitionName );
		em.close();
		return resp;
	}

	@Override
	public CrxResponse setConfigurationValue(Session session, Long hwconfId, String partitionName, String key, String value) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new CloneToolController(session,em).setConfigurationValue(hwconfId,partitionName,key,value);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse delete(Session session, Long hwconfId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new CloneToolController(session,em).delete(hwconfId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse deletePartition(Session session, Long hwconfId, String partitionName) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new CloneToolController(session,em).deletePartition(hwconfId,partitionName);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse deleteConfigurationValue(Session session, Long hwconfId, String partitionName, String key) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new CloneToolController(session,em).deleteConfigurationValue(hwconfId,partitionName,key);
		em.close();
		return resp;
	}

	@Override
	public List<HWConf> getAllHWConf(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<HWConf> resp = new CloneToolController(session,em).getAllHWConf();
		em.close();
		return resp;
	}

	@Override
	public String getRoomsToRegister(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		StringBuilder roomList = new StringBuilder();
		for( Room room : new RoomController(session,em).getAllToRegister() ) {
			roomList.append(room.getId()).append("##").append(room.getName()).append(" ");
		}
		em.close();
		return roomList.toString();
	}

	@Override
	public String getAvailableIPAddresses(Session session, long roomId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		StringBuilder roomList = new StringBuilder();
		for( String name : new RoomController(session,em).getAvailableIPAddresses(roomId, 0) ) {
			roomList.append(name.replaceFirst(" ","/")).append(" ").append(name.split(" ")[1]).append(" ");
		}
		em.close();
		return roomList.toString();
	}

	@Override
	public CrxResponse addDevice(Session session, long roomId, String macAddress, String IP, String name) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		RoomController rc = new RoomController(session,em);
		Room room = rc.getById(roomId);
		Device device = new Device();
		device.setName(name);
		device.setMac(macAddress);
		device.setIp(IP);
		if( room.getHwconf() != null ) {
			device.setHwconfId(room.getHwconf().getId());
		}
		ArrayList<Device> devices = new ArrayList<Device>();
		devices.add(device);
		CrxResponse resp = rc.addDevices(roomId, devices);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse startCloning(Session session, Long hwconfId, Clone parameters) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new CloneToolController(session,em).startCloning(hwconfId,parameters);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse startCloning(Session session, Long hwconfId, int multiCast) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		return new CloneToolController(session,em).startCloning("hwconf", hwconfId, multiCast);
	}

	@Override
	public CrxResponse startCloningInRoom(Session session, Long roomId, int multiCast) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new CloneToolController(session,em).startCloning("room", roomId, multiCast);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse startCloningOnDevice(Session session, Long deviceId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new CloneToolController(session,em).startCloning("device", deviceId, 0);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse stopCloning(Session session, Long hwconfId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new CloneToolController(session,em).stopCloning("hwconf",hwconfId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse stopCloningInRoom(Session session, Long roomId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new CloneToolController(session,em).stopCloning("room",roomId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse stopCloningOnDevice(Session session, Long deviceId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new CloneToolController(session,em).stopCloning("device",deviceId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse stopCloningOnDevice(Session session, String deviceIP) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		Device device = new DeviceController(session,em).getByIP(deviceIP);
		CrxResponse resp = new CloneToolController(session,em).stopCloning("device",device.getId());
		em.close();
		return resp;
	}

	@Override
	public String resetMinion(Session session, Long deviceId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		String resp = new CloneToolController(session,em).resetMinion(deviceId);
		em.close();
		return resp;
	}

	@Override
	public String[] getMulticastDevices(Session session) {
		Config config = new Config("/etc/sysconfig/dhcpd","DHCPD_");
		return config.getConfigValue("INTERFACE").split("\\s+");
	}

	@Override
	public CrxResponse startMulticast(Session session, Long partitionId, String networkDevice) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new CloneToolController(session,em).startMulticast(partitionId,networkDevice);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse modifyPartition(Session session, Long partitionId, Partition partition) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new CloneToolController(session,em).modifyPartition(partitionId, partition);
		em.close();
		return resp;
	}

	@Override
	public String getHostname(UriInfo ui, HttpServletRequest req) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		Session session  = new SessionController(em).getLocalhostSession();
		DeviceController deviceController = new DeviceController(session,em);
		Device device = deviceController.getByIP(req.getRemoteAddr());
		em.close();
		if( device != null ) {
			return device.getName();
		}
		return "";
	}

	@Override
	public String getFqhn(UriInfo ui, HttpServletRequest req) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		Session session  = new SessionController(em).getLocalhostSession();
		DeviceController deviceController = new DeviceController(session,em);
		Device device = deviceController.getByIP(req.getRemoteAddr());
		String resp = "";
		if( device != null ) {
			resp = device.getName().concat(".").concat(deviceController.getConfigValue("DOMAIN"));
		}
		em.close();
		return resp;
	}

	@Override
	public String getDomainName(UriInfo ui, HttpServletRequest req) {
		return new Config().getConfigValue("DOMAIN");
	}

	@Override
	public String isMaster(UriInfo ui, HttpServletRequest req) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		Session session  = new SessionController(em).getLocalhostSession();
		DeviceController deviceController = new DeviceController(session,em);
		Device device = deviceController.getByIP(req.getRemoteAddr());
		String resp = "";
		if( device != null  &&	deviceController.checkConfig(device, "isMaster") ) {
			resp = "true";
		}
		em.close();
		return resp;
	}

	@Override
	public CrxResponse importHWConfs(Session session, List<HWConf> hwconfs) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CloneToolController  cloneToolController = new CloneToolController(session,em);
		CrxResponse ossResponse = null;
		for( HWConf hwconf : hwconfs ) {
			ossResponse = cloneToolController.addHWConf(hwconf);
			if( ossResponse.getCode().equals("ERROR")) {
				break;
			}
		}
		em.close();
		return ossResponse;
	}
}
