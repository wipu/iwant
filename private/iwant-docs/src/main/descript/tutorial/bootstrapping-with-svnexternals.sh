doc-content() {

p "Let's create a local svn repository and check it out."

cmd "svnadmin create svn-repo"
cmd 'svn co "file://$(readlink -f svn-repo)" iwant-tutorial'
cmd "cd iwant-tutorial"

cmd "mkdir as-iwant-tutorial-developer"
cmd "svn add as-iwant-tutorial-developer"
cmde "0 0" "echo '-r 623 https://svn.code.sf.net/p/iwant/code/trunk/iwant-distillery/as-some-developer/with with' | svn ps svn:externals --file - as-iwant-tutorial-developer"

cmd "svn commit -m 'external iwant bootstrapper'"
cmd "svn up"

section "Choosing url for iwant to use as engine"

cmde 1 'as-iwant-tutorial-developer/with/bash/iwant/help.sh'
edit as-iwant-tutorial-developer/i-have/conf/iwant-from "local-iwant-from" <<EOF
iwant-from=https://svn.code.sf.net/p/iwant/code/trunk@623
EOF
cmde "1" "as-iwant-tutorial-developer/with/bash/iwant/help.sh"

}
