doc-content() {

cd iwant-tutorial

p "bash targets"

cmd "find as-iwant-tutorial-developer/i-have/wsdef/src"

wsdef-edit targetimplementedinbash

cmd "mkdir as-iwant-tutorial-developer/i-have/wsdef/src/main/bash"
index-sh | edit-script "_index.sh" "index-v0"
hello-from-bash | edit-script "hello-from-bash.sh" "hello-v0"

cmde "0" "as-iwant-tutorial-developer/with/bash/iwant/list-of/targets"

cmde "0 0" "as-iwant-tutorial-developer/with/bash/iwant/target/hello-from-bash/as-path | xargs -r cat"

}

index-sh() {
    cat <<\EOF
targets() {
    target hello-from-bash
}
EOF
}

hello-from-bash() {
    cat <<\EOF
path() {
    iwant-log "Refreshing $IWANT_DEST"
    echo "Hello from bash" > "$IWANT_DEST"
}
EOF
}

edit-script() {
    local SCRIPTNAME=$1
    local EDITNAME=$2
    log "Editing script $SCRIPTNAME, $EDITNAME"
    edit as-iwant-tutorial-developer/i-have/wsdef/src/main/bash/"$SCRIPTNAME" "$EDITNAME"
}
