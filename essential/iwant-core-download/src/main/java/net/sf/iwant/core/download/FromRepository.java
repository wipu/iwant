package net.sf.iwant.core.download;

import java.net.URL;

import net.sf.iwant.entry2.Iwant2;

public class FromRepository {

	public static ArtifactGroup repo1MavenOrg() {
		return new ArtifactGroup(Iwant2.REPO_MAVEN_ORG);
	}

	public static class ArtifactGroup {

		private final String urlPrefix;
		private String group;
		private String name;

		public ArtifactGroup(String urlPrefix) {
			this.urlPrefix = urlPrefix;
		}

		public ArtifactName group(String group) {
			this.group = group;
			return new ArtifactName();
		}

		public class ArtifactName {

			public ArtifactVersion name(String name) {
				ArtifactGroup.this.name = name;
				return new ArtifactVersion();
			}

		}

		public class ArtifactVersion {

			public Downloaded version(String version) {
				URL url = url(version);
				// TODO specify checksum urls when supported
				return Downloaded.withName(jarName(version)).url(url.toString())
						.noCheck();
			}

			private String jarName(String version) {
				return Iwant2.jarName(name, version);
			}

			private URL url(String version) {
				return Iwant2.urlForGnv(urlPrefix, group, name, version);
			}

		}

	}

}
