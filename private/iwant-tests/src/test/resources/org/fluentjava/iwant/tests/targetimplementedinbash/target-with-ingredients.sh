ingredients() {
    param PARAM "param value"
    source-dep INGR1 "src-ingr"
    target-dep INGR2 "target-ingr"
}

path() {
    echo "Using PARAM=$PARAM and $INGR1 and $INGR2 to generate $IWANT_DEST" > "$IWANT_DEST"
}
