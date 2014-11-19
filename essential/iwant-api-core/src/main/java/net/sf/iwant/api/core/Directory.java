package net.sf.iwant.api.core;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.model.TargetEvaluationContext;
import net.sf.iwant.coreservices.FileUtil;

public class Directory extends Target {

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

		DirectoryContentPlease<AFTEREND> copyOf(Path path, String name);

		DirectoryContentPlease<AFTEREND> copyOf(Path path);

	}

	private abstract static class DirCreator<AFTEREND> implements
			DirectoryContentPlease<AFTEREND>, FileCreator {

		private final String fullRelativePath;
		private final List<FileCreator> children = new ArrayList<>();

		DirCreator(String fullRelativePath) {
			this.fullRelativePath = fullRelativePath;
		}

		@Override
		public DirectoryContentPlease<DirectoryContentPlease<AFTEREND>> dir(
				String dirName) {
			SubDir<DirectoryContentPlease<AFTEREND>> sub = new SubDir<DirectoryContentPlease<AFTEREND>>(
					this, fullRelativePath + "/" + dirName);
			children.add(sub);
			return sub;
		}

		@Override
		public DirectoryContentPlease<AFTEREND> copyOf(Path path) {
			return copyOf(path, path.name());
		}

		@Override
		public DirectoryContentPlease<AFTEREND> copyOf(final Path path,
				final String name) {
			children.add(new FileCreator() {
				@Override
				public void createUnder(File parent, TargetEvaluationContext ctx)
						throws Exception {
					File from = ctx.cached(path);
					File to = new File(parent, name);
					FileUtil.copyRecursively(from, to, true);
				}

				@Override
				public void addIngredientsTo(List<Path> ingredients) {
					ingredients.add(path);
				}

				@Override
				public void contentDescriptor(StringBuilder descr) {
					descr.append(this).append("\n");
				}

				@Override
				public String toString() {
					return "COPY " + name + " <- " + path;
				}

			});
			return this;
		}

		@Override
		public void createUnder(File parent, TargetEvaluationContext ctx)
				throws Exception {
			File me = new File(parent, fullRelativePath);
			me.mkdirs();
			for (FileCreator child : children) {
				child.createUnder(me, ctx);
			}
		}

		@Override
		public String toString() {
			return " DIR " + fullRelativePath;
		}

		@Override
		public void addIngredientsTo(List<Path> ingredients) {
			for (FileCreator child : children) {
				child.addIngredientsTo(ingredients);
			}
		}

		@Override
		public void contentDescriptor(StringBuilder descr) {
			descr.append(this).append("\n");
			for (FileCreator child : children) {
				child.contentDescriptor(descr);
			}
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

		void addIngredientsTo(List<Path> ingredients);

		void contentDescriptor(StringBuilder descr);

	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public List<Path> ingredients() {
		List<Path> ingredients = new ArrayList<>();
		root.addIngredientsTo(ingredients);
		return ingredients;
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		root.createUnder(ctx.cached(this), ctx);
	}

	@Override
	public String contentDescriptor() {
		StringBuilder descr = new StringBuilder();
		root.contentDescriptor(descr);
		return descr.toString();
	}

}
