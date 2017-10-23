package net.sf.iwant.entry3;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import net.sf.iwant.api.javamodules.JavaBinModule;
import net.sf.iwant.api.javamodules.JavaClasses;
import net.sf.iwant.api.javamodules.JavaClasses.JavaClassesSpex;
import net.sf.iwant.api.javamodules.JavaModule;
import net.sf.iwant.api.model.ExternalSource;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.wsdef.IwantPluginWish;
import net.sf.iwant.api.wsdef.IwantPluginWishes;
import net.sf.iwant.api.wsdef.WorkspaceModuleContext;
import net.sf.iwant.core.download.Downloaded;
import net.sf.iwant.core.download.FromRepository;
import net.sf.iwant.core.download.GnvArtifact;
import net.sf.iwant.core.download.TestedIwantDependencies;

public class WorkspaceDefinitionContextImpl implements WorkspaceModuleContext {

	private final Set<JavaModule> iwantApiModules;
	private final JavaModule wsdefdefModule;
	private final File cachedIwantSrcRoot;

	public WorkspaceDefinitionContextImpl(Set<JavaModule> iwantApiModules,
			File cachedIwantSrcRoot, JavaModule wsdefdefModule) {
		this.iwantApiModules = iwantApiModules;
		this.wsdefdefModule = wsdefdefModule;
		this.cachedIwantSrcRoot = cachedIwantSrcRoot;
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
		File src = new File(cachedIwantSrcRoot,
				"optional/" + pluginName + "/src/main/java");
		try {
			return new ExternalSource(src);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Set<JavaModule> pluginWithDependencies(String pluginName,
			Path... dependencies) {
		Set<JavaModule> depModules = new LinkedHashSet<>();
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

		Set<JavaModule> mods = new LinkedHashSet<>();
		mods.add(
				JavaBinModule.providing(pluginClasses.end(), pluginJava).end());
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
					Set<JavaModule> deps = new LinkedHashSet<>();
					deps.add(JavaBinModule.providing(commonsIoJar()).end());
					deps.addAll(ant().withDependencies());
					return pluginWithDependencies("iwant-plugin-findbugs",
							deps);
				}

			};
		}

		@Override
		public IwantPluginWish github() {
			return new IwantPluginWish() {
				@Override
				public Set<JavaModule> withDependencies() {
					Set<JavaModule> deps = ant().withDependencies();
					Set<JavaModule> modules = pluginWithDependencies(
							"iwant-plugin-github", deps);
					return modules;
				}
			};
		}

		@Override
		public IwantPluginWish jacoco() {
			return new IwantPluginWish() {
				@Override
				public Set<JavaModule> withDependencies() {
					Set<JavaModule> deps = new LinkedHashSet<>();
					deps.addAll(ant().withDependencies());
					deps.add(JavaBinModule.providing(commonsIoJar()).end());
					return pluginWithDependencies("iwant-plugin-jacoco", deps);
				}

			};
		}

		@Override
		public IwantPluginWish pmd() {
			return new IwantPluginWish() {
				@Override
				public Set<JavaModule> withDependencies() {
					return pluginWithDependencies("iwant-plugin-pmd", antJar(),
							FromRepository.repo1MavenOrg().group("asm")
									.name("asm").version("3.2").jar(),
							commonsIoJar(),
							FromRepository.repo1MavenOrg().group("jaxen")
									.name("jaxen").version("1.1.4").jar(),
							FromRepository.repo1MavenOrg().group("pmd")
									.name("pmd").version("4.3").jar());
				}

			};
		}

		@Override
		public IwantPluginWish testng() {
			return new IwantPluginWish() {
				@Override
				public Set<JavaModule> withDependencies() {
					Set<JavaModule> deps = new LinkedHashSet<>();
					deps.add(JavaBinModule.providing(jcommanderJar()).end());
					deps.add(JavaBinModule.providing(testngJar()).end());
					return pluginWithDependencies("iwant-plugin-testng", deps);
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

	private static GnvArtifact<Downloaded> commonsIoJar() {
		return FromRepository.repo1MavenOrg().group("org/apache/commons")
				.name("commons-io").version("1.3.2").jar();
	}

	private static Path jcommanderJar() {
		return TestedIwantDependencies.jcommander();
	}

	private static Path testngJar() {
		return TestedIwantDependencies.testng();
	}

}
