#!/bin/bash

set -eu

cyg() {
  local IN=$1
  case "$(uname)" in
    CYGWIN*) cygpath --windows -a "$IN" ;;
    *) echo "$IN" ;;
  esac
}

AS_SOMEONE=$(dirname "$0")/../../..
AS_SOMEONE=$(cd "$AS_SOMEONE" && pwd)
CYG_AS_SOMEONE=$(cyg "$AS_SOMEONE")

# TODO how to define this only once:
CACHED=$(cyg "$AS_SOMEONE/.i-cached")

CLASSES=$CACHED/.internal/entry-classes
CYG_CLASSES=$(cyg "$CLASSES")
mkdir -p "$CLASSES"
CYG_SRC=$(cyg "$AS_SOMEONE/with/java/net/sf/iwant/entry/Iwant.java")
javac -d "$CYG_CLASSES" "$CYG_SRC"

PH=${IWANT_PROXY_HOST:-}
PP=${IWANT_PROXY_PORT:-}

java \
  -Dhttp.proxyHost=$PH -Dhttp.proxyPort=$PP \
  -Dhttps.proxyHost=$PH -Dhttps.proxyPort=$PP \
  -cp "$CYG_CLASSES" net.sf.iwant.entry.Iwant "$CYG_AS_SOMEONE" "$@"
