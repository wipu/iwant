doc-content() {

cd iwant-tutorial

wsdefdef-edit iwantpluginwar

p "We have to generate Eclipse settings before editing the workspace definition."

cmd "as-iwant-tutorial-developer/with/bash/iwant/side-effect/eclipse-settings/effective"

p "Now we define a war target using iwant-plugin-war."

wsdef-edit iwantpluginwar

p "We create the base directory to be warred."

cmd 'mkdir web'
cmd 'echo "hello" > index.html'

p "Finally we list the content of the war file."

cmde "0" "as-iwant-tutorial-developer/with/bash/iwant/list-of/targets"
cmde "0 0 0 0" "as-iwant-tutorial-developer/with/bash/iwant/target/demo.war/as-path | xargs -r unzip -l | tail -n +4 | cut -b 30-"

out-was <<EOF
(0/1 D! org.fluentjava.iwant.api.core.Concatenated web.xml)
(0/1 D! org.fluentjava.iwant.plugin.war.War demo.war)
 META-INF/
 META-INF/MANIFEST.MF
 WEB-INF/
 WEB-INF/web.xml
 -------
 4 files
EOF

}
