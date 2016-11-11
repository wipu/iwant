doc-content() {

cd iwant-tutorial

p "In addition to compiling, running automated tests is one of the main tasks of build scripts."

p "But since we want to keep things declarative instead of imperative, we don't want to tell iwant to compile or run tests. Instead we wish for paths to targets that include the results of compiling and running tests."

p "The result of a compilation as a noun is easy, and many build solutions manage to keep the vocabulary declarative there. But how to avoid being imperative with test runs?"

p "The result of a test run is basically a boolean: the tests either all passed or there was a failure. But we can do better than that: in addition to the success of the tests, we are also interested in the code coverage they give us."

html "<p class='text'>So, with iwant, <i>\"running tests\" means wishing for a coverage report</i>.</p>"

p "Let's see how this is done. First we'll enable the needed plugin, jacoco, and refresh eclipse settings."
wsdefdef-edit jacoco
cmde "0" "as-iwant-tutorial-developer/with/bash/iwant/side-effect/eclipse-settings/effective"

p "Then we define the jacoco-report target."

wsdef-edit jacoco

p "We list targets to refresh the wish scripts and then wish for the coverage report."

cmd "as-iwant-tutorial-developer/with/bash/iwant/list-of/targets"

cmd "as-iwant-tutorial-developer/with/bash/iwant/target/jacoco-report/as-path"

p "Let's see what is in the report. Here we already benefit from our declarative wish: since we didn't tell iwant to run anything and we haven't touched anything, we can just make the same wish again without having to wait for another test run."

cmde "0 0" "as-iwant-tutorial-developer/with/bash/iwant/target/jacoco-report/as-path | xargs ls"

p "You are probably more interested in the html version, but here we'll take a look at the csv file."

cmd "cat as-iwant-tutorial-developer/.i-cached/target/jacoco-report/report.csv"
out-was <<EOF
GROUP,PACKAGE,CLASS,INSTRUCTION_MISSED,INSTRUCTION_COVERED,BRANCH_MISSED,BRANCH_COVERED,LINE_MISSED,LINE_COVERED,COMPLEXITY_MISSED,COMPLEXITY_COVERED,METHOD_MISSED,METHOD_COVERED
jacoco-report,com.example.hello,HelloMain,10,4,0,0,3,1,2,1,2,1
jacoco-report,com.example.helloutil,HelloUtil,3,11,0,0,1,1,1,1,1,1
EOF

}
