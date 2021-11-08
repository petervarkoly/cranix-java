package de.cranix.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.cranix.dao.*;
import de.cranix.helper.CrxSystemCmd;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import javax.json.Json;
import javax.json.JsonObject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.ws.rs.WebApplicationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.cranix.helper.CranixConstants.*;

public class PrinterService extends Service {
    private static final Path DRIVERS = Paths.get(cranixBaseDir + "templates/drivers.txt");
    private static final Path PRINTERS = Paths.get(cranixPrinters);

    final String[] encodings = {"US-ASCII", "ISO-8859-1", "UTF-8", "UTF-16BE", "UTF-16LE", "UTF-16"};

    public PrinterService(Session session, EntityManager em) {
        super(session, em);
    }

    public String getModel(String name) {

        List<String> lines;
        Path path = Paths.get("/etc/cups/ppd/" + name + ".ppd");
        Pattern pattern = Pattern.compile(".NickName: \"(.*)\"");
        for (String encoding : encodings) {
            try {
                lines = Files.readAllLines(path, Charset.forName(encoding));
                for (String line : lines) {
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
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
     *
     * @param printerId
     * @return The printer as Printer object
     */
    public Printer getById(long printerId) {
        try {
            Printer printer = this.em.find(Printer.class, printerId);
            if (printer != null) {
                if (printer.getDevice() != null) {
                    printer.setDeviceName(printer.getDevice().getName());
                }
                printer.setModel(getModel(printer.getName()));
            }
            return printer;
        } catch (Exception e) {
            logger.debug("id " + printerId + " " + e.getMessage());
            return null;
        }
    }

    /**
     * Find a printer by the name
     *
     * @param name
     * @return
     */
    public Printer getByName(String name) {
        try {
            Query query = this.em.createNamedQuery("Printer.getByName");
            query.setParameter("name", name);
            Printer printer = (Printer) query.getSingleResult();
            printer.setRoomId(printer.getDevice().getRoom().getId());
            printer.setMac(printer.getDevice().getMac());
            printer.setIp(printer.getDevice().getIp());
            printer.setDeviceName(printer.getDevice().getName());
            return printer;
        } catch (Exception e) {
            logger.debug("name " + name + " " + e.getMessage());
            return null;
        }
    }

    /**
     * Delivers a list of all available printer
     *
     * @return
     */
    public List<Printer> getPrinters() {
        List<JsonObject> printers = new ArrayList<JsonObject>();
        List<Printer> printers2 = new ArrayList<Printer>();
        String[] program = new String[1];
        StringBuffer reply = new StringBuffer();
        StringBuffer stderr = new StringBuffer();
        program[0] = cranixBaseDir + "tools/get_printer_list.py";
        CrxSystemCmd.exec(program, reply, stderr, null);
        logger.debug(stderr.toString());
        try {
            printers = (List<JsonObject>) Json.createReader(
                    IOUtils.toInputStream(reply.toString(), "UTF-8")).read();
        } catch (Exception e) {
            logger.debug("getPrinters :" + e.getMessage());
            return printers2;
        }
        for (JsonObject p : printers) {
            if (!p.containsKey("name")) {
                continue;
            }
            logger.debug("printer" + p);
            logger.debug("printer" + p.getString("name"));
            Printer printer = getByName(p.getString("name"));
            if (printer != null) {
                printer.setModel(getModel(printer.getName()));
                printer.setState(p.getString("status", "disabled"));
                printer.setAcceptingJobs(p.getBoolean("acceptingJobs", false));
                printer.setWindowsDriver(p.getBoolean("windowsDriver", false));
                printer.setActiveJobs(p.getInt("activeJobs", 0));
                printer.setModel(getModel(p.getString("name")));
                printers2.add(printer);
            } else {
                logger.error("Can not find printer:" + p.getString("name"));
            }
        }
        return printers2;
    }

    public CrxResponse deletePrinter(String name) {
        Printer printer = this.getByName(name);
        if (printer != null) {
            return this.deletePrinter(printer);
        }
        return new CrxResponse(this.getSession(), "ERROR", "Can not find printer with name %s.", null, name);
    }

    public CrxResponse deletePrinter(Long printerId) {
        Printer printer = this.em.find(Printer.class, printerId);
        if (printer != null) {
            return this.deletePrinter(printer);
        }
        return new CrxResponse(this.getSession(), "ERROR", "Can not find printer with id %s.", null, String.valueOf(printerId));
    }

    /**
     * Deletes a printer found by name.
     *
     * @param printer
     * @return
     */
    public CrxResponse deletePrinter(Printer printer) {
        CrxResponse crxResponse = new CrxResponse(session, "OK", "Printer was deleted succesfully.");
        try {
            Device printerDevice = printer.getDevice();
            String[] program = new String[3];
            StringBuffer reply = new StringBuffer();
            StringBuffer stderr = new StringBuffer();
            program[0] = "/usr/sbin/lpadmin";
            program[1] = "-x";
            program[2] = printer.getName();
            CrxSystemCmd.exec(program, reply, stderr, null);
            logger.debug("deletePrinter reply:" + reply.toString() + " err:" + stderr.toString());
            this.em.getTransaction().begin();
            printerDevice.getPrinterQueue().remove(printer);
            this.em.remove(printer);
            this.em.merge(printerDevice);
            this.em.getTransaction().commit();
            if (printerDevice.getPrinterQueue().isEmpty()) {
                crxResponse = new DeviceService(this.session, this.em).delete(printerDevice, true);
            }
            this.systemctl("reload", "samba-ad");
        } catch (Exception e) {
            logger.debug("deletePrinter :" + e.getMessage());
            return null;
        } finally {
            if (this.em.getTransaction().isActive()) {
                this.em.getTransaction().rollback();
            }
        }
        return crxResponse;
    }

    public CrxResponse activateWindowsDriver(Long id) {
        Printer printer = this.getById(id);
        if (printer == null) {
            return new CrxResponse(session, "ERROR", "Can not find the printer.");
        }
        return this.activateWindowsDriver(printer.getName());
    }

    public CrxResponse activateWindowsDriver(String printerName) {
        logger.debug("Activating windows driver for: " + printerName);
        String[] program = new String[7];
        StringBuffer reply = new StringBuffer();
        StringBuffer stderr = new StringBuffer();
        program[0] = "/usr/sbin/cupsaddsmb";
        program[1] = "-v";
        program[2] = "-H";
        program[3] = "localhost";
        program[4] = "-U";
        program[5] = "register%" + this.getProperty("de.cranix.dao.User.Register.Password");
        program[6] = printerName;
        int success = CrxSystemCmd.exec(program, reply, stderr, null);
        try {
            logger.debug(new ObjectMapper().writeValueAsString(program));
        } catch (Exception e) {
            logger.debug("{ \"ERROR\" : \"CAN NOT MAP THE OBJECT\" }");
        }
        logger.debug("activateWindowsDriver cupsaddsmb stderr: " + stderr.toString());
        if (success != 0) {
            return new CrxResponse(session, "ERROR", stderr.toString());
        }
        program = new String[6];
        program[0] = "/usr/bin/rpcclient";
        program[1] = "-U";
        program[2] = "register%" + this.getProperty("de.cranix.dao.User.Register.Password");
        program[3] = "localhost";
        program[4] = "-c";
        program[5] = "setdriver " + printerName + " " + printerName;
        logger.debug("activateWindowsDriver rpcclient: " + reply.toString());
        CrxSystemCmd.exec(program, reply, stderr, null);
        logger.debug("activateWindowsDriver error" + stderr.toString());
        logger.debug("activateWindowsDriver reply" + reply.toString());
        return new CrxResponse(session, "OK", "Windows driver was activated.");
    }

    public CrxResponse addPrinter(
            String name,
            String mac,
            Long roomId,
            String ip,
            String model,
            boolean windowsDriver,
            InputStream fileInputStream,
            FormDataContentDisposition contentDispositionHeader) {

        logger.debug("addPrinter: " + name + "#" + mac + "#" + roomId + "#" + model + "#" + (windowsDriver ? "yes" : "no") + "#" + session.getPassword());

        //First we create a device object
        RoomService roomService = new RoomService(this.session, this.em);
        HWConf hwconf = new CloneToolService(this.session, this.em).getByName("Printer");
        Device device = new Device();
        device.setMac(mac.trim());
        device.setName(name);
        device.setIp(ip);
        device.setHwconfId(hwconf.getId());
        logger.debug(hwconf.getName() + "#" + hwconf.getId());
        List<Device> devices = new ArrayList<Device>();
        devices.add(device);
        //Persist the device object
        CrxResponse crxResponse = roomService.addDevices(roomId, devices).get(0);
        if (crxResponse.getCode().equals("ERROR")) {
            return crxResponse;
        }
        return addPrinterQueue(session, name, device.getId(), model, windowsDriver, fileInputStream, contentDispositionHeader);
    }

    public CrxResponse addPrinterQueue(
            Session session,
            String name,
            Long deviceId,
            String model,
            boolean windowsDriver,
            InputStream fileInputStream,
            FormDataContentDisposition contentDispositionHeader) {

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
        } catch (Exception e) {
            logger.debug("addPrinterQueue :" + e.getMessage());
            return new CrxResponse(session, "ERROR", e.getMessage());
        }
        //Create the printer in CUPS
        String driverFile = "/usr/share/cups/model/Postscript.ppd.gz";
        if (fileInputStream != null) {
            File file = null;
            try {
                file = File.createTempFile("crx_driverFile", name, new File(cranixTmpDir));
                Files.copy(fileInputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                return new CrxResponse(session, "ERROR", e.getMessage());
            }
            driverFile = file.toPath().toString();
        } else {
            try {
                for (String line : Files.readAllLines(DRIVERS)) {
                    String[] fields = line.split("###");
                    if (fields.length == 2 && fields[0].equals(model)) {
                        driverFile = fields[1];
                        break;
                    }
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                return new CrxResponse(session, "ERROR", e.getMessage());
            }
        }
        logger.debug("Add printer/usr/sbin/lpadmin -p " + name + " -P " + driverFile + " -o printer-error-policy=abort-job -o PageSize=A4 -v socket://" + name);
        String[] program = new String[11];
        StringBuffer reply = new StringBuffer();
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
        program[10] = "socket://" + deviceHostName;

        CrxSystemCmd.exec(program, reply, stderr, null);
        logger.debug(stderr.toString());
        logger.debug(reply.toString());
        this.systemctl("try-restart", "samba-ad");
        //Now we have to check if the printer is already visible in samba
        int tries = 6;
        program = new String[6];
        program[0] = "/usr/bin/rpcclient";
        program[1] = "-U";
        program[2] = "register%" + this.getProperty("de.cranix.dao.User.Register.Password");
        program[3] = "localhost";
        program[4] = "-c";
        program[5] = "getprinter " + name;
        do {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            reply = new StringBuffer();
            stderr = new StringBuffer();
            CrxSystemCmd.exec(program, reply, stderr, null);
            logger.debug("activateWindowsDriver error" + stderr.toString());
        } while (!stderr.toString().isEmpty() && tries > -1);

        if (windowsDriver) {
            CrxResponse crxResponse = activateWindowsDriver(name);
            if (crxResponse.getCode().equals("ERROR")) {
                return crxResponse;
            }
        }
        enablePrinter(name);

        return new CrxResponse(
                session, "OK",
                "Printer was created succesfully.",
                printer.getId()
        );
    }

    public CrxResponse enablePrinter(Long id) {
        Printer printer = this.getById(id);
        if (printer == null) {
            return new CrxResponse(session, "ERROR", "Can not find the printer.");
        }
        return this.enablePrinter(printer.getName());
    }

    public CrxResponse enablePrinter(String printerName) {
        String[] program = new String[2];
        StringBuffer reply = new StringBuffer();
        StringBuffer stderr = new StringBuffer();
        program[0] = "/usr/sbin/cupsenable";
        program[1] = printerName;
        CrxSystemCmd.exec(program, reply, stderr, null);
        program[0] = "/usr/sbin/cupsaccept";
        program[1] = printerName;
        CrxSystemCmd.exec(program, reply, stderr, null);
        return new CrxResponse(session, "OK", "Printer was enabled succesfully.");
    }

    public CrxResponse disablePrinter(Long id) {
        Printer printer = this.getById(id);
        if (printer == null) {
            return new CrxResponse(session, "ERROR", "Can not find the printer.");
        }
        return this.disablePrinter(printer.getName());
    }

    public CrxResponse disablePrinter(String printerName) {
        String[] program = new String[2];
        StringBuffer reply = new StringBuffer();
        StringBuffer stderr = new StringBuffer();
        program[0] = "/usr/sbin/cupsdisable";
        program[1] = printerName;
        CrxSystemCmd.exec(program, reply, stderr, null);
        program[0] = "/usr/sbin/cupsreject";
        program[1] = printerName;
        CrxSystemCmd.exec(program, reply, stderr, null);
        return new CrxResponse(session, "OK", "Printer was disabled succesfully.");
    }

    public CrxResponse setDriver(
            Long printerId,
            String model,
            InputStream fileInputStream,
            FormDataContentDisposition contentDispositionHeader) {
        Printer printer = this.getById(printerId);
        if (printer == null) {
            throw new WebApplicationException(404);
        }
        String driverFile = "/usr/share/cups/model/Postscript.ppd.gz";
        if (fileInputStream != null) {
            File file = null;
            try {
                file = File.createTempFile("crx_driverFile", printer.getName(), new File(cranixTmpDir));
                Files.copy(fileInputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                return new CrxResponse(session, "ERROR", e.getMessage());
            }
            driverFile = file.toPath().toString();
        } else {
            logger.debug("setDriver :" + printerId + " " + model);
            try {
                for (String line : Files.readAllLines(DRIVERS)) {
                    String[] fields = line.split("###");
                    if (fields.length == 2 && fields[0].equals(model)) {
                        driverFile = fields[1];
                        break;
                    }
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                return new CrxResponse(session, "ERROR", e.getMessage());
            }
        }
        logger.debug("Add printer/usr/sbin/lpadmin -p " + printer.getName() + " -P " + driverFile + " -o printer-error-policy=abort-job -o PageSize=A4");
        String[] program = new String[9];
        StringBuffer reply = new StringBuffer();
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
        CrxSystemCmd.exec(program, reply, stderr, null);
        logger.debug("setDriver error" + stderr.toString());
        logger.debug("setDriver reply" + reply.toString());
        //TODO check output
        return new CrxResponse(session, "OK", "Printer driver was set succesfully.");
    }

    public CrxResponse resetPrinter(Long id) {
        Printer printer = this.getById(id);
        if (printer == null) {
            return new CrxResponse(session, "ERROR", "Can not find the printer.");
        }
        return resetPrinter(printer.getName());
    }

    public CrxResponse resetPrinter(String printerName) {
        String[] program = new String[4];
        StringBuffer reply = new StringBuffer();
        StringBuffer stderr = new StringBuffer();
        program[0] = "/usr/bin/lprm";
        program[1] = "-P";
        program[2] = printerName;
        program[3] = "-";
        CrxSystemCmd.exec(program, reply, stderr, null);
        this.enablePrinter(printerName);
        return new CrxResponse(session, "OK", "Printer was reseted succesfully.");
    }

    static public Map<String, String[]> getAvailableDrivers() {
        Map<String, String[]> drivers = new HashMap<String, String[]>();
        try {
            for (String line : Files.readAllLines(PRINTERS)) {
                String[] fields = line.split("###");
                if (fields.length == 2) {
                    drivers.put(fields[0], fields[1].split("%%"));
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return drivers;
    }

    static public List<PrintersOfManufacturer> getDrivers() {
        List<PrintersOfManufacturer> printers = new ArrayList<PrintersOfManufacturer>();
        try {
            for (String line : Files.readAllLines(PRINTERS)) {
                PrintersOfManufacturer printersOfManufacturer = new PrintersOfManufacturer();
                String[] fields = line.split("###");
                if (fields.length == 2) {
                    printersOfManufacturer.setName(fields[0]);
                    printersOfManufacturer.setPrinters(fields[1].split("%%"));
                    printers.add(printersOfManufacturer);
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return printers;
    }
}
