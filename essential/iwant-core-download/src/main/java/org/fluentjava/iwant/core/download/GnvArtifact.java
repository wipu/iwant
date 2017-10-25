package org.fluentjava.iwant.core.download;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.fluentjava.iwant.api.model.CacheScopeChoices;
import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.api.model.TargetEvaluationContext;

/**
 * Be careful not to use the artifact directly since this wrapper "hijacks" its
 * identity.
 */
public class GnvArtifact<ARTIFACT extends Target> extends Target {

	private ARTIFACT artifact;
	private String urlPrefix;
	private String group;
	private String shortName;
	private String version;

	public GnvArtifact(ARTIFACT artifact, String urlPrefix, String group,
			String shortName, String version) {
		super(artifact.name());
		this.artifact = artifact;
		this.urlPrefix = urlPrefix;
		this.group = group;
		this.shortName = shortName;
		this.version = version;
	}

	public ARTIFACT artifact() {
		return artifact;
	}

	public String urlPrefix() {
		return urlPrefix;
	}

	public String group() {
		return group;
	}

	public String shortName() {
		return shortName;
	}

	public String version() {
		return version;
	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		return artifact.content(ctx);
	}

	@Override
	public boolean isNameAWorkspaceRelativePathToFreshContent() {
		return artifact.isNameAWorkspaceRelativePathToFreshContent();
	}

	@Override
	public List<Path> ingredients() {
		return artifact.ingredients();
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		artifact.path(ctx);
	}

	@Override
	public String contentDescriptor() {
		return artifact.contentDescriptor();
	}

	@Override
	public File cachedAt(CacheScopeChoices cachedAt) {
		return artifact.cachedAt(cachedAt);
	}

	@Override
	public boolean supportsParallelism() {
		return artifact.supportsParallelism();
	}

	@Override
	public boolean expectsCachedTargetMissingBeforeRefresh() {
		return artifact.expectsCachedTargetMissingBeforeRefresh();
	}

}
