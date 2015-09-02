package net.sf.iwant.entry3;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Set;

import net.sf.iwant.api.javamodules.JavaModule;
import net.sf.iwant.api.javamodules.JavaSrcModule;
import net.sf.iwant.api.model.Caches;
import net.sf.iwant.api.model.IwantCoreServices;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.SideEffect;
import net.sf.iwant.api.model.SideEffectContext;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.model.TargetEvaluationContext;
import net.sf.iwant.api.model.WsInfo;
import net.sf.iwant.api.wsdef.IwantPluginWishes;
import net.sf.iwant.api.wsdef.SideEffectDefinitionContext;
import net.sf.iwant.api.wsdef.TargetDefinitionContext;
import net.sf.iwant.api.wsdef.Workspace;
import net.sf.iwant.api.wsdef.WorkspaceContext;
import net.sf.iwant.api.wsdef.WorkspaceModuleContext;
import net.sf.iwant.coreservices.IwantCoreServicesImpl;
import net.sf.iwant.coreservices.StreamUtil;
import net.sf.iwant.entry.Iwant;
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
	private final JavaSrcModule wsdefdefJavaModule;
	private final JavaSrcModule wsdefJavaModule;
	private final WorkspaceModuleContext wsDefCtx;

	public WishEvaluator(OutputStream out, OutputStream err, File wsRoot,
			Iwant iwant, WsInfo wsInfo, Caches caches, int workerCount,
			JavaSrcModule wsdefdefJavaModule, JavaSrcModule wsdefJavaModule,
			WorkspaceModuleContext wsDefCtx) {
		this.out = out;
		this.err = err;
		this.wsRoot = wsRoot;
		this.iwant = iwant;
		this.wsInfo = wsInfo;
		this.caches = caches;
		this.workerCount = workerCount;
		this.wsdefdefJavaModule = wsdefdefJavaModule;
		this.wsdefJavaModule = wsdefJavaModule;
		this.wsDefCtx = wsDefCtx;
		this.ctx = new Ctx();
	}

	public TargetEvaluationContext targetEvaluationContext() {
		return ctx;
	}

	public SideEffectDefinitionContext sideEffectDefinitionContext() {
		return ctx;
	}

	public TargetDefinitionContext targetDefinitionContext() {
		return ctx;
	}

	public WorkspaceContext workspaceContext() {
		return ctx;
	}

	public void iwant(String wish, Workspace ws) {
		failIfConflictingPathDefinitions(ws);
		if ("list-of/targets".equals(wish)) {
			PrintWriter wr = new PrintWriter(out);
			for (Target target : ws.targets(ctx)) {
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
		for (Target target : ws.targets(ctx)) {
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
				+ "\nlegal targets:" + ws.targets(ctx));
	}

	private void failIfConflictingPathDefinitions(Workspace ws) {
		PathDefinitionConflictChecker.failIfConflictingPathDefinitions(ws
				.targets(ctx));
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

	public void asPath(Path path) {
		File cachedContent = freshCachedContent(path);
		PrintWriter wr = new PrintWriter(out);
		wr.println(cachedContent);
		wr.close();
	}

	private class Ctx implements TargetEvaluationContext, SideEffectContext,
			SideEffectDefinitionContext, TargetDefinitionContext,
			WorkspaceContext {

		private final IwantCoreServices iwantCoreServices = new IwantCoreServicesImpl(
				iwant);

		@Override
		public IwantCoreServices iwant() {
			return iwantCoreServices;
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
		public JavaSrcModule wsdefdefJavaModule() {
			return wsdefdefJavaModule;
		}

		@Override
		public JavaSrcModule wsdefJavaModule() {
			return wsdefJavaModule;
		}

		@Override
		public File freshTemporaryDirectory() {
			String workerName = Thread.currentThread().getName();
			return caches.temporaryDirectory(workerName);
		}

		@Override
		public Set<? extends JavaModule> iwantApiModules() {
			return wsDefCtx.iwantApiModules();
		}

		@Override
		public IwantPluginWishes iwantPlugin() {
			return wsDefCtx.iwantPlugin();
		}

		@Override
		public File iwantFreshCached(Path target) {
			return freshCachedContent(target);
		}

	}

}
