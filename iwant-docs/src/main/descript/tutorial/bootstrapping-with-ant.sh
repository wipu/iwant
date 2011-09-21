end-section() {
  debuglog "TODO Really, define end-section in descript."
}

LOCAL_IWANT_ROOT=$(readlink -f "$LOCAL_IWANT/../..")

copy-phase1-xml() {
cmd 'echo $LOCAL_IWANT'
cmd "svn export \"$LOCAL_IWANT_ROOT/iwant-bootstrapper/phase1/iw\""
out-was <<EOF
Export complete.
EOF
}

svn-export-phase1-xml() {
  die "TODO implement"
}

get-phase1-xml() {
  debuglog "TODO check LOCAL_IWANT"
  copy-phase1-xml
}

antcmd() {
  cmd 'ant | head -n -1'
}

phase1-run-1() {
section "First run to create iwant-from.conf"
p "The first run creates us a file in which we can specify the iwant revision to use."
cmd find ..
out-was <<EOF
..
../iw
../iw/build.xml
EOF
failing-cmd 1 ant
cmd find ..
out-was <<EOF
..
../iw
../iw/build.xml
../i-have
../i-have/iwant-from.conf
EOF
cmd 'cat ../i-have/iwant-from.conf'
end-section
}

phase1-run-with-incorrect-iwant-from() {
section "Test handling of incorrect iwant-from.conf"
p "The bootstrapper complains if iwant-rev is not specified."
edit "../i-have/iwant-from.conf" empty-file <<EOF
EOF
failing-cmd 1 ant
p "It also complains about missing iwant-url."
edit "../i-have/iwant-from.conf" only-rev <<EOF
iwant-rev=
EOF
failing-cmd 1 ant
p "No further side-effects until we fix the issue:"
cmd find ..
out-was <<EOF
..
../iw
../iw/build.xml
../i-have
../i-have/iwant-from.conf
EOF
end-section
}

optimize-downloads() {
  p "Using cached external libraries to optimize building this article."
  local OPTIMCACHE=$LOCAL_IWANT_ROOT/iwant-iwant/iwant/cached/iwant/optimization
  local SVNKITZIP=org.tmatesoft.svn_1.3.5.standalone.nojna.zip
  local INTERNALCACHE=../iwant/cached/.internal/unmodifiable
  [ -e "$OPTIMCACHE/$SVNKITZIP" ] || {
    log "Fetching svnkit using the ant script to test."
    ant svnkit.zip
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

edit "../i-have/iwant-from.conf" use-local-iwant <<EOF
iwant-rev=
iwant-url=$LOCAL_IWANT_ROOT
EOF
optimize-downloads
failing-cmd 1 ant
cmd 'find ../i-have'
end-section
}

phase1-run-with-correct-ws-info() {
section "Generate the workspace definition java file"
p "Let's modify the file the iwant generated for us."
edit '../i-have/ws-info.conf' creation <<EOF
# paths are relative to this file's directory
WSNAME=ant-bootstrap-example
WSROOT=../..
WSDEF_SRC=wsdef
WSDEF_CLASS=com.antbootstrapexample.wsdef.Workspace
EOF
p "Now iwant will generate the Workspace definition."
failing-cmd 1 ant
cmd 'find ../i-have/wsdef'
out-was <<EOF
../i-have/wsdef
../i-have/wsdef/com
../i-have/wsdef/com/antbootstrapexample
../i-have/wsdef/com/antbootstrapexample/wsdef
../i-have/wsdef/com/antbootstrapexample/wsdef/Workspace.java
EOF
end-section
}

phase1-run-with-default-wsjava() {
section 'Using the new workspace'
p "Bootstrapping is now ready, let's run once more for help."
cmd 'ant 2>&1 | head -n -4 | tail -n 7'
out-was <<EOF
     [java] Try one of these:
     [java]   ant list-of-targets
     [java]   ant -D/target=TARGETNAME
     [java]     (use tab or ls/dir -D to see valid targets)
     [java] 

BUILD FAILED
EOF
p "Let's try it."
cmd 'ant list-of-targets'
cmd 'ant -D/target=aConstant'
cmd 'cat ../iwant/cached/ant-bootstrap-example/target/aConstant'
out-was <<EOF
Constant generated content
EOF
end-section
}

doc() {

section 'Boostrapping iwant with ant'
#------------------------------------

cmd 'mkdir -p example/as-example-developer && cd example/as-example-developer'
get-phase1-xml

cmd 'cd iw'

phase1-run-1
phase1-run-with-incorrect-iwant-from
phase1-run-with-correct-iwant-from
phase1-run-with-correct-ws-info
phase1-run-with-default-wsjava

end-section

}
