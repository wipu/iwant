package net.sf.iwant.api.javamodules;

import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.iwant.api.javamodules.JavaSrcModule.IwantSrcModuleSpex;

public abstract class JavaModules {

	private final SortedSet<JavaSrcModule> allSrcModules = new TreeSet<JavaSrcModule>();

	public SortedSet<JavaSrcModule> allSrcModules() {
		return allSrcModules;
	}

	protected IwantSrcModuleSpex srcModule(String name) {
		return srcModule(null, name);
	}

	protected IwantSrcModuleSpex srcModule(String parentDir, String name) {
		String loc = parentDir == null ? name : parentDir + "/" + name;
		IwantSrcModuleSpex m = new CollectingSrcSpex().name(name)
				.locationUnderWsRoot(loc);
		return commonSettings(m);
	}

	/**
	 * Override if needed
	 */
	protected IwantSrcModuleSpex commonSettings(IwantSrcModuleSpex m) {
		return m.javaCompliance(JavaCompliance.JAVA_1_7).mavenLayout();
	}

	private class CollectingSrcSpex extends IwantSrcModuleSpex {
		@Override
		public JavaSrcModule end() {
			JavaSrcModule mod = super.end();
			allSrcModules.add(mod);
			return mod;
		}
	}

}
