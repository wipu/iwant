#!/bin/bash
set -eu

# Here we use our own newest and shiniest local version of ourselves
# to build

HERE=$(dirname "$0")
cd "$HERE"/../../..
AS_ID=$(pwd)
cd ..
CLONE=$(pwd)

TMP=/tmp/iwant-for-iwant
rebuild-tmp() {
    echo "Rebuilding $TMP" >&2
    set -x
    rm -rf "$TMP"
    mkdir -p "$TMP"
    cp -a "$CLONE" "$TMP"/all
    cd "$TMP"/all
    find . -name '*.class' | xargs -r rm
    rm -rf as-iwant-developer/.i-cached
    rm -rf essential/iwant-core/src/main/bash/new-eclipse-env/cache
    cd ..
    zip -rq all.zip all
    rm -rf all
}
rebuild-tmp

fetch() {
    local RELPATH=$1
    echo "Fetching $RELPATH" >&2
    local PARENT=$(dirname "$RELPATH")
    mkdir -p "$AS_ID/$PARENT"
    cp -av "$CLONE/essential/iwant-entry/as-some-developer/$RELPATH" "$AS_ID/$PARENT"/
}

set -x
fetch with/ant/iw/build.xml
fetch with/bash/iwant/help.sh
chmod u+x "$AS_ID"/with/bash/iwant/help.sh
fetch with/java/org/fluentjava/iwant/entry/Iwant.java

# TODO simplify these cache invalidations:

rm -rf $HOME/.org.fluentjava.iwant/cached/UnmodifiableUrl/file%3A/tmp/iwant-for-iwant/

rm -rf $HOME/.org.fluentjava.iwant/cached/UnmodifiableZip/file%3A$HOME/.org.fluentjava.iwant/cached/UnmodifiableUrl/file%25253A/tmp/iwant-for-iwant

rm -rf $HOME/.org.fluentjava.iwant/cached/UnmodifiableIwantBootstrapperClassesFromIwantWsRoot/file%3A/$HOME/.org.fluentjava.iwant/cached/UnmodifiableZip/file%25253A$HOME/.org.fluentjava.iwant/cached/UnmodifiableUrl/file%252525253A/tmp/iwant-for-iwant/

HOME_ENC=$(echo $HOME | sed 's|^/||')

rm -rf $HOME/.org.fluentjava.iwant/cached/CombinedSrcFromUnmodifiableIwantEssential/%2F$HOME_ENC/.org.fluentjava.iwant/cached/UnmodifiableZip/file%253A$HOME/.org.fluentjava.iwant/cached/UnmodifiableUrl/file%2525253A/tmp/iwant-for-iwant/

rm -rf $HOME/.org.fluentjava.iwant/cached/ClassesFromUnmodifiableIwantEssential/%2F$HOME_ENC/.org.fluentjava.iwant/cached/UnmodifiableZip/file%253A$HOME/.org.fluentjava.iwant/cached/UnmodifiableUrl/file%2525253A/tmp/iwant-for-iwant/

echo "Done" >&2
