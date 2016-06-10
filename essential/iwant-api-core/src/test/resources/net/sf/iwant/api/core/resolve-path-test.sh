IWANT_DEPREFS=/tmp/iwant-deprefsyntaxproto-deprefs

log() {
    echo "--- $@" >&2
}

die() {
    log "$@"
    exit 1
}

testcase() {
    local NAME=$1
    local CACHED=$2
    log "Testing case '$NAME'='$CACHED'"
    local RESOLVED=$(iwant-cached "$NAME")
    [ "$CACHED" == "$RESOLVED" ] ||
	    die "failure, expected '$CACHED' got '$RESOLVED'"
}

# this is what java does:
unparse() {
    local NAME=$1
    local CACHED=$2
    echo $NAME::$CACHED
}

all-cases() {
    local OP=$1
    log "Calling $OP for all cases"
    $OP "simple" "/cached/simple"
    $OP "white space" "/cached/white space"
    $OP "sla/sh/ed" "/cached/sla/sh/ed"
    $OP "C:/windows" "C:/windows"
    $OP 'C:\windows\crap' 'C:\windows\crap'
}

all-cases unparse > "$IWANT_DEPREFS"

log "$IWANT_DEPREFS now contains:"
cat "$IWANT_DEPREFS" >&2
log

all-cases testcase
log "All passed :D"
