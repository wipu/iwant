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

	public TargetRefreshTask(Target target, TargetEvaluationContext ctx) {
		this.target = target;
		this.ctx = ctx;
		for (Path ingredient : target.ingredients()) {
			if (!(ingredient instanceof Target)) {
				continue;
			}
			deps.add(new TargetRefreshTask((Target) ingredient, ctx));
		}
	}

	@Override
	public synchronized void refresh(
			Map<ResourcePool, Resource> allocatedResources) {
		File cachedDescriptor = cachedDescriptorFile();
		cachedDescriptor.getParentFile().mkdirs();
		File cachedTarget = target.cachedAt(ctx);
		cachedTarget.getParentFile().mkdirs();
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
		return new File(ctx.cachedDescriptors(), target.name());
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
		File cachedContent = target.cachedAt(ctx);
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
			File src = ingredient.cachedAt(ctx);
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

}
