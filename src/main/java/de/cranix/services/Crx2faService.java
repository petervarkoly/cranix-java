package de.cranix.services;

import de.cranix.dao.Crx2fa;
import de.cranix.dao.CrxResponse;
import de.cranix.dao.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;

public class Crx2faService extends Service{
    Logger logger = LoggerFactory.getLogger(SessionService.class);
    public Crx2faService(Session session, EntityManager em) {
        super(session, em);
    }

    public CrxResponse add(Crx2fa crx2fa){
        try {
            this.em.getTransaction().begin();
            crx2fa.setCreator(this.session.getUser());
        }
    }
}
