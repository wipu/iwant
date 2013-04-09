package net.sf.iwant.api.javamodules;

import java.io.IOException;

import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.TargetEvaluationContext;

public abstract class JavaBinModule extends JavaModule {

	public static IwantBinModuleSpex named(String name) {
		return new IwantBinModuleSpex(name);
	}

	public static JavaBinModule providing(Path mainArtifact) {
		return providing(mainArtifact, null);
	}

	public static JavaBinModule providing(Path mainArtifact, Path sources) {
		return new PathProvider(mainArtifact, sources);
	}

	public abstract String eclipseSourceReference(TargetEvaluationContext ctx);

	public abstract String eclipseBinaryReference(TargetEvaluationContext ctx);

	public static class IwantBinModuleSpex {

		private String name;
		private String src;

		public IwantBinModuleSpex(String name) {
			this.name = name;
		}

		public IwantBinModuleSpex source(String src) {
			this.src = src;
			return this;
		}

		public JavaBinModule inside(JavaSrcModule libsModule) {
			return new ProvidedBySrcModule(name, libsModule, src);
		}

	}

	private static class ProvidedBySrcModule extends JavaBinModule {

		private final String name;
		private JavaSrcModule libsModule;
		private String srcZip;

		public ProvidedBySrcModule(String name, JavaSrcModule libsModule,
				String srcZip) {
			this.name = name;
			this.libsModule = libsModule;
			this.srcZip = srcZip;
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public String eclipseBinaryReference(TargetEvaluationContext ctx) {
			return eclipseRef(name);
		}

		private String eclipseRef(String name) {
			return "/" + libsModule.name() + "/" + name;
		}

		@Override
		public String eclipseSourceReference(TargetEvaluationContext ctx) {
			return srcZip == null ? null : eclipseRef(srcZip);
		}

		@Override
		public Path mainArtifact() {
			return Source.underWsroot(libsModule.locationUnderWsRoot() + "/"
					+ name);
		}

	}

	private static class PathProvider extends JavaBinModule {

		private final Path mainArtifact;
		private final Path sources;

		public PathProvider(Path mainArtifact, Path sources) {
			this.mainArtifact = mainArtifact;
			this.sources = sources;
		}

		@Override
		public String eclipseSourceReference(TargetEvaluationContext ctx) {
			if (sources == null) {
				return null;
			}
			return cached(ctx, sources);
		}

		@Override
		public String eclipseBinaryReference(TargetEvaluationContext ctx) {
			return cached(ctx, mainArtifact);
		}

		private static String cached(TargetEvaluationContext ctx, Path path) {
			try {
				return ctx.cached(path).getCanonicalPath();
			} catch (IOException e) {
				throw new IllegalArgumentException(e);
			}
		}

		@Override
		public String name() {
			return mainArtifact.name();
		}

		@Override
		public Path mainArtifact() {
			return mainArtifact;
		}

	}

}
