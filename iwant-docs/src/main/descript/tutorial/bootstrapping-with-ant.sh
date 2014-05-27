end-section() {
  debuglog "TODO Really, define end-section in descript."
}

local-get-phase1() {
cmd "svn export \"$LOCAL_IWANT_WSROOT/iwant-bootstrapper/as-someone/with\""
out-was <<EOF
Export complete.
EOF
}

remote-get-phase1() {
local REV=623
cmd "svn export -r $REV https://svn.code.sf.net/p/iwant/code/trunk/iwant-bootstrapper/as-someone/with"
out-was <<EOF
A    with
A    with/ant
A    with/ant/iw
A    with/ant/iw/build.xml
A    with/bash
A    with/bash/iwant
A    with/bash/iwant/help.sh
Exported revision $REV.
EOF
}

is-local() {
  [ "${LOCAL_IWANT_WSROOT:-}" != "" ]
}

get-phase1() {
  cmd 'mkdir as-example-developer && cd as-example-developer'
  if is-local; then
    local-get-phase1
  else
    remote-get-phase1
  fi
}

antcmd() {
  cmd 'ant | head -n -1'
}

EXAMPLENAME=ant
PHASE1=ant
REL_AS_SOMEONE=../../..
REL_IHAVE=$REL_AS_SOMEONE/i-have
PHASE1XML=build.xml

phase1-run-1() {
section "First run to create iwant-from.conf"
p "The first run creates us a file in which we can specify the iwant revision to use."
cmde "0 0" "find $REL_AS_SOMEONE -not -type d | sort"
out-was <<EOF
$REL_AS_SOMEONE/with/ant/iw/build.xml
$REL_AS_SOMEONE/with/bash/iwant/help.sh
EOF
cmde 1 "$PHASE1"
cmde "0 0" "find $REL_AS_SOMEONE -not -type d | sort"
out-was <<EOF
$REL_AS_SOMEONE/i-have/iwant-from.conf
$REL_AS_SOMEONE/with/ant/iw/build.xml
$REL_AS_SOMEONE/with/bash/iwant/help.sh
EOF
cmd "cat $REL_AS_SOMEONE/i-have/iwant-from.conf"
end-section
}

phase1-run-with-incorrect-iwant-from() {
section "Test handling of incorrect iwant-from.conf"
p "The bootstrapper complains if iwant-url is not specified."
edit "$REL_IHAVE/iwant-from.conf" empty-file <<EOF
EOF
cmde 1 "$PHASE1"
p "No further side-effects until we fix the issue:"
cmde "0 0" "find $REL_AS_SOMEONE -not -type d | sort"
out-was <<EOF
$REL_AS_SOMEONE/i-have/iwant-from.conf
$REL_AS_SOMEONE/with/ant/iw/build.xml
$REL_AS_SOMEONE/with/bash/iwant/help.sh
EOF
end-section
}

optimize-downloads() {
  p "Using cached external libraries to optimize building this article."
  local OPTIMCACHE=$LOCAL_IWANT_WSROOT/as-iwant-developer/with/bash/iwant/cached/.internal/unmodifiable
  local SVNKITZIP=org.tmatesoft.svn_1.7.10.standalone.nojna.zip
  local INTERNALCACHE=$REL_AS_SOMEONE/with/bash/iwant/cached/.internal/unmodifiable
  [ -e "$OPTIMCACHE/$SVNKITZIP" ] || {
    log "Fetching svnkit using the ant script to test."
    ant -f "$PHASE1XML" svnkit.zip
    log "Caching it to $OPTIMCACHE"
    mkdir -p "$OPTIMCACHE"
    cp "$INTERNALCACHE/$SVNKITZIP" "$OPTIMCACHE"/
  }
  log "Copying cached svnkit from $OPTIMCACHE to $INTERNALCACHE"
  mkdir -p "$INTERNALCACHE"
  cp -v "$OPTIMCACHE/$SVNKITZIP" "$INTERNALCACHE"/
  log "Also copying jars for iwant compilation."
  cp -v "$LOCAL_IWANT_WSROOT/as-iwant-developer/with/bash/iwant/cached/.internal/iwant/iwant-bootstrapper/phase2/iw/cached/.internal/bin/"*.jar "$INTERNALCACHE"/
}

phase1-run-with-iwant-from-local() {
section "We'll use a local copy of iwant."

edit "$REL_IHAVE/iwant-from.conf" use-local-iwant <<EOF
iwant-url=$LOCAL_IWANT_WSROOT
EOF
optimize-downloads
}

phase1-run-with-iwant-from-sfnet() {
section "We'll use iwant HEAD."

edit "$REL_IHAVE/iwant-from.conf" use-local-iwant <<EOF
iwant-url=https://svn.code.sf.net/p/iwant/code/trunk
EOF
}

cmd-phase1-filter-iwant-src-export() {
  cmde "1 0" "$PHASE1 | grep -v '^     \[java\] A'"
}

phase1-run-with-correct-iwant-from() {
if is-local; then
  phase1-run-with-iwant-from-local
else
  phase1-run-with-iwant-from-sfnet
fi
cmd-phase1-filter-iwant-src-export
cmde "0 0" "find $REL_IHAVE | sort"
out-was <<EOF
$REL_IHAVE
$REL_IHAVE/iwant-from.conf
$REL_IHAVE/ws-info.conf
EOF
end-section
}

phase1-run-with-correct-ws-info() {
section "Generate the workspace definition java file"
p "Let's modify the file the iwant generated for us."
edit "$REL_IHAVE/ws-info.conf" creation <<EOF
# paths are relative to this file's directory
WSNAME=$EXAMPLENAME-bootstrap-example
WSROOT=../..
WSDEF_SRC=wsdef
WSDEF_CLASS=com.${EXAMPLENAME}bootstrapexample.wsdef.Workspace
EOF
p "Now iwant will generate the Workspace definition."
cmde 1 "$PHASE1"
cmd "find $REL_IHAVE/wsdef"
out-was <<EOF
$REL_IHAVE/wsdef
$REL_IHAVE/wsdef/com
$REL_IHAVE/wsdef/com/${EXAMPLENAME}bootstrapexample
$REL_IHAVE/wsdef/com/${EXAMPLENAME}bootstrapexample/wsdef
$REL_IHAVE/wsdef/com/${EXAMPLENAME}bootstrapexample/wsdef/Workspace.java
EOF
end-section
}

cd-to-iw() {
  log "Nothing to do for cd-to-iw"
}

phase1-run-for-successful-help() {
cmde "1 0" "$PHASE1 2>&1 | sed -n '/Please tell what/,+9 p'"
out-was <<EOF
     [java] Please tell what you want.
     [java] 
     [java] Ant usage:
     [java]   as-someone/with/ant/iw $ ant list-of-targets
     [java]   as-someone/with/ant/iw $ ant -D/target=TARGETNAME
     [java] Shell usage:
     [java]   as-someone/with/bash $ iwant/list-of/targets
     [java]   as-someone/with/bash $ iwant/target/TARGETNAME/as-path

BUILD FAILED
EOF
}

phase1-run-with-default-wsjava() {
section 'Using the new workspace'
p "Bootstrapping is now ready, let's run once more for help."
phase1-run-for-successful-help
p "Let's try first the ant cli."
cd-to-iw
cmd 'ant list-of-targets'
cmd 'ant -D/target=aConstant'
cmd "cat ../../bash/iwant/cached/$EXAMPLENAME-bootstrap-example/target/aConstant"
out-was <<EOF
Constant generated content
EOF
p "Then the bash cli."
cmd "cd ../../bash"
p "Let's see what wish scripts we have."
cmde "0 0 0" "find iwant/ -not -type d | grep -v '^iwant/cached' | sort"
out-was <<EOF
iwant/help.sh
iwant/list-of/targets
iwant/target/aConstant/as-path
iwant/target/eclipse-projects/as-path
EOF
cmd "iwant/list-of/targets"
out-was <<EOF
aConstant
eclipse-projects
EOF
p "We make a wish."
cmd "iwant/target/aConstant/as-path"
out-was <<EOF
$(readlink -f iwant/cached/$EXAMPLENAME-bootstrap-example/target/aConstant)
EOF
cmd 'cat $(iwant/target/aConstant/as-path)'
out-was <<EOF
Constant generated content
EOF
end-section
}

doc() {

section 'Boostrapping iwant with ant'
#------------------------------------

cmd 'mkdir -p example && cd example'
get-phase1
cmd 'cd with/ant/iw'

phase1-run-1
phase1-run-with-incorrect-iwant-from
phase1-run-with-correct-iwant-from
phase1-run-with-correct-ws-info
phase1-run-with-default-wsjava

end-section

}
