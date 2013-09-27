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

native-path() {
  local IN=$1
  case "$(uname)" in
    CYGWIN*) cygpath -a -m "$IN" ;;
    *) echo "$IN" ;;
  esac
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

[ $# == 2 ] || die "Usage: $0 TARGETDIR linux32|linux64|win32|win64"
TARGETDIR=$1
ARCH=$2

ECL_CODENAME=kepler
ECL_REL=R
ECL_DISTBASE=eclipse-java-$ECL_CODENAME-$ECL_REL
ECL_URLBASE='http://www.eclipse.org/downloads/download.php?file=/technology/epp/downloads/release/'$ECL_CODENAME/$ECL_REL

eclipse-url-linux32() {
  DISTNAME=$ECL_DISTBASE-linux-gtk.tar.gz
  DISTURL=$ECL_URLBASE/$DISTNAME'&r=1'
  DISTMD5='87d80f6ca8800cee92d159d0d8c4c5db'
}

eclipse-url-linux64() {
  DISTNAME=$ECL_DISTBASE-linux-gtk-x86_64.tar.gz
  DISTURL=$ECL_URLBASE/$DISTNAME'&r=1'
  DISTMD5='280a63f83f1df0932bec656c5e2ac595'
}

eclipse-url-win32() {
  DISTNAME=$ECL_DISTBASE-win32.zip
  DISTURL=$ECL_URLBASE/$DISTNAME'&r=1'
  DISTMD5='51d69f035a770a8514c4dee2cbea1c1e'
}

eclipse-url-win64() {
  DISTNAME=$ECL_DISTBASE-win32-x86_64.zip
  DISTURL=$ECL_URLBASE/$DISTNAME'&r=1'
  DISTMD5='39283f577b5f17c99e5f8a806520ac0a'
}

eclipse-dist() {
  eclipse-url-$ARCH
  downloaded-tool ECLIPSEDIST "$DISTURL" "$DISTNAME" "$DISTMD5" "$CACHE"
}

downloaded-tool SUBCLIPSEDIST 'http://subclipse.tigris.org/files/documents/906/49336/site-1.10.2.zip' subclipse-site-1.10.2.zip "690f45551c1d5f9827c3080221dbb294" "$CACHE"

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
  unzip -q "$ECLIPSEDIST"
  log "Fixing file permissions"
  find eclipse -type f -exec chmod u+x '{}' ';'
}

uncompress-win64() {
  uncompress-win32
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
RECENT_WORKSPACES=$(native-path "$WORKSPACE")
EOF
}

subclipse() {
  log "Unzipping subclipse"
  unzip -q "$SUBCLIPSEDIST" \
    "features/*.jar" \
    "plugins/*.jar" \
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
  runtime-settings-file org.eclipse.debug.ui.prefs
  runtime-settings-file org.eclipse.jdt.core.prefs
  runtime-settings-file org.eclipse.jdt.ui.prefs
  runtime-settings-file org.eclipse.ui.editors.prefs
  runtime-settings-file org.eclipse.e4.ui.css.swt.theme.prefs
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
org.eclipse.jdt.ui.text.code_templates_migrated=true
org.eclipse.jdt.ui.text.custom_code_templates=<?xml version\="1.0" encoding\="UTF-8" standalone\="no"?><templates><template autoinsert\="false" context\="methodbody_context" deleted\="false" description\="Code in created method stubs" enabled\="true" id\="org.eclipse.jdt.ui.text.codetemplates.methodbody" name\="methodbody">int todoTestAndImplement;\nthrow new UnsupportedOperationException("TODO test and implement");</template></templates>
org.eclipse.jdt.ui.text.custom_templates=<?xml version\="1.0" encoding\="UTF-8" standalone\="no"?><templates/>
org.eclipse.jdt.ui.text.templates_migrated=true
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
semanticHighlighting.annotationElementReference.color=77,77,77
semanticHighlighting.annotationElementReference.enabled=true
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
semanticHighlighting.staticFinalField.italic=true
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

conf_org.eclipse.jdt.core.prefs() {
cat <<EOF
eclipse.preferences.version=1
org.eclipse.jdt.core.compiler.problem.emptyStatement=warning
org.eclipse.jdt.core.compiler.problem.fallthroughCase=warning
org.eclipse.jdt.core.compiler.problem.missingDefaultCase=warning
org.eclipse.jdt.core.compiler.problem.missingDeprecatedAnnotation=warning
org.eclipse.jdt.core.compiler.problem.missingHashCodeMethod=warning
org.eclipse.jdt.core.compiler.problem.missingOverrideAnnotation=warning
org.eclipse.jdt.core.compiler.problem.missingSerialVersion=ignore
org.eclipse.jdt.core.compiler.problem.missingSynchronizedOnInheritedMethod=warning
org.eclipse.jdt.core.compiler.problem.parameterAssignment=warning
org.eclipse.jdt.core.compiler.problem.possibleAccidentalBooleanAssignment=warning
org.eclipse.jdt.core.compiler.problem.potentialNullReference=warning
org.eclipse.jdt.core.compiler.problem.potentiallyUnclosedCloseable=ignore
org.eclipse.jdt.core.compiler.problem.redundantNullCheck=warning
org.eclipse.jdt.core.compiler.problem.redundantSuperinterface=warning
org.eclipse.jdt.core.compiler.problem.reportMethodCanBeStatic=warning
org.eclipse.jdt.core.compiler.problem.unclosedCloseable=warning
org.eclipse.jdt.core.compiler.problem.undocumentedEmptyBlock=warning
org.eclipse.jdt.core.compiler.problem.unnecessaryTypeCheck=warning
org.eclipse.jdt.core.compiler.problem.unusedDeclaredThrownException=warning
org.eclipse.jdt.core.compiler.problem.unusedDeclaredThrownExceptionExemptExceptionAndThrowable=disabled
org.eclipse.jdt.core.compiler.problem.unusedDeclaredThrownExceptionIncludeDocCommentReference=disabled
org.eclipse.jdt.core.compiler.problem.unusedObjectAllocation=warning
org.eclipse.jdt.core.compiler.problem.unusedParameter=warning
org.eclipse.jdt.core.compiler.problem.unusedParameterIncludeDocCommentReference=disabled
org.eclipse.jdt.core.compiler.problem.unusedTypeParameter=warning
org.eclipse.jdt.core.timeoutForParameterNameFromAttachedJavadoc=50
EOF
}

conf_org.eclipse.core.resources.prefs() {
cat <<EOF
eclipse.preferences.version=1
encoding=UTF-8
version=1
EOF
}

# the Console settings increase the output buffer size
# the launch-related settings select "Launch previously launched" instead of the open one
conf_org.eclipse.debug.ui.prefs() {
cat <<EOF
eclipse.preferences.version=1
Console.highWaterMark=1008000
Console.lowWaterMark=1000000
org.eclipse.debug.ui.PREF_LAUNCH_PERSPECTIVES=<?xml version\="1.0" encoding\="UTF-8" standalone\="no"?>\n<launchPerspectives/>\n
org.eclipse.debug.ui.UseContextualLaunch=false
EOF
}

# shamelessly opinionated: the new default theme is horrible
conf_org.eclipse.e4.ui.css.swt.theme.prefs() {
cat <<EOF
eclipse.preferences.version=1
themeid=org.eclipse.e4.ui.css.theme.e4_classic
EOF
}

targetdir
pristine-eclipse
workspace
select-workspace
subclipse
#custom-formatting
#m2-repo
