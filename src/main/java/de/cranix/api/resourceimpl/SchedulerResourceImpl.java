/* (c) 2020 Peter Varkoly <peter@varkoly.de> */
package de.cranix.api.resourceimpl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import de.cranix.api.resources.SchedulerResource;
import de.cranix.dao.Category;
import de.cranix.dao.CrxResponse;
import de.cranix.dao.Session;
import de.cranix.services.UserService;
import de.cranix.helper.CrxEntityManagerFactory;

@SuppressWarnings( "unchecked" )
public class SchedulerResourceImpl implements SchedulerResource {

	public SchedulerResourceImpl() {
		super();
	}

	@Override
	public CrxResponse deleteExpieredGuestUser(Session session) {
		EntityManager em = CrxEntityManagerFactory.instance().createEntityManager();
		UserService uc = new UserService(session,em);
		Query query = em.createNamedQuery("Category.expiredByType").setParameter("type", "guestUser");
		Integer counter = 0;
		for(Category category : (List<Category>) query.getResultList() ) {
			uc.deleteGuestUsers(category.getId());
			counter++;
		}
		em.close();
		if( counter == 0 ) {
			return new CrxResponse(session,"OK","No guest user accounts to delete.");
		}
		return new CrxResponse(session,"OK","%s guest user groups was deleted.",null,counter.toString());
	}

}
