#!/bin/bash

set -eu

HERE=$(dirname "$0")

. "$HERE/functions.sh"

IWANT_TARGETS_SCRIPT=$1
IWANT_TARGETS_OUT=$2

iwant-log "Determining targets from $IWANT_TARGETS_SCRIPT"

target() {
    _target "$@" >> "$IWANT_TARGETS_OUT"
}

_target() {
    local NAME=$(indented "$1")
    shift
    iwant-log "  $NAME"
    echo ":$NAME"
    while [ $# -gt 0 ]; do
	local ARG=$(indented "$1")
	echo ":$ARG"
	shift
    done
    echo "::"
}

. "$IWANT_TARGETS_SCRIPT"
targets
