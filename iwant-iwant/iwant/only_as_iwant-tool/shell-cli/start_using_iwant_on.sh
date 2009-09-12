#/bin/bash

if [ $# != 1 ]; then
    echo "Usage: $0 ROOTDEF"
    echo "Where ROOTDEF points to the root definition java file."
    exit 1
fi

ROOTDEF="$1"

if [ ! -e "$ROOTDEF" ];then
    echo "Workspace definition file does not exist: $WSDEF"
    exit 1
fi

function abs() {
	echo $(cd "$1" && pwd)
}

MYDIR=$(dirname "$0")
MYDIR=$(abs "$MYDIR")
IWANT=$(dirname "$MYDIR")
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

WEBSITE_BUILD="$TARGET/once/website"
TUTORIAL_BUILD="$TARGET/once/tutorial"
TUTORIAL_HTML="$TUTORIAL_BUILD/tutorial.html"
NGREASE=$(abs ../../../svn/trunk)

TUTORIAL_SRC=$(abs ../iwant-docs/src/main/descript/tutorial)

script path-to-fresh/tutorial <<EOF
#!/bin/bash
mkdir -p $(dirname "$TUTORIAL_BUILD")
bash "$NGREASE/ngrease-descript/src/main/bash/descript.sh" \
	"$TUTORIAL_SRC" "$TUTORIAL_BUILD"
NGREASEPATH="${TUTORIAL_BUILD}:../iwant-docs/src/main/java:$NGREASE/ngrease-descript/src/main/java" \
	"$NGREASE/ngrease-release/target/ngrease-all-r569/bin/ngrease" \
		-r "/net/sf/ngrease/descript/descripted-as-html-source.ngr" \
		> $TUTORIAL_HTML
EOF

script "path-to-fresh/website" <<EOF
#!/bin/bash
mkdir -p "$WEBSITE_BUILD"
cp $IWANT/../../iwant-docs/src/main/html/website/* "$WEBSITE_BUILD/"
echo "$WEBSITE_BUILD"
EOF

script "command-to/deploy-website" <<EOF
echo "# Assuming the website target is uptodate (TODO should be!), pipe this to a shell:"
echo rsync -e ssh --delete-delay -vrucli "$WEBSITE_BUILD/" wipu_@shell.sourceforge.net:iwant-htdocs/
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
