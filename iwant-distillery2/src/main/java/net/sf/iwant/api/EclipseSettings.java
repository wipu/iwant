package net.sf.iwant.api;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.iwant.eclipsesettings.DotClasspath;
import net.sf.iwant.eclipsesettings.DotClasspath.DotClasspathSpex;
import net.sf.iwant.eclipsesettings.DotProject;
import net.sf.iwant.eclipsesettings.OrgEclipseJdtCorePrefs;
import net.sf.iwant.eclipsesettings.OrgEclipseJdtUiPrefs;

public class EclipseSettings implements SideEffect {

	private final String name;
	private final SortedSet<JavaModule> javaModules;

	private EclipseSettings(String name, SortedSet<JavaModule> javaModules) {
		this.name = name;
		this.javaModules = javaModules;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public void mutate(SideEffectContext ctx) {
		try {
			generateEclipseSettings(ctx);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Eclipse settings generation failed.", e);
		}
	}

	private void generateEclipseSettings(SideEffectContext ctx)
			throws IOException {
		for (JavaModule module : javaModules) {
			DotProject dotProject = DotProject.named(module.name()).end();
			DotClasspathSpex dotClasspath = DotClasspath.with().src(
					module.mainJava());
			for (JavaModule dep : module.mainDeps()) {
				if (dep.isExplicit()) {
					dotClasspath = dotClasspath.srcDep(dep.name());
				} else {
					dotClasspath = dotClasspath.binDep(ctx
							.targetEvaluationContext()
							.cached(dep.mainClasses()).getAbsolutePath());
				}
			}

			File moduleRoot = new File(ctx.wsRoot(),
					module.locationUnderWsRoot());

			new FileWriter(new File(moduleRoot, ".project")).append(
					dotProject.asFileContent()).close();
			new FileWriter(new File(moduleRoot, ".classpath")).append(
					dotClasspath.end().asFileContent()).close();
			new File(moduleRoot, ".settings").mkdirs();
			new FileWriter(new File(moduleRoot,
					".settings/org.eclipse.jdt.core.prefs")).append(
					OrgEclipseJdtCorePrefs.withDefaultValues().asFileContent())
					.close();
			new FileWriter(new File(moduleRoot,
					".settings/org.eclipse.jdt.ui.prefs")).append(
					OrgEclipseJdtUiPrefs.withDefaultValues().asFileContent())
					.close();
		}
	}

	public static EclipseSettingsSpex with() {
		return new EclipseSettingsSpex();
	}

	public static class EclipseSettingsSpex {

		private String name;

		private final SortedSet<JavaModule> javaModules = new TreeSet<JavaModule>();

		public EclipseSettingsSpex name(String name) {
			this.name = name;
			return this;
		}

		public EclipseSettingsSpex modules(JavaModule... modules) {
			javaModules.addAll(Arrays.asList(modules));
			return this;
		}

		public EclipseSettings end() {
			return new EclipseSettings(name, javaModules);
		}

	}

}
