package org.fluentjava.iwant.api.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.fluentjava.iwant.api.model.SystemEnv.SystemEnvPlease;
import org.junit.Test;

public class SystemEnvTest {

	@Test
	public void toStringIsHumanFriendly() {
		SystemEnv env = SystemEnv.with().string("a", "a1")
				.path("b", Source.underWsroot("b1")).end();

		assertEquals("SystemEnv{a=string:a1, b=path:b1}", env.toString());
	}

	@Test
	public void valueGetterReturnsValueOrNull() {
		Source src = Source.underWsroot("b1");
		SystemEnv env = SystemEnv.with().string("a", "a1").path("b", src).end();

		assertNull(env.get("nonexistent"));
		assertEquals("a1", env.get("a"));
		assertSame(src, env.get("b"));
	}

	@Test
	public void latestIsUsedIfSameValueIsDefinedTwice() {
		SystemEnv env = SystemEnv.with().string("a", "a1")
				.path("b", Source.underWsroot("b1"))
				.path("a", Source.underWsroot("a2")).string("b", "b2").end();

		assertEquals("SystemEnv{a=path:a2, b=string:b2}", env.toString());
	}

	@Test
	public void shovelToShovelsAllValuesToGivenBuilder() {
		StringBuilder log = new StringBuilder();
		class ValueLogger implements SystemEnvPlease {

			@Override
			public SystemEnvPlease string(String name, String value) {
				log.append("(string " + name + " " + value + ")");
				return this;
			}

			@Override
			public SystemEnvPlease path(String name, Path value) {
				log.append("(path " + name + " " + value + ")");
				return this;
			}

		}

		SystemEnv env = SystemEnv.with().string("string1", "value 1")
				.path("src", Source.underWsroot("src/1"))
				.string("string2", "value 2").end();

		env.shovelTo(new ValueLogger());

		assertEquals("(string string1 value 1)" + "(path src src/1)"
				+ "(string string2 value 2)", log.toString());
	}

}
