package org.fluentjava.iwant.core.javafinder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Set;

import org.fluentjava.iwant.api.javamodules.JavaModule;
import org.fluentjava.iwant.api.javamodules.JavaSrcModule;
import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.api.wsdef.IwantPluginWishes;
import org.fluentjava.iwant.api.wsdef.WishDefinitionContext;
import org.fluentjava.iwant.entry.Iwant.IwantException;
import org.junit.Before;
import org.junit.Test;

public class WsdefJavaOfTest {

	private Ctx ctx;

	@Before
	public void before() {
		ctx = new Ctx();
	}

	static class Ctx implements WishDefinitionContext {

		private JavaSrcModule wsdefJavaModule;

		@Override
		public JavaSrcModule wsdefdefJavaModule() {
			throw new UnsupportedOperationException("TODO test and implement");
		}

		@Override
		public JavaSrcModule wsdefJavaModule() {
			return wsdefJavaModule;
		}

		@Override
		public Set<? extends JavaModule> iwantApiModules() {
			throw new UnsupportedOperationException("TODO test and implement");
		}

		@Override
		public IwantPluginWishes iwantPlugin() {
			throw new UnsupportedOperationException("TODO test and implement");
		}

	}

	// the tests are artificial: the classes are supposed to be defined as
	// sources in the given wsdef module.

	@Test
	public void sourceInStandardLocationForForThisClass() {
		ctx.wsdefJavaModule = JavaSrcModule.with().name("wsdef")
				.locationUnderWsRoot("wsdef-location").mainJava("src/main/java")
				.end();

		Path src = new WsdefJavaOf(ctx).classUnderSrcMainJava(getClass());

		assertEquals(
				"wsdef-location/src/main/java/"
						+ "org/fluentjava/iwant/core/javafinder/WsdefJavaOfTest.java",
				src.name());
	}

	@Test
	public void sourceInDifferentAndCustomLocationForADifferentClass() {
		ctx.wsdefJavaModule = JavaSrcModule.with().name("wsdef")
				.locationUnderWsRoot("different-wsdef-location")
				.mainJava("mainjava").end();

		Source src = new WsdefJavaOf(ctx).classUnder(JavaModule.class,
				"mainjava");

		assertEquals(
				"different-wsdef-location/mainjava/"
						+ "org/fluentjava/iwant/api/javamodules/JavaModule.java",
				src.name());
	}

	@Test
	public void missingSrcDirGivesAnFriendlyException() {
		ctx.wsdefJavaModule = JavaSrcModule.with().name("wsdef")
				.locationUnderWsRoot("wsdef-location")
				.mainJava("wrong-src-name").end();

		WsdefJavaOf wsdefSrc = new WsdefJavaOf(ctx);

		try {
			wsdefSrc.classUnderSrcMainJava(getClass());
			fail();
		} catch (IwantException e) {
			assertEquals(
					"Module wsdef does not have java diretory src/main/java",
					e.getMessage());
		}
	}

}
