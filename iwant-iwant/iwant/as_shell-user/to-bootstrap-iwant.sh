#!/bin/bash

set -eu

here=$(dirname "$0")
iwant=$("$here/iwant-path.sh")
wsroot=$iwant/../..

. "$wsroot/iwant-core/src/main/bash/bootstrap-functions.sh"

bootstrapped-iwant
