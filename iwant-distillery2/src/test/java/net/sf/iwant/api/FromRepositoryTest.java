package net.sf.iwant.api;

import junit.framework.TestCase;

public class FromRepositoryTest extends TestCase {

	public void testIbiblioAnt171() {
		Downloaded ant171 = FromRepository.ibiblio().group("org/apache/ant")
				.name("ant").version("1.7.1");

		assertEquals("ant-1.7.1", ant171.name());
		assertEquals("http://mirrors.ibiblio.org/maven2/"
				+ "org/apache/ant/ant/1.7.1/ant-1.7.1.jar", ant171.url()
				.toString());
		// TODO refer to correct checksum url
		assertNull(ant171.md5());
	}

	public void testIbiblioCommonsMath12() {
		Downloaded commonsMath12 = FromRepository.ibiblio()
				.group("commons-math").name("commons-math").version("1.2");

		assertEquals("commons-math-1.2", commonsMath12.name());
		assertEquals("http://mirrors.ibiblio.org/maven2/"
				+ "commons-math/commons-math/1.2/commons-math-1.2.jar",
				commonsMath12.url().toString());
		// TODO refer to correct checksum url
		assertNull(commonsMath12.md5());
	}

}