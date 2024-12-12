package de.cranix.services;

import de.cranix.dao.CrxResponse;
import de.cranix.dao.ParentRequest;
import de.cranix.dao.Session;
import de.cranix.dao.User;
import org.checkerframework.checker.units.qual.C;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

public class ParentService extends Service{

    public ParentService(Session session, EntityManager em) {
        super(session,em);
    }

    public CrxResponse add(User user){
        try{
            user.setRole("parents");
            this.em.getTransaction().begin();
            this.em.persist(user);
            this.em.getTransaction().commit();
        }catch (Exception e){
            logger.error("ParentService add" + e.getMessage());
            return new CrxResponse("ERROR",e.getMessage());
        }
        return new CrxResponse("OK","Parent was created successfully",user.getId());
    }

    public CrxResponse modify(User user){
        if(!user.getRole().equals("parents")){
            return new CrxResponse("ERROR","Only parents can be modified here.");
        }
        try{
            this.em.getTransaction().begin();
            this.em.merge(user);
            this.em.getTransaction().commit();
        }catch (Exception e){
            logger.error("ParentService add" + e.getMessage());
            return new CrxResponse("ERROR",e.getMessage());
        }
        return new CrxResponse("OK","Parent was modified successfully");
    }

    public CrxResponse delete(Long id){
        User user = this.em.find(User.class,id);
        if(!user.getRole().equals("parents")){
            return new CrxResponse("ERROR","Only parents can be modified here.");
        }
        try{
            this.em.getTransaction().begin();
            this.em.remove(user);
            this.em.getTransaction().commit();
        }catch (Exception e){
            logger.error("ParentService add" + e.getMessage());
            return new CrxResponse("ERROR",e.getMessage());
        }
        return new CrxResponse("OK","Parent was deleted successfully");
    }

    public CrxResponse setChildren(Long id, List<User> children){
        User user = this.em.find(User.class,id);
        if(!user.getRole().equals("parents")){
            return new CrxResponse("ERROR","Only parents can be modified here.");
        }
        for(User child: children){
            if(!child.getRole().equals("students")){
                return new CrxResponse("ERROR","Only students can be assigned to parents.");
            }
        }
        try{
            this.em.getTransaction().begin();
            user.setChildren(children);
            this.em.merge(user);
            this.em.getTransaction().commit();
        }catch (Exception e){
            logger.error("ParentService add" + e.getMessage());
            return new CrxResponse("ERROR",e.getMessage());
        }
        return new CrxResponse("OK","Parent was deleted successfully");
    }

    public CrxResponse createParentRequest(ParentRequest parentRequest, HttpServletRequest req) {
        //TODO not allow to much from same IP
        try {
            this.em.getTransaction().begin();
            this.em.persist(parentRequest);
            this.em.getTransaction().commit();
        }catch (Exception e){
            logger.error("ParentService createParentRequest" + e.getMessage());
            return new CrxResponse("ERROR","Your request was not created.");
        }
        return new CrxResponse("OK","Your request was created successfully.");
    }
}
