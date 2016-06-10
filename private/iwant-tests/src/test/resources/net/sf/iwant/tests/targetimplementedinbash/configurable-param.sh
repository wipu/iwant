ingredients() {
    PARAM=$1
    param PARAM "$PARAM"
    source-dep INGR1 "src-ingr"
}

path() {
    echo "Using PARAM=$PARAM and $INGR1" > "$IWANT_DEST"
}
