package net.sf.iwant.api;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import junit.framework.TestCase;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.IwantNetwork;
import net.sf.iwant.entry3.CachesMock;
import net.sf.iwant.testing.IwantEntry3TestArea;
import net.sf.iwant.testing.IwantNetworkMock;

public class ClassNameListTest extends TestCase {

	private IwantEntry3TestArea testArea;
	private Iwant iwant;
	private IwantNetwork network;
	private File tmpDir;
	private CachesMock caches;
	private File wsRoot;
	private TargetEvaluationContextMock ctx;
	private File cacheDir;

	private PrintStream oldOut;

	private PrintStream oldErr;

	private ByteArrayOutputStream out;

	private ByteArrayOutputStream err;

	@Override
	public void setUp() {
		testArea = new IwantEntry3TestArea();
		network = new IwantNetworkMock(testArea);
		iwant = Iwant.using(network);
		wsRoot = testArea.root();
		caches = new CachesMock(wsRoot);
		cacheDir = testArea.newDir("cache");
		caches.cachesModifiableTargetsAt(cacheDir);
		tmpDir = new File(testArea.root(), "tmpDir");
		tmpDir.mkdirs();
		caches.providesTemporaryDirectoryAt(tmpDir);
		ctx = new TargetEvaluationContextMock(iwant, caches);

		oldOut = System.out;
		oldErr = System.err;
		out = new ByteArrayOutputStream();
		err = new ByteArrayOutputStream();
		System.setOut(new PrintStream(out));
		System.setErr(new PrintStream(err));
	}

	@Override
	protected void tearDown() throws Exception {
		System.setErr(oldErr);
		System.setOut(oldOut);
		System.err.println(err());
	}

	private String err() {
		return err.toString();
	}

	public void testGivenClassesIsIngredient() {
		Path classes = Source.underWsroot("classes");

		ClassNameList list = ClassNameList.with().name("list").classes(classes)
				.end();

		assertEquals("[classes]", list.ingredients().toString());
	}

	public void testDescriptorOfListWithJustClasses() {
		Path classes = Source.underWsroot("classes");

		ClassNameList list = ClassNameList.with().name("list").classes(classes)
				.end();

		assertEquals("net.sf.iwant.api.ClassNameList {\n"
				+ "  classes:classes\n" + "}\n", list.contentDescriptor());
	}

	public void testAllClassesFromEmptyDirectory() throws Exception {
		testArea.newDir("classes");

		ClassNameList list = ClassNameList.with().name("list")
				.classes(Source.underWsroot("classes")).end();
		list.path(ctx);

		assertEquals("", testArea.contentOf(ctx.cached(list)));
	}

	public void testAllClassesFromDirectoryWithOneClassInDefaultPackage()
			throws Exception {
		testArea.newDir("classes");
		testArea.hasFile("classes/A.class", "whatever");

		ClassNameList list = ClassNameList.with().name("list")
				.classes(Source.underWsroot("classes")).end();
		list.path(ctx);

		assertEquals("A\n", testArea.contentOf(ctx.cached(list)));
	}

	public void testAllClassesFromDirectoryWithClassInManyPackages()
			throws Exception {
		testArea.newDir("classes");
		testArea.hasFile("classes/A.class", "whatever");
		testArea.hasFile("classes/b/B.class", "whatever");
		testArea.hasFile("classes/c/subc/C.class", "whatever");

		ClassNameList list = ClassNameList.with().name("list")
				.classes(Source.underWsroot("classes")).end();
		list.path(ctx);

		assertEquals("A\nb.B\nc.subc.C\n", testArea.contentOf(ctx.cached(list)));
	}

	public void testNonClassFilesAreExcludedAutomatically() throws Exception {
		testArea.newDir("classes");
		testArea.hasFile("classes/A.notclass", "whatever");
		testArea.hasFile("classes/b/B1.class", "whatever");
		testArea.hasFile("classes/b/B2.notclass", "whatever");
		testArea.hasFile("classes/c/subc/C1.class", "whatever");
		testArea.hasFile("classes/c/subc/C2.notclass", "whatever");

		ClassNameList list = ClassNameList.with().name("list")
				.classes(Source.underWsroot("classes")).end();
		list.path(ctx);

		assertEquals("b.B1\nc.subc.C1\n", testArea.contentOf(ctx.cached(list)));
	}

}
