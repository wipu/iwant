package net.sf.iwant.api;

import java.io.File;
import java.io.FileWriter;

import junit.framework.TestCase;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.IwantException;
import net.sf.iwant.entry.Iwant.IwantNetwork;
import net.sf.iwant.entry.IwantNetworkMock;
import net.sf.iwant.entry3.IwantEntry3TestArea;

public class JavaClassesTargetTest extends TestCase {

	private IwantEntry3TestArea testArea;
	private TargetEvaluationContextMock ctx;
	private IwantNetwork network;
	private Iwant iwant;
	private File wsRoot;
	private File cached;

	@Override
	public void setUp() {
		testArea = new IwantEntry3TestArea();
		network = new IwantNetworkMock(testArea);
		iwant = Iwant.using(network);
		ctx = new TargetEvaluationContextMock(iwant);
		wsRoot = new File(testArea.root(), "wsRoot");
		ctx.hasWsRoot(wsRoot);
		cached = new File(testArea.root(), "cached");
		ctx.cachesModifiableTargetsAt(cached);
	}

	public void testSrcDirIsAnIgredient() throws Exception {
		Path src = Source.underWsroot("src");
		Target target = new JavaClasses("classes", src);

		assertTrue(target.ingredients().contains(src));
	}

	public void testCrapToPathFails() throws Exception {
		File srcDir = new File(wsRoot, "src");
		srcDir.mkdirs();
		new FileWriter(new File(srcDir, "Crap.java")).append("crap").close();
		Source src = Source.underWsroot("src");
		Target target = new JavaClasses("crap", src);

		try {
			target.path(ctx);
			fail();
		} catch (IwantException e) {
			assertEquals("Compilation failed.", e.getMessage());
		}
	}

	public void testValidToPathCompiles() throws Exception {
		File srcDir = new File(wsRoot, "src");
		srcDir.mkdirs();
		new FileWriter(new File(srcDir, "Valid.java")).append("class Valid {}")
				.close();
		Source src = Source.underWsroot("src");
		Target target = new JavaClasses("valid", src);

		target.path(ctx);

		assertTrue(new File(cached, "valid/Valid.class").exists());
	}

}
