package org.fluentjava.iwant.embedded;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

import org.fluentjava.iwant.api.core.Concatenated;
import org.fluentjava.iwant.api.core.HelloTarget;
import org.fluentjava.iwant.api.model.TargetEvaluationContext;
import org.fluentjava.iwant.api.target.TargetBase;
import org.fluentjava.iwant.coreservices.FileUtil;
import org.fluentjava.iwant.entry.Iwant;
import org.fluentjava.iwant.entry3.UserPrefs;
import org.fluentjava.iwant.testarea.TestArea;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AsEmbeddedIwantUserTest {

	private TestArea testArea;
	private File cacheDir;
	private File wsRoot;

	@BeforeEach
	public void before() {
		testArea = TestArea.forTest(this);
		wsRoot = testArea.newDir("wsRoot");
		cacheDir = testArea.newDir("cached");
	}

	private static void assertFile(File expected, File actual) {
		assertEquals(expected.getAbsolutePath(), actual.getAbsolutePath());
	}

	@Test
	public void helloAsPath() {
		File cached = AsEmbeddedIwantUser.with().workspaceAt(wsRoot)
				.cacheAt(cacheDir).iwant()
				.target(new HelloTarget("hello", "hello message")).asPath();

		assertFile(new File(cacheDir, "target/hello"), cached);
		assertEquals("hello message", testArea.contentOf(cached));
	}

	@Test
	public void customUserPrefs() {
		class ThreadDetector extends TargetBase {

			AtomicReference<String> threadName = new AtomicReference<>();

			public ThreadDetector(String name) {
				super(name);
			}

			@Override
			protected IngredientsAndParametersDefined ingredientsAndParameters(
					IngredientsAndParametersPlease iUse) {
				return iUse.nothingElse();
			}

			@Override
			public void path(TargetEvaluationContext ctx) throws Exception {
				File dest = ctx.cached(this);
				FileUtil.newTextFile(dest, name());
				threadName.set(Thread.currentThread().getName());
			}

		}

		ThreadDetector td1 = new ThreadDetector("td1");
		ThreadDetector td2 = new ThreadDetector("td2");

		Concatenated root = Concatenated.named("root").unixPathTo(td1)
				.unixPathTo(td2).end();

		// 1 thread only
		UserPrefs prefsFor1 = new UserPrefs() {
			@Override
			public int workerCount() {
				return 1;
			}
		};

		AsEmbeddedIwantUser.with().userPrefs(prefsFor1).workspaceAt(wsRoot)
				.cacheAt(cacheDir).iwant().target(root).asPath();
		assertEquals(td1.threadName.get(), td2.threadName.get());

		// 2 threads configured => td1 and td2 are built using different
		// threads. Here we don't even care if happens always, 1 out of 20 is
		// good enough:

		UserPrefs prefsFor2 = new UserPrefs() {
			@Override
			public int workerCount() {
				return 2;
			}
		};
		for (int i = 0; i < 20; i++) {
			Iwant.del(cacheDir);

			AsEmbeddedIwantUser.with().userPrefs(prefsFor2).workspaceAt(wsRoot)
					.cacheAt(cacheDir).iwant().target(root).asPath();
			if (!td1.threadName.get().equals(td2.threadName.get())) {
				return;
			}
		}
		fail("User prefs of using 2 threads were never obeyed!");
	}

}
