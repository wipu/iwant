package org.fluentjava.iwant.eclipsesettings;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.fluentjava.iwant.api.core.Concatenated;
import org.fluentjava.iwant.api.core.Concatenated.ConcatenatedBuilder;
import org.fluentjava.iwant.api.javamodules.JavaBinModule;
import org.fluentjava.iwant.api.javamodules.JavaModule;
import org.fluentjava.iwant.api.javamodules.JavaSrcModule;
import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.SideEffect;
import org.fluentjava.iwant.api.model.SideEffectContext;
import org.fluentjava.iwant.entry.Iwant;

public class EclipseSettings implements SideEffect {

	private final String name;
	private final SortedSet<JavaModule> javaModules;

	private EclipseSettings(String name, SortedSet<JavaModule> javaModules) {
		this.name = name;
		this.javaModules = Collections.unmodifiableSortedSet(javaModules);
	}

	@Override
	public String name() {
		return name;
	}

	public SortedSet<JavaModule> modules() {
		return javaModules;
	}

	@Override
	public void mutate(SideEffectContext ctx) {
		Exception refreshFailure = bestEffortToEnsureFreshReferences(ctx);
		try {
			generateEclipseSettings(ctx);
			ensureSrcDirs(ctx);
			if (refreshFailure != null) {
				try (PrintWriter err = new PrintWriter(ctx.err())) {
					err.println(
							"WARNING: Refresh of eclipse settings references failed:\n"
									+ refreshFailure);
				}
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Eclipse settings generation failed.",
					e);
		}
	}

	private void ensureSrcDirs(SideEffectContext ctx) {
		for (JavaModule mod : javaModules) {
			if (mod instanceof JavaSrcModule) {
				ensureSrcDirs(ctx, (JavaSrcModule) mod);
			}
		}
	}

	private static void ensureSrcDirs(SideEffectContext ctx,
			JavaSrcModule mod) {
		File modDir = new File(ctx.wsRoot(), mod.locationUnderWsRoot());
		ensureSrcDirs(modDir, mod.mainJavas());
		ensureSrcDirs(modDir, mod.mainResources());
		ensureSrcDirs(modDir, mod.testJavas());
		ensureSrcDirs(modDir, mod.testResources());
	}

	private static void ensureSrcDirs(File modDir,
			List<String> relativeSrcDirs) {
		for (String relativeSrcDir : relativeSrcDirs) {
			File srcDir = new File(modDir, relativeSrcDir);
			Iwant.mkdirs(srcDir);
		}
	}

	private Exception bestEffortToEnsureFreshReferences(SideEffectContext ctx) {
		ConcatenatedBuilder classLocations = Concatenated
				.named(name + ".bin-refs");
		Set<Path> paths = new LinkedHashSet<>();
		for (JavaModule mod : javaModules) {
			Set<JavaModule> modDeps = EclipseProject.dependenciesOf(mod);
			for (JavaModule dep : modDeps) {
				if (!(dep instanceof JavaBinModule)) {
					continue;
				}
				JavaBinModule binDep = (JavaBinModule) dep;
				if (binDep.mainArtifact() != null) {
					paths.add(binDep.mainArtifact());
				}
				if (binDep.source() != null) {
					paths.add(binDep.source());
				}
			}
		}
		for (Path path : paths) {
			classLocations.nativePathTo(path).string("\n");
		}
		try {
			ctx.iwantFreshCached(classLocations.end());
			return null;
		} catch (Exception e) {
			return e;
		}
	}

	private void generateEclipseSettings(SideEffectContext ctx) {
		EclipseSettingsWriter writer = EclipseSettingsWriter.with().context(ctx)
				.modules(javaModules).end();
		writer.write();
	}

	public static EclipseSettingsSpex with() {
		return new EclipseSettingsSpex();
	}

	public static class EclipseSettingsSpex {

		private String name;

		private final SortedSet<JavaModule> javaModules = new TreeSet<>();

		public EclipseSettingsSpex name(String name) {
			this.name = name;
			return this;
		}

		public EclipseSettingsSpex modules(JavaModule... modules) {
			return modules(Arrays.asList(modules));
		}

		public EclipseSettingsSpex modules(JavaSrcModule... modules) {
			return modules(Arrays.asList(modules));
		}

		public EclipseSettingsSpex modules(
				Collection<? extends JavaModule> modules) {
			javaModules.addAll(modules);
			return this;
		}

		public EclipseSettings end() {
			return new EclipseSettings(name, javaModules);
		}

	}

}
