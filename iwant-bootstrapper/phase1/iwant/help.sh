#!/bin/bash

AS_SOMEONE_IWANT=$(dirname "$0")
AS_SOMEONE_IWANT=$(readlink -f "$AS_SOMEONE_IWANT")
AS_SOMEONE=$(dirname "$AS_SOMEONE_IWANT")
cd "$AS_SOMEONE/iw"

iwant-messages-forwarded() {
  grep -o ':iwant:out:.*\|:iwant:err:.*' |
  while read LINE; do
    local PREF=${LINE:7:4}
    local MSG=${LINE:11}
    [ "out:" == "$PREF" ] && {
      echo "$MSG"
      continue
    }
    [ "err:" == "$PREF" ] && {
      echo "$MSG" >> /dev/stderr
      continue
    }
    echo "Internal error: Don't know where to output line $LINE" >> /dev/stderr
    exit 1
  done
}

ant 2>&1 -Diwant-print-prefix=:iwant: "$@" | iwant-messages-forwarded

exit "${PIPESTATUS[0]}"
