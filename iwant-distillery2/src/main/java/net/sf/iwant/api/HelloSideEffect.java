package net.sf.iwant.api;

import java.io.PrintWriter;

public class HelloSideEffect implements SideEffect {

	private final String name;

	public HelloSideEffect(String name) {
		this.name = name;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public void mutate(SideEffectContext ctx) throws Exception {
		PrintWriter err = new PrintWriter(ctx.err());
		err.print(name() + " mutating.\n");
		err.close();
	}

}
