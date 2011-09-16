# to be sourced

. "$iwant/$wsroot/iwant-core/src/main/bash/iwant-functions.sh"
set -eu

testarea="$cached/testarea"

cached-script() {
  cp "$iwant/$wsroot/iwant-core/src/main/bash/$1" "$iwant/$scriptcache/$2"
}

cached-scripts() {
  mkdir -p "$iwant/$scriptcache"
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
    "$iwant/$wsroot/iwant-lib-junit-3.8.1" \
    "1f40fb782a4f2cf78f161d32670f7a3a"

  remote-file \
    http://mirrors.ibiblio.org/pub/mirrors/maven2/org/apache/ant/ant/1.7.1 \
    ant-1.7.1.jar \
    "$iwant/$wsroot/iwant-lib-ant-1.7.1" \
    "ef62988c744551fb51f330eaa311bfc0"

  remote-file \
    http://mirrors.ibiblio.org/pub/mirrors/maven2/org/apache/ant/ant-junit/1.7.1 \
    ant-junit-1.7.1.jar \
    "$iwant/$wsroot/iwant-lib-ant-1.7.1" \
    "c1b2bfa2389c405c7c07d23f368d6944"
}

projsrc() {
  echo "$iwant/$wsroot/$1/src/main/java:$iwant/$wsroot/$1/src/test/java"
}

bootstrap-cpitems() {
  mkdir -p "$iwant/$classescache"
  mkdir -p "$iwant/$testarea/iwanttestarea"
  cp "$iwant/$wsroot/iwant-lib-ant-1.7.1/"*.jar "$iwant/$cpitemscache/"
  cp "$iwant/$wsroot/iwant-lib-junit-3.8.1/"*.jar "$iwant/$cpitemscache/"

  javac \
	-sourcepath \
		"$(projsrc iwant-core):$(projsrc iwant-iwant)" \
        -cp "$iwant/$wsroot/iwant-lib-junit-3.8.1/junit-3.8.1.jar:$iwant/$wsroot/iwant-lib-ant-1.7.1/ant-1.7.1.jar:$iwant/$wsroot/iwant-lib-ant-1.7.1/ant-junit-1.7.1.jar" \
	-d "$iwant/$classescache" \
	"$iwant/$wsroot/iwant-iwant/src/main/java/net/sf/iwant/iwant/IwantWorkspace.java" \
	"$iwant/$wsroot/iwant-core/src/test/java/net/sf/iwant/core/IwantSuite.java"
}

bootstrap-testrun() {
  # 2-phase tests need em here:
  mkdir -p "$iwant/$testarea/iwanttestarea/iwant"
  cp -a "$iwant/$cpitemscache" "$iwant/$testarea/iwanttestarea/iwant/"

  java -cp "$iwant/$testarea:$iwant/$classescache:$iwant/$wsroot/iwant-lib-junit-3.8.1/junit-3.8.1.jar:$iwant/$wsroot/iwant-lib-ant-1.7.1/ant-1.7.1.jar:$iwant/$wsroot/iwant-lib-ant-1.7.1/ant-junit-1.7.1.jar" \
    junit.textui.TestRunner -c net.sf.iwant.core.IwantSuite
}

targetscript() {
  cp "$iwant/$wsroot/iwant-core/src/main/bash/$1" "$iwant/$as_iwant_user/$2"
}

as-iwant-user-targetscripts() {
  mkdir -p "$iwant/$as_iwant_user"
  targetscript to-use-iwant-on.sh to-use-iwant-on.sh
  as-iwant-user-to-develop-iwant-targetscript
}

as-iwant-user-to-develop-iwant-targetscript() {
script "$iwant/$as_iwant_user/to-develop-iwant.sh" <<\EOF
#!/bin/bash -eu
here=$(dirname "$0")
iwant=$here/..
wsroot=../..
. "$iwant/cached/iwant/scripts/bootstrap-functions.sh"
to-develop-iwant-targetdir
EOF
}

bootstrapped-iwant() {
  cached-scripts
  remote-files
  bootstrap-cpitems
  bootstrap-testrun
  as-iwant-user-targetscripts
  echo To use iwant, just start your sentences with iwant/$(basename "$iwant/$as_iwant_user")/
  echo You can find your options with e.g.
  echo \$ find iwant/$(basename "$iwant/$as_iwant_user")/
}

developer-script() {
  script "$iwant/$as_iwant_developer/$1"
}

script() {
  local FILE="$1"
  local DIR=$(dirname "$FILE")
  mkdir -p "$DIR"
  cat > "$FILE"
  chmod u+x "$FILE"
}

as_iwant_developer="as-iwant-developer"

TUTORIAL_SRC="$wsroot/iwant-docs/src/main/descript/tutorial"

tutorial-targetscript() {
local TO=$1
local LOCAL_IWANT=$2
# descript needs absolute path here, since it's going to cd:
[ -n "$LOCAL_IWANT" ] && LOCAL_IWANT=$(readlink -f "$LOCAL_IWANT")
local TUTORIAL_DIR=$3
local TUTORIAL=$TUTORIAL_DIR/tutorial.html
developer-script "$TO" <<EOF
#!/bin/bash
set -eu
here=\$(dirname "\$0")
iwant=\$here/../../..
iwant=\$(readlink -f "\$iwant")
rm -rf "\$iwant/$TUTORIAL_DIR"
mkdir -p "\$iwant/$TUTORIAL_DIR"
LOCAL_IWANT="$LOCAL_IWANT" bash "\$iwant/$wsroot/iwant-lib-descript/descript.sh" \\
        "\$iwant/$TUTORIAL_SRC/article.sh" "\$iwant/$TUTORIAL" true
echo \$iwant/$TUTORIAL
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
here=\$(dirname "\$0")
iwant=\$here/../../..
\$iwant/$as_iwant_developer/target/$TUTORIAL_NAME/as-path >/dev/null
mkdir -p "\$iwant/$WEBSITE_BUILD"
rm -rf "\$iwant/$WEBSITE_BUILD/"*
cp \$iwant/$wsroot/iwant-docs/src/main/html/website/* "\$iwant/$WEBSITE_BUILD/"
cp "\$iwant/$CACHEDIR/$TUTORIAL_NAME/tutorial.html" "\$iwant/$WEBSITE_BUILD/"
echo "\$iwant/$WEBSITE_BUILD"
EOF
}

deploy-website-commandscript() {
developer-script "command-to/deploy-website" <<EOF
echo "# Assuming the website target is uptodate (TODO should be!), pipe this to a shell:"
echo rsync -e ssh --delete-delay -vrucli "$iwant/$cached/website/" wipu_@shell.sourceforge.net:iwant-htdocs/
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
  mkdir -p "$iwant/$as_iwant_developer"
  tutorial-targetscript target/tutorial/as-path "" "$cached/tutorial"
  tutorial-targetscript target/local-tutorial/as-path "$iwant" "$cached/local-tutorial" 
  website-targetscript "target/website/as-path" "$cached/website" "$cached" "tutorial" 
  website-targetscript "target/local-website/as-path" "$cached/local-website" "$cached" "local-tutorial" 
  deploy-website-commandscript
  tag-deployed-website-commandscript
  echo To develop iwant, just start your sentences with iwant/$(basename "$iwant/$as_iwant_developer")/
  echo You can find your options with e.g.
  echo \$ find iwant/$(basename "$iwant/$as_iwant_developer")/
}
