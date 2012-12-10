#!/bin/bash

set -eu

log() {
  echo "$@" >>/dev/stderr
}

die() {
  log $@
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
    log "Downloading from $URL"
    wget --no-check-certificate \
      "$URL" \
      -O "$CACHED" || {
        echo "Download failed, removing $CACHED"
        rm "$CACHED"
      }
  }
  log "Cached download is at $CACHED"
  echo "$MD5  $CACHED" | md5sum -c - || die "Broken file: $CACHED"
  # return by setting a var instead of echoing to stdout
  # this way the die above exits our caller
  # (command substitution creates a subshell
  # and exit only exits it, not the calling shell):
  eval "$VARNAME='$CACHED'"
}

NEESCRIPT=$(readlink -f "$0")
NEEHOME=$(dirname "$NEESCRIPT")
NEEHOME=$(readlink -f "$NEEHOME")
log "NEEHOME=$NEEHOME"
CACHE=$NEEHOME/cache
[ -e "$CACHE" ] || {
  log "Creating cache at $CACHE"
  mkdir -p "$CACHE"
}

[ $# == 2 ] || die "Usage: $0 TARGETDIR linux32|linux64|win32"
TARGETDIR=$1
ARCH=$2

ECL_CODENAME=juno
ECL_REL=SR1
ECL_DISTBASE=eclipse-java-$ECL_CODENAME-$ECL_REL
ECL_URLBASE='http://www.eclipse.org/downloads/download.php?file=/technology/epp/downloads/release/'$ECL_CODENAME/$ECL_REL

eclipse-url-linux32() {
  DISTNAME=$ECL_DISTBASE-linux-gtk.tar.gz
  DISTURL=$ECL_URLBASE/$DISTNAME'&r=1'
  DISTMD5='6047f8c34745016e3329e6248e487e4f'
}

eclipse-url-linux64() {
  DISTNAME=$ECL_DISTBASE-linux-gtk-x86_64.tar.gz
  DISTURL=$ECL_URLBASE/$DISTNAME'&r=1'
  DISTMD5='8f9d32687f2350042f6c754b11f81e7f'
}

eclipse-url-win32() {
  DISTNAME=$ECL_DISTBASE-win32.zip
  DISTURL=$ECL_URLBASE/$DISTNAME'&r=1'
  DISTMD5='b2057e0aaab5be19205c399d740ca85a'
}

eclipse-dist() {
  eclipse-url-$ARCH
  downloaded-tool ECLIPSEDIST "$DISTURL" "$DISTNAME" "$DISTMD5" "$CACHE"
}

downloaded-tool SUBCLIPSEDIST 'http://subclipse.tigris.org/files/documents/906/47653/site-1.6.12.zip' subclipse-site-1.6.12.zip "77a844be91a76d39d1957e6eec1a6e2b" "$CACHE"

targetdir() {
  [ -e "$TARGETDIR" ] && die "Refusing to touch the existing TARGETDIR: $TARGETDIR"
  log "Creating TARGETDIR:"
  mkdir -p "$TARGETDIR"
  # we cannot get canonical path until it exists:
  TARGETDIR=$(readlink -f "$TARGETDIR")
  log "$TARGETDIR"
}

uncompress-linux() {
  log "Untarring eclipse"
  tar xzf "$ECLIPSEDIST"
}

uncompress-linux32() {
  uncompress-linux
}

uncompress-linux64() {
  uncompress-linux
}

uncompress-win32() {
  log "Unzipping eclipse"
  unzip "$ECLIPSEDIST"
  log "Fixing file permissions"
  find eclipse -type f | xargs chmod u+x
}

pristine-eclipse() {
  eclipse-dist
  ECLIPSE=$TARGETDIR/eclipse
  cd "$TARGETDIR"
  uncompress-$ARCH
  cd - >/dev/null
}

select-workspace() {
  log "Selecting workspace"
  local ECL_CONFS="$ECLIPSE/configuration/.settings"
  mkdir -p "$ECL_CONFS"
  org.eclipse.ui.ide.prefs > "$ECL_CONFS/org.eclipse.ui.ide.prefs"
}

org.eclipse.ui.ide.prefs() {
cat <<EOF
#Wed Jul 28 10:43:46 EEST 2010
RECENT_WORKSPACES_PROTOCOL=3
MAX_RECENT_WORKSPACES=5
SHOW_WORKSPACE_SELECTION_DIALOG=false
eclipse.preferences.version=1
RECENT_WORKSPACES=$WORKSPACE
EOF
}

subclipse() {
  log "Unzipping subclipse"
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
  log "Configuring formatter"
  cd "$TARGETDIR"
  patch -p0 < "$NEEHOME/patches/custom-formatting.diff"
}

m2-repo() {
  log "Configuring M2REPO variable"
  local M2REPO=$(readlink -f ~/.m2/repository)
  echo "org.eclipse.jdt.core.classpathVariable.M2_REPO=$M2REPO" >> "$WORKSPACE/.metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.jdt.core.prefs"
}

workspace() {
  log "Configuring workspace"
  WORKSPACE=$TARGETDIR/workspace
  mkdir "$WORKSPACE"
  runtime-settings-file org.eclipse.core.resources.prefs
  runtime-settings-file org.eclipse.jdt.ui.prefs
  runtime-settings-file org.eclipse.ui.editors.prefs
}

runtime-settings-file() {
  local FILE=$1
  local DESTDIR=$WORKSPACE/.metadata/.plugins/org.eclipse.core.runtime/.settings
  mkdir -p "$DESTDIR"
  log "Generating $FILE"
  "conf_$FILE" > "$DESTDIR/$FILE"
}

conf_org.eclipse.jdt.ui.prefs() {
cat <<EOF
content_assist_proposals_background=255,255,255
content_assist_proposals_foreground=0,0,0
eclipse.preferences.version=1
fontPropagated=true
org.eclipse.jdt.ui.editor.tab.width=
org.eclipse.jdt.ui.formatterprofiles.version=12
org.eclipse.jdt.ui.javadoclocations.migrated=true
org.eclipse.jface.textfont=1|Monospace|10.0|0|GTK|1|;
proposalOrderMigrated=true
spelling_locale_initialized=true
tabWidthPropagated=true
useAnnotationsPrefPage=true
useQuickDiffPrefPage=true
escapeStrings=true

java_bracket=127,127,127
java_string=0,204,0
semanticHighlighting.abstractMethodInvocation.color=77,77,77
semanticHighlighting.abstractMethodInvocation.enabled=true
semanticHighlighting.localVariable.color=30,144,255
semanticHighlighting.localVariable.enabled=true
semanticHighlighting.localVariableDeclaration.color=30,144,255
semanticHighlighting.localVariableDeclaration.enabled=true
semanticHighlighting.methodDeclarationName.enabled=true
semanticHighlighting.number.color=255,165,0
semanticHighlighting.number.enabled=true
semanticHighlighting.parameterVariable.color=139,105,20
semanticHighlighting.parameterVariable.enabled=true
semanticHighlighting.staticFinalField.bold=true
semanticHighlighting.staticFinalField.color=0,0,192
semanticHighlighting.staticFinalField.enabled=true
semanticHighlighting.typeArgument.enabled=true
semanticHighlighting.typeArgument.underline=true
semanticHighlighting.typeParameter.color=46,114,51
semanticHighlighting.typeParameter.enabled=true
EOF
}

conf_org.eclipse.ui.editors.prefs() {
cat <<EOF
eclipse.preferences.version=1
lineNumberRuler=true
overviewRuler_migration=migrated_3.1
EOF
}

conf_org.eclipse.core.resources.prefs() {
cat <<EOF
eclipse.preferences.version=1
encoding=UTF-8
version=1
EOF
}

targetdir
pristine-eclipse
workspace
select-workspace
subclipse
#custom-formatting
#m2-repo
