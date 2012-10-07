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
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private File cachedDescriptorFile() {
		return new File(ctx.cachedDescriptors(), target.name());
	}

	@Override
	public synchronized boolean isDirty() {
		String cachedDescriptor = cachedDescriptor();
		if (cachedDescriptor == null
				|| !cachedDescriptor.equals(target.contentDescriptor())) {
			return true;
		}
		File cachedContent = target.cachedAt(ctx);
		if (!cachedContent.exists()) {
			return true;
		}
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
