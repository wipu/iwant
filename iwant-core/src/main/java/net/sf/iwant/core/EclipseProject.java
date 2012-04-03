package net.sf.iwant.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * TODO this class' interface resembles Content so find a natural way to make
 * this one.
 */
public class EclipseProject implements Comparable<EclipseProject> {

	private final String name;
	private final List<String> srcs;
	private final List<Path> libs;
	private final boolean hasIwantAnt;
	private final Target<?>[] publicTargetsForAnt;

	public EclipseProject(String name, List<String> srcs, List<Path> libs,
			boolean hasIwantAnt, Target<?>[] publicTargetsForAnt) {
		this.name = name;
		this.srcs = srcs;
		this.libs = libs;
		this.hasIwantAnt = hasIwantAnt;
		this.publicTargetsForAnt = publicTargetsForAnt;
	}

	public static EclipseProjectBuilder with() {
		return new EclipseProjectBuilder();
	}

	public static class EclipseProjectBuilder {

		private String name;

		private final List<String> srcs = new ArrayList<String>();

		private final List<Path> libs = new ArrayList<Path>();

		private boolean hasIwantAnt;

		private Target<?>[] publicTargetsForAnt = new Target[0];

		public EclipseProjectBuilder name(String name) {
			this.name = name;
			return this;
		}

		public EclipseProjectBuilder src(String src) {
			this.srcs.add(src);
			return this;
		}

		public EclipseProjectBuilder lib(Path lib) {
			this.libs.add(lib);
			return this;
		}

		public EclipseProjectBuilder libs(Collection<? extends Path> libs) {
			this.libs.addAll(libs);
			return this;
		}

		public EclipseProjectBuilder iwantAnt(Target<?>... publicTargetsForAnt) {
			this.publicTargetsForAnt = publicTargetsForAnt;
			this.hasIwantAnt = true;
			return this;
		}

		public EclipseProject end() {
			return new EclipseProject(name, srcs, libs, hasIwantAnt,
					publicTargetsForAnt);
		}

	}

	public String name() {
		return name;
	}

	public List<String> srcs() {
		return srcs;
	}

	public List<Path> libs() {
		return libs;
	}

	public boolean hasIwantAnt() {
		return hasIwantAnt;
	}

	public Target<?>[] publicTargetsForAnt() {
		return publicTargetsForAnt;
	}

	public String definitionDescription() {
		StringBuilder b = new StringBuilder();
		b.append(getClass().getCanonicalName() + " {\n");
		b.append("  name:" + name).append("\n");
		b.append("  srcs:" + srcs).append("\n");
		b.append("  libs {\n");
		for (Path lib : libs) {
			b.append("    ").append(lib.name()).append("\n");
		}
		b.append("  }\n");
		b.append("  hasIwantAnt:").append(hasIwantAnt).append("\n");
		b.append("  publicTargets {\n");
		for (Target<?> publicTarget : publicTargetsForAnt)
			b.append("    ").append(publicTarget.name()).append("\n");
		b.append("  }\n");
		b.append("}\n");
		return b.toString();
	}

	@Override
	public int compareTo(EclipseProject o) {
		return name.compareTo(o.name);
	}

	void refresh(RefreshEnvironment refresh) throws Exception {
		FileUtils.ensureDir(refresh.destination());
		dotProject(refresh);
		dotClasspath(refresh);
	}

	private void dotProject(RefreshEnvironment refresh) throws IOException {
		File destination = new File(refresh.destination().getCanonicalPath()
				+ "/.project");
		StringBuilder b = new StringBuilder();
		b.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		b.append("<projectDescription>\n");
		b.append("        <name>" + name() + "</name>\n");
		b.append("        <comment></comment>\n");
		b.append("        <projects>\n");
		b.append("        </projects>\n");
		b.append("        <buildSpec>\n");
		if (hasIwantAnt) {
			b.append("                <buildCommand>\n");
			b.append("                        <name>org.eclipse.ui.externaltools.ExternalToolBuilder</name>\n");
			b.append("                        <triggers>auto,full,incremental,</triggers>\n");
			b.append("                        <arguments>\n");
			b.append("                                <dictionary>\n");
			b.append("                                        <key>LaunchConfigHandle</key>\n");
			b.append("                                        <value>&lt;project&gt;/.externalToolBuilders/iwant-ant-for-eclipse.launch</value>\n");
			b.append("                                </dictionary>\n");
			b.append("                        </arguments>\n");
			b.append("                </buildCommand>\n");
		}
		b.append("                <buildCommand>\n");
		b.append("                        <name>org.eclipse.jdt.core.javabuilder</name>\n");
		b.append("                        <arguments>\n");
		b.append("                        </arguments>\n");
		b.append("                </buildCommand>\n");
		b.append("        </buildSpec>\n");
		b.append("        <natures>\n");
		b.append("                <nature>org.eclipse.jdt.core.javanature</nature>\n");
		b.append("        </natures>\n");
		b.append("</projectDescription>\n");
		new FileWriter(destination).append(b.toString()).close();
	}

	private void dotClasspath(RefreshEnvironment refresh) throws IOException {
		File destination = new File(refresh.destination().getCanonicalPath()
				+ "/.classpath");
		StringBuilder b = new StringBuilder();
		b.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		b.append("<classpath>\n");
		for (String src : srcs) {
			b.append("        <classpathentry kind=\"src\" path=\"" + src
					+ "\"/>\n");
		}
		b.append("        <classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER\"/>\n");
		for (Path lib : libs) {
			b.append("        <classpathentry kind=\"lib\" path=\""
					+ FileUtils.abs(lib.asAbsolutePath(refresh.locations()))
					+ "\"/>\n");
		}
		b.append("        <classpathentry kind=\"output\" path=\"classes\"/>\n");
		b.append("</classpath>\n");
		new FileWriter(destination).append(b.toString()).close();
	}
}
