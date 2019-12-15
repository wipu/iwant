package org.fluentjava.iwant.core.download;

import java.net.URL;

import org.fluentjava.iwant.entry2.Iwant2;

public class FromRepository {

	public static ArtifactGroup repo1MavenOrg() {
		return at(Iwant2.REPO_MAVEN_ORG);
	}

	public static ArtifactGroup at(String urlPrefix) {
		return new ArtifactGroup(urlPrefix);
	}

	public static class ArtifactGroup {

		private final String urlPrefix;
		private String group;
		private String name;
		private String version;

		public ArtifactGroup(String urlPrefix) {
			this.urlPrefix = urlPrefix;
		}

		public ArtifactName group(String group) {
			this.group = group.replace("/", ".");
			return new ArtifactName();
		}

		public class ArtifactName {

			public ArtifactVersion name(String name) {
				ArtifactGroup.this.name = name;
				return new ArtifactVersion();
			}

		}

		public class ArtifactVersion {

			public ArtifactType version(String version) {
				ArtifactGroup.this.version = version;
				return new ArtifactType();
			}

		}

		public class ArtifactType {

			public GnvArtifact<Downloaded> jar() {
				return artifact("");
			}

			public GnvArtifact<Downloaded> testJar() {
				return artifact("-test");
			}

			public GnvArtifact<Downloaded> sourcesJar() {
				return artifact("-sources");
			}

			public GnvArtifact<Downloaded> artifact(String classifier) {
				URL url = url(version, classifier);
				// TODO specify checksum urls when supported
				Downloaded artifact = Downloaded
						.withName(jarName(version, classifier))
						.url(url.toString()).noCheck();

				return new GnvArtifact<>(artifact, urlPrefix, group, name,
						version);
			}

			private String jarName(String version, String typeExt) {
				return Iwant2.jarName(name, version, typeExt);
			}

			private URL url(String version, String classifier) {
				return Iwant2.urlForGnv(urlPrefix, group, name, version,
						classifier);
			}

		}

	}

}
