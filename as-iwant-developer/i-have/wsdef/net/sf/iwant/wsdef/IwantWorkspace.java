package net.sf.iwant.wsdef;

import net.sf.iwant.core.Concatenated;
import net.sf.iwant.core.ContainerPath;
import net.sf.iwant.core.EclipseProject;
import net.sf.iwant.core.EclipseProjects;
import net.sf.iwant.core.JavaClasses;
import net.sf.iwant.core.Locations;
import net.sf.iwant.core.RootPath;
import net.sf.iwant.core.ScriptGeneratedContent;
import net.sf.iwant.core.Source;
import net.sf.iwant.core.Target;
import net.sf.iwant.core.WorkspaceDefinition;

public class IwantWorkspace implements WorkspaceDefinition {

	@Override
	public ContainerPath wsRoot(Locations locations) {
		return new Root(locations);
	}

	public static class Root extends RootPath {

		public Root(Locations locations) {
			super(locations);
		}

		public Target<EclipseProjects> eclipseProjects() {
			return target("eclipse-projects").content(
					EclipseProjects.with().project(wsdefEclipseProject()))
					.end();
		}

		public Target<JavaClasses> iwantCoreMainClasses() {
			return target("iwantCoreMainClasses").content(
					JavaClasses.compiledFrom(iwantCoreMainSrc()).using(
							builtin().ant171classes())).end();
		}

		private Source iwantCoreMainSrc() {
			return source("iwant-core/src/main/java");
		}

		public Target<ScriptGeneratedContent> localAntBootstrappingTutorial() {
			return target("localAntBootstrappingTutorial").content(
					ScriptGeneratedContent
							.of(localAntBootstrappingTutorialScript())).end();
		}

		public Target<Concatenated> localAntBootstrappingTutorialScript() {
			return bootstrappingTutorialScript(
					"localAntBootstrappingTutorialScript",
					"bootstrapping-with-ant.sh");
		}

		public Target<ScriptGeneratedContent> localBashBootstrappingTutorial() {
			return target("localBashBootstrappingTutorial").content(
					ScriptGeneratedContent
							.of(localBashBootstrappingTutorialScript())).end();
		}

		public Target<Concatenated> localBashBootstrappingTutorialScript() {
			return bootstrappingTutorialScript(
					"localBashBootstrappingTutorialScript",
					"bootstrapping-with-bash.sh");
		}

		public Target<ScriptGeneratedContent> localTutorial() {
			return target("localTutorial").content(
					ScriptGeneratedContent.of(localTutorialScript())).end();
		}

		public Target<Concatenated> localTutorialScript() {
			return bootstrappingTutorialScript("localTutorialScript",
					"article.sh");
		}

		private Target<Concatenated> bootstrappingTutorialScript(
				String targetName, String descriptFileName) {
			// TODO depend on these so we get refreshed when they have been
			// modified:
			String descriptSh = locations.wsRoot()
					+ "/iwant-lib-descript/descript.sh";
			String docSh = locations.wsRoot()
					+ "/iwant-docs/src/main/descript/tutorial/"
					+ descriptFileName;

			StringBuilder b = new StringBuilder();
			b.append("#!/bin/bash\n");
			b.append("set -eu\n");
			b.append("DEST=$1\n");
			b.append("LOCAL_IWANT_WSROOT=\"" + locations.wsRoot()
					+ "\" bash \"" + descriptSh + "\"");
			b.append(" \"" + docSh + "\"");
			b.append(" \"$DEST\" true\n");
			return target(targetName).content(
					Concatenated.from().string(b.toString()).end()).end();
		}

		public EclipseProject wsdefEclipseProject() {
			return EclipseProject.with().name("as-$WSNAME-developer")
					.src("i-have/wsdef").libs(builtin().all()).end();
		}

	}

}
