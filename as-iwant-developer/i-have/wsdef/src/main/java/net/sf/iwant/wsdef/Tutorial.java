package net.sf.iwant.wsdef;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.model.TargetEvaluationContext;

import org.apache.commons.io.FileUtils;

public class Tutorial extends Target {

	private final Descripted bootstrappingDoc;
	private final Descripted creatingWsdefDoc;
	private final String namePrefix;
	private final List<Descripted> pages = new ArrayList<Descripted>();
	private final Path styleCss;

	private Tutorial(String namePrefix, Descripted bootstrappingDoc) {
		super(namePrefix + "tutorial");
		this.namePrefix = namePrefix;
		this.bootstrappingDoc = bootstrappingDoc;
		pages.add(bootstrappingDoc);
		this.styleCss = Source
				.underWsroot("iwant-docs/src/main/html/website/style.css");
		this.creatingWsdefDoc = new Descripted(namePrefix, "creating-wsdef",
				"Creating the workspace definition", tutorialWsdefSrc(), null,
				bootstrappingDoc);
		pages.add(this.creatingWsdefDoc);

		pages.add(pageAboutUsingWsdef("ant-cli",
				"Using ant cli instead of bash"));
		pages.add(pageAboutUsingWsdef("helloworld-with-eclipse",
				"Hello world with Eclipse"));
		pages.add(pageAboutUsingWsdef("ext-libs-in-wsdef",
				"Using external libraries in workspace definition"));
		pages.add(pageAboutUsingWsdef("antgenerated",
				"Using ant to define target content"));
		pages.add(pageAboutUsingWsdef("scriptgenerated",
				"Using a script/program define target content"));
		pages.add(pageAboutUsingWsdef("using-iwant-plugin-ant",
				"Using an iwant plugin (for untarring)"));
		pages.add(pageAboutUsingWsdef("usingmoduleinbuild",
				"Using a module of the workspace in the build"));
	}

	public static Tutorial local() {
		return new Tutorial(
				"local-",
				new Descripted(
						"",
						"bootstrapping-locally",
						"Acquiring iwant bootstrapper by svn-exporting it from a local directory",
						tutorialWsdefSrc(), Source.underWsroot(""), null));
	}

	public static Tutorial remote() {
		return new Tutorial("remote-", new Descripted("",
				"bootstrapping-with-svnexternals",
				"Acquiring iwant bootstrapper by using svn:externals",
				tutorialWsdefSrc(), null, null));
	}

	private static Source tutorialWsdefSrc() {
		return Source.underWsroot("iwant-tutorial-wsdefs/src");
	}

	private Descripted pageAboutUsingWsdef(String docName, String titleText) {
		return new Descripted(namePrefix, docName, titleText,
				tutorialWsdefSrc(), null, creatingWsdefDoc);
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
		List<Path> ingredients = new ArrayList<Path>();
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
		index.append("<link rel=\"stylesheet\" href=\"style.css\" type=\"text/css\" charset=\"utf-8\" />\n");
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

		FileUtils.writeStringToFile(new File(dest, "index.html"),
				index.toString());

		FileUtils.copyFileToDirectory(ctx.cached(styleCss), dest);
	}

	private String page(TargetEvaluationContext ctx, String fileName,
			Path fromPath, Descripted prev, Descripted next) throws IOException {
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