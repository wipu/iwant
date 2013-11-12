package net.sf.iwant.api.javamodules;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.TargetEvaluationContext;

public abstract class JavaBinModule extends JavaModule {

	private final String name;
	private final Set<JavaModule> runtimeDeps;

	private JavaBinModule(String name,
			Set<Class<? extends JavaModuleCharacteristic>> characteristics,
			Set<JavaModule> runtimeDeps) {
		super(characteristics);
		this.name = name;
		this.runtimeDeps = runtimeDeps;
	}

	public static IwantBinModuleSpex named(String name) {
		return new IwantBinModuleSpex(name);
	}

	public static PathProviderSpex providing(Path mainArtifact) {
		return providing(mainArtifact, null);
	}

	public static PathProviderSpex providing(Path mainArtifact, Path sources) {
		return new PathProviderSpex(mainArtifact, sources);
	}

	@Override
	public final String name() {
		return name;
	}

	@Override
	public final String toString() {
		return name();
	}

	public abstract Path source();

	public abstract String eclipseSourceReference(TargetEvaluationContext ctx);

	public abstract String eclipseBinaryReference(TargetEvaluationContext ctx);

	@Override
	public final Set<JavaModule> mainDepsForCompilation() {
		return Collections.emptySet();
	}

	@Override
	public final Set<JavaModule> mainDepsForRunOnly() {
		return runtimeDeps;
	}

	@Override
	public final Set<JavaModule> testDepsForCompilationExcludingMainDeps() {
		return Collections.emptySet();
	}

	@Override
	public final Set<JavaModule> testDepsForRunOnlyExcludingMainDeps() {
		return Collections.emptySet();
	}

	@Override
	public synchronized Set<JavaModule> effectivePathForTestCompile() {
		return Collections.emptySet();
	}

	@Override
	public synchronized Set<JavaModule> effectivePathForTestRuntime() {
		return Collections.emptySet();
	}

	public static class IwantBinModuleSpex {

		private String name;
		private String src;
		private final Set<Class<? extends JavaModuleCharacteristic>> characteristics = new HashSet<Class<? extends JavaModuleCharacteristic>>();
		private final Set<JavaModule> runtimeDeps = new LinkedHashSet<JavaModule>();

		public IwantBinModuleSpex(String name) {
			this.name = name;
		}

		public IwantBinModuleSpex source(String src) {
			this.src = src;
			return this;
		}

		public IwantBinModuleSpex has(
				Class<? extends JavaModuleCharacteristic> characteristic) {
			this.characteristics.add(characteristic);
			return this;
		}

		public IwantBinModuleSpex runtimeDeps(JavaModule... runtimeDeps) {
			return runtimeDeps(Arrays.asList(runtimeDeps));
		}

		public IwantBinModuleSpex runtimeDeps(
				Collection<? extends JavaModule> runtimeDeps) {
			this.runtimeDeps.addAll(runtimeDeps);
			return this;
		}

		public JavaBinModule inside(JavaSrcModule libsModule) {
			return new ProvidedBySrcModule(name, libsModule, src,
					characteristics, runtimeDeps);
		}

	}

	private static class ProvidedBySrcModule extends JavaBinModule {

		private JavaSrcModule libsModule;
		private String srcZip;

		public ProvidedBySrcModule(String name, JavaSrcModule libsModule,
				String srcZip,
				Set<Class<? extends JavaModuleCharacteristic>> characteristics,
				Set<JavaModule> runtimeDeps) {
			super(name, characteristics, runtimeDeps);
			this.libsModule = libsModule;
			this.srcZip = srcZip;
		}

		@Override
		public String eclipseBinaryReference(TargetEvaluationContext ctx) {
			return eclipseRef(name());
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
					+ name());
		}

		@Override
		public Path source() {
			if (srcZip == null) {
				return null;
			}
			return Source.underWsroot(libsModule.locationUnderWsRoot() + "/"
					+ srcZip);
		}

	}

	public static class PathProviderSpex {

		private final Path mainArtifact;
		private final Path sources;
		private final Set<Class<? extends JavaModuleCharacteristic>> characteristics = new HashSet<Class<? extends JavaModuleCharacteristic>>();
		private final Set<JavaModule> runtimeDeps = new LinkedHashSet<JavaModule>();

		private PathProviderSpex(Path mainArtifact, Path sources) {
			this.mainArtifact = mainArtifact;
			this.sources = sources;
		}

		public PathProviderSpex has(
				Class<? extends JavaModuleCharacteristic> characteristic) {
			this.characteristics.add(characteristic);
			return this;
		}

		public PathProviderSpex runtimeDeps(JavaModule... runtimeDeps) {
			return runtimeDeps(Arrays.asList(runtimeDeps));
		}

		public PathProviderSpex runtimeDeps(
				Collection<? extends JavaModule> runtimeDeps) {
			this.runtimeDeps.addAll(runtimeDeps);
			return this;
		}

		public JavaBinModule end() {
			return new PathProvider(mainArtifact, sources, characteristics,
					runtimeDeps);
		}

	}

	private static class PathProvider extends JavaBinModule {

		private final Path mainArtifact;
		private final Path sources;

		public PathProvider(Path mainArtifact, Path sources,
				Set<Class<? extends JavaModuleCharacteristic>> characteristics,
				Set<JavaModule> runtimeDeps) {
			super(mainArtifact.name(), characteristics, runtimeDeps);
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
		public Path mainArtifact() {
			return mainArtifact;
		}

		@Override
		public Path source() {
			return sources;
		}

	}

}
