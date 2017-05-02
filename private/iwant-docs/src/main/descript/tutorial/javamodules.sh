doc-content() {

cd iwant-tutorial

p "First we define one java source module with main and test java and test resources and dependency to the binary module junit."
p "We also add the module to the eclipse-settings side-effect, and also define a target that lists the java class directory artifacts of the module."
wsdef-edit javamodule

p "Then we generate Eclipse settings for it so we can use Eclipse for writing the code."
cmd "as-iwant-tutorial-developer/with/bash/iwant/side-effect/eclipse-settings/effective"

p "Now we can import the project to Eclipse and write some code."

# invisible mkdir, user does it with eclipse
mkdir -p example-hello/src/{main,test}/java/com/example/hello
module-edit hello src/test/java first HelloMainTest
module-edit hello src/main/java first HelloMain

p "Finally, we can build and run some classes."

cmd "as-iwant-tutorial-developer/with/bash/iwant/target/hello-classes/as-path"
cmde "0 0" "as-iwant-tutorial-developer/with/bash/iwant/target/hello-classes/as-path | xargs -r cat"
cmd "java -cp as-iwant-tutorial-developer/.i-cached/target/example-hello-main-classes com.example.hello.HelloMain tutorial"
out-was <<EOF
Hello tutorial
EOF

p "Next we will use an optional convenience class for easier definition of modules."

p "First we'll enable the needed plugin and refresh eclipse settings."
wsdefdef-edit javamodules
cmde "0" "as-iwant-tutorial-developer/with/bash/iwant/side-effect/eclipse-settings/effective"

p "Now we will use the class JavaModule to define more modules and utilize some convenience functionality of it."

wsdef-edit javamodules

p "We write some code to the new module"
cmde "0" "as-iwant-tutorial-developer/with/bash/iwant/side-effect/eclipse-settings/effective"
mkdir -p example-helloutil/src/{main,test}/java/com/example/helloutil
module-edit helloutil src/test/java first HelloUtilTest
module-edit helloutil src/main/java first HelloUtil
module-edit hello src/main/java useutil HelloMain

p "Now we can get all our classes as a classpath string."
cmde "0 0" "as-iwant-tutorial-developer/with/bash/iwant/target/all-as-cp/as-path | xargs -r cat"

p "Let's use the classpath to run the application again."
cmd 'java -cp $(as-iwant-tutorial-developer/with/bash/iwant/target/all-as-cp/as-path | xargs -r cat) com.example.hello.HelloMain "same tutorial"'
out-was <<EOF
Hello same tutorial
EOF

}
