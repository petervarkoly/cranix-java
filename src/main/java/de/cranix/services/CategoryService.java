/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved  */
package de.cranix.services;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import de.cranix.dao.*;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unchecked")
public class CategoryService extends Service {

    Logger logger = LoggerFactory.getLogger(CategoryService.class);

    public CategoryService(Session session, EntityManager em) {
        super(session, em);
    }

    public List<Category> getAll() {
        List<Category> res = new ArrayList<Category>();
        try {
            Query query = this.em.createNamedQuery("Category.findAll");
            for (Category cat : (List<Category>) query.getResultList()) {
                res.add(cat);
            }
            return res;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        } finally {
        }
    }

    public Category getById(long categoryId) {
        try {
            Category cat = this.em.find(Category.class, categoryId);
            return cat;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        } finally {
        }
    }

    public List<Category> search(String search) {
        try {
            Query query = this.em.createNamedQuery("Category.search");
            query.setParameter("search", search + "%");
            return query.getResultList();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new ArrayList<>();
        } finally {
        }
    }

    public List<Category> getByType(String search) {
        List<Category> categories = new ArrayList<Category>();
        try {
            Query query = this.em.createNamedQuery("Category.getByType").setParameter("type", search);
            for (Category c : (List<Category>) query.getResultList()) {
                categories.add(c);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
        }
        return categories;
    }

    public Category getByName(String name) {
        try {
            Query query = this.em.createNamedQuery("Category.getByName").setParameter("name", name);
            return (Category) query.getSingleResult();
        } catch (Exception e) {
            logger.debug(e.getMessage());
            return null;
        }
    }

    public CrxResponse add(Category category) {
        //Check category parameter
        StringBuilder errorMessage = new StringBuilder();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        for (ConstraintViolation<Category> violation : factory.getValidator().validate(category)) {
            errorMessage.append(violation.getMessage()).append(getNl());
        }
        if (errorMessage.length() > 0) {
            return new CrxResponse("ERROR", "Validation Error" + errorMessage.toString());
        }
        Category deepCopy = (Category) SerializationUtils.clone(category);
        logger.debug("deepCopy deepCopy:" + deepCopy);
        try {
            // First we check if the parameter are unique.
            Query query = this.em.createNamedQuery("Category.getByName").setParameter("name", category.getName());
            if (!query.getResultList().isEmpty()) {
                return new CrxResponse("ERROR", "Category name is not unique.");
            }
            if (!category.getDescription().isEmpty()) {
                query = this.em.createNamedQuery("Category.getByDescription").setParameter("description", category.getDescription());
                if (!query.getResultList().isEmpty()) {
                    return new CrxResponse("ERROR", "Category description is not unique.");
                }
            }
            category.setCreator(this.session.getUser());
            this.em.getTransaction().begin();
            this.em.persist(category);
            this.em.getTransaction().commit();
            logger.debug("Created Category:" + category);
            logger.debug("deepCopy deepCopy:" + deepCopy);
            Long categoryId = category.getId();
            CrxResponse resp1;
            if (deepCopy.getAnnouncementIds() != null) {
                for (long id : deepCopy.getAnnouncementIds()) {
                    resp1 = this.addMember(categoryId, "announcement", id);
                    logger.debug("announcement resp" + resp1);
                }
            }
            if (deepCopy.getContactIds() != null) {
                for (long id : deepCopy.getContactIds()) {
                    resp1 = this.addMember(categoryId, "contact", id);
                    logger.debug("contact resp" + resp1);
                }
            }
            if (deepCopy.getDeviceIds() != null) {
                for (long id : deepCopy.getDeviceIds()) {
                    resp1 = this.addMember(categoryId, "device", id);
                    logger.debug("device resp" + resp1);
                }
            }
            if (deepCopy.getFaqIds() != null) {
                for (long id : deepCopy.getFaqIds()) {
                    resp1 = this.addMember(categoryId, "faq", id);
                    logger.debug("faq resp" + resp1);
                }
            }
            if (deepCopy.getGroupIds() != null) {
                for (long id : deepCopy.getGroupIds()) {
                    resp1 = this.addMember(categoryId, "group", id);
                    logger.debug("group resp" + resp1);
                }
            }
            if (deepCopy.getHwconfIds() != null) {
                for (long id : deepCopy.getHwconfIds()) {
                    resp1 = this.addMember(categoryId, "hwconf", id);
                    logger.debug("hwconf resp" + resp1);
                }
            }
            if (deepCopy.getSoftwareIds() != null) {
                for (long id : deepCopy.getSoftwareIds()) {
                    resp1 = this.addMember(categoryId, "software", id);
                    logger.debug("software resp" + resp1);
                }
            }
            if (deepCopy.getRoomIds() != null) {
                for (long id : deepCopy.getRoomIds()) {
                    resp1 = this.addMember(categoryId, "room", id);
                    logger.debug("room resp" + resp1);
                }
            }
            if (deepCopy.getUserIds() != null) {
                for (long id : deepCopy.getUserIds()) {
                    resp1 = this.addMember(categoryId, "user", id);
                    logger.debug("user resp" + resp1);
                }
            }
        } catch (Exception e) {
            logger.error("Exeption: " + e.getMessage());
            return new CrxResponse("ERROR", e.getMessage());
        }
        return new CrxResponse("OK", "Category was created", category.getId());
    }

    public CrxResponse modify(Category category) {
        //Check category parameter

        logger.debug("very new:" + category);
        StringBuilder errorMessage = new StringBuilder();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        for (ConstraintViolation<Category> violation : factory.getValidator().validate(category)) {
            errorMessage.append(violation.getMessage()).append(getNl());
        }
        if (errorMessage.length() > 0) {
            return new CrxResponse("ERROR", errorMessage.toString());
        }
        Category oldCategory = this.getById(category.getId());
        CrxResponse resp1;
        Long categoryId = category.getId();

        logger.debug("old:" + oldCategory);
        logger.debug("new:" + category);
        if( category.getAnnouncementIds() == null ) {
            category.setAnnouncementIds(new ArrayList<Long>());
        }
        if( category.getContactIds() == null ) {
            category.setContactIds(new ArrayList<Long>());
        }
        if( category.getDeviceIds() == null ) {
            category.setDeviceIds(new ArrayList<Long>());
        }
        if( category.getFaqIds() == null ) {
            category.setFaqIds(new ArrayList<Long>());
        }
        if( category.getGroupIds() == null ) {
            category.setGroupIds(new ArrayList<Long>());
        }
        if( category.getHwconfIds() == null ) {
            category.setHwconfIds(new ArrayList<Long>());
        }
        if( category.getRoomIds() == null ) {
            category.setRoomIds(new ArrayList<Long>());
        }
        if( category.getSoftwareIds() == null ) {
            category.setSoftwareIds(new ArrayList<Long>());
        }
        if( category.getUserIds() == null ) {
            category.setUserIds(new ArrayList<Long>());
        }
        logger.debug("new after reset:" + category);
        //First add new objects
        for( long id : category.getAnnouncementIds() ) {
            if( ! oldCategory.getAnnouncementIds().contains(id) ) {
                resp1 = this.addMember(categoryId, "announcement", id);
                logger.debug("announcement resp" + resp1);
            }
        }
        for( long id : category.getContactIds() ) {
            if( ! oldCategory.getContactIds().contains(id) ) {
                resp1 = this.addMember(categoryId, "contact", id);
                logger.debug("contact resp" + resp1);
            }
        }
        for( long id : category.getDeviceIds() ) {
            if( ! oldCategory.getDeviceIds().contains(id) ) {
                resp1 = this.addMember(categoryId, "device", id);
                logger.debug("device resp" + resp1);
            }
        }
        for( long id : category.getFaqIds() ) {
            if( ! oldCategory.getFaqIds().contains(id) ) {
                resp1 = this.addMember(categoryId, "faq", id);
                logger.debug("faq resp" + resp1);
            }
        }
        for( long id : category.getGroupIds() ) {
            if( ! oldCategory.getGroupIds().contains(id) ) {
                resp1 = this.addMember(categoryId, "group", id);
                logger.debug("group resp" + resp1);
            }
        }
        for( long id : category.getHwconfIds() ) {
            if( ! oldCategory.getHwconfIds().contains(id) ) {
                resp1 = this.addMember(categoryId, "hwconf", id);
                logger.debug("hwconf resp" + resp1);
            }
        }
        for( long id : category.getRoomIds() ) {
            if( ! oldCategory.getRoomIds().contains(id) ) {
                resp1 = this.addMember(categoryId, "room", id);
                logger.debug("room resp" + resp1);
            }
        }
        for( long id : category.getSoftwareIds() ) {
            if( ! oldCategory.getSoftwareIds().contains(id) ) {
                resp1 = this.addMember(categoryId, "software", id);
                logger.debug("software resp" + resp1);
            }
        }
        for( long id : category.getUserIds() ) {
            if( ! oldCategory.getUserIds().contains(id) ) {
                resp1 = this.addMember(categoryId, "user", id);
                logger.debug("user resp" + resp1);
            }
        }
        //Now remove objects
        for( long id : oldCategory.getAnnouncementIds() ) {
            if( ! category.getAnnouncementIds().contains(id) ) {
                resp1 = this.deleteMember(categoryId, "announcement", id);
                logger.debug("announcement resp" + resp1);
            }
        }
        for( long id : oldCategory.getContactIds() ) {
            if( ! category.getContactIds().contains(id) ) {
                resp1 = this.deleteMember(categoryId, "contact", id);
                logger.debug("contact resp" + resp1);
            }
        }
        for( long id : oldCategory.getDeviceIds() ) {
            if( ! category.getDeviceIds().contains(id) ) {
                resp1 = this.deleteMember(categoryId, "device", id);
                logger.debug("device resp" + resp1);
            }
        }
        for( long id : oldCategory.getFaqIds() ) {
            if( ! category.getFaqIds().contains(id) ) {
                resp1 = this.deleteMember(categoryId, "faq", id);
                logger.debug("faq resp" + resp1);
            }
        }
        for( long id : oldCategory.getGroupIds() ) {
            if( ! category.getGroupIds().contains(id) ) {
                resp1 = this.deleteMember(categoryId, "group", id);
                logger.debug("group resp" + resp1);
            }
        }
        for( long id : oldCategory.getHwconfIds() ) {
            if( ! category.getHwconfIds().contains(id) ) {
                resp1 = this.deleteMember(categoryId, "hwconf", id);
                logger.debug("hwconf resp" + resp1);
            }
        }
        for( long id : oldCategory.getRoomIds() ) {
            if( ! category.getRoomIds().contains(id) ) {
                resp1 = this.deleteMember(categoryId, "room", id);
                logger.debug("room resp" + resp1);
            }
        }
        for( long id : oldCategory.getSoftwareIds() ) {
            if( ! category.getSoftwareIds().contains(id) ) {
                resp1 = this.deleteMember(categoryId, "software", id);
                logger.debug("software resp" + resp1);
            }
        }
        for( long id : oldCategory.getUserIds() ) {
            if( ! category.getUserIds().contains(id) ) {
                resp1 = this.deleteMember(categoryId, "user", id);
                logger.debug("user resp" + resp1);
            }
        }
        try {
            oldCategory = this.getById(category.getId());
            oldCategory.setDescription(category.getDescription());
            oldCategory.setName(category.getName());
            oldCategory.setStudentsOnly(category.getStudentsOnly());
            oldCategory.setPublicAccess(category.isPublicAccess());
            this.em.getTransaction().begin();
            this.em.merge(oldCategory);
            this.em.getTransaction().commit();;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new CrxResponse("ERROR", e.getMessage());
        }
        return new CrxResponse("OK", "Category was modified");
    }

    public CrxResponse delete(Long categoryId) {
        return this.delete(this.getById(categoryId));
    }

    public CrxResponse delete(Category category) {
        if (this.isProtected(category)) {
            return new CrxResponse("ERROR", "This category must not be deleted.");
        }
        // Remove group from GroupMember of table
        try {
            this.em.getTransaction().begin();
            if (!this.em.contains(category)) {
                category = this.em.merge(category);
            }
            for (Device o : category.getDevices()) {
                o.getCategories().remove(category);
                this.em.merge(o);
            }
            for (Group o : category.getGroups()) {
                o.getCategories().remove(category);
                this.em.merge(o);
            }
            for (HWConf o : category.getHwconfs()) {
                o.getCategories().remove(category);
                this.em.merge(o);
            }
            for (Room o : category.getRooms()) {
                o.getCategories().remove(category);
                this.em.merge(o);
            }
            for (Software o : category.getSoftwares()) {
                o.getCategories().remove(category);
                this.em.merge(o);
            }
            for (User o : category.getUsers()) {
                o.getCategories().remove(category);
                this.em.merge(o);
            }
            for (FAQ o : category.getFaqs()) {
                if (o.getCategories().size() == 1) {
                    this.em.remove(o);
                } else {
                    o.getCategories().remove(category);
                    this.em.merge(o);
                }
            }
            for (Contact o : category.getContacts()) {
                if (o.getCategories().size() == 1) {
                    this.em.remove(o);
                } else {
                    o.getCategories().remove(category);
                    this.em.merge(o);
                }
            }
            for (Announcement o : category.getAnnouncements()) {
                if (o.getCategories().size() == 1) {
                    this.em.remove(o);
                } else {
                    o.getCategories().remove(category);
                    this.em.merge(o);
                }
            }
            this.em.remove(category);
            this.em.getTransaction().commit();
            this.em.getEntityManagerFactory().getCache().evictAll();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new CrxResponse("ERROR", e.getMessage());
        } finally {

        }
        return new CrxResponse("OK", "Category was deleted");
    }

    public CrxResponse manageCategory(Long id, String action, Map<String, String> actionContent ){
        Category category = this.getById(id);
        if( category == null ){
            return new CrxResponse("ERROR", "Can not find the category with id: %s",id);
        }
        switch (action){
            case "delete":
                return this.delete(category);
            default:
                return new CrxResponse("ERROR", "Unknonw action.");
        }
    }

    public List<CrxResponse> applyAction(CrxActionMap actionMap) {
        List<CrxResponse> responses = new ArrayList<>();
        for (Long id : actionMap.getObjectIds()) {
            responses.add(this.manageCategory(id, actionMap.getName(), null));
        }
        return responses;
    }

    public List<Long> getAvailableMembers(Long categoryId, String objectName) {
        Category c = this.getById(categoryId);
        List<Long> objectIds = new ArrayList<Long>();
        if (c == null) {
            return objectIds;
        }
        Query query = this.em.createNamedQuery(objectName + ".findAllId");
        for (Long l : (List<Long>) query.getResultList()) {
            objectIds.add(l);
        }
        switch (objectName.toLowerCase()) {
            case ("device"):
                for (Device d : c.getDevices()) {
                    objectIds.remove(d.getId());
                }
                break;
            case ("group"):
                for (Group g : c.getGroups()) {
                    objectIds.remove(g.getId());
                }
                break;
            case ("hwconf"):
                for (HWConf h : c.getHwconfs()) {
                    objectIds.remove(h.getId());
                }
                break;
            case ("room"):
                for (Room r : c.getRooms()) {
                    objectIds.remove(r.getId());
                }
                break;
            case ("software"):
                for (Software s : c.getSoftwares()) {
                    objectIds.remove(s.getId());
                }
                break;
            case ("user"):
                for (User u : c.getUsers()) {
                    objectIds.remove(u.getId());
                }
                break;
            case ("faq"):
                for (FAQ f : c.getFaqs()) {
                    objectIds.remove(f.getId());
                }
                break;
            case ("announcement"):
                for (Announcement a : c.getAnnouncements()) {
                    objectIds.remove(a.getId());
                }
                break;
            case ("contact"):
                for (Contact cont : c.getContacts()) {
                    objectIds.remove(cont.getId());
                }
        }
        return objectIds;
    }

    public List<Long> getMembers(Long categoryId, String objectName) {
        Category c = this.getById(categoryId);
        List<Long> objectIds = new ArrayList<Long>();
        if (c == null) {
            return objectIds;
        }
        switch (objectName.toLowerCase()) {
            case ("device"):
                for (Device d : c.getDevices()) {
                    objectIds.add(d.getId());
                }
                break;
            case ("group"):
                for (Group g : c.getGroups()) {
                    objectIds.add(g.getId());
                }
                break;
            case ("hwconf"):
                for (HWConf h : c.getHwconfs()) {
                    objectIds.add(h.getId());
                }
                break;
            case ("room"):
                for (Room r : c.getRooms()) {
                    objectIds.add(r.getId());
                }
                break;
            case ("software"):
                for (Software s : c.getSoftwares()) {
                    objectIds.add(s.getId());
                }
                break;
            case ("user"):
                for (User u : c.getUsers()) {
                    objectIds.add(u.getId());
                }
                break;
            case ("faq"):
                for (FAQ f : c.getFaqs()) {
                    objectIds.add(f.getId());
                }
                break;
            case ("announcement"):
                for (Announcement a : c.getAnnouncements()) {
                    objectIds.add(a.getId());
                }
                break;
            case ("contact"):
                for (Contact cont : c.getContacts()) {
                    objectIds.add(cont.getId());
                }
        }
        return objectIds;
    }

    public CrxResponse addMember(Long categoryId, String objectName, Long objectId) {
        Category category = this.em.find(Category.class, categoryId);
        return this.addMember(category, objectName, objectId);
    }

    public CrxResponse addMember(Category category, String objectName, Long objectId) {
        boolean changes = false;
        try {
            this.em.getTransaction().begin();
            switch (objectName.toLowerCase()) {
                case ("device"):
                    Device device = this.em.find(Device.class, objectId);
                    if (!category.getDevices().contains(device)) {
                        category.getDevices().add(device);
                        category.getDeviceIds().add(device.getId());
                        device.getCategories().add(category);
                        this.em.merge(device);
                        changes = true;
                    }
                    break;
                case ("group"):
                    Group group = this.em.find(Group.class, objectId);
                    if (!category.getGroups().contains(group)) {
                        category.getGroups().add(group);
                        category.getGroupIds().add(group.getId());
                        group.getCategories().add(category);
                        this.em.merge(group);
                        changes = true;
                    }
                    break;
                case ("hwconf"):
                    HWConf hwconf = this.em.find(HWConf.class, objectId);
                    if (!category.getHwconfs().contains(hwconf)) {
                        category.getHwconfs().add(hwconf);
                        category.getHwconfIds().add(hwconf.getId());
                        hwconf.getCategories().add(category);
                        this.em.merge(hwconf);
                        changes = true;
                    }
                    break;
                case ("room"):
                    Room room = this.em.find(Room.class, objectId);
                    if (!category.getRooms().contains(room)) {
                        category.getRooms().add(room);
                        category.getRoomIds().add(room.getId());
                        room.getCategories().add(category);
                        this.em.merge(room);
                        changes = true;
                    }
                    break;
                case ("software"):
                    Software software = this.em.find(Software.class, objectId);
                    if (!category.getSoftwares().contains(software)) {
                        category.getSoftwares().add(software);
                        category.getSoftwareIds().add(software.getId());
                        software.getCategories().add(category);
                        if (category.getRemovedSoftwares().contains(software)) {
                            category.getRemovedSoftwares().remove(software);
                            software.getRemovedFromCategories().remove(category);
                        }
                        this.em.merge(software);
                        changes = true;
                    }
                    break;
                case ("user"):
                    User user = this.em.find(User.class, objectId);
                    if (!category.getUsers().contains(user)) {
                        category.getUsers().add(user);
                        category.getUserIds().add(user.getId());
                        user.getCategories().add(category);
                        this.em.merge(user);
                        changes = true;
                    }
                    break;
                case ("faq"):
                    FAQ faq = this.em.find(FAQ.class, objectId);
                    if (!category.getFaqs().contains(faq)) {
                        category.getFaqs().add(faq);
                        category.getFaqIds().add(faq.getId());
                        faq.getCategories().add(category);
                        this.em.merge(faq);
                        changes = true;
                    }
                    break;
                case ("announcement"):
                    Announcement info = this.em.find(Announcement.class, objectId);
                    if (!category.getAnnouncements().contains(info)) {
                        category.getAnnouncements().add(info);
                        category.getAnnouncementIds().add(info.getId());
                        info.getCategories().add(category);
                        this.em.merge(info);
                        changes = true;
                    }
                    break;
                case ("contact"):
                    Contact contact = this.em.find(Contact.class, objectId);
                    if (!category.getContacts().contains(contact)) {
                        category.getContacts().add(contact);
                        category.getContactIds().add(contact.getId());
                        contact.getCategories().add(category);
                        this.em.merge(contact);
                        changes = true;
                    }
                    break;
            }
            if (changes) {
                this.em.merge(category);
            }
            this.em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("addMember: " + e.getMessage());
            return new CrxResponse("ERROR", e.getMessage());
        }
        return new CrxResponse("OK", "Category was modified");
    }

    public CrxResponse deleteMember(Long categoryId, String objectName, Long objectId) {
        try {
            Category category = this.em.find(Category.class, categoryId);
            logger.debug("CategoryId:" + categoryId + " Category " + category);
            this.em.getTransaction().begin();
            switch (objectName.toLowerCase()) {
                case ("device"):
                    Device device = this.em.find(Device.class, objectId);
                    if (category.getDevices().contains(device)) {
                        category.getDevices().remove(device);
                        device.getCategories().remove(category);
                        this.em.merge(device);
                    }
                    break;
                case ("group"):
                    Group group = this.em.find(Group.class, objectId);
                    if (category.getGroups().contains(group)) {
                        category.getGroups().remove(group);
                        group.getCategories().remove(category);
                        this.em.merge(group);
                    }
                    break;
                case ("hwconf"):
                    HWConf hwconf = this.em.find(HWConf.class, objectId);
                    if (category.getHwconfs().contains(hwconf)) {
                        category.getHwconfs().remove(hwconf);
                        hwconf.getCategories().remove(category);
                        this.em.merge(hwconf);
                    }
                    break;
                case ("room"):
                    Room room = this.em.find(Room.class, objectId);
                    if (category.getRooms().contains(room)) {
                        category.getRooms().remove(room);
                        room.getCategories().remove(category);
                        this.em.merge(room);
                    }
                    break;
                case ("software"):
                    Software software = this.em.find(Software.class, objectId);
                    logger.debug("Software:" + software);
                    if (category.getSoftwares().contains(software)) {
                        category.getSoftwares().remove(software);
                        category.getRemovedSoftwares().add(software);
                        software.getCategories().remove(category);
                        software.getRemovedFromCategories().add(category);
                        this.em.merge(software);
                    }
                    break;
                case ("user"):
                    User user = this.em.find(User.class, objectId);
                    if (category.getUsers().contains(user)) {
                        category.getUsers().remove(user);
                        user.getCategories().remove(category);
                        this.em.merge(user);
                    }
                    break;
                case ("faq"):
                    FAQ faq = this.em.find(FAQ.class, objectId);
                    if (category.getFaqs().contains(faq)) {
                        category.getFaqs().remove(faq);
                        faq.getCategories().remove(category);
                        this.em.merge(faq);
                    }
                    break;
                case ("announcement"):
                    Announcement info = this.em.find(Announcement.class, objectId);
                    if (category.getAnnouncements().contains(info)) {
                        category.getAnnouncements().remove(info);
                        info.getCategories().remove(category);
                        this.em.merge(info);
                    }
                    break;
                case ("contact"):
                    Contact contact = this.em.find(Contact.class, objectId);
                    if (category.getContacts().contains(contact)) {
                        category.getContacts().remove(contact);
                        contact.getCategories().remove(category);
                        this.em.merge(contact);
                    }
                    break;
            }
            this.em.merge(category);
            this.em.getTransaction().commit();
        } catch (Exception e) {
            logger.error("deleteMember:" + e.getMessage());
            return new CrxResponse("ERROR", e.getMessage());
        } finally {
        }
        return new CrxResponse("OK", "Category was modified");
    }

    public List<Category> getCategories(List<Long> categoryIds) {
        List<Category> categories = new ArrayList<Category>();
        for (Long id : categoryIds) {
            categories.add(this.getById(id));
        }
        return categories;
    }
}
