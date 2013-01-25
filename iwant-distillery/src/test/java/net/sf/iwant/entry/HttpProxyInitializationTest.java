package net.sf.iwant.entry;

import junit.framework.TestCase;

public class HttpProxyInitializationTest extends TestCase {

	private static final String H_KEY = "http.proxyHost";
	private static final String P_KEY = "http.proxyPort";
	private static final String SH_KEY = "https.proxyHost";
	private static final String SP_KEY = "https.proxyPort";

	private String originalH;
	private String originalP;
	private String originalSH;
	private String originalSP;

	@Override
	public void setUp() {
		originalH = System.getProperty(H_KEY);
		originalP = System.getProperty(P_KEY);
		originalSH = System.getProperty(SH_KEY);
		originalSP = System.getProperty(SP_KEY);
	}

	@Override
	public void tearDown() {
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
			String httpProxyPort, String httpsProxyHost, String httpsProxyPort) {
		assertEquals(httpProxyHost, System.getProperty(H_KEY));
		assertEquals(httpProxyPort, System.getProperty(P_KEY));
		assertEquals(httpsProxyHost, System.getProperty(SH_KEY));
		assertEquals(httpsProxyPort, System.getProperty(SP_KEY));
	}

	// the tests

	public void testNullValues() {
		Iwant.unixHttpProxyToJavaHttpProxy(null, null);
		assertJavaSettings(null, null, null, null);
	}

	public void testEmptyValues() {
		Iwant.unixHttpProxyToJavaHttpProxy("", "");
		assertJavaSettings("", null, "", null);
	}

	public void testHttpProxyHost() {
		Iwant.unixHttpProxyToJavaHttpProxy("http-proxy-host", "");
		assertJavaSettings("http-proxy-host", null, "", null);
	}

	public void testHttpProxyHostAndPort() {
		Iwant.unixHttpProxyToJavaHttpProxy("http-proxy-host:8080", "");
		assertJavaSettings("http-proxy-host", "8080", "", null);
	}

	public void testHttpProxyHostAndPortAndHttpsProxyHost() {
		Iwant.unixHttpProxyToJavaHttpProxy("http-proxy-host:8080",
				"https-proxy-host");
		assertJavaSettings("http-proxy-host", "8080", "https-proxy-host", null);
	}

	public void testHttpProxyHostAndPortAndHttpsProxyHostAndPort() {
		Iwant.unixHttpProxyToJavaHttpProxy("http-proxy-host:8080",
				"https-proxy-host:8081");
		assertJavaSettings("http-proxy-host", "8080", "https-proxy-host",
				"8081");
	}

}
