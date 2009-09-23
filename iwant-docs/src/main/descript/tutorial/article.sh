function bootstrapping() {
doc 'section {name {Bootstrapping from svn}'

doc 'p {First let'\''s check out <code>iwant</code> from svn
        and bootstrap it as a shell user.}'
cmd 'svn co -r 16 https://iwant.svn.sourceforge.net/svnroot/iwant/trunk iwant-svn | tail -n 1'
cmd 'ls'
cmd 'cd iwant-svn/iwant-iwant'
cmd 'iwant/as_shell-user/to_use_iwant.sh'
cmd 'iwant/as_iwant-user/some_help'

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

cmd 'mkdir -p example-ws/ws-def/src'
cmd 'iwant/as_iwant-user/start_using_iwant_on example-ws/ws-def/src'
out-was <<\EOF
Please describe the workspace in file example-ws/ws-def/src/Workspace.java
EOF

doc '}'

doc '}'
