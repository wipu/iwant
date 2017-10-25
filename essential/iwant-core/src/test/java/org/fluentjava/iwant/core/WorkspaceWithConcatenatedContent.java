package org.fluentjava.iwant.core;

public class WorkspaceWithConcatenatedContent implements WorkspaceDefinition {

	public static class Root extends RootPath {

		public Root(Locations locations) {
			super(locations);
		}

		public Source src() {
			return source("src");
		}

		public Target<Concatenated> copyOfSrc() {
			return target("copyOfSrc").content(
					Concatenated.from().contentOf(src()).end()).end();
		}

		public Target<Concatenated> anotherTargetContentAndBytesConcatenated() {
			return target("anotherTargetContentAndBytesConcatenated").content(
					Concatenated.from().contentOf(copyOfSrc())
							.bytes('A', 'B', 'C', '\n').string("DEF\n").end())
					.end();
		}

		public Target<Concatenated> anotherTargetPathAndStringConcatenated() {
			return target("anotherTargetPathAndStringConcatenated").content(
					Concatenated.from().string("path=").pathTo(copyOfSrc())
							.end()).end();
		}

	}

	@Override
	public ContainerPath wsRoot(Locations locations) {
		return new Root(locations);
	}

}
