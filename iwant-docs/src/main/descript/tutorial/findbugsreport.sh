doc-content() {

cd iwant-tutorial

p "First we add the iwant findbugs plugin to the project."

wsdefdef-edit findbugsreport

p "We refresh eclipse settings so we can use the plugin in our workspace definition."

cmde "0" "as-iwant-tutorial-developer/with/bash/iwant/side-effect/eclipse-settings/effective"

p "After refreshing eclipse we define a module to test findbugs on, and define a findbugs report target for its main java."

wsdef-edit findbugsreport

p "Another eclipse settings refresh to create the new eclipse project."

cmde "0" "as-iwant-tutorial-developer/with/bash/iwant/side-effect/eclipse-settings/effective"

p "After importing the new project into eclipse we create some code with a findbugs issues in it."

cmd "mkdir -p example-findbugsfodder/src/com/example/findbugsfodder"
module-edit findbugsfodder src findbugsreport FindbugsFodder

p "Now we can analyze the code."

cmde "0" "as-iwant-tutorial-developer/with/bash/iwant/list-of/targets"
out-was <<EOF
hello
example-findbugsfodder-main-java-findbugs-report
EOF

cmde "0 0" "as-iwant-tutorial-developer/with/bash/iwant/target/example-findbugsfodder-main-java-findbugs-report/as-path | xargs -I x find x -type f"
cmd "grep 'Null pointer dereference.*FindbugsFodder' as-iwant-tutorial-developer/.i-cached/target/example-findbugsfodder-main-java-findbugs-report/findbugs-report/example-findbugsfodder-main-java-findbugs-report.html"
out-was <<EOF
<td>Null pointer dereference of ? in com.example.findbugsfodder.FindbugsFodder.nullReference(Object)</td>
EOF

}
