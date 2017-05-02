doc-content() {

cd iwant-tutorial

p "Next we add downloaded classes to be used in the workspace definition."
wsdefdef-edit v00commonsmathjar

p "Eclipse settings need to be regenerated, because we have a new dependency."

cmd "as-iwant-tutorial-developer/with/bash/iwant/side-effect/eclipse-settings/effective"

p "Now we can use commons-math in the workspace definition."
wsdef-edit v01commonsmathjar
cmd "as-iwant-tutorial-developer/with/bash/iwant/list-of/targets"
cmde "0 0" "as-iwant-tutorial-developer/with/bash/iwant/target/arithmeticWithExtLib/as-path | xargs -r cat "
out-was <<EOF
(0/1 D! net.sf.iwant.api.core.HelloTarget arithmeticWithExtLib)
1/2 + 2/4 = 1
EOF

}
