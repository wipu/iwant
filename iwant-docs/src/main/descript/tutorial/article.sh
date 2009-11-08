function bootstrapping() {
doc 'section {name {Bootstrapping from svn}'

doc 'p {First let'\''s check out <code>iwant</code> from svn
        and bootstrap it as a shell user.}'
cmd 'svn co -r 56 https://iwant.svn.sourceforge.net/svnroot/iwant/trunk iwant-svn | tail -n 1'
cmd 'ls'
cmd 'cd iwant-svn/iwant-iwant'
cmd 'iwant/as_shell-user/to-bootstrap-iwant.sh 2>&1 | tail -n 3'
cmd 'iwant/as-iwant-user/to-use-iwant-on.sh'
out-was <<EOF
Usage: iwant/as-iwant-user/to-use-iwant-on.sh WSNAME WSROOT WSSRC WSDEFCLASS
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
	cp -a "$LOCAL_IWANT/cached/iwant/cpitems" iwant/cached/iwant/
fi

doc 'section {name {Starting using kbd:iwant on a workspace}'

WSROOT=example-ws
WSSRC=$WSROOT/ws-def/src
WSJAVA=$WSSRC/example/Workspace.java
WSCLASS=example.Workspace

cmd "mkdir -p $WSSRC/example"
create "$WSJAVA"
cmd "iwant/as-iwant-user/to-use-iwant-on.sh example $WSROOT $WSSRC $WSCLASS"

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

cmd 'iwant/as-example-developer/target/aConstant/as-path | grep -o iwant/cached/example.*'
out-was <<EOF
iwant/cached/example/target/aConstant
EOF

cmd 'cat iwant/cached/example/target/aConstant'
out-was <<EOF
Constant generated content
EOF

edit "$WSJAVA" Workspace.java.new-constant-content.diff
cmd 'cat $(iwant/as-example-developer/target/aConstant/as-path)'
out-was <<EOF
Modified constant content
EOF

doc '}'

doc 'section {name {Java classes}'

A_TESTS="example-ws/project-a/tests"
cmd "mkdir -p $A_TESTS/example"
create "$A_TESTS/example/ATest.java"
edit "$WSJAVA" Workspace.java.a-tests.diff
cmd 'iwant/as-example-developer/list-of/targets | grep projectATests'
out-was <<EOF
projectATests
EOF

cmd 'java -cp $(iwant/as-example-developer/target/projectATests/as-path) example.ATest'
out-was <<EOF
TODO make this a junit test
EOF

A_SRC="example-ws/project-a/src"
sleep 2
edit "$A_TESTS/example/ATest.java" ATest.java.aprod.diff
cmd "mkdir -p $A_SRC/example"
create "$A_SRC/example/AProd.java"
edit "$WSJAVA" Workspace.java.a-src.diff
cmd 'iwant/as-example-developer/target/projectATests/as-path | grep -o iwant/cached/example.*'
out-was <<EOF
iwant/cached/example/target/projectATests
EOF
cmd 'java -cp iwant/cached/example/target/projectATests:iwant/cached/example/target/projectAClasses example.ATest'
out-was <<EOF
TODO make this a junit test to assert 0
EOF

doc '}'

doc 'section {name {JUnit tests}'

sleep 2
edit "$A_TESTS/example/ATest.java" ATest.java.junit.diff
edit "$WSJAVA" Workspace.java.junit.diff
cmd 'iwant/as-example-developer/list-of/targets | grep projectATestResult'
out-was <<EOF
projectATestResult
EOF
cmd '(iwant/as-example-developer/target/projectATestResult/as-path && echo exit status was zero) | sed s:$(pwd)/::'
out-was <<EOF
Test example.ATest FAILED
iwant/cached/example/target/projectATestResult
exit status was zero
EOF

cmd 'grep -m 1 expected $(iwant/as-example-developer/target/projectATestResult/as-path)'
out-was <<EOF
expected:<42> but was:<0>
EOF

edit "$A_SRC/example/AProd.java" AProd.java.redtogreen.diff
cmd 'cat $(iwant/as-example-developer/target/projectATestResult/as-path) | sed s/[^\ ]*\ sec/***/'
out-was <<EOF
Testsuite: example.ATest
Tests run: 1, Failures: 0, Errors: 0, Time elapsed: ***

Testcase: testAValue took ***
EOF

doc '}'

doc 'section {name {Laziness}'

TS="touched-after-src"
cmd "touch $TS"
cmd "find iwant/cached/example/target -newer $TS"
echo -n | out-was
cmd 'iwant/as-example-developer/target/projectATestResult/as-path > /dev/null'
cmd "find iwant/cached/example/target -newer $TS"
echo -n | out-was

doc '}'

doc '}'
