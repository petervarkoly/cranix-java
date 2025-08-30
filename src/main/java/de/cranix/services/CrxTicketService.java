package de.cranix.services;

import de.cranix.dao.*;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class CrxTicketService extends Service {

    private boolean manager = false;

    public CrxTicketService(Session session, EntityManager em) {
        super(session, em);
        manager = this.isAllowed("crxticket.worker") || this.isAllowed("crxticket.manager");
    }

    public CrxResponse add(CrxTicket ticket) {
        try {
            em.getTransaction().begin();
            ticket.setCreator(this.session.getUser());
            CrxTicketArticle article = new CrxTicketArticle(this.session);
            article.setText(ticket.getText());
            ticket.addArticle(article);
            em.persist(ticket);
            em.getTransaction().commit();
        } catch (Exception e) {
            return new CrxResponse("ERROR", e.getMessage());
        }
        return new CrxResponse("OK", "Ticket was created", ticket.getId());
    }

    public CrxTicket getById(Long id) {
        return em.find(CrxTicket.class, id);
    }

    public CrxResponse close(CrxTicket ticket) {
        try {
            em.getTransaction().begin();
            ticket.setTicketStatus("D");
            em.merge(ticket);
            em.getTransaction().commit();
        } catch (Exception e) {
            return new CrxResponse("ERROR", e.getMessage());
        }
        return new CrxResponse("OK", "Ticket was closed successfully.");
    }

    public CrxResponse close(Long ticketId) {
        return this.close(this.getById(ticketId));
    }

    public CrxResponse addArticle(Long id, CrxTicketArticle article) {
        try {
            CrxTicket ticket = getById(id);
            if(ticket.getCreator().equals(this.session.getUser())){
                //Now a response is arrived from the creator
                if(!ticket.getTicketStatus().equals("N")) {
                    ticket.setTicketStatus("R");
                }
            }else if(manager){
                //Worker or manager answered a ticket
                ticket.setTicketStatus("W");
            }else{
                return new CrxResponse("ERROR", "You are not allowed to answer this ticket");
            }
            article.setCreator(this.session.getUser());
            ticket.setModified(new Date());
            ticket.addArticle(article);
            em.getTransaction().begin();
            em.merge(ticket);
            em.getTransaction().commit();
        } catch (Exception e) {
            return new CrxResponse("ERROR", e.getMessage());
        }
        return new CrxResponse("OK", "Article was created successfully.");
    }

    public CrxResponse assignTicket(Long ticketId, Long userId) {
        try {
            CrxTicket ticket = getById(ticketId);
            User assignee = em.find(User.class, userId);
            ticket.setAssignee(assignee);
            em.getTransaction().begin();
            em.merge(ticket);
            em.getTransaction().commit();
        } catch (Exception e) {
            return new CrxResponse("ERROR", e.getMessage());
        }
        return new CrxResponse("OK", "Now you are the assignee");
    }

    public List<CrxTicket> getMyTickets(String status) {
        Query query = em.createNamedQuery("CrxTicket.findAll");
        List<CrxTicket> tickets = new ArrayList<>();
        Long creatorId = this.session.getUserId();
        for (CrxTicket ticket : (List<CrxTicket>) query.getResultList()) {
            if (ticket.getCreatorId() == creatorId) {
                if (status.contains(ticket.getTicketStatus())) {
                    tickets.add(ticket);
                }
            }
        }
        return tickets;
    }

    public List<CrxTicket> getTicketsForMe(String status) {
        Query query = em.createNamedQuery("CrxTicket.findAll");
        List<CrxTicket> tickets = new ArrayList<>();
        Long assigneeId = this.session.getUserId();
        for (CrxTicket ticket : (List<CrxTicket>) query.getResultList()) {
            if (ticket.getAssignee() == null || ticket.getAssignee().getId() == assigneeId) {
                if (status.contains(ticket.getTicketStatus())) {
                    tickets.add(ticket);
                }
            }
        }
        return tickets;
    }

    public CrxResponse delete(Long id) {
        try {
            CrxTicket ticket = getById(id);
            em.getTransaction().begin();
            em.remove(ticket);
            em.getTransaction().commit();
            return new CrxResponse("OK","Ticket was deleted successfully");
        }catch (Exception e){
            return new CrxResponse("ERROR",e.getMessage());
        }
    }

    public Object getStatus() {

        Query query = em.createNamedQuery("CrxTicket.findAll");
        HashMap<String, Integer> result = new HashMap<>();
        result.put("N",0);
        result.put("R",0);
        result.put("W",0);
        Long assigneeId = this.session.getUserId();
        String status = "";
        for (CrxTicket ticket : (List<CrxTicket>) query.getResultList()) {
            status = ticket.getTicketStatus();
            if(status.equals("D")){
                continue;
            }
            if(ticket.getCreator().equals(session.getUser())){
                result.put(status, result.get(status)+1);
                continue;
            }
            if(manager){
                if(ticket.getAssignee() == null  || ticket.getAssignee().getId() == assigneeId){
                    result.put(status, result.get(status)+1);
                }
            }
        }
        return  result;
    }
}
