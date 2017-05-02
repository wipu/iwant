doc-content() {

cd iwant-tutorial

wsdefdef-edit usingmoduleinbuild
cmd "mkdir -p example-util/src/main/java/com/example/util"
cmde "0" "as-iwant-tutorial-developer/with/bash/iwant/side-effect/eclipse-settings/effective"

p "We import the new module to Eclipse and create a utility."

module-edit util src/main/java usingmoduleinbuild ExampleUtil

p "Now the utility is available in our build as well as being served by it."

wsdef-edit usingmoduleinbuild

cmde "0" "as-iwant-tutorial-developer/with/bash/iwant/list-of/targets"
cmde "0 0" "as-iwant-tutorial-developer/with/bash/iwant/target/targetUsingModuleFromSameWs/as-path | xargs -r cat "

}
