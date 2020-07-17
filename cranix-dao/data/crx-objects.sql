#TEST ONLY
#DROP DATABASE CRX;

CREATE DATABASE CRX DEFAULT CHARACTER SET utf8mb4 COLLATE = utf8mb4_unicode_ci;
USE CRX;

CREATE TABLE CrxNextID (
  id bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;
INSERT INTO CrxNextID Values(4000100);

CREATE TABLE IF NOT EXISTS Translations(
        id      BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
	lang	VARCHAR(2)   NOT NULL DEFAULT 'DE',
	string  VARCHAR(512) NOT NULL,
        value   VARCHAR(512),
	PRIMARY KEY  (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;

CREATE TABLE IF NOT EXISTS MissedTranslations(
        id      BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
	lang	VARCHAR(2)   NOT NULL DEFAULT 'DE',
	string  VARCHAR(250) NOT NULL,
	PRIMARY KEY  (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;

CREATE TABLE IF NOT EXISTS Users (
        id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
        uid          VARCHAR(32) NOT NULL,
        uuid         VARCHAR(64) DEFAULT NULL,
        role         VARCHAR(16) NOT NULL,
        surName      VARCHAR(64) NOT NULL,
        givenName    VARCHAR(64),
        birthDay     DATE    NOT NULL,
	fsQuota	     INTEGER DEFAULT 0,
	fsQuotaUsed  INTEGER DEFAULT 0,
	msQuota	     INTEGER DEFAULT 0,
	msQuotaUsed  INTEGER DEFAULT 0,
	initialPassword  VARCHAR(32) DEFAULT '',
        creator_id   BIGINT UNSIGNED DEFAULT NULL,
	FOREIGN KEY(creator_id)  REFERENCES Users(id),
        PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;
CREATE UNIQUE INDEX users_uid on Users(uid);

CREATE TABLE IF NOT EXISTS Groups (
        id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
        name        VARCHAR(32) NOT NULL,
        description VARCHAR(64) NOT NULL,
        groupType   VARCHAR(16) NOT NULL,
        owner_id    BIGINT UNSIGNED DEFAULT NULL,
	FOREIGN KEY(owner_id)  REFERENCES Users(id),
        PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;
CREATE UNIQUE INDEX groups_name on Groups(name);

CREATE TABLE IF NOT EXISTS GroupMember (
        user_id        BIGINT UNSIGNED NOT NULL,
        group_id       BIGINT UNSIGNED NOT NULL,
	FOREIGN KEY(user_id)  REFERENCES Users(id),
	FOREIGN KEY(group_id) REFERENCES Groups(id),
	PRIMARY KEY(user_id,group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;

CREATE TABLE IF NOT EXISTS Aliases (
        id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
        user_id         BIGINT UNSIGNED NOT NULL,
        alias           VARCHAR(64) NOT NULL,
        FOREIGN KEY(user_id)  REFERENCES Users(id),
        PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;

CREATE TABLE IF NOT EXISTS HWConfs (
        id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
        name          VARCHAR(32) NOT NULL,
        description   VARCHAR(64) DEFAULT "",
        deviceType    VARCHAR(16) NOT NULL,
        creator_id   BIGINT UNSIGNED DEFAULT NULL,
	FOREIGN KEY(creator_id)  REFERENCES Users(id),
        PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;

CREATE TABLE IF NOT EXISTS Partitions (
        id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
        hwconf_id    BIGINT UNSIGNED NOT NULL,
        name         VARCHAR(32) DEFAULT NULL,
        description  VARCHAR(64) DEFAULT NULL,
        OS           VARCHAR(16) DEFAULT NULL,
        joinType     VARCHAR(16) DEFAULT NULL,
        tool         VARCHAR(16) DEFAULT NULL,
        format       VARCHAR(16) DEFAULT NULL,
        creator_id   BIGINT UNSIGNED DEFAULT NULL,
	FOREIGN KEY(creator_id)  REFERENCES Users(id),
        FOREIGN KEY(hwconf_id) REFERENCES HWConfs(id),
        PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;

CREATE TABLE IF NOT EXISTS Rooms (
        id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
        hwconf_id    BIGINT UNSIGNED DEFAULT NULL,
        name         VARCHAR(32) NOT NULL,
        description  VARCHAR(64) DEFAULT "",
        roomType     VARCHAR(16) NOT NULL,
        roomControl  VARCHAR(16) NOT NULL,
        roomRows     INTEGER  DEFAULT 5,
        places       INTEGER  DEFAULT 5,
        startIP      VARCHAR(16) DEFAULT NULL,
        netMask      INTEGER  DEFAULT NULL,
        creator_id   BIGINT UNSIGNED DEFAULT NULL,
	FOREIGN KEY(creator_id)  REFERENCES Users(id),
        FOREIGN KEY(hwconf_id) REFERENCES HWConfs(id),
        PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;
CREATE UNIQUE INDEX rooms_name on Rooms(name);

CREATE TABLE IF NOT EXISTS Devices (
        id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
        room_id      BIGINT UNSIGNED NOT NULL,
        hwconf_id    BIGINT UNSIGNED DEFAULT NULL,
        owner_id     BIGINT UNSIGNED DEFAULT NULL,
        name         VARCHAR(32) NOT NULL,
        IP           VARCHAR(16) NOT NULL,
        wlanIp       VARCHAR(16) DEFAULT '',
        MAC          VARCHAR(17) DEFAULT '',
        wlanMac      VARCHAR(17) DEFAULT '',
        roomRow      INTEGER  DEFAULT 0,
        place        INTEGER  DEFAULT 0,
	serial       VARCHAR(32) DEFAULT '',
	inventary    VARCHAR(32) DEFAULT '',
	locality     VARCHAR(32) DEFAULT '',
        counter      BIGINT UNSIGNED DEFAULT NULL,
        FOREIGN KEY(room_id)   REFERENCES Rooms(id),
        FOREIGN KEY(hwconf_id) REFERENCES HWConfs(id),
        FOREIGN KEY(owner_id)  REFERENCES Users(id),
        PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;
CREATE UNIQUE INDEX device_names on Devices(name);

CREATE TABLE IF NOT EXISTS AccessInRooms (
        id             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
        room_id        BIGINT UNSIGNED NOT NULL,
        accessType     VARCHAR(3) NOT NULL,
        pointInTime    CHAR(5) DEFAULT '06:00',
	monday         CHAR(1) DEFAULT 'Y',
	tuesday        CHAR(1) DEFAULT 'Y',
	wednesday      CHAR(1) DEFAULT 'Y',
	thursday       CHAR(1) DEFAULT 'Y',
	friday         CHAR(1) DEFAULT 'Y',
	saturday       CHAR(1) DEFAULT 'N',
	sunday         CHAR(1) DEFAULT 'N',
	holiday        CHAR(1) DEFAULT 'N',
        direct          CHAR(1) DEFAULT 'N',
        login           CHAR(1) DEFAULT 'Y',
        proxy           CHAR(1) DEFAULT 'Y',
        printing        CHAR(1) DEFAULT 'Y',
        portal          CHAR(1) DEFAULT 'Y',
	action          VARCHAR(32) DEFAULT '',
        creator_id   BIGINT UNSIGNED DEFAULT NULL,
	FOREIGN KEY(creator_id)  REFERENCES Users(id),
        FOREIGN KEY(room_id) REFERENCES Rooms(id),
        PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;

CREATE TABLE IF NOT EXISTS LoggedOn (
        user_id      BIGINT UNSIGNED NOT NULL,
        device_id    BIGINT UNSIGNED NOT NULL,
        FOREIGN KEY(user_id)   REFERENCES Users(id),
        FOREIGN KEY(device_id) REFERENCES Devices(id),
        PRIMARY KEY(device_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;

CREATE TABLE IF NOT EXISTS Printers (
        id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
        device_id    BIGINT UNSIGNED NOT NULL,
        creator_id   BIGINT UNSIGNED DEFAULT NULL,
        name         VARCHAR(32) NOT NULL,
        FOREIGN KEY(device_id)  REFERENCES Devices(id),
        FOREIGN KEY(creator_id) REFERENCES Users(id),
        PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;

CREATE TABLE IF NOT EXISTS DefaultPrinter (
        id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
        room_id      BIGINT UNSIGNED DEFAULT NULL,
        device_id    BIGINT UNSIGNED DEFAULT NULL,
        printer_id   BIGINT UNSIGNED NOT NULL,
        FOREIGN KEY(room_id)    REFERENCES Rooms(id),
        FOREIGN KEY(device_id)  REFERENCES Devices(id),
        FOREIGN KEY(printer_id) REFERENCES Printers(id),
        PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;

CREATE TABLE IF NOT EXISTS AvailablePrinters (
        id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
        room_id      BIGINT UNSIGNED DEFAULT NULL,
        device_id    BIGINT UNSIGNED DEFAULT NULL,
        printer_id   BIGINT UNSIGNED NOT NULL,
        FOREIGN KEY(room_id)    REFERENCES Rooms(id),
        FOREIGN KEY(device_id)  REFERENCES Devices(id),
        FOREIGN KEY(printer_id) REFERENCES Printers(id),
        PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;

CREATE TABLE IF NOT EXISTS Acls (
        id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
        user_id      BIGINT UNSIGNED DEFAULT NULL,
        group_id     BIGINT UNSIGNED DEFAULT NULL,
        acl          VARCHAR(32) NOT NULL,
        allowed      CHAR(1) NOT NULL DEFAULT 'Y',
        creator_id   BIGINT UNSIGNED DEFAULT NULL,
        FOREIGN KEY(creator_id)  REFERENCES Users(id),
        FOREIGN KEY(user_id)  REFERENCES Users(id),
        FOREIGN KEY(group_id) REFERENCES Groups(id),
        PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;

CREATE TABLE IF NOT EXISTS  Tests (
        id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
        testName      VARCHAR(128) NOT NULL,
        teacher_id    BIGINT UNSIGNED,
        room_id       BIGINT UNSIGNED,
        testDir       VARCHAR(128) NOT NULL,
        currentStep   VARCHAR(128) NOT NULL,
        startTime     TIMESTAMP NOT NULL,
        endTime       TIMESTAMP NOT NULL,
        login         CHAR(1) NOT NULL DEFAULT 'Y',
        proxy         CHAR(1) NOT NULL DEFAULT 'N',
        direct        CHAR(1) NOT NULL DEFAULT 'N',
        portal        CHAR(1) NOT NULL DEFAULT 'N',
        FOREIGN KEY(teacher_id) REFERENCES Users(id),
        FOREIGN KEY(room_id)    REFERENCES Rooms(id),
        PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;

CREATE TABLE IF NOT EXISTS TestFiles (
        id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
        test_id      BIGINT UNSIGNED NOT NULL,
        user_id      BIGINT UNSIGNED,
        getOrPost    VARCHAR(128) NOT NULL,
        fileName     VARCHAR(256) NOT NULL,
        dateTime     TIMESTAMP NOT NULL,
        FOREIGN KEY(test_id) REFERENCES Tests(id),
        FOREIGN KEY(user_id) REFERENCES Users(id),
        PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;

CREATE TABLE IF NOT EXISTS TestUsers (
        id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
        test_id      BIGINT UNSIGNED NOT NULL,
        user_id      BIGINT UNSIGNED,
        device_id    BIGINT UNSIGNED,
        FOREIGN KEY(test_id) REFERENCES   Tests(id),
        FOREIGN KEY(user_id) REFERENCES   Users(id),
        FOREIGN KEY(device_id) REFERENCES Devices(id),
        PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;

CREATE TABLE IF NOT EXISTS Enumerates (
        id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
        name         VARCHAR(32)  NOT NULL,
        value        VARCHAR(32)  NOT NULL,
        creator_id   BIGINT UNSIGNED DEFAULT NULL,
	FOREIGN KEY(creator_id)  REFERENCES Users(id),
        PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;

#Some additional config tables
CREATE TABLE IF NOT EXISTS CrxConfig (
	id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
	objectType   VARCHAR(12)     NOT NULL,
        objectId     BIGINT UNSIGNED NOT NULL,
        keyword      VARCHAR(64) NOT NULL,
        value        VARCHAR(128) NOT NULL,
        creator_id   BIGINT UNSIGNED DEFAULT NULL,
	FOREIGN KEY(creator_id)  REFERENCES Users(id),
        PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;

CREATE TABLE IF NOT EXISTS CrxMConfig (
	id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
	objectType   VARCHAR(12)     NOT NULL,
        objectId     BIGINT UNSIGNED NOT NULL,
        keyword      VARCHAR(64) NOT NULL,
        value        VARCHAR(128) NOT NULL,
        creator_id   BIGINT UNSIGNED DEFAULT NULL,
	FOREIGN KEY(creator_id)  REFERENCES Users(id),
        PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;

CREATE TABLE IF NOT EXISTS RoomSmartControlls (
	id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
        room_id      BIGINT UNSIGNED NOT NULL,
        user_id      BIGINT UNSIGNED NOT NULL,
        startTime    TIMESTAMP NOT NULL,
        endTime      TIMESTAMP NOT NULL,
        FOREIGN KEY(room_id) REFERENCES   Rooms(id),
        FOREIGN KEY(user_id) REFERENCES   Users(id),
        PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;

CREATE TABLE IF NOT EXISTS Sessions (
        id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
        user_id      BIGINT UNSIGNED NOT NULL,
        room_id      BIGINT UNSIGNED DEFAULT NULL,
        device_id    BIGINT UNSIGNED DEFAULT NULL,
        createdate   TIMESTAMP NOT NULL,
        ip           VARCHAR(30),
        token        VARCHAR(60),
        FOREIGN KEY(user_id)   REFERENCES Users(id),
        FOREIGN KEY(room_id)   REFERENCES Rooms(id),
	FOREIGN KEY(device_id) REFERENCES Devices(id),
        PRIMARY KEY(id)
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;

CREATE TABLE IF NOT EXISTS CrxResponses (
        id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
	session_id   BIGINT UNSIGNED NOT NULL,
	code	VARCHAR(64) NOT NULL,
	value   VARCHAR(1024) NOT NULL,
	FOREIGN KEY(session_id) REFERENCES Sessions(id),
	PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;

CREATE TABLE IF NOT EXISTS Softwares (
        id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
	name        VARCHAR(128) NOT NULL,
	description VARCHAR(128) DEFAULT NULL,
	weight      INTEGER DEFAULT 0,
	manually    CHAR(1) DEFAULT 'N',
        creator_id   BIGINT UNSIGNED DEFAULT NULL,
	FOREIGN KEY(creator_id)  REFERENCES Users(id),
        PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;

CREATE TABLE IF NOT EXISTS SoftwareRequirements (
        software_id		    BIGINT UNSIGNED,
        requirement_id		    BIGINT UNSIGNED,
        FOREIGN KEY(software_id)    REFERENCES Softwares(id),
        FOREIGN KEY(requirement_id) REFERENCES Softwares(id),
	PRIMARY KEY(software_id,requirement_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;

CREATE TABLE IF NOT EXISTS SoftwareFullNames (
        id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
        software_id    BIGINT UNSIGNED,
	fullName       VARCHAR(128) NOT NULL,
        FOREIGN KEY(software_id)    REFERENCES Softwares(id),
        PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;

# status C -> current this is the most recent version and does exists on the server and can be installed
# status R -> replaced this version does not exists on the server but is installed on some clients
# status D -> this is an older version which does exists on the server and can be installed
CREATE TABLE IF NOT EXISTS SoftwareVersions (
        id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
        software_id    BIGINT UNSIGNED,
	version        VARCHAR(128) NOT NULL,
	status         VARCHAR(1)   NOT NULL,
        FOREIGN KEY(software_id)    REFERENCES Softwares(id),
        PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;

# status I  -> installed
# status IS -> installation scheduled
# status US -> update scheduled
# status MI -> manually installed
# status MD -> manually deinstalled
# status DS -> deinstallation scheduled
# status DF -> deinstallation failed
# status IF -> installation failed
# status FR -> installed version is frozen: This must not be updated.
CREATE TABLE IF NOT EXISTS SoftwareStatus (
        id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
        softwareversion_id BIGINT UNSIGNED NOT NULL,
        device_id          BIGINT UNSIGNED NOT NULL,
	status             VARCHAR(2) NOT NULL,
	FOREIGN KEY(softwareversion_id) REFERENCES SoftwareVersions(id),
	FOREIGN KEY(device_id)          REFERENCES Devices(id),
	PRIMARY KEY(id),
	UNIQUE KEY sv_d (softwareversion_id,device_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;

# licenseType C -> command line license
# licenseType F -> file license in this case the value contains the license file name
CREATE TABLE IF NOT EXISTS SoftwareLicenses (
        id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
        software_id    BIGINT UNSIGNED,
	licenseType    CHAR(1) DEFAULT 'C',
	count          INTEGER DEFAULT 1,
	value          VARCHAR(1024) NOT NULL,
        creator_id   BIGINT UNSIGNED DEFAULT NULL,
	FOREIGN KEY(creator_id)  REFERENCES Users(id),
        FOREIGN KEY(software_id) REFERENCES Softwares(id),
        PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;

CREATE TABLE IF NOT EXISTS LicenseToDevice (
        license_id         BIGINT UNSIGNED NOT NULL,
        device_id          BIGINT UNSIGNED NOT NULL,
	FOREIGN KEY(license_id)  REFERENCES SoftwareLicenses(id),
	FOREIGN KEY(device_id)   REFERENCES Devices(id),
	PRIMARY KEY(license_id,device_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;

CREATE TABLE IF NOT EXISTS PositiveLists (
	id BIGINT   UNSIGNED NOT NULL AUTO_INCREMENT,
        owner_id    BIGINT UNSIGNED DEFAULT NULL,
        name        VARCHAR(32) NOT NULL,
        subject     VARCHAR(32) NOT NULL,
        description VARCHAR(64) NOT NULL,
	FOREIGN KEY(owner_id)  REFERENCES Users(id),
        PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;

CREATE TABLE IF NOT EXISTS Announcements (
	id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
        owner_id   BIGINT UNSIGNED DEFAULT NULL,
        validFrom  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        validUntil TIMESTAMP,
        keywords   VARCHAR(128) NOT NULL,
        title      VARCHAR(128) NOT NULL,
        text       MEDIUMTEXT,
        issue      VARCHAR(128),
        FOREIGN KEY(owner_id)  REFERENCES Users(id),
        PRIMARY KEY  (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;

CREATE TABLE IF NOT EXISTS HaveSeen (
        user_id            BIGINT UNSIGNED DEFAULT NULL,
        announcement_id    BIGINT UNSIGNED DEFAULT NULL,
	FOREIGN KEY(user_id)           REFERENCES Users(id),
	FOREIGN KEY(announcement_id)   REFERENCES Announcements(id),
	PRIMARY KEY(announcement_id,user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;

CREATE TABLE IF NOT EXISTS FAQs (
	id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
        owner_id   BIGINT UNSIGNED DEFAULT NULL,
        issue      VARCHAR(128) default NULL,
        title      VARCHAR(128) NOT NULL,
        text       MEDIUMTEXT,
        FOREIGN KEY(owner_id)  REFERENCES Users(id),
        PRIMARY KEY  (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;

CREATE TABLE IF NOT EXISTS Contacts (
	id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
        owner_id   BIGINT UNSIGNED DEFAULT NULL,
        issue      VARCHAR(128) default NULL,
        name       VARCHAR(128) default NULL,
        phone      VARCHAR(128) default NULL,
        email      VARCHAR(128) default NULL,
        title      VARCHAR(128) default NULL,
        FOREIGN KEY(owner_id)  REFERENCES Users(id),
        PRIMARY KEY  (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;

CREATE TABLE IF NOT EXISTS Categories (
        id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
	name         VARCHAR(32) NOT NULL,
	description  VARCHAR(64) DEFAULT NULL,
	categoryType VARCHAR(16) DEFAULT NULL,
        owner_id     BIGINT UNSIGNED DEFAULT NULL,
	studentsOnly CHAR(1) DEFAULT 'N',
	publicAccess CHAR(1) DEFAULT 'Y',
        validFrom    DATE    DEFAULT NULL,
        validUntil   DATE    DEFAULT NULL,
        FOREIGN KEY(owner_id)  REFERENCES Users(id),
        PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;

CREATE TABLE IF NOT EXISTS DeviceInCategories (
        device_id          BIGINT UNSIGNED NOT NULL,
        category_id        BIGINT UNSIGNED NOT NULL,
	FOREIGN KEY(device_id)    REFERENCES Devices(id),
	FOREIGN KEY(category_id)  REFERENCES Categories(id),
	PRIMARY KEY(device_id,category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;

CREATE TABLE IF NOT EXISTS GroupInCategories (
        group_id           BIGINT UNSIGNED NOT NULL,
        category_id        BIGINT UNSIGNED NOT NULL,
	FOREIGN KEY(group_id)     REFERENCES Groups(id),	
	FOREIGN KEY(category_id)  REFERENCES Categories(id),
	PRIMARY KEY(group_id,category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;

CREATE TABLE IF NOT EXISTS HWConfInCategories (
        hwconf_id          BIGINT UNSIGNED NOT NULL,
        category_id        BIGINT UNSIGNED NOT NULL,
	FOREIGN KEY(hwconf_id)    REFERENCES HWConfs(id),
	FOREIGN KEY(category_id)  REFERENCES Categories(id),
	PRIMARY KEY(hwconf_id,category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;

CREATE TABLE IF NOT EXISTS RoomInCategories (
        room_id            BIGINT UNSIGNED NOT NULL,
        category_id        BIGINT UNSIGNED NOT NULL,
	FOREIGN KEY(room_id)      REFERENCES Rooms(id),
	FOREIGN KEY(category_id)  REFERENCES Categories(id),
	PRIMARY KEY(room_id,category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;

CREATE TABLE IF NOT EXISTS SoftwareInCategories (
        software_id        BIGINT UNSIGNED NOT NULL,
        category_id        BIGINT UNSIGNED NOT NULL,
	FOREIGN KEY(software_id)  REFERENCES Softwares(id),
	FOREIGN KEY(category_id)  REFERENCES Categories(id),
	PRIMARY KEY(software_id,category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;

CREATE TABLE IF NOT EXISTS SoftwareRemovedFromCategories (
        software_id        BIGINT UNSIGNED NOT NULL,
        category_id        BIGINT UNSIGNED NOT NULL,
	FOREIGN KEY(software_id)  REFERENCES Softwares(id),
	FOREIGN KEY(category_id)  REFERENCES Categories(id),
	PRIMARY KEY(software_id,category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;

CREATE TABLE IF NOT EXISTS UserInCategories (
        user_id            BIGINT UNSIGNED NOT NULL,
        category_id        BIGINT UNSIGNED NOT NULL,
	FOREIGN KEY(user_id)      REFERENCES Users(id),
	FOREIGN KEY(category_id)  REFERENCES Categories(id),
	PRIMARY KEY(user_id,category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;

CREATE TABLE IF NOT EXISTS AnnouncementInCategories (
        announcement_id    BIGINT UNSIGNED NOT NULL,
        category_id        BIGINT UNSIGNED NOT NULL,
	FOREIGN KEY(announcement_id) REFERENCES Announcements(id),
	FOREIGN KEY(category_id)     REFERENCES Categories(id),
	PRIMARY KEY(announcement_id,category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;

CREATE TABLE IF NOT EXISTS FAQInCategories (
        faq_id    BIGINT UNSIGNED NOT NULL,
        category_id        BIGINT UNSIGNED NOT NULL,
	FOREIGN KEY(faq_id) REFERENCES FAQs(id),
	FOREIGN KEY(category_id)     REFERENCES Categories(id),
	PRIMARY KEY(faq_id,category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;

CREATE TABLE IF NOT EXISTS ContactInCategories (
        contact_id               BIGINT UNSIGNED NOT NULL,
        category_id              BIGINT UNSIGNED NOT NULL,
	FOREIGN KEY(contact_id)  REFERENCES Contacts(id),
	FOREIGN KEY(category_id) REFERENCES Categories(id),
	PRIMARY KEY(contact_id,category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;

CREATE TABLE IF NOT EXISTS Jobs (
       id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
       description     VARCHAR(128)    NOT NULL,
       startTime       timestamp       NOT NULL DEFAULT CURRENT_TIMESTAMP,
       endTime         timestamp       DEFAULT '0000-00-00 00:00:00',
       exitCode        INT             DEFAULT NULL,
       PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;

CREATE TABLE IF NOT EXISTS Tickets (
        id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
        user_id         BIGINT UNSIGNED NOT NULL,
        uuid            VARCHAR(64) NOT NULL,
        title           VARCHAR(255) DEFAULT NULL,
	done            CHAR(1) DEFAULT 'N',
        FOREIGN KEY(user_id)  REFERENCES Users(id),
        PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci ;

