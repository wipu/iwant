package org.fluentjava.iwant.entry.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.fluentjava.iwant.entry.Iwant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HttpProxyInitializationTest {

	private static final String H_KEY = "http.proxyHost";
	private static final String P_KEY = "http.proxyPort";
	private static final String SH_KEY = "https.proxyHost";
	private static final String SP_KEY = "https.proxyPort";

	private String originalH;
	private String originalP;
	private String originalSH;
	private String originalSP;

	@BeforeEach
	public void before() {
		originalH = System.getProperty(H_KEY);
		originalP = System.getProperty(P_KEY);
		originalSH = System.getProperty(SH_KEY);
		originalSP = System.getProperty(SP_KEY);
	}

	@AfterEach
	public void after() {
		restore(H_KEY, originalH);
		restore(P_KEY, originalP);
		restore(SH_KEY, originalSH);
		restore(SP_KEY, originalSP);
	}

	private static void restore(String key, String value) {
		if (value == null) {
			System.clearProperty(key);
		} else {
			System.setProperty(key, value);
		}
	}

	private static void assertJavaSettings(String httpProxyHost,
			String httpProxyPort, String httpsProxyHost,
			String httpsProxyPort) {
		assertEquals(httpProxyHost, System.getProperty(H_KEY));
		assertEquals(httpProxyPort, System.getProperty(P_KEY));
		assertEquals(httpsProxyHost, System.getProperty(SH_KEY));
		assertEquals(httpsProxyPort, System.getProperty(SP_KEY));
	}

	// the tests

	@Test
	public void nullValues() {
		Iwant.unixHttpProxyToJavaHttpProxy(null, null);
		assertJavaSettings(null, null, null, null);
	}

	@Test
	public void emptyValues() {
		Iwant.unixHttpProxyToJavaHttpProxy("", "");
		assertJavaSettings(null, null, null, null);
	}

	@Test
	public void httpProxyThatIsInvalidUrlOnlyAHostName() {
		try {
			Iwant.unixHttpProxyToJavaHttpProxy("http-proxy-host", "");
			fail();
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	@Test
	public void httpProxyHost() {
		Iwant.unixHttpProxyToJavaHttpProxy("http://http-proxy-host", "");
		assertJavaSettings("http-proxy-host", null, null, null);
	}

	@Test
	public void httpProxyHostAndPort() {
		Iwant.unixHttpProxyToJavaHttpProxy("http://http-proxy-host:8080", "");
		assertJavaSettings("http-proxy-host", "8080", null, null);
	}

	@Test
	public void httpProxyHostAndPortAndHttpsProxyHost() {
		Iwant.unixHttpProxyToJavaHttpProxy("http://http-proxy-host:8080",
				"http://https-proxy-host");
		assertJavaSettings("http-proxy-host", "8080", "https-proxy-host", null);
	}

	@Test
	public void httpProxyHostAndPortAndHttpsProxyHostAndPort() {
		Iwant.unixHttpProxyToJavaHttpProxy("http://http-proxy-host:8080",
				"http://https-proxy-host:8081");
		assertJavaSettings("http-proxy-host", "8080", "https-proxy-host",
				"8081");
	}

}
