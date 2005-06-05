#!/usr/bin/perl -pl

if ( /^From / ) {
	close(OUT);
	open(OUT, ">>message.".$i++) || die "Can't open new file!  $i\n";
	select(OUT);
	print STDERR "Opened $i";
}
