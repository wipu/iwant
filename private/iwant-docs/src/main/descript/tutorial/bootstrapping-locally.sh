doc-content() {

cmd "mkdir iwant-tutorial"
cmd "cd iwant-tutorial"

local WSROOTNAME=$(basename "$LOCAL_IWANT_WSROOT")
local WSROOTPARENT=$(dirname "$LOCAL_IWANT_WSROOT")
local IWANTZIP=/tmp/iwant-for-local-tutorial.zip
cmd cd "$WSROOTPARENT"
cmd "rm '$IWANTZIP'"
cmd "zip -q -0 -r '$IWANTZIP' $WSROOTNAME"
cmd "cd - > /dev/null"

p 'First we remove the cached "unmodifiable" iwant sources so the tutorial will use the latest local iwant files.'

cmd "rm -rf $HOME/.org.fluentjava.iwant/cached/UnmodifiableUrl/file%3A$IWANTZIP"
cmd "rm -rf $HOME/.org.fluentjava.iwant/cached/UnmodifiableZip/file%3A$HOME/.org.fluentjava.iwant/cached/UnmodifiableUrl/file%25253A$IWANTZIP"

cmd "cp -a '$LOCAL_IWANT_WSROOT/essential/iwant-entry/as-some-developer' as-iwant-tutorial-developer"

cmd 'find . -type f'

section "Choosing url for iwant to use as engine"

cmde 1 'as-iwant-tutorial-developer/with/bash/iwant/help.sh'
edit as-iwant-tutorial-developer/i-have/conf/iwant-from "local-iwant-from" <<EOF
iwant-from=file://$IWANTZIP
EOF
cmde "1" "as-iwant-tutorial-developer/with/bash/iwant/help.sh 2>&1"

}
