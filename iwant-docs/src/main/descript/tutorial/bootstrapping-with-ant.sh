end-section() {
  debuglog "TODO Really, define end-section in descript."
}

LOCAL_IWANT_ROOT=$(readlink -f "$LOCAL_IWANT/../..")

copy-phase1() {
cmd "svn export \"$LOCAL_IWANT_ROOT/iwant-bootstrapper/phase1\" as-example-developer"
out-was <<EOF
Export complete.
EOF
}

get-phase1() {
  debuglog "TODO check LOCAL_IWANT"
  copy-phase1
}

antcmd() {
  cmd 'ant | head -n -1'
}

EXAMPLENAME=ant
PHASE1=ant
REL_AS_SOMEONE=..
REL_IHAVE=$REL_AS_SOMEONE/i-have
PHASE1XML=build.xml

phase1-run-1() {
section "First run to create iwant-from.conf"
p "The first run creates us a file in which we can specify the iwant revision to use."
cmd "find $REL_AS_SOMEONE"
out-was <<EOF
$REL_AS_SOMEONE
$REL_AS_SOMEONE/iw
$REL_AS_SOMEONE/iw/build.xml
$REL_AS_SOMEONE/iwant
$REL_AS_SOMEONE/iwant/help.sh
EOF
cmde 1 "$PHASE1"
cmd "find $REL_AS_SOMEONE"
out-was <<EOF
$REL_AS_SOMEONE
$REL_AS_SOMEONE/iw
$REL_AS_SOMEONE/iw/build.xml
$REL_AS_SOMEONE/iwant
$REL_AS_SOMEONE/iwant/help.sh
$REL_AS_SOMEONE/i-have
$REL_AS_SOMEONE/i-have/iwant-from.conf
EOF
cmd "cat $REL_AS_SOMEONE/i-have/iwant-from.conf"
end-section
}

phase1-run-with-incorrect-iwant-from() {
section "Test handling of incorrect iwant-from.conf"
p "The bootstrapper complains if iwant-rev is not specified."
edit "$REL_IHAVE/iwant-from.conf" empty-file <<EOF
EOF
cmde 1 "$PHASE1"
p "It also complains about missing iwant-url."
edit "$REL_IHAVE/iwant-from.conf" only-rev <<EOF
iwant-rev=
EOF
cmde 1 "$PHASE1"
p "No further side-effects until we fix the issue:"
cmd find $REL_AS_SOMEONE
out-was <<EOF
$REL_AS_SOMEONE
$REL_AS_SOMEONE/iw
$REL_AS_SOMEONE/iw/build.xml
$REL_AS_SOMEONE/iwant
$REL_AS_SOMEONE/iwant/help.sh
$REL_AS_SOMEONE/i-have
$REL_AS_SOMEONE/i-have/iwant-from.conf
EOF
end-section
}

optimize-downloads() {
  p "Using cached external libraries to optimize building this article."
  local OPTIMCACHE=$LOCAL_IWANT_ROOT/iwant-iwant/iwant/cached/iwant/optimization
  local SVNKITZIP=org.tmatesoft.svn_1.3.5.standalone.nojna.zip
  local INTERNALCACHE=$REL_AS_SOMEONE/iwant/cached/.internal/unmodifiable
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
  cp -v "$LOCAL_IWANT/cached/iwant/cpitems/"*.jar "$INTERNALCACHE"/
}

phase1-run-with-correct-iwant-from() {
section "We'll use a local copy of iwant."

edit "$REL_IHAVE/iwant-from.conf" use-local-iwant <<EOF
iwant-rev=
iwant-url=$LOCAL_IWANT_ROOT
EOF
optimize-downloads
cmde 1 "$PHASE1"
cmd find $REL_IHAVE
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
cmde "1 0 0" "$PHASE1 2>&1 | head -n -4 | tail -n 7"
out-was <<EOF
     [java] Try one of these:
     [java]   ant list-of-targets
     [java]   ant -D/target=TARGETNAME
     [java]     (use tab or ls/dir -D to see valid targets)
     [java] 

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
cmd "cat ../iwant/cached/$EXAMPLENAME-bootstrap-example/target/aConstant"
out-was <<EOF
Constant generated content
EOF
p "Then the bash cli."
cmd "cd .."
p "TODO generate the wish scripts:"
cmde 127 "iwant/list-of/targets"
p "Abusing internals:"
cmd "iwant/help.sh -D/target=aConstant"
out-was <<EOF
$(readlink -f iwant/cached/$EXAMPLENAME-bootstrap-example/target/aConstant)
EOF
cmd 'cat $(iwant/help.sh -D/target=aConstant)'
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
cmd 'cd as-example-developer/iw'

phase1-run-1
phase1-run-with-incorrect-iwant-from
phase1-run-with-correct-iwant-from
phase1-run-with-correct-ws-info
phase1-run-with-default-wsjava

end-section

}
