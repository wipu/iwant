#!/bin/bash

set -eu

AS_SOMEONE_IWANT=$(dirname "$0")
AS_SOMEONE=$(dirname "$AS_SOMEONE_IWANT")
cd "$AS_SOMEONE/iw"

ant "$@"
