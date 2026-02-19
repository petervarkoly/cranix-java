package de.cranix.services;

import de.cranix.dao.*;
import org.apache.commons.lang3.SerializationUtils;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SoftwareSetService extends CategoryService {

    public SoftwareSetService(Session session, EntityManager em) {
        super(session, em);
    }

    public CrxResponse addSet(Category category) {
        Category deepCopy = (Category) SerializationUtils.clone(category);
        category.setCategoryType("installation");
        category.setPublicAccess(false);
        CrxResponse response = this.add(category);
        logger.debug("resp" + response);
        if (response.getCode().equals("OK")) {
            CrxResponse resp1;
            long installationId = response.getObjectId();
            if (deepCopy.getSoftwareIds() != null) {
                for (long id : deepCopy.getSoftwareIds()) {
                    resp1 = this.addMember(installationId, "software", id);
                    logger.debug("software resp" + resp1);
                }
            }
            if (deepCopy.getHwconfIds() != null) {
                for (long id : deepCopy.getHwconfIds()) {
                    resp1 = this.addMember(installationId, "hwconf", id);
                    logger.debug("hwconf resp" + resp1);
                }
            }
            if (deepCopy.getRoomIds() != null) {
                for (long id : deepCopy.getRoomIds()) {
                    resp1 = this.addMember(installationId, "room", id);
                    logger.debug("room resp" + resp1);
                }
            }
            if (deepCopy.getDeviceIds() != null) {
                for (long id : deepCopy.getDeviceIds()) {
                    resp1 = this.addMember(installationId, "device", id);
                    logger.debug("device resp" + resp1);
                }
            }
            return new SoftwareService(session, em).applySoftwareStateToHosts();
        }
        return response;
    }

    public CrxResponse modifySet(Long installationId, Category category) {
        Category oldCategory = this.getById(installationId);
        CrxResponse resp = null;
        CrxResponse resp1;
        logger.info("old:" + oldCategory);
        logger.info("category:" + category);
        logger.info("resp:" + resp);
	SoftwareService softwareService = new SoftwareService(session, em);
	if( softwareService.writingSaltConfig() ) {
                return new CrxResponse("ERROR","An other process is writing the SaltStack configuration. Please try it later!");
         }
        //First add new objects
        for (long id : category.getSoftwareIds()) {
            if (!oldCategory.getSoftwareIds().contains(id)) {
                resp1 = this.addMember(installationId, "software", id);
                logger.info("add software:" + id + " resp:" + resp1);
            }
        }
        for (long id : category.getHwconfIds()) {
            if (!oldCategory.getHwconfIds().contains(id)) {
                resp1 = this.addMember(installationId, "hwconf", id);
                logger.info("add hwconf:" + id + " resp:" + resp1);
            }
        }
        for (long id : category.getRoomIds()) {
            if (!oldCategory.getHwconfIds().contains(id)) {
                resp1 = this.addMember(installationId, "room", id);
                logger.info("add room:" + id + " resp:" + resp1);
            }
        }
        for (long id : category.getDeviceIds()) {
            if (!oldCategory.getDeviceIds().contains(id)) {
                resp1 = this.addMember(installationId, "device", id);
                logger.info("add device:" + id + " resp:" + resp1);
            }
        }
        //Now remove objects
        for (long id : oldCategory.getSoftwareIds()) {
            if (!category.getSoftwareIds().contains(id)) {
                resp1 = this.deleteMember(installationId, "software", id);
                logger.info("delete software:" + id + " resp:" + resp1);
            }
        }
        for (long id : oldCategory.getHwconfIds()) {
            if (!category.getHwconfIds().contains(id)) {
                resp1 = this.deleteMember(installationId, "hwconf", id);
                logger.info("delete hwconf:" + id + " resp:" + resp1);
            }
        }
        for (long id : oldCategory.getRoomIds()) {
            if (!category.getRoomIds().contains(id)) {
                resp1 = this.deleteMember(installationId, "room", id);
                logger.info("delete room:" + id + " resp:" + resp1);
            }
        }
        for (long id : oldCategory.getDeviceIds()) {
            if (!category.getDeviceIds().contains(id)) {
                resp1 = this.deleteMember(installationId, "device", id);
                logger.info("delete device:" + id + " resp:" + resp1);
            }
        }
        this.modify(category);
        oldCategory = this.getById(installationId);
        List<Device> devices = oldCategory.getDevices();
        for (Room room : oldCategory.getRooms()) {
            devices.addAll(room.getDevices());
        }
        for (HWConf hwConf : oldCategory.getHwconfs()) {
            devices.addAll(hwConf.getDevices());
        }
        return softwareService.applySoftwareStateToHostsBatch(devices);
    }
}
