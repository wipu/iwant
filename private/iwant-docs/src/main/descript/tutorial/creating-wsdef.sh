doc-content() {

cd iwant-tutorial

html "<p class='text'>After defining the iwant revision to use, the wizard <a href='bootstrapping.html'>requested us to describe the workspace</a> by editing a configuration file it created. We'll do so now. We name our workspace \"iwant-tutorial\" and we also define a fully qualified name for the class that defines the entrypoint to our own build code, the <i>workspace definition definition</i>.</p>"

p "It is advisable to follow the convention of naming the last part of the workspace definition definition package \"wsdefdef\". It will help iwant to generate a good workspace definition name in the next phase."

edit as-iwant-tutorial-developer/i-have/conf/ws-info "thewsinfo" <<EOF
# paths are relative to this file's directory
WSNAME=iwant-tutorial
WSROOT=../../..
WSDEFDEF_MODULE=../wsdefdef
WSDEFDEF_CLASS=com.example.wsdefdef.IwantTutorialWorkspaceProvider
EOF

p "We make another wish for help to continue."

cmde "1" "as-iwant-tutorial-developer/with/bash/iwant/help.sh"
out-was <<EOF
I created
$PWD/as-iwant-tutorial-developer/i-have/wsdefdef/src/main/java/com/example/wsdefdef/IwantTutorialWorkspaceProvider.java
and
$PWD/as-iwant-tutorial-developer/i-have/wsdef/src/main/java/com/example/wsdef/IwanttutorialWorkspaceFactory.java
and
$PWD/as-iwant-tutorial-developer/i-have/wsdef/src/main/java/com/example/wsdef/IwanttutorialWorkspace.java
Please edit them and rerun me.
If you want to use Eclipse for editing, run $PWD/as-iwant-tutorial-developer/with/bash/iwant/side-effect/eclipse-settings/effective first.
EOF

p "Now we have a functional workspace definition, also known as a build script, for our workspace."

html "<p class='text'>As we can see from the output, there is at least one new wish available for us, the side-effect of creating eclipse settings. iwant maintains a bash script, a <i>wish script</i> for each wish defined in the workspace definition. This makes it very convenient to make wishes by using tab.</p>"

p "Let's find out what wish scripts we have."

cmde "0 0" 'find as-iwant-tutorial-developer/with/bash/iwant -type f | sort'
out-was <<EOF
as-iwant-tutorial-developer/with/bash/iwant/help.sh
as-iwant-tutorial-developer/with/bash/iwant/list-of/side-effects
as-iwant-tutorial-developer/with/bash/iwant/list-of/targets
as-iwant-tutorial-developer/with/bash/iwant/side-effect/eclipse-settings/effective
as-iwant-tutorial-developer/with/bash/iwant/target/hello/as-path
EOF

p "It is advisable to ignore the wish scripts (except for help.sh, the iwant cli) from the version control, since iwant regenerates them at every wish, according to the workspace definition."

section "Optional: configuring the worker thread count"

p "In this tutorial we'll use only one worker thread to keep the output deterministic. You should probably use a bigger number, depending on your machine. You'll probably want to skip this phase and use the default value, the number of cores in your system:"
cmd 'echo workerCount=1 > as-iwant-tutorial-developer/i-have/conf/user-preferences'

section "Finishing with the wizard"

p "Let's end the wizard by wishing for help one more time."
cmde "1" "as-iwant-tutorial-developer/with/bash/iwant/help.sh"
out-was <<EOF
(0/1 D! net.sf.iwant.api.javamodules.JavaClasses iwant-tutorial-wsdefdef-main-classes)
(0/1 D! net.sf.iwant.api.javamodules.JavaClasses iwant-tutorial-wsdef-main-classes)
(Using user preferences from file $PWD/as-iwant-tutorial-developer/i-have/conf/user-preferences:
[workerCount=1])
Try $PWD/as-iwant-tutorial-developer/with/bash/iwant/list-of/side-effects
or
$PWD/as-iwant-tutorial-developer/with/bash/iwant/list-of/targets
EOF

}
