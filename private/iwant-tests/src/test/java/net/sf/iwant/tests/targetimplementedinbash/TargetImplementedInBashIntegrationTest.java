package net.sf.iwant.tests.targetimplementedinbash;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import net.sf.iwant.entry3.Iwant3;
import net.sf.iwant.entry3.Iwant3.CombinedSrcFromUnmodifiableIwantEssential;
import net.sf.iwant.entrymocks.IwantNetworkMock;
import net.sf.iwant.iwantwsrootfinder.IwantWsRootFinder;
import net.sf.iwant.testarea.TestArea;

public class TargetImplementedInBashIntegrationTest {

	private static final String LINE_SEPARATOR_KEY = "line.separator";
	private static File srcDir;
	private TestArea testArea;
	private IwantNetworkMock network;
	private Iwant3 iwant3;
	private File wsRoot;
	private File asTest;
	private InputStream originalIn;
	private PrintStream originalOut;
	private PrintStream originalErr;
	private ByteArrayOutputStream out;
	private ByteArrayOutputStream err;
	private String originalLineSeparator;
	private File iwantEssential;
	private File combinedIwantSrc;

	@BeforeClass
	public static void beforeClass() {
		File iwantWs = IwantWsRootFinder.essential().getParentFile();
		srcDir = new File(iwantWs, "private/iwant-tests/src/test/java");
	}

	@Before
	public void before() throws Exception {
		testArea = TestArea.forTest(this);
		testArea.hasFile("wsroot/as-test-developer/with/bash/iwant/help.sh",
				"#!/bin/bash\njust a mock because this exists in real life\n");
		testArea.hasFile("wsroot/as-test-developer/i-have/conf/iwant-from",
				"#just a mock because this exists in real life\n"
						+ "iwant-from=http://localhost/not-needed-here\n");
		network = new IwantNetworkMock(testArea);
		combinedIwantSrc = new File(testArea.root(), "combined-iwant-src");
		iwantEssential = IwantWsRootFinder.mockEssential();
		network.cachesAt(
				new CombinedSrcFromUnmodifiableIwantEssential(iwantEssential),
				combinedIwantSrc);
		iwant3 = Iwant3.using(network, iwantEssential);
		wsRoot = new File(testArea.root(), "wsroot");
		asTest = new File(wsRoot, "as-test-developer");

		wsInfoHasContent();
		iHaveContainsTheJavaFiles();
		iHaveContainsTheScripts();

		originalIn = System.in;
		originalOut = System.out;
		originalErr = System.err;
		originalLineSeparator = System.getProperty(LINE_SEPARATOR_KEY);
		System.setProperty(LINE_SEPARATOR_KEY, "\n");
		startOfOutAndErrCapture();
	}

	private void startOfOutAndErrCapture() {
		out = new ByteArrayOutputStream();
		err = new ByteArrayOutputStream();
		System.setOut(new PrintStream(out));
		System.setErr(new PrintStream(err));
	}

	private String out() {
		return out.toString();
	}

	private String err() {
		return err.toString();
	}

	@After
	public void after() {
		System.setIn(originalIn);
		System.setOut(originalOut);
		System.setErr(originalErr);
		System.setProperty(LINE_SEPARATOR_KEY, originalLineSeparator);

		if (!out().isEmpty()) {
			System.err.println("=== out:\n" + out());
		}
		if (!err().isEmpty()) {
			System.err.println("=== err:\n" + err());
		}
	}

	private static String wsInfoContent() {
		return "# paths are relative to this file's directory\n"
				+ "WSNAME=test\n" + "WSROOT=../../..\n"
				+ "WSDEFDEF_MODULE=../wsdefdef\n" + "WSDEFDEF_CLASS="
				+ TargetImplementedInBashWsdefdef.class.getCanonicalName()
				+ "\n";
	}

	private void wsInfoHasContent() {
		testArea.hasFile("wsroot/as-test-developer/i-have/conf/ws-info",
				wsInfoContent());
	}

	private static String fqJavaFileName(Class<?> classFromThisModule) {
		return classFromThisModule.getCanonicalName().replace(".", "/")
				+ ".java";
	}

	private void iHaveContainsTheJavaFiles() throws IOException {
		iHaveContainsJavaFile("wsdefdef",
				TargetImplementedInBashWsdefdef.class);
		iHaveContainsJavaFile("wsdef", TargetImplementedInBashWsFactory.class);
		iHaveContainsJavaFile("wsdef", TargetImplementedInBashWsdef.class);
	}

	private void iHaveContainsTheScripts()
			throws IOException, URISyntaxException {
		wsdefContainsBashFile("_index.sh");
		wsdefContainsBashFile("configurable-param.sh");
		wsdefContainsBashFile("ingredientless.sh");
		wsdefContainsBashFile("target-with-ingredients.sh");
	}

	private void iHaveContainsJavaFile(String module,
			Class<?> classFromThisModule) throws IOException {
		File destSrcDir = new File(wsRoot,
				"as-test-developer/i-have/" + module + "/src/main/java");
		File destJava = new File(destSrcDir,
				fqJavaFileName(classFromThisModule));
		FileUtils.copyFile(javaOf(classFromThisModule), destJava);
	}

	private void wsdefContainsBashFile(String scriptName)
			throws IOException, URISyntaxException {
		File destSrcDir = new File(wsRoot,
				"as-test-developer/i-have/wsdef/src/main/bash");
		File script = new File(getClass().getResource(scriptName).toURI());
		FileUtils.copyFileToDirectory(script, destSrcDir);
	}

	private static File javaOf(Class<?> classFromThisModule) {
		return new File(srcDir, fqJavaFileName(classFromThisModule));
	}

	// -------------------------------------------------
	// the tests
	// -------------------------------------------------

	@Test
	public void ingredientlessTarget() throws Exception {
		iwant3.evaluate(asTest, "target/ingredientless/as-path");

		assertEquals("hello from ingredientless\n", testArea.contentOf(
				new File(asTest, ".i-cached/target/ingredientless")));
	}

	@Test
	public void targetWithIngredients() throws Exception {
		testArea.hasFile("wsroot/src-ingr", "src-ingr content");

		iwant3.evaluate(asTest, "target/target-with-ingredients/as-path");

		assertEquals("Using PARAM=param value and " + wsRoot + "/src-ingr and "
				+ asTest + "/.i-cached/target/target-ingr to generate " + asTest
				+ "/.i-cached/target/target-with-ingredients\n" + "",
				testArea.contentOf(new File(asTest,
						".i-cached/target/target-with-ingredients")));
	}

	@Test
	public void configurableParamWithValueOne() throws Exception {
		iwant3.evaluate(asTest, "target/confparam1/as-path");

		assertEquals(
				"Using PARAM=param value 1 and " + wsRoot + "/src-ingr\n" + "",
				testArea.contentOf(
						new File(asTest, ".i-cached/target/confparam1")));
	}

	@Test
	public void configurableParamWithValueTwo() throws Exception {
		iwant3.evaluate(asTest, "target/confparam2/as-path");

		assertEquals(
				"Using PARAM=param value 2 and " + wsRoot + "/src-ingr\n" + "",
				testArea.contentOf(
						new File(asTest, ".i-cached/target/confparam2")));
	}

}
