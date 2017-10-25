package org.fluentjava.iwant.plugin.pmd;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.apimocks.IwantTestCase;
import org.fluentjava.iwant.entry.Iwant;

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
		final String packageDirName = "org/fluentjava/iwant/plugin/pmd/"
				+ lastPartOfPackage;
		File packageDir = new File(srcDir, packageDirName);
		Iwant.mkdirs(packageDir);

		FileUtils.copyFile(
				FileUtils.toFile(getClass().getResource(
						"/" + packageDirName + "/" + javaClassName + ".txt")),
				new File(packageDir, javaClassName + ".java"));
	}

}
