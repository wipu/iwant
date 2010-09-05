#!/bin/bash

set -eu

if [ $# != 4 ]; then
    echo "Usage: $0 WSNAME WSROOT WSSRC WSDEFCLASS"
    exit 1
fi

here=$(dirname "$0")
iwant=$here/..
. "$iwant/cached/iwant/scripts/iwant-functions.sh"

use-iwant-on "$@"
