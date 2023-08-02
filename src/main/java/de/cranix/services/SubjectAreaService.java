package de.cranix.services;

import de.cranix.dao.CrxResponse;
import de.cranix.dao.Session;
import de.cranix.dao.SubjectArea;
import de.cranix.dao.TeachingSubject;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

public class SubjectAreaService extends Service{
    public SubjectAreaService(Session session, EntityManager em) {
        super(session, em);
    }

    public List<SubjectArea> getAll() {
        try {
            Query query = this.em.createNamedQuery("SubjectArea.findAll");
            return (List<SubjectArea>) query.getResultList();
        } catch (Exception e) {
            logger.error("getAll" + e.getMessage());
            return new ArrayList<>();
        }
    }

    public SubjectArea getByName(String name) {
        try {
            Query query = this.em.createNamedQuery("SubjectArea.findByName").setParameter("name", name);
            return (SubjectArea)query.getSingleResult();
        } catch (Exception e) {
            logger.error("getByName" + e.getMessage());
        }
        return null;
    }

    public CrxResponse add(SubjectArea subjectArea){
        this.em.getTransaction().begin();
        subjectArea.setCreator(this.session.getUser());
        this.em.persist(subjectArea);
        this.em.getTransaction().commit();
        return new CrxResponse(this.session,"OK","Subject area was created.",subjectArea.getId());
    }

    public CrxResponse add(Long subjectId, SubjectArea subjectArea) {
        this.em.getTransaction().begin();
        TeachingSubject teachingSubject = this.em.find(TeachingSubject.class,subjectId);
        if( this.getByName(subjectArea.getName()) != null) {
            return new CrxResponse(this.session,"ERROR","Subject Subject does already exist.");
        }
        subjectArea.setTeachingSubject(teachingSubject);
        subjectArea.setCreator(this.session.getUser());
        this.em.persist(subjectArea);
        teachingSubject.getSubjectAreaList().add(subjectArea);
        this.em.merge(subjectArea);
        this.em.getTransaction().commit();
        return new CrxResponse(this.session,"OK","Teaching area was created.",subjectArea.getId());
    }
}
