package net.sf.iwant.api.core;

import java.io.PrintWriter;

import net.sf.iwant.api.model.SideEffect;
import net.sf.iwant.api.model.SideEffectContext;

public class HelloSideEffect implements SideEffect {

	private final String name;
	private final String message;

	public HelloSideEffect(String name) {
		this(name, null);
	}

	public HelloSideEffect(String name, String message) {
		this.name = name;
		this.message = message;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public void mutate(SideEffectContext ctx) throws Exception {
		PrintWriter err = new PrintWriter(ctx.err());
		err.print(name() + " mutating.\n");
		if (message != null) {
			err.print(message);
		}
		err.close();
	}

}
