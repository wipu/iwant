package net.sf.iwant.wsdef;

import net.sf.iwant.core.Concatenated;
import net.sf.iwant.core.Constant;
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
			// TODO depend on these so we get refreshed when they have been
			// modified:
			String descriptSh = locations.wsRoot()
					+ "/iwant-lib-descript/descript.sh";
			String docSh = locations.wsRoot()
					+ "/iwant-docs/src/main/descript/tutorial/bootstrapping-with-ant.sh";

			StringBuilder b = new StringBuilder();
			b.append("#!/bin/bash\n");
			b.append("set -eu\n");
			b.append("DEST=$1\n");
			b.append("LOCAL_IWANT=\"" + locations.wsRoot()
					+ "/iwant-iwant/iwant\" bash \"" + descriptSh + "\"");
			b.append(" \"" + docSh + "\"");
			b.append(" \"$DEST\" true\n");
			return target("localAntBootstrappingTutorialScript").content(
					Concatenated.from().string(b.toString()).end()).end();
		}

		public EclipseProject wsdefEclipseProject() {
			return EclipseProject.with().name("as-$WSNAME-developer")
					.src("i-have/wsdef").libs(builtin().all()).end();
		}

	}

}
