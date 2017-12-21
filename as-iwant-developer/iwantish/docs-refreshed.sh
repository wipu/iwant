#!/bin/bash

set -eu -o pipefail

set -o xtrace

HERE=$(dirname "$0")
AS_ME=$HERE/..
WSROOT=$AS_ME/..
WEBSITE=$("$AS_ME/with/bash/iwant/help.sh" target/remote-website/as-path)

DOCS=$WSROOT/docs

rm -rf "$DOCS"
mkdir "$DOCS"

cp -av "$WEBSITE"/* "$DOCS"/
echo -n iwant.fluentjava.org > "$DOCS/CNAME"
