package de.cranix.services;

import de.cranix.dao.Category;
import de.cranix.dao.CrxResponse;
import de.cranix.dao.Session;
import org.apache.commons.lang3.SerializationUtils;

import javax.persistence.EntityManager;
import java.util.ArrayList;

public class SoftwareSetService extends CategoryService {

    public SoftwareSetService(Session session, EntityManager em) {
		super(session,em);
	}

    public CrxResponse addSet(Category category) {
        category.setCategoryType("installation");
        category.setPublicAccess(false);
        CrxResponse response = this.add(category);
        logger.debug("resp" + response);
        if( response.getCode().equals("OK") ) {
            return new SoftwareService(session, em).applySoftwareStateToHosts();
        }
        return response;
    }

    public CrxResponse modifySet(Long installationId, Category category) {
        this.modify(category);
        return new SoftwareService(session,em).applySoftwareStateToHosts();
    }
}
