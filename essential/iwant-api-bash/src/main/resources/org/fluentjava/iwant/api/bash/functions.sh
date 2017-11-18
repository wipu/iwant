# to be sourced

iwant-log() {
    local MSG="--- $@"
    echo "$MSG" >&2
    iwant-filelog "$MSG"
}

iwant-filelog() {
    echo "$@" >> ~/.org.fluentjava.iwant/shell-log
}

die() {
    iwant-log "$@"
    exit 1
}

indented() {
    echo -n "$1" | sed 's/^/ /'
}

ingredients() {
    iwant-filelog "No ingredients declared"
}

iwant-cached() {
    local NAME=$(echo "$1" | sed 's/\\/\\\\/g')
    grep "^$NAME::" "$IWANT_DEPREFS" | sed 's/^.*:://'
}

targets() {
    die "Please define targets in $1"
}
