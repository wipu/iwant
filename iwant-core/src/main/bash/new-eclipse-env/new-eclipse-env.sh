#!/bin/bash

set -eu

die() {
  echo $@
  exit 1
}

downloaded-tool() {
  local VARNAME=$1
  local URL=$2
  local NAME=$3
  local MD5=$4
  local CACHEDIR=$5
  local CACHED=$CACHEDIR/$NAME
  [ -e "$CACHED" ] || {
    echo "Downloading from $URL"
    wget --no-check-certificate \
      "$URL" \
      -O "$CACHED" || {
        echo "Download failed, removing $CACHED"
        rm "$CACHED"
      }
  }
  echo "$MD5  $CACHED" | md5sum -c - || die "Broken file: $CACHED"
  # return by setting a var instead of echoing to stdout
  # this way the die above exits our caller
  # (command substitution creates a subshell
  # and exit only exits it, not the calling shell):
  eval "$VARNAME='$CACHED'"
}

NEESCRIPT=$(readlink -f "$0")
NEEHOME=$(dirname "$NEESCRIPT")
echo "NEEHOME=$NEEHOME"
NEEHOME=$(readlink -f "$NEEHOME")
CACHE=$NEEHOME/cache
[ -e "$CACHE" ] || {
  echo "Creating cache at $CACHE"
  mkdir -p "$CACHE"
}
NEETEMPLATES=$NEEHOME/templates

[ $# == 2 ] || die "Usage: $0 TARGETDIR linux32|linux64|win32"
TARGETDIR=$1
ARCH=$2

ECL_CODENAME=indigo
ECL_REL=SR1
ECL_DISTBASE=eclipse-java-$ECL_CODENAME-$ECL_REL
ECL_URLBASE='http://www.eclipse.org/downloads/download.php?file=/technology/epp/downloads/release/'$ECL_CODENAME/$ECL_REL

eclipse-url-linux32() {
  DISTNAME=$ECL_DISTBASE-linux-gtk.tar.gz
  DISTURL=$ECL_URLBASE/$DISTNAME'&r=1'
  DISTMD5='2a0af3038349efa7ddc9af00a4d66dda'
}

eclipse-url-linux64() {
  DISTNAME=$ECL_DISTBASE-linux-gtk-x86_64.tar.gz
  DISTURL=$ECL_URLBASE/$DISTNAME'&r=1'
  DISTMD5='71efb534eeae80644cd421e72a22e7e9'
}

eclipse-url-win32() {
  DISTNAME=$ECL_DISTBASE-win32.zip
  DISTURL=$ECL_URLBASE/$DISTNAME'&r=1'
  DISTMD5='7ef3ee6b2f0206888097c6b2713bb44c'
}

eclipse-dist() {
  eclipse-url-$ARCH
  downloaded-tool ECLIPSEDIST "$DISTURL" "$DISTNAME" "$DISTMD5" "$CACHE"
}

downloaded-tool SUBCLIPSEDIST 'http://subclipse.tigris.org/files/documents/906/47653/site-1.6.12.zip' subclipse-site-1.6.12.zip "77a844be91a76d39d1957e6eec1a6e2b" "$CACHE"

targetdir() {
  [ -e "$TARGETDIR" ] && die "Refusing to touch the existing TARGETDIR: $TARGETDIR"
  mkdir -p "$TARGETDIR"
}

uncompress-linux() {
  tar xzf "$ECLIPSEDIST"
}

uncompress-linux32() {
  uncompress-linux
}

uncompress-linux64() {
  uncompress-linux
}

uncompress-win32() {
  unzip "$ECLIPSEDIST"
}

pristine-eclipse() {
  eclipse-dist
  ECLIPSE=$TARGETDIR/eclipse
  cd "$TARGETDIR"
  uncompress-$ARCH
  cd - >/dev/null
}

workspace() {
  WORKSPACE=$TARGETDIR/workspace
  mkdir "$WORKSPACE"
  cp -a "$NEETEMPLATES/workspace/.metadata" "$WORKSPACE"/
}

select-workspace() {
  cp -a "$NEETEMPLATES/eclipse/configuration/.settings" "$ECLIPSE/configuration/"
  sed -i "s|NEE_RECENT_WORKSPACES|$WORKSPACE|" "$ECLIPSE/configuration/.settings/org.eclipse.ui.ide.prefs"
}

subclipse() {
  unzip "$SUBCLIPSEDIST" \
    features/com.collabnet.subversion.merge.feature_2.1.0.jar \
    features/org.tigris.subversion.clientadapter.feature_1.6.12.jar \
    features/org.tigris.subversion.clientadapter.javahl.feature_1.6.12.jar \
    features/org.tigris.subversion.subclipse.graph.feature_1.0.8.jar \
    features/org.tigris.subversion.subclipse_1.6.12.jar \
    plugins/com.collabnet.subversion.merge_2.1.0.jar \
    plugins/org.tigris.subversion.clientadapter.javahl_1.6.12.jar \
    plugins/org.tigris.subversion.clientadapter_1.6.12.jar \
    plugins/org.tigris.subversion.subclipse.core_1.6.12.jar \
    plugins/org.tigris.subversion.subclipse.core_1.6.8.jar \
    plugins/org.tigris.subversion.subclipse.doc_1.3.0.jar \
    plugins/org.tigris.subversion.subclipse.graph_1.0.8.jar \
    plugins/org.tigris.subversion.subclipse.ui_1.6.12.jar \
  -d "$ECLIPSE/"
}

custom-formatting() {
  cd "$TARGETDIR"
  patch -p0 < "$NEEHOME/patches/custom-formatting.diff"
}

m2-repo() {
  local M2REPO=$(readlink -f ~/.m2/repository)
  echo "org.eclipse.jdt.core.classpathVariable.M2_REPO=$M2REPO" >> "$WORKSPACE/.metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.jdt.core.prefs"
}

targetdir
pristine-eclipse
workspace
select-workspace
subclipse
#custom-formatting
#m2-repo
