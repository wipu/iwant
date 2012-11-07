#!/bin/bash

set -eu

HERE=$(dirname "$0")
HERE=$(readlink -f "$HERE")

cd "$HERE"

fresh-dir() {
  DIR=$1
  rm -rf "$DIR"
  mkdir -p "$DIR"
}

die() {
  echo "$@"
  exit 1
}

CACHED=$HERE/cached

tested-java-classes() {
  JUNIT=~/.net.sf.iwant/cached/UnmodifiableUrl/http%3A/%2Fmirrors.ibiblio.org/maven2/junit/junit/4.8.2/junit-4.8.2.jar
  IWANT_TESTAREA_PROJ=$HERE/../iwant-testarea
  IWANT_TESTRUNNER_PROJ=$HERE/../iwant-testrunner

  CLASSES_TO_TEST=$CACHED/classes-to-test
  fresh-dir "$CLASSES_TO_TEST"
  javac \
    -d "$CLASSES_TO_TEST" \
    -cp "$JUNIT" \
    $(find $HERE/as-some-developer/with/java -name '*.java') \
    $(find $HERE/src/main/java -name '*.java') \
    $(find $IWANT_TESTAREA_PROJ/src/main/java -name '*.java') \
    $(find $IWANT_TESTRUNNER_PROJ/src/main/java -name '*.java') \
    $(find $IWANT_TESTRUNNER_PROJ/src/test/java -name '*.java') \
    $(find $HERE/src/test/java -name '*.java')
  cp "$HERE/src/test/java/net/sf/iwant/entry/"*.zip \
    "$CLASSES_TO_TEST/net/sf/iwant/entry/"
  touch "$CLASSES_TO_TEST/compiled-by-refresh.sh"

  java -cp "$CLASSES_TO_TEST:$JUNIT" junit.textui.TestRunner net.sf.iwant.testrunner.IwantTestRunnerTest

  java -cp "$CLASSES_TO_TEST:$IWANT_TESTAREA_PROJ/testarea-classdir:$JUNIT:$HERE/classpath-marker" net.sf.iwant.testrunner.IwantTestRunner net.sf.iwant.entry.IwantEntrySuite
}

mocked-java-entry-content() {
cat <<EOF
package net.sf.iwant.entry;
public class Iwant {
  public static void main(String[] args) {
    System.out.println("Mocked iwant entry");
    System.out.println("CWD="+System.getProperty("user.dir"));
    System.out.println("args="+java.util.Arrays.toString(args));
    System.exit(1);
  }
}
EOF
}

write-mocked-java-entry() {
  local PACKAGE=$1
  mkdir -p "$PACKAGE"
  mocked-java-entry-content > "$PACKAGE"/Iwant.java
}

tested-help.sh() {
  HELPSH_TESTAREA=$CACHED/test.sh-testarea
  fresh-dir "$HELPSH_TESTAREA"
  AS_HELPSH_TEST=$HELPSH_TESTAREA/as-help.sh-test

  # initialize iwant entry with mocked java
  svn export as-some-developer "$AS_HELPSH_TEST"/
  write-mocked-java-entry "$AS_HELPSH_TEST/with/java/net/sf/iwant/entry"

  # test help.sh compiles and runs the class correctly:
  cd "$HELPSH_TESTAREA"
  HELPSH_OUT=$HELPSH_TESTAREA/help.sh-out
  HELPSH_ERR=$HELPSH_TESTAREA/help.sh-err
  as-help.sh-test/with/bash/iwant/help.sh cmdline arguments to test > "$HELPSH_OUT" 2> "$HELPSH_ERR" && die "help.sh exited with status 0"
  grep '.' "$HELPSH_ERR" && die "wrong stderr"
  HELPSH_EXPECTED_OUT=$HELPSH_TESTAREA/help.sh-expected-out
cat > "$HELPSH_EXPECTED_OUT" <<EOF
Mocked iwant entry
CWD=$(pwd)
args=[$AS_HELPSH_TEST, cmdline, arguments, to, test]
EOF
  diff "$HELPSH_EXPECTED_OUT" "$HELPSH_OUT" || die "wrong stdout"
}

java-bootstrapping.html() {
  DESCRNAMEBASE=$HERE/java-bootstrapping

  IWANT_DISTILLERY=$HERE $HERE/../iwant-lib-descript/descript.sh "$DESCRNAMEBASE".sh "$DESCRNAMEBASE".html true
}

tested-java-classes
tested-help.sh
java-bootstrapping.html
