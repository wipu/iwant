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
    local SUM=$4
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
    echo "$SUM  $CACHED" | sha512sum -c - || die "Broken file: $CACHED"
    # return by setting a var instead of echoing to stdout
    # this way the die above exits our caller
    # (command substitution creates a subshell
    # and exit only exits it, not the calling shell):
    eval "$VARNAME='$CACHED'"
}

native-path-ascii() {
    local IN=$1
    local N=$(native-path "$IN")
    to-ascii "$N"
}

# JDK dropped native2ascii so we have to use jq for
# converting special chars (like scandinavian letters) to
# the \uXXXX format
to-ascii() {
    local IN=$1
    to-json-array "$IN" |
	jq -ac |
	sed 's/^\["//' | sed 's/"]$//'
}

to-json-array() {
    local IN=$1
    echo '["'$IN'"]'
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

OPT_SUBCLIPSE=false
OPT_EGIT=false

if [ $# -lt 3 ]; then
    log "Usage: $0 TARGETDIR linux64|win64 2022-03|2020-12|2020-06|2019-12|2019-09|2019-06 [OPTS...]"
    log "Supported OPTS:"
    log "  --egit      : enable git plugin (disabled by default)"
    log "  --subclipse : enable svn plugin (disabled by default)"
    die ""
fi
TARGETDIR=$1
shift
ARCH=$1
shift
VERSION=$1
shift

log "Arguments: TARGETDIR=$TARGETDIR, ARCH=$ARCH, VERSION=$VERSION"

while [ $# -gt 0 ]; do
    OPT=$1
    shift
    case "$OPT" in
	"--egit")
	    log "OPT --egit requested"
	    OPT_EGIT=true
	    ;;
	"--subclipse")
	    log "OPT --subclipse requested"
	    OPT_SUBCLIPSE=true
	    ;;
	*) die "Unsupported OPT: $OPT"
    esac
done

log "Requested options:"
log "OPT_SUBCLIPSE=$OPT_SUBCLIPSE"
log "OPT_EGIT=$OPT_EGIT"


ECL_REL=R
ECL_DISTBASE=eclipse-java-$VERSION-$ECL_REL
ECL_URLBASE='https://www.eclipse.org/downloads/download.php?file=/technology/epp/downloads/release/'$VERSION/$ECL_REL

eclipse-url-linux64() {
    DISTNAME=$ECL_DISTBASE-linux-gtk-x86_64.tar.gz
    DISTURL=$ECL_URLBASE/$DISTNAME'&r=1'
}

eclipse-sum-linux64-2022-03() {
    DISTSUM='e903ad34560246175289c944c228c1729d843839e5a7caad3b9e8d24bb91760c2911efb7039ebb2ffbec65efc4ef661319938587dc89efe5de848123a277d423'
}

eclipse-sum-linux64-2020-12() {
    DISTSUM='1d5aac59454d35175c6b388674d23de91f45d953141389d82b5557d46de92beb2b14396427bfab8f6b198ed58bed67094755d26d3d379c92733f2a74d51b02bd'
}

eclipse-sum-linux64-2020-06() {
    DISTSUM='2b471ba974f0632e2e8198c77a4a85cd126bbc1c2e9313e388997820181b39124cb4443c8aac8af0bbb7973bcb2e55303b566f91f58f35e1bf9eb3ea8f5289ec'
}

eclipse-sum-linux64-2019-12() {
    DISTSUM='358d1c6c6900d3cfcf9d89a32228695206902297a7f74c440981660c7e4db810fae22e721e78b4d7df84b3bf951f91b1ba87dcd1fe9c7b00235b87f8633f4883'
}

eclipse-sum-linux64-2019-09() {
    DISTSUM='eb408902f079d6666863bc318a0586589be9a86e4cd57125ef1f97eb4f4a9d6b70aa52ea23129f5f95eb513c3ce1889683516d91e85a484fcae7328fa8e1eeff'
}

eclipse-sum-linux64-2019-06() {
    DISTSUM='2d3cfbd888b32d5022780932ea6c0999878bb5828cbbb952ef6d911e0c544977365af8631fb71c5e1dee71c3f364d02f04f15f903eb2daccebdc087c4a058d81'
}

eclipse-url-win64() {
    DISTNAME=$ECL_DISTBASE-win32-x86_64.zip
    DISTURL=$ECL_URLBASE/$DISTNAME'&r=1'
}

eclipse-sum-win64-2022-03() {
    DISTSUM='607b6f92973ff25cdbd136636687c3ca2958bc6b382bdf138373cf693e577dca0db85f92788515e71249a957dec28d8118b208d16d563332473e21b63869c669'
}

eclipse-sum-win64-2020-06() {
    DISTSUM='6dbb1f4472dc720ddb17363635970dccd446fba7a104a8dcbcd5c632108b52c1026b61ccbc945f855f06e6668e6267b09b524730ee4536db20c12b2e376bcd6b'
}

eclipse-sum-win64-2019-12() {
    DISTSUM='bbd6e53a8bf4f732f4eb1d0da608b88a0508772d5b4da14376dd4e0e92dcdbe5c0dee65e4c744b89dbe4e9793fe334dd1572c0f2f400b2ad5c48090c9a9cd0ba'
}

eclipse-sum-win64-2019-09() {
    DISTSUM='3c96f0fc1e9f3f0a4468753242d38360347a709ba42f6ed836cce8c90ba33c9641bb3d1fb9eee6e936e49d53107c74d1b9dc71b1aa623f11c793e3bc5dd3a4bc'
}

eclipse-sum-win64-2019-06() {
    DISTSUM='de3b75dd62241b76b0dd6db2c4beb4def77881331f9d5ef91cd8285c1a8512071a2d55c38e393640c199af7efc0bd9f4c414fc64cc3091ad2c2d39120ba2c651'
}

eclipse-dist() {
    eclipse-url-$ARCH
    eclipse-sum-$ARCH-$VERSION
    downloaded-tool ECLIPSEDIST "$DISTURL" "$DISTNAME" "$DISTSUM" "$CACHE"
}

"$OPT_SUBCLIPSE" && downloaded-tool SUBCLIPSEDIST 'http://subclipse.tigris.org/files/documents/906/49336/site-1.10.2.zip' subclipse-site-1.10.2.zip "690f45551c1d5f9827c3080221dbb294" "$CACHE"

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

uncompress-linux64() {
    uncompress-linux
}

uncompress-win64() {
    log "Unzipping eclipse"
    unzip -q "$ECLIPSEDIST"
    log "Fixing file permissions"
    find eclipse -type f -exec chmod u+x '{}' ';'
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
RECENT_WORKSPACES=$(native-path-ascii "$WORKSPACE")
EOF
}

subclipse() {
    log "Unzipping subclipse"
    unzip -q "$SUBCLIPSEDIST" \
	  "features/*.jar" \
	  "plugins/*.jar" \
	  -d "$ECLIPSE/"
}

# currently egit works incorrectly with submodules so:
disable-egit() {
    log "Disabling egit plugin"
    local DIR=$ECLIPSE/plugins
    rm -vf "$DIR"/org.eclipse.egit*
    rm -vf "$DIR"/org.eclipse.mylyn.git*
}

custom-formatting() {
    log "Configuring formatter"
    cd "$TARGETDIR"
    patch -p0 < "$NEEHOME/patches/custom-formatting.diff"
}

# eclipse already does this automatically
addvar-m2-repo() {
    local M2_REPO=$(readlink -f ~/.m2/repository)
    add-classpath-var M2_REPO "$M2_REPO"
}

addvar-user-home() {
    add-classpath-var USER_HOME "$HOME"
}

add-classpath-var() {
    local KEY=$1
    local VALUE=$2
    log "Adding classpath variable $KEY=$VALUE"
    echo "org.eclipse.jdt.core.classpathVariable.$KEY=$VALUE" >> "$WORKSPACE/.metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.jdt.core.prefs"
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
    cat <<\EOF
content_assist_proposals_background=255,255,255
content_assist_proposals_foreground=0,0,0
eclipse.preferences.version=1
fontPropagated=true
org.eclipse.jdt.ui.editor.tab.width=
org.eclipse.jdt.ui.formatterprofiles.version=12
org.eclipse.jdt.ui.javadoclocations.migrated=true
org.eclipse.jdt.ui.text.code_templates_migrated=true
org.eclipse.jdt.ui.text.custom_code_templates=<?xml version\="1.0" encoding\="UTF-8" standalone\="no"?><templates><template autoinsert\="false" context\="catchblock_context" deleted\="false" description\="Code in new catch blocks" enabled\="true" id\="org.eclipse.jdt.ui.text.codetemplates.catchblock" name\="catchblock">\tint todoProperExceptionTypeOrHandleOtherwise;\n\tthrow new UnsupportedOperationException("TODO proper exception type or handle otherwise", e);\n</template><template autoinsert\="false" context\="methodbody_context" deleted\="false" description\="Code in created method stubs" enabled\="true" id\="org.eclipse.jdt.ui.text.codetemplates.methodbody" name\="methodbody">int todoTestAndImplement;\nthrow new UnsupportedOperationException("TODO test and implement");</template></templates>
org.eclipse.jdt.ui.text.custom_templates=<?xml version\="1.0" encoding\="UTF-8" standalone\="no"?><templates><template autoinsert\="false" context\="java-statements" deleted\="false" description\="try catch block" enabled\="true" id\="org.eclipse.jdt.ui.templates.try" name\="try">try {\n\t${line_selection}${cursor}\n} catch (${Exception} ${exception_variable_name}) {\n\tint todoProperExceptionTypeOrHandleOtherwise;\n\tthrow new UnsupportedOperationException("TODO proper exception type or handle otherwise", e);\n}</template></templates>
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
AbstractTextEditor.Color.Background=252,252,231
AbstractTextEditor.Color.Background.SystemDefault=false
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
[ "true" == "$OPT_SUBCLIPSE" ] && subclipse
[ "true" == "$OPT_EGIT" ] || disable-egit
addvar-user-home
#custom-formatting
#m2-repo
