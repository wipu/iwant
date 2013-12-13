package net.sf.iwant.api;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.iwant.api.javamodules.JavaBinModule;
import net.sf.iwant.api.javamodules.JavaModule;
import net.sf.iwant.api.javamodules.JavaSrcModule;
import net.sf.iwant.api.model.Concatenated;
import net.sf.iwant.api.model.Concatenated.ConcatenatedBuilder;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.SideEffect;
import net.sf.iwant.api.model.SideEffectContext;
import net.sf.iwant.eclipsesettings.EclipseSettingsWriter;

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
			if (refreshFailure != null) {
				PrintWriter err = new PrintWriter(ctx.err());
				err.println("WARNING: Refresh of eclipse settings references failed:\n"
						+ refreshFailure);
				err.close();
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Eclipse settings generation failed.", e);
		}
	}

	private Exception bestEffortToEnsureFreshReferences(SideEffectContext ctx) {
		ConcatenatedBuilder classLocations = Concatenated.named(name
				+ ".bin-refs");
		Set<Path> paths = new LinkedHashSet<Path>();
		for (JavaModule mod : javaModules) {
			Set<JavaModule> modDeps = new LinkedHashSet<JavaModule>();
			modDeps.addAll(mod.effectivePathForMainForCompile());
			modDeps.addAll(mod.effectivePathForTestCompile());
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
			classLocations.pathTo(path).string("\n");
		}
		try {
			ctx.iwantAsPath(classLocations.end());
			return null;
		} catch (Exception e) {
			return e;
		}
	}

	private void generateEclipseSettings(SideEffectContext ctx) {
		EclipseSettingsWriter writer = EclipseSettingsWriter.with()
				.context(ctx).modules(javaModules).end();
		writer.write();
	}

	public static EclipseSettingsSpex with() {
		return new EclipseSettingsSpex();
	}

	public static class EclipseSettingsSpex {

		private String name;

		private final SortedSet<JavaModule> javaModules = new TreeSet<JavaModule>();

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
