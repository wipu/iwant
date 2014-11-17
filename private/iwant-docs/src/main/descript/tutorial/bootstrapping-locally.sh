doc-content() {

cmd "mkdir iwant-tutorial"
cmd "cd iwant-tutorial"

cmd svn export "$LOCAL_IWANT_WSROOT/essential/iwant-entry/as-some-developer" as-iwant-tutorial-developer
cmd 'find . -type f'

section "Choosing url for iwant to use as engine"

cmde 1 'as-iwant-tutorial-developer/with/bash/iwant/help.sh'
edit as-iwant-tutorial-developer/i-have/conf/iwant-from "local-iwant-from" <<EOF
iwant-from=file://$LOCAL_IWANT_WSROOT
EOF
cmde "1" "as-iwant-tutorial-developer/with/bash/iwant/help.sh 2>&1"

p "This is an optimization for this tutorial:"

edit as-iwant-tutorial-developer/i-have/conf/iwant-from "iwant-re-export-optimization" <<EOF
iwant-from=file://$LOCAL_IWANT_WSROOT
# an optimization for this tutorial:
re-export=false
EOF
}
