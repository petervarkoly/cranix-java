package de.cranix.services;

import de.cranix.dao.CrxResponse;
import de.cranix.dao.Session;
import de.cranix.dao.TeachingSubject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

public class TeachingSubjectService extends Service {

    Logger logger = LoggerFactory.getLogger(TeachingSubjectService.class);

    public TeachingSubjectService(Session session, EntityManager em) {
        super(session, em);
    }

    public List<TeachingSubject> getAll() {
        try {
            Query query = this.em.createNamedQuery("TeachingSubject.findAll");
            return (List<TeachingSubject>) query.getResultList();
        } catch (Exception e) {
            logger.error("getTeachingSubjects: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public TeachingSubject getByName(String name) {
        try {
            Query query = this.em.createNamedQuery("TeachingSubject.findByName").setParameter("name", name);
            return (TeachingSubject)query.getSingleResult();
        } catch (Exception e) {
            logger.error("getTeachingSubjectByName" + e.getMessage());
        }
        return null;
    }

    public TeachingSubject getById(Long id) {
        try {
            return this.em.find(TeachingSubject.class,id);
        } catch (Exception e) {
            return null;
        }
    }

    public CrxResponse add(TeachingSubject subject) {
        this.em.getTransaction().begin();
        if( this.getByName(subject.getName()) != null) {
            return new CrxResponse("ERROR","Teaching Subject does already exist.");
        }
        subject.setCreator(this.session.getUser());
        this.em.persist(subject);
        this.em.getTransaction().commit();
        return new CrxResponse("OK","Teaching Subject was created.",subject.getId());
    }

}
