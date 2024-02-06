#!/usr/bin/perl
use strict;

my $ROLES=`grep \@RolesAllowed *`;

my @USERROLES  = ( 'students', 'teachers', 'sysadmins', 'workstations', 'guests' );
my @GROUPTYPES = ( 'primary', 'class', 'workgroup', 'guests' );

my $hroles = {};
my $forTeachers = {};

foreach( split /\n/, $ROLES )
{
	if(	/\@RolesAllowed\("(.*)"\)/ )
	{
		my $r = $1;
		$hroles->{$1} = 1;
		if( $r =~ /education/  || $r =~ /information/ ) {
			next if( $r =~ /softwares*/ );
			$forTeachers->{$r} = 1;
		}
	}

}
foreach( sort keys %$hroles )
{
	print "INSERT INTO Enumerates SET name='apiAcl',value='$_';\n";
}
foreach( sort keys %$hroles )
{
	print "INSERT INTO Acls SET group_id=1,acl='$_',allowed='Y';\n";
}

foreach( sort keys %$forTeachers )
{
	print "INSERT INTO Acls SET group_id=2,acl='$_',allowed='Y';\n";
}

foreach( @USERROLES ) {
	print "INSERT INTO Enumerates SET name='apiAcl',value='user.add.$_';\n";
	print "INSERT INTO Enumerates SET name='apiAcl',value='user.delete.$_';\n";
	print "INSERT INTO Enumerates SET name='apiAcl',value='user.modify.$_';\n";
}
foreach( @GROUPTYPES ) {
	print "INSERT INTO Enumerates VALUES(NULL,'apiAcl','group.add.$_';\n";
	print "INSERT INTO Enumerates VALUES(NULL,'apiAcl','group.delete.$_';\n";
	print "INSERT INTO Enumerates VALUES(NULL,'apiAcl','group.modify.$_';\n";
}
print "INSERT INTO Acls SET group_id=2,acl='group.add.guests',allowed='Y';\n";
print "INSERT INTO Acls SET group_id=2,acl='user.add.guests',allowed='Y';\n";
print "INSERT INTO Acls SET group_id=2,acl='group.add.workgroup',allowed='Y';\n";
print "INSERT INTO Acls SET group_id=2,acl='group.delete.workgroup',allowed='Y';\n";
print "INSERT INTO Acls SET group_id=2,acl='group.modify.workgroup',allowed='Y';\n";
