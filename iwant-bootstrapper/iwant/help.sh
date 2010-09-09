#!/bin/bash

set -eu

HELP_SH=$0
WISHROOT=$(dirname "$HELP_SH")
HERE=$(dirname "$WISHROOT")

cached() {
  CACHED=$WISHROOT/cached
  [ -e "$CACHED" ] || mkdir "$CACHED"
}

i-have() {
  IHAVE=$HERE/i-have
  [ -e "$IHAVE" ] || mkdir "$IHAVE"
}

iwant-from-conf() {
  i-have
  IWANT_FROM_CONF=$IHAVE/iwant-from.conf
  [ -e "$IWANT_FROM_CONF" ] || guide-iwant-from-conf
}

guide-iwant-from-conf() {
cat > "$IWANT_FROM_CONF" <<EOF
# Uncomment and modify one of these:
#svn-revision 99
#local-svn-workingcopy ~/svn/sf.net/iwant
#local-iwant-wishdir ~/svn/sf.net/iwant/iwant-iwant/iwant
EOF
cat <<EOF
Welcome.

Please start by specify what version of iwant you wish to use.
I created file $IWANT_FROM_CONF for you.
Modify it and rerun $HELP_SH
EOF
exit
}

iwant() {
  cached
  IWANT=$CACHED/iwant
  [ -e "$IWANT" ] || fetch-iwant
}

fetch-iwant() {
  . "$IWANT_FROM_CONF"
}

local-svn-workingcopy() {
  local FROM=$1
  local IWANT_SRC=$CACHED/iwant-from-local-svn-workingcopy
  svn export "$FROM" "$IWANT_SRC" >/dev/null
  "$IWANT_SRC/iwant-iwant/iwant/as_shell-user/to-bootstrap-iwant.sh"
  local-iwant-cache "$IWANT_SRC/iwant-iwant/iwant/cached"
}

local-iwant-wishdir() {
  local FROM=$1
  cp -a "$FROM/as-iwant-user" "$WISHROOT"/
  iwant-cached "$FROM/cached/iwant/scripts" "$FROM/cached/iwant/cpitems"
}

iwant-cached() {
  cached
  mkdir "$IWANT"
  cp -a "$@" "$IWANT"/
}

iwant-from-conf
iwant
echo ready
