package com.example.wsdef2;

import net.sf.iwant.core.ContainerPath;
import net.sf.iwant.core.Locations;
import net.sf.iwant.core.RootPath;
import net.sf.iwant.core.Target;
import net.sf.iwant.core.WorkspaceDefinition;

public class ExampleWorkspace implements WorkspaceDefinition {

    public ContainerPath wsRoot(Locations locations) {
        return new Root(locations);
    }

    public static class Root extends RootPath {

        public Root(Locations locations) {
            super(locations);
        }

        public Target<CustomContent> targetWithCustomContent() {
            return target("targetWithCustomContent").
                content(CustomContent.value("Hello")).end();
        }

    }

}
