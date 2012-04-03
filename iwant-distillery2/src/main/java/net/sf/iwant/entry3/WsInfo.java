package net.sf.iwant.entry3;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

import net.sf.iwant.entry3.Iwant3.IwantException;

public class WsInfo {

	private final String wsName;
	private final File wsRoot;
	private final File wsdefSrc;
	private final File wsdefJava;
	private final String wsdefClass;

	public WsInfo(Reader in, File wsInfo) {
		Properties p = new Properties();
		try {
			p.load(in);
		} catch (IOException e) {
			throw new IllegalStateException("Failed to read " + wsInfo, e);
		}
		File iHave = wsInfo.getParentFile();
		wsName = property(p, wsInfo, "WSNAME");
		wsRoot = new File(iHave, property(p, wsInfo, "WSROOT"));
		wsdefSrc = new File(iHave, property(p, wsInfo, "WSDEF_SRC"));
		wsdefClass = property(p, wsInfo, "WSDEF_CLASS");
		wsdefJava = wsdefJava(wsdefSrc, wsdefClass);
	}

	private static File wsdefJava(File wsdefSrc, String wsdefClass) {
		String java = wsdefClass.replaceAll("\\.", "/") + ".java";
		return new File(wsdefSrc, java);
	}

	private static String property(Properties p, File wsInfo, String key) {
		String value = p.getProperty(key);
		if (value == null) {
			throw new IwantException("Please specify " + key + " in " + wsInfo);
		}
		return value;
	}

	public String wsName() {
		return wsName;
	}

	public File wsRoot() {
		return wsRoot;
	}

	public File wsdefSrc() {
		return wsdefSrc;
	}

	public String wsdefClass() {
		return wsdefClass;
	}

	public File wsdefJava() {
		return wsdefJava;
	}

	public String wsdefPackage() {
		return wsdefClass.substring(0, wsdefClass.lastIndexOf('.'));
	}

}
