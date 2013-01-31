package net.sf.iwant.api;

import java.io.File;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.iwant.eclipsesettings.DotClasspath;
import net.sf.iwant.eclipsesettings.DotClasspath.DotClasspathSpex;
import net.sf.iwant.eclipsesettings.DotProject;
import net.sf.iwant.eclipsesettings.OrgEclipseJdtCorePrefs;
import net.sf.iwant.eclipsesettings.OrgEclipseJdtUiPrefs;
import net.sf.iwant.entry.Iwant;

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

	private void generateEclipseSettings(SideEffectContext ctx) {
		for (JavaModule module : javaModules) {
			DotProject dotProject = DotProject.named(module.name()).end();

			DotClasspathSpex dotClasspath = DotClasspath.with();
			if (module.mainJava() != null) {
				dotClasspath = dotClasspath.src(module.mainJava());
			}
			if (module.testJava() != null) {
				dotClasspath = dotClasspath.src(module.testJava());
			}
			for (JavaModule dep : module.mainDeps()) {
				dotClasspath = dotClasspathWithDep(ctx, dotClasspath, dep);
			}
			for (JavaModule dep : module.testDeps()) {
				dotClasspath = dotClasspathWithDep(ctx, dotClasspath, dep);
			}

			File moduleRoot = new File(ctx.wsRoot(),
					module.locationUnderWsRoot());

			Iwant.newTextFile(new File(moduleRoot, ".project"),
					dotProject.asFileContent());
			Iwant.newTextFile(new File(moduleRoot, ".classpath"), dotClasspath
					.end().asFileContent());
			new File(moduleRoot, ".settings").mkdirs();
			Iwant.newTextFile(new File(moduleRoot,
					".settings/org.eclipse.jdt.core.prefs"),
					OrgEclipseJdtCorePrefs.withDefaultValues().asFileContent());
			Iwant.newTextFile(new File(moduleRoot,
					".settings/org.eclipse.jdt.ui.prefs"), OrgEclipseJdtUiPrefs
					.withDefaultValues().asFileContent());
		}
	}

	private static DotClasspathSpex dotClasspathWithDep(SideEffectContext ctx,
			DotClasspathSpex dotClasspath, JavaModule dep) {
		if (dep.isExplicit()) {
			return dotClasspath.srcDep(dep.name());
		} else {
			return dotClasspath.binDep(ctx.targetEvaluationContext()
					.cached(dep.mainClasses()).getAbsolutePath());
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
