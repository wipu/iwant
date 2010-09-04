# to be sourced

abs() {
  readlink -f "$1"
}

# variables that need to be defined before sourcing:
here=$(abs "$here")
iwant=$(abs "$iwant")

as_iwant_user="$iwant/as-iwant-user"

cache="$iwant/cached/iwant"
scriptcache="$cache/scripts"
cpitemscache="$cache/cpitems"
classescache="$cpitemscache/iwant-core"
