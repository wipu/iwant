package net.sf.iwant.wsdef;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.iwant.api.core.ScriptGenerated;
import net.sf.iwant.api.model.Path;
import net.sf.iwant.api.model.Source;
import net.sf.iwant.api.model.Target;
import net.sf.iwant.api.model.TargetEvaluationContext;
import net.sf.iwant.entry.Iwant;

public class Descripted extends Target {

	private final Source doc;
	private final Source descript;
	private final Path maybeIwantWsroot;
	private final Source abstractArticle;
	private final Descripted maybeInitialState;
	private final Path tutorialWsdefSrc;
	private final String titleText;
	private final String docName;

	public Descripted(String namePrefix, String docName, String titleText,
			Path tutorialWsdefSrc, Path maybeIwantWsroot,
			Descripted maybeInitialState) {
		super(namePrefix + docName + ".html");
		this.docName = docName;
		this.titleText = titleText;
		this.tutorialWsdefSrc = tutorialWsdefSrc;
		this.maybeIwantWsroot = maybeIwantWsroot;
		this.maybeInitialState = maybeInitialState;
		this.doc = Source
				.underWsroot("private/iwant-docs/src/main/descript/tutorial/"
						+ docName + ".sh");
		this.abstractArticle = Source
				.underWsroot("private/iwant-docs/src/main/descript/tutorial/abstract-article.sh");
		this.descript = Source.underWsroot("iwant-lib-descript/descript.sh");
	}

	public String titleText() {
		return titleText;
	}

	public String docName() {
		return docName;
	}

	@Override
	public InputStream content(TargetEvaluationContext ctx) throws Exception {
		throw new UnsupportedOperationException("TODO test and implement");
	}

	@Override
	public boolean supportsParallelism() {
		// TODO return true when known to work
		return false;
	}

	@Override
	public List<Path> ingredients() {
		List<Path> ingredients = new ArrayList<>();
		ingredients.add(doc);
		ingredients.add(abstractArticle);
		ingredients.add(descript);
		ingredients.add(tutorialWsdefSrc);
		if (maybeIwantWsroot != null) {
			ingredients.add(maybeIwantWsroot);
		}
		if (maybeInitialState != null) {
			ingredients.add(maybeInitialState);
		}
		ingredients.add(Source
				.underWsroot("as-iwant-developer/i-have/wsdef/src/main/java/"
						+ "net/sf/iwant/wsdef/Descripted.java"));
		return ingredients;
	}

	@Override
	public void path(TargetEvaluationContext ctx) throws Exception {
		File dest = ctx.cached(this);
		dest.mkdirs();

		File htmlHeader = newHtmlHeaderFile(dest);
		File htmlFooter = newHtmlFooterFile(dest);
		File htmlBodyContent = new File(dest, "body-content.html");
		File fullHtml = new File(dest, "doc.html");

		File iwantWsRoot = maybeIwantWsroot == null ? null : ctx
				.cached(maybeIwantWsroot);

		StringBuilder sh = new StringBuilder();
		sh.append("#!/bin/bash\n");
		sh.append("set -eu\n");
		sh.append("export IWANT_TUTORIAL_WSDEF_SRC="
				+ ctx.cached(tutorialWsdefSrc) + "\n");
		if (iwantWsRoot != null) {
			sh.append("export LOCAL_IWANT_WSROOT=" + iwantWsRoot + "\n");
		} else {
			sh.append("export REV_TO_TEST=775\n");
		}
		if (maybeInitialState != null) {
			File initialState = new File(ctx.cached(maybeInitialState),
					"final-state.tar");
			sh.append("export INITIAL_STATE=" + initialState + "\n");
			sh.append("export INIT_STATE_PAGE=" + maybeInitialState.docName
					+ ".html\n");
			sh.append("export INIT_STATE_TITLE='" + maybeInitialState.titleText
					+ "'\n");
		}
		sh.append("DOC=combined.sh\n");
		sh.append("cat " + ctx.cached(abstractArticle) + " " + ctx.cached(doc)
				+ " > \"$DOC\"\n");
		sh.append("echo \"export PAGETITLE='" + titleText + "'\" >> \"$DOC\"\n");
		sh.append("'" + ctx.cached(descript) + "' \"$DOC\" '" + htmlBodyContent
				+ "' false\n");
		sh.append("cat '" + htmlHeader + "' '" + htmlBodyContent + "' '"
				+ htmlFooter + "' > '" + fullHtml + "'\n");

		File script = Iwant.newTextFile(new File(dest, name() + ".sh"),
				sh.toString());
		script.setExecutable(true);
		ScriptGenerated.execute(ctx, dest, script, new String[] {});
	}

	private File newHtmlHeaderFile(File dest) {
		StringBuilder html = new StringBuilder();
		html.append("<html>\n");
		html.append("<head>\n");
		html.append("<title>" + titleText + "</title>\n");
		html.append("<link rel=\"stylesheet\" href=\"style.css\" type=\"text/css\" charset=\"utf-8\" />\n");
		html.append("<link rel=\"icon\" href=\"favicon.ico\"/>\n");
		html.append("<link rel=\"shortcut icon\" href=\"favicon.ico\"/>\n");
		html.append("</head>\n");
		html.append("<body>\n");
		appendNavigationLinkPanelPlaceholder(html);
		return Iwant
				.newTextFile(new File(dest, "header.html"), html.toString());
	}

	private static void appendNavigationLinkPanelPlaceholder(StringBuilder html) {
		html.append("NAVIGATION_LINK_PANEL_PLACEHOLDER\n");
	}

	private static File newHtmlFooterFile(File dest) {
		StringBuilder html = new StringBuilder();
		appendNavigationLinkPanelPlaceholder(html);
		html.append("</body></html>\n");
		return Iwant
				.newTextFile(new File(dest, "footer.html"), html.toString());
	}

	@Override
	public String contentDescriptor() {
		return getClass().getCanonicalName() + ":" + ingredients() + docName
				+ titleText;
	}

}
