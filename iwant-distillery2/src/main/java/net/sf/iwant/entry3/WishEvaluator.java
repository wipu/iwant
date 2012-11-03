package net.sf.iwant.entry3;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;

import net.sf.iwant.api.IwantWorkspace;
import net.sf.iwant.api.JavaModule;
import net.sf.iwant.api.Path;
import net.sf.iwant.api.SideEffect;
import net.sf.iwant.api.SideEffectContext;
import net.sf.iwant.api.SideEffectDefinitionContext;
import net.sf.iwant.api.Target;
import net.sf.iwant.api.TargetEvaluationContext;
import net.sf.iwant.api.WsInfo;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.io.StreamUtil;
import net.sf.iwant.planner.Planner;

public class WishEvaluator {

	private final OutputStream out;
	private final OutputStream err;
	private final File wsRoot;
	private final Iwant iwant;
	private final WsInfo wsInfo;
	private final Ctx ctx;
	private final Caches caches;
	private final int workerCount;
	private final JavaModule wsdefdefJavaModule;
	private final JavaModule wsdefJavaModule;

	public WishEvaluator(OutputStream out, OutputStream err, File wsRoot,
			Iwant iwant, WsInfo wsInfo, Caches caches, int workerCount,
			JavaModule wsdefdefJavaModule, JavaModule wsdefJavaModule) {
		this.out = out;
		this.err = err;
		this.wsRoot = wsRoot;
		this.iwant = iwant;
		this.wsInfo = wsInfo;
		this.caches = caches;
		this.workerCount = workerCount;
		this.wsdefdefJavaModule = wsdefdefJavaModule;
		this.wsdefJavaModule = wsdefJavaModule;
		this.ctx = new Ctx();
	}

	public TargetEvaluationContext targetEvaluationContext() {
		return ctx;
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
			for (SideEffect se : ws.sideEffects(ctx)) {
				wr.println(se.name());
			}
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
		for (SideEffect se : ws.sideEffects(ctx)) {
			if (("side-effect/" + se.name() + "/effective").equals(wish)) {
				try {
					se.mutate(ctx);
				} catch (RuntimeException e) {
					throw e;
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				return;
			}
		}
		throw new IllegalArgumentException("Illegal wish: " + wish
				+ "\nlegal targets:" + ws.targets());
	}

	File freshCachedContent(Path path) {
		Iwant.debugLog("freshCachedContent", path);
		File cachedContent = ctx.cached(path);
		if (path instanceof Target) {
			Target target = (Target) path;
			try {
				Planner planner = new Planner(new TargetRefreshTask(target,
						ctx, caches), workerCount);
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
		return new TargetRefreshTask(target, ctx, caches).isDirty();
	}

	public void asPath(Path path) {
		File cachedContent = freshCachedContent(path);
		PrintWriter wr = new PrintWriter(out);
		wr.println(cachedContent);
		wr.close();
	}

	private class Ctx implements TargetEvaluationContext, SideEffectContext,
			SideEffectDefinitionContext {

		@Override
		public Iwant iwant() {
			return iwant;
		}

		@Override
		public File wsRoot() {
			return wsRoot;
		}

		@Override
		public File cached(Path path) {
			return caches.contentOf(path);
		}

		@Override
		public WsInfo wsInfo() {
			return wsInfo;
		}

		@Override
		public OutputStream err() {
			return err;
		}

		@Override
		public TargetEvaluationContext targetEvaluationContext() {
			return this;
		}

		@Override
		public JavaModule wsdefdefJavaModule() {
			return wsdefdefJavaModule;
		}

		@Override
		public JavaModule wsdefJavaModule() {
			return wsdefJavaModule;
		}

	}

}
