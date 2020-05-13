/* (c) 2017 EXTIS GmbH - all rights reserved  */

package de.cranix.dao.internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import org.slf4j.LoggerFactory;

import de.claxss.importlib.ImportOrder;
import de.claxss.importlib.Importer;
import de.claxss.importlib.ImporterObject;
import de.claxss.importlib.Person;
import de.claxss.importlib.SchoolClass;
import de.claxss.importlib.common.ImporterUtil;
import de.cranix.dao.Group;
import de.cranix.dao.CrxResponse;
import de.cranix.dao.Session;
import de.cranix.dao.User;
import de.cranix.dao.controller.GroupController;
import de.cranix.dao.controller.SystemController;
import de.cranix.dao.controller.UserController;

public class ImportHandler extends Thread {
	private Session session;
	private Importer importer;
	private ImportOrder order;
	EntityManager em;
	StringBuilder responseString;
	boolean done = false;

	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ImportHandler.class);

	public ImportHandler(Session session, Importer importer, ImportOrder o, EntityManager em) {
		this.session = session;
		this.importer = importer;
		this.order = o;
		this.em = em;
		responseString = new StringBuilder();
	}

	public String getResponseString() {
		return responseString.toString();
	}

	public void handleObjects() {
		start();
		// doHandleObjects();

		// return responseString.toString();
	}

	private void doHandleObjects() {
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd.HH-mm-ss");
		importStartDt = fmt.format(new Date());

		String os = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
		if ((os.indexOf("mac") >= 0) || (os.indexOf("darwin") >= 0)) {
			filepath = "/tmp/userimport." + importStartDt;
		} else {
			filepath = "/home/groups/SYSADMINS/userimports/" + importStartDt;
			File dir = new File("/home/groups/SYSADMINS/userimports/");
			dir.mkdir();
		}

		ImporterObject object;
		int ctr = 0;
		String oldConfigValuePWCheck = "yes";
		final SystemController systemController = new SystemController(session, em);

		try {
			final UserController userController = new UserController(session, em);
			final GroupController groupController = new GroupController(session, em);

			oldConfigValuePWCheck = systemController.getConfigValue("CHECK_PASSWORD_QUALITY");
			if (oldConfigValuePWCheck == null || !"no".equals(oldConfigValuePWCheck)) {
				systemController.setConfigValue("CHECK_PASSWORD_QUALITY", "no");
			}

			List<User> allUsers = null;
			if (order.getRequestedUserRole() != null && order.getRequestedUserRole().length() > 0) {
				allUsers = userController.getByRole(order.getRequestedUserRole());
			} else {
				allUsers = userController.getAll();
			}
			List<Group> allClasses = groupController.getByType("class");
			Set<String> foundRoles = new HashSet<String>();
			List<Person> oldUserList = buildOldUserlist(userController);
			do {

				// handle data
				object = importer.getNextObject();

				if (object != null) {
					String objectMsg = object.getObjectMessage();
					order.setPercentCompleted((100 / importer.getNumberOfObjects()) * ctr);
					ctr++;
					LOG.debug("found object: " + object.getClass());
					if (object instanceof de.claxss.importlib.SchoolClass) {
						if (!doCompareAndImportSchoolClass(session, (de.claxss.importlib.SchoolClass) object, order,
								responseString, groupController, allClasses)) {
							writeLog(importer, order, "Import " + object.getObjectMessage() + ": FAILED");

						} else {

							// appendLog(importer, order, "Import " + object.getObjectMessage() + ": OK");
						}
					} else if (object instanceof de.claxss.importlib.Person) {

						if (!doCompareAndImportUser(session, (de.claxss.importlib.Person) object, order, importer,
								responseString, groupController, userController, foundRoles, oldUserList, allUsers)) {

							writeLog(importer, order, "Import " + objectMsg + ": FAILED");
						} else {

							// appendLog(importer, order, "Import " + objectMsg + ": OK");
						}
					}
					order.setImportResult(responseString.toString());
					// LOG.debug("Setting result string: " +
					// order.getImportResult());
					// try {
					// LOG.debug("Waiting for debugging....");
					// Thread.sleep(10000); // TODO remove me
					// } catch (InterruptedException e) {
					// LOG.error(e.getMessage(), e);
					// e.printStackTrace();
					// }
				}
			} while (object != null);

			// cleanup processing

			if (order.isIncludesAll()) {
				for (User user : allUsers) {
					if (foundRoles.contains(user.getRole()) && !"Default profile".equals(user.getGivenName())) {
						if (!order.isTestOnly()) {
							// responseString.append("Lösche Benutzer: ").append(user.getUid()).append(" "
							// ).append(user.getGivenName()).append("
							// ").append(user.getSurName()).append(LINESEP);
							appendLog(importer, order, user, "Lösche Benutzer: ", "", responseString);
							userController.delete(user);
						} else {
							// responseString.append("Werde Benutzer löschen:
							// ").append(user.getUid()).append(" " ).append(user.getGivenName()).append("
							// ").append(user.getSurName()).append(LINESEP);
							appendLog(importer, order, user, "Werde Benutzer löschen: ", "", responseString);
						}
					}
					order.setImportResult(responseString.toString());

				}
			}
			if (order.isContainsAllClasses()) {
				for (Group group : allClasses) {
					if (!order.isTestOnly()) {
						// responseString.append("Lösche Klasse:
						// ").append(group.getName()).append(LINESEP);
						appendLog(importer, order, group, "Lösche Klasse: ", "", responseString);
						groupController.delete(group);
					} else {
						// responseString.append("Werde Klasse löschen:
						// ").append(group.getName()).append(LINESEP);
						appendLog(importer, order, group, "Werde Klasse löschen: ", "", responseString);

					}
					order.setImportResult(responseString.toString());

				}
			}
			if (order.isCleanupClassesDir()) {
				if (!order.isTestOnly()) {
					// responseString.append("Lösche Inhalte der
					// Klassenverzeichnisse").append(LINESEP);
					appendLog(importer, order, "Lösche Inhalte der Klassenverzeichnisse", responseString);
					groupController.cleanClassDirectories();
				} else {
					// responseString.append("Werde Inhalte der Klassenverzeichnisse
					// löschen").append(LINESEP);
					appendLog(importer, order, "Werde Inhalte der Klassenverzeichnisse löschen", responseString);
				}
			}
			order.setImportResult(responseString.toString());

			if (order.isCleanupUserData()) {
				// TODO cleanup user home dies
				// maybe we will implement this later, actually not done
			}

		} finally {
			closeLogfiles();
			if (oldConfigValuePWCheck == null || !"no".equals(oldConfigValuePWCheck)) {
				systemController.setConfigValue("CHECK_PASSWORD_QUALITY", oldConfigValuePWCheck);
			}
		}
		order.setPercentCompleted(100);
		done = true;
	}

	private OutputStream logfile = null;
	// private OutputStream useraddLogfile = null;
	Map<String, OutputStream> useraddLogfiles = new HashMap<String, OutputStream>();
	private String CSVSEP = ";";
	private String LINESEP = "\n";
	private String CLASSESSEP = ",";
	private String importStartDt;
	private String filepath;

	private OutputStream getUserAddLogFile(String schoolclass) throws FileNotFoundException {
		OutputStream result = useraddLogfiles.get(schoolclass);
		if (result == null) {
			File uaf = new File(filepath + "/userlist_" + schoolclass + ".csv");

			result = new FileOutputStream(uaf);
			useraddLogfiles.put(schoolclass, result);
			StringBuilder b = new StringBuilder();
			b.append("LOGIN").append(CSVSEP).append("NACHNAME").append(CSVSEP).append("VORNAME").append(CSVSEP)
					.append("GEBURTSTAG").append(CSVSEP).append("KLASSE").append(CSVSEP).append("PASSWORT")
					.append(LINESEP);
			try {
				result.write(b.toString().getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				LOG.error("createLogfiles: " + e.getMessage());
			} catch (IOException e) {
				LOG.error("createLogfiles: " + e.getMessage());
			}
		}
		return result;
	}

	private void createLogfiles(Importer i, ImportOrder o) {

		if (logfile == null) {

			File dir = new File(filepath);
			dir.mkdir();
			
			File params = new File(filepath + "/parameters.json");
			FileOutputStream paramout;
			try {
				paramout = new FileOutputStream(params);
				StringBuilder paramb = new StringBuilder();
				paramb.append("{\"role\":\"").append(o.getRequestedUserRole()!=null ? o.getRequestedUserRole() : "")
				      .append("\",\"lang\":\"DE\",\"identifier\":\"sn-gn-bd\",\"startTime\":\"")
				      .append(importStartDt).append("\",\"test\":")
				      .append(o.isTestOnly())
				      .append(",\"password\":\"")
				      .append(o.getPwd()!=null ? o.getPwd():"")
				      .append("\",\"mustchange\":")
				      .append("false")
				      .append(",\"full\":")
				      .append(o.isIncludesAll())
				      .append(",\"allClasses\":")
				      .append(o.isContainsAllClasses())
				      .append(",\"cleanClassDirs\":")
				      .append(o.isCleanupClassesDir())
				      .append(",\"resetPassword\":")
				      .append("false").append("}");
				paramout.write(paramb.toString().getBytes());
				paramout.close();
			} catch (FileNotFoundException e1) {
				LOG.error(e1.getMessage(),e1);
			} catch (IOException e) {
				LOG.error(e.getMessage(),e);
			}
	
			File lf = new File(filepath + "/import.log");
			try {
				if (logfile == null) {
					logfile = new FileOutputStream(lf);
					StringBuilder b = new StringBuilder();
					b.append(importStartDt).append(" Import: ").append(i.getImporterDescription().getName())
							.append(" Modus: ").append(o.isTestOnly() ? "Test Import" : "Tatsächlicher Import").append("<br>")
							.append(LINESEP);
					try {
						logfile.write(b.toString().getBytes("UTF-8"));
					} catch (UnsupportedEncodingException e) {
						LOG.error("createLogfiles: " + e.getMessage());
					} catch (IOException e) {
						LOG.error("createLogfiles: " + e.getMessage());
					}
				}

			} catch (FileNotFoundException e) {
				LOG.error("createLogfiles: " + e.getMessage());
			}
		}
		// less userlist.6B.txt
		// schooladmin.extis.test1:/home/groups/SYSADMINS/userimport.2016-09-12.10-52-03
		// BENUTZERK334RZEL:NACHNAME:VORNAME:GEBURTSTAG:KLASSE:LOGIN:PASSWORT
		// Abed Aziz:Jusuf:2016-08-10:6B:jusuabed02:

	}

	private void closeLogfiles() {
		if (logfile != null) {
			try {
				logfile.close();
			} catch (IOException e) {
				LOG.error("closeLogfiles logfile:" + e.getMessage());
			}
			logfile = null;
		}
		for (OutputStream file : useraddLogfiles.values()) {
			try {
				file.close();
			} catch (IOException e) {
				LOG.error("closeLogfiles useraddLogfile:" + e.getMessage());
			}
		}
		useraddLogfiles.clear();

	}

	private String normalizeValue(String value) {
		if (value == null) {
			return "";
		} else if (value.contains(CSVSEP)) {
			if (value.contains("\"")) {
				value = value.replaceAll("\"", "\\\"");
			}
			return "\"" + value + "\"";
		}
		return value;
	}

	private String getFirstClass(User user) {
		if (user.getGroups() != null) {
			if (user.getGroups().size() == 1) {
				return user.getGroups().get(0).getName();
			}
			for (Group group : user.getGroups()) {
				if ("class".equals(group.getGroupType())) {
					return group.getName();
				}
			}
		}
		return "";
	}

	private String getCSVClasses(User user) {
		StringBuilder b = new StringBuilder();
		if (user.getGroups() != null) {
			if (user.getGroups().size() == 1) {
				return user.getGroups().get(0).getName();
			}
			int i = 0;
			for (Group group : user.getGroups()) {
				if ("class".equals(group.getGroupType())) {
					if (i > 0) {
						b.append(CLASSESSEP);
					}
					b.append(group.getName());
					i++;
				}
			}
			return b.toString();
		}
		return "";
	}

	private String extractPW(CrxResponse res) {
		if (res.getParameters() != null && res.getParameters().size() >= 4) {
			return res.getParameters().get(3);
		}
		return "";
	}

	private void writeLog(Importer i, ImportOrder o, String msg) {
		createLogfiles(i, o);
		try {
			logfile.write(msg.getBytes("UTF-8"));
			logfile.write("<br>".getBytes());
			logfile.write(LINESEP.getBytes());
		} catch (UnsupportedEncodingException e) {
			LOG.error("appendLog:" + e.getMessage());
		} catch (IOException e) {
			LOG.error("appendLog:" + e.getMessage());
		}
	}

	private String getClassesArray(List<SchoolClass> classes) {
		if (classes != null && classes.size() > 0) {
			if (classes.size() == 1) {
				return classes.get(0).getOriginalName();
			} else {
				StringBuilder b = new StringBuilder();
				for (SchoolClass schoolClass : classes) {
					b.append(schoolClass.getOriginalName()).append(" ");
				}
				return b.toString();
			}
		}
		return null;
	}

	private void appendLog(Importer i, ImportOrder o, String message, StringBuilder responseString) {
		writeLog(importer, order, message);
		responseString.append(message).append(LINESEP);
	}

	private void appendLog(Importer i, ImportOrder o, User user, String prolog, String epilog,
			StringBuilder responseString) {
		StringBuilder b = new StringBuilder();
		SimpleDateFormat fmt = new SimpleDateFormat("dd.MM.yyyy");
		String birthday = user.getBirthDay() != null ? fmt.format(user.getBirthDay()) : "";
		b.append(prolog).append(user.getSurName()).append(", ").append(user.getGivenName()).append(", ")
				.append(birthday).append(", ").append(user.getUid()).append(", ").append(epilog);
		String txt = b.toString();
		responseString.append(txt).append(LINESEP);
		writeLog(importer, order, txt);
	}

	private void appendLog(Importer i, ImportOrder o, Group group, String prolog, String epilog,
			StringBuilder responseString) {
		StringBuilder b = new StringBuilder();
		b.append(prolog).append(group.getName()).append(epilog);
		String txt = b.toString();
		responseString.append(txt).append(LINESEP);
		writeLog(importer, order, txt);
	}

	private void appendUserAddLog(Importer i, ImportOrder o, CrxResponse res, User newUser, boolean create) {
		if (!o.isTestOnly()) {
			createLogfiles(i, o);
			try {
				StringBuilder buf = new StringBuilder();
				SimpleDateFormat fmt = new SimpleDateFormat("dd.MM.yyyy");
				String birthday = newUser.getBirthDay() != null ? fmt.format(newUser.getBirthDay()) : "";
				String classes = getCSVClasses(newUser);
				String firstclass = getFirstClass(newUser);
				buf.append(newUser.getUid()).append(CSVSEP).append(normalizeValue(newUser.getSurName())).append(CSVSEP)
						.append(normalizeValue(newUser.getGivenName())).append(CSVSEP).append(birthday).append(CSVSEP)
						.append(normalizeValue(classes)).append(CSVSEP).append(res != null ? extractPW(res) : "")
						.append(LINESEP);
				getUserAddLogFile(firstclass).write(buf.toString().getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				LOG.error("appendUserAddLog:" + e.getMessage());
			} catch (IOException e) {
				LOG.error("appendUserAddLog:" + e.getMessage());
			}
		}
	}

	private boolean doCompareAndImportUser(Session session, Person person, ImportOrder o, Importer importer,
			StringBuilder responseString, GroupController groupController, UserController userController,
			Set<String> foundRoles, List<Person> oldUserList, List<User> remainingUsers) {

		Person existingUser = null;

		// List<Person> handledUsers = new ArrayList<Person>();

		if (person.getPersonNumber() != null && person.getPersonNumber().length() > 0) {
			// try to find per uuid
			for (Person person2 : oldUserList) {
				if (person.getPersonNumber().equals(person2.getPersonNumber())) {
					existingUser = person2;
					LOG.info("user  found by uuid: " + person.getPersonNumber() + " " + person.getFirstname() + " "
							+ person.getName() + " " + person.getLoginId() + " " + person.getBirthday());
				}
			}
		}
		if (existingUser != null) {
			existingUser = ImporterUtil.findUserByUid(oldUserList, person);
		}
		if (existingUser == null) {
			LOG.info("user not found by uid: " + person.getFirstname() + " " + person.getName() + " "
					+ person.getLoginId() + " " + person.getBirthday());
		}
		if (existingUser == null) {
			if (o.getRequestedUserRole() != null && o.getRequestedUserRole().length() > 0
					&& o.getRequestedUserRole().equals("students")) {
				person.setPersonType(Person.PersonType.STUDENT);
				;
			}

			existingUser = ImporterUtil.findUser(o, oldUserList, person); // TODO
																			// handle
																			// whether
																			// to
																			// ignore
																			// birthday
																			// or
																			// not
																			// (additional
																			// boolean
																			// parameter
																			// provided
																			// by
																			// lib)
		}
		if (existingUser == null) {
			LOG.info("user not found: " + person.getFirstname() + " " + person.getName() + " " + person.getLoginId()
					+ " " + person.getBirthday());
		}
		/* ========== second step: create or update the user ============ */
		if (existingUser != null) {
			// handledUsers.add(existingUser);

			User ossUser = (User) existingUser.getData();
			remainingUsers.remove(ossUser);
			foundRoles.add(ossUser.getRole());

			// update the user
			boolean change = false;
			if (person.getLoginId() != null && !person.getLoginId().equals(ossUser.getUid())) {
				ossUser.setUid(person.getLoginId());
				change = true;
			}
			if (person.getFirstname() != null && !person.getFirstname().equals(ossUser.getGivenName())) {
				ossUser.setGivenName(person.getFirstname());
				change = true;
			}
			if (person.getName() != null && !person.getName().equals(ossUser.getSurName())) {
				ossUser.setSurName(person.getName());
				change = true;
			}
			if (person.getBirthday() != null && !person.getBirthday().equals(ossUser.getBirthDay())) {
				ossUser.setBirthDay(person.getBirthday());
				change = true;
			}
			if (change && !o.isTestOnly()) {
				CrxResponse res = userController.modify(ossUser);
				appendUserAddLog(importer, o, res, ossUser, false);
			} else if (!o.isTestOnly()) {
				appendUserAddLog(importer, o, null, ossUser, false);
			}
			if (person.getSchoolClasses() != null && person.getSchoolClasses().size() > 0) {
				// use new classes
				List<SchoolClass> removeClasses = new ArrayList<SchoolClass>();
				List<SchoolClass> newClasses = new ArrayList<SchoolClass>();
				List<SchoolClass> keepClasses = new ArrayList<SchoolClass>();
				ImporterUtil.handleSchoolClassesDiff(existingUser, person, removeClasses, newClasses, keepClasses);
				//StringBuilder bclasses = new StringBuilder();
				if (!o.isTestOnly()) {
					for (SchoolClass schoolClass : removeClasses) {
						groupController.removeMember(((Group) schoolClass.getData()).getId(), ossUser.getId());
					}
					for (SchoolClass schoolClass : newClasses) {

						Group group = groupController.getByExactName(schoolClass.getOriginalName());
						if (group == null) {
							group = groupController.getByExactName(schoolClass.getOriginalName().toUpperCase());
						}
						if (group == null) {
							group = groupController.getByExactName(schoolClass.getNormalizedName());
						}

						if (group != null) {
							//bclasses.append(group.getName()).append(" ");
							groupController.addMember(group.getId(), ossUser.getId());
						}
					}
				}
//				responseString.append("Benutzer wird aktualisiert: ").append(existingUser.getName()).append(", ")
//						.append(existingUser.getFirstname()).append(" ").append(bclasses.toString()).append(LINESEP);
				String oldClassesStr = getClassesArray(keepClasses);
				String newClassesStr = getClassesArray(newClasses);
				String delClassesStr = getClassesArray(removeClasses);
				StringBuilder b = new StringBuilder();
				if (oldClassesStr!=null) {
					b.append("Erhalten: ").append(oldClassesStr);
				}
				if (newClassesStr!=null) {
					b.append(" Neu: ").append(newClassesStr);
				}
				if (delClassesStr!=null) {
					b.append(" Gelöscht: ").append(delClassesStr);
				}
				appendLog(importer, order, ossUser, "Benutzer wird aktualisiert: ", b.toString(),responseString);
			}
			if (change && !o.isTestOnly()) {
				userController.modify(ossUser);
			}
		} else {
			// create the user
			User newUser = new User();
			newUser.setUid(person.getLoginId());
			if (person.getPersonNumber() != null && person.getPersonNumber().length() > 0) {
				newUser.setUuid(person.getPersonNumber());
			}
			newUser.setGivenName(person.getFirstname() != null ? person.getFirstname() : "?");
			newUser.setSurName(person.getName() != null ? person.getName() : "?");
			newUser.setRole((o.getRequestedUserRole() != null && o.getRequestedUserRole().length() > 0)
					? o.getRequestedUserRole()
					: getOSSRole(person));
			foundRoles.add(newUser.getRole());
			if (person.getBirthday() != null) {
				newUser.setBirthDay(person.getBirthday());
			} else {
				newUser.setBirthDay(new Date());
			}

			if (person.getPassword() != null && person.getPassword().length() > 0) {
				newUser.setPassword(person.getPassword());
			} else if (o.getNewUserPassword() != null && o.getNewUserPassword().length() > 0) {
				// handling of given new user password
				if (o.getNewUserPassword().equals("[teachers:random,students:birthday]")) {

					if ("students".equals(newUser.getRole())) {
						SimpleDateFormat fmt = new SimpleDateFormat("dd.MM.yyyy");
						newUser.setPassword(fmt.format(newUser.getBirthDay()));

					} else {
						// no password set -> random

					}
				} else {
					newUser.setPassword(o.getNewUserPassword());
				}
			}

			CrxResponse useraddRes = null;
			if (!o.isTestOnly()) {
				useraddRes = userController.add(newUser);
				if (useraddRes != null && useraddRes.getObjectId() != null) {
					newUser = userController.getById(useraddRes.getObjectId());
				} else {
					if (useraddRes == null) {
						LOG.error("userController.add returns no response");
//						responseString.append("Benutzer kann nicht angelegt werden: ").append(newUser.getSurName())
//								.append(", ").append(newUser.getGivenName()).append(LINESEP);
						appendLog(importer, order, newUser, "Benutzer kann nicht angelegt werden: ", "",
								responseString);
					} else {
						LOG.error("userController.add returns null as objectid in response: " + useraddRes.getCode()
								+ " " + useraddRes.getValue());
//						responseString.append("Benutzer kann nicht angelegt werden: ").append(newUser.getSurName())
//								.append(", ").append(newUser.getGivenName()).append(". Grund: ")
//								.append(useraddRes.getValue()).append(LINESEP);
						appendLog(importer, order, newUser, "Benutzer kann nicht angelegt werden: ",
								" Grund: " + useraddRes.getValue(), responseString);

					}
					return false;
				}
				// appendUserAddLog(importer, o, res, newUser, true);
			} else {
				// appendUserAddLog(importer, o, null, newUser, true);
			}

			StringBuilder newUserClassesBuilder = new StringBuilder();
			if (newUser != null && newUser.getId() != null && !o.isTestOnly()) {
				if (person.getSchoolClasses() != null) {
					for (SchoolClass schoolClass : person.getSchoolClasses()) {
						Group group = groupController.getByExactName(schoolClass.getOriginalName());
						if (group == null) {
							group = groupController.getByExactName(schoolClass.getOriginalName().toUpperCase());
						}
						if (group == null) {
							group = groupController.getByExactName(schoolClass.getNormalizedName());
						}

						if (group != null && group.getId() != null) {
							LOG.debug("Add user to classes" + newUser.getUid() + " " + group.getName());
							newUserClassesBuilder.append(group.getName()).append(" ");
							groupController.addMember(group.getId(), newUser.getId());
						} else {
							LOG.info("Group not found: " + schoolClass.getNormalizedName() + " "
									+ (group != null ? group.getName() : ""));
						}
					}
				}
			}
//			responseString.append("Benutzer wird neu angelegt: ").append(newUser.getSurName()).append(", ")
//			.append(newUser.getGivenName()).append(" ").append(newUserClassesBuilder);
//			
//			responseString.append(LINESEP);
			appendLog(importer, order, newUser, "Benutzer wird neu angelegt: ", newUserClassesBuilder.toString(),
					responseString);

			if (!o.isTestOnly()) {
				// done here to get the classnames of the user
				newUser = userController.getById(useraddRes.getObjectId());
				appendUserAddLog(importer, o, useraddRes, newUser, true);
			} else {
				appendUserAddLog(importer, o, null, newUser, true);
			}
		}

		return true;
	}

	private List<Person> buildOldUserlist(final UserController userController) {
		/* get old list */
		List<User> oldUserlist = null;
		List<Person> oldUserlistIL = new ArrayList<Person>();

		oldUserlist = userController.getAll();

		for (User user : oldUserlist) {
			Person p = new Person();
			p.setBirthday(user.getBirthDay());
			if ("students".equals(user.getRole())) {
				p.addRole(Person.PersonType.STUDENT);
			} else if ("teachers".equals(user.getRole())) {
				p.addRole(Person.PersonType.TEACHER);
			}
			p.setFirstname(user.getGivenName());
			p.setName(user.getSurName());
			p.setLoginId(user.getUid());
			p.setData(user);
			p.setPersonNumber(user.getUuid());
			for (Group group : user.getGroups()) {
				if ("class".equals(group.getGroupType())) {
					SchoolClass sc = new SchoolClass(group.getName());
					sc.setOriginalName(group.getName());
					sc.setLongName(group.getDescription());
					sc.setData(group);
					p.addSchoolClass(sc);
				}
			}
			oldUserlistIL.add(p);
		}
		return oldUserlistIL;
	}

	private String getOSSRole(Person p) {
		switch (p.getPersonType()) {
		case STUDENT:
			return "students";

		case TEACHER:
			return "teachers";
		case PERSON:
			return "administration";
		}
		return "unknown";
	}

	private boolean doCompareAndImportSchoolClass(Session session, de.claxss.importlib.SchoolClass schoolClass,
			ImportOrder o, StringBuilder responseString, GroupController groupController,
			List<Group> remainingClasses) {
		// LOG.error("importing group: " + schoolClass.getNormalizedName());
		if (schoolClass != null && schoolClass.getNormalizedName() != null) {

			Group existingClass = groupController.getByExactName(schoolClass.getOriginalName());
			if (existingClass == null) {
				existingClass = groupController.getByExactName(schoolClass.getOriginalName().toUpperCase());
			}
			if (existingClass == null) {
				existingClass = groupController.getByExactName(schoolClass.getNormalizedName());
			}

			if (existingClass == null) {
				Group newClass = new Group();
				newClass.setName(schoolClass.getOriginalName().toUpperCase()); // getNormalizedName changed to original
																				// name for dosys
				newClass.setDescription(schoolClass.getLongName() != null ? schoolClass.getLongName()
						: schoolClass.getNormalizedName());
				newClass.setGroupType("class");

				if (!o.isTestOnly()) {
					groupController.add(newClass);
				}
				// responseString.append("Neue Klasse wird angelegt:
				// ").append(newClass.getName()).append(LINESEP);
				appendLog(importer, order, newClass, "Neue Klasse wird angelegt: ", "", responseString);

			} else {
				remainingClasses.remove(existingClass);
			}
		}

		return true;
	}

	@Override
	public void run() {
		doHandleObjects();
	}
}
