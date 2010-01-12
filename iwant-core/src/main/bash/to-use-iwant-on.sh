#!/bin/bash

set -eu

if [ $# != 4 ]; then
    echo "Usage: $0 WSNAME WSROOT WSSRC WSDEFCLASS"
    exit 1
fi

WSNAME="$1"
WSROOT=$(cd "$2" && pwd)
WSSRC=$(cd "$3" && pwd)
WSDEFCLASS="$4"

here=$(dirname "$0")
iwant="$($here/iwant-path.sh)"

function createscript() {
    "$iwant/cached/iwant/scripts/createscript.sh" \
	"$WSNAME" \
	"$WSROOT" \
	"$1" \
	"$WSSRC" \
	"$WSDEFCLASS" \
	"$2"
}

createscript "help" ""
createscript "list-of/targets" " | $iwant/cached/iwant/scripts/create-target-scripts.sh \"$WSNAME\" \"$WSROOT\" \"$WSSRC\" \"$WSDEFCLASS\""

echo To get access to targets of the $WSNAME workspace, start your sentences with
echo \$ iwant/as-$WSNAME-developer
