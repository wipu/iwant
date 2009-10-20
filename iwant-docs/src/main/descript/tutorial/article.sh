function bootstrapping() {
doc 'section {name {Bootstrapping from svn}'

doc 'p {First let'\''s check out <code>iwant</code> from svn
        and bootstrap it as a shell user.}'
cmd 'svn co -r 37 https://iwant.svn.sourceforge.net/svnroot/iwant/trunk iwant-svn | tail -n 1'
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
	ln -s "$LOCAL_IWANT"
fi

doc 'section {name {Starting using kbd:iwant on a workspace}'

WSSRC=example-ws/ws-def/src
WSJAVA=$WSSRC/example/Workspace
WSCLASS=example.Workspace

cmd "mkdir -p $WSSRC/example"
cmd "echo public class Workspace {} > $WSJAVA"
cmd "iwant/as-iwant-user/to-use-iwant-on.sh example $WSSRC $WSCLASS"

out-was <<EOF
To get access to targets of the example workspace, start your sentences with
$ iwant/as-example-developer
EOF

cmd 'find iwant/as-example-developer'
out-was <<EOF
iwant/as-example-developer
iwant/as-example-developer/help
iwant/as-example-developer/list-of
iwant/as-example-developer/list-of/targets
EOF

doc '}'

doc '}'
