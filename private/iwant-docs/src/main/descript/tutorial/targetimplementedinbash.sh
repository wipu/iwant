doc-content() {

cd iwant-tutorial

p "bash targets"

cmd "find as-iwant-tutorial-developer/i-have/wsdef/src"

wsdef-edit targetimplementedinbash

p "Let's see where we need to define the index."

cmde "1" "as-iwant-tutorial-developer/with/bash/iwant/list-of/targets"

p "We create an index with one target."

cmd "mkdir as-iwant-tutorial-developer/i-have/wsdef/src/main/bash"
index-sh-hello | edit-script "_index.sh" "index-v-hello"

p "We define the target in a script named after the target name."

hello-from-bash | edit-script "hello-from-bash.sh" "hello-v0"

p "Now we can evaluate our first target defined in bash."

cmde "0" "as-iwant-tutorial-developer/with/bash/iwant/list-of/targets"
cmde "0 0" "as-iwant-tutorial-developer/with/bash/iwant/target/hello-from-bash/as-path | xargs -r cat"

p "target with ingredients"

index-sh-ingredients | edit-script "_index.sh" "index-v-ingredients"
target-with-ingredients | edit-script "target-with-ingredients.sh" "target-with-ingredients-v0"
cmd 'echo "source-ingredient-content" > source-ingredient'
cmde "0" "as-iwant-tutorial-developer/with/bash/iwant/list-of/targets"
cmde "0 0" "as-iwant-tutorial-developer/with/bash/iwant/target/target-with-ingredients/as-path | xargs -r cat"

p "parameterized target"

target-with-parameters | edit-script "target-with-parameters.sh" "target-with-parameters-v0"

p "name, script name, then arguments for ingredients"

index-sh-parameters | edit-script "_index.sh" "index-v-parameters"

cmde "0" "as-iwant-tutorial-developer/with/bash/iwant/list-of/targets"
cmde "0 0" "as-iwant-tutorial-developer/with/bash/iwant/target/target-with-parameters-v1/as-path | xargs -r cat"
cmde "0 0" "as-iwant-tutorial-developer/with/bash/iwant/target/target-with-parameters-v2/as-path | xargs -r cat"

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

# target with parameters

index-sh-parameters() {
    cat <<\EOF
targets() {
    target hello-from-bash
    target target-with-ingredients
    target target-with-parameters-v1 target-with-parameters.sh 1 one
    target target-with-parameters-v2 target-with-parameters.sh 2 two
}
EOF
}

target-with-parameters() {
    cat <<\EOF
ingredients() {
    param PARAM1 "$1"
    param PARAM2 "$2"
}

path() {
    echo "Target derived from parameters:" > "$IWANT_DEST"
    echo "--- PARAM1: $PARAM1" >> "$IWANT_DEST"
    echo "--- PARAM2: $PARAM2" >> "$IWANT_DEST"
}

EOF
}

edit-script() {
    local SCRIPTNAME=$1
    local EDITNAME=$2
    log "Editing script $SCRIPTNAME, $EDITNAME"
    edit as-iwant-tutorial-developer/i-have/wsdef/src/main/bash/"$SCRIPTNAME" "$EDITNAME"
}
