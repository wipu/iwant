#!/bin/bash

set -eu

HERE=$(dirname "$0")
HERE=$(readlink -f "$HERE")

cd "$HERE"

fresh-dir() {
  DIR=$1
  rm -rf "$DIR"
  mkdir -p "$DIR"
}

CACHED=$HERE/cached

JUNIT=$HERE/../as-iwant-developer/with/bash/iwant/cached/.internal/unmodifiable/junit-3.8.1.jar

CLASSES_TO_TEST=$CACHED/classes-to-test
fresh-dir "$CLASSES_TO_TEST"
javac \
  -d "$CLASSES_TO_TEST" \
  -cp "$JUNIT" \
  $(find $HERE/src -name '*.java')

java -cp "$CLASSES_TO_TEST:$JUNIT" junit.textui.TestRunner net.sf.iwant.entry.IwantEntrySuite

DESCRNAMEBASE=$HERE/java-bootstrapping

IWANT_DISTILLERY=$HERE $HERE/../iwant-lib-descript/descript.sh "$DESCRNAMEBASE".sh "$DESCRNAMEBASE".html true

