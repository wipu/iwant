package net.sf.iwant.entry3;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;

import net.sf.iwant.api.IwantWorkspace;
import net.sf.iwant.api.Target;
import net.sf.iwant.entry.Iwant;
import net.sf.iwant.io.StreamUtil;

public class WishEvaluator {

	private final OutputStream out;
	private final File asSomeone;

	public WishEvaluator(OutputStream out, File asSomeone) {
		this.out = out;
		this.asSomeone = asSomeone;
	}

	public void iwant(String wish, IwantWorkspace ws) {
		if ("list-of/targets".equals(wish)) {
			PrintWriter wr = new PrintWriter(out);
			for (Target target : ws.targets()) {
				wr.println(target.name());
			}
			wr.close();
			return;
		}
		for (Target target : ws.targets()) {
			if (wish.equals("target/" + target.name() + "/as-path")) {
				asPath(target);
				return;
			}
			if (wish.equals("target/" + target.name() + "/content")) {
				content(target);
				return;
			}
		}
		throw new IllegalArgumentException("Illegal wish: " + wish
				+ "\nlegal targets:" + ws.targets());
	}

	public void content(Target target) {
		StreamUtil.pipe(target.content(), out);
	}

	public void asPath(Target target) {
		File cachedTarget = new File(asSomeone, ".todo-cached/target/"
				+ target.name());
		Iwant.ensureDir(cachedTarget);
		File cachedContent = new File(cachedTarget, "content");
		try {
			target.refreshTo(cachedContent);
		} catch (Exception e) {
			throw new RuntimeException("Refresh failed", e);
		}
		PrintWriter wr = new PrintWriter(out);
		wr.println(cachedContent);
		wr.close();
	}

}
