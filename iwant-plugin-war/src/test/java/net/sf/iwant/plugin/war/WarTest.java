package net.sf.iwant.plugin.war;

import java.io.File;

import net.sf.iwant.api.model.Source;
import net.sf.iwant.apimocks.IwantTestCase;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.plugin.ant.Unzipped;

public class WarTest extends IwantTestCase {

	private File newTmpDirWithUnzippedContentOf(War war) {
		File cachedWar = new File(cached, war.name());
		File tmp = testArea.newDir("tmp");
		Unzipped.unzipTo(cachedWar, tmp);
		return tmp;
	}

	private Source sourceWithContent(String path, String content) {
		File file = new File(wsRoot, path);
		file.getParentFile().mkdirs();
		Iwant.newTextFile(file, content);
		return Source.underWsroot(path);
	}

	// the tests
	// ------------------

	public void testIngredientsAndDescriptorOfMinimalWar() {
		War war = War.with().name("test.war")
				.basedir(Source.underWsroot("empty-basedir"))
				.webXml(Source.underWsroot("web.xml")).end();

		assertEquals("net.sf.iwant.plugin.war.War {\n"
				+ "  basedir:empty-basedir\n" + "  webxml:web.xml\n"
				+ "  excludes {\n" + "  }\n" + "  libs {\n" + "  }\n"
				+ "  classes {\n" + "  }\n" + "  resources {\n" + "  }\n"
				+ "}\n" + "", war.contentDescriptor());
		assertEquals("[empty-basedir, web.xml]", war.ingredients().toString());
	}

	public void testIngredientsAndDescriptorOfWarWithFullFeatures() {
		War war = War
				.with()
				.name("test.war")
				.basedir(Source.underWsroot("empty-basedir"))
				.exclude("exclude1", "exclude2")
				.webXml(Source.underWsroot("confs"), "web.xml")
				.classes(Source.underWsroot("classes1"),
						Source.underWsroot("classes2"))
				.resourceDirectories(Source.underWsroot("res1"),
						Source.underWsroot("res2"))
				.libs(Source.underWsroot("lib/a.jar"),
						Source.underWsroot("lib/b.jar")).end();

		assertEquals("net.sf.iwant.plugin.war.War {\n"
				+ "  basedir:empty-basedir\n" + "  webxml:confs/web.xml\n"
				+ "  excludes {\n" + "    exclude1\n" + "    exclude2\n"
				+ "  }\n" + "  libs {\n" + "    lib/a.jar\n"
				+ "    lib/b.jar\n" + "  }\n" + "  classes {\n"
				+ "    classes1\n" + "    classes2\n" + "  }\n"
				+ "  resources {\n" + "    res1\n" + "    res2\n" + "  }\n"
				+ "}\n" + "", war.contentDescriptor());
		assertEquals(
				"[empty-basedir, confs, lib/a.jar, lib/b.jar, classes1, classes2, res1, res2]",
				war.ingredients().toString());
	}

	public void testExplicitWebXml() throws Exception {
		Source webXml = sourceWithContent("constant-web.xml", "web.xml content");
		File baseDir = new File(wsRoot, "empty-basedir");
		baseDir.mkdirs();

		War war = War.with().name("test.war")
				.basedir(Source.underWsroot("empty-basedir")).webXml(webXml)
				.end();
		war.path(ctx);

		File tmp = newTmpDirWithUnzippedContentOf(war);

		assertEquals("web.xml content",
				testArea.contentOf(new File(tmp, "WEB-INF/web.xml")));
	}

	public void testWebXmlUnderGivenDirectory() throws Exception {
		File generatedConfs = new File(wsRoot, "generated-confs");
		generatedConfs.mkdirs();
		Iwant.newTextFile(new File(generatedConfs, "generated-web.xml"),
				"generated web.xml content");
		File baseDir = new File(wsRoot, "empty-basedir");
		baseDir.mkdirs();

		War war = War
				.with()
				.name("test.war")
				.basedir(Source.underWsroot("empty-basedir"))
				.webXml(Source.underWsroot("generated-confs"),
						"generated-web.xml").end();
		war.path(ctx);

		File tmp = newTmpDirWithUnzippedContentOf(war);

		assertEquals("generated web.xml content",
				testArea.contentOf(new File(tmp, "WEB-INF/web.xml")));
	}

	public void testNonEmptyBasedirWithFilesToExclude() throws Exception {
		File web = new File(wsRoot, "web");
		web.mkdirs();
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

		War war = War.with().name("test.war")
				.basedir(Source.underWsroot("web"))
				.webXml(Source.underWsroot("correct-web.xml"))
				.exclude("WEB-INF/web.xml", "**/*exclude").end();
		war.path(ctx);

		File tmp = newTmpDirWithUnzippedContentOf(war);

		assertEquals("web.xml content to use",
				testArea.contentOf(new File(tmp, "WEB-INF/web.xml")));
		assertEquals("index.html content",
				testArea.contentOf(new File(tmp, "index.html")));
		assertEquals("file.html content",
				testArea.contentOf(new File(tmp, "subdir/file.html")));

		assertFalse(new File(tmp, "file-to-exclude").exists());
		assertFalse(new File(tmp, "subdir/another-file-to-exclude").exists());
	}

	public void testWarWithClassesLibsAndResources() throws Exception {
		File baseDir = new File(wsRoot, "basedir");
		baseDir.mkdirs();
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
				testArea.contentOf(new File(tmp, "WEB-INF/lib/a.jar")));
		assertEquals("b.jar content",
				testArea.contentOf(new File(tmp, "WEB-INF/lib/b.jar")));
		assertEquals("A.class content",
				testArea.contentOf(new File(tmp, "WEB-INF/classes/A.class")));
		assertEquals("B.class content",
				testArea.contentOf(new File(tmp, "WEB-INF/classes/B.class")));
		assertEquals("a.txt content",
				testArea.contentOf(new File(tmp, "a.txt")));
	}

}
