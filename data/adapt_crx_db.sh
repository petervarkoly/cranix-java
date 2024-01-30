#!/bin/bash
#
SERVICE=cranix-api
if [ -e /opt/cranix-java/bin/start-cephalix-api ]; then
	SERVICE=cephalix-api
fi
/usr/bin/systemctl stop $SERVICE cron

ALLG=$( echo "SELECT id FROM TeachingSubjects where name='Allgemeinwissen';" | mysql CRX | tail -n 1 )
echo "UPDATE CrxChallenges set teachingsubject_id=$ALLG where teachingsubject_id NOT IN (SELECT id FROM TeachingSubjects)" | mysql CRX

echo "ALTER TABLE AccessInRooms DROP FOREIGN KEY AccessInRooms_ibfk_1;"| mysql CRX
echo "ALTER TABLE AccessInRooms DROP FOREIGN KEY AccessInRooms_ibfk_2;"| mysql CRX
echo "ALTER TABLE AccessInRooms DROP FOREIGN KEY FK_AccessInRooms_CREATOR_ID;"| mysql CRX
echo "ALTER TABLE AccessInRooms DROP FOREIGN KEY FK_AccessInRooms_ROOM_ID;"| mysql CRX
echo "ALTER TABLE Acls DROP FOREIGN KEY Acls_ibfk_1;"| mysql CRX
echo "ALTER TABLE Acls DROP FOREIGN KEY Acls_ibfk_2;"| mysql CRX
echo "ALTER TABLE Acls DROP FOREIGN KEY Acls_ibfk_3;"| mysql CRX
echo "ALTER TABLE Acls DROP FOREIGN KEY FK_Acls_CREATOR_ID;"| mysql CRX
echo "ALTER TABLE Acls DROP FOREIGN KEY FK_Acls_GROUP_ID;"| mysql CRX
echo "ALTER TABLE Acls DROP FOREIGN KEY FK_Acls_USER_ID;"| mysql CRX
echo "ALTER TABLE Aliases DROP FOREIGN KEY Aliases_ibfk_1;"| mysql CRX
echo "ALTER TABLE Aliases DROP FOREIGN KEY FK_Aliases_USER_ID;"| mysql CRX
echo "ALTER TABLE AnnouncementInCategories DROP FOREIGN KEY AnnouncementInCategories_ibfk_1;"| mysql CRX
echo "ALTER TABLE AnnouncementInCategories DROP FOREIGN KEY AnnouncementInCategories_ibfk_2;"| mysql CRX
echo "ALTER TABLE AnnouncementInCategories DROP FOREIGN KEY FK_AnnouncementInCategories_announcement_id;"| mysql CRX
echo "ALTER TABLE AnnouncementInCategories DROP FOREIGN KEY FK_AnnouncementInCategories_category_id;"| mysql CRX
echo "ALTER TABLE Announcements DROP FOREIGN KEY Announcements_ibfk_1;"| mysql CRX
echo "ALTER TABLE Announcements DROP FOREIGN KEY FK_Announcements_owner_id;"| mysql CRX
echo "ALTER TABLE AvailablePrinters DROP FOREIGN KEY AvailablePrinters_ibfk_1;"| mysql CRX
echo "ALTER TABLE AvailablePrinters DROP FOREIGN KEY AvailablePrinters_ibfk_2;"| mysql CRX
echo "ALTER TABLE AvailablePrinters DROP FOREIGN KEY AvailablePrinters_ibfk_3;"| mysql CRX
echo "ALTER TABLE AvailablePrinters DROP FOREIGN KEY FK_AvailablePrinters_device_id;"| mysql CRX
echo "ALTER TABLE AvailablePrinters DROP FOREIGN KEY FK_AvailablePrinters_printer_id;"| mysql CRX
echo "ALTER TABLE AvailablePrinters DROP FOREIGN KEY FK_AvailablePrinters_room_id;"| mysql CRX
echo "ALTER TABLE Categories DROP FOREIGN KEY Categories_ibfk_1;"| mysql CRX
echo "ALTER TABLE Categories DROP FOREIGN KEY FK_Categories_OWNER_ID;"| mysql CRX
echo "ALTER TABLE ChallengesInArea DROP FOREIGN KEY FK_ChallengesInArea_crxchallenge_id;"| mysql CRX
echo "ALTER TABLE ChallengesInArea DROP FOREIGN KEY FK_ChallengesInArea_subjectarea_id;"| mysql CRX
echo "ALTER TABLE ContactInCategories DROP FOREIGN KEY ContactInCategories_ibfk_1;"| mysql CRX
echo "ALTER TABLE ContactInCategories DROP FOREIGN KEY ContactInCategories_ibfk_2;"| mysql CRX
echo "ALTER TABLE ContactInCategories DROP FOREIGN KEY FK_ContactInCategories_category_id;"| mysql CRX
echo "ALTER TABLE ContactInCategories DROP FOREIGN KEY FK_ContactInCategories_contact_id;"| mysql CRX
echo "ALTER TABLE Contacts DROP FOREIGN KEY Contacts_ibfk_1;"| mysql CRX
echo "ALTER TABLE Contacts DROP FOREIGN KEY FK_Contacts_owner_id;"| mysql CRX
echo "ALTER TABLE Crx2faSessions DROP FOREIGN KEY FK_Crx2faSessions_MYCRX2FA_id;"| mysql CRX
echo "ALTER TABLE Crx2faSessions DROP FOREIGN KEY FK_Crx2faSessions_crx2fa_id;"| mysql CRX
echo "ALTER TABLE CrxChallenges DROP FOREIGN KEY FK_CrxChallenges_teachingsubject_id;"| mysql CRX
echo "ALTER TABLE CrxConfig DROP FOREIGN KEY CrxConfig_ibfk_1;"| mysql CRX
echo "ALTER TABLE CrxConfig DROP FOREIGN KEY FK_CrxConfig_CREATOR_ID;"| mysql CRX
echo "ALTER TABLE CrxMConfig DROP FOREIGN KEY CrxMConfig_ibfk_1;"| mysql CRX
echo "ALTER TABLE CrxMConfig DROP FOREIGN KEY FK_CrxMConfig_CREATOR_ID;"| mysql CRX
echo "ALTER TABLE CrxQuestionAnswers DROP FOREIGN KEY FK_CrxQuestionAnswers_crxquestion_id;"| mysql CRX
echo "ALTER TABLE CrxQuestions DROP FOREIGN KEY FK_CrxQuestions_crxchallenge_id;"| mysql CRX
echo "ALTER TABLE CrxResponse DROP FOREIGN KEY CrxResponse_ibfk_1;"| mysql CRX
echo "ALTER TABLE DefaultPrinter DROP FOREIGN KEY DefaultPrinter_ibfk_1;"| mysql CRX
echo "ALTER TABLE DefaultPrinter DROP FOREIGN KEY DefaultPrinter_ibfk_2;"| mysql CRX
echo "ALTER TABLE DefaultPrinter DROP FOREIGN KEY DefaultPrinter_ibfk_3;"| mysql CRX
echo "ALTER TABLE DefaultPrinter DROP FOREIGN KEY FK_DefaultPrinter_device_id;"| mysql CRX
echo "ALTER TABLE DefaultPrinter DROP FOREIGN KEY FK_DefaultPrinter_printer_id;"| mysql CRX
echo "ALTER TABLE DefaultPrinter DROP FOREIGN KEY FK_DefaultPrinter_room_id;"| mysql CRX
echo "ALTER TABLE DeviceInCategories DROP FOREIGN KEY DeviceInCategories_ibfk_1;"| mysql CRX
echo "ALTER TABLE DeviceInCategories DROP FOREIGN KEY DeviceInCategories_ibfk_2;"| mysql CRX
echo "ALTER TABLE DeviceInCategories DROP FOREIGN KEY FK_DeviceInCategories_category_id;"| mysql CRX
echo "ALTER TABLE DeviceInCategories DROP FOREIGN KEY FK_DeviceInCategories_device_id;"| mysql CRX
echo "ALTER TABLE Devices DROP FOREIGN KEY Devices_ibfk_1;"| mysql CRX
echo "ALTER TABLE Devices DROP FOREIGN KEY Devices_ibfk_2;"| mysql CRX
echo "ALTER TABLE Devices DROP FOREIGN KEY Devices_ibfk_3;"| mysql CRX
echo "ALTER TABLE Devices DROP FOREIGN KEY FK_Devices_hwconf_id;"| mysql CRX
echo "ALTER TABLE Devices DROP FOREIGN KEY FK_Devices_owner_id;"| mysql CRX
echo "ALTER TABLE Devices DROP FOREIGN KEY FK_Devices_room_id;"| mysql CRX
echo "ALTER TABLE Enumerates DROP FOREIGN KEY Enumerates_ibfk_1;"| mysql CRX
echo "ALTER TABLE Enumerates DROP FOREIGN KEY FK_Enumerates_CREATOR_ID;"| mysql CRX
echo "ALTER TABLE FAQInCategories DROP FOREIGN KEY FAQInCategories_ibfk_1;"| mysql CRX
echo "ALTER TABLE FAQInCategories DROP FOREIGN KEY FAQInCategories_ibfk_2;"| mysql CRX
echo "ALTER TABLE FAQInCategories DROP FOREIGN KEY FK_FAQInCategories_category_id;"| mysql CRX
echo "ALTER TABLE FAQInCategories DROP FOREIGN KEY FK_FAQInCategories_faq_id;"| mysql CRX
echo "ALTER TABLE FAQs DROP FOREIGN KEY FAQs_ibfk_1;"| mysql CRX
echo "ALTER TABLE FAQs DROP FOREIGN KEY FK_FAQs_OWNER_ID;"| mysql CRX
echo "ALTER TABLE GroupInCategories DROP FOREIGN KEY FK_GroupInCategories_category_id;"| mysql CRX
echo "ALTER TABLE GroupInCategories DROP FOREIGN KEY FK_GroupInCategories_group_id;"| mysql CRX
echo "ALTER TABLE GroupInCategories DROP FOREIGN KEY GroupInCategories_ibfk_1;"| mysql CRX
echo "ALTER TABLE GroupInCategories DROP FOREIGN KEY GroupInCategories_ibfk_2;"| mysql CRX
echo "ALTER TABLE GroupMember DROP FOREIGN KEY FK_GroupMember_group_id;"| mysql CRX
echo "ALTER TABLE GroupMember DROP FOREIGN KEY FK_GroupMember_user_id;"| mysql CRX
echo "ALTER TABLE GroupMember DROP FOREIGN KEY GroupMember_ibfk_1;"| mysql CRX
echo "ALTER TABLE GroupMember DROP FOREIGN KEY GroupMember_ibfk_2;"| mysql CRX
echo "ALTER TABLE Groups DROP FOREIGN KEY FK_Groups_OWNER_ID;"| mysql CRX
echo "ALTER TABLE Groups DROP FOREIGN KEY Groups_ibfk_1;"| mysql CRX
echo "ALTER TABLE HWConfInCategories DROP FOREIGN KEY FK_HWConfInCategories_category_id;"| mysql CRX
echo "ALTER TABLE HWConfInCategories DROP FOREIGN KEY FK_HWConfInCategories_hwconf_id;"| mysql CRX
echo "ALTER TABLE HWConfInCategories DROP FOREIGN KEY HWConfInCategories_ibfk_1;"| mysql CRX
echo "ALTER TABLE HWConfInCategories DROP FOREIGN KEY HWConfInCategories_ibfk_2;"| mysql CRX
echo "ALTER TABLE HWConfs DROP FOREIGN KEY FK_HWConfs_CREATOR_ID;"| mysql CRX
echo "ALTER TABLE HWConfs DROP FOREIGN KEY HWConfs_ibfk_1;"| mysql CRX
echo "ALTER TABLE HaveSeen DROP FOREIGN KEY FK_HaveSeen_announcement_id;"| mysql CRX
echo "ALTER TABLE HaveSeen DROP FOREIGN KEY FK_HaveSeen_user_id;"| mysql CRX
echo "ALTER TABLE HaveSeen DROP FOREIGN KEY HaveSeen_ibfk_1;"| mysql CRX
echo "ALTER TABLE HaveSeen DROP FOREIGN KEY HaveSeen_ibfk_2;"| mysql CRX
echo "ALTER TABLE LicenseToDevice DROP FOREIGN KEY FK_LicenseToDevice_device_id;"| mysql CRX
echo "ALTER TABLE LicenseToDevice DROP FOREIGN KEY FK_LicenseToDevice_license_id;"| mysql CRX
echo "ALTER TABLE LicenseToDevice DROP FOREIGN KEY LicenseToDevice_ibfk_1;"| mysql CRX
echo "ALTER TABLE LicenseToDevice DROP FOREIGN KEY LicenseToDevice_ibfk_2;"| mysql CRX
echo "ALTER TABLE LoggedOn DROP FOREIGN KEY FK_LoggedOn_device_id;"| mysql CRX
echo "ALTER TABLE LoggedOn DROP FOREIGN KEY FK_LoggedOn_user_id;"| mysql CRX
echo "ALTER TABLE LoggedOn DROP FOREIGN KEY LoggedOn_ibfk_1;"| mysql CRX
echo "ALTER TABLE LoggedOn DROP FOREIGN KEY LoggedOn_ibfk_2;"| mysql CRX
echo "ALTER TABLE Partitions DROP FOREIGN KEY FK_Partitions_CREATOR_ID;"| mysql CRX
echo "ALTER TABLE Partitions DROP FOREIGN KEY FK_Partitions_HWCONF_ID;"| mysql CRX
echo "ALTER TABLE Partitions DROP FOREIGN KEY Partitions_ibfk_1;"| mysql CRX
echo "ALTER TABLE Partitions DROP FOREIGN KEY Partitions_ibfk_2;"| mysql CRX
echo "ALTER TABLE PositiveLists DROP FOREIGN KEY FK_PositiveLists_OWNER_ID;"| mysql CRX
echo "ALTER TABLE PositiveLists DROP FOREIGN KEY PositiveLists_ibfk_1;"| mysql CRX
echo "ALTER TABLE Printers DROP FOREIGN KEY FK_Printers_CREATOR_ID;"| mysql CRX
echo "ALTER TABLE Printers DROP FOREIGN KEY FK_Printers_DEVICE_ID;"| mysql CRX
echo "ALTER TABLE Printers DROP FOREIGN KEY Printers_ibfk_1;"| mysql CRX
echo "ALTER TABLE Printers DROP FOREIGN KEY Printers_ibfk_2;"| mysql CRX
echo "ALTER TABLE QuestionInArea DROP FOREIGN KEY FK_QuestionInArea_crxquestion_id;"| mysql CRX
echo "ALTER TABLE QuestionInArea DROP FOREIGN KEY FK_QuestionInArea_subjectarea_id;"| mysql CRX
echo "ALTER TABLE RoomInCategories DROP FOREIGN KEY FK_RoomInCategories_category_id;"| mysql CRX
echo "ALTER TABLE RoomInCategories DROP FOREIGN KEY FK_RoomInCategories_room_id;"| mysql CRX
echo "ALTER TABLE RoomInCategories DROP FOREIGN KEY RoomInCategories_ibfk_1;"| mysql CRX
echo "ALTER TABLE RoomInCategories DROP FOREIGN KEY RoomInCategories_ibfk_2;"| mysql CRX
echo "ALTER TABLE RoomSmartControlls DROP FOREIGN KEY FK_RoomSmartControlls_room_id;"| mysql CRX
echo "ALTER TABLE RoomSmartControlls DROP FOREIGN KEY FK_RoomSmartControlls_user_id;"| mysql CRX
echo "ALTER TABLE RoomSmartControlls DROP FOREIGN KEY RoomSmartControlls_ibfk_1;"| mysql CRX
echo "ALTER TABLE RoomSmartControlls DROP FOREIGN KEY RoomSmartControlls_ibfk_2;"| mysql CRX
echo "ALTER TABLE Rooms DROP FOREIGN KEY FK_Rooms_CREATOR_ID;"| mysql CRX
echo "ALTER TABLE Rooms DROP FOREIGN KEY FK_Rooms_hwconf_id;"| mysql CRX
echo "ALTER TABLE Rooms DROP FOREIGN KEY Rooms_ibfk_1;"| mysql CRX
echo "ALTER TABLE Rooms DROP FOREIGN KEY Rooms_ibfk_2;"| mysql CRX
echo "ALTER TABLE Sessions DROP FOREIGN KEY FK_Sessions_DEVICE_ID;"| mysql CRX
echo "ALTER TABLE Sessions DROP FOREIGN KEY FK_Sessions_ROOM_ID;"| mysql CRX
echo "ALTER TABLE Sessions DROP FOREIGN KEY FK_Sessions_user_id;"| mysql CRX
echo "ALTER TABLE Sessions DROP FOREIGN KEY Sessions_ibfk_1;"| mysql CRX
echo "ALTER TABLE Sessions DROP FOREIGN KEY Sessions_ibfk_2;"| mysql CRX
echo "ALTER TABLE Sessions DROP FOREIGN KEY Sessions_ibfk_3;"| mysql CRX
echo "ALTER TABLE SoftwareFullNames DROP FOREIGN KEY FK_SoftwareFullNames_SOFTWARE_ID;"| mysql CRX
echo "ALTER TABLE SoftwareFullNames DROP FOREIGN KEY SoftwareFullNames_ibfk_1;"| mysql CRX
echo "ALTER TABLE SoftwareInCategories DROP FOREIGN KEY FK_SoftwareInCategories_category_id;"| mysql CRX
echo "ALTER TABLE SoftwareInCategories DROP FOREIGN KEY FK_SoftwareInCategories_software_id;"| mysql CRX
echo "ALTER TABLE SoftwareInCategories DROP FOREIGN KEY SoftwareInCategories_ibfk_1;"| mysql CRX
echo "ALTER TABLE SoftwareInCategories DROP FOREIGN KEY SoftwareInCategories_ibfk_2;"| mysql CRX
echo "ALTER TABLE SoftwareLicenses DROP FOREIGN KEY FK_SoftwareLicenses_CREATOR_ID;"| mysql CRX
echo "ALTER TABLE SoftwareLicenses DROP FOREIGN KEY FK_SoftwareLicenses_SOFTWARE_ID;"| mysql CRX
echo "ALTER TABLE SoftwareLicenses DROP FOREIGN KEY SoftwareLicenses_ibfk_1;"| mysql CRX
echo "ALTER TABLE SoftwareLicenses DROP FOREIGN KEY SoftwareLicenses_ibfk_2;"| mysql CRX
echo "ALTER TABLE SoftwareRemovedFromCategories DROP FOREIGN KEY FK_SoftwareRemovedFromCategories_category_id;"| mysql CRX
echo "ALTER TABLE SoftwareRemovedFromCategories DROP FOREIGN KEY FK_SoftwareRemovedFromCategories_software_id;"| mysql CRX
echo "ALTER TABLE SoftwareRemovedFromCategories DROP FOREIGN KEY SoftwareRemovedFromCategories_ibfk_1;"| mysql CRX
echo "ALTER TABLE SoftwareRemovedFromCategories DROP FOREIGN KEY SoftwareRemovedFromCategories_ibfk_2;"| mysql CRX
echo "ALTER TABLE SoftwareRequirements DROP FOREIGN KEY FK_SoftwareRequirements_requirement_id;"| mysql CRX
echo "ALTER TABLE SoftwareRequirements DROP FOREIGN KEY FK_SoftwareRequirements_software_id;"| mysql CRX
echo "ALTER TABLE SoftwareRequirements DROP FOREIGN KEY SoftwareRequirements_ibfk_1;"| mysql CRX
echo "ALTER TABLE SoftwareRequirements DROP FOREIGN KEY SoftwareRequirements_ibfk_2;"| mysql CRX
echo "ALTER TABLE SoftwareStatus DROP FOREIGN KEY FK_SoftwareStatus_DEVICE_ID;"| mysql CRX
echo "ALTER TABLE SoftwareStatus DROP FOREIGN KEY FK_SoftwareStatus_SOFTWAREVERSION_ID;"| mysql CRX
echo "ALTER TABLE SoftwareStatus DROP FOREIGN KEY SoftwareStatus_ibfk_1;"| mysql CRX
echo "ALTER TABLE SoftwareStatus DROP FOREIGN KEY SoftwareStatus_ibfk_2;"| mysql CRX
echo "ALTER TABLE SoftwareVersions DROP FOREIGN KEY FK_SoftwareVersions_software_id;"| mysql CRX
echo "ALTER TABLE SoftwareVersions DROP FOREIGN KEY SoftwareVersions_ibfk_1;"| mysql CRX
echo "ALTER TABLE Softwares DROP FOREIGN KEY FK_Softwares_CREATOR_ID;"| mysql CRX
echo "ALTER TABLE Softwares DROP FOREIGN KEY Softwares_ibfk_1;"| mysql CRX
echo "ALTER TABLE TaskResponses DROP FOREIGN KEY FK_TaskResponses_OWNER_ID;"| mysql CRX
echo "ALTER TABLE TaskResponses DROP FOREIGN KEY FK_TaskResponses_PARENT_ID;"| mysql CRX
echo "ALTER TABLE TaskResponses DROP FOREIGN KEY TaskResponses_ibfk_1;"| mysql CRX
echo "ALTER TABLE TaskResponses DROP FOREIGN KEY TaskResponses_ibfk_2;"| mysql CRX
echo "ALTER TABLE Tasks DROP FOREIGN KEY Tasks_ibfk_1;"| mysql CRX
echo "ALTER TABLE Tasks DROP FOREIGN KEY Tasks_ibfk_2;"| mysql CRX
echo "ALTER TABLE UserInCategories DROP FOREIGN KEY FK_UserInCategories_category_id;"| mysql CRX
echo "ALTER TABLE UserInCategories DROP FOREIGN KEY FK_UserInCategories_user_id;"| mysql CRX
echo "ALTER TABLE UserInCategories DROP FOREIGN KEY UserInCategories_ibfk_1;"| mysql CRX
echo "ALTER TABLE UserInCategories DROP FOREIGN KEY UserInCategories_ibfk_2;"| mysql CRX
echo "ALTER TABLE Users DROP FOREIGN KEY FK_Users_CREATOR_ID;"| mysql CRX
echo "ALTER TABLE Users DROP FOREIGN KEY Users_ibfk_1;"| mysql CRX
echo "ALTER TABLE UsersOfChallenges DROP FOREIGN KEY FK_UsersOfChallenges_crxchallenge_id;"| mysql CRX
echo "ALTER TABLE AccessInRooms DROP KEY creator_id;"| mysql CRX
echo "ALTER TABLE AccessInRooms DROP KEY room_id;"| mysql CRX
echo "ALTER TABLE Acls DROP KEY creator_id;"| mysql CRX
echo "ALTER TABLE Acls DROP KEY user_id;"| mysql CRX
echo "ALTER TABLE Acls DROP KEY group_id;"| mysql CRX
echo "ALTER TABLE Aliases DROP KEY user_id;"| mysql CRX
echo "ALTER TABLE AnnouncementInCategories DROP KEY category_id;"| mysql CRX
echo "ALTER TABLE Announcements DROP KEY owner_id;"| mysql CRX
echo "ALTER TABLE AvailablePrinters DROP KEY room_id;"| mysql CRX
echo "ALTER TABLE AvailablePrinters DROP KEY device_id;"| mysql CRX
echo "ALTER TABLE AvailablePrinters DROP KEY printer_id;"| mysql CRX
echo "ALTER TABLE Categories DROP KEY owner_id;"| mysql CRX
echo "ALTER TABLE ChallengesInArea DROP KEY FK_ChallengesInArea_subjectarea_id;"| mysql CRX
echo "ALTER TABLE ContactInCategories DROP KEY category_id;"| mysql CRX
echo "ALTER TABLE Contacts DROP KEY owner_id;"| mysql CRX
echo "ALTER TABLE Crx2faSessions DROP KEY FK_Crx2faSessions_mycrx2fa_id;"| mysql CRX
echo "ALTER TABLE Crx2faSessions DROP KEY FK_Crx2faSessions_crx2fa_id;"| mysql CRX
echo "ALTER TABLE CrxChallenges DROP KEY FK_CrxChallenges_teachingsubject_id;"| mysql CRX
echo "ALTER TABLE CrxConfig DROP KEY creator_id;"| mysql CRX
echo "ALTER TABLE CrxMConfig DROP KEY creator_id;"| mysql CRX
echo "ALTER TABLE CrxQuestionAnswers DROP KEY FK_CrxQuestionAnswers_crxquestion_id;"| mysql CRX
echo "ALTER TABLE CrxQuestions DROP KEY FK_CrxQuestions_crxchallenge_id;"| mysql CRX
echo "ALTER TABLE CrxResponse DROP KEY session_id;"| mysql CRX
echo "ALTER TABLE DefaultPrinter DROP KEY room_id;"| mysql CRX
echo "ALTER TABLE DefaultPrinter DROP KEY device_id;"| mysql CRX
echo "ALTER TABLE DefaultPrinter DROP KEY printer_id;"| mysql CRX
echo "ALTER TABLE DeviceInCategories DROP KEY category_id;"| mysql CRX
echo "ALTER TABLE Devices DROP KEY room_id;"| mysql CRX
echo "ALTER TABLE Devices DROP KEY hwconf_id;"| mysql CRX
echo "ALTER TABLE Devices DROP KEY owner_id;"| mysql CRX
echo "ALTER TABLE Enumerates DROP KEY creator_id;"| mysql CRX
echo "ALTER TABLE FAQInCategories DROP KEY category_id;"| mysql CRX
echo "ALTER TABLE FAQs DROP KEY owner_id;"| mysql CRX
echo "ALTER TABLE GroupInCategories DROP KEY category_id;"| mysql CRX
echo "ALTER TABLE GroupMember DROP KEY group_id;"| mysql CRX
echo "ALTER TABLE Groups DROP KEY owner_id;"| mysql CRX
echo "ALTER TABLE HWConfInCategories DROP KEY category_id;"| mysql CRX
echo "ALTER TABLE HWConfs DROP KEY creator_id;"| mysql CRX
echo "ALTER TABLE HaveSeen DROP KEY user_id;"| mysql CRX
echo "ALTER TABLE LicenseToDevice DROP KEY device_id;"| mysql CRX
echo "ALTER TABLE LoggedOn DROP KEY user_id;"| mysql CRX
echo "ALTER TABLE Partitions DROP KEY creator_id;"| mysql CRX
echo "ALTER TABLE Partitions DROP KEY hwconf_id;"| mysql CRX
echo "ALTER TABLE PositiveLists DROP KEY owner_id;"| mysql CRX
echo "ALTER TABLE Printers DROP KEY device_id;"| mysql CRX
echo "ALTER TABLE Printers DROP KEY creator_id;"| mysql CRX
echo "ALTER TABLE QuestionInArea DROP KEY FK_QuestionInArea_crxquestion_id;"| mysql CRX
echo "ALTER TABLE RoomInCategories DROP KEY category_id;"| mysql CRX
echo "ALTER TABLE RoomSmartControlls DROP KEY room_id;"| mysql CRX
echo "ALTER TABLE RoomSmartControlls DROP KEY user_id;"| mysql CRX
echo "ALTER TABLE Rooms DROP KEY creator_id;"| mysql CRX
echo "ALTER TABLE Rooms DROP KEY hwconf_id;"| mysql CRX
echo "ALTER TABLE Sessions DROP KEY user_id;"| mysql CRX
echo "ALTER TABLE Sessions DROP KEY room_id;"| mysql CRX
echo "ALTER TABLE Sessions DROP KEY device_id;"| mysql CRX
echo "ALTER TABLE SoftwareFullNames DROP KEY software_id;"| mysql CRX
echo "ALTER TABLE SoftwareInCategories DROP KEY category_id;"| mysql CRX
echo "ALTER TABLE SoftwareLicenses DROP KEY creator_id;"| mysql CRX
echo "ALTER TABLE SoftwareLicenses DROP KEY software_id;"| mysql CRX
echo "ALTER TABLE SoftwareRemovedFromCategories DROP KEY category_id;"| mysql CRX
echo "ALTER TABLE SoftwareRequirements DROP KEY requirement_id;"| mysql CRX
echo "ALTER TABLE SoftwareStatus DROP KEY device_id;"| mysql CRX
echo "ALTER TABLE SoftwareVersions DROP KEY software_id;"| mysql CRX
echo "ALTER TABLE Softwares DROP KEY creator_id;"| mysql CRX
echo "ALTER TABLE TaskResponses DROP KEY owner_id;"| mysql CRX
echo "ALTER TABLE TaskResponses DROP KEY parent_id;"| mysql CRX
echo "ALTER TABLE Tasks DROP KEY owner_id;"| mysql CRX
echo "ALTER TABLE Tasks DROP KEY parent_id;"| mysql CRX
echo "ALTER TABLE TestFiles DROP KEY test_id;"| mysql CRX
echo "ALTER TABLE TestFiles DROP KEY user_id;"| mysql CRX
echo "ALTER TABLE TestUsers DROP KEY test_id;"| mysql CRX
echo "ALTER TABLE TestUsers DROP KEY user_id;"| mysql CRX
echo "ALTER TABLE TestUsers DROP KEY device_id;"| mysql CRX
echo "ALTER TABLE Tests DROP KEY teacher_id;"| mysql CRX
echo "ALTER TABLE Tests DROP KEY room_id;"| mysql CRX
echo "ALTER TABLE UserInCategories DROP KEY category_id;"| mysql CRX
echo "ALTER TABLE Users DROP KEY creator_id;"| mysql CRX
echo "ALTER TABLE Aliases DROP INDEX aliases;"| mysql CRX
echo "ALTER TABLE Categories DROP INDEX categories;"| mysql CRX
echo "ALTER TABLE Crx2fas DROP INDEX UNQ_Crx2fas_0;"| mysql CRX
echo "ALTER TABLE CrxChallengeAnswers DROP INDEX UNQ_CrxChallengeAnswers_0;"| mysql CRX
echo "ALTER TABLE CrxConfig DROP INDEX CrxConfig;"| mysql CRX
echo "ALTER TABLE Devices DROP INDEX device_names;"| mysql CRX
echo "ALTER TABLE Devices DROP INDEX device_ip;"| mysql CRX
echo "ALTER TABLE Groups DROP INDEX groups_name;"| mysql CRX
echo "ALTER TABLE HWConfs DROP INDEX hwconfs_name;"| mysql CRX
echo "ALTER TABLE Partitions DROP INDEX partitions;"| mysql CRX
echo "ALTER TABLE Printers DROP INDEX printers_name;"| mysql CRX
echo "ALTER TABLE Rooms DROP INDEX rooms_name;"| mysql CRX
echo "ALTER TABLE SoftwareStatus DROP INDEX sv_d;"| mysql CRX
echo "ALTER TABLE Softwares DROP INDEX softwares_name;"| mysql CRX
echo "ALTER TABLE TeachingSubjects DROP INDEX name;"| mysql CRX
echo "ALTER TABLE Users DROP INDEX users_uid;"| mysql CRX
echo "ALTER TABLE AccessInRooms MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE Acls MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE Aliases MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE AnnouncementInCategories MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE Announcements MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE AvailablePrinters MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE Categories MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE ChallengesInArea MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE ChallengesInArea MODIFY COLUMN crxchallenge_id bigint(20) unsigned;"| mysql CRX
echo "ALTER TABLE ChallengesInArea MODIFY COLUMN subjectarea_id bigint(20) unsigned;"| mysql CRX
echo "ALTER TABLE ContactInCategories MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE Contacts MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE Crx2faSessions MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE Crx2faSessions MODIFY COLUMN creator_id bigint(20) unsigned;"| mysql CRX
echo "ALTER TABLE Crx2faSessions MODIFY COLUMN mycrx2fa_id bigint(20) unsigned;"| mysql CRX
echo "ALTER TABLE Crx2faSessions MODIFY COLUMN crx2fa_id bigint(20) unsigned;"| mysql CRX
echo "ALTER TABLE Crx2fas MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE Crx2fas MODIFY COLUMN creator_id bigint(20) unsigned;"| mysql CRX
echo "ALTER TABLE CrxChallengeAnswers MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE CrxChallengeAnswers MODIFY COLUMN creator_id bigint(20) unsigned;"| mysql CRX
echo "ALTER TABLE CrxChallengeAnswers MODIFY COLUMN crxquestionanswer_id bigint(20) unsigned;"| mysql CRX
echo "ALTER TABLE CrxChallenges MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE CrxChallenges MODIFY COLUMN creator_id bigint(20) unsigned;"| mysql CRX
echo "ALTER TABLE CrxChallenges MODIFY COLUMN teachingsubject_id bigint(20) unsigned;"| mysql CRX
echo "ALTER TABLE CrxConfig MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE CrxMConfig MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE CrxNextID MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE CrxQuestionAnswers MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE CrxQuestionAnswers MODIFY COLUMN creator_id bigint(20) unsigned;"| mysql CRX
echo "ALTER TABLE CrxQuestionAnswers MODIFY COLUMN crxquestion_id bigint(20) unsigned;"| mysql CRX
echo "ALTER TABLE CrxQuestions MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE CrxQuestions MODIFY COLUMN creator_id bigint(20) unsigned;"| mysql CRX
echo "ALTER TABLE CrxQuestions MODIFY COLUMN crxchallenge_id bigint(20) unsigned;"| mysql CRX
echo "ALTER TABLE CrxResponse MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE DefaultPrinter MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE DeviceInCategories MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE Devices MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE Enumerates MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE FAQInCategories MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE FAQs MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE GroupInCategories MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE GroupMember MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE Groups MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE GroupsOfChallenges MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE GroupsOfChallenges MODIFY COLUMN crxchallenge_id bigint(20) unsigned;"| mysql CRX
echo "ALTER TABLE GroupsOfChallenges MODIFY COLUMN group_id bigint(20) unsigned;"| mysql CRX
echo "ALTER TABLE HWConfInCategories MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE HWConfs MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE HaveSeen MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE Jobs MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE LicenseToDevice MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE LoggedOn MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE MissedTranslations MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE Partitions MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE PositiveLists MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE Printers MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE QuestionInArea MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE QuestionInArea MODIFY COLUMN subjectarea_id bigint(20) unsigned;"| mysql CRX
echo "ALTER TABLE QuestionInArea MODIFY COLUMN crxquestion_id bigint(20) unsigned;"| mysql CRX
echo "ALTER TABLE Responses MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE RoomInCategories MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE RoomSmartControlls MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE Rooms MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE SEQUENCE MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE Sessions MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE Sessions MODIFY COLUMN crx2fasession_id bigint(20) unsigned;"| mysql CRX
echo "ALTER TABLE SoftwareFullNames MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE SoftwareInCategories MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE SoftwareLicenses MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE SoftwareRemovedFromCategories MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE SoftwareRequirements MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE SoftwareStatus MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE SoftwareVersions MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE Softwares MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE SubjectAreas MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE SubjectAreas MODIFY COLUMN creator_id bigint(20) unsigned;"| mysql CRX
echo "ALTER TABLE SubjectAreas MODIFY COLUMN teachingsubject_id bigint(20) unsigned;"| mysql CRX
echo "ALTER TABLE TaskResponses MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE Tasks MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE TeachingSubjects MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE TeachingSubjects MODIFY COLUMN creator_id bigint(20) unsigned;"| mysql CRX
echo "ALTER TABLE TestFiles MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE TestUsers MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE Tests MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE Translations MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE UserInCategories MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE Users MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE UsersOfChallenges MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
echo "ALTER TABLE UsersOfChallenges MODIFY COLUMN crxchallenge_id bigint(20) unsigned;"| mysql CRX
echo "ALTER TABLE UsersOfChallenges MODIFY COLUMN user_id bigint(20) unsigned;"| mysql CRX
echo "ALTER TABLE Devices MODIFY COLUMN wlanIp varchar(16) DEFAULT NULL;"| mysql CRX
echo "UPDATE Devices SET wlanIp=NULL where wlanIp = '';"| mysql CRX
echo "UPDATE CrxQuestions SET crxchallenge_id=NULL WHERE crxchallenge_id NOT IN (SELECT id FROM CrxChallenges);"| mysql CRX

if [ $SERVICE == "cephalix-api" ]; then
	echo "ALTER TABLE CephalixArticles DROP FOREIGN KEY CephalixArticles_ibfk_1;"| mysql CRX
	echo "ALTER TABLE CephalixArticles DROP FOREIGN KEY FK_CephalixArticles_cephalixticket_id;"| mysql CRX
	echo "ALTER TABLE CephalixCareMessages DROP FOREIGN KEY CephalixCareMessages_ibfk_1;"| mysql CRX
	echo "ALTER TABLE CephalixCareMessages DROP FOREIGN KEY FK_CephalixCareMessages_CEPHALIXCARE_ID;"| mysql CRX
	echo "ALTER TABLE CephalixCares DROP FOREIGN KEY CephalixCares_ibfk_2;"| mysql CRX
	echo "ALTER TABLE CephalixCares DROP FOREIGN KEY FK_CephalixCares_CEPHALIXINSTITUTE_ID;"| mysql CRX
	echo "ALTER TABLE CephalixDynDns DROP FOREIGN KEY CephalixDynDns_ibfk_2;"| mysql CRX
	echo "ALTER TABLE CephalixDynDns DROP FOREIGN KEY FK_CephalixDynDns_CEPHALIXINSTITUTE_ID;"| mysql CRX
	echo "ALTER TABLE CephalixITUsage DROP FOREIGN KEY CephalixITUsage_ibfk_1;"| mysql CRX
	echo "ALTER TABLE CephalixITUsage DROP FOREIGN KEY FK_CephalixITUsage_CEPHALIXINSTITUTE_ID;"| mysql CRX
	echo "ALTER TABLE CephalixITUsageAvarage DROP FOREIGN KEY CephalixITUsageAvarage_ibfk_1;"| mysql CRX
	echo "ALTER TABLE CephalixITUsageAvarage DROP FOREIGN KEY FK_CephalixITUsageAvarage_CEPHALIXINSTITUTE_ID;"| mysql CRX
	echo "ALTER TABLE CephalixInstitutes DROP FOREIGN KEY CephalixInstitutes_ibfk_1;"| mysql CRX
	echo "ALTER TABLE CephalixInstitutes DROP FOREIGN KEY FK_CephalixInstitutes_CEPHALIXCUSTOMER_ID;"| mysql CRX
	echo "ALTER TABLE CephalixMappings DROP FOREIGN KEY CephalixMappings_ibfk_1;"| mysql CRX
	echo "ALTER TABLE CephalixMappings DROP FOREIGN KEY FK_CephalixMappings_cephalixinstitute_id;"| mysql CRX
	echo "ALTER TABLE CephalixNotices DROP FOREIGN KEY CephalixNotices_ibfk_1;"| mysql CRX
	echo "ALTER TABLE CephalixNotices DROP FOREIGN KEY FK_CephalixNotices_cephalixinstitute_id;"| mysql CRX
	echo "ALTER TABLE CephalixOssCareMessages DROP FOREIGN KEY CephalixOssCareMessages_ibfk_1;"| mysql CRX
	echo "ALTER TABLE CephalixOssCares DROP FOREIGN KEY CephalixOssCares_ibfk_2;"| mysql CRX
	echo "ALTER TABLE CephalixRepositoriesToCustomer DROP FOREIGN KEY CephalixRepositoriesToCustomer_cephalixcustomer_id;"| mysql CRX
	echo "ALTER TABLE CephalixRepositoriesToCustomer DROP FOREIGN KEY CephalixRepositoriesToCustomer_ibfk_1;"| mysql CRX
	echo "ALTER TABLE CephalixRepositoriesToCustomer DROP FOREIGN KEY CephalixRepositoriesToCustomer_ibfk_2;"| mysql CRX
	echo "ALTER TABLE CephalixRepositoriesToCustomer DROP FOREIGN KEY CephalixRepositoriesToCustomercephalixrepositoryid;"| mysql CRX
	echo "ALTER TABLE CephalixRepositoriesToInstitute DROP FOREIGN KEY CephalixRepositoriesToInstitute_ibfk_1;"| mysql CRX
	echo "ALTER TABLE CephalixRepositoriesToInstitute DROP FOREIGN KEY CephalixRepositoriesToInstitute_ibfk_2;"| mysql CRX
	echo "ALTER TABLE CephalixRepositoriesToInstitute DROP FOREIGN KEY CephalixRepositoriesToInstitutecephalixinstituteid;"| mysql CRX
	echo "ALTER TABLE CephalixRepositoriesToInstitute DROP FOREIGN KEY CphalixRepositoriesToInstitutecephalixrepositoryid;"| mysql CRX
	echo "ALTER TABLE CephalixSupervisor DROP FOREIGN KEY CephalixSupervisor_ibfk_1;"| mysql CRX
	echo "ALTER TABLE CephalixSupervisor DROP FOREIGN KEY CephalixSupervisor_ibfk_2;"| mysql CRX
	echo "ALTER TABLE CephalixSystemStatus DROP FOREIGN KEY CephalixSystemStatus_ibfk_1;"| mysql CRX
	echo "ALTER TABLE CephalixSystemStatus DROP FOREIGN KEY FK_CephalixSystemStatus_CEPHALIXINSTITUTE_ID;"| mysql CRX
	echo "ALTER TABLE CephalixTickets DROP FOREIGN KEY CephalixTickets_ibfk_1;"| mysql CRX
	echo "ALTER TABLE CephalixTickets DROP FOREIGN KEY FK_CephalixTickets_cephalixinstitute_id;"| mysql CRX
	echo "ALTER TABLE CephalixArticles DROP KEY cephalixticket_id;"| mysql CRX
	echo "ALTER TABLE CephalixCareMessages DROP KEY cephalixcare_id;"| mysql CRX
	echo "ALTER TABLE CephalixCares DROP KEY cephalixinstitute_id;"| mysql CRX
	echo "ALTER TABLE CephalixDynDns DROP KEY cephalixinstitute_id;"| mysql CRX
	echo "ALTER TABLE CephalixITUsage DROP KEY cephalixinstitute_id;"| mysql CRX
	echo "ALTER TABLE CephalixITUsageAvarage DROP KEY cephalixinstitute_id;"| mysql CRX
	echo "ALTER TABLE CephalixInstitutes DROP KEY cephalixcustomer_id;"| mysql CRX
	echo "ALTER TABLE CephalixMappings DROP KEY cephalixinstitute_id;"| mysql CRX
	echo "ALTER TABLE CephalixNotices DROP KEY cephalixinstitute_id;"| mysql CRX
	echo "ALTER TABLE CephalixOssCareMessages DROP KEY cephalixosscare_id;"| mysql CRX
	echo "ALTER TABLE CephalixOssCares DROP KEY cephalixinstitute_id;"| mysql CRX
	echo "ALTER TABLE CephalixRepositoriesToCustomer DROP KEY cephalixcustomer_id;"| mysql CRX
	echo "ALTER TABLE CephalixRepositoriesToInstitute DROP KEY cephalixinstitute_id;"| mysql CRX
	echo "ALTER TABLE CephalixSupervisor DROP KEY cephalixinstitute_id;"| mysql CRX
	echo "ALTER TABLE CephalixSupervisor DROP KEY cephalixcustomer_id;"| mysql CRX
	echo "ALTER TABLE CephalixSystemStatus DROP KEY cephalixinstitute_id;"| mysql CRX
	echo "ALTER TABLE CephalixTickets DROP KEY cephalixinstitute_id;"| mysql CRX
	echo "ALTER TABLE Cephalix2faMapping MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
	echo "ALTER TABLE Cephalix2faMapping MODIFY COLUMN creator_id bigint(20) unsigned;"| mysql CRX
	echo "ALTER TABLE Cephalix2faMapping MODIFY COLUMN cephalixinstitute_id bigint(20) unsigned;"| mysql CRX
	echo "ALTER TABLE CephalixArticles MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
	echo "ALTER TABLE CephalixCareMessages MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
	echo "ALTER TABLE CephalixCares MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
	echo "ALTER TABLE CephalixCustomers MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
	echo "ALTER TABLE CephalixDynDns MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
	echo "ALTER TABLE CephalixITUsage MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
	echo "ALTER TABLE CephalixITUsageAvarage MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
	echo "ALTER TABLE CephalixInstitutes MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
	echo "ALTER TABLE CephalixMappings MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
	echo "ALTER TABLE CephalixNotices MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
	echo "ALTER TABLE CephalixOssCareMessages MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
	echo "ALTER TABLE CephalixOssCares MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
	echo "ALTER TABLE CephalixRepositories MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
	echo "ALTER TABLE CephalixRepositoriesToCustomer MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
	echo "ALTER TABLE CephalixRepositoriesToInstitute MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
	echo "ALTER TABLE CephalixSupervisor MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
	echo "ALTER TABLE CephalixSystemStatus MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
	echo "ALTER TABLE CephalixTickets MODIFY COLUMN id bigint(20) unsigned NOT NULL AUTO_INCREMENT;"| mysql CRX
	echo "ALTER TABLE CephalixDynDns DROP INDEX ddnshostdom;"| mysql CRX
fi
/usr/bin/systemctl start $SERVICE
/usr/share/cranix/tools/wait-for-api.sh

for table in Announcements Categories Contacts Devices FAQs Groups PositiveLists TaskResponses
do
	echo "$table"
	echo "UPDATE $table set creator_id = owner_id" | mysql CRX
	echo "ALTER TABLE $table DROP COLUMN owner_id" | mysql CRX
done
if [ $SERVICE == "cephalix-api" ]; then
	echo "UPDATE CephalixTickets set creator_id = ownerId ;" | mysql CRX
	echo "ALTER TABLE CephalixTickets DROP COLUMN ownerId;"  | mysql CRX
	for table in CephalixArticles CephalixCustomers CephalixInstitutes CephalixSystemStatus CephalixTickets
	do
		echo "$table"
		echo "UPDATE $table SET created = recDate" | mysql CRX
		echo "UPDATE $table SET modified = recDate" | mysql CRX
		echo "ALTER TABLE $table DROP COLUMN recDate" | mysql CRX
	done
	echo "UPDATE CephalixMappings set creator_id=1;" | mysql CRX
fi
/usr/bin/systemctl stop $SERVICE
sleep 5
/usr/bin/systemctl start $SERVICE
