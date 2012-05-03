package com.example.wsdef;

import net.sf.iwant.api.IwantWorkspace;

public class Workspace implements IwantWorkspace {

	public void iwant(String wish) {
		if ("list-of/targets".equals(wish)) {
			System.out.println("hello");
		} else {
			System.out.println("todo path to hello");
		}
	}

}
