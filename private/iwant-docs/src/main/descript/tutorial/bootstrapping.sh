doc-content() {

p "Let's check out the iwant bootstrapper."

cmd "mkdir iwant-tutorial"
cmd "cd iwant-tutorial"

cmd svn export -r $REV_TO_TEST https://svn.code.sf.net/p/iwant/code/trunk/essential/iwant-entry/as-some-developer as-iwant-tutorial-developer

section "Choosing url for iwant to use as engine"

cmde 1 'as-iwant-tutorial-developer/with/bash/iwant/help.sh'
edit as-iwant-tutorial-developer/i-have/conf/iwant-from "local-iwant-from" <<EOF
iwant-from=https://svn.code.sf.net/p/iwant/code/trunk@$REV_TO_TEST
EOF
cmde "1" "as-iwant-tutorial-developer/with/bash/iwant/help.sh"

}
