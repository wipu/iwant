doc() {
LOCAL_IWANT_WSROOT=$(readlink -f "$IWANT_DISTILLERY/..")
svn export "$IWANT_DISTILLERY/as-some-developer" as-distillery-developer
cmd 'find . -type f'
cmde 1 'as-distillery-developer/with/bash/iwant/help.sh'
edit as-distillery-developer/i-have/iwant-from "local-iwant-from" <<EOF
iwant-from=file://$LOCAL_IWANT_WSROOT
EOF
cmde "1" "as-distillery-developer/with/bash/iwant/help.sh 2>&1"
cmd 'cat as-distillery-developer/i-have/ws-info'
p 'An optimization for this tutorial.'
edit as-distillery-developer/i-have/iwant-from "dont-reexport-iwant" <<EOF
iwant-from=file://$LOCAL_IWANT_WSROOT
re-export=false
EOF
cmde "1" "as-distillery-developer/with/bash/iwant/help.sh 2>&1"
cmde "0 0" 'find as-distillery-developer -type f | grep -v ".i-cached"'
p "End of wizard, everything is set for the final usage help message:"
cmde "1" "as-distillery-developer/with/bash/iwant/help.sh"

p "In this tutorial we'll use only one worker thread to keep the output deterministic. You should probably use bigger number, depending on your machine."
cmd 'echo workerCount=1 > as-distillery-developer/i-have/user-preferences'
cmde "1" "as-distillery-developer/with/bash/iwant/help.sh"

cmde "0" "as-distillery-developer/with/bash/iwant/list-of/targets"
cmde "0" "as-distillery-developer/with/bash/iwant/target/hello/as-path"

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

p "Next we add downloaded classes to be used in the workspace definition."
wsdefdef-edit v00antjar

p "TODO: document regenerating eclipse settings and refresing Eclipse."

p "Now we can use ant classes in the workspace definition."
wsdef-edit v01antjar
cmde "0 0" "as-distillery-developer/with/bash/iwant/target/hello2/as-path | xargs cat "

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
    edit as-distillery-developer/i-have/${TYPE}/com/example/$TYPE/${CLASS}.java "$TYPE-$NAME"
}
