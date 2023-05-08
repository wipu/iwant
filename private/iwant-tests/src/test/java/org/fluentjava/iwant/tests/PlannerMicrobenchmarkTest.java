package org.fluentjava.iwant.tests;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.fluentjava.iwant.api.model.TargetEvaluationContext;
import org.fluentjava.iwant.api.target.TargetBase;
import org.fluentjava.iwant.apimocks.CachesMock;
import org.fluentjava.iwant.apimocks.TargetEvaluationContextMock;
import org.fluentjava.iwant.entry.Iwant;
import org.fluentjava.iwant.entry3.IngredientCheckingTargetEvaluationContext.ReferenceLegalityCheckCache;
import org.fluentjava.iwant.entry3.TargetRefreshTask;
import org.fluentjava.iwant.planner.Planner;
import org.fluentjava.iwant.plannerapi.Task;
import org.fluentjava.iwant.testarea.TestArea;
import org.junit.Before;
import org.junit.Test;

public class PlannerMicrobenchmarkTest {

	private static final Random RND = new Random();
	private TestArea testArea;
	private File wsRoot;
	private CachesMock caches;
	private Iwant iwant;
	private TargetEvaluationContextMock ctx;
	private File cachedDescriptors;
	private File cachedModifiableTargets;
	private int targetCount;
	private AtomicInteger targetRefreshCount;

	@Before
	public void setUp() {
		testArea = TestArea.forTest(this);
		wsRoot = testArea.newDir("wsroot");
		caches = new CachesMock(wsRoot);
		iwant = Iwant.usingRealNetwork();
		ctx = new TargetEvaluationContextMock(iwant, caches);
		cachedDescriptors = new File(testArea.root(), "cachedDescriptors");
		cachedModifiableTargets = new File(testArea.root(),
				"cachedModifiableTargets");
		caches.cachesDesciptorsAt(cachedDescriptors);
		caches.cachesModifiableTargetsAt(cachedModifiableTargets);
		targetCount = 0;
		targetRefreshCount = new AtomicInteger(0);
	}

	@Test
	public void refreshingBigRandomTaskTreeWithRandomParallelism() {
		int levelCount = 4;
		int targetsPerLevel = 200;
		double nonParallelProbability = 0.1D;
		double depProbability = 0.2D;

		List<T> lowerLevelTargets = new ArrayList<>();
		List<T> currentLevel = new ArrayList<>();
		int globalDepCount = 0;
		for (int level = 0; level < levelCount; level++) {
			currentLevel.clear();
			for (int i = 0; i < targetsPerLevel; i++) {
				T t = new T(level + "-" + i,
						randomBoolean(nonParallelProbability));
				for (T depCandidate : lowerLevelTargets) {
					if (randomBoolean(depProbability)) {
						t.addDep(depCandidate);
						globalDepCount++;
					}
				}
				currentLevel.add(t);
			}
			lowerLevelTargets.addAll(currentLevel);
		}

		T root = new T("root", true);
		for (T dep : currentLevel) {
			root.addDep(dep);
		}

		System.err.println("Target count: " + targetCount);
		System.err.println("Global dep count: " + globalDepCount);
		long t1 = System.currentTimeMillis();

		Task rootTask = TargetRefreshTask.instance(root, ctx, caches,
				new HashMap<>(), new ReferenceLegalityCheckCache());
		Planner planner = new Planner(rootTask, 4);

		logTime("TargetRefreshTask and Planner creation", t1);

		t1 = System.currentTimeMillis();
		planner.start();
		planner.join();
		logTime("Planner start and join", t1);

		assertEquals(levelCount * targetsPerLevel + 1/* one for root */,
				targetRefreshCount.get());
	}

	private static void logTime(String msg, long t1) {
		long t2 = System.currentTimeMillis();
		System.err.println(msg + " took " + (t2 - t1) + "ms.");
	}

	private static boolean randomBoolean(double probability) {
		double rnd = RND.nextDouble();
		return rnd < probability;
	}

	private class T extends TargetBase {

		private final boolean supportsParallelism;
		private final List<T> deps = new ArrayList<>();

		public T(String name, boolean supportsParallelism) {
			super(name);
			this.supportsParallelism = supportsParallelism;
			targetCount++;
		}

		public void addDep(T dep) {
			deps.add(dep);
		}

		@Override
		protected IngredientsAndParametersDefined ingredientsAndParameters(
				IngredientsAndParametersPlease iUse) {
			for (T dep : deps) {
				iUse.ingredients(dep.name(), dep);
			}
			return iUse.nothingElse();
		}

		@Override
		public boolean supportsParallelism() {
			return supportsParallelism;
		}

		@Override
		public void path(TargetEvaluationContext ctx) throws Exception {
			File dest = ctx.cached(this);
			System.err.println(
					Thread.currentThread().getName() + " refreshing " + name());
			Iwant.mkdirs(dest);
			for (T dep : deps) {
				File cachedDep = ctx.cached(dep);
				FileUtils.writeStringToFile(new File(dest, dep.name()),
						cachedDep.getAbsolutePath());
			}
			targetRefreshCount.incrementAndGet();
		}

	}

}