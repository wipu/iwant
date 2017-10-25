package org.fluentjava.iwant.entry.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;
import java.util.concurrent.CountDownLatch;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.net.PfxOptions;
import org.fluentjava.iwant.entry.Iwant;
import org.fluentjava.iwant.testarea.TestArea;

public class HttpDownloadTest {

	private static final int HTTP_PORT = 8888;
	private static final int HTTPS_PORT = HTTP_PORT + 1;
	private static Vertx vertx;
	private TestArea testArea;
	private HttpServer httpServer;
	private HttpServer httpsServer;
	private KeyManager[] defaultKeyManagers;
	private HostnameVerifier defaultHostnameVerifier;
	private SSLSocketFactory defaultSocketFactory;
	private TrustManager[] defaultTrustManagers;

	@BeforeClass
	public static void beforeClass() {
		vertx = Vertx.vertx();
	}

	@Before
	public void before() throws InterruptedException, URISyntaxException,
			NoSuchAlgorithmException, KeyStoreException, KeyManagementException,
			UnrecoverableKeyException {
		testArea = TestArea.forTest(this);
		createRedirectingServer();
		disableCertificateChecks();
	}

	@After
	public void after() throws InterruptedException, KeyManagementException,
			NoSuchAlgorithmException {
		enableDefaultCertificateChecks();
		stopServer(httpServer);
		stopServer(httpsServer);
	}

	private void disableCertificateChecks()
			throws NoSuchAlgorithmException, KeyStoreException,
			KeyManagementException, UnrecoverableKeyException {
		defaultTrustManagers = findDefaultTrustManagers();
		defaultKeyManagers = findDefaultKeyManagers();
		defaultSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
		defaultHostnameVerifier = HttpsURLConnection
				.getDefaultHostnameVerifier();

		SSLContext sc = reinitializedSslContext(null,
				new TrustManager[] { new BlueEyedTrustManager() });

		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		HttpsURLConnection.setDefaultHostnameVerifier((name, ssls) -> true);
	}

	private static SSLContext reinitializedSslContext(KeyManager[] km,
			TrustManager[] tm)
			throws NoSuchAlgorithmException, KeyManagementException {
		SSLContext sc = SSLContext.getInstance("TLS");
		sc.init(km, tm, new java.security.SecureRandom());
		return sc;
	}

	private static KeyManager[] findDefaultKeyManagers()
			throws NoSuchAlgorithmException, UnrecoverableKeyException,
			KeyStoreException {
		KeyManagerFactory kmf = KeyManagerFactory
				.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(null, null);
		return kmf.getKeyManagers();
	}

	private static TrustManager[] findDefaultTrustManagers()
			throws NoSuchAlgorithmException, KeyStoreException {
		TrustManagerFactory tmf = TrustManagerFactory
				.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init((KeyStore) null);
		return tmf.getTrustManagers();
	}

	private void enableDefaultCertificateChecks()
			throws NoSuchAlgorithmException, KeyManagementException {
		reinitializedSslContext(defaultKeyManagers, defaultTrustManagers);
		HttpsURLConnection.setDefaultSSLSocketFactory(defaultSocketFactory);
		HttpsURLConnection.setDefaultHostnameVerifier(defaultHostnameVerifier);
	}

	private static class BlueEyedTrustManager implements X509TrustManager {
		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[0];
		}

		@Override
		public void checkClientTrusted(
				java.security.cert.X509Certificate[] certs, String authType) {
			// we trust anything
		}

		@Override
		public void checkServerTrusted(
				java.security.cert.X509Certificate[] certs, String authType) {
			// we trust anything
		}
	}

	private static void stopServer(HttpServer httpServer)
			throws InterruptedException {
		if (httpServer == null) {
			return;
		}
		CountDownLatch closed = new CountDownLatch(1);
		System.err.println("Stopping " + httpServer);
		httpServer.close((v) -> closed.countDown());
		closed.await();
		System.err.println("Stopped " + httpServer);
	}

	private void createRedirectingServer()
			throws InterruptedException, URISyntaxException {
		HttpServer httpServer = vertx.createHttpServer();
		httpServer.requestHandler(req -> {
			System.err.println(httpServer + " serving " + req.absoluteURI());
			if ("/http-to-http".equals(req.path())) {
				redirect(req, "http://localhost:" + HTTP_PORT + "/final-http");
			} else if ("/http-to-https".equals(req.path())) {
				redirect(req,
						"https://localhost:" + HTTPS_PORT + "/final-https");
			} else if ("/final-http".equals(req.path())) {
				string200Ok(req, "final-http-body");
			} else {
				req.response().setStatusCode(404).end();
			}
		});
		this.httpServer = started(httpServer, HTTP_PORT);

		HttpServerOptions opts = new HttpServerOptions();
		opts.setSsl(true);
		opts.setPfxKeyCertOptions(new PfxOptions()
				.setPath(new File(getClass().getResource("ia.p12").toURI())
						.getAbsolutePath())
				.setPassword("q"));
		HttpServer httpsServer = vertx.createHttpServer(opts);
		httpsServer.requestHandler(req -> {
			System.err.println(httpsServer + " serving " + req.absoluteURI());
			if ("/final-https".equals(req.path())) {
				string200Ok(req, "final-https-body");
			} else {
				req.response().setStatusCode(404).end();
			}
		});
		this.httpsServer = started(httpsServer, HTTPS_PORT);
	}

	private static void string200Ok(HttpServerRequest req, String body) {
		req.response().setStatusCode(200);
		req.response().headers().add("Content-Length", "" + body.length());
		req.response().write(body).end();
	}

	private static void redirect(HttpServerRequest req, String dest) {
		int status = 301;
		req.response().setStatusCode(status).headers().add("Location", dest);
		String body = "Redirecting (" + status + ") to " + dest;
		req.response().headers().add("Content-Length", "" + body.length());
		req.response().write(body).end();
	}

	private static HttpServer started(HttpServer httpServer, int port)
			throws InterruptedException {
		CountDownLatch started = new CountDownLatch(1);
		System.err.println("Starting " + httpServer);
		httpServer.listen(port, (v) -> started.countDown());
		started.await();
		System.err.println("Started " + httpServer);
		return httpServer;
	}

	private void contentOfUrlShallBe(String url, String expectedBody)
			throws MalformedURLException, IOException {
		URL from = new URL(url);
		File to = new File(testArea.root(), "downloaded");
		System.err.println("Downloading " + from + " to " + to);
		Iwant.usingRealNetwork().downloaded(from, to);

		assertEquals(expectedBody, FileUtils.readFileToString(to, "UTF-8"));
	}

	@Test
	public void downloadFollowsSimpleHttpToHttpRedirect() throws IOException {
		contentOfUrlShallBe("http://localhost:" + HTTP_PORT + "/http-to-http",
				"final-http-body");
	}

	@Test
	public void directDownloadFromHttpsWorks() throws IOException {
		contentOfUrlShallBe("https://localhost:" + HTTPS_PORT + "/final-https",
				"final-https-body");
	}

	@Test
	public void downloadFollowsHttpToHttpsRedirect() throws IOException {
		contentOfUrlShallBe("http://localhost:" + HTTP_PORT + "/http-to-https",
				"final-https-body");
	}

	/**
	 * Internal test: let's make sure we don't leave security off after this
	 * test
	 */
	@Test
	public void certificateCheckRestorationWorks() throws IOException,
			KeyManagementException, NoSuchAlgorithmException {
		enableDefaultCertificateChecks();
		try {
			directDownloadFromHttpsWorks();
			fail();
		} catch (RuntimeException e) {
			assertTrue(e.getCause() instanceof SSLHandshakeException);
			Throwable cause = e;
			while (cause.getCause() != null) {
				cause = cause.getCause();
			}
			assertEquals(
					"unable to find valid certification path to requested target",
					cause.getMessage());
		}
	}

}
