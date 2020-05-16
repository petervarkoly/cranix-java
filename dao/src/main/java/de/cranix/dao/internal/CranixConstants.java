package de.cranix.dao.internal;

import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

public interface CranixConstants {

	static String roleTeacher  = "teachers";
	static String roleStudent  = "students";
	static String roleSysadmin = "sysadmins";
	static String roleGuest    = "guests";
	static String roleAdministratrion = "administration";
	static String roleWorkstation = "workstations";
	static String winLineSeparator = "\r\n";
	static String cranixConfDir    = "/opt/cranix-java/conf/";
	static String cranixPropFile   = "/opt/cranix-java/conf/cranix-api.properties";
	static String cranixTmpDir     = "/opt/cranix-java/tmp/";
	static String cranixBaseDir    = "/usr/share/cranix/";
	static String cranixPrinters   = cranixBaseDir + "templates/printers.txt";
	static String cranixSysConfig  = "/etc/sysconfig/cranix";
	static String cranixSysPrefix  = "CRANIX_";

	static FileAttribute<Set<PosixFilePermission>> privatDirAttribute      = PosixFilePermissions.asFileAttribute( PosixFilePermissions.fromString("rwx------"));
	static FileAttribute<Set<PosixFilePermission>> privatFileAttribute     = PosixFilePermissions.asFileAttribute( PosixFilePermissions.fromString("rw-------"));
	static FileAttribute<Set<PosixFilePermission>> groupReadFileAttribute  = PosixFilePermissions.asFileAttribute( PosixFilePermissions.fromString("rw-r-----"));
	static FileAttribute<Set<PosixFilePermission>> groupWriteFileAttribute = PosixFilePermissions.asFileAttribute( PosixFilePermissions.fromString("rw-rw----"));
	static FileAttribute<Set<PosixFilePermission>> groupReadDirAttribute   = PosixFilePermissions.asFileAttribute( PosixFilePermissions.fromString("rwxr-x---"));
	static FileAttribute<Set<PosixFilePermission>> groupWriteDireAttribute = PosixFilePermissions.asFileAttribute( PosixFilePermissions.fromString("rwxrwx---"));

	static Set<PosixFilePermission> privatDirPermission      = PosixFilePermissions.fromString("rwx------");
	static Set<PosixFilePermission> privatFilePermission     = PosixFilePermissions.fromString("rw-------");
	static Set<PosixFilePermission> groupReadFilePermission  = PosixFilePermissions.fromString("rw-r-----");
	static Set<PosixFilePermission> groupWriteFilePermission = PosixFilePermissions.fromString("rw-rw----");
	static Set<PosixFilePermission> groupReadDirPermission   = PosixFilePermissions.fromString("rwxr-x---");
	static Set<PosixFilePermission> groupWriteDirePermission = PosixFilePermissions.fromString("rwxrwx---");
	static Set<PosixFilePermission> worldReadFilePermission  = PosixFilePermissions.fromString("rw-r--r--");
	static Set<PosixFilePermission> worldWriteFilePermission = PosixFilePermissions.fromString("rw-rw-rw-");
	static Set<PosixFilePermission> worldReadDirPermission   = PosixFilePermissions.fromString("rwxr-xr-x");
	static Set<PosixFilePermission> worldWriteDirePermission = PosixFilePermissions.fromString("rwxrwxrwx");
}
