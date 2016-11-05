#!/bin/bash

set -eu

HERE=$(dirname "$0")

. "$HERE/functions.sh"

IWANT_SCRIPT=$1
shift
export IWANT_DEST=$1
shift
IWANT_DEPREFS=$1
shift

iwant-log "Refreshing $IWANT_SCRIPT $@"

param() {
    _set-var-to-value "$@"
}

_set-var-to-value() {
    local NAME=$1
    local VALUE=$2
    iwant-filelog "Resolved ingredient: $NAME=$VALUE"
    export $NAME="$VALUE"
}

target-dep() {
    _set-var-to-cached "$@"
}

source-dep() {
    _set-var-to-cached "$@"
}

_set-var-to-cached() {
    local NAME=$1
    local REF=$2
    local VALUE=$(iwant-cached "$REF")
    _set-var-to-value "$NAME" "$VALUE"
}

path() {
    die "Please define function 'path'"
}

. "$IWANT_SCRIPT"

iwant-filelog "Resolving ingredient values"
ingredients "$@"
iwant-filelog "Refreshing"
path
