package de.cranix.services;

import de.cranix.dao.*;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

import static de.cranix.helper.CranixConstants.roleGuest;

public class GuestUserService extends Service{
    public GuestUserService(Session session, EntityManager em) {
        super(session, em);
    }

    /**
     * Get the list of created guest users categories
     *
     * @return The list of the guest users categories
     */
    public List<GuestUsers> getAll() {
        final CategoryService categoryService = new CategoryService(this.session, this.em);
        List<Category> categories = new ArrayList<Category>();
        if (categoryService.isSuperuser()) {
            categories = categoryService.getByType("guestUsers");
        } else {
            for (Category category : categoryService.getByType("guestUsers")) {
                if (category.getCreator().equals(session.getUser()) || category.isPublicAccess()) {
                    categories.add(category);
                }
            }
        }
        List<GuestUsers> guestUsers = new ArrayList<>();
        for (Category category : categories) {
            guestUsers.add(new GuestUsers(category));
        }
        return guestUsers;
    }

    /**
     * Gets a guest user category by id.
     *
     * @param guestUsersId The technical id of the category.
     * @return The list of the guest users.
     */
    public GuestUsers getById(Long guestUsersId) {
        Category category = new CategoryService(this.session, this.em).getById(guestUsersId);
        if( category != null ) {
            return new GuestUsers(category);
        }
        return null;
    }

    /**
     * Delete a guest user category. Only the guest members will be deleted.
     *
     * @param guestUsersId The technical id of the category.
     * @return The result as an CrxResponse
     */
    public CrxResponse delete(Long guestUsersId) {
        UserService userService = new UserService(this.session, this.em);
        final CategoryService categoryService = new CategoryService(this.session, this.em);
        final GroupService groupService = new GroupService(this.session, this.em);
        final RoomService roomService = new RoomService(this.session, this.em);
        Category category = categoryService.getById(guestUsersId);
        if( category == null ){
            return new CrxResponse("ERROR","Can not find category with id %s",null,guestUsersId.toString());
        }
        ArrayList<Long> toRemove = new ArrayList<>();
        for (User user : category.getUsers()) {
            if (user.getRole().equals(roleGuest)) {
                toRemove.add(user.getId());
            }
        }
        for (Long id : toRemove ) {
            userService.delete(id);
        }
        category.setUsers(new ArrayList<User>());
        toRemove = new ArrayList<>();
        for (Group group : category.getGroups()) {
            if (group.getGroupType().equals(roleGuest)) {
                toRemove.add(group.getId());
            }
        }
        for (Long id : toRemove ) {
            groupService.delete(id);
        }
        category.setGroups(new ArrayList<Group>());
        toRemove = new ArrayList<>();
        for (Room room : category.getRooms() ) {
            if( room.getName().equals(category.getName() + "-adhoc")) {
                toRemove.add(room.getId());
            }
        }
        for (Long id : toRemove ) {
            roomService.delete(id,true);
        }
        return categoryService.delete(category);
    }

    public CrxResponse add(GuestUsers guestUsers) {
        //String name, String description, Long roomId, Long count, Date validUntil)
        final CategoryService categoryService = new CategoryService(this.session, this.em);
        final GroupService groupService = new GroupService(this.session, this.em);
        final RoomService roomService = new RoomService(this.session, this.em);
        Room room = null;
        //TODO make it confiugrable
        if (guestUsers.getCount() > 255) {
            return new CrxResponse("ERROR", "A guest group must not conains more the 255 members.");
        }
        UserService userService = new UserService(this.session, this.em);
        // Check the password
        boolean checkPassword = this.getConfigValue("CHECK_PASSWORD_QUALITY").equalsIgnoreCase("yes");
        this.setConfigValue("CHECK_PASSWORD_QUALITY", "no");
        String password = guestUsers.getName() + "01";
        if (!guestUsers.getPassword().isEmpty()) {
            password = guestUsers.getPassword();
        }
        CrxResponse crxResponse = this.checkPassword(password);
        if (crxResponse != null) {
            if (checkPassword) {
                this.setConfigValue("CHECK_PASSWORD_QUALITY", "yes");
            }
            logger.error("Reset Password" + crxResponse);
            return crxResponse;
        }
        // First we check if we can create room with the requested size
        if (guestUsers.isCreateAdHocRoom()) {
            String devicesProUser = this.getConfigValue("GUEST_ADHOC_DEVICE_PRO_USER");
            if( devicesProUser.isEmpty() ) {
                devicesProUser = this.getConfigValue("CLASS_ADHOC_DEVICE_PRO_USER");
            }
            if( devicesProUser.isEmpty() ) {
                devicesProUser = "1";
            }
            int devicesProUserInt =guestUsers.getCount() * Integer.parseInt(devicesProUser);
            int roomNetMask = 32 - (int) (Math.log(devicesProUserInt) / Math.log(2) + 1.0);
            room = new Room();
            String network = this.getConfigValue("GUEST_ADHOC_NETWORK");
            if ( network.isEmpty() ) {
                network = this.getConfigValue("CLASS_ADHOC_NETWORK");
            }
            if ( !network.isEmpty() ) {
                room.setNetwork(network);
            }
            room.setIgnoreNetbios(true);
            room.setNetMask(roomNetMask);
            room.setName(guestUsers.getName() + "-adhoc");
            room.setDescription(guestUsers.getDescription());
            room.setRoomControl("allTeachers");
            room.setHwconf(roomService.getBYODHwconf());
            room.setRoomType("adHocRoom");
            crxResponse = roomService.add(room);
            if (crxResponse.getCode().equals("ERROR")) {
                return crxResponse;
            } else {
                room = roomService.getById(crxResponse.getObjectId());
            }
        }

        Category category = new Category();
        category.setCategoryType("guestUsers");
        category.setName(guestUsers.getName());
        category.setDescription(guestUsers.getDescription());
        category.setValidFrom(categoryService.now());
        category.setValidUntil(guestUsers.getValidUntil());
        crxResponse = categoryService.add(category);
        if (crxResponse.getCode().equals("ERROR")) {
            return crxResponse;
        }
        category = categoryService.getById(crxResponse.getObjectId());
        Group group = new Group();
        group.setGroupType(roleGuest);
        group.setName(guestUsers.getName());
        group.setDescription(guestUsers.getDescription());
        crxResponse = groupService.add(group);
        if (crxResponse.getCode().equals("ERROR")) {
            categoryService.delete(category.getId());
            return crxResponse;
        }

        group = groupService.getById(crxResponse.getObjectId());
        try {
            this.em.getTransaction().begin();
            category.setGroups(new ArrayList<Group>());
            category.getGroups().add(group);
            if (room != null) {
                category.setRooms(new ArrayList<Room>());
                category.getRooms().add(room);
                room.setCategories(new ArrayList<Category>());
                room.getCategories().add(category);
                this.em.merge(room);
            }
            group.setCategories(new ArrayList<Category>());
            group.getCategories().add(category);
            this.em.merge(category);
            this.em.merge(group);
            this.em.getTransaction().commit();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
        for (Long i = 1l; i < guestUsers.getCount() + 1; i++) {
            String userName = String.format("%s%02d", guestUsers.getName(), i);
            User user = new User();
            user.setUid(userName);
            user.setSurName("GuestUser");
            user.setGivenName(userName);
            user.setRole(roleGuest);
            if (!guestUsers.getPassword().isEmpty()) {
                user.setPassword(password);
            } else {
                user.setPassword("");
            }
            crxResponse = userService.add(user);
            logger.debug("Create user crxResponse:" + crxResponse);
            user = userService.getById(crxResponse.getObjectId());
            crxResponse = groupService.addMember(group, user);
            logger.debug("Create user " + crxResponse);
            categoryService.addMember(category.getId(), "user", user.getId());
        }
        crxResponse.setObjectId(category.getId());
        crxResponse.setValue("Guest Users were created succesfully");
        crxResponse.setCode("OK");
        return crxResponse;
    }


    public  Integer deleteExpiredGuestUser() {
        Query query = em.createNamedQuery("Category.expiredByType").setParameter("type", "guestUser");
        Integer counter = 0;
        for(Category category : (List<Category>) query.getResultList() ) {
            this.delete(category.getId());
            counter++;
        }
        return counter;
    }
}
