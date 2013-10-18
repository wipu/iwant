package net.sf.iwant.plugin.pmd;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.apimocks.CachesMock;
import net.sf.iwant.apimocks.TargetEvaluationContextMock;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.IwantNetwork;
import net.sf.iwant.testing.IwantNetworkMock;

import org.apache.commons.io.FileUtils;

public abstract class PmdTestBase extends TestCase {

	private IwantPluginPmdTestArea testArea;
	protected TargetEvaluationContextMock ctx;
	private IwantNetwork network;
	private Iwant iwant;
	protected File wsRoot;
	private File cached;
	private CachesMock caches;

	@Override
	public void setUp() {
		testArea = new IwantPluginPmdTestArea();
		network = new IwantNetworkMock(testArea);
		iwant = Iwant.using(network);
		wsRoot = new File(testArea.root(), "wsRoot");
		caches = new CachesMock(wsRoot);
		ctx = new TargetEvaluationContextMock(iwant, caches);
		ctx.hasWsRoot(wsRoot);
		cached = testArea.newDir("cached");
		caches.cachesModifiableTargetsAt(cached);
	}

	protected String reportContent(Target report, String extension)
			throws IOException {
		File reportFile = new File(ctx.cached(report), report.name() + "."
				+ extension);
		assertTrue(reportFile.exists());
		String reportFileContent = FileUtils.readFileToString(reportFile);
		return reportFileContent;
	}

	protected void srcDirHasPmdFodder(File srcDir) throws IOException {
		final String packageDirName = "net/sf/iwant/plugin/pmd/testfodder";
		File packageDir = new File(srcDir, packageDirName);
		packageDir.mkdirs();

		String javaFileName = "ClassWithPmdIssues";
		FileUtils.copyFile(
				FileUtils.toFile(getClass().getResource(
						"/" + packageDirName + "/" + javaFileName + ".txt")),
				new File(packageDir, javaFileName + ".java"));
	}

}
