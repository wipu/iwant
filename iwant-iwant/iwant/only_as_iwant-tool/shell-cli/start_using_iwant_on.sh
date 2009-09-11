#/bin/bash

if [ $# != 1 ]; then
    echo "Usage: $0 ROOTDEF"
    echo "Where ROOTDEF points to the root definition java file."
    exit 1
fi

ROOTDEF="$1"

if [ ! -e "$ROOTDEF" ];then
    echo "Workspace definition file does not exist: $WSDEF"
    exit 1
fi

MYDIR=$(dirname "$0")
IWANT=$(dirname "$MYDIR")
WSNAME=iwant
TARGET=$IWANT/as_${WSNAME}-developer

# TODO incremental
rm -rf "$TARGET"
mkdir "$TARGET"

echo "To get access to targets of the $WSNAME project, start your sentences with"
echo "\$ $TARGET"
