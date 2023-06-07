package de.cranix.services;

import de.cranix.dao.CrxResponse;
import de.cranix.dao.Session;
import de.cranix.dao.SubjectArea;
import de.cranix.dao.TeachingSubject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

import static org.reflections.Reflections.log;

public class TeachingSubjectService extends Service {

    Logger logger = LoggerFactory.getLogger(TeachingSubjectService.class);

    public TeachingSubjectService(Session session, EntityManager em) {
        super(session, em);
    }

    public List<TeachingSubject> getTeachingSubjects() {
        try {
            Query query = this.em.createNamedQuery("TeachingSubject.findAll");
            return (List<TeachingSubject>) query.getResultList();
        } catch (Exception e) {
            logger.error("getTeachingSubjects: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public TeachingSubject getTeachingSubjectByName(String name) {
        try {
            Query query = this.em.createNamedQuery("TeachingSubject.findByName").setParameter("name", name);
            return (TeachingSubject)query.getSingleResult();
        } catch (Exception e) {
            logger.error("getTeachingSubjectByName" + e.getMessage());
        }
        return null;
    }

    public TeachingSubject getTeachingSubjectById(Long id) {
        try {
            return this.em.find(TeachingSubject.class,id);
        } catch (Exception e) {
            return null;
        }
    }

    public CrxResponse addTeachingSubjects(TeachingSubject subject) {
        this.em.getTransaction().begin();
        if( this.getTeachingSubjectByName(subject.getName()) != null) {
            return new CrxResponse(this.session,"ERROR","Teaching Subject does already exist.");
        }
        subject.setCreator(this.session.getUser());
        this.em.persist(subject);
        this.em.getTransaction().commit();
        return new CrxResponse(this.session,"OK","Teaching Subject was created.",subject.getId());
    }

    public List<SubjectArea> getSubjectAreas() {
        try {
            Query query = this.em.createNamedQuery("SubjectArea.findAll");
            return (List<SubjectArea>) query.getResultList();
        } catch (Exception e) {
            logger.error("getTeachingSubjects: " + e.getMessage());
            return new ArrayList<>();
        }
    }


    public SubjectArea getSubjectAreaById(Long id) {
        try {
            return this.em.find(SubjectArea.class,id);
        } catch (Exception e) {
            return null;
        }
    }

    public SubjectArea getSubjectAreaByName(String name) {
        try {
            Query query = this.em.createNamedQuery("SubjectArea.findByName").setParameter("name", name);
            return (SubjectArea)query.getSingleResult();
        } catch (Exception e) {
            logger.error("getSubjectAreaByName" + e.getMessage());
        }
        return null;
    }

    public CrxResponse addSubjectArea(Long subjectId, SubjectArea subjectArea) {
        this.em.getTransaction().begin();
        TeachingSubject teachingSubject = this.getTeachingSubjectById(subjectId);
        if( this.getSubjectAreaByName(subjectArea.getName()) != null) {
            return new CrxResponse(this.session,"ERROR","Teaching Subject does already exist.");
        }
        subjectArea.setTeachingSubject(teachingSubject);
        subjectArea.setCreator(this.session.getUser());
        this.em.persist(subjectArea);
        teachingSubject.getSubjectAreaList().add(subjectArea);
        this.em.merge(subjectArea);
        this.em.getTransaction().commit();
        return new CrxResponse(this.session,"OK","Teaching Subject was created.",subjectArea.getId());
    }
}
