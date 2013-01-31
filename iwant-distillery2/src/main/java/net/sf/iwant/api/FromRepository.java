package net.sf.iwant.api;

import java.net.URL;

import net.sf.iwant.entry.Iwant;

public class FromRepository {

	public static ArtifactGroup ibiblio() {
		return new ArtifactGroup("http://mirrors.ibiblio.org/maven2/");
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
				return Downloaded.withName(name + "-" + version)
						.url(url.toString()).noCheck();
			}

			private URL url(String version) {
				return Iwant.url(urlPrefix + group + "/" + name + "/" + version
						+ "/" + name + "-" + version + ".jar");
			}

		}

	}

}
