/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.api.resourceimpl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import de.cranix.api.resources.GroupResource;
import de.cranix.dao.Group;
import de.cranix.dao.CrxActionMap;
import de.cranix.dao.User;
import de.cranix.dao.controller.GroupController;
import de.cranix.dao.internal.CommonEntityManagerFactory;
import de.cranix.dao.Session;
import de.cranix.dao.CrxResponse;

public class GroupResourceImpl implements GroupResource {

		public GroupResourceImpl() {
	}

	@Override
	public Group getById(Session session, Long groupId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		Group group =  new GroupController(session,em).getById(groupId);
		em.close();
		return group;
	}

	@Override
	public List<User> getAvailableMembers(Session session, Long groupId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<User> users = new GroupController(session,em).getAvailableMember(groupId);
		em.close();
		return users;
	}

	@Override
	public List<User> getMembers(Session session, Long groupId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<User> resp = new GroupController(session,em).getMembers(groupId);
		em.close();
		return resp;
	}

	@Override
	public List<Group> getByType(Session session, String type) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<Group> resp = new GroupController(session,em).getByType(type);
		em.close();
		return resp;
	}

	@Override
	public List<Group> getAll(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<Group> resp = new GroupController(session,em).getAll();
		em.close();
		return resp;
	}

	@Override
	public List<Group> search(Session session, String search) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<Group> resp = new GroupController(session,em).search(search);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse add(Session session, Group group) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new GroupController(session,em).add(group);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse modify(Session session, Group group) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new GroupController(session,em).modify(group);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse modify(Session session,Long groupId, Group group) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		group.setId(groupId);
		CrxResponse resp = new GroupController(session,em).modify(group);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse delete(Session session, Long groupId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new GroupController(session,em).delete(groupId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse setMembers(Session session, Long groupId, List<Long> users) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new GroupController(session,em).setMembers(groupId,users);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse removeMember(Session session, Long groupId, Long userId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new GroupController(session,em).removeMember(groupId,userId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse addMember(Session session, Long groupId, Long userId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new GroupController(session,em).addMember(groupId,userId);
		em.close();
		return resp;
	}

	@Override
	public List<Group> getGroups(Session session, List<Long> groupIds) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<Group> resp = new GroupController(session,em).getGroups(groupIds);
		em.close();
		return resp;
	}

	@Override
	public String getMembersText(Session session, String groupName) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<String> member = new ArrayList<String>();
		final GroupController gc = new GroupController(session,em);
		Group group = gc.getByName(groupName);
		for(User user : group.getUsers() ) {
			member.add(user.getUid());
		}
		String resp = String.join(gc.getNl(),member);
		em.close();
		return resp;
	}

	@Override
	public String getByTypeText(Session session, String type) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<String> groups = new ArrayList<String>();
		final GroupController gc = new GroupController(session,em);
		for( Group group : gc.getByType(type)) {
			groups.add(group.getName());
		}
		String resp = String.join(gc.getNl(),groups);
		em.close();
		return resp;
	}

	@Override
	public String delete(Session session, String groupName) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		String resp = new GroupController(session,em).delete(groupName).getCode();
		em.close();
		return resp;
	}

	@Override
	public CrxResponse cleanUpDirectory(Session session, Long groupId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		GroupController gc = new GroupController(session,em);
		Group group = gc.getById(groupId);
		CrxResponse resp = gc.cleanGrupDirectory(group);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse importGroups(Session session, InputStream fileInputStream,
			FormDataContentDisposition contentDispositionHeader) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new GroupController(session,em).importGroups(fileInputStream, contentDispositionHeader);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse applyAction(Session session, CrxActionMap actionMap) {
		// TODO Auto-generated method stub
		return null;
	}
}
