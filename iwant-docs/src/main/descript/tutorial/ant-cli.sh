doc-content() {

cd iwant-tutorial

section "Using ant cli instead of bash"

p "In addition to the shell user interface there is also an ant interface. It is handy not only for Windows users, but also for calling iwant from java applications like Eclipse."

cmd "cd as-iwant-tutorial-developer/with/ant/iw"

p "Here we modify the time to keep this tutorial reproducible."

cmde "0 0" "ant | sed 's/[0-9]* second/\?/'"
cmde "0 0" "ant -Dwish=list-of/targets | sed 's/[0-9]* second/\?/'"
cmde "0 0" "ant -Dwish=target/hello/as-path | sed 's/[0-9]* second/\?/'"

}
