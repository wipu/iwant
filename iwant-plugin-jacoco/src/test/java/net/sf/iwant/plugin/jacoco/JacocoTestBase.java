package net.sf.iwant.plugin.jacoco;

import java.io.File;
import java.io.IOException;

import net.sf.iwant.api.javamodules.JavaBinModule;
import net.sf.iwant.api.javamodules.JavaClasses;
import net.sf.iwant.api.javamodules.JavaClassesAndSources;
import net.sf.iwant.api.javamodules.JavaModule;
import net.sf.iwant.api.model.ExternalSource;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.apimocks.IwantTestCase;
import net.sf.iwant.core.download.FromRepository;
import net.sf.iwant.core.download.TestedIwantDependencies;
import net.sf.iwant.embedded.AsEmbeddedIwantUser;
import net.sf.iwant.entry.Iwant;

public abstract class JacocoTestBase extends IwantTestCase {

	@Override
	protected void moreSetUp() throws Exception {
		caches.cachesUrlAt(jacoco().zip().url(), cachedJacocoZip());
		jacoco().path(ctx);
	}

	private Path downloaded(Path downloaded) throws IOException {
		return new ExternalSource(AsEmbeddedIwantUser.with()
				.workspaceAt(wsRoot).cacheAt(cacheDir).iwant()
				.target((Target) downloaded).asPath());
	}

	protected static JacocoDistribution jacoco() {
		return JacocoDistribution.newestTestedVersion();
	}

	private static File cachedJacocoZip() {
		return Iwant.usingRealNetwork().downloaded(jacoco().zip().url());
	}

	protected Path asm() throws IOException {
		return downloaded(FromRepository.repo1MavenOrg().group("org/ow2/asm")
				.name("asm-all").version("5.0.1"));
	}

	protected Path antJar() throws IOException {
		return downloaded(TestedIwantDependencies.antJar());
	}

	protected Path antLauncherJar() throws IOException {
		return downloaded(TestedIwantDependencies.antLauncherJar());
	}

	protected JavaModule junit() throws IOException {
		return JavaBinModule.providing(
				downloaded(TestedIwantDependencies.junit())).end();
	}

	protected JavaClassesAndSources newJavaClassesAndSources(String name,
			String className, String... codeLinesForMain) throws Exception {
		String srcDirString = name + "-src";
		File srcDir = new File(wsRoot, srcDirString);

		StringBuilder code = new StringBuilder();
		code.append("package " + name + ";\n");
		code.append("public class " + className + " {\n");
		code.append("  public static void main(String[] args) throws Throwable {\n");
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
