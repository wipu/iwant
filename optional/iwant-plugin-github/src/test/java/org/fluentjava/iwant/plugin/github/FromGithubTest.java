package org.fluentjava.iwant.plugin.github;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.fluentjava.iwant.api.core.SubPath;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.api.zip.Unzipped;
import org.fluentjava.iwant.core.download.Downloaded;
import org.junit.jupiter.api.Test;

public class FromGithubTest {

	@Test
	public void itIsASubPathOfUnzippedDownloaded() {
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
