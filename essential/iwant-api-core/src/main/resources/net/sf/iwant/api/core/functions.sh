# to be sourced

iwant-log() {
    echo "--- $@" >&2
}

die() {
    iwant-log "$@"
    exit 1
}

indented() {
    echo -n "$1" | sed 's/^/ /'
}

ingredients() {
    iwant-log "No ingredients declared"
}

iwant-cached() {
    local NAME=$(echo "$1" | sed 's/\\/\\\\/g')
    grep "$NAME" "$IWANT_DEPREFS" | sed 's/^.*:://'
}

targets() {
    die "Please define targets"
}
