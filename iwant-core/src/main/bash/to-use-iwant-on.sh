#!/bin/bash

set -eu

if [ $# != 4 ]; then
    echo "Usage: $0 WSNAME WSROOT WSSRC WSDEFCLASS"
    exit 1
fi

here=$(dirname "$0")
iwant=$here/..
. "$iwant/cached/iwant/scripts/iwant-functions.sh"

WSNAME="$1"
WSROOT=$(abs "$2")
WSSRC=$(abs "$3")
WSDEFCLASS="$4"

ws-script() {
  createscript \
    "$WSNAME" \
    "$WSROOT" \
    "$1" \
    "$WSSRC" \
    "$WSDEFCLASS" \
    "$2"
}

ws-script "help" ""
ws-script "list-of/targets" " | $iwant/cached/iwant/scripts/create-target-scripts.sh \"$WSNAME\" \"$WSROOT\" \"$WSSRC\" \"$WSDEFCLASS\""

echo To get access to targets of the $WSNAME workspace, start your sentences with
echo \$ iwant/as-$WSNAME-developer
