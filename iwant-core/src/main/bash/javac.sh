#!/bin/bash

DEST="$1"
shift
SRC="$1"
shift

CP=""
while [ x != "x$1" ]; do
    CP="$CP$1:"
    shift
done

set -eu

CP=$(echo $CP | sed 's/:$//')

echo -e "  Compiling $DEST..."
mkdir -p "$DEST"
JAVAFILES=$(find "$SRC" -name '*.java')
javac -d "$DEST" -classpath "$CP" "$JAVAFILES"
