package net.sf.iwant.entry3;

import java.io.File;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.PrintWriter;

import net.sf.iwant.api.CacheLocations;
import net.sf.iwant.api.IwantWorkspace;
import net.sf.iwant.api.Path;
import net.sf.iwant.api.Target;
import net.sf.iwant.api.TargetEvaluationContext;
import net.sf.iwant.eclipsesettings.DotClasspath;
import net.sf.iwant.eclipsesettings.DotProject;
import net.sf.iwant.eclipsesettings.OrgEclipseJdtCorePrefs;
import net.sf.iwant.eclipsesettings.OrgEclipseJdtUiPrefs;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.io.StreamUtil;
import net.sf.iwant.planner.Planner;

public class WishEvaluator {

	private final OutputStream out;
	private final File asSomeone;
	private final File wsRoot;
	private final File iwantApiClasses;
	private final Iwant iwant;
	private final Ctx ctx;

	public WishEvaluator(OutputStream out, File asSomeone, File wsRoot,
			File iwantApiClasses, Iwant iwant) {
		this.out = out;
		this.asSomeone = asSomeone;
		this.wsRoot = wsRoot;
		this.iwantApiClasses = iwantApiClasses;
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
			generateEclipseSettings();
			return;
		}
		throw new IllegalArgumentException("Illegal wish: " + wish
				+ "\nlegal targets:" + ws.targets());
	}

	/**
	 * TODO delegate to a side-effect that really reads wsdef
	 */
	private void generateEclipseSettings() {
		try {
			// TODO read wsdef from WsInfo (needs modifications):
			WsDefEclipseProject proj = new WsDefEclipseProject(
					asSomeone.getName(), "i-have/wsdef", iwantApiClasses);
			DotProject dotProject = proj.dotProject();
			new FileWriter(new File(asSomeone, ".project")).append(
					dotProject.asFileContent()).close();

			DotClasspath dotClasspath = proj.dotClasspath();
			new FileWriter(new File(asSomeone, ".classpath")).append(
					dotClasspath.asFileContent()).close();

			new File(asSomeone, ".settings").mkdirs();
			new FileWriter(new File(asSomeone,
					".settings/org.eclipse.jdt.core.prefs")).append(
					OrgEclipseJdtCorePrefs.withDefaultValues().asFileContent())
					.close();

			new FileWriter(new File(asSomeone,
					".settings/org.eclipse.jdt.ui.prefs")).append(
					OrgEclipseJdtUiPrefs.withDefaultValues().asFileContent())
					.close();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Eclipse settings generation failed.", e);
		}
	}

	private File freshCachedContent(Path path) {
		File cachedContent = path.cachedAt(ctx);
		if (path instanceof Target) {
			Target target = (Target) path;
			try {
				Planner planner = new Planner(
						new TargetRefreshTask(target, ctx), 1);
				planner.start();
				planner.join();
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				throw new RuntimeException("Piping content failed", e);
			}
		}
		return cachedContent;
	}

	public void content(Target target) {
		try {
			freshCachedContent(target);
			StreamUtil.pipe(target.content(ctx), out);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Piping content failed", e);
		}
	}

	boolean needsRefreshing(Path path) {
		if (!(path instanceof Target)) {
			return false;
		}
		Target target = (Target) path;
		return new TargetRefreshTask(target, ctx).isDirty();
	}

	private File cachedDescriptors() {
		File descriptors = new File(asSomeone, ".todo-cached/descriptor");
		descriptors.mkdirs();
		return descriptors;
	}

	public void asPath(Path path) {
		File cachedContent = freshCachedContent(path);
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

		@Override
		public File cachedDescriptors() {
			return WishEvaluator.this.cachedDescriptors();
		}

	}

}
