package net.sf.iwant.api;

import java.io.File;
import java.io.FileWriter;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.iwant.eclipsesettings.DotClasspath;
import net.sf.iwant.eclipsesettings.DotProject;
import net.sf.iwant.eclipsesettings.OrgEclipseJdtCorePrefs;
import net.sf.iwant.eclipsesettings.OrgEclipseJdtUiPrefs;
import net.sf.iwant.entry3.FileUtil;
import net.sf.iwant.entry3.WorkspaceEclipseProject;

public class EclipseSettings implements SideEffect {

	private final String name;

	private EclipseSettings(String name) {
		this.name = name;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public void mutate(SideEffectContext ctx) {
		generateEclipseSettings(ctx);
	}

	private static void generateEclipseSettings(SideEffectContext ctx) {
		WsInfo wsInfo = ctx.wsInfo();
		File wsRoot = ctx.wsRoot();
		File wsdefSrc = ctx.wsdDefClassesTarget().srcDir()
				.cachedAt(ctx.targetEvaluationContext());
		try {
			String relativeWsdefdefSrc = FileUtil
					.relativePathOfFileUnderParent(wsInfo.wsdefdefSrc(), wsRoot);
			String relativeWsdef = FileUtil.relativePathOfFileUnderParent(
					wsdefSrc, wsRoot);
			SortedSet<String> relativeWsdefDeps = new TreeSet<String>();
			for (Path wsdefDep : ctx.wsdDefClassesTarget().classLocations()) {
				File wsdefDepFile = wsdefDep.cachedAt(ctx
						.targetEvaluationContext());
				String asString = wsdefDepFile.getAbsolutePath();
				if (asString.startsWith(wsRoot.getAbsolutePath())) {
					asString = FileUtil.relativePathOfFileUnderParent(
							wsdefDepFile, wsRoot);
				}
				relativeWsdefDeps.add(asString);
			}

			WorkspaceEclipseProject proj = new WorkspaceEclipseProject(
					wsInfo.wsName(), relativeWsdefdefSrc, relativeWsdef,
					relativeWsdefDeps);
			DotProject dotProject = proj.dotProject();
			new FileWriter(new File(wsRoot, ".project")).append(
					dotProject.asFileContent()).close();

			DotClasspath dotClasspath = proj.dotClasspath();
			new FileWriter(new File(wsRoot, ".classpath")).append(
					dotClasspath.asFileContent()).close();

			new File(wsRoot, ".settings").mkdirs();

			new FileWriter(new File(wsRoot,
					".settings/org.eclipse.jdt.core.prefs")).append(
					OrgEclipseJdtCorePrefs.withDefaultValues().asFileContent())
					.close();

			new FileWriter(new File(wsRoot,
					".settings/org.eclipse.jdt.ui.prefs")).append(
					OrgEclipseJdtUiPrefs.withDefaultValues().asFileContent())
					.close();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Eclipse settings generation failed.", e);
		}
	}

	public static EclipseSettingsSpex with() {
		return new EclipseSettingsSpex();
	}

	public static class EclipseSettingsSpex {

		private String name;

		public EclipseSettingsSpex name(String name) {
			this.name = name;
			return this;
		}

		public EclipseSettings end() {
			return new EclipseSettings(name);
		}

	}

}
