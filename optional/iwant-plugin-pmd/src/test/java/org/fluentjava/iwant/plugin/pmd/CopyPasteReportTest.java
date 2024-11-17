package org.fluentjava.iwant.plugin.pmd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;

import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.entry.Iwant;
import org.fluentjava.iwant.entry.Iwant.IwantException;
import org.junit.jupiter.api.Test;

public class CopyPasteReportTest extends PmdTestBase {

	@Test
	public void sourceDirectoriesAreIngredients() {
		Path src1 = Source.underWsroot("src1");
		Path src2 = Source.underWsroot("src2");

		CopyPasteReport report = CopyPasteReport.with().name("copypaste-report")
				.from(src1, src2).end();

		assertTrue(report.ingredients().contains(src1));
		assertTrue(report.ingredients().contains(src2));
	}

	@Test
	public void sourceDirectoriesAreInContentDescriptor() {
		Path src1 = Source.underWsroot("src-one");
		Path src2 = Source.underWsroot("src-two");

		CopyPasteReport report = CopyPasteReport.with().name("copypaste-report")
				.from(src1, src2).end();

		assertTrue(report.contentDescriptor().contains(src1.name()));
		assertTrue(report.contentDescriptor().contains(src2.name()));
	}

	@Test
	public void minimumTokenCountIsInContentDescriptor() {
		Path src = Source.underWsroot("src");

		assertTrue(CopyPasteReport.with().name("report-10").from(src).end()
				.contentDescriptor().contains("minimumTokenCount:\n  100\n"));
		assertTrue(CopyPasteReport.with().name("report-150").from(src)
				.minimumTokenCount(150).end().contentDescriptor()
				.contains("minimumTokenCount:\n  150\n"));
	}

	@Test
	public void reportOfZeroSrcDirectoriesIsAnError() throws Exception {
		File srcDir = new File(wsRoot, "src");
		Iwant.mkdirs(srcDir);

		CopyPasteReport report = CopyPasteReport.with().name("copypaste-report")
				.end();
		try {
			report.path(ctx);
			fail();
		} catch (IwantException e) {
			assertEquals("No source directories given.", e.getMessage());
		}

		assertNull(txtReportContent(report));
	}

	@Test
	public void reportOfEmptySrcDirectoryDoesProducesAnEmptyReportFile()
			throws Exception {
		File srcDir = new File(wsRoot, "src");
		Iwant.mkdirs(srcDir);

		CopyPasteReport report = CopyPasteReport.with().name("copypaste-report")
				.from(Source.underWsroot("src")).end();
		report.path(ctx);

		assertEquals("", txtReportContent(report));
	}

	@Test
	public void reportOfTwoSrcDirectoriesIsEmptyWhenMinimumTokenCountIsUndefined()
			throws Exception {
		File src1 = new File(wsRoot, "src1");
		srcDirHasPmdFodder(src1, "testfodder", "ClassWithPmdIssues");

		File src2 = new File(wsRoot, "src2");
		srcDirHasPmdFodder(src2, "testfodder2", "CopyOfClassWithPmdIssues");

		CopyPasteReport report = CopyPasteReport.with().name("copypaste-report")
				.from(Source.underWsroot("src1"), Source.underWsroot("src2"))
				.end();
		report.path(ctx);

		String txtReportContent = txtReportContent(report);
		assertEquals("", txtReportContent);
	}

	@Test
	public void reportOfTwoSrcDirectoriesMentionsRedundancyWhenMinimumTokenCountIsLow()
			throws Exception {
		File src1 = new File(wsRoot, "src1");
		srcDirHasPmdFodder(src1, "testfodder", "ClassWithPmdIssues");

		File src2 = new File(wsRoot, "src2");
		srcDirHasPmdFodder(src2, "testfodder2", "CopyOfClassWithPmdIssues");

		CopyPasteReport report = CopyPasteReport.with().name("copypaste-report")
				.from(Source.underWsroot("src1"), Source.underWsroot("src2"))
				.minimumTokenCount(10).end();
		report.path(ctx);

		String txtReportContent = txtReportContent(report);
		assertTrue(txtReportContent
				.contains("Found a 9 line (14 tokens) duplication"));
	}

	@Test
	public void reportOfTwoSrcDirectoriesIsEmptyWhenMinimumTokenCountIsHigh()
			throws Exception {
		File src1 = new File(wsRoot, "src1");
		srcDirHasPmdFodder(src1, "testfodder", "ClassWithPmdIssues");

		File src2 = new File(wsRoot, "src2");
		srcDirHasPmdFodder(src2, "testfodder2", "CopyOfClassWithPmdIssues");

		CopyPasteReport report = CopyPasteReport.with().name("copypaste-report")
				.from(Source.underWsroot("src1"), Source.underWsroot("src2"))
				.minimumTokenCount(100).end();
		report.path(ctx);

		String txtReportContent = txtReportContent(report);
		assertEquals("", txtReportContent);
	}

}
