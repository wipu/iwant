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

WSSRC=example-ws/ws-def/src
WSJAVA=$WSSRC/Workspace.java
cmd "mkdir -p $WSSRC"
cmd "iwant/as_iwant-user/start_using_iwant_on $WSSRC"
out-was <<EOF
Please describe the workspace in file $WSJAVA
EOF


cmd "echo garbage > $WSJAVA"
cmd "iwant/as_iwant-user/start_using_iwant_on $WSSRC 2>&1 | head -n 3"
out-was <<EOF
$WSJAVA:1: 'class' or 'interface' expected
garbage
^
EOF

cmd "echo public class Workspace {} > $WSJAVA"
cmd "iwant/as_iwant-user/start_using_iwant_on $WSSRC"
out-was <<EOF
Exception in thread "main" java.lang.NoSuchMethodError: main
EOF

cmd "echo public class Workspace {public static void main\(String[] args\){}} > $WSJAVA"
cmd "iwant/as_iwant-user/start_using_iwant_on $WSSRC"
out-was <<\EOF
Start the output with line iwant-workspace
EOF

cmd 'echo public class Workspace {public static void main\(String[] args\){System.out.println\(\"iwant-workspace\"\)\;}} > '"$WSJAVA"
cmd "iwant/as_iwant-user/start_using_iwant_on $WSSRC"
out-was <<\EOF
Please specify the workspace name on a line of form name:THENAME
EOF

cmd 'echo public class Workspace {public static void main\(String[] args\){System.out.println\(\"iwant-workspace\"\)\;System.out.println\(\"name:example-ws\"\)\;}} > '"$WSJAVA"
cmd "iwant/as_iwant-user/start_using_iwant_on $WSSRC"
out-was <<EOF
iwant-workspace
name:example-ws
To get access to targets of the example-ws workspace, start your sentences with
$ iwant/as_example-ws-developer
EOF

cmd 'ls iwant/as_example-ws-developer'
out-was <<EOF
command-to
path-to-fresh
EOF

doc '}'

doc '}'
