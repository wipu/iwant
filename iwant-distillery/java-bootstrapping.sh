doc() {

html "<h1>iwant tutorial</h1>"

section "Acquiring iwant bootstrapper by exporting from svn"

LOCAL_IWANT_WSROOT=$(readlink -f "$IWANT_DISTILLERY/..")
cmd svn export "$IWANT_DISTILLERY/as-some-developer" as-distillery-developer
cmd 'find . -type f'

section "Choosing url for iwant to use as engine"

cmde 1 'as-distillery-developer/with/bash/iwant/help.sh'
edit as-distillery-developer/i-have/iwant-from "local-iwant-from" <<EOF
iwant-from=file://$LOCAL_IWANT_WSROOT
EOF
cmde "1" "as-distillery-developer/with/bash/iwant/help.sh 2>&1"

p "This is an optimization for this tutorial:"

edit as-distillery-developer/i-have/iwant-from "iwant-re-export-optimization" <<EOF
iwant-from=file://$LOCAL_IWANT_WSROOT
# an optimization for this tutorial:
re-export=false
EOF

section "Defining your workspace and its build basics"

p "Now we start defining our workspace and its build definition."

edit as-distillery-developer/i-have/ws-info "no-changes" <<EOF
# paths are relative to this file's directory
WSNAME=iwant-tutorial
WSROOT=../..
WSDEF_SRC=wsdefdef/src/main/java
WSDEF_CLASS=com.example.wsdefdef.WorkspaceProvider
EOF

cmde "1" "as-distillery-developer/with/bash/iwant/help.sh 2>&1"
cmde "0 0" 'find as-distillery-developer -type f | grep -v ".i-cached"'
p "End of wizard, everything is set for the final usage help message:"
cmde "1" "as-distillery-developer/with/bash/iwant/help.sh"

section "Configuring the worker thread count"

p "In this tutorial we'll use only one worker thread to keep the output deterministic. You should probably use a bigger number, depending on your machine."
cmd 'echo workerCount=1 > as-distillery-developer/i-have/user-preferences'
cmde "1" "as-distillery-developer/with/bash/iwant/help.sh"

cmde "0" "as-distillery-developer/with/bash/iwant/list-of/targets"
cmde "0" "as-distillery-developer/with/bash/iwant/target/hello/as-path"

section "Using Eclipse to edit your build definition"

p "Before we try editing the wsdef, we'll tell iwant to generate eclipse settings."

cmd "as-distillery-developer/with/bash/iwant/list-of/side-effects"
cmd "as-distillery-developer/with/bash/iwant/side-effect/eclipse-settings/effective"
cmd "ls .project .classpath .settings"

p "Now we can import the project to eclipse (don't copy it to the workspace!) and try our first edit."
wsdef-edit v00modifiedhello
cmde "0" "as-distillery-developer/with/bash/iwant/list-of/targets"
out-was <<EOF
(0 workspaceClasses)
hello
hello2
EOF
cmde "0" "as-distillery-developer/with/bash/iwant/target/hello2/as-path"
cmde "0" 'cat ""$(as-distillery-developer/with/bash/iwant/target/hello2/as-path)'

section "Using external libraries in your build"

p "Next we add downloaded classes to be used in the workspace definition."
wsdefdef-edit v00commonsmathjar

p "TODO: document regenerating eclipse settings and refresing Eclipse."

p "Now we can use commons-math in the workspace definition."
wsdef-edit v01commonsmathjar
cmde "0 0" "as-distillery-developer/with/bash/iwant/target/hello2/as-path | xargs cat "

section "Using ant to define content for a target"

p "Let's write another target using ant, downloaded from ibiblio.org. The ant script refers to another target."

wsdef-edit v02antgeneratedtarget
cmde "0" "as-distillery-developer/with/bash/iwant/list-of/targets"
cmde "0 0" "as-distillery-developer/with/bash/iwant/target/antGenerated/as-path | xargs cat "

}

wsdef-edit() {
  local NAME=$1
  def-edit wsdef "$NAME" Workspace
}

wsdefdef-edit() {
  local NAME=$1
  def-edit wsdefdef "$NAME" WorkspaceProvider
}

def-edit() {
  local TYPE=$1
  local NAME=$2
  local CLASS=$3
  log "def-edit $TYPE $NAME $CLASS"
  cat "$LOCAL_IWANT_WSROOT/iwant-tutorial-wsdefs/src/com/example/$TYPE/$NAME/${CLASS}.java" |
    sed "s/^package .*;/package com.example.${TYPE};/" |
    edit as-distillery-developer/i-have/${TYPE}/src/main/java/com/example/$TYPE/${CLASS}.java "$TYPE-$NAME"
}
