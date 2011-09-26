BOOTSTRAPPING_WITH_BASH_PARENT=$(dirname "$1")
. "$BOOTSTRAPPING_WITH_BASH_PARENT/bootstrapping-with-ant.sh"

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

EXAMPLENAME=bash
PHASE1=iwant/help.sh
REL_AS_SOMEONE=.
REL_IHAVE=i-have

cd-to-iw() {
  cmd "cd iw"
}

doc() {

section 'Boostrapping iwant with bash'
#-------------------------------------

cmd 'mkdir -p example && cd example'
get-phase1

cmd 'cd as-example-developer'

phase1-run-1
phase1-run-with-incorrect-iwant-from
phase1-run-with-correct-iwant-from
phase1-run-with-correct-ws-info
phase1-run-with-default-wsjava

end-section

}
