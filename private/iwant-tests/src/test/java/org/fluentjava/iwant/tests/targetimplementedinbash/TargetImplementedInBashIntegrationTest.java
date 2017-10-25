package net.sf.iwant.tests.targetimplementedinbash;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import net.sf.iwant.api.core.ScriptGenerated;
import net.sf.iwant.core.download.TestedIwantDependencies;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.UnmodifiableIwantBootstrapperClassesFromIwantWsRoot;
import net.sf.iwant.entry2.Iwant2;
import net.sf.iwant.entry2.Iwant2.ClassesFromUnmodifiableIwantEssential;
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

	@BeforeClass
	public static void beforeClass() {
		File iwantWs = IwantWsRootFinder.essential().getParentFile();
		srcDir = new File(iwantWs, "private/iwant-tests/src/test/java");
	}

	@Before
	public void before() throws Exception {
		testArea = TestArea.forTest(this);
		wsRoot = new File(testArea.root(), "wsroot");
		asTest = new File(wsRoot, "as-test-developer");
		testArea.fileHasContent(new File(asTest, "/with/bash/iwant/help.sh"),
				"#!/bin/bash\njust a mock because this exists in real life\n");
		File iwantZip = mockWsRootZip();
		URL iwantFromUrl = Iwant.fileToUrl(iwantZip);
		testArea.fileHasContent(new File(asTest, "/i-have/conf/iwant-from"),
				"#just a mock because this exists in real life\n"
						+ "iwant-from=" + iwantFromUrl + "\n");
		network = new IwantNetworkMock(testArea);

		File cachedIwantZip = network.cachesUrlAt(iwantFromUrl,
				"cached-iwant.zip");
		File cachedIwantZipUnzipped = network.cachesZipAt(
				Iwant.fileToUrl(cachedIwantZip), "iwant.zip.unzipped");
		File cachedIwantEssential = new File(cachedIwantZipUnzipped,
				"iwant-mock-wsroot/essential");
		network.cachesAt(
				new UnmodifiableIwantBootstrapperClassesFromIwantWsRoot(
						cachedIwantEssential),
				"iwant-bootstrapper-classes");
		network.cachesAt(
				new CombinedSrcFromUnmodifiableIwantEssential(
						cachedIwantEssential),
				"combined-iwant-essential-sources");
		network.cachesAt(
				new ClassesFromUnmodifiableIwantEssential(cachedIwantEssential),
				"classes-from-iwant-essential");

		network.usesRealCacheFor(
				TestedIwantDependencies.antJar().artifact().url());
		network.usesRealCacheFor(
				TestedIwantDependencies.antLauncherJar().artifact().url());

		Iwant.using(network).iwantSourceOfWishedVersion(asTest);
		Iwant2.using(network).allIwantClasses(cachedIwantEssential);

		iwant3 = Iwant3.using(network, cachedIwantEssential);

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

	/**
	 * TODO reuse, this is redundant
	 */
	private File mockWsRootZip() {
		try {
			File wsRoot = IwantWsRootFinder.mockWsRoot();
			File zip = new File(testArea.root(), "mock-iwant-wsroot.zip");
			ScriptGenerated.execute(wsRoot.getParentFile(), Arrays.asList("zip",
					"-0", "-q", "-r", zip.getAbsolutePath(), wsRoot.getName()));
			return zip;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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
