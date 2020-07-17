/* (c) 2020 Péter Varkoly <peter@varkoly.de> - all rights reserved */
/* (c) 2016 EXTIS GmbH - all rights reserved */
package de.cranix.api.auth;

import java.util.Optional;

import javax.persistence.EntityManager;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.cranix.dao.Session;
import de.cranix.dao.controller.SessionController;
import de.cranix.dao.internal.CommonEntityManagerFactory;

public class CrxTokenAuthenticator implements Authenticator<String, Session> {

    Logger logger = LoggerFactory.getLogger(CrxTokenAuthenticator.class);

    @Override
    public Optional<Session> authenticate(String token) throws AuthenticationException {

        logger.debug("Token: " + token);
        EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
        final SessionController sessionController = new SessionController(em);
        final Session session = sessionController.validateToken(token);
        em.close();
       
        if (session != null) {
            logger.debug("authentication successful!");
            return Optional.of(session);
        } else {
            logger.debug("authentication failed!");
            return Optional.empty();
        }
    }

}
