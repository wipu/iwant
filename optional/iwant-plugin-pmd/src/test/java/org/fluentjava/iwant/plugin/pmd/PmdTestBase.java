package net.sf.iwant.plugin.pmd;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import net.sf.iwant.api.model.Target;
import net.sf.iwant.apimocks.IwantTestCase;
import net.sf.iwant.entry.Iwant;

public abstract class PmdTestBase extends IwantTestCase {

	protected String txtReportContent(Target report) throws IOException {
		return reportContent(report, "txt");
	}

	protected String xmlReportContent(Target report) throws IOException {
		return reportContent(report, "xml");
	}

	protected String reportContent(Target report, String extension)
			throws IOException {
		File reportFile = new File(ctx.cached(report),
				report.name() + "." + extension);
		if (!reportFile.exists()) {
			return null;
		}
		String reportFileContent = FileUtils.readFileToString(reportFile);
		return reportFileContent;
	}

	protected void srcDirHasPmdFodder(File srcDir, String lastPartOfPackage,
			String javaClassName) throws IOException {
		final String packageDirName = "net/sf/iwant/plugin/pmd/"
				+ lastPartOfPackage;
		File packageDir = new File(srcDir, packageDirName);
		Iwant.mkdirs(packageDir);

		FileUtils.copyFile(
				FileUtils.toFile(getClass().getResource(
						"/" + packageDirName + "/" + javaClassName + ".txt")),
				new File(packageDir, javaClassName + ".java"));
	}

}
