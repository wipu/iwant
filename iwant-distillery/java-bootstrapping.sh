doc() {
LOCAL_IWANT_WSROOT=$(readlink -f "$IWANT_DISTILLERY/..")
svn export "$IWANT_DISTILLERY/as-some-developer" as-distillery-developer
cmd 'find .'
cmde 1 'as-distillery-developer/with/bash/iwant/help.sh'
edit as-distillery-developer/i-have/iwant-from local-iwant-from <<EOF
iwant-from=file$LOCAL_IWANT_WSROOT
EOF
}
