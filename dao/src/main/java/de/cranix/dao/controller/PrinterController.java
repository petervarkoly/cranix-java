package de.cranix.dao.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonObject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.cranix.dao.Device;
import de.cranix.dao.HWConf;
import de.cranix.dao.CrxResponse;
import de.cranix.dao.Printer;
import de.cranix.dao.Session;
import de.cranix.dao.tools.OSSShellTools;
import static de.cranix.dao.internal.CranixConstants.*;

public class PrinterController extends Controller {
	private Path DRIVERS   = Paths.get(cranixBaseDir + "templates/drivers.txt");
	final String[] encodings = { "US-ASCII", "ISO-8859-1", "UTF-8", "UTF-16BE", "UTF-16LE", "UTF-16" };
	public PrinterController(Session session,EntityManager em) {
		super(session,em);
		// TODO Auto-generated constructor stub
	}

	public String getModel(String name){

		List<String> lines;
		Path path = Paths.get("/etc/cups/ppd/" + name + ".ppd");
		Pattern pattern = Pattern.compile(".NickName: \"(.*)\"");
		for (String encoding : encodings) {
		    try {
		        lines = Files.readAllLines(path, Charset.forName(encoding));
		        for (String line : lines) {
				Matcher matcher = pattern.matcher(line);
				if( matcher.find() ) {
					return matcher.group(1);
				}
		        }
		        break;
		    } catch (IOException ioe) {
		        logger.error(encoding + " failed, trying next.");
		    }
		}
		return "";
	}

	/**
	 * Find a printer by id
	 * @param printerId
	 * @return
	 */
	public Printer getById(long printerId) {
		try {
			Printer printer = this.em.find(Printer.class, printerId);
			if( printer != null ) {
				if( printer.getDevice() != null ) {
					printer.setDeviceName(printer.getDevice().getName());
				}
				printer.setModel(getModel(printer.getName()));
			}
			return printer;
		} catch (Exception e) {
			return null;
		} finally {
		}
	}

	/**
	 * Find a printer by the name
	 * @param name
	 * @return
	 */
	public Printer getByName(String name) {
		try {
			Query query = this.em.createNamedQuery("Printer.getByName");
			query.setParameter("name", name);
			Printer printer = (Printer) query.getSingleResult();
			printer.setRoomId(printer.getDevice().getRoom().getId());
			return printer;
		} catch (Exception e) {
			logger.debug("name " + name  + " " + e.getMessage());
			return null;
		} finally {
		}
	}

	/**
	 * Delivers a list of all available printer
	 * @return
	 */
	public List<Printer> getPrinters() {
		List<JsonObject> printers  = new ArrayList<JsonObject>();
		List<Printer>    printers2 = new ArrayList<Printer>();
		String[] program = new String[1];
		StringBuffer reply  = new StringBuffer();
		StringBuffer stderr = new StringBuffer();
		program[0] = cranixBaseDir + "tools/get_printer_list.py";
		OSSShellTools.exec(program, reply, stderr, null);
		logger.debug(stderr.toString());
		try {
			printers = (List<JsonObject> )Json.createReader(
					 IOUtils.toInputStream(reply.toString(), "UTF-8")).read();
		} catch (Exception e) {
			logger.debug("getPrinters :" + e.getMessage());
			return printers2;
		}
		for( JsonObject p : printers ) {
			if( !p.containsKey("name") ){
				continue;
			}
			logger.debug("printer" + p);
			logger.debug("printer" + p.getString("name"));
			Printer printer = getByName(p.getString("name"));
			if( printer != null ) {
				printer.setModel(getModel(printer.getName()));
				printer.setState(p.getString("status","disabled"));
				printer.setAcceptingJobs(p.getBoolean("acceptingJobs",false));
				printer.setWindowsDriver(p.getBoolean("windowsDriver",false));
				printer.setActiveJobs(p.getInt("activeJobs",0));
				printer.setModel(getModel(p.getString("name")));
				printers2.add(printer);
			} else {
				logger.error("Can not find printer:" + p.getString("name"));
			}
		}
		return printers2;
	}
	
	/**
	 * Deletes a printer found by name.
	 * @param printerId
	 * @return
	 */
	public CrxResponse deletePrinter(Long printerId) {
		/*if( session.getPassword().equals("dummy") ) {
			logger.error("deletePrinter: The session password of the administrator is expiered.");
			return new CrxResponse(session,"ERROR","The session password of the administrator is expiered. Please login into the web interface again.");
		}*/

		CrxResponse ossResponse = new CrxResponse(session,"OK","Printer was deleted succesfully.");
		try {
			Printer printer = this.em.find(Printer.class, printerId);
			if( printer == null ) {
				logger.error("deletePrinter: Can not find printer.");
				return new CrxResponse(this.getSession(),"ERROR", "Can not find printer with id %s.",null,String.valueOf(printerId));
			}
			Device  printerDevice = printer.getDevice();
			String[] program    = new String[3];
			StringBuffer reply  = new StringBuffer();
			StringBuffer stderr = new StringBuffer();
			program[0] = "/usr/sbin/lpadmin";
			program[1] = "-x";
			program[2] = printer.getName();
			OSSShellTools.exec(program, reply, stderr, null);
			logger.debug("deletePrinter reply:" + reply.toString() + " err:" + stderr.toString());
			this.em.getTransaction().begin();
			printerDevice.getPrinterQueue().remove(printer);
			this.em.remove(printer);
			this.em.merge(printerDevice);
			this.em.getTransaction().commit();
			if( printerDevice.getPrinterQueue().isEmpty() ) {
				ossResponse = new DeviceController(this.session,this.em).delete(printerDevice, true);
			}
			this.systemctl("reload", "samba-printserver");
		} catch (Exception e) {
			logger.debug("deletePrinter :" + e.getMessage());
			return null;
		} finally {
			if( this.em.getTransaction().isActive() ) {
				this.em.getTransaction().rollback();
			}
		}
		return ossResponse;
	}

	public CrxResponse activateWindowsDriver(String printerName) {
		logger.debug("Activating windows driver for: " + printerName);
		if( session.getPassword().equals("dummy") ) {
			return new CrxResponse(session,"ERROR","The session password of the administrator is expiered. Please login into the web interface again.");
		}
		String printserver   = this.getConfigValue("PRINTSERVER");
		String[] program     = new String[7];
		StringBuffer reply   = new StringBuffer();
		StringBuffer stderr  = new StringBuffer();
		program[0] = "/usr/sbin/cupsaddsmb";
		program[1] = "-v";
		program[2] = "-H";
		program[3] = printserver;
		program[4] = "-U";
		program[5] = "Administrator%" + session.getPassword();
		program[6] = printerName;
		int success = OSSShellTools.exec(program, reply, stderr, null);
		try {
			logger.debug(new ObjectMapper().writeValueAsString(program));
		} catch (Exception e) {
			logger.debug( "{ \"ERROR\" : \"CAN NOT MAP THE OBJECT\" }" );
		}
		logger.debug("activateWindowsDriver cupsaddsmb stderr: " + stderr.toString());
		if( success != 0 ) {
			return new CrxResponse(session,"ERROR", stderr.toString());
		}
		program     = new String[6];
		program[0] = "/usr/bin/rpcclient";
		program[1] = "-U";
		program[2] = "Administrator%" + session.getPassword();
		program[3] = printserver;
		program[4] = "-c";
		program[5] = "setdriver " + printerName + " " + printerName;
		logger.debug("activateWindowsDriver rpcclient: " + reply.toString());
		OSSShellTools.exec(program, reply, stderr, null);
		logger.debug("activateWindowsDriver error" + stderr.toString());
		logger.debug("activateWindowsDriver reply" + reply.toString());
		return new CrxResponse(session,"OK","Windows driver was activated.");
	}

	public CrxResponse addPrinter(String name, String mac, Long roomId, String model, boolean windowsDriver,
			InputStream fileInputStream, FormDataContentDisposition contentDispositionHeader) {
		logger.debug("addPrinter: " + name + "#" + mac + "#" + roomId + "#" + model +"#" + ( windowsDriver ? "yes" : "no" ) + "#" + session.getPassword() );

		if( session.getPassword().equals("dummy") ) {
			return new CrxResponse(session,"ERROR","The session password of the administrator is expiered. Please login into the web interface again.");
		}
		//First we create a device object
		RoomController roomController = new RoomController(this.session,this.em);;
		HWConf hwconf = new CloneToolController(this.session,this.em).getByName("Printer");
		Device device = new Device();
		device.setMac(mac.trim());
		device.setName(name);
		device.setHwconfId(hwconf.getId());
		logger.debug(hwconf.getName() + "#" + hwconf.getId() );
		List<Device> devices = new ArrayList<Device>();
		devices.add(device);
		//Persist the device object
		CrxResponse ossResponse = roomController.addDevices(roomId, devices);
		if( ossResponse.getCode().equals("ERROR")) {
			return ossResponse;
		}
		return addPrinterQueue(session,name,device.getId(),model,windowsDriver,fileInputStream,contentDispositionHeader);
	}

	public CrxResponse addPrinterQueue(Session session, String name, Long deviceId, String model, boolean windowsDriver,
				InputStream fileInputStream, FormDataContentDisposition contentDispositionHeader) {

		if( session.getPassword().equals("dummy") ) {
			return new CrxResponse(session,"ERROR","The session password of the administrator is expiered. Please login into the web interface again.");
		}
		String deviceHostName;
		Printer printer = new Printer();
		//Create the printer object
		try {
			Device device = this.em.find(Device.class, deviceId);
			this.em.getTransaction().begin();
			
			printer.setDevice(device);
			printer.setName(name);
			this.em.persist(printer);
			logger.debug("Created Printer: " + printer);
			device.getPrinterQueue().add(printer);
			this.em.merge(device);
			this.em.getTransaction().commit();
			deviceHostName = device.getName();
		} catch (Exception e){
			logger.debug("addPrinterQueue :" + e.getMessage());
			return new CrxResponse(session,"ERROR",e.getMessage());
		} finally {
		}
		//Create the printer in CUPS
		String driverFile = "/usr/share/cups/model/Postscript.ppd.gz";
		if( fileInputStream != null ) {
			File file = null;
			try {
				file = File.createTempFile("oss_driverFile", name, new File(cranixTmpDir));
				Files.copy(fileInputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				return new CrxResponse(session,"ERROR", e.getMessage());
			}
			driverFile = file.toPath().toString();
		} else {
			try {
				for( String line : Files.readAllLines(DRIVERS) ) {
					String[] fields = line.split("###");
					if( fields.length == 2 && fields[0].equals(model) ) {
						driverFile = fields[1];
						break;
					}
				}
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				return new CrxResponse(session,"ERROR", e.getMessage());
			}
		}
		logger.debug("Add printer/usr/sbin/lpadmin -p " + name + " -P " + driverFile + " -o printer-error-policy=abort-job -o PageSize=A4 -v socket://" + name   );
		String[] program = new String[11];
		StringBuffer reply  = new StringBuffer();
		StringBuffer stderr = new StringBuffer();
		program[0] = "/usr/sbin/lpadmin";
		program[1] = "-p";
		program[2] = name;
		program[3] = "-P";
		program[4] = driverFile;
		program[5] = "-o";
		program[6] = "printer-error-policy=abort-job";
		program[7] = "-o";
		program[8] = "PageSize=A4";
		program[9] = "-v";
		program[10]= "socket://"+ deviceHostName;
				
		OSSShellTools.exec(program, reply, stderr, null);
		logger.debug(stderr.toString());
		logger.debug(reply.toString());
		this.systemctl("try-restart", "samba-printserver");
		//Now we have to check if the printer is already visible in samba
		int tries = 6;
		program = new String[6];
		program[0] = "/usr/bin/rpcclient";
		program[1] = "-U";
		program[2] = "Administrator%" + session.getPassword();
		program[3] = this.getConfigValue("PRINTSERVER");
		program[4] = "-c";
		program[5] = "getprinter "+name;
		do {
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			reply   = new StringBuffer();
			stderr  = new StringBuffer();
			OSSShellTools.exec(program, reply, stderr, null);
			logger.debug("activateWindowsDriver error" + stderr.toString());
		} while( !stderr.toString().isEmpty() && tries > -1  );
		
		if(windowsDriver) {
			CrxResponse ossResponse = activateWindowsDriver(name);
			if( ossResponse.getCode().equals("ERROR")) { 
				return ossResponse;
			}
		}
		enablePrinter(name);

		return new CrxResponse(
				session,"OK",
				"Printer was created succesfully.",
				printer.getId()
				);
	}

	public CrxResponse enablePrinter(String printerName) {
		String[] program = new String[2];
		StringBuffer reply  = new StringBuffer();
		StringBuffer stderr = new StringBuffer();
		program[0] = "/usr/sbin/cupsenable";
		program[1] = printerName;
		OSSShellTools.exec(program, reply, stderr, null);
		program[0] = "/usr/sbin/cupsaccept";
		program[1] = printerName;
		OSSShellTools.exec(program, reply, stderr, null);
		return new CrxResponse(session,"OK","Printer was enabled succesfully.");
	}

	public CrxResponse disablePrinter(String printerName) {
		String[] program = new String[2];
		StringBuffer reply  = new StringBuffer();
		StringBuffer stderr = new StringBuffer();
		program[0] = "/usr/sbin/cupsdisable";
		program[1] = printerName;
		OSSShellTools.exec(program, reply, stderr, null);
		program[0] = "/usr/sbin/cupsreject";
		program[1] = printerName;
		OSSShellTools.exec(program, reply, stderr, null);
		return new CrxResponse(session,"OK","Printer was disabled succesfully.");
	}

}
