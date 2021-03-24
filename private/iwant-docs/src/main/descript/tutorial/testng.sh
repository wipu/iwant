doc-content() {

cd iwant-tutorial

p "testng in addition to junit"

wsdefdef-edit testng
cmde "0" "as-iwant-tutorial-developer/with/bash/iwant/side-effect/eclipse-settings/effective"

p " target."

wsdef-edit testng

cmd "mkdir -p example-testnguser/src/test/java/com/example/testnguser"
module-edit testnguser src/test/java redphase TestngExampleTest
cmd "mkdir -p example-testnguser/src/main/java/com/example/testnguser"
module-edit testnguser src/main/java redphase TestngExample

p "red phase"

cmde 1 "as-iwant-tutorial-developer/with/bash/iwant/target/jacoco-report/as-path"

p "green phase"

cmd "mkdir -p example-testnguser/src/main/java/com/example/testnguser"
module-edit testnguser src/main/java greenphase TestngExample
cmd "as-iwant-tutorial-developer/with/bash/iwant/target/jacoco-report/as-path"

cmd "cat as-iwant-tutorial-developer/.i-cached/target/jacoco-report/report.csv"
out-was <<EOF
GROUP,PACKAGE,CLASS,INSTRUCTION_MISSED,INSTRUCTION_COVERED,BRANCH_MISSED,BRANCH_COVERED,LINE_MISSED,LINE_COVERED,COMPLEXITY_MISSED,COMPLEXITY_COVERED,METHOD_MISSED,METHOD_COVERED
jacoco-report,com.example.hello,HelloMain,10,4,0,0,3,1,2,1,2,1
jacoco-report,com.example.helloutil,HelloUtil,3,4,0,0,1,1,1,1,1,1
jacoco-report,com.example.testnguser,TestngExample,3,2,0,0,1,1,1,1,1,1
EOF

}
