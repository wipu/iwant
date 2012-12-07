package net.sf.iwant.entry3;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import net.sf.iwant.api.Downloaded;
import net.sf.iwant.api.ExternalSource;
import net.sf.iwant.api.HelloTarget;
import net.sf.iwant.api.Path;
import net.sf.iwant.api.Source;
import net.sf.iwant.api.Target;
import net.sf.iwant.api.TargetEvaluationContext;
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

	public void testTaskToStringMentionsTargetsClassAndName() {
		TargetMock t1 = new TargetMock("t1");
		t1.hasNoIngredients();
		TargetMock t2 = new TargetMock("t2");
		t2.hasNoIngredients();
		HelloTarget hello = new HelloTarget("hello", "whatever");

		assertEquals("net.sf.iwant.entry3.TargetMock t1",
				new TargetRefreshTask(t1, ctx, caches).toString());
		assertEquals("net.sf.iwant.entry3.TargetMock t2",
				new TargetRefreshTask(t2, ctx, caches).toString());
		assertEquals("net.sf.iwant.api.HelloTarget hello",
				new TargetRefreshTask(hello, ctx, caches).toString());
	}

	public void testEqualityAndHashcodeAreDeterminedByName() {
		TargetMock a1 = new TargetMock("a");
		a1.hasNoIngredients();
		TargetMock a2 = new TargetMock("a");
		a2.hasNoIngredients();
		TargetMock b = new TargetMock("b");
		b.hasNoIngredients();

		TargetRefreshTask a1Task = new TargetRefreshTask(a1, ctx, caches);
		TargetRefreshTask a2Task = new TargetRefreshTask(a2, ctx, caches);
		TargetRefreshTask bTask = new TargetRefreshTask(b, ctx, caches);

		assertEquals(a1Task, a2Task);
		assertEquals(a1Task.hashCode(), a2Task.hashCode());

		assertFalse(a1Task.equals(bTask));
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

	public void testTaskSupportsParallelismIffTargetSupportsIt() {
		TargetMock nonPar = new TargetMock("nonpar");
		nonPar.hasNoIngredients();
		nonPar.doesNotSupportParallelism();

		TargetMock par = new TargetMock("par");
		par.hasNoIngredients();

		assertFalse(new TargetRefreshTask(nonPar, ctx, caches)
				.supportsParallelism());
		assertTrue(new TargetRefreshTask(par, ctx, caches)
				.supportsParallelism());
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
			dest.mkdir();
			File subFile = new File(dest, fileNameToCreateUnderDirectory);
			new FileWriter(subFile).append(
					fileNameToCreateUnderDirectory + " content").close();
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

		TargetRefreshTask task = new TargetRefreshTask(target, ctx, caches);

		assertTrue(task.isDirty());
		task.refresh(Collections.<ResourcePool, Resource> emptyMap());
		assertFalse(task.isDirty());
		assertEquals("f1 content", testArea.contentOf("cached/target/f1"));

		// target content changes => f1 gets replaced by f2
		target.willVerifyCachedTargetExistence();
		target.willCreateFile("f2");
		assertTrue(task.isDirty());
		task.refresh(Collections.<ResourcePool, Resource> emptyMap());
		assertEquals("f2 content", testArea.contentOf("cached/target/f2"));
		assertFalse(task.isDirty());
		assertFalse(new File(cachedTarget, "target/f1").exists());
	}

	public void testExistingCachedContentIsNotDeletedBeforeRefreshIfTargetSaysSo() {
		TargetThatVerifiesDirectoryExistenceAndThenCreatesADirectory target = new TargetThatVerifiesDirectoryExistenceAndThenCreatesADirectory(
				"target");
		target.expectsCachedTargetMissingBeforeRefresh(false);
		target.willCreateFile("f1");

		TargetRefreshTask task = new TargetRefreshTask(target, ctx, caches);

		assertTrue(task.isDirty());
		task.refresh(Collections.<ResourcePool, Resource> emptyMap());
		assertFalse(task.isDirty());
		assertEquals("f1 content", testArea.contentOf("cached/target/f1"));

		// target content changes => f1 stays and f2 is created alongside it
		target.willVerifyCachedTargetExistence();
		target.willCreateFile("f2");
		assertTrue(task.isDirty());
		task.refresh(Collections.<ResourcePool, Resource> emptyMap());
		assertEquals("f1 content", testArea.contentOf("cached/target/f1"));
		assertEquals("f2 content", testArea.contentOf("cached/target/f2"));
		assertFalse(task.isDirty());
	}

	public void testParentOfCachedContentIsCreatedBeforeRefreshWhenCachedTargetIsNotDeleted() {
		TargetThatVerifiesDirectoryExistenceAndThenCreatesADirectory target = new TargetThatVerifiesDirectoryExistenceAndThenCreatesADirectory(
				"parent/target");
		target.expectsCachedTargetMissingBeforeRefresh(false);
		target.willCreateFile("f");

		TargetRefreshTask task = new TargetRefreshTask(target, ctx, caches);
		task.refresh(Collections.<ResourcePool, Resource> emptyMap());

		assertEquals("f content", testArea.contentOf("cached/parent/target/f"));
	}

	public void testParentOfCachedContentIsCreatedBeforeRefreshAlsoWhenCachedTargetIsDeleted() {
		TargetThatVerifiesDirectoryExistenceAndThenCreatesADirectory target = new TargetThatVerifiesDirectoryExistenceAndThenCreatesADirectory(
				"parent/target");
		target.expectsCachedTargetMissingBeforeRefresh(true);
		target.willCreateFile("f");

		TargetRefreshTask task = new TargetRefreshTask(target, ctx, caches);
		task.refresh(Collections.<ResourcePool, Resource> emptyMap());

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
				.url(urlThatShallNotBeContacted.toExternalForm()).md5("todo");

		TargetRefreshTask task = new TargetRefreshTask(target, ctx, caches);

		// no ConnectException means no download was tried
		task.refresh(Collections.<ResourcePool, Resource> emptyMap());

		// descriptor is created, downloaded file content stays the same
		assertTrue(cachedDescriptor.exists());
		assertEquals("downloaded content", testArea.contentOf("cached-url"));
	}

}
