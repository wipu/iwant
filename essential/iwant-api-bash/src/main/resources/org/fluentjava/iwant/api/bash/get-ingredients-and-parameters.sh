#!/bin/bash

set -eu

HERE=$(dirname "$0")

. "$HERE/functions.sh"

IWANT_INGR_SCRIPT=$1
shift
IWANT_INGR_OUT=$1
shift

iwant-filelog "Determining ingredients of $IWANT_INGR_SCRIPT $@"

param() {
    _ingr param "$@"
}

target-dep() {
    _ingr target-dep "$@"
}

source-dep() {
    _ingr source-dep "$@"
}

_ingr() {
    _ingr-content "$@" >> "$IWANT_INGR_OUT"
}

_ingr-content() {
    TYPE=$(indented "$1")
    NAME=$(indented "$2")
    VALUE=$(indented "$3")
    echo ":$TYPE"
    echo ":$NAME"
    echo ":$VALUE"
    echo "::"
}

. "$IWANT_INGR_SCRIPT"
ingredients "$@"
