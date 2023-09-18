/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved  */
package de.cranix.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.cranix.dao.*;
import de.cranix.helper.CrxSystemCmd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.OrderBy;
import javax.persistence.Query;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.cranix.helper.CranixConstants.*;
import static de.cranix.helper.StaticHelpers.*;

@SuppressWarnings("unchecked")
public class UserService extends Service {

    Logger logger = LoggerFactory.getLogger(UserService.class);


    public UserService(Session session, EntityManager em) {
        super(session, em);
    }

    public User getById(long userId) {
        try {
            User user = this.em.find(User.class, userId);
            if (user != null) {
                for (Alias alias : user.getAliases()) {
                    user.getMailAliases().add(alias.getAlias());
                }
            }
            return user;
        } catch (Exception e) {
            logger.debug("getByID: " + e.getMessage());
            return null;
        }
    }

    public String getHomeDir(User user) {
        StringBuilder homeDir = new StringBuilder(this.getConfigValue("HOME_BASE"));
        if (this.getConfigValue("SORT_HOMES").equals("no")) {
            homeDir.append(user.getUid().toLowerCase()).append("/");
        } else {
            homeDir.append("/").append(user.getRole()).append("/").append(user.getUid().toLowerCase()).append("/");
        }
        return homeDir.toString();
    }

    @OrderBy("uid,surName")
    public List<User> getByRole(String role) {
        List<User> users = new ArrayList<User>();
        try {
            Query query = this.em.createNamedQuery("User.getByRole");
            query.setParameter("role", role);
            users = query.getResultList();
        } catch (Exception e) {
            logger.error("getByRole: " + e.getMessage());
        }
	//users.sort(Comparator.comparing(User::getUid));
        return users;
    }

    public User getByUid(String uid) {
        try {
            Query query = this.em.createNamedQuery("User.getByUid");
            query.setParameter("uid", uid);
            List<User> result = query.getResultList();
            if (result != null && result.size() > 0) {
                return result.get(0);
            } else {
                logger.debug("getByUid: uid not found. uid=" + uid);
            }
        } catch (Exception e) {
            logger.error("getByUid: uid=" + uid + " " + e.getMessage());
            return null;
        }
        return null;
    }

    public List<User> search(String search) {
        try {
            Query query = this.em.createNamedQuery("User.search");
            query.setParameter("search", search + "%");
            return query.getResultList();
        } catch (Exception e) {
            logger.error("search: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @OrderBy("uid,surName")
    public List<User> findByName(String givenName, String surName) {
        try {
            Query query = this.em.createNamedQuery("User.findByName");
            query.setParameter("givenName", givenName);
            query.setParameter("surName", surName);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("findByName: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @OrderBy("uid,surName")
    public List<User> findByNameAndRole(String givenName, String surName, String role) {
        try {
            Query query = this.em.createNamedQuery("User.findByNameAndRole");
            query.setParameter("givenName", givenName);
            query.setParameter("surName", surName);
            query.setParameter("role", role);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("findByNameAndRole: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @OrderBy("uid,surName")
    public List<User> getAll() {
        List<User> users = new ArrayList<User>();
        boolean userManage = this.isAllowed("user.manage");
        try {
            Query query = this.em.createNamedQuery("User.findAll");
            for (User user : (List<User>) query.getResultList()) {
                if (userManage || user.getRole().equals(roleStudent)
                        || (user.getRole().equals(roleGuest) && user.getCreator().equals(session.getUser()))) {
                    users.add(user);
                }
            }
        } catch (Exception e) {
            logger.error("getAll: " + e.getMessage());
        }
        //users.sort(Comparator.comparing(User::getUid));
        return users;
    }

    public void createUid(User user) {
        user.setUid(this.createUid(user.getGivenName(), user.getSurName(), user.getBirthDay()));
    }

    @SuppressWarnings("deprecation")
    public String createUid(String givenName, String surName, String birthDay) {
        String userId = "";
        Pattern pattern = Pattern.compile("([GNY])(\\d+)");
        for (Matcher m = pattern.matcher(this.getConfigValue("LOGIN_SCHEME")); m.find(); ) {
            int endIndex = Integer.parseInt(m.group(2));
            //logger.debug("have found" + m.group(1) + " " + m.group(2) + " " + endIndex + " " + givenName );
            switch (m.group(1)) {
                case "V":
                case "G":
                    endIndex = Math.min(endIndex, givenName.length());
                    userId = userId.concat(givenName.substring(0, endIndex));
                    break;
                case "N":
                case "S":
                    endIndex = Math.min(endIndex, surName.length());
                    userId = userId.concat(surName.substring(0, endIndex));
                    break;
                case "Y":
                    String bds = birthDay.substring(0, 4);
                    switch (endIndex) {
                        case 2:
                            userId = userId.concat(bds.substring(2, 4));
                            break;
                        case 4:
                            userId = userId.concat(bds);
                    }
                    break;
            }
        }
        String newUserId = this.getConfigValue("LOGIN_PREFIX") + userId;
        int i = 1;
        while (!this.isNameUnique(newUserId)) {
            newUserId = this.getConfigValue("LOGIN_PREFIX") + userId + i;
            i++;
        }
        if(this.getConfigValue("LOGIN_TELEX").equals("yes")) {
            return normalizeTelex(newUserId.toLowerCase()).replaceAll("[^a-zA-Z0-9]", "");
        }
        return normalize(newUserId.toLowerCase()).replaceAll("[^a-zA-Z0-9]", "");
    }

    public CrxResponse add(User user) {
        logger.debug("User to create:" + user);
        // Check role
        if (user.getRole() == null) {
            return new CrxResponse(
                    this.getSession(),
                    "ERROR",
                    "You have to define the role of the user.");
        }
        if (!this.mayAdd(user)) {
            return new CrxResponse(
                    this.getSession(),
                    "ERROR",
                    "You must not create user whith role %s.",
                    null,
                    user.getRole());
        }
        // Check Birthday
        if (user.getBirthDay() == null || user.getBirthDay() == "") {
            if (!user.getRole().equals(roleStudent)) {
                user.setBirthDay(this.nowDateString());
            } else {
                return new CrxResponse(this.getSession(), "ERROR", "You have to define the birthday.");
            }
        }
        // Create uid if not given
        if (user.getUid() == null || user.getUid().isEmpty()) {
            this.createUid(user);
        } else {
            user.setUid(user.getUid().toLowerCase());
            // First we check if the parameter are unique.
            // workstation users have a user called as itself
            if (!user.getRole().equals("workstations") && !this.isNameUnique(user.getUid())) {
                return new CrxResponse(this.getSession(), "ERROR", "User name is not unique.");
            }
            // Check if uid contains non allowed characters
            if (this.checkNonASCII(user.getUid())) {
                return new CrxResponse(this.getSession(), "ERROR", "Uid contains not allowed characters.");
            }
        }
        // Check the user password
        if (user.getRole().equals("workstations") || (user.getRole().equals(roleGuest) && user.getPassword().isEmpty())) {
            user.setPassword(user.getUid());
        } else if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword(createRandomPassword());
        } else {
            CrxResponse crxResponse = this.checkPassword(user.getPassword());
            if (crxResponse != null) {
                return crxResponse;
            }
        }
        if (user.getFsQuota() == null) {
            user.setFsQuota(Integer.getInteger(this.getConfigValue("FILE_QUOTA")));
        }
        if (user.getMsQuota() == null) {
            user.setMsQuota(Integer.getInteger(this.getConfigValue("MAIL_QUOTA")));
        }
        // Make backup from password. password field is transient!
        user.setInitialPassword(user.getPassword());
        user.setCreator(this.session.getUser());
        // Check user parameter
        StringBuilder errorMessage = new StringBuilder();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        for (ConstraintViolation<User> violation : factory.getValidator().validate(user)) {
            errorMessage.append(violation.getMessage()).append(getNl());
        }
        if (errorMessage.length() > 0) {
            return new CrxResponse(this.getSession(), "ERROR", errorMessage.toString());
        }
        if (user.getMailAliases() != null) {
            for (String alias : user.getMailAliases()) {
                String tmp = alias.trim();
                if (!tmp.isEmpty()) {
                    if (isUserAliasUnique(tmp)) {
                        user.addAlias(new Alias(user, tmp));
                    }
                }
            }
        }
        try {
            Group group = new GroupService(this.session, this.em).getByName(user.getRole());
            this.em.getTransaction().begin();
            this.em.persist(user);
            group.addUser(user);
            this.em.getTransaction().commit();
            logger.debug("Created user" + user);
        } catch (Exception e) {
            logger.error("add: " + e.getMessage());
            return new CrxResponse(this.getSession(), "ERROR", e.getMessage());
        }
        startPlugin("add_user", user);
        List<String> parameters = new ArrayList<String>();
        parameters.add(user.getUid());
        parameters.add(user.getGivenName());
        parameters.add(user.getSurName());
        parameters.add(user.getPassword());
        return new CrxResponse(this.getSession(), "OK", "%s ( %s %s ) was created with password '%s'", user.getId(),
                parameters);
    }

    public List<CrxResponse> add(List<User> users) {
        List<CrxResponse> results = new ArrayList<CrxResponse>();
        for (User user : users) {
            results.add(this.add(user));
        }
        return results;
    }

    public CrxResponse modify(User user) {
        User oldUser = this.getById(user.getId());
        if (!this.mayModify(oldUser)) {
            return new CrxResponse(this.getSession(), "ERROR", "You must not modify this user.");
        }
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            CrxResponse crxResponse = this.checkPassword(user.getPassword());
            if (crxResponse != null) {
                return crxResponse;
            }
        }
        logger.debug("modifyUser user:" + user);
        oldUser.setUuid(user.getUuid());
        oldUser.setGivenName(user.getGivenName());
        oldUser.setSurName(user.getSurName());
        oldUser.setBirthDay(user.getBirthDay());
        oldUser.setPassword(user.getPassword());
        oldUser.setFsQuota(user.getFsQuota());
        oldUser.setMsQuota(user.getMsQuota());
        List<Alias> newAliases = new ArrayList<Alias>();
        if (user.getMailAliases() != null) {
            ArrayList<String> oldAliases = new ArrayList<String>();
            for (Alias alias : oldUser.getAliases()) {
                oldAliases.add(alias.getAlias());
            }
            for (String alias : user.getMailAliases()) {
                String tmp = alias.trim();
                if (!tmp.isEmpty()) {
                    if (!oldAliases.contains(tmp) && !this.isUserAliasUnique(tmp)) {
                        return new CrxResponse(this.getSession(), "ERROR", "Alias '%s' is not unique", null, tmp);
                    }
                    newAliases.add(new Alias(oldUser, tmp));
                }
            }
        }
        // Check user parameter
        StringBuilder errorMessage = new StringBuilder();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        for (ConstraintViolation<User> violation : factory.getValidator().validate(oldUser)) {
            errorMessage.append(violation.getMessage()).append(getNl());
        }
        if (errorMessage.length() > 0) {
            return new CrxResponse(this.getSession(), "ERROR", errorMessage.toString());
        }
        try {
            this.em.getTransaction().begin();
            for (Alias alias : oldUser.getAliases()) {
                this.em.remove(alias);
            }
            oldUser.setAliases(null);
            this.em.merge(oldUser);
            this.em.getTransaction().commit();
            this.em.getTransaction().begin();
            oldUser.setAliases(newAliases);
            this.em.merge(oldUser);
            this.em.getTransaction().commit();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new CrxResponse(this.getSession(), "ERROR", e.getMessage());
        }
        startPlugin("modify_user", oldUser);
        return new CrxResponse(this.getSession(), "OK", "User was modified succesfully");
    }

    public List<CrxResponse> deleteStudents(List<Long> userIds) {
        List<CrxResponse> responses = new ArrayList<CrxResponse>();
        for (Long userId : userIds) {
            User user = this.getById(userId);
            if (user != null && user.getRole().equals(roleStudent)) {
                responses.add(this.delete(user));
            }
        }
        return responses;
    }

    public CrxResponse delete(long userId) {
        return this.delete(this.getById(userId));
    }

    public CrxResponse delete(String uid) {
        return this.delete(this.getByUid(uid));
    }

    public CrxResponse delete(User user) {
        if (user == null) {
            return new CrxResponse(this.getSession(), "ERROR", "Can not find the user.");
        }
        if (this.isProtected(user)) {
            return new CrxResponse(this.getSession(), "ERROR", "This user must not be deleted.");
        }
        if (!this.mayDelete(user)) {
            return new CrxResponse(this.getSession(), "ERROR", "You must not delete this user.");
        }
        startPlugin("delete_user", user);
        //TODO make it configurable
        //Remove the devices before doing anything else
        if (!user.getOwnedDevices().isEmpty()) {
            DeviceService dc = new DeviceService(this.session, this.em);
            List<Long> dIds = new ArrayList<>();
            for (Device device : user.getOwnedDevices()) {
                dIds.add(device.getId());
            }
            for(Long id: dIds) {
                dc.delete(id, false);
            }
            DHCPConfig dhcpConfig = new DHCPConfig(session, this.em);
            dhcpConfig.Create();
        }
        User admin = getById(1L);
        this.em.getTransaction().begin();
        if (user.getRole().equals(roleStudent) || user.getRole().equals(roleWorkstation)) {
            this.deleteCreatedObjects(user);
        } else {
            this.inheritCreatedObjects(user, admin);
        }
        if (!em.contains(user)) {
            user = this.em.merge(user);
        }
        for (Group group : user.getGroups()) {
            group.getUsers().remove(user);
            this.em.merge(group);
        }
        this.em.remove(user);
        this.em.getTransaction().commit();
        return new CrxResponse(this.getSession(), "OK", "User was deleted");
    }

    public List<Group> getAvailableGroups(long userId) {

        User user = this.getById(userId);
        Query query = this.em.createNamedQuery("Group.findAll");
        List<Group> allGroups = query.getResultList();
        allGroups.removeAll(user.getGroups());
        return allGroups;
    }

    public List<Group> getGroups(long userId) {
        User user = this.getById(userId);
        return user.getGroups();
    }

    public CrxResponse setGroups(long userId, List<Long> groupIds) {
        List<Group> groupsToRemove = new ArrayList<Group>();
        List<Group> groupsToAdd = new ArrayList<Group>();
        List<Group> groups = new ArrayList<Group>();
        for (Long groupId : groupIds) {
            groups.add(em.find(Group.class, groupId));
        }
        User user = this.getById(userId);
        if (!this.mayModify(user)) {
            logger.error("setGroups: Session user may not modify: %s", null, userId);
            return new CrxResponse(this.getSession(), "ERROR", "You must not modify this user.");
        }
        for (Group group : groups) {
            if (!user.getGroups().contains(group)) {
                groupsToAdd.add(group);
            }
        }
        for (Group group : user.getGroups()) {
            if (user.getRole().equals(group.getName())) {
                // User must not be removed from it's primary group.
                continue;
            }
            if (!groups.contains(group)) {
                groupsToRemove.add(group);
            }
        }
        try {
            this.em.getTransaction().begin();
            for (Group group : groupsToAdd) {
                group.getUsers().add(user);
                user.getGroups().add(group);
                this.em.merge(group);
            }
            for (Group group : groupsToRemove) {
                group.getUsers().remove(user);
                user.getGroups().remove(group);
                this.em.merge(group);
            }
            this.em.merge(user);
            this.em.getTransaction().commit();
        } catch (Exception e) {
            return new CrxResponse(this.getSession(), "ERROR", e.getMessage());
        }
        for (Group group : groupsToAdd) {
            changeMemberPlugin("addmembers", group, user);
        }
        for (Group group : groupsToRemove) {
            changeMemberPlugin("removemembers", group, user);
        }
        return new CrxResponse(this.getSession(), "OK", "The groups of the user was set.");
    }

    public CrxResponse syncFsQuotas(List<List<String>> quotas) {
        User user;
        try {
            for (List<String> quota : quotas) {
                if (quota.isEmpty())
                    continue;
                user = this.getByUid(quota.get(0));
                if (user != null) {
                    user.setFsQuotaUsed(Integer.valueOf(quota.get(1)));
                    user.setFsQuota(Integer.valueOf(quota.get(2)));
                    this.em.getTransaction().begin();
                    this.em.merge(user);
                    this.em.getTransaction().commit();
                }
            }
        } catch (Exception e) {
            return new CrxResponse(this.getSession(), "ERROR", e.getMessage());
        }
        return new CrxResponse(this.getSession(), "OK", "The filesystem quotas was synced succesfully");
    }

    public CrxResponse syncMsQuotas(List<List<String>> quotas) {
        User user;
        try {
            for (List<String> quota : quotas) {
                if (quota.isEmpty())
                    continue;
                user = this.getByUid(quota.get(0));
                if (user != null) {
                    user.setMsQuotaUsed(Integer.valueOf(quota.get(1)));
                    user.setMsQuota(Integer.valueOf(quota.get(2)));
                    this.em.getTransaction().begin();
                    this.em.merge(user);
                    this.em.getTransaction().commit();
                }
            }
        } catch (Exception e) {
            return new CrxResponse(this.getSession(), "ERROR", e.getMessage());
        }
        return new CrxResponse(this.getSession(), "OK", "The mailsystem quotas was synced succesfully");
    }

    public String getGroupsOfUser(String userName, String groupType) {
        UserService userService = new UserService(this.session, this.em);
        User user = userService.getByUid(userName);
        if (user == null) {
            return "";
        }
        List<String> groups = new ArrayList<String>();
        for (Group group : user.getGroups()) {
            if (group.getGroupType().equals(groupType)) {
                groups.add(group.getName());
            }
        }
        return String.join(this.getNl(), groups);
    }
    public List<CrxResponse> resetUserPassword(List<Long> userIds, String password, boolean mustChange) {
        logger.debug("resetUserPassword: " + password);
        List<CrxResponse> responses = new ArrayList<CrxResponse>();
        boolean checkPassword = this.getConfigValue("CHECK_PASSWORD_QUALITY").equalsIgnoreCase("yes");
        this.setConfigValue("CHECK_PASSWORD_QUALITY", "no");
        CrxResponse passwordResponse = this.checkPassword(password);
        if (passwordResponse != null) {
            if (checkPassword) {
                this.setConfigValue("CHECK_PASSWORD_QUALITY", "yes");
            }
            logger.error("Reset Password" + passwordResponse);
            responses.add(passwordResponse);
            return responses;
        }
        for (Long id : userIds) {
            User user = this.getById(id);
            if (user == null) {
                logger.error("resetUserPassword: Can not find user with id: %s", null, id);
                continue;
            }
            if (!this.mayModify(user)) {
                logger.error("resetUserPassword: Session user may not modify: %s", null, id);
                continue;
            }
            /* We allow it. We will make it confiugrable
             *if( user.getRole().equals(roleWorkstation) ) {
             *	      logger.error("resetUserPassword: Must not change workstation users password.");
             *	      continue;
             *      }
             */
            user.setPassword(password);
            user.setMustChange(mustChange);
            startPlugin("modify_user", user);
            responses.add(new CrxResponse(this.getSession(), "OK", "The password of '%s' was reseted.", null, user.getUid()));
        }
        if (checkPassword) {
            this.setConfigValue("CHECK_PASSWORD_QUALITY", "yes");
        }
        return responses;
    }

    public List<CrxResponse> copyTemplate(List<Long> userIds, String stringValue) {
        List<CrxResponse> responses = new ArrayList<CrxResponse>();
        StringBuffer reply = new StringBuffer();
        StringBuffer error = new StringBuffer();
        String[] program = new String[2];
        if (stringValue != null) {
            program = new String[3];
            program[2] = stringValue;
        }
        program[0] = "/usr/sbin/crx_copy_template_home.sh";
        for (Long id : userIds) {
            User user = this.getById(id);
            if (user != null) {
                if (!this.mayModify(user)) {
                    logger.error("copyTemplate: Session user may not modify: %s", null, id);
                    continue;
                }
                program[1] = user.getUid();
                CrxSystemCmd.exec(program, reply, error, null);
                responses.add(new CrxResponse(this.getSession(), "OK", "The template for '%s' was copied.", null, user.getUid()));
            }
        }
        return responses;
    }

    public List<CrxResponse> removeProfile(List<Long> userIds) {
        List<CrxResponse> responses = new ArrayList<CrxResponse>();
        StringBuffer reply = new StringBuffer();
        StringBuffer error = new StringBuffer();
        String[] program = new String[2];
        program[0] = "/usr/sbin/crx_remove_profile.sh";
        for (Long id : userIds) {
            User user = this.getById(id);
            if (user != null) {
                if (!this.mayModify(user)) {
                    logger.error("removeProfile: Session user may not modify: %s", null, id);
                    continue;
                }
                program[1] = user.getUid();
                CrxSystemCmd.exec(program, reply, error, null);
                responses.add(new CrxResponse(this.getSession(), "OK", "The windows profile of '%s' was removed.", null, user.getUid()));
            }
        }
        return responses;
    }

    public List<CrxResponse> mandatoryProfile(List<Long> userIds, boolean booleanValue) {
        List<CrxResponse> responses = new ArrayList<CrxResponse>();
        StringBuffer reply = new StringBuffer();
        StringBuffer error = new StringBuffer();
        String[] program = new String[2];
        if (booleanValue) {
            program[0] = cranixBaseDir + "tools/set_profil_ro.sh";
        } else {
            program[0] = cranixBaseDir + "tools/set_profil_rw.sh";
        }
        for (Long id : userIds) {
            User user = this.getById(id);
            if (user != null) {
                if (!this.mayModify(user)) {
                    logger.error("removeProfile: Session user may not modify: %s", null, id);
                    continue;
                }
                program[1] = user.getUid();
                CrxSystemCmd.exec(program, reply, error, null);
                responses.add(new CrxResponse(this.getSession(), "OK", "The windows profile of '%s' was froozen.", null, user.getUid()));
            }
        }
        return responses;
    }

    public List<CrxResponse> disableLogin(List<Long> userIds, boolean disable) {
        List<CrxResponse> responses = new ArrayList<CrxResponse>();
        StringBuffer reply = new StringBuffer();
        StringBuffer error = new StringBuffer();
        String[] program = new String[4];
        program[0] = "/usr/bin/samba-tool";
        program[1] = "user";
        if (disable) {
            program[2] = "disable";
        } else {
            program[2] = "enable";
        }
        for (Long id : userIds) {
            User user = this.getById(id);
            if (user != null) {
                if (!this.mayModify(user)) {
                    logger.error("disableLogin: Session user may not modify: %s", null, id);
                    continue;
                }
                program[3] = user.getUid();
                CrxSystemCmd.exec(program, reply, error, null);
                if (disable) {
                    responses.add(new CrxResponse(this.getSession(), "OK", "'%s' was disabled.", null, user.getUid()));
                } else {
                    responses.add(new CrxResponse(this.getSession(), "OK", "'%s' was enabled.", null, user.getUid()));
                }
            }
        }
        return responses;
    }

    public List<CrxResponse> setFsQuota(List<Long> userIds, Long fsQuota) {
        List<CrxResponse> responses = new ArrayList<CrxResponse>();
        StringBuffer reply = new StringBuffer();
        StringBuffer error = new StringBuffer();
        String[] program = new String[3];
        program[0] = "/usr/sbin/crx_set_quota.sh";
        program[2] = String.valueOf(fsQuota);
        Integer quota = Integer.valueOf(program[2]);
        for (Long id : userIds) {
            User user = this.getById(id);
            if (user != null) {
                if (!this.mayModify(user)) {
                    logger.error("setFsQuota: Session user may not modify: %s", null, id);
                    continue;
                }
                program[1] = user.getUid();
                user.setFsQuota(quota);
                this.em.getTransaction().begin();
                this.em.merge(user);
                this.em.getTransaction().commit();
                CrxSystemCmd.exec(program, reply, error, null);
                responses.add(new CrxResponse(this.getSession(), "OK", "The file system quota for '%s' was set.", null, user.getUid()));
            }
        }
        return responses;
    }

    public List<CrxResponse> setMsQuota(List<Long> userIds, Long msQuota) {
        List<CrxResponse> responses = new ArrayList<CrxResponse>();
        StringBuffer reply = new StringBuffer();
        StringBuffer error = new StringBuffer();
        String[] program = new String[3];
        program[0] = "/usr/sbin/crx_set_mquota.pl";
        program[2] = String.valueOf(msQuota * 1024);
        Integer quota = Integer.valueOf(program[2]);
        for (Long id : userIds) {
            User user = this.getById(id);
            if (user != null) {
                if (!this.mayModify(user)) {
                    logger.error("setMsQuota: Session user may not modify: %s", id);
                    continue;
                }
                program[1] = user.getUid();
                user.setMsQuota(quota);
                this.em.getTransaction().begin();
                this.em.merge(user);
                this.em.getTransaction().commit();
                CrxSystemCmd.exec(program, reply, error, null);
                responses.add(new CrxResponse(this.getSession(), "OK", "The mail system quota for '%s' was set.", null, user.getUid()));
            }
        }
        return responses;
    }

    public CrxResponse collectFileFromUser(User user, String project, boolean sortInDirs, boolean cleanUpExport) {
        String[] program = new String[11];
        StringBuffer reply = new StringBuffer();
        StringBuffer stderr = new StringBuffer();
        program[0] = "/usr/sbin/crx_collect_files.sh";
        program[1] = "-t";
        program[2] = this.session.getUser().getUid();
        program[3] = "-f";
        program[4] = user.getUid();
        program[5] = "-p";
        program[6] = project;
        program[7] = "-c";
        program[9] = "-d";
        if (cleanUpExport) {
            program[8] = "y";
        } else {
            program[8] = "n";
        }
        if (sortInDirs) {
            program[10] = "y";
        } else {
            program[10] = "n";
        }
        CrxSystemCmd.exec(program, reply, stderr, null);
        if (stderr.toString().isEmpty()) {
            if (logger.isDebugEnabled()) {
                StringBuilder st = new StringBuilder();
                for (int faktor = 0; faktor < program.length; faktor++) {
                    st.append(program[faktor]).append(" ");
                }
                logger.debug("Collect Program:" + st.toString());
                logger.debug("Collected project " + project + " from " + user.getUid());
            }
            return new CrxResponse(this.getSession(), "OK", "File was collected from: %s", null, user.getUid());
        }
        logger.error("Can not collect project " + project + " from " + user.getUid() + stderr.toString());

        return new CrxResponse(this.getSession(), "ERROR", stderr.toString());
    }

    public List<CrxResponse> disableInternet(List<Long> userIds, boolean disable) {
        List<CrxResponse> responses = new ArrayList<CrxResponse>();
        for (Long userId : userIds) {
            User user = this.getById(userId);
            if (user != null) {
                if (!this.mayModify(user)) {
                    logger.error("disableInternet: Session user may not modify: %s", null, userId);
                    continue;
                }
                if (disable) {
                    this.setConfig(this.getById(userId), "internetDisabled", "yes");
                    responses.add(new CrxResponse(this.getSession(), "OK", "Surfing is for '%s' disabled.", null, user.getUid()));
                } else {
                    responses.add(new CrxResponse(this.getSession(), "OK", "Surfing is for '%s' enabled.", null, user.getUid()));
                    this.deleteConfig(this.getById(userId), "internetDisabled");
                }
            }
        }
        return responses;
    }

    public void inheritCreatedObjects(User creator, User newCreator) {
        try {
            //Acls
            for (Acl o : creator.getCreatedAcls()) {
                o.setCreator(newCreator);
                this.em.merge(o);
            }
            creator.setCreatedAcls(null);
            //AccessInRoom
            for (AccessInRoom o : creator.getCreatedAccessInRoom()) {
                o.setCreator(newCreator);
                this.em.merge(o);
            }
            creator.setCreatedAccessInRoom(null);
            //Announcement
            for (Announcement o : creator.getMyAnnouncements()) {
                o.setOwner(newCreator);
                this.em.merge(o);
            }
            creator.setMyAnnouncements(null);
            //Categories
            for (Category o : creator.getOwnedCategories()) {
                o.setOwner(newCreator);
                this.em.merge(o);
            }
            creator.setOwnedCategories(null);
            //Contacts
            for (Contact o : creator.getMyContacts()) {
                o.setOwner(newCreator);
                this.em.merge(o);
            }
            creator.setMyContacts(null);
            //Groups
            for (Group o : creator.getOwnedGroups()) {
                o.setOwner(newCreator);
                this.em.merge(o);
            }
            creator.setOwnedGroups(null);
            //HWConfs
            for (HWConf o : creator.getCreatedHWConfs()) {
                o.setCreator(newCreator);
                this.em.merge(o);
            }
            creator.setCreatedHWConfs(null);
            //PositiveList
            for (PositiveList o : creator.getOwnedPositiveLists()) {
                o.setOwner(newCreator);
                this.em.merge(o);
            }
            creator.setOwnedPositiveLists(null);
            //Partitions
            for (Partition o : creator.getCreatedPartitions()) {
                o.setCreator(newCreator);
                this.em.merge(o);
            }
            creator.setCreatedPartitions(null);
            //Rooms
            for (Room o : creator.getCreatedRooms()) {
                o.setCreator(newCreator);
                this.em.merge(o);
            }
            creator.setCreatedRooms(null);
            //User
            for (User o : creator.getCreatedUsers()) {
                o.setCreator(newCreator);
                this.em.merge(o);
            }
            creator.setCreatedUsers(null);
            //CrxConfig
            for (CrxConfig o : creator.getCreatedCrxConfig()) {
                o.setCreator(newCreator);
                this.em.merge(o);
            }
            //CrxMConfig
            for (CrxMConfig o : creator.getCreatedCrxMConfig()) {
                o.setCreator(newCreator);
                this.em.merge(o);
            }
            //Sessions will be deleted
            for (Session o : creator.getSessions()) {
                this.em.remove(o);
            }
            creator.setCreatedUsers(null);
            //Delete all correspondig configs.
            this.deletAllConfigs(creator);
            this.em.merge(newCreator);
        } catch (Exception e) {
            logger.error("inheritCreatedObjects:" + e.getMessage());
        }
    }

    public void deleteCreatedObjects(User creator) {
        try {
            //Acls
            for (Acl o : creator.getCreatedAcls()) {
                this.em.remove(o);
            }
            creator.setCreatedAcls(null);
            //AccessInRoom
            for (AccessInRoom o : creator.getCreatedAccessInRoom()) {
                this.em.remove(o);
            }
            creator.setCreatedAccessInRoom(null);
            //Announcement
            for (Announcement o : creator.getMyAnnouncements()) {
                this.em.remove(o);
            }
            creator.setMyAnnouncements(null);
            //Categories
            for (Category o : creator.getOwnedCategories()) {
                this.em.remove(o);
            }
            creator.setOwnedCategories(null);
            //Contacts
            for (Contact o : creator.getMyContacts()) {
                this.em.remove(o);
            }
            creator.setMyContacts(null);
            //Groups
            for (Group o : creator.getOwnedGroups()) {
                this.em.remove(o);
            }
            creator.setOwnedGroups(null);
            //HWConfs
            for (HWConf o : creator.getCreatedHWConfs()) {
                this.em.remove(o);
            }
            creator.setCreatedHWConfs(null);
            //PositiveList
            for (PositiveList o : creator.getOwnedPositiveLists()) {
                this.em.remove(o);
            }
            creator.setOwnedPositiveLists(null);
            //Rooms
            for (Room o : creator.getCreatedRooms()) {
                this.em.remove(o);
            }
            creator.setCreatedRooms(null);
            //User
            for (User o : creator.getCreatedUsers()) {
                this.em.remove(o);
            }
            creator.setCreatedUsers(null);
			/*Sessions will be deleted
			for( Session o : creator.getSessions() ) {
				this.em.remove(o);
			}*/
            //Delete all correspondig configs.
            this.deletAllConfigs(creator);
            //creator.setSessions(null);
            this.em.merge(creator);
        } catch (Exception e) {
            logger.error("delete owned objects:" + e.getMessage());
        }
    }

    @SuppressWarnings("unlikely-arg-type")
    public CrxResponse addAlias(User user, String alias) {
        if (user.getAliases().contains(alias)) {
            return new CrxResponse(this.getSession(), "OK", "The alias was already add to the user.");
        }
        if (!this.isUserAliasUnique(alias)) {
            return new CrxResponse(this.getSession(), "ERROR", "The alias was already add to an other user.");
        }
        try {
            Alias newAlias = new Alias(user, alias);
            user.getAliases().add(newAlias);
            this.em.getTransaction().begin();
            this.em.merge(user);
            this.em.getTransaction().commit();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new CrxResponse(this.getSession(), "ERROR", e.getMessage());
        }
        return new CrxResponse(this.getSession(), "OK", "The alias was add to the user succesfully.");
    }

    public CrxResponse addDefaultAliase(User user) {
        boolean error = false;
        CrxResponse crxResponse = this.addAlias(user, normalize(user.getGivenName() + "." + user.getSurName()).replace(" ", ""));
        error = crxResponse.getCode().equals("ERROR");
        crxResponse = this.addAlias(user, normalize(user.getSurName() + "." + user.getGivenName()).replace(" ", ""));
        if (error) {
            if (crxResponse.getCode().equals("ERROR")) {
                return new CrxResponse(this.getSession(), "ERROR", "Can not add default aliases.");
            } else {
                return new CrxResponse(this.getSession(), "ERROR", "Can not add Givenname.Surname alias.");
            }
        } else {
            if (crxResponse.getCode().equals("ERROR")) {
                return new CrxResponse(this.getSession(), "ERROR", "Can not ad Surname.Givenname alias");
            } else {
                return new CrxResponse(this.getSession(), "OK", "The alias was add to the user succesfully.");
            }
        }
    }

    public List<CrxResponse> applyAction(CrxActionMap crxActionMap) {
        List<CrxResponse> responses = new ArrayList<>();
        logger.debug(crxActionMap.toString());
        switch (crxActionMap.getName().toLowerCase()) {
            case "setpassword":
                return this.resetUserPassword(
                        crxActionMap.getObjectIds(),
                        crxActionMap.getStringValue(),
                        crxActionMap.isBooleanValue());
            case "setfilesystemquota":
                return this.setFsQuota(
                        crxActionMap.getObjectIds(),
                        crxActionMap.getLongValue());
            case "setmailsystemquota":
                return this.setMsQuota(
                        crxActionMap.getObjectIds(),
                        crxActionMap.getLongValue());
            case "disablelogin":
                return this.disableLogin(
                        crxActionMap.getObjectIds(),
                        true);
            case "enablelogin":
                return this.disableLogin(
                        crxActionMap.getObjectIds(),
                        false);
            case "disableinternet":
                return this.disableInternet(
                        crxActionMap.getObjectIds(),
                        true);
            case "enableinternet":
                return this.disableInternet(
                        crxActionMap.getObjectIds(),
                        false);
            case "mandatoryprofile":
                return this.mandatoryProfile(
                        crxActionMap.getObjectIds(),
                        crxActionMap.isBooleanValue());
            case "rwprofile":
                return this.mandatoryProfile(
                        crxActionMap.getObjectIds(),
                        false);
            case "copytemplate":
                return this.copyTemplate(
                        crxActionMap.getObjectIds(),
                        crxActionMap.getStringValue());
            case "removeprofiles":
                return this.removeProfile(crxActionMap.getObjectIds());
            case "delete":
                for (Long userId : crxActionMap.getObjectIds()) {
                    User user = this.getById(userId);
                    if (user != null) {
                        logger.debug("delete user:" + user);
                        responses.add(this.delete(user));
                    }
                }
        }
        return responses;
    }

    CrxResponse registerUserDevice(String MAC, User user){
        this.em.refresh(user);
        RoomService roomService = new RoomService(session, em);
        List<Room> rooms = roomService.getRoomToRegisterForUser(user);
        if(!rooms.isEmpty()) {
            Room room  = rooms.get(0);
            List<String> ipAddress = roomService.getAvailableIPAddresses(room.getId(), 1);
            String devName = user.getUid().replaceAll("_", "-")
                    .replaceAll("\\.", "") +
                    "-" + MAC.substring(8).replaceAll(":", "").toLowerCase();
            Device device = new Device();
            HWConf hwconf = room.getHwconf();
            device.setMac(MAC);
            device.setOwner(user);
            device.setIp(ipAddress.get(0).split(" ")[0]);
            device.setHwconf(hwconf);
            device.setRoom(room);
            this.em.getTransaction().begin();
            this.em.persist(device);
            this.em.merge(room);
            user.getOwnedDevices().add(device);
            this.em.merge(user);
            this.em.getTransaction().commit();
            startPlugin("add_device", device);
            return new CrxResponse(session,"OK","Device was registered for student:" + user.getUid());
        } else {
            logger.debug("User has no room to register:" + user.getUid());
            return new CrxResponse(session,"ERR","No adhoc room for student:" + user.getUid());
        }
    }
    public List<CrxResponse> moveStudentsDevices(){
        List<CrxResponse> responses = new ArrayList<>();
        List<Long> deviceIdsToDelete = new ArrayList<>();
        List<String> devices = new ArrayList<>();
        Map<String,User> newDevices = new HashMap<>();
        DeviceService deviceService = new DeviceService(this.session, this.em);
        for(User user: this.getByRole(roleStudent)){
            for (Device device : user.getOwnedDevices()) {
                deviceIdsToDelete.add(device.getId());
                newDevices.put(device.getMac(),user);
                devices.add(user.getUid() +";" + device.getMac());
            }
        }
        try {
            File file = File.createTempFile("moveStudentsDevices", ".json", new File(cranixTmpDir));
            Files.write(file.toPath(),devices);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
        for(Long deviceId : deviceIdsToDelete ) {
            responses.add(deviceService.delete(deviceId,false));
        }
        for(String mac: newDevices.keySet()){
            responses.add(registerUserDevice(mac, newDevices.get(mac)));
        }
        new DHCPConfig(this.session,this.em).Create();
        return responses;
    }
    public CrxResponse moveUserDevices(String uid) {
        return this.moveUserDevices(this.getByUid(uid));
    }
    public CrxResponse moveUserDevices(User user) {
        Group userClass = null;
        Integer counter = 0;
        List<String> params = new ArrayList<>();
        for ( Group group : user.getGroups() ){
            if(group.getGroupType().equals("class")){
                userClass = group;
                break;
            }
        }
        if(userClass == null) {
           return new CrxResponse(this.session,"ERROR","User is not member in any classes.");
        }
        AdHocLanService adHocLanService = new AdHocLanService(this.session,this.em);
        Room classRoom = adHocLanService.getAdHocRoomOfGroup(userClass);
        for (Device device : user.getOwnedDevices()) {
            logger.debug("user: " + user.getUid() + " device: " + device.getName() );
            for( Category category: device.getRoom().getCategories() ){
                if( category.getCategoryType().equals("adhocroom")) {
                    if(! category.getGroups().contains(userClass)) {
                        this.em.getTransaction().begin();
                        device.getRoom().getDevices().remove(device);
                        this.em.merge(device.getRoom());
                        device.setRoom(classRoom);
                        this.em.merge(classRoom);
                        this.em.getTransaction().commit();
                        counter++;
                    }
                }
            }
        }
        params.add(counter.toString());
        params.add(user.getUid());
        return new CrxResponse(this.session,"OK","%s devices of %s was moved in the new class.",params);
    }
}
