package de.cranix.services;

import de.cranix.dao.CrxNote;
import de.cranix.dao.CrxResponse;
import de.cranix.dao.Session;
import de.cranix.dao.User;

import javax.persistence.EntityManager;
import javax.ws.rs.WebApplicationException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrxNoteService extends Service{

    Logger logger = LoggerFactory.getLogger(CrxNoteService.class);

    public CrxNoteService(Session session, EntityManager em){
        super(session,em);
    }

    public CrxResponse add(CrxNote note){
        try {
            logger.debug("CrxNote add:" + note);
            User user = em.find(User.class,this.session.getUserId());
            user.addCrxNote(note);
            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();
            return new CrxResponse("OK","Note was created successfully");
        } catch (Exception e) {
            return new CrxResponse("ERROR", "Note was not created:" + e.getMessage());
        }
    }

    public CrxResponse remove(Long id){
        try {
            CrxNote note = em.find(CrxNote.class,id);
            if(note == null){
                return new CrxResponse("ERROR", "Note was not found");
            }
            User creator = note.getCreator();
            if( !this.isSuperuser() && !creator.equals(session.getUser())){
                return new CrxResponse("ERROR", "You have no rights to remove this note.");
            }
            em.getTransaction().begin();
            creator.removeCrxNote(note);
            em.merge(creator);
            em.remove(note);
            em.getTransaction().commit();
            return new CrxResponse("OK","Note was removed successfully");
        } catch (Exception e) {
            return new CrxResponse("ERROR", "Note was not removed:" + e.getMessage());
        }
    }

    public CrxResponse patch(CrxNote note) {
        logger.debug("CrxNote patch:" + note);
        CrxNote oldNote = em.find(CrxNote.class,note.getId());
        if(oldNote == null){
            return new CrxResponse("ERROR", "Note was not found");
        }
        User creator = oldNote.getCreator();
        if( !this.isSuperuser() && !creator.equals(session.getUser())){
            return new CrxResponse("ERROR", "You have no rights to modify this note.");
        }
        oldNote.setGrading(note.getGrading());
        oldNote.setNoteType(note.getNoteType());
        oldNote.setModified(new Date());
        oldNote.setReminder(note.getReminder());
        oldNote.setTitle(note.getTitle());
        oldNote.setText(note.getText());
        oldNote.setWeighting(note.getWeighting());
        try{
            em.getTransaction().begin();
            em.merge(oldNote);
            em.getTransaction().commit();
            return new CrxResponse("OK","Note was modified successfully");
        }catch (Exception e){
            return new CrxResponse("ERROR","Note was not modified:" + e.getMessage()
            );
        }
    }

    public CrxNote getById(Long id){
        CrxNote note = em.find(CrxNote.class,id);
        if(note == null){
            throw new WebApplicationException(404);
        }
        if( !this.isSuperuser() && !note.getCreator().equals(session.getUser())){
            throw new WebApplicationException(403);
        }
        return note;
    }

    public List<CrxNote> get() {
        return em.find(User.class,this.session.getUserId()).getCrxNotes();
    }

    public List<CrxNote> getByFilter(CrxNote filter) {
        List<CrxNote> notes = new ArrayList<>();
        for(CrxNote note: em.find(User.class,this.session.getUserId()).getCrxNotes()){
            if(
                    filter.getObjectType().equals(note.getObjectType()) &&
                    filter.getObjectId().equals(note.getObjectId())
            ){
                if( filter.getPtmId() == 0 || note.getPtmId() == filter.getPtmId() ) {
                    notes.add(note);
                }
            }
        }
        return notes;
    }
}