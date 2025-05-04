package de.cranix.services;

import de.cranix.dao.CrxNotice;
import de.cranix.dao.CrxResponse;
import de.cranix.dao.Session;
import de.cranix.dao.User;

import javax.persistence.EntityManager;
import javax.ws.rs.WebApplicationException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CrxNoticeService extends Service{

    public CrxNoticeService(Session session, EntityManager em){
        super(session,em);
    }

    public CrxResponse add(CrxNotice notice){
        try {
            User user = em.find(User.class,this.session.getUserId());
            user.addCrxNotice(notice);
            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();
            return new CrxResponse("OK","Notice was created successfully");
        } catch (Exception e) {
            return new CrxResponse("ERROR", "Notice was not created:" + e.getMessage());
        }
    }

    public CrxResponse remove(Long id){
        try {
            CrxNotice notice = em.find(CrxNotice.class,id);
            if(notice == null){
                return new CrxResponse("ERROR", "Notice was not found");
            }
            User creator = notice.getCreator();
            if( !this.isSuperuser() && !creator.equals(session.getUser())){
                return new CrxResponse("ERROR", "You have no rights to remove this notice.");
            }
            em.getTransaction().begin();
            creator.removeCrxNotice(notice);
            em.merge(creator);
            em.remove(notice);
            em.getTransaction().commit();
            return new CrxResponse("OK","Notice was removed successfully");
        } catch (Exception e) {
            return new CrxResponse("ERROR", "Notice was not removed:" + e.getMessage());
        }
    }

    public CrxResponse patch(CrxNotice notice) {
        CrxNotice oldNotice = em.find(CrxNotice.class,notice.getId());
        if(oldNotice == null){
            return new CrxResponse("ERROR", "Notice was not found");
        }
        User creator = oldNotice.getCreator();
        if( !this.isSuperuser() && !creator.equals(session.getUser())){
            return new CrxResponse("ERROR", "You have no rights to modify this notice.");
        }
        oldNotice.setGrading(notice.getGrading());
        oldNotice.setNoticeType(notice.getNoticeType());
        oldNotice.setModified(new Date());
        oldNotice.setReminder(notice.getReminder());
        oldNotice.setTitle(notice.getTitle());
        oldNotice.setText(notice.getText());
        oldNotice.setWeighting(notice.getWeighting());
        try{
            em.getTransaction().begin();
            em.merge(oldNotice);
            em.getTransaction().commit();
            return new CrxResponse("OK","Notice was modified successfully");
        }catch (Exception e){
            return new CrxResponse("ERROR","Notice was not modified:" + e.getMessage()
            );
        }
    }

    public CrxNotice getById(Long id){
        CrxNotice notice = em.find(CrxNotice.class,id);
        if(notice == null){
            throw new WebApplicationException(404);
        }
        if( !this.isSuperuser() && !notice.getCreator().equals(session.getUser())){
            throw new WebApplicationException(403);
        }
        return notice;
    }

    public List<CrxNotice> get() {
        return em.find(User.class,this.session.getUserId()).getCrxNotices();
    }

    public List<CrxNotice> getByFilter(CrxNotice filter) {
        List<CrxNotice> notices = new ArrayList<>();
        for(CrxNotice notice: em.find(User.class,this.session.getUserId()).getCrxNotices()){
            if(
                    filter.getObjectType().equals(notice.getObjectType()) &&
                    filter.getObjectId().equals(notice.getObjectId())
            ){
                if(
                        filter.getIssueType().isEmpty() ||
                                (
                                        filter.getIssueType().equals(notice.getIssueType()) &&
                                        filter.getIssueId().equals(notice.getIssueId())
                                )
                ) {
                    notices.add(notice);
                }
            }
        }
        return notices;
    }
}
