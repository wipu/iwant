doc() {
  svn export "$IWANT_DISTILLERY/as-some-developer" as-distillery-developer
  cmd 'find .'
  cmd 'as-distillery-developer/with/bash/iwant/help.sh'
}
