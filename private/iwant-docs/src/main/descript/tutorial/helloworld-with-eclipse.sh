doc-content() {

cd iwant-tutorial

p "Before we try editing the wsdef, we'll tell iwant to generate eclipse settings."

cmd "as-iwant-tutorial-developer/with/bash/iwant/list-of/side-effects"
cmd "as-iwant-tutorial-developer/with/bash/iwant/side-effect/eclipse-settings/effective"
cmd "ls as-iwant-tutorial-developer/i-have/{wsdefdef,wsdef}/{.project,.classpath,.settings}"

p "Now we can import the project to eclipse (don't copy it to the workspace!) and try our first edit."
wsdef-edit v00modifiedhello
cmde "0" "as-iwant-tutorial-developer/with/bash/iwant/list-of/targets"
out-was <<EOF
(0/1 S~ net.sf.iwant.api.javamodules.JavaClasses iwant-tutorial-wsdef-main-classes)
hello
hello2
EOF
cmde "0" "as-iwant-tutorial-developer/with/bash/iwant/target/hello2/as-path"
cmde "0 0" 'as-iwant-tutorial-developer/with/bash/iwant/target/hello2/as-path | xargs cat'

}
