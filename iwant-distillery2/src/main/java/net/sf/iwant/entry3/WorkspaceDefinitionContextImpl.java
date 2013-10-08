package net.sf.iwant.entry3;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

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

	private class IwantPluginWishesImpl implements IwantPluginWishes {

		@Override
		public IwantPluginWish ant() {
			return new Ant();
		}

		private class Ant implements IwantPluginWish {

			@Override
			public Set<JavaModule> withDependencies() {
				String pluginName = "iwant-plugin-ant";

				Path antJar = TestedIwantDependencies.antJar();

				Path antPluginJava = pluginMainJava(pluginName);
				JavaClassesSpex antPluginClasses = JavaClasses.with()
						.name(pluginName).srcDirs(antPluginJava).debug(true);
				for (JavaModule iwantApiModule : iwantApiModules) {
					antPluginClasses.classLocations(iwantApiModule
							.mainArtifact());
				}
				antPluginClasses.classLocations(antJar);

				Set<JavaModule> mods = new LinkedHashSet<JavaModule>();
				mods.add(JavaBinModule.providing(antPluginClasses.end(),
						antPluginJava).end());
				mods.addAll(iwantApiModules);
				mods.add(JavaBinModule.providing(antJar).end());
				return mods;
			}

		}

	}

}
