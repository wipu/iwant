#!/bin/bash

set -eu

here=$(dirname "$0")
iwant=$here/..
wsroot=../..

. "$iwant/$wsroot/iwant-core/src/main/bash/bootstrap-functions.sh"

bootstrapped-iwant
