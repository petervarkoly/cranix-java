package de.cranix.api.resourceimpl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.ws.rs.WebApplicationException;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.cranix.api.resources.PrinterResource;
import de.cranix.dao.Device;
import de.cranix.dao.CrxResponse;
import de.cranix.dao.Printer;
import de.cranix.dao.PrintersOfManufacturer;
import de.cranix.dao.Session;
import de.cranix.dao.controller.*;
import de.cranix.dao.internal.CommonEntityManagerFactory;
import de.cranix.dao.tools.OSSShellTools;
import static de.cranix.dao.internal.CranixConstants.*;

public class PrinterResourceImpl implements PrinterResource {

	Logger logger = LoggerFactory.getLogger(PrinterResourceImpl.class);

	private Path PRINTERS  = Paths.get(cranixPrinters);

	public PrinterResourceImpl() {
	}

	@Override
	public List<Printer> getPrinters(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<Printer> resp = new PrinterController(session,em).getPrinters();
		em.close();
		return resp;
	}

	@Override
	public CrxResponse deletePrinter(Session session, Long printerId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new PrinterController(session,em).deletePrinter(printerId);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse deletePrinter(Session session, String printerName) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		PrinterController pc = new PrinterController(session,em);
		Printer printer = pc.getByName(printerName);
		if( printer == null ) {
			em.close();
			throw new WebApplicationException(404);
		}
		CrxResponse resp = pc.deletePrinter(printer.getId());
		em.close();
		return resp;
	}

	@Override
	public CrxResponse resetPrinter(Session session, Long printerId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		Printer printer = new PrinterController(session,em).getById(printerId);
		if( printer == null ) {
			em.close();
			throw new WebApplicationException(404);
		}
		CrxResponse resp = resetPrinter(session, printer.getName());
		em.close();
		return resp;
	}

	@Override
	public CrxResponse resetPrinter(Session session, String printerName) {
		String[] program = new String[4];
		StringBuffer reply  = new StringBuffer();
		StringBuffer stderr = new StringBuffer();
		program[0] = "/usr/bin/lprm";
		program[1] = "-P";
		program[2] = printerName;
		program[3] = "-";
		OSSShellTools.exec(program, reply, stderr, null);
		this.enablePrinter(session, printerName);
		return new CrxResponse(session,"OK","Printer was reseted succesfully.");
	}

	@Override
	public CrxResponse enablePrinter(Session session, Long printerId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		PrinterController pc = new PrinterController(session,em);
		Printer printer = pc.getById(printerId);
		if( printer == null ) {
			em.close();
			throw new WebApplicationException(404);
		}
		CrxResponse resp = pc.enablePrinter(printer.getName());
		em.close();
		return resp;
	}

	@Override
	public CrxResponse enablePrinter(Session session, String printerName) {
		CrxResponse resp = new PrinterController(session,null).enablePrinter(printerName);
		return resp;
	}

	@Override
	public CrxResponse disablePrinter(Session session, Long printerId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		PrinterController pc = new PrinterController(session,em);
		Printer printer = pc.getById(printerId);
		if( printer == null ) {
			throw new WebApplicationException(404);
		}
		CrxResponse resp = pc.disablePrinter(printer.getName());
		em.close();
		return resp;
	}

	@Override
	public CrxResponse disablePrinter(Session session, String printerName) {
		return new PrinterController(session,null).disablePrinter(printerName);
	}

	@Override
	public CrxResponse activateWindowsDriver(Session session, Long printerId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		Printer printer = new PrinterController(session,em).getById(printerId);
		em.close();
		if( printer == null ) {
			throw new WebApplicationException(404);
		}
		return activateWindowsDriver( session, printer.getName() );
	}

	@Override
	public CrxResponse activateWindowsDriver(Session session, String printerName) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new PrinterController(session,em).activateWindowsDriver(printerName);
		em.close();
		return resp;
	}

	@Override
	public CrxResponse addPrinter(Session session,
			String name,
			String mac,
			Long roomId,
			String model,
			boolean windowsDriver,
			InputStream fileInputStream,
			FormDataContentDisposition contentDispositionHeader) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new PrinterController(session,em).addPrinter(name.toLowerCase().trim(),mac,roomId,model,windowsDriver,fileInputStream,contentDispositionHeader);
		em.close();
		return resp;
	}

	@Override
	public Map<String,String[]> getAvailableDrivers(Session session) {
		Map<String,String[]> drivers = new HashMap<String,String[]>();
		try {
			for( String line : Files.readAllLines(PRINTERS) ) {
				String[] fields = line.split("###");
				if( fields.length == 2 ) {
					drivers.put(fields[0], fields[1].split("%%"));
				}
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return drivers;
	}

	@Override
	public List<PrintersOfManufacturer> getDrivers(Session session) {
		List<PrintersOfManufacturer> printers = new ArrayList<PrintersOfManufacturer>();
		try {
			for( String line : Files.readAllLines(PRINTERS) ) {
				PrintersOfManufacturer printersOfManufacturer = new PrintersOfManufacturer();
				String[] fields = line.split("###");
				if( fields.length == 2 ) {
					printersOfManufacturer.setName(fields[0]);
					printersOfManufacturer.setPrinters(fields[1].split("%%"));
					printers.add(printersOfManufacturer);
				}
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return printers;
	}

	@Override
	public CrxResponse setDriver(Session session,
			Long printerId, InputStream fileInputStream,
			FormDataContentDisposition contentDispositionHeader) {

		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		Printer printer = new PrinterController(session,em).getById(printerId);
		em.close();
		if( printer == null ) {
			throw new WebApplicationException(404);
		}
		File file = null;
		try {
			file = File.createTempFile("oss_driverFile", printer.getName(), new File(cranixTmpDir));
			Files.copy(fileInputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return new CrxResponse(session,"ERROR", e.getMessage());
		}
		String driverFile = file.toPath().toString();
		String[] program = new String[11];
		StringBuffer reply  = new StringBuffer();
		StringBuffer stderr = new StringBuffer();
		program[0] = "/usr/sbin/lpadmin";
		program[1] = "-p";
		program[2] = printer.getName();
		program[3] = "-P";
		program[4] = driverFile;
		program[5] = "-o";
		program[6] = "printer-error-policy=abort-job";
		program[7] = "-o";
		program[8] = "PageSize=A4";
		program[9] = "-v";
		program[10]= "socket://"+ printer.getName();

		OSSShellTools.exec(program, reply, stderr, null);
		logger.debug("activateWindowsDriver error" + stderr.toString());
		logger.debug("activateWindowsDriver reply" + reply.toString());
		//TODO check output
		return new CrxResponse(session,"OK", "Printer driver was set succesfully.");
	}

	@Override
	public CrxResponse addPrinterQueue(Session session, String name, Long deviceId, String model, boolean windowsDriver,
			InputStream fileInputStream, FormDataContentDisposition contentDispositionHeader) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		CrxResponse resp = new PrinterController(session,em).addPrinterQueue(session,name.toLowerCase().trim(),deviceId,model,windowsDriver,fileInputStream,contentDispositionHeader);
		em.close();
		return resp;
	}

	@Override
	public List<Device> getPrinterDevices(Session session) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		List<Device> resp = new CloneToolController(session,em).getByName("Printer").getDevices();
		em.close();
		return resp;
	}

	@Override
	public Printer getPrinterById(Session session, Long printerId) {
		EntityManager em = CommonEntityManagerFactory.instance("dummy").getEntityManagerFactory().createEntityManager();
		Printer resp = new PrinterController(session,em).getById(printerId);
		em.close();
		return resp;
	}
}
