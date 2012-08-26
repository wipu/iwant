package net.sf.iwant.entry3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import net.sf.iwant.api.CacheLocations;
import net.sf.iwant.api.IwantWorkspace;
import net.sf.iwant.api.Path;
import net.sf.iwant.api.Target;
import net.sf.iwant.api.TargetEvaluationContext;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.io.StreamUtil;

public class WishEvaluator {

	private final OutputStream out;
	private final File asSomeone;
	private final File wsRoot;
	private final Iwant iwant;
	private final Ctx ctx;

	public WishEvaluator(OutputStream out, File asSomeone, File wsRoot,
			Iwant iwant) {
		this.out = out;
		this.asSomeone = asSomeone;
		this.wsRoot = wsRoot;
		this.iwant = iwant;
		this.ctx = new Ctx();
	}

	public void iwant(String wish, IwantWorkspace ws) {
		if ("list-of/targets".equals(wish)) {
			PrintWriter wr = new PrintWriter(out);
			for (Target target : ws.targets()) {
				wr.println(target.name());
			}
			wr.close();
			return;
		}
		if ("list-of/side-effects".equals(wish)) {
			PrintWriter wr = new PrintWriter(out);
			wr.println("eclipse-settings");
			wr.close();
			return;
		}
		for (Target target : ws.targets()) {
			if (wish.equals("target/" + target.name() + "/as-path")) {
				asPath(target);
				return;
			}
			if (wish.equals("target/" + target.name() + "/content")) {
				content(target);
				return;
			}
		}
		if ("side-effect/eclipse-settings/effective".equals(wish)) {
			PrintWriter wr = new PrintWriter(out);
			wr.println("todo implement");
			wr.close();
			return;
		}
		throw new IllegalArgumentException("Illegal wish: " + wish
				+ "\nlegal targets:" + ws.targets());
	}

	public void content(Target target) {
		try {
			refreshIngredients(target);
			StreamUtil.pipe(target.content(ctx), out);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Piping content failed", e);
		}
	}

	private void refreshIngredients(Path path) {
		for (Path ingredient : path.ingredients()) {
			refreshCache(ingredient);
		}
	}

	private File refreshCache(Path path) {
		File cachedTarget = path.cachedAt(ctx);
		if (!needsRefreshing(path)) {
			return cachedTarget;
		}
		Iwant.ensureDir(cachedTarget.getParentFile());
		try {
			path.path(ctx);
			String newDescriptor = path.contentDescriptor();
			new FileWriter(descriptorFile(path)).append(newDescriptor).close();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Refresh failed", e);
		}
		return cachedTarget;
	}

	boolean needsRefreshing(Path path) {
		try {
			File cachedTarget = path.cachedAt(ctx);
			if (!cachedTarget.exists()) {
				return true;
			}
			// TODO how to handle Source here
			File descriptorFile = descriptorFile(path);
			if (!descriptorFile.exists()) {
				return true;
			}
			String cachedDescriptor = StreamUtil.toString(new FileInputStream(
					descriptorFile));
			String currentDescriptor = path.contentDescriptor();
			if (!cachedDescriptor.equals(currentDescriptor)) {
				return true;
			}
			return false;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private File descriptorFile(Path path) {
		return new File(cachedDescriptors(), path.name());
	}

	private File cachedDescriptors() {
		File descriptors = new File(asSomeone, ".todo-cached/descriptor");
		Iwant.ensureDir(descriptors);
		return descriptors;
	}

	public void asPath(Path target) {
		refreshIngredients(target);
		File cachedContent = refreshCache(target);
		PrintWriter wr = new PrintWriter(out);
		wr.println(cachedContent);
		wr.close();
	}

	private File cachedModifiable() {
		return new File(asSomeone, ".todo-cached/target");
	}

	private class Ctx implements TargetEvaluationContext, CacheLocations {

		@Override
		public CacheLocations cached() {
			return this;
		}

		@Override
		public Iwant iwant() {
			return iwant;
		}

		@Override
		public File modifiableTargets() {
			return cachedModifiable();
		}

		@Override
		public File wsRoot() {
			return wsRoot;
		}

		@Override
		public File freshPathTo(Path path) {
			return path.cachedAt(this);
		}

	}

}
