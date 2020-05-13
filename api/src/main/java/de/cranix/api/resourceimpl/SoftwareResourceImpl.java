/* (c) 2017 Peter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.api.resourceimpl;

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

import de.cranix.api.resources.SoftwareResource;
import de.cranix.dao.Category;
import de.cranix.dao.Device;
import de.cranix.dao.HWConf;
import de.cranix.dao.CrxBaseObject;
import de.cranix.dao.CrxResponse;
import de.cranix.dao.Room;
import de.cranix.dao.Session;
import de.cranix.dao.Software;
import de.cranix.dao.SoftwareLicense;
import de.cranix.dao.SoftwareStatus;
import de.cranix.dao.SoftwareVersion;
import de.cranix.dao.controller.SoftwareController;
import de.cranix.dao.internal.CommonEntityManagerFactory;
import de.cranix.dao.controller.CategoryController;
import de.cranix.dao.controller.CloneToolController;
import de.cranix.dao.controller.DeviceController;
import de.cranix.dao.controller.RoomController;

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
	public CrxResponse add(Session session, Software software) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new SoftwareController(session,em).add(software,true);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse modify(Session session, Software software) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new SoftwareController(session,em).modify(software);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse delete(Session session, long softwareId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new SoftwareController(session,em).delete(softwareId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse apply(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new SoftwareController(session,em).applySoftwareStateToHosts();
		em.close();
		return resp;
	}


	@Override
	public CrxResponse createInstallation(Session session, Category category) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new SoftwareController(session,em).createInstallationCategory(category);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse addSoftwareToInstalation(Session session, long installationId, long softwareId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new SoftwareController(session,em).addSoftwareToCategory(softwareId,installationId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse addDeviceToInstalation(Session session, long installationId, long deviceId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CategoryController categoryController = new CategoryController(session,em);
		CrxResponse resp = categoryController.addMember(installationId, "Device", deviceId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse addRoomToInstalation(Session session, long installationId, long roomId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CategoryController categoryController = new CategoryController(session,em);
		CrxResponse resp = categoryController.addMember(installationId, "Room", roomId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse addHWConfToInstalation(Session session, long installationId, long hwconfId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CategoryController categoryController = new CategoryController(session,em);
		CrxResponse resp = categoryController.addMember(installationId, "HWConf", hwconfId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse deleteInstalation(Session session, long installationId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CategoryController categoryController = new CategoryController(session,em);
		CrxResponse ossResponse = categoryController.delete(installationId);
		if( ossResponse.getCode().equals("OK") ) {
			ossResponse = this.apply(session);
		}
		em.close();
		return ossResponse;
	}

	@Override
	public CrxResponse deleteSoftwareFromInstalation(Session session, long installationId, long softwareId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		SoftwareController softwareController = new SoftwareController(session,em);
		CrxResponse resp = softwareController.deleteSoftwareFromCategory(softwareId,installationId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse deleteDeviceFromInstalation(Session session, long installationId, long deviceId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CategoryController categoryController = new CategoryController(session,em);
		CrxResponse resp = categoryController.deleteMember(installationId, "Device", deviceId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse deleteRoomFromInstalation(Session session, long installationId, long roomId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CategoryController categoryController = new CategoryController(session,em);
		CrxResponse resp = categoryController.deleteMember(installationId, "Room", roomId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse deleteHWConfFromInstalation(Session session, long installationId, long hwconfId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CategoryController categoryController = new CategoryController(session,em);
		CrxResponse resp = categoryController.deleteMember(installationId, "HWConf", hwconfId);
		em.close();
		return resp;
	}

	@Override
	public List<CrxBaseObject> getSoftwares(Session session, long installationId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		Category category = new CategoryController(session,em).getById(installationId);
		List<CrxBaseObject> objects = new ArrayList<CrxBaseObject>();
		for( Software object : category.getSoftwares() ) {
			objects.add(new CrxBaseObject(object.getId(),object.getName()));
		}
		em.close();
		return objects;
	}

	@Override
	public List<CrxBaseObject> getDevices(Session session, long installationId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		Category category = new CategoryController(session,em).getById(installationId);
		List<CrxBaseObject> objects = new ArrayList<CrxBaseObject>();
		for( Device object : category.getDevices() ) {
			objects.add(new CrxBaseObject(object.getId(),object.getName()));
		}
		em.close();
		return objects;
	}

	@Override
	public List<CrxBaseObject> getRooms(Session session, long installationId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		Category category = new CategoryController(session,em).getById(installationId);
		List<CrxBaseObject> objects = new ArrayList<CrxBaseObject>();
		for( Room object : category.getRooms() ) {
			objects.add(new CrxBaseObject(object.getId(),object.getName()));
		}
		em.close();
		return objects;
	}

	@Override
	public List<CrxBaseObject> getHWConfs(Session session, long installationId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		Category category = new CategoryController(session,em).getById(installationId);
		List<CrxBaseObject> objects = new ArrayList<CrxBaseObject>();
		for( HWConf object : category.getHWConfs() ) {
			objects.add(new CrxBaseObject(object.getId(),object.getName()));
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
	public CrxResponse download(Session session, List<String> softwares) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		SoftwareController softwareController = new SoftwareController(session,em);
		CrxResponse resp = softwareController.downloadSoftwares(softwares);
		em.close();
		return resp;
	}
	@Override
	public CrxResponse downloadOne(Session session, String softwareName) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<String> softwares = new ArrayList<String>();
		softwares.add(softwareName);
		SoftwareController softwareController = new SoftwareController(session,em);
		CrxResponse resp = softwareController.downloadSoftwares(softwares);
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
	public CrxResponse setSoftwareInstalledOnDevice(Session session, String deviceName, String softwareName,
			String version) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new SoftwareController(session,em).setSoftwareStatusOnDeviceByName(deviceName, softwareName, softwareName, version, "I");
		em.close();
		return resp;
	}

	@Override
	public CrxResponse setSoftwareInstalledOnDevice(Session session, String deviceName, Map<String, String> software) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new SoftwareController(session,em).setSoftwareStatusOnDeviceByName(
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
	public CrxResponse addLicenseToSoftware(
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
		CrxResponse resp = new SoftwareController(session,em).addLicenseToSoftware(
				softwareLicense,
				softwareId,
				fileInputStream,
				contentDispositionHeader);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse modifyLicense(
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
		CrxResponse resp = softwareController.modifySoftwareLIcense(
				softwareLicense,fileInputStream,contentDispositionHeader);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse deleteLicense(Session session, long licenseId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new SoftwareController(session,em).deleteLicence(licenseId);
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
	public CrxResponse addRequirements(Session session, List<String> requirement) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new SoftwareController(session,em).addRequirements(requirement);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse addRequirements(Session session, long softwareId, long requirementId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new SoftwareController(session,em).addRequirements(softwareId,requirementId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse deleteRequirements(Session session, long softwareId, long requirementId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new SoftwareController(session,em).deleteRequirements(softwareId,requirementId);
		em.close();
		return resp;
	}

	@Override
	public String downloadStatus(Session session) {
		try {
			return	String.join(" ", Files.readAllLines(Paths.get("/run/lock/cranix-api/crx_download_packages")));
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
	public CrxResponse applyState(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		DeviceController deviceController = new DeviceController(session,em);
		for( Device device : deviceController.getAll() ) {
			deviceController.manageDevice(device, "applyState", null);
		}
		em.close();
		return new CrxResponse(session,"OK","Salt High State was applied on all minions.");
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
	public CrxResponse updatesSoftwares(Session session, List<String> softwares) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new SoftwareController(session,em).updateSoftwares(softwares);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse deleteDownloadedSoftwares(Session session, List<String> softwares) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new SoftwareController(session,em).deleteDownloadedSoftwares(softwares);
		em.close();
		return resp;
	}

	@Override
	public List<CrxBaseObject> getAvailableSoftwares(Session session, long installationId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		SoftwareController sc = new SoftwareController(session,em);
		List<CrxBaseObject> objects = new ArrayList<CrxBaseObject>();
		Category installationSet = new CategoryController(session,em).getById(installationId);
		for( Software software : sc.getAllInstallable() ) {
			if( !installationSet.getSoftwares().contains(software) ) {
				objects.add(new CrxBaseObject(software.getId(),software.getName()));
			}
		}
		em.close();
		return objects;
	}

	@Override
	public List<CrxBaseObject> getAvailableDevices(Session session, long installationId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<CrxBaseObject> objects = new ArrayList<CrxBaseObject>();
		DeviceController dc = new DeviceController(session,em);
		for( Long deviceId : new CategoryController(session,em).getAvailableMembers(installationId, "Device") ) {
			Device device = dc.getById(deviceId);
			if( device != null  &&  device.getHwconf() != null && device.getHwconf().getDeviceType().equals("FatClient") ) {
				objects.add(new CrxBaseObject(device.getId(),device.getName()));
			}
		}
		em.close();
		return objects;
	}

	@Override
	public List<CrxBaseObject> getAvailableRooms(Session session, long installationId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<CrxBaseObject> objects = new ArrayList<CrxBaseObject>();
		RoomController rc = new RoomController(session,em);
		for( Long roomId : new CategoryController(session,em).getAvailableMembers(installationId, "Room") ) {
			Room room = rc.getById(roomId);
			if( room != null  &&  room.getHwconf() != null && room.getHwconf().getDeviceType().equals("FatClient") ) {
				objects.add(new CrxBaseObject(room.getId(),room.getName()));
			}
		}
		em.close();
		return objects;
	}

	@Override
	public List<CrxBaseObject> getAvailableHWConfs(Session session, long installationId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<CrxBaseObject> objects = new ArrayList<CrxBaseObject>();
		CloneToolController cc = new CloneToolController(session,em);
		for( Long hwconfId : new CategoryController(session,em).getAvailableMembers(installationId, "HWConf") ) {
			HWConf hwconf = cc.getById(hwconfId);
			if( hwconf != null && hwconf.getDeviceType().equals("FatClient") ) {
				objects.add(new CrxBaseObject(hwconf.getId(),hwconf.getName()));
			}
		}
		em.close();
		return objects;
	}

}
