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
cmde "0 0" "as-iwant-tutorial-developer/with/bash/iwant/target/hello-classes/as-path | xargs cat"
cmd "java -cp as-iwant-tutorial-developer/.i-cached/target/example-hello-main-classes com.example.hello.HelloMain tutorial"
out-was <<EOF
Hello tutorial
EOF

}
