package net.sf.iwant.core;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Checksum;
import org.apache.tools.ant.taskdefs.Get;

public class Downloaded implements Content {

	private final String from;
	private String md5;
	private String sha;

	public static Downloaded from(String from) {
		return new Downloaded(from);
	}

	public Downloaded(String from) {
		this.from = from;
	}

	public Downloaded md5(String md5) {
		this.md5 = md5;
		return this;
	}

	public Downloaded sha(String sha) {
		this.sha = sha;
		return this;
	}

	public SortedSet<Path> ingredients() {
		return new TreeSet<Path>();
	}

	public void refresh(RefreshEnvironment refresh) throws Exception {
		Get get = new Get();
		get.setSrc(new URL(from));
		get.setDest(refresh.destination());
		get.execute();

		checkIfRequired(refresh.destination(), "MD5", md5);
		checkIfRequired(refresh.destination(), "SHA", sha);
	}

	private void checkIfRequired(File destination, String algorithm,
			String checksum) throws IOException {
		if (checksum == null) {
			return;
		}
		Project project = new Project();
		Checksum checkSum = new Checksum();
		checkSum.setProject(project);
		checkSum.setFile(destination);
		checkSum.setAlgorithm(algorithm);
		checkSum.setProperty(checksum);
		checkSum.setVerifyproperty("correct");
		checkSum.execute();
		if (!"true".equals(project.getProperty("correct"))) {
			System.err.println("Checksum failed, please delete " + destination
					+ " or correct the checksum and try again.");
			throw new IOException("Checksum failed for " + destination);
		}
	}

	public String definitionDescription() {
		StringBuilder b = new StringBuilder();
		b.append(getClass().getName()).append(" {\n");
		b.append("  from:").append(from).append("\n");
		b.append("  md5:" + md5).append("\n");
		b.append("  sha:" + sha).append("\n");
		b.append("}\n");
		return b.toString();
	}

}
