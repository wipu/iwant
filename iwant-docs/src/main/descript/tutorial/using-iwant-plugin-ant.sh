doc-content() {

cd iwant-tutorial

section "Using an iwant-plugin (for untarring)"

wsdefdef-edit v01iwantPluginAnt

p "We have to generate Eclipse settings before editing the workspace definition."

cmd "as-iwant-tutorial-developer/with/bash/iwant/side-effect/eclipse-settings/effective"

p "Now we define an untarred target using iwant-plugin-ant."

wsdef-edit v04iwantPluginAnt

p "We create the source tar to be untarred."

cmd 'mkdir Untarred-test'
cmd 'echo "hello" > Untarred-test/tarred-file'
cmd 'tar czf Untarred-test.tar.gz Untarred-test/'
cmd 'rm -rf Untarred-test'

p "Finally we list the content of the untarred tar file."

cmde "0" "as-iwant-tutorial-developer/with/bash/iwant/list-of/targets"
cmde "0 0" "as-iwant-tutorial-developer/with/bash/iwant/target/Untarred-test/as-path | xargs find"
out-was <<EOF
(0/1 T~ net.sf.iwant.api.javamodules.JavaClasses iwant-tutorial-workspace-main-classes)
(0/1 D! net.sf.iwant.plugin.ant.Untarred Untarred-test)
Expanding: $PWD/Untarred-test.tar.gz into $PWD/as-iwant-tutorial-developer/.i-cached/target/Untarred-test
$PWD/as-iwant-tutorial-developer/.i-cached/target/Untarred-test
$PWD/as-iwant-tutorial-developer/.i-cached/target/Untarred-test/Untarred-test
$PWD/as-iwant-tutorial-developer/.i-cached/target/Untarred-test/Untarred-test/tarred-file
EOF

}
