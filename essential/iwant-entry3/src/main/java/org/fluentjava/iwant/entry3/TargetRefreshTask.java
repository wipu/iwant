package net.sf.iwant.entry3;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import net.sf.iwant.api.model.Caches;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.model.TargetEvaluationContext;
import net.sf.iwant.coreservices.FileUtil;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.plannerapi.Resource;
import net.sf.iwant.plannerapi.ResourcePool;
import net.sf.iwant.plannerapi.Task;
import net.sf.iwant.plannerapi.TaskDirtiness;

public class TargetRefreshTask implements Task {

	private final Target target;
	private final TargetEvaluationContext ctx;
	private final Collection<Task> deps = new ArrayList<>();
	private final Caches caches;

	public TargetRefreshTask(Target target, TargetEvaluationContext ctx,
			Caches caches) {
		this.target = target;
		this.ctx = ctx;
		this.caches = caches;
		for (Path ingredient : target.ingredients()) {
			if (!(ingredient instanceof Target)) {
				continue;
			}
			deps.add(new TargetRefreshTask((Target) ingredient, ctx, caches));
		}
	}

	@Override
	public void refresh(Map<ResourcePool, Resource> allocatedResources) {
		File cachedDescriptor = cachedDescriptorFile();
		Iwant.mkdirs(cachedDescriptor.getParentFile());
		// make sure refresh is retried even if it's interrupted
		Iwant.del(cachedDescriptor);

		File cachedTarget = ctx.cached(target);
		Iwant.mkdirs(cachedTarget.getParentFile());
		if (target.expectsCachedTargetMissingBeforeRefresh()) {
			Iwant.del(cachedTarget);
		}
		try {
			target.path(
					new IngredientCheckingTargetEvaluationContext(target, ctx));
			Iwant.newTextFile(cachedDescriptor, target.contentDescriptor());
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private File cachedDescriptorFile() {
		return caches.contentDescriptorOf(target);
	}

	@Override
	public TaskDirtiness dirtiness() {
		String cachedDescriptor = cachedDescriptor();
		if (cachedDescriptor == null) {
			return TaskDirtiness.DIRTY_CACHED_DESCRIPTOR_MISSING;
		}
		if (!cachedDescriptor.equals(target.contentDescriptor())) {
			return TaskDirtiness.DIRTY_DESCRIPTOR_CHANGED;
		}
		File cachedContent = ctx.cached(target);
		if (!cachedContent.exists()) {
			return TaskDirtiness.DIRTY_CACHED_CONTENT_MISSING;
		}
		return isIngredientModifiedSince(cachedDescriptorFile().lastModified());
	}

	private String cachedDescriptor() {
		File file = cachedDescriptorFile();
		if (!file.exists()) {
			return null;
		}
		return FileUtil.contentAsString(file);
	}

	private void logReasonOfDirtiness(File ingredient, String reason) {
		Iwant.fileLog(this + " is dirty, because " + ingredient + reason);
	}

	private TaskDirtiness isIngredientModifiedSince(long time) {
		for (Path ingredient : target.ingredients()) {
			if (ingredient instanceof Target) {
				Target targetIngredient = (Target) ingredient;
				File ingredientDescriptor = caches
						.contentDescriptorOf(targetIngredient);
				if (!ingredientDescriptor.exists()
						|| ingredientDescriptor.lastModified() > time) {
					logReasonOfDirtiness(ingredientDescriptor, " was modified");
					return TaskDirtiness.DIRTY_TARGET_INGREDIENT_MODIFIED;
				}
			} else {
				File src = ctx.cached(ingredient);
				if (!src.exists()) {
					logReasonOfDirtiness(src, " is missing");
					return TaskDirtiness.DIRTY_SRC_INGREDIENT_MISSING;
				}
				if (Iwant.isModifiedSince(src, time)) {
					logReasonOfDirtiness(src, " was modified");
					return TaskDirtiness.DIRTY_SRC_INGREDIENT_MODIFIED;
				}
			}
		}
		return TaskDirtiness.NOT_DIRTY;
	}

	@Override
	public Collection<Task> dependencies() {
		return Collections.unmodifiableCollection(deps);
	}

	@Override
	public String name() {
		return target.name();
	}

	@Override
	public Collection<ResourcePool> requiredResources() {
		return Collections.emptyList();
	}

	public Target target() {
		return target;
	}

	@Override
	public boolean supportsParallelism() {
		return target.supportsParallelism();
	}

	@Override
	public String toString() {
		return target.getClass().getCanonicalName() + " " + name();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name() == null) ? 0 : name().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TargetRefreshTask other = (TargetRefreshTask) obj;
		if (name() == null) {
			if (other.name() != null) {
				return false;
			}
		} else if (!name().equals(other.name())) {
			return false;
		}
		return true;
	}

}
