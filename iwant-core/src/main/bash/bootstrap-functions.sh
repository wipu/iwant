# to be sourced

. "$wsroot/iwant-core/src/main/bash/iwant-functions.sh"

# variables that need to be defined before sourcing:
wsroot=$(abs "$wsroot")

testarea="$cache/testarea"

cached-script() {
  cp "$wsroot/iwant-core/src/main/bash/$1" "$scriptcache/$2"
}

cached-scripts() {
  mkdir -p "$scriptcache"
  cached-script iwant-functions.sh iwant-functions.sh
  cached-script bootstrap-functions.sh bootstrap-functions.sh
}

remote-file() {
  local FROM="$1"
  local FILE="$2"
  local TO="$3"
  local MD5="$4"
  local TOFILE="$TO/$FILE"
  if [ ! -e "$TOFILE" ]; then
    wget "$FROM/$FILE" -O "$TOFILE"
  fi
  echo "$MD5 *$TOFILE" | md5sum -c
}

remote-files() {
  remote-file \
    http://mirrors.ibiblio.org/pub/mirrors/maven2/junit/junit/3.8.1 \
    junit-3.8.1.jar \
    "$wsroot/iwant-lib-junit-3.8.1" \
    "1f40fb782a4f2cf78f161d32670f7a3a"

  remote-file \
    http://mirrors.ibiblio.org/pub/mirrors/maven2/org/apache/ant/ant/1.7.1 \
    ant-1.7.1.jar \
    "$wsroot/iwant-lib-ant-1.7.1" \
    "ef62988c744551fb51f330eaa311bfc0"

  remote-file \
    http://mirrors.ibiblio.org/pub/mirrors/maven2/org/apache/ant/ant-junit/1.7.1 \
    ant-junit-1.7.1.jar \
    "$wsroot/iwant-lib-ant-1.7.1" \
    "c1b2bfa2389c405c7c07d23f368d6944"
}

projsrc() {
  echo "$wsroot/$1/src/main/java:$wsroot/$1/src/test/java"
}

bootstrap-cpitems() {
  mkdir -p "$classescache"
  mkdir -p "$testarea/iwanttestarea"
  cp "$wsroot/iwant-lib-ant-1.7.1/"*.jar "$cpitemscache/"
  cp "$wsroot/iwant-lib-junit-3.8.1/"*.jar "$cpitemscache/"

  javac \
	-sourcepath \
		"$(projsrc iwant-core):$(projsrc iwant-iwant)" \
        -cp "$wsroot/iwant-lib-junit-3.8.1/junit-3.8.1.jar:$wsroot/iwant-lib-ant-1.7.1/ant-1.7.1.jar:$wsroot/iwant-lib-ant-1.7.1/ant-junit-1.7.1.jar" \
	-d "$classescache" \
	"$wsroot/iwant-iwant/src/main/java/net/sf/iwant/iwant/IwantWorkspace.java" \
	"$wsroot/iwant-core/src/test/java/net/sf/iwant/core/Suite.java"
}

bootstrap-testrun() {
  java -cp "$testarea:$classescache:$wsroot/iwant-lib-junit-3.8.1/junit-3.8.1.jar:$wsroot/iwant-lib-ant-1.7.1/ant-1.7.1.jar:$wsroot/iwant-lib-ant-1.7.1/ant-junit-1.7.1.jar" \
    junit.textui.TestRunner -c net.sf.iwant.core.Suite
}

targetscript() {
  cp "$wsroot/iwant-core/src/main/bash/$1" "$as_iwant_user/$2"
}

as-iwant-user-targetscripts() {
  mkdir -p "$as_iwant_user"
  targetscript to-use-iwant-on.sh to-use-iwant-on.sh
  as-iwant-user-to-develop-iwant-targetscript
}

as-iwant-user-to-develop-iwant-targetscript() {
script "$as_iwant_user/to-develop-iwant.sh" <<\EOF
#!/bin/bash -eu
here=$(dirname "$0")
iwant=$here/..
wsroot=$iwant/../..
. "$here/../cached/iwant/scripts/bootstrap-functions.sh"
to-develop-iwant-targetdir
EOF
}

bootstrapped-iwant() {
  cached-scripts
  remote-files
  bootstrap-cpitems
  bootstrap-testrun
  as-iwant-user-targetscripts
  echo To use iwant, just start your sentences with iwant/$(basename "$as_iwant_user")/
  echo You can find your options with e.g.
  echo \$ find iwant/$(basename "$as_iwant_user")/
}

developer-script() {
  script "$as_iwant_developer/$1"
}

script() {
  local FILE="$1"
  local DIR=$(dirname "$FILE")
  mkdir -p "$DIR"
  cat > "$FILE"
  chmod u+x "$FILE"
}

as_iwant_developer="$iwant/as-iwant-developer"

NGREASE="$wsroot/../ngrease"
TUTORIAL_SRC="$wsroot/iwant-docs/src/main/descript/tutorial"

tutorial-targetscript() {
local TO="$1"
local LOCAL_IWANT="$2"
local TUTORIAL_BUILD="$3"
developer-script "$TO" <<EOF
#!/bin/bash
set -eu
mkdir -p $(dirname "$TUTORIAL_BUILD")
rm -rf "$TUTORIAL_BUILD"
LOCAL_IWANT="$LOCAL_IWANT" bash "$NGREASE/ngrease-descript/src/main/bash/descript.sh" \\
        "$TUTORIAL_SRC" "$TUTORIAL_BUILD"
NGREASEPATH="${TUTORIAL_BUILD}:$wsroot/iwant-docs/src/main/java:$NGREASE/ngrease-descript/src/main/java" \\
        "$NGREASE/ngrease-release/target/ngrease-all-0.4.0pre/bin/ngrease" \\
                -r "/net/sf/ngrease/descript/descripted-as-html-source.ngr" \\
                > $TUTORIAL_BUILD/tutorial.html
echo $TUTORIAL_BUILD/tutorial.html
EOF
}

website-targetscript() {
local TO="$1"
local WEBSITE_BUILD="$2"
local CACHEDIR="$3"
local TUTORIAL_NAME="$4"
developer-script "$TO" <<EOF
#!/bin/bash
set -eu
$as_iwant_developer/target/$TUTORIAL_NAME/as-path >/dev/null
mkdir -p "$WEBSITE_BUILD"
rm -rf "$WEBSITE_BUILD/*"
cp $wsroot/iwant-docs/src/main/html/website/* "$WEBSITE_BUILD/"
cp "$CACHEDIR/$TUTORIAL_NAME/tutorial.html" "$WEBSITE_BUILD/"
echo "$WEBSITE_BUILD"
EOF
}

deploy-website-commandscript() {
developer-script "command-to/deploy-website" <<EOF
echo "# Assuming the website target is uptodate (TODO should be!), pipe this to a shell:"
echo rsync -e ssh --delete-delay -vrucli "$cache/website/" wipu_@shell.sourceforge.net:iwant-htdocs/
EOF
}

tag-deployed-website-commandscript() {
developer-script "command-to/tag-deployed-website" <<\EOF
#!/bin/bash
if [ $# != 2 ]; then
  echo "Usage: $0 REV TIME"
  echo "e.g. $0 555 2009-03-16"
  exit 1
fi

REV=$1
TIME=$2

TAG="${TIME}-website-update"
SVNBASE=https://iwant.svn.sourceforge.net/svnroot/iwant

echo "# Assuming the website target is up to date, pipe this a shell:"
echo svn cp -r $REV "$SVNBASE/trunk" "$SVNBASE/tags/$TAG" -m \""Tagged $TAG"\"
EOF
}

to-develop-iwant-targetdir() {
  mkdir -p "$as_iwant_developer"
  tutorial-targetscript target/tutorial/as-path "" "$cache/tutorial"
  tutorial-targetscript target/local-tutorial/as-path "$iwant" "$cache/local-tutorial" 
  website-targetscript "target/website/as-path" "$cache/website" "$cache" "tutorial" 
  website-targetscript "target/local-website/as-path" "$cache/local-website" "$cache" "local-tutorial" 
  deploy-website-commandscript
  tag-deployed-website-commandscript
  echo To develop iwant, just start your sentences with iwant/$(basename "$as_iwant_developer")/
  echo You can find your options with e.g.
  echo \$ find iwant/$(basename "$as_iwant_developer")/
}
