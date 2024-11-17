package org.fluentjava.iwant.embedded;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;

import org.fluentjava.iwant.api.javamodules.JavaSrcModule;
import org.fluentjava.iwant.api.model.Caches;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.api.model.WsInfo;
import org.fluentjava.iwant.api.wsdef.WorkspaceModuleContext;
import org.fluentjava.iwant.entry.Iwant;
import org.fluentjava.iwant.entry3.CachesImpl;
import org.fluentjava.iwant.entry3.DefaultUserPrefs;
import org.fluentjava.iwant.entry3.UserPrefs;
import org.fluentjava.iwant.entry3.WishEvaluator;

public class AsEmbeddedIwantUser {

	public static IHavePlease with() {
		return new IHavePlease();
	}

	public static class IHavePlease {

		private File wsRoot;
		private File wsCache;
		private UserPrefs userPrefs = new DefaultUserPrefs(null);

		public IHavePlease workspaceAt(File wsRoot) {
			this.wsRoot = wsRoot;
			return this;
		}

		public IHavePlease cacheAt(File wsCache) {
			this.wsCache = wsCache;
			return this;
		}

		public IHavePlease userPrefs(UserPrefs userPrefs) {
			this.userPrefs = userPrefs;
			return this;
		}

		public TargetOrSideEffect iwant() {
			return new TargetOrSideEffect();
		}

		public class TargetOrSideEffect {

			public PathOrContent target(Target target) {
				return new PathOrContent(target);
			}

			public class PathOrContent {

				private final Target target;

				public PathOrContent(Target target) {
					this.target = target;
				}

				public File asPath() {
					try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
						OutputStream err = System.err;
						File iwantApiClasses = null;
						Iwant iwant = Iwant.usingRealNetwork();
						WsInfo wsInfo = null;
						Caches caches = new CachesImpl(wsCache, iwantApiClasses,
								iwant.network());
						int workerCount = userPrefs.workerCount();
						JavaSrcModule wsdefdefJavaModule = null;
						JavaSrcModule wsdefJavaModule = null;
						WorkspaceModuleContext wsdefCtx = null;
						WishEvaluator evaluator = new WishEvaluator(out, err,
								wsRoot, iwant, wsInfo, caches, workerCount,
								wsdefdefJavaModule, wsdefJavaModule, wsdefCtx);
						evaluator.asPath(target);
						String cachedTarget = out.toString();
						// TODO refactoring needed, we shouldn't be parsing
						// strings from our own code
						cachedTarget = cachedTarget.replaceAll("\n", "");
						return new File(cachedTarget);
					} catch (RuntimeException e) {
						throw e;
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}

			}

		}

	}

}
