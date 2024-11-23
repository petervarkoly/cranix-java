/*M!999999\- enable the sandbox mode */ 
-- MariaDB dump 10.19  Distrib 10.11.9-MariaDB, for Linux (x86_64)
--
-- Host: localhost    Database: CRX
-- ------------------------------------------------------
-- Server version	10.11.9-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `CRX`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `CRX` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_520_ci */;

USE `CRX`;

--
-- Table structure for table `AccessInRooms`
--

DROP TABLE IF EXISTS `AccessInRooms`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `AccessInRooms` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `accessType` varchar(255) DEFAULT NULL,
  `action` varchar(255) DEFAULT NULL,
  `created` timestamp NULL DEFAULT current_timestamp(),
  `direct` char(1) DEFAULT 'N',
  `friday` char(1) DEFAULT 'Y',
  `holiday` char(1) DEFAULT 'N',
  `login` char(1) DEFAULT 'Y',
  `modified` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `monday` char(1) DEFAULT 'Y',
  `pointInTime` char(5) DEFAULT '06:00',
  `portal` char(1) DEFAULT 'Y',
  `printing` char(1) DEFAULT 'Y',
  `proxy` char(1) DEFAULT 'Y',
  `saturday` char(1) DEFAULT 'N',
  `sunday` char(1) DEFAULT 'N',
  `thursday` char(1) DEFAULT 'Y',
  `tuesday` char(1) DEFAULT 'Y',
  `wednesday` char(1) DEFAULT 'Y',
  `creator_id` bigint(20) unsigned DEFAULT NULL,
  `room_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_AccessInRooms_room_id` (`room_id`),
  KEY `FK_AccessInRooms_creator_id` (`creator_id`),
  CONSTRAINT `FK_AccessInRooms_creator_id` FOREIGN KEY (`creator_id`) REFERENCES `Users` (`id`),
  CONSTRAINT `FK_AccessInRooms_room_id` FOREIGN KEY (`room_id`) REFERENCES `Rooms` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Acls`
--

DROP TABLE IF EXISTS `Acls`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Acls` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) unsigned DEFAULT NULL,
  `group_id` bigint(20) unsigned DEFAULT NULL,
  `acl` varchar(32) NOT NULL,
  `allowed` char(1) NOT NULL DEFAULT 'Y',
  `creator_id` bigint(20) unsigned DEFAULT NULL,
  `created` timestamp NULL DEFAULT current_timestamp(),
  `modified` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `UNQ_Acls_0` (`acl`,`group_id`,`user_id`),
  KEY `FK_Acls_creator_id` (`creator_id`),
  KEY `FK_Acls_group_id` (`group_id`),
  KEY `FK_Acls_user_id` (`user_id`),
  CONSTRAINT `FK_Acls_creator_id` FOREIGN KEY (`creator_id`) REFERENCES `Users` (`id`),
  CONSTRAINT `FK_Acls_group_id` FOREIGN KEY (`group_id`) REFERENCES `Groups` (`id`),
  CONSTRAINT `FK_Acls_user_id` FOREIGN KEY (`user_id`) REFERENCES `Users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=110 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Aliases`
--

DROP TABLE IF EXISTS `Aliases`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Aliases` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `alias` varchar(255) DEFAULT NULL,
  `created` timestamp NULL DEFAULT current_timestamp(),
  `modified` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `creator_id` bigint(20) unsigned DEFAULT NULL,
  `user_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UNQ_Aliases_0` (`user_id`,`alias`),
  KEY `FK_Aliases_creator_id` (`creator_id`),
  CONSTRAINT `FK_Aliases_creator_id` FOREIGN KEY (`creator_id`) REFERENCES `Users` (`id`),
  CONSTRAINT `FK_Aliases_user_id` FOREIGN KEY (`user_id`) REFERENCES `Users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `AnnouncementInCategories`
--

DROP TABLE IF EXISTS `AnnouncementInCategories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `AnnouncementInCategories` (
  `announcement_id` bigint(20) unsigned NOT NULL,
  `category_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`announcement_id`,`category_id`),
  KEY `FK_AnnouncementInCategories_category_id` (`category_id`),
  CONSTRAINT `FK_AnnouncementInCategories_announcement_id` FOREIGN KEY (`announcement_id`) REFERENCES `Announcements` (`id`),
  CONSTRAINT `FK_AnnouncementInCategories_category_id` FOREIGN KEY (`category_id`) REFERENCES `Categories` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Announcements`
--

DROP TABLE IF EXISTS `Announcements`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Announcements` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `created` timestamp NULL DEFAULT current_timestamp(),
  `issue` varchar(255) DEFAULT NULL,
  `keywords` varchar(255) DEFAULT NULL,
  `modified` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `text` mediumtext DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `validFrom` datetime DEFAULT NULL,
  `validUntil` datetime DEFAULT NULL,
  `creator_id` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_Announcements_creator_id` (`creator_id`),
  CONSTRAINT `FK_Announcements_creator_id` FOREIGN KEY (`creator_id`) REFERENCES `Users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `AvailablePrinters`
--

DROP TABLE IF EXISTS `AvailablePrinters`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `AvailablePrinters` (
  `device_id` bigint(20) unsigned NOT NULL,
  `printer_id` bigint(20) unsigned NOT NULL,
  `room_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`device_id`,`printer_id`,`room_id`),
  KEY `FK_AvailablePrinters_room_id` (`room_id`),
  KEY `FK_AvailablePrinters_printer_id` (`printer_id`),
  CONSTRAINT `FK_AvailablePrinters_device_id` FOREIGN KEY (`device_id`) REFERENCES `Devices` (`id`),
  CONSTRAINT `FK_AvailablePrinters_printer_id` FOREIGN KEY (`printer_id`) REFERENCES `Printers` (`id`),
  CONSTRAINT `FK_AvailablePrinters_room_id` FOREIGN KEY (`room_id`) REFERENCES `Rooms` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Categories`
--

DROP TABLE IF EXISTS `Categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Categories` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `categoryType` varchar(64) DEFAULT NULL,
  `created` timestamp NULL DEFAULT current_timestamp(),
  `description` varchar(64) DEFAULT NULL,
  `modified` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `name` varchar(32) DEFAULT NULL,
  `publicAccess` char(1) DEFAULT 'Y',
  `studentsOnly` char(1) DEFAULT 'Y',
  `validFrom` datetime DEFAULT NULL,
  `validUntil` datetime DEFAULT NULL,
  `creator_id` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UNQ_Categories_0` (`name`,`categoryType`),
  KEY `FK_Categories_creator_id` (`creator_id`),
  CONSTRAINT `FK_Categories_creator_id` FOREIGN KEY (`creator_id`) REFERENCES `Users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ChallengesInArea`
--

DROP TABLE IF EXISTS `ChallengesInArea`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ChallengesInArea` (
  `crxchallenge_id` bigint(20) unsigned NOT NULL,
  `subjectarea_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`crxchallenge_id`,`subjectarea_id`),
  KEY `FK_ChallengesInArea_subjectarea_id` (`subjectarea_id`),
  CONSTRAINT `FK_ChallengesInArea_crxchallenge_id` FOREIGN KEY (`crxchallenge_id`) REFERENCES `CrxChallenges` (`id`),
  CONSTRAINT `FK_ChallengesInArea_subjectarea_id` FOREIGN KEY (`subjectarea_id`) REFERENCES `SubjectAreas` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ContactInCategories`
--

DROP TABLE IF EXISTS `ContactInCategories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ContactInCategories` (
  `category_id` bigint(20) unsigned NOT NULL,
  `contact_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`category_id`,`contact_id`),
  KEY `FK_ContactInCategories_contact_id` (`contact_id`),
  CONSTRAINT `FK_ContactInCategories_category_id` FOREIGN KEY (`category_id`) REFERENCES `Categories` (`id`),
  CONSTRAINT `FK_ContactInCategories_contact_id` FOREIGN KEY (`contact_id`) REFERENCES `Contacts` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Contacts`
--

DROP TABLE IF EXISTS `Contacts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Contacts` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `created` timestamp NULL DEFAULT current_timestamp(),
  `email` varchar(255) DEFAULT NULL,
  `issue` varchar(255) DEFAULT NULL,
  `modified` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `name` varchar(255) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `creator_id` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_Contacts_creator_id` (`creator_id`),
  CONSTRAINT `FK_Contacts_creator_id` FOREIGN KEY (`creator_id`) REFERENCES `Users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Crx2faSessions`
--

DROP TABLE IF EXISTS `Crx2faSessions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Crx2faSessions` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `checked` varchar(1) DEFAULT NULL,
  `clientIP` varchar(128) DEFAULT NULL,
  `created` timestamp NULL DEFAULT current_timestamp(),
  `modified` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `pin` varchar(6) DEFAULT NULL,
  `validHours` int(11) DEFAULT NULL,
  `creator_id` bigint(20) unsigned DEFAULT NULL,
  `crx2fa_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_Crx2faSessions_crx2fa_id` (`crx2fa_id`),
  KEY `FK_Crx2faSessions_creator_id` (`creator_id`),
  CONSTRAINT `FK_Crx2faSessions_creator_id` FOREIGN KEY (`creator_id`) REFERENCES `Users` (`id`),
  CONSTRAINT `FK_Crx2faSessions_crx2fa_id` FOREIGN KEY (`crx2fa_id`) REFERENCES `Crx2fas` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Crx2fas`
--

DROP TABLE IF EXISTS `Crx2fas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Crx2fas` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `created` timestamp NULL DEFAULT current_timestamp(),
  `address` varchar(4000) DEFAULT NULL,
  `crx2faType` varchar(5) DEFAULT NULL,
  `modified` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `serial` varchar(40) DEFAULT NULL,
  `timeStep` int(11) DEFAULT NULL,
  `validHours` int(11) DEFAULT NULL,
  `creator_id` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UNQ_Crx2fas_0` (`creator_id`,`crx2faType`),
  CONSTRAINT `FK_Crx2fas_creator_id` FOREIGN KEY (`creator_id`) REFERENCES `Users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `CrxCalendar`
--

DROP TABLE IF EXISTS `CrxCalendar`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CrxCalendar` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `allDay` char(1) DEFAULT 'Y',
  `created` timestamp NULL DEFAULT current_timestamp(),
  `description` varchar(255) DEFAULT NULL,
  `end` timestamp NULL DEFAULT current_timestamp(),
  `location` varchar(255) DEFAULT NULL,
  `modified` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `rruleFreq` varchar(255) DEFAULT NULL,
  `rruleInterval` varchar(255) DEFAULT NULL,
  `rruleUntil` timestamp NULL DEFAULT NULL,
  `start` timestamp NULL DEFAULT current_timestamp(),
  `title` varchar(255) DEFAULT NULL,
  `uuid` varchar(255) DEFAULT NULL,
  `creator_id` bigint(20) unsigned DEFAULT NULL,
  `rrule` varchar(300) DEFAULT NULL,
  `duration` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UNQ_CrxCalendar_0` (`uuid`),
  KEY `FK_CrxCalendar_creator_id` (`creator_id`),
  CONSTRAINT `FK_CrxCalendar_creator_id` FOREIGN KEY (`creator_id`) REFERENCES `Users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `CrxChallengeAnswers`
--

DROP TABLE IF EXISTS `CrxChallengeAnswers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CrxChallengeAnswers` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `correct` varchar(1) DEFAULT NULL,
  `created` timestamp NULL DEFAULT current_timestamp(),
  `modified` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `creator_id` bigint(20) unsigned DEFAULT NULL,
  `crxquestionanswer_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UNQ_CrxChallengeAnswers_0` (`creator_id`,`crxquestionanswer_id`),
  KEY `FK_CrxChallengeAnswers_crxquestionanswer_id` (`crxquestionanswer_id`),
  CONSTRAINT `FK_CrxChallengeAnswers_creator_id` FOREIGN KEY (`creator_id`) REFERENCES `Users` (`id`),
  CONSTRAINT `FK_CrxChallengeAnswers_crxquestionanswer_id` FOREIGN KEY (`crxquestionanswer_id`) REFERENCES `CrxQuestionAnswers` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `CrxChallenges`
--

DROP TABLE IF EXISTS `CrxChallenges`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CrxChallenges` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `created` timestamp NULL DEFAULT current_timestamp(),
  `description` longtext DEFAULT NULL,
  `modified` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `released` varchar(1) DEFAULT NULL,
  `studentsOnly` varchar(1) DEFAULT NULL,
  `value` int(11) DEFAULT NULL,
  `creator_id` bigint(20) unsigned DEFAULT NULL,
  `teachingsubject_id` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_CrxChallenges_teachingsubject_id` (`teachingsubject_id`),
  KEY `FK_CrxChallenges_creator_id` (`creator_id`),
  CONSTRAINT `FK_CrxChallenges_creator_id` FOREIGN KEY (`creator_id`) REFERENCES `Users` (`id`),
  CONSTRAINT `FK_CrxChallenges_teachingsubject_id` FOREIGN KEY (`teachingsubject_id`) REFERENCES `TeachingSubjects` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `CrxConfig`
--

DROP TABLE IF EXISTS `CrxConfig`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CrxConfig` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `created` timestamp NULL DEFAULT current_timestamp(),
  `keyword` varchar(255) DEFAULT NULL,
  `modified` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `objectId` bigint(20) DEFAULT NULL,
  `objectType` varchar(255) DEFAULT NULL,
  `value` varchar(255) DEFAULT NULL,
  `creator_id` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UNQ_CrxConfig_0` (`objectType`,`objectId`,`keyword`,`value`),
  KEY `FK_CrxConfig_creator_id` (`creator_id`),
  CONSTRAINT `FK_CrxConfig_creator_id` FOREIGN KEY (`creator_id`) REFERENCES `Users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `CrxMConfig`
--

DROP TABLE IF EXISTS `CrxMConfig`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CrxMConfig` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `created` timestamp NULL DEFAULT current_timestamp(),
  `keyword` varchar(255) DEFAULT NULL,
  `modified` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `objectId` bigint(20) DEFAULT NULL,
  `objectType` varchar(255) DEFAULT NULL,
  `value` varchar(255) DEFAULT NULL,
  `creator_id` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UNQ_CrxMConfig_0` (`objectType`,`objectId`,`keyword`,`value`),
  KEY `FK_CrxMConfig_creator_id` (`creator_id`),
  CONSTRAINT `FK_CrxMConfig_creator_id` FOREIGN KEY (`creator_id`) REFERENCES `Users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `CrxNextID`
--

DROP TABLE IF EXISTS `CrxNextID`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CrxNextID` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `recTime` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4000107 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `CrxQuestionAnswers`
--

DROP TABLE IF EXISTS `CrxQuestionAnswers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CrxQuestionAnswers` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `answer` longtext DEFAULT NULL,
  `correct` varchar(1) DEFAULT NULL,
  `created` timestamp NULL DEFAULT current_timestamp(),
  `modified` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `creator_id` bigint(20) unsigned DEFAULT NULL,
  `crxquestion_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_CrxQuestionAnswers_crxquestion_id` (`crxquestion_id`),
  KEY `FK_CrxQuestionAnswers_creator_id` (`creator_id`),
  CONSTRAINT `FK_CrxQuestionAnswers_creator_id` FOREIGN KEY (`creator_id`) REFERENCES `Users` (`id`),
  CONSTRAINT `FK_CrxQuestionAnswers_crxquestion_id` FOREIGN KEY (`crxquestion_id`) REFERENCES `CrxQuestions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `CrxQuestions`
--

DROP TABLE IF EXISTS `CrxQuestions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CrxQuestions` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `answerType` int(11) DEFAULT NULL,
  `created` timestamp NULL DEFAULT current_timestamp(),
  `modified` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `question` longtext DEFAULT NULL,
  `value` int(11) DEFAULT NULL,
  `crxchallenge_id` bigint(20) unsigned DEFAULT NULL,
  `creator_id` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_CrxQuestions_crxchallenge_id` (`crxchallenge_id`),
  KEY `FK_CrxQuestions_creator_id` (`creator_id`),
  CONSTRAINT `FK_CrxQuestions_creator_id` FOREIGN KEY (`creator_id`) REFERENCES `Users` (`id`),
  CONSTRAINT `FK_CrxQuestions_crxchallenge_id` FOREIGN KEY (`crxchallenge_id`) REFERENCES `CrxChallenges` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DefaultPrinter`
--

DROP TABLE IF EXISTS `DefaultPrinter`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DefaultPrinter` (
  `device_id` bigint(20) unsigned NOT NULL,
  `printer_id` bigint(20) unsigned NOT NULL,
  `room_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`device_id`,`printer_id`,`room_id`),
  KEY `FK_DefaultPrinter_printer_id` (`printer_id`),
  KEY `FK_DefaultPrinter_room_id` (`room_id`),
  CONSTRAINT `FK_DefaultPrinter_device_id` FOREIGN KEY (`device_id`) REFERENCES `Devices` (`id`),
  CONSTRAINT `FK_DefaultPrinter_printer_id` FOREIGN KEY (`printer_id`) REFERENCES `Printers` (`id`),
  CONSTRAINT `FK_DefaultPrinter_room_id` FOREIGN KEY (`room_id`) REFERENCES `Rooms` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `DeviceInCategories`
--

DROP TABLE IF EXISTS `DeviceInCategories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DeviceInCategories` (
  `device_id` bigint(20) unsigned NOT NULL,
  `category_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`device_id`,`category_id`),
  KEY `FK_DeviceInCategories_category_id` (`category_id`),
  CONSTRAINT `FK_DeviceInCategories_category_id` FOREIGN KEY (`category_id`) REFERENCES `Categories` (`id`),
  CONSTRAINT `FK_DeviceInCategories_device_id` FOREIGN KEY (`device_id`) REFERENCES `Devices` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Devices`
--

DROP TABLE IF EXISTS `Devices`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Devices` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `counter` bigint(20) DEFAULT NULL,
  `created` timestamp NULL DEFAULT current_timestamp(),
  `inventary` varchar(32) DEFAULT NULL,
  `IP` varchar(16) DEFAULT NULL,
  `locality` varchar(32) DEFAULT NULL,
  `MAC` varchar(17) DEFAULT NULL,
  `modified` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `name` varchar(32) DEFAULT NULL,
  `place` int(11) DEFAULT NULL,
  `roomRow` int(11) DEFAULT NULL,
  `serial` varchar(32) DEFAULT NULL,
  `wlanIp` varchar(16) DEFAULT NULL,
  `wlanMac` varchar(17) DEFAULT NULL,
  `creator_id` bigint(20) unsigned DEFAULT NULL,
  `hwconf_id` bigint(20) unsigned DEFAULT NULL,
  `room_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UNQ_Devices_0` (`name`),
  UNIQUE KEY `UNQ_Devices_1` (`IP`),
  KEY `FK_Devices_creator_id` (`creator_id`),
  KEY `FK_Devices_hwconf_id` (`hwconf_id`),
  KEY `FK_Devices_room_id` (`room_id`),
  CONSTRAINT `FK_Devices_creator_id` FOREIGN KEY (`creator_id`) REFERENCES `Users` (`id`),
  CONSTRAINT `FK_Devices_hwconf_id` FOREIGN KEY (`hwconf_id`) REFERENCES `HWConfs` (`id`),
  CONSTRAINT `FK_Devices_room_id` FOREIGN KEY (`room_id`) REFERENCES `Rooms` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Enumerates`
--

DROP TABLE IF EXISTS `Enumerates`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Enumerates` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `created` timestamp NULL DEFAULT current_timestamp(),
  `modified` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `name` varchar(255) DEFAULT NULL,
  `value` varchar(255) DEFAULT NULL,
  `creator_id` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UNQ_Enumerates_0` (`name`,`value`),
  KEY `FK_Enumerates_creator_id` (`creator_id`),
  CONSTRAINT `FK_Enumerates_creator_id` FOREIGN KEY (`creator_id`) REFERENCES `Users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=138 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `FAQInCategories`
--

DROP TABLE IF EXISTS `FAQInCategories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `FAQInCategories` (
  `category_id` bigint(20) unsigned NOT NULL,
  `faq_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`category_id`,`faq_id`),
  KEY `FK_FAQInCategories_faq_id` (`faq_id`),
  CONSTRAINT `FK_FAQInCategories_category_id` FOREIGN KEY (`category_id`) REFERENCES `Categories` (`id`),
  CONSTRAINT `FK_FAQInCategories_faq_id` FOREIGN KEY (`faq_id`) REFERENCES `FAQs` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `FAQs`
--

DROP TABLE IF EXISTS `FAQs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `FAQs` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `created` timestamp NULL DEFAULT current_timestamp(),
  `issue` varchar(255) DEFAULT NULL,
  `modified` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `text` mediumtext DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `creator_id` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_FAQs_creator_id` (`creator_id`),
  CONSTRAINT `FK_FAQs_creator_id` FOREIGN KEY (`creator_id`) REFERENCES `Users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `GroupEvents`
--

DROP TABLE IF EXISTS `GroupEvents`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `GroupEvents` (
  `group_id` bigint(20) unsigned NOT NULL,
  `event_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`group_id`,`event_id`),
  KEY `FK_GroupEvents_event_id` (`event_id`),
  CONSTRAINT `FK_GroupEvents_event_id` FOREIGN KEY (`event_id`) REFERENCES `CrxCalendar` (`id`),
  CONSTRAINT `FK_GroupEvents_group_id` FOREIGN KEY (`group_id`) REFERENCES `Groups` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `GroupInCategories`
--

DROP TABLE IF EXISTS `GroupInCategories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `GroupInCategories` (
  `group_id` bigint(20) unsigned NOT NULL,
  `category_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`group_id`,`category_id`),
  KEY `FK_GroupInCategories_category_id` (`category_id`),
  CONSTRAINT `FK_GroupInCategories_category_id` FOREIGN KEY (`category_id`) REFERENCES `Categories` (`id`),
  CONSTRAINT `FK_GroupInCategories_group_id` FOREIGN KEY (`group_id`) REFERENCES `Groups` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `GroupMember`
--

DROP TABLE IF EXISTS `GroupMember`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `GroupMember` (
  `group_id` bigint(20) unsigned NOT NULL,
  `user_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`group_id`,`user_id`),
  KEY `FK_GroupMember_user_id` (`user_id`),
  CONSTRAINT `FK_GroupMember_group_id` FOREIGN KEY (`group_id`) REFERENCES `Groups` (`id`),
  CONSTRAINT `FK_GroupMember_user_id` FOREIGN KEY (`user_id`) REFERENCES `Users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Groups`
--

DROP TABLE IF EXISTS `Groups`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Groups` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `created` timestamp NULL DEFAULT current_timestamp(),
  `description` varchar(255) DEFAULT NULL,
  `groupType` varchar(255) DEFAULT NULL,
  `modified` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `name` varchar(255) DEFAULT NULL,
  `creator_id` bigint(20) unsigned DEFAULT NULL,
  `color` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UNQ_Groups_0` (`name`),
  KEY `FK_Groups_creator_id` (`creator_id`),
  CONSTRAINT `FK_Groups_creator_id` FOREIGN KEY (`creator_id`) REFERENCES `Users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `GroupsOfChallenges`
--

DROP TABLE IF EXISTS `GroupsOfChallenges`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `GroupsOfChallenges` (
  `crxchallenge_id` bigint(20) unsigned NOT NULL,
  `group_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`crxchallenge_id`,`group_id`),
  KEY `FK_GroupsOfChallenges_group_id` (`group_id`),
  CONSTRAINT `FK_GroupsOfChallenges_crxchallenge_id` FOREIGN KEY (`crxchallenge_id`) REFERENCES `CrxChallenges` (`id`),
  CONSTRAINT `FK_GroupsOfChallenges_group_id` FOREIGN KEY (`group_id`) REFERENCES `Groups` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `HWConfInCategories`
--

DROP TABLE IF EXISTS `HWConfInCategories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `HWConfInCategories` (
  `hwconf_id` bigint(20) unsigned NOT NULL,
  `category_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`hwconf_id`,`category_id`),
  KEY `FK_HWConfInCategories_category_id` (`category_id`),
  CONSTRAINT `FK_HWConfInCategories_category_id` FOREIGN KEY (`category_id`) REFERENCES `Categories` (`id`),
  CONSTRAINT `FK_HWConfInCategories_hwconf_id` FOREIGN KEY (`hwconf_id`) REFERENCES `HWConfs` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `HWConfs`
--

DROP TABLE IF EXISTS `HWConfs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `HWConfs` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `created` timestamp NULL DEFAULT current_timestamp(),
  `description` varchar(255) DEFAULT NULL,
  `deviceType` varchar(255) DEFAULT NULL,
  `modified` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `name` varchar(255) DEFAULT NULL,
  `creator_id` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  UNIQUE KEY `UNQ_HWConfs_0` (`name`),
  KEY `FK_HWConfs_creator_id` (`creator_id`),
  CONSTRAINT `FK_HWConfs_creator_id` FOREIGN KEY (`creator_id`) REFERENCES `Users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `HaveSeen`
--

DROP TABLE IF EXISTS `HaveSeen`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `HaveSeen` (
  `announcement_id` bigint(20) unsigned NOT NULL,
  `user_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`announcement_id`,`user_id`),
  KEY `FK_HaveSeen_user_id` (`user_id`),
  CONSTRAINT `FK_HaveSeen_announcement_id` FOREIGN KEY (`announcement_id`) REFERENCES `Announcements` (`id`),
  CONSTRAINT `FK_HaveSeen_user_id` FOREIGN KEY (`user_id`) REFERENCES `Users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Jobs`
--

DROP TABLE IF EXISTS `Jobs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Jobs` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `created` timestamp NULL DEFAULT current_timestamp(),
  `description` varchar(128) DEFAULT NULL,
  `exitCode` int(11) DEFAULT NULL,
  `modified` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `creator_id` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_Jobs_creator_id` (`creator_id`),
  CONSTRAINT `FK_Jobs_creator_id` FOREIGN KEY (`creator_id`) REFERENCES `Users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `LicenseToDevice`
--

DROP TABLE IF EXISTS `LicenseToDevice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `LicenseToDevice` (
  `device_id` bigint(20) unsigned NOT NULL,
  `license_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`device_id`,`license_id`),
  KEY `FK_LicenseToDevice_license_id` (`license_id`),
  CONSTRAINT `FK_LicenseToDevice_device_id` FOREIGN KEY (`device_id`) REFERENCES `Devices` (`id`),
  CONSTRAINT `FK_LicenseToDevice_license_id` FOREIGN KEY (`license_id`) REFERENCES `SoftwareLicenses` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `LoggedOn`
--

DROP TABLE IF EXISTS `LoggedOn`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `LoggedOn` (
  `device_id` bigint(20) unsigned NOT NULL,
  `user_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`device_id`,`user_id`),
  KEY `FK_LoggedOn_user_id` (`user_id`),
  CONSTRAINT `FK_LoggedOn_device_id` FOREIGN KEY (`device_id`) REFERENCES `Devices` (`id`),
  CONSTRAINT `FK_LoggedOn_user_id` FOREIGN KEY (`user_id`) REFERENCES `Users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `MailAccess`
--

DROP TABLE IF EXISTS `MailAccess`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `MailAccess` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `action` varchar(10) DEFAULT NULL,
  `address` varchar(64) DEFAULT NULL,
  `created` timestamp NULL DEFAULT current_timestamp(),
  `modified` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `creator_id` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `address` (`address`),
  KEY `FK_MailAccess_creator_id` (`creator_id`),
  CONSTRAINT `FK_MailAccess_creator_id` FOREIGN KEY (`creator_id`) REFERENCES `Users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Partitions`
--

DROP TABLE IF EXISTS `Partitions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Partitions` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `created` timestamp NULL DEFAULT current_timestamp(),
  `description` varchar(64) DEFAULT NULL,
  `format` varchar(16) DEFAULT NULL,
  `joinType` varchar(16) DEFAULT NULL,
  `modified` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `name` varchar(32) DEFAULT NULL,
  `OS` varchar(16) DEFAULT NULL,
  `tool` varchar(16) DEFAULT NULL,
  `creator_id` bigint(20) unsigned DEFAULT NULL,
  `hwconf_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UNQ_Partitions_0` (`hwconf_id`,`name`),
  KEY `FK_Partitions_creator_id` (`creator_id`),
  CONSTRAINT `FK_Partitions_creator_id` FOREIGN KEY (`creator_id`) REFERENCES `Users` (`id`),
  CONSTRAINT `FK_Partitions_hwconf_id` FOREIGN KEY (`hwconf_id`) REFERENCES `HWConfs` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `PositiveLists`
--

DROP TABLE IF EXISTS `PositiveLists`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `PositiveLists` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `created` timestamp NULL DEFAULT current_timestamp(),
  `description` varchar(64) DEFAULT NULL,
  `modified` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `name` varchar(32) DEFAULT NULL,
  `subject` varchar(32) DEFAULT NULL,
  `creator_id` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_PositiveLists_creator_id` (`creator_id`),
  CONSTRAINT `FK_PositiveLists_creator_id` FOREIGN KEY (`creator_id`) REFERENCES `Users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Printers`
--

DROP TABLE IF EXISTS `Printers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Printers` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `created` timestamp NULL DEFAULT current_timestamp(),
  `modified` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `name` varchar(255) DEFAULT NULL,
  `creator_id` bigint(20) unsigned DEFAULT NULL,
  `device_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UNQ_Printers_0` (`name`),
  KEY `FK_Printers_device_id` (`device_id`),
  KEY `FK_Printers_creator_id` (`creator_id`),
  CONSTRAINT `FK_Printers_creator_id` FOREIGN KEY (`creator_id`) REFERENCES `Users` (`id`),
  CONSTRAINT `FK_Printers_device_id` FOREIGN KEY (`device_id`) REFERENCES `Devices` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `QuestionInArea`
--

DROP TABLE IF EXISTS `QuestionInArea`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `QuestionInArea` (
  `subjectarea_id` bigint(20) unsigned NOT NULL,
  `crxquestion_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`subjectarea_id`,`crxquestion_id`),
  KEY `FK_QuestionInArea_crxquestion_id` (`crxquestion_id`),
  CONSTRAINT `FK_QuestionInArea_crxquestion_id` FOREIGN KEY (`crxquestion_id`) REFERENCES `CrxQuestions` (`id`),
  CONSTRAINT `FK_QuestionInArea_subjectarea_id` FOREIGN KEY (`subjectarea_id`) REFERENCES `SubjectAreas` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `RoomInCategories`
--

DROP TABLE IF EXISTS `RoomInCategories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `RoomInCategories` (
  `room_id` bigint(20) unsigned NOT NULL,
  `category_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`room_id`,`category_id`),
  KEY `FK_RoomInCategories_category_id` (`category_id`),
  CONSTRAINT `FK_RoomInCategories_category_id` FOREIGN KEY (`category_id`) REFERENCES `Categories` (`id`),
  CONSTRAINT `FK_RoomInCategories_room_id` FOREIGN KEY (`room_id`) REFERENCES `Rooms` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `RoomSmartControlls`
--

DROP TABLE IF EXISTS `RoomSmartControlls`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `RoomSmartControlls` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `created` timestamp NULL DEFAULT current_timestamp(),
  `endTime` datetime DEFAULT NULL,
  `modified` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `creator_id` bigint(20) unsigned DEFAULT NULL,
  `room_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_RoomSmartControlls_creator_id` (`creator_id`),
  KEY `FK_RoomSmartControlls_room_id` (`room_id`),
  CONSTRAINT `FK_RoomSmartControlls_creator_id` FOREIGN KEY (`creator_id`) REFERENCES `Users` (`id`),
  CONSTRAINT `FK_RoomSmartControlls_room_id` FOREIGN KEY (`room_id`) REFERENCES `Rooms` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Rooms`
--

DROP TABLE IF EXISTS `Rooms`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Rooms` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `created` timestamp NULL DEFAULT current_timestamp(),
  `description` varchar(255) DEFAULT NULL,
  `modified` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `name` varchar(255) DEFAULT NULL,
  `netMask` int(11) DEFAULT NULL,
  `places` int(11) DEFAULT NULL,
  `roomControl` varchar(255) DEFAULT NULL,
  `roomType` varchar(255) DEFAULT NULL,
  `roomRows` int(11) DEFAULT NULL,
  `startIP` varchar(255) DEFAULT NULL,
  `creator_id` bigint(20) unsigned DEFAULT NULL,
  `hwconf_id` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UNQ_Rooms_0` (`name`),
  KEY `FK_Rooms_hwconf_id` (`hwconf_id`),
  KEY `FK_Rooms_creator_id` (`creator_id`),
  CONSTRAINT `FK_Rooms_creator_id` FOREIGN KEY (`creator_id`) REFERENCES `Users` (`id`),
  CONSTRAINT `FK_Rooms_hwconf_id` FOREIGN KEY (`hwconf_id`) REFERENCES `HWConfs` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Sessions`
--

DROP TABLE IF EXISTS `Sessions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Sessions` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `createdate` timestamp NULL DEFAULT current_timestamp(),
  `ip` varchar(255) DEFAULT NULL,
  `token` varchar(255) DEFAULT NULL,
  `device_id` bigint(20) unsigned DEFAULT NULL,
  `room_id` bigint(20) unsigned DEFAULT NULL,
  `user_id` bigint(20) unsigned NOT NULL,
  `crx2fasession_id` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_Sessions_user_id` (`user_id`),
  KEY `FK_Sessions_device_id` (`device_id`),
  KEY `FK_Sessions_crx2fasession_id` (`crx2fasession_id`),
  KEY `FK_Sessions_room_id` (`room_id`),
  CONSTRAINT `FK_Sessions_crx2fasession_id` FOREIGN KEY (`crx2fasession_id`) REFERENCES `Crx2faSessions` (`id`),
  CONSTRAINT `FK_Sessions_device_id` FOREIGN KEY (`device_id`) REFERENCES `Devices` (`id`),
  CONSTRAINT `FK_Sessions_room_id` FOREIGN KEY (`room_id`) REFERENCES `Rooms` (`id`),
  CONSTRAINT `FK_Sessions_user_id` FOREIGN KEY (`user_id`) REFERENCES `Users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=229 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `SoftwareFullNames`
--

DROP TABLE IF EXISTS `SoftwareFullNames`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SoftwareFullNames` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `created` timestamp NULL DEFAULT current_timestamp(),
  `fullName` varchar(128) DEFAULT NULL,
  `modified` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `creator_id` bigint(20) unsigned DEFAULT NULL,
  `software_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_SoftwareFullNames_creator_id` (`creator_id`),
  KEY `FK_SoftwareFullNames_software_id` (`software_id`),
  CONSTRAINT `FK_SoftwareFullNames_creator_id` FOREIGN KEY (`creator_id`) REFERENCES `Users` (`id`),
  CONSTRAINT `FK_SoftwareFullNames_software_id` FOREIGN KEY (`software_id`) REFERENCES `Softwares` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `SoftwareInCategories`
--

DROP TABLE IF EXISTS `SoftwareInCategories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SoftwareInCategories` (
  `software_id` bigint(20) unsigned NOT NULL,
  `category_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`software_id`,`category_id`),
  KEY `FK_SoftwareInCategories_category_id` (`category_id`),
  CONSTRAINT `FK_SoftwareInCategories_category_id` FOREIGN KEY (`category_id`) REFERENCES `Categories` (`id`),
  CONSTRAINT `FK_SoftwareInCategories_software_id` FOREIGN KEY (`software_id`) REFERENCES `Softwares` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `SoftwareLicenses`
--

DROP TABLE IF EXISTS `SoftwareLicenses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SoftwareLicenses` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `count` int(11) DEFAULT NULL,
  `created` timestamp NULL DEFAULT current_timestamp(),
  `licenseType` char(1) DEFAULT NULL,
  `modified` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `value` varchar(1024) DEFAULT NULL,
  `creator_id` bigint(20) unsigned DEFAULT NULL,
  `software_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_SoftwareLicenses_creator_id` (`creator_id`),
  KEY `FK_SoftwareLicenses_software_id` (`software_id`),
  CONSTRAINT `FK_SoftwareLicenses_creator_id` FOREIGN KEY (`creator_id`) REFERENCES `Users` (`id`),
  CONSTRAINT `FK_SoftwareLicenses_software_id` FOREIGN KEY (`software_id`) REFERENCES `Softwares` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `SoftwareRemovedFromCategories`
--

DROP TABLE IF EXISTS `SoftwareRemovedFromCategories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SoftwareRemovedFromCategories` (
  `software_id` bigint(20) unsigned NOT NULL,
  `category_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`software_id`,`category_id`),
  KEY `FK_SoftwareRemovedFromCategories_category_id` (`category_id`),
  CONSTRAINT `FK_SoftwareRemovedFromCategories_category_id` FOREIGN KEY (`category_id`) REFERENCES `Categories` (`id`),
  CONSTRAINT `FK_SoftwareRemovedFromCategories_software_id` FOREIGN KEY (`software_id`) REFERENCES `Softwares` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `SoftwareRequirements`
--

DROP TABLE IF EXISTS `SoftwareRequirements`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SoftwareRequirements` (
  `requirement_id` bigint(20) unsigned NOT NULL,
  `software_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`requirement_id`,`software_id`),
  KEY `FK_SoftwareRequirements_software_id` (`software_id`),
  CONSTRAINT `FK_SoftwareRequirements_requirement_id` FOREIGN KEY (`requirement_id`) REFERENCES `Softwares` (`id`),
  CONSTRAINT `FK_SoftwareRequirements_software_id` FOREIGN KEY (`software_id`) REFERENCES `Softwares` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `SoftwareStatus`
--

DROP TABLE IF EXISTS `SoftwareStatus`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SoftwareStatus` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `created` timestamp NULL DEFAULT current_timestamp(),
  `modified` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `status` varchar(255) DEFAULT NULL,
  `creator_id` bigint(20) unsigned DEFAULT NULL,
  `device_id` bigint(20) unsigned NOT NULL,
  `softwareversion_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UNQ_SoftwareStatus_0` (`softwareversion_id`,`device_id`),
  KEY `FK_SoftwareStatus_creator_id` (`creator_id`),
  KEY `FK_SoftwareStatus_device_id` (`device_id`),
  CONSTRAINT `FK_SoftwareStatus_creator_id` FOREIGN KEY (`creator_id`) REFERENCES `Users` (`id`),
  CONSTRAINT `FK_SoftwareStatus_device_id` FOREIGN KEY (`device_id`) REFERENCES `Devices` (`id`),
  CONSTRAINT `FK_SoftwareStatus_softwareversion_id` FOREIGN KEY (`softwareversion_id`) REFERENCES `SoftwareVersions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `SoftwareVersions`
--

DROP TABLE IF EXISTS `SoftwareVersions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SoftwareVersions` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `created` timestamp NULL DEFAULT current_timestamp(),
  `modified` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `status` varchar(1) DEFAULT NULL,
  `version` varchar(128) DEFAULT NULL,
  `creator_id` bigint(20) unsigned DEFAULT NULL,
  `software_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_SoftwareVersions_creator_id` (`creator_id`),
  KEY `FK_SoftwareVersions_software_id` (`software_id`),
  CONSTRAINT `FK_SoftwareVersions_creator_id` FOREIGN KEY (`creator_id`) REFERENCES `Users` (`id`),
  CONSTRAINT `FK_SoftwareVersions_software_id` FOREIGN KEY (`software_id`) REFERENCES `Softwares` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Softwares`
--

DROP TABLE IF EXISTS `Softwares`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Softwares` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `created` timestamp NULL DEFAULT current_timestamp(),
  `description` varchar(128) DEFAULT NULL,
  `manually` char(1) DEFAULT 'Y',
  `modified` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `name` varchar(128) DEFAULT NULL,
  `weight` int(11) DEFAULT NULL,
  `creator_id` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UNQ_Softwares_0` (`name`),
  KEY `FK_Softwares_creator_id` (`creator_id`),
  CONSTRAINT `FK_Softwares_creator_id` FOREIGN KEY (`creator_id`) REFERENCES `Users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `SubjectAreas`
--

DROP TABLE IF EXISTS `SubjectAreas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SubjectAreas` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `created` timestamp NULL DEFAULT current_timestamp(),
  `modified` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `name` varchar(64) DEFAULT NULL,
  `creator_id` bigint(20) unsigned DEFAULT NULL,
  `teachingsubject_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_SubjectAreas_creator_id` (`creator_id`),
  KEY `FK_SubjectAreas_teachingsubject_id` (`teachingsubject_id`),
  CONSTRAINT `FK_SubjectAreas_creator_id` FOREIGN KEY (`creator_id`) REFERENCES `Users` (`id`),
  CONSTRAINT `FK_SubjectAreas_teachingsubject_id` FOREIGN KEY (`teachingsubject_id`) REFERENCES `TeachingSubjects` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `TaskResponses`
--

DROP TABLE IF EXISTS `TaskResponses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `TaskResponses` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `created` timestamp NULL DEFAULT current_timestamp(),
  `modified` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `rating` varchar(8192) DEFAULT NULL,
  `text` longtext DEFAULT NULL,
  `creator_id` bigint(20) unsigned DEFAULT NULL,
  `parent_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_TaskResponses_creator_id` (`creator_id`),
  KEY `FK_TaskResponses_parent_id` (`parent_id`),
  CONSTRAINT `FK_TaskResponses_creator_id` FOREIGN KEY (`creator_id`) REFERENCES `Users` (`id`),
  CONSTRAINT `FK_TaskResponses_parent_id` FOREIGN KEY (`parent_id`) REFERENCES `Announcements` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `TeachingSubjects`
--

DROP TABLE IF EXISTS `TeachingSubjects`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `TeachingSubjects` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `created` timestamp NULL DEFAULT current_timestamp(),
  `modified` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `name` varchar(64) DEFAULT NULL,
  `creator_id` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`),
  UNIQUE KEY `UNQ_TeachingSubjects_0` (`name`),
  KEY `FK_TeachingSubjects_creator_id` (`creator_id`),
  CONSTRAINT `FK_TeachingSubjects_creator_id` FOREIGN KEY (`creator_id`) REFERENCES `Users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Translations`
--

DROP TABLE IF EXISTS `Translations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Translations` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `LANG` varchar(255) DEFAULT NULL,
  `STRING` varchar(255) DEFAULT NULL,
  `VALUE` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `UserEvents`
--

DROP TABLE IF EXISTS `UserEvents`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `UserEvents` (
  `event_id` bigint(20) unsigned NOT NULL,
  `user_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`event_id`,`user_id`),
  KEY `FK_UserEvents_user_id` (`user_id`),
  CONSTRAINT `FK_UserEvents_event_id` FOREIGN KEY (`event_id`) REFERENCES `CrxCalendar` (`id`),
  CONSTRAINT `FK_UserEvents_user_id` FOREIGN KEY (`user_id`) REFERENCES `Users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `UserInCategories`
--

DROP TABLE IF EXISTS `UserInCategories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `UserInCategories` (
  `category_id` bigint(20) unsigned NOT NULL,
  `user_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`category_id`,`user_id`),
  KEY `FK_UserInCategories_user_id` (`user_id`),
  CONSTRAINT `FK_UserInCategories_category_id` FOREIGN KEY (`category_id`) REFERENCES `Categories` (`id`),
  CONSTRAINT `FK_UserInCategories_user_id` FOREIGN KEY (`user_id`) REFERENCES `Users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Users`
--

DROP TABLE IF EXISTS `Users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Users` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `birthDay` date NOT NULL DEFAULT current_timestamp(),
  `created` timestamp NULL DEFAULT current_timestamp(),
  `fsQuota` int(11) DEFAULT NULL,
  `fsQuotaUsed` int(11) DEFAULT NULL,
  `givenName` varchar(255) DEFAULT NULL,
  `initialPassword` varchar(255) DEFAULT NULL,
  `modified` timestamp NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `msQuota` int(11) DEFAULT NULL,
  `msQuotaUsed` int(11) DEFAULT NULL,
  `role` varchar(255) DEFAULT NULL,
  `surName` varchar(255) DEFAULT NULL,
  `uid` varchar(255) DEFAULT NULL,
  `uuid` varchar(255) DEFAULT NULL,
  `creator_id` bigint(20) unsigned DEFAULT NULL,
  `color` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UNQ_Users_0` (`uid`),
  KEY `FK_Users_creator_id` (`creator_id`),
  CONSTRAINT `FK_Users_creator_id` FOREIGN KEY (`creator_id`) REFERENCES `Users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `UsersOfChallenges`
--

DROP TABLE IF EXISTS `UsersOfChallenges`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `UsersOfChallenges` (
  `crxchallenge_id` bigint(20) unsigned NOT NULL,
  `user_id` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`crxchallenge_id`,`user_id`),
  KEY `FK_UsersOfChallenges_user_id` (`user_id`),
  CONSTRAINT `FK_UsersOfChallenges_crxchallenge_id` FOREIGN KEY (`crxchallenge_id`) REFERENCES `CrxChallenges` (`id`),
  CONSTRAINT `FK_UsersOfChallenges_user_id` FOREIGN KEY (`user_id`) REFERENCES `Users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_520_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2024-11-23 12:11:27
