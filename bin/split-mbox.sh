#!/bin/bash

BASE_DIR=`dirname $0`/..

if [ -z $1 ]; then
    "Please specify an mbox file to split"
    exit
fi

sed -e 's/^$/\x0D/' -e 's/\([^\x0D]\)$/\1\x0D/g' "$1" | $BASE_DIR/bin/split-mbox.pl

for i in message.*; do 
    echo "Renaming $i"
    sed -e '/^From /d' $i > $i.tmp
    rm -f $i
    mv $i.tmp `md5sum $i.tmp | awk '{print $1}'`.msg
done
