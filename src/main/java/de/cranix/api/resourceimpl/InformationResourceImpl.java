/* (c) 2020 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.api.resourceimpl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import de.cranix.api.resources.InformationResource;
import de.cranix.dao.Announcement;
import de.cranix.dao.Category;
import de.cranix.dao.Contact;
import de.cranix.dao.FAQ;
import de.cranix.dao.CrxResponse;
import de.cranix.dao.Session;
import de.cranix.services.InformationService;
import de.cranix.helper.CrxEntityManagerFactory;

public class InformationResourceImpl implements InformationResource {

	public InformationResourceImpl() {
	}

	@Override
	public CrxResponse addAnnouncement(Session session, Announcement announcement) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		InformationService infoService = new InformationService(session,em);
		CrxResponse resp = infoService.addAnnouncement(announcement);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse addContact(Session session, Contact contact) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		InformationService infoService = new InformationService(session,em);
		CrxResponse resp = infoService.addContact(contact);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse addFAQ(Session session, FAQ faq) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		InformationService infoService = new InformationService(session,em);
		CrxResponse resp = infoService.addFAQ(faq);
		em.close();
		return resp;
	}

	@Override
	public List<Announcement> getAnnouncements(Session session) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		InformationService infoService = new InformationService(session,em);
		List<Announcement> resp = infoService.getAnnouncements();
		em.close();
		return resp;
	}

	@Override
	public List<Announcement> getNewAnnouncements(Session session) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<Announcement> resp = new InformationService(session,em).getNewAnnouncements();
		em.close();
		return resp;
	}

	@Override
	public CrxResponse setAnnouncementHaveSeen(Session session, Long announcementId) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new InformationService(session,em).setAnnouncementHaveSeen(announcementId);
		em.close();
		return resp;
	}

	@Override
	public List<Contact> getContacts(Session session) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		InformationService infoService = new InformationService(session,em);
		List<Contact> resp = infoService.getContacts();
		em.close();
		return resp;
	}

	@Override
	public List<FAQ> getFAQs(Session session) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		InformationService infoService = new InformationService(session,em);
		List<FAQ> resp = infoService.getFAQs();
		em.close();
		return resp;
	}

	@Override
	public CrxResponse deleteAnnouncement(Session session, Long announcementId) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		InformationService infoService = new InformationService(session,em);
		CrxResponse resp = infoService.deleteAnnouncement(announcementId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse deleteContact(Session session, Long contactId) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		InformationService infoService = new InformationService(session,em);
		CrxResponse resp = infoService.deleteContact(contactId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse deleteFAQ(Session session, Long faqId) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		InformationService infoService = new InformationService(session,em);
		CrxResponse resp = infoService.deleteFAQ(faqId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse modifyAnnouncement(Session session, Long announcementId, Announcement announcement) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		InformationService infoService = new InformationService(session,em);
		announcement.setId(announcementId);
		CrxResponse resp = infoService.modifyAnnouncement(announcement);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse modifyContact(Session session, Long contactId, Contact contact) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		InformationService infoService = new InformationService(session,em);
		contact.setId(contactId);
		CrxResponse resp = infoService.modifyContact(contact);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse modifyFAQ(Session session, Long faqId, FAQ faq) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		InformationService infoService = new InformationService(session,em);
		faq.setId(faqId);
		CrxResponse resp = infoService.modifyFAQ(faq);
		em.close();
		return resp;
	}

	@Override
	public List<Announcement> getMyAnnouncements(Session session) {
		List<Announcement> announcements = new ArrayList<Announcement>();
		for( Announcement a :  session.getUser().getMyAnnouncements() ) {
			a.setText("");
			announcements.add(a);
		}
		return announcements;
	}

	@Override
	public List<Contact> getMyContacts(Session session) {
		return session.getUser().getMyContacts();
	}

	@Override
	public List<FAQ> getMyFAQs(Session session) {
		List<FAQ> faqs = new ArrayList<FAQ>();
		for( FAQ faq :  session.getUser().getMyFAQs() ) {
			faq.setText("");
			faqs.add(faq);
		}
		return faqs;
	}

	@Override
	public List<Category> getInformationCategories(Session session) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<Category> resp = new InformationService(session,em).getInfoCategories();
		em.close();
		return resp;
	}

	@Override
	public Announcement getAnnouncement(Session session, Long announcementId) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		Announcement resp = new InformationService(session,em).getAnnouncementById(announcementId);
		em.close();
		return resp;
	}

	@Override
	public Contact getContact(Session session, Long contactId) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		Contact resp = new InformationService(session,em).getContactById(contactId);
		em.close();
		return resp;
	}

	@Override
	public FAQ getFAQ(Session session, Long faqId) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		FAQ resp = new InformationService(session,em).getFAQById(faqId);
		em.close();
		return resp;
	}
}
