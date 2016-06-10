#!/bin/bash

set -eu

HERE=$(dirname "$0")
FUNCTIONS=$1

. "$FUNCTIONS"

echo "Testing $FUNCTIONS"

assert-equals() {
    echo assert-equals "$@"
    [ "$1" == "$2" ] || die "fail"
    echo ok
}

run-test() {
    local TEST="$HERE/${1}.sh"
    echo "Running $TEST"
    . "$TEST"
}

run-test indented-test
run-test resolve-path-test
