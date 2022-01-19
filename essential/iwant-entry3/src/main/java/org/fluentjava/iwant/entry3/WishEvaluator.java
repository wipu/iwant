package org.fluentjava.iwant.entry3;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fluentjava.iwant.api.bash.TargetImplementedInBash;
import org.fluentjava.iwant.api.javamodules.JavaModule;
import org.fluentjava.iwant.api.javamodules.JavaSrcModule;
import org.fluentjava.iwant.api.model.Caches;
import org.fluentjava.iwant.api.model.IwantCoreServices;
import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.SideEffect;
import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.api.model.TargetEvaluationContext;
import org.fluentjava.iwant.api.model.WsInfo;
import org.fluentjava.iwant.api.wsdef.IKnowWhatIAmDoingContext;
import org.fluentjava.iwant.api.wsdef.IwantPluginWishes;
import org.fluentjava.iwant.api.wsdef.SideEffectDefinitionContext;
import org.fluentjava.iwant.api.wsdef.TargetDefinitionContext;
import org.fluentjava.iwant.api.wsdef.Workspace;
import org.fluentjava.iwant.api.wsdef.WorkspaceContext;
import org.fluentjava.iwant.api.wsdef.WorkspaceModuleContext;
import org.fluentjava.iwant.coreservices.IwantCoreServicesImpl;
import org.fluentjava.iwant.coreservices.StreamUtil;
import org.fluentjava.iwant.entry.Iwant;
import org.fluentjava.iwant.planner.Planner;

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
		List<? extends Target> targets = ws.targets(ctx);
		ctx.setTargetsAndInjectIngrDefCtx(targets);

		failIfConflictingPathDefinitions(targets);
		TargetNameChecker.check(targets);

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
		for (Target target : targets) {
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
		throw new IllegalArgumentException(
				"Illegal wish: " + wish + "\nlegal targets:" + targets);
	}

	private static void failIfConflictingPathDefinitions(
			List<? extends Target> targets) {
		PathDefinitionConflictChecker.failIfConflictingPathDefinitions(targets);
	}

	File freshCachedContent(Path path) {
		Iwant.debugLog("freshCachedContent", path);
		File cachedContent = ctx.cached(path);
		if (path instanceof Target) {
			Target target = (Target) path;
			try {
				Map<String, TargetRefreshTask> taskInstanceCache = new HashMap<>();
				Planner planner = new Planner(TargetRefreshTask.instance(target,
						ctx, caches, taskInstanceCache), workerCount);
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

	private class Ctx implements IKnowWhatIAmDoingContext {

		private final IwantCoreServices iwantCoreServices = new IwantCoreServicesImpl(
				iwant);
		private List<? extends Target> targets;

		private void setTargetsAndInjectIngrDefCtx(
				List<? extends Target> targets) {
			this.targets = targets;
			for (Target target : targets) {
				if (target instanceof TargetImplementedInBash) {
					TargetImplementedInBash tib = (TargetImplementedInBash) target;
					tib.setIngredientDefinitionContext(this);
				}
			}
		}

		@Override
		public List<? extends Target> targets() {
			return targets;
		}

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
		public File locationOf(Source src) {
			return cached(src);
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
