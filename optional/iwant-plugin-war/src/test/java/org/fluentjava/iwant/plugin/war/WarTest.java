package org.fluentjava.iwant.plugin.war;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;

import org.fluentjava.iwant.api.model.Source;
import org.fluentjava.iwant.api.zip.Unzipped;
import org.fluentjava.iwant.apimocks.IwantTestCase;
import org.fluentjava.iwant.entry.Iwant;
import org.junit.jupiter.api.Test;

public class WarTest extends IwantTestCase {

	private File newTmpDirWithUnzippedContentOf(War war) {
		File cachedWar = new File(cached, war.name());
		File tmp = anExistingDirectory("tmp");
		Unzipped.unzipTo(cachedWar, tmp);
		return tmp;
	}

	private Source sourceWithContent(String path, String content) {
		File file = new File(wsRoot, path);
		Iwant.mkdirs(file.getParentFile());
		Iwant.newTextFile(file, content);
		return Source.underWsroot(path);
	}

	// the tests
	// ------------------

	@Test
	public void ingredientsAndDescriptorOfMinimalWar() {
		War war = War.with().name("test.war")
				.basedir(Source.underWsroot("empty-basedir"))
				.webXml(Source.underWsroot("web.xml")).end();

		assertEquals("org.fluentjava.iwant.plugin.war.War {\n"
				+ "  basedir:empty-basedir\n" + "  webxml:web.xml\n"
				+ "  excludes {\n" + "  }\n" + "  libs {\n" + "  }\n"
				+ "  classes {\n" + "  }\n" + "  resources {\n" + "  }\n"
				+ "}\n" + "", war.contentDescriptor());
		assertEquals("[empty-basedir, web.xml]", war.ingredients().toString());
	}

	@Test
	public void ingredientsAndDescriptorOfWarWithFullFeatures() {
		War war = War.with().name("test.war")
				.basedir(Source.underWsroot("empty-basedir"))
				.exclude("exclude1", "exclude2")
				.webXml(Source.underWsroot("confs"), "web.xml")
				.classes(Source.underWsroot("classes1"),
						Source.underWsroot("classes2"))
				.resourceDirectories(Source.underWsroot("res1"),
						Source.underWsroot("res2"))
				.libs(Source.underWsroot("lib/a.jar"),
						Source.underWsroot("lib/b.jar"))
				.end();

		assertEquals("org.fluentjava.iwant.plugin.war.War {\n"
				+ "  basedir:empty-basedir\n" + "  webxml:confs/web.xml\n"
				+ "  excludes {\n" + "    exclude1\n" + "    exclude2\n"
				+ "  }\n" + "  libs {\n" + "    lib/a.jar\n" + "    lib/b.jar\n"
				+ "  }\n" + "  classes {\n" + "    classes1\n"
				+ "    classes2\n" + "  }\n" + "  resources {\n" + "    res1\n"
				+ "    res2\n" + "  }\n" + "}\n" + "", war.contentDescriptor());
		assertEquals(
				"[empty-basedir, confs, lib/a.jar, lib/b.jar, classes1, classes2, res1, res2]",
				war.ingredients().toString());
	}

	@Test
	public void explicitWebXml() throws Exception {
		Source webXml = sourceWithContent("constant-web.xml",
				"web.xml content");
		File baseDir = new File(wsRoot, "empty-basedir");
		Iwant.mkdirs(baseDir);

		War war = War.with().name("test.war")
				.basedir(Source.underWsroot("empty-basedir")).webXml(webXml)
				.end();
		war.path(ctx);

		File tmp = newTmpDirWithUnzippedContentOf(war);

		assertEquals("web.xml content",
				contentOf(new File(tmp, "WEB-INF/web.xml")));
	}

	@Test
	public void webXmlUnderGivenDirectory() throws Exception {
		File generatedConfs = new File(wsRoot, "generated-confs");
		Iwant.mkdirs(generatedConfs);
		Iwant.newTextFile(new File(generatedConfs, "generated-web.xml"),
				"generated web.xml content");
		File baseDir = new File(wsRoot, "empty-basedir");
		Iwant.mkdirs(baseDir);

		War war = War.with().name("test.war")
				.basedir(Source.underWsroot("empty-basedir"))
				.webXml(Source.underWsroot("generated-confs"),
						"generated-web.xml")
				.end();
		war.path(ctx);

		File tmp = newTmpDirWithUnzippedContentOf(war);

		assertEquals("generated web.xml content",
				contentOf(new File(tmp, "WEB-INF/web.xml")));
	}

	@Test
	public void nonEmptyBasedirWithFilesToExclude() throws Exception {
		File web = new File(wsRoot, "web");
		Iwant.mkdirs(web);
		Iwant.newTextFile(new File(web, "index.html"), "index.html content");
		Iwant.newTextFile(new File(web, "file-to-exclude"),
				"file-to-exclude content");
		Iwant.newTextFile(new File(web, "subdir/another-file-to-exclude"),
				"file-to-exclude content");
		Iwant.newTextFile(new File(web, "subdir/file.html"),
				"file.html content");
		Iwant.newTextFile(new File(web, "WEB-INF/web.xml"),
				"web.xml content to exclude");
		Iwant.newTextFile(new File(wsRoot, "correct-web.xml"),
				"web.xml content to use");

		War war = War.with().name("test.war").basedir(Source.underWsroot("web"))
				.webXml(Source.underWsroot("correct-web.xml"))
				.exclude("WEB-INF/web.xml", "**/*exclude").end();
		war.path(ctx);

		File tmp = newTmpDirWithUnzippedContentOf(war);

		assertEquals("web.xml content to use",
				contentOf(new File(tmp, "WEB-INF/web.xml")));
		assertEquals("index.html content",
				contentOf(new File(tmp, "index.html")));
		assertEquals("file.html content",
				contentOf(new File(tmp, "subdir/file.html")));

		assertFalse(new File(tmp, "file-to-exclude").exists());
		assertFalse(new File(tmp, "subdir/another-file-to-exclude").exists());
	}

	@Test
	public void warWithClassesLibsAndResources() throws Exception {
		File baseDir = new File(wsRoot, "basedir");
		Iwant.mkdirs(baseDir);
		Source webXml = sourceWithContent("web.xml", "web.xml content");
		Source aJar = sourceWithContent("lib1/a.jar", "a.jar content");
		Source bJar = sourceWithContent("lib2/b.jar", "b.jar content");
		sourceWithContent("classes/A.class", "A.class content");
		sourceWithContent("classes/B.class", "B.class content");
		sourceWithContent("res/a.txt", "a.txt content");

		War war = War.with().name("test.war")
				.basedir(Source.underWsroot("basedir")).webXml(webXml)
				.classes(Source.underWsroot("classes")).libs(aJar, bJar)
				.resourceDirectories(Source.underWsroot("res")).end();
		war.path(ctx);

		File tmp = newTmpDirWithUnzippedContentOf(war);

		assertEquals("a.jar content",
				contentOf(new File(tmp, "WEB-INF/lib/a.jar")));
		assertEquals("b.jar content",
				contentOf(new File(tmp, "WEB-INF/lib/b.jar")));
		assertEquals("A.class content",
				contentOf(new File(tmp, "WEB-INF/classes/A.class")));
		assertEquals("B.class content",
				contentOf(new File(tmp, "WEB-INF/classes/B.class")));
		assertEquals("a.txt content", contentOf(new File(tmp, "a.txt")));
	}

}
