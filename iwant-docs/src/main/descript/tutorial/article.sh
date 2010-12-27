function bootstrapping() {
die "Sorry, deprecated"
doc 'section {name {Bootstrapping from svn}'

doc 'p {First let'\''s check out <code>iwant</code> from svn
        and bootstrap it as a shell user.}'
cmd 'svn co -r 64 https://iwant.svn.sourceforge.net/svnroot/iwant/trunk iwant-svn | tail -n 1'
cmd 'ls'
cmd 'cd iwant-svn/iwant-iwant'
cmd 'iwant/as_shell-user/to-bootstrap-iwant.sh 2>&1 | tail -n 3'
cmd 'iwant/as-iwant-user/to-use-iwant-on.sh'
out-was <<EOF
Usage: iwant/as-iwant-user/to-use-iwant-on.sh WSNAME WSROOT WSSRC WSDEFCLASS
EOF

doc '}'
}

local-bootstrapper() {
cmd "svn export \"$LOCAL_IWANT/../../iwant-bootstrapper/iwant\" iwant"
out-was <<EOF  
Export complete.
EOF
}

conf-iwant-from-local-wishdir() {
  cmd "echo local-iwant-wishdir \\\"$LOCAL_IWANT\\\" > i-have/iwant-from.conf"
}

conf-iwant-from-sfnet() {
  cmd "echo \"svn-revision 100\" > i-have/iwant-from.conf"
}

svn-bootstrapper() {
cmd "svn export -r 100 https://iwant.svn.sourceforge.net/svnroot/iwant/trunk/iwant-bootstrapper/iwant iwant"
out-was <<EOF
A    iwant
A    iwant/help.sh
Exported revision 100.
EOF
}

bootstrap() {
local FETCH_BOOTSTRAPPER=$1
local CONF_IWANT_FROM=$2
doc 'section {name {Bootstrapping}'
cmd 'mkdir -p example/as-example-developer && cd example/as-example-developer'
"$FETCH_BOOTSTRAPPER"
cmd 'iwant/help.sh'
out-was <<EOF
Welcome.

Please start by specify what version of iwant you wish to use.
I created file i-have/iwant-from.conf for you.
Modify it and rerun iwant/help.sh
EOF
cmd 'cat i-have/iwant-from.conf'
"$CONF_IWANT_FROM"
cmd 'iwant/help.sh 2>/dev/null | tail -n 2'
out-was <<EOF
Next, modify i-have/ws-info.conf to define your workspace.
After that, rerun iwant/help.sh
EOF
doc '}'
}

doc 'article {name {Tutorial}'

if [ "x" == "x$LOCAL_IWANT" ]; then
  bootstrap svn-bootstrapper conf-iwant-from-sfnet
else
  bootstrap local-bootstrapper conf-iwant-from-local-wishdir
fi

section Starting using kbd:iwant on a workspace
#----------------------------------------------

cmd 'cat i-have/ws-info.conf'
doc "p {Let's go with the defaults.}"
cmd 'iwant/help.sh'
out-was <<EOF
I created a stub workspace definition at iwant/../i-have/wsdef/com/example/wsdef/Workspace.java
Use find or code completion (tab) to see what you can iwant/
Have fun.
EOF

cmd 'find iwant/list-of'
out-was <<EOF
iwant/list-of
iwant/list-of/targets
EOF

cmd 'iwant/list-of/targets'
out-was <<EOF
aConstant
eclipse-projects
EOF

cmd 'find iwant/target -type f | sort'
out-was <<EOF
iwant/target/aConstant/as-path
iwant/target/eclipse-projects/as-path
EOF

cmd 'iwant/target/aConstant/as-path'
out-was <<EOF
iwant/target/aConstant/../../cached/example/target/aConstant
EOF

cmd 'cat iwant/cached/example/target/aConstant'
out-was <<EOF
Constant generated content
EOF

end

section Editing the workspace definition with Eclipse
#----------------------------------------------------

cmd 'iwant/target/eclipse-projects/as-path'
out-was <<EOF
iwant/target/eclipse-projects/../../cached/example/target/eclipse-projects
EOF
cmd 'find iwant/cached/example/target/eclipse-projects'
out-was <<EOF
iwant/cached/example/target/eclipse-projects
iwant/cached/example/target/eclipse-projects/as-example-developer
iwant/cached/example/target/eclipse-projects/as-example-developer/.classpath
iwant/cached/example/target/eclipse-projects/as-example-developer/.project
EOF

doc 'p {Import the project into Eclipse. Make sure not to copy it to the workspace.}'

WSJAVA=i-have/wsdef/com/example/wsdef/Workspace.java

edit "$WSJAVA" Workspace.java.new-constant-content.diff
cmd 'cat $(iwant/target/aConstant/as-path)'
out-was <<EOF
Modified constant content
EOF

end

doc 'section {name {Java classes}'

A_TESTS="project-a/tests"
cmd "mkdir -p ../$A_TESTS/example"
create-from "../$A_TESTS/example/ATest.java" "example-ws/$A_TESTS/example/ATest.java"
edit "$WSJAVA" Workspace.java.a-tests.diff
cmd 'iwant/list-of/targets | grep projectATests'
out-was <<EOF
projectATests
EOF

cmd 'java -cp $(iwant/target/projectATests/as-path) example.ATest'
out-was <<EOF
TODO make this a junit test
EOF

A_SRC="project-a/src"
sleep 2
edit "../$A_TESTS/example/ATest.java" ATest.java.aprod.diff
cmd "mkdir -p ../$A_SRC/example"
create-from "../$A_SRC/example/AProd.java" "example-ws/$A_SRC/example/AProd.java"
edit "$WSJAVA" Workspace.java.a-src.diff
cmd 'iwant/target/projectATests/as-path'
out-was <<EOF
iwant/target/projectATests/../../cached/example/target/projectATests
EOF
cmd 'java -cp iwant/cached/example/target/projectATests:iwant/cached/example/target/projectAClasses example.ATest'
out-was <<EOF
TODO make this a junit test to assert 0
EOF

doc '}'

doc 'section {name {JUnit tests}'

sleep 2
edit "../$A_TESTS/example/ATest.java" ATest.java.junit.diff
edit "$WSJAVA" Workspace.java.junit.diff
cmd 'iwant/list-of/targets | grep projectATestResult'
out-was <<EOF
projectATestResult
EOF
cmd '(iwant/target/projectATestResult/as-path && echo exit status was zero) | sed s:$(pwd)/::'
out-was <<EOF
Test example.ATest FAILED
iwant/target/projectATestResult/../../cached/example/target/projectATestResult
exit status was zero
EOF

cmd 'grep -m 1 expected $(iwant/target/projectATestResult/as-path)'
out-was <<EOF
expected:<42> but was:<0>
EOF

edit "../$A_SRC/example/AProd.java" AProd.java.redtogreen.diff
cmd 'cat $(iwant/target/projectATestResult/as-path) | sed s/[^\ ]*\ sec/***/'
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
cmd 'cat $(iwant/target/aConstant/as-path)'
out-was <<EOF
Modified constant content
EOF
cmd "find iwant/cached/example/target -newer $TS"
out-was < ../tmp/empty
edit "$WSJAVA" Workspace.java.another-constant-change-to-demo-laziness.diff
cmd 'iwant/target/projectATestResult/as-path > /dev/null'
cmd "find iwant/cached/example/target -newer $TS"
echo -n | out-was
cmd 'cat $(iwant/target/aConstant/as-path)'
out-was <<EOF
A change unrelated to java and test targets
EOF
cmd "find iwant/cached/example/target -newer $TS"
out-was <<EOF
iwant/cached/example/target/aConstant
EOF

doc '}'

doc 'section {name {Downloaded content}'

edit "../$A_SRC/example/AProd.java" AProd.java.use-commons-math.diff
cmd 'iwant/target/projectATestResult/as-path 2> /dev/null || echo "Compilation failed"'
out-was <<EOF
Compilation failed
EOF
edit "$WSJAVA" Workspace.java.use-commons-math.diff
cmd 'iwant/target/projectATestResult/as-path 2>&1 | sed s:$(pwd)/::'
out-was <<EOF
Getting: http://mirrors.ibiblio.org/pub/mirrors/maven2/commons-math/commons-math/1.2/commons-math-1.2.jar
To: iwant/target/projectATestResult/../../cached/example/target/commons-math
iwant/target/projectATestResult/../../cached/example/target/projectATestResult
EOF

doc '}'

doc 'section {name {Two-phase workspace definition}'

cmd 'mkdir -p ../example-wsdef2/src/com/example/wsdef2'
create-from '../example-wsdef2/src/com/example/wsdef2/ExampleWorkspace.java' ExampleWorkspace.java
create-from '../example-wsdef2/src/com/example/wsdef2/CustomContent.java' CustomContent.java
edit "$WSJAVA" Workspace.java.refer-to-phase2.diff
cmd 'iwant/list-of/targets'
out-was <<EOF
aConstant
commons-math
eclipse-projects
projectAClasses
projectATestResult
projectATests
wsdef2Classes
targetWithCustomContent
EOF

cmd 'cat $(iwant/target/targetWithCustomContent/as-path)'
out-was <<EOF
Hello 42
EOF

doc '}'

doc '}'
