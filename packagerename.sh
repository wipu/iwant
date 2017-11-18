#!/bin/bash

set -eu

HERE=$(dirname "$0")
cd "$HERE"

srcdirs() {
    local TYPE=$1
    find . -type d -path '*/'"$TYPE"
}

all-srcdirs() {
    srcdirs src/main/java
    srcdirs src/main/resources
    srcdirs src/test/java
    srcdirs src/test/resources
    srcdirs as-some-developer/with/java
    echo ./private/iwant-tutorial-wsdefs/src
}

without-caches-etc() {
    cat |
	grep -v '^./private/iwant-testarea/testarea-root' |
	grep -v '^./as-iwant-developer/.i-cached/' |
	grep -v '^./as-iwant-developer/i-have' |
	grep -v '^./as-iwant-developer/with' |
	grep -v '^./iwant-distillery' |
	grep -v '^./packagerename.sh' |
	cat
}

handle-srcdir() {
    local SRCDIR=$1
    [ -e "$SRCDIR/org/fluentjava/iwant" ] || return 0
    echo
    echo "#handling $SRCDIR/org/fluentjava/iwant"
    echo "mkdir -p $SRCDIR/org/fluentjava"
    echo "git mv $SRCDIR/org/fluentjava/iwant $SRCDIR/org/fluentjava/"
}



all-srcdirs | without-caches-etc | while read SRCDIR; do
    handle-srcdir "$SRCDIR"
done

## then the file contents:

javafiles-to-fix() {
    find . -name '*.java' -o -name '*.sh' | without-caches-etc
}

javafiles-to-fix | while read JAVA; do
    echo "sed -i 's/net\.sf\.iwant/org.fluentjava.iwant/g' $JAVA"
    echo "sed -i 's:net/sf/iwant:org/fluentjava/iwant:g' $JAVA"
done
