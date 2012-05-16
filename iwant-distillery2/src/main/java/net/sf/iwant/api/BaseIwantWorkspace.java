package net.sf.iwant.api;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;

public abstract class BaseIwantWorkspace implements IwantWorkspace {

	@Override
	public void iwant(String wish, OutputStream out) {
		PrintWriter wr = new PrintWriter(out);
		try {
			if ("list-of/targets".equals(wish)) {
				for (Object target : targets()) {
					wr.println(target);
				}
				return;
			}
			for (Object target : targets()) {
				if (wish.equals("target/" + target + "/as-path")) {
					wr.println("todo path to " + target);
					return;
				}
			}
			throw new IllegalArgumentException("Illegal wish: " + wish
					+ "\nlegal targets:" + targets());
		} finally {
			wr.close();
		}
	}

	abstract protected Collection<?> targets();

}
