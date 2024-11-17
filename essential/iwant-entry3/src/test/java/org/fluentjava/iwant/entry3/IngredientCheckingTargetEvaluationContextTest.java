package org.fluentjava.iwant.entry3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;

import org.fluentjava.iwant.api.core.HelloTarget;
import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.apimocks.CachesMock;
import org.fluentjava.iwant.apimocks.TargetEvaluationContextMock;
import org.fluentjava.iwant.apimocks.TargetMock;
import org.fluentjava.iwant.entry.Iwant;
import org.fluentjava.iwant.entry.Iwant.IwantException;
import org.fluentjava.iwant.entry.Iwant.IwantNetwork;
import org.fluentjava.iwant.entry3.IngredientCheckingTargetEvaluationContext.ReferenceLegalityCheckCache;
import org.fluentjava.iwant.entrymocks.IwantNetworkMock;
import org.fluentjava.iwant.testarea.TestArea;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class IngredientCheckingTargetEvaluationContextTest {

	private Iwant iwant;
	private IwantNetwork network;
	private CachesMock caches;
	private File wsRoot;
	private TargetEvaluationContextMock delegate;
	private TargetMock target;
	private IngredientCheckingTargetEvaluationContext ctx;

	@BeforeEach
	public void before() {
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
		ReferenceLegalityCheckCache refLegalityCheckCache = new ReferenceLegalityCheckCache();
		ctx = new IngredientCheckingTargetEvaluationContext(target, delegate,
				refLegalityCheckCache);
	}

	@Test
	public void referenceToCachedSelfIsDelegated() {
		target.hasNoIngredients();
		assertEquals(delegate.cached(target), ctx.cached(target));
	}

	/**
	 * In some weird situation a target may refer to another instance of itself
	 * so no ingredient check done here.
	 */
	@Test
	public void referenceToCopyOfCachedSelfIsDelegated() {
		target.hasNoIngredients();
		assertEquals(delegate.cached(target),
				ctx.cached(new TargetMock("target")));
	}

	/**
	 * With current design it would be too much to require proper equals from
	 * all Target implementations.
	 */
	@Test
	public void comparisonUsesNameSoEvenADifferentPathWithSameNameIsInterpretedAsTargetItself() {
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

	@Test
	public void referenceToIngredientIsDelegated() {
		TargetMock ingredient = new TargetMock("ingredient");
		target.hasIngredients(ingredient);
		assertEquals(delegate.cached(ingredient), ctx.cached(ingredient));
	}

	@Test
	public void referenceToNonIngredientCausesAFailure() {
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
	@Test
	public void referenceToIngredientOfIngredientIsDelegated() {
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
	@Test
	public void ingredientOfIngredientIsAlsoComparedByNameNotEquality() {
		TargetMock ingredientOfIngredient = new TargetMock(
				"ingredient-of-ingredient");
		ingredientOfIngredient.hasNoIngredients();
		TargetMock ingredient = new TargetMock("ingredient");
		ingredient.hasIngredients(ingredientOfIngredient);

		target.hasIngredients(ingredient);

		assertEquals(delegate.cached(ingredientOfIngredient),
				ctx.cached(ingredientOfIngredient));
	}

	@Test
	public void nullIngredientIsReportedAsFriendlyException() {
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

	@Test
	public void otherMethodsAreJustDelegated() {
		assertSame(delegate.iwant(), ctx.iwant());
		assertSame(delegate.wsRoot(), ctx.wsRoot());
		assertSame(delegate.freshTemporaryDirectory(),
				ctx.freshTemporaryDirectory());
	}

}
