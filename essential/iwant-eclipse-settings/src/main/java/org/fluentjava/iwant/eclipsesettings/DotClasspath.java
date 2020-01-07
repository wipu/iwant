package org.fluentjava.iwant.eclipsesettings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DotClasspath {

	private final List<String> srcs;
	private final List<String> containers;
	private final List<String> deps;

	public DotClasspath(List<String> srcs, List<String> containers,
			List<String> deps) {
		this.srcs = srcs;
		this.containers = containers;
		this.deps = deps;
	}

	public List<String> srcs() {
		return Collections.unmodifiableList(srcs);
	}

	public List<String> deps() {
		return Collections.unmodifiableList(deps);
	}

	public static DotClasspathSpex with() {
		return new DotClasspathSpex()
				.container("org.eclipse.jdt.launching.JRE_CONTAINER");
	}

	public String asFileContent() {
		StringBuilder b = new StringBuilder();
		b.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		b.append("<classpath>\n");
		for (String src : srcs) {
			b.append(src);
		}
		for (String container : containers) {
			b.append("        <classpathentry kind=\"con\" path=\"" + container
					+ "\"/>\n");
		}
		for (String dep : deps) {
			b.append(dep);
		}
		b.append(
				"        <classpathentry kind=\"output\" path=\"classes\"/>\n");
		b.append("</classpath>\n");
		return b.toString();
	}

	public static class DotClasspathSpex {

		private final List<String> containers = new ArrayList<>();
		private final List<String> srcs = new ArrayList<>();
		private final List<String> deps = new ArrayList<>();

		public DotClasspath end() {
			return new DotClasspath(srcs, containers, deps);
		}

		public DotClasspathSpex container(String container) {
			containers.add(container);
			return this;
		}

		public DotClasspathSpex kotlinContainer() {
			return container("org.jetbrains.kotlin.core.KOTLIN_CONTAINER");
		}

		public DotClasspathSpex src(String src) {
			srcs.add("        <classpathentry kind=\"src\" path=\"" + src
					+ "\"/>\n");
			return this;
		}

		public DotClasspathSpex srcDep(String projectName) {
			deps.add(
					"        <classpathentry combineaccessrules=\"false\" kind=\"src\" path=\"/"
							+ projectName + "\"/>\n");
			return this;
		}

		public DotClasspathSpex binDep(String jarPath) {
			deps.add("        <classpathentry kind=\"lib\" path=\"" + jarPath
					+ "\"/>\n");
			return this;
		}

		public DotClasspathSpex binDep(String jarPath, String srcZipPath) {
			deps.add("        <classpathentry kind=\"lib\" path=\"" + jarPath
					+ "\" sourcepath=\"" + srcZipPath + "\"/>\n");
			return this;
		}

		public DotClasspathSpex exportedClasses(String binPath,
				String srcPath) {
			deps.add(
					"        <classpathentry exported=\"true\" kind=\"lib\" path=\""
							+ binPath + "\" sourcepath=\"" + srcPath
							+ "\"/>\n");
			return this;
		}

	}

}
