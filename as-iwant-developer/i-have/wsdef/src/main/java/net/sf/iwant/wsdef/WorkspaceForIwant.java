package net.sf.iwant.wsdef;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.sf.iwant.api.core.Concatenated;
import net.sf.iwant.api.core.Concatenated.ConcatenatedBuilder;
import net.sf.iwant.api.core.ScriptGenerated;
import net.sf.iwant.api.javamodules.JavaSrcModule;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.SideEffect;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.wsdef.SideEffectDefinitionContext;
import net.sf.iwant.api.wsdef.TargetDefinitionContext;
import net.sf.iwant.api.wsdef.Workspace;
import net.sf.iwant.core.download.FromRepository;
import net.sf.iwant.core.download.TestedIwantDependencies;
import net.sf.iwant.deprecated.emma.EmmaTargetsOfJavaModules;
import net.sf.iwant.eclipsesettings.EclipseSettings;
import net.sf.iwant.plugin.findbugs.FindbugsDistribution;
import net.sf.iwant.plugin.findbugs.FindbugsOutputFormat;
import net.sf.iwant.plugin.findbugs.FindbugsReport;
import net.sf.iwant.plugin.jacoco.JacocoDistribution;
import net.sf.iwant.plugin.jacoco.JacocoTargetsOfJavaModules;

public class WorkspaceForIwant implements Workspace {

	private final FindbugsDistribution findbugs = FindbugsDistribution
			.ofVersion("3.0.0");

	private static final Target copyOfLocalIwantWs = new CopyOfLocalIwantWsForTutorial();

	private final Path asm501Jar = FromRepository.repo1MavenOrg()
			.group("org/ow2/asm").name("asm-all").version("5.0.1");

	private final IwantModules modules = new IwantModules();

	@Override
	public List<? extends Target> targets(TargetDefinitionContext ctx) {
		return Arrays.asList(copyOfLocalIwantWs, emmaCoverageReport(),
				faviconIco(), findbugsReport(), jacocoReport(), localWebsite(),
				logoGif(), remoteWebsite());
	}

	@Override
	public List<? extends SideEffect> sideEffects(
			SideEffectDefinitionContext ctx) {
		return Arrays.asList(EclipseSettings.with().name("eclipse-settings")
				.modules(ctx.wsdefdefJavaModule(), ctx.wsdefJavaModule())
				.modules(modules.allSrcModules()).end());
	}

	// the targets

	private static Path emma() {
		return TestedIwantDependencies.emma();
	}

	private static JacocoDistribution jacoco() {
		return JacocoDistribution.newestTestedVersion();
	}

	private Target jacocoReport() {
		return JacocoTargetsOfJavaModules.with()
				.jacocoWithDeps(jacoco(), asm501Jar)
				.antJars(TestedIwantDependencies.antJar(),
						TestedIwantDependencies.antLauncherJar())
				.modules(modules.modulesForCoverage()).end()
				.jacocoReport("jacoco-report");

	}

	private Target emmaCoverageReport() {
		EmmaTargetsOfJavaModules emmaTargets = EmmaTargetsOfJavaModules.with()
				.antJars(TestedIwantDependencies.antJar(),
						TestedIwantDependencies.antLauncherJar())
				.emma(emma()).modules(modules.allSrcModules())
				.butNotInstrumenting(modules.iwantMockWsroot)
				.filter(emmaFilter()).end();
		return emmaTargets.emmaReport("emma-coverage");

	}

	private static Path emmaFilter() {
		ConcatenatedBuilder filter = Concatenated.named("emma-filter");
		// only used in the tutorial, not "real" code:
		filter.string("-com.example.*\n");
		return filter.end();
	}

	private Target findbugsReport() {
		return findbugsReport("findbugs-report", modules.allSrcModules(),
				FindbugsOutputFormat.HTML);

	}

	private FindbugsReport findbugsReport(String name,
			Collection<JavaSrcModule> modules,
			FindbugsOutputFormat outputFormat) {
		return FindbugsReport.with().name(name).outputFormat(outputFormat)
				.using(findbugs, TestedIwantDependencies.antJar(),
						TestedIwantDependencies.antLauncherJar())
				.modulesToAnalyze(modules).end();
	}

	private static Target localTutorial() {
		return Tutorial.local(copyOfLocalIwantWs);
	}

	private static Target localWebsite() {
		return new Website("local-website", localTutorial(), logoGif(),
				faviconIco());
	}

	private static Target remoteTutorial() {
		return Tutorial.remote();
	}

	private static Target remoteWebsite() {
		return new Website("remote-website", remoteTutorial(), logoGif(),
				faviconIco());
	}

	private static Source logoAsy() {
		return Source
				.underWsroot("private/iwant-docs/src/main/asy/iwant-logo.asy");
	}

	private static Target logoEpsSh() {
		ConcatenatedBuilder sh = Concatenated.named("iwant-logo.eps.sh");
		sh.string("#!/bin/bash -eux\n");
		sh.string("DEST=$1\n");
		sh.string("asy -o \"$DEST\" '");
		sh.unixPathTo(logoAsy());
		sh.string("'\n");
		return sh.end();
	}

	private static Target logoGifSh() {
		ConcatenatedBuilder sh = Concatenated.named("iwant-logo.gif.sh");
		sh.string("#!/bin/bash -eux\n");
		sh.string("DEST=$1\n");
		sh.string("convert '");
		sh.unixPathTo(logoEps());
		sh.string("' -resize '50%' \"$DEST\"\n");
		return sh.end();
	}

	private static Target faviconIcoSh() {
		ConcatenatedBuilder sh = Concatenated.named("favicon.ico.sh");
		sh.string("#!/bin/bash -eux\n");
		sh.string("DEST=$1\n");
		sh.string("cat '").unixPathTo(logoAsy())
				.string("' | sed 's/^drawFull();/drawStar();/' > temp.asy\n");
		sh.string("asy -o temp.eps temp.asy\n");
		sh.string("convert temp.eps -resize 32x32 temp.png\n");
		sh.string("icotool -c -o \"$DEST\" temp.png\n");
		return sh.end();
	}

	private static Target logoEps() {
		return ScriptGenerated.named("iwant-logo.eps").byScript(logoEpsSh());
	}

	private static Target logoGif() {
		return ScriptGenerated.named("iwant-logo.gif").byScript(logoGifSh());
	}

	private static Target faviconIco() {
		return ScriptGenerated.named("favicon.ico").byScript(faviconIcoSh());
	}

}
