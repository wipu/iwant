ingredients() {
    param PARAM "$1"
    source-dep INGR1 "src-ingr"
}

path() {
    echo "Using PARAM=$PARAM and $INGR1" > "$IWANT_DEST"
}
