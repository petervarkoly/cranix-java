package de.cranix.services;

import de.cranix.dao.CrxResponse;
import de.cranix.dao.MailAccess;
import de.cranix.dao.Session;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

public class MailserverService extends  Service{
    public MailserverService(Session session, EntityManager em){
        super(session,em);
    }

    public List<MailAccess> getAllMailAccess(){
        Query query = this.em.createNamedQuery("MailAccess.findAll");
        return query.getResultList();
    }

    public CrxResponse addMailAccess(MailAccess mailAccess) {
        try{
            mailAccess.setCreator(this.session.getUser());
            em.getTransaction().begin();
            em.persist(mailAccess);
            em.getTransaction().commit();
        } catch (Exception e) {
            return new CrxResponse(null,"ERROR", e.getMessage());
        }
        return new CrxResponse(null,"OK", "Mail Access Entry was created successfully.");
    }

    public CrxResponse deleteMailAccess(Long id) {
        try{
            MailAccess mailAccess = this.em.find(MailAccess.class, id);
            if( mailAccess == null){
                return new CrxResponse(null,"ERROR", "Can not find MailAccess.");
            }
            em.getTransaction().begin();
            em.remove(mailAccess);
            em.getTransaction().commit();
        } catch (Exception e) {
            return new CrxResponse(null,"ERROR", e.getMessage());
        }
        return new CrxResponse(null,"OK", "Mail Access Entry was removed successfully.");
    }
}
