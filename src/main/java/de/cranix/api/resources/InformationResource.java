/* (c) 2021 Peter Varkoly <pvarkoly@cephalix.eu> - all rights reserved */
package de.cranix.api.resources;

import static de.cranix.api.resources.Resource.*;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import de.cranix.dao.*;
import de.cranix.services.InformationService;
import de.cranix.helper.CrxEntityManagerFactory;

import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Path("informations")
@Api(value = "informations")
@Produces(JSON_UTF8)
public class InformationResource {

	Logger logger = LoggerFactory.getLogger(InformationResource.class);
	public InformationResource() { }

	@POST
	@Path("announcements")
	@ApiOperation(value = "Creates a ne announcement.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("information.add")
	public CrxResponse addAnnouncement(
		@ApiParam(hidden = true) @Auth Session session,
		Announcement announcement
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new InformationService(session,em).addAnnouncement(announcement);
		em.close();
		return resp;
	}

	@POST
	@Path("tasks")
	@ApiOperation(value = "Creates a ne announcement.")
	@ApiResponses(value = {
			@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("information.add")
	public CrxResponse addTask(
			@ApiParam(hidden = true) @Auth Session session,
			Announcement announcement
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new InformationService(session,em).addAnnouncement(announcement);
		em.close();
		return resp;
	}

	@POST
	@Path("contacts")
	@ApiOperation(value = "Creates a new contact.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("information.add")
	public CrxResponse addContact(
		@ApiParam(hidden = true) @Auth Session session,
		Contact contact
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new InformationService(session,em).addContact(contact);
		em.close();
		return resp;
	}

	@POST
	@Path("faqs")
	@ApiOperation(value = "Creates a new FAQ.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("information.add")
	public CrxResponse addFAQ(
		@ApiParam(hidden = true) @Auth Session session,
		FAQ faq
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new InformationService(session,em).addFAQ(faq);
		em.close();
		return resp;
	}

	@GET
	@Path("announcements")
	@ApiOperation(value = "Gets the announcements corresponding to an user.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	public List<Announcement> getAnnouncements( @ApiParam(hidden = true) @Auth Session session) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<Announcement> resp = new InformationService(session,em).getAnnouncements();
		em.close();
		return resp;
	}

	@GET
	@Path("newAnnouncements")
	@ApiOperation(value = "Gets the announcements corresponding to an user.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	public List<Announcement> getNewAnnouncements( @ApiParam(hidden = true) @Auth Session session) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<Announcement> resp = new InformationService(session,em).getNewAnnouncements();
		em.close();
		return resp;
	}

	@PUT
	@Path("announcements/{announcementId}/seen")
	@ApiOperation(value = "Mark the announcement for the user as have seen.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	public CrxResponse setAnnouncementHaveSeen(
		@ApiParam(hidden = true)      @Auth Session session,
		@PathParam("announcementId")  Long announcementId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new InformationService(session,em).setAnnouncementHaveSeen(announcementId);
		em.close();
		return resp;
	}

	@GET
	@Path("tasks")
	@ApiOperation(value = "Gets the announcements corresponding to an user.")
	@ApiResponses(value = {
			@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	public List<Announcement> getTasks( @ApiParam(hidden = true) @Auth Session session) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<Announcement> resp = new InformationService(session,em).getTasks();
		em.close();
		return resp;
	}

	@GET
	@Path("newTasks")
	@ApiOperation(value = "Gets the announcements corresponding to an user.")
	@ApiResponses(value = {
			@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	public List<Announcement> getNewTasks( @ApiParam(hidden = true) @Auth Session session) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<Announcement> resp = new InformationService(session,em).getNewTasks();
		em.close();
		return resp;
	}

	@PUT
	@Path("tastks/{taskId}/seen")
	@ApiOperation(value = "Mark the announcement for the user as have seen.")
	@ApiResponses(value = {
			@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	public CrxResponse setTaskHaveSeen(
			@ApiParam(hidden = true)      @Auth Session session,
			@PathParam("taskId")  Long taskId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new InformationService(session,em).setAnnouncementHaveSeen(taskId);
		em.close();
		return resp;
	}

	@GET
	@Path("contacts")
	@ApiOperation(value = "Gets the contacts corresponding to an user.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	public List<Contact> getContacts( @ApiParam(hidden = true) @Auth Session session) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<Contact> resp = new InformationService(session,em).getContacts();
		em.close();
		return resp;
	}

	@GET
	@Path("faqs")
	@ApiOperation(value = "Gets the FAQs corresponding to an user.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	public List<FAQ> getFAQs( @ApiParam(hidden = true) @Auth Session session) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<FAQ> resp = new InformationService(session,em).getFAQs();
		em.close();
		return resp;
	}

	@GET
	@Path("my/announcements")
	@ApiOperation(value = "Gets the announcements of an user.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("information.add")
	public List<Announcement> getMyAnnouncements( @ApiParam(hidden = true) @Auth Session session) {
		List<Announcement> announcements = new ArrayList<Announcement>();
		logger.debug("SessionUser" + session.getUser().getMyAnnouncements());
		for( Announcement a :  session.getUser().getMyAnnouncements() ) {
			if(!a.getIssue().equals("task")) {
				a.setText("");
				announcements.add(a);;
			}
		}
		return announcements;
	}

	@GET
	@Path("my/tasks")
	@ApiOperation(value = "Gets the announcements of an user.")
	@ApiResponses(value = {
			@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("information.add")
	public List<Announcement> getMyTasks( @ApiParam(hidden = true) @Auth Session session) {
		List<Announcement> announcements = new ArrayList<Announcement>();
		for( Announcement a :  session.getUser().getMyAnnouncements() ) {
			if(a.getIssue().equals("task")) {
				a.setText("");
				announcements.add(a);;
			}
		}
		return announcements;
	}

	@GET
	@Path("my/contacts")
	@ApiOperation(value = "Gets the contacts of an user.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("information.add")
	public List<Contact> getMyContacts( @ApiParam(hidden = true) @Auth Session session) {
		return session.getUser().getMyContacts();
	}

	@GET
	@Path("my/faqs")
	@ApiOperation(value = "Gets the FAQs of an user.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("information.add")
	public List<FAQ> getMyFAQs( @ApiParam(hidden = true) @Auth Session session) {
		List<FAQ> faqs = new ArrayList<FAQ>();
		for( FAQ faq :  session.getUser().getMyFAQs() ) {
			faq.setText("");
			faqs.add(faq);
		}
		return faqs;
	}

	@PATCH
	@Path("announcements/{announcementId}")
	@ApiOperation(value = "Modify an announcement.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("information.add")
	public CrxResponse modifyAnnouncement(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("announcementId") Long announcementId,
		Announcement announcement
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		announcement.setId(announcementId);
		CrxResponse resp = new InformationService(session,em).modifyAnnouncement(announcement);
		em.close();
		return resp;
	}
	@PATCH
	@Path("tasks/{taskId}")
	@ApiOperation(value = "Modify an announcement.")
	@ApiResponses(value = {
			@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("information.add")
	public CrxResponse modifyTask(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("taskId") Long taskId,
			Announcement announcement
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		announcement.setId(taskId);
		CrxResponse resp = new InformationService(session,em).modifyAnnouncement(announcement);
		em.close();
		return resp;
	}

	@PATCH
	@Path("contacts/{contactId}")
	@ApiOperation(value = "Modify a contact.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("information.add")
	public CrxResponse modifyContact(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("contactId") Long contactId,
		Contact contact
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		contact.setId(contactId);
		CrxResponse resp = new InformationService(session,em).modifyContact(contact);
		em.close();
		return resp;
	}

	@PATCH
	@Path("faqs/{faqId}")
	@ApiOperation(value = "Modify a FAQ.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("information.add")
	public CrxResponse modifyFAQ(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("faqId") Long faqId,
		FAQ faq
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		faq.setId(faqId);
		CrxResponse resp = new InformationService(session,em).modifyFAQ(faq);
		em.close();
		return resp;
	}

	@PATCH
	@Path("taskResponses/{id}")
	@ApiOperation(value = "Modify a task response.")
	@ApiResponses(value = {
			@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll()
	public CrxResponse modifyTaskResponse(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("id") Long id,
			TaskResponse taskResponse
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new InformationService(session,em).modifyTaskResponse(taskResponse);
		em.close();
		return resp;
	}

	@GET
	@Path("announcements/{announcementId}")
	@ApiOperation(value = "Get an announcement by id.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	public Announcement getAnnouncement(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("announcementId") Long announcementId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		Announcement resp = new InformationService(session,em).getAnnouncementById(announcementId);
		em.close();
		return resp;
	}

	@GET
	@Path("tasks/{taskId}")
	@ApiOperation(value = "Get a task by id.")
	@ApiResponses(value = {
			@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	public Announcement getTask(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("taskId") Long taskId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		Announcement resp = new InformationService(session,em).getAnnouncementById(taskId);
		em.close();
		return resp;
	}

	@GET
	@Path("tasks/{taskId}/responses")
	@ApiOperation(value = "Get the responses for a task by task id.")
	@ApiResponses(value = {
			@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	public List<TaskResponse> getTaskResponses(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("taskId") Long taskId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		Announcement announcement = new InformationService(session,em).getAnnouncementById(taskId);
		em.close();
		return ( announcement == null ) ? null : announcement.getTaskResponses();
	}

	@POST
	@Path("taskResponses")
	@ApiOperation(value = "Rate task response. This call only modifies the rating of the task response.<br>" +
			"Only the owner of the parent task is allowed to do it")
	@ApiResponses(value = {
			@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	public CrxResponse rateTaskResponse(
			@ApiParam(hidden = true) @Auth Session session,
			TaskResponse taskResponse
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse response = new InformationService(session,em).rateTaskResponse(taskResponse);
		em.close();
		return response;
	}

	@GET
	@Path("taskResponses/{id}")
	@ApiOperation(value = "Get a task response.<br>" +
			"Only the owner of the task is allowed to do it")
	@ApiResponses(value = {
			@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	public TaskResponse getTaskResponse(
			@ApiParam(hidden = true) @Auth Session session,
			@PathParam("id") Long id
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		TaskResponse response = new InformationService(session,em).findTaskResponseById(id);
		em.close();
		return response;
	}
	@GET
	@Path("contacts/{contactId}")
	@ApiOperation(value = "Get a contact by id.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	public Contact getContact(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("contactId") Long contactId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		Contact resp = new InformationService(session,em).getContactById(contactId);
		em.close();
		return resp;
	}

	@GET
	@Path("faqs/{faqId}")
	@ApiOperation(value = "Get a FAQ by id.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	public FAQ getFAQ(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("faqId") Long faqId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		FAQ resp = new InformationService(session,em).getFAQById(faqId);
		em.close();
		return resp;
	}

	@DELETE
	@Path("announcements/{announcementId}")
	@ApiOperation(value = "Deletes an announcement.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("information.delete")
	public CrxResponse deleteAnnouncement(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("announcementId") Long announcementId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new InformationService(session,em).deleteAnnouncement(announcementId);
		em.close();
		return resp;
	}

	@DELETE
	@Path("contacts/{contactId}")
	@ApiOperation(value = "Deletes a contact.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("information.delete")
	public CrxResponse deleteContact(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("contactId") Long contactId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new InformationService(session,em).deleteContact(contactId);
		em.close();
		return resp;
	}

	@DELETE
	@Path("faqs/{faqId}")
	@ApiOperation(value = "Delets a FAQ.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@RolesAllowed("information.delete")
	public CrxResponse deleteFAQ(
		@ApiParam(hidden = true) @Auth Session session,
		@PathParam("faqId") Long faqId
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		CrxResponse resp = new InformationService(session,em).deleteFAQ(faqId);
		em.close();
		return resp;
	}

	@GET
	@Path("categories")
	@ApiOperation(value = "Gets the contacts corresponding to an user.")
	@ApiResponses(value = {
		@ApiResponse(code = 500, message = "Server broken, please contact administrator")
	})
	@PermitAll
	public List<Category> getInformationCategories(
		@ApiParam(hidden = true) @Auth Session session
	) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		List<Category> resp = new InformationService(session,em).getInfoCategories();
		em.close();
		return resp;
	}
}
