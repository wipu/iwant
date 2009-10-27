#!/bin/bash

set -eu

here=$(dirname "$0")
iwant="$($here/iwant-path.sh)"

WSNAME="$1"
WSROOT="$2"
TARGET="$3"
WSSRC="$4"
WSDEFCLASS="$5"
POSTPROCESSOR="$6"

ROOTDIR="$iwant/as-$WSNAME-developer"
TARGETDIR=$ROOTDIR/$(dirname "$TARGET")
mkdir -p "$TARGETDIR"
TARGETFILE="$ROOTDIR/$TARGET"

cat > "$TARGETFILE" <<EOF
#!/bin/bash
set -eu

"$iwant/cached/iwant/scripts/javac.sh" \\
 "$iwant/cached/$WSNAME-wsdefclasses" \\
 "$WSSRC" \\
 "$iwant/cached/iwant/cpitems/iwant-core" \\
 "$iwant/cached/iwant/cpitems/ant-1.7.1.jar"
java \
 -cp "$iwant/cached/$WSNAME-wsdefclasses:$iwant/cached/iwant/cpitems/iwant-core:$iwant/cached/iwant/cpitems/ant-1.7.1.jar" \\
 net.sf.iwant.core.WorkspaceBuilder \\
 "$WSDEFCLASS" \\
 "$WSROOT" \\
 "$TARGET" \\
 "$iwant/cached/$WSNAME"$POSTPROCESSOR
EOF

chmod u+x "$TARGETFILE"
