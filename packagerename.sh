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
	cat
}

handle-srcdir() {
    local SRCDIR=$1
    [ -e "$SRCDIR/net/sf/iwant" ] || return 0
    echo
    echo "#handling $SRCDIR/net/sf/iwant"
    echo "mkdir -p $SRCDIR/org/fluentjava"
    echo "git mv $SRCDIR/net/sf/iwant $SRCDIR/org/fluentjava/"
}



all-srcdirs | without-caches-etc | while read SRCDIR; do
    handle-srcdir "$SRCDIR"
done
