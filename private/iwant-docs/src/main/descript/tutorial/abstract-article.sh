doc() {
  [ "x${INITIAL_STATE:-}" == "x" ] || {
    log "Creating initial state from $INITIAL_STATE"
    tar xf "$INITIAL_STATE"
  } 
  IWANT_DOC_DIR=$(pwd)
  
  section "$PAGETITLE"
  doc-content

  cd "$IWANT_DOC_DIR"
  SAVED_STATE=$(readlink -f ../../final-state.tar)
  log "Saving state to $SAVED_STATE"
  tar cf "$SAVED_STATE" ./
}

doc-content() {
  section "TODO override doc-content"
}

wsdef-edit() {
  local NAME=$1
  def-edit wsdef "$NAME" IwanttutorialWorkspace
}

wsdefdef-edit() {
  local NAME=$1
  def-edit wsdefdef "$NAME" IwantTutorialWorkspaceProvider
}

# TODO remove arg NAME
without-subpackage() {
  local NAME=$1
  sed "s:\.editversion[a-zA-Z0-9]*::g"
}

module-edit() {
  local MODULE=$1
  local SRCDIR=$2
  local NAME=$3
  local CLASS=$4
  log "module-edit $MODULE $SRCDIR $NAME $CLASS"
  cat "$IWANT_TUTORIAL_WSDEF_SRC/com/example/$MODULE/editversion$NAME/${CLASS}.java" |
    without-subpackage "$NAME" |
    edit example-$MODULE/${SRCDIR}/com/example/$MODULE/${CLASS}.java "$MODULE-$NAME"
}

def-edit() {
  local TYPE=$1
  local NAME=$2
  local CLASS=$3
  log "def-edit $TYPE $NAME $CLASS"
  cat "$IWANT_TUTORIAL_WSDEF_SRC/com/example/$TYPE/editversion$NAME/${CLASS}.java" |
    without-subpackage "$NAME" |
    edit as-iwant-tutorial-developer/i-have/${TYPE}/src/main/java/com/example/$TYPE/${CLASS}.java "$TYPE-$NAME"
}
