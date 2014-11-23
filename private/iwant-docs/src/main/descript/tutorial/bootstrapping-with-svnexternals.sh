doc-content() {

p "A handy way of avoiding having to commit the iwant bootstrapper to the user's svn repository is to link it using svn:externals."

p "Let's create a local svn repository and check it out."

cmd "svnadmin create svn-repo"
cmd 'svn co "file://$(readlink -f svn-repo)" iwant-tutorial'
cmd "cd iwant-tutorial"

p "Here we use a shell oneliner to define the svn:externals url. You may want to use the interactive svn pe command."

cmd "mkdir as-iwant-tutorial-developer"
cmd "svn add as-iwant-tutorial-developer"
cmde "0 0" "echo '-r $REV_TO_TEST https://svn.code.sf.net/p/iwant/code/trunk/essential/iwant-entry/as-some-developer/with with' | svn ps svn:externals --file - as-iwant-tutorial-developer"

p "A commit and update is needed for svn to fetch the external bootstrapper."

cmd "svn commit -m 'external iwant bootstrapper'"
out-was <<EOF
Adding         as-iwant-tutorial-developer

Committed revision 1.
EOF
cmd "svn up"
out-was <<EOF
Updating '.':

Fetching external item into 'as-iwant-tutorial-developer/with':
A    as-iwant-tutorial-developer/with/java
A    as-iwant-tutorial-developer/with/java/net
A    as-iwant-tutorial-developer/with/java/net/sf
A    as-iwant-tutorial-developer/with/java/net/sf/iwant
A    as-iwant-tutorial-developer/with/java/net/sf/iwant/entry
A    as-iwant-tutorial-developer/with/java/net/sf/iwant/entry/Iwant.java
A    as-iwant-tutorial-developer/with/ant
A    as-iwant-tutorial-developer/with/ant/iw
A    as-iwant-tutorial-developer/with/ant/iw/build.xml
A    as-iwant-tutorial-developer/with/bash
A    as-iwant-tutorial-developer/with/bash/iwant
A    as-iwant-tutorial-developer/with/bash/iwant/help.sh
Updated external to revision 748.

At revision 1.
EOF

p "Now we can start using the bootstrapper normally."

cmde 1 'as-iwant-tutorial-developer/with/bash/iwant/help.sh'
out-was <<EOF
I created $PWD/as-iwant-tutorial-developer/i-have/conf/iwant-from
Please edit it and rerun me.
EOF

}
