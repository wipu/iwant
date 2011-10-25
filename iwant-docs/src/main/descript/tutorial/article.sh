REL_AS_SOMEONE=../..
REL_IHAVE=$REL_AS_SOMEONE/i-have
REL_WSROOT=$REL_AS_SOMEONE/..

local-bootstrapper() {
cmd "svn export \"$LOCAL_IWANT_WSROOT/iwant-bootstrapper/as-someone/with\""
out-was <<EOF  
Export complete.
EOF
}

conf-iwant-from-local-wishdir() {
edit "$REL_IHAVE/iwant-from.conf" use-local-iwant <<EOF
iwant-rev=
iwant-url=$LOCAL_IWANT_WSROOT
EOF
}

conf-iwant-from-sfnet() {
p 'TODO use a tag here.'
edit "$REL_IHAVE/iwant-from.conf" use-remote-iwant <<EOF
iwant-rev=
iwant-url=https://iwant.svn.sourceforge.net/svnroot/iwant/trunk
EOF
}

svn-bootstrapper() {
BOOTSTRAPPER_REV=192
cmd "svn export -r $BOOTSTRAPPER_REV https://iwant.svn.sourceforge.net/svnroot/iwant/trunk/iwant-bootstrapper/as-someone/with"
out-was <<EOF
A    with
A    with/ant
A    with/ant/iw
A    with/ant/iw/build.xml
A    with/bash
A    with/bash/iwant
A    with/bash/iwant/help.sh
Exported revision $BOOTSTRAPPER_REV.
EOF
}

bootstrap() {
local FETCH_BOOTSTRAPPER=$1
local CONF_IWANT_FROM=$2
section "Bootstrapping"
cmd 'mkdir -p example/as-example-developer && cd example/as-example-developer'
WSROOT=$(readlink -f ..)
"$FETCH_BOOTSTRAPPER"
cmd 'cd with/bash'
cmde 1 'iwant/help.sh'
out-was <<EOF
I created file $WSROOT/as-example-developer/i-have/iwant-from.conf for your convenience. Please edit it and rerun me.
EOF
"$CONF_IWANT_FROM"
cmde 1 'iwant/help.sh'
out-was <<EOF
I created $WSROOT/as-example-developer/i-have/ws-info.conf for you. Please edit it and rerun me.
EOF
end-section
}

is-online-tutorial() {
  [ "${LOCAL_IWANT_WSROOT:-}" == "" ]
}

# temporary, remove when descript supports these properly:
end-section() {
  debuglog "TODO support end-section with nesting"
}

kbd() {
  echo "$@"
}

doc-name() {
  echo Tutorial
}

doc() {

if is-online-tutorial; then
  bootstrap svn-bootstrapper conf-iwant-from-sfnet
else
  bootstrap local-bootstrapper conf-iwant-from-local-wishdir
fi

section "Starting using $(kbd iwant) on a workspace"
#---------------------------------------------------

cmd "cat $REL_IHAVE/ws-info.conf"
p "Let's go with the defaults."
cmde 1 'iwant/help.sh'
out-was <<EOF
I created $WSROOT/as-example-developer/i-have/wsdef/com/example/wsdef/Workspace.java for you. Please edit it and rerun me.
EOF

cmd "cat $REL_IHAVE/wsdef/com/example/wsdef/Workspace.java"
cmde 1 'iwant/help.sh'

cmd 'iwant/list-of/targets'
out-was <<EOF
aConstant
eclipse-projects
EOF

cmd 'iwant/target/aConstant/as-path'
out-was <<EOF
$PWD/iwant/cached/example/target/aConstant
EOF

cmd 'cat iwant/cached/example/target/aConstant'
out-was <<EOF
Constant generated content
EOF

end-section

MYEDITS=$(dirname "$DOC")/edits

my-edit() {
  local FILE=$1
  local DIFF=$2
  local MYDIFF=$MYEDITS/$DIFF
  debuglog "my-edit $FILE $DIFF ($MYDIFF)"
  diffedit "$FILE" "edited-by-$DIFF" < "$MYDIFF"
}

create-from() {
  local FILE=$1
  local FROM=$2
  local MYFROM=$MYEDITS/$FROM
  debuglog "create-from $FILE $FROM ($MYFROM)"
  edit "$FILE" "creation" < "$MYFROM"
}

section "Editing the workspace definition with Eclipse"
#------------------------------------------------------

cmd 'iwant/target/eclipse-projects/as-path'
out-was <<EOF
$PWD/iwant/cached/example/target/eclipse-projects
EOF
cmd 'find iwant/cached/example/target/eclipse-projects'
out-was <<EOF
iwant/cached/example/target/eclipse-projects
iwant/cached/example/target/eclipse-projects/as-example-developer
iwant/cached/example/target/eclipse-projects/as-example-developer/.classpath
iwant/cached/example/target/eclipse-projects/as-example-developer/.project
EOF

p 'Import the project into Eclipse. Make sure not to copy it to the workspace.'

WSJAVA=$REL_IHAVE/wsdef/com/example/wsdef/Workspace.java

my-edit "$WSJAVA" Workspace.java.new-constant-content.diff
cmd 'cat $(iwant/target/aConstant/as-path)'
out-was <<EOF
Modified constant content
EOF

end-section

section 'Java classes'
#---------------------

A_TESTS=project-a/tests
sleep 2
cmd "mkdir -p $REL_WSROOT/$A_TESTS/example"
create-from "$REL_WSROOT/$A_TESTS/example/ATest.java" "example-ws/$A_TESTS/example/ATest.java"
my-edit "$WSJAVA" Workspace.java.a-tests.diff
cmde "0 0" 'iwant/list-of/targets | grep projectATests'
out-was <<EOF
projectATests
EOF

cmd 'iwant/target/projectATests/as-path'
cmd 'java -cp $(iwant/target/projectATests/as-path) example.ATest'
out-was <<EOF
TODO make this a junit test
EOF

A_SRC="project-a/src"
sleep 2
my-edit "$REL_WSROOT/$A_TESTS/example/ATest.java" ATest.java.aprod.diff
cmd "mkdir -p $REL_WSROOT/$A_SRC/example"
create-from "$REL_WSROOT/$A_SRC/example/AProd.java" "example-ws/$A_SRC/example/AProd.java"
my-edit "$WSJAVA" Workspace.java.a-src.diff
cmd 'iwant/target/projectATests/as-path'
out-was <<EOF
$PWD/iwant/cached/example/target/projectATests
EOF
cmd 'java -cp iwant/cached/example/target/projectATests:iwant/cached/example/target/projectAClasses example.ATest'
out-was <<EOF
TODO make this a junit test to assert 0
EOF

end-section

section "JUnit tests"
#--------------------

sleep 2
my-edit "$REL_WSROOT/$A_TESTS/example/ATest.java" ATest.java.junit.diff
my-edit "$WSJAVA" Workspace.java.junit.diff
cmde "0 0" 'iwant/list-of/targets | grep projectATestResult'
out-was <<EOF
projectATestResult
EOF
cmd 'iwant/target/projectATestResult/as-path || echo "Refresh failed."'
out-was <<EOF
Test example.ATest FAILED
$PWD/iwant/cached/example/target/projectATestResult
EOF

cmd 'grep -m 1 expected $(iwant/target/projectATestResult/as-path)'
out-was <<EOF
expected:<42> but was:<0>
EOF

my-edit "$REL_WSROOT/$A_SRC/example/AProd.java" AProd.java.redtogreen.diff
cmde "0 0" 'cat $(iwant/target/projectATestResult/as-path) | sed s/[^\ ]*\ sec/***/'
out-was <<EOF
Testsuite: example.ATest
Tests run: 1, Failures: 0, Errors: 0, Time elapsed: ***

Testcase: testAValue took ***
EOF

end-section

section "Laziness"
#-----------------

find-newer-than-ts() {
  cmd "find iwant/cached/example/target -mindepth 1 -newer $TS"
}

TS="touched-after-src"
cmd "touch $TS"
find-newer-than-ts
echo -n | out-was
cmd 'cat $(iwant/target/aConstant/as-path)'
out-was <<EOF
Modified constant content
EOF
find-newer-than-ts
echo -n | out-was
my-edit "$WSJAVA" Workspace.java.another-constant-change-to-demo-laziness.diff
cmd 'iwant/target/projectATestResult/as-path > /dev/null'
find-newer-than-ts
echo -n | out-was
cmd 'cat $(iwant/target/aConstant/as-path)'
out-was <<EOF
A change unrelated to java and test targets
EOF
find-newer-than-ts
out-was <<EOF
iwant/cached/example/target/aConstant
EOF

end-section

section "Downloaded content"
#---------------------------

my-edit "$REL_WSROOT/$A_SRC/example/AProd.java" AProd.java.use-commons-math.diff
cmd 'iwant/target/projectATestResult/as-path 2> /dev/null || echo "Compilation failed"'
out-was <<EOF
Compilation failed
EOF
my-edit "$WSJAVA" Workspace.java.use-commons-math.diff
cmd 'iwant/target/projectATestResult/as-path'
out-was <<EOF
Downloading http://mirrors.ibiblio.org/pub/mirrors/maven2/commons-math/commons-math/1.2/commons-math-1.2.jar
$PWD/iwant/cached/example/target/projectATestResult
EOF

end-section

section "Two-phase workspace definition"
#---------------------------------------

cmd 'mkdir -p $REL_WSROOT/example-wsdef2/src/com/example/wsdef2'
create-from "$REL_WSROOT/example-wsdef2/src/com/example/wsdef2/ExampleWorkspace.java" ExampleWorkspace.java
create-from "$REL_WSROOT/example-wsdef2/src/com/example/wsdef2/CustomContent.java" CustomContent.java
my-edit "$WSJAVA" Workspace.java.refer-to-phase2.diff
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

end-section

section "Script-generated content"
#---------------------------------

sleep 2
my-edit "$REL_WSROOT/example-wsdef2/src/com/example/wsdef2/ExampleWorkspace.java" ExampleWorkspace.java.scriptGeneratedContent.diff

cmde "0 0" 'iwant/list-of/targets | grep scriptGeneratedContent'
out-was <<EOF
scriptGeneratedContent
EOF
cmd 'iwant/target/scriptGeneratedContent/as-path'
out-was <<EOF
$PWD/iwant/cached/example/target/scriptGeneratedContent
EOF

cmd 'cat $(iwant/target/scriptGeneratedContent/as-path)'
out-was <<EOF
hello from script
EOF

end-section

end-section
}
