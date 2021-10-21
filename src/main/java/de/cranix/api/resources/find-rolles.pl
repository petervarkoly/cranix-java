#!/usr/bin/perl
use strict;

my $ROLES=`grep \@RolesAllowed *`;

my @USERROLES  = ( 'students', 'teachers', 'sysadmins', 'workstations' );
my @GROUPTYPES = ( 'primary', 'class', 'workgroup' );

my $hroles = {};
my $forTeachers = {};

foreach( split /\n/, $ROLES )
{
	if(	/\@RolesAllowed\("(.*)"\)/ )
	{
		my $r = $1;
		next if( $r eq "printers.add" );
		$hroles->{$1} = 1;
		if( $r =~ /education/  || $r =~ /information/ ) {
			next if( $r =~ /softwares*/ );
			$forTeachers->{$r} = 1;
		}
	}

}
foreach( sort keys %$hroles )
{
	print "INSERT INTO Enumerates VALUES(NULL,'apiAcl','$_',6);\n";
}
foreach( sort keys %$hroles )
{
	print "INSERT INTO Acls VALUES(NULL,NULL,1,'$_','Y',6);\n";
}

foreach( sort keys %$forTeachers )
{
	print "INSERT INTO Acls VALUES(NULL,NULL,2,'$_','Y',6);\n";
}
print "INSERT INTO Acls VALUES(NULL,1,NULL,'printers.add','Y',6);\n";

foreach( @USERROLES ) {
	print "INSERT INTO Enumerates VALUES(NULL,'apiAcl','user.add.$_',6);\n";
	print "INSERT INTO Enumerates VALUES(NULL,'apiAcl','user.delete.$_',6);\n";
	print "INSERT INTO Enumerates VALUES(NULL,'apiAcl','user.modify.$_',6);\n";
}
foreach( @GROUPTYPES ) {
	print "INSERT INTO Enumerates VALUES(NULL,'apiAcl','group.add.$_',6);\n";
	print "INSERT INTO Enumerates VALUES(NULL,'apiAcl','group.delete.$_',6);\n";
	print "INSERT INTO Enumerates VALUES(NULL,'apiAcl','group.modify.$_',6);\n";
}
print "INSERT INTO Acls VALUES(NULL,NULL,2,'group.add.workgroup','Y',6);\n";
print "INSERT INTO Acls VALUES(NULL,NULL,2,'group.delete.workgroup','Y',6);\n";
print "INSERT INTO Acls VALUES(NULL,NULL,2,'group.modify.workgroup','Y',6);\n";
