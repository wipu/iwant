doc() {

html "<h1>iwant tutorial</h1>"

section "Acquiring iwant bootstrapper by exporting from svn"

cmd svn export "$LOCAL_IWANT_WSROOT/iwant-distillery/as-some-developer" as-distillery-developer
cmd 'find . -type f'

section "Choosing url for iwant to use as engine"

cmde 1 'as-distillery-developer/with/bash/iwant/help.sh'
edit as-distillery-developer/i-have/conf/iwant-from "local-iwant-from" <<EOF
iwant-from=file://$LOCAL_IWANT_WSROOT
EOF
cmde "1" "as-distillery-developer/with/bash/iwant/help.sh 2>&1"

p "This is an optimization for this tutorial:"

edit as-distillery-developer/i-have/conf/iwant-from "iwant-re-export-optimization" <<EOF
iwant-from=file://$LOCAL_IWANT_WSROOT
# an optimization for this tutorial:
re-export=false
EOF
}
