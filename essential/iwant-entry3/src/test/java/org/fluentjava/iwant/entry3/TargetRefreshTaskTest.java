package org.fluentjava.iwant.entry3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.fluentjava.iwant.api.core.HelloTarget;
import org.fluentjava.iwant.api.model.ExternalSource;
import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.api.model.TargetEvaluationContext;
import org.fluentjava.iwant.apimocks.CachesMock;
import org.fluentjava.iwant.apimocks.TargetEvaluationContextMock;
import org.fluentjava.iwant.apimocks.TargetMock;
import org.fluentjava.iwant.core.download.Downloaded;
import org.fluentjava.iwant.entry.Iwant;
import org.fluentjava.iwant.entry.Iwant.IwantNetwork;
import org.fluentjava.iwant.entry3.IngredientCheckingTargetEvaluationContext.ReferenceLegalityCheckCache;
import org.fluentjava.iwant.entrymocks.IwantNetworkMock;
import org.fluentjava.iwant.plannerapi.Resource;
import org.fluentjava.iwant.plannerapi.ResourcePool;
import org.fluentjava.iwant.plannerapi.TaskDirtiness;
import org.fluentjava.iwant.testarea.TestArea;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TargetRefreshTaskTest {

	private TestArea testArea;
	private IwantNetwork network;
	private Iwant iwant;
	private TargetEvaluationContextMock ctx;
	private File cachedTarget;
	private File cachedDescriptors;
	private CachesMock caches;
	private File wsRoot;

	@BeforeEach
	public void before() {
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
		Iwant.mkdirs(cached);
		return cached;
	}

	private void cacheContainsFreshDescriptor(Target target) {
		cacheContainsDescriptor(target, target.contentDescriptor());
	}

	private File cacheContainsDescriptor(Target target,
			String cachedDescriptor) {
		return Iwant.textFileEnsuredToHaveContent(
				new File(cachedDescriptors, target.name()), cachedDescriptor);
	}

	private TargetRefreshTask task(Target target) {
		// here instance caching is not important so we simply create a
		// new cache every time
		HashMap<String, TargetRefreshTask> instanceCache = new HashMap<>();
		ReferenceLegalityCheckCache refLegalityCheckCache = new ReferenceLegalityCheckCache();
		return TargetRefreshTask.instance(target, ctx, caches, instanceCache,
				refLegalityCheckCache);
	}

	@Test
	public void taskNameIsTargetsName() {
		TargetMock t1 = new TargetMock("t1");
		t1.hasNoIngredients();
		TargetMock t2 = new TargetMock("t2");
		t2.hasNoIngredients();

		assertEquals("t1", task(t1).name());
		assertEquals("t2", task(t2).name());
	}

	@Test
	public void taskToStringMentionsTargetsClassAndName() {
		TargetMock t1 = new TargetMock("t1");
		t1.hasNoIngredients();
		TargetMock t2 = new TargetMock("t2");
		t2.hasNoIngredients();
		HelloTarget hello = new HelloTarget("hello", "whatever");

		assertEquals("org.fluentjava.iwant.apimocks.TargetMock t1",
				task(t1).toString());
		assertEquals("org.fluentjava.iwant.apimocks.TargetMock t2",
				task(t2).toString());
		assertEquals("org.fluentjava.iwant.api.core.HelloTarget hello",
				task(hello).toString());
	}

	@Test
	public void equalityAndHashcodeAreDeterminedByName() {
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

	@Test
	public void taskGetter() {
		TargetMock target = new TargetMock("target");
		target.hasNoIngredients();

		assertSame(target, task(target).target());
	}

	@Test
	public void taskOfIngredientlessTargetHasNoDeps() {
		TargetMock target = new TargetMock("target");
		target.hasNoIngredients();

		assertTrue(task(target).dependencies().isEmpty());
	}

	@Test
	public void taskDependenciesAreRefreshTasksOfTargetIngredientsButNotSource() {
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

	@Test
	public void taskIsDirtyIfCachedTargetContentIsMissingEvenIfDescriptorDoes() {
		TargetMock target = new TargetMock("target");
		target.hasNoIngredients();
		target.hasContentDescriptor("descr");
		// no cached content
		cacheContainsFreshDescriptor(target);

		assertEquals(TaskDirtiness.DIRTY_CACHED_CONTENT_MISSING,
				task(target).dirtiness());
	}

	@Test
	public void ingredientlessTaskIsNotDirtyIfCachedTargetContentAndDescriptorExist() {
		TargetMock target = new TargetMock("target");
		target.hasNoIngredients();
		target.hasContentDescriptor("descr");
		cacheContainsContentOf(target);
		cacheContainsFreshDescriptor(target);

		assertEquals(TaskDirtiness.NOT_DIRTY, task(target).dirtiness());
	}

	@Test
	public void noResourcesRequiredIfTargetDoesNotRequireThem() {
		TargetMock target = new TargetMock("target");
		target.hasNoIngredients();

		assertTrue(task(target).requiredResources().isEmpty());
	}

	@Test
	public void refreshWritesCachedContent() {
		TargetMock target = new TargetMock("target");
		target.hasNoIngredients();
		target.hasContent("target content");
		target.hasContentDescriptor("target descriptor");

		task(target).refresh(Collections.<ResourcePool, Resource> emptyMap());

		assertEquals("target content", testArea.contentOf("cached/target"));
	}

	@Test
	public void refreshWritesCachedContentDescriptor() {
		TargetMock target = new TargetMock("target");
		target.hasNoIngredients();
		target.hasContent("target content");
		target.hasContentDescriptor("target descriptor");

		task(target).refresh(Collections.<ResourcePool, Resource> emptyMap());

		assertEquals("target descriptor",
				testArea.contentOf("cached-descriptor/target"));
	}

	@Test
	public void taskIsDirtyIfDescriptorChanged() {
		TargetMock target = new TargetMock("target");
		target.hasNoIngredients();
		target.hasContentDescriptor("new-descriptor");
		cacheContainsContentOf(target);
		cacheContainsDescriptor(target, "old-descriptor");

		assertEquals(TaskDirtiness.DIRTY_DESCRIPTOR_CHANGED,
				task(target).dirtiness());
	}

	@Test
	public void taskIsDirtyIfCachedDescriptorIsMissing() {
		TargetMock target = new TargetMock("target");
		target.hasNoIngredients();
		target.hasContentDescriptor("new-descriptor");
		cacheContainsContentOf(target);
		// no cached descriptor

		assertEquals(TaskDirtiness.DIRTY_CACHED_DESCRIPTOR_MISSING,
				task(target).dirtiness());
	}

	@Test
	public void taskIsNotDirtyIfDescriptorNotChanged() {
		TargetMock target = new TargetMock("target");
		target.hasNoIngredients();
		target.hasContentDescriptor("current");
		cacheContainsContentOf(target);
		cacheContainsDescriptor(target, "current");

		assertEquals(TaskDirtiness.NOT_DIRTY, task(target).dirtiness());
	}

	@Test
	public void taskIsDirtyIfFileUnderSourceDirWasModifiedAfterDescriptor() {
		File srcDir = testArea.newDir("src");
		File srcFile = testArea.hasFile("src/src-file", "src-content");
		srcFile.setLastModified(System.currentTimeMillis() + 2000);
		// srcDir itself hasn't been modified, only the file under it:
		srcDir.setLastModified(System.currentTimeMillis() - 2000);

		TargetMock target = new TargetMock("target");
		target.hasIngredients(ExternalSource.at(srcDir));
		target.hasContentDescriptor("current");
		cacheContainsContentOf(target);
		cacheContainsDescriptor(target, "current");

		assertEquals(TaskDirtiness.DIRTY_SRC_INGREDIENT_MODIFIED,
				task(target).dirtiness());
	}

	@Test
	public void taskIsDirtyIfFileUnderSourceDirWasModifiedAtTheSameTimeAsDescriptor() {
		File srcDir = testArea.newDir("src");
		File srcFile = testArea.hasFile("src/src-file", "src-content");

		TargetMock target = new TargetMock("target");
		target.hasIngredients(ExternalSource.at(srcDir));
		target.hasContentDescriptor("current");
		cacheContainsContentOf(target);
		File descriptor = cacheContainsDescriptor(target, "current");
		descriptor.setLastModified(srcFile.lastModified());

		assertEquals(TaskDirtiness.DIRTY_SRC_INGREDIENT_MODIFIED,
				task(target).dirtiness());
	}

	@Test
	public void taskIsDirtyIfSourceIngredientIsMissing() {
		File srcDir = new File(testArea.root(), "non-existent");

		TargetMock target = new TargetMock("target");
		target.hasIngredients(ExternalSource.at(srcDir));
		target.hasContentDescriptor("current");
		cacheContainsContentOf(target);
		cacheContainsDescriptor(target, "current");

		assertEquals(TaskDirtiness.DIRTY_SRC_INGREDIENT_MISSING,
				task(target).dirtiness());
	}

	@Test
	public void cleanTaskWithSourceIngredient() {
		File srcDir = testArea.newDir("src");
		File srcFile = testArea.hasFile("src/src-file", "src-content");
		srcFile.setLastModified(System.currentTimeMillis() - 2000);
		srcDir.setLastModified(System.currentTimeMillis() - 2000);

		TargetMock target = new TargetMock("target");
		target.hasIngredients(ExternalSource.at(srcDir));
		target.hasContentDescriptor("current");
		cacheContainsContentOf(target);
		cacheContainsDescriptor(target, "current");

		assertEquals(TaskDirtiness.NOT_DIRTY, task(target).dirtiness());
	}

	@Test
	public void targetIsDirtyIfIngredientDescriptorIsNewer() {
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
	@Test
	public void targetIsCleanIfIngredientDescriptorIsAsNewAsTargetsOwnDescriptor() {
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

	@Test
	public void targetIsDirtyIfIngredientDescriptorIsMissing() {
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

	@Test
	public void cleanTaskWithTargetIngredient() {
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

	@Test
	public void taskSupportsParallelismIffTargetSupportsIt() {
		TargetMock nonPar = new TargetMock("nonpar");
		nonPar.hasNoIngredients();
		nonPar.doesNotSupportParallelism();

		TargetMock par = new TargetMock("par");
		par.hasNoIngredients();

		assertFalse(task(nonPar).supportsParallelism());
		assertTrue(task(par).supportsParallelism());
	}

	private static class TargetThatVerifiesDirectoryExistenceAndThenCreatesADirectory
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
			Iwant.textFileEnsuredToHaveContentAndBeTouched(subFile,
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

	@Test
	public void existingCachedContentIsDeletedBeforeRefresh() {
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

	@Test
	public void existingCachedContentIsNotDeletedBeforeRefreshIfTargetSaysSo() {
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

	@Test
	public void parentOfCachedContentIsCreatedBeforeRefreshWhenCachedTargetIsNotDeleted() {
		TargetThatVerifiesDirectoryExistenceAndThenCreatesADirectory target = new TargetThatVerifiesDirectoryExistenceAndThenCreatesADirectory(
				"parent/target");
		target.expectsCachedTargetMissingBeforeRefresh(false);
		target.willCreateFile("f");

		task(target).refresh(Collections.<ResourcePool, Resource> emptyMap());

		assertEquals("f content", testArea.contentOf("cached/parent/target/f"));
	}

	@Test
	public void parentOfCachedContentIsCreatedBeforeRefreshAlsoWhenCachedTargetIsDeleted() {
		TargetThatVerifiesDirectoryExistenceAndThenCreatesADirectory target = new TargetThatVerifiesDirectoryExistenceAndThenCreatesADirectory(
				"parent/target");
		target.expectsCachedTargetMissingBeforeRefresh(true);
		target.willCreateFile("f");

		task(target).refresh(Collections.<ResourcePool, Resource> emptyMap());

		assertEquals("f content", testArea.contentOf("cached/parent/target/f"));
	}

	@Test
	public void downloadedTriesDownloadIfDescriptorIsMissingButNoRealDownloadIsExecutedBecauseCachedFileIsNotDeletedBeforeRefresh()
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
