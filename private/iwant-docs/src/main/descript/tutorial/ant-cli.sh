doc-content() {

cd iwant-tutorial

p "In addition to the shell user interface there is also an ant interface. It is handy not only for Windows users, but also for calling iwant from java applications like Eclipse."

cmd "cd as-iwant-tutorial-developer/with/ant/iw"

p "Here we filter out the time to keep this tutorial reproducible."

cmde "0 0" "ant | grep -v '^Total time:'"
cmde "0 0" "ant -Dwish=list-of/targets | grep -v '^Total time:'"
cmde "0 0" "ant -Dwish=target/hello/as-path | grep -v '^Total time:'"

}
