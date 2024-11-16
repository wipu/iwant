package org.fluentjava.iwant.wsdef;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.fluentjava.iwant.api.core.Concatenated;
import org.fluentjava.iwant.api.core.Concatenated.ConcatenatedBuilder;
import org.fluentjava.iwant.api.core.ScriptGenerated;
import org.fluentjava.iwant.api.javamodules.JavaSrcModule;
import org.fluentjava.iwant.api.model.SideEffect;
import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.api.wsdef.SideEffectDefinitionContext;
import org.fluentjava.iwant.api.wsdef.TargetDefinitionContext;
import org.fluentjava.iwant.api.wsdef.Workspace;
import org.fluentjava.iwant.api.wsdef.WorkspaceContext;
import org.fluentjava.iwant.core.download.Downloaded;
import org.fluentjava.iwant.core.download.TestedIwantDependencies;
import org.fluentjava.iwant.eclipsesettings.EclipseSettings;
import org.fluentjava.iwant.plugin.findbugs.FindbugsDistribution;
import org.fluentjava.iwant.plugin.findbugs.FindbugsOutputFormat;
import org.fluentjava.iwant.plugin.findbugs.FindbugsReport;
import org.fluentjava.iwant.plugin.jacoco.JacocoDistribution;
import org.fluentjava.iwant.plugin.jacoco.JacocoTargetsOfJavaModules;

public class WorkspaceForIwant implements Workspace {

	private final FindbugsDistribution findbugs = FindbugsDistribution._4_7_3;

	private static final Target copyOfLocalIwantWs = new CopyOfLocalIwantWsForTutorial();

	private final IwantModules modules;

	public WorkspaceForIwant(WorkspaceContext ctx) {
		this.modules = new IwantModules(ctx);
	}

	@Override
	public List<? extends Target> targets(TargetDefinitionContext ctx) {
		return Arrays.asList(copyOfLocalIwantWs, faviconIco(), findbugsReport(),
				jacocoReport(), localWebsite(), logoGif(), remoteWebsite());
	}

	@Override
	public List<? extends SideEffect> sideEffects(
			SideEffectDefinitionContext ctx) {
		return Arrays.asList(EclipseSettings.with().name("eclipse-settings")
				.modules(ctx.wsdefdefJavaModule(), ctx.wsdefJavaModule())
				.modules(modules.allSrcModules()).end());
	}

	// the targets

	private static JacocoDistribution jacoco() {
		return JacocoDistribution.newestTestedVersion();
	}

	private Target jacocoReport() {
		return JacocoTargetsOfJavaModules.with().jacoco(jacoco())
				.antJars(TestedIwantDependencies.antJar(),
						TestedIwantDependencies.antLauncherJar())
				.modules(modules.modulesForCoverage()).end()
				.jacocoReport("jacoco-report");

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
		// TODO why and since when does asy add another name extension:
		sh.string("mv $DEST.eps $DEST\n");
		return sh.end();
	}

	private static String imagemagickCommit() {
		return "72fb5349c1489c145a56f49e7f23277fd82af72d";
	}

	private static Target imagemagickZip() {
		String commit = imagemagickCommit();
		String url = "https://github.com/ImageMagick/ImageMagick/archive/"
				+ commit + ".zip";
		return Downloaded.withName("imagemagick-" + commit + ".zip").url(url)
				.noCheck();
	}

	/**
	 * This is needed because newer versions refuse to convert EPS for security
	 * reasons. Here we trust the EPS since we create it ourselves.
	 */
	private static Target imagemagick() {
		Target zip = imagemagickZip();
		String name = zip + ".installed";
		ConcatenatedBuilder sh = Concatenated.named(name + ".sh");

		sh.string("#!/bin/bash -eux\n");
		sh.string("DEST=$1\n");
		sh.string("unzip -q '").unixPathTo(zip).string("'\n");
		sh.string("cd ImageMagick-" + imagemagickCommit() + "\n");
		sh.string("./configure --prefix=\"$DEST\"\n");
		sh.string("make -j$(nproc)\n");
		sh.string("make install\n");

		return ScriptGenerated.named(name).byScript(sh.end());
	}

	private static Target logoGifSh() {
		ConcatenatedBuilder sh = Concatenated.named("iwant-logo.gif.sh");
		sh.string("#!/bin/bash -eux\n");
		sh.string("DEST=$1\n");
		sh.unixPathTo(imagemagick()).string("/bin/convert '");
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
		// TODO why and since when does asy add another name extension:
		sh.string("mv temp.eps.eps temp.eps\n");
		sh.unixPathTo(imagemagick())
				.string("/bin/convert temp.eps -resize 32x32 temp.png\n");
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
