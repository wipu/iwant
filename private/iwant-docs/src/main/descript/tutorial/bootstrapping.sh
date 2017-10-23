doc-content() {

section "Introduction to the iwant cli and bootstrapping"

p "Since a build system is so integral a part of a reproducible build, each project that uses iwant defines the exact version of iwant to use. To achieve this, the command line interface of iwant is a very shallow bootstrapper that only knows how to download and build the actual iwant engine and delegates the actual work to it."

html "<p class='text'>The bootstrapper contains two command line interfaces: a bash script and an ant script. They both do the same thing: they compile and run the third part of the bootstrapper, a java class. This java class is the <i>entry</i> to iwant.</p>"

p "The entry class fetches (if necessary, of course) the requested version of iwant code and compiles and runs the next phase of bootstrapping, <code>Iwant2.java</code>."

p "The entry2 phase compiles the full iwant system it itself is part of and runs the actual entry to the system, <code>Iwant3.java</code>."

p "If in any of the bootstrapping phases some configuration is missing, the bootstrapper acts as a wizard that helps the user in creating the configuration."

html "<p class='text'>In this tutorial we'll be mostly using the bash interface. The <a href='ant-cli.html'>Ant cli</a> chapter gives a short introduction to the ant command line interface.</p>"

section "Bootstrapping"

p "Let's see how all this works in practice."

p "First we'll create a directory for our project workspace."

cmd "mkdir iwant-tutorial"
cmd "cd iwant-tutorial"

p "Then we acquire the iwant command line interface, or bootstrapper, by svn exporting it. In this tutorial we use revision ${REV_TO_TEST}, but you may want to use a newer revision or to omit the -r option to get the latest revision."

cmd svn export -r $REV_TO_TEST https://svn.code.sf.net/p/iwant/code/trunk/essential/iwant-entry/as-some-developer as-iwant-tutorial-developer
out-was <<EOF
A    as-iwant-tutorial-developer
A    as-iwant-tutorial-developer/with
A    as-iwant-tutorial-developer/with/java
A    as-iwant-tutorial-developer/with/java/net
A    as-iwant-tutorial-developer/with/java/net/sf
A    as-iwant-tutorial-developer/with/java/net/sf/iwant
A    as-iwant-tutorial-developer/with/java/net/sf/iwant/entry
A    as-iwant-tutorial-developer/with/ant
A    as-iwant-tutorial-developer/with/ant/iw
A    as-iwant-tutorial-developer/with/bash
A    as-iwant-tutorial-developer/with/bash/iwant
A    as-iwant-tutorial-developer/with/java/net/sf/iwant/entry/Iwant.java
A    as-iwant-tutorial-developer/with/ant/iw/build.xml
A    as-iwant-tutorial-developer/with/bash/iwant/help.sh
Exported revision $REV_TO_TEST.
EOF

section "Choosing url for iwant to use as engine"

p "Now we are ready to make our first wish. By using the code completion feature (tab) of bash, we see that the only wish available is 'help':"

cmde 1 'as-iwant-tutorial-developer/with/bash/iwant/help.sh'
out-was <<EOF
I created $PWD/as-iwant-tutorial-developer/i-have/conf/iwant-from
Please edit it and rerun me.
EOF

p "Since we just started creating our workspace, the wizard starts helping us. We follow its advice by choosing a revision of iwant and asking for help again. Note: here it is not wise to leave the revision out."

edit as-iwant-tutorial-developer/i-have/conf/iwant-from "remote-iwant-from" <<EOF
iwant-from=https://svn.code.sf.net/p/iwant/code/trunk@$REV_TO_TEST
EOF
cmde "1" "as-iwant-tutorial-developer/with/bash/iwant/help.sh"
out-was <<EOF
I created $PWD/as-iwant-tutorial-developer/i-have/conf/ws-info
Please edit it and rerun me.
EOF

html "<p class='text'>We will <a href='creating-wsdef.html'>continue defining the workspace</a> in a separate chapter. The next chapter shows <a href='bootstrapping-with-svnexternals.html'>an alternative way of acquiring the bootstrapper using svn:externals</a>, a handy mechanism for projects that use svn for version control.</p>"

}
