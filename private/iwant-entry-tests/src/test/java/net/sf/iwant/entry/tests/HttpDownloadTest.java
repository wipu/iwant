package net.sf.iwant.entry.tests;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.net.PfxOptions;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.testarea.TestArea;

public class HttpDownloadTest {

	private static final int HTTP_PORT = 8888;
	private static final int HTTPS_PORT = HTTP_PORT + 1;
	private static Vertx vertx;
	private TestArea testArea;
	private HttpServer httpServer;
	private HttpServer httpsServer;

	@BeforeClass
	public static void beforeClass() {
		vertx = Vertx.vertx();
	}

	@Before
	public void before() throws InterruptedException, URISyntaxException {
		testArea = TestArea.forTest(this);
		createRedirectingServer();
	}

	@After
	public void after() throws InterruptedException {
		stopServer(httpServer);
		stopServer(httpsServer);
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

	@Ignore
	@Test
	public void directDownloadFromHttpsWorks() throws IOException {
		contentOfUrlShallBe("https://localhost:" + HTTPS_PORT + "/final-https",
				"hello via redirect");
	}

	@Ignore
	@Test
	public void downloadFollowsHttpToHttpsRedirect() throws IOException {
		contentOfUrlShallBe("http://localhost:" + HTTP_PORT + "/http-to-https",
				"hello via redirect");
	}

}
