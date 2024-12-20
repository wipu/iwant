package org.fluentjava.iwant.entry3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.fluentjava.iwant.api.core.Concatenated;
import org.fluentjava.iwant.api.core.Concatenated.ConcatenatedBuilder;
import org.fluentjava.iwant.api.core.Directory;
import org.fluentjava.iwant.api.core.HelloSideEffect;
import org.fluentjava.iwant.api.core.HelloTarget;
import org.fluentjava.iwant.api.core.ScriptGenerated;
import org.fluentjava.iwant.api.core.SubPath;
import org.fluentjava.iwant.api.javamodules.JavaBinModule;
import org.fluentjava.iwant.api.javamodules.JavaClasses;
import org.fluentjava.iwant.api.javamodules.JavaModule;
import org.fluentjava.iwant.api.javamodules.JavaSrcModule;
import org.fluentjava.iwant.api.model.Caches;
import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.SideEffect;
import org.fluentjava.iwant.api.model.SideEffectContext;
import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.api.wsdef.SideEffectDefinitionContext;
import org.fluentjava.iwant.api.wsdef.TargetDefinitionContext;
import org.fluentjava.iwant.api.wsdef.Workspace;
import org.fluentjava.iwant.api.wsdef.WorkspaceModuleContext;
import org.fluentjava.iwant.apimocks.TargetMock;
import org.fluentjava.iwant.apimocks.WsInfoMock;
import org.fluentjava.iwant.coreservices.FileUtil;
import org.fluentjava.iwant.eclipsesettings.EclipseSettings;
import org.fluentjava.iwant.entry.Iwant;
import org.fluentjava.iwant.entry.Iwant.IwantException;
import org.fluentjava.iwant.entrymocks.IwantNetworkMock;
import org.fluentjava.iwant.testarea.TestArea;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WishEvaluatorTest {

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
			.<JavaModule> singleton(JavaBinModule
					.providing(Source.underWsroot("mock-iwant-classes")).end());
	private InputStream originalIn;
	private PrintStream originalOut;
	private PrintStream originalErr;
	private WorkspaceModuleContext wsdefCtx;
	private JavaModule wsdefdefModule;
	private File cachedIwantSrcRoot;

	@BeforeEach
	public void before() {
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
		wsdefdefModule = JavaSrcModule.with().name("wsdefdef").end();
		cachedIwantSrcRoot = testArea.newDir("cachedIwantSrcRoot");
		wsdefCtx = new WorkspaceDefinitionContextImpl(iwantApiModules,
				cachedIwantSrcRoot, wsdefdefModule);
		evaluator = new WishEvaluator(out, err, wsRoot, iwant, wsInfo, caches,
				workerCount, wsdefdefJavaModule, wsdefJavaModule, wsdefCtx);
	}

	private String out() {
		return out.toString();
	}

	private String err() {
		return err.toString();
	}

	@AfterEach
	public void after() {
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

	private void evaluateAndFail(String wish, Workspace ws,
			String expectedErrorMessage) {
		try {
			evaluator.iwant(wish, ws);
			fail();
		} catch (IllegalStateException e) {
			assertEquals(expectedErrorMessage, e.getCause().getMessage());
		}
	}

	private static class Hello implements Workspace {

		@Override
		public List<? extends Target> targets(TargetDefinitionContext ctx) {
			return Arrays.asList(new HelloTarget("hello", "hello content"));
		}

		@Override
		public List<? extends SideEffect> sideEffects(
				SideEffectDefinitionContext ctx) {
			return Collections.emptyList();
		}

	}

	private static class TwoHellos implements Workspace {

		@Override
		public List<? extends Target> targets(TargetDefinitionContext ctx) {
			return Arrays.asList(new HelloTarget("hello1", "content 1"),
					new HelloTarget("hello2", "content 2"));
		}

		@Override
		public List<? extends SideEffect> sideEffects(
				SideEffectDefinitionContext ctx) {
			return Collections.emptyList();
		}

	}

	@Test
	public void illegalWishFromHello() {
		Workspace hello = new Hello();
		try {
			evaluator.iwant("illegal/wish", hello);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("Illegal wish: illegal/wish\nlegal targets:[hello]",
					e.getMessage());
		}
		assertEquals("", out.toString());
	}

	@Test
	public void listOfTargetsFromHello() {
		Workspace hello = new Hello();
		evaluator.iwant("list-of/targets", hello);
		assertEquals("hello\n", out.toString());
	}

	@Test
	public void targetHelloAsPathFromHello() {
		Workspace hello = new Hello();

		evaluator.iwant("target/hello/as-path", hello);

		File cached = new File(asSomeone, ".i-cached/target/hello");
		assertEquals(cached + "\n", out.toString());
		assertEquals("hello content", testArea.contentOf(cached));
	}

	@Test
	public void targetHelloContentFromHello() {
		Workspace hello = new Hello();
		evaluator.iwant("target/hello/content", hello);
		assertEquals("hello content", out.toString());
	}

	@Test
	public void listOfTargetsFromTwoHellos() {
		Workspace hellos = new TwoHellos();
		evaluator.iwant("list-of/targets", hellos);
		assertEquals("hello1\nhello2\n", out.toString());
	}

	@Test
	public void targetHello1AsPathFromTwoHellos() {
		Workspace hellos = new TwoHellos();

		evaluator.iwant("target/hello1/as-path", hellos);

		File cached = new File(asSomeone, ".i-cached/target/hello1");
		assertEquals(cached + "\n", out.toString());
		assertEquals("content 1", testArea.contentOf(cached));
	}

	@Test
	public void targetHello1ContentFromTwoHellos() {
		Workspace hellos = new TwoHellos();
		evaluator.iwant("target/hello1/content", hellos);
		assertEquals("content 1", out.toString());
	}

	@Test
	public void targetHello2AsPathFromTwoHellos() {
		Workspace hellos = new TwoHellos();

		evaluator.iwant("target/hello2/as-path", hellos);

		File cached = new File(asSomeone, ".i-cached/target/hello2");
		assertEquals(cached + "\n", out.toString());
		assertEquals("content 2", testArea.contentOf(cached));
	}

	@Test
	public void targetHello2ContentFromTwoHellos() {
		Workspace hellos = new TwoHellos();
		evaluator.iwant("target/hello2/content", hellos);
		assertEquals("content 2", out.toString());
	}

	// target without a wsdef

	@Test
	public void standaloneHelloTargetContent() {
		Target target = new HelloTarget("standalone",
				"Hello from standalone target\n");
		evaluator.content(target);
		assertEquals("Hello from standalone target\n", out.toString());
	}

	// another target as ingredient

	@Test
	public void streamOfTargetThatUsesAnotherTargetStreamAsIngredient() {
		Target ingredient = new HelloTarget("ingredient", "ingredient content");
		Target target = new TargetThatNeedsAnotherAsStream("target",
				ingredient);
		evaluator.content(target);
		assertEquals("Stream using 'ingredient content' as ingredient",
				out.toString());
	}

	@Test
	public void pathToTargetThatUsesAnotherTargetStreamAsIngredient() {
		Target ingredient = new HelloTarget("ingredient", "ingredient content");
		Target target = new TargetThatNeedsAnotherAsStream("target",
				ingredient);

		evaluator.asPath(target);

		File cached = new File(asSomeone, ".i-cached/target/target");
		assertEquals(cached + "\n", out.toString());
		assertEquals("Stream using 'ingredient content' as ingredient",
				testArea.contentOf(cached));
	}

	@Test
	public void streamOfTargetThatUsesAnotherTargetPathAsIngredient() {
		Target ingredient = new HelloTarget("ingredient", "ingredient content");
		Target target = new TargetThatNeedsAnotherAsPath("target", ingredient);
		evaluator.content(target);
		assertEquals("Stream using 'ingredient content' as ingredient",
				out.toString());
	}

	@Test
	public void pathToTargetThatUsesAnotherTargetPathAsIngredient() {
		Target ingredient = new HelloTarget("ingredient", "ingredient content");
		Target target = new TargetThatNeedsAnotherAsPath("target", ingredient);

		evaluator.asPath(target);

		File cached = new File(asSomeone, ".i-cached/target/target");
		assertEquals(cached + "\n", out.toString());
		assertEquals("Stream using 'ingredient content' as ingredient",
				testArea.contentOf(cached));
	}

	// source as ingredient

	@Test
	public void streamOfTargetThatUsesASourceStreamAsIngredient() {
		testArea.hasFile("wsroot/src", "src content");
		Path ingredient = Source.underWsroot("src");
		Target target = new TargetThatNeedsAnotherAsStream("target",
				ingredient);
		evaluator.content(target);
		assertEquals("Stream using 'src content' as ingredient",
				out.toString());
	}

	@Test
	public void streamOfTargetThatUsesASourcePathAsIngredient() {
		testArea.hasFile("wsroot/src", "src content");
		Path ingredient = Source.underWsroot("src");
		Target target = new TargetThatNeedsAnotherAsPath("target", ingredient);
		evaluator.content(target);
		assertEquals("Stream using 'src content' as ingredient",
				out.toString());
	}

	// ...

	@Test
	public void targetIsRefreshedIfDescriptorOfIngredientsIngredientChanged() {
		TargetMock t1 = new TargetMock("t1");
		t1.hasNoIngredients();
		t1.hasContent("t1 content 1");
		t1.hasContentDescriptor("t1 descr 1");

		Target t2 = new TargetThatNeedsAnotherAsStream("t2", t1);
		Target t3 = new TargetThatNeedsAnotherAsStream("t3", t2);

		evaluator.asPath(t3);

		assertEquals(
				"Stream using 'Stream using 't1 content 1'"
						+ " as ingredient' as ingredient",
				testArea.contentOf(new File(asSomeone, ".i-cached/target/t3")));

		// modification:
		t1.hasContent("t1 content 2");
		t1.hasContentDescriptor("t1 descr 2");

		evaluator.asPath(t3);

		assertEquals(
				"Stream using 'Stream using 't1 content 2'"
						+ " as ingredient' as ingredient",
				testArea.contentOf(new File(asSomeone, ".i-cached/target/t3")));
	}

	// side-effects

	private static class OnlyEclipseSettingsAsSideEffect implements Workspace {

		@Override
		public List<? extends Target> targets(TargetDefinitionContext ctx) {
			return Arrays.asList(new HelloTarget("hello", "content"));
		}

		@Override
		public List<? extends SideEffect> sideEffects(
				SideEffectDefinitionContext ctx) {
			return Arrays.asList(
					EclipseSettings.with().name("eclipse-settings").end());
		}

	}

	private static class TwoSideEffects implements Workspace {

		@Override
		public List<? extends Target> targets(TargetDefinitionContext ctx) {
			return Arrays.asList(new HelloTarget("hello", "content"));
		}

		@Override
		public List<? extends SideEffect> sideEffects(
				SideEffectDefinitionContext ctx) {
			return Arrays.asList(new HelloSideEffect("hello-1"),
					new HelloSideEffect("hello-2"));
		}

	}

	@Test
	public void emptyListOfSideEffects() {
		Workspace hellos = new TwoHellos();
		evaluator.iwant("list-of/side-effects", hellos);
		assertEquals("", out.toString());
	}

	@Test
	public void listOfSideEffectsOfOnlyEclipseSettingsAsSideEffect() {
		Workspace hellos = new OnlyEclipseSettingsAsSideEffect();
		evaluator.iwant("list-of/side-effects", hellos);
		assertEquals("eclipse-settings\n", out.toString());
	}

	@Test
	public void listOfSideEffectsOfTwoSideEffects() {
		Workspace hellos = new TwoSideEffects();
		evaluator.iwant("list-of/side-effects", hellos);
		assertEquals("hello-1\nhello-2\n", out.toString());
	}

	@Test
	public void hello1EffectiveOfTwoSideEffects() {
		Workspace hellos = new TwoSideEffects();

		evaluator.iwant("side-effect/hello-1/effective", hellos);

		assertEquals("hello-1 mutating.\n", err.toString());
	}

	@Test
	public void hello2EffectiveOfTwoSideEffects() {
		Workspace hellos = new TwoSideEffects();

		evaluator.iwant("side-effect/hello-2/effective", hellos);

		assertEquals("hello-2 mutating.\n", err.toString());
	}

	@Test
	public void deletionOfJavaFileIsDetectedAndCompilationIsRetriedAndFailed() {
		File srcDir = new File(wsRoot, "src");
		Iwant.textFileEnsuredToHaveContent(new File(srcDir, "pak1/Caller.java"),
				"package pak1;\npublic class Caller {pak2.Callee callee;}");
		File calleeJava = new File(srcDir, "pak2/Callee.java");
		Iwant.textFileEnsuredToHaveContent(calleeJava,
				"package pak2;\npublic class Callee {}");
		Source src = Source.underWsroot("src");
		Target target = JavaClasses.with().name("multiple").srcDirs(src)
				.classLocations().end();

		evaluator.asPath(target);

		assertTrue(new File(caches.contentOf(target), "pak1/Caller.class")
				.exists());
		assertTrue(new File(caches.contentOf(target), "pak2/Callee.class")
				.exists());

		// no retry with Callee.java missing
		Iwant.del(calleeJava);

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

	@Test
	public void twoTargetsReferToSamePathButOnlyTheOneThatDeclaresItAsIngredientSucceeds() {
		Path src = new HelloTarget("src", "src content");

		ConcatenatedBuilder correctSpex = Concatenated.named("correct");
		correctSpex.nativePathTo(src);
		Target correct = correctSpex.end();

		Target incorrect = new TargetThatForgetsToDeclareAnIngredient(
				"incorrect", src);

		Target root = Concatenated.named("root").nativePathTo(correct)
				.nativePathTo(incorrect).end();

		try {
			evaluator.asPath(root);
			fail();
		} catch (IwantException e) {
			assertEquals("Target incorrect referred to src without"
					+ " declaring it an ingredient.", e.getMessage());
		}

		// correct shall succeed:
		assertEquals(asSomeone + "/.i-cached/target/src", testArea
				.contentOf(new File(asSomeone, ".i-cached/target/correct")));

		// incorrect shall not even produce the file:
		assertFalse(new File(asSomeone, ".i-cached/target/incorrect").exists());
	}

	/**
	 * Even though planner has been tested, TargetRefreshTask used to
	 * synchronize too much, preventing concurrency from working properly
	 * end-to-end
	 */
	@Test
	public void concurrencyWorksEndToEnd() throws InterruptedException {
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

		assertEquals("part1", testArea
				.contentOf(new File(asSomeone, ".i-cached/target/part1")));
		assertEquals("part2", testArea
				.contentOf(new File(asSomeone, ".i-cached/target/part2")));
		assertEquals("part3", testArea
				.contentOf(new File(asSomeone, ".i-cached/target/part3")));
		assertEquals("part4", testArea
				.contentOf(new File(asSomeone, ".i-cached/target/part4")));
		assertEquals("listOfParts", testArea.contentOf(
				new File(asSomeone, ".i-cached/target/listOfParts")));
	}

	@Test
	public void contextUsesThreadNameToMakeSureAllWorkersGetOwnTemporaryDirectory()
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

		File tempsForThisWs = new File(Iwant.IWANT_GLOBAL_TMP_DIR,
				asSomeone + "/.i-cached/temp");
		assertEquals(new File(tempsForThisWs, "worker-1"), results.get(0));
		assertEquals(new File(tempsForThisWs, "worker-2"), results.get(1));
	}

	@Test
	public void scriptsWorkCorrectlyInParallel() {
		final int workerCount = 8;
		evaluator = new WishEvaluator(out, err, wsRoot, iwant, wsInfo, caches,
				workerCount, wsdefdefJavaModule, wsdefJavaModule, wsdefCtx);

		final int partCount = 20;
		List<Target> parts = new ArrayList<>();
		ConcatenatedBuilder listOfPartsSpex = Concatenated.named("listOfParts");
		for (int i = 0; i < partCount; i++) {
			ConcatenatedBuilder scriptContent = Concatenated
					.named("script-" + i);
			scriptContent.string("#!/bin/bash\n");
			scriptContent.string("set -eu\n");
			scriptContent.string("DEST=$1\n");
			scriptContent.string("echo " + i + " > \"$DEST\"\n");
			Concatenated script = scriptContent.end();
			Target part = ScriptGenerated.named("part-" + i).byScript(script);
			parts.add(part);
			listOfPartsSpex = listOfPartsSpex.unixPathTo(part);
		}
		Target listOfParts = listOfPartsSpex.end();

		evaluator.asPath(listOfParts);

		for (int i = 0; i < partCount; i++) {
			assertEquals(i + "\n", testArea.contentOf(
					new File(asSomeone, ".i-cached/target/part-" + i)));
		}
	}

	@Test
	public void sideEffectDefinitionContextPassesIwantApiClasses() {
		final AtomicReference<Set<? extends JavaModule>> fromCtx = new AtomicReference<>();
		Workspace ws = new Workspace() {

			@Override
			public List<? extends Target> targets(TargetDefinitionContext ctx) {
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

	@Test
	public void sameNameOnPathAndTargetCausesErrorAtListOfOrAsPath() {
		Workspace ws = new Workspace() {

			@Override
			public List<? extends Target> targets(TargetDefinitionContext ctx) {
				return Arrays.asList(targetA(), targetB());
			}

			private Target targetA() {
				ConcatenatedBuilder a = Concatenated.named("a");
				a.nativePathTo(sourceB());
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
					+ " class org.fluentjava.iwant.api.core.HelloTarget\n"
					+ "and another is of\n"
					+ " class org.fluentjava.iwant.api.model.Source",
					e.getMessage());
		}

	}

	/**
	 * Bug: only source timestamps were checked, so if a refresh is interrupted,
	 * it's not retried because no source ingredients are newer than the cached
	 * descriptor, only cached target files.
	 */
	@Test
	public void targetWithNonFreshTargetIngredientAndExistingCachedDescriptorStaysNonFreshEvenIfItFails() {
		TargetMock ingredient = new TargetMock("ingredient");
		ingredient.hasContentDescriptor("ingredient descr 1");
		ingredient.hasContent("ingredient content");
		ingredient.hasNoIngredients();

		final TargetMock target = new TargetMock("failing");
		target.hasContentDescriptor("failing");
		target.hasContent("failing content");
		target.hasIngredients(ingredient);

		Workspace ws = new Workspace() {
			@Override
			public List<? extends Target> targets(TargetDefinitionContext ctx) {
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

	@Test
	public void targetRefreshIsRetriedEvenIfReasonWasDirtinessOfIngredientAndItsRefreshWasInterruptedEarlier() {
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

		Workspace ws = new Workspace() {
			@Override
			public List<? extends Target> targets(TargetDefinitionContext ctx) {
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
	@Test
	public void targetIsRefreshedEvenIfATargetIngredientWasRefreshedDuringEarlierRun()
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

		Workspace ws = new Workspace() {
			@Override
			public List<? extends Target> targets(TargetDefinitionContext ctx) {
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

	@Test
	public void sideEffectThatWantsAndUsesTargetsAsPaths() {
		final Target target1 = new HelloTarget("target1", "target1 content");
		final Target target2 = Concatenated.named("target2")
				.string("target2 using ").contentOf(target1).end();
		Workspace ws = new Workspace() {
			@Override
			public List<? extends Target> targets(TargetDefinitionContext ctx) {
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
						File cachedTarget1 = ctx.iwantFreshCached(target1);
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
		expectedErr.append(
				"(0/1 D! org.fluentjava.iwant.api.core.HelloTarget target1)\n");
		expectedErr.append(
				"(0/1 D! org.fluentjava.iwant.api.core.Concatenated target2)\n");
		expectedErr.append("Content:\n");
		expectedErr.append("target2 using target1 content\n");
		expectedErr.append("Wanting target1\n");
		expectedErr.append("Content:\n");
		expectedErr.append("target1 content\n");

		assertEquals(expectedErr.toString(), err());
		assertEquals("", out());
	}

	private static class IwantPluginReferenceInSideEffectDefinition
			implements Workspace {

		@Override
		public List<? extends Target> targets(TargetDefinitionContext ctx) {
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

	@Test
	public void sideEffectThatReferencesIwantPlugin() {
		Workspace hellos = new IwantPluginReferenceInSideEffectDefinition();

		evaluator.iwant("side-effect/ant-plugin-print/effective", hellos);

		assertEquals("ant-plugin-print mutating.\n"
				+ "ant-plugin modules: [iwant-plugin-ant,"
				+ " mock-iwant-classes, ant-1.10.14.jar,"
				+ " ant-launcher-1.10.14.jar]", err.toString());
	}

	private static class WorkspaceWithTarget implements Workspace {

		private final Target target;

		WorkspaceWithTarget(Target target) {
			this.target = target;
		}

		@Override
		public List<? extends Target> targets(TargetDefinitionContext ctx) {
			return Arrays.asList(target);
		}

		@Override
		public List<? extends SideEffect> sideEffects(
				SideEffectDefinitionContext ctx) {
			return Collections.emptyList();
		}

	}

	/**
	 * More cases are tested in TargetNameCheckerTest
	 */
	@Test
	public void illegalTargetNameCausesFailure() {
		WorkspaceWithTarget ws = new WorkspaceWithTarget(
				new HelloTarget("a::b", ""));
		try {
			evaluator.iwant("list-of/targets", ws);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals(
					"Name contains double colon (breaks TargetImplementedInBash): a::b",
					e.getMessage());
		}
	}

	/**
	 * SubPath used to copy but it was unnecessary. This tests no copying
	 * happens. It also tests the already cached parent is not deleted before
	 * refreshing the SubPath.
	 */
	@Test
	public void subPathPointsToUnderParentAndItHasCorrectCachedContent()
			throws Exception {
		Target hello = new HelloTarget("hello", "hello content");
		Directory parent = Directory.named("parent").dir("sub").copyOf(hello)
				.named("hello2").end().end().end();
		SubPath sub = new SubPath("sub", parent, "sub");

		WorkspaceWithTarget ws = new WorkspaceWithTarget(sub);

		evaluator.iwant("target/sub/as-path", ws);

		assertEquals(caches.contentOf(parent) + "/sub\n", out());
		File cachedSub = caches.contentOf(sub);
		assertEquals("hello content",
				testArea.contentOf(new File(cachedSub, "hello2")));
	}

}
