package net.sf.iwant.eclipsesettings;

import java.util.LinkedHashSet;
import java.util.Set;

import net.sf.iwant.api.JavaBinModule;
import net.sf.iwant.api.JavaModule;
import net.sf.iwant.api.JavaSrcModule;
import net.sf.iwant.api.TargetEvaluationContext;
import net.sf.iwant.eclipsesettings.DotClasspath.DotClasspathSpex;

public class EclipseProject {

	private final JavaSrcModule module;
	private final TargetEvaluationContext ctx;

	public EclipseProject(JavaSrcModule module, TargetEvaluationContext ctx) {
		this.module = module;
		this.ctx = ctx;
	}

	public DotProject eclipseDotProject() {
		return DotProject.named(module.name())
				.hasExternalBuilder(hasExternalBuilder()).end();
	}

	public DotClasspath eclipseDotClasspath() {
		DotClasspathSpex dcp = DotClasspath.with();
		dcp = optionalSrc(dcp, module.mainJava());
		dcp = optionalSrc(dcp, module.mainResources());
		dcp = optionalSrc(dcp, module.testJava());
		dcp = optionalSrc(dcp, module.testResources());

		Set<JavaModule> allDeps = new LinkedHashSet<JavaModule>();
		allDeps.addAll(module.mainDeps());
		allDeps.addAll(module.testDeps());
		for (JavaModule dep : allDeps) {
			dcp = dep(dcp, dep);
		}

		if (hasExternalBuilder()) {
			dcp = dcp.exportedClasses("eclipse-ant-generated/"
					+ module.generatedClasses().name(),
					"eclipse-ant-generated/" + module.generatedSrc().name());
		}

		return dcp.end();
	}

	public ProjectExternalBuilderLaunch externalBuilderLaunch() {
		return hasExternalBuilder() ? new ProjectExternalBuilderLaunch(
				module.name(), module.generatedSrc(), "eclipse-ant-generated")
				: null;
	}

	public EclipseAntScript eclipseAntScript(String asSomeone) {
		return hasExternalBuilder() ? new EclipseAntScript(module.name(),
				module.relativeWsRoot(), module.generatedClasses().name(),
				module.generatedSrc().name(), asSomeone) : null;
	}

	public OrgEclipseJdtCorePrefs orgEclipseJdtCorePrefs() {
		return new OrgEclipseJdtCorePrefs(module.codeStylePolicy());
	}

	private boolean hasExternalBuilder() {
		return module.generatedClasses() != null;
	}

	private DotClasspathSpex dep(DotClasspathSpex dcp, JavaModule dep) {
		if (dep instanceof JavaSrcModule) {
			return dcp.srcDep(dep.name());
		}
		if (dep instanceof JavaBinModule) {
			JavaBinModule binDep = (JavaBinModule) dep;
			String src = binDep.eclipseSourceReference(ctx);
			if (src == null) {
				return dcp.binDep(binDep.eclipseBinaryReference(ctx));
			} else {
				return dcp.binDep(binDep.eclipseBinaryReference(ctx), src);
			}
		}
		throw new UnsupportedOperationException("Don't know how to handle "
				+ dep.getClass());
	}

	private static DotClasspathSpex optionalSrc(DotClasspathSpex dcp,
			String maybeSrc) {
		if (maybeSrc != null) {
			return dcp.src(maybeSrc);
		}
		return dcp;
	}

	public OrgEclipseJdtUiPrefs orgEclipseJdtUiPrefs() {
		return new OrgEclipseJdtUiPrefs();
	}

}