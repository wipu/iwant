package org.fluentjava.iwant.core.javamodules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.fluentjava.iwant.api.javamodules.JavaBinModule;
import org.fluentjava.iwant.api.javamodules.JavaCompliance;
import org.fluentjava.iwant.api.javamodules.JavaModule;
import org.fluentjava.iwant.api.javamodules.JavaSrcModule;
import org.fluentjava.iwant.api.javamodules.JavaSrcModule.IwantSrcModuleSpex;
import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.zip.Jar;
import org.fluentjava.iwant.api.zip.Jar.JarSpex;
import org.fluentjava.iwant.core.download.FromRepository;

public abstract class JavaModules {

	private final SortedSet<JavaSrcModule> allSrcModules = new TreeSet<>();

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
		return m.javaCompliance(JavaCompliance.JAVA_21).mavenLayout();
	}

	private class CollectingSrcSpex extends IwantSrcModuleSpex {
		@Override
		public JavaSrcModule end() {
			JavaSrcModule mod = super.end();
			allSrcModules.add(mod);
			return mod;
		}
	}

	public static JavaBinModule binModule(String group, String name,
			String version, JavaModule... runtimeDeps) {
		Path jar = FromRepository.repo1MavenOrg().group(group).name(name)
				.version(version).jar();
		Path src = FromRepository.repo1MavenOrg().group(group).name(name)
				.version(version).sourcesJar();
		return binModule(jar, src, runtimeDeps);
	}

	public static JavaBinModule binModuleTest(String group, String name,
			String version, JavaModule... runtimeDeps) {
		Path jar = FromRepository.repo1MavenOrg().group(group).name(name)
				.version(version).testJar();
		Path src = FromRepository.repo1MavenOrg().group(group).name(name)
				.version(version).sourcesJar();
		return binModule(jar, src, runtimeDeps);
	}

	public static JavaBinModule srclessBinModule(String group, String name,
			String version, JavaModule... runtimeDeps) {
		Path jar = FromRepository.repo1MavenOrg().group(group).name(name)
				.version(version).jar();
		return binModule(jar, runtimeDeps);
	}

	public static JavaBinModule srclessBinModuleTest(String group, String name,
			String version, JavaModule... runtimeDeps) {
		Path jar = FromRepository.repo1MavenOrg().group(group).name(name)
				.version(version).testJar();
		return binModule(jar, runtimeDeps);
	}

	public static JavaBinModule binModule(Path mainArtifact,
			JavaModule... runtimeDeps) {
		return binModule(mainArtifact, null, runtimeDeps);
	}

	public static JavaBinModule binModule(Path mainArtifact,
			Path mainArtifactSrc, JavaModule... runtimeDeps) {
		return JavaBinModule.providing(mainArtifact, mainArtifactSrc)
				.runtimeDeps(runtimeDeps).end();
	}

	public static List<Path> mainArtifactsOf(JavaModule... modules) {
		return mainArtifactsOf(Arrays.asList(modules));
	}

	public static List<Path> testArtifactsOf(JavaModule... modules) {
		return testArtifactsOf(Arrays.asList(modules));
	}

	public static List<Path> mainArtifactsOf(
			Collection<? extends JavaModule> modules) {
		List<Path> artifacts = new ArrayList<>();
		for (JavaModule module : modules) {
			Path mainArtifact = module.mainArtifact();
			if (mainArtifact != null) {
				artifacts.add(mainArtifact);
			}
		}
		return artifacts;
	}

	public static List<Path> testArtifactsOf(
			Collection<? extends JavaModule> modules) {
		List<Path> artifacts = new ArrayList<>();
		for (JavaModule module : modules) {
			if (!(module instanceof JavaSrcModule)) {
				continue;
			}
			JavaSrcModule srcModule = (JavaSrcModule) module;
			Path artifact = srcModule.testArtifact();
			if (artifact != null) {
				artifacts.add(artifact);
			}
		}
		return artifacts;
	}

	public static List<Path> mainArtifactJarsOf(JavaModule... modules) {
		return mainArtifactJarsOf(null, modules);
	}

	public static List<Path> mainArtifactJarsOf(String version,
			JavaModule... modules) {
		return mainArtifactJarsOf(version, Arrays.asList(modules));
	}

	public static List<Path> testArtifactJarsOf(JavaModule... modules) {
		return testArtifactJarsOf(Arrays.asList(modules));
	}

	public static List<Path> mainArtifactJarsOf(
			Collection<? extends JavaModule> modules) {
		return mainArtifactJarsOf(null, modules);
	}

	public static List<Path> mainArtifactJarsOf(String version,
			Collection<? extends JavaModule> modules) {
		List<Path> jars = new ArrayList<>();
		for (JavaModule module : modules) {
			Path jar = mainJarOf(version, module);
			if (jar != null) {
				jars.add(jar);
			}
		}
		return jars;
	}

	public static List<Path> testArtifactJarsOf(
			Collection<? extends JavaModule> modules) {
		List<Path> jars = new ArrayList<>();
		for (JavaModule module : modules) {
			Path jar = testJarOf(module);
			if (jar != null) {
				jars.add(jar);
			}
		}
		return jars;
	}

	public static Path mainJarOf(JavaModule module) {
		return mainJarOf(null, module);
	}

	public static Path mainJarOf(String version, JavaModule module) {
		if (module.mainArtifact() == null) {
			return null;
		}
		if (module instanceof JavaBinModule) {
			return module.mainArtifact();
		} else {
			String versionString = version == null ? "" : "-" + version;
			return Jar.with().name(module.name() + versionString + ".jar")
					.classes(module.mainArtifact()).end();
		}
	}

	public static Path testJarOf(JavaModule module) {
		if (!(module instanceof JavaSrcModule)) {
			return null;
		}
		JavaSrcModule srcModule = (JavaSrcModule) module;
		Path tests = srcModule.testArtifact();
		if (tests == null) {
			return null;
		}
		return Jar.with().name(srcModule.name() + "-tests.jar").classes(tests)
				.end();
	}

	public static Jar srcJarOf(JavaModule module) {
		return srcJarOf(null, module);
	}

	public static Jar srcJarOf(String version, JavaModule module) {
		if (!(module instanceof JavaSrcModule)) {
			return null;
		}
		JavaSrcModule srcModule = (JavaSrcModule) module;
		if (srcModule.mainJavasAsPaths().isEmpty()) {
			return null;
		}
		String versionString = version == null ? "" : "-" + version;
		JarSpex jar = Jar.with()
				.name(module.name() + versionString + "-sources.jar");
		for (Path srcDir : srcModule.mainJavasAsPaths()) {
			jar.classes(srcDir);
		}
		for (Path res : srcModule.mainResourcesAsPaths()) {
			jar.classes(res);
		}
		return jar.end();
	}

	public static Set<JavaModule> runtimeDepsOf(JavaModule... modules) {
		return runtimeDepsOf(new TreeSet<>(Arrays.asList(modules)));
	}

	public static Set<JavaModule> runtimeDepsOf(
			Collection<JavaModule> modules) {
		Set<JavaModule> deps = new LinkedHashSet<>();
		Set<JavaModule> seen = new LinkedHashSet<>();
		runtimeDepsOf(deps, seen, modules);
		return deps;
	}

	private static void runtimeDepsOf(Set<JavaModule> deps,
			Set<JavaModule> seen, Collection<JavaModule> modules) {
		for (JavaModule module : modules) {
			runtimeDepsOf(deps, seen, module);
		}
	}

	private static void runtimeDepsOf(Set<JavaModule> deps,
			Set<JavaModule> seen, JavaModule module) {
		if (seen.contains(module)) {
			return;
		}
		seen.add(module);
		for (JavaModule dep : module.effectivePathForMainRuntime()) {
			deps.add(dep);
			runtimeDepsOf(deps, seen, dep);
		}
	}

}
