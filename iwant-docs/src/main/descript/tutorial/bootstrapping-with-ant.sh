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
p "Bootstrapper complains if iwant-rev is not specified."
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
cmd 'cat ../i-have/ws-info.conf'
out-was <<EOF
# paths are relative to this file's directory
WSNAME=example
WSROOT=../..
WSDEF_SRC=../i-have/wsdef
WSDEF_CLASS=com.example.wsdef.Workspace
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

end-section

}
