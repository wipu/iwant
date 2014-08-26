package net.sf.iwant.entry3;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.TestCase;
import net.sf.iwant.api.EclipseSettings;
import net.sf.iwant.api.HelloSideEffect;
import net.sf.iwant.api.IwantWorkspace;
import net.sf.iwant.api.ScriptGenerated;
import net.sf.iwant.api.SideEffectDefinitionContext;
import net.sf.iwant.api.WorkspaceDefinitionContext;
import net.sf.iwant.api.WsInfoMock;
import net.sf.iwant.api.javamodules.JavaBinModule;
import net.sf.iwant.api.javamodules.JavaClasses;
import net.sf.iwant.api.javamodules.JavaModule;
import net.sf.iwant.api.javamodules.JavaSrcModule;
import net.sf.iwant.api.model.Caches;
import net.sf.iwant.api.model.Concatenated;
import net.sf.iwant.api.model.Concatenated.ConcatenatedBuilder;
import net.sf.iwant.api.model.HelloTarget;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.SideEffect;
import net.sf.iwant.api.model.SideEffectContext;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.coreservices.FileUtil;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.IwantException;
import net.sf.iwant.testarea.TestArea;
import net.sf.iwant.testing.IwantNetworkMock;

public class WishEvaluatorTest extends TestCase {

	private TestArea testArea;
	private File asSomeone;
	private File wsRoot;
	private ByteArrayOutputStream out;
	private ByteArrayOutputStream err;
	private WishEvaluator evaluator;
	private IwantNetworkMock network;
	private Iwant iwant;
	private WsInfoMock wsInfo;
	private Caches caches;
	private JavaSrcModule wsdefdefJavaModule;
	private JavaSrcModule wsdefJavaModule;
	private Set<JavaModule> iwantApiModules = Collections
			.<JavaModule> singleton(JavaBinModule.providing(
					Source.underWsroot("mock-iwant-classes")).end());
	private InputStream originalIn;
	private PrintStream originalOut;
	private PrintStream originalErr;
	private WorkspaceDefinitionContext wsdefCtx;
	private File iwantWs;
	private JavaModule wsdefdefModule;

	@Override
	public void setUp() throws IOException {
		testArea = TestArea.forTest(this);
		network = new IwantNetworkMock(testArea);
		iwant = Iwant.using(network);
		asSomeone = testArea.newDir("as-" + getClass().getSimpleName());
		wsRoot = testArea.newDir("wsroot");
		originalIn = System.in;
		originalOut = System.out;
		originalErr = System.err;
		out = new ByteArrayOutputStream();
		err = new ByteArrayOutputStream();
		System.setOut(new PrintStream(out));
		System.setErr(new PrintStream(err));
		wsInfo = new WsInfoMock();
		wsInfo.hasWsRoot(wsRoot);
		wsdefdefJavaModule = JavaSrcModule.with().name("wsdefdef")
				.locationUnderWsRoot("wsdefdef").mainJava("src").end();
		wsdefJavaModule = JavaSrcModule.with().name("wsdef")
				.locationUnderWsRoot("wsdef").mainJava("src").end();
		caches = new CachesImpl(new File(asSomeone, ".i-cached"),
				wsInfo.wsRoot(), network);
		int workerCount = 1;
		iwantWs = testArea.newDir("iwant-ws");
		wsdefdefModule = JavaSrcModule.with().name("wsdefdef").end();
		wsdefCtx = new WorkspaceDefinitionContextImpl(iwantApiModules, iwantWs,
				wsdefdefModule);
		evaluator = new WishEvaluator(out, err, wsRoot, iwant, wsInfo, caches,
				workerCount, wsdefdefJavaModule, wsdefJavaModule, wsdefCtx);
	}

	private String out() {
		return out.toString();
	}

	private String err() {
		return err.toString();
	}

	@Override
	public void tearDown() {
		System.setIn(originalIn);
		System.setOut(originalOut);
		System.setErr(originalErr);

		if (!out().isEmpty()) {
			System.err.println("=== out:\n" + out());
		}
		if (!err().isEmpty()) {
			System.err.println("=== err:\n" + err());
		}
	}

	private void evaluateAndFail(String wish, IwantWorkspace ws,
			String expectedErrorMessage) {
		try {
			evaluator.iwant(wish, ws);
			fail();
		} catch (IllegalStateException e) {
			assertEquals(expectedErrorMessage, e.getCause().getMessage());
		}
	}

	private class Hello implements IwantWorkspace {

		@Override
		public List<? extends Target> targets() {
			return Arrays.asList(new HelloTarget("hello", "hello content"));
		}

		@Override
		public List<? extends SideEffect> sideEffects(
				SideEffectDefinitionContext ctx) {
			return Collections.emptyList();
		}

	}

	private class TwoHellos implements IwantWorkspace {

		@Override
		public List<? extends Target> targets() {
			return Arrays.asList(new HelloTarget("hello1", "content 1"),
					new HelloTarget("hello2", "content 2"));
		}

		@Override
		public List<? extends SideEffect> sideEffects(
				SideEffectDefinitionContext ctx) {
			return Collections.emptyList();
		}

	}

	public void testIllegalWishFromHello() {
		IwantWorkspace hello = new Hello();
		try {
			evaluator.iwant("illegal/wish", hello);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("Illegal wish: illegal/wish\nlegal targets:[hello]",
					e.getMessage());
		}
		assertEquals("", out.toString());
	}

	public void testListOfTargetsFromHello() {
		IwantWorkspace hello = new Hello();
		evaluator.iwant("list-of/targets", hello);
		assertEquals("hello\n", out.toString());
	}

	public void testTargetHelloAsPathFromHello() {
		IwantWorkspace hello = new Hello();

		evaluator.iwant("target/hello/as-path", hello);

		File cached = new File(asSomeone, ".i-cached/target/hello");
		assertEquals(cached + "\n", out.toString());
		assertEquals("hello content", testArea.contentOf(cached));
	}

	public void testTargetHelloContentFromHello() {
		IwantWorkspace hello = new Hello();
		evaluator.iwant("target/hello/content", hello);
		assertEquals("hello content", out.toString());
	}

	public void testListOfTargetsFromTwoHellos() {
		IwantWorkspace hellos = new TwoHellos();
		evaluator.iwant("list-of/targets", hellos);
		assertEquals("hello1\nhello2\n", out.toString());
	}

	public void testTargetHello1AsPathFromTwoHellos() {
		IwantWorkspace hellos = new TwoHellos();

		evaluator.iwant("target/hello1/as-path", hellos);

		File cached = new File(asSomeone, ".i-cached/target/hello1");
		assertEquals(cached + "\n", out.toString());
		assertEquals("content 1", testArea.contentOf(cached));
	}

	public void testTargetHello1ContentFromTwoHellos() {
		IwantWorkspace hellos = new TwoHellos();
		evaluator.iwant("target/hello1/content", hellos);
		assertEquals("content 1", out.toString());
	}

	public void testTargetHello2AsPathFromTwoHellos() {
		IwantWorkspace hellos = new TwoHellos();

		evaluator.iwant("target/hello2/as-path", hellos);

		File cached = new File(asSomeone, ".i-cached/target/hello2");
		assertEquals(cached + "\n", out.toString());
		assertEquals("content 2", testArea.contentOf(cached));
	}

	public void testTargetHello2ContentFromTwoHellos() {
		IwantWorkspace hellos = new TwoHellos();
		evaluator.iwant("target/hello2/content", hellos);
		assertEquals("content 2", out.toString());
	}

	// target without a wsdef

	public void testStandaloneHelloTargetContent() {
		Target target = new HelloTarget("standalone",
				"Hello from standalone target\n");
		evaluator.content(target);
		assertEquals("Hello from standalone target\n", out.toString());
	}

	// another target as ingredient

	public void testStreamOfTargetThatUsesAnotherTargetStreamAsIngredient() {
		Target ingredient = new HelloTarget("ingredient", "ingredient content");
		Target target = new TargetThatNeedsAnotherAsStream("target", ingredient);
		evaluator.content(target);
		assertEquals("Stream using 'ingredient content' as ingredient",
				out.toString());
	}

	public void testPathToTargetThatUsesAnotherTargetStreamAsIngredient() {
		Target ingredient = new HelloTarget("ingredient", "ingredient content");
		Target target = new TargetThatNeedsAnotherAsStream("target", ingredient);

		evaluator.asPath(target);

		File cached = new File(asSomeone, ".i-cached/target/target");
		assertEquals(cached + "\n", out.toString());
		assertEquals("Stream using 'ingredient content' as ingredient",
				testArea.contentOf(cached));
	}

	public void testStreamOfTargetThatUsesAnotherTargetPathAsIngredient() {
		Target ingredient = new HelloTarget("ingredient", "ingredient content");
		Target target = new TargetThatNeedsAnotherAsPath("target", ingredient);
		evaluator.content(target);
		assertEquals("Stream using 'ingredient content' as ingredient",
				out.toString());
	}

	public void testPathToTargetThatUsesAnotherTargetPathAsIngredient() {
		Target ingredient = new HelloTarget("ingredient", "ingredient content");
		Target target = new TargetThatNeedsAnotherAsPath("target", ingredient);

		evaluator.asPath(target);

		File cached = new File(asSomeone, ".i-cached/target/target");
		assertEquals(cached + "\n", out.toString());
		assertEquals("Stream using 'ingredient content' as ingredient",
				testArea.contentOf(cached));
	}

	// source as ingredient

	public void testStreamOfTargetThatUsesASourceStreamAsIngredient() {
		testArea.hasFile("wsroot/src", "src content");
		Path ingredient = Source.underWsroot("src");
		Target target = new TargetThatNeedsAnotherAsStream("target", ingredient);
		evaluator.content(target);
		assertEquals("Stream using 'src content' as ingredient", out.toString());
	}

	public void testStreamOfTargetThatUsesASourcePathAsIngredient() {
		testArea.hasFile("wsroot/src", "src content");
		Path ingredient = Source.underWsroot("src");
		Target target = new TargetThatNeedsAnotherAsPath("target", ingredient);
		evaluator.content(target);
		assertEquals("Stream using 'src content' as ingredient", out.toString());
	}

	// ...

	public void testTargetIsRefreshedIfDescriptorOfIngredientsIngredientChanged() {
		TargetMock t1 = new TargetMock("t1");
		t1.hasNoIngredients();
		t1.hasContent("t1 content 1");
		t1.hasContentDescriptor("t1 descr 1");

		Target t2 = new TargetThatNeedsAnotherAsStream("t2", t1);
		Target t3 = new TargetThatNeedsAnotherAsStream("t3", t2);

		evaluator.asPath(t3);

		assertEquals("Stream using 'Stream using 't1 content 1'"
				+ " as ingredient' as ingredient",
				testArea.contentOf(new File(asSomeone, ".i-cached/target/t3")));

		// modification:
		t1.hasContent("t1 content 2");
		t1.hasContentDescriptor("t1 descr 2");

		evaluator.asPath(t3);

		assertEquals("Stream using 'Stream using 't1 content 2'"
				+ " as ingredient' as ingredient",
				testArea.contentOf(new File(asSomeone, ".i-cached/target/t3")));
	}

	// side-effects

	private class OnlyEclipseSettingsAsSideEffect implements IwantWorkspace {

		@Override
		public List<? extends Target> targets() {
			return Arrays.asList(new HelloTarget("hello", "content"));
		}

		@Override
		public List<? extends SideEffect> sideEffects(
				SideEffectDefinitionContext ctx) {
			return Arrays.asList(EclipseSettings.with()
					.name("eclipse-settings").end());
		}

	}

	private class TwoSideEffects implements IwantWorkspace {

		@Override
		public List<? extends Target> targets() {
			return Arrays.asList(new HelloTarget("hello", "content"));
		}

		@Override
		public List<? extends SideEffect> sideEffects(
				SideEffectDefinitionContext ctx) {
			return Arrays.asList(new HelloSideEffect("hello-1"),
					new HelloSideEffect("hello-2"));
		}

	}

	public void testEmptyListOfSideEffects() {
		IwantWorkspace hellos = new TwoHellos();
		evaluator.iwant("list-of/side-effects", hellos);
		assertEquals("", out.toString());
	}

	public void testListOfSideEffectsOfOnlyEclipseSettingsAsSideEffect() {
		IwantWorkspace hellos = new OnlyEclipseSettingsAsSideEffect();
		evaluator.iwant("list-of/side-effects", hellos);
		assertEquals("eclipse-settings\n", out.toString());
	}

	public void testListOfSideEffectsOfTwoSideEffects() {
		IwantWorkspace hellos = new TwoSideEffects();
		evaluator.iwant("list-of/side-effects", hellos);
		assertEquals("hello-1\nhello-2\n", out.toString());
	}

	public void testHello1EffectiveOfTwoSideEffects() {
		IwantWorkspace hellos = new TwoSideEffects();

		evaluator.iwant("side-effect/hello-1/effective", hellos);

		assertEquals("hello-1 mutating.\n", err.toString());
	}

	public void testHello2EffectiveOfTwoSideEffects() {
		IwantWorkspace hellos = new TwoSideEffects();

		evaluator.iwant("side-effect/hello-2/effective", hellos);

		assertEquals("hello-2 mutating.\n", err.toString());
	}

	public void testDeletionOfJavaFileIsDetectedAndCompilationIsRetriedAndFailed() {
		File srcDir = new File(wsRoot, "src");
		Iwant.newTextFile(new File(srcDir, "pak1/Caller.java"),
				"package pak1;\npublic class Caller {pak2.Callee callee;}");
		File calleeJava = new File(srcDir, "pak2/Callee.java");
		Iwant.newTextFile(calleeJava, "package pak2;\npublic class Callee {}");
		Source src = Source.underWsroot("src");
		Target target = JavaClasses.with().name("multiple").srcDirs(src)
				.classLocations().end();

		evaluator.asPath(target);

		assertTrue(new File(caches.contentOf(target), "pak1/Caller.class")
				.exists());
		assertTrue(new File(caches.contentOf(target), "pak2/Callee.class")
				.exists());

		// no retry with Callee.java missing
		calleeJava.delete();

		try {
			evaluator.asPath(target);
			fail();
		} catch (IwantException e) {
			assertEquals("Compilation failed.", e.getMessage());
		}

		assertFalse(new File(caches.contentOf(target), "pak1/Caller.class")
				.exists());
		assertFalse(new File(caches.contentOf(target), "pak2/Callee.class")
				.exists());
	}

	public void testTwoTargetsReferToSamePathButOnlyTheOneThatDeclaresItAsIngredientSucceeds() {
		Path src = new HelloTarget("src", "src content");

		ConcatenatedBuilder correctSpex = Concatenated.named("correct");
		correctSpex.pathTo(src);
		Target correct = correctSpex.end();

		Target incorrect = new TargetThatForgetsToDeclareAnIngredient(
				"incorrect", src);

		Target root = Concatenated.named("root").pathTo(correct)
				.pathTo(incorrect).end();

		try {
			evaluator.asPath(root);
			fail();
		} catch (IwantException e) {
			assertEquals("Target incorrect referred to src without"
					+ " declaring it as an ingredient.", e.getMessage());
		}

		// correct shall succeed:
		assertEquals(asSomeone + "/.i-cached/target/src",
				testArea.contentOf(new File(asSomeone,
						".i-cached/target/correct")));

		// incorrect shall not even produce the file:
		assertFalse(new File(asSomeone, ".i-cached/target/incorrect").exists());
	}

	/**
	 * Even though planner has been tested, TargetRefreshTask used to
	 * synchronize too much, preventing concurrency from working properly
	 * end-to-end
	 */
	public void testConcurrencyWorksEndToEnd() throws InterruptedException {
		final int workerCount = 2;
		evaluator = new WishEvaluator(out, err, wsRoot, iwant, wsInfo, caches,
				workerCount, wsdefdefJavaModule, wsdefJavaModule, wsdefCtx);

		ConcurrencyControllableTarget part1 = new ConcurrencyControllableTarget(
				"part1");
		ConcurrencyControllableTarget part2 = new ConcurrencyControllableTarget(
				"part2");
		ConcurrencyControllableTarget part3 = new ConcurrencyControllableTarget(
				"part3");
		ConcurrencyControllableTarget part4 = new ConcurrencyControllableTarget(
				"part4");
		final ConcurrencyControllableTarget listOfParts = new ConcurrencyControllableTarget(
				"listOfParts", part1, part2, part3, part4);

		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				evaluator.asPath(listOfParts);
			}
		});

		thread.start();
		// =>
		part1.shallEventuallyStartRefresh();
		part2.shallEventuallyStartRefresh();
		part3.shallNotStartRefresh();
		part4.shallNotStartRefresh();
		listOfParts.shallNotStartRefresh();

		part1.finishesRefresh();
		// =>
		part3.shallEventuallyStartRefresh();
		part4.shallNotStartRefresh();
		listOfParts.shallNotStartRefresh();

		part3.finishesRefresh();
		// =>
		part4.shallEventuallyStartRefresh();
		listOfParts.shallNotStartRefresh();

		part1.finishesRefresh();
		// =>
		// nothing yet

		part4.finishesRefresh();
		// =>
		// nothing yet

		part2.finishesRefresh();
		// =>
		listOfParts.shallEventuallyStartRefresh();

		listOfParts.finishesRefresh();
		thread.join();
		// =>

		assertEquals("part1", testArea.contentOf(new File(asSomeone,
				".i-cached/target/part1")));
		assertEquals("part2", testArea.contentOf(new File(asSomeone,
				".i-cached/target/part2")));
		assertEquals("part3", testArea.contentOf(new File(asSomeone,
				".i-cached/target/part3")));
		assertEquals("part4", testArea.contentOf(new File(asSomeone,
				".i-cached/target/part4")));
		assertEquals("listOfParts", testArea.contentOf(new File(asSomeone,
				".i-cached/target/listOfParts")));
	}

	public void testContextUsesThreadNameToMakeSureAllWorkersGetOwnTemporaryDirectory()
			throws InterruptedException {
		final List<File> results = Collections
				.synchronizedList(new ArrayList<File>());
		Runnable tester = new Runnable() {
			@Override
			public void run() {
				File tmpDir = evaluator.targetEvaluationContext()
						.freshTemporaryDirectory();
				results.add(tmpDir);
			}
		};
		Thread worker1 = new Thread(tester, "worker-1");
		Thread worker2 = new Thread(tester, "worker-2");
		worker1.start();
		worker1.join();
		worker2.start();
		worker2.join();

		assertEquals(new File(asSomeone, ".i-cached/temp/worker-1"),
				results.get(0));
		assertEquals(new File(asSomeone, ".i-cached/temp/worker-2"),
				results.get(1));
	}

	public void testScriptsWorkCorrectlyInParallel() {
		final int workerCount = 8;
		evaluator = new WishEvaluator(out, err, wsRoot, iwant, wsInfo, caches,
				workerCount, wsdefdefJavaModule, wsdefJavaModule, wsdefCtx);

		final int partCount = 20;
		List<Target> parts = new ArrayList<Target>();
		ConcatenatedBuilder listOfPartsSpex = Concatenated.named("listOfParts");
		for (int i = 0; i < partCount; i++) {
			ConcatenatedBuilder scriptContent = Concatenated.named("script-"
					+ i);
			scriptContent.string("#!/bin/bash\n");
			scriptContent.string("set -eu\n");
			scriptContent.string("DEST=$1\n");
			scriptContent.string("echo " + i + " > \"$DEST\"\n");
			Concatenated script = scriptContent.end();
			Target part = ScriptGenerated.named("part-" + i).byScript(script);
			parts.add(part);
			listOfPartsSpex = listOfPartsSpex.pathTo(part);
		}
		Target listOfParts = listOfPartsSpex.end();

		evaluator.asPath(listOfParts);

		for (int i = 0; i < partCount; i++) {
			assertEquals(i + "\n", testArea.contentOf(new File(asSomeone,
					".i-cached/target/part-" + i)));
		}
	}

	public void testSideEffectDefinitionContextPassesIwantApiClasses() {
		final AtomicReference<Set<? extends JavaModule>> fromCtx = new AtomicReference<Set<? extends JavaModule>>();
		IwantWorkspace ws = new IwantWorkspace() {

			@Override
			public List<? extends Target> targets() {
				return Collections.emptyList();
			}

			@Override
			public List<? extends SideEffect> sideEffects(
					SideEffectDefinitionContext ctx) {
				fromCtx.set(ctx.iwantApiModules());
				return Collections.emptyList();
			}
		};

		evaluator.iwant("list-of/side-effects", ws);

		assertSame(iwantApiModules, fromCtx.get());
	}

	public void testSameNameOnPathAndTargetCausesErrorAtListOfOrAsPath() {
		IwantWorkspace ws = new IwantWorkspace() {

			@Override
			public List<? extends Target> targets() {
				return Arrays.asList(targetA(), targetB());
			}

			private Target targetA() {
				ConcatenatedBuilder a = Concatenated.named("a");
				a.pathTo(sourceB());
				return a.end();
			}

			private Path sourceB() {
				return Source.underWsroot("b");
			}

			private Target targetB() {
				return new HelloTarget("b", "b");
			}

			@Override
			public List<? extends SideEffect> sideEffects(
					SideEffectDefinitionContext ctx) {
				return Collections.emptyList();
			}
		};

		try {
			evaluator.iwant("list-of/targets", ws);
			fail();
		} catch (Iwant.IwantException e) {
			assertEquals("Two conflicting definitions for Path name b:\n"
					+ "One is of\n"
					+ " class net.sf.iwant.api.model.HelloTarget\n"
					+ "and another is of\n"
					+ " class net.sf.iwant.api.model.Source", e.getMessage());
		}

	}

	/**
	 * Bug: only source timestamps were checked, so if a refresh is interrupted,
	 * it's not retried because no source ingredients are newer than the cached
	 * descriptor, only cached target files.
	 */
	public void testTargetWithNonFreshTargetIngredientAndExistingCachedDescriptorStaysNonFreshEvenIfItFails() {
		TargetMock ingredient = new TargetMock("ingredient");
		ingredient.hasContentDescriptor("ingredient descr 1");
		ingredient.hasContent("ingredient content");
		ingredient.hasNoIngredients();

		final TargetMock target = new TargetMock("failing");
		target.hasContentDescriptor("failing");
		target.hasContent("failing content");
		target.hasIngredients(ingredient);

		IwantWorkspace ws = new IwantWorkspace() {
			@Override
			public List<? extends Target> targets() {
				return Arrays.asList(target);
			}

			@Override
			public List<? extends SideEffect> sideEffects(
					SideEffectDefinitionContext ctx) {
				return Collections.emptyList();
			}
		};

		// first a successful refresh so a cached descriptor exists
		evaluator.iwant("target/failing/as-path", ws);
		// then ingredient gets dirty and the failing target fails
		ingredient.hasContentDescriptor("ingredient descr 2");
		target.shallFailAfterCreatingCachedContent("Simulated failure");
		evaluateAndFail("target/failing/as-path", ws, "Simulated failure");
		// if there is a bug, no refresh is even retried here so no failure
		evaluateAndFail("target/failing/as-path", ws, "Simulated failure");
	}

	public void testTargetRefreshIsRetriedEvenIfReasonWasDirtinessOfIngredientAndItsRefreshWasInterruptedEarlier() {
		TargetMock a = new TargetMock("a");
		a.hasContentDescriptor("a descr 1");
		a.hasContent("a content");
		a.hasNoIngredients();

		TargetMock b = new TargetMock("b");
		b.hasContentDescriptor("b descr 1");
		b.hasContent("b content");
		b.hasIngredients(a);

		final TargetMock c = new TargetMock("c");
		c.hasContentDescriptor("c");
		c.hasContent("c content");
		c.hasIngredients(b);

		final TargetMock d = new TargetMock("d");
		d.hasContentDescriptor("d");
		d.hasContent("d content");
		d.hasIngredients(c);

		IwantWorkspace ws = new IwantWorkspace() {
			@Override
			public List<? extends Target> targets() {
				return Arrays.asList(d);
			}

			@Override
			public List<? extends SideEffect> sideEffects(
					SideEffectDefinitionContext ctx) {
				return Collections.emptyList();
			}
		};

		// first a successful refresh so a cached descriptors exist
		evaluator.iwant("target/d/as-path", ws);
		// then ingredient gets dirty and the failing target fails
		a.hasContentDescriptor("ingredient descr 2");
		b.shallFailAfterCreatingCachedContent("b failure");
		d.shallFailAfterCreatingCachedContent("d failure");
		evaluateAndFail("target/d/as-path", ws, "b failure");

		// at retry not only is b refreshed but also d, eventually
		b.shallNotFailAfterCreatingCachedContent();
		evaluateAndFail("target/d/as-path", ws, "d failure");
	}

	/**
	 * Before the fix this failed, because only source timestamps were checked,
	 * target dirtiness relied on refreshing them during the same run.
	 */
	public void testTargetIsRefreshedEvenIfATargetIngredientWasRefreshedDuringEarlierRun()
			throws InterruptedException {
		TargetMock a = new TargetMock("a");
		a.hasContentDescriptor("a descr 1");
		a.hasContent("a content");
		a.hasNoIngredients();

		final TargetMock b = new TargetMock("b");
		b.hasContentDescriptor("b descr 1");
		b.hasContent("b content");
		b.hasIngredients(a);

		final TargetMock c = new TargetMock("c");
		c.hasContentDescriptor("c");
		c.hasContent("c content");
		c.hasIngredients(b);

		IwantWorkspace ws = new IwantWorkspace() {
			@Override
			public List<? extends Target> targets() {
				return Arrays.asList(b, c);
			}

			@Override
			public List<? extends SideEffect> sideEffects(
					SideEffectDefinitionContext ctx) {
				return Collections.emptyList();
			}
		};

		// first a successful refresh so a cached descriptors exist
		evaluator.iwant("target/c/as-path", ws);
		assertEquals(1, c.timesPathWasCalled());
		assertEquals(1, b.timesPathWasCalled());
		assertEquals(1, a.timesPathWasCalled());

		// then time passes and ingredient b is refreshed directly
		b.hasContentDescriptor("modified b");
		Thread.sleep(1000L);
		evaluator.iwant("target/b/as-path", ws);
		assertEquals(1, c.timesPathWasCalled());
		assertEquals(2, b.timesPathWasCalled());
		assertEquals(1, a.timesPathWasCalled());

		// c as path requires a refresh even if no source ingredients were
		// touched
		evaluator.iwant("target/c/as-path", ws);
		assertEquals(2, c.timesPathWasCalled());
	}

	public void testSideEffectThatWantsAndUsesTargetsAsPaths() {
		final Target target1 = new HelloTarget("target1", "target1 content");
		final Target target2 = Concatenated.named("target2")
				.string("target2 using ").contentOf(target1).end();
		IwantWorkspace ws = new IwantWorkspace() {
			@Override
			public List<? extends Target> targets() {
				return Arrays.asList(target1, target2);
			}

			@Override
			public List<? extends SideEffect> sideEffects(
					SideEffectDefinitionContext ctx) {
				return Arrays.asList(new SideEffect() {

					@Override
					public String name() {
						return "target-wanter";
					}

					@Override
					public void mutate(SideEffectContext ctx) throws Exception {
						want(target2, ctx);
						want(target1, ctx);
					}

					private void want(final Target target1,
							SideEffectContext ctx) {
						System.err.println("Wanting " + target1);
						File cachedTarget1 = ctx.iwantAsPath(target1);
						String target1Content = FileUtil
								.contentAsString(cachedTarget1);
						System.err.println("Content:\n" + target1Content);
					}

				});
			}
		};

		evaluator.iwant("side-effect/target-wanter/effective", ws);

		StringBuilder expectedErr = new StringBuilder();
		expectedErr.append("Wanting target2\n");
		expectedErr
				.append("(0/1 D! net.sf.iwant.api.model.HelloTarget target1)\n");
		expectedErr
				.append("(0/1 D! net.sf.iwant.api.model.Concatenated target2)\n");
		expectedErr.append("Content:\n");
		expectedErr.append("target2 using target1 content\n");
		expectedErr.append("Wanting target1\n");
		expectedErr.append("Content:\n");
		expectedErr.append("target1 content\n");

		assertEquals(expectedErr.toString(), err());
		assertEquals("", out());
	}

	private class IwantPluginReferenceInSideEffectDefinition implements
			IwantWorkspace {

		@Override
		public List<? extends Target> targets() {
			return Arrays.asList(new HelloTarget("hello", "content"));
		}

		@Override
		public List<? extends SideEffect> sideEffects(
				SideEffectDefinitionContext ctx) {
			Set<JavaModule> antPluginModules = ctx.iwantPlugin().ant()
					.withDependencies();
			return Arrays.asList(new HelloSideEffect("ant-plugin-print",
					"ant-plugin modules: " + antPluginModules));
		}

	}

	public void testSideEffectThatReferencesIwantPlugin() {
		IwantWorkspace hellos = new IwantPluginReferenceInSideEffectDefinition();

		evaluator.iwant("side-effect/ant-plugin-print/effective", hellos);

		assertEquals("ant-plugin-print mutating.\n"
				+ "ant-plugin modules: [iwant-plugin-ant,"
				+ " mock-iwant-classes, ant-1.7.1.jar,"
				+ " ant-launcher-1.7.1.jar]", err.toString());
	}

}
