doc-content() {

cd iwant-tutorial

p "When the existing functionality is not enough, you can implement your own target."

html-line "<h3>Target with ingredients</h3>"

p "Let's define a custom target that sums the sizes of (the cached contents of) given paths. First we will write a naive implementation that just uses the cached contents of other paths without declaring them ingredients."

wsdef-edit customtargetundeclaredingr
def-edit wsdef customtargetundeclaredingr FileSizeSum
cmde "0" "as-iwant-tutorial-developer/with/bash/iwant/list-of/targets"
cmde "1 0" "as-iwant-tutorial-developer/with/bash/iwant/target/file-size-sum/as-path | xargs -r find"
out-was <<EOF
(0/1 D! com.example.wsdef.FileSizeSum file-size-sum)
Refreshing $PWD/as-iwant-tutorial-developer/.i-cached/target/file-size-sum
(FAILED com.example.wsdef.FileSizeSum file-size-sum)
Target file-size-sum referred to ingredient1 without declaring it an ingredient.
EOF

p "The context iwant passes to our target allows us to resolve a path to a file that contains its cached content. But if we haven't declared the referred path an ingredient, iwant will throw an exception."

p "We can fix this by declaring the ingredients:"

def-edit wsdef customtargetdeclaredingr FileSizeSum
cmde "0 0" "as-iwant-tutorial-developer/with/bash/iwant/target/file-size-sum/as-path | xargs -r cat"
out-was <<EOF
(0/1 S~ org.fluentjava.iwant.api.javamodules.JavaClasses iwant-tutorial-wsdef-main-classes)
(0/1 D! org.fluentjava.iwant.api.core.HelloTarget ingredient1)
(0/1 D! org.fluentjava.iwant.api.core.HelloTarget ingredient2)
(0/1 D! com.example.wsdef.FileSizeSum file-size-sum)
Refreshing $PWD/as-iwant-tutorial-developer/.i-cached/target/file-size-sum
5
EOF

p "Now that we declared the ingredients, iwant automatically told them to refresh their cached contents before telling our target to refresh its. We can see this from the output. The D! before a target's type and name means the target is refreshed because its cached content descriptor (and thus also its content) is missing."

p "(We can also see that the workspace definition classes were updated. The S~ in the output means they were updated because of modified source ingredients. We just modified the source of our target, a part of the workspace definition.)"

p "iwant uses a target's cached content descriptor and its timestamp to determine whether the target is dirty (needs refreshing) or up-to-date. We as target authors just defined the content descriptor in the method ingredientsAndParameters."

p "This is what our cached content descriptor looks like:"

cmd "cat as-iwant-tutorial-developer/.i-cached/descriptor/file-size-sum"
out-was <<EOF
com.example.wsdef.FileSizeSum
i:pathsToSum:
  ingredient1
  ingredient2
EOF

html-line "<h3>Target with parameters</h3>"

p "Next we will make our target accept static input, a parameter. We will add a header line above the size sum line. Again, we first try what happens if we don't declare this change to our content definition."

wsdef-edit customtargetundeclaredparam
def-edit wsdef customtargetundeclaredparam FileSizeSum
cmde "0 0" "as-iwant-tutorial-developer/with/bash/iwant/target/file-size-sum/as-path | xargs -r cat"
out-was <<EOF
(0/1 S~ org.fluentjava.iwant.api.javamodules.JavaClasses iwant-tutorial-wsdef-main-classes)
5
EOF

html "<p class='text'>Whoops, cache invalidation! Unfortunately, <u>at the moment iwant cannot help, if the target author forgets to declare a parameter</u> i.e. input that is not a reference to another path, an ingredient.<p>"

p "We can fix this by declaring the header line a parameter:"

def-edit wsdef customtargetdeclaredparam FileSizeSum
cmde "0 0" "as-iwant-tutorial-developer/with/bash/iwant/target/file-size-sum/as-path | xargs -r cat"
out-was <<EOF
(0/1 S~ org.fluentjava.iwant.api.javamodules.JavaClasses iwant-tutorial-wsdef-main-classes)
(0/1 D~ com.example.wsdef.FileSizeSum file-size-sum)
Refreshing $PWD/as-iwant-tutorial-developer/.i-cached/target/file-size-sum
The sum
5
EOF

p "Now, after the last wish, our target is up-to-date:"
cmde "0 0" "as-iwant-tutorial-developer/with/bash/iwant/target/file-size-sum/as-path | xargs -r cat"
out-was <<EOF
The sum
5
EOF

p "But if we change the value of the parameter, the target will become dirty and be refreshed."

wsdef-edit customtargetdeclaredparam
cmde "0 0" "as-iwant-tutorial-developer/with/bash/iwant/target/file-size-sum/as-path | xargs -r cat"
out-was <<EOF
(0/1 S~ org.fluentjava.iwant.api.javamodules.JavaClasses iwant-tutorial-wsdef-main-classes)
(0/1 D~ com.example.wsdef.FileSizeSum file-size-sum)
Refreshing $PWD/as-iwant-tutorial-developer/.i-cached/target/file-size-sum
The sum of file sizes
5
EOF

p "The D~ in the output means the target was refreshed because of a changed content descriptor. Out of curiosity, let's see what our content descriptor now looks like."

cmd "cat as-iwant-tutorial-developer/.i-cached/descriptor/file-size-sum"
out-was <<EOF
com.example.wsdef.FileSizeSum
i:pathsToSum:
  ingredient1
  ingredient2
p:headerLineContent:
  The sum of file sizes
EOF

html-line "<h3>Target under construction</h3>"

p "When developing a target, it is convenient to declare its own source an ingredient. This way any change to its source makes it dirty. If the target uses other classes, they or their classes also need to be declared."

p "Let's try it for our target."

def-edit wsdef customtargetselfingr FileSizeSum
wsdef-edit customtargetselfingr
def-edit wsdef customtargetselfingr IwanttutorialWorkspaceFactory
cmde "0 0" "as-iwant-tutorial-developer/with/bash/iwant/target/file-size-sum/as-path | xargs -r cat"
out-was <<EOF
(0/1 S~ org.fluentjava.iwant.api.javamodules.JavaClasses iwant-tutorial-wsdef-main-classes)
(0/1 D~ com.example.wsdef.FileSizeSum file-size-sum)
Refreshing $PWD/as-iwant-tutorial-developer/.i-cached/target/file-size-sum
The sum of file sizes
5
EOF

p "Our target is now up-to-date, but if we touch its source, it will be refreshed."

cmde "0 0" "as-iwant-tutorial-developer/with/bash/iwant/target/file-size-sum/as-path | xargs -r cat"
out-was <<EOF
The sum of file sizes
5
EOF
cmd "touch as-iwant-tutorial-developer/i-have/wsdef/src/main/java/com/example/wsdef/FileSizeSum.java"
cmde "0 0" "as-iwant-tutorial-developer/with/bash/iwant/target/file-size-sum/as-path | xargs -r cat"
out-was <<EOF
(0/1 S~ org.fluentjava.iwant.api.javamodules.JavaClasses iwant-tutorial-wsdef-main-classes)
(0/1 S~ com.example.wsdef.FileSizeSum file-size-sum)
Refreshing $PWD/as-iwant-tutorial-developer/.i-cached/target/file-size-sum
The sum of file sizes
5
EOF

}
