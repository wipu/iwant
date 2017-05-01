package net.sf.iwant.core.javafinder;

import net.sf.iwant.api.javamodules.JavaSrcModule;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.wsdef.WishDefinitionContext;
import net.sf.iwant.entry.Iwant.IwantException;

public class WsdefJavaOf {

	private final WishDefinitionContext ctx;

	public WsdefJavaOf(WishDefinitionContext ctx) {
		this.ctx = ctx;
	}

	public Source classUnderSrcMainJava(Class<?> classDefinedInWsdef) {
		return classUnder(classDefinedInWsdef, "src/main/java");
	}

	public Source classUnder(Class<?> classDefinedInWsdef,
			String relativeJavaDir) {
		JavaSrcModule wsdef = ctx.wsdefJavaModule();

		verifyJavaDirExists(wsdef, relativeJavaDir);

		String java = classDefinedInWsdef.getName().replace(".", "/") + ".java";
		return Source.underWsroot(wsdef.locationUnderWsRoot() + "/"
				+ relativeJavaDir + "/" + java);
	}

	private static void verifyJavaDirExists(JavaSrcModule wsdef,
			String relativeJavaDir) {
		for (String mainJava : wsdef.mainJavas()) {
			if (relativeJavaDir.equals(mainJava)) {
				return;
			}
		}
		throw new IwantException("Module " + wsdef
				+ " does not have java diretory " + relativeJavaDir);
	}

}
