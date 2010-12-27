# to be sourced
set -eu

as_iwant_user="as-iwant-user"

cached="cached/iwant"
scriptcache="$cached/scripts"
cpitemscache="$cached/cpitems"
classescache="$cpitemscache/iwant-core"

# TODO how about running the wsdef with beanshell so
# we won't need compilation?
compiled-java() {
  local DEST="$1"
  shift
  local SRC="$1"
  shift

  local CP=""
  while [ $# -gt 0 ]; do
    CP="$CP$1:"
    shift
  done

  CP=$(echo $CP | sed 's/:$//')

  mkdir -p "$DEST"
  local JAVAFILES=$(find "$SRC" -name '*.java')
  javac -d "$DEST" -classpath "$CP" "$JAVAFILES"
}

create-target-scripts() {
  local WSNAME="$1"
  local WSROOT="$2"
  local WSSRC="$3"
  local WSDEFCLASS="$4"

  while read target; do
    echo "$target"
    createscript "$WSNAME" "$WSROOT" "target/$target/as-path" "$WSSRC" "$WSDEFCLASS" ""
  done
}

wish-to-dotdots() {
  echo $1 | sed 's:[^/]*::g' | sed 's:/:/..:g'
}

createscript() {
local WSNAME="$1"
local WSROOT="$2"
local TARGET="$3"
local WSSRC="$4"
local WSDEFCLASS="$5"
local POSTPROCESSOR="$6"

local ROOTDIR="$iwant"
local TARGETDIR=$ROOTDIR/$(dirname "$TARGET")
mkdir -p "$TARGETDIR"
local TARGETFILE="$ROOTDIR/$TARGET"

local DOTS=$(wish-to-dotdots "$TARGET")

cat > "$TARGETFILE" <<EOF
#!/bin/bash
set -eu

here=\$(dirname "\$0")
iwant=\$here$DOTS
"\$iwant/help.sh" > "\$iwant/cached/refresh-out"
. "\$iwant/cached/iwant/scripts/iwant-functions.sh"

compiled-wsdef \\
  "$WSNAME" \\
  "$WSSRC"
wsdef-run \\
  "$WSNAME" "$WSDEFCLASS" "$WSROOT" "$TARGET"$POSTPROCESSOR
EOF

chmod u+x "$TARGETFILE"
}

compiled-wsdef() {
  local WSNAME=$1
  local WSSRC=$2
  compiled-java \
    "$iwant/cached/$WSNAME-wsdefclasses" \
    "$iwant/$WSSRC" \
    "$iwant/cached/iwant/cpitems/iwant-core" \
    "$iwant/cached/iwant/cpitems/ant-1.7.1.jar" \
    "$iwant/cached/iwant/cpitems/ant-junit-1.7.1.jar" \
    "$iwant/cached/iwant/cpitems/junit-3.8.1.jar"
}

wsdef-run() {
  local WSNAME=$1
  local WSDEFCLASS=$2
  local WSROOT=$3
  local TARGET=$4
  java \
   -cp "$iwant/cached/$WSNAME-wsdefclasses:$iwant/cached/iwant/cpitems/iwant-core:$iwant/cached/iwant/cpitems/ant-1.7.1.jar:$iwant/cached/iwant/cpitems/ant-junit-1.7.1.jar:$iwant/cached/iwant/cpitems/junit-3.8.1.jar" \
   net.sf.iwant.core.WorkspaceBuilder \
   "$WSDEFCLASS" \
   "$iwant/$WSROOT" \
   "$TARGET" \
   "$iwant/cached/$WSNAME"
}

use-iwant-on() {
  local WSNAME="$1"
  local WSROOT="$2"
  local WSSRC="$3"
  local WSDEFCLASS="$4"

  createscript "$WSNAME" "$WSROOT" "list-of/targets" "$WSSRC" "$WSDEFCLASS" " | create-target-scripts \"$WSNAME\" \"$WSROOT\" \"$WSSRC\" \"$WSDEFCLASS\""
}
