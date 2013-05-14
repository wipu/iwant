doc-content() {

cd iwant-tutorial

section "Defining your workspace and its build basics"

p "Now we start defining our workspace and its build definition."

edit as-iwant-tutorial-developer/i-have/conf/ws-info "no-changes" <<EOF
# paths are relative to this file's directory
WSNAME=iwant-tutorial
WSROOT=../../..
WSDEFDEF_MODULE=../wsdefdef
WSDEFDEF_CLASS=com.example.wsdefdef.WorkspaceProvider
EOF

cmde "1" "as-iwant-tutorial-developer/with/bash/iwant/help.sh 2>&1"
cmde "0 0 0" 'find as-iwant-tutorial-developer/i-have -type f | grep -v ".i-cached" | sort'
p "End of wizard, everything is set for the final usage help message:"
cmde "1" "as-iwant-tutorial-developer/with/bash/iwant/help.sh"

section "Configuring the worker thread count"

p "In this tutorial we'll use only one worker thread to keep the output deterministic. You should probably use a bigger number, depending on your machine."
cmd 'echo workerCount=1 > as-iwant-tutorial-developer/i-have/conf/user-preferences'
cmde "1" "as-iwant-tutorial-developer/with/bash/iwant/help.sh"

cmde "0" "as-iwant-tutorial-developer/with/bash/iwant/list-of/targets"
cmde "0" "as-iwant-tutorial-developer/with/bash/iwant/target/hello/as-path"

section "Using Eclipse to edit your build definition"

p "Before we try editing the wsdef, we'll tell iwant to generate eclipse settings."

cmd "as-iwant-tutorial-developer/with/bash/iwant/list-of/side-effects"
cmd "as-iwant-tutorial-developer/with/bash/iwant/side-effect/eclipse-settings/effective"
cmd "ls as-iwant-tutorial-developer/i-have/{wsdefdef,wsdef}/{.project,.classpath,.settings}"

p "Now we can import the project to eclipse (don't copy it to the workspace!) and try our first edit."
wsdef-edit v00modifiedhello
cmde "0" "as-iwant-tutorial-developer/with/bash/iwant/list-of/targets"
out-was <<EOF
(0/1 S~ net.sf.iwant.api.javamodules.JavaClasses iwant-tutorial-workspace-main-classes)
hello
hello2
EOF
cmde "0" "as-iwant-tutorial-developer/with/bash/iwant/target/hello2/as-path"
cmde "0 0" 'as-iwant-tutorial-developer/with/bash/iwant/target/hello2/as-path | xargs cat'

}
