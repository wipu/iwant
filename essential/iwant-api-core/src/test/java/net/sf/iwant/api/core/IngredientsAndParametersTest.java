package net.sf.iwant.api.core;

import static org.junit.Assert.assertEquals;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.model.TargetEvaluationContext;
import net.sf.iwant.apimocks.TargetMock;

import org.junit.Before;
import org.junit.Test;

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

	private class NoIngredientsOrAttributes extends TestTarget {
		@Override
		protected IngredientsAndParametersDefined ingredientsAndAttributes(
				IngredientsAndParametersPlease iUse) {
			return iUse.nothingElse();
		}
	}

	@Test
	public void noIngredientsOrAttributes() {
		Target t = new NoIngredientsOrAttributes();

		assertEquals("[]", t.ingredients().toString());
		assertEquals(
				"net.sf.iwant.api.core.IngredientsAndParametersTest.NoIngredientsOrAttributes\n",
				t.contentDescriptor());
	}

	private class SomeIngredientsAndAttributes extends TestTarget {
		@Override
		protected IngredientsAndParametersDefined ingredientsAndAttributes(
				IngredientsAndParametersPlease iUse) {
			return iUse.parameter("flags", "--int", 1)
					.ingredients("c-files", i1s, i2t).parameter("-W", "all")
					.ingredients("h-files", i3s, i4t).nothingElse();
		}
	}

	@Test
	public void someIngredientsAndAttributes() {
		Target t = new SomeIngredientsAndAttributes();

		assertEquals("[i1, i2, i3, i4]", t.ingredients().toString());
		assertEquals(
				"net.sf.iwant.api.core.IngredientsAndParametersTest.SomeIngredientsAndAttributes\n"
						+ "p:flags:\n"
						+ "  --int\n"
						+ "  1\n"
						+ "i:c-files:\n"
						+ "  i1\n"
						+ "  i2\n"
						+ "p:-W:\n"
						+ "  all\n"
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
		protected IngredientsAndParametersDefined ingredientsAndAttributes(
				IngredientsAndParametersPlease iUse) {
			return iUse
					.parameter("multiline\npname", "multiline\npvalue")
					.parameter("escchar\\pname", "escchar\\pvalue")
					.ingredients("multiline\niname",
							new TargetMock("multiline\nivalue"))
					.ingredients("escchar\\iname",
							new TargetMock("escchar\\ivalue")).nothingElse();
		}
	}

	@Test
	public void multiLineToStringsAreEscaped() {
		Target t = new EscapeTest();

		assertEquals("[multiline\n" + "ivalue, escchar\\ivalue]", t
				.ingredients().toString());
		assertEquals(
				"net.sf.iwant.api.core.IngredientsAndParametersTest.EscapeTest\n"
						+ "p:multiline\\npname:\n" + "  multiline\\npvalue\n"
						+ "p:escchar\\\\pname:\n" + "  escchar\\\\pvalue\n"
						+ "i:multiline\\niname:\n" + "  multiline\\nivalue\n"
						+ "i:escchar\\\\iname:\n" + "  escchar\\\\ivalue\n"
						+ "", t.contentDescriptor());
	}

	/**
	 * Null will cause problems as a Path name, but we may as well escape it
	 * here
	 */
	private class Nulls extends TestTarget {

		@Override
		protected IngredientsAndParametersDefined ingredientsAndAttributes(
				IngredientsAndParametersPlease iUse) {
			return iUse.parameter("nullp", (Object) null)
					.ingredients("nulli", (Path) null).nothingElse();
		}

	}

	@Test
	public void nulls() {
		Target t = new Nulls();

		assertEquals("[null]", t.ingredients().toString());
		assertEquals(
				"net.sf.iwant.api.core.IngredientsAndParametersTest.Nulls\n"
						+ "p:nullp:\n" + " null\n" + "i:nulli:\n" + " null\n"
						+ "", t.contentDescriptor());

	}

}
