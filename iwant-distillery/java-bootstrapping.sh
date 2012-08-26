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
cmde "0 0" 'find as-distillery-developer -type f | grep -v ".cached"'
p "End of wizard, everything is set for the final usage help message:"
cmde "1" "as-distillery-developer/with/bash/iwant/help.sh"
cmde "0" "as-distillery-developer/with/bash/iwant/list-of/targets"
cmde "0" "as-distillery-developer/with/bash/iwant/target/hello/as-path"

p "Before we try editing the wsdef, we'll tell iwant to generate eclipse settings."

cmde 127 "as-distillery-developer/with/bash/iwant/list-of/side-effects"
cmde 127 "as-distillery-developer/with/bash/iwant/side-effect/eclipse-settings/effective"
cmde 2 "ls as-distillery-developer/{.project,.classpath}"

p "Now we can import the project to eclipse (don't copy it to the workspace!) and try our first edit."
edit as-distillery-developer/i-have/wsdef/com/example/wsdef/Workspace.java "1st-edit" < \
  "$LOCAL_IWANT_WSROOT/iwant-tutorial-wsdefs/0-modified-hello/com/example/wsdef/Workspace.java"
cmde "0" "as-distillery-developer/with/bash/iwant/list-of/targets"
cmde "0" "as-distillery-developer/with/bash/iwant/target/hello2/as-path"
cmde "0" 'cat ""$(as-distillery-developer/with/bash/iwant/target/hello2/as-path)'

}
