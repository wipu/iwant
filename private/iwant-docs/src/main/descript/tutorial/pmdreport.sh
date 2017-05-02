doc-content() {

cd iwant-tutorial

p "First we add the iwant pmd plugin to the project."

wsdefdef-edit pmdreport

p "We refresh eclipse settings so we can use the plugin in our workspace definition."

cmde "0" "as-iwant-tutorial-developer/with/bash/iwant/side-effect/eclipse-settings/effective"

p "After refreshing eclipse we define a module to test PMD on, and define a PMD report target for its main java."

wsdef-edit pmdreport

p "Another eclipse settings refresh to create the new eclipse project."

cmde "0" "as-iwant-tutorial-developer/with/bash/iwant/side-effect/eclipse-settings/effective"

p "After importing the new project into eclipse we create some code with an PMD issue in it."

cmd "mkdir -p example-pmdfodder/src/com/example/pmdfodder"
module-edit pmdfodder src pmdreport PmdFodder

p "Now we can analyze the code."

cmde "0" "as-iwant-tutorial-developer/with/bash/iwant/list-of/targets"
out-was <<EOF
hello
example-pmdfodder-main-java-pmd-report
EOF

cmde "0 0" "as-iwant-tutorial-developer/with/bash/iwant/target/example-pmdfodder-main-java-pmd-report/as-path | xargs -r ls"
cmd "cat as-iwant-tutorial-developer/.i-cached/target/example-pmdfodder-main-java-pmd-report/example-pmdfodder-main-java-pmd-report.txt"
(echo;echo -e "com/example/pmdfodder/PmdFodder.java:5\tMethod names should not start with capital letters") | out-was
cmde "0 0" "cat as-iwant-tutorial-developer/.i-cached/target/example-pmdfodder-main-java-pmd-report/example-pmdfodder-main-java-pmd-report.xml | wc -l"
out-was <<EOF
8
EOF

}
