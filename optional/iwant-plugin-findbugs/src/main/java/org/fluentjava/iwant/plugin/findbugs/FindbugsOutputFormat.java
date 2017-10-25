package net.sf.iwant.plugin.findbugs;

public enum FindbugsOutputFormat {

	EMACS, HTML, TEXT, XML, XDOCS,

	;

	@Override
	public String toString() {
		return super.toString().toLowerCase();
	}

}
