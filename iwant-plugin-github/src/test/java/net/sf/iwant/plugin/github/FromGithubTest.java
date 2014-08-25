package net.sf.iwant.plugin.github;

import junit.framework.TestCase;
import net.sf.iwant.api.Downloaded;
import net.sf.iwant.api.SubPath;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.plugin.ant.Unzipped;

public class FromGithubTest extends TestCase {

	public void testItIsASubPathOfUnzippedDownloaded() {
		Target target = FromGithub.user("aUser").project("aProject")
				.commit("ff");

		assertEquals("aProject-code", target.name());

		SubPath sub = (SubPath) target;
		assertEquals("aProject-ff", sub.relativePath());

		Unzipped unzipped = (Unzipped) sub.parent();
		Downloaded downloaded = (Downloaded) unzipped.from();
		assertEquals("https://github.com/aUser/aProject/archive/ff.zip",
				downloaded.url().toExternalForm());
	}
}
