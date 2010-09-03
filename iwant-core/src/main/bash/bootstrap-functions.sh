# to be sourced

. "$wsroot/iwant-core/src/main/bash/iwant-functions.sh"

testarea="$cache/testarea"

cached-script() {
  cp "$wsroot/iwant-core/src/main/bash/$1" "$scriptcache/$2"
}

cached-scripts() {
  mkdir -p "$scriptcache"
  cached-script createscript.sh createscript.sh
  cached-script iwant-path-for-cached-scripts.sh iwant-path.sh
  cached-script javac.sh javac.sh
  cached-script create-target-scripts.sh create-target-scripts.sh
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

as-iwant-user-scripts() {
  mkdir -p "$as_iwant_user"
  targetscript to-use-iwant-on.sh to-use-iwant-on.sh
  targetscript iwant-path-for-targetscripts.sh iwant-path.sh
}

bootstrapped-iwant() {
  cached-scripts
  remote-files
  bootstrap-cpitems
  bootstrap-testrun
}
