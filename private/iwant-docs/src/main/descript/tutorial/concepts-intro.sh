subsec() {
  local TITLE=$1
  html "<h2>$TITLE</h2>"
}

humanquote() {
  TEXT=$1
  html '<p><i>'
  html "$TEXT"
  html '</i></p>'
}

doc-content() {

subsec "Declarative content of files"

humanquote '"What is this file called cool-app.jar?"'

p "If you had to answer a question like this, you would probably answer something like"

humanquote '"It is a (zip) file that contains the main (i.e. not test) java classes of cool-app."'
p "Or, the same using iwant:"
inline-snippet "coolAppJar"

humanquote '"Well, what are the java classes, then?"'
humanquote '"They are bytecode compiled from the main java files of cool-app."'
inline-snippet "coolAppClasses"

humanquote '"And what are the java files?"'
humanquote '"They are text files written and maintained by the developers."'
inline-snippet "coolAppJava"

html "<p>The answers here are examples of declarative definitions. They answer to questions that start with <i>what</i> by describing file content. The focus is in in <i>nouns</i>.</p>"

html "<p>One of the main principles of iwant is that <u>build script authors and users declare <i>what</i> they want</u>. The question of <i>how</i>, the imperative part, or the <i>verbs</i>, belongs to developers of iwant and its plugins.</p>"

html "If only it wasn't so difficult to know what to want..."

subsec "Graph of static and dynamic content"

p "todo"

}
