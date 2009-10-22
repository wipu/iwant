#!/bin/bash

set -eu

if [ $# != 3 ]; then
    echo "Usage: $0 WSNAME WSSRC WSDEFCLASS"
    exit 1
fi

WSNAME="$1"
WSSRC=$(cd "$2"; pwd)
WSDEFCLASS="$3"

here=$(dirname "$0")
iwant="$($here/iwant-path.sh)"

function createscript() {
    "$iwant/cached/iwant/scripts/createscript.sh" \
	"$WSNAME" \
	"$1" \
	"$WSSRC" \
	"$WSDEFCLASS" \
	"$2"
}

createscript "help" ""
createscript "list-of/targets" " | $iwant/cached/iwant/scripts/create-target-scripts.sh \"$WSNAME\" \"$WSSRC\" \"$WSDEFCLASS\""

echo To get access to targets of the $WSNAME workspace, start your sentences with
echo \$ iwant/as-$WSNAME-developer
