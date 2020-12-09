/* (c) 2017 Peter Varkoly <peter@varkoly.de> - all rights reserved */
package de.openschoolserver.api.resourceimpl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import de.openschoolserver.api.resources.SoftwareResource;
import de.openschoolserver.dao.Category;
import de.openschoolserver.dao.Device;
import de.openschoolserver.dao.HWConf;
import de.openschoolserver.dao.OssBaseObject;
import de.openschoolserver.dao.OssResponse;
import de.openschoolserver.dao.Room;
import de.openschoolserver.dao.Session;
import de.openschoolserver.dao.Software;
import de.openschoolserver.dao.SoftwareLicense;
import de.openschoolserver.dao.SoftwareStatus;
import de.openschoolserver.dao.SoftwareVersion;
import de.openschoolserver.dao.controller.SoftwareController;
import de.openschoolserver.dao.internal.CommonEntityManagerFactory;
import de.openschoolserver.dao.controller.CategoryController;
import de.openschoolserver.dao.controller.CloneToolController;
import de.openschoolserver.dao.controller.DeviceController;
import de.openschoolserver.dao.controller.RoomController;

public class SoftwareResourceImpl implements SoftwareResource {
	Logger logger           = LoggerFactory.getLogger(SoftwareResource.class);

	public SoftwareResourceImpl() {
	}

	@Override
	public List<Software> getAll(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<Software> resp = new SoftwareController(session,em).getAll();
		em.close();
		return resp;
	}

	@Override
	public List<Software> getAllInstallable(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<Software> resp = new SoftwareController(session,em).getAllInstallable();
		em.close();
		return resp;
	}

	@Override
	public Software getById(Session session, long softwareId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		Software resp = new SoftwareController(session,em).getById(softwareId);
		em.close();
		return resp;
	}

	@Override
	public List<Software> search(Session session, String search) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<Software> resp = new SoftwareController(session,em).search(search);
		em.close();
		return resp;
	}

	@Override
	public OssResponse add(Session session, Software software) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		OssResponse resp = new SoftwareController(session,em).add(software,true);
		em.close();
		return resp;
	}

	@Override
	public OssResponse modify(Session session, Software software) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		OssResponse resp = new SoftwareController(session,em).modify(software);
		em.close();
		return resp;
	}

	@Override
	public OssResponse delete(Session session, long softwareId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		OssResponse resp = new SoftwareController(session,em).delete(softwareId);
		em.close();
		return resp;
	}

	@Override
	public OssResponse apply(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		OssResponse resp = new SoftwareController(session,em).applySoftwareStateToHosts();
		em.close();
		return resp;
	}


	@Override
	public OssResponse createInstallation(Session session, Category category) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		OssResponse resp = new SoftwareController(session,em).createInstallationCategory(category);
		em.close();
		return resp;
	}

	@Override
	public OssResponse addSoftwareToInstalation(Session session, long installationId, long softwareId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		OssResponse resp = new SoftwareController(session,em).addSoftwareToCategory(softwareId,installationId);
		em.close();
		return resp;
	}

	@Override
	public OssResponse addDeviceToInstalation(Session session, long installationId, long deviceId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CategoryController categoryController = new CategoryController(session,em);
		OssResponse resp = categoryController.addMember(installationId, "Device", deviceId);
		em.close();
		return resp;
	}

	@Override
	public OssResponse addRoomToInstalation(Session session, long installationId, long roomId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CategoryController categoryController = new CategoryController(session,em);
		OssResponse resp = categoryController.addMember(installationId, "Room", roomId);
		em.close();
		return resp;
	}

	@Override
	public OssResponse addHWConfToInstalation(Session session, long installationId, long hwconfId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CategoryController categoryController = new CategoryController(session,em);
		OssResponse resp = categoryController.addMember(installationId, "HWConf", hwconfId);
		em.close();
		return resp;
	}

	@Override
	public OssResponse deleteInstalation(Session session, long installationId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CategoryController categoryController = new CategoryController(session,em);
		OssResponse ossResponse = categoryController.delete(installationId);
		if( ossResponse.getCode().equals("OK") ) {
			ossResponse = this.apply(session);
		}
		em.close();
		return ossResponse;
	}

	@Override
	public OssResponse deleteSoftwareFromInstalation(Session session, long installationId, long softwareId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		SoftwareController softwareController = new SoftwareController(session,em);
		OssResponse resp = softwareController.deleteSoftwareFromCategory(softwareId,installationId);
		em.close();
		return resp;
	}

	@Override
	public OssResponse deleteDeviceFromInstalation(Session session, long installationId, long deviceId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CategoryController categoryController = new CategoryController(session,em);
		OssResponse resp = categoryController.deleteMember(installationId, "Device", deviceId);
		em.close();
		return resp;
	}

	@Override
	public OssResponse deleteRoomFromInstalation(Session session, long installationId, long roomId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CategoryController categoryController = new CategoryController(session,em);
		OssResponse resp = categoryController.deleteMember(installationId, "Room", roomId);
		em.close();
		return resp;
	}

	@Override
	public OssResponse deleteHWConfFromInstalation(Session session, long installationId, long hwconfId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CategoryController categoryController = new CategoryController(session,em);
		OssResponse resp = categoryController.deleteMember(installationId, "HWConf", hwconfId);
		em.close();
		return resp;
	}

	@Override
	public List<OssBaseObject> getSoftwares(Session session, long installationId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		Category category = new CategoryController(session,em).getById(installationId);
		List<OssBaseObject> objects = new ArrayList<OssBaseObject>();
		for( Software object : category.getSoftwares() ) {
			objects.add(new OssBaseObject(object.getId(),object.getName()));
		}
		em.close();
		return objects;
	}

	@Override
	public List<OssBaseObject> getDevices(Session session, long installationId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		Category category = new CategoryController(session,em).getById(installationId);
		List<OssBaseObject> objects = new ArrayList<OssBaseObject>();
		for( Device object : category.getDevices() ) {
			objects.add(new OssBaseObject(object.getId(),object.getName()));
		}
		em.close();
		return objects;
	}

	@Override
	public List<OssBaseObject> getRooms(Session session, long installationId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		Category category = new CategoryController(session,em).getById(installationId);
		List<OssBaseObject> objects = new ArrayList<OssBaseObject>();
		for( Room object : category.getRooms() ) {
			objects.add(new OssBaseObject(object.getId(),object.getName()));
		}
		em.close();
		return objects;
	}

	@Override
	public List<OssBaseObject> getHWConfs(Session session, long installationId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		Category category = new CategoryController(session,em).getById(installationId);
		List<OssBaseObject> objects = new ArrayList<OssBaseObject>();
		for( HWConf object : category.getHwconfs() ) {
			objects.add(new OssBaseObject(object.getId(),object.getName()));
		}
		em.close();
		return objects;
	}

	@Override
	public List<Map<String, String>> getAvailable(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		SoftwareController softwareController = new SoftwareController(session,em);
		List<Map<String, String>> resp = softwareController.getAvailableSoftware();
		em.close();
		return resp;
	}

	@Override
	public OssResponse download(Session session, List<String> softwares) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		SoftwareController softwareController = new SoftwareController(session,em);
		OssResponse resp = softwareController.downloadSoftwares(softwares);
		em.close();
		return resp;
	}
	@Override
	public OssResponse downloadOne(Session session, String softwareName) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<String> softwares = new ArrayList<String>();
		softwares.add(softwareName);
		SoftwareController softwareController = new SoftwareController(session,em);
		OssResponse resp = softwareController.downloadSoftwares(softwares);
		em.close();
		return resp;
	}

	@Override
	public List<Map<String, String>> listDownloadedSoftware(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<Map<String, String>> resp = new SoftwareController(session,em).listDownloadedSoftware();
		em.close();
		return resp;
	}

	@Override
	public OssResponse setSoftwareInstalledOnDevice(Session session, String deviceName, String softwareName,
			String version) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		OssResponse resp = new SoftwareController(session,em).setSoftwareStatusOnDeviceByName(deviceName, softwareName, softwareName, version, "I");
		em.close();
		return resp;
	}

	@Override
	public OssResponse setSoftwareInstalledOnDevice(Session session, String deviceName, Map<String, String> software) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		OssResponse resp = new SoftwareController(session,em).setSoftwareStatusOnDeviceByName(
				deviceName,
				software.get("name"),
				software.get("description"),
				software.get("version"),
				"I");
		em.close();
		return resp;
	}

	@Override
	public String getSoftwareLicencesOnDevice(Session session, String deviceName) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		SoftwareController softwareController = new SoftwareController(session,em);
		String resp = softwareController.getSoftwareLicencesOnDevice(deviceName);
		em.close();
		return resp;
	}

	@Override
	public List<Category> getInstallations(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<Category> resp = new CategoryController(session,em).getByType("installation");
		em.close();
		return resp;
	}

	@Override
	public List<Software> getSoftwares(Session session, List<Long> softwareIds) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<Software> resp = new SoftwareController(session,em).getSoftwareStatusById(softwareIds);
		em.close();
		return resp;
	}

	@Override
	public OssResponse addLicenseToSoftware(
			Session     session,
			long        softwareId,
			Character   licenseType,
			Integer     count,
			String      value,
			InputStream fileInputStream,
			FormDataContentDisposition contentDispositionHeader) {

		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		SoftwareLicense softwareLicense = new SoftwareLicense();
		softwareLicense.setValue(value);
		softwareLicense.setCount(count);
		softwareLicense.setLicenseType(licenseType);
		OssResponse resp = new SoftwareController(session,em).addLicenseToSoftware(
				softwareLicense,
				softwareId,
				fileInputStream,
				contentDispositionHeader);
		em.close();
		return resp;
	}

	@Override
	public OssResponse modifyLicense(
			Session     session,
			long        licenseId,
			Character   licenseType,
			Integer     count,
			String      value,
			InputStream fileInputStream,
			FormDataContentDisposition contentDispositionHeader
		) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		SoftwareController  softwareController = new SoftwareController(session,em);
		SoftwareLicense softwareLicense = softwareController.getSoftwareLicenseById(licenseId);
		softwareLicense.setCount(count);
		softwareLicense.setValue(value);
		softwareLicense.setLicenseType(licenseType);
		OssResponse resp = softwareController.modifySoftwareLIcense(
				softwareLicense,fileInputStream,contentDispositionHeader);
		em.close();
		return resp;
	}

	@Override
	public OssResponse deleteLicense(Session session, long licenseId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		OssResponse resp = new SoftwareController(session,em).deleteLicence(licenseId);
		em.close();
		return resp;
	}


	@Override
	public List<SoftwareStatus> softwareStatus(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<SoftwareStatus> resp = new SoftwareController(session,em).getAllStatus();
		em.close();
		return resp;
	}

	@Override
	public List<SoftwareStatus> getSoftwareStatusOnDevice(Session session, Long deviceId, Long softwareId) {
		if( softwareId == null) {
			return this.getAllSoftwareStatusOnDevice(session, deviceId);
		}
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<SoftwareStatus> resp = new SoftwareController(session,em).getSoftwareStatusOnDeviceById(deviceId, softwareId);
		em.close();
		return resp;
	}

	@Override
	public List<SoftwareStatus> getAllSoftwareStatusOnDevice(Session session, Long deviceId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<SoftwareStatus> resp = new SoftwareController(session,em).getAllSoftwareStatusOnDeviceById(deviceId);
		em.close();
		return resp;
	}

	@Override
	public List<SoftwareStatus> getSoftwareStatus(Session session, Long softwareId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		SoftwareController softwareController = new SoftwareController(session,em);
		List<SoftwareStatus> softwareStatus = new ArrayList<SoftwareStatus>();
		Software software = softwareController.getById(softwareId);
		for( SoftwareVersion sv : software.getSoftwareVersions() ) {
			for( SoftwareStatus st : sv.getSoftwareStatuses() ) {
				st.setSoftwareName(software.getName());
				st.setDeviceName(st.getDevice().getName());
				st.setVersion(sv.getVersion());
				st.setManually(software.getManually());
				softwareStatus.add(st);
			}
		}
		em.close();
		return softwareStatus;
	}

	@Override
	public List<SoftwareLicense> getSoftwareLicenses(Session session, long softwareId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<SoftwareLicense> licenses = new ArrayList<SoftwareLicense>();
		for( SoftwareLicense license : new SoftwareController(session,em).getById(softwareId).getSoftwareLicenses() ) {
			license.setUsed(license.getDevices().size());
			licenses.add(license);
		}
		em.close();
		return licenses;
	}

	@Override
	public OssResponse addRequirements(Session session, List<String> requirement) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		OssResponse resp = new SoftwareController(session,em).addRequirements(requirement);
		em.close();
		return resp;
	}

	@Override
	public OssResponse addRequirements(Session session, long softwareId, long requirementId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		OssResponse resp = new SoftwareController(session,em).addRequirements(softwareId,requirementId);
		em.close();
		return resp;
	}

	@Override
	public OssResponse deleteRequirements(Session session, long softwareId, long requirementId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		OssResponse resp = new SoftwareController(session,em).deleteRequirements(softwareId,requirementId);
		em.close();
		return resp;
	}

	@Override
	public String downloadStatus(Session session) {
		try {
			return	String.join(" ", Files.readAllLines(Paths.get("/run/lock/oss-api/oss_download_packages")));
		} catch( IOException e ) {
			return "";
		}
	}

	@Override
	public List<SoftwareStatus> getRoomsStatus(Session session, Long roomId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<SoftwareStatus> ss = new ArrayList<SoftwareStatus>();
		Room room = new RoomController(session,em).getById(roomId);
		SoftwareController sc = new SoftwareController(session,em);
		for( Device device : room.getDevices() ) {
			ss.addAll(sc.getAllSoftwareStatusOnDevice(device));
		}
		em.close();
		return ss;
	}

	@Override
	public List<SoftwareStatus> getHWConsStatus(Session session, Long hwconfId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<SoftwareStatus> ss = new ArrayList<SoftwareStatus>();
		SoftwareController sc = new SoftwareController(session,em);
		HWConf hwconf = new CloneToolController(session,em).getById(hwconfId);
		for( Device device : hwconf.getDevices() ) {
			ss.addAll(sc.getAllSoftwareStatusOnDevice(device));
		}
		em.close();
		return ss;
	}

	@Override
	public OssResponse applyState(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		DeviceController deviceController = new DeviceController(session,em);
		for( Device device : deviceController.getAll() ) {
			deviceController.manageDevice(device, "applyState", null);
		}
		em.close();
		return new OssResponse(session,"OK","Salt High State was applied on all minions.");
	}

	@Override
	public Category getInstallation(Session session, long installationId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		Category resp = new CategoryController(session,em).getById(installationId);
		em.close();
		return resp;
	}

	@Override
	public List<Map<String, String>> listUpdatesForSoftwarePackages(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<Map<String, String>> resp = new SoftwareController(session,em).listUpdatesForSoftwarePackages();
		em.close();
		return resp;
	}

	@Override
	public OssResponse updatesSoftwares(Session session, List<String> softwares) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		OssResponse resp = new SoftwareController(session,em).updateSoftwares(softwares);
		em.close();
		return resp;
	}

	@Override
	public OssResponse deleteDownloadedSoftwares(Session session, List<String> softwares) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		OssResponse resp = new SoftwareController(session,em).deleteDownloadedSoftwares(softwares);
		em.close();
		return resp;
	}

	@Override
	public List<OssBaseObject> getAvailableSoftwares(Session session, long installationId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		SoftwareController sc = new SoftwareController(session,em);
		List<OssBaseObject> objects = new ArrayList<OssBaseObject>();
		Category installationSet = new CategoryController(session,em).getById(installationId);
		for( Software software : sc.getAllInstallable() ) {
			if( !installationSet.getSoftwares().contains(software) ) {
				objects.add(new OssBaseObject(software.getId(),software.getName()));
			}
		}
		em.close();
		return objects;
	}

	@Override
	public List<OssBaseObject> getAvailableDevices(Session session, long installationId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<OssBaseObject> objects = new ArrayList<OssBaseObject>();
		DeviceController dc = new DeviceController(session,em);
		for( Long deviceId : new CategoryController(session,em).getAvailableMembers(installationId, "Device") ) {
			Device device = dc.getById(deviceId);
			if( device != null  &&  device.getHwconf() != null && device.getHwconf().getDeviceType().equals("FatClient") ) {
				objects.add(new OssBaseObject(device.getId(),device.getName()));
			}
		}
		em.close();
		return objects;
	}

	@Override
	public List<OssBaseObject> getAvailableRooms(Session session, long installationId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<OssBaseObject> objects = new ArrayList<OssBaseObject>();
		RoomController rc = new RoomController(session,em);
		for( Long roomId : new CategoryController(session,em).getAvailableMembers(installationId, "Room") ) {
			Room room = rc.getById(roomId);
			if( room != null  &&  room.getHwconf() != null && room.getHwconf().getDeviceType().equals("FatClient") ) {
				objects.add(new OssBaseObject(room.getId(),room.getName()));
			}
		}
		em.close();
		return objects;
	}

	@Override
	public List<OssBaseObject> getAvailableHWConfs(Session session, long installationId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<OssBaseObject> objects = new ArrayList<OssBaseObject>();
		CloneToolController cc = new CloneToolController(session,em);
		for( Long hwconfId : new CategoryController(session,em).getAvailableMembers(installationId, "HWConf") ) {
			HWConf hwconf = cc.getById(hwconfId);
			if( hwconf != null && hwconf.getDeviceType().equals("FatClient") ) {
				objects.add(new OssBaseObject(hwconf.getId(),hwconf.getName()));
			}
		}
		em.close();
		return objects;
	}

}
