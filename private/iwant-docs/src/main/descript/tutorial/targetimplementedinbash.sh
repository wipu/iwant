doc-content() {

cd iwant-tutorial

p "bash targets"

cmd "find as-iwant-tutorial-developer/i-have/wsdef/src"

wsdef-edit targetimplementedinbash

cmd "mkdir as-iwant-tutorial-developer/i-have/wsdef/src/main/bash"

index-sh-hello | edit-script "_index.sh" "index-v-hello"
hello-from-bash | edit-script "hello-from-bash.sh" "hello-v0"

cmde "0" "as-iwant-tutorial-developer/with/bash/iwant/list-of/targets"

cmde "0 0" "as-iwant-tutorial-developer/with/bash/iwant/target/hello-from-bash/as-path | xargs -r cat"

p "target with ingredients"

index-sh-ingredients | edit-script "_index.sh" "index-v-ingredients"
target-with-ingredients | edit-script "target-with-ingredients.sh" "target-with-ingredients-v0"
cmd 'echo "source-ingredient-content" > source-ingredient'
cmde "0" "as-iwant-tutorial-developer/with/bash/iwant/list-of/targets"
cmde "0 0" "as-iwant-tutorial-developer/with/bash/iwant/target/target-with-ingredients/as-path | xargs -r cat"

}

# hello

index-sh-hello() {
    cat <<\EOF
targets() {
    target hello-from-bash
}
EOF
}

hello-from-bash() {
    cat <<\EOF
path() {
    echo "Hello from bash" > "$IWANT_DEST"
}
EOF
}

# target with ingredients

index-sh-ingredients() {
    cat <<\EOF
targets() {
    target hello-from-bash
    target target-with-ingredients
}
EOF
}

target-with-ingredients() {
    cat <<\EOF
ingredients() {
    source-dep INGR1 source-ingredient
    target-dep INGR2 hello-from-bash
}

path() {
    echo "Target derived from ingredients:" > "$IWANT_DEST"
    echo "--- $INGR1:" >> "$IWANT_DEST"
    cat "$INGR1" >> "$IWANT_DEST"
    echo "--- $INGR2:" >> "$IWANT_DEST"
    cat "$INGR2" >> "$IWANT_DEST"
}

EOF
}

edit-script() {
    local SCRIPTNAME=$1
    local EDITNAME=$2
    log "Editing script $SCRIPTNAME, $EDITNAME"
    edit as-iwant-tutorial-developer/i-have/wsdef/src/main/bash/"$SCRIPTNAME" "$EDITNAME"
}
