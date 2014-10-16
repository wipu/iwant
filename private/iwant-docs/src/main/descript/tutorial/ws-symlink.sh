doc-content() {

cd iwant-tutorial

p "Canonical paths are used so even if you use a symbolic link to your workspace, the cache works as expected."

CACHED_HELLO=$(readlink -f ../iwant-tutorial/as-iwant-tutorial-developer/.i-cached/target/hello)

cmde "0 0" "find as-iwant-tutorial-developer -name '*.java' | xargs touch"

cmde "0" "as-iwant-tutorial-developer/with/bash/iwant/list-of/targets"
out-was <<EOF
(0/1 S~ net.sf.iwant.api.javamodules.JavaClasses iwant-tutorial-wsdefdef-main-classes)
(0/1 S~ net.sf.iwant.api.javamodules.JavaClasses iwant-tutorial-workspace-main-classes)
hello
EOF

cmd "cd .."
cmd "ln -s iwant-tutorial symlink-to-iwant-tutorial"
cmd "cd symlink-to-iwant-tutorial"

cmde "0" "as-iwant-tutorial-developer/with/bash/iwant/list-of/targets"
out-was <<EOF
hello
EOF

cmde "0" "as-iwant-tutorial-developer/with/bash/iwant/target/hello/as-path"
out-was <<EOF
$CACHED_HELLO
EOF

}
