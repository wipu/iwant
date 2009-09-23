#/bin/bash

if [ $# != 1 ]; then
    echo "Usage: $0 WSDEFDIR"
    echo "Where WSDEFDIR points to the workspace definition source directory."
    exit 1
fi

WSDEFDIR="$1"
WSDEFJAVA="$WSDEFDIR/Workspace.java"

if [ ! -r "$WSDEFJAVA" ];then
    echo "Please describe the workspace in file $WSDEFJAVA"
    exit 1
fi

function abs() {
	echo $(cd "$1" && pwd)
}

MYDIR=$(dirname "$0")
MYDIR=$(abs "$MYDIR")
IWANT=$(dirname "$MYDIR")
WSDEFCLASSES="$IWANT/only_as_iwant-tool/tmp"

rm -rf "$WSDEFCLASSES"
mkdir -p "$WSDEFCLASSES"
javac -d "$WSDEFCLASSES" "$WSDEFJAVA" || exit 1
java -cp "$WSDEFCLASSES" Workspace || exit 1

WSNAME=iwant
TARGET=$IWANT/as_${WSNAME}-developer

# TODO incremental
rm -rf "$TARGET"
mkdir "$TARGET"
mkdir "$TARGET/path-to-fresh"
mkdir "$TARGET/command-to"

function script() {
    local FILE="$TARGET/$1"
    cat > "$FILE"
    chmod u+x "$FILE"
}

NGREASE=$(abs ../../../svn/trunk)

TUTORIAL_SRC=$(abs ../iwant-docs/src/main/descript/tutorial)

function tutorial() {
local TO="$1"
local LOCAL_IWANT="$2"
local TUTORIAL_BUILD="$3"
script "$TO" <<EOF
#!/bin/bash
mkdir -p $(dirname "$TUTORIAL_BUILD")
LOCAL_IWANT="$LOCAL_IWANT" bash "$NGREASE/ngrease-descript/src/main/bash/descript.sh" \
	"$TUTORIAL_SRC" "$TUTORIAL_BUILD"
NGREASEPATH="${TUTORIAL_BUILD}:../iwant-docs/src/main/java:$NGREASE/ngrease-descript/src/main/java" \
	"$NGREASE/ngrease-release/target/ngrease-all-r569/bin/ngrease" \
		-r "/net/sf/ngrease/descript/descripted-as-html-source.ngr" \
		> $TUTORIAL_BUILD/tutorial.html
echo $TUTORIAL_BUILD/tutorial.html
EOF
}

tutorial path-to-fresh/tutorial "" "$TARGET/once/tutorial"
tutorial path-to-fresh/local-tutorial "$IWANT" "$TARGET/once/local-tutorial" 

function website() {
local TO="$1"
local WEBSITE_BUILD="$2"
local ONCEDIR="$3"
local TUTORIAL_NAME="$4"
script "$TO" <<EOF
#!/bin/bash
iwant/as_iwant-developer/path-to-fresh/$TUTORIAL_NAME
mkdir -p "$WEBSITE_BUILD"
cp $IWANT/../../iwant-docs/src/main/html/website/* "$WEBSITE_BUILD/"
cp "$ONCEDIR/$TUTORIAL_NAME/tutorial.html" "$WEBSITE_BUILD/"
echo "$WEBSITE_BUILD"
EOF
}

website "path-to-fresh/website" "$TARGET/once/website" "$TARGET/once" "tutorial" 
website "path-to-fresh/local-website" "$TARGET/once/local-website" "$TARGET/once" "local-tutorial" 

script "command-to/deploy-website" <<EOF
echo "# Assuming the website target is uptodate (TODO should be!), pipe this to a shell:"
echo rsync -e ssh --delete-delay -vrucli "$TARGET/once/website/" wipu_@shell.sourceforge.net:iwant-htdocs/
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

echo "To get access to targets of the $WSNAME project, start your sentences with"
echo "\$ $TARGET"
