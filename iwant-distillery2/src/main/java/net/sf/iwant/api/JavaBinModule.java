package net.sf.iwant.api;

import java.io.IOException;

public abstract class JavaBinModule extends JavaModule {

	public static IwantBinModuleSpex named(String name) {
		return new IwantBinModuleSpex(name);
	}

	public static JavaBinModule providing(Path mainArtifact) {
		return new PathProvider(mainArtifact);
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

		private Path mainArtifact;

		public PathProvider(Path mainArtifact) {
			this.mainArtifact = mainArtifact;
		}

		@Override
		public String eclipseSourceReference(TargetEvaluationContext ctx) {
			// TODO support source
			return null;
		}

		@Override
		public String eclipseBinaryReference(TargetEvaluationContext ctx) {
			try {
				return ctx.cached(mainArtifact).getCanonicalPath();
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
