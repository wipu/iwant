package net.sf.iwant.plugin.war;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.PatternSet.NameEntry;
import org.apache.tools.ant.types.ZipFileSet;

import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.model.TargetEvaluationContext;

public class War extends Target {

	private final Path basedir;
	private final Path webXmlOrWebXmlDirectory;
	private final String webXmlName;
	private final List<String> excludes;
	private final List<Path> libs;
	private final List<Path> classeses;
	private final List<Path> resourceDirectories;

	public War(String name, Path basedir, Path webXmlOrWebXmlDirectory,
			String webXmlName, List<String> excludes, List<Path> libs,
			List<Path> classeses, List<Path> resourceDirectories) {
		super(name);
		this.basedir = basedir;
		this.webXmlOrWebXmlDirectory = webXmlOrWebXmlDirectory;
		this.webXmlName = webXmlName;
		this.excludes = excludes;
		this.libs = libs;
		this.classeses = classeses;
		this.resourceDirectories = resourceDirectories;
	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public String contentDescriptor() {
		StringBuilder b = new StringBuilder();
		b.append(getClass().getCanonicalName()).append(" {\n");
		b.append("  basedir:" + basedir).append("\n");

		b.append("  webxml:" + webXmlOrWebXmlDirectory);
		if (webXmlName != null) {
			b.append("/").append(webXmlName);
		}
		b.append("\n");

		b.append("  excludes {\n");
		for (String exclude : excludes) {
			b.append("    ").append(exclude).append("\n");
		}
		b.append("  }\n");

		b.append("  libs {\n");
		for (Path lib : libs) {
			b.append("    ").append(lib).append("\n");
		}
		b.append("  }\n");

		b.append("  classes {\n");
		for (Path classes : classeses) {
			b.append("    ").append(classes).append("\n");
		}
		b.append("  }\n");

		b.append("  resources {\n");
		for (Path res : resourceDirectories) {
			b.append("    ").append(res).append("\n");
		}
		b.append("  }\n");

		b.append("}\n");
		return b.toString();
	}

	@Override
	public List<Path> ingredients() {
		List<Path> ingredients = new ArrayList<>();
		ingredients.add(basedir);
		ingredients.add(webXmlOrWebXmlDirectory);
		ingredients.addAll(libs);
		ingredients.addAll(classeses);
		ingredients.addAll(resourceDirectories);
		return ingredients;
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		Project project = new Project();
		org.apache.tools.ant.taskdefs.War war = new org.apache.tools.ant.taskdefs.War();
		war.setProject(project);

		File cachedBasedir = ctx.cached(basedir);
		war.setBasedir(cachedBasedir);
		war.setDestFile(ctx.cached(this));

		File cachedWebXml = ctx.cached(webXmlOrWebXmlDirectory);
		File webXmlFile = webXmlName == null ? cachedWebXml
				: new File(cachedWebXml, webXmlName);
		war.setWebxml(webXmlFile);

		for (String exclude : excludes) {
			NameEntry ne = war.createExclude();
			ne.setName(exclude);
		}

		for (Path lib : libs) {
			File cachedLib = ctx.cached(lib);
			ZipFileSet fs = new ZipFileSet();
			fs.setFile(cachedLib);
			war.addLib(fs);
		}
		for (Path classes : classeses) {
			File cachedClasses = ctx.cached(classes);
			ZipFileSet fs = new ZipFileSet();
			fs.setDir(cachedClasses);
			war.addClasses(fs);
		}
		for (Path resourceDirectory : resourceDirectories) {
			File cachedResourceDirectory = ctx.cached(resourceDirectory);
			FileSet fs = new FileSet();
			fs.setDir(cachedResourceDirectory);
			fs.setIncludes("**/*");
			war.addFileset(fs);
		}

		war.execute();
	}

	public static WarSpex with() {
		return new WarSpex();
	}

	public static class WarSpex {

		private String name;
		private Path basedir;
		private Path webXmlOrWebXmlDirectory;
		private final List<String> excludes = new ArrayList<>();
		private final List<Path> libs = new ArrayList<>();
		private final List<Path> classeses = new ArrayList<>();
		private final List<Path> resourceDirectories = new ArrayList<>();
		private String webXmlName;

		public WarSpex name(String name) {
			this.name = name;
			return this;
		}

		public WarSpex basedir(Path basedir) {
			this.basedir = basedir;
			return this;
		}

		public WarSpex webXml(Path webXml) {
			this.webXmlOrWebXmlDirectory = webXml;
			return this;
		}

		/**
		 * TODO remove this when child paths are supported
		 */
		public WarSpex webXml(Path webXmlDirectory, String webXmlName) {
			this.webXmlOrWebXmlDirectory = webXmlDirectory;
			this.webXmlName = webXmlName;
			return this;
		}

		public WarSpex exclude(String... excludes) {
			this.excludes.addAll(Arrays.asList(excludes));
			return this;
		}

		public WarSpex libs(Path... libs) {
			return libs(Arrays.asList(libs));
		}

		public WarSpex libs(Collection<Path> libs) {
			this.libs.addAll(libs);
			return this;
		}

		public WarSpex classes(Path... classes) {
			this.classeses.addAll(Arrays.asList(classes));
			return this;
		}

		public WarSpex resourceDirectories(Path... resourceDirectories) {
			this.resourceDirectories.addAll(Arrays.asList(resourceDirectories));
			return this;
		}

		public War end() {
			return new War(name, basedir, webXmlOrWebXmlDirectory, webXmlName,
					excludes, libs, classeses, resourceDirectories);
		}

	}

}