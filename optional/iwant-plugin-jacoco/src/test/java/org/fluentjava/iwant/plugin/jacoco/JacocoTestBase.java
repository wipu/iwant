package org.fluentjava.iwant.plugin.jacoco;

import java.io.File;

import org.fluentjava.iwant.api.javamodules.JavaBinModule;
import org.fluentjava.iwant.api.javamodules.JavaClasses;
import org.fluentjava.iwant.api.javamodules.JavaClassesAndSources;
import org.fluentjava.iwant.api.javamodules.JavaModule;
import org.fluentjava.iwant.api.model.ExternalSource;
import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.apimocks.IwantTestCase;
import org.fluentjava.iwant.core.download.FromRepository;
import org.fluentjava.iwant.core.download.TestedIwantDependencies;
import org.fluentjava.iwant.embedded.AsEmbeddedIwantUser;
import org.fluentjava.iwant.entry.Iwant;

public abstract class JacocoTestBase extends IwantTestCase {

	@Override
	protected void moreSetUp() throws Exception {
		caches.cachesUrlAt(jacoco().zip().url(), cachedJacocoZip());
		jacoco().path(ctx);
	}

	private Path downloaded(Path downloaded) {
		return new ExternalSource(AsEmbeddedIwantUser.with().workspaceAt(wsRoot)
				.cacheAt(cached).iwant().target((Target) downloaded).asPath());
	}

	protected static JacocoDistribution jacoco() {
		return JacocoDistribution.newestTestedVersion();
	}

	private static File cachedJacocoZip() {
		return Iwant.usingRealNetwork().downloaded(jacoco().zip().url());
	}

	protected Path asm() {
		return downloaded(FromRepository.repo1MavenOrg().group("org/ow2/asm")
				.name("asm").version("6.2.1").jar());
	}

	protected Path antJar() {
		return downloaded(TestedIwantDependencies.antJar());
	}

	protected Path antLauncherJar() {
		return downloaded(TestedIwantDependencies.antLauncherJar());
	}

	protected JavaModule junit() {
		return JavaBinModule
				.providing(downloaded(TestedIwantDependencies.junit())).end();
	}

	protected JavaClassesAndSources newJavaClassesAndSources(String name,
			String className, String... codeLinesForMain) throws Exception {
		String srcDirString = name + "-src";
		File srcDir = new File(wsRoot, srcDirString);

		StringBuilder code = new StringBuilder();
		code.append("package " + name + ";\n");
		code.append("public class " + className + " {\n");
		code.append(
				"  public static void main(String[] args) throws Throwable {\n");
		for (String codeLine : codeLinesForMain) {
			code.append(codeLine).append("\n");
		}
		code.append("  }\n");
		code.append("}\n");

		Iwant.newTextFile(new File(srcDir, className + ".java"),
				code.toString());
		JavaClasses classes = JavaClasses.with().name(name + "-classes")
				.srcDirs(Source.underWsroot(srcDirString)).classLocations()
				.end();
		classes.path(ctx);
		return new JavaClassesAndSources(classes,
				Source.underWsroot(srcDirString));
	}

}
