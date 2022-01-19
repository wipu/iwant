package org.fluentjava.iwant.tests;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.fluentjava.iwant.api.javamodules.JavaModule;
import org.fluentjava.iwant.api.javamodules.JavaSrcModule;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.api.model.TargetEvaluationContext;
import org.fluentjava.iwant.apimocks.CachesMock;
import org.fluentjava.iwant.apimocks.TargetEvaluationContextMock;
import org.fluentjava.iwant.core.javamodules.JavaModules;
import org.fluentjava.iwant.entry.Iwant;
import org.fluentjava.iwant.entry.Iwant.IwantNetwork;
import org.fluentjava.iwant.entry3.TargetRefreshTask;
import org.fluentjava.iwant.entrymocks.IwantNetworkMock;
import org.fluentjava.iwant.planner.TaskAllocation;
import org.fluentjava.iwant.planner.TaskQueue;
import org.fluentjava.iwant.plannerapi.Task;
import org.fluentjava.iwant.testarea.TestArea;
import org.junit.Before;
import org.junit.Test;

public class TaskQueueWithJavaModulesMicrobencmarkTest {

	private TestArea testArea;
	private CachesMock caches;
	private TargetEvaluationContext ctx;

	@Before
	public void before() {
		testArea = TestArea.forTest(this);
		caches = new CachesMock(testArea.root());
		File cachedDescriptors = testArea.newDir("cachedDescriptors");
		caches.cachesDesciptorsAt(cachedDescriptors);

		IwantNetwork network = new IwantNetworkMock(testArea);
		Iwant iwant = Iwant.using(network);
		ctx = new TargetEvaluationContextMock(iwant, caches);
	}

	private static <T> SortedSet<T> union(SortedSet<T> a, SortedSet<T> b) {
		SortedSet<T> u = new TreeSet<>();
		u.addAll(a);
		u.addAll(b);
		return u;
	}

	private static String id(String prefix, int index) {
		return String.format("%s%04d", prefix, index);
	}

	@Test
	public void moduleIdFormatInternalTest() {
		assertEquals("a0000", id("a", 0));
		assertEquals("b0123", id("b", 123));
	}

	@Test
	public void takeAndMarkDoneTasksFromBigTreeOfJavaModuleDependencies() {
		long testStartTime = System.currentTimeMillis();
		int jarCount = 1000;
		SortedSet<JavaModule> level0 = binModules(jarCount);
		SortedSet<JavaModule> level1 = srcModules("level1-", 25, level0);
		SortedSet<JavaModule> level2 = srcModules("level2-", 25,
				union(level1, level0));
		SortedSet<JavaModule> level3 = srcModules("level3-", 25,
				union(level2, level0));
		JavaSrcModule rootMod = JavaSrcModule.with().name("root").mavenLayout()
				.mainDeps(union(level3, level0)).testDeps(level2).end();

		Map<String, TargetRefreshTask> taskInstanceCache = new HashMap<>();

		long t1 = System.currentTimeMillis();
		Task rootTask = TargetRefreshTask.instance(
				(Target) rootMod.mainArtifact(), ctx, caches,
				taskInstanceCache);
		logTime("rootTask creation", t1);
		t1 = System.currentTimeMillis();
		TaskQueue queue = new TaskQueue(rootTask);
		logTime("queue creation", t1);

		t1 = System.currentTimeMillis();
		TaskAllocation allocation = queue.next();
		logTime("call 1 to next", t1);
		TargetRefreshTask task = (TargetRefreshTask) allocation.task();
		assertEquals("name0000-ver0000.jar", task.target().name());
		t1 = System.currentTimeMillis();
		queue.markDone(allocation);
		logTime("call 1 to markDone", t1);

		t1 = System.currentTimeMillis();
		allocation = queue.next();
		logTime("call 2 to next", t1);
		task = (TargetRefreshTask) allocation.task();
		assertEquals("name0001-ver0001.jar", task.target().name());
		t1 = System.currentTimeMillis();
		queue.markDone(allocation);
		logTime("call 2 to markDone", t1);

		// consume the rest of level 0 (jars):
		for (int i = 2; i < jarCount; i++) {
			t1 = System.currentTimeMillis();
			allocation = queue.next();
			logTime("call " + (i + 1) + " to next", t1);
			task = (TargetRefreshTask) allocation.task();
			assertEquals(id("name", i) + "-" + id("ver", i) + ".jar",
					task.target().name());
			t1 = System.currentTimeMillis();
			queue.markDone(allocation);
			logTime("call " + (i + 1) + " to markDone", t1);
		}

		// consume src module main classes in parallel
		consumeLevelInParallel(level1, "level1", queue);
		consumeLevelInParallel(level2, "level2", queue);
		consumeLevelInParallel(level3, "level3", queue);

		logTime("the whole test", testStartTime);
	}

	private static void consumeLevelInParallel(SortedSet<JavaModule> level,
			String levelName, TaskQueue queue) {
		long t1;
		TargetRefreshTask task;
		List<TaskAllocation> allocations = new ArrayList<>();
		for (int i = 0; i < level.size(); i++) {
			t1 = System.currentTimeMillis();
			TaskAllocation alloc = queue.next();
			logTime("call " + (i + 1) + " to next", t1);
			allocations.add(alloc);
			task = (TargetRefreshTask) alloc.task();
			assertEquals(id(levelName + "-", i) + "-main-classes",
					task.target().name());
		}
		for (TaskAllocation alloc : allocations) {
			t1 = System.currentTimeMillis();
			queue.markDone(alloc);
			logTime("call to markDone", t1);
		}
	}

	private static void logTime(String message, long t1) {
		long t2 = System.currentTimeMillis();
		System.err.println(message + " took " + (t2 - t1) + "ms.");
		System.err.flush();
	}

	private static SortedSet<JavaModule> binModules(int count) {
		SortedSet<JavaModule> mods = new TreeSet<>();
		for (int i = 0; i < count; i++) {
			JavaModule mod = JavaModules.binModule(id("group", i),
					id("name", i), id("ver", i));
			mods.add(mod);
		}
		return mods;
	}

	private static SortedSet<JavaModule> srcModules(String namePrefix,
			int count, SortedSet<JavaModule> deps) {
		SortedSet<JavaModule> mods = new TreeSet<>();
		for (int i = 0; i < count; i++) {
			JavaModule mod = JavaSrcModule.with().name(id(namePrefix, i))
					.mavenLayout().mainDeps(deps).testDeps(deps).end();
			mods.add(mod);
		}
		return mods;
	}

}
