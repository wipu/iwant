package com.example.wsdef;

import java.util.Arrays;
import java.util.Collection;

import net.sf.iwant.api.BaseIwantWorkspace;

public class Workspace extends BaseIwantWorkspace {

	@Override
	public Collection<?> targets() {
		return Arrays.asList("hello", "hello2");
	}

}
