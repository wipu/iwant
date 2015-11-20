package net.sf.iwant.entry3;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import net.sf.iwant.api.core.HelloTarget;
import net.sf.iwant.api.model.ExternalSource;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.model.TargetEvaluationContext;
import net.sf.iwant.apimocks.CachesMock;
import net.sf.iwant.apimocks.TargetEvaluationContextMock;
import net.sf.iwant.apimocks.TargetMock;
import net.sf.iwant.core.download.Downloaded;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.IwantNetwork;
import net.sf.iwant.entrymocks.IwantNetworkMock;
import net.sf.iwant.plannerapi.Resource;
import net.sf.iwant.plannerapi.ResourcePool;
import net.sf.iwant.plannerapi.TaskDirtiness;
import net.sf.iwant.testarea.TestArea;

public class TargetRefreshTaskTest extends TestCase {

	private TestArea testArea;
	private IwantNetwork network;
	private Iwant iwant;
	private TargetEvaluationContextMock ctx;
	private File cachedTarget;
	private File cachedDescriptors;
	private CachesMock caches;
	private File wsRoot;

	@Override
	public void setUp() {
		testArea = TestArea.forTest(this);
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

	private File cacheContainsContentOf(Target target) {
		File cached = ctx.cached(target);
		cached.mkdir();
		return cached;
	}

	private void cacheContainsFreshDescriptor(Target target) {
		cacheContainsDescriptor(target, target.contentDescriptor());
	}

	private File cacheContainsDescriptor(Target target,
			String cachedDescriptor) {
		return Iwant.newTextFile(new File(cachedDescriptors, target.name()),
				cachedDescriptor);
	}

	private TargetRefreshTask task(Target target) {
		return new TargetRefreshTask(target, ctx, caches);
	}

	public void testTaskNameIsTargetsName() {
		TargetMock t1 = new TargetMock("t1");
		t1.hasNoIngredients();
		TargetMock t2 = new TargetMock("t2");
		t2.hasNoIngredients();

		assertEquals("t1", task(t1).name());
		assertEquals("t2", task(t2).name());
	}

	public void testTaskToStringMentionsTargetsClassAndName() {
		TargetMock t1 = new TargetMock("t1");
		t1.hasNoIngredients();
		TargetMock t2 = new TargetMock("t2");
		t2.hasNoIngredients();
		HelloTarget hello = new HelloTarget("hello", "whatever");

		assertEquals("net.sf.iwant.apimocks.TargetMock t1",
				task(t1).toString());
		assertEquals("net.sf.iwant.apimocks.TargetMock t2",
				task(t2).toString());
		assertEquals("net.sf.iwant.api.core.HelloTarget hello",
				task(hello).toString());
	}

	public void testEqualityAndHashcodeAreDeterminedByName() {
		TargetMock a1 = new TargetMock("a");
		a1.hasNoIngredients();
		TargetMock a2 = new TargetMock("a");
		a2.hasNoIngredients();
		TargetMock b = new TargetMock("b");
		b.hasNoIngredients();

		TargetRefreshTask a1Task = task(a1);
		TargetRefreshTask a2Task = task(a2);
		TargetRefreshTask bTask = task(b);

		assertEquals(a1Task, a2Task);
		assertEquals(a1Task.hashCode(), a2Task.hashCode());

		assertFalse(a1Task.equals(bTask));
	}

	public void testTaskGetter() {
		TargetMock target = new TargetMock("target");
		target.hasNoIngredients();

		assertSame(target, task(target).target());
	}

	public void testTaskOfIngredientlessTargetHasNoDeps() {
		TargetMock target = new TargetMock("target");
		target.hasNoIngredients();

		assertTrue(task(target).dependencies().isEmpty());
	}

	public void testTaskDependenciesAreRefreshTasksOfTargetIngredientsButNotSource() {
		Path srcIngredient = Source.underWsroot("src-ingredient");
		TargetMock targetIngredient = new TargetMock("target-ingredient");
		targetIngredient.hasNoIngredients();

		TargetMock target = new TargetMock("target");
		target.hasIngredients(Arrays.asList(srcIngredient, targetIngredient));

		TargetRefreshTask task = task(target);

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

		assertEquals(TaskDirtiness.DIRTY_CACHED_CONTENT_MISSING,
				task(target).dirtiness());
	}

	public void testIngredientlessTaskIsNotDirtyIfCachedTargetContentAndDescriptorExist() {
		TargetMock target = new TargetMock("target");
		target.hasNoIngredients();
		target.hasContentDescriptor("descr");
		cacheContainsContentOf(target);
		cacheContainsFreshDescriptor(target);

		assertEquals(TaskDirtiness.NOT_DIRTY, task(target).dirtiness());
	}

	public void testNoResourcesRequiredIfTargetDoesNotRequireThem() {
		TargetMock target = new TargetMock("target");
		target.hasNoIngredients();

		assertTrue(task(target).requiredResources().isEmpty());
	}

	public void testRefreshWritesCachedContent() {
		TargetMock target = new TargetMock("target");
		target.hasNoIngredients();
		target.hasContent("target content");
		target.hasContentDescriptor("target descriptor");

		task(target).refresh(Collections.<ResourcePool, Resource> emptyMap());

		assertEquals("target content", testArea.contentOf("cached/target"));
	}

	public void testRefreshWritesCachedContentDescriptor() {
		TargetMock target = new TargetMock("target");
		target.hasNoIngredients();
		target.hasContent("target content");
		target.hasContentDescriptor("target descriptor");

		task(target).refresh(Collections.<ResourcePool, Resource> emptyMap());

		assertEquals("target descriptor",
				testArea.contentOf("cached-descriptor/target"));
	}

	public void testTaskIsDirtyIfDescriptorChanged() {
		TargetMock target = new TargetMock("target");
		target.hasNoIngredients();
		target.hasContentDescriptor("new-descriptor");
		cacheContainsContentOf(target);
		cacheContainsDescriptor(target, "old-descriptor");

		assertEquals(TaskDirtiness.DIRTY_DESCRIPTOR_CHANGED,
				task(target).dirtiness());
	}

	public void testTaskIsDirtyIfCachedDescriptorIsMissing() {
		TargetMock target = new TargetMock("target");
		target.hasNoIngredients();
		target.hasContentDescriptor("new-descriptor");
		cacheContainsContentOf(target);
		// no cached descriptor

		assertEquals(TaskDirtiness.DIRTY_CACHED_DESCRIPTOR_MISSING,
				task(target).dirtiness());
	}

	public void testTaskIsNotDirtyIfDescriptorNotChanged() {
		TargetMock target = new TargetMock("target");
		target.hasNoIngredients();
		target.hasContentDescriptor("current");
		cacheContainsContentOf(target);
		cacheContainsDescriptor(target, "current");

		assertEquals(TaskDirtiness.NOT_DIRTY, task(target).dirtiness());
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

		assertEquals(TaskDirtiness.DIRTY_SRC_INGREDIENT_MODIFIED,
				task(target).dirtiness());
	}

	public void testTaskIsDirtyIfFileUnderSourceDirWasModifiedAtTheSameTimeAsDescriptor()
			throws IOException {
		File srcDir = testArea.newDir("src");
		testArea.hasFile("src/src-file", "src-content");

		TargetMock target = new TargetMock("target");
		target.hasIngredients(new ExternalSource(srcDir));
		target.hasContentDescriptor("current");
		cacheContainsContentOf(target);
		cacheContainsDescriptor(target, "current");

		assertEquals(TaskDirtiness.DIRTY_SRC_INGREDIENT_MODIFIED,
				task(target).dirtiness());
	}

	public void testTaskIsDirtyIfSourceIngredientIsMissing()
			throws IOException {
		File srcDir = new File(testArea.root(), "non-existent");

		TargetMock target = new TargetMock("target");
		target.hasIngredients(new ExternalSource(srcDir));
		target.hasContentDescriptor("current");
		cacheContainsContentOf(target);
		cacheContainsDescriptor(target, "current");

		assertEquals(TaskDirtiness.DIRTY_SRC_INGREDIENT_MISSING,
				task(target).dirtiness());
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

		assertEquals(TaskDirtiness.NOT_DIRTY, task(target).dirtiness());
	}

	public void testTargetIsDirtyIfIngredientDescriptorIsNewer() {
		TargetMock ingredient = new TargetMock("ingredient");
		ingredient.hasNoIngredients();
		ingredient.hasContent("current");
		ingredient.hasContentDescriptor("current");
		cacheContainsContentOf(ingredient)
				.setLastModified(System.currentTimeMillis() - 2000);
		cacheContainsDescriptor(ingredient, "current")
				.setLastModified(System.currentTimeMillis() + 2000);

		TargetMock target = new TargetMock("target");
		target.hasIngredients(ingredient);
		target.hasContentDescriptor("current");
		cacheContainsContentOf(target);
		cacheContainsDescriptor(target, "current");

		new File(cachedDescriptors, "ingredient")
				.setLastModified(System.currentTimeMillis() + 2000);

		assertEquals(TaskDirtiness.DIRTY_TARGET_INGREDIENT_MODIFIED,
				task(target).dirtiness());
	}

	/**
	 * Often refresh is so fast that we want to optimize like this
	 */
	public void testTargetIsCleanIfIngredientDescriptorIsAsNewAsTargetsOwnDescriptor() {
		TargetMock ingredient = new TargetMock("ingredient");
		ingredient.hasNoIngredients();
		ingredient.hasContent("current");
		ingredient.hasContentDescriptor("current");
		cacheContainsContentOf(ingredient)
				.setLastModified(System.currentTimeMillis() - 2000);
		cacheContainsDescriptor(ingredient, "current")
				.setLastModified(System.currentTimeMillis() + 2000);

		TargetMock target = new TargetMock("target");
		target.hasIngredients(ingredient);
		target.hasContentDescriptor("current");
		cacheContainsContentOf(target);
		File ownDescriptor = cacheContainsDescriptor(target, "current");

		new File(cachedDescriptors, "ingredient")
				.setLastModified(ownDescriptor.lastModified());

		assertEquals(TaskDirtiness.NOT_DIRTY, task(target).dirtiness());
	}

	public void testTargetIsDirtyIfIngredientDescriptorIsMissing() {
		TargetMock ingredient = new TargetMock("ingredient");
		ingredient.hasNoIngredients();
		ingredient.hasContent("current");
		ingredient.hasContentDescriptor("current");
		cacheContainsContentOf(ingredient)
				.setLastModified(System.currentTimeMillis() - 2000);

		TargetMock target = new TargetMock("target");
		target.hasIngredients(ingredient);
		target.hasContentDescriptor("current");
		cacheContainsContentOf(target);
		cacheContainsDescriptor(target, "current");

		new File(cachedDescriptors, "ingredient")
				.setLastModified(System.currentTimeMillis() + 2000);

		assertEquals(TaskDirtiness.DIRTY_TARGET_INGREDIENT_MODIFIED,
				task(target).dirtiness());
	}

	public void testCleanTaskWithTargetIngredient() {
		TargetMock ingredient = new TargetMock("ingredient");
		ingredient.hasNoIngredients();
		ingredient.hasContent("current");
		ingredient.hasContentDescriptor("current");
		cacheContainsContentOf(ingredient);
		cacheContainsDescriptor(ingredient, "current")
				.setLastModified(System.currentTimeMillis() - 2000);

		TargetMock target = new TargetMock("target");
		target.hasIngredients(ingredient);
		target.hasContentDescriptor("current");
		cacheContainsContentOf(target);
		cacheContainsDescriptor(target, "current");

		assertEquals(TaskDirtiness.NOT_DIRTY, task(target).dirtiness());
	}

	public void testTaskSupportsParallelismIffTargetSupportsIt() {
		TargetMock nonPar = new TargetMock("nonpar");
		nonPar.hasNoIngredients();
		nonPar.doesNotSupportParallelism();

		TargetMock par = new TargetMock("par");
		par.hasNoIngredients();

		assertFalse(task(nonPar).supportsParallelism());
		assertTrue(task(par).supportsParallelism());
	}

	private class TargetThatVerifiesDirectoryExistenceAndThenCreatesADirectory
			extends Target {

		private String fileNameToCreateUnderDirectory;
		private Boolean expectsCachedTargetMissingBeforeRefresh;
		private boolean mustVerifyCachedTargetExistence = false;

		public TargetThatVerifiesDirectoryExistenceAndThenCreatesADirectory(
				String name) {
			super(name);
		}

		public synchronized void willCreateFile(
				String fileNameToCreateUnderDirectory) {
			this.fileNameToCreateUnderDirectory = fileNameToCreateUnderDirectory;
		}

		@Override
		public InputStream content(TargetEvaluationContext ctx)
				throws Exception {
			throw new UnsupportedOperationException("TODO test and implement");
		}

		@Override
		public synchronized void path(TargetEvaluationContext ctx)
				throws Exception {
			File dest = ctx.cached(this);
			if (mustVerifyCachedTargetExistence) {
				verifyCachedTargetExistence(dest);
			}
			if (!dest.getParentFile().exists()) {
				fail("Parent of " + dest + " should exist.");
			}
			File subFile = new File(dest, fileNameToCreateUnderDirectory);
			Iwant.newTextFile(subFile,
					fileNameToCreateUnderDirectory + " content");
		}

		private void verifyCachedTargetExistence(File dest) {
			if (expectsCachedTargetMissingBeforeRefresh && dest.exists()) {
				fail("Cached target should not exist!");
			} else if (!expectsCachedTargetMissingBeforeRefresh
					&& !dest.exists()) {
				fail("Cached target should exist!");
			}
		}

		@Override
		public synchronized boolean expectsCachedTargetMissingBeforeRefresh() {
			return expectsCachedTargetMissingBeforeRefresh;
		}

		public synchronized void expectsCachedTargetMissingBeforeRefresh(
				boolean expectsCachedTargetMissingBeforeRefresh) {
			this.expectsCachedTargetMissingBeforeRefresh = expectsCachedTargetMissingBeforeRefresh;
		}

		@Override
		public List<Path> ingredients() {
			return Collections.emptyList();
		}

		@Override
		public synchronized String contentDescriptor() {
			return getClass().getCanonicalName() + ":"
					+ fileNameToCreateUnderDirectory;
		}

		public synchronized void willVerifyCachedTargetExistence() {
			this.mustVerifyCachedTargetExistence = true;
		}

	}

	public void testExistingCachedContentIsDeletedBeforeRefresh() {
		TargetThatVerifiesDirectoryExistenceAndThenCreatesADirectory target = new TargetThatVerifiesDirectoryExistenceAndThenCreatesADirectory(
				"target");
		target.expectsCachedTargetMissingBeforeRefresh(true);
		target.willCreateFile("f1");

		assertEquals(TaskDirtiness.DIRTY_CACHED_DESCRIPTOR_MISSING,
				task(target).dirtiness());
		task(target).refresh(Collections.<ResourcePool, Resource> emptyMap());
		assertEquals(TaskDirtiness.NOT_DIRTY, task(target).dirtiness());
		assertEquals("f1 content", testArea.contentOf("cached/target/f1"));

		// target content changes => f1 gets replaced by f2
		target.willVerifyCachedTargetExistence();
		target.willCreateFile("f2");
		assertEquals(TaskDirtiness.DIRTY_DESCRIPTOR_CHANGED,
				task(target).dirtiness());
		task(target).refresh(Collections.<ResourcePool, Resource> emptyMap());
		assertEquals("f2 content", testArea.contentOf("cached/target/f2"));
		assertEquals(TaskDirtiness.NOT_DIRTY, task(target).dirtiness());
		assertFalse(new File(cachedTarget, "target/f1").exists());
	}

	public void testExistingCachedContentIsNotDeletedBeforeRefreshIfTargetSaysSo() {
		TargetThatVerifiesDirectoryExistenceAndThenCreatesADirectory target = new TargetThatVerifiesDirectoryExistenceAndThenCreatesADirectory(
				"target");
		target.expectsCachedTargetMissingBeforeRefresh(false);
		target.willCreateFile("f1");

		assertEquals(TaskDirtiness.DIRTY_CACHED_DESCRIPTOR_MISSING,
				task(target).dirtiness());
		task(target).refresh(Collections.<ResourcePool, Resource> emptyMap());
		assertEquals(TaskDirtiness.NOT_DIRTY, task(target).dirtiness());
		assertEquals("f1 content", testArea.contentOf("cached/target/f1"));

		// target content changes => f1 stays and f2 is created alongside it
		target.willVerifyCachedTargetExistence();
		target.willCreateFile("f2");
		assertEquals(TaskDirtiness.DIRTY_DESCRIPTOR_CHANGED,
				task(target).dirtiness());
		task(target).refresh(Collections.<ResourcePool, Resource> emptyMap());
		assertEquals("f1 content", testArea.contentOf("cached/target/f1"));
		assertEquals("f2 content", testArea.contentOf("cached/target/f2"));
		assertEquals(TaskDirtiness.NOT_DIRTY, task(target).dirtiness());
	}

	public void testParentOfCachedContentIsCreatedBeforeRefreshWhenCachedTargetIsNotDeleted() {
		TargetThatVerifiesDirectoryExistenceAndThenCreatesADirectory target = new TargetThatVerifiesDirectoryExistenceAndThenCreatesADirectory(
				"parent/target");
		target.expectsCachedTargetMissingBeforeRefresh(false);
		target.willCreateFile("f");

		task(target).refresh(Collections.<ResourcePool, Resource> emptyMap());

		assertEquals("f content", testArea.contentOf("cached/parent/target/f"));
	}

	public void testParentOfCachedContentIsCreatedBeforeRefreshAlsoWhenCachedTargetIsDeleted() {
		TargetThatVerifiesDirectoryExistenceAndThenCreatesADirectory target = new TargetThatVerifiesDirectoryExistenceAndThenCreatesADirectory(
				"parent/target");
		target.expectsCachedTargetMissingBeforeRefresh(true);
		target.willCreateFile("f");

		task(target).refresh(Collections.<ResourcePool, Resource> emptyMap());

		assertEquals("f content", testArea.contentOf("cached/parent/target/f"));
	}

	public void testDownloadedTriesDownloadIfDescriptorIsMissingButNoRealDownloadIsExecutedBecauseCachedFileIsNotDeletedBeforeRefresh()
			throws IOException {
		URL urlThatShallNotBeContacted = Iwant.url("http://localhost:9999");
		try {
			urlThatShallNotBeContacted.openStream();
			fail("Internal failure, expected " + urlThatShallNotBeContacted
					+ " not to respond.");
		} catch (ConnectException e) {
			assertEquals("Connection refused", e.getMessage());
		}

		// cached file exists but descriptor doesn't
		File cachedUrl = testArea.hasFile("cached-url", "downloaded content");
		caches.cachesUrlAt(urlThatShallNotBeContacted, cachedUrl);
		File cachedDescriptor = new File(cachedDescriptors, "downloaded");
		assertFalse(cachedDescriptor.exists());

		Downloaded target = Downloaded.withName("downloaded")
				.url(urlThatShallNotBeContacted.toExternalForm())
				.md5("c1a9782e30af3d9d04061c3d4a7e93d5");

		// no ConnectException means no download was tried
		task(target).refresh(Collections.<ResourcePool, Resource> emptyMap());

		// descriptor is created, downloaded file content stays the same
		assertTrue(cachedDescriptor.exists());
		assertEquals("downloaded content", testArea.contentOf("cached-url"));
	}

}
