package org.fluentjava.iwant.entry3;

import java.util.Arrays;

import junit.framework.TestCase;
import org.fluentjava.iwant.api.core.Concatenated;
import org.fluentjava.iwant.api.core.HelloTarget;
import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.apimocks.TargetMock;
import org.fluentjava.iwant.entry.Iwant;

public class PathDefinitionConflictCheckerTest extends TestCase {

	private static void assertError(String errorMessage, Path... paths) {
		try {
			PathDefinitionConflictChecker
					.failIfConflictingPathDefinitions(Arrays.asList(paths));
			if (errorMessage != null) {
				fail("Expected failure with message:\n" + errorMessage);
			}
		} catch (Iwant.IwantException e) {
			if (errorMessage == null) {
				fail("Should not have failed, but failed:\n" + e.getMessage());
			}
			assertEquals(errorMessage, e.getMessage());
		}
	}

	public void testTwoIdenticalTargets() {
		assertError(null, new HelloTarget("a", "a"), new HelloTarget("a", "a"));
	}

	public void testTwoIdenticalTargetsWithIngredients() {
		Path p1 = Concatenated.named("a")
				.nativePathTo(Source.underWsroot("ingr")).end();
		Path p2 = Concatenated.named("a")
				.nativePathTo(Source.underWsroot("ingr")).end();

		assertError(null, p1, p2);
	}

	public void testTwoRootTargetsWithDifferentClass() {
		HelloTarget helloA = new HelloTarget("a", "a");
		Concatenated concatenatedA = Concatenated.named("a").end();

		assertError(
				"Two conflicting definitions for Path name a:\n" + "One is of\n"
						+ " class org.fluentjava.iwant.api.core.Concatenated\n"
						+ "and another is of\n"
						+ " class org.fluentjava.iwant.api.core.HelloTarget",
				helloA, concatenatedA);
	}

	public void testPathIngredientOfTargetConflictsWithAnotherTarget() {
		Path targetB = new HelloTarget("b", "b");
		Path sourceB = Source.underWsroot("b");
		Path a = Concatenated.named("a").nativePathTo(sourceB).end();

		assertError("Two conflicting definitions for Path name b:\n"
				+ "One is of\n" + " class org.fluentjava.iwant.api.core.HelloTarget\n"
				+ "and another is of\n"
				+ " class org.fluentjava.iwant.api.model.Source", a, targetB);
	}

	public void testTwoConcatenatedsHaveIngredientsOfDifferentName() {
		Path p1 = Concatenated.named("a")
				.nativePathTo(Source.underWsroot("ingr1")).end();
		Path p2 = Concatenated.named("a")
				.nativePathTo(Source.underWsroot("ingr2")).end();

		assertError("Two conflicting definitions for Path name a:\n"
				+ "One has content descriptor:\n"
				+ "org.fluentjava.iwant.api.core.Concatenated\n" + "i:native-path:\n"
				+ "  ingr2\n" + "\n" + "and another:\n"
				+ "org.fluentjava.iwant.api.core.Concatenated\n" + "i:native-path:\n"
				+ "  ingr1\n" + "", p1, p2);
	}

	public void testOneConcatenatedHasNullIngredientInsteadOfNotNull() {
		Path p1 = Concatenated.named("a")
				.nativePathTo(Source.underWsroot("ingr1")).end();
		Path p2 = Concatenated.named("a").nativePathTo(null).end();

		// check both ways so no NPE here
		String errorMessage = "Two conflicting definitions for Path name a:\n"
				+ "One has content descriptor:\n"
				+ "org.fluentjava.iwant.api.core.Concatenated\n" + "i:native-path:\n"
				+ " null\n" + "\n" + "and another:\n"
				+ "org.fluentjava.iwant.api.core.Concatenated\n" + "i:native-path:\n"
				+ "  ingr1\n" + "";
		assertError(errorMessage, p1, p2);
		assertError("Null Path", p2, p1);
	}

	public void testTwoConcatenatedsHaveDifferentNumberOfIngredients() {
		Path p1 = Concatenated.named("a")
				.nativePathTo(Source.underWsroot("common")).end();
		Path p2 = Concatenated.named("a")
				.nativePathTo(Source.underWsroot("common"))
				.nativePathTo(Source.underWsroot("p2-only")).end();

		assertError("Two conflicting definitions for Path name a:\n"
				+ "One has content descriptor:\n"
				+ "org.fluentjava.iwant.api.core.Concatenated\n" + "i:native-path:\n"
				+ "  common\n" + "i:native-path:\n" + "  p2-only\n" + "\n"
				+ "and another:\n" + "org.fluentjava.iwant.api.core.Concatenated\n"
				+ "i:native-path:\n" + "  common\n" + "", p1, p2);
	}

	public void testBuggyTargetsThatGiveIdenticalDescriptorButDifferentIngredients() {
		TargetMock a1 = new TargetMock("a");
		a1.hasContentDescriptor("a descriptor");
		a1.hasIngredients(Source.underWsroot("ingr1"));
		TargetMock a2 = new TargetMock("a");
		a2.hasContentDescriptor("a descriptor");
		a2.hasIngredients(Source.underWsroot("ingr2"));

		assertError("Two conflicting definitions for Path name a:\n"
				+ "One definition has ingredients:\n" + " 'ingr2'\n" + "\n"
				+ "while another has:\n" + " 'ingr1'\n", a1, a2);
	}

	public void testBuggyTargetsThatGiveIdenticalDescriptorButDifferentNumberOfIngredients() {
		TargetMock a1 = new TargetMock("a");
		a1.hasContentDescriptor("a descriptor");
		a1.hasIngredients(Source.underWsroot("ingr1"));
		TargetMock a2 = new TargetMock("a");
		a2.hasContentDescriptor("a descriptor");
		a2.hasIngredients(Source.underWsroot("ingr1"),
				Source.underWsroot("ingr2"));

		assertError("Two conflicting definitions for Path name a:\n"
				+ "One definition has ingredients:\n" + " 'ingr1'\n"
				+ " 'ingr2'\n" + "\n" + "while another has:\n" + " 'ingr1'\n",
				a1, a2);
	}

	public void testBuggyTargetsThatGiveIdenticalDescriptorButAnotherGivesNullIngredient() {
		TargetMock a1 = new TargetMock("a");
		a1.hasContentDescriptor("a descriptor");
		a1.hasIngredients(Source.underWsroot("ingr1"));
		TargetMock a2 = new TargetMock("a");
		a2.hasContentDescriptor("a descriptor");
		a2.hasIngredients((Path) null);

		assertError("Two conflicting definitions for Path name a:\n"
				+ "One definition has ingredients:\n" + " null\n" + "\n"
				+ "while another has:\n" + " 'ingr1'\n", a1, a2);
	}

	public void testNullName() {
		Path path = new HelloTarget(null, "");

		assertError(
				"A Path of class "
						+ "org.fluentjava.iwant.api.core.HelloTarget has null name.",
				path);
	}

}
