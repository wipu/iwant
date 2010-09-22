#!/bin/bash

set -eu

HELP_SH=$0
WISHROOT=$(dirname "$HELP_SH")
HERE=$(dirname "$WISHROOT")

cached() {
  CACHED=$WISHROOT/cached
  [ -e "$CACHED" ] || mkdir "$CACHED"
}

i-have() {
  IHAVE=$HERE/i-have
  [ -e "$IHAVE" ] || mkdir "$IHAVE"
}

iwant-from-conf() {
  i-have
  IWANT_FROM_CONF=$IHAVE/iwant-from.conf
  [ -e "$IWANT_FROM_CONF" ] || guide-iwant-from-conf
}

guide-iwant-from-conf() {
cat > "$IWANT_FROM_CONF" <<EOF
# Uncomment and modify one of these:
#svn-revision 99
#local-svn-workingcopy ~/svn/sf.net/iwant
#local-iwant-wishdir ~/svn/sf.net/iwant/iwant-iwant/iwant
EOF
cat <<EOF
Welcome.

Please start by specify what version of iwant you wish to use.
I created file $IWANT_FROM_CONF for you.
Modify it and rerun $HELP_SH
EOF
exit
}

iwant() {
  cached
  IWANT=$CACHED/iwant
  [ -e "$IWANT" ] || fetch-iwant
}

fetch-iwant() {
  . "$IWANT_FROM_CONF"
}

local-svn-workingcopy() {
  local FROM=$1
  local IWANT_SRC=$CACHED/iwant-from-local-svn-workingcopy
  svn export "$FROM" "$IWANT_SRC" >/dev/null
  "$IWANT_SRC/iwant-iwant/iwant/as_shell-user/to-bootstrap-iwant.sh"
  local-iwant-cache "$IWANT_SRC/iwant-iwant/iwant/cached"
}

local-iwant-wishdir() {
  local FROM=$1
  cp -a "$FROM/as-iwant-user" "$WISHROOT"/
  iwant-cached "$FROM/cached/iwant/scripts" "$FROM/cached/iwant/cpitems"
}

svn-revision() {
  local REV=$1
  local IWANT_SRC=$CACHED/iwant-r$REV
  svn export -r "$REV" "https://iwant.svn.sourceforge.net/svnroot/iwant/trunk" "$IWANT_SRC"
  "$IWANT_SRC/iwant-iwant/iwant/as_shell-user/to-bootstrap-iwant.sh"
  local-iwant-wishdir "$IWANT_SRC/iwant-iwant/iwant"
}

iwant-cached() {
  cached
  mkdir "$IWANT"
  cp -a "$@" "$IWANT"/
}

ws-info-conf() {
  WS_INFO_CONF=$IHAVE/ws-info.conf
  [ -e "$WS_INFO_CONF" ] || guide-ws-info-conf
}

guide-ws-info-conf() {
cat > "$WS_INFO_CONF" <<EOF
# paths are relative to this file's directory
WSNAME=example
WSROOT=../..
WSDEF_SRC=../i-have/wsdef
WSDEF_CLASS=com.example.wsdef.Workspace
EOF
cat <<EOF
Next, modify $WS_INFO_CONF to define your workspace.
After that, rerun $HELP_SH
EOF
exit
}

ws-info() {
  . "$WS_INFO_CONF"
  WSDEF_JAVA=$WISHROOT/$WSDEF_SRC/$(fqcn-as-javafile "$WSDEF_CLASS")
}

fqcn-as-javafile() {
  local FQCN=$1
  fqcn-as-fileprefix "$FQCN" | sed 's/$/.java/'
}

fqcn-as-fileprefix() {
  echo $FQCN | dots-to-slashes
}

slashes-to-dots() {
  sed 's:/:.:g'
}

dots-to-slashes() {
  sed 's:\.:/:g'
}

fqcn-as-packagedir() {
  local FQCN=$1
  local AS_FILE=$(fqcn-as-fileprefix "$FQCN")
  echo $(dirname "$AS_FILE")
}

wsdef() {
  ws-info
  [ -e "$WSDEF_JAVA" ] || wsdef-stub
}

wsdef-stub() {
local WSDEF_JAVA_DIR=$(dirname "$WSDEF_JAVA")
mkdir -p "$WSDEF_JAVA_DIR"
wsdef-stub-java > "$WSDEF_JAVA"
cat <<EOF
I created a stub workspace definition at $WSDEF_JAVA
EOF
}

wsdef-stub-java() {
local PACKAGE=$(fqcn-as-packagedir "$WSDEF_CLASS" | slashes-to-dots)
local CLASS=$(echo $WSDEF_CLASS | dots-to-slashes)
CLASS=$(basename "$CLASS")
cat <<EOF
package $PACKAGE;

import net.sf.iwant.core.Constant;
import net.sf.iwant.core.ContainerPath;
import net.sf.iwant.core.EclipseProject;
import net.sf.iwant.core.EclipseProjects;
import net.sf.iwant.core.Locations;
import net.sf.iwant.core.RootPath;
import net.sf.iwant.core.Target;
import net.sf.iwant.core.WorkspaceDefinition;

public class $CLASS implements WorkspaceDefinition {

    public ContainerPath wsRoot(Locations locations) {
        return new Root(locations);
    }

    public static class Root extends RootPath {

        public Root(Locations locations) {
            super(locations);
        }

        public Target aConstant() {
            return target("aConstant").
                content(Constant.value("Constant generated content\n")).end();
        }

        public Target eclipseProjects() {
            return target("eclipse-projects").
                content(EclipseProjects.with().
                    project(wsdefEclipseProject())).end();
        }

        public EclipseProject wsdefEclipseProject() {
            return EclipseProject.with().name("as-$WSNAME-developer").
                src(source("as-$WSNAME-developer/i-have/wsdef")).libs(builtin().all()).end();
        }

    }

}
EOF
}

use-iwant-on-ws() {
  # TODO use the same variable names in the sourced file:
  here=$HERE
  iwant=$WISHROOT
  . "$IWANT/scripts/iwant-functions.sh"
  use-iwant-on "$WSNAME" "$WSROOT" "$WSDEF_SRC" "$WSDEF_CLASS"
}

iwant-from-conf
iwant
ws-info-conf
ws-info
wsdef
use-iwant-on-ws

cat <<EOF
Use find or code completion (tab) to see what you can iwant/
Have fun.
EOF
