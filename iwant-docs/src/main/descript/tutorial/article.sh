function bootstrapping() {
doc 'section {name {Bootstrapping from svn}'

doc 'p {First let'\''s check out <code>iwant</code> from svn
        and bootstrap it as a shell user.}'
cmd 'svn co -r 41 https://iwant.svn.sourceforge.net/svnroot/iwant/trunk iwant-svn | tail -n 1'
cmd 'ls'
cmd 'cd iwant-svn/iwant-iwant'
cmd 'iwant/as_shell-user/to-bootstrap-iwant.sh 2>&1 | tail -n 3'
cmd 'iwant/as-iwant-user/to-use-iwant-on.sh'
out-was <<EOF
Usage: iwant/as-iwant-user/to-use-iwant-on.sh WSNAME WSSRC WSDEFCLASS
EOF

doc '}'
}


doc 'article {name {Tutorial}'

if [ "x" == "x$LOCAL_IWANT" ]; then
	bootstrapping
else
	doc 'p {i {Using local iwant}}'
	mkdir -p iwant/cached/iwant
	cp -a "$LOCAL_IWANT/as-iwant-user" iwant/
	cp -a "$LOCAL_IWANT/cached/iwant/scripts" iwant/cached/iwant/
	cp -a "$LOCAL_IWANT/cached/iwant/classes" iwant/cached/iwant/
fi

doc 'section {name {Starting using kbd:iwant on a workspace}'

WSSRC=example-ws/ws-def/src
WSJAVA=$WSSRC/example/Workspace.java
WSCLASS=example.Workspace

cmd "mkdir -p $WSSRC/example"
cmd "echo package example\; > $WSJAVA"
cmd "echo import net.sf.iwant.core.Path\; >> $WSJAVA"
cmd "echo public class Workspace { >> $WSJAVA"
cmd "echo public Path aConstant\(\) {return null\;} >> $WSJAVA"
cmd "echo } >> $WSJAVA"
cmd "iwant/as-iwant-user/to-use-iwant-on.sh example $WSSRC $WSCLASS"

out-was <<EOF
To get access to targets of the example workspace, start your sentences with
$ iwant/as-example-developer
EOF

cmd 'find iwant/as-example-developer -type f'
out-was <<EOF
iwant/as-example-developer/help
iwant/as-example-developer/list-of/targets
EOF

cmd 'iwant/as-example-developer/list-of/targets'
out-was <<EOF
aConstant
EOF

cmd 'find iwant/as-example-developer -type f'
out-was <<EOF
iwant/as-example-developer/help
iwant/as-example-developer/list-of/targets
iwant/as-example-developer/target/aConstant/as-path
EOF

cmd 'iwant/as-example-developer/target/aConstant/as-path'
out-was <<EOF
iwant/cached/example/target/aConstant
EOF

cmd 'cat iwant/cached/example/target/aConstant'
out-was <<EOF
Constant generated content
EOF

doc '}'

doc '}'
