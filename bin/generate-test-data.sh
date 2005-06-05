#!/bin/bash

BASE_DIR=`dirname $0`/..

DIR=$1

if [ -z $1 ]; then
    DIR=$BASE_DIR/testmsgs
fi

for i in $DIR/*.msg; do
    echo Creating ${i%%.msg}.xml
    $BASE_DIR/bin/mimedump.pl ${i%%.msg} < $i
    echo Creating ${i%%.msg}.decoded.xml
    $BASE_DIR/bin/mimedump.pl -decode ${i%%.msg}_decoded < $i
done
