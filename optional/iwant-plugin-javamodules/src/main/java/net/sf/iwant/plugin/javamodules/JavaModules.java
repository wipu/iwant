package net.sf.iwant.plugin.javamodules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.iwant.api.javamodules.JavaBinModule;
import net.sf.iwant.api.javamodules.JavaCompliance;
import net.sf.iwant.api.javamodules.JavaModule;
import net.sf.iwant.api.javamodules.JavaSrcModule;
import net.sf.iwant.api.javamodules.JavaSrcModule.IwantSrcModuleSpex;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.core.download.FromRepository;
import net.sf.iwant.plugin.ant.Jar;

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

	protected JavaBinModule binModule(String group, String name,
			String version, JavaModule... runtimeDeps) {
		Path jar = FromRepository.repo1MavenOrg().group(group).name(name)
				.version(version);
		return binModule(jar, runtimeDeps);
	}

	public static JavaBinModule binModule(Path mainArtifact,
			JavaModule... runtimeDeps) {
		return JavaBinModule.providing(mainArtifact).runtimeDeps(runtimeDeps)
				.end();
	}

	public static List<Path> mainArtifactsOf(JavaModule... modules) {
		return mainArtifactsOf(Arrays.asList(modules));
	}

	public static List<Path> mainArtifactsOf(
			Collection<? extends JavaModule> modules) {
		List<Path> artifacts = new ArrayList<Path>();
		for (JavaModule module : modules) {
			Path mainArtifact = module.mainArtifact();
			if (mainArtifact != null) {
				artifacts.add(mainArtifact);
			}
		}
		return artifacts;
	}

	public static List<Path> mainArtifactJarsOf(JavaModule... modules) {
		return mainArtifactJarsOf(Arrays.asList(modules));
	}

	public static List<Path> mainArtifactJarsOf(
			Collection<? extends JavaModule> modules) {
		List<Path> jars = new ArrayList<Path>();
		for (JavaModule module : modules) {
			Path jar = mainJarOf(module);
			if (jar != null) {
				jars.add(jar);
			}
		}
		return jars;
	}

	public static Path mainJarOf(JavaModule module) {
		if (module.mainArtifact() == null) {
			return null;
		}
		if (module instanceof JavaBinModule) {
			return module.mainArtifact();
		} else {
			return Jar.with().name(module.name() + ".jar")
					.classes(module.mainArtifact()).end();
		}
	}

}
