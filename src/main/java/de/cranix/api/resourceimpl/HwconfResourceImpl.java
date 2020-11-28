/* (c) 2020 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.api.resourceimpl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.WebApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;

import de.cranix.api.resources.HwconfResource;
import de.cranix.dao.Clone;
import de.cranix.dao.Device;
import de.cranix.dao.HWConf;
import de.cranix.dao.CrxActionMap;
import de.cranix.dao.CrxResponse;
import de.cranix.dao.Partition;
import de.cranix.dao.Session;
import de.cranix.dao.controller.CloneToolController;
import de.cranix.dao.internal.CommonEntityManagerFactory;


public class HwconfResourceImpl implements HwconfResource {

	Logger logger = LoggerFactory.getLogger(HwconfResourceImpl.class);

	public HwconfResourceImpl() {
	}

	@Override
	public CrxResponse add(Session session, HWConf hwconf) {
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
	public CrxResponse addPartition(Session session, Long hwconfId, Partition partition) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new CloneToolController(session,em).addPartitionToHWConf(hwconfId, partition);
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
	public List<HWConf> getAllHWConf(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<HWConf> resp = new CloneToolController(session,em).getAllHWConf();
		em.close();
		return resp;
	}

	@Override
	public CrxResponse startRecover(Session session, Long hwconfId, Clone parameters) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new CloneToolController(session,em).startCloning(hwconfId,parameters);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse startRecover(Session session, Long hwconfId, int multiCast) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		return new CloneToolController(session,em).startCloning("hwconf", hwconfId, multiCast);
	}

	@Override
	public CrxResponse stopRecover(Session session, Long hwconfId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new CloneToolController(session,em).stopCloning("hwconf",hwconfId);
		em.close();
		return resp;
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
	public CrxResponse importHWConfs(Session session, List<HWConf> hwconfs) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CloneToolController  cloneToolController = new CloneToolController(session,em);
		CrxResponse crxResponse = null;
		for( HWConf hwconf : hwconfs ) {
			crxResponse = cloneToolController.addHWConf(hwconf);
			if( crxResponse.getCode().equals("ERROR")) {
				break;
			}
		}
		em.close();
		return crxResponse;
	}

	@Override
	public List<CrxResponse> applyAction(Session session, CrxActionMap crxActionMap) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
                List<CrxResponse> responses = new ArrayList<CrxResponse>();
		CloneToolController  cloneToolController = new CloneToolController(session,em);
                logger.debug(crxActionMap.toString());
                for( Long id : crxActionMap.getObjectIds() ) {
                        switch(crxActionMap.getName().toLowerCase()) {
                                case "delete":  responses.add(cloneToolController.delete(id));
                                                break;
/*                                case "cleanup": responses.add(cloneToolController.cleanUp(id));
                                                break;*/
                                case "startclone": responses.add(cloneToolController.startCloning("hwconf",id,0));
                                                break;
                                case "stopclone": responses.add(cloneToolController.stopCloning("hwconf",id));
                                                break;
                        }
                }
                em.close();
                return responses;

	}
}
