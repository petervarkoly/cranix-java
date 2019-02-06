/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.openschoolserver.api.resourceimpl;

import de.openschoolserver.dao.HWConf;


import de.openschoolserver.dao.Clone;
import de.openschoolserver.dao.Device;
import de.openschoolserver.dao.Partition;
import de.openschoolserver.dao.OssResponse;
import de.openschoolserver.dao.Session;
import de.openschoolserver.dao.Room;
import de.openschoolserver.dao.controller.CloneToolController;
import de.openschoolserver.dao.controller.Config;
import de.openschoolserver.dao.controller.RoomController;
import de.openschoolserver.dao.controller.SessionController;
import de.openschoolserver.dao.internal.CommonEntityManagerFactory;
import de.openschoolserver.dao.controller.DeviceController;
import de.openschoolserver.api.resources.CloneToolResource;

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
		if( device != null && device.getHwconf() != null ) {
			return Long.toString(device.getHwconf().getId());
		}
		return "";
	}


	@Override
	public String isMaster(Session session, Long deviceId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final DeviceController deviceController = new DeviceController(session,em);
		Device device = deviceController.getById(deviceId);
		if( device != null &&  deviceController.checkConfig(device,"isMaster" ) ) {
			return "true";
		}
		return "";
	}

	@Override
	public Long getMaster(Session session, Long hwconfId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CloneToolController cloneToolController = new CloneToolController(session,em);
		HWConf hwconf = cloneToolController.getById(hwconfId);
		if( hwconf == null ) {
			return null;
		}
		for( Device device : hwconf.getDevices() ) {
			if( cloneToolController.checkConfig(device, "isMaster") ) {
				return device.getId();
			}
		}
		return null;
	}

	@Override
	public OssResponse setMaster(Session session, Long deviceId, int isMaster) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final DeviceController deviceController = new DeviceController(session,em);
		Device device = deviceController.getById(deviceId);
		if( device == null ) {
			return new OssResponse(session,"ERRO","Device was not found.");
		}
		if( deviceController.checkConfig(device,"isMaster" ) && isMaster == 0) {
			return deviceController.deleteConfig(device, "isMaster");
		}
		if( isMaster == 1 ) {
			for( Device dev : device.getHwconf().getDevices() ) {
				if( !dev.equals(device) ) {
					deviceController.deleteConfig(dev,"isMaster");
				}
			}
		}
		if( ! deviceController.checkConfig(device,"isMaster" ) && isMaster == 1 ) {
			return deviceController.setConfig(device, "isMaster","true");
		}

		return new OssResponse(session,"OK","Nothing to change.");
	}

	@Override
	public OssResponse setMaster(Session session, int isMaster) {
		return this.setMaster(session, session.getDevice().getId(), isMaster);
	}

	@Override
	public HWConf getById(Session session, Long hwconfId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final HWConf hwconf = new CloneToolController(session,em).getById(hwconfId);
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
		return new CloneToolController(session,em).getPartitions(hwconfId);
	}

	@Override
	public String getDescription(Session session, Long hwconfId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		return new CloneToolController(session,em).getDescription(hwconfId);
	}

	@Override
	public Partition getPartition(Session session, Long hwconfId, String partition) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		return new CloneToolController(session,em).getPartition(hwconfId, partition);
	}

	@Override
	public String getConfigurationValue(UriInfo ui,
	        HttpServletRequest req,
	        Long hwconfId,
	        String partition,
	        String key) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		Session session  = new SessionController(em).getLocalhostSession();
		return new CloneToolController(session,em).getConfigurationValue(hwconfId,partition,key);
	}

	@Override
	public OssResponse addHWConf(Session session, HWConf hwconf) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		return new CloneToolController(session,em).addHWConf(hwconf);
	}

	@Override
	public OssResponse modifyHWConf(Session session, Long hwconfId, HWConf hwconf) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		return new CloneToolController(session,em).modifyHWConf(hwconfId, hwconf);
	}

	@Override
	public OssResponse addPartition(Session session, Long hwconfId, Partition partition) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		return new CloneToolController(session,em).addPartitionToHWConf(hwconfId, partition);
	}

	@Override
	public OssResponse addPartition(Session session, Long hwconfId, String partitionName) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		return new CloneToolController(session,em).addPartitionToHWConf(hwconfId, partitionName );
	}

	@Override
	public OssResponse setConfigurationValue(Session session, Long hwconfId, String partitionName, String key, String value) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		return new CloneToolController(session,em).setConfigurationValue(hwconfId,partitionName,key,value);
	}

	@Override
	public OssResponse delete(Session session, Long hwconfId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		return new CloneToolController(session,em).delete(hwconfId);
	}

	@Override
	public OssResponse deletePartition(Session session, Long hwconfId, String partitionName) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		return new CloneToolController(session,em).deletePartition(hwconfId,partitionName);
	}

	@Override
	public OssResponse deleteConfigurationValue(Session session, Long hwconfId, String partitionName, String key) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		return new CloneToolController(session,em).deleteConfigurationValue(hwconfId,partitionName,key);
	}

	@Override
	public List<HWConf> getAllHWConf(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		return new CloneToolController(session,em).getAllHWConf();
	}

	@Override
	public String getRoomsToRegister(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		StringBuilder roomList = new StringBuilder();
		for( Room room : new RoomController(session,em).getAllToRegister() ) {
			roomList.append(room.getId()).append("##").append(room.getName()).append(" ");
		}
		return roomList.toString();
	}

	@Override
	public String getAvailableIPAddresses(Session session, long roomId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		StringBuilder roomList = new StringBuilder();
		for( String name : new RoomController(session,em).getAvailableIPAddresses(roomId, 0) ) {
			roomList.append(name.replaceFirst(" ","/")).append(" ").append(name.split(" ")[1]).append(" ");
		}
		return roomList.toString();
	}

	@Override
	public OssResponse addDevice(Session session, long roomId, String macAddress, String IP, String name) {
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
		return rc.addDevices(roomId, devices);
	}

	@Override
	public OssResponse startCloning(Session session, Long hwconfId, Clone parameters) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		return new CloneToolController(session,em).startCloning(hwconfId,parameters);
	}

	@Override
	public OssResponse startCloning(Session session, Long hwconfId, int multiCast) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		return new CloneToolController(session,em).startCloning("hwconf", hwconfId, multiCast);
	}

	@Override
	public OssResponse startCloningInRoom(Session session, Long roomId, int multiCast) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		return new CloneToolController(session,em).startCloning("room", roomId, multiCast);
	}

	@Override
	public OssResponse startCloningOnDevice(Session session, Long deviceId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		return new CloneToolController(session,em).startCloning("device", deviceId, 0);
	}

	@Override
	public OssResponse stopCloning(Session session, Long hwconfId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		return new CloneToolController(session,em).stopCloning("hwconf",hwconfId);
	}

	@Override
	public OssResponse stopCloningInRoom(Session session, Long roomId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		return new CloneToolController(session,em).stopCloning("room",roomId);
	}

	@Override
	public OssResponse stopCloningOnDevice(Session session, Long deviceId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		return new CloneToolController(session,em).stopCloning("device",deviceId);
	}

	@Override
	public String resetMinion(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		if( session.getDevice() == null ) {
			throw new WebApplicationException(404);
		}
		return new CloneToolController(session,em).resetMinion(session.getDevice().getId());
	}

	@Override
	public String resetMinion(Session session, Long deviceId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		return new CloneToolController(session,em).resetMinion(deviceId);
	}

	@Override
	public OssResponse stopCloningOnDevice(Session session, String deviceIP) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		Device device = new DeviceController(session,em).getByIP(deviceIP);
		String pathPxe  = String.format("/srv/tftp/pxelinux.cfg/01-%s", device.getMac().toLowerCase().replace(":", "-"));
		String pathElilo= String.format("/srv/tftp/%s.conf", device.getMac().toUpperCase().replace(":", "-"));
		try {
			Files.deleteIfExists(Paths.get(pathPxe));
			Files.deleteIfExists(Paths.get(pathElilo));
		}catch( IOException e ) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String[] getMulticastDevices(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		Config config = new Config("/etc/sysconfig/dhcpd","DHCPD_");
		return config.getConfigValue("INTERFACE").split("\\s+");
	}

	@Override
	public OssResponse startMulticast(Session session, Long partitionId, String networkDevice) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		return new CloneToolController(session,em).startMulticast(partitionId,networkDevice);
	}

	@Override
	public OssResponse modifyPartition(Session session, Long partitionId, Partition partition) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		return new CloneToolController(session,em).modifyPartition(partitionId, partition);
	}

	@Override
	public String getHostname(UriInfo ui, HttpServletRequest req) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		Session session  = new SessionController(em).getLocalhostSession();
		DeviceController deviceController = new DeviceController(session,em);
		Device device = deviceController.getByIP(req.getRemoteAddr());
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
		if( device != null ) {
			return device.getName().concat(".").concat(deviceController.getConfigValue("DOMAIN"));
		}
		return "";
	}

	@Override
	public String getDomainName(UriInfo ui, HttpServletRequest req) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		SessionController sc = new SessionController(em);
		return sc.getConfigValue("DOMAIN");
	}

	@Override
	public String isMaster(UriInfo ui, HttpServletRequest req) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		Session session  = new SessionController(em).getLocalhostSession();
		DeviceController deviceController = new DeviceController(session,em);
		Device device = deviceController.getByIP(req.getRemoteAddr());
		if( device != null  &&	deviceController.checkConfig(device, "isMaster") ) {
			return "true";
		}
		return "";
	}

	@Override
	public OssResponse importHWConfs(Session session, List<HWConf> hwconfs) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CloneToolController  cloneToolController = new CloneToolController(session,em);
		OssResponse ossResponse = null;
		for( HWConf hwconf : hwconfs ) {
			ossResponse = cloneToolController.addHWConf(hwconf);
			if( ossResponse.getCode().equals("ERROR")) {
				return ossResponse;
			}
		}
		return ossResponse;
	}
}
