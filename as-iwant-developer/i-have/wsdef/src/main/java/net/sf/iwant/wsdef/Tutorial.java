package net.sf.iwant.wsdef;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.model.TargetEvaluationContext;

public class Tutorial extends Target {

	private final Descripted bootstrappingDoc;
	private final Descripted creatingWsdefDoc;
	private final String namePrefix;
	private final List<Descripted> pages = new ArrayList<>();
	private final Path styleCss;

	private Tutorial(String namePrefix, List<Descripted> bootstrappingDocs) {
		super(namePrefix + "tutorial");
		this.namePrefix = namePrefix;
		this.styleCss = Source.underWsroot(
				"private/iwant-docs/src/main/html/website/style.css");

		pages.add(new Descripted(namePrefix, "concepts-intro",
				"Introduction of concepts", tutorialWsdefSrc(), null, null));
		this.bootstrappingDoc = bootstrappingDocs.get(0);
		pages.addAll(bootstrappingDocs);
		this.creatingWsdefDoc = new Descripted(namePrefix, "creating-wsdef",
				"Creating the workspace definition", tutorialWsdefSrc(), null,
				bootstrappingDoc);
		pages.add(this.creatingWsdefDoc);

		pages.add(pageAboutUsingWsdef("helloworld-with-eclipse",
				"Hello world with Eclipse"));
		pages.add(pageAboutUsingWsdef("ant-cli",
				"Using ant cli instead of bash"));

		pages.add(pageAboutUsingWsdef("antgenerated",
				"Using ant to define target content"));
		pages.add(pageAboutUsingWsdef("scriptgenerated",
				"Using a script/program define target content"));

		pages.add(pageAboutUsingWsdef("using-iwant-plugin-ant",
				"Using an iwant plugin (for untarring)"));
		pages.add(pageAboutUsingWsdef("ext-libs-in-wsdef",
				"Using external libraries in workspace definition"));

		Descripted javamodulesDoc = pageAboutUsingWsdef("javamodules",
				"Defining Java modules");
		pages.add(javamodulesDoc);

		Descripted jacocoDoc = pageAboutUsing("jacoco",
				"Test coverage report using jacoco", javamodulesDoc);
		pages.add(jacocoDoc);

		pages.add(pageAboutUsing("testng", "Using TestNG instead of JUnit",
				jacocoDoc));

		pages.add(pageAboutUsingWsdef("pmdreport",
				"Static code analysis report using PMD"));
		pages.add(pageAboutUsingWsdef("findbugsreport",
				"Static code analysis report using findbugs"));
		pages.add(pageAboutUsingWsdef("using-iwant-plugin-war",
				"Defining a web archive (war)"));
		pages.add(pageAboutUsingWsdef("fromgithub", "(Code) from github"));

		pages.add(pageAboutUsingWsdef("usingmoduleinbuild",
				"Using a module of the workspace in the build"));

		pages.add(pageAboutUsingWsdef("custom-target",
				"Writing a custom Target"));

		pages.add(pageAboutUsingWsdef("ws-symlink",
				"Using a symbolic link for the workspace"));
		pages.add(new Descripted(namePrefix, "iwantmore", "I want more",
				tutorialWsdefSrc(), null, null));
	}

	public static Tutorial local(Path copyOfLocalIwantWs) {
		List<Descripted> bs = new ArrayList<>();
		bs.add(new Descripted("", "bootstrapping-locally",
				"Acquiring iwant bootstrapper by svn-exporting it from a local directory",
				tutorialWsdefSrc(), copyOfLocalIwantWs, null));
		return new Tutorial("local-", bs);
	}

	public static Tutorial remote() {
		List<Descripted> bs = new ArrayList<>();
		bs.add(new Descripted("", "bootstrapping",
				"The command line interface and bootstrapping",
				tutorialWsdefSrc(), null, null));
		bs.add(new Descripted("", "bootstrapping-with-svnexternals",
				"Alternative: acquiring iwant bootstrapper by using svn:externals",
				tutorialWsdefSrc(), null, null));
		return new Tutorial("remote-", bs);
	}

	private static Source tutorialWsdefSrc() {
		return Source.underWsroot("private/iwant-tutorial-wsdefs/src");
	}

	private Descripted pageAboutUsingWsdef(String docName, String titleText) {
		return pageAboutUsing(docName, titleText, creatingWsdefDoc);
	}

	private Descripted pageAboutUsing(String docName, String titleText,
			Descripted initialState) {
		return new Descripted(namePrefix, docName, titleText,
				tutorialWsdefSrc(), null, initialState);
	}

	private static String fileName(Descripted page) {
		return page.docName() + ".html";
	}

	private static String asLink(Descripted page) {
		return "<a href='" + fileName(page) + "'>" + page.titleText()
				+ "</a><br/>\n";
	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public List<Path> ingredients() {
		List<Path> ingredients = new ArrayList<>();
		ingredients.add(bootstrappingDoc);
		ingredients.add(creatingWsdefDoc);
		ingredients.add(styleCss);
		for (Descripted page : pages) {
			ingredients.add(page);
		}
		ingredients.add(Source
				.underWsroot("as-iwant-developer/i-have/wsdef/src/main/java/"
						+ "net/sf/iwant/wsdef/Tutorial.java"));
		return ingredients;
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		File dest = ctx.cached(this);
		dest.mkdirs();

		StringBuilder index = new StringBuilder();
		index.append("<html>\n");
		index.append("<head>\n");
		index.append("<title>iwant tutorial</title>\n");
		index.append(
				"<link rel=\"stylesheet\" href=\"style.css\" type=\"text/css\" charset=\"utf-8\" />\n");
		index.append("<link rel=\"icon\" href=\"favicon.ico\"/>\n");
		index.append("<link rel=\"shortcut icon\" href=\"favicon.ico\"/>\n");
		index.append("</head>\n");
		index.append("<body>\n");

		Descripted prev = null;
		for (int i = 0; i < pages.size(); i++) {
			Descripted page = pages.get(i);
			Descripted next = i + 1 < pages.size() ? pages.get(i + 1) : null;
			page(ctx, fileName(page), page, prev, next);
			index.append(asLink(page));
			prev = page;
		}

		index.append("</body></html>\n");

		FileUtils.writeStringToFile(new File(dest, "tutorial.html"),
				index.toString());

		FileUtils.copyFileToDirectory(ctx.cached(styleCss), dest);
	}

	private String page(TargetEvaluationContext ctx, String fileName,
			Path fromPath, Descripted prev, Descripted next)
					throws IOException {
		File dest = ctx.cached(this);
		File to = new File(dest, fileName);
		File from = new File(ctx.cached(fromPath), "doc.html");
		System.err.println(to.getName() + " <- " + from);
		String content = FileUtils.readFileToString(from);
		content = content.replaceAll("NAVIGATION_LINK_PANEL_PLACEHOLDER",
				navigationPanel(prev, next));
		FileUtils.writeStringToFile(to, content);
		return fileName;
	}

	private static String navigationPanel(Descripted prev, Descripted next) {
		StringBuilder html = new StringBuilder();
		html.append("<div>");
		if (prev != null) {
			html.append("<a href='").append(fileName(prev)).append("'>");
			html.append("<< ").append(prev.titleText());
			html.append("</a> | ");
		}
		html.append("<a href='index.html'>");
		html.append("^ Site index ^");
		html.append("</a> | ");
		html.append("<a href='tutorial.html'>");
		html.append("^ Tutorial index ^");
		html.append("</a>");
		if (next != null) {
			html.append(" | <a href='").append(fileName(next)).append("'>");
			html.append(next.titleText()).append(" >>");
			html.append("</a>");
		}
		html.append("</div>\n");
		return html.toString();
	}

	@Override
	public String contentDescriptor() {
		return getClass().getCanonicalName() + ":" + ingredients();
	}

}
