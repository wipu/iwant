package net.sf.iwant.eclipsesettings;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import net.sf.iwant.api.javamodules.JavaModule;
import net.sf.iwant.api.javamodules.JavaSrcModule;
import net.sf.iwant.api.model.SideEffectContext;
import net.sf.iwant.entry.Iwant;

public class EclipseSettingsWriter {

	private final Set<JavaModule> modules;
	private final SideEffectContext ctx;

	public EclipseSettingsWriter(Set<JavaModule> modules,
			SideEffectContext ctx) {
		this.modules = modules;
		this.ctx = ctx;
	}

	public static EclipseSettingsWriterSpex with() {
		return new EclipseSettingsWriterSpex();
	}

	public static class EclipseSettingsWriterSpex {

		private final Set<JavaModule> modules = new LinkedHashSet<>();
		private SideEffectContext ctx;

		public EclipseSettingsWriterSpex modules(JavaModule... modules) {
			return modules(Arrays.asList(modules));
		}

		public EclipseSettingsWriterSpex modules(
				Collection<? extends JavaModule> modules) {
			this.modules.addAll(modules);
			return this;
		}

		public EclipseSettingsWriterSpex context(SideEffectContext ctx) {
			this.ctx = ctx;
			return this;
		}

		public EclipseSettingsWriter end() {
			return new EclipseSettingsWriter(modules, ctx);
		}

	}

	public void write() {
		for (JavaModule module : modules) {
			if (module instanceof JavaSrcModule) {
				write((JavaSrcModule) module);
			}
		}
	}

	private void write(JavaSrcModule module) {
		System.err.println("(" + module.locationUnderWsRoot() + ")");
		File modDir = new File(ctx.wsRoot(), module.locationUnderWsRoot());

		EclipseProject project = new EclipseProject(module,
				ctx.targetEvaluationContext());

		DotProject dotProject = project.eclipseDotProject();
		writeFile(modDir, ".project", dotProject.asFileContent());

		DotClasspath dotClasspath = project.eclipseDotClasspath();
		writeFile(modDir, ".classpath", dotClasspath.asFileContent());

		OrgEclipseJdtCorePrefs corePrefs = project.orgEclipseJdtCorePrefs();
		writeFile(modDir, ".settings/org.eclipse.jdt.core.prefs",
				corePrefs.asFileContent());

		OrgEclipseJdtUiPrefs uiPrefs = project.orgEclipseJdtUiPrefs();
		writeFile(modDir, ".settings/org.eclipse.jdt.ui.prefs",
				uiPrefs.asFileContent());

		ProjectExternalBuilderLaunch extBuild = project.externalBuilderLaunch();
		if (extBuild != null) {
			writeFile(modDir,
					".externalToolBuilders/" + module.name() + ".launch",
					extBuild.asFileContent());
		}

		EclipseAntScript antScript = project
				.eclipseAntScript(ctx.wsInfo().relativeAsSomeone());
		if (antScript != null) {
			writeFile(modDir, "eclipse-ant-build.xml",
					antScript.asFileContent());
		}
	}

	private static void writeFile(File modDir, String fileName,
			String fileContent) {
		System.err.println("(  " + fileName + ")");
		Iwant.newTextFile(new File(modDir, fileName), fileContent);
	}

}
