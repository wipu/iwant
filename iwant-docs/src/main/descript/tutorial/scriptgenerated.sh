doc-content() {

cd iwant-tutorial

p "In case java-based tools are not enough, you can use a script to define content for a target. Each script invocation gets a dedicated temporary directory in which to create temporary files, if needed."

wsdef-edit v03scriptgeneratedtarget
cmde "0" "as-iwant-tutorial-developer/with/bash/iwant/list-of/targets"
cmde "0 0" "as-iwant-tutorial-developer/with/bash/iwant/target/scriptGenerated/as-path | xargs cat "
out-was <<EOF
(0/1 D! net.sf.iwant.api.core.Concatenated shellScript)
(0/1 D! net.sf.iwant.api.ScriptGenerated scriptGenerated)
Running $PWD/as-iwant-tutorial-developer/.i-cached/temp/w-0/script
We have a dedicated temporary dir:
$PWD/as-iwant-tutorial-developer/.i-cached/temp/w-0
It is ok to create temporary files
script*
tmpfile
Generating $PWD/as-iwant-tutorial-developer/.i-cached/target/scriptGenerated
Hello from script
EOF

}
