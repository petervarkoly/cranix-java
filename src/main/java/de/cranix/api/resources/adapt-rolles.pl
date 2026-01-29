#!/usr/bin/perl
use strict;

my $text=`grep \@RolesAllowed *.java`;

my @USERROLES  = ( 'students', 'teachers', 'sysadmins', 'workstations', 'guests' );
my @GROUPTYPES = ( 'primary', 'class', 'workgroup', 'guests' );
my @ROLES_TO_DELETE = ('education.groups', 'education.guestusers', 'education.rooms', 'education.users', 'user.guestusers');

my $hroles = {};
my $forTeachers = {};

my @ROLES = ($text =~ /"(.+?)"/gs);
foreach( @ROLES )
{
	my $r = $_;
	if( $r =~ /^2fa/ ) {
		next;
	}
	if( $r !~ /\./ ) {
		next;
	}
	$hroles->{$r} = 1;
	if( $r =~ /education/  || $r =~ /information/ ) {
		next if( $r =~ /softwares*/ );
		$forTeachers->{$r} = 1;
	}

}
foreach( sort keys %$hroles )
{
	print "/usr/sbin/crx_api.sh PUT  system/enumerates/apiAcl/$_\n";
}
print "mkdir -p /var/adm/cranix/roles-adapted\n";
foreach( sort keys %$hroles )
{
	my $path = "/var/adm/cranix/roles-adapted/1-$_";
	print "test -e $path || ( touch $path; /usr/sbin/crx_api.sh POST system/acls/groups/1 '{\"acl\":\"$_\",\"allowed\":true,\"userId\":null,\"groupId\":1}' )\n";
}

foreach( sort keys %$forTeachers )
{
	my $path = "/var/adm/cranix/roles-adapted/2-$_";
	print "test -e $path || ( touch $path; /usr/sbin/crx_api.sh POST system/acls/groups/2 '{\"acl\":\"$_\",\"allowed\":true,\"userId\":null,\"groupId\":2}' )\n";
}
#Additional rights for teachers
foreach( ('calendar.use') ) {
	my $path = "/var/adm/cranix/roles-adapted/2-$_";
	print "test -e $path || ( touch $path; /usr/sbin/crx_api.sh POST system/acls/groups/2 '{\"acl\":\"$_\",\"allowed\":true,\"userId\":null,\"groupId\":2}' )\n";
}
#Additional rights for students
foreach( ('calendar.read') ) {
	my $path = "/var/adm/cranix/roles-adapted/3-$_";
	print "test -e $path || ( touch $path; /usr/sbin/crx_api.sh POST system/acls/groups/3 '{\"acl\":\"$_\",\"allowed\":true,\"userId\":null,\"groupId\":3}' )\n";
}

for( @ROLES_TO_DELETE )
{
	print "echo 'DELETE FROM Enumerates WHERE value=\'$_\';'| mysql CRX\n";
	print "echo 'DELETE FROM Acls WHERE Acl=\'$_\';'| mysql CRX\n";
}
