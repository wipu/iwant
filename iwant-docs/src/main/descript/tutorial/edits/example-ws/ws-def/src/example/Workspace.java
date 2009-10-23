package example;

import net.sf.iwant.core.Constant;
import net.sf.iwant.core.RootPath;
import net.sf.iwant.core.Target;

public class Workspace extends RootPath {

    public Target aConstant() {
        return target("aConstant").
            content(Constant.value("Constant generated content\n")).end();
    }

}
