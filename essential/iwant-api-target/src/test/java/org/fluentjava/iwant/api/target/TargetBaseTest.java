package org.fluentjava.iwant.api.target;

import static org.junit.Assert.assertEquals;

import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.api.model.SystemEnv;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.api.model.TargetEvaluationContext;
import org.junit.Test;

public class TargetBaseTest {

	private static class TargetUsingEnv extends TargetBase {

		private final SystemEnv env;

		public TargetUsingEnv(SystemEnv env) {
			super("target");
			this.env = env;
		}

		@Override
		protected IngredientsAndParametersDefined ingredientsAndParameters(
				IngredientsAndParametersPlease iUse) {
			return iUse.optionalSystemEnv(env).nothingElse();
		}

		@Override
		public void path(TargetEvaluationContext ctx) throws Exception {
			throw new UnsupportedOperationException("TODO test and implement");
		}

	}

	@Test
	public void systemEnvAsIngredientsAndParameters() {
		SystemEnv env = SystemEnv.with().string("s1", "s1v")
				.path("p1", Source.underWsroot("p1v")).string("s2", "s2v")
				.path("p2", Source.underWsroot("p2v")).end();

		Target target = new TargetUsingEnv(env);

		assertEquals(
				"org.fluentjava.iwant.api.target.TargetBaseTest.TargetUsingEnv\n"
						+ "p:env:s1:\n" + "  s1v\n" + "i:env:p1:\n" + "  p1v\n"
						+ "p:env:s2:\n" + "  s2v\n" + "i:env:p2:\n" + "  p2v\n"
						+ "",
				target.contentDescriptor());
		assertEquals("[p1v, p2v]", target.ingredients().toString());
	}

	@Test
	public void nullSystemEnvAsIngredientsAndParameters() {
		Target target = new TargetUsingEnv(null);

		assertEquals(
				"org.fluentjava.iwant.api.target.TargetBaseTest.TargetUsingEnv\n"
						+ "",
				target.contentDescriptor());
		assertEquals("[]", target.ingredients().toString());
	}

}
