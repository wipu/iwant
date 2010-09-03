#!/bin/bash

set -eu

here=$(dirname "$0")
iwant=$("$here/iwant-path.sh")
wsroot=$iwant/../..

. "$wsroot/iwant-core/src/main/bash/bootstrap-functions.sh"

bootstrapped-iwant

targetscript() {
  cp "$wsroot/iwant-core/src/main/bash/$1" "$as_iwant_user/$2"
}

mkdir -p "$as_iwant_user"
targetscript to-use-iwant-on.sh to-use-iwant-on.sh
targetscript iwant-path-for-targetscripts.sh iwant-path.sh

# some hard-coded iwant development scripts
# TODO move these to the wsdef and create only if
# explicitly told to use iwant on iwant
# -----------------------------------------------------------------

as_iwant_developer="$iwant/as-iwant-developer"
mkdir -p "$as_iwant_developer"

function script() {
    local FILE="$as_iwant_developer/$1"
    mkdir -p $(dirname "$FILE")
    cat > "$FILE"
    chmod u+x "$FILE"
}

function abs() {
        echo $(cd "$1" && pwd)
}

NGREASE="$wsroot/../ngrease"
TUTORIAL_SRC="$wsroot/iwant-docs/src/main/descript/tutorial"

function tutorial() {
local TO="$1"
local LOCAL_IWANT="$2"
local TUTORIAL_BUILD="$3"
script "$TO" <<EOF
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

tutorial target/tutorial/as-path "" "$cache/tutorial"
tutorial target/local-tutorial/as-path "$iwant" "$cache/local-tutorial" 

function website() {
local TO="$1"
local WEBSITE_BUILD="$2"
local CACHEDIR="$3"
local TUTORIAL_NAME="$4"
script "$TO" <<EOF
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

website "target/website/as-path" "$cache/website" "$cache" "tutorial" 
website "target/local-website/as-path" "$cache/local-website" "$cache" "local-tutorial" 

script "command-to/deploy-website" <<EOF
echo "# Assuming the website target is uptodate (TODO should be!), pipe this to a shell:"
echo rsync -e ssh --delete-delay -vrucli "$cache/website/" wipu_@shell.sourceforge.net:iwant-htdocs/
EOF

script "command-to/tag-deployed-website" <<\EOF
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

# -----------------------------------------------------------------

echo To use iwant, just start your sentences with iwant/$(basename "$as_iwant_user")/
echo You can find your options with e.g.
echo \$ find iwant/$(basename "$as_iwant_user")/
