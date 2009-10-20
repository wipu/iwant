#!/bin/bash

set -eu

here=$(dirname "$0")
iwant="$($here/iwant-path.sh)"

WSNAME="$1"
TARGET="$2"
WSSRC="$3"
WSDEFCLASS="$4"

ROOTDIR="$iwant/as-$WSNAME-developer"
TARGETDIR=$ROOTDIR/$(dirname "$TARGET")
mkdir -p "$TARGETDIR"
TARGETFILE="$ROOTDIR/$TARGET"

cat > "$TARGETFILE" <<EOF
#!/bin/bash

"$iwant/cached/iwant/scripts/javac.sh" \\
 "$iwant/cached/$WSNAME-wsdefclasses" \\
 "$WSSRC" \\
 "$iwant/cached/iwant/classes"
java \
 -cp "$iwant/cached/$WSNAME-wsdefclasses:$iwant/cached/iwant/classes" \\
 "$WSDEFCLASS" \\
 "$ROOTDIR" \\
 "$TARGET" \\
 "$iwant/cached/$WSNAME"
EOF

chmod u+x "$TARGETFILE"
