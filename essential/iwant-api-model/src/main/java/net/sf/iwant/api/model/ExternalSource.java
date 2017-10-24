package net.sf.iwant.api.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public class ExternalSource implements Path {

	private final File file;
	private String name;

	public ExternalSource(File file) {
		this.file = file;
		try {
			this.name = file.getCanonicalPath();
		} catch (IOException e) {
			throw new IllegalStateException(
					"Cannot get canonical path of " + file, e);
		}
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public boolean isNameAWorkspaceRelativePathToFreshContent() {
		return false;
	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		System.err.println("TODO content");
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public File cachedAt(CacheScopeChoices cachedAt) {
		return file;
	}

	@Override
	public List<Path> ingredients() {
		return Collections.emptyList();
	}

	@Override
	public String toString() {
		return name;
	}

}
