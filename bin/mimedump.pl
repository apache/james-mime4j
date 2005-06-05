#!/usr/bin/perl -w

use MIME::Base64;
use MIME::Decoder;
use MIME::Parser;
use MIME::QuotedPrint;
use File::Basename;

sub dump_header {
    my ($entity) = @_;
    
    print "<header>\r\n";
    $_ = $entity->head->as_string();#original_text;
    s/&/&amp;/g;
    s/</&lt;/g;
    s/>/&gt;/g;
    s/^([^ \t].*(\n[ \t]+.*)*)$/<field>\n$1\n<\/field>/gm;
    s/([^\r])\n/$1\r\n/g;
    s/\r\n<\/field>/<\/field>/gm;
    print $_;
    print "</header>\r\n";
}

sub dump_preamble {
    my ($entity) = @_;
    my $preamble = $entity->preamble;

    print "<preamble>\r\n";
    foreach (@$preamble) {
        s/&/&amp;/g;
        s/</&lt;/g;
        s/>/&gt;/g;
        s/^\n/\r\n/;
        s/([^\r])\n/$1\r\n/g;
        if ($_ ne "") {
            print "$_";
        }
    }
    print "</preamble>\r\n";
}

sub dump_epilogue {
    my ($entity) = @_;
    my $epilogue = $entity->epilogue;

    print "<epilogue>\r\n";
    foreach (@$epilogue) {
        s/&/&amp;/g;
        s/</&lt;/g;
        s/>/&gt;/g;
        s/^\n/\r\n/;
        s/([^\r])\n/$1\r\n/g;
        if ($_ ne "") {
            print "$_";
        }
    }
    print "</epilogue>\r\n";
}

sub dump_entity {
    my ($entity, $decode, $prefix, $id) = @_;
    my $IO;

    dump_header($entity);

    my ($type, $subtype) = split('/', $entity->head->mime_type);
    my @parts = $entity->parts;
    if (@parts) {                     # multipart...

        if ($type =~ /^message$/i) {
            print "<message>\r\n";
            dump_entity($parts[0], $decode, $prefix, $id . "_1");
            print "</message>\r\n";
        } else {
            print "<multipart>\r\n";
            dump_preamble($entity);
            my $i;
            foreach $i (0 .. $#parts) {
                print "<body-part>\r\n";
                dump_entity($parts[$i], $decode, $prefix, $id . "_" . ($i+1));
                print "</body-part>\r\n";
            }
            dump_epilogue($entity);
            print "</multipart>\r\n";
        }
        
    } else {

        my $body = $entity->bodyhandle;
        
        if ($decode) {
            $file = "$prefix" . "_" . "$id";
            if ($type eq "text") {
                $file = $file . ".txt";
                $tag = "text-body";
            } else {
                $file = $file . ".bin";
                $tag = "binary-body";
            }

            print "<$tag name=\"" . basename($file) . "\"/>\r\n";
            if (defined($body)) {
                open(OUT, ">" . $file);
                $body->print(\*OUT);
            }
        } else {

            if (defined($body)) {
            
                # Check if the body contains an embedded message encoded using base64 or qp
            
                if ($type =~ /^message$/i) {
                    print "<message>\r\n";
                
                    my $new_parser = new MIME::Parser;
                    $new_parser->extract_uuencode(0);
                    $new_parser->extract_encoded_messages(0);

                    # Do the base64 or qp decoding manually
                    my $decoded;
                    if ($entity->head->mime_encoding eq "base64") {
                        $decoded = decode_base64($body->as_string);
                    } else {
                        $decoded = decode_qp($body->as_string);
                    }
                    open(IN, '<', \$decoded);
                    my $new_entity = $new_parser->read(\*IN) or die "couldn't parse MIME stream";
                    dump_entity($new_entity, $decode, $prefix, $id . "_1");
                    
                    print "</message>\r\n";
                    
                } else {
                
                    $_ = $body->as_string;
                    s/&/&amp;/g;
                    s/</&lt;/g;
                    s/>/&gt;/g;
                    s/^\n/\r\n/mg;
                    s/([^\r])\n/$1\r\n/g;
                    print "<body>\r\n$_</body>\r\n";
                    
                }
            }
        
        }
    }
}


$decode = 0;
if (defined($ARGV[0])) {
    if ($ARGV[0] eq "-decode") {
        $decode = 1;
        shift(@ARGV);
    }
}

if (defined($ARGV[0])) {
    $file = $ARGV[0];
}

!$decode or defined($file) and $file ne "" or die "specifiy a file name prefix";

if (defined($file) and $file ne "") {
    open(XMLOUT, ">" . $file . ".xml");
    select(XMLOUT);
}

uninstall MIME::Decoder 'x-uu', 'x-uuencode', 'x-gzip64';
if (!$decode) {
    uninstall MIME::Decoder 'base64', 'quoted-printable';
}

# Create a new MIME parser:
$parser = new MIME::Parser;
$parser->extract_uuencode(0);
#$parser->output_to_core(1);
$parser->extract_encoded_messages(0);

# Read the MIME message:
$entity = $parser->read(\*STDIN) or die "couldn't parse MIME stream";

print "<message>\r\n";
dump_entity($entity, $decode, $file, "1");
print "</message>\r\n";
