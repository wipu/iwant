#!/bin/bash

set -eu

function abs() {
	echo $(cd "$1" && pwd)
}

MYDIR=$(dirname "$0")
MYDIR=$(abs "$MYDIR")
IWANT=$(dirname "$MYDIR")
AS_IWANT_USER=as_iwant-user
TARGET="$IWANT/$AS_IWANT_USER"
BOOTSTRAP="$IWANT/only-as-iwant-bootstrap"
WSROOT=$(abs "$IWANT/../..")

SOME_HELP=some_help
START_USING_IWANT_ON=start_using_iwant_on

function iwant_is_ready() {
    echo To use iwant, just start your sentences with iwant/as_iwant-user/
    echo For example:
    echo \$ iwant/$AS_IWANT_USER/$SOME_HELP
}

function die() {
    exit 1
}

function remote_file() {
    local FROM="$1"
    local FILE="$2"
    local TO="$3"
    local MD5="$4"
    local TOFILE="$TO/$FILE"
    if [ ! -e "$TOFILE" ]; then
	wget "$FROM/$FILE" -O "$TOFILE"
    fi
    echo "$MD5 *$TOFILE" | md5sum -c
}

remote_file \
    http://mirrors.ibiblio.org/pub/mirrors/maven2/junit/junit/3.8.1 \
    junit-3.8.1.jar \
    "$WSROOT/iwant-lib-junit-3.8.1" \
    "1f40fb782a4f2cf78f161d32670f7a3a"

remote_file \
    http://mirrors.ibiblio.org/pub/mirrors/maven2/org/apache/ant/ant/1.7.1 \
    ant-1.7.1.jar \
    "$WSROOT/iwant-lib-ant-1.7.1" \
    "ef62988c744551fb51f330eaa311bfc0"

function projsrc() {
	echo "$WSROOT/$1/src/main/java:$WSROOT/$1/src/test/java"
}

BOOTSTRAP_CLASSES="$BOOTSTRAP/classes"
mkdir -p "$BOOTSTRAP_CLASSES"
javac \
	-sourcepath \
		"$(projsrc iwant-core):$(projsrc iwant-iwant)" \
        -cp "$WSROOT/iwant-lib-junit-3.8.1/junit-3.8.1.jar" \
	-d "$BOOTSTRAP_CLASSES" \
	"$WSROOT/iwant-iwant/src/main/java/Workspace.java" \
	"$WSROOT/iwant-core/src/test/java/net/sf/iwant/core/Suite.java" \
	|| die

java -cp "$BOOTSTRAP_CLASSES:$WSROOT/iwant-lib-junit-3.8.1/junit-3.8.1.jar" \
    junit.textui.TestRunner -c net.sf.iwant.core.Suite

# TODO incremental
rm -rf "$TARGET"
mkdir "$TARGET"

function install_scripts() {
    for f in $*; do
	cp "$IWANT/only_as_iwant-tool/shell-cli/${f}.sh" "$TARGET/$f"
    done
}

install_scripts \
    $SOME_HELP $START_USING_IWANT_ON

iwant_is_ready
