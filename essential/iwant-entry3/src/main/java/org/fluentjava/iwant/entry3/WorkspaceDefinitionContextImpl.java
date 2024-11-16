package org.fluentjava.iwant.entry3;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

import org.fluentjava.iwant.api.javamodules.JavaBinModule;
import org.fluentjava.iwant.api.javamodules.JavaClasses;
import org.fluentjava.iwant.api.javamodules.JavaClasses.JavaClassesSpex;
import org.fluentjava.iwant.api.javamodules.JavaModule;
import org.fluentjava.iwant.api.model.ExternalSource;
import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.wsdef.IwantPluginWish;
import org.fluentjava.iwant.api.wsdef.IwantPluginWishes;
import org.fluentjava.iwant.api.wsdef.WorkspaceModuleContext;
import org.fluentjava.iwant.core.download.Downloaded;
import org.fluentjava.iwant.core.download.GnvArtifact;
import org.fluentjava.iwant.core.download.TestedIwantDependencies;

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
		return ExternalSource.at(src);
	}

	private static JavaModule mod(Path artifact) {
		return JavaBinModule.providing(artifact).end();
	}

	private Set<JavaModule> pluginWithDependencies(String pluginName,
			Set<JavaModule> dependencies) {
		Path pluginJava = pluginMainJava(pluginName);
		JavaClassesSpex pluginClasses = JavaClasses.with().name(pluginName)
				.srcDirs(pluginJava).debug(true);
		for (JavaModule dependency : dependencies) {
			pluginClasses.classLocations(dependency.mainArtifact());
		}

		Set<JavaModule> mods = new LinkedHashSet<>();
		mods.add(
				JavaBinModule.providing(pluginClasses.end(), pluginJava).end());
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
					Set<JavaModule> deps = new LinkedHashSet<>();
					deps.addAll(iwantApiModules);
					deps.add(apacheAnt());
					deps.add(apacheAntLauncher());
					return pluginWithDependencies("iwant-plugin-ant", deps);
				}
			};
		}

		@Override
		public IwantPluginWish findbugs() {
			return new IwantPluginWish() {
				@Override
				public Set<JavaModule> withDependencies() {
					Set<JavaModule> deps = new LinkedHashSet<>();
					deps.addAll(iwantApiModules);
					deps.add(commonsIo());
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
					deps.addAll(iwantApiModules);
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
					deps.addAll(iwantApiModules);
					deps.addAll(ant().withDependencies());
					deps.add(commonsIo());
					return pluginWithDependencies("iwant-plugin-jacoco", deps);
				}

			};
		}

		@Override
		public IwantPluginWish junit5runner() {
			return new IwantPluginWish() {
				@Override
				public Set<JavaModule> withDependencies() {
					// NOTE: it's important *NOT* to add iwantApiModules as deps
					// here
					// because this junit5runner is used for running tests of
					// iwant's own
					// modules, and there the existence of real api prevents
					// using "mocked"
					// versions of them in tests. (Iwant2Test)
					// And this module anyway will never need any iwant api: it
					// simply calls
					// junit with given tests.
					Set<JavaModule> deps = new LinkedHashSet<>();
					for (GnvArtifact<Downloaded> dep : TestedIwantDependencies
							.junitJupiterCompileDeps()) {
						deps.add(mod(dep));
					}
					for (GnvArtifact<Downloaded> dep : TestedIwantDependencies
							.junitJupiterRtDeps()) {
						deps.add(mod(dep));
					}
					return pluginWithDependencies("iwant-plugin-junit5runner",
							deps);
				}

			};
		}

		@Override
		public IwantPluginWish pmd() {
			return new IwantPluginWish() {
				@Override
				public Set<JavaModule> withDependencies() {
					Set<JavaModule> deps = new LinkedHashSet<>();
					deps.addAll(iwantApiModules);
					deps.add(apacheAnt());
					deps.add(asm());
					deps.add(commonsIo());
					deps.add(jaxen());
					deps.add(pmdPmd());

					return pluginWithDependencies("iwant-plugin-pmd", deps);
				}

			};
		}

		@Override
		public IwantPluginWish testng() {
			return new IwantPluginWish() {
				@Override
				public Set<JavaModule> withDependencies() {
					Set<JavaModule> deps = new LinkedHashSet<>();
					deps.addAll(iwantApiModules);
					deps.add(jcommander());
					deps.add(orgTestng());
					return pluginWithDependencies("iwant-plugin-testng", deps);
				}

			};
		}

		@Override
		public IwantPluginWish war() {
			return new IwantPluginWish() {
				@Override
				public Set<JavaModule> withDependencies() {
					Set<JavaModule> deps = new LinkedHashSet<>();
					deps.addAll(iwantApiModules);
					deps.add(apacheAnt());
					return pluginWithDependencies("iwant-plugin-war", deps);
				}
			};
		}

	}

	private static JavaModule apacheAnt() {
		return mod(TestedIwantDependencies.antJar());
	}

	private static JavaModule apacheAntLauncher() {
		return mod(TestedIwantDependencies.antLauncherJar());
	}

	private static JavaModule commonsIo() {
		return mod(TestedIwantDependencies.commonsIo());
	}

	private static JavaModule jcommander() {
		return mod(TestedIwantDependencies.jcommander());
	}

	private static JavaModule orgTestng() {
		return mod(TestedIwantDependencies.testng());
	}

	private static JavaModule asm() {
		return mod(TestedIwantDependencies.asm());
	}

	private static JavaModule jaxen() {
		return mod(TestedIwantDependencies.jaxen());
	}

	private static JavaModule pmdPmd() {
		return mod(TestedIwantDependencies.pmd());
	}

}
