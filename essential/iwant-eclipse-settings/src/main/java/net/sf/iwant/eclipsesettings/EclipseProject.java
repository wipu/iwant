package net.sf.iwant.eclipsesettings;

import java.util.LinkedHashSet;
import java.util.Set;

import net.sf.iwant.api.javamodules.JavaBinModule;
import net.sf.iwant.api.javamodules.JavaModule;
import net.sf.iwant.api.javamodules.JavaSrcModule;
import net.sf.iwant.api.model.TargetEvaluationContext;
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
				.hasExternalBuilder(hasExternalBuilder())
				.hasScalaSupport(hasScalaSupport()).end();
	}

	private boolean hasScalaSupport() {
		return module.scalaVersion() != null;
	}

	public DotClasspath eclipseDotClasspath() {
		DotClasspathSpex dcp = DotClasspath.with();
		for (String mainJava : module.mainJavas()) {
			dcp = optionalSrc(dcp, mainJava);
		}
		for (String res : module.mainResources()) {
			dcp = optionalSrc(dcp, res);
		}
		for (String testJava : module.testJavas()) {
			dcp = optionalSrc(dcp, testJava);
		}
		for (String res : module.testResources()) {
			dcp = optionalSrc(dcp, res);
		}

		for (JavaModule dep : dependenciesOf(module)) {
			dcp = dep(dcp, dep);
		}

		if (hasExternalBuilder()) {
			dcp = dcp.exportedClasses(
					"eclipse-ant-generated/" + module.generatedClasses().name(),
					"eclipse-ant-generated/" + module.generatedSrc().name());
		}

		return dcp.end();
	}

	static Set<JavaModule> dependenciesOf(JavaModule module) {
		Set<JavaModule> deps = new LinkedHashSet<>();
		deps.addAll(module.effectivePathForTestRuntime());
		deps.remove(module);
		return deps;
	}

	public ProjectExternalBuilderLaunch externalBuilderLaunch() {
		return hasExternalBuilder() ? new ProjectExternalBuilderLaunch(
				module.name(), module.generatedSrc(),
				module.generatorSourcesToFollow(), "eclipse-ant-generated")
				: null;
	}

	public EclipseAntScript eclipseAntScript(String asSomeone) {
		return hasExternalBuilder() ? new EclipseAntScript(module.name(),
				module.relativeWsRoot(), module.wsrootRelativeParentDir(),
				module.generatedClasses().name(), module.generatedSrc().name(),
				asSomeone) : null;
	}

	public OrgEclipseJdtCorePrefs orgEclipseJdtCorePrefs() {
		return new OrgEclipseJdtCorePrefs(module.codeStylePolicy(),
				module.codeFormatterPolicy(), module.javaCompliance());
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
		throw new UnsupportedOperationException(
				"Don't know how to handle " + dep.getClass());
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
