doc() {
  svn export "$IWANT_DISTILLERY/as-some-developer" as-distillery-developer
  cmd 'find .'
  cmde 1 'as-distillery-developer/with/bash/iwant/help.sh'
  cmd cat as-distillery-developer/i-have/iwant-from
}
