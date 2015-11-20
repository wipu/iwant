package net.sf.iwant.core.download;

import java.net.URL;

import net.sf.iwant.entry.Iwant;

public class FromRepository {

	public static ArtifactGroup ibiblio() {
		return new ArtifactGroup("http://mirrors.ibiblio.org/maven2/");
	}

	public static ArtifactGroup repo1MavenOrg() {
		return new ArtifactGroup("http://repo1.maven.org/maven2/");
	}

	public static class ArtifactGroup {

		private final String urlPrefix;
		private String group;
		private String name;

		public ArtifactGroup(String urlPrefix) {
			this.urlPrefix = urlPrefix;
		}

		public ArtifactName group(String group) {
			this.group = group.replace(".", "/");
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
				return name + "-" + version + ".jar";
			}

			private URL url(String version) {
				return Iwant.url(urlPrefix + group + "/" + name + "/" + version
						+ "/" + jarName(version));
			}

		}

	}

}
