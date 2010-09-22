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
	private final Source src;
	private final List<Path> libs;

	public EclipseProject(String name, Source src, List<Path> libs) {
		this.name = name;
		this.src = src;
		this.libs = libs;
	}

	public static EclipseProjectBuilder with() {
		return new EclipseProjectBuilder();
	}

	public static class EclipseProjectBuilder {

		private String name;

		private Source src;

		private final List<Path> libs = new ArrayList();

		public EclipseProjectBuilder name(String name) {
			this.name = name;
			return this;
		}

		public EclipseProjectBuilder src(Source src) {
			this.src = src;
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

		public EclipseProject end() {
			return new EclipseProject(name, src, libs);
		}

	}

	public String name() {
		return name;
	}

	public Source src() {
		return src;
	}

	public List<Path> libs() {
		return libs;
	}

	public String definitionDescription() {
		StringBuilder b = new StringBuilder();
		b.append(getClass().getCanonicalName() + " {\n");
		b.append("  name:" + name).append("\n");
		b.append("  src:").append(src.name()).append("\n");
		b.append("  libs {\n");
		for (Path lib : libs) {
			b.append("    ").append(lib.name()).append("\n");
		}
		b.append("  }\n");
		b.append("}\n");
		return b.toString();
	}

	public int compareTo(EclipseProject o) {
		return name.compareTo(o.name);
	}

	void refresh(File destination) throws Exception {
		ensureDir(destination);
		dotProject(new File(destination.getCanonicalPath() + "/.project"));
		dotClasspath(new File(destination.getCanonicalPath() + "/.classpath"));
	}

	/**
	 * TODO create and reuse a fluent reusable file declaration library
	 */
	private static void ensureDir(File dir) {
		File parent = dir.getParentFile();
		if (!parent.exists()) {
			ensureDir(parent);
		}
		dir.mkdir();
	}

	/**
	 * TODO encapsulate all information inside a Path abstraction so we can get
	 * an absolute or a relative path whenever needed.
	 */
	private static String abs(String path) {
		try {
			return new File(path).getCanonicalPath();
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private void dotProject(File destination) throws IOException {
		StringBuilder b = new StringBuilder();
		b.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		b.append("<projectDescription>\n");
		b.append("        <name>" + name() + "</name>\n");
		b.append("        <comment></comment>\n");
		b.append("        <projects>\n");
		b.append("        </projects>\n");
		b.append("        <buildSpec>\n");
		b.append("                <buildCommand>\n");
		b.append("                        <name>org.eclipse.jdt.core.javabuilder</name>\n");
		b.append("                        <arguments>\n");
		b.append("                        </arguments>\n");
		b.append("                </buildCommand>\n");
		b.append("        </buildSpec>\n");
		b.append("        <natures>\n");
		b.append("                <nature>org.eclipse.jdt.core.javanature</nature>\n");
		b.append("        </natures>\n");
		b.append("        <linkedResources>\n");
		b.append("                <link>\n");
		b.append("                        <name>src</name>\n");
		b.append("                        <type>2</type>\n");
		b.append("                        <location>" + abs(src.name())
				+ "</location>\n");
		b.append("                </link>\n");
		b.append("        </linkedResources>\n");
		b.append("</projectDescription>\n");
		new FileWriter(destination).append(b.toString()).close();
	}

	private void dotClasspath(File destination) throws IOException {
		StringBuilder b = new StringBuilder();
		b.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		b.append("<classpath>\n");
		b.append("        <classpathentry kind=\"src\" path=\"src\"/>\n");
		b.append("        <classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER\"/>\n");
		for (Path lib : libs) {
			b.append("        <classpathentry kind=\"lib\" path=\""
					+ abs(lib.name()) + "\"/>\n");
		}
		b.append("        <classpathentry kind=\"output\" path=\"classes\"/>\n");
		b.append("</classpath>\n");
		new FileWriter(destination).append(b.toString()).close();
	}

}
