package net.sf.iwant.wsdef;

import net.sf.iwant.core.Concatenated;
import net.sf.iwant.core.Concatenated.ConcatenatedBuilder;
import net.sf.iwant.core.ContainerPath;
import net.sf.iwant.core.EclipseProject;
import net.sf.iwant.core.EclipseProjects;
import net.sf.iwant.core.JavaClasses;
import net.sf.iwant.core.Locations;
import net.sf.iwant.core.Path;
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

		public Target<ScriptGeneratedContent> antBootstrappingTutorial() {
			return target("antBootstrappingTutorial")
					.content(
							ScriptGeneratedContent
									.of(antBootstrappingTutorialScript()))
					.end();
		}

		public Target<Concatenated> localAntBootstrappingTutorialScript() {
			return bootstrappingTutorialScript(
					"localAntBootstrappingTutorialScript",
					bootstrappingWithAntSh(), true);
		}

		public Target<Concatenated> antBootstrappingTutorialScript() {
			return bootstrappingTutorialScript(
					"antBootstrappingTutorialScript", bootstrappingWithAntSh(),
					false);
		}

		public Target<ScriptGeneratedContent> localBashBootstrappingTutorial() {
			return target("localBashBootstrappingTutorial").content(
					ScriptGeneratedContent
							.of(localBashBootstrappingTutorialScript())).end();
		}

		public Target<ScriptGeneratedContent> bashBootstrappingTutorial() {
			return target("bashBootstrappingTutorial").content(
					ScriptGeneratedContent
							.of(bashBootstrappingTutorialScript())).end();
		}

		public Target<Concatenated> localBashBootstrappingTutorialScript() {
			return bootstrappingTutorialScript(
					"localBashBootstrappingTutorialScript",
					bootstrappingWithBashSh(), true);
		}

		public Target<Concatenated> bashBootstrappingTutorialScript() {
			return bootstrappingTutorialScript(
					"bashBootstrappingTutorialScript",
					bootstrappingWithBashSh(), false);
		}

		public Target<ScriptGeneratedContent> localTutorial() {
			return target("localTutorial").content(
					ScriptGeneratedContent.of(localTutorialScript())).end();
		}

		public Target<Concatenated> localTutorialScript() {
			return bootstrappingTutorialScript("localTutorialScript",
					articleSh(), true);
		}

		public Target<ScriptGeneratedContent> localWebsite() {
			return target("localWebsite").content(
					ScriptGeneratedContent.of(localWebsiteScript())).end();
		}

		public Target<Concatenated> localWebsiteScript() {
			return websiteScript("localWebsiteScript",
					localAntBootstrappingTutorial(),
					localBashBootstrappingTutorial(), localTutorial());
		}

		private Source descriptSh() {
			return source("iwant-lib-descript/descript.sh");
		}

		private Source articleSh() {
			return source("iwant-docs/src/main/descript/tutorial/article.sh");
		}

		private Source bootstrappingWithAntSh() {
			return source("iwant-docs/src/main/descript/tutorial/bootstrapping-with-ant.sh");
		}

		private Source bootstrappingWithBashSh() {
			return source("iwant-docs/src/main/descript/tutorial/bootstrapping-with-bash.sh");
		}

		private Target<Concatenated> bootstrappingTutorialScript(
				String targetName, Source tutorialSh, boolean isLocal) {
			ConcatenatedBuilder b = Concatenated.from();
			b.string("#!/bin/bash\n");
			b.string("set -eu\n");
			b.string("DEST=$1\n");
			if (isLocal) {
				b.string("LOCAL_IWANT_WSROOT=\"" + locations.wsRoot() + "\" ");
			}
			b.string("bash \"").pathTo(descriptSh()).string("\"");
			b.string(" \"").pathTo(tutorialSh).string("\"");
			b.string(" \"$DEST\" true\n");
			return target(targetName).content(b.end()).end();
		}

		private Source websiteHtml() {
			return source("iwant-docs/src/main/html/website");
		}

		private Target<Concatenated> websiteScript(String targetName,
				Path antTutorial, Path bashTutorial, Path tutorial) {
			ConcatenatedBuilder b = Concatenated.from();
			b.string("#!/bin/bash\n");
			b.string("set -eu\n");
			b.string("DEST=$1\n");
			b.string("mkdir \"$DEST\"\n");
			b.string("cp '").pathTo(websiteHtml()).string("'/* \"$DEST\"/\n");
			b.string("cp '").pathTo(antTutorial)
					.string("' \"$DEST\"/bootstrapping-with-ant.html\n");
			b.string("cp '").pathTo(bashTutorial)
					.string("' \"$DEST\"/bootstrapping-with-bash.html\n");
			b.string("cp '").pathTo(tutorial)
					.string("' \"$DEST\"/tutorial.html\n");
			return target(targetName).content(b.end()).end();
		}

		public EclipseProject wsdefEclipseProject() {
			return EclipseProject.with().name("as-$WSNAME-developer")
					.src("i-have/wsdef").libs(builtin().all()).end();
		}

	}

}
