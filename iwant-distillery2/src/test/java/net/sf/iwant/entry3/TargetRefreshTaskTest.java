package net.sf.iwant.entry3;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import junit.framework.TestCase;
import net.sf.iwant.api.ExternalSource;
import net.sf.iwant.api.Path;
import net.sf.iwant.api.Source;
import net.sf.iwant.api.Target;
import net.sf.iwant.api.TargetEvaluationContextMock;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.IwantNetwork;
import net.sf.iwant.entry.IwantNetworkMock;
import net.sf.iwant.planner.Resource;
import net.sf.iwant.planner.ResourcePool;

public class TargetRefreshTaskTest extends TestCase {

	private IwantEntry3TestArea testArea;
	private IwantNetwork network;
	private Iwant iwant;
	private TargetEvaluationContextMock ctx;
	private File cachedTarget;
	private File cachedDescriptors;
	private CachesMock caches;
	private File wsRoot;

	@Override
	public void setUp() {
		testArea = new IwantEntry3TestArea();
		network = new IwantNetworkMock(testArea);
		iwant = Iwant.using(network);
		wsRoot = testArea.newDir("wsroot");
		caches = new CachesMock(wsRoot);
		ctx = new TargetEvaluationContextMock(iwant, caches);
		cachedTarget = testArea.newDir("cached");
		caches.cachesModifiableTargetsAt(cachedTarget);
		cachedDescriptors = testArea.newDir("cached-descriptor");
		caches.cachesDesciptorsAt(cachedDescriptors);
	}

	private void cacheContainsContentOf(Target target) {
		ctx.cached(target).mkdir();
	}

	private void cacheContainsFreshDescriptor(Target target) {
		cacheContainsDescriptor(target, target.contentDescriptor());
	}

	private void cacheContainsDescriptor(Target target, String cachedDescriptor) {
		try {
			new FileWriter(new File(cachedDescriptors, target.name())).append(
					cachedDescriptor).close();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	public void testTaskNameIsTargetsName() {
		TargetMock t1 = new TargetMock("t1");
		t1.hasNoIngredients();
		TargetMock t2 = new TargetMock("t2");
		t2.hasNoIngredients();

		assertEquals("t1", new TargetRefreshTask(t1, ctx, caches).name());
		assertEquals("t2", new TargetRefreshTask(t2, ctx, caches).name());
	}

	public void testTaskGetter() {
		TargetMock target = new TargetMock("target");
		target.hasNoIngredients();

		TargetRefreshTask task = new TargetRefreshTask(target, ctx, caches);

		assertSame(target, task.target());
	}

	public void testTaskOfIngredientlessTargetHasNoDeps() {
		TargetMock target = new TargetMock("target");
		target.hasNoIngredients();

		TargetRefreshTask task = new TargetRefreshTask(target, ctx, caches);

		assertTrue(task.dependencies().isEmpty());
	}

	public void testTaskDependenciesAreRefreshTasksOfTargetIngredientsButNotSource() {
		Path srcIngredient = Source.underWsroot("src-ingredient");
		TargetMock targetIngredient = new TargetMock("target-ingredient");
		targetIngredient.hasNoIngredients();

		TargetMock target = new TargetMock("target");
		target.hasIngredients(Arrays.asList(srcIngredient, targetIngredient));

		TargetRefreshTask task = new TargetRefreshTask(target, ctx, caches);

		assertEquals(1, task.dependencies().size());
		TargetRefreshTask depTask = (TargetRefreshTask) task.dependencies()
				.iterator().next();
		assertSame(targetIngredient, depTask.target());
	}

	public void testTaskIsDirtyIfCachedTargetContentIsMissingEvenIfDescriptorDoes() {
		TargetMock target = new TargetMock("target");
		target.hasNoIngredients();
		target.hasContentDescriptor("descr");
		// no cached content
		cacheContainsFreshDescriptor(target);

		assertTrue(new TargetRefreshTask(target, ctx, caches).isDirty());
	}

	public void testIngredientlessTaskIsNotDirtyIfCachedTargetContentAndDescriptorExist() {
		TargetMock target = new TargetMock("target");
		target.hasNoIngredients();
		target.hasContentDescriptor("descr");
		cacheContainsContentOf(target);
		cacheContainsFreshDescriptor(target);

		assertFalse(new TargetRefreshTask(target, ctx, caches).isDirty());
	}

	public void testNoResourcesRequiredIfTargetDoesNotRequireThem() {
		TargetMock target = new TargetMock("target");
		target.hasNoIngredients();

		assertTrue(new TargetRefreshTask(target, ctx, caches)
				.requiredResources().isEmpty());
	}

	public void testRefreshWritesCachedContent() {
		TargetMock target = new TargetMock("target");
		target.hasNoIngredients();
		target.hasContent("target content");
		target.hasContentDescriptor("target descriptor");

		TargetRefreshTask task = new TargetRefreshTask(target, ctx, caches);
		task.refresh(Collections.<ResourcePool, Resource> emptyMap());

		assertEquals("target content", testArea.contentOf("cached/target"));
	}

	public void testRefreshWritesCachedContentDescriptor() {
		TargetMock target = new TargetMock("target");
		target.hasNoIngredients();
		target.hasContent("target content");
		target.hasContentDescriptor("target descriptor");

		TargetRefreshTask task = new TargetRefreshTask(target, ctx, caches);
		task.refresh(Collections.<ResourcePool, Resource> emptyMap());

		assertEquals("target descriptor",
				testArea.contentOf("cached-descriptor/target"));
	}

	public void testTaskIsDirtyIfDescriptorChanged() {
		TargetMock target = new TargetMock("target");
		target.hasNoIngredients();
		target.hasContentDescriptor("new-descriptor");
		cacheContainsContentOf(target);
		cacheContainsDescriptor(target, "old-descriptor");

		TargetRefreshTask task = new TargetRefreshTask(target, ctx, caches);

		assertTrue(task.isDirty());
	}

	public void testTaskIsDirtyIfCachedDescriptorIsMissing() {
		TargetMock target = new TargetMock("target");
		target.hasNoIngredients();
		target.hasContentDescriptor("new-descriptor");
		cacheContainsContentOf(target);
		// no cached descriptor

		TargetRefreshTask task = new TargetRefreshTask(target, ctx, caches);

		assertTrue(task.isDirty());
	}

	public void testTaskIsNotDirtyIfDescriptorNotChanged() {
		TargetMock target = new TargetMock("target");
		target.hasNoIngredients();
		target.hasContentDescriptor("current");
		cacheContainsContentOf(target);
		cacheContainsDescriptor(target, "current");

		TargetRefreshTask task = new TargetRefreshTask(target, ctx, caches);

		assertFalse(task.isDirty());
	}

	public void testTaskIsDirtyIfFileUnderSourceDirWasModifiedAfterDescriptor()
			throws IOException {
		File srcDir = testArea.newDir("src");
		File srcFile = testArea.hasFile("src/src-file", "src-content");
		srcFile.setLastModified(System.currentTimeMillis() + 2000);
		// srcDir itself hasn't been modified, only the file under it:
		srcDir.setLastModified(System.currentTimeMillis() - 2000);

		TargetMock target = new TargetMock("target");
		target.hasIngredients(new ExternalSource(srcDir));
		target.hasContentDescriptor("current");
		cacheContainsContentOf(target);
		cacheContainsDescriptor(target, "current");

		TargetRefreshTask task = new TargetRefreshTask(target, ctx, caches);

		assertTrue(task.isDirty());
	}

	public void testCleanTaskWithSourceIngredient() throws IOException {
		File srcDir = testArea.newDir("src");
		File srcFile = testArea.hasFile("src/src-file", "src-content");
		srcFile.setLastModified(System.currentTimeMillis() - 2000);
		srcDir.setLastModified(System.currentTimeMillis() - 2000);

		TargetMock target = new TargetMock("target");
		target.hasIngredients(new ExternalSource(srcDir));
		target.hasContentDescriptor("current");
		cacheContainsContentOf(target);
		cacheContainsDescriptor(target, "current");

		TargetRefreshTask task = new TargetRefreshTask(target, ctx, caches);

		assertFalse(task.isDirty());
	}

}
