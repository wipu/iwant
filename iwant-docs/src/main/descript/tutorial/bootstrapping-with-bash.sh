BOOTSTRAPPING_WITH_BASH_PARENT=$(dirname "$1")
. "$BOOTSTRAPPING_WITH_BASH_PARENT/bootstrapping-with-ant.sh"

EXAMPLENAME=bash
PHASE1=iwant/help.sh
REL_AS_SOMEONE=../..
REL_IHAVE=$REL_AS_SOMEONE/i-have
PHASE1XML=../ant/iw/build.xml

cd-to-iw() {
  cmd "cd ../ant/iw"
}

phase1-run-for-successful-help() {
cmde 1 'iwant/help.sh'
out-was <<EOF
Please tell what you want.

Ant usage:
  as-someone/with/ant/iw $ ant list-of-targets
  as-someone/with/ant/iw $ ant -D/target=TARGETNAME
Shell usage:
  as-someone/with/bash $ iwant/list-of/targets
  as-someone/with/bash $ iwant/target/TARGETNAME/as-path
EOF
}

cmd-phase1-filter-iwant-src-export() {
  # nothing to filter
  cmde 1 "$PHASE1"
}

doc() {

section 'Boostrapping iwant with bash'
#-------------------------------------

cmd 'mkdir -p example && cd example'
get-phase1

cmd 'cd with/bash'

phase1-run-1
phase1-run-with-incorrect-iwant-from
phase1-run-with-correct-iwant-from
phase1-run-with-correct-ws-info
phase1-run-with-default-wsjava

end-section

}
