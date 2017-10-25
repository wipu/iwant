package org.fluentjava.iwant.core;

public class WorkspaceWithTwoConstantTargetFiles implements WorkspaceDefinition {

	public static class Root extends RootPath {

		public Root(Locations locations) {
			super(locations);
		}

		public Target<Constant> constantOne() {
			return target("constantOne").content(
					Constant.value("constantOne content\n")).end();
		}

		public Path notATarget() {
			throw new UnsupportedOperationException("Not to be called");
		}

		public String notEvenAPath() {
			throw new UnsupportedOperationException("Not to be called");
		}

		public Target<Constant> constantTwo() {
			return target("constant2-container/constant2").content(
					Constant.value("constantTwo alias constant2 content\n"))
					.end();
		}

		public Target<?> targetButNeedsParameters(String parameter) {
			System.err.println(parameter);
			return targetButNoneOfYourBusiness();
		}

		private Target<?> targetButNoneOfYourBusiness() {
			throw new UnsupportedOperationException("Not to be called "
					+ locations);
		}

	}

	@Override
	public ContainerPath wsRoot(Locations locations) {
		return new Root(locations);
	}

}