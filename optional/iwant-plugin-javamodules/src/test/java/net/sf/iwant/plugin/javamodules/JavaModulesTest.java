package net.sf.iwant.plugin.javamodules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import net.sf.iwant.api.javamodules.CodeFormatterPolicy;
import net.sf.iwant.api.javamodules.JavaBinModule;
import net.sf.iwant.api.javamodules.JavaCompliance;
import net.sf.iwant.api.javamodules.JavaSrcModule;
import net.sf.iwant.api.javamodules.JavaSrcModule.IwantSrcModuleSpex;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.core.download.Downloaded;
import net.sf.iwant.plugin.ant.Jar;

public class JavaModulesTest {

	private static String descr(Path path) {
		return ((Target) path).contentDescriptor();
	}

	@Test
	public void allSrcModulesWhenThereAreNone() {
		JavaModules m = new JavaModules() {
			// no modules
		};

		assertEquals(0, m.allSrcModules().size());
	}

	@Test
	public void allSrcModulesWhenThereAre2() {
		class Mods extends JavaModules {
			// reverse order, will be sorted:
			JavaSrcModule m2 = srcModule("m2").end();
			JavaSrcModule m1 = srcModule("m1").end();
		}
		Mods m = new Mods();

		assertEquals(2, m.allSrcModules().size());
		Iterator<JavaSrcModule> it = m.allSrcModules().iterator();
		assertEquals(m.m1, it.next());
		assertEquals(m.m2, it.next());
	}

	@Test
	public void defaultLocationUnderWsroot() {
		class Mods extends JavaModules {
			JavaSrcModule mod = srcModule("mod").end();
		}
		Mods m = new Mods();

		assertEquals("mod", m.mod.locationUnderWsRoot());
	}

	@Test
	public void customParentDirInLocationUnderWsRoot() {
		class Mods extends JavaModules {
			JavaSrcModule mod = srcModule("custom-parent", "mod-under-custom")
					.end();
		}
		Mods m = new Mods();

		assertEquals("custom-parent/mod-under-custom",
				m.mod.locationUnderWsRoot());
	}

	@Test
	public void someDefaultSettings() {
		class Mods extends JavaModules {
			JavaSrcModule mod = srcModule("mod").end();
		}
		Mods m = new Mods();

		assertEquals("[]", m.mod.characteristics().toString());
		assertEquals(CodeFormatterPolicy.defaults(),
				m.mod.codeFormatterPolicy());
		assertEquals(JavaCompliance.JAVA_1_8, m.mod.javaCompliance());
		assertEquals("[src/main/java]", m.mod.mainJavas().toString());
		assertEquals("[src/test/java]", m.mod.testJavas().toString());
	}

	@Test
	public void customCommonSettings() {
		class Mods extends JavaModules {
			@Override
			protected IwantSrcModuleSpex commonSettings(IwantSrcModuleSpex m) {
				CodeFormatterPolicy codeFormatterPolicy = new CodeFormatterPolicy();
				codeFormatterPolicy.lineSplit = 200;
				return m.mainJava("src").codeFormatter(codeFormatterPolicy);
			}

			JavaSrcModule mod = srcModule("mod").end();
		}
		Mods m = new Mods();

		assertEquals(Integer.valueOf(200),
				m.mod.codeFormatterPolicy().lineSplit);
		assertEquals("[src]", m.mod.mainJavas().toString());
		assertEquals("[]", m.mod.testJavas().toString());
	}

	@Test
	public void binModuleAsDep() {
		class Mods extends JavaModules {
			JavaBinModule bin = binModule("commons-io", "commons-io", "2.4");
			JavaSrcModule src = srcModule("mod").mainDeps(bin).end();
		}
		Mods m = new Mods();

		assertEquals("[commons-io-2.4.jar]",
				m.src.mainDepsForCompilation().toString());
		Downloaded binArtifact = (Downloaded) m.bin.mainArtifact();
		assertEquals(
				"http://repo1.maven.org/maven2/commons-io/"
						+ "commons-io/2.4/commons-io-2.4.jar",
				binArtifact.url().toString());
	}

	@Test
	public void mainArtifactsOfModules() {
		class Mods extends JavaModules {
			JavaBinModule bin = binModule("commons-io", "commons-io", "2.4");
			JavaSrcModule src = srcModule("mod").mainDeps(bin).end();
			JavaSrcModule onlyTests = srcModule("only-tests").noMainJava()
					.testDeps(src).end();
		}
		Mods m = new Mods();

		List<Path> mas = JavaModules.mainArtifactsOf(m.bin, m.src, m.onlyTests);
		assertEquals(2, mas.size());
		assertEquals("net.sf.iwant.core.download.Downloaded\n" + "p:url:\n"
				+ "  http://repo1.maven.org/maven2/commons-io/commons-io/2.4/commons-io-2.4.jar\n"
				+ "p:md5:\n" + " null\n" + "", descr(mas.get(0)));
		assertEquals("net.sf.iwant.api.javamodules.JavaClasses\n"
				+ "i:srcDirs:\n" + "  mod/src/main/java\n" + "i:resourceDirs:\n"
				+ "  mod/src/main/resources\n" + "i:classLocations:\n"
				+ "  commons-io-2.4.jar\n" + "p:javacOptions:\n" + "  -Xlint\n"
				+ "  -Xlint:-serial\n" + "  -source\n" + "  1.8\n" + "  -g\n"
				+ "p:encoding:\n" + " null\n" + "", descr(mas.get(1)));
		// test only module has no main artifact
	}

	@Test
	public void mainArtifactJarsOfModulesWithoutVersionNumber() {
		class Mods extends JavaModules {
			JavaBinModule bin = binModule("commons-io", "commons-io", "2.4");
			JavaSrcModule src = srcModule("mod").mainDeps(bin).end();
			JavaSrcModule onlyTests = srcModule("only-tests").noMainJava()
					.testDeps(src).end();
		}
		Mods m = new Mods();

		List<Path> jars = JavaModules.mainArtifactJarsOf(m.bin, m.src,
				m.onlyTests);
		assertEquals(2, jars.size());
		assertEquals(descr(m.bin.mainArtifact()), descr(jars.get(0)));
		assertEquals("mod.jar", jars.get(1).name());
		assertEquals("net.sf.iwant.plugin.ant.Jar\n" + "i:classDirs:\n"
				+ "  mod-main-classes\n" + "", descr(jars.get(1)));
		// test only module has no main artifact
	}

	@Test
	public void mainArtifactJarsOfModulesWithVersionNumber() {
		class Mods extends JavaModules {
			JavaBinModule bin = binModule("commons-io", "commons-io", "2.4");
			JavaSrcModule src = srcModule("mod").mainDeps(bin).end();
			JavaSrcModule onlyTests = srcModule("only-tests").noMainJava()
					.testDeps(src).end();
		}
		Mods m = new Mods();

		List<Path> jars = JavaModules.mainArtifactJarsOf("0.9", m.bin, m.src,
				m.onlyTests);
		assertEquals(2, jars.size());
		assertEquals(descr(m.bin.mainArtifact()), descr(jars.get(0)));
		assertEquals("mod-0.9.jar", jars.get(1).name());
		assertEquals("net.sf.iwant.plugin.ant.Jar\n" + "i:classDirs:\n"
				+ "  mod-main-classes\n" + "", descr(jars.get(1)));
		// test only module has no main artifact
	}

	@Test
	public void mainArtifactJarOfModuleUsesVersionNumberIfGiven() {
		class Mods extends JavaModules {
			JavaSrcModule src = srcModule("mod").end();
		}
		Mods m = new Mods();

		assertEquals("mod-1.0.jar", JavaModules.mainJarOf("1.0", m.src).name());
		assertEquals("mod.jar", JavaModules.mainJarOf(m.src).name());
	}

	@Test
	public void testArtifactsOfModules() {
		class Mods extends JavaModules {
			JavaBinModule bin = binModule("commons-io", "commons-io", "2.4");
			JavaSrcModule src = srcModule("mod").mainDeps(bin).end();
			JavaSrcModule onlyMain = srcModule("only-main").noTestJava()
					.mainDeps(src).end();
			JavaSrcModule onlyTests = srcModule("only-tests").noMainJava()
					.testDeps(onlyMain).end();
		}
		Mods m = new Mods();

		List<Path> tas = JavaModules.testArtifactsOf(m.bin, m.src, m.onlyMain,
				m.onlyTests);
		assertEquals(2, tas.size());
		assertEquals(
				"net.sf.iwant.api.javamodules.JavaClasses\n" + "i:srcDirs:\n"
						+ "  mod/src/test/java\n" + "i:resourceDirs:\n"
						+ "  mod/src/test/resources\n" + "i:classLocations:\n"
						+ "  mod-main-classes\n" + "  commons-io-2.4.jar\n"
						+ "p:javacOptions:\n" + "  -Xlint\n"
						+ "  -Xlint:-serial\n" + "  -source\n" + "  1.8\n"
						+ "  -g\n" + "p:encoding:\n" + " null\n" + "",
				descr(tas.get(0)));
		assertEquals(
				"net.sf.iwant.api.javamodules.JavaClasses\n" + "i:srcDirs:\n"
						+ "  only-tests/src/test/java\n" + "i:resourceDirs:\n"
						+ "  only-tests/src/test/resources\n"
						+ "i:classLocations:\n" + "  only-main-main-classes\n"
						+ "p:javacOptions:\n" + "  -Xlint\n"
						+ "  -Xlint:-serial\n" + "  -source\n" + "  1.8\n"
						+ "  -g\n" + "p:encoding:\n" + " null\n" + "",
				descr(tas.get(1)));
		// bin and main only have no test artifact
	}

	@Test
	public void testArtifactJarsOfModules() {
		class Mods extends JavaModules {
			JavaBinModule bin = binModule("commons-io", "commons-io", "2.4");
			JavaSrcModule src = srcModule("mod").mainDeps(bin).end();
			JavaSrcModule onlyMain = srcModule("only-main").noTestJava()
					.mainDeps(src).end();
			JavaSrcModule onlyTests = srcModule("only-tests").noMainJava()
					.testDeps(onlyMain).end();
		}
		Mods m = new Mods();

		List<Path> jars = JavaModules.testArtifactJarsOf(m.bin, m.src,
				m.onlyMain, m.onlyTests);
		assertEquals(2, jars.size());
		assertEquals("net.sf.iwant.plugin.ant.Jar\n" + "i:classDirs:\n"
				+ "  mod-test-classes\n" + "", descr(jars.get(0)));
		assertEquals(
				"net.sf.iwant.plugin.ant.Jar\n" + "i:classDirs:\n"
						+ "  only-tests-test-classes\n" + "",
				descr(jars.get(1)));
		// bin and main only have no test artifact
	}

	@Test
	public void srcJarOfModuleContainsAllMainJavasScalasAndResources() {
		Jar src = JavaModules.srcJarOf(JavaSrcModule.with().name("mod")
				.mainJava("java1").mainJava("java2").mainScala("scala1")
				.mainScala("scala2").mainResources("res1").mainResources("res2")
				.end());

		assertEquals(
				"[mod/java1, mod/java2, mod/scala1, mod/scala2, mod/res1, mod/res2]",
				src.classDirs().toString());
	}

	@Test
	public void moduleWithOnlyTestSourcesHasNoSrcJar() {
		Jar src = JavaModules.srcJarOf(JavaSrcModule.with().name("mod")
				.testJava("java1").testJava("java2").testScala("scala1")
				.testScala("scala2").testResources("res1").testResources("res2")
				.end());

		assertNull(src);
	}

	@Test
	public void srcJarNameContainsVersionIfGiven() {
		JavaSrcModule mod = JavaSrcModule.with().name("mod").mainJava("src")
				.end();

		assertEquals("mod-sources.jar", JavaModules.srcJarOf(mod).name());
		assertEquals("mod-1.0-sources.jar",
				JavaModules.srcJarOf("1.0", mod).name());
	}

	@Test
	public void runtimeDepsOfModules() {
		class Mods extends JavaModules {
			JavaSrcModule commonUtil = srcModule("commonUtil").end();
			JavaSrcModule mainUtil = srcModule("mainUtil").end();
			JavaSrcModule mainRtUtil = srcModule("mainRtUtil").end();
			JavaSrcModule testUtil = srcModule("testUtil").end();
			JavaSrcModule mod = srcModule("mod").mainDeps(mainUtil, commonUtil)
					.mainRuntimeDeps(mainRtUtil).testDeps(testUtil).end();
			JavaSrcModule mod2 = srcModule("mod2").mainDeps(mod, commonUtil)
					.end();
		}
		Mods m = new Mods();

		assertEquals("[mod2, mod, mainUtil, commonUtil, mainRtUtil]",
				JavaModules.runtimeDepsOf(m.mod2).toString());
	}

}
