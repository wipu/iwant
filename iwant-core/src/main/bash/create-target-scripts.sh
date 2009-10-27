#!/bin/bash

set -eu

WSNAME="$1"
WSROOT="$2"
WSSRC="$3"
WSDEFCLASS="$4"

here=$(dirname "$0")

while read target; do
    echo "$target"
    "$here/createscript.sh" "$WSNAME" "$WSROOT" "target/$target/as-path" "$WSSRC" "$WSDEFCLASS" ""
done
