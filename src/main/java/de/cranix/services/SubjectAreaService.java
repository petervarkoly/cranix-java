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
        return new CrxResponse("OK","Subject area was created.",subjectArea.getId());
    }

    public CrxResponse add(Long subjectId, SubjectArea subjectArea) {
        if( this.getByName(subjectArea.getName()) != null) {
            return new CrxResponse("ERROR","Subject area does already exist.");
        }
        this.em.getTransaction().begin();
        TeachingSubject teachingSubject = this.em.find(TeachingSubject.class,subjectId);
        subjectArea.setTeachingSubject(teachingSubject);
        subjectArea.setCreator(this.session.getUser());
        this.em.persist(subjectArea);
        teachingSubject.getSubjectAreaList().add(subjectArea);
        this.em.merge(subjectArea);
        this.em.getTransaction().commit();
        return new CrxResponse("OK","Teaching area was created.",subjectArea.getId());
    }

    public CrxResponse delete(Long id) {
        SubjectArea subjectArea = this.em.find(SubjectArea.class, id);
        if(subjectArea == null) {
            return new CrxResponse("ERROR","Subject Subject does not exist.");
        }
        try {
            this.em.getTransaction().begin();
            TeachingSubject teachingSubject = subjectArea.getTeachingSubject();
            teachingSubject.getSubjectAreaList().remove(subjectArea);
            this.em.merge((teachingSubject));
            this.em.getTransaction().commit();
            return new CrxResponse("OK", "Subject area was removed successfully.");
        }catch (Exception e){
            logger.error("delete:"+ id + " Error:" + e.getMessage());
            return new CrxResponse("ERROR", "Subject area could not be removed successfully.");
        }

    }

    public CrxResponse modify(SubjectArea subjectArea) {
        SubjectArea oldSubjectArea = this.em.find(SubjectArea.class, subjectArea.getId());
        if(oldSubjectArea == null){
            return new CrxResponse("ERROR","Can not find the subject area.");
        }
        oldSubjectArea.setName(subjectArea.getName());
        this.em.getTransaction().begin();
        this.em.merge(oldSubjectArea);
        this.em.getTransaction().commit();
        return new CrxResponse("OK","Subject area was modified successfully.");
    }
}
