doc-content() {

cd iwant-tutorial

html "<p class='text'>Now, like the wizard advised after <a href='creating-wsdef.html'>defining the workspace</a>, let's generate eclipse settings.</p>"

cmd "as-iwant-tutorial-developer/with/bash/iwant/side-effect/eclipse-settings/effective"
out-was <<EOF
(0/1 D! net.sf.iwant.api.core.Concatenated eclipse-settings.bin-refs)
(as-iwant-tutorial-developer/i-have/wsdef)
(  .project)
(  .classpath)
(  .settings/org.eclipse.jdt.core.prefs)
(  .settings/org.eclipse.jdt.ui.prefs)
(as-iwant-tutorial-developer/i-have/wsdefdef)
(  .project)
(  .classpath)
(  .settings/org.eclipse.jdt.core.prefs)
(  .settings/org.eclipse.jdt.ui.prefs)
EOF

p "Now we can import the projects to eclipse (without copying them to the workspace!) and study what we have. In fact, let's even make the first edit: another hello target alongside with the existing one."

wsdef-edit v00modifiedhello

p "Now we can see our new target in the list of targets."

cmde "0" "as-iwant-tutorial-developer/with/bash/iwant/list-of/targets"
out-was <<EOF
(0/1 S~ net.sf.iwant.api.javamodules.JavaClasses iwant-tutorial-wsdef-main-classes)
hello
hello2
EOF

p "Building a target means wishing for a path to a file that contains the fresh content of the target. Let's try that for the two targets we have."

cmde "0" "as-iwant-tutorial-developer/with/bash/iwant/target/hello/as-path"
out-was <<EOF
(0/1 D! net.sf.iwant.api.core.HelloTarget hello)
$PWD/as-iwant-tutorial-developer/.i-cached/target/hello
EOF
cmde "0" "as-iwant-tutorial-developer/with/bash/iwant/target/hello2/as-path"
out-was <<EOF
(0/1 D! net.sf.iwant.api.core.HelloTarget hello2)
$PWD/as-iwant-tutorial-developer/.i-cached/target/hello2
EOF

p "iwant only prints the requested path to stdout, all diagnostic output goes to stderr. This means we can redirect the path to further processing. This is a convenient idiom for getting the content of a target:"

cmde "0 0" 'as-iwant-tutorial-developer/with/bash/iwant/target/hello2/as-path | xargs cat'
out-was <<EOF
hello from my first target
EOF

}
