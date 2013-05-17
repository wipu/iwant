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
  def-edit wsdef "$NAME" Workspace
}

wsdefdef-edit() {
  local NAME=$1
  def-edit wsdefdef "$NAME" WorkspaceProvider
}

module-edit() {
  local MODULE=$1
  local SRCDIR=$2
  local NAME=$3
  local CLASS=$4
  log "module-edit $MODULE $SRCDIR $NAME $CLASS"
  cat "$IWANT_TUTORIAL_WSDEF_SRC/com/example/$MODULE/$NAME/${CLASS}.java" |
    sed "s:\.$NAME::g" |
    edit example-$MODULE/${SRCDIR}/com/example/$MODULE/${CLASS}.java "$MODULE-$NAME"
}

def-edit() {
  local TYPE=$1
  local NAME=$2
  local CLASS=$3
  log "def-edit $TYPE $NAME $CLASS"
  cat "$IWANT_TUTORIAL_WSDEF_SRC/com/example/$TYPE/$NAME/${CLASS}.java" |
    sed "s:\.$NAME::g" |
    edit as-iwant-tutorial-developer/i-have/${TYPE}/src/main/java/com/example/$TYPE/${CLASS}.java "$TYPE-$NAME"
}
