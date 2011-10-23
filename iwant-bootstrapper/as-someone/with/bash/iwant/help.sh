#!/bin/bash

HERE=$(dirname "$0")
AS_SOMEONE=$HERE/../../..
cd "$AS_SOMEONE/with/ant/iw"

log() {
  echo "sh-$$ | $@" >> /tmp/iwant.log
}

iwant-messages-forwarded() {
  grep -o ':iwant:out:.*\|:iwant:err:.*' |
  while read LINE; do
    local PREF=${LINE:7:4}
    local MSG=${LINE:11}
    [ "out:" == "$PREF" ] && {
      log "to stdout: $MSG"
      echo "$MSG" >> /dev/stdout
      continue
    }
    [ "err:" == "$PREF" ] && {
      echo "$MSG" >> /dev/stderr
      continue
    }
    local ERRORMSG="Internal error: Don't know where to output line $LINE"
    log "$ERRORMSG"
    echo "$ERRORMSG" >> /dev/stderr
    exit 1
  done
}

log "Starting: $0 $@"

ant 2>&1 -Diwant-print-prefix=:iwant: "$@" | iwant-messages-forwarded
EXITSTATUS=${PIPESTATUS[0]}

log "Exiting: $0 -> $EXITSTATUS"
exit "$EXITSTATUS"
