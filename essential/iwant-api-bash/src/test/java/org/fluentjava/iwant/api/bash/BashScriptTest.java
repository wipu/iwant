package org.fluentjava.iwant.api.bash;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.fluentjava.iwant.api.core.ScriptGenerated;
import org.junit.Test;

public class BashScriptTest {

	private File resourceNamed(String name) {
		try {
			return new File(getClass().getResource(name).toURI());
		} catch (URISyntaxException e) {
			throw new IllegalStateException(e);
		}
	}

	@Test
	public void all() throws IOException, InterruptedException {
		File test = resourceNamed("all.sh");
		File functions = resourceNamed("functions.sh");
		ScriptGenerated.execute(test.getParentFile(), Arrays
				.asList(test.getCanonicalPath(), functions.getCanonicalPath()));
	}

}
