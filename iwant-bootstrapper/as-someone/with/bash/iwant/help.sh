#!/bin/bash

HERE=$(dirname "$0")
AS_SOMEONE=$HERE/../../..
cd "$AS_SOMEONE/with/ant/iw"

log() {
  echo "sh-$$ | $@" >> /tmp/iwant.log
}

logcolor() {
  # TODO why doesn't [ -t 1 ] && printf... work here?
  if [ -t 2 ]; then printf "\033[${1}m" >> /dev/stderr; fi
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
      logcolor 1
      logcolor 34
      echo "$MSG" >> /dev/stderr
      logcolor 0
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
