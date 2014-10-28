package net.sf.iwant.api.javamodules;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;

import net.sf.iwant.api.javamodules.JavaSrcModule.IwantSrcModuleSpex;

import org.junit.Test;

public class JavaModulesTest {

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
		assertEquals(JavaCompliance.JAVA_1_7, m.mod.javaCompliance());
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

}
