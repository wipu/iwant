#!/bin/bash

set -eu

set -o xtrace

HERE=$(dirname "$0")
WEBSITE=$("$HERE/../with/bash/iwant/help.sh" target/remote-website/as-path)

rsync -e ssh --delete-delay -vrucli "$WEBSITE"/ wipu_@shell.sourceforge.net:iwant-htdocs/
