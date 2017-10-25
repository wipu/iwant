doc-content() {

section "Introduction to the iwant cli and bootstrapping"

p "Since a build system is so integral a part of a reproducible build, each project that uses iwant defines the exact version of iwant to use. To achieve this, the command line interface of iwant is a very shallow bootstrapper that only knows how to download and build the actual iwant engine and delegates the actual work to it."

html "<p class='text'>The bootstrapper contains two command line interfaces: a bash script and an ant script. They both do the same thing: they compile and run the third part of the bootstrapper, a java class. This java class is the <i>entry</i> to iwant.</p>"

html "<p class='text'>The entry class fetches (if necessary, of course) the requested version of iwant code and compiles and runs the next phase of bootstrapping, <code>Iwant2.java</code>.</p>"

html "<p class='text'>The entry2 phase compiles the full iwant system it itself is part of and runs the actual entry to the system, <code>Iwant3.java</code>.</p>"

html "<p class='text'>If in any of the bootstrapping phases some configuration is missing, the bootstrapper acts as a wizard that helps the user in creating the configuration.</p>"

html "<p class='text'>In this tutorial we'll be mostly using the bash interface. The <a href='ant-cli.html'>Ant cli</a> chapter gives a short introduction to the ant command line interface.</p>"

section "Bootstrapping iwant"

p "Let's see how all this works in practice."

p "First we'll create a directory for our project workspace and for the iwant bash cli."

cmd "mkdir -p iwant-tutorial/as-iwant-tutorial-developer/with/bash/iwant"
cmd "cd iwant-tutorial"

p "Then we download our first wish script: the script that downloads us the iwant bootstrapper."

cmd "cd as-iwant-tutorial-developer/with/bash/iwant"
cmd wget https://raw.githubusercontent.com/wipu/iwant/master/essential/iwant-entry/as-some-developer/with/bash/iwant/iwant.sh
cmd "chmod u+x iwant.sh"

p "In this tutorial we use commit $REV_TO_TEST of iwant, but if you want to use the latest tested version, you can skip the following command:"

cmd "sed -i \"s/COMMIT=.*/COMMIT=$REV_TO_TEST/\" iwant.sh"

p "Now we cd back to the workspace root; that's where we normally make our wishes."

cmd "cd -"

p "Now we are ready to make our first wish. By using the code completion feature (tab) of bash, we see that the only wish available is 'iwant.sh' that grants us iwant itself:"

cmd "as-iwant-tutorial-developer/with/bash/iwant/iwant.sh"

p "Now we have the iwant bash cli available:"

cmde "0 0" "find as-iwant-tutorial-developer/with -type f | sort"
out-was <<EOF
as-iwant-tutorial-developer/with/ant/iw/build.xml
as-iwant-tutorial-developer/with/bash/iwant/help.sh
as-iwant-tutorial-developer/with/bash/iwant/iwant.sh
as-iwant-tutorial-developer/with/java/org/fluentjava/iwant/entry/Iwant.java
EOF

section "Starting using iwant"

p "Now we are ready to make our first wish for iwant itself. By using the code completion feature (tab) of bash, we see that we now have another wish available: 'help':"

cmde 1 'as-iwant-tutorial-developer/with/bash/iwant/help.sh'
out-was <<EOF
I created $PWD/as-iwant-tutorial-developer/i-have/conf/ws-info
Please edit it and rerun me.
EOF

html "<p class='text'>We will <a href='creating-wsdef.html'>continue defining the workspace</a> in a separate chapter. The next chapter shows <a href='bootstrapping-with-svnexternals.html'>an alternative way of acquiring the bootstrapper using svn:externals</a>, a handy mechanism for projects that use svn for version control.</p>"

}
