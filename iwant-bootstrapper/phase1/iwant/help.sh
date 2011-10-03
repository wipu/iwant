#!/bin/bash

AS_SOMEONE_IWANT=$(dirname "$0")
AS_SOMEONE_IWANT=$(readlink -f "$AS_SOMEONE_IWANT")
AS_SOMEONE=$(dirname "$AS_SOMEONE_IWANT")
cd "$AS_SOMEONE/iw"

iwant-messages-forwarded() {
  grep -o ':iwant:out:.*\|:iwant:err:.*' |
  awk -F : '/^:iwant:out:/{print $4} /^:iwant:err:/ {print $4 > "/dev/stderr"}'
}

ant 2>&1 -Diwant-print-prefix=:iwant: "$@" | iwant-messages-forwarded

exit "${PIPESTATUS[0]}"
