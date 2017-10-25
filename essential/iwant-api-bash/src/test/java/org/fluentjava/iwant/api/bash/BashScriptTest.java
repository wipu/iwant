package net.sf.iwant.api.bash;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.junit.Test;

import net.sf.iwant.api.core.ScriptGenerated;

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
