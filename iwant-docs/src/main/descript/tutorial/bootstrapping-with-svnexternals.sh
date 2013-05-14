doc-content() {

section "Acquiring iwant bootstrapper by using svn:externals"

p "Let's create a local svn repository and check it out."

cmd "svnadmin create svn-repo"
cmd 'svn co "file://$(readlink -f svn-repo)" iwant-tutorial'
cmd "cd iwant-tutorial"

cmd "mkdir as-iwant-tutorial-developer"
cmd "svn add as-iwant-tutorial-developer"
cmde "0 0" "echo '-r 532 https://iwant.svn.sourceforge.net/svnroot/iwant/trunk/iwant-distillery/as-some-developer/with with' | svn ps svn:externals --file - as-iwant-tutorial-developer"

cmd "svn commit -m 'external iwant bootstrapper'"
cmd "svn up"

section "Choosing url for iwant to use as engine"

cmde 1 'as-iwant-tutorial-developer/with/bash/iwant/help.sh'
edit as-iwant-tutorial-developer/i-have/conf/iwant-from "local-iwant-from" <<EOF
iwant-from=https://iwant.svn.sourceforge.net/svnroot/iwant/trunk@532
EOF
cmde "1" "as-iwant-tutorial-developer/with/bash/iwant/help.sh"

}
