#!/bin/bash

set -eu

AS_SOMEONE=$(dirname "$0")/../../..
AS_SOMEONE=$(cd "$AS_SOMEONE" && pwd)

# TODO what's a good path, how to define this only once:
CACHED=$AS_SOMEONE/with/bash/iwant/.cached

CLASSES=$CACHED/.internal/entry-classes
mkdir -p "$CLASSES"
javac -d "$CLASSES" "$AS_SOMEONE/with/java/net/sf/iwant/entry/Iwant.java"

java -cp "$CLASSES" net.sf.iwant.entry.Iwant "$AS_SOMEONE" "$@"
