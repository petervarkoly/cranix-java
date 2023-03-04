/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.services;

import java.util.List;
import java.util.ArrayList;

import javax.persistence.EntityManager;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import de.cranix.dao.*;

/**
 * @author varkoly
 *
 */
public class InformationService extends Service {

	/**
	 * @param session
	 */
	public InformationService(Session session,EntityManager em) {
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
		announcement.setCreator(user);
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
		contact.setCreator(user);
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
		faq.setCreator(user);
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
	public List<Announcement> getTasks() {
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
					if(!announcement.getIssue().equals("task")) {
						continue;
					}
					if( announcement.getValidFrom().before(this.now()) &&
							announcement.getValidUntil().after(this.now())
					)
					{
						announcement.setSeenByMe( announcement.getHaveSeenUsers().contains(this.session.getUser()));
						announcement.setCategories(categories);
						announcement.setText("");
						announcements.add(announcement);
					}
				}
			}
		}
		return announcements;
	}

	public List<Announcement> getNewTasks() {
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
					if(!announcement.getIssue().equals("task")) {
						continue;
					}
					if( !this.now().before(announcement.getValidFrom()) &&
							!this.now().after(announcement.getValidUntil()) &&
							!user.getReadAnnouncements().contains(announcement) )
					{
						announcement.setCategories(categories);
						announcement.setSeenByMe(false);
						announcements.add(announcement);
					}
				}
			}
		}
		return announcements;
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
					if(announcement.getIssue().equals("task")) {
						continue;
					}
					if( announcement.getValidFrom().before(this.now()) &&
					    announcement.getValidUntil().after(this.now())
					)
					{
						announcement.setSeenByMe( announcement.getHaveSeenUsers().contains(this.session.getUser()));
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
					if(announcement.getIssue().equals("task")) {
						continue;
					}
					if( !this.now().before(announcement.getValidFrom()) &&
						!this.now().after(announcement.getValidUntil()) &&
						!user.getReadAnnouncements().contains(announcement) )
					{
						announcement.setCategories(categories);
						announcement.setSeenByMe(false);
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
			if(announcement.getHaveSeenUsers().contains(user) ) {
				return new CrxResponse(this.getSession(),"OK","Annoncement was set as seen.");
			}
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
			oldAnnouncement.setTitle(announcement.getTitle());
			oldAnnouncement.setIssue(announcement.getIssue());
			oldAnnouncement.setKeywords(announcement.getKeywords());
			oldAnnouncement.setText(announcement.getText());
			oldAnnouncement.setValidFrom(announcement.getValidFrom());
			oldAnnouncement.setValidUntil(announcement.getValidUntil());
			oldAnnouncement.setHaveSeenUsers(new ArrayList<User>());
			for( Category category: oldAnnouncement.getCategories() ) {
				if( !announcement.getCategoryIds().contains(category.getId())) {
					category.getAnnouncements().remove(oldAnnouncement);
					oldAnnouncement.getCategories().remove(category);
					this.em.merge(category);
				}
			}
			for( Long categoryId: announcement.getCategoryIds() ) {
				boolean found = false;
				for( Category category: oldAnnouncement.getCategories() ) {
					if( categoryId == category.getId() ) {
						found = true;
						break;
					}
				}
				if( !found ) {
					Category newCategory = this.em.find(Category.class, categoryId);
					newCategory.getAnnouncements().add(oldAnnouncement);
					oldAnnouncement.getCategories().add(newCategory);
					this.em.merge(newCategory);
				}
			}
			this.em.merge(oldAnnouncement);
			this.em.merge(announcement.getCreator());
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
			for( Category category: oldContact.getCategories() ) {
				if( !contact.getCategoryIds().contains(category.getId())) {
					category.getContacts().remove(oldContact);
					oldContact.getCategories().remove(category);
					this.em.merge(category);
				}
			}
			for( Long categoryId: contact.getCategoryIds() ) {
				boolean found = false;
				for( Category category: oldContact.getCategories() ) {
					if( categoryId == category.getId() ) {
						found = true;
						break;
					}
				}
				if( !found ) {
					Category newCategory = this.em.find(Category.class, categoryId);
					newCategory.getContacts().add(oldContact);
					oldContact.getCategories().add(newCategory);
					this.em.merge(newCategory);
				}
			}
			this.em.merge(oldContact);
			this.em.merge(oldContact.getCreator());
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
			for( Category category: oldFaq.getCategories() ) {
				if( !faq.getCategoryIds().contains(category.getId())) {
					category.getFaqs().remove(oldFaq);
					oldFaq.getCategories().remove(category);
					this.em.merge(category);
				}
			}
			for( Long categoryId: faq.getCategoryIds() ) {
				boolean found = false;
				for( Category category: oldFaq.getCategories() ) {
					if( categoryId == category.getId() ) {
						found = true;
						break;
					}
				}
				if( !found ) {
					Category newCategory = this.em.find(Category.class, categoryId);
					newCategory.getFaqs().add(oldFaq);
					oldFaq.getCategories().add(newCategory);
					this.em.merge(newCategory);
				}
			}
			this.em.merge(oldFaq);
			this.em.merge(oldFaq.getCreator());
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
	 * @param announcementId The technical id of the announcement.
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
	 * @param contactId The technical id of the contact.
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
		CategoryService categoryService = new CategoryService(this.session,this.em);
		boolean isSuperuser = this.isSuperuser();
		List<Category> categories = new ArrayList<Category>();
		for(Category category : categoryService.getByType("informations") ) {
			if(isSuperuser ||  category.isPublicAccess() || this.session.getUser().equals(category.getCreator())) {
				if( !categories.contains(category) ) {
					categories.add(category);
				}
			}
		}
		for(Category category : categoryService.getByType("smartRoom") ) {
			logger.debug("getInfoCategories smartRoom:" + category );
			if( isSuperuser ||  category.isPublicAccess() || this.session.getUser().equals(category.getCreator())) {
				if( !categories.contains(category) ) {
					categories.add(category);
				}
			}
		}
		return categories;
	}

    public CrxResponse addTaskResponse(TaskResponse taskResponse) {
		Announcement announcement = this.getAnnouncementById((taskResponse.getParentId()));
		User owner       = this.session.getUser();
		try {
			this.em.getTransaction().begin();
			taskResponse.setRating("");
			taskResponse.setCreator(owner);
			announcement.addTasksResponses(taskResponse);
			if( ! announcement.getHaveSeenUsers().contains(owner) ) {
				announcement.getHaveSeenUsers().add(owner);
			}
			this.em.merge(announcement);
			owner.addTaskResponse(taskResponse);
			this.em.merge(owner);
			this.em.getTransaction().commit();
		} catch (Exception e) {
			logger.error("add " + e.getMessage(),e);
			return new CrxResponse(this.getSession(),"ERROR", e.getMessage());
		}
		return new CrxResponse(this.getSession(),"OK", "Task Response was created successfully");
    }

    public CrxResponse modifyTaskResponse(TaskResponse taskResponse) {
		TaskResponse oldResponse = this.em.find(TaskResponse.class,taskResponse.getId());
		if( ! this.session.getUser().equals(oldResponse.getCreator()) ) {
			return new CrxResponse(this.getSession(),"ERROR", "You are not allowed to modify this task response");
		}
		oldResponse.setText(taskResponse.getText());
		try {
			this.em.getTransaction().begin();
			this.em.merge(oldResponse);
			this.em.getTransaction().commit();
		} catch (Exception e) {
			logger.error("add " + e.getMessage(),e);
			return new CrxResponse(this.getSession(),"ERROR", e.getMessage());
		}
		return new CrxResponse(this.getSession(),"OK", "Task Response was modified successfully");
	}

	public CrxResponse rateTaskResponse(TaskResponse taskResponse) {
		TaskResponse oldResponse = this.em.find(TaskResponse.class,taskResponse.getId());
		if( ! this.session.getUser().equals(oldResponse.getParent().getCreator()) ) {
			return new CrxResponse(this.getSession(),"ERROR", "You are not allowed to rate this task response");
		}
		oldResponse.setRating(taskResponse.getRating());
		try {
			this.em.getTransaction().begin();
			this.em.merge(oldResponse);
			this.em.getTransaction().commit();
		} catch (Exception e) {
			logger.error("add " + e.getMessage(),e);
			return new CrxResponse(this.getSession(),"ERROR", e.getMessage());
		}
		return new CrxResponse(this.getSession(),"OK", "Task Response was rated successfully");

	}

	public TaskResponse findTaskResponseById(Long id) {
		try {
			TaskResponse taskResponse = this.em.find(TaskResponse.class, id);
			if( this.session.getUser().equals(taskResponse.getCreator()) ) {
				return  taskResponse;
			}
		} catch (Exception e) {
			logger.error("add " + e.getMessage(),e);
			return null;
		}
		return  null;
	}
}
