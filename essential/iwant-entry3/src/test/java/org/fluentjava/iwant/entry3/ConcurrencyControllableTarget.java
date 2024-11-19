package org.fluentjava.iwant.entry3;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.TargetEvaluationContext;
import org.fluentjava.iwant.api.target.TargetBase;
import org.fluentjava.iwant.entry.Iwant;
import org.fluentjava.iwant.plannerapi.Resource;
import org.fluentjava.iwant.plannerapi.ResourcePool;
import org.fluentjava.iwant.plannerapi.TaskDirtiness;
import org.fluentjava.iwant.plannermocks.TaskMock;

public class ConcurrencyControllableTarget extends TargetBase {

	private final TaskMock task;
	private final List<Path> dependencies;

	public ConcurrencyControllableTarget(String name, Path... dependencies) {
		super(name);
		// let's utilize TaskMock for concurrency control inside here
		task = new TaskMock(name, TaskDirtiness.DIRTY_SRC_INGREDIENT_MISSING,
				Collections.<ResourcePool> emptyList(), true);
		this.dependencies = Arrays.asList(dependencies);
	}

	private static void log(Object... msg) {
		System.err.println(Arrays.toString(msg));
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		log(this, "path starting");
		task.refresh(Collections.<ResourcePool, Resource> emptyMap());
		Iwant.textFileEnsuredToHaveContent(ctx.cached(this), name());
		log(this, "path returning");
	}

	@Override
	protected IngredientsAndParametersDefined ingredientsAndParameters(
			IngredientsAndParametersPlease iUse) {
		// TODO is name really an important parameter, if it is, explain why
		return iUse.ingredients("dependencies", dependencies)
				.parameter("name", name()).nothingElse();
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
