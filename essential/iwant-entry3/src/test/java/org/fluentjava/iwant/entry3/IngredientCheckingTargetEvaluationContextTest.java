package org.fluentjava.iwant.entry3;

import java.io.File;

import junit.framework.TestCase;
import org.fluentjava.iwant.api.core.HelloTarget;
import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.apimocks.CachesMock;
import org.fluentjava.iwant.apimocks.TargetEvaluationContextMock;
import org.fluentjava.iwant.apimocks.TargetMock;
import org.fluentjava.iwant.entry.Iwant;
import org.fluentjava.iwant.entry.Iwant.IwantException;
import org.fluentjava.iwant.entry.Iwant.IwantNetwork;
import org.fluentjava.iwant.entrymocks.IwantNetworkMock;
import org.fluentjava.iwant.testarea.TestArea;

public class IngredientCheckingTargetEvaluationContextTest extends TestCase {

	private Iwant iwant;
	private IwantNetwork network;
	private CachesMock caches;
	private File wsRoot;
	private TargetEvaluationContextMock delegate;
	private TargetMock target;
	private IngredientCheckingTargetEvaluationContext ctx;

	@Override
	public void setUp() {
		TestArea testArea = TestArea.forTest(this);
		network = new IwantNetworkMock(testArea);
		iwant = Iwant.using(network);
		wsRoot = testArea.root();
		caches = new CachesMock(wsRoot);
		caches.cachesModifiableTargetsAt(testArea.newDir("cached-targets"));
		caches.providesTemporaryDirectoryAt(testArea.newDir("tempDir"));
		delegate = new TargetEvaluationContextMock(iwant, caches);
		delegate.hasWsRoot(wsRoot);
		target = new TargetMock("target");
		ctx = new IngredientCheckingTargetEvaluationContext(target, delegate);
	}

	public void testReferenceToCachedSelfIsDelegated() {
		target.hasNoIngredients();
		assertEquals(delegate.cached(target), ctx.cached(target));
	}

	/**
	 * In some weird situation a target may refer to another instance of itself
	 * so no ingredient check done here.
	 */
	public void testReferenceToCopyOfCachedSelfIsDelegated() {
		target.hasNoIngredients();
		assertEquals(delegate.cached(target),
				ctx.cached(new TargetMock("target")));
	}

	/**
	 * With current design it would be too much to require proper equals from
	 * all Target implementations.
	 */
	public void testComparisonUsesNameSoEvenADifferentPathWithSameNameIsInterpretedAsTargetItself() {
		target.hasNoIngredients();

		HelloTarget differentTargetImplWithSameName = new HelloTarget("target",
				"whatever");
		assertEquals(delegate.cached(differentTargetImplWithSameName),
				ctx.cached(differentTargetImplWithSameName));

		// even Source is ok
		Source sourceWithSameName = Source.underWsroot("target");
		assertEquals(delegate.cached(sourceWithSameName),
				ctx.cached(sourceWithSameName));
	}

	public void testReferenceToIngredientIsDelegated() {
		TargetMock ingredient = new TargetMock("ingredient");
		target.hasIngredients(ingredient);
		assertEquals(delegate.cached(ingredient), ctx.cached(ingredient));
	}

	public void testReferenceToNonIngredientCausesAFailure() {
		target.hasNoIngredients();
		try {
			ctx.cached(new TargetMock("implicit ingredient"));
			fail();
		} catch (IwantException e) {
			assertEquals(
					"Target target referred to implicit ingredient without "
							+ "declaring it an ingredient.",
					e.getMessage());
		}
	}

	/**
	 * It's ok to use ingredients of ingredients, otherwise you would have to
	 * declare everything recursively
	 */
	public void testReferenceToIngredientOfIngredientIsDelegated() {
		TargetMock ingredientOfIngredient = new TargetMock(
				"ingredient-of-ingredient");
		ingredientOfIngredient.hasNoIngredients();
		TargetMock ingredient = new TargetMock("ingredient");
		ingredient.hasIngredients(ingredientOfIngredient);

		target.hasIngredients(ingredient);

		Path copyOfIngredientOfIngredient = Source
				.underWsroot("ingredient-of-ingredient");
		assertEquals(delegate.cached(copyOfIngredientOfIngredient),
				ctx.cached(copyOfIngredientOfIngredient));
	}

	/**
	 * See the other name related test for explanation
	 */
	public void testIngredientOfIngredientIsAlsoComparedByNameNotEquality() {
		TargetMock ingredientOfIngredient = new TargetMock(
				"ingredient-of-ingredient");
		ingredientOfIngredient.hasNoIngredients();
		TargetMock ingredient = new TargetMock("ingredient");
		ingredient.hasIngredients(ingredientOfIngredient);

		target.hasIngredients(ingredient);

		assertEquals(delegate.cached(ingredientOfIngredient),
				ctx.cached(ingredientOfIngredient));
	}

	public void testNullIngredientIsReportedAsFriendlyException() {
		TargetMock ingredient = new TargetMock("ingredient");
		ingredient.hasIngredients(new Path[] { null });
		target.hasIngredients(ingredient);
		try {
			ctx.cached(new TargetMock("something"));
			fail();
		} catch (IwantException e) {
			assertEquals("Path 'ingredient' has a null ingredient.",
					e.getMessage());
		}
	}

	public void testOtherMethodsAreJustDelegated() {
		assertSame(delegate.iwant(), ctx.iwant());
		assertSame(delegate.wsRoot(), ctx.wsRoot());
		assertSame(delegate.freshTemporaryDirectory(),
				ctx.freshTemporaryDirectory());
	}

}
