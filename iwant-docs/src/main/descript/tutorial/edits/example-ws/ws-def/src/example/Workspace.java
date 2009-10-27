package example;

import net.sf.iwant.core.Constant;
import net.sf.iwant.core.ContainerPath;
import net.sf.iwant.core.Locations;
import net.sf.iwant.core.RootPath;
import net.sf.iwant.core.Target;
import net.sf.iwant.core.WorkspaceDefinition;

public class Workspace implements WorkspaceDefinition {

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

    }

}
