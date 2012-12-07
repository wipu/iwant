package net.sf.iwant.entry3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import net.sf.iwant.api.Path;
import net.sf.iwant.api.Target;
import net.sf.iwant.api.TargetEvaluationContext;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.io.StreamUtil;
import net.sf.iwant.planner.Resource;
import net.sf.iwant.planner.ResourcePool;
import net.sf.iwant.planner.Task;

public class TargetRefreshTask implements Task {

	private final Target target;
	private final TargetEvaluationContext ctx;
	private final Collection<Task> deps = new ArrayList<Task>();
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
	public synchronized void refresh(
			Map<ResourcePool, Resource> allocatedResources) {
		File cachedDescriptor = cachedDescriptorFile();
		cachedDescriptor.getParentFile().mkdirs();
		File cachedTarget = ctx.cached(target);
		cachedTarget.getParentFile().mkdirs();
		if (target.expectsCachedTargetMissingBeforeRefresh()) {
			Iwant.del(cachedTarget);
		}
		try {
			target.path(ctx);
			new FileWriter(cachedDescriptor).append(target.contentDescriptor())
					.close();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private File cachedDescriptorFile() {
		return caches.contentDescriptorOf(target);
	}

	private void debugLog(Object... lines) {
		Iwant.debugLog(getClass().getSimpleName(), lines);
	}

	@Override
	public synchronized boolean isDirty() {
		String cachedDescriptor = cachedDescriptor();
		if (cachedDescriptor == null
				|| !cachedDescriptor.equals(target.contentDescriptor())) {
			debugLog(target + " is dirty because descriptor chanced.");
			return true;
		}
		File cachedContent = ctx.cached(target);
		if (!cachedContent.exists()) {
			debugLog(target + " is dirty because cached content missing.");
			return true;
		}
		if (isSourceModifiedSince(cachedDescriptorFile().lastModified())) {
			Iwant.fileLog(target + " is dirty because a source was modified.");
			return true;
		}
		debugLog(target + " is not dirty.");
		return false;
	}

	private String cachedDescriptor() {
		try {
			File file = cachedDescriptorFile();
			if (!file.exists()) {
				return null;
			}
			InputStream in = new FileInputStream(file);
			return StreamUtil.toString(in);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private boolean isSourceModifiedSince(long time) {
		for (Path ingredient : target.ingredients()) {
			if (ingredient instanceof Target) {
				// targets are handled as dependency tasks
				continue;
			}
			File src = ctx.cached(ingredient);
			if (isModifiedSince(src, time)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * TODO put all this time stamp logic to one place
	 */
	private boolean isModifiedSince(File src, long time) {
		if (src.lastModified() >= time) {
			Iwant.fileLog("File was modified since " + time + ": " + src);
			return true;
		}
		if (src.isDirectory()) {
			for (File child : src.listFiles()) {
				if (isModifiedSince(child, time)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public synchronized Collection<Task> dependencies() {
		return Collections.unmodifiableCollection(deps);
	}

	@Override
	public synchronized String name() {
		return target.name();
	}

	@Override
	public synchronized Collection<ResourcePool> requiredResources() {
		return Collections.emptyList();
	}

	public synchronized Target target() {
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
