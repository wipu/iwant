#!/bin/bash

set -eu

WSNAME="$1"
WSSRC="$2"
WSDEFCLASS="$3"

here=$(dirname "$0")

while read target; do
    echo "$target"
    "$here/createscript.sh" "$1" "target/$target/as-path" "$WSSRC" "$WSDEFCLASS" ""
done
