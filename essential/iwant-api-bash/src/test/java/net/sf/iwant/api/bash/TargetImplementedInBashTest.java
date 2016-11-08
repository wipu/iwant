package net.sf.iwant.api.bash;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.iwant.api.core.HelloTarget;
import net.sf.iwant.api.javamodules.JavaSrcModule;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.wsdef.TargetDefinitionContext;
import net.sf.iwant.apimocks.IwantTestCase;
import net.sf.iwant.entry.Iwant.IwantException;

public class TargetImplementedInBashTest extends IwantTestCase {

	@Override
	protected void moreSetUp() throws Exception {
		ctx = new IKnowWhatIAmDoingContextMock(ctx);
		ctx.hasWsRoot(wsRoot);
	}

	private IKnowWhatIAmDoingContextMock ctxMock() {
		return (IKnowWhatIAmDoingContextMock) ctx;
	}

	private TargetDefinitionContext tdCtx() {
		return ctxMock();
	}

	private void prepareContext(TargetImplementedInBash target,
			Target... targetIngredients) {
		List<Target> targets = new ArrayList<>();
		targets.add(target);
		targets.addAll(Arrays.asList(targetIngredients));
		ctx.setTargets(targets);
		target.setIngredientDefinitionContext(ctx);
	}

	public void testScriptIsAnIngredient() {
		wsRootHasFile("script",
				"path() {\n" + "echo 'hello' > \"$(iwant-dest)\"\n" + "}\n");
		TargetImplementedInBash target = new TargetImplementedInBash("t",
				Source.underWsroot("script"), Arrays.asList());

		prepareContext(target);

		assertEquals(1, target.ingredients().size());
		Source ingr = (Source) target.ingredients().get(0);
		assertEquals("script", ingr.name());
	}

	public void testIngredientlessAsPath() throws Exception {
		wsRootHasFile("script",
				"path() {\n" + "echo 'hello' > \"$IWANT_DEST\"\n" + "}\n");
		TargetImplementedInBash target = new TargetImplementedInBash("t",
				Source.underWsroot("script"), Arrays.asList());

		prepareContext(target);
		target.path(ctx);

		assertEquals("hello\n", contentOfCached("t"));
	}

	public void testTargetWithTargetIngredient() throws Exception {
		Target ingr = new HelloTarget("ingr", "ingr content");
		ingr.path(ctx);

		wsRootHasFile("script",
				"ingredients() {\n" + "target-dep INGR ingr\n" + "}\n"
						+ "path() {\n"
						+ "echo \"using $INGR\" > \"$IWANT_DEST\"\n"
						+ "cat \"$INGR\" >> \"$IWANT_DEST\"\n" + "}\n");
		TargetImplementedInBash target = new TargetImplementedInBash("t",
				Source.underWsroot("script"), Arrays.asList());

		prepareContext(target, ingr);
		target.path(ctx);

		assertEquals("using " + cached + "/ingr\n" + "ingr content",
				contentOfCached("t"));
	}

	public void testTargetWithSourceIngredient() throws Exception {
		wsRootHasFile("ingr", "ingr content");

		wsRootHasFile("script",
				"ingredients() {\n" + "source-dep INGR ingr\n" + "}\n"
						+ "path() {\n"
						+ "echo \"using $INGR\" > \"$IWANT_DEST\"\n"
						+ "cat \"$INGR\" >> \"$IWANT_DEST\"\n" + "}\n");
		TargetImplementedInBash target = new TargetImplementedInBash("t",
				Source.underWsroot("script"), Arrays.asList());

		prepareContext(target);
		target.path(ctx);

		assertEquals("using " + wsRoot + "/ingr\n" + "ingr content",
				contentOfCached("t"));
	}

	public void testIndexWithNoTargetsDefined() {
		File indexSh = wsRootHasFile("_index.sh", "");

		try {
			TargetImplementedInBash.instancesFromIndexSh(tdCtx(),
					Source.underWsroot("_index.sh"));
			fail();
		} catch (IwantException e) {
			assertEquals("Script exited with non-zero status 1",
					e.getMessage());
		}
		assertEquals("--- Please define targets in " + indexSh + "\n" + "",
				err());
	}

	public void testIndexWithEmptyTargetList() {
		wsRootHasFile("_index.sh", "targets() { true; }");

		List<TargetImplementedInBash> targets = TargetImplementedInBash
				.instancesFromIndexSh(tdCtx(), Source.underWsroot("_index.sh"));

		assertEquals(0, targets.size());
	}

	public void testIndexAndSimpleTargetDirectlyInWsroot() {
		wsRootHasFile("_index.sh", "targets() { target simple; }");

		List<TargetImplementedInBash> targets = TargetImplementedInBash
				.instancesFromIndexSh(tdCtx(), Source.underWsroot("_index.sh"));

		assertEquals(1, targets.size());
		assertEquals("simple", targets.get(0).name());
		assertEquals("simple.sh", targets.get(0).script().name());
		assertEquals("[]", targets.get(0).arguments().toString());
	}

	public void testIndexAndSimpleTargetUnderSubDirectory() {
		wsRootHasFile("a/b/_index.sh", "targets() { target simple2; }");

		List<TargetImplementedInBash> targets = TargetImplementedInBash
				.instancesFromIndexSh(tdCtx(),
						Source.underWsroot("a/b/_index.sh"));

		assertEquals(1, targets.size());
		assertEquals("simple2", targets.get(0).name());
		assertEquals("a/b/simple2.sh", targets.get(0).script().name());
		assertEquals("[]", targets.get(0).arguments().toString());
	}

	public void testIndexWithTargetsWithScriptAndArgs() {
		wsRootHasFile("scripts/_index.sh",
				"targets() { target t0 t0.sh t0a0; target t1 t1.sh t1a0 t1a1; }");

		List<TargetImplementedInBash> targets = TargetImplementedInBash
				.instancesFromIndexSh(tdCtx(),
						Source.underWsroot("scripts/_index.sh"));

		assertEquals(2, targets.size());

		assertEquals("t0", targets.get(0).name());
		assertEquals("scripts/t0.sh", targets.get(0).script().name());
		assertEquals("[t0a0]", targets.get(0).arguments().toString());

		assertEquals("t1", targets.get(1).name());
		assertEquals("scripts/t1.sh", targets.get(1).script().name());
		assertEquals("[t1a0, t1a1]", targets.get(1).arguments().toString());
	}

	/**
	 * There was a bug with this
	 */
	public void testIngredientWhoseNameIsContainedInTargetsOwnName()
			throws Exception {
		Target ingr = new HelloTarget("ingr", "ingr content");
		ingr.path(ctx);

		wsRootHasFile("user-of-ingr-target.sh",
				"ingredients() {\n" + "target-dep INGR ingr\n" + "}\n"
						+ "path() {\n"
						+ "echo \"using $INGR\" > \"$IWANT_DEST\"\n"
						+ "cat \"$INGR\" >> \"$IWANT_DEST\"\n" + "}\n");
		TargetImplementedInBash target = new TargetImplementedInBash(
				"user-of-ingr-target",
				Source.underWsroot("user-of-ingr-target.sh"), Arrays.asList());

		prepareContext(target, ingr);
		target.path(ctx);

		assertEquals("using " + cached + "/ingr\n" + "ingr content",
				contentOfCached("user-of-ingr-target"));
	}

	public void testNonexistentIndexShCausesFriendlyWarning() {
		try {
			TargetImplementedInBash.instancesFromIndexSh(tdCtx(),
					Source.underWsroot("_index.sh"));
			fail();
		} catch (IwantException e) {
			assertEquals("Please define targets in " + wsRoot + "/_index.sh",
					e.getMessage());
		}

	}

	public void testNonexistentDefaultIndexShCausesFriendlyWarningWithCorrectLocation() {
		ctxMock().hasWsdefModule(JavaSrcModule.with().name("wsdef")
				.locationUnderWsRoot("wsdef-location").end());

		try {
			TargetImplementedInBash.instancesFromDefaultIndexSh(tdCtx());
			fail();
		} catch (IwantException e) {
			assertEquals(
					"Please define targets in " + wsRoot
							+ "/wsdef-location/src/main/bash/_index.sh",
					e.getMessage());
		}

	}

}
