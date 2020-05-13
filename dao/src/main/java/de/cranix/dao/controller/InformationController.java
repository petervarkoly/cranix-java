/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.dao.controller;

import java.util.List;
import java.util.ArrayList;

import javax.persistence.EntityManager;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import de.cranix.dao.Announcement;
import de.cranix.dao.Category;
import de.cranix.dao.Contact;
import de.cranix.dao.FAQ;
import de.cranix.dao.Group;
import de.cranix.dao.CrxResponse;
import de.cranix.dao.Session;
import de.cranix.dao.User;
/**
 * @author varkoly
 *
 */
public class InformationController extends Controller {

	/**
	 * @param session
	 */
	public InformationController(Session session,EntityManager em) {
		super(session,em);
	}

	/**
	 * Creates a new announcement
	 * @param announcement
	 * @return The result in form of CrxResponse
	 * @see Announcement
	 */
	public CrxResponse addAnnouncement(Announcement announcement) {
		//Check parameters
		StringBuilder errorMessage = new StringBuilder();
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		for (ConstraintViolation<Announcement> violation : factory.getValidator().validate(announcement) ) {
			errorMessage.append(violation.getMessage()).append(getNl());
		}
		if( errorMessage.length() > 0 ) {
			return new CrxResponse(this.getSession(),"ERROR", errorMessage.toString());
		}
		User user = this.session.getUser();
		announcement.setOwner(user);
		announcement.setCategories( new ArrayList<Category>() );
		Category category;
		try {
			this.em.getTransaction().begin();
			this.em.persist(announcement);
			user.getMyAnnouncements().add(announcement);
			this.em.merge(user);
			for( Long categoryId : announcement.getCategoryIds() ) {
				try {
					category = this.em.find(Category.class, categoryId);
					category.getAnnouncements().add(announcement);
					announcement.getCategories().add(category);
					this.em.merge(category);
					this.em.merge(announcement);
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
			}
			this.em.getTransaction().commit();
			logger.debug("Created Announcement:" + announcement);
			return new CrxResponse(this.getSession(),"OK", "Announcement was created succesfully.",announcement.getId());
		} catch (Exception e) {
			logger.error("add " + e.getMessage(),e);
			return new CrxResponse(this.getSession(),"ERROR", e.getMessage());
		} finally {
		}
	}

	/**
	 * Creates a new contact
	 * @param contact 
	 * @return The result in form of CrxResponse
	 * @see Contact
	 */
	public CrxResponse addContact(Contact contact) {
		//Check parameters
		StringBuilder errorMessage = new StringBuilder();
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		for (ConstraintViolation<Contact> violation : factory.getValidator().validate(contact) ) {
			errorMessage.append(violation.getMessage()).append(getNl());
		}
		if( errorMessage.length() > 0 ) {
			return new CrxResponse(this.getSession(),"ERROR", errorMessage.toString());
		}
		User user = this.session.getUser();
		contact.setOwner(user);
		Category category;
		try {
			this.em.getTransaction().begin();
			this.em.persist(contact);
			user.getMyContacts().add(contact);
			this.em.merge(user);
			for( Long categoryId : contact.getCategoryIds() ) {
				try {
					category = this.em.find(Category.class, categoryId);
					category.getContacts().add(contact);
					contact.getCategories().add(category);
					this.em.merge(category);
					this.em.merge(contact);
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
			}
			this.em.getTransaction().commit();
			logger.debug("Created Contact:" + contact);
			return new CrxResponse(this.getSession(),"OK", "Contact was created succesfully.",contact.getId());
		} catch (Exception e) {
			logger.error("add " + e.getMessage(),e);
			return new CrxResponse(this.getSession(),"ERROR", e.getMessage());
		} finally {
		}
	}

	/**
	 * Creates a new FAQ
	 * @param faq 
	 * @return The result in form of CrxResponse
	 * @see FAQ
	 */
	public CrxResponse addFAQ(FAQ faq) {
		//Check parameters
		StringBuilder errorMessage = new StringBuilder();
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		for (ConstraintViolation<FAQ> violation : factory.getValidator().validate(faq) ) {
			errorMessage.append(violation.getMessage()).append(getNl());
		}
		if( errorMessage.length() > 0 ) {
			return new CrxResponse(this.getSession(),"ERROR", errorMessage.toString());
		}
		User user = this.session.getUser();
		faq.setOwner(user);
		Category category;
		try {
			this.em.getTransaction().begin();
			this.em.persist(faq);
			user.getMyFAQs().add(faq);
			this.em.merge(user);
			for( Long categoryId : faq.getCategoryIds() ) {
				try {
					category = this.em.find(Category.class, categoryId);
					category.getFaqs().add(faq);
					faq.getCategories().add(category);
					this.em.merge(category);
					this.em.merge(faq);
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
			}
			this.em.getTransaction().commit();
			logger.debug("Created FAQ:" + faq);
			return new CrxResponse(this.getSession(),"OK", "FAQ was created succesfully.",faq.getId());
		} catch (Exception e) {
			logger.error("add " + e.getMessage(),e);
			return new CrxResponse(this.getSession(),"ERROR", e.getMessage());
		} finally {
		}
	}

	/**
	 * Delivers the valid announcements corresponding to the session user.
	 * @return
	 * @see Announcement
	 */
	public List<Announcement> getAnnouncements() {
		List<Announcement> announcements = new ArrayList<Announcement>();
		User user;
		try {
			user = this.em.find(User.class, this.session.getUser().getId());
		} catch (Exception e) {
			logger.error("add " + e.getMessage(),e);
			return null;
		} finally {
		}
		for(Group group : user.getGroups() ) {
			for(Category category : group.getCategories() ) {
				List<Category> categories = new ArrayList<Category>();
				categories.add(category);
				for(Announcement announcement : category.getAnnouncements() ) {
					if( announcement.getValidFrom().before(this.now()) &&
					    announcement.getValidUntil().after(this.now())
					)
					{
						announcement.setCategories(categories);
						announcement.setText("");
						announcements.add(announcement);
					}
				}
			}
		}
		
		return announcements;
	}

	public List<Announcement> getNewAnnouncements() {
		List<Announcement> announcements = new ArrayList<Announcement>();
		User user;
		try {
			user = this.em.find(User.class, this.session.getUser().getId());
		} catch (Exception e) {
			logger.error("add " + e.getMessage(),e);
			return null;
		} finally {
		}
		for(Group group : user.getGroups() ) {
			for(Category category : group.getCategories() ) {
				List<Category> categories = new ArrayList<Category>();
				categories.add(category);
				for(Announcement announcement : category.getAnnouncements() ) {
					if( !this.now().before(announcement.getValidFrom()) &&
						!this.now().after(announcement.getValidUntil()) &&
						!user.getReadAnnouncements().contains(announcement) )
					{
						announcement.setCategories(categories);
						announcements.add(announcement);
					}
				}
			}
		}
		return announcements;
	}

	public CrxResponse setAnnouncementHaveSeen(Long announcementId) {
		try {
			Announcement announcement = this.em.find(Announcement.class, announcementId);
			User user = this.session.getUser();
			announcement.getHaveSeenUsers().add(user);
			user.getReadAnnouncements().add(announcement);
			this.em.getTransaction().begin();
			this.em.merge(user);
			this.em.merge(announcement);
			this.em.getTransaction().commit();
		}catch (Exception e) {
			logger.error("setAnnouncementHaveSeen:" + this.getSession().getUserId() + " " + e.getMessage(),e);
			return new CrxResponse(this.getSession(),"ERROR","Annoncement could not be set as seen.");
		} finally {
		}
		return new CrxResponse(this.getSession(),"OK","Annoncement was set as seen.");
	}

	/**
	 * Delivers the list of FAQs corresponding to the session user.
	 * @return
	 */
	public List<FAQ> getFAQs() {
		List<FAQ> faqs = new ArrayList<FAQ>();
		User user;
		try {
			user = this.em.find(User.class, this.session.getUser().getId());
		} catch (Exception e) {
			logger.error("add " + e.getMessage(),e);
			return null;
		} finally {
		}
		for(Group group : user.getGroups() ) {
			for(Category category : group.getCategories() ) {
				List<Category> categories = new ArrayList<Category>();
				categories.add(category);
				for(FAQ faq : category.getFaqs() ) {
					faq.setCategories(categories);
					faq.setText("");
					faqs.add(faq);
				}
			}
		}
		return faqs;
	}


	/**
	 * Delivers the list of contacts corresponding to the session user
	 * @return
	 */
	public List<Contact> getContacts() {
		List<Contact> contacts = new ArrayList<Contact>();
		User user;
		try {
			user = this.em.find(User.class, this.session.getUser().getId());
		} catch (Exception e) {
			logger.error("add " + e.getMessage(),e);
			return null;
		} finally {
		}
		for(Group group : user.getGroups() ) {
			for(Category category : group.getCategories() ) {
				List<Category> categories = new ArrayList<Category>();
				categories.add(category);
				for(Contact contact : category.getContacts() ) {
					contact.setCategories(categories);
					contacts.add(contact);
				}
			}
		}
		return contacts;
	}

	public Announcement getAnnouncementById(Long AnnouncementId) {
		try {
			return this.em.find(Announcement.class, AnnouncementId);
		} catch (Exception e) {
			logger.error("add " + e.getMessage(),e);
			return null;
		} finally {
		}
	}

	public Contact getContactById(Long ContactId) {
		try {
			return this.em.find(Contact.class, ContactId);
		} catch (Exception e) {
			logger.error("add " + e.getMessage(),e);
			return null;
		} finally {
		}
	}

	public FAQ getFAQById(Long FAQId) {
		try {
			return this.em.find(FAQ.class, FAQId);
		} catch (Exception e) {
			logger.error("add " + e.getMessage(),e);
			return null;
		} finally {
		}
	}

	public CrxResponse modifyAnnouncement(Announcement announcement) {
		//Check parameters
		StringBuilder errorMessage = new StringBuilder();
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		for (ConstraintViolation<Announcement> violation : factory.getValidator().validate(announcement) ) {
			errorMessage.append(violation.getMessage()).append(getNl());
		}
		if( errorMessage.length() > 0 ) {
			return new CrxResponse(this.getSession(),"ERROR", errorMessage.toString());
		}
		if( !this.mayModify(announcement) )
		{
			return new CrxResponse(this.getSession(),"ERROR", "You have no rights to modify this Announcement");
		}
		try {
			this.em.getTransaction().begin();
			Announcement oldAnnouncement = this.em.find(Announcement.class, announcement.getId());
			for( User user : oldAnnouncement.getHaveSeenUsers() ) {
				user.getReadAnnouncements().remove(oldAnnouncement);
				this.em.merge(user);
				//TODO check if there are relay changes.
			}
			oldAnnouncement.setIssue(announcement.getIssue());
			oldAnnouncement.setKeywords(announcement.getKeywords());
			oldAnnouncement.setText(announcement.getText());
			oldAnnouncement.setValidFrom(announcement.getValidFrom());
			oldAnnouncement.setValidUntil(announcement.getValidUntil());
			oldAnnouncement.setHaveSeenUsers(new ArrayList<User>());
			this.em.merge(announcement);
			this.em.getTransaction().commit();
			return new CrxResponse(this.getSession(),"OK", "Announcement was modified succesfully.");
		} catch (Exception e) {
			logger.error("add " + e.getMessage(),e);
			return new CrxResponse(this.getSession(),"ERROR", e.getMessage());
		} finally {
		}
	}

	public CrxResponse modifyContact(Contact contact) {
		//Check parameters
		StringBuilder errorMessage = new StringBuilder();
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		for (ConstraintViolation<Contact> violation : factory.getValidator().validate(contact) ) {
			errorMessage.append(violation.getMessage()).append(getNl());
		}
		if( errorMessage.length() > 0 ) {
			return new CrxResponse(this.getSession(),"ERROR", errorMessage.toString());
		}
		if( !this.mayModify(contact) )
		{
			return new CrxResponse(this.getSession(),"ERROR", "You have no rights to modify this contact");
		}
		try {
			this.em.getTransaction().begin();
			Contact oldContact = this.em.find(Contact.class, contact.getId());
			oldContact.setName(contact.getName());
			oldContact.setEmail(contact.getEmail());
			oldContact.setPhone(contact.getPhone());
			oldContact.setTitle(contact.getTitle());
			oldContact.setIssue(contact.getIssue());
			this.em.merge(oldContact);
			this.em.getTransaction().commit();
			return new CrxResponse(this.getSession(),"OK", "Contact was modified succesfully.");
		} catch (Exception e) {
			logger.error("add " + e.getMessage(),e);
			return new CrxResponse(this.getSession(),"ERROR", e.getMessage());
		} finally {
		}
	}

	public CrxResponse modifyFAQ(FAQ faq) {
		//Check parameters
		StringBuilder errorMessage = new StringBuilder();
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		for (ConstraintViolation<FAQ> violation : factory.getValidator().validate(faq) ) {
			errorMessage.append(violation.getMessage()).append(getNl());
		}
		if( errorMessage.length() > 0 ) {
			return new CrxResponse(this.getSession(),"ERROR", errorMessage.toString());
		}
		if( !this.mayModify(faq) )
		{
			return new CrxResponse(this.getSession(),"ERROR", "You have no rights to modify this FAQ ");
		}
		try {
			this.em.getTransaction().begin();
			FAQ oldFaq = this.em.find(FAQ.class, faq.getId());
			oldFaq.setIssue(faq.getIssue());
			oldFaq.setTitle(faq.getTitle());
			oldFaq.setText(faq.getText());
			this.em.merge(oldFaq);
			this.em.getTransaction().commit();
			return new CrxResponse(this.getSession(),"OK", "FAQ was modified succesfully.");
		} catch (Exception e) {
			logger.error("add " + e.getMessage(),e);
			return new CrxResponse(this.getSession(),"ERROR", e.getMessage());
		} finally {
		}
	}

	/**
	 * Remove a announcement.
	 * @param faqId The technical id of the announcement.
	 * @return The result in form of CrxResponse
	 */
	public CrxResponse deleteAnnouncement(Long announcementId) {
		Announcement announcement;
		try {
			announcement = this.em.find(Announcement.class, announcementId);
		} catch (Exception e) {
			logger.error("add " + e.getMessage(),e);
			return null;
		}
		if( !this.mayModify(announcement) )
		{
			return new CrxResponse(this.getSession(),"ERROR", "You have no rights to delete this Announcement");
		}
		try {
			this.em.getTransaction().begin();
			this.em.merge(announcement);
			for( Category category : announcement.getCategories() ) {
				category.getAnnouncements().remove(announcement);
				this.em.merge(category);
			}
			this.em.remove(announcement);
			this.em.getTransaction().commit();
			return new CrxResponse(this.getSession(),"OK", "Announcement was deleted succesfully.");
		} catch (Exception e) {
			logger.error("add " + e.getMessage(),e);
			return new CrxResponse(this.getSession(),"ERROR", e.getMessage());
		} finally {
		}
	}

	/**
	 * Remove a contact.
	 * @param faqId The technical id of the contact.
	 * @return The result in form of CrxResponse
	 */
	public CrxResponse deleteContact(Long contactId) {
		Contact contact;
		try {
			contact = this.em.find(Contact.class, contactId);
		} catch (Exception e) {
			logger.error("add " + e.getMessage(),e);
			return null;
		}
		if( !this.mayModify(contact) )
		{
			return new CrxResponse(this.getSession(),"ERROR", "You have no rights to delete this contact");
		}
		try {
			this.em.getTransaction().begin();
			this.em.merge(contact);
			for( Category category : contact.getCategories() ) {
				category.getContacts().remove(contact);
				this.em.merge(category);
			}
			this.em.remove(contact);
			this.em.getTransaction().commit();
			return new CrxResponse(this.getSession(),"OK", "Contact was deleted succesfully.");
		} catch (Exception e) {
			logger.error("add " + e.getMessage(),e);
			return new CrxResponse(this.getSession(),"ERROR", e.getMessage());
		} finally {
		}
	}

	/**
	 * Remove a FAQ.
	 * @param faqId The technical id of the faq.
	 * @return The result in form of CrxResponse
	 */
	public CrxResponse deleteFAQ(Long faqId) {
		FAQ faq;
		try {
			faq = this.em.find(FAQ.class, faqId);
		} catch (Exception e) {
			logger.error("add " + e.getMessage(),e);
			return null;
		}
		if( !this.mayModify(faq) )
		{
			return new CrxResponse(this.getSession(),"ERROR", "You have no rights to delete this FAQ");
		}
		try {
			this.em.getTransaction().begin();
			this.em.merge(faq);
			for( Category category : faq.getCategories() ) {
				category.getFaqs().remove(faq);
				this.em.merge(category);
			}
			this.em.remove(faq);
			this.em.getTransaction().commit();
			return new CrxResponse(this.getSession(),"OK", "FAQ was deleted succesfully.");
		} catch (Exception e) {
			logger.error("add " + e.getMessage(),e);
			return new CrxResponse(this.getSession(),"ERROR", e.getMessage());
		} finally {
		}
	}


	/**
	 * Get the list of information categories, the user have access on it.
	 * These are the own categories and the public categories by type informations.
	 * @return The list of the found categories.
	 */
	public List<Category> getInfoCategories() {
		CategoryController categoryController = new CategoryController(this.session,this.em);
		boolean isSuperuser = this.isSuperuser();
		List<Category> categories = new ArrayList<Category>();
		for(Category category : categoryController.getByType("informations") ) {
			if(isSuperuser ||  category.isPublicAccess() || this.session.getUser().equals(category.getOwner())) {
				if( !categories.contains(category) ) {
					categories.add(category);
				}
			}
		}
		for(Category category : categoryController.getByType("smartRoom") ) {
			logger.debug("getInfoCategories smartRoom:" + category );
			if( isSuperuser ||  category.isPublicAccess() || this.session.getUser().equals(category.getOwner())) {
				if( !categories.contains(category) ) {
					categories.add(category);
				}
			}
		}
		return categories;
	}

}
