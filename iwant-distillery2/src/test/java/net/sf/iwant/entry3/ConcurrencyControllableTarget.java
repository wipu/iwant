package net.sf.iwant.entry3;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.model.TargetEvaluationContext;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.planner.Resource;
import net.sf.iwant.planner.ResourcePool;
import net.sf.iwant.planner.TaskDirtiness;
import net.sf.iwant.planner.TaskMock;

public class ConcurrencyControllableTarget extends Target {

	private final TaskMock task;
	private final List<Path> dependencies;

	public ConcurrencyControllableTarget(String name, Path... dependencies) {
		super(name);
		// let's utilize TaskMock for concurrency control inside here
		task = new TaskMock(name, TaskDirtiness.DIRTY_SRC_MODIFIED,
				Collections.<ResourcePool> emptyList(), true);
		this.dependencies = Arrays.asList(dependencies);
	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	private static void log(Object... msg) {
		System.err.println(Arrays.toString(msg));
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		log(this, "path starting");
		task.refresh(Collections.<ResourcePool, Resource> emptyMap());
		Iwant.newTextFile(ctx.cached(this), name());
		log(this, "path returning");
	}

	@Override
	public List<Path> ingredients() {
		return dependencies;
	}

	@Override
	public String contentDescriptor() {
		return name();
	}

	public void shallEventuallyStartRefresh() {
		log(this, "shallEventuallyStartRefresh");
		task.shallEventuallyStartRefresh();
	}

	public void shallNotStartRefresh() {
		log(this, "shallNotStartRefresh");
		task.shallNotStartRefresh();
	}

	public void finishesRefresh() {
		log(this, "finishesRefresh");
		task.finishesRefresh();
	}

}
