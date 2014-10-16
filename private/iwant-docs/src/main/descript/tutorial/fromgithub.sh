doc-content() {

cd iwant-tutorial

p "First we add the iwant github plugin to the project."

wsdefdef-edit fromgithub

p "We refresh eclipse settings so we can use the plugin in our workspace definition."

cmde "0" "as-iwant-tutorial-developer/with/bash/iwant/side-effect/eclipse-settings/effective"

p "After refreshing eclipse we define a java classes target compiled from sources gotten from github."

wsdef-edit fromgithub
cmde "0" "as-iwant-tutorial-developer/with/bash/iwant/list-of/targets"
cmde "0" "as-iwant-tutorial-developer/with/bash/iwant/target/joulu-code/as-path"
cmd "ls as-iwant-tutorial-developer/.i-cached/target/joulu-code"
out-was <<EOF
LICENSE
README.md
as-joulu-developer
byte-consumer
byte-producer
byte-producers
collections
equivalence
optional
stories
strongly-typed
unsigned-byte
EOF

}
