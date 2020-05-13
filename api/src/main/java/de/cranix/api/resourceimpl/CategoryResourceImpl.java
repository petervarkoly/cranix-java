/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.api.resourceimpl;

import java.util.List;

import javax.persistence.EntityManager;

import de.cranix.api.resources.CategoryResource;
import de.cranix.dao.Category;
import de.cranix.dao.CrxResponse;
import de.cranix.dao.Session;
import de.cranix.dao.controller.CategoryController;
import de.cranix.dao.internal.CommonEntityManagerFactory;

public class CategoryResourceImpl implements CategoryResource {

	public CategoryResourceImpl() {
	}

	@Override
	public List<Category> getAll(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final CategoryController categoryController = new CategoryController(session,em);
		List<Category> resp = categoryController.getAll();
		em.close();
		return resp;
	}

	@Override
	public Category getById(Session session, long categoryId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final CategoryController categoryController = new CategoryController(session,em);
		Category resp = categoryController.getById(categoryId);
		em.close();
		return resp;
	}

	@Override
	public List<Long> getMember(Session session, long categoryId, String memberType) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final CategoryController categoryController = new CategoryController(session,em);
		List<Long> resp = categoryController.getMembers(categoryId,memberType);
		em.close();
		return resp;
	}

	@Override
	public List<Long> getAvailableMember(Session session, long categoryId, String memberType) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final CategoryController categoryController = new CategoryController(session,em);
		List<Long> resp = categoryController.getAvailableMembers(categoryId,memberType);
		em.close();
		return resp;
	}

	@Override
	public List<Category> search(Session session, String search) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final CategoryController categoryController = new CategoryController(session,em);
		List<Category> resp = categoryController.search(search);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse add(Session session, Category category) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final CategoryController categoryController = new CategoryController(session,em);
		CrxResponse resp = categoryController.add(category);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse addMember(Session session, long categoryId, String memberType, long memberId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final CategoryController categoryController = new CategoryController(session,em);
		CrxResponse resp = categoryController.addMember(categoryId, memberType, memberId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse removeMember(Session session, long categoryId, String memberType, long memberId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final CategoryController categoryController = new CategoryController(session,em);
		CrxResponse resp = categoryController.deleteMember(categoryId, memberType, memberId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse modify(Session session, Category category) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final CategoryController categoryController = new CategoryController(session,em);
		CrxResponse resp = categoryController.modify(category);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse delete(Session session, long categoryId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final CategoryController categoryController = new CategoryController(session,em);
		CrxResponse resp = categoryController.delete(categoryId);
		em.close();
		return resp;
	}

	@Override
	public List<Category> getCategories(Session session, List<Long> categoryIds) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		final CategoryController categoryController = new CategoryController(session,em);
		List<Category> resp = categoryController.getCategories(categoryIds);
		em.close();
		return resp;
	}

}
