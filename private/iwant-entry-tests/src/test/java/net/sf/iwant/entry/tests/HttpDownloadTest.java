package net.sf.iwant.entry.tests;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.testarea.TestArea;

public class HttpDownloadTest {

	private static final int PORT = 8888;
	private static Vertx vertx;
	private TestArea testArea;
	private HttpServer httpServer;

	@BeforeClass
	public static void beforeClass() {
		vertx = Vertx.vertx();
	}

	@Before
	public void before() {
		testArea = TestArea.forTest(this);
	}

	@After
	public void after() throws InterruptedException {
		if (httpServer == null) {
			return;
		}
		CountDownLatch closed = new CountDownLatch(1);
		httpServer.close((v) -> closed.countDown());
		closed.await();
	}

	private void createRedirectingServer(String src, String dest, String body)
			throws InterruptedException {
		httpServer = vertx.createHttpServer();
		httpServer.requestHandler(req -> {
			System.err.println("Serving " + req.absoluteURI());
			if (req.path().startsWith(src)) {
				req.response().setStatusCode(301).headers().add("Location",
						"http://localhost:" + PORT + dest);
				req.response().end();
			} else if (req.path().startsWith(dest)) {
				req.response().setStatusCode(200);
				req.response().headers().add("Content-Length",
						"" + body.length());
				req.response().write(body).end();
			} else {
				req.response().setStatusCode(404).end();
			}
		});
		startServer();
	}

	private void startServer() throws InterruptedException {
		CountDownLatch started = new CountDownLatch(1);
		httpServer.listen(PORT, (v) -> started.countDown());
		started.await();
	}

	@Test
	public void downloadFollowsRedirect()
			throws InterruptedException, IOException {
		createRedirectingServer("/src", "/dest", "hello via redirect");

		URL from = new URL("http://localhost:" + PORT + "/src");
		File to = new File(testArea.root(), "downloaded");
		Iwant.usingRealNetwork().downloaded(from, to);

		assertEquals("hello via redirect",
				FileUtils.readFileToString(to, "UTF-8"));
	}

}
