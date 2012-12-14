package net.sf.iwant.entry3;

import java.io.File;

import junit.framework.TestCase;
import net.sf.iwant.api.HelloTarget;
import net.sf.iwant.api.Source;
import net.sf.iwant.api.TargetEvaluationContextMock;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.IwantException;
import net.sf.iwant.entry.Iwant.IwantNetwork;
import net.sf.iwant.entry.IwantNetworkMock;
import net.sf.iwant.testarea.TestArea;

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
		TestArea testArea = new IwantEntry3TestArea();
		network = new IwantNetworkMock(testArea);
		iwant = Iwant.using(network);
		wsRoot = testArea.root();
		caches = new CachesMock(wsRoot);
		caches.cachesModifiableTargetsAt(testArea.newDir("cached-targets"));
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
							+ "declaring it as an ingredient.", e.getMessage());
		}
	}

	public void testOtherMethodsAreJustDelegated() {
		assertSame(delegate.iwant(), ctx.iwant());
		assertSame(delegate.wsRoot(), ctx.wsRoot());
	}

}
