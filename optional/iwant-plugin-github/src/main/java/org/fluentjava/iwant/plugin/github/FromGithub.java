package org.fluentjava.iwant.plugin.github;

import org.fluentjava.iwant.api.core.SubPath;
import org.fluentjava.iwant.api.model.Path;
import org.fluentjava.iwant.api.model.Target;
import org.fluentjava.iwant.api.zip.Unzipped;
import org.fluentjava.iwant.core.download.Downloaded;

public class FromGithub {

	public static ProjectPlease user(String user) {
		return new ProjectPlease(user);
	}

	public static class ProjectPlease {

		private final String user;

		public ProjectPlease(String user) {
			this.user = user;
		}

		public CommitPlease project(String project) {
			return new CommitPlease(project);
		}

		public class CommitPlease {

			private final String project;

			public CommitPlease(String project) {
				this.project = project;
			}

			public Target commit(String commit) {
				Path zip = Downloaded.withName(project + "-code.zip")
						.url("https://github.com/" + user + "/" + project
								+ "/archive/" + commit + ".zip")
						.noCheck();
				Path zipUnzipped = Unzipped.with().name(zip + ".unzipped")
						.from(zip).end();
				Target code = new SubPath(project + "-code", zipUnzipped,
						project + "-" + commit);
				return code;
			}

		}

	}

}
