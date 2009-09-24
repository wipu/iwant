#!/bin/bash

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

function projsrc() {
	echo "$WSROOT/$1/src/main/java:$WSROOT/$1/src/test/java"
}

#"$WSROOT/iwant-core/src/main/java:$WSROOT/iwant-core/src/test/java:$WSROOT/iwant-iwant/src/main/java:$WSROOT/iwant-iwant/src/test/java:$WSROOT/iwant-junitlite/src/main/java:$WSROOT/iwant-junitlite/src/test/java" \

BOOTSTRAP_CLASSES="$BOOTSTRAP/classes"
mkdir -p "$BOOTSTRAP_CLASSES"
javac \
	-sourcepath \
		"$(projsrc iwant-core):$(projsrc iwant-iwant):$(projsrc iwant-junitlite)" \
	-d "$BOOTSTRAP_CLASSES" \
	"$WSROOT/iwant-iwant/src/main/java/Workspace.java" \
	"$WSROOT/iwant-core/src/test/java/net/sf/iwant/core/Suite.java" \
	"$WSROOT/iwant-junitlite/src/main/java/net/sf/iwant/junitlite/TestRunner.java" \
	|| die

java -cp "$BOOTSTRAP_CLASSES" net.sf.iwant.junitlite.TestRunner \
	a b c \
	|| die

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
