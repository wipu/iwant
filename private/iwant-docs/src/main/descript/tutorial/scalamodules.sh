doc-content() {

cd iwant-tutorial

wsdef-edit scalamodule

p "Then we generate Eclipse settings for it so we can use Eclipse for writing the code."
cmd "as-iwant-tutorial-developer/with/bash/iwant/side-effect/eclipse-settings/effective"

p "Now we can import the project to Eclipse and write some code."

# invisible mkdir, user does it with eclipse
mkdir -p example-mixedscala/src/main/{java,scala}/com/example/mixedscala
module-edit mixedscala src/main/java  first JavaThatDependsOnScala
module-edit mixedscala src/main/scala first ScalaThatDependsOnJava scala
module-edit mixedscala src/main/java  first JavaHello

p "TODO remove:"
module-edit mixedscala src/main/java first ScalaThatDependsOnJava

p "Finally, we can build and run some classes."

cmd "as-iwant-tutorial-developer/with/bash/iwant/target/example-mixedscala-classpath/as-path"
cmde "0 0" "as-iwant-tutorial-developer/with/bash/iwant/target/example-mixedscala-classpath/as-path | xargs cat"
cmd 'java -cp "$(as-iwant-tutorial-developer/with/bash/iwant/target/example-mixedscala-classpath/as-path | xargs cat)" com.example.mixedscala.JavaThatDependsOnScala'
out-was <<EOF
scala calling hello from java
EOF

}
