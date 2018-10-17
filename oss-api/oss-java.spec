#
# spec file for package oss-java
#
# Copyright (c) 2018 Peter Varkoly, Nuernberg, Germany.
#

Name:           oss-java
Version:	1.0
Release:	@RELEASE@
License:	GPL-2.0+ and LGPL-2.1+
Vendor:         Dipl.Ing. Peter Varkoly <peter@varkoly.de>
Summary:	Java Libraries and API for OSS
Url:		http://www.openscholserver.net
Group:		Applications/Productivity
Source:		oss-java.tar.bz2
Source1:	oss-api.properties
Source2:	config.yml	
Source3:	start-oss-api
Source4:	oss-api.service
Source5:	turn-to-new-printer.sh
Source6:	oss-objects.sql
Source7:	school-INSERT.sql
Source8:	business-INSERT.sql
Requires: 	systemd 
Requires:	oss-base
BuildArch:      noarch
BuildRoot:      %{_tmppath}/%{name}-%{version}-build

# For update from OSS-3-4
Provides:       lmd
Obsoletes:      lmd

#For compatibility oss cephalix cranix
Provides:       java-base-api

%description

%prep
%setup -q -n oss-java

%build

%install
mkdir -p %{buildroot}/opt/oss-java/{conf,data,tmp}
mkdir -p %{buildroot}/usr/lib/systemd/system/
mv *  %{buildroot}/opt/oss-java/
cp %{SOURCE1}  %{buildroot}/opt/oss-java/conf/
cp %{SOURCE2}  %{buildroot}/opt/oss-java/conf/
cp %{SOURCE3}  %{buildroot}/opt/oss-java/bin/
cp %{SOURCE4}  %{buildroot}/usr/lib/systemd/system/
cp %{SOURCE5}  %{buildroot}/opt/oss-java/bin/
cp %{SOURCE6}  %{buildroot}/opt/oss-java/data/
cp %{SOURCE7}  %{buildroot}/opt/oss-java/data/
cp %{SOURCE8}  %{buildroot}/opt/oss-java/data/

%pre
%service_add_pre oss-api.service

%preun
%service_del_preun oss-api.service
 
%post
/opt/oss-java/bin/turn-to-new-printer.sh
%service_add_post oss-api.service

%postun
%service_del_postun oss-api.service

%files
#%doc ChangeLog README COPYING
%defattr(640,root,root,751)
%dir /opt/oss-java/
%dir /opt/oss-java/conf/
%dir /opt/oss-java/tmp/
%config(noreplace) /opt/oss-java/conf/*
/opt/oss-java/data/
%defattr(644,root,root,751)
/opt/oss-java/lib
/usr/lib/systemd/system/oss-api.service
%defattr(750,root,root,750)
/opt/oss-java/bin/
