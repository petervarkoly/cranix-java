package de.cranix.services;

import de.cephalix.dao.CephalixTicket;
import de.cranix.dao.*;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

public class CrxTicketService extends Service {
    public CrxTicketService(Session session, EntityManager em) {
        super(session, em);
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
            }else{
                ticket.setTicketStatus("W");
            }
            article.setCreator(this.session.getUser());
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
            User owner = em.find(User.class, userId);
            ticket.setOwner(owner);
            em.getTransaction().begin();
            em.merge(ticket);
            em.getTransaction().commit();
        } catch (Exception e) {
            return new CrxResponse("ERROR", e.getMessage());
        }
        return new CrxResponse("OK", "Now you are the owner");
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
        Long ownerId = this.session.getUserId();
        for (CrxTicket ticket : (List<CrxTicket>) query.getResultList()) {
            if (ticket.getOwner() == null || ticket.getOwner().getId() == ownerId) {
                if (status.contains(ticket.getTicketStatus())) {
                    tickets.add(ticket);
                }
            }
        }
        return tickets;
    }

    public CrxResponse delete(Long id) {

    }
}
