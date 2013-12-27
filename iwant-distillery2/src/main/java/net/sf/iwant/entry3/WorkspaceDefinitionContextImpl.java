package net.sf.iwant.entry3;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import net.sf.iwant.api.Downloaded;
import net.sf.iwant.api.FromRepository;
import net.sf.iwant.api.IwantPluginWish;
import net.sf.iwant.api.IwantPluginWishes;
import net.sf.iwant.api.TestedIwantDependencies;
import net.sf.iwant.api.WorkspaceDefinitionContext;
import net.sf.iwant.api.javamodules.JavaBinModule;
import net.sf.iwant.api.javamodules.JavaClasses;
import net.sf.iwant.api.javamodules.JavaClasses.JavaClassesSpex;
import net.sf.iwant.api.javamodules.JavaModule;
import net.sf.iwant.api.model.ExternalSource;
import net.sf.iwant.api.model.Path;

public class WorkspaceDefinitionContextImpl implements
		WorkspaceDefinitionContext {

	private final Set<JavaModule> iwantApiModules;
	private final File iwantWs;
	private final JavaModule wsdefdefModule;

	public WorkspaceDefinitionContextImpl(Set<JavaModule> iwantApiModules,
			File iwantWs, JavaModule wsdefdefModule) {
		this.iwantApiModules = iwantApiModules;
		this.iwantWs = iwantWs;
		this.wsdefdefModule = wsdefdefModule;
	}

	@Override
	public Set<JavaModule> iwantApiModules() {
		return iwantApiModules;
	}

	@Override
	public JavaModule wsdefdefModule() {
		return wsdefdefModule;
	}

	@Override
	public IwantPluginWishes iwantPlugin() {
		return new IwantPluginWishesImpl();
	}

	private Path pluginMainJava(String pluginName) {
		try {
			return new ExternalSource(new File(iwantWs, pluginName
					+ "/src/main/java"));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private Set<JavaModule> pluginWithDependencies(String pluginName,
			Path... dependencies) {
		Set<JavaModule> depModules = new LinkedHashSet<JavaModule>();
		for (Path dependency : dependencies) {
			depModules.add(JavaBinModule.providing(dependency).end());
		}
		return pluginWithDependencies(pluginName, depModules);
	}

	private Set<JavaModule> pluginWithDependencies(String pluginName,
			Set<JavaModule> dependencies) {
		Path pluginJava = pluginMainJava(pluginName);
		JavaClassesSpex pluginClasses = JavaClasses.with().name(pluginName)
				.srcDirs(pluginJava).debug(true);
		for (JavaModule iwantApiModule : iwantApiModules) {
			pluginClasses.classLocations(iwantApiModule.mainArtifact());
		}
		for (JavaModule dependency : dependencies) {
			pluginClasses.classLocations(dependency.mainArtifact());
		}

		Set<JavaModule> mods = new LinkedHashSet<JavaModule>();
		mods.add(JavaBinModule.providing(pluginClasses.end(), pluginJava).end());
		mods.addAll(iwantApiModules);
		mods.addAll(dependencies);
		return mods;
	}

	/**
	 * TODO reuse the dependencies with iwant's own build
	 */
	private class IwantPluginWishesImpl implements IwantPluginWishes {

		@Override
		public IwantPluginWish ant() {
			return new IwantPluginWish() {
				@Override
				public Set<JavaModule> withDependencies() {
					// launcher needed in case the user of this plugin also
					// wants to use AntGenerated that dynamically loads ant
					// (unless we already have a smarter classloader)
					return pluginWithDependencies("iwant-plugin-ant", antJar(),
							antLauncherJar());
				}
			};
		}

		@Override
		public IwantPluginWish findbugs() {
			return new IwantPluginWish() {
				@Override
				public Set<JavaModule> withDependencies() {
					Set<JavaModule> deps = new LinkedHashSet<JavaModule>();
					deps.add(JavaBinModule.providing(commonsIoJar()).end());
					deps.addAll(ant().withDependencies());
					return pluginWithDependencies("iwant-plugin-findbugs", deps);
				}

			};
		}

		@Override
		public IwantPluginWish pmd() {
			return new IwantPluginWish() {
				@Override
				public Set<JavaModule> withDependencies() {
					return pluginWithDependencies(
							"iwant-plugin-pmd",
							antJar(),
							FromRepository.ibiblio().group("asm").name("asm")
									.version("3.2"),
							commonsIoJar(),
							FromRepository.ibiblio().group("jaxen")
									.name("jaxen").version("1.1.4"),
							FromRepository.ibiblio().group("pmd").name("pmd")
									.version("4.3"));
				}

			};
		}

		@Override
		public IwantPluginWish war() {
			return new IwantPluginWish() {
				@Override
				public Set<JavaModule> withDependencies() {
					return pluginWithDependencies("iwant-plugin-war", antJar());
				}
			};
		}

	}

	private static Path antJar() {
		return TestedIwantDependencies.antJar();
	}

	private static Path antLauncherJar() {
		return TestedIwantDependencies.antLauncherJar();
	}

	private static Downloaded commonsIoJar() {
		return FromRepository.ibiblio().group("org/apache/commons")
				.name("commons-io").version("1.3.2");
	}

}
