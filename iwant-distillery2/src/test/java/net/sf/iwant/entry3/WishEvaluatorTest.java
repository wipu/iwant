package net.sf.iwant.entry3;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import net.sf.iwant.api.EclipseSettings;
import net.sf.iwant.api.HelloSideEffect;
import net.sf.iwant.api.HelloTarget;
import net.sf.iwant.api.IwantWorkspace;
import net.sf.iwant.api.JavaClasses;
import net.sf.iwant.api.JavaModule;
import net.sf.iwant.api.Path;
import net.sf.iwant.api.SideEffect;
import net.sf.iwant.api.SideEffectDefinitionContext;
import net.sf.iwant.api.Source;
import net.sf.iwant.api.Target;
import net.sf.iwant.api.WsInfoMock;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.IwantNetworkMock;

public class WishEvaluatorTest extends TestCase {

	private IwantEntry3TestArea testArea;
	private File asSomeone;
	private File wsRoot;
	private ByteArrayOutputStream out;
	private ByteArrayOutputStream err;
	private WishEvaluator evaluator;
	private IwantNetworkMock network;
	private Iwant iwant;
	private WsInfoMock wsInfo;
	private Caches caches;
	private JavaModule wsdefdefJavaModule;
	private JavaModule wsdefJavaModule;

	@Override
	public void setUp() throws IOException {
		testArea = new IwantEntry3TestArea();
		network = new IwantNetworkMock(testArea);
		iwant = Iwant.using(network);
		asSomeone = testArea.newDir("as-" + getClass().getSimpleName());
		wsRoot = testArea.newDir("wsroot");
		out = new ByteArrayOutputStream();
		err = new ByteArrayOutputStream();
		wsInfo = new WsInfoMock();
		wsInfo.hasWsRoot(wsRoot);
		wsdefdefJavaModule = JavaModule.with().name("wsdefdef")
				.locationUnderWsRoot("wsdefdef").mainJava("src").end();
		wsdefJavaModule = JavaModule.with().name("wsdef")
				.locationUnderWsRoot("wsdef").mainJava("src").end();
		caches = new CachesImpl(new File(asSomeone, ".i-cached"),
				wsInfo.wsRoot(), network);
		int workerCount = 1;
		evaluator = new WishEvaluator(out, err, wsRoot, iwant, wsInfo, caches,
				workerCount, wsdefdefJavaModule, wsdefJavaModule);
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

	// file laziness

	public void sourceNeedsNoRefresh() {
		testArea.hasFile("src", "does not matter");
		Source src = Source.underWsroot("src");
		assertFalse(evaluator.needsRefreshing(src));

		evaluator.asPath(src);

		assertFalse(evaluator.needsRefreshing(src));
	}

	public void testSecondAsPathCausesNoRefresh() {
		TargetMock target = new TargetMock("ingredientless");
		target.hasNoIngredients();
		target.hasContent("ingredientless content");
		target.hasContentDescriptor("ingredientless descr");
		assertTrue(evaluator.needsRefreshing(target));
		evaluator.asPath(target);

		target.shallNotBeToldToWriteFile();
		assertFalse(evaluator.needsRefreshing(target));
		evaluator.asPath(target);

		assertEquals("ingredientless content", testArea.contentOf(new File(
				asSomeone, ".i-cached/target/ingredientless")));
	}

	public void testSecondAsPathCausesRefreshWhenDescriptorChanges() {
		TargetMock target = new TargetMock("ingredientless");
		target.hasNoIngredients();
		target.hasContent("content 1");
		target.hasContentDescriptor("descr 1");
		assertTrue(evaluator.needsRefreshing(target));
		evaluator.asPath(target);

		target.hasContent("content 2");
		target.hasContentDescriptor("descr 2");
		assertTrue(evaluator.needsRefreshing(target));
		evaluator.asPath(target);

		assertEquals("content 2", testArea.contentOf(new File(asSomeone,
				".i-cached/target/ingredientless")));
	}

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

	public void testDeletionOfJavaFileIsDetectedAndCompilationIsRetriedAndFailed()
			throws Exception {
		File srcDir = new File(wsRoot, "src");
		new File(srcDir, "pak1").mkdirs();
		new File(srcDir, "pak2").mkdirs();
		new FileWriter(new File(srcDir, "pak1/Caller.java")).append(
				"package pak1;\npublic class Caller {pak2.Callee callee;}")
				.close();
		File calleeJava = new File(srcDir, "pak2/Callee.java");
		new FileWriter(calleeJava).append(
				"package pak2;\npublic class Callee {}").close();
		Source src = Source.underWsroot("src");
		Target target = new JavaClasses("multiple", src,
				Collections.<Path> emptyList());

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
		} catch (IllegalStateException e) {
			assertEquals("Compilation failed.", e.getCause().getMessage());
		}

		assertFalse(new File(caches.contentOf(target), "pak1/Caller.class")
				.exists());
		assertFalse(new File(caches.contentOf(target), "pak2/Callee.class")
				.exists());
	}

}
