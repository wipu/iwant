package net.sf.iwant.api.core;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.TargetEvaluationContext;
import net.sf.iwant.api.target.TargetBase;
import net.sf.iwant.coreservices.FileUtil;
import net.sf.iwant.entry.Iwant;

public class Directory extends TargetBase {

	private final Root root;

	private Directory(Root root) {
		super(root.targetName);
		this.root = root;
	}

	public static DirectoryContentPlease<Directory> named(String name) {
		return new Root(name);
	}

	public interface DirectoryContentPlease<AFTEREND> {

		AFTEREND end();

		DirectoryContentPlease<DirectoryContentPlease<AFTEREND>> dir(
				String dirName);

		FileAttributesPlease<DirectoryContentPlease<AFTEREND>> copyOf(
				Path path);

	}

	public interface FileAttributesPlease<AFTEREND> {

		/**
		 * An optional way to get back to the parent, for fluent sentences.
		 */
		AFTEREND end();

		FileAttributesPlease<AFTEREND> named(String newName);

		FileAttributesPlease<AFTEREND> executable(boolean executable);

	}

	private abstract static class DirCreator<AFTEREND>
			implements DirectoryContentPlease<AFTEREND>, FileCreator {

		private final String fullRelativePath;
		private final List<FileCreator> children = new ArrayList<>();

		DirCreator(String fullRelativePath) {
			this.fullRelativePath = fullRelativePath;
		}

		@Override
		public DirectoryContentPlease<DirectoryContentPlease<AFTEREND>> dir(
				String dirName) {
			SubDir<DirectoryContentPlease<AFTEREND>> sub = new SubDir<>(this,
					fullRelativePath + "/" + dirName);
			children.add(sub);
			return sub;
		}

		@Override
		public FileAttributesPlease<DirectoryContentPlease<AFTEREND>> copyOf(
				Path path) {
			FileCopier<DirectoryContentPlease<AFTEREND>> fileCopier = new FileCopier<>(
					path, this);
			children.add(fileCopier);
			return fileCopier;
		}

		@Override
		public void createUnder(File parent, TargetEvaluationContext ctx)
				throws Exception {
			File me = new File(parent, fullRelativePath);
			Iwant.mkdirs(me);
			for (FileCreator child : children) {
				child.createUnder(me, ctx);
			}
		}

		@Override
		public String toString() {
			return " DIR " + fullRelativePath;
		}

		@Override
		public IngredientsAndParametersPlease ingredientsAndAttributes(
				IngredientsAndParametersPlease iUse) {
			IngredientsAndParametersPlease weUse = iUse;
			weUse = weUse.parameter("fullRelativePath", fullRelativePath);
			for (FileCreator child : children) {
				weUse = child.ingredientsAndAttributes(weUse);
			}
			return weUse;
		}

	}

	private static class FileCopier<AFTEREND>
			implements FileAttributesPlease<AFTEREND>, FileCreator {

		private final Path path;
		private final AFTEREND afterEnd;
		private String nameOfCopy;
		private Boolean executable = null;

		public FileCopier(Path path, AFTEREND afterEnd) {
			this.path = path;
			this.afterEnd = afterEnd;
			this.nameOfCopy = path.name();
		}

		@Override
		public AFTEREND end() {
			return afterEnd;
		}

		@Override
		public FileAttributesPlease<AFTEREND> named(String newName) {
			this.nameOfCopy = newName;
			return this;
		}

		@Override
		public FileAttributesPlease<AFTEREND> executable(boolean executable) {
			this.executable = executable;
			return this;
		}

		@Override
		public void createUnder(File parent, TargetEvaluationContext ctx)
				throws Exception {
			File from = ctx.cached(path);
			File to = new File(parent, nameOfCopy);
			FileUtil.copyRecursively(from, to, true);
			if (executable != null) {
				to.setExecutable(executable);
			}
		}

		@Override
		public IngredientsAndParametersPlease ingredientsAndAttributes(
				IngredientsAndParametersPlease iUse) {
			return iUse.ingredients("copy-from", path)
					.parameter("copy-as", nameOfCopy)
					.parameter("executable", executable);
		}

		@Override
		public String toString() {
			return "COPY " + nameOfCopy + " <- " + path;
		}

	}

	private static class Root extends DirCreator<Directory> {

		private String targetName;

		public Root(String targetName) {
			super("");
			this.targetName = targetName;
		}

		@Override
		public Directory end() {
			return new Directory(this);
		}

	}

	private static class SubDir<AFTEREND> extends DirCreator<AFTEREND> {

		private AFTEREND afterEnd;

		SubDir(AFTEREND afterEnd, String fullRelativePath) {
			super(fullRelativePath);
			this.afterEnd = afterEnd;
		}

		@Override
		public AFTEREND end() {
			return afterEnd;
		}

	}

	private interface FileCreator {

		void createUnder(File parent, TargetEvaluationContext ctx)
				throws Exception;

		IngredientsAndParametersPlease ingredientsAndAttributes(
				IngredientsAndParametersPlease iUse);

	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	protected IngredientsAndParametersDefined ingredientsAndParameters(
			IngredientsAndParametersPlease iUse) {
		return root.ingredientsAndAttributes(iUse).nothingElse();
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		root.createUnder(ctx.cached(this), ctx);
	}

}
