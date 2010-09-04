# to be sourced

abs() {
  readlink -f "$1"
}

# variables that need to be defined before sourcing:
here=$(abs "$here")
iwant=$(abs "$iwant")

as_iwant_user="$iwant/as-iwant-user"

cache="$iwant/cached/iwant"
scriptcache="$cache/scripts"
cpitemscache="$cache/cpitems"
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

local ROOTDIR="$iwant/as-$WSNAME-developer"
local TARGETDIR=$ROOTDIR/$(dirname "$TARGET")
mkdir -p "$TARGETDIR"
local TARGETFILE="$ROOTDIR/$TARGET"

local DOTS=$(wish-to-dotdots "$TARGET")

cat > "$TARGETFILE" <<EOF
#!/bin/bash
set -eu

here=\$(dirname "\$0")
iwant=\$here/..$DOTS
. "\$iwant/cached/iwant/scripts/iwant-functions.sh"

compiled-java \\
 "\$iwant/cached/$WSNAME-wsdefclasses" \\
 "$WSSRC" \\
 "\$iwant/cached/iwant/cpitems/iwant-core" \\
 "\$iwant/cached/iwant/cpitems/ant-1.7.1.jar" \\
 "\$iwant/cached/iwant/cpitems/ant-junit-1.7.1.jar" \\
 "\$iwant/cached/iwant/cpitems/junit-3.8.1.jar"
java \
 -cp "\$iwant/cached/$WSNAME-wsdefclasses:\$iwant/cached/iwant/cpitems/iwant-core:\$iwant/cached/iwant/cpitems/ant-1.7.1.jar:\$iwant/cached/iwant/cpitems/ant-junit-1.7.1.jar:\$iwant/cached/iwant/cpitems/junit-3.8.1.jar" \\
 net.sf.iwant.core.WorkspaceBuilder \\
 "$WSDEFCLASS" \\
 "$WSROOT" \\
 "$TARGET" \\
 "\$iwant/cached/$WSNAME"$POSTPROCESSOR
EOF

chmod u+x "$TARGETFILE"
}
