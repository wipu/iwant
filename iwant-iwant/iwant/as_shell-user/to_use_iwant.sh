#!/bin/bash

AS_SHELL_USER=$(dirname "$0")
IWANT=$(dirname "$AS_SHELL_USER")
AS_IWANT_USER=as_iwant-user
TARGET="$IWANT/$AS_IWANT_USER"

SOME_HELP=some_help
START_USING_IWANT_ON=start_using_iwant_on

function iwant_is_ready() {
    echo To use iwant, just start your sentences with iwant/as_iwant-user/
    echo For example:
    echo \$ iwant/$AS_IWANT_USER/$SOME_HELP
}

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
