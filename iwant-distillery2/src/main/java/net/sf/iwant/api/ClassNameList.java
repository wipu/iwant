package net.sf.iwant.api;

import java.io.InputStream;
import java.util.List;

import net.sf.iwant.entry.Iwant;

public class ClassNameList extends Target {

	public ClassNameList(String name) {
		super(name);
	}

	public static ClassNameListSpex with() {
		return new ClassNameListSpex();
	}

	public static class ClassNameListSpex {

		private String name;

		public ClassNameListSpex name(String name) {
			this.name = name;
			return this;
		}

		public ClassNameListSpex classes(
				@SuppressWarnings("unused") JavaClasses classes) {
			return this;
		}

		public ClassNameList end() {
			return new ClassNameList(name);
		}

	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		Iwant.newTextFile(ctx.cached(this), "ATest\nBTest\n");
	}

	@Override
	public List<Path> ingredients() {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public String contentDescriptor() {
		throw new UnsupportedOperationException("TODO test and implement");
	}

}
