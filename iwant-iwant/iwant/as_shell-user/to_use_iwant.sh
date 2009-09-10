#!/bin/bash

AS_SHELL_USER=$(dirname "$0")
IWANT="$AS_SHELL_USER/.."
AS_IWANT_USER=as_iwant-user
TARGET="$IWANT/$AS_IWANT_USER"

HELPSCRIPT=some_help

function iwant_is_ready() {
    echo To use iwant, just start your sentences with iwant/as_iwant-user/
    echo For example:
    echo \$ iwant/$AS_IWANT_USER/$HELPSCRIPT
}

# TODO incremental
rm -rf "$TARGET"
mkdir "$TARGET"

function script() {
    local FILE="$TARGET/$1"
    cat > "$FILE"
    chmod u+x "$FILE"
}

script $HELPSCRIPT <<EOF
#!/bin/bash
cat <<EOF2
Write a workspace definition using the iwant framework in any source directory SRC.
Then type
\$ iwant/as_iwant-user/start_using_iwant_on SRC
and follow instructions.
EOF2
EOF

iwant_is_ready
