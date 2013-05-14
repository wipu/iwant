doc-content() {

cd iwant-tutorial

section "Using ant to define content for a target"

p "Let's write another target using ant, downloaded from ibiblio.org. The ant script refers to another target."

wsdef-edit v02antgeneratedtarget
cmde "0" "as-iwant-tutorial-developer/with/bash/iwant/list-of/targets"
cmde "0 0" "as-iwant-tutorial-developer/with/bash/iwant/target/antGenerated/as-path | xargs cat "


}
