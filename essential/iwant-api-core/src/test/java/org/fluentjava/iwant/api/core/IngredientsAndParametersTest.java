package org.fluentjava.iwant.api.core;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.api.model.TargetEvaluationContext;
import org.fluentjava.iwant.api.target.TargetBase;
import org.fluentjava.iwant.apimocks.TargetMock;

public class IngredientsAndParametersTest {

	private Path i1s;
	private Path i2t;
	private Path i3s;
	private Path i4t;

	@Before
	public void before() {
		i1s = Source.underWsroot("i1");
		i2t = new TargetMock("i2");
		i3s = Source.underWsroot("i3");
		i4t = new TargetMock("i4");
	}

	private abstract class TestTarget extends TargetBase {

		public TestTarget() {
			super("test-target");
		}

		@Override
		public void path(TargetEvaluationContext ctx) throws Exception {
			throw new UnsupportedOperationException(
					"not to be called in this test");
		}

	}

	private class NoIngredientsOrParameters extends TestTarget {
		@Override
		protected IngredientsAndParametersDefined ingredientsAndParameters(
				IngredientsAndParametersPlease iUse) {
			return iUse.nothingElse();
		}
	}

	@Test
	public void noIngredientsOrParameters() {
		Target t = new NoIngredientsOrParameters();

		assertEquals("[]", t.ingredients().toString());
		assertEquals(
				"org.fluentjava.iwant.api.core.IngredientsAndParametersTest.NoIngredientsOrParameters\n"
						+ "",
				t.contentDescriptor());
	}

	private class SomeIngredientsAndParameters extends TestTarget {
		@Override
		protected IngredientsAndParametersDefined ingredientsAndParameters(
				IngredientsAndParametersPlease iUse) {
			return iUse.parameter("flags", "--int", 1)
					.ingredients("c-files", i1s, i2t).parameter("-W", "all")
					.ingredients("h-files", i3s, i4t).nothingElse();
		}
	}

	@Test
	public void someIngredientsAndParameters() {
		Target t = new SomeIngredientsAndParameters();

		assertEquals("[i1, i2, i3, i4]", t.ingredients().toString());
		assertEquals(
				"org.fluentjava.iwant.api.core.IngredientsAndParametersTest.SomeIngredientsAndParameters\n"
						+ "p:flags:\n" + "  --int\n" + "  1\n" + "i:c-files:\n"
						+ "  i1\n" + "  i2\n" + "p:-W:\n" + "  all\n"
						+ "i:h-files:\n" + "  i3\n" + "  i4\n" + "",
				t.contentDescriptor());
	}

	/**
	 * Newline in Path name will cause other problems (filesystems most probably
	 * don't allow that), but here we can escape that as well.
	 * 
	 * (And backslash in a filename is definitely disallowed.)
	 */
	private class EscapeTest extends TestTarget {
		@Override
		protected IngredientsAndParametersDefined ingredientsAndParameters(
				IngredientsAndParametersPlease iUse) {
			return iUse.parameter("multiline\npname", "multiline\npvalue")
					.parameter("escchar\\pname", "escchar\\pvalue")
					.ingredients("multiline\niname",
							new TargetMock("multiline\nivalue"))
					.ingredients("escchar\\iname",
							new TargetMock("escchar\\ivalue"))
					.nothingElse();
		}
	}

	@Test
	public void multiLineToStringsAreEscaped() {
		Target t = new EscapeTest();

		assertEquals("[multiline\n" + "ivalue, escchar\\ivalue]",
				t.ingredients().toString());
		assertEquals(
				"org.fluentjava.iwant.api.core.IngredientsAndParametersTest.EscapeTest\n"
						+ "p:multiline\\npname:\n" + "  multiline\\npvalue\n"
						+ "p:escchar\\\\pname:\n" + "  escchar\\\\pvalue\n"
						+ "i:multiline\\niname:\n" + "  multiline\\nivalue\n"
						+ "i:escchar\\\\iname:\n" + "  escchar\\\\ivalue\n"
						+ "",
				t.contentDescriptor());
	}

	/**
	 * Null will cause problems as a Path name, but we may as well escape it
	 * here
	 */
	private class Nulls extends TestTarget {

		@Override
		protected IngredientsAndParametersDefined ingredientsAndParameters(
				IngredientsAndParametersPlease iUse) {
			return iUse.parameter("nullp", (Object) null)
					.parameter("nullps", (List<Object>) null)
					.ingredients("nulli", (Path) null)
					.ingredients("nullis", (List<Path>) null)
					.optionalIngredients("nullois", (List<Path>) null)
					.nothingElse();
		}

	}

	@Test
	public void nulls() {
		Target t = new Nulls();

		assertEquals("[null]", t.ingredients().toString());
		assertEquals(
				"org.fluentjava.iwant.api.core.IngredientsAndParametersTest.Nulls\n"
						+ "p:nullp:\n" + " null\n" + "p:nullps:\n"
						+ " null-collection\n" + "i:nulli:\n" + " null\n"
						+ "i:nullis:\n" + " null-collection\n" + "i:nullois:\n"
						+ " null-collection\n" + "",
				t.contentDescriptor());

	}

	private class OptionalIngredients extends TestTarget {
		@Override
		protected IngredientsAndParametersDefined ingredientsAndParameters(
				IngredientsAndParametersPlease iUse) {
			return iUse.optionalIngredients("o1", null, i1s)
					.optionalIngredients("o2", i2t, null).nothingElse();
		}
	}

	@Test
	public void optionalIngredients() {
		Target t = new OptionalIngredients();

		assertEquals("[i1, i2]", t.ingredients().toString());
		assertEquals(
				"org.fluentjava.iwant.api.core.IngredientsAndParametersTest.OptionalIngredients\n"
						+ "i:o1:\n" + " null\n" + "  i1\n" + "i:o2:\n"
						+ "  i2\n" + " null\n" + "",
				t.contentDescriptor());

	}

}
